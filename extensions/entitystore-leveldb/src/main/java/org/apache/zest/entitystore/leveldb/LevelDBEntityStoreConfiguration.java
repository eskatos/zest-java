/*
 * Copyright 2012, Paul Merlin.
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
package org.apache.zest.entitystore.leveldb;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;

/**
 * Configuration for LevelDBEntityStoreService.
 */
// START SNIPPET: config
public interface LevelDBEntityStoreConfiguration
    extends ConfigurationComposite
{

    /**
     * LevelDB flavour, can be 'java' or 'jni'.
     * By default, tries 'jni' and fallback to 'java'.
     */
    @Optional
    Property<String> flavour();

    @Optional
    Property<Integer> blockRestartInterval();

    @Optional
    Property<Integer> blockSize();

    @Optional
    Property<Long> cacheSize();

    @Optional
    Property<Boolean> compression();

    @Optional
    Property<Integer> maxOpenFiles();

    @Optional
    Property<Boolean> paranoidChecks();

    @Optional
    Property<Boolean> verifyChecksums();

    @Optional
    Property<Integer> writeBufferSize();

}
// END SNIPPET: config