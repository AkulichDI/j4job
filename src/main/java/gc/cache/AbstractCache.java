package gc.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCache<K,V> {

    private final Map<K, SoftReference<V>> cache = new HashMap<>();

    public final void put(K key, V value) {
        cache.put(key, new SoftReference<>(value));
    }
    public final V get(K key) {
        V value = cache.getOrDefault(key, new SoftReference<>(null)).get();
        if ( value == null) {
            value = load(key);
            put(key, value);
        }

        return value;
    }

    protected abstract V load (K key);





}
