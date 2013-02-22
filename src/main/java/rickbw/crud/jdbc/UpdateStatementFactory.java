package rickbw.crud.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableList;


@NotThreadSafe
public final class UpdateStatementFactory extends StatementFactory<Integer> {

    public UpdateStatementFactory(final String statementString) {
        this(statementString, ImmutableList.of());
    }

    public UpdateStatementFactory(final String statementString, final Iterable<?> statementParams) {
        super(statementString, statementParams);
    }

    @Override
    protected Statement<Integer> createStatement(final PreparedStatement stmt) throws SQLException {
        final Statement<Integer> result = new Statement<Integer>() {
            @Override
            public Integer call() throws SQLException {
                return stmt.executeUpdate();
            }

            @Override
            public void close() throws SQLException {
                stmt.close();
            }
        };
        return result;
    }

}
