package cn.liberg.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 非线程安全的LRU(Least Recently Used)缓存实现
 * 基于{@link java.util.LinkedHashMap}
 *
 * @param <K>
 * @param <V>
 *
 * @author Liberg
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    public LRUCache(int capacity) {
        super(16, 0.75F, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    protected final int capacity;

    @Override
    public String toString() {
        final int count = Math.min(size(), 10);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(Map.Entry<K, V> entry : entrySet()) {
            i++;
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            if(i >= count) {
                break;
            } else {
                sb.append(",");
            }
        }
        if(size() > count) {
            sb.append(",...");
            sb.append(size()-count);
            sb.append("more");
        }
        return this.getClass().getSimpleName() + "{" +
                "capacity=" + capacity +
                ", entries=[" + sb.toString() + "]}";
    }

}
