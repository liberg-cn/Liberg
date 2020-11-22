package cn.liberg.database;

/**
 * 数据库配置接口
 *
 * @author Liberg
 * @see IDataBase
 */
public interface IDataBaseConf {

    /**
     * jdbc driver类名称，eg："com.mysql.cj.jdbc.Driver"
     * @return
     */
    String getDriverName();

    /**
     * 数据库名称
     * @return
     */
    String getDbName();

    /**
     * 数据库连接url，eg："jdbc:mysql://localhost:3306/"
     * @return
     */
    String getUrl();

    /**
     * 访问数据库服务的用户名，eg："root"
     * @return
     */
    String getUserName();

    /**
     * 访问数据库服务的密码
     * @return
     */
    String getPassword();

    /**
     * 符编码类型，eg："utf8"
     * @return 字
     */
    String getCharset();

    /**
     * 排序规则，eg："utf8_general_ci"
     * @return
     */
    String getCollation();
}
