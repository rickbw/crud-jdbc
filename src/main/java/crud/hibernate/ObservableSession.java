/* Copyright 2015 Rick Warren
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

import javax.annotation.Nonnull;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionEventListener;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Function;


public class ObservableSession implements AutoCloseable {

    private final Session session;

    private transient Func1<Object, Object> cachedDeleter = null;
    private transient Func1<Object, Object> cachedMerger = null;
    private transient Func1<Object, Object> cachedPersister = null;
    private transient Func1<Object, Serializable> cachedSaver = null;
    private transient Func1<Object, Object> cachedSaveOrUpdater = null;
    private transient Func1<Object, Object> cachedUpdater = null;


    /**
     * @see Session#beginTransaction()
     */
    public CloseableTransaction openTransaction() {
        final Transaction tx = this.session.beginTransaction();
        return new CloseableTransaction(tx);
    }

    /**
     * @see Session#getTransaction()
     */
    public Transaction getTransaction() {
        return this.session.getTransaction();
    }

    /**
     * Query one or more objects of the given class by their unique IDs, as
     * by {@link IdentifierLoadAccess}. Eventually, you will call
     * {@link ObservableIdentifierLoadAccess#getter()} or {@link ObservableIdentifierLoadAccess#loader()}.
     *
     * @see Session#byId(Class)
     */
    public <T> ObservableIdentifierLoadAccess<T> byId(final Class<T> clazz) {
        return new ObservableIdentifierLoadAccess<>(this.session, clazz);
    }

    /**
     * @return  A {@link Function} that will delete any object passed to it,
     *          and then return that object. For use with
     *          {@link Observable#map(Func1)}.
     */
    public Func1<Object, Object> deleter() {
        if (this.cachedDeleter == null) {
            this.cachedDeleter = new Func1<Object, Object>() {
                @Override
                public Object call(final Object deleteMe) {
                    ObservableSession.this.session.delete(deleteMe);
                    return deleteMe;
                }
            };
        }
        return this.cachedDeleter;
    }

    public Func1<Object, Object> merger() {
        if (this.cachedMerger == null) {
            this.cachedMerger = new Func1<Object, Object>() {
                @Override
                public Object call(final Object mergeMe) {
                    return ObservableSession.this.session.merge(mergeMe);
                }
            };
        }
        return this.cachedMerger;
    }

    public Func1<Object, Object> persister() {
        if (this.cachedPersister == null) {
            this.cachedPersister = new Func1<Object, Object>() {
                @Override
                public Object call(final Object persistMe) {
                    ObservableSession.this.session.persist(persistMe);
                    return persistMe;
                }
            };
        }
        return this.cachedPersister;
    }

    public Func1<Object, Serializable> saver() {
        if (this.cachedSaver == null) {
            this.cachedSaver = new Func1<Object, Serializable>() {
                @Override
                public Serializable call(final Object saveMe) {
                    return ObservableSession.this.session.save(saveMe);
                }
            };
        }
        return this.cachedSaver;
    }

    public Func1<Object, Object> saveOrUpdater() {
        if (this.cachedSaveOrUpdater == null) {
            this.cachedSaveOrUpdater = new Func1<Object, Object>() {
                @Override
                public Object call(final Object saveOrUpdateMe) {
                    ObservableSession.this.session.saveOrUpdate(saveOrUpdateMe);
                    return saveOrUpdateMe;
                }
            };
        }
        return this.cachedSaveOrUpdater;
    }

    public Func1<Object, Object> updater() {
        if (this.cachedUpdater == null) {
            this.cachedUpdater = new Func1<Object, Object>() {
                @Override
                public Object call(final Object updateMe) {
                    ObservableSession.this.session.update(updateMe);
                    return updateMe;
                }
            };
        }
        return this.cachedUpdater;
    }

    /**
     * @return  A function to map entities to their identifiers, as with
     *          {@link Session#getIdentifier(Object)}. For use with
     *          {@link Observable#map(Func1)}.
     */
    public Func1<Object, Serializable> identifierGetter() {
        return new Func1<Object, Serializable>() {
            @Override
            public Serializable call(final Object entity) {
                return ObservableSession.this.session.getIdentifier(entity);
            }
        };
    }

    /**
     * @return  A {@link Function} that will overwrite the state of the given
     *          entity with that of the corresponding entity with the given ID.
     *
     * @see Session#load(Object, Serializable)
     */
    public Func1<Object, Object> overwriter(final Serializable id) {
        return new Func1<Object, Object>() {
            @Override
            public Object call(final Object realDstEntity) {
                ObservableSession.this.session.load(realDstEntity, id);
                return realDstEntity;
            }
        };
    }

    public ObservableCriteria createCriteria(final Class<?> persistentClass) {
        final Criteria criteria = this.session.createCriteria(persistentClass);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(
            final Class<?> persistentClass,
            final String alias) {
        final Criteria criteria = this.session.createCriteria(persistentClass, alias);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(final String entityName) {
        final Criteria criteria = this.session.createCriteria(entityName);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(final String entityName, final String alias) {
        final Criteria criteria = this.session.createCriteria(entityName, alias);
        return new ObservableCriteria(criteria);
    }

    public ObservableQuery getNamedQuery(final String queryName) {
        final Query query = this.session.getNamedQuery(queryName);
        return new ObservableQuery(query);
    }

    public ObservableQuery createQuery(final String queryString) {
        final Query query = this.session.createQuery(queryString);
        return new ObservableQuery(query);
    }

    public void cancelQuery() {
        this.session.cancelQuery();
    }

    public void clear() {
        this.session.clear();
    }

    public void flush() {
        this.session.flush();
    }

    public FlushMode getFlushMode() {
        return this.session.getFlushMode();
    }

    public void setFlushMode(final FlushMode flushMode) {
        this.session.setFlushMode(flushMode);
    }

    public CacheMode getCacheMode() {
        return this.session.getCacheMode();
    }

    public void setCacheMode(final CacheMode cacheMode) {
        this.session.setCacheMode(cacheMode);
    }

    public boolean isOpen() {
        return this.session.isOpen();
    }

    public boolean isConnected() {
        return this.session.isConnected();
    }

    public boolean isDirty() {
        return this.session.isDirty();
    }

    public boolean isDefaultReadOnly() {
        return this.session.isDefaultReadOnly();
    }

    public void setDefaultReadOnly(final boolean readOnly) {
        this.session.setDefaultReadOnly(readOnly);
    }

    public TypeHelper getTypeHelper() {
        return this.session.getTypeHelper();
    }

    public LobHelper getLobHelper() {
        return this.session.getLobHelper();
    }

    public void addEventListeners(final SessionEventListener... listeners) {
        this.session.addEventListeners(listeners);
    }

    @Override
    public void close() {
        this.session.close();
    }

    /*package*/ ObservableSession(@Nonnull final Session session) {
        this.session = Objects.requireNonNull(session);
    }

}
