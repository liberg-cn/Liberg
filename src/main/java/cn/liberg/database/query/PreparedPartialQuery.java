package cn.liberg.database.query;

import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparedPartialQuery implements AutoCloseable {
    private Column[] columns;
    private PreparedStatement statement;
    private int index = 0;

    public PreparedPartialQuery(Column[] columns, PreparedStatement statement) {
        this.columns = columns;
        this.statement = statement;
    }

    public PreparedPartialQuery set(IntegerColumn column, int value) throws SQLException {
        statement.setInt(++index, value);
        return this;
    }
    public PreparedPartialQuery set(LongColumn column, long value) throws SQLException {
        statement.setLong(++index, value);
        return this;
    }

    public PreparedPartialQuery set(StringColumn column, String value) throws SQLException {
        statement.setString(++index, value);
        return this;
    }

    public void reset() {
        index = 0;
    }

    public Map<String, Object> one() throws SQLException {
        ResultSet rs = statement.executeQuery();
        if(rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for(Column column : columns) {
                map.put(column.getName(), column.getValue(rs));
            }
            return map;
        } else {
            return null;
        }
    }

    public List<Map<String, Object>> all() throws SQLException {
        ResultSet rs = statement.executeQuery();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        while(rs.next()) {
            map = new HashMap<>();
            for(Column column : columns) {
                map.put(column.getName(), column.getValue(rs));
            }
            list.add(map);
        }
        return list;
    }

    @Override
    public void close() {
        try {
            DBHelper.self().close(statement, statement.getConnection());
        } catch (SQLException e) {

        }
    }
}
