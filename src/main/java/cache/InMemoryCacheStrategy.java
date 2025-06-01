package cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheStrategy<T> implements CachingStrategy<T> {
    private final Map<String, T> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<T> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void put(String key, T value) {
        cache.put(key, value);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(cache.values());
    }
}