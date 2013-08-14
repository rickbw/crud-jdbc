package rickbw.crud.jdbc;

import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import com.google.common.base.Preconditions;


public final class JdbcReadableResourceProvider
implements ReadableResourceProvider<StatementFactory, ResultSet> {

    private final DataSource dataSource;
    private final ExecutorService executor;


    public JdbcReadableResourceProvider(
            final DataSource dataSource,
            final ExecutorService executor) {
        this.dataSource = Preconditions.checkNotNull(dataSource);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ReadableResource<ResultSet> get(final StatementFactory key) {
        final ReadableResource<ResultSet> resource = new JdbcReadableResource(
                this.dataSource,
                key,
                this.executor);
        return resource;
    }

}
