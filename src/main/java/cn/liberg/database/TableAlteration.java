package cn.liberg.database;

import com.mysql.cj.exceptions.MysqlErrorNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据表结构升级实现类。
 *
 * <p>出于数据安全性的考虑，Liberg鼓励复用和新增列，不建议删除已有的列。
 * 因此TableAlteration类没有提供"deleteColumn"之类的的方法
 *
 * @author Liberg
 */
public class TableAlteration {
    private static final Logger logger = LoggerFactory.getLogger(TableAlteration.class);

    private String tableName;
    private List<String> sqls;
    private String alterPrefix;

    public TableAlteration(String tableName) {
        this.tableName = tableName;
        sqls = new ArrayList<>();
        alterPrefix = "ALTER TABLE " + tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void exec(Statement stat) throws SQLException {
        try {
            for (String sql : sqls) {
                stat.execute(sql);
            }
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == MysqlErrorNumbers.ER_DUP_FIELDNAME
                    || errorCode == MysqlErrorNumbers.ER_DUP_KEYNAME
                    || errorCode == MysqlErrorNumbers.ER_CANT_DROP_FIELD_OR_KEY ) {
                // 1060 Duplicate column name
                // 1061 Duplicate key name
                // 1091 Can't DROP 'xxx'. Check that column/key exists
                logger.warn("TableAlteration exception: {},{}", errorCode, e.getMessage());
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
