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
package org.apache.zest.runtime.instantiation;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.fail;

public class ValueInstantiationTests
    extends AbstractZestTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( MyValue.class );
    }

    @Test
    public void whenTryingToCreateValueFromNullTypeThenExpectNullArgumentException()
        throws Exception
    {
        // valueBuilderFactory.newValueBuilder()
        try
        {
            valueBuilderFactory.newValueBuilder( null );
            fail( "NullArgumentException was expected." );
        }
        catch( NullArgumentException e )
        {
            // expected
        }

        // valueBuilderFactory.newValue();
        try
        {
            valueBuilderFactory.newValue( null );
            fail( "NullArgumentException was expected." );
        }
        catch( NullArgumentException e )
        {
            // expected
        }

        //module.newValueFromSerializedState();
        try
        {
            valueBuilderFactory.newValueFromSerializedState( null, "abc:123" );
            ValueBuilder<My> builder = valueBuilderFactory.newValueBuilder( null );
            fail( "NullArgumentException was expected." );
        }
        catch( NullArgumentException e )
        {
            // expected
        }
    }

    @Test
    public void whenCreatingServiceCompositeGivenAServiceCompositeThenSucceed()
        throws Exception
    {
        ValueBuilder<My> builder = valueBuilderFactory.newValueBuilder( My.class );
        My my = builder.newInstance();
        Assert.assertEquals( "Niclas", my.doSomething() );
    }

    @Mixins( MyMixin.class )
    public interface MyValue
        extends ValueComposite, My
    {
    }

    public interface My
    {
        String doSomething();
    }

    public static class MyMixin
        implements My
    {

        public String doSomething()
        {
            return "Niclas";
        }
    }
}
