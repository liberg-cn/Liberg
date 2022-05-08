package cn.liberg.core;

import cn.liberg.database.BaseDao;

import java.util.HashMap;

/**
 * 只查询一张表的部分字段时，查出的一条记录会映射到一个{@link Segment}对象，
 * 而不是相应的entity。
 *
 * <p>
 *     通过{@code get(Column column)}方法，可以拿到column列对应的值。
 * </p>
 *
 * <T> 指代相应的实体类型
 * @author Liberg
 */
public class Segment<T> extends HashMap<String, Object> {

    public Segment(BaseDao<T> dao) {
        super(8);
    }

    public Segment(BaseDao<T> dao, int initialCapacity) {
        super(initialCapacity);
    }

    public <F> F get(Field<F> field) {
        return (F) get(field.shortName);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
