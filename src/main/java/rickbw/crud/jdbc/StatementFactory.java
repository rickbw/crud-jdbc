package rickbw.crud.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


public abstract class StatementFactory<RESPONSE> {

    private final String statementString;
    private final ImmutableList<Object> statementParams;


    protected StatementFactory(final String statementString, final Iterable<?> statementParams) {
        Preconditions.checkArgument(!statementString.isEmpty(), "empty statement string");
        this.statementString = statementString;
        this.statementParams = ImmutableList.copyOf(statementParams);
    }

    public final Statement<RESPONSE> prepareStatement(final Connection connection) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(this.statementString);
        try {
            final Iterator<Object> params = this.statementParams.iterator();
            for (int i = 1; params.hasNext(); ++i) {
                final Object param = params.next();
                statement.setObject(i, param);
            }
        } catch (final SQLException sqlx) {
            statement.close();
            throw sqlx;
        } catch (final RuntimeException rex) {
            statement.close();
            throw rex;
        } catch (final Error err) {
            statement.close();
            throw err;
        }

        final Statement<RESPONSE> results = createStatement(statement);
        return results;
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
        final StatementFactory<?> other = (StatementFactory<?>) obj;
        if (!this.statementParams.equals(other.statementParams)) {
            return false;
        }
        if (!this.statementString.equals(other.statementString)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.statementParams.hashCode();
        result = prime * result + this.statementString.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append(" [statementString=").append(this.statementString);
        if (!this.statementParams.isEmpty()) {
            buf.append(", statementParams=").append(this.statementParams);
        }
        buf.append(']');
        return buf.toString();
    }

    protected abstract Statement<RESPONSE> createStatement(PreparedStatement stmt) throws SQLException;

}
