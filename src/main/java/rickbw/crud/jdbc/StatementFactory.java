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
import java.sql.SQLException;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


public final class StatementFactory {

    private final String statementString;
    private final ImmutableList<Object> statementParams;


    public StatementFactory(final String statementString, final Iterable<?> statementParams) {
        Preconditions.checkArgument(!statementString.isEmpty(), "empty statement string");
        this.statementString = statementString;
        this.statementParams = ImmutableList.copyOf(statementParams);
    }

    public PreparedStatement prepareStatement(final Connection connection) throws SQLException {
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
        return statement;
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
        final StatementFactory other = (StatementFactory) obj;
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

}
