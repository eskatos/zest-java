package org.qi4j.library.framework;
/**
 *  TODO
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.Composite;
import org.qi4j.CompositeBuilder;
import org.qi4j.annotation.Mixins;
import org.qi4j.test.AbstractQi4jTest;

public class DecoratorMixinTest extends AbstractQi4jTest
{
    public void testGenericDecoratorOfDomainobject() throws Exception
    {
        CompositeBuilder<Composite1> cb = compositeBuilderFactory.newCompositeBuilder( Composite1.class );
        cb.decorate( new Test1.Test1Mixin() );
        Test1 test = cb.newInstance();

        assertEquals( "ok", test.test() );
    }

    public void testGenericDecoratorOfInvocationHandler() throws Exception
    {
        CompositeBuilder<Composite1> cb = compositeBuilderFactory.newCompositeBuilder( Composite1.class );
        cb.decorate( new InvocationHandler()
        {

            public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
            {
                return "ok";
            }
        } );
        Test1 test = cb.newInstance();

        assertEquals( "ok", test.test() );
    }

    interface Test1
    {
        public String test();

        public class Test1Mixin
            implements Test1
        {

            public String test()
            {
                return "ok";
            }
        }
    }

    @Mixins( DecoratorMixin.class )
    interface Composite1 extends Composite, Test1
    {
    }
}