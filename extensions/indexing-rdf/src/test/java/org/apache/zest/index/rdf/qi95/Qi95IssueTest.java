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
package org.apache.zest.index.rdf.qi95;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.junit.Rule;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.bootstrap.*;
import org.apache.zest.entitystore.jdbm.JdbmConfiguration;
import org.apache.zest.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.library.rdf.repository.NativeConfiguration;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class Qi95IssueTest
{

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void canCreateAndQueryWithNativeRdfAndJdbm()
        throws Exception
    {
        Application application = createApplication( nativeRdf, jdbmStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithAllInMemory()
        throws Exception
    {
        Application application = createApplication( inMemoryRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithNativeRdfWithInMemoryStore()
        throws Exception
    {
        Application application = createApplication( nativeRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithInMemoryRdfWithJdbm()
        throws Exception
    {
        Application application = createApplication( inMemoryRdf, jdbmStore, domain );
        try
        {
            application.activate();

            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    public void createABunchOfStuffAndDoQueries( UnitOfWorkFactory unitOfWorkFactory,
                                                 QueryBuilderFactory queryBuilderFactory
    )
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        newItemType( uow, "Band" );
        newItemType( uow, "Bracelet" );
        newItemType( uow, "Necklace" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<ItemType> qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> initialList = copyOf( uow.newQuery( qb ));

        assertTrue( "Band is not in the initial list", hasItemTypeNamed( "Band", initialList ) );
        assertTrue( "Bracelet is not in the initial list", hasItemTypeNamed( "Bracelet", initialList ) );
        assertTrue( "Necklace is not in the initial list", hasItemTypeNamed( "Necklace", initialList ) );

        newItemType( uow, "Watch" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> listAfterFirstQueryAndAdd = copyOf( uow.newQuery( qb ) );

        assertTrue( "Band is not in the list after the first query and add", hasItemTypeNamed( "Band", listAfterFirstQueryAndAdd ) );
        assertTrue( "Bracelet is not in the list after the first query and add", hasItemTypeNamed( "Bracelet", listAfterFirstQueryAndAdd ) );
        assertTrue( "Necklace is not in the list after the first query and add", hasItemTypeNamed( "Necklace", listAfterFirstQueryAndAdd ) );
        assertTrue( "Watch is not in the list after the first query and add", hasItemTypeNamed( "Watch", listAfterFirstQueryAndAdd ) );

        newItemType( uow, "Ear ring" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        Iterable<ItemType> finalList = copyOf( uow.newQuery( qb ) );
        assertTrue( "Band is not in the final list", hasItemTypeNamed( "Band", finalList ) );
        assertTrue( "Bracelet is not in the final list", hasItemTypeNamed( "Bracelet", finalList ) );
        assertTrue( "Necklace is not in the final list", hasItemTypeNamed( "Necklace", finalList ) );
        assertTrue( "Watch is not in the final list", hasItemTypeNamed( "Watch", finalList ) );
        assertTrue( "Ear ring is not in the final list", hasItemTypeNamed( "Ear ring", finalList ) );
        uow.complete();
    }

    private Application createApplication( final ModuleAssemblyBuilder queryServiceModuleBuilder,
                                              final ModuleAssemblyBuilder entityStoreModuleBuilder,
                                              final LayerAssemblyBuilder domainLayerBuilder
    )
        throws AssemblyException
    {
        Energy4Java zest = new Energy4Java();
        Application application = zest.newApplication( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();

                LayerAssembly configLayer = applicationAssembly.layer( "Config" );
                configModule.buildModuleAssembly( configLayer, "Configuration" );

                LayerAssembly infrastructureLayer = applicationAssembly.layer( "Infrastructure" );
                infrastructureLayer.uses( configLayer );

                queryServiceModuleBuilder.buildModuleAssembly( infrastructureLayer, "Query Service" );
                entityStoreModuleBuilder.buildModuleAssembly( infrastructureLayer, "Entity Store" );

                LayerAssembly domainLayer = domainLayerBuilder.buildLayerAssembly( applicationAssembly );
                domainLayer.uses( infrastructureLayer );
                return applicationAssembly;
            }
        } );
        return application;
    }

    interface LayerAssemblyBuilder
    {
        LayerAssembly buildLayerAssembly( ApplicationAssembly appAssembly )
            throws AssemblyException;
    }

    interface ModuleAssemblyBuilder
    {
        ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException;
    }

    final ModuleAssemblyBuilder nativeRdf = new ModuleAssemblyBuilder()
    {
        @Override
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new RdfNativeSesameStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder inMemoryStore = new ModuleAssemblyBuilder()
    {
        @Override
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new EntityTestAssembler().visibleIn( Visibility.application ) );
        }
    };

    final ModuleAssemblyBuilder inMemoryRdf = new ModuleAssemblyBuilder()
    {
        @Override
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new RdfMemoryStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder jdbmStore = new ModuleAssemblyBuilder()
    {
        @Override
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, jdbmEntityStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder configModule = new ModuleAssemblyBuilder()
    {
        @Override
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, entityStoreConfigAssembler() );
        }
    };

    final LayerAssemblyBuilder domain = new LayerAssemblyBuilder()
    {
        @Override
        public LayerAssembly buildLayerAssembly( ApplicationAssembly appAssembly )
            throws AssemblyException
        {
            LayerAssembly domainLayer = appAssembly.layer( "Domain" );
            addModule( domainLayer, "Domain", new Assembler()
            {
                @Override
                @SuppressWarnings( "unchecked" )
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ItemTypeEntity.class );
                    new DefaultUnitOfWorkAssembler().assemble( module );
                }
            } );
            return domainLayer;
        }
    };

    private Assembler entityStoreConfigAssembler()
    {
        return new Assembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );
                new DefaultUnitOfWorkAssembler().assemble( module );

                module.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
                module.forMixin( NativeConfiguration.class )
                    .declareDefaults()
                    .dataDirectory()
                    .set( rdfDirectory().getAbsolutePath() );

                module.entities( JdbmConfiguration.class ).visibleIn( Visibility.application );
                module.forMixin( JdbmConfiguration.class )
                    .declareDefaults()
                    .file()
                    .set( jdbmDirectory().getAbsolutePath() );
            }
        };
    }

    private Assembler jdbmEntityStoreAssembler()
    {
        return new Assembler()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new OrgJsonValueSerializationAssembler().assemble( module );
                new JdbmEntityStoreAssembler().visibleIn( Visibility.application ).assemble( module );
                new DefaultUnitOfWorkAssembler().assemble( module );
            }
        };
    }

    private ModuleAssembly addModule( LayerAssembly layerAssembly, String name, Assembler assembler )
        throws AssemblyException
    {
        ModuleAssembly moduleAssembly = layerAssembly.module( name );
        assembler.assemble( moduleAssembly );
        new DefaultUnitOfWorkAssembler().assemble( moduleAssembly );
        return moduleAssembly;
    }

    private File rdfDirectory()
    {
        return createTempDirectory( "rdf-index" );
    }

    private File jdbmDirectory()
    {
        return createTempDirectory( "jdbm-store" );
    }

    private File createTempDirectory( String name )
    {
        File t = new File( tmpDir.getRoot(), name );
        t.mkdirs();
        return t;
    }

    boolean hasItemTypeNamed( String name, Iterable<ItemType> list )
    {
        for( ItemType i : list )
        {
            Property<String> nameProperty = i.name();
            String entityName = nameProperty.get();
            if( entityName.equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    private Iterable<ItemType> copyOf( Iterable<ItemType> iterable )
    {
        Collection<ItemType> copy = new ArrayList<ItemType>();
        for( ItemType i : iterable )
        {
            copy.add( i );
        }
        return Collections.unmodifiableCollection( copy );
    }

    private ItemType newItemType( UnitOfWork uow, String name )
    {
        EntityBuilder<ItemType> builder = uow.newEntityBuilder( ItemType.class );
        builder.instance().name().set( name );
        return builder.newInstance();
    }

    interface ItemType
    {
        Property<String> name();
    }

    interface ItemTypeEntity
        extends ItemType, EntityComposite
    {
    }
}
