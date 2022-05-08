package cn.liberg.core;

/**
 * 数据库表的id列映射到实体的id成员
 *
 * @author Liberg
 */
public abstract class IdColumn<E> extends LongColumn<E> {

    public IdColumn() {
        super(ID, ID);
    }

}
