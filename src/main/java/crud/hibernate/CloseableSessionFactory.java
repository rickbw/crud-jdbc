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
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;


/**
 * An implementation of {@link SessionFactory} that implements
 * {@link Closeable} (and, by extension, {@link AutoCloseable}) and creates
 * instances of {@link CloseableSession}. This Factory, and the
 * {@code CloseableSession}s it creates, are compatible with the try-with-
 * resources syntax from Java 7 and later.
 *
 * Instances are backed by another {@code SessionFactory} instance, and
 * delegate all calls to it.
 */
public class CloseableSessionFactory implements SessionFactory, Closeable {

    private static final long serialVersionUID = -8210542466004645239L;

    private final SessionFactory delegate;


    public CloseableSessionFactory(final SessionFactory delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * Create a new instance of {@link CloseableSession}. It will be backed
     * by a {@link Session} created by this Factory's delegate
     * {@link SessionFactory}, itself created as with
     * {@link SessionFactory#openSession()}.
     *
     * @throws  HibernateException  if an error occurs.
     *
     * @see #openSession()
     */
    public CloseableSession openCloseableSession() {
        final Session newSession = this.delegate.openSession();
        return new CloseableSession(newSession);
    }

    /**
     * Get the current {@link Session} as a {@link CloseableSession}. The
     * result will be backed by the current {@code Session}, as obtained by
     * {@link #getCurrentSession()}.
     *
     * @throws  HibernateException  if an error occurs.
     *
     * @see #getCurrentSession()
     */
    public CloseableSession getCurrentCloseableSession() {
        final Session currentSession = this.delegate.getCurrentSession();
        return new CloseableSession(currentSession);
    }

    @Override
    public Session openSession() throws HibernateException {
        return this.delegate.openSession();
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return this.delegate.getCurrentSession();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return this.delegate.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(final Connection connection) {
        return this.delegate.openStatelessSession(connection);
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return this.delegate.getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return this.delegate.withOptions();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return this.delegate.withStatelessOptions();
    }

    @Override
    public Reference getReference() throws NamingException {
        return this.delegate.getReference();
    }

    @Override
    public ClassMetadata getClassMetadata(@SuppressWarnings("rawtypes") final Class entityClass) {
        return this.delegate.getClassMetadata(entityClass);
    }

    @Override
    public ClassMetadata getClassMetadata(final String entityName) {
        return this.delegate.getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(final String roleName) {
        return this.delegate.getCollectionMetadata(roleName);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return this.delegate.getAllClassMetadata();
    }

    @Override
    public Map<String, CollectionMetadata> getAllCollectionMetadata() {
        @SuppressWarnings("unchecked")
        final Map<String, CollectionMetadata> metadata = this.delegate.getAllCollectionMetadata();
        return metadata;
    }

    @Override
    public Statistics getStatistics() {
        return this.delegate.getStatistics();
    }

    @Override
    public Cache getCache() {
        return this.delegate.getCache();
    }

    @Override
    public void evictEntity(final String entityName, final Serializable id)
    throws HibernateException {
        this.evictEntity(entityName, id);
    }

    @Override
    public void evictCollection(final String roleName, final Serializable id)
    throws HibernateException {
        this.evictCollection(roleName, id);
    }

    @Override
    public Set<String> getDefinedFilterNames() {
        @SuppressWarnings("unchecked")
        final Set<String> names = this.delegate.getDefinedFilterNames();
        return names;
    }

    @Override
    public FilterDefinition getFilterDefinition(final String filterName)
    throws HibernateException {
        return this.delegate.getFilterDefinition(filterName);
    }

    @Override
    public boolean containsFetchProfileDefinition(final String name) {
        return this.delegate.containsFetchProfileDefinition(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return this.delegate.getTypeHelper();
    }

    @Override
    public boolean isClosed() {
        return this.delegate.isClosed();
    }

    @Override
    public void close() throws HibernateException {
        this.delegate.close();
    }

    @Override
    @Deprecated
    public void evict(@SuppressWarnings("rawtypes") final Class persistentClass)
    throws HibernateException {
        this.delegate.evict(persistentClass);
    }

    @Override
    @Deprecated
    public void evict(
            @SuppressWarnings("rawtypes") final Class persistentClass,
            final Serializable id)
    throws HibernateException {
        this.delegate.evict(persistentClass, id);
    }

    @Override
    @Deprecated
    public void evictEntity(final String entityName) throws HibernateException {
        this.delegate.evictEntity(entityName);
    }

    @Override
    @Deprecated
    public void evictCollection(final String roleName) throws HibernateException {
        this.delegate.evictCollection(roleName);
    }

    @Override
    @Deprecated
    public void evictQueries(final String cacheRegion) throws HibernateException {
        this.delegate.evictQueries(cacheRegion);
    }

    @Override
    @Deprecated
    public void evictQueries() throws HibernateException {
        this.delegate.evictQueries();
    }

}
