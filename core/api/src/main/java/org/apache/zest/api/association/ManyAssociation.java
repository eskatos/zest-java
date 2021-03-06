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

import java.util.List;
import java.util.Set;
import org.apache.zest.api.entity.EntityReference;

/**
 * Association to a collection of entities.
 */
public interface ManyAssociation<T> extends Iterable<T>, AbstractAssociation
{
    /**
     * Returns the number of references in this association.
     * @return the number of references in this association.
     */
    int count();

    boolean contains( T entity );

    boolean add( int i, T entity );

    boolean add( T entity );

    boolean remove( T entity );

    T get( int i );

    List<T> toList();

    Set<T> toSet();

    /**
     * Returns an unmodifiable Iterable of the references to the associated entities.
     * @return the references to the associated entities.
     */
    Iterable<EntityReference> references();
}
