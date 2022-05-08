package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CachedIdColumn<E> extends CachedColumn<E, Long> {

    public CachedIdColumn(int capacity) {
        super(ID, ID, capacity);
    }

    public CachedIdColumn() {
        this(0);
    }

    public Long getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }
}
