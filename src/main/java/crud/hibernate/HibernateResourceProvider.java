/* Copyright 2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package crud.hibernate;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.SessionFactory;

import crud.DeletableResourceProvider;
import crud.ReadableResourceProvider;
import crud.WritableResourceProvider;


/**
 * Provide instances of {@link HibernateResource} for interacting with
 * different persistent entities of the same type.
 *
 * @param <ID>  The type of the entities' unique identifiers. Hibernate uses
 *              these identifiers to look up those entities.
 * @param <T>   The type of the entities themselves. Hibernate must be aware
 *              of this type.
 */
public class HibernateResourceProvider<ID extends Serializable, T>
implements ReadableResourceProvider<ID, T>,
           WritableResourceProvider<ID, T, Void>,
           DeletableResourceProvider<ID, Void> {

    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;


    public HibernateResourceProvider(
            final SessionFactory sessionFactory,
            final Class<T> entityClass) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory);
        this.entityClass = Objects.requireNonNull(entityClass);
    }

    @Override
    public HibernateResource<T> get(final ID entityId) {
        return HibernateResource.create(this.sessionFactory, this.entityClass, entityId);
    }

}
