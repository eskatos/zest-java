/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.logging.tracing;

import java.util.Iterator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.logging.trace.Trace;
import org.qi4j.logging.trace.TraceAllConcern;
import org.qi4j.logging.trace.TraceConcern;
import org.qi4j.logging.trace.assemblies.TracingAssembler;
import org.qi4j.logging.trace.records.CompositeTraceRecordEntity;
import org.qi4j.logging.trace.records.EntityTraceRecordEntity;
import org.qi4j.logging.trace.records.ServiceTraceRecordEntity;
import org.qi4j.logging.trace.records.TraceRecord;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

public class TracingTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( SomeService.class ).instantiateOnStartup();
        module.addServices( SomeService2.class ).instantiateOnStartup();
        module.addAssembler( new TracingAssembler() );
        module.addServices( MemoryEntityStoreService.class );
        module.addAssembler( new RdfMemoryStoreAssembler() );
        module.addServices( UuidIdentityGeneratorService.class );
        module.addEntities( CompositeTraceRecordEntity.class );
        module.addEntities( EntityTraceRecordEntity.class );
        module.addEntities( ServiceTraceRecordEntity.class );
    }

    @Test
    public void whenTraceOnMixinTypeMethodExpectOneEntryInEntityStore()
        throws Exception
    {
        SomeService sc = moduleInstance.serviceFinder().findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 456, sc.doSomethingLessImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = uow.queryBuilderFactory().newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = builder.newQuery();
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Test
    public void whenTraceAllOnCompositeTypeExpectTwoEntryInEntityStore()
        throws Exception
    {
        SomeService2 sc = moduleInstance.serviceFinder().findService( SomeService2.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 456, sc.doSomethingLessImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = uow.queryBuilderFactory().newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = builder.newQuery();
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertTrue( result.hasNext() );
        TraceRecord rec2 = result.next();
        assertEquals( "doSomethingLessImportant", rec2.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Test
    @Ignore( "Waiting for resolution of QI-146" )
    public void whenTraceOnMixinImplExpectTwoEntryInEntityStore()
        throws Exception
    {
        SomeService sc = moduleInstance.serviceFinder().findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 789, sc.doSomethingModeratelyImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            QueryBuilder<TraceRecord> builder = uow.queryBuilderFactory().newQueryBuilder( TraceRecord.class );
            Query<TraceRecord> query = builder.newQuery();
            // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
            Iterator<TraceRecord> result = query.iterator();
            assertTrue( result.hasNext() );
            TraceRecord rec1 = result.next();
            assertEquals( "doSomethingImportant", rec1.methodName().get() );
            assertTrue( result.hasNext() );
            TraceRecord rec2 = result.next();
            assertEquals( "doSomethingModeratelyImportant", rec2.methodName().get() );
            assertFalse( result.hasNext() );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
        catch( Error e )
        {
            uow.discard();
            throw e;
        }
    }


    @Test
    public void whenTraceOnConcernExpectOneEntryInEntityStore()
        throws Exception
    {
        // It is not possible to put Annotation on Concern Methods, so it should only record one.

        SomeService sc = moduleInstance.serviceFinder().findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 753, sc.doSomethingInsanelyImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = uow.queryBuilderFactory().newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = builder.newQuery();
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Mixins( SomeMixin.class )
    @Concerns( { SomeConcern.class, TraceConcern.class } )
    public interface SomeService extends Some, ServiceComposite
    {
    }

    @Mixins( SomeMixin.class )
    @Concerns( { SomeConcern.class, TraceAllConcern.class } )
    public interface SomeService2 extends Some, ServiceComposite
    {
    }

    public interface Some
    {
        @Trace int doSomethingImportant();

        int doSomethingLessImportant();

        int doSomethingModeratelyImportant();

        int doSomethingInsanelyImportant();
    }

    public static class SomeConcern extends ConcernOf<Some>
        implements Some
    {
        public int doSomethingImportant()
        {
            System.out.println( "-- doSomethingImportant()" );
            return next.doSomethingImportant();
        }

        public int doSomethingLessImportant()
        {
            System.out.println( "-- doSomethingLessImportant()" );
            return next.doSomethingLessImportant();
        }

        public int doSomethingModeratelyImportant()
        {
            System.out.println( "-- doSomethingLessImportant()" );
            return next.doSomethingModeratelyImportant();
        }

        @Trace
        public int doSomethingInsanelyImportant()
        {
            System.out.println( "-- doSomethingInsanelyImportant()" );
            return next.doSomethingInsanelyImportant();
        }


    }

    public static class SomeMixin
        implements Some
    {

        public int doSomethingImportant()
        {
            System.out.println( "---- doSomethingImportant()" );
            return 123;
        }

        public int doSomethingLessImportant()
        {
            System.out.println( "---- doSomethingLessImportant()" );
            return 456;
        }

        @Trace
        public int doSomethingModeratelyImportant()
        {
            System.out.println( "---- doSomethingModeratelyImportant()" );
            return 789;
        }

        public int doSomethingInsanelyImportant()
        {
            System.out.println( "---- doSomethingInsanelyImportant()" );
            return 753;
        }
    }
}
