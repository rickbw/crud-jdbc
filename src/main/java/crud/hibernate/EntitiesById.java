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
import javax.annotation.Nullable;

import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LockOptions;
import org.hibernate.Session;

import rx.Observable;
import rx.functions.Func1;


public class EntitiesById<T> {

    private final IdentifierLoadAccess access;
    private final Class<T> clazz;

    private transient Func1<Serializable, T> cachedLoadFunc = null;
    private transient Func1<Serializable, Observable<T>> cachedGetFunc = null;


    /**
     * @return  An {@link EntitiesById} identical to this one, but using the
     *          given {@link LockOptions}.
     */
    public final EntitiesById<T> withOptions(final LockOptions lockOptions) {
        final IdentifierLoadAccess newAccess = this.access.with(lockOptions);
        return new EntitiesById<>(newAccess, this.clazz);
    }

    public final Func1<Serializable, Observable<T>> get() {
        if (this.cachedGetFunc == null) {
            this.cachedGetFunc = new GetFunction();
        }
        return this.cachedGetFunc;
    }

    public final Func1<Serializable, T> load() {
        if (this.cachedLoadFunc == null) {
            this.cachedLoadFunc = new LoadFunction();
        }
        return this.cachedLoadFunc;
    }

    /*package*/ EntitiesById(final Session session, final Class<T> clazz) {
        this(session.byId(clazz), clazz);
    }

    private EntitiesById(final IdentifierLoadAccess access, final Class<T> clazz) {
        this.access = Objects.requireNonNull(access);
        this.clazz = Objects.requireNonNull(clazz);
    }


    private final class LoadFunction implements Func1<Serializable, T> {
        @Override
        public T call(final Serializable id) {
            @Nonnull final Object entity = EntitiesById.this.access.load(id);
            return EntitiesById.this.clazz.cast(entity);
        }
    }


    private final class GetFunction implements Func1<Serializable, Observable<T>> {
        @Override
        public Observable<T> call(final Serializable id) {
            @Nullable final Object entity = EntitiesById.this.access.getReference(id);
            if (entity != null) {
                return Observable.just(entity).cast(EntitiesById.this.clazz);
            } else {
                return Observable.empty();
            }
        }
    }

}
