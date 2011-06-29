/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.constraints;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.constraints.MinLengthConstraint;
import org.qi4j.library.constraints.annotation.Matches;
import org.qi4j.library.constraints.annotation.MinLength;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PropertyConstraintTest
    extends AbstractQi4jTest
{
    @org.junit.Test
    public void givenConstraintOnPropertyWhenInvalidValueThenThrowException()
        throws Throwable
    {
        TransientBuilder<Test> builder = transientBuilderFactory.newTransientBuilder( Test.class );
        builder.prototype().test().set( "XXXXXX" );
        Test test = builder.newInstance();
        try
        {
            test.test().set( "YY" );
            fail( "Should have thrown a ConstraintViolationException." );
        }
        catch( ConstraintViolationException e )
        {
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 2, violations.size() );
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    @Constraints( { MinLengthConstraint.class } )
    public interface TestComposite
        extends Test, TransientComposite
    {
    }

    public interface Test
    {
        @MinLength( 3 )
        @Matches( "X*" )
        Property<String> test();
    }
}