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
package org.apache.zest.entitystore.leveldb;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.library.fileconfig.FileConfiguration;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

/**
 * LevelDB implementation of MapEntityStore.
 */
public class LevelDBEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{

    @Service
    private FileConfiguration fileConfig;
    @This
    private Configuration<LevelDBEntityStoreConfiguration> configuration;
    @Uses
    private ServiceDescriptor descriptor;
    private Charset charset;
    private DB db;

    @Override
    public void activateService()
        throws Exception
    {
        charset = Charset.forName( "UTF-8" );
        configuration.refresh();
        LevelDBEntityStoreConfiguration config = configuration.get();

        // Choose flavour
        String flavour = config.flavour().get();
        DBFactory factory;
        if( "jni".equalsIgnoreCase( flavour ) )
        {
            factory = newJniDBFactory();
        }
        else if( "java".equalsIgnoreCase( flavour ) )
        {
            factory = newJavaDBFactory();
        }
        else
        {
            factory = newDBFactory();
        }

        // Apply configuration
        Options options = new Options();
        options.createIfMissing( true );
        if( config.blockRestartInterval().get() != null )
        {
            options.blockRestartInterval( config.blockRestartInterval().get() );
        }
        if( config.blockSize().get() != null )
        {
            options.blockSize( config.blockSize().get() );
        }
        if( config.cacheSize().get() != null )
        {
            options.cacheSize( config.cacheSize().get() );
        }
        if( config.compression().get() != null )
        {
            options.compressionType( config.compression().get()
                                     ? CompressionType.SNAPPY
                                     : CompressionType.NONE );
        }
        if( config.maxOpenFiles().get() != null )
        {
            options.maxOpenFiles( config.maxOpenFiles().get() );
        }
        if( config.paranoidChecks().get() != null )
        {
            options.paranoidChecks( config.paranoidChecks().get() );
        }
        if( config.verifyChecksums().get() != null )
        {
            options.verifyChecksums( config.verifyChecksums().get() );
        }
        if( config.writeBufferSize().get() != null )
        {
            options.writeBufferSize( config.writeBufferSize().get() );
        }

        // Open/Create the database
        File dbFile = new File( fileConfig.dataDirectory(), descriptor.identity().toString() );
        db = factory.open( dbFile, options );
    }

    /**
     * Tries in order: JNI and then pure Java LevelDB implementations.
     */
    private DBFactory newDBFactory()
    {
        try
        {
            return newJniDBFactory();
        }
        catch( Exception ex )
        {
            try
            {
                return newJavaDBFactory();
            }
            catch( Exception ex2 )
            {
                throw new RuntimeException( "Unable to create a LevelDB DBFactory instance. "
                                            + "Tried JNI and pure Java. "
                                            + "The stacktrace is the pure Java attempt.", ex2 );
            }
        }
    }

    private DBFactory newJniDBFactory()
        throws Exception
    {
        return (DBFactory) Class.forName( "org.fusesource.leveldbjni.JniDBFactory" ).newInstance();
    }

    private DBFactory newJavaDBFactory()
        throws Exception
    {
        return (DBFactory) Class.forName( "org.iq80.leveldb.impl.Iq80DBFactory" ).newInstance();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        try
        {
            db.close();
        }
        finally
        {
            db = null;
            charset = null;
        }
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        byte[] state = db.get( entityReference.identity().toString().getBytes( charset ) );
        if( state == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        String jsonState = new String( state, charset );
        return new StringReader( jsonState );
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        DBIterator iterator = db.iterator();
                        try
                        {
                            for( iterator.seekToFirst(); iterator.hasNext(); iterator.next() )
                            {
                                byte[] state = iterator.peekNext().getValue();
                                String jsonState = new String( state, charset );
                                receiver.receive( new StringReader( jsonState ) );
                            }
                        }
                        finally
                        {
                            iterator.close();
                        }
                    }

                } );
            }

        };
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        final WriteBatch writeBatch = db.createWriteBatch();
        try
        {
            changes.visitMap( new MapChanger()
            {

                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String jsonState = toString();
                            writeBatch.put( ref.identity().toString().getBytes( charset ), jsonState.getBytes( charset ) );
                        }

                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String jsonState = toString();
                            writeBatch.put( ref.identity().toString().getBytes( charset ), jsonState.getBytes( charset ) );
                        }

                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
                {
                    writeBatch.delete( ref.identity().toString().getBytes( charset ) );
                }

            } );
            db.write( writeBatch );
        }
        finally
        {
            writeBatch.close();
        }
    }

}
