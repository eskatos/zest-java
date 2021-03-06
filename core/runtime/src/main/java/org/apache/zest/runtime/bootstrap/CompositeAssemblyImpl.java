/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.GenericAssociationInfo;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.InvalidCompositeException;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.constraint.Constraint;
import org.apache.zest.api.constraint.ConstraintDeclaration;
import org.apache.zest.api.constraint.Constraints;
import org.apache.zest.api.constraint.Name;
import org.apache.zest.api.entity.Lifecycle;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Initializable;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.GenericPropertyInfo;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.api.type.HasTypes;
import org.apache.zest.api.util.Annotations;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.util.Fields;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.functional.HierarchicalVisitorAdapter;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.AssociationsModel;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.ManyAssociationsModel;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationsModel;
import org.apache.zest.runtime.composite.AbstractConstraintModel;
import org.apache.zest.runtime.composite.CompositeConstraintModel;
import org.apache.zest.runtime.composite.CompositeMethodModel;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.ConcernModel;
import org.apache.zest.runtime.composite.ConcernsModel;
import org.apache.zest.runtime.composite.ConstraintModel;
import org.apache.zest.runtime.composite.ConstraintsModel;
import org.apache.zest.runtime.composite.Genericpredicate;
import org.apache.zest.runtime.composite.MixinModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.composite.SideEffectModel;
import org.apache.zest.runtime.composite.SideEffectsModel;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.composite.ValueConstraintsInstance;
import org.apache.zest.runtime.composite.ValueConstraintsModel;
import org.apache.zest.runtime.injection.Dependencies;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.property.PropertiesModel;
import org.apache.zest.runtime.property.PropertyModel;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.util.Annotations.hasAnnotation;
import static org.apache.zest.api.util.Annotations.isType;
import static org.apache.zest.api.util.Annotations.type;
import static org.apache.zest.api.util.Classes.classHierarchy;
import static org.apache.zest.api.util.Classes.interfacesOf;
import static org.apache.zest.api.util.Classes.isAssignableFrom;
import static org.apache.zest.api.util.Classes.typeOf;
import static org.apache.zest.api.util.Classes.typesOf;
import static org.apache.zest.api.util.Classes.wrapperClass;
import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.first;
import static org.apache.zest.functional.Iterables.iterable;
import static org.apache.zest.runtime.legacy.Specifications.translate;

/**
 * Declaration of a Composite.
 */
