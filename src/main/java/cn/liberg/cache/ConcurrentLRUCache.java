package cn.liberg.cache;

/**
 * 线程安全的，
 * 基于{@link cn.liberg.cache.LRUCache}、实现{@link ICache}接口的LRU缓存
 * @param <K>
 * @param <V>
 *
 * @author Liberg
 */
public class ConcurrentLRUCache<K, V> implements ICache<K, V> {

    private final LRUCache<K, V> lruCache;

    /**
     * @param capacity 指定缓存的容量限制
     */
    public ConcurrentLRUCache(int capacity) {
        lruCache = new LRUCache<>(capacity);
    }

    @Override
    public synchronized V get(K key) {
       return lruCache.get(key);
    }

    @Override
    public synchronized void put(K key, V obj) {
        lruCache.put(key, obj);
    }

    @Override
    public synchronized void remove(K key) {
        lruCache.remove(key);
    }

    @Override
    public void clear() {
        lruCache.clear();
    }

    @Override
    public int size() {
        return lruCache.size();
    }

    @Override
    public int capacity() {
        return lruCache.capacity;
    }

    @Override
    public String toString() {
        return lruCache.toString();
    }
}
