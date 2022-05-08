package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体long字段-->数据库BIGINT列
 *
 * @author Liberg
 */
public abstract class LongColumn<E> extends Column<E, Long> {

    public LongColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public Long getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }
}