public abstract class CompositeAssemblyImpl
    implements HasTypes
{
    protected List<Class<?>> concerns = new ArrayList<>();
    protected List<Class<?>> sideEffects = new ArrayList<>();
    protected List<Class<?>> mixins = new ArrayList<>();
    protected List<Class<?>> types = new ArrayList<>();
    protected MetaInfo metaInfo = new MetaInfo();
    protected Visibility visibility = Visibility.module;

    protected boolean immutable;
    protected PropertiesModel propertiesModel;
    protected StateModel stateModel;
    protected MixinsModel mixinsModel;
    protected CompositeMethodsModel compositeMethodsModel;
    private AssemblyHelper helper;
    protected StateDeclarations stateDeclarations;

    protected Set<String> registeredStateNames = new HashSet<>();

    public CompositeAssemblyImpl( Class<?> mainType )
    {
        types.add( mainType );
    }

    @Override
    public Stream<Class<?>> types()
    {
        return types.stream();
    }

    protected StateModel createStateModel()
    {
        return new StateModel( propertiesModel );
    }

    protected MixinsModel createMixinsModel()
    {
        return new MixinsModel();
    }

    protected void buildComposite( AssemblyHelper helper,
                                   StateDeclarations stateDeclarations
    )
    {
        this.stateDeclarations = stateDeclarations;
        this.helper = helper;
        for( Class<?> compositeType : types )
        {
            metaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, metaInfo );
        }

        immutable = metaInfo.get( Immutable.class ) != null;
        propertiesModel = new PropertiesModel();
        stateModel = createStateModel();
        mixinsModel = createMixinsModel();
//        compositeMethodsModel = new CompositeMethodsModel();
        compositeMethodsModel = new CompositeMethodsModel( mixinsModel );

        // Implement composite methods
        List<Class<?>> constraintClasses = toList( constraintDeclarations( getAllTypes() ) );
        List<Class<?>> concernClasses = toList( concat( concerns.stream(), concernDeclarations( getAllTypes() ) ) );
        List<Class<?>> sideEffectClasses = toList( concat( sideEffects.stream(), sideEffectDeclarations( getAllTypes() ) ) );
        List<Class<?>> mixinClasses = toList( concat( mixins.stream(), mixinDeclarations( getAllTypes() ) ) );
        //noinspection unchecked
        implementMixinType( types,
                            constraintClasses,
                            concernClasses,
                            sideEffectClasses,
                            mixinClasses
        );

        // Add state from methods and fields
        //noinspection unchecked
        addState( constraintClasses );
    }

    private List<Class<?>> toList( Stream<Class<?>> stream )
    {
        return stream.collect( Collectors.toList() );
    }

    protected void addAnnotationsMetaInfo( Class<?> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i = declaredInterfaces.length - 1; i >= 0; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[ i ], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }

    protected void implementMixinType( List<? extends Class<?>> types,
                                       List<Class<?>> constraintClasses,
                                       List<Class<?>> concernClasses,
                                       List<Class<?>> sideEffectClasses,
                                       List<Class<?>> mixinClasses
    )
    {
        Set<Class<?>> thisDependencies = new HashSet<>();
        types.forEach( mixinType -> {
            for( Method method : mixinType.getMethods() )
            {
                if( !compositeMethodsModel.isImplemented( method )
                    && !Proxy.class.equals( method.getDeclaringClass().getSuperclass() )
                    && !Proxy.class.equals( method.getDeclaringClass() )
                    && !Modifier.isStatic( method.getModifiers() ) )
                {
                    MixinModel mixinModel = implementMethod( method, mixinClasses );
                    ConcernsModel concernsModel = concernsFor(
                        method,
                        mixinModel.mixinClass(),
                        concat( concernDeclarations( mixinModel.mixinClass() ),
                                concernClasses.stream() )
                    );
                    SideEffectsModel sideEffectsModel = sideEffectsFor(
                        method,
                        mixinModel.mixinClass(),
                        concat( sideEffectDeclarations( mixinModel.mixinClass() ),
                                sideEffectClasses.stream() )
                    );
                    method.setAccessible( true );
                    ConstraintsModel constraints = constraintsFor(
                        method,
                        toList( concat( constraintDeclarations( mixinModel.mixinClass() ),
                                        constraintClasses.stream() ) )
                    );
                    CompositeMethodModel methodComposite = new CompositeMethodModel(
                        method,
                        constraints,
                        concernsModel,
                        sideEffectsModel,
                        mixinsModel
                    );

                    DependencyModel.InjectionTypeFunction injectionTypeFunction = new DependencyModel.InjectionTypeFunction();
                    DependencyModel.ScopeSpecification thisSpec = new DependencyModel.ScopeSpecification( This.class );
                    Stream<? extends Dependencies> source = Stream.of( methodComposite, mixinModel );
                    source.flatMap( Dependencies::dependencies )
                        .filter( thisSpec )
                        .map( injectionTypeFunction )
                        .forEach( thisDependencies::add );

                    interfacesOf( mixinModel.mixinClass() )
                        .map( Classes.RAW_CLASS )
                        .filter( clazz -> Stream.of( Initializable.class, Lifecycle.class, InvocationHandler.class )
                            .noneMatch( c -> c
                                .equals( clazz ) ) )
                        .forEach( thisDependencies::add );

//                    // Implement @This references
//                    Iterable<Class<?>> map = map( new DependencyModel.InjectionTypeFunction(),
//                                                  filter( new DependencyModel.ScopeSpecification( This.class ),
//                                                          methodComposite.dependencies() ) );

//                    Iterable<Class<?>> map1 = map( new DependencyModel.InjectionTypeFunction(),
//                                                   filter( new DependencyModel.ScopeSpecification( This.class ),
//                                                           mixinModel.dependencies() ) );

//                    @SuppressWarnings( "unchecked" )
//                    Iterable<Class<?>> filter = filter(
//                        not( in( Initializable.class, Lifecycle.class, InvocationHandler.class ) ),
//                        map( Classes.RAW_CLASS, interfacesOf( mixinModel.mixinClass() ) )
//                    );
//                    Iterable<? extends Class<?>> flatten = flatten( map, map1, filter );
//                    addAll( thisDependencies, flatten );

                    compositeMethodsModel.addMethod( methodComposite );
                }
            }
            // Add type to set of mixin types
            mixinsModel.addMixinType( mixinType );
        } );

        // Implement all @This dependencies that were found
        thisDependencies.forEach( thisDependency -> {
            // Add additional declarations from the @This type
            Stream<Class<?>> typeConstraintClasses = concat(
                constraintClasses.stream(),
                constraintDeclarations( thisDependency ) );
            Stream<Class<?>> typeConcernClasses = concat(
                concernClasses.stream(),
                concernDeclarations( thisDependency ) );
            Stream<Class<?>> typeSideEffectClasses = concat(
                sideEffectClasses.stream(),
                sideEffectDeclarations( thisDependency ) );
            Stream<Class<?>> typeMixinClasses = concat(
                mixinClasses.stream(),
                mixinDeclarations( thisDependency ) );
            List<? extends Class<?>> singleton = Collections.singletonList( thisDependency );
            implementMixinType( singleton,
                                toList( typeConstraintClasses ),
                                toList( typeConcernClasses ),
                                toList( typeSideEffectClasses ),
                                toList( typeMixinClasses )
            );
        } );
    }

    @SuppressWarnings( "raw" )
    protected MixinModel implementMethod( Method method, List<Class<?>> mixinDeclarations )
    {
        MixinModel implementationModel = mixinsModel.mixinFor( method );
        if( implementationModel != null )
        {
            return implementationModel;
        }
        Class mixinClass = findTypedImplementation( method, mixinDeclarations.stream() );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check generic implementations
        mixinClass = findGenericImplementation( method, mixinDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        throw new InvalidCompositeException( "No implementation found for method \n    " + method.toGenericString()
                                             + "\nin\n    " + types );
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    private Class<?> findTypedImplementation( final Method method, Stream<Class<?>> mixins )
    {
        // Check if mixinClass implements the method. If so, check if the mixinClass is generic or if the filter passes.
        // If a mixinClass is both generic AND non-generic at the same time, then the filter applies to the non-generic
        // side only.
        Predicate<Class<?>> appliesToSpec = item -> helper.appliesTo( item, method, types, item );
        return mixins
            .filter( isAssignableFrom( method.getDeclaringClass() )
                         .and( Genericpredicate.INSTANCE
                                   .or( appliesToSpec ) ) )
            .findFirst().orElse( null );
    }

    @SuppressWarnings( "unchecked" )
    private Class<?> findGenericImplementation( final Method method, Iterable<Class<?>> mixins )
    {
        // Check if mixinClass is generic and the applies-to filter passes
        return first( filter( Genericpredicate.INSTANCE
                                  .and( item -> helper.appliesTo( item, method, types, item ) ), mixins ) );
    }

    private MixinModel implementMethodWithClass( Method method, Class mixinClass )
    {
        MixinModel mixinModel = mixinsModel.getMixinModel( mixinClass );
        if( mixinModel == null )
        {
            mixinModel = helper.getMixinModel( mixinClass );
            mixinsModel.addMixinModel( mixinModel );
        }

        mixinsModel.addMethodMixin( method, mixinModel );

        return mixinModel;
    }

    protected void addState( final List<Class<?>> constraintClasses )
    {
        // Add method state
        compositeMethodsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof CompositeMethodModel )
                {
                    CompositeMethodModel methodModel = (CompositeMethodModel) visited;
                    if( methodModel.method().getParameterTypes().length == 0 )
                    {
                        addStateFor( methodModel.method(), constraintClasses );
                    }

                    return false;
                }

                return super.visitEnter( visited );
            }
        } );

        // Add field state
        mixinsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof MixinModel )
                {
                    MixinModel model = (MixinModel) visited;
                    Consumer<Field> addState = field -> addStateFor( field, constraintClasses );
                    Fields.FIELDS_OF.apply( model.mixinClass() )
                        .filter( Annotations.hasAnnotation( State.class ) )
                        .forEach( addState );
                    return false;
                }
                return super.visitEnter( visited );
            }
        } );
    }

    protected void addStateFor( AccessibleObject accessor, List<Class<?>> constraintClasses )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if( registeredStateNames.contains( stateName ) )
        {
            return; // Skip already registered names
        }

        Class<?> accessorType = Classes.RAW_CLASS.apply( typeOf( accessor ) );
        if( Property.class.isAssignableFrom( accessorType ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( Association.class.isAssignableFrom( accessorType ) )
        {
            associationsModel().addAssociation( newAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( ManyAssociation.class.isAssignableFrom( accessorType ) )
        {
            manyAssociationsModel().addManyAssociation( newManyAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( NamedAssociation.class.isAssignableFrom( accessorType ) )
        {
            namedAssociationsModel().addNamedAssociation( newNamedAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    protected AssociationsModel associationsModel()
    {
        return null;
    }

    protected ManyAssociationsModel manyAssociationsModel()
    {
        return null;
    }

    protected NamedAssociationsModel namedAssociationsModel()
    {
        return null;
    }

    protected PropertyModel newPropertyModel( AccessibleObject accessor,
                                              List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );
        ValueConstraintsModel valueConstraintsModel = constraintsFor(
            annotations.stream(),
            GenericPropertyInfo.propertyTypeOf( accessor ),
            ( (Member) accessor ).getName(),
            optional,
            constraintClasses,
            accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        UseDefaults useDefaultsDeclaration = metaInfo.get( UseDefaults.class );
        Object initialValue = stateDeclarations.initialValueOf( accessor );
        if( initialValue == null && useDefaultsDeclaration != null )
        {
            initialValue = useDefaultsDeclaration.value();
        }
        boolean useDefaults = useDefaultsDeclaration != null || stateDeclarations.useDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        return new PropertyModel(
            accessor,
            immutable,
            useDefaults,
            valueConstraintsInstance,
            metaInfo,
            initialValue
        );
    }

    // Model
    private ConstraintsModel constraintsFor( Method method,
                                             List<Class<?>> constraintClasses
    )
    {
        List<ValueConstraintsModel> parameterConstraintModels = Collections.emptyList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameterAnnotations.length; i++ )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ i ];

            Name nameAnnotation = (Name) first( filter( isType( Name.class ), iterable( parameterAnnotation ) ) );
            String name = nameAnnotation == null ? "param" + ( i + 1 ) : nameAnnotation.value();

            boolean optional = first( filter( isType( Optional.class ), iterable( parameterAnnotation ) ) ) != null;
            ValueConstraintsModel parameterConstraintsModel = constraintsFor(
                Arrays.stream( parameterAnnotation ),
                parameterTypes[ i ],
                name,
                optional,
                constraintClasses,
                method );
            if( parameterConstraintsModel.isConstrained() )
            {
                constrained = true;
            }

            if( parameterConstraintModels.isEmpty() )
            {
                parameterConstraintModels = new ArrayList<>();
            }
            parameterConstraintModels.add( parameterConstraintsModel );
        }

        if( !constrained )
        {
            return new ConstraintsModel( Collections.<ValueConstraintsModel>emptyList() );
        }
        else
        {
            return new ConstraintsModel( parameterConstraintModels );
        }
    }

    protected ValueConstraintsModel constraintsFor(
        Stream<Annotation> constraintAnnotations,
        Type valueType,
        String name,
        boolean optional,
        Iterable<Class<?>> constraintClasses,
        AccessibleObject accessor
    )
    {
        valueType = wrapperClass( valueType );

        List<AbstractConstraintModel> constraintModels = new ArrayList<>();
        List<Annotation> filtered = constraintAnnotations
            .filter( translate( type(), hasAnnotation( ConstraintDeclaration.class ) ) )
            .collect( Collectors.toList() );

        // TODO: This massive block below should be cleaned up.
        nextConstraint:
        for( Annotation constraintAnnotation : filtered )
        {
            // Check composite declarations first
            Class<? extends Annotation> annotationType = constraintAnnotation.annotationType();
            for( Class<?> constraint : constraintClasses )
            {
                Class<? extends Constraint<?, ?>> constraintType = (Class<? extends Constraint<?, ?>>) constraint;
                if( helper.appliesTo( constraintType, annotationType, valueType ) )
                {
                    constraintModels.add( new ConstraintModel( constraintAnnotation, constraintType ) );
                    continue nextConstraint;
                }
            }

            // Check the annotation itself
            Constraints constraints = annotationType.getAnnotation( Constraints.class );
            if( constraints != null )
            {
                for( Class<? extends Constraint<?, ?>> constraintClass : constraints.value() )
                {
                    if( helper.appliesTo( constraintClass, annotationType, valueType ) )
                    {
                        constraintModels.add( new ConstraintModel( constraintAnnotation, constraintClass ) );
                        continue nextConstraint;
                    }
                }
            }

            // No implementation found!
            // Check if if it's a composite constraints
            if( Arrays.stream( annotationType.getAnnotations() )
                .anyMatch( translate( type(), hasAnnotation( ConstraintDeclaration.class ) ) ) )
            {
                ValueConstraintsModel valueConstraintsModel = constraintsFor(
                    Arrays.stream( annotationType.getAnnotations() ),
                    valueType,
                    name,
                    optional,
                    constraintClasses,
                    accessor );
                CompositeConstraintModel compositeConstraintModel = new CompositeConstraintModel(
                    constraintAnnotation,
                    valueConstraintsModel );
                constraintModels.add( compositeConstraintModel );
            }
            else
            {
                throw new InvalidCompositeException(
                    "Cannot find implementation of constraint @"
                    + annotationType.getSimpleName()
                    + " for "
                    + valueType
                    + " in method "
                    + ( (Member) accessor ).getName()
                    + " of composite " + types );
            }
        }
        return new ValueConstraintsModel( constraintModels, name, optional );
    }

    private ConcernsModel concernsFor( Method method,
                                       Class<?> mixinClass,
                                       Stream<Class<?>> concernClasses
    )
    {
        List<ConcernModel> concernsFor = new ArrayList<>();
        concernClasses.forEach( concern -> {
            if( helper.appliesTo( concern, method, types, mixinClass ) )
            {
                addConcernOrRepositionIfExists( concernsFor, helper.getConcernModel( concern ) );
            }
            else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( concern, mixinMethod, types, mixinClass ) )
                        {
                            addConcernOrRepositionIfExists( concernsFor, helper.getConcernModel( concern ) );
                        }
                    }
                    catch( NoSuchMethodException e )
                    {
                        // Ignore
                    }
                }
            }
        } );

        // Check annotations on method that have @Concerns annotations themselves
        for( Annotation annotation : method.getAnnotations() )
        {
            @SuppressWarnings( "raw" )
            Concerns concerns = annotation.annotationType().getAnnotation( Concerns.class );
            if( concerns != null )
            {
                for( Class<?> concern : concerns.value() )
                {
                    if( helper.appliesTo( concern, method, types, mixinClass ) )
                    {
                        ConcernModel concernModel = helper.getConcernModel( concern );
                        addConcernOrRepositionIfExists( concernsFor, concernModel );
                    }
                }
            }
        }

        if( concernsFor.isEmpty() )
        {
            return ConcernsModel.EMPTY_CONCERNS;
        }
        else
        {
            return new ConcernsModel( concernsFor );
        }
    }

    private void addConcernOrRepositionIfExists( List<ConcernModel> concernsFor, ConcernModel concernModel )
    {
        // This remove/add is to allow re-ordering of the concerns
        concernsFor.remove( concernModel );
        concernsFor.add( concernModel );
    }

    private SideEffectsModel sideEffectsFor( Method method,
                                             Class<?> mixinClass,
                                             Stream<Class<?>> sideEffectClasses
    )
    {
        List<SideEffectModel> sideEffectsFor = new ArrayList<>();
        sideEffectClasses.forEach( sideEffect -> {
            SideEffectModel sideEffectModel = helper.getSideEffectModel( sideEffect );
            if( helper.appliesTo( sideEffect, method, types, mixinClass ) )
            {
                addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
            }
            else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( sideEffect, mixinMethod, types, mixinClass ) )
                        {
                            addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
                        }
                    }
                    catch( NoSuchMethodException e )
                    {
                        // Ignore
                    }
                }
            }
        } );

        // Check annotations on method that have @Concerns annotations themselves
        for( Annotation annotation : method.getAnnotations() )
        {
            @SuppressWarnings( "raw" )
            SideEffects sideEffects = annotation.annotationType().getAnnotation( SideEffects.class );
            if( sideEffects != null )
            {
                for( Class<?> sideEffect : sideEffects.value() )
                {
                    if( helper.appliesTo( sideEffect, method, types, mixinClass ) )
                    {
                        SideEffectModel sideEffectModel = helper.getSideEffectModel( sideEffect );
                        addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
                    }
                }
            }
        }

        if( sideEffectsFor.isEmpty() )
        {
            return SideEffectsModel.EMPTY_SIDEEFFECTS;
        }
        else
        {
            return new SideEffectsModel( sideEffectsFor );
        }
    }

    private void addSideEffectOrRepositionIfExists( List<SideEffectModel> sideEffectsFor,
                                                    SideEffectModel sideEffectModel
    )
    {
        // This add/remove is to allow reording of SideEffects.
        sideEffectsFor.remove( sideEffectModel );
        sideEffectsFor.add( sideEffectModel );
    }

    @SuppressWarnings( "unchecked" )
    private Stream<Class<?>> constraintDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return constraintDeclarations( types );
    }

    private Stream<Class<?>> constraintDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, Constraints.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, Constraints.class ).value() ) );
    }

    @SuppressWarnings( "unchecked" )
    private Stream<Class<?>> concernDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return concernDeclarations( types );
    }

    private Stream<Class<?>> concernDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, Concerns.class ) != null )
            .flatMap( new Function<Type, Stream<? extends Class<?>>>()
            {
                @Override
                public Stream<? extends Class<?>> apply( Type mixinType )
                {
                    return Arrays.stream( Annotations.annotationOn( mixinType, Concerns.class ).value() );
                }
            } );
    }

    @SuppressWarnings( "unchecked" )
    protected Stream<Class<?>> sideEffectDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return sideEffectDeclarations( types );
    }

    private Stream<Class<?>> sideEffectDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, SideEffects.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, SideEffects.class ).value() ) );
    }

    protected Stream<Class<?>> mixinDeclarations( Class<?> type )
    {
        //Stream<? extends Type> types = typesOf( type );
        return mixinDeclarations( Stream.of( type ) );
    }

    private Stream<Class<?>> mixinDeclarations( Stream<? extends Class> types )
    {
        return types.flatMap( this::getTypes ).flatMap( Classes::typesOf )
            .filter( mixinType -> Annotations.annotationOn( mixinType, Mixins.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, Mixins.class ).value() ) );
    }

    private Stream<Class> getAllTypes()
    {
        return this.types.stream().flatMap( this::getTypes );
    }

    private Stream<Class> getTypes( Class<?> clazz )
    {
        if( clazz.isInterface() )
        {
            return typesOf( clazz ).map( Classes.RAW_CLASS );
        }
        else
        {
            return classHierarchy( clazz ).map( Classes.RAW_CLASS );
        }
    }

    public AssociationModel newAssociationModel( AccessibleObject accessor,
                                                 List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for Association references
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the Association itself
        valueConstraintsModel = constraintsFor( annotations.stream(), Association.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance associationValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            associationValueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new AssociationModel( accessor, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
    }

    public ManyAssociationModel newManyAssociationModel( AccessibleObject accessor,
                                                         List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in ManyAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the ManyAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(), ManyAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance manyValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            manyValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
    }

    public NamedAssociationModel newNamedAssociationModel( AccessibleObject accessor,
                                                           List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in NamedAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the NamedAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(), NamedAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance namedValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            namedValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new NamedAssociationModel( accessor, valueConstraintsInstance, namedValueConstraintsInstance, metaInfo );
    }
}
