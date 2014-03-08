/* Copyright 2013–2014 Rick Warren
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

package rickbw.crud.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;


public final class JdbcReadableResource implements ReadableResource<ResultSet> {

    private final DataSource connectionProvider;
    private final StatementFactory queryFactory;

    private final ExecutorService executor;


    public JdbcReadableResource(
            final DataSource connectionProvider,
            final StatementFactory queryFactory,
            final ExecutorService executor) {
        this.connectionProvider = Preconditions.checkNotNull(connectionProvider);
        this.queryFactory = Preconditions.checkNotNull(queryFactory);
        this.executor = Preconditions.checkNotNull(executor);
    }

    /**
     * The caller should <em>not</em> call {@link ResultSet#next()} on the
     * {@link ResultSet} it obtains from the {@link Observable} returned by
     * this method; all result iteration is handled internally by this method.
     * The full result set is provided solely for the client to retrieve the
     * values from it, e.g. with {@link ResultSet#getInt(int)}.
     */
    @Override
    public Observable<ResultSet> get() {
        final Observable<ResultSet> result = Observable.create(new Observable.OnSubscribe<ResultSet>() {
            @Override
            public void call(final Subscriber<? super ResultSet> subscriber) {
                final Task task = new Task(subscriber);
                final Future<?> taskResult = executor.submit(task);
                final Subscription sub = new TaskSubscription(task, taskResult);
                subscriber.add(sub);
            }
        });
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JdbcReadableResource other = (JdbcReadableResource) obj;
        if (!this.connectionProvider.equals(other.connectionProvider)) {
            return false;
        }
        if (!this.queryFactory.equals(other.queryFactory)) {
            return false;
        }
        // don't include executor: doesn't determine resource identity
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.connectionProvider.hashCode();
        result = prime * result + this.queryFactory.hashCode();
        // don't include executor: doesn't determine resource identity
        return result;
    }

    private final class Task implements Runnable {
        private final Subscriber<? super ResultSet> subscriber;
        private volatile boolean cancelled = false;

        public Task(final Subscriber<? super ResultSet> subscriber) {
            this.subscriber = subscriber;
            assert null != this.subscriber;
        }

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run() {
            try {
                /* We need to get the Connection within run(), because any
                 * given Connection should only ever be used by one thread.
                 */
                try (final Connection connection = connectionProvider.getConnection();
                     final PreparedStatement query = queryFactory.prepareStatement(connection);
                     final ResultSet resultSet = query.executeQuery()) {
                    while (resultSet.next() && !this.cancelled) {
                        this.subscriber.onNext(resultSet);
                    }
                }

                /* Call onCompleted() after closing everything, because any
                 * close method could throw, and we're not allowed to call
                 * both onCompleted() AND onError().
                 */
                this.subscriber.onCompleted();
            } catch (final Throwable sqlx) {
                this.subscriber.onError(sqlx);
            }
        }
    }

    private static final class TaskSubscription implements Subscription {
        private static final boolean mayInterruptIfRunning = true;
        private final Future<?> taskResult;
        private final Task task;

        public TaskSubscription(final Task task, final Future<?> taskResult) {
            this.taskResult = taskResult;
            assert this.taskResult != null;
            this.task = task;
            assert this.task != null;
        }

        @Override
        public void unsubscribe() {
            this.taskResult.cancel(mayInterruptIfRunning);
            this.task.cancel();
        }

        @Override
        public boolean isUnsubscribed() {
            return this.taskResult.isCancelled();
        }
    }

}
