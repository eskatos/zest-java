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

package org.apache.zest.library.restlet.assembly.infrastructue;

import java.util.function.Function;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.layered.ModuleAssembler;
import org.apache.zest.valueserialization.jackson.JacksonValueSerializationAssembler;

public class SerializationModule
    implements ModuleAssembler
{
    public static final String NAME = "Serialization Module";
    private final Function<Application, Module> typeFinder;

    public SerializationModule( Function<Application, Module> typeFinder )
    {
        this.typeFinder = typeFinder;
    }

    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        new JacksonValueSerializationAssembler()
            .visibleIn( Visibility.layer )
            .withValuesModuleFinder( typeFinder )
            .assemble( module );
        return module;
    }
}
