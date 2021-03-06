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
package org.apache.zest.runtime.mixin;

import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of declaring Mixin in @This declared interface
 */
public class MixinsOnThisInjectionTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestCase.class );
    }

    @Test
    public void givenCompositeWithThisInjectionAndNoMixinDeclarationWhenBindingCompositeThenUseInterfaceDeclaredMixin()
    {
        TestCase TestCase = transientBuilderFactory.newTransient( TestCase.class );
        assertThat( "Composite can be instantiated", TestCase.sayHello(), equalTo( "Hello" ) );
    }

    @Mixins( TestMixin.class )
    public interface TestCase
        extends TransientComposite
    {
        String sayHello();
    }

    public abstract static class TestMixin
        implements TestCase
    {
        @This
        TestCase2 testCase2;

        public String sayHello()
        {
            return testCase2.sayHello();
        }
    }

    @Mixins( TestMixin2.class )
    public interface TestCase2
    {
        String sayHello();
    }

    public abstract static class TestMixin2
        implements TestCase2
    {
        public String sayHello()
        {
            return "Hello";
        }
    }
}