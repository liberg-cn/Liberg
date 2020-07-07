package cn.liberg.database.query;

import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedPartialUpdate implements AutoCloseable {
    private Column[] columns;
    private PreparedStatement statement;
    private int index = 0;

    public PreparedPartialUpdate(Column[] columns, PreparedStatement statement) {
        this.columns = columns;
        this.statement = statement;
    }
    public PreparedPartialUpdate set(IntegerColumn column, int value) throws SQLException {
        statement.setInt(++index, value);
        return this;
    }
    public PreparedPartialUpdate set(LongColumn column, long value) throws SQLException {
        statement.setLong(++index, value);
        return this;
    }

    public PreparedPartialUpdate set(StringColumn column, String value) throws SQLException {
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
