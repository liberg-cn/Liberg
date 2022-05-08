package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CachedByteColumn<E> extends CachedColumn<E, Byte> {

    public CachedByteColumn(String entityFieldName, String shortName, int capacity) {
        super(entityFieldName, shortName, capacity);
    }

    public CachedByteColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    public Byte getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getByte(columnIndex);
    }
}
