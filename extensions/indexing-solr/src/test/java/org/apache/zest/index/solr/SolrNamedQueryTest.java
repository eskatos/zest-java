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
package org.apache.zest.index.solr;

import java.util.function.Predicate;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.fileconfig.FileConfigurationAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.test.indexing.AbstractNamedQueryTest;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

@Ignore( "SOLR Index/Query is not working at all" )
public class SolrNamedQueryTest
    extends AbstractNamedQueryTest
{
    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
        new SolrAssembler().assemble( module );
    }

    @Override
    protected String[] queryStrings()
    {
        return new String[] {}; // TODO Write example Solr named queries
    }

    @Override
    protected Predicate<Composite> createNamedQueryDescriptor( String queryName, String queryString )
    {
        return SolrExpressions.search( queryString );
    }
}
