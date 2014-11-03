/* Copyright 2014 Rick Warren
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

import java.io.Closeable;
import java.io.Serializable;
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * A wrapper for a Hibernate {@link Session} that supports the try-with-
 * resources syntax from Java 7 and later, for safer operation. Instances are
 * obtained from a {@link CloseableSessionFactory}.
 */
public class CloseableSession implements Closeable, Serializable {

    /**
     * This class implements {@link Serializable} because {@link Session}
     * does, even though it's gross.
     */
    private static final long serialVersionUID = -5954691811821439850L;

    private final Session delegate;


    /**
     * Begin a new {@link Transaction}, as with
     * {@link Session#beginTransaction()}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public CloseableTransaction beginTransaction() {
        final Transaction tx = this.delegate.beginTransaction();
        return new CloseableTransaction(tx);
    }

    /**
     * Get the underlying Hibernate {@link Session}, in order to perform
     * additional actions.
     *
     * Unfortunately, it's not possible for {@code CloseableSession} to
     * implement Session directly, because the return types of {@link Session#close()}
     * and {@link AutoCloseable#close()} are not compatible.
     */
    public Session getDelegateSession() {
        return this.delegate;
    }

    /**
     * @throws  HibernateException  if an error occurs.
     */
    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * @see CloseableSessionFactory#openCloseableSession()
     */
    /*package*/ CloseableSession(final Session delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

}
