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
package org.apache.zest.entitystore.sql.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.library.sql.common.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DatabaseSQLServiceSpi
{

    boolean schemaExists( Connection connection )
            throws SQLException;

    String getCurrentSchemaName();

    boolean tableExists( Connection connection )
            throws SQLException;

    @SuppressWarnings( "PublicInnerClass" )
    public abstract class CommonMixin
            implements DatabaseSQLServiceSpi
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLServiceSpi.class );

        @This
        private DatabaseSQLServiceState state;

        @Override
        public boolean schemaExists( Connection connection )
                throws SQLException
        {
            ResultSet rs = null;
            try {
                Boolean schemaFound = false;
                rs = connection.getMetaData().getSchemas();
                String schemaName = this.getCurrentSchemaName();

                while ( rs.next() && !schemaFound ) {
                    String eachResult = rs.getString( 1 );
                    LOGGER.trace( "Schema candidate: {}", eachResult );
                    schemaFound = eachResult.equalsIgnoreCase( schemaName );
                }
                LOGGER.trace( "Schema {} found? {}", schemaName, schemaFound );
                return schemaFound;
            } finally {
                SQLUtil.closeQuietly( rs );
            }
        }

        @Override
        public String getCurrentSchemaName()
        {
            return this.state.schemaName().get();
        }

    }

}
