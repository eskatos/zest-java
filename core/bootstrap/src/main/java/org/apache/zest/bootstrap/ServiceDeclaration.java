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

package org.apache.zest.bootstrap;

import org.apache.zest.api.activation.Activator;
import org.apache.zest.api.common.Visibility;

/**
 * Fluent API for declaring services hosted in Zest. Instances
 * of this API are acquired by calling {@link ModuleAssembly#services(Class[])}.
 */
public interface ServiceDeclaration
{
    ServiceDeclaration setMetaInfo( Object serviceAttribute );

    ServiceDeclaration visibleIn( Visibility visibility );

    ServiceDeclaration withConcerns( Class<?>... concerns );

    ServiceDeclaration withSideEffects( Class<?>... sideEffects );

    ServiceDeclaration withMixins( Class<?>... mixins );

    ServiceDeclaration withTypes( Class<?>... types );

    ServiceDeclaration identifiedBy( String identity );

    ServiceDeclaration taggedWith( String... tags );

    ServiceDeclaration instantiateOnStartup();

    /**
     * Set the service activators. Activators are executed in order around the
     * ServiceReference activation and passivation.
     *
     * @param activators the service activators
     * @return the assembly
     */    
    @SuppressWarnings( { "unchecked","varargs" } )
    ServiceDeclaration withActivators( Class<? extends Activator<?>>... activators );
}
