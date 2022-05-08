package cn.liberg.core;

/**
 * Column类定义数据表的列和entity字段的映射绑定关系。
 * <p>
 * 实体字段名支持以字母m(member、成员变量)开头的形式，比如mPackage；
 * 数据表的列名除了id特殊外，其他列均以下划线_开头，以防列名跟sql关键字冲突。
 * 详见{@link Column#parseColumnName}方法的实现。
 *
 * <p>需要指出的是，entity中指定了{@code @dbmap(isIndex=false)}注解的字段不会建立映射关系。
 *
 * <p>为了保持XxxDao更“轻量级”，实体中的字段除了名称外，其他信息并没有记录到相应的{@code Column}中，
 * 而是在构造建表语句时，由{@link cn.liberg.database.TableBuilder#add}方法中进行传入。
 * 传入的信息包括: 类型、长度、是否是索引字段、列注释等。
 * <p>
 * 子类包括：
 * {@link ByteColumn}
 * {@link IntegerColumn}
 * {@link LongColumn}
 * {@link StringColumn}
 * {@link IdColumn}
 *
 * @param <E> 与数据表对应的entity实体类型
 * @param <F> 数据列的类型
 * @author Liberg
 * @see cn.liberg.database.TableBuilder
 * @see ByteColumn
 * @see IntegerColumn
 * @see LongColumn
 * @see StringColumn
 * @see IdColumn
 */
public abstract class Column<E, F> extends Field<F> {

    public Column(String fieldName, String shortName) {
        super(fieldName, shortName);
    }

    /**
     * 从实体对象entity中读取列的值
     */
    public abstract F get(E entity);

    /**
     * 将值设置到实体对象entity中
     */
    public abstract void set(E entity, F value);

}
