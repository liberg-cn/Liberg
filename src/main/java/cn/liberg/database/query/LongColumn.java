package cn.liberg.database.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongColumn extends Column {

    public LongColumn(String name) {
        super(name);
    }

    @Override
    public Long getValue(ResultSet rs) throws SQLException {
        return rs.getLong(name);
    }
}
