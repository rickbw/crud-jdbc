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
package crud.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import com.google.common.base.Preconditions;

import crud.spi.UpdatableSpec;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;


public final class JdbcUpdatableResource implements UpdatableSpec<Object, Integer> {

    private final DataSource connectionProvider;
    private final String updateStatementTemplate;

    private final ExecutorService executor;


    public JdbcUpdatableResource(
            final DataSource connectionProvider,
            final String updateStatementTemplate,
            final ExecutorService executor) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
        this.updateStatementTemplate = updateStatementTemplate;
        Preconditions.checkArgument(
                !this.updateStatementTemplate.isEmpty(),
                "empty statement template");
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * The integer result indicates the number of rows modified by the update.
     */
    @Override
    public Observable<Integer> update(final Observable<Object> updateParams) {
        final StatementFactory updateFactory = new StatementFactory(this.updateStatementTemplate, updateParams);
        final Observable<Integer> result = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> susbcriber) {
                final Task task = new Task(updateFactory, susbcriber);
                final Future<?> taskResult = executor.submit(task);
                susbcriber.add(Subscriptions.from(taskResult));
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
        final JdbcUpdatableResource other = (JdbcUpdatableResource) obj;
        if (!this.connectionProvider.equals(other.connectionProvider)) {
            return false;
        }
        if (!this.updateStatementTemplate.equals(other.updateStatementTemplate)) {
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
        result = prime * result + this.updateStatementTemplate.hashCode();
        // don't include executor: doesn't determine resource identity
        return result;
    }

    private final class Task implements Runnable {
        private final StatementFactory updateFactory;
        private final Subscriber<? super Integer> subscriber;

        public Task(final StatementFactory updateFactory, final Subscriber<? super Integer> subscriber) {
            this.updateFactory = updateFactory;
            assert null != updateFactory;
            this.subscriber = subscriber;
            assert null != this.subscriber;
        }

        @Override
        public void run() {
            try {
                /* We need to get the Connection within run(), because any
                 * given Connection should only ever be used by one thread.
                 */
                try (final Connection connection = connectionProvider.getConnection();
                     final PreparedStatement update = this.updateFactory.prepareStatement(connection)) {
                    final int numRowsModified = update.executeUpdate();
                    this.subscriber.onNext(numRowsModified);
                }

                /* Call onCompleted() after closing everything, because any
                 * close method could throw, and we're not allowed to call
                 * both onCompleted() AND onError().
                 */
                this.subscriber.onCompleted();
            } catch (final SQLException sqlx) {
                this.subscriber.onError(sqlx);
            }
        }
    }

}
