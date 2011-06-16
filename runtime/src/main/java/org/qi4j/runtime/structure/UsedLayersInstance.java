/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.object.ObjectDescriptor;

import static org.qi4j.runtime.structure.VisibilitySpecification.APPLICATION;

/**
 * JAVADOC
 */
public final class UsedLayersInstance
{
    private final List<LayerInstance> usedLayerInstances;

    public UsedLayersInstance( List<LayerInstance> usedLayerInstances )
    {
        this.usedLayerInstances = usedLayerInstances;
    }

    Iterable<ModelModule<ObjectModel>> visibleObjects()
    {
        return Iterables.flattenIterables( Iterables.map( new Function<LayerInstance, Iterable<ModelModule<ObjectModel>>>()
        {
            @Override
            public Iterable<ModelModule<ObjectModel>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleObjects( Visibility.application);
            }
        }, usedLayerInstances ));
    }

    Iterable<ModelModule<TransientModel>> visibleTransients()
    {
        return Iterables.flattenIterables( Iterables.map( new Function<LayerInstance, Iterable<ModelModule<TransientModel>>>()
        {
            @Override
            public Iterable<ModelModule<TransientModel>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleTransients( Visibility.application);
            }
        }, usedLayerInstances ));
    }

    Iterable<ModelModule<EntityModel>> visibleEntities()
    {
        return Iterables.flattenIterables( Iterables.map( new Function<LayerInstance, Iterable<ModelModule<EntityModel>>>()
        {
            @Override
            public Iterable<ModelModule<EntityModel>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleEntities( Visibility.application);
            }
        }, usedLayerInstances ));
    }

    Iterable<ModelModule<ValueModel>> visibleValues()
    {
        return Iterables.flattenIterables( Iterables.map( new Function<LayerInstance, Iterable<ModelModule<ValueModel>>>()
        {
            @Override
            public Iterable<ModelModule<ValueModel>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleValues( Visibility.application);
            }
        }, usedLayerInstances ));
    }

    Iterable<ServiceReference> visibleServices()
    {
        return Iterables.flattenIterables( Iterables.map( new Function<LayerInstance, Iterable<ServiceReference>>()
        {
            @Override
            public Iterable<ServiceReference> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleServices(Visibility.application);
            }
        }, usedLayerInstances ));
    }
}
