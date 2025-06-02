package cache;

import java.util.*;

/**
 * A null object implementation of the CachingStrategy interface that performs no caching.
 * This strategy implements the Null Object pattern, providing a no-operation implementation
 * of all caching methods while conforming to the CachingStrategy interface.
 *
 * The NoCacheStrategy is useful in scenarios where:
 *
 * Caching needs to be disabled without changing the application logic
 * Testing is being performed with caching disabled
 * Memory is severely constrained and caching would be detrimental
 * Data changes so frequently that caching provides no benefit
 *
 * Using this strategy allows the application to maintain the same code structure
 * regardless of whether caching is enabled or not, following the Open/Closed principle.
 *
 *
 * @param <T> The type of objects that would normally be cached
 */
public class NoCacheStrategy<T> implements CachingStrategy<T> {

    /**
     * Always returns an empty Optional, indicating no cached value was found.
     *
     * @param key The key that would normally identify a cached item (ignored in this implementation)
     * @return An empty Optional, as no items are cached
     */
    @Override
    public Optional<T> get(String key) {
        return Optional.empty();
    }

    /**
     * No-operation implementation of the put method.
     * This method does nothing, effectively disabling caching.
     *
     * @param key The key that would normally be used to store the item (ignored)
     * @param value The value that would normally be cached (ignored)
     */
    @Override
    public void put(String key, T value) {
        // Do nothing - no caching
    }

    /**
     * No-operation implementation of the remove method.
     * This method does nothing since no items are cached.
     *
     * @param key The key that would normally identify the item to remove (ignored)
     */
    @Override
    public void remove(String key) {
        // Do nothing - no caching
    }

    /**
     * No-operation implementation of the clear method.
     * This method does nothing since no items are cached.
     */
    @Override
    public void clear() {
        // Do nothing - no caching
    }

    /**
     * Always returns an empty list, since no items are cached.
     * This method uses Collections.emptyList() for efficiency and immutability.
     *
     * @return An empty, immutable list
     */
    @Override
    public List<T> getAll() {
        return Collections.emptyList();
    }
}