package cn.liberg.cache;

import java.util.*;

/**
 * 对最近访问过的热数据进行缓存，缓存总条数可设置（比如设为20万），缓存淘汰机制：
 * 1. 先加入哈希表的数据先淘汰
 * 2. 命中的数据如果在淘汰区（前1/4），则交换到最新的位置
 * 3. 命中的数据如果在保留区（后3/4），则不作处理
 */
public class LibertCache<T> {
    private final Map<String, CycleBag> map;
    private final int capacity;
    private final CycleBag[] array;
    private int pos = 0;
    private int judgeSize;

    private static class CycleBag {
        String key;
        Object value;
        int pos;

        CycleBag(String key, Object value, int pos) {
            this.key = key;
            this.value = value;
            this.pos = pos;
        }
    }

    public LibertCache(int capacity) {
        if (capacity < 8) {
            throw new IllegalArgumentException("capacity should >= 8 ");
        }
        //初始容量是1/4最多经过2~3次扩容，可达到最大容量
        judgeSize = capacity >>> 2;
        map = new HashMap<>(judgeSize);
        this.capacity = capacity;
        array = new CycleBag[capacity];
        Arrays.fill(array, 0, array.length, null);
    }

    public List<T> getAll() {
        List<T> list = new ArrayList<>(map.size());
        for(int i=0;i<array.length;i++) {
            CycleBag bag = array[i];
            if(bag!=null && bag.value!=null) {
                list.add((T) bag.value);
            }
        }
        return list;
    }

    public void put(String key, T value) {
        CycleBag exist = null;
        CycleBag mvBag = null;
        synchronized (array) {
            exist = map.get(key);
            if (exist == null) {
                mvBag = array[pos];
                if (mvBag == null) {
                    mvBag = new CycleBag(key, value, pos);
                    array[pos++] = mvBag;
                } else {
                    map.remove(mvBag.key);
                    mvBag.key = key;
                    mvBag.value = value;
                    pos++;
                }
                if (pos >= capacity) {
                    pos = 0;
                }
                map.put(key, mvBag);
            } else {
                exist.value = value;
            }
        }
    }

    public void remove(String key) {
        synchronized (array) {
            CycleBag exist = map.get(key);
            if (exist != null) {
                array[exist.pos] = null;
                map.remove(key);
            }
        }
    }

    public T get(String key) {
        CycleBag bag;
        synchronized (array) {
            bag = map.get(key);
            if (bag != null) {
                int dis = bag.pos - pos;
                if (dis < 0) {
                    dis += capacity;
                }
                if (dis <= judgeSize) {
                    if (dis > 0) {
                        //exchange the position
                        CycleBag mvBag = array[pos];
                        int bagPos = bag.pos;
                        array[pos] = bag;
                        array[bagPos] = mvBag;
                        bag.pos = pos;
                        if (mvBag != null) {
                            mvBag.pos = bagPos;
                        }
                    }
                    if ((++pos) >= capacity) {
                        pos = 0;
                    }
                }
            }
        }
        return bag != null ? (T) bag.value : null;
    }

    public int size() {
        return map.size();
    }

    public int getCapacity() {
        return capacity;
    }

}
