/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.property;

import java.lang.reflect.Type;

/**
 * Provide access to metadata information about a property.
 * The {@link Property} interface extends this one, so there
 * is always easy access to it if you have a reference to a Property.
 */
public interface PropertyInfo
{
    /**
     * Access metadata about the property with a given type.
     * The info is registered for the property during
     * assembly of the application.
     *
     * @param infoType the type of the metadata to return
     * @return a metadata object that implements the requested type or null if none is registered
     */
    <T> T getPropertyInfo( Class<T> infoType );

    /**
     * Get the name of the property, which is equal to the name of the metod that declared it.
     *
     * @return the name of the property.
     */
    String getName();

    /**
     * Get the qualified name of the property which is equal to:<br/>
     * <interface name>:<method name>
     *
     * @return the qualified name of the property
     */
    String getQualifiedName();

    /**
     * Get the type of the property. If the property is declared
     * as Property<X> then X is returned.
     *
     * @return the property type
     */
    Type getPropertyType();
}
