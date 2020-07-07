package cn.liberg.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableAlteration {
    private String tableName;
    private List<String> sqls;
    private String alterPrefix;

    public TableAlteration(String tableName) {
        this.tableName = tableName;
        sqls = new ArrayList<>();
        alterPrefix = "ALTER TABLE " + tableName;
    }

    public void exec(Statement stat) throws SQLException {
        try {
            for (String sql : sqls) {
                stat.execute(sql);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1060 || e.getErrorCode() == 1061
                    || e.getErrorCode() == 1091) {
                // 1060 Duplicate column name '_db' --------ADD DDL
                // 1091 Error Code: 1091. Can't DROP '_db1'; check that
                // column/key exists --------DELETE DDL
            } else {
                throw e;
            }
        }
    }

    public TableAlteration addIndex(String column) {
        sqls.add(alterPrefix + " ADD INDEX(`" + column + "`);");
        return this;
    }

    public TableAlteration dropIndex(String column) {
        sqls.add(alterPrefix + " DROP INDEX(`" + column + "`);");
        return this;
    }

    public TableAlteration addColumn(String column, String type, String afterOf) {
        String fmt = " ADD COLUMN `%1$s` %2$s AFTER `%3$s`;";
        sqls.add(alterPrefix + String.format(fmt, column, type, afterOf));
        return this;
    }

    public TableAlteration modifyColumn(String column, String newType) {
        String fmt = " MODIFY %1$s %2$s;";
        sqls.add(alterPrefix + String.format(fmt, column, newType));
        return this;
    }

}
