package rickbw.crud.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableList;


public final class QueryStatementFactory extends StatementFactory<ResultSet> {

    public QueryStatementFactory(final String statementString) {
        this(statementString, ImmutableList.of());
    }

    public QueryStatementFactory(final String statementString, final Iterable<?> statementParams) {
        super(statementString, statementParams);
    }

    @Override
    protected Statement<ResultSet> createStatement(final PreparedStatement stmt) throws SQLException {
        final Statement<ResultSet> result = new Statement<ResultSet>() {
            @Override
            public ResultSet call() throws SQLException {
                return stmt.executeQuery();
            }

            @Override
            public void close() throws SQLException {
                stmt.close();
            }
        };
        return result;
    }

}
