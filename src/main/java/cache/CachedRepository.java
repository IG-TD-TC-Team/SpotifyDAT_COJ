package cache;

import persistence.interfaces.Repository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A decorator for Repository implementations that adds caching functionality.
 * This class follows the Decorator pattern, adding caching behavior to any Repository
 * implementation without modifying the original implementation.
 *
 * @param <T> The entity type being stored in the repository
 */
public class CachedRepository<T> implements Repository<T> {
    private final Repository<T> repository;
    private final CachingStrategy<T> cache;
    private final Function<T, String> keyExtractor;

    /**
     * Constructs a new CachedRepository with the specified repository, cache strategy,
     * and key extraction function.
     *
     * @param repository The underlying repository to delegate operations to
     * @param cache The caching strategy to use for storing entities
     * @param keyExtractor A function that extracts a unique cache key from an entity
     * @throws NullPointerException if any parameter is null
     */
    public CachedRepository(Repository<T> repository,
                            CachingStrategy<T> cache,
                            Function<T, String> keyExtractor) {
        this.repository = repository;
        this.cache = cache;
        this.keyExtractor = keyExtractor;
    }

    /**
     * Retrieves all entities, first checking the cache and falling back to the repository
     * if the cache is empty.
     *
     * @return A list of all entities
     */
    @Override
    public List<T> findAll() {
        List<T> cached = cache.getAll();
        if (!cached.isEmpty()) {
            return cached;
        }

        List<T> all = repository.findAll();
        all.forEach(item -> cache.put(keyExtractor.apply(item), item));
        return all;
    }

    /**
     * Retrieves an entity by its ID, first checking the cache and falling back to the
     * repository if not found in the cache.
     *
     * @param id The unique identifier of the entity
     * @return An Optional containing the entity if found, or empty if not found
     */
    @Override
    public Optional<T> findById(int id) {
        String key = String.valueOf(id);
        Optional<T> cached = cache.get(key);

        if (cached.isPresent()) {
            return cached;
        }

        Optional<T> item = repository.findById(id);
        item.ifPresent(value -> cache.put(key, value));
        return item;
    }

    /**
     * Saves a new entity to the repository and updates the cache accordingly.
     *
     * @param entity The entity to save
     * @return The saved entity (may contain generated values)
     */
    @Override
    public T save(T entity) {
        T saved = repository.save(entity);
        cache.put(keyExtractor.apply(saved), saved);
        return saved;
    }

    /**
     * Saves multiple entities to the repository and refreshes the cache.
     *
     * @param entities The list of entities to save
     */
    @Override
    public void saveAll(List<T> entities) {
        repository.saveAll(entities);
        cache.clear();
        entities.forEach(entity -> cache.put(keyExtractor.apply(entity), entity));
    }

    /**
     * Updates an existing entity in the repository and the cache.
     *
     * @param entity The entity with updated values
     * @return An Optional containing the updated entity if successful, or empty if not found
     */
    @Override
    public Optional<T> update(T entity) {
        Optional<T> updated = repository.update(entity);
        updated.ifPresent(value -> cache.put(keyExtractor.apply(value), value));
        return updated;
    }

    /**
     * Deletes an entity by its ID from both the repository and the cache.
     *
     * @param id The unique identifier of the entity to delete
     * @return true if the entity was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            cache.remove(String.valueOf(id));
        }
        return deleted;
    }
}