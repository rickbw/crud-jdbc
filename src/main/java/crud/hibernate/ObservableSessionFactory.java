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
import org.hibernate.SessionFactory;


public class ObservableSessionFactory implements AutoCloseable {

    private final SessionFactory sessionFactory;


    public static ObservableSessionFactory from(final SessionFactory sessionFactory) {
        return new ObservableSessionFactory(sessionFactory);
    }

    // TODO: Unify with SessionBuilder
    public ObservableSession openSession() {
        final Session delegate = this.sessionFactory.openSession();
        return new ObservableSession(delegate);
    }

    // TODO: Unify with SessionBuilder
    public CurrentObservableSession currentSession() {
        final Session delegate = this.sessionFactory.getCurrentSession();
        return new CurrentObservableSession(delegate);
    }

    public boolean isClosed() {
        return this.sessionFactory.isClosed();
    }

    @Override
    public void close() {
        this.sessionFactory.close();
    }

    private ObservableSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory);
    }

}
