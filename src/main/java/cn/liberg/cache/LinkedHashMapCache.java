package cn.liberg.cache;

public class LinkedHashMapCache<T> implements ICache<T> {

    private LRUCache<String, T> lruCache;

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
