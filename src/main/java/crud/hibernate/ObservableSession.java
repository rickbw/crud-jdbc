/* Copyright 2015 Rick Warren
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
package crud.hibernate;

import java.util.Objects;

import org.hibernate.Session;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;


public class ObservableSession implements AutoCloseable {

    private final Session session;

    private final Supplier<SessionCache> cache = Suppliers.memoize(new Supplier<SessionCache>() {
        @Override
        public SessionCache get() {
            return new SessionCache(ObservableSession.this.session);
        }
    });


    public ObservableTransaction openTransaction() {
        return new ObservableTransaction(this.session);
    }

    public SessionCache cache() {
        return this.cache.get();
    }

    @Override
    public void close() {
        this.session.close();
    }

    /*package*/ ObservableSession(final Session session) {
        this.session = Objects.requireNonNull(session);
    }

}
