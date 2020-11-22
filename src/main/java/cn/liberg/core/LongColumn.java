package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体long字段-->数据库BIGINT列
 *
 * @author Liberg
 */
public class LongColumn extends Column {

    public LongColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public ColumnType type() {
        return ColumnType.Long;
    }

    @Override
    public Long getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }
}
