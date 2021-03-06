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

package org.apache.zest.library.rest.server.restlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.StringIdentity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.InitializationException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.server.spi.RequestReader;
import org.restlet.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates to a list of potential readers. Register readers on startup.
 */
public class RequestReaderDelegator
    implements RequestReader
{
    private static final Object[] NULL_PARAMS = new Object[ 0 ];

    List<RequestReader> requestReaders = new ArrayList<RequestReader>();

    @Structure
    Module module;

    public void init( @Service Iterable<ServiceReference<RequestReader>> requestReaderReferences )
        throws InitializationException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        Identity requestreaderdelegator = new StringIdentity("requestreaderdelegator");

        // Add custom readers first
        for( ServiceReference<RequestReader> requestReader : requestReaderReferences )
        {
            if( !requestReader.identity().equals(requestreaderdelegator) )
            {
                logger.info( "Registered request reader:" + requestReader.identity() );
                registerRequestReader( requestReader.get() );
            }
        }

        // Add defaults
        ResourceBundle defaultRequestReaders = ResourceBundle.getBundle( "org.apache.zest.library.rest.server.rest-server" );

        String requestReaderClasses = defaultRequestReaders.getString( "requestreaders" );
        logger.info( "Using request readers:" + requestReaderClasses );
        for( String className : requestReaderClasses.split( "," ) )
        {
            try
            {
                Class readerClass = module.descriptor().classLoader().loadClass( className.trim() );
                RequestReader writer = (RequestReader) module.newObject( readerClass );
                registerRequestReader( writer );
            }
            catch( ClassNotFoundException e )
            {
                logger.warn( "Could not register request reader " + className, e );
            }
        }
    }

    public void registerRequestReader( RequestReader reader )
    {
        requestReaders.add( reader );
    }

    @Override
    public Object[] readRequest( Request request, Method method )
    {
        if( method.getParameterTypes().length == 0 )
        {
            return NULL_PARAMS;
        }

        for( RequestReader requestReader : requestReaders )
        {
            Object[] arguments = requestReader.readRequest( request, method );

            if( arguments != null )
            {
                return arguments;
            }
        }

        return null;
    }
}
