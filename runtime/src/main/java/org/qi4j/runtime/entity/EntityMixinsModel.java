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

package org.qi4j.runtime.entity;

import java.util.HashSet;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.State;
import org.qi4j.entity.Entity;
import org.qi4j.runtime.composite.AbstractMixinsModel;
import org.qi4j.runtime.composite.MixinDeclaration;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.UsesInstance;

/**
 * TODO
 */
public class EntityMixinsModel
    extends AbstractMixinsModel
{
    public EntityMixinsModel( Class<? extends Composite> compositeType )
    {
        super( compositeType );
        mixins.add( new MixinDeclaration( EntityMixin.class, Entity.class ) );
    }

    public void implementThisUsing( EntityModel entityModel )
    {
        Set<Class> thisMixinTypes = new HashSet<Class>();
        for( MixinModel mixinModel : mixinModels )
        {
            thisMixinTypes.addAll( mixinModel.thisMixinTypes() );
        }

        for( Class thisMixinType : thisMixinTypes )
        {
            entityModel.implementMixinType( thisMixinType );
        }
    }

    public void newMixins( EntityInstance entityInstance, State state, Object[] mixins )
    {
        int i = 0;
        for( MixinModel mixinModel : mixinModels )
        {
            mixins[ i++ ] = mixinModel.newInstance( entityInstance, UsesInstance.NO_USES, state );
        }
    }
}
