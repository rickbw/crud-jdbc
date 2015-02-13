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

import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Preconditions;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Function;


public class ObservableTransaction implements AutoCloseable {

    private final Session session;
    private final Transaction transaction;

    private boolean open = true;
    private Func1<Object, Object> cachedDeleter = null;
    private Func1<Object, Object> cachedMerger = null;
    private Func1<Object, Object> cachedPersister = null;
    private Func1<Object, Serializable> cachedSaver = null;
    private Func1<Object, Object> cachedSaveOrUpdater = null;
    private Func1<Object, Object> cachedUpdater = null;


    /**
     * Query one or more objects of the given class by their unique IDs, as
     * by {@link IdentifierLoadAccess}. Eventually, you will call
     * {@link EntitiesById#fetch()}, for use with
     * {@link Observable#flatMap(Func1)}.
     */
    public <T> EntitiesById<T> byId(final Class<T> clazz) {
        return new EntitiesById<>(this.session, clazz);
    }

    /**
     * @return  A {@link Function} that will delete any object passed to it,
     *          and then return that object. For use with
     *          {@link Observable#map(Func1)}.
     */
    public Func1<Object, Object> delete() {
        if (this.cachedDeleter == null) {
            this.cachedDeleter = new Func1<Object, Object>() {
                @Override
                public Object call(final Object deleteMe) {
                    checkOpen();
                    ObservableTransaction.this.session.delete(deleteMe);
                    return deleteMe;
                }
            };
        }
        return this.cachedDeleter;
    }

    public Func1<Object, Object> merge() {
        if (this.cachedMerger == null) {
            this.cachedMerger = new Func1<Object, Object>() {
                @Override
                public Object call(final Object mergeMe) {
                    checkOpen();
                    return ObservableTransaction.this.session.merge(mergeMe);
                }
            };
        }
        return this.cachedMerger;
    }

    public Func1<Object, Object> persist() {
        if (this.cachedPersister == null) {
            this.cachedPersister = new Func1<Object, Object>() {
                @Override
                public Object call(final Object persistMe) {
                    checkOpen();
                    ObservableTransaction.this.session.persist(persistMe);
                    return persistMe;
                }
            };
        }
        return this.cachedPersister;
    }

    public Func1<Object, Serializable> save() {
        if (this.cachedSaver == null) {
            this.cachedSaver = new Func1<Object, Serializable>() {
                @Override
                public Serializable call(final Object saveMe) {
                    checkOpen();
                    return ObservableTransaction.this.session.save(saveMe);
                }
            };
        }
        return this.cachedSaver;
    }

    public Func1<Object, Object> saveOrUpdate() {
        if (this.cachedSaveOrUpdater == null) {
            this.cachedSaveOrUpdater = new Func1<Object, Object>() {
                @Override
                public Object call(final Object saveOrUpdateMe) {
                    checkOpen();
                    ObservableTransaction.this.session.saveOrUpdate(saveOrUpdateMe);
                    return saveOrUpdateMe;
                }
            };
        }
        return this.cachedSaveOrUpdater;
    }

    public Func1<Object, Object> update() {
        if (this.cachedUpdater == null) {
            this.cachedUpdater = new Func1<Object, Object>() {
                @Override
                public Object call(final Object updateMe) {
                    checkOpen();
                    ObservableTransaction.this.session.update(updateMe);
                    return updateMe;
                }
            };
        }
        return this.cachedUpdater;
    }

    public void commit() {
        checkOpen();
        this.transaction.commit();
        this.open = false;
    }

    public void rollback() {
        checkOpen();
        this.transaction.rollback();
        this.open = false;
    }

    @Override
    public void close() {
        if (this.open) {
            rollback();
        }
    }

    /*package*/ ObservableTransaction(final Session session) {
        this.session = session;
        this.transaction = this.session.beginTransaction();
    }

    private void checkOpen() {
        Preconditions.checkState(ObservableTransaction.this.open, "Already committed or rolled back");
    }

}
