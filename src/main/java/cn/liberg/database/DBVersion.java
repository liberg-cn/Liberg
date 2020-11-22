package cn.liberg.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责创建{@code db_version}表，
 * 维护和查询当前数据库的版本。
 *
 * @author Liberg
 * @see DBVersionManager
 */
public final class DBVersion {
    private static final Logger logger = LoggerFactory.getLogger(DBVersion.class);
    private static final String TABLE_NAME = "db_version";
    private static final String COLUMN_DB = "_db";
    private static final String COLUMN_CODE = "_version_code";
    private static Map<String, Integer> map = null;

    private Statement stat;

    public DBVersion(Statement stat) {
        this.stat = stat;
        if (map == null) {
            initMap(stat);
        }
    }

    private static synchronized void initMap(Statement stat) {
        if (map != null) {
            return;
        }
        map = new HashMap<>(16);
        String sql = String.format("select %1$s,%2$s from %3$s", COLUMN_DB, COLUMN_CODE, TABLE_NAME);
        ResultSet rs = null;
        try {
            createTableIfAbsent(stat);
            rs = stat.executeQuery(sql);
            String dbName;
            int code;
            while (rs.next()) {
                dbName = rs.getString(COLUMN_DB);
                code = rs.getInt(COLUMN_CODE);
                map.put(dbName, code);
            }
        } catch (Exception e) {
            logger.error("DBVersion init failed.", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void createTableIfAbsent(Statement stat) throws SQLException {
        String sql = "create table if Not exists " + TABLE_NAME
                + " (`id` BIGINT primary key AUTO_INCREMENT,"
                + COLUMN_DB + " VARCHAR(255),"
                + COLUMN_CODE + " BIGINT"
                + ");";
        stat.executeUpdate(sql);
    }

    public void saveVersion(String dbName, int version, boolean isUpdate) throws SQLException {
        String sql;
        if (isUpdate) {
            sql = " update %1$s set %2$s=%3$s where %4$s='%5$s' ; ";
            sql = String.format(sql, TABLE_NAME, COLUMN_CODE, version, COLUMN_DB, dbName);
        } else {
            sql = " insert %1$s(%2$s,%3$s) values('%4$s',%5$s) ; ";
            sql = String.format(sql, TABLE_NAME, COLUMN_DB, COLUMN_CODE, dbName, version);
        }
        stat.executeUpdate(sql);
        map.put(dbName, version);
    }

    public static int getVersion(String dbName) {
        Integer code = map.get(dbName);
        return code == null ? -1 : code.intValue();
    }

}
