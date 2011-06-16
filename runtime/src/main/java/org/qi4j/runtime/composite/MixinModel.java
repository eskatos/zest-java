/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.mixin.MixinDescriptor;

import static org.qi4j.api.util.Iterables.map;
import static org.qi4j.api.util.Iterables.unique;

/**
 * JAVADOC
 */
public final class MixinModel
    implements Binder, MixinDescriptor, Serializable
{
    private final Class mixinClass;
    private final Class instantiationClass;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;
    private final ConcernsDeclaration concernsDeclaration;
    private final SideEffectsDeclaration sideEffectsDeclaration;
    private final Iterable<Class<?>> thisMixinTypes;

    public MixinModel( Class declaredMixinClass, Class instantiationClass )
    {
        injectedFieldsModel = new InjectedFieldsModel( declaredMixinClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredMixinClass );

        this.mixinClass = declaredMixinClass;
        this.instantiationClass = instantiationClass;
        constructorsModel = new ConstructorsModel( instantiationClass );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( declaredMixinClass, concerns );
        concernsDeclaration = new ConcernsDeclaration( concerns );
        sideEffectsDeclaration = new SideEffectsDeclaration( declaredMixinClass, Collections.<Class<?>>emptyList() );

        thisMixinTypes = buildThisMixinTypes();

/*
        mixinInvoker = new MethodInterceptor()
        {
            public Object intercept( Object obj, Method method, Object[] args, MethodProxy proxy )
                    throws Throwable
            {
                return proxy.invokeSuper( obj, args );
            }
        };
*/
    }

    public Class mixinClass()
    {
        return mixinClass;
    }

    public Class instantiationClass()
    {
        return instantiationClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( mixinClass );
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flatten( constructorsModel.dependencies(), injectedFieldsModel.dependencies(), injectedMethodsModel.dependencies() );
    }

    public <ThrowableType extends Throwable> void visitModel( ModelVisitor<ThrowableType> modelVisitor )
        throws ThrowableType
    {
        modelVisitor.visit( this );

        constructorsModel.visitModel( modelVisitor );
        injectedFieldsModel.visitModel( modelVisitor );
        injectedMethodsModel.visitModel( modelVisitor );
    }

    // Binding

    public void bind( Resolution context )
        throws BindingException
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context

    public Object newInstance( CompositeInstance compositeInstance, StateHolder state, UsesInstance uses )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        return newInstance( injectionContext );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        Object mixin;
        CompositeInstance compositeInstance = injectionContext.compositeInstance();
        try
        {
            mixin = constructorsModel.newInstance( injectionContext );

            if( FragmentClassLoader.isGenerated( instantiationClass ) )
            {
                try
                {
                    instantiationClass.getDeclaredField( "_instance" ).set( mixin,
                                                                            injectionContext.compositeInstance() );
                }
                catch( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
                catch( NoSuchFieldException e )
                {
                    e.printStackTrace();
                }
            }
        }
        catch( InvalidCompositeException e )
        {
            e.setMixinClass( mixinClass );
            throw e;
        }
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        if( mixin instanceof Initializable )
        {
            try
            {
                ( (Initializable) mixin ).initialize();
            }
            catch( InitializationException e )
            {
                Class<? extends Composite> compositeType = compositeInstance.type();
                String message = "Unable to initialize " + mixinClass + " in composite " + compositeType;
                throw new ConstructionException( message, e );
            }
        }
        return mixin;
    }

    public Iterable<Class<?>> thisMixinTypes()
    {
        return thisMixinTypes;
    }

    private Iterable<Class<?>> buildThisMixinTypes()
    {
        return map( new DependencyModel.InjectionTypeFunction(), unique( Iterables.filter( new DependencyModel.ScopeSpecification( This.class ), dependencies() ) ) );
    }

    protected FragmentInvocationHandler newInvocationHandler( Method method )
    {
        if( InvocationHandler.class.isAssignableFrom( mixinClass )
            && !method.getDeclaringClass().isAssignableFrom( mixinClass ) )
        {
            return new GenericFragmentInvocationHandler();
        }
        else
        {
            return new TypedModifierInvocationHandler();
        }
    }

    public MethodConcernsModel concernsFor( Method method, Class<? extends Composite> type, AssemblyHelper helper )
    {
        return concernsDeclaration.concernsFor( method, type, helper );
    }

    public MethodSideEffectsModel sideEffectsFor( Method method,
                                                  Class<? extends Composite> type,
                                                  AssemblyHelper helper
    )
    {
        return sideEffectsDeclaration.sideEffectsFor( method, type, helper );
    }

    @Override
    public String toString()
    {
        return mixinClass.getName();
    }

    public void activate( Object mixin )
        throws Exception
    {
        if( mixin instanceof Activatable )
        {
            ( (Activatable) mixin ).activate();
        }
    }

    public void passivate( Object mixin )
        throws Exception
    {
        if( mixin instanceof Activatable )
        {
            ( (Activatable) mixin ).passivate();
        }
    }
}
