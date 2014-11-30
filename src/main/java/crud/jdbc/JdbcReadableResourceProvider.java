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

import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import crud.spi.GettableSetSpec;
import crud.spi.GettableSpec;


public final class JdbcReadableResourceProvider
implements GettableSetSpec<StatementFactory, ResultSet> {

    private final DataSource dataSource;
    private final ExecutorService executor;


    public JdbcReadableResourceProvider(
            final DataSource dataSource,
            final ExecutorService executor) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public GettableSpec<ResultSet> getter(final StatementFactory key) {
        final GettableSpec<ResultSet> resource = new JdbcReadableResource(
                this.dataSource,
                key,
                this.executor);
        return resource;
    }

}
