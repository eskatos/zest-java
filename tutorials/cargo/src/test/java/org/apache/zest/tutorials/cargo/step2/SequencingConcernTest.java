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
package org.apache.zest.tutorials.cargo.step2;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.test.mock.MockComposite;
import org.apache.zest.test.mock.MockPlayerMixin;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for SequencingConcern.
 *
 * @author Alin Dreghiciu
 */
public class SequencingConcernTest
{
    /**
     * Tests that when shipping service fails to make the booking generator is not called and booking failure code is
     * returned.
     */
    @Test
    @Ignore( "Expectations need to be figured out." )
    public void failingBooking()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( ShippingServiceTestComposite.class );
            }
        };
        ShippingService shippingService = createMock( ShippingService.class );
        Cargo cargo = createMock( Cargo.class );
        Voyage voyage = createMock( Voyage.class );
        HasSequence sequence = createMock( HasSequence.class );
        expect( shippingService.makeBooking( cargo, voyage ) ).andReturn( -1000 );
        expect( voyage.bookedCargoSize().get() ).andReturn( 0.0 )
            .atLeastOnce();
        expect( cargo.size().get() ).andReturn( 0.0 )
            .atLeastOnce();
        expect( sequence.sequence().get() ).andReturn( 0 )
            .atLeastOnce();
        replay( shippingService, cargo, voyage );
        ShippingServiceTestComposite underTest =
            assembler.module().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( -1000 ) ) );
        verify( shippingService, cargo, voyage );
    }

    /**
     * Tests that when shipping service succeeds to make the booking generator gets called and generated value is
     * returned.
     */
    @Test
    @Ignore( "Expectations need to be figured out." )
    public void successfulBooking()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( ShippingServiceTestComposite.class );
            }
        };
        ShippingService shippingService = createMock( ShippingService.class );
        Cargo cargo = createMock( Cargo.class );
        Voyage voyage = createMock( Voyage.class );
        HasSequence generator = createMock( HasSequence.class );
        Property<Integer> sequence = createMock( Property.class );
        expect( shippingService.makeBooking( cargo, voyage ) ).andReturn( 100 );
        expect( generator.sequence() ).andReturn( sequence ).anyTimes();
        expect( sequence.get() ).andReturn( 1000 );
        replay( shippingService, cargo, voyage, generator, sequence );
        ShippingServiceTestComposite underTest =
            assembler.module().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        underTest.useMock( generator ).forClass( HasSequence.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( 1000 ) ) );
        verify( shippingService, cargo, voyage, generator, sequence );
    }

    @Mixins( MockPlayerMixin.class )
    @Concerns( SequencingConcern.class )
    public static interface ShippingServiceTestComposite
        extends ShippingService, HasSequence, MockComposite
    {
    }
}
