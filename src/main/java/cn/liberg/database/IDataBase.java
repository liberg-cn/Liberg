package cn.liberg.database;

import java.sql.SQLException;
import java.sql.Statement;

public interface IDataBase {

    public String getName();

    public int getCurrentVersion();

    public IDataBaseConf getConfig();

    //创建数据库表结构
    public void createTable(Statement stat) throws SQLException;

    //数据库升级扩展
    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException;

    //初始化数据库数据，比如创建一个超级管理员等
    public void initData();

    default public String typeInt() {
        return "INT NOT NULL DEFAULT 0";
    }

    default public String typeInt(int defVal) {
        return "INT NOT NULL DEFAULT " + defVal;
    }

    default public String typeLong() {
        return "BIGINT NOT NULL DEFAULT 0";
    }

    default public String typeLong(long defVal) {
        return "BIGINT NOT NULL DEFAULT " + defVal;
    }

    default public String typeString(int len) {
        return "VARCHAR(" + len + ") NULL";
    }

    default public String typeString() {
        return "VARCHAR(255) NULL";
    }

    default public String typeString(int len, String defVal) {
        return "VARCHAR(" + len + ") NOT NULL DEFAULT '" + defVal + "'";
    }

    default public String typeString(String defVal) {
        return "VARCHAR(255) NOT NULL DEFAULT '" + defVal + "'";
    }

    default public String typeText() {
        return "TEXT DEFAULT NULL";
    }

}
