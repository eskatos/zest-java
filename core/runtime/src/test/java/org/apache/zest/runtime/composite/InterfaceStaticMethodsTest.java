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
 */
package org.apache.zest.runtime.composite;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that interface static methods are ignored when assembling composites.
 */
public class InterfaceStaticMethodsTest extends AbstractZestTest
{
    public interface StaticMethods
    {
        @UseDefaults( "foo" )
        Property<String> foo();

        static String bar()
        {
            return "bar";
        }
    }

    public interface OverrideStaticMethods extends StaticMethods
    {
        static String bar()
        {
            return "bar overridden";
        }
    }

    @Override
    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        module.transients( StaticMethods.class, OverrideStaticMethods.class );
    }

    @Test
    public void staticMethods() throws NoSuchMethodException
    {
        StaticMethods staticMethods = transientBuilderFactory.newTransient( StaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( StaticMethods.bar(), equalTo( "bar" ) );
    }

    @Test
    public void overrideStaticMethods()
    {
        OverrideStaticMethods staticMethods = transientBuilderFactory.newTransient( OverrideStaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( OverrideStaticMethods.bar(), equalTo( "bar overridden" ) );
    }
}
