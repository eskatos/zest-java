/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.zest.library.rdf.entity;

import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rdf.DcRdf;
import org.apache.zest.library.rdf.Rdfs;
import org.apache.zest.library.rdf.serializer.RdfXmlSerializer;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

/**
 * JAVADOC
 */
public class EntitySerializerTest
    extends AbstractZestTest
{
    @Service
    EntityStore entityStore;
    @Uses
    EntityStateSerializer serializer;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        new OrgJsonValueSerializationAssembler().assemble( module );

        module.entities( TestEntity.class );
        module.values( TestValue.class, Test2Value.class );
        module.objects( EntityStateSerializer.class, EntitySerializerTest.class );
    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        createDummyData();

        module.injectTo( this );
    }

    @Test
    public void testEntitySerializer()
        throws RDFHandlerException
    {
        EntityReference entityReference = new EntityReference( "test2" );
        Usecase usecase = UsecaseBuilder.newUsecase( "Test" );
        long currentTime = System.currentTimeMillis();
        EntityStoreUnitOfWork unitOfWork = entityStore.newUnitOfWork( usecase, currentTime );
        EntityState entityState = unitOfWork.entityStateOf( module, entityReference );

        Iterable<Statement> graph = serializer.serialize( entityState );

        String[] prefixes = new String[]{ "rdf", "dc", " vc" };
        String[] namespaces = new String[]{ Rdfs.RDF, DcRdf.NAMESPACE, "http://www.w3.org/2001/vcard-rdf/3.0#" };

        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ), prefixes, namespaces );
    }

    void createDummyData()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            ValueBuilder<TestValue> valueBuilder = module.newValueBuilder( TestValue.class );
            valueBuilder.prototype().test1().set( 4L );
            ValueBuilder<Test2Value> valueBuilder2 = module.newValueBuilder( Test2Value.class );
            valueBuilder2.prototype().data().set( "Habba" );
            valueBuilder.prototype().test3().set( valueBuilder2.newInstance() );
            TestValue testValue = valueBuilder.newInstance();

            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class, "test1" );
            TestEntity rickardTemplate = builder.instance();
            rickardTemplate.name().set( "Rickard" );
            rickardTemplate.title().set( "Mr" );
            rickardTemplate.value().set( testValue );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( TestEntity.class, "test2" );
            TestEntity niclasTemplate = builder2.instance();
            niclasTemplate.name().set( "Niclas" );
            niclasTemplate.title().set( "Mr" );
            niclasTemplate.association().set( testEntity );
            niclasTemplate.manyAssoc().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            valueBuilder = module.newValueBuilderWithPrototype( testValue );
            valueBuilder.prototype().test1().set( 5L );
            testValue = valueBuilder.newInstance();
            niclasTemplate.value().set( testValue );
            builder2.newInstance();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }
}

