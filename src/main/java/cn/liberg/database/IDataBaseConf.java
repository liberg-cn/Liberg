package cn.liberg.database;

public interface IDataBaseConf {

    String getDriverName();

    String getDbName();

    String getUrl();

    String getUserName();

    String getPassword();

    String getCharset();

    String getCollation();
}
