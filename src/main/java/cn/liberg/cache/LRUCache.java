package cn.liberg.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    public LRUCache(int capacity) {
        super(16, 0.75F, true);
        this.capacity = capacity;
    }

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
        return "LRUCache{" +
                "capacity=" + capacity +
                ", entrys=[" + sb.toString() + "]}";
    }

}
