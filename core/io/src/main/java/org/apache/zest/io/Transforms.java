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

package org.apache.zest.io;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Utility class for I/O transforms
 */
public class Transforms
{
    /**
     * Filter items in a transfer by applying the given Specification to each item.
     *
     * @param specification            The Specification defining the items to not filter away.
     * @param output                   The Output instance to receive to result.
     * @param <T>                      The item type
     * @param <Receiver2ThrowableType> Exception type that might be thrown by the Receiver.
     *
     * @return And Output encapsulation the filter operation.
     */
    public static <T, Receiver2ThrowableType extends Throwable> Output<T, Receiver2ThrowableType> filter( final Predicate<? super T> specification,
                                                                                                          final Output<T, Receiver2ThrowableType> output
    )
    {
        return new Output<T, Receiver2ThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends T, SenderThrowableType> sender )
                throws Receiver2ThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            @Override
                            public void receive( T item )
                                throws ReceiverThrowableType
                            {
                                if( specification.test( item ) )
                                {
                                    receiver.receive( item );
                                }
                            }
                        } );
                    }
                } );
            }
        };
    }

    /**
     * Map items in a transfer from one type to another by applying the given function.
     *
     * @param function                 The transformation function to apply to the streaming items.
     * @param output                   The output to receive the transformed items.
     * @param <From>                   The type of the incoming items.
     * @param <To>                     The type of the transformed items.
     * @param <Receiver2ThrowableType> The exception type that the Receiver might throw.
     *
     * @return An Output instance that encapsulates the map transformation.
     */
    public static <From, To, Receiver2ThrowableType extends Throwable> Output<From, Receiver2ThrowableType> map( final Function<? super From, ? extends To> function,
                                                                                                                 final Output<To, Receiver2ThrowableType> output
    )
    {
        return new Output<From, Receiver2ThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends From, SenderThrowableType> sender )
                throws Receiver2ThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<To, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super To, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<From, ReceiverThrowableType>()
                        {
                            @Override
                            public void receive( From item )
                                throws ReceiverThrowableType
                            {
                                receiver.receive( function.apply( item ) );
                            }
                        } );
                    }
                } );
            }
        };
    }

    /**
     * Apply the given function to items in the transfer that match the given specification. Other items will pass
     * through directly.
     *
     * @param specification            The Specification defining which items should be transformed.
     * @param function                 The transformation function.
     * @param output                   The Output that will receive the resulting items.
     * @param <T>                      The item type. Items can not be transformed to a new type.
     * @param <Receiver2ThrowableType> The exception that the Receiver might throw.
     *
     * @return An Output instance that encapsulates the operation.
     */
    public static <T, Receiver2ThrowableType extends Throwable> Output<T, Receiver2ThrowableType> filteredMap( final Predicate<? super T> specification,
                                                                                                               final Function<? super T, ? extends T> function,
                                                                                                               final Output<T, Receiver2ThrowableType> output
    )
    {
        return new Output<T, Receiver2ThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends T, SenderThrowableType> sender )
                throws Receiver2ThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            @Override
                            public void receive( T item )
                                throws ReceiverThrowableType
                            {
                                if( specification.test( item ) )
                                {
                                    receiver.receive( function.apply( item ) );
                                }
                                else
                                {
                                    receiver.receive( item );
                                }
                            }
                        } );
                    }
                } );
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on
     * the sending side and a write-lock would be used on the receiving side. Inputs can use this as well to create a
     * wrapper on the send side when transferTo is invoked.
     *
     * @param lock                    the lock to be used for transfers
     * @param output                  output to be wrapped
     * @param <T>                     The Item type
     * @param <Receiver2ThrowableType> The Exception type that the Receiver might throw.
     *
     * @return Output wrapper that uses the given lock during transfers.
     */
    public static <T, Receiver2ThrowableType extends Throwable> Output<T, Receiver2ThrowableType> lock( final Lock lock,
                                                                                                      final Output<T, Receiver2ThrowableType> output
    )
    {
        return new Output<T, Receiver2ThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends T, SenderThrowableType> sender )
                throws Receiver2ThrowableType, SenderThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while( true )
                {
                    try
                    {
                        //noinspection StatementWithEmptyBody
                        while( !lock.tryLock( 1000, TimeUnit.MILLISECONDS ) )
                        {
                            // On timeout, try again
                        }
                        break; // Finally got a lock
                    }
                    catch( InterruptedException e )
                    {
                        // Try again
                    }
                }

                try
                {
                    output.receiveFrom( sender );
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on the sending side and a write-lock
     * would be used on the receiving side.
     *
     * @param lock                  the lock to be used for transfers
     * @param input                 input to be wrapped
     * @param <T>                   The item type.
     * @param <SenderThrowableType> The Exception type that the Sender might throw.
     *
     * @return Input wrapper that uses the given lock during transfers.
     */
    public static <T, SenderThrowableType extends Throwable> Input<T, SenderThrowableType> lock( final Lock lock,
                                                                                                 final Input<T, SenderThrowableType> input
    )
    {
        return new Input<T, SenderThrowableType>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super T, ReceiverThrowableType> output )
                throws SenderThrowableType, ReceiverThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while( true )
                {
                    try
                    {
                        //noinspection StatementWithEmptyBody
                        while( !( lock.tryLock() || lock.tryLock( 1000, TimeUnit.MILLISECONDS ) ) )
                        {
                            // On timeout, try again
                        }
                        break; // Finally got a lock
                    }
                    catch( InterruptedException e )
                    {
                        // Try again
                    }
                }

                try
                {
                    input.transferTo( output );
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * Count the number of items in the transfer.
     *
     * @param <T>
     */
    // START SNIPPET: counter
    public static class Counter<T>
        implements Function<T, T>
    {
        private volatile long count = 0;

        public long count()
        {
            return count;
        }

        @Override
        public T apply( T t )
        {
            count++;
            return t;
        }
    }
    // END SNIPPET: counter

    /**
     * Convert strings to bytes using the given CharSet
     */
    @SuppressWarnings( "UnusedDeclaration" )
    public static class String2Bytes
        implements Function<String, byte[]>
    {
        private Charset charSet;

        public String2Bytes( Charset charSet )
        {
            this.charSet = charSet;
        }

        @Override
        public byte[] apply( String s )
        {
            return s.getBytes( charSet );
        }
    }

    /**
     * Convert ByteBuffers to Strings using the given CharSet
     */
    public static class ByteBuffer2String
        implements Function<ByteBuffer, String>
    {
        private Charset charSet;

        public ByteBuffer2String( Charset charSet )
        {
            this.charSet = charSet;
        }

        @Override
        public String apply( ByteBuffer buffer )
        {
            return new String( buffer.array(), charSet );
        }
    }

    /**
     * Convert objects to Strings using .toString()
     */
    @SuppressWarnings( "UnusedDeclaration" )
    public static class ObjectToString
        implements Function<Object, String>
    {
        @Override
        public String apply( Object o )
        {
            return o.toString();
        }
    }

    /**
     * Log the toString() representation of transferred items to the given log. The string is first formatted using MessageFormat
     * with the given format.
     *
     * @param <T>
     */
    public static class Log<T>
        implements Function<T, T>
    {
        private Logger logger;
        private MessageFormat format;

        public Log( Logger logger, String format )
        {
            this.logger = logger;
            this.format = new MessageFormat( format );
        }

        @Override
        public T apply( T item )
        {
            logger.info( format.format( new String[]{ item.toString() } ) );
            return item;
        }
    }

    /**
     * Track progress of transfer by emitting a log message in given intervals.
     *
     * If logger or format is null, then you need to override the logProgress to do something
     *
     * @param <T> type of items to be transferred
     */
    // START SNIPPET: progress
    public static class ProgressLog<T>
        implements Function<T, T>
    {
        private Counter<T> counter;
        private Log<String> log;
        private final long interval;

        public ProgressLog( Logger logger, String format, long interval )
        {
            this.interval = interval;
            if( logger != null && format != null )
            {
                log = new Log<>( logger, format );
            }
            counter = new Counter<>();
        }

        public ProgressLog( long interval )
        {
            this.interval = interval;
            counter = new Counter<>();
        }

        @Override
        public T apply( T t )
        {
            counter.apply( t );
            if( counter.count % interval == 0 )
            {
                logProgress();
            }
            return t;
        }

        // Override this to do something other than logging the progress
        protected void logProgress()
        {
            if( log != null )
            {
                log.apply( counter.count + "" );
            }
        }
    }
    // END SNIPPET: progress
}
