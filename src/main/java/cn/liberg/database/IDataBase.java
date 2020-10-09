package cn.liberg.database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Liberg
 */
public interface IDataBase {

    /**
     * 获取数据库的名称
     * @return 数据库名称
     */
    public String getName();

    /**
     * 获取数据库的当前版本
     * @return 数据库当前版本
     */
    public int getCurrentVersion();

    /**
     * 获取数据库的配置信息
     * @return
     */
    public IDataBaseConf getConfig();

    /**
     * 此方法中完成数据库表结构的创建
     * @param stat
     * @throws SQLException
     */
    public void createTable(Statement stat) throws SQLException;

    /**
     * 实现数据库从dbVersion到newVersion的版本升级逻辑，升级内容包括结构和数据
     * @param stat
     * @param dbVersion
     * @param newVersion
     * @return
     * @throws SQLException
     */
    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException;

    //

    /**
     * 此方法中完成数据库数据的初始化，比如创建一个超级管理员等
     */
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
