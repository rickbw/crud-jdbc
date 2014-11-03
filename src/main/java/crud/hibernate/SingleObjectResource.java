package crud.hibernate;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import crud.DeletableResource;
import crud.ReadableResource;
import crud.Resource;
import crud.ResourceProvider;
import crud.WritableResource;
import rx.Observable;


/**
 * Interact with a single, well-known persistent object. This {@link Resource}
 * implementation has no associated {@link ResourceProvider}, because there's
 * no associate level of indirection.
 *
 * TODO: Scrap this class. Reimplement it with a proper {@link ResourceProvider},
 * based on the {@link Serializable} entity ID.
 */
public class SingleObjectResource<RSRC>
implements ReadableResource<RSRC>,
           WritableResource<RSRC, RSRC>,
           DeletableResource<RSRC> {

    private final SessionFactory sessionFactory;
    private final RSRC object;


    /**
     * Construct a new {@link Resource} to manage the state of the given
     * object. Persistent operations on that object will be made in the
     * context of {@link Session}s obtained from the given
     * {@link SessionFactory} using {@link SessionFactory#getCurrentSession()};
     * this {@code Resource} does not open or close {@code Session}s.
     *
     * The given persistent object may be copied and/or updated asynchronously
     * during the lifetime of this {@code Resource}. Calling code is therefore
     * discouraged from leaving dangling references to the object, as they may
     * become inconsistent with the state of the {@code Resource}. It is
     * especially discouraged from modifying such dangling objects, as the
     * results will be undefined.
     */
    public SingleObjectResource(final SessionFactory sessionFactory, final RSRC object) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory);
        this.object = Objects.requireNonNull(object);
    }

    /**
     * Get the latest state of the persistent object from the data store.
     */
    @Override
    public Observable<RSRC> get() {
        /* FIXME: Does refresh() work if this.object is new or detached?
         * The documentation doesn't say.
         */
        this.sessionFactory.getCurrentSession().refresh(this.object);
        return Observable.from(this.object);
    }

    /**
     * Persist the object to the data store. If the state contains a previous
     * version of the object, that version will be replaced. If not, a new
     * record will be inserted.
     */
    @Override
    public Observable<RSRC> write(final RSRC newValue) {
        final Session session = this.sessionFactory.getCurrentSession();
        session.saveOrUpdate(newValue);
        return Observable.from(this.object);
    }

    /**
     * Delete the persistent object from the data store.
     */
    @Override
    public Observable<RSRC> delete() {
        /* Deleting works, independent of any previous read, through the
         * Hibernate-native API. If we were using JPA, we'd need to to make
         * sure to read before deleting.
         */
        this.sessionFactory.getCurrentSession().delete(this.object);
        return Observable.from(this.object);
    }

}
