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

package org.apache.zest.runtime.objects;

import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.concern.GenericConcern;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

/**
 * JAVADOC
 */
public class ObjectConcernTest
{

    @Test
    public void testConcernOnObject()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( TestObject.class );
            }
        };

        TestObject object = assembler.module().newObject( TestObject.class );

        object.doStuff();
    }

    @Concerns( LogConcern.class )
    public static class TestObject
    {
        public void doStuff()
        {
        }
    }

    public static class LogConcern
        extends GenericConcern
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            System.out.println( "Invoked " + method.getName() + " on " + proxy );

            return next.invoke( proxy, method, args );
        }
    }
}
