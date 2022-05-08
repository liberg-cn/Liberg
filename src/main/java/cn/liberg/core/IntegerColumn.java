package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体int字段-->数据库INT列
 *
 * @author Liberg
 */
public abstract class IntegerColumn<E> extends Column<E, Integer> {

    public IntegerColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public Integer getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getInt(columnIndex);
    }
}
