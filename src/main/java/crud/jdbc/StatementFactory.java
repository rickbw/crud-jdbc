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
import java.util.Iterator;
import java.util.Objects;

import com.google.common.base.Preconditions;

import rx.Observable;


public final class StatementFactory {

    private final String statementString;
    private final Observable<?> statementParams;


    public StatementFactory(final String statementString, final Observable<?> statementParams) {
        Preconditions.checkArgument(!statementString.isEmpty(), "empty statement string");
        this.statementString = statementString;
        this.statementParams = Objects.requireNonNull(statementParams);
    }

    public PreparedStatement prepareStatement(final Connection connection) throws SQLException {
        @SuppressWarnings("resource")   // don't close on success
        final PreparedStatement statement = connection.prepareStatement(this.statementString);
        try {
            // XXX: Too bad about the blocking here.
            final Iterator<?> params = this.statementParams.toBlocking().getIterator();
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
        if (!this.statementString.equals(other.statementString)) {
            // Check String before parameters: probably cheaper
            return false;
        }
        if (!this.statementParams.equals(other.statementParams)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.statementString.hashCode();
        result = prime * result + this.statementParams.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " [statementString=" + this.statementString
                + ']';
    }

}
