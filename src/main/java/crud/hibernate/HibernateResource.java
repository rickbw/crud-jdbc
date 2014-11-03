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

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import crud.DeletableResource;
import crud.ReadableResource;
import crud.Resource;
import crud.ResourceProvider;
import crud.WritableResource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


/**
 * Access and update a single persistent entity, identified by a unique
 * ID. Instances of this {@link Resource} may be obtained from a
 * {@link HibernateResourceProvider}.
 */
public abstract class HibernateResource<T>
implements ReadableResource<T>,
           WritableResource<T, Void>,
           DeletableResource<Void> {

    /**
     * Retrieve the entity associated with this {@link Resource}, if any.
     */
    @Override
    public abstract Observable<T> get();

    /**
     * Set the value of this {@link Resource} to the given new value.
     *
     * @return  An empty {@link Observable}, which will call
     *          {@link Observer#onCompleted()} once the write has completed.
     */
    @Override
    public abstract Observable<Void> write(final T newValue);

    /**
     * Remove the value from the data store, such that {@link #get()}
     * subsequently would return an empty {@link Observable}.
     *
     * @return  An empty {@link Observable}, which will call
     *          {@link Observer#onCompleted()} once the entity has been
     *          removed.
     */
    @Override
    public abstract Observable<Void> delete();

    /**
     * For the use of the {@link ResourceProvider}.
     */
    /*package*/ static <ID extends Serializable, T> HibernateResource<T> create(
            final SessionFactory sessionFactory,
            final Class<T> entityClass,
            final ID entityId) {
        return new GenericHibernateResource<>(sessionFactory, entityClass, entityId);
    }

    private HibernateResource() {
        // prevent external instantiation
    }


    private static final class GenericHibernateResource<ID extends Serializable, T>
    extends HibernateResource<T> {
        private final SessionFactory sessionFactory;
        private final Class<T> entityClass;
        private final ID entityId;

        public GenericHibernateResource(
                final SessionFactory sessionFactory,
                final Class<T> entityClass,
                final ID entityId) {
            super();
            this.sessionFactory = Objects.requireNonNull(sessionFactory);
            this.entityClass = Objects.requireNonNull(entityClass);
            this.entityId = Objects.requireNonNull(entityId);
        }

        @Override
        public Observable<T> get() {
            /* TODO: Take this operation off of the current thread, while
             * maintaining a well-defined ordering of operations.
             */
            return Observable.create(new Observable.OnSubscribe<T>() {
                @Override
                public void call(final Subscriber<? super T> subscriber) {
                    /* TODO: Where can the current Session come from, since
                     * the caller doesn't know on what thread the observation
                     * runs?
                     */
                    final Session session = sessionFactory.getCurrentSession();
                    /* TODO: If we could assume that the entity exists, we could use
                     * load() instead of get(), which would be faster up front, because
                     * actual loading can be lazy.
                     */
                    @Nullable final Object entity = session.get(entityClass, entityId);
                    if (entity != null) {
                        final T typeSafeEntity = entityClass.cast(entity);
                        subscriber.onNext(typeSafeEntity);
                    }
                    subscriber.onCompleted();
                }
            });
        }

        @Override
        public Observable<Void> write(final T newValue) {
            /* TODO: Take this operation off of the current thread, while
             * maintaining a well-defined ordering of operations.
             */
            return Observable.create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(final Subscriber<? super Void> subscriber) {
                    final Session session = sessionFactory.getCurrentSession();
                    /* TODO: If we could assume that the entity already exists, we
                     * could use update() instead of saveOrUpdate(). (We can't use
                     * save() in any case, because it's not idempotent.)
                     */
                    session.saveOrUpdate(newValue);
                    subscriber.onCompleted();
                }
            });
        }

        @Override
        public Observable<Void> delete() {
            /* TODO: Take this operation off of the current thread, while
             * maintaining a well-defined ordering of operations.
             */
            return Observable.create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(final Subscriber<? super Void> subscriber) {
                    final Session session = sessionFactory.getCurrentSession();
                    /* Unfortunately, Hibernate doesn't provide an API to delete based
                     * on the ID; we need to actually have the persistent entity itself
                     * before we can delete it. To keep that cheap, since we only need
                     * the ID, we use load() instead of get(), which can return either
                     * the entity from the local session (if it already exists) or a
                     * lazy-loading proxy. Then we delete that proxy -- hopefully
                     * without it ever fully initializing from the data store.
                     */
                    final Object entityOrProxy = session.load(entityClass, entityId);
                    session.delete(entityOrProxy);
                    subscriber.onCompleted();
                }
            });
        }
    }

}
