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
package org.apache.zest.sample.sqlsupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.zest.entitystore.sql.internal.SQLs;
import org.apache.zest.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.apache.zest.index.sql.support.postgresql.PostgreSQLAppStartup;
import org.apache.zest.library.sql.common.SQLConfiguration;
import org.apache.zest.library.sql.common.SQLUtil;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

/**
 * SQL Support Sample Main Class.
 * <p><strong>psql postgres</strong></p>
 * <p>CREATE USER jdbc_test_login WITH PASSWORD 'password';</p>
 * <p>CREATE DATABASE jdbc_test_db;</p>
 * <p>GRANT ALL PRIVILEGES ON DATABASE jdbc_test_db TO jdbc_test_login;</p>
 * <p><strong>psql -d jdbc_test_db</strong></p>
 * <p>CREATE EXTENSION ltree;</p>
 */
public class Main
{

    public static void main( String[] args )
            throws Exception
    {
        final Application application = new Energy4Java().newApplication( new AppAssembler() );
        application.activate();
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            @Override
            @SuppressWarnings( "CallToThreadDumpStack" )
            public void run()
            {
                try {
                    application.passivate();
                } catch ( Exception ex ) {
                    System.err.println( "Unable to passivate Zest application!" );
                    ex.printStackTrace();
                }
            }

        } ) );
        Module domainModule = application.findModule( "app", "domain" );

        int exitStatus = 0;

        try {

            UnitOfWork uow = domainModule.unitOfWorkFactory().newUnitOfWork();
            EntityBuilder<PretextEntity> builder = uow.newEntityBuilder( PretextEntity.class );
            PretextEntity pretext = builder.instance();
            pretext.reason().set( "Testing purpose" );
            builder.newInstance();
            uow.complete();

            uow = domainModule.unitOfWorkFactory().newUnitOfWork();
            QueryBuilder<PretextEntity> queryBuilder = domainModule.newQueryBuilder( PretextEntity.class );
            queryBuilder = queryBuilder.where( eq( templateFor( PretextEntity.class ).reason(), "Testing purpose" ) );
            Query<PretextEntity> query = uow.newQuery( queryBuilder );
            pretext = query.find();
            if ( pretext == null ) {
                System.err.println( "ERROR: Unable to find pretext!" );
                exitStatus = -1;
            } else {
                System.out.println( "SUCCESS: Found Pretext with reason: " + pretext.reason().get() );
            }
            uow.discard();

        } finally {
            deleteData( application.findModule( "infra", "persistence" ) );
        }

        System.exit( exitStatus );
    }

    /**
     * Completely delete data so the sample can be run multiple times.
     */
    private static void deleteData( Module persistenceModule )
            throws SQLException
    {
        // EntityStore Data
        {
            UnitOfWork uow = persistenceModule.unitOfWorkFactory().newUnitOfWork();
            try {
                SQLConfiguration config = uow.get( SQLConfiguration.class, PostgreSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
                Connection connection = persistenceModule.findService( DataSource.class ).get().getConnection();
                connection.setAutoCommit( false );
                connection.setReadOnly( false );
                String schemaName = config.schemaName().get();
                if ( schemaName == null ) {
                    schemaName = SQLs.DEFAULT_SCHEMA_NAME;
                }

                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    stmt.execute( "DROP SCHEMA " + schemaName + " CASCADE" );
                    connection.commit();
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
            } finally {
                uow.discard();
            }
        }

        // Indexing Data
        {
            UnitOfWork uow = persistenceModule.unitOfWorkFactory().newUnitOfWork();
            try {
                SQLConfiguration config = uow.get( SQLConfiguration.class, PostgreSQLIndexQueryAssembler.DEFAULT_IDENTITY  );
                Connection connection = persistenceModule.findService( DataSource.class ).get().getConnection();
                connection.setAutoCommit( false );
                connection.setReadOnly( false );
                String schemaName = config.schemaName().get();
                if ( schemaName == null ) {
                    schemaName = PostgreSQLAppStartup.DEFAULT_SCHEMA_NAME;
                }

                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    stmt.execute( "DROP SCHEMA " + schemaName + " CASCADE" );
                    connection.commit();
                } finally {
                    SQLUtil.closeQuietly( stmt );
                    SQLUtil.closeQuietly( connection );
                }
            } finally {
                uow.discard();
            }
        }
    }

}
