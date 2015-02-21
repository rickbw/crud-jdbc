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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

import com.google.common.base.Optional;

import rx.Observable;
import rx.Subscriber;


public class ObservableQuery {

    private final Query query;


    public List<Type> getReturnTypes() {
        return Arrays.asList(this.query.getReturnTypes());
    }

    public String getQueryString() {
        return this.query.getQueryString();
    }

    public Optional<Integer> getMaxResults() {
        @Nullable Integer max = this.query.getMaxResults();
        if (max != null && max < 0) {
            max = null;
        }
        return Optional.fromNullable(max);
    }

    public ObservableQuery setMaxResults(final int maxResults) {
        this.query.setMaxResults(maxResults);
        return this;
    }

    public Optional<Integer> getFirstResult() {
        @Nullable Integer first = this.query.getFirstResult();
        if (first != null && (first == 0 || first < 0)) {
            first = null;
        }
        return Optional.fromNullable(first);
    }

    public ObservableQuery setFirstResult(final int firstResult) {
        this.query.setFirstResult(firstResult);
        return this;
    }

    public FlushMode getFlushMode() {
        return this.query.getFlushMode();
    }

    public ObservableQuery setFlushMode(final FlushMode flushMode) {
        this.query.setFlushMode(flushMode);
        return this;
    }

    public CacheMode getCacheMode() {
        return this.query.getCacheMode();
    }

    public ObservableQuery setCacheMode(final CacheMode cacheMode) {
        this.query.setCacheMode(cacheMode);
        return this;
    }

    public boolean isCacheable() {
        return this.query.isCacheable();
    }

    public ObservableQuery setCacheable(final boolean cacheable) {
        this.query.setCacheable(cacheable);
        return this;
    }

    public String getCacheRegion() {
        return this.query.getCacheRegion();
    }

    public ObservableQuery setCacheRegion(final String cacheRegion) {
        this.query.setCacheRegion(cacheRegion);
        return this;
    }

    public Optional<Integer> getTimeout() {
        return Optional.fromNullable(this.query.getTimeout());
    }

    public ObservableQuery setTimeout(final int timeout) {
        this.query.setTimeout(timeout);
        return this;
    }

    public Optional<Integer> getFetchSize() {
        return Optional.fromNullable(this.query.getFetchSize());
    }

    public ObservableQuery setFetchSize(final int fetchSize) {
        this.query.setFetchSize(fetchSize);
        return this;
    }

    public boolean isReadOnly() {
        return this.query.isReadOnly();
    }

    public ObservableQuery setReadOnly(final boolean readOnly) {
        this.query.setReadOnly(readOnly);
        return this;
    }

    public LockOptions getLockOptions() {
        return this.query.getLockOptions();
    }

    public ObservableQuery setLockOptions(final LockOptions lockOptions) {
        this.query.setLockOptions(lockOptions);
        return this;
    }

    public ObservableQuery setLockMode(final String alias, final LockMode lockMode) {
        this.query.setLockMode(alias, lockMode);
        return this;
    }

    public String getComment() {
        return this.query.getComment();
    }

    public ObservableQuery setComment(final String comment) {
        this.query.setComment(comment);
        return this;
    }

    public ObservableQuery addQueryHint(final String hint) {
        this.query.addQueryHint(hint);
        return this;
    }

    public List<String> getReturnAliases() {
        return Arrays.asList(this.query.getReturnAliases());
    }

    public List<String> getNamedParameters() {
        return Arrays.asList(this.query.getNamedParameters());
    }

    public Observable<Object> observe() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> sub) {
                final ScrollableResults scroller = ObservableQuery.this.query.scroll(ScrollMode.FORWARD_ONLY);
                while (scroller.next()) {
                    final Object wholeRecord = scroller.get(0);
                    sub.onNext(wholeRecord);
                }
                sub.onCompleted();
            }
        });
    }

    public Observable<Object> observeUnique() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> sub) {
                @Nullable final Object result = ObservableQuery.this.query.uniqueResult();
                if (result != null) {
                    sub.onNext(result);
                }
                sub.onCompleted();
            }
        });
    }

    public Observable<Integer> executeUpdate() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> sub) {
                final int rowsUpdated = ObservableQuery.this.query.executeUpdate();
                sub.onNext(rowsUpdated);
                sub.onCompleted();
            }
        });
    }

    /**
     * @see Query#setParameter(int, Object)
     * @see Query#setParameter(int, Object, Type)
     * @see Query#setParameter(String, Object)
     * @see Query#setParameter(String, Object, Type)
     */
    public ObservableQuery substitute(final Parameter param) {
        param.apply(this.query);
        return this;
    }

    /**
     * @see Query#setProperties(Object)
     */
    public ObservableQuery substituteBean(final Object bean) {
        this.query.setProperties(bean);
        return this;
    }

    /**
     * @see Query#setProperties(Map)
     */
    public ObservableQuery substituteProperties(final Map<? super String, ?> bean) {
        this.query.setProperties(bean);
        return this;
    }

    public ObservableQuery setResultTransformer(final ResultTransformer transformer) {
        this.query.setResultTransformer(transformer);
        return this;
    }

    /*package*/ ObservableQuery(@Nonnull final Query query) {
        this.query = Objects.requireNonNull(query);
    }

}
