package cache;

import java.util.*;

public class NoCacheStrategy<T> implements CachingStrategy<T> {

    @Override
    public Optional<T> get(String key) {
        return Optional.empty();
    }

    @Override
    public void put(String key, T value) {
        // Do nothing - no caching
    }

    @Override
    public void remove(String key) {
        // Do nothing - no caching
    }

    @Override
    public void clear() {
        // Do nothing - no caching
    }

    @Override
    public List<T> getAll() {
        return Collections.emptyList();
    }
}