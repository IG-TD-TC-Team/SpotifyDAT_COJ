package cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of the CachingStrategy that stores cached objects in memory.
 * This implementation uses a ConcurrentHashMap for thread-safe caching operations,
 * making it suitable for multi-threaded applications.
 *
 * The in-memory cache provides fast access to cached data but is limited by the
 * available memory. It's best suited for applications where:
 *
 *   Cache data fits comfortably in available memory
 *   Low latency access is a priority
 *   Cache data doesn't need to persist across application restarts
 *
 * @param <T> The type of objects to be cached
 */
public class InMemoryCacheStrategy<T> implements CachingStrategy<T> {
    /**
     * The internal map that stores cached objects.
     * ConcurrentHashMap is used to ensure thread-safety without sacrificing
     * too much performance in concurrent access scenarios.
     */
    private final Map<String, T> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves an item from the in-memory cache by its key.
     *
     * @param key The unique key identifying the cached item
     * @return An Optional containing the cached item if found, or empty if not found
     */
    @Override
    public Optional<T> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    /**
     * Stores an item in the in-memory cache with the specified key.
     * If an item with the same key already exists, it will be replaced.
     *
     * @param key The unique key to associate with the item
     * @param value The item to cache
     */
    @Override
    public void put(String key, T value) {
        cache.put(key, value);
    }

    /**
     * Removes an item from the in-memory cache by its key.
     *
     * @param key The unique key of the item to remove
     */
    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * Clears all items from the in-memory cache.
     * This operation resets the cache to an empty state.
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * Retrieves all items currently stored in the in-memory cache.
     * This method returns a new ArrayList containing all cached values,
     * so modifications to the returned list won't affect the cache.
     *
     * @return A list of all cached items
     */
    @Override
    public List<T> getAll() {
        return new ArrayList<>(cache.values());
    }
}