package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体int字段-->数据库INT列
 *
 * @author Liberg
 */
public class IntegerColumn extends Column {

    public IntegerColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public ColumnType type() {
        return ColumnType.Integer;
    }

    @Override
    public Integer getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getInt(columnIndex);
    }
}
