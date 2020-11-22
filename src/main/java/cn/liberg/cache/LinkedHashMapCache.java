package cn.liberg.cache;

/**
 * 基于{@link java.util.LinkedHashMap}、实现{@link ICache}接口的LRU缓存
 * @param <T>
 *
 * @author Liberg
 */
public class LinkedHashMapCache<T> implements ICache<T> {

    private LRUCache<String, T> lruCache;

    /**
     * @param capacity 指定缓存的容量限制
     */
    public LinkedHashMapCache(int capacity) {
        lruCache = new LRUCache<>(capacity);
    }

    @Override
    public synchronized T get(String key) {
       return lruCache.get(key);
    }

    @Override
    public synchronized void put(String key, T obj) {
        lruCache.put(key, obj);
    }

    @Override
    public synchronized void remove(String key) {
        lruCache.remove(key);
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
        return "LinkedHashMapCache{" +
                lruCache.toString() +
                "}";
    }
}
