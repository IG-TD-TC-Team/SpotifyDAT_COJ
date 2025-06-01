package cache;

import persistence.interfaces.Repository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CachedRepository<T> implements Repository<T> {
    private final Repository<T> repository;
    private final CachingStrategy<T> cache;
    private final Function<T, String> keyExtractor;

    public CachedRepository(Repository<T> repository,
                            CachingStrategy<T> cache,
                            Function<T, String> keyExtractor) {
        this.repository = repository;
        this.cache = cache;
        this.keyExtractor = keyExtractor;
    }

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

    @Override
    public T save(T entity) {
        T saved = repository.save(entity);
        cache.put(keyExtractor.apply(saved), saved);
        return saved;
    }

    @Override
    public void saveAll(List<T> entities) {
        repository.saveAll(entities);
        cache.clear();
        entities.forEach(entity -> cache.put(keyExtractor.apply(entity), entity));
    }

    @Override
    public Optional<T> update(T entity) {
        Optional<T> updated = repository.update(entity);
        updated.ifPresent(value -> cache.put(keyExtractor.apply(value), value));
        return updated;
    }

    @Override
    public boolean deleteById(int id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            cache.remove(String.valueOf(id));
        }
        return deleted;
    }
}