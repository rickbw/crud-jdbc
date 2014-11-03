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
import java.util.Objects;

import javax.transaction.Synchronization;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.engine.transaction.spi.LocalStatus;


/**
 * A {@link Transaction} that supports the try-with-resources syntax in Java 7
 * and later, for safer operation. Instances are obtained from
 * {@link CloseableSession#beginTransaction()}.
 */
public final class CloseableTransaction implements Transaction, Closeable {

    private final Transaction delegate;
    private boolean isCommittedOrRolledBack = false;


    @Override
    public void commit() {
        this.delegate.commit();
        this.isCommittedOrRolledBack = true;
    }

    @Override
    public void rollback() {
        this.delegate.rollback();
        this.isCommittedOrRolledBack = true;
    }

    @Override
    public void begin() {
        this.delegate.begin();
        this.isCommittedOrRolledBack = false;
    }

    @Override
    public boolean wasRolledBack() {
        return this.delegate.wasRolledBack();
    }

    @Override
    public boolean wasCommitted() {
        return this.delegate.wasCommitted();
    }

    @Override
    public boolean isActive() {
        return this.delegate.isActive();
    }

    @Override
    public void registerSynchronization(final Synchronization synchronization) {
        this.delegate.registerSynchronization(synchronization);
    }

    @Override
    public int getTimeout() {
        return this.delegate.getTimeout();
    }

    @Override
    public void setTimeout(final int seconds) {
        this.delegate.setTimeout(seconds);
    }

    @Override
    public boolean isInitiator() {
        return this.delegate.isInitiator();
    }

    @Override
    public LocalStatus getLocalStatus() {
        return this.delegate.getLocalStatus();
    }

    @Override
    public boolean isParticipating() {
        return this.delegate.isParticipating();
    }

    /**
     * If this transaction hasn't yet been committed (see {@link #commit()}),
     * roll back, as with {@link #rollback()}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    @Override
    public void close() {
        if (!this.isCommittedOrRolledBack) {
            rollback();
        }
    }

    /**
     * This class doesn't provide any public constructors or factory methods,
     * because it's unsafe to wrap a {@link Transaction} when some statements
     * may already have been executed within it. Instead, this constructor is
     * provided for the sole use of {@link CloseableSession}.
     *
     * @see CloseableSession#beginTransaction()
     */
    /*package*/ CloseableTransaction(final Transaction delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

}
