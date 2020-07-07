package cn.liberg.database.query;

import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreparedColumnUpdate implements AutoCloseable {
    private Column column;
    private PreparedStatement statement;
    private int index = 0;

    public PreparedColumnUpdate(Column column, PreparedStatement statement) {
        this.column = column;
        this.statement = statement;
    }
    public PreparedColumnUpdate set(IntegerColumn column, int value) throws SQLException {
        statement.setInt(++index, value);
        return this;
    }
    public PreparedColumnUpdate set(LongColumn column, long value) throws SQLException {
        statement.setLong(++index, value);
        return this;
    }

    public PreparedColumnUpdate set(StringColumn column, String value) throws SQLException {
        statement.setString(++index, value);
        return this;
    }

    public void reset() {
        index = 0;
    }

    public void execute() throws SQLException {
        statement.executeUpdate();
    }

    @Override
    public void close() {
        try {
            DBHelper.self().close(statement, statement.getConnection());
        } catch (SQLException e) {

        }
    }
}
