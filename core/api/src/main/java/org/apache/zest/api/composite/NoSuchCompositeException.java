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
package org.apache.zest.api.composite;

import org.apache.zest.api.common.InvalidApplicationException;

/**
 * This exception is thrown if client code tries to create a non-existing Composite type.
 */
public class NoSuchCompositeException
    extends InvalidApplicationException
{
    private static final long serialVersionUID = 1L;

    private final String compositeType;
    private final String moduleName;
    private final String visibleTypes;

    protected NoSuchCompositeException( String metaType, String compositeType, String moduleName, String visibleTypes )
    {
        super( "Could not find any visible " + metaType + " of type [" + compositeType + "] in module [" +
               moduleName + "].\n" + visibleTypes );
        this.compositeType = compositeType;
        this.moduleName = moduleName;
        this.visibleTypes = visibleTypes;
    }

    public String compositeType()
    {
        return compositeType;
    }

    public String moduleName()
    {
        return moduleName;
    }

    public String visibleTypes()
    {
        return visibleTypes;
    }
}
