package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CachedLongColumn<E> extends CachedColumn<E, Long> {

    public CachedLongColumn(String entityFieldName, String shortName, int capacity) {
        super(entityFieldName, shortName, capacity);
    }

    public CachedLongColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    public Long getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }
}
