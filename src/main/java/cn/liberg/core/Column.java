package cn.liberg.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Column类定义数据表的列和entity字段的映射绑定关系。
 *
 * 实体字段名支持以字母m(member、成员变量)开头的形式，比如mPackage；
 * 数据表的列名除了id特殊外，其他列均以下划线_开头，以防列名跟sql关键字冲突。
 * 详见{@link Column#parseColumnName}方法的实现。
 *
 * <p>需要指出的是，entity中指定了{@code @dbmap(isIndex=false)}注解的字段不会建立映射关系。
 *
 * <p>为了保持XXXDao更“轻量级”，实体中的字段除了名称外，其他信息并没有记录到相应的{@code Column}中，
 * 而是在构造建表语句时，由{@link cn.liberg.database.TableBuilder#add}方法中进行传入。
 * 传入的信息包括: 类型、长度、是否是索引字段、列注释等。
 *
 * 子类包括：
 * {@link ByteColumn}
 * {@link IntegerColumn}
 * {@link LongColumn}
 * {@link StringColumn}
 * {@link IdColumn}
 *
 * @author Liberg
 *
 * @see cn.liberg.database.TableBuilder
 * @see ByteColumn
 * @see IntegerColumn
 * @see LongColumn
 * @see StringColumn
 * @see IdColumn
 */
public abstract class Column<T> {
    public static final String ID = "id";
    /**
     * 数据表列名称
     */
    public final String name;
    /**
     * 实体字段名称
     */
    public final String entityFieldName;
    /**
     * 实体字段名称的简写，同一实体内部不重复
     * 便于跟FastJSON等框架配合，使返回客户端的json数据变得简短
     */
    public final String shortName;
    protected volatile Field entityField = null;

    /**
     * 将实体字段名称，映射为数据表的列名称
     * eg:
     * userName --> _user_name
     * mPackage --> _package
     *
     * id --> id，是唯一例外
     *
     * @param entityFieldName
     * @return
     */
    public static String parseColumnName(String entityFieldName) {
        if(ID.equals(entityFieldName)) {
            return ID;
        }
        boolean first = true;
        int start = 0;
        if (entityFieldName.length() > 1 && entityFieldName.charAt(0) == 'm'
                && Character.isUpperCase(entityFieldName.charAt(1))) {
            first = false;
            start = 1;
        }

        StringBuilder sb = new StringBuilder("_");
        for (int i = start; i < entityFieldName.length(); i++) {
            char c = entityFieldName.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                if (first) {
                    sb.append('_');
                    first = false;
                }
                sb.append((char)(c + 32));
            } else {
                first = true;
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * @param entityFieldName 实体字段名称
     * @param shortName 实体字段的简称，取各个单词首字母，全小写。
     *                  若可能重复，则通过1/2/3...进行编号
     */
    public Column(String entityFieldName, String shortName) {
        this.name = parseColumnName(entityFieldName);
        this.entityFieldName = entityFieldName;
        this.shortName = shortName;
    }

    /**
     * 从数据库查询结果的ResultSet中读取列的值
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    public abstract T getValue(ResultSet rs, int columnIndex) throws SQLException;

    public Object getEntityValue(Object entity) throws OperatorException {
        try {
            if (entityField == null) {
                entityField = entity.getClass().getDeclaredField(entityFieldName);
            }
            return entityField.get(entity);
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_SERVER, e);
        }
    }

    public ColumnType type() {
        return ColumnType.UNKNOWN;
    }

    @Override
    public String toString() {
        return name;
    }
}
