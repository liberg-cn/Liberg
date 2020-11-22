package cn.liberg.core;

import java.util.HashMap;

/**
 * 只查询一张表的部分字段时，查出的一条记录会映射到一个{@link Segment}对象，
 * 而不是相应的entity。
 *
 * @author Liberg
 */
public class Segment extends HashMap<String, Object> {

    public Segment() {
        super(8);
    }

    public Segment(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
