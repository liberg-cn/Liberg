package cn.liberg.database.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringColumn extends Column {

    public StringColumn(String name) {
        super(name);
    }

    @Override
    public String getValue(ResultSet rs) throws SQLException {
        return rs.getString(name);
    }
}
