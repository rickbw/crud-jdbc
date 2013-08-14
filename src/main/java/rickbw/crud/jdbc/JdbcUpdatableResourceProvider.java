package rickbw.crud.jdbc;

import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import com.google.common.base.Preconditions;


public final class JdbcUpdatableResourceProvider
implements UpdatableResourceProvider<String, Iterable<?>, Integer> {

    private final DataSource dataSource;
    private final ExecutorService executor;


    public JdbcUpdatableResourceProvider(
            final DataSource dataSource,
            final ExecutorService executor) {
        this.dataSource = Preconditions.checkNotNull(dataSource);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public UpdatableResource<Iterable<?>, Integer> get(final String updateStatementTemplate) {
        final UpdatableResource<Iterable<?>, Integer> resource = new JdbcUpdatableResource(
                this.dataSource,
                updateStatementTemplate,
                this.executor);
        return resource;
    }

}
