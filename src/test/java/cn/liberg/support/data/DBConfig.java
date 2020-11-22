package cn.liberg.support.data;

import cn.liberg.database.IDataBaseConf;

public class DBConfig implements IDataBaseConf {
    private String driverName = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/";
    private String dbName = "liberg_test";
    private String userName = "root";
    private String password = "";
    private String charset = "utf8";
    private String collation = "utf8_general_ci";


    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public String getCollation() {
        return collation;
    }
}

