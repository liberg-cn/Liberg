package cn.liberg.database.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteColumn extends Column {

    public ByteColumn(String name) {
        super(name);
    }

    @Override
    public Byte getValue(ResultSet rs) throws SQLException {
        return rs.getByte(name);
    }
}
