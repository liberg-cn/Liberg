package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CachedStringColumn<E> extends CachedColumn<E, String> {

    public CachedStringColumn(String entityFieldName, String shortName, int capacity) {
        super(entityFieldName, shortName, capacity);
    }

    public CachedStringColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    public String getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }
}
