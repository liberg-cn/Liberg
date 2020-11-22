package cn.liberg.cache;

import java.util.*;

/**
 * 一种位图实现方式。
 * 比{@link java.util.BitSet}有优势？
 *
 * @author Liberg
 */
public class BitsCache {
    Map<String, byte[]> bitsMap;
    private int byteCount;
    private int bitsCount;
    public static final byte[] MASK = {
            (byte)(1<<7),
            1<<6,
            1<<5,
            1<<4,
            1<<3,
            1<<2,
            1<<1,
            1<<0,
    };

    public BitsCache(int bitsCount) {
        int group = (bitsCount>>>5)+5;
        this.byteCount = group<<2;
        this.bitsCount = this.byteCount<<3;
        bitsMap = new HashMap<>();
    }

    public boolean isSet(String key, int pos) {
        checkPos(pos);
        byte[] bytes = bitsMap.get(key);
        if(bytes == null) {
            return false;
        } else {
            int mask = MASK[pos&7];
            return (bytes[pos>>>3] & mask) == mask;
        }
    }


    public boolean isSet(String key, List<Integer> list) {
        for(int i : list) {
            if(!isSet(key, i)) {
                return false;
            }
        }
        return true;
    }

    public void set(String key, int pos) {
        checkPos(pos);
        synchronized (this) {
            byte[] bytes = bitsMap.get(key);
            if(bytes == null) {
                bytes = new byte[byteCount];
                bitsMap.put(key, bytes);
            }
            bytes[pos>>>3] |= MASK[pos&7];
        }
    }

    public void set(String key, List<Integer> positions) {
        synchronized (this) {
            byte[] bytes = bitsMap.get(key);
            if(bytes == null) {
                bytes = new byte[byteCount];
                bitsMap.put(key, bytes);
            }
            for(int pos : positions) {
                if(pos>=0 && pos<bitsCount) {
                    bytes[pos>>>3] |= MASK[pos&7];
                }
            }
        }
    }

    public void set(List<String> keys, int pos) {
        checkPos(pos);
        synchronized (this) {
            int idx = pos>>>3;
            byte mask = MASK[pos&7];
            for(String key : keys) {
                byte[] bytes = bitsMap.get(key);
                if(bytes == null) {
                    bytes = new byte[byteCount];
                    bitsMap.put(key, bytes);
                }
                bytes[idx] |= mask;
            }
        }
    }

    public void unset(String key, int pos) {
        checkPos(pos);
        synchronized (this) {
            byte[] bytes = bitsMap.get(key);
            if(bytes == null) {
                bytes = new byte[byteCount];
                bitsMap.put(key, bytes);
            }
            bytes[pos>>>3] &= ~MASK[pos&7];
        }
    }

    public void unset(int pos) {
        checkPos(pos);
        final int idx = pos>>>3;
        final int mask = ~MASK[pos&7];
        synchronized (this) {
            for(Map.Entry<String, byte[]> entry: bitsMap.entrySet()) {
                byte[] bytes = entry.getValue();
                bytes[idx] &= mask;
            }
        }
    }

    public void remove(String key) {
        synchronized (this) {
            byte[] bytes = bitsMap.get(key);
            if(bytes != null) {
                bitsMap.remove(key);
                bytes =null;
            }
        }
    }

    public List<Integer> listTrue(String key) {
        List<Integer> list = new ArrayList<>();
        byte[] bytes = bitsMap.get(key);
        if(bytes != null) {
            for(int i=0;i<bitsCount;i++) {
                int mask = MASK[i&7];
                if((bytes[i>>>3] & mask) == mask) {
                    list.add(i);
                }
            }
        }
        return list;
    }

    public List<Integer> listTrueByPage(String key, int pageNum, int pageSize) {
        List<Integer> list = new ArrayList<>();
        byte[] bytes = bitsMap.get(key);
        if(bytes != null) {
            int cnt = 0;
            int start = (pageNum-1)*pageSize;
            for(int i=0;i<bitsCount;i++) {
                int mask = MASK[i&7];
                if((bytes[i>>>3] & mask) == mask) {
                    if(cnt>=start) {
                        list.add(i);
                        if(list.size()>=pageSize) {
                            break;
                        }
                    }
                    cnt++;
                }
            }
        }
        return list;
    }



    private void checkPos(int pos) {
        if(pos<0 || pos>=bitsCount) {
            throw new IndexOutOfBoundsException(pos + " is not in range[0," + bitsCount + ")");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BitsCache{");
        sb.append("byteCount="+byteCount);
        sb.append(",bitsCount="+bitsCount);
        sb.append(",mapSize="+bitsMap.size()+"}");
        sb.append(",detail:\r\n");
        int count = 0;
        for(Map.Entry<String, byte[]> entry : bitsMap.entrySet()) {
            count++;
            sb.append("["+entry.getKey()+"]: ");
            List<Integer> list = listTrueByPage(entry.getKey(), 1, 20);
            for(int i : list) {
                sb.append(i);
                sb.append(",");
            }
            sb.append("...");
            if(list.size()>0) {
                sb.append("is true\r\n");
            } else {
                sb.append("\r\n");
            }
            if(count>=20) {
                break;
            }
        }
        sb.append("...");
        return sb.toString();
    }

    public static boolean isSet(byte val, int index) {
        //index的有效范围是0~7
        int mask = MASK[index&7];
        return (val&mask) == mask;
    }

    public static void main(String[] args) {

        byte val = 0b0001_1000;
        System.out.println(val);
        System.out.println(isSet(val,0));
        System.out.println(isSet(val,3));
        System.out.println(isSet(val,4));



//        BitsCache cache = new BitsCache(32);
//
//        List<Integer> list = new ArrayList<>();
//        list.add(0);
//        list.add(3);
//        list.add(8);
//        list.add(9);
//        list.add(19);
//
//        for(int i=0;i< list.size();i++) {
//            cache.set("abc", list.get(i));
//            cache.set("def", list.get(i));
//
//
//        }
//
//        System.out.println(cache);
//        System.out.println(cache.isSet("def", list));
//        System.out.println(cache.isSet("abc", list));
//        list.add(21);
//        System.out.println(cache.isSet("def", list));
//        System.out.println(cache.isSet("abc", list));
//        cache.set("abc", 21);
//        System.out.println(cache);
//        System.out.println(cache.isSet("def", list));
//        System.out.println(cache.isSet("abc", list));
    }

}
