package cache;

import java.util.Optional;
import java.util.List;

/**
 * Defines a strategy for caching objects of type T.
 * This interface follows the Strategy pattern, allowing different caching implementations
 * to be used interchangeably based on application requirements.
 *
 * Implementations might include in-memory caching, distributed caching, or even
 * no-caching strategies. The caching behavior is abstracted away from the clients
 * that use it, allowing for flexible cache configuration.
 *
 *
 * @param <T> The type of objects to be cached
 */
public interface CachingStrategy<T> {

    /**
     * Retrieves an item from the cache by its key.
     *
     * @param key The unique key identifying the cached item
     * @return An Optional containing the cached item if found, or empty if not found
     */
    Optional<T> get(String key);

    /**
     * Stores an item in the cache with the specified key.
     * If an item with the same key already exists, it will be replaced.
     *
     * @param key The unique key to associate with the item
     * @param value The item to cache
     */
    void put(String key, T value);

    /**
     * Removes an item from the cache by its key.
     *
     * @param key The unique key of the item to remove
     */
    void remove(String key);

    /**
     * Clears all items from the cache.
     * This operation should reset the cache to an empty state.
     */
    void clear();

    /**
     * Retrieves all items currently stored in the cache.
     * Different implementations may handle this differently, especially
     * for distributed or very large caches.
     *
     * @return A list of all cached items
     */
    List<T> getAll();
}
