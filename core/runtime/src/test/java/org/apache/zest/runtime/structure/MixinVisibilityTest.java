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

package org.apache.zest.runtime.structure;

import org.junit.Test;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.NoSuchTransientException;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.ApplicationAssemblerAdapter;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.ModuleAssembly;

import static org.junit.Assert.assertEquals;

/**
 * JAVADOC
 */
public class MixinVisibilityTest
{
    @Test
    public void testMixinInModuleIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     new Assembler()
                     {
                         public void assemble( ModuleAssembly module )
                             throws AssemblyException
                         {
                             module.setName( "Module A" );
                             module.transients( B1Composite.class );
                             module.objects( ObjectA.class );
                         }
                     }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInModuleWillFail()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     new Assembler()
                     {
                         public void assemble( ModuleAssembly module )
                             throws AssemblyException
                         {
                             module.setName( "Module A" );
                             module.transients( B1Composite.class, B2Composite.class );
                             module.objects( ObjectA.class );
                         }
                     }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = NoSuchTransientException.class )
    public void testMixinInLayerIsNotVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class );
                          }
                      }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLayerIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailSameModule()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class, B2Composite.class )
                                  .visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailDiffModule()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  { // Module 1
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module A" );
                            module.objects( ObjectA.class );
                        }
                    }
                  },
                  { // Module 2
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module B" );
                            module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                        }
                    }
                  },
                  { // Module 3
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module C" );
                            module.transients( B2Composite.class ).visibleIn( Visibility.layer );
                        }
                    }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    // @Test( expected= MixinTypeNotAvailableException.class )

    public void testMixinInLowerLayerIsNotVisible()
        throws Exception
    {

        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      }
                  }
                },
                { // Layer 2
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module " ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLowerLayerIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      }
                  }
                },
                { // Layer 2
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class ).visibleIn( Visibility.application );
                          }
                      }
                  }
                }
            };

        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    class AssemblerB
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module B" );
            module.transients( B1Composite.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure
        TransientBuilderFactory cbf;

        String test1()
        {
            B1 instance = cbf.newTransient( B1.class );
            return instance.test();
        }

        String test2()
        {
            TransientBuilder<B2> builder = cbf.newTransientBuilder( B2.class );
            builder.prototypeFor( B2.class ).b2().set( "abc" );
            B2 instance = builder.newInstance();
            return instance.b2().get();
        }
    }

    @Mixins( { MixinB.class } )
    public interface B1Composite
        extends TransientComposite, B1
    {
    }

    public interface B2Composite
        extends TransientComposite, B2
    {
    }

    public interface B2
    {
        @Optional
        Property<String> b2();
    }

    public interface B1
        extends B2
    {
        String test();
    }

    public abstract static class MixinB
        implements B1
    {
        public String test()
        {
            return "ok";
        }
    }
}