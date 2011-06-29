/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.property;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for types of values in ValueComposites and Properties.
 */
public class ValueType
{
    protected final Class<?> type;

    public ValueType( Class<?> type )
    {
        this.type = type;
    }

    public Class<?> type()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return type.getName();
    }
}