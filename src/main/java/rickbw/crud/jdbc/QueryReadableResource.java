package rickbw.crud.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import rickbw.crud.ReadableResource;
import com.google.common.base.Preconditions;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


public final class QueryReadableResource implements ReadableResource<ResultSet> {

    private final DataSource connectionProvider;
    private final StatementFactory<ResultSet> queryFactory;

    private final ExecutorService executor;


    public QueryReadableResource(
            final DataSource connectionProvider,
            final StatementFactory<ResultSet> queryFactory,
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
        final Observable<ResultSet> result = Observable.create(new Func1<Observer<ResultSet>, Subscription>() {
            @Override
            public Subscription call(final Observer<ResultSet> observer) {
                final Task task = new Task(observer);
                final Future<?> taskResult = executor.submit(task);
                final Subscription sub = new TaskSubscription(task, taskResult);
                return sub;
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
        final QueryReadableResource other = (QueryReadableResource) obj;
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
        private final Observer<ResultSet> observer;
        private volatile boolean cancelled = false;

        public Task(final Observer<ResultSet> observer) {
            this.observer = observer;
            assert null != this.observer;
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
                final Connection connection = connectionProvider.getConnection();
                try {
                    final Statement<ResultSet> statement = queryFactory.prepareStatement(connection);
                    try {
                        final ResultSet resultSet = statement.call();
                        try {
                            while (resultSet.next() && !this.cancelled) {
                                this.observer.onNext(resultSet);
                            }
                        } finally {
                            resultSet.close();
                        }
                    } finally {
                        statement.close();
                    }
                } finally {
                    connection.close();
                }

                /* Call onCompleted() after closing everything, because any
                 * close method could throw, and we're not allowed to call
                 * both onCompleted() AND onError().
                 */
                this.observer.onCompleted();
            } catch (final SQLException sqlx) {
                this.observer.onError(sqlx);
            }
        }
    }

    private static final class TaskSubscription implements Subscription {
        private final Task task;
        private final Future<?> taskResult;

        public TaskSubscription(final Task task, final Future<?> taskResult) {
            this.task = task;
            assert null != this.task;
            this.taskResult = taskResult;
            assert null != this.taskResult;
        }

        @Override
        public void unsubscribe() {
            this.taskResult.cancel(false);
            this.task.cancel();
        }
    }

}
