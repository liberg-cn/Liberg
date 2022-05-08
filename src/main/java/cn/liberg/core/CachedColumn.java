package cn.liberg.core;

import cn.liberg.cache.ConcurrentLRUCache;

public abstract class CachedColumn<E, F> extends Column<E, F> implements ICacheEntity<E> {
    final ConcurrentLRUCache<F, E> lruCache;

    /**
     * capacity传入0，则不通过本列作为key缓存查询到的数据
     * @param entityFieldName
     * @param shortName
     * @param capacity
     */
    public CachedColumn(String entityFieldName, String shortName, int capacity) {
        super(entityFieldName, shortName);
        if(capacity > 0) {
            lruCache = new ConcurrentLRUCache<>(capacity);
        } else {
            lruCache = null;
        }
    }

    public CachedColumn(String entityFieldName, String shortName) {
        this(entityFieldName, shortName, 0);
    }

    public E getFromCache(F key) {
        if(lruCache != null) {
            return lruCache.get(key);
        } else {
            return null;
        }
    }

    public void setToCache(F value, E entity) {
        if(lruCache != null) {
            lruCache.put(value, entity);
        }
    }

    @Override
    public void put(E entity) {
        setToCache(get(entity), entity);
    }

    @Override
    public void remove(E entity) {
        if(lruCache != null) {
            lruCache.remove(get(entity));
        }
    }

    @Override
    public void clear() {
        if(lruCache != null) {
            lruCache.clear();
        }
    }

    public int size() {
        return lruCache == null ? 0 : lruCache.size();
    }

    public int capacity() {
        return lruCache == null ? 0 : lruCache.capacity();
    }
}
