package cache;

import java.util.Optional;
import java.util.List;

public interface CachingStrategy<T> {
    Optional<T> get(String key);
    void put(String key, T value);
    void remove(String key);
    void clear();
    List<T> getAll();
}
