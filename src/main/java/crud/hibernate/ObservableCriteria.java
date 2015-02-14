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

import java.util.Objects;

import javax.annotation.Nullable;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

import rx.Observable;
import rx.Subscriber;


public class ObservableCriteria {

    private final Criteria delegate;


    /*package*/ ObservableCriteria(final Criteria delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public Observable<Object> observe() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> sub) {
                final ScrollableResults scroller = ObservableCriteria.this.delegate.scroll(ScrollMode.FORWARD_ONLY);
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
                @Nullable final Object result = ObservableCriteria.this.delegate.uniqueResult();
                if (result != null) {
                    sub.onNext(result);
                }
                sub.onCompleted();
            }
        });
    }

    public String getAlias() {
        return this.delegate.getAlias();
    }

    public ObservableCriteria setProjection(final Projection projection) {
        this.delegate.setProjection(projection);
        return this;
    }

    public ObservableCriteria add(final Criterion criterion) {
        this.delegate.add(criterion);
        return this;
    }

    public ObservableCriteria addOrder(final Order order) {
        this.delegate.addOrder(order);
        return this;
    }

    public ObservableCriteria setFetchMode(
            final String associationPath,
            final FetchMode mode) {
        this.delegate.setFetchMode(associationPath, mode);
        return this;
    }

    public ObservableCriteria setLockMode(final LockMode lockMode) {
        this.delegate.setLockMode(lockMode);
        return this;
    }

    public ObservableCriteria setLockMode(
            final String alias,
            final LockMode lockMode) {
        this.delegate.setLockMode(alias, lockMode);
        return this;
    }

    public ObservableCriteria createAlias(
            final String associationPath,
            final String alias) {
        this.delegate.createAlias(associationPath, alias);
        return this;
    }

    public ObservableCriteria createAlias(
            final String associationPath,
            final String alias,
            final JoinType joinType) {
        this.delegate.createAlias(associationPath, alias, joinType);
        return this;
    }

    public ObservableCriteria createAlias(
            final String associationPath,
            final String alias,
            final JoinType joinType,
            final Criterion withClause) {
        this.delegate.createAlias(associationPath, alias, joinType, withClause);
        return this;
    }

    public ObservableCriteria createCriteria(final String associationPath) {
        final Criteria criteria = this.delegate.createCriteria(associationPath);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(
            final String associationPath,
            final JoinType joinType) {
        final Criteria criteria = this.delegate.createCriteria(
                associationPath,
                joinType);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(
            final String associationPath,
            final String alias) {
        final Criteria criteria = this.delegate.createCriteria(
                associationPath,
                alias);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(
            final String associationPath,
            final String alias,
            final JoinType joinType) {
        final Criteria criteria = this.delegate.createCriteria(
                associationPath,
                alias,
                joinType);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria createCriteria(
            final String associationPath,
            final String alias,
            final JoinType joinType,
            final Criterion withClause) {
        final Criteria criteria = this.delegate.createCriteria(
                associationPath,
                alias,
                joinType,
                withClause);
        return new ObservableCriteria(criteria);
    }

    public ObservableCriteria setResultTransformer(final ResultTransformer resultTransformer) {
        this.delegate.setResultTransformer(resultTransformer);
        return this;
    }

    public ObservableCriteria setMaxResults(final int maxResults) {
        this.delegate.setMaxResults(maxResults);
        return this;
    }

    public ObservableCriteria setFirstResult(final int firstResult) {
        this.delegate.setFirstResult(firstResult);
        return this;
    }

    public boolean isReadOnlyInitialized() {
        return this.delegate.isReadOnlyInitialized();
    }

    public boolean isReadOnly() {
        return this.delegate.isReadOnly();
    }

    public ObservableCriteria setReadOnly(final boolean readOnly) {
        this.delegate.setReadOnly(readOnly);
        return this;
    }

    public ObservableCriteria setFetchSize(final int fetchSize) {
        this.delegate.setFetchSize(fetchSize);
        return this;
    }

    public ObservableCriteria setTimeout(final int timeout) {
        this.delegate.setTimeout(timeout);
        return this;
    }

    public ObservableCriteria setCacheable(final boolean cacheable) {
        this.delegate.setCacheable(cacheable);
        return this;
    }

    public ObservableCriteria setCacheRegion(final String cacheRegion) {
        this.delegate.setCacheRegion(cacheRegion);
        return this;
    }

    public ObservableCriteria setComment(final String comment) {
        this.delegate.setComment(comment);
        return this;
    }

    public ObservableCriteria addQueryHint(final String hint) {
        this.delegate.addQueryHint(hint);
        return this;
    }

    public ObservableCriteria setFlushMode(final FlushMode flushMode) {
        this.delegate.setFlushMode(flushMode);
        return this;
    }

    public ObservableCriteria setCacheMode(final CacheMode cacheMode) {
        this.delegate.setCacheMode(cacheMode);
        return this;
    }

}
