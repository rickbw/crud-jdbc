package rickbw.crud.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import rickbw.crud.UpdatableResource;
import rickbw.crud.adapter.FutureSubscription;
import com.google.common.base.Preconditions;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


public final class JdbcUpdatableResource implements UpdatableResource<Iterable<?>, Integer> {

    private final DataSource connectionProvider;
    private final String updateStatementTemplate;

    private final ExecutorService executor;


    public JdbcUpdatableResource(
            final DataSource connectionProvider,
            final String updateStatementTemplate,
            final ExecutorService executor) {
        this.connectionProvider = Preconditions.checkNotNull(connectionProvider);
        this.updateStatementTemplate = updateStatementTemplate;
        Preconditions.checkArgument(
                !this.updateStatementTemplate.isEmpty(),
                "empty statement template");
        this.executor = Preconditions.checkNotNull(executor);
    }

    /**
     * The integer result indicates the number of rows modified by the update.
     */
    @Override
    public Observable<Integer> update(final Iterable<?> updateParams) {
        final StatementFactory updateFactory = new StatementFactory(this.updateStatementTemplate, updateParams);
        final Observable<Integer> result = Observable.create(new Func1<Observer<Integer>, Subscription>() {
            @Override
            public Subscription call(final Observer<Integer> observer) {
                final Task task = new Task(updateFactory, observer);
                final Future<?> taskResult = executor.submit(task);
                final Subscription sub = new FutureSubscription(taskResult);
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
        private final Observer<Integer> observer;

        public Task(final StatementFactory updateFactory, final Observer<Integer> observer) {
            this.updateFactory = updateFactory;
            assert null != updateFactory;
            this.observer = observer;
            assert null != this.observer;
        }

        @Override
        public void run() {
            try {
                /* We need to get the Connection within run(), because any
                 * given Connection should only ever be used by one thread.
                 */
                final Connection connection = connectionProvider.getConnection();
                try {
                    final PreparedStatement update = this.updateFactory.prepareStatement(connection);
                    try {
                        final int numRowsModified = update.executeUpdate();
                        this.observer.onNext(numRowsModified);
                    } finally {
                        update.close();
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

}
