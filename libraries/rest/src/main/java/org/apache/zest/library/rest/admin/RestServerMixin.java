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

package org.apache.zest.library.rest.admin;

import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.structure.Module;
import org.restlet.Component;
import org.restlet.data.Protocol;

public abstract class RestServerMixin
    implements RestServerComposite
{
    @Structure
    private Module module;

    @This
    private Configuration<RestServerConfiguration> configuration;

    private Component component;

    @Override
    public void startServer()
        throws Exception
    {
        configuration.refresh();

        component = new Component();
        component.getServers().add( Protocol.HTTP, configuration.get().port().get() );
        RestApplication application = module.newObject( RestApplication.class, component.getContext() );
        component.getDefaultHost().attach( application );
        component.start();
    }

    @Override
    public void stopServer()
        throws Exception
    {
        component.stop();
    }
}
