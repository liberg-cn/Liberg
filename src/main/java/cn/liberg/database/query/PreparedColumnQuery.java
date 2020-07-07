package cn.liberg.database.query;

import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreparedColumnQuery implements AutoCloseable {
    private Column column;
    private PreparedStatement statement;
    private int index = 0;

    public PreparedColumnQuery(Column column, PreparedStatement statement) {
        this.column = column;
        this.statement = statement;
    }
    public PreparedColumnQuery set(IntegerColumn column, int value) throws SQLException {
        statement.setInt(++index, value);
        return this;
    }
    public PreparedColumnQuery set(LongColumn column, long value) throws SQLException {
        statement.setLong(++index, value);
        return this;
    }

    public PreparedColumnQuery set(StringColumn column, String value) throws SQLException {
        statement.setString(++index, value);
        return this;
    }

    public void reset() {
        index = 0;
    }

    public <T> T one() throws SQLException {
        ResultSet rs = statement.executeQuery();
        if(rs.next()) {
            return (T) column.getValue(rs);
        } else {
            return null;
        }
    }

    public <T> List<T> all() throws SQLException {
        ResultSet rs = statement.executeQuery();
        List<T> list = new ArrayList<>();
        while(rs.next()) {
            list.add((T) column.getValue(rs));
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
