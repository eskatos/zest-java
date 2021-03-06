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
package org.apache.zest.runtime.composite;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.functional.Iterables;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.ManyAssociationState;
import org.apache.zest.spi.entity.NamedAssociationState;

/**
 * Function based StateResolver.
 */
public class FunctionStateResolver
    implements StateResolver
{
    final Function<PropertyDescriptor, Object> propertyFunction;
    final Function<AssociationDescriptor, EntityReference> associationFunction;
    final Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction;
    final Function<AssociationDescriptor, Map<String, EntityReference>> namedAssociationFunction;

    public FunctionStateResolver( Function<PropertyDescriptor, Object> propertyFunction,
                                  Function<AssociationDescriptor, EntityReference> associationFunction,
                                  Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction,
                                  Function<AssociationDescriptor, Map<String, EntityReference>> namedAssociationFunction )
    {
        this.propertyFunction = propertyFunction;
        this.associationFunction = associationFunction;
        this.manyAssociationFunction = manyAssociationFunction;
        this.namedAssociationFunction = namedAssociationFunction;
    }

    @Override
    public Object getPropertyState( PropertyDescriptor propertyDescriptor )
    {
        return propertyFunction.apply( propertyDescriptor );
    }

    @Override
    public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
    {
        return associationFunction.apply( associationDescriptor );
    }

    @Override
    public List<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
    {
        return Iterables.toList( manyAssociationFunction.apply( associationDescriptor ) );
    }

    @Override
    public Map<String, EntityReference> getNamedAssociationState( AssociationDescriptor associationDescriptor )
    {
        return namedAssociationFunction.apply( associationDescriptor );
    }

    public void populateState( EntityModel model, EntityState state )
    {
        model.state().properties().forEach( propDesc -> {
            Object value = getPropertyState( propDesc );
            state.setPropertyValue( propDesc.qualifiedName(), value );
        } );
        model.state().associations().forEach( assDesc -> {
            EntityReference ref = getAssociationState( assDesc );
            state.setAssociationValue( assDesc.qualifiedName(), ref );
        } );
        model.state().manyAssociations().forEach( manyAssDesc -> {
            ManyAssociationState associationState = state.manyAssociationValueOf( manyAssDesc.qualifiedName() );
            // First clear existing ones
            for( EntityReference ref : associationState )
            {
                associationState.remove( ref );
            }
            // then add the new ones.
            for( EntityReference ref : getManyAssociationState( manyAssDesc ) )
            {
                associationState.add( 0, ref );
            }
        } );
        model.state().namedAssociations().forEach( namedAssDesc -> {
            NamedAssociationState associationState = state.namedAssociationValueOf( namedAssDesc.qualifiedName() );
            // First clear existing ones
            for( String name : associationState )
            {
                associationState.remove( name );
            }
            // then add the new ones.
            for( Map.Entry<String, EntityReference> entry : getNamedAssociationState( namedAssDesc ).entrySet() )
            {
                associationState.put( entry.getKey(), entry.getValue() );
            }
        } );
    }
}
