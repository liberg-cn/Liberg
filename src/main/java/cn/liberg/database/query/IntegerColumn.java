package cn.liberg.database.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerColumn extends Column {

    public IntegerColumn(String name) {
        super(name);
    }

    @Override
    public Integer getValue(ResultSet rs) throws SQLException {
        return rs.getInt(name);
    }
}
