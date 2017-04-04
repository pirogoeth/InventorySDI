package inventory.sql;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implements a caching layer for query gateways.
 *
 * Some calls to the gateway will always cause a cache invalidation.
 * Create will not require cache invalidation, as the new object can
 * be added to the cache.
 *
 * Updates and deletes will always cause cache invalidation.
 *
 * @param <T> query gateway types
 */
abstract class CachingConnector<T> extends Connector {

    private final List<T> cache = new ArrayList<>();

    /**
     * Marker used to determine if the cache should be updated.
     */
    private boolean dirty = true;

    protected void cacheItem(T item) {
        this.cache.add(item);
    }

    protected void setCache(List<T> newCache) {
        this.cache.addAll(newCache);
    }

    /**
     * Updates the connector's local cache.
     */
    protected abstract void updateCache();

    protected void ensureCacheClean() {
        if ( this.dirty ) {
            LOG.debug("Updating invalidated cache..");
            this.cache.clear();
            this.dirty = false;
            this.updateCache();
        }
    }

    /**
     * Marks the cache as dirty.
     */
    protected void invalidate() {
        if ( ! this.dirty ) {
            LOG.debug("Marking cache as dirty");
            this.dirty = true;
        } else {
            LOG.debug("Trying to invalidate already dirtied cache - operations performed on dirty cache?");
        }
    }

    protected boolean isDirty() {
        return this.dirty;
    }

    /**
     * Allows application of a stream to the cached data.
     *
     * @return Stream of T
     */
    protected Stream<T> stream() {
        if ( this.dirty ) {
            LOG.warn("Opening stream on dirty cache!");
        }

        return this.cache.stream();
    }

    /**
     * Allows application of a filtering predicate to the cached data.
     *
     * @param pred filter
     * @return Stream of T
     */
    protected Stream<T> filter(Predicate<T> pred) {
        if ( this.dirty ) {
            LOG.warn("Filtering on dirty cache!");
        }

        return this.cache.stream().filter(pred);
    }

}
