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
package org.apache.zest.test.performance.runtime.composite;

import java.time.Duration;
import java.time.Instant;
import org.apache.zest.api.time.SystemTime;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

/**
 * Tests performance of new composite creation.
 */
public class CompositeCreationPerformanceTest
{
    @Test
    public void newInstanceForRegisteredCompositePerformance()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( AnyComposite.class );
                module.objects( AnyObject.class );
                module.values( AnyValue.class );
            }
        };
        int loops = 2;
        long t0 = 0;
        {
            for( int i = 0; i < loops; i++ )
            {
                t0 = t0 + testJavaObjectCreationPerformance();
            }
            t0 = t0 / loops;
        }
        long t1 = 0;
        {
            TransientBuilderFactory module = assembler.module();
            for( int i = 0; i < loops; i++ )
            {
                t1 = t1 + testCompositeCreationPerformance( module );
            }
            t1 = t1 / loops;
        }
        long t2 = 0;
        {
            ObjectFactory objectFactory = assembler.module();
            for( int i = 0; i < loops; i++ )
            {
                t2 = t2 + testObjectCreationPerformance( objectFactory );
            }
            t2 = t2 / loops;
        }
        long t3 = 0;
        {
            ValueBuilderFactory valueBuilderFactory = assembler.module();
            for( int i = 0; i < loops; i++ )
            {
                t3 = t3 + testValueCreationPerformance( valueBuilderFactory );
            }
            t3 = t3 / loops;
        }

        long t4 = 0;
        {
            TransientBuilderFactory module = assembler.module();
            for( int i = 0; i < loops; i++ )
            {
                t4 = t4 + testCompositeCreationWithBuilderPerformance( module );
            }
            t4 = t4 / loops;
        }
        long t6 = 0;
        {
            ValueBuilderFactory valueBuilderFactory = assembler.module();
            for( int i = 0; i < loops; i++ )
            {
                t6 = t6 + testValueCreationWithBuilderPerformance( valueBuilderFactory );
            }
            t6 = t6 / loops;
        }

        System.out.println( "Transient: " + ( t1 / t0 ) + "x" );
        System.out.println( "TransientBuilder: " + ( t4 / t0 ) + "x" );
        System.out.println( "Value: " + ( t3 / t0 ) + "x" );
        System.out.println( "ValueBuilder: " + ( t6 / t0 ) + "x" );
        System.out.println( "Object: " + ( t2 / t0 ) + "x" );
    }

    private long testCompositeCreationPerformance( TransientBuilderFactory module )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            module.newTransient( AnyComposite.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Composite Creation Time:" + time + " nanoseconds per composite" );
        return time;
    }

    private long testCompositeCreationWithBuilderPerformance( TransientBuilderFactory module )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            TransientBuilder<AnyComposite> builder = module.newTransientBuilder( AnyComposite.class );
            builder.newInstance();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Composite (builder) Creation Time:" + time + " nanoseconds per composite" );
        return time;
    }

    private long testValueCreationPerformance( ValueBuilderFactory valueBuilderFactory )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            valueBuilderFactory.newValue( AnyValue.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Value Creation Time:" + time + " nanoseconds per composite" );
        return time;
    }

    private long testValueCreationWithBuilderPerformance( ValueBuilderFactory valueBuilderFactory )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            ValueBuilder<AnyValue> builder = valueBuilderFactory.newValueBuilder( AnyValue.class );
            builder.newInstance();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Value (builder) Creation Time:" + time + " nanoseconds per composite" );
        return time;
    }

    private long testObjectCreationPerformance( ObjectFactory objectFactory )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            objectFactory.newObject( AnyObject.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Zest Object Creation Time:" + time + " nanoseconds per object" );
        return time;
    }

    private long testJavaObjectCreationPerformance()
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            new AnyObject();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        System.out.println( "Minimum Java Object Creation Time:" + time + " nanoseconds per object" );
        return time;
    }

    public static interface AnyComposite
        extends TransientComposite
    {
    }

    public static interface AnyValue
        extends ValueComposite
    {
    }

    public static class AnyObject
    {
    }

}
