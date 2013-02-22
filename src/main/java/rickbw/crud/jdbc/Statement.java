package rickbw.crud.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;


/**
 * A wrapper around {@link PreparedStatement} that makes queries and updates
 * more polymorphic.
 */
public interface Statement<RESPONSE> extends Callable<RESPONSE> {

    /**
     * Execute the statement, e.g. like
     * {@link PreparedStatement#executeQuery()} or
     * {@link PreparedStatement#executeUpdate()}.
     */
    @Override
    public RESPONSE call() throws SQLException;

    /**
     * Close the statement, e.g. like {@link PreparedStatement#close()}.
     */
    public abstract void close() throws SQLException;

}
