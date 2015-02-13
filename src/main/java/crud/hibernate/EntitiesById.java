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
import rx.functions.Function;


public class EntitiesById<T> {

    private final IdentifierLoadAccess access;
    private final Class<T> clazz;

    private transient Func1<Serializable, Observable<T>> cachedFunc = null;


    /**
     * @return  An {@link EntitiesById} identical to this one, but using the
     *          given {@link LockOptions}.
     */
    public final EntitiesById<T> withOptions(final LockOptions lockOptions) {
        final IdentifierLoadAccess newAccess = this.access.with(lockOptions);
        return doDeepCopy(newAccess, this.clazz);
    }

    /**
     * @return  An {@link EntitiesById} identical to this one, but that
     *          assumes that identified entities are always present. This
     *          assumption allows objects to be returned faster, by means
     *          of lazy proxies.
     */
    public EntitiesById<T> assumePresent() {
        return new ProxyLoader<>(this.access, this.clazz);
    }

    /**
     * @return  A {@link Function} that retrieves entities by a given ID, for
     *          use with {@link Observable#flatMap(Func1)}.
     */
    public final Func1<Serializable, Observable<T>> fetch() {
        if (this.cachedFunc == null) {
            this.cachedFunc = doCreateFunction();
        }
        return this.cachedFunc;
    }

    /*package*/ EntitiesById<T> doDeepCopy(final IdentifierLoadAccess newAccess, final Class<T> newClazz) {
        return new EntitiesById<>(newAccess, newClazz);
    }

    /*package*/ Func1<Serializable, Observable<T>> doCreateFunction() {
        return new LoadFunction();
    }

    /*package*/ EntitiesById(final Session session, final Class<T> clazz) {
        this(session.byId(clazz), clazz);
    }

    private EntitiesById(final IdentifierLoadAccess access, final Class<T> clazz) {
        this.access = Objects.requireNonNull(access);
        this.clazz = Objects.requireNonNull(clazz);
    }


    private static final class ProxyLoader<T> extends EntitiesById<T> {
        @Override
        public EntitiesById<T> assumePresent() {
            return this; // no change
        }

        @Override
        /*package*/ EntitiesById<T> doDeepCopy(final IdentifierLoadAccess newAccess, final Class<T> newClazz) {
            return new ProxyLoader<>(newAccess, newClazz);
        }

        @Override
        /*package*/ Func1<Serializable, Observable<T>> doCreateFunction() {
            return new GetFunction();
        }

        private ProxyLoader(final IdentifierLoadAccess access, final Class<T> clazz) {
            super(access, clazz);
        }

        private final class GetFunction implements Func1<Serializable, Observable<T>> {
            @Override
            public Observable<T> call(final Serializable id) {
                @Nonnull final Object entity = ProxyLoader.super.access.getReference(id);
                return Observable.just(entity).cast(ProxyLoader.super.clazz);
            }
        }
    }


    private final class LoadFunction implements Func1<Serializable, Observable<T>> {
        @Override
        public Observable<T> call(final Serializable id) {
            @Nullable final Object entity = EntitiesById.this.access.load(id);
            if (entity != null) {
                return Observable.just(entity).cast(EntitiesById.this.clazz);
            } else {
                return Observable.empty();
            }
        }
    }

}
