package cn.liberg.database;

import cn.liberg.core.Column;
import cn.liberg.core.Field;
import cn.liberg.core.IntegerColumn;
import cn.liberg.core.StringColumn;
import cn.liberg.support.data.DBConfig;
import cn.liberg.support.data.DBImpl;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class TableBuilderTest {

    @Test
    public void testBuild() {
        DBImpl dbImpl = new DBImpl(new DBConfig());

        Field<String> columnName = new Field<String>("name", "n"){
            @Override
            public String getValue(ResultSet rs, int columnIndex) throws SQLException {
                return null;
            }
        };
        Field<String> columnPassword = new Field<String>("password", "n"){
            @Override
            public String getValue(ResultSet rs, int columnIndex) throws SQLException {
                return null;
            }
        };
        Field<Integer> columnAge = new Field<Integer>("age", "a"){
            @Override
            public Integer getValue(ResultSet rs, int columnIndex) throws SQLException {
                return null;
            }
        };

        TableBuilder tb = new TableBuilder("user");
        tb.add(columnName, true, dbImpl.typeString(63), "名称");
        tb.add(columnPassword, dbImpl.typeString(63));
        tb.add(columnAge, dbImpl.typeInt(), "年龄");


        String sql = tb.build();
        String expectedSql = "CREATE TABLE IF NOT EXISTS user(id BIGINT PRIMARY KEY AUTO_INCREMENT" +
                ",_name VARCHAR(63) NULL COMMENT '名称'," +
                "_password VARCHAR(63) NULL" +
                ",_age INT NOT NULL DEFAULT 0 COMMENT '年龄'" +
                ",KEY `user__name`(_name));";
        assertEquals(sql, expectedSql);
    }
}