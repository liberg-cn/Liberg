package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据表的列抽象
 * 将实体类的成员字段名称映射为数据表的列名
 *
 * @param <F> 列的类型
 */
public abstract class Field<F> {
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

    /**
     * @param entityFieldName 实体字段名称
     * @param shortName       实体字段的简称，取各个单词首字母，全小写。
     *                        若可能重复，则通过1/2/3...进行编号
     */
    public Field(String entityFieldName, String shortName) {
        this.entityFieldName = entityFieldName;
        this.name = parseColumnName(entityFieldName);
        this.shortName = shortName;
    }

    /**
     * 从数据库查询结果的ResultSet中读取列的值
     *
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    public abstract F getValue(ResultSet rs, int columnIndex) throws SQLException;

    @Override
    public String toString() {
        return name;
    }

    /**
     * 将实体字段名称，映射为数据表的列名称
     * eg:
     * userName --> _user_name
     * mPackage --> _package
     * <p>
     * id --> id，是唯一例外
     *
     * @param entityFieldName
     * @return
     */
    public static String parseColumnName(String entityFieldName) {
        if (ID.equals(entityFieldName)) {
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
                sb.append((char) (c + 32));
            } else {
                first = true;
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
