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

package org.apache.zest.runtime.entity.associations;

import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

/**
 * Test that associations can be marked as @Immutable
 */
public class ImmutableAssociationTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
    }

    @Test
    public void givenEntityWithImmutableAssociationWhenBuildingThenNoException()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity father = unitOfWork.newEntity( PersonEntity.class );

            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity instance = builder.instance();
            instance.father().set( father );
            PersonEntity child = builder.newInstance();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test( expected = IllegalStateException.class )
    public void givenEntityWithImmutableAssociationWhenChangingValueThenThrowException()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity father = builder.instance();
            father = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity child = builder.instance();
            child = builder.newInstance();

            child.father().set( father );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void givenEntityWithImmutableManyAssociationWhenBuildingThenNoException()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person1 = builder.instance();
            person1 = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person2 = builder.instance();
            person2.colleagues().add( 0, person1 );
            person2 = builder.newInstance();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test( expected = IllegalStateException.class )
    public void givenEntityWithImmutableManyAssociationWhenChangingValueThenThrowException()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person1 = builder.instance();
            person1 = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person2 = builder.instance();
            person2 = builder.newInstance();

            person1.colleagues().add( 0, person2 );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    interface PersonEntity
        extends EntityComposite
    {
        @Optional
        @Immutable
        Association<PersonEntity> father();

        @Immutable
        ManyAssociation<PersonEntity> children();

        @Immutable
        ManyAssociation<PersonEntity> friends();

        @Immutable
        ManyAssociation<PersonEntity> colleagues();
    }
}
