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
package org.apache.zest.api.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.apache.zest.api.util.Classes.typeOf;

/**
 * Generic Association info.
 */
public final class GenericAssociationInfo
{
    public static Type associationTypeOf( AccessibleObject accessor )
    {
        return toAssociationType( typeOf( accessor ) );
    }

    public static Type toAssociationType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }
        if (!(methodReturnType instanceof Class))
        {
            throw new IllegalArgumentException( "Unable to make an association with " + methodReturnType );
        }
        Type[] interfaces = ((Class<?>) methodReturnType).getGenericInterfaces();
        for (Type anInterface : interfaces)
        {
            Type associationType = toAssociationType(anInterface);
            if (associationType != null)
            {
                return associationType;
            }
        }
        return null;
    }
}
