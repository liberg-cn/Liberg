package cn.liberg.core;

import cn.liberg.cache.ConcurrentLRUCache;
import cn.liberg.database.Condition;
import cn.liberg.database.WhereMeta;

public class CachedColumnTrio<E, F1, F2, F3> implements ICacheEntity<E> {
    private CachedColumn<E, F1> column1;
    private CachedColumn<E, F2> column2;
    private CachedColumn<E, F3> column3;
    private final ConcurrentLRUCache<String, E> lruCache;

    public CachedColumnTrio(CachedColumn<E, F1> column1,
                            CachedColumn<E, F2> column2,
                            CachedColumn<E, F3> column3,
                            int capacity) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        lruCache = new ConcurrentLRUCache(capacity);;
    }

    public E getFromCache(F1 key1, F2 key2, F3 key3) {
        return lruCache.get(key1 + "_" + key2 + "_" + key3);
    }

    public void setToCache(F1 value1, F2 value2, F3 value3, E entity) {
        lruCache.put(value1 + "_" + value2 + "_" + value3, entity);
        column1.setToCache(value1, entity);
        column2.setToCache(value2, entity);
        column3.setToCache(value3, entity);
    }

    @Override
    public void put(E entity) {
        setToCache(column1.get(entity), column2.get(entity), column3.get(entity), entity);
    }

    @Override
    public void remove(E entity) {
        lruCache.remove(column1.get(entity) + "_" + column2.get(entity) + "_" + column3.get(entity));
    }

    @Override
    public void clear() {
        lruCache.clear();
    }

    public String build(F1 value1, F2 value2, F3 value3) {
        StringBuilder sb = new StringBuilder();
        sb.append(column1.name);
        sb.append(Condition.EQ);
        sb.append(value1);
        sb.append(WhereMeta.AND);
        sb.append(column2.name);
        sb.append(Condition.EQ);
        sb.append(value2);
        sb.append(WhereMeta.AND);
        sb.append(column3.name);
        sb.append(Condition.EQ);
        sb.append(value3);
        return sb.toString();
    }

    public int size() {
        return lruCache.size();
    }

    public int capacity() {
        return lruCache.capacity();
    }
}
