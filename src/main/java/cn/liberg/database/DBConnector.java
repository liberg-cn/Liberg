package cn.liberg.database;

import com.mysql.cj.exceptions.MysqlErrorNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 一个小巧的数据库连接池实现
 *
 * @author Liberg
 */
public class DBConnector {
    private static final Logger logger = LoggerFactory.getLogger(DBConnector.class);

    private static int MAX_CONNECTION_COUNT = 1200;
    private static int MAX_CONNECTION_FREE_TIME = 1 * 60 * 60 * 1000;

    /**
     * 保留空闲的连接
     */
    private final ArrayList<ConnectionInfo> freeList;

    /**
     * 处于事务中的连接单独保存到Map
     * 一个事务的开始、提交、回滚必须在同一个连接中完成
     */
    private final HashMap<String, Connection> inTransactionMap;
    private IDataBaseConf dbConf;
    private String connectUrl;
    /**
     * 当前连接数，包括空闲连接
     */
    private volatile int connectCount = 0;
    /**
     * 连接数达到的上限
     */
    private volatile int maxCount = 0;

    public DBConnector() {
        freeList = new ArrayList<>();
        inTransactionMap = new HashMap<>();
    }

    private class ConnectionInfo {
        public Connection connection;
        public Long lastUseTime;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public int getFreeCount() {
        int freeCount;
        synchronized (freeList) {
            freeCount = freeList.size();
        }
        return freeCount;
    }

    public int getMaxConnectCount() {
        return maxCount;
    }

    public void init(IDataBaseConf conf) {
        dbConf = conf;

        String url = dbConf.getUrl();
        StringBuilder sb = new StringBuilder(256);
        sb.append(url);
        if (!url.endsWith("/")) {
            sb.append('/');
        }
        sb.append(dbConf.getDbName());
        sb.append("?useSSL=false&serverTimezone=UTC&characterEncoding=");
        sb.append(dbConf.getCharset());
        connectUrl = sb.toString();

        // Load jdbc driver class, and try to create database if absent
        tryConnect(dbConf);
    }


    public Connection getConnect() throws SQLException {
        Connection con;
        String threadId = Long.toString(Thread.currentThread().getId());
        synchronized (inTransactionMap) {
            con = inTransactionMap.get(threadId);
        }
        if (con == null) {
            synchronized (freeList) {
                while (freeList.size() > 0) {
                    ConnectionInfo conInfo = freeList.remove(0);
                    //free too much
                    while (freeList.size() > 200) {
                        ConnectionInfo freeConInfo = freeList.remove(0);
                        freeConnection(freeConInfo.connection, true);
                    }
                    if (!conInfo.connection.isClosed()) {
                        long now = (new Date()).getTime();
                        if (now - conInfo.lastUseTime <= MAX_CONNECTION_FREE_TIME) {
                            con = conInfo.connection;
                            break;
                        } else {
                            freeConnection(conInfo.connection, true);
                        }
                    }
                }
                if (con == null) {
                    if (connectCount < MAX_CONNECTION_COUNT) {
                        con = DriverManager.getConnection(connectUrl, dbConf.getUserName(), dbConf.getPassword());
                        connectCount++;
                        if (connectCount > maxCount) {
                            maxCount = connectCount;
                        }
                    } else {
                        throw new SQLException("Max connection count limited: " + MAX_CONNECTION_COUNT);
                    }
                }
            }
        }
        return con;
    }

    private void tryConnect(IDataBaseConf conf) {
        try {
            Class.forName(conf.getDriverName());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load jdbc driver: " + conf.getDriverName(), e);
        }

        SQLException exception = null;
        try {
            Connection conn = DriverManager.getConnection(connectUrl, dbConf.getUserName(), dbConf.getPassword());
            conn.close();
        } catch (SQLException e) {
            exception = e;
        }
        //1049 Unknown database
        if (exception != null && exception.getErrorCode() == MysqlErrorNumbers.ER_BAD_DB_ERROR) {
            String dbName = conf.getDbName();

            //kick off the database name
            String url = connectUrl.replace('/' + dbName + '?', "/?");
            Connection conn = null;
            Statement stat = null;
            try {
                conn = DriverManager.getConnection(url, dbConf.getUserName(), dbConf.getPassword());
                stat = conn.createStatement();
                createDatabase(stat, conf);
                logger.info("Database created: {}", dbName);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to connect: " + url);
            } finally {
                try {
                    if (conn != null) {
                        stat.close();
                    }
                    if (stat != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                }
            }
        }
    }

    public void freeConnection(Connection connection, boolean forceClose) {
        if (connection == null) {
            return;
        }
        final String threadId = Long.toString(Thread.currentThread().getId());
        synchronized (inTransactionMap) {
            if (inTransactionMap.containsKey(threadId)) {
                // 处于事务中的连接应由endTransact/transactRollback完成释放
                return;
            }
        }
        synchronized (freeList) {
            if (forceClose) {
                try {
                    connection.close();
                    connectCount--;
                } catch (SQLException e) {
                    logger.error("Connection close error", e);
                }
            } else {
                ConnectionInfo info = new ConnectionInfo();
                info.connection = connection;
                info.lastUseTime = System.currentTimeMillis();
                freeList.add(info);
            }
        }
    }

    public void beginTransact() throws SQLException {
        Connection connect = getConnect();
        connect.setAutoCommit(false);
        final String threadId = Long.toString(Thread.currentThread().getId());
        synchronized (inTransactionMap) {
            inTransactionMap.put(threadId, connect);
        }
    }

    public void transactRollback() throws SQLException {
        Connection con = null;
        final String threadId = Long.toString(Thread.currentThread().getId());
        synchronized (inTransactionMap) {
            con = inTransactionMap.remove(threadId);
        }
        if (con != null) {
            try {
                con.rollback();
            } finally {
                con.setAutoCommit(true);
                freeConnection(con, false);
            }
        }
    }

    public void endTransact() throws SQLException {
        Connection con = null;
        final String threadId = Long.toString(Thread.currentThread().getId());
        synchronized (inTransactionMap) {
            con = inTransactionMap.remove(threadId);
        }
        if (con != null) {
            try {
                con.commit();
            } finally {
                con.setAutoCommit(true);
                freeConnection(con, false);
            }
        }
    }

    public void freeAllConnection(Connection connect) {
        freeConnection(connect, true);
        if(freeList.size() > 0) {
            synchronized (freeList) {
                while (freeList.size() > 0) {
                    ConnectionInfo conInfo = freeList.remove(0);
                    freeConnection(conInfo.connection, true);
                }
            }
        }
    }

    public void freeAllConnection() {
        if(freeList.size() > 0) {
            synchronized (freeList) {
                while (freeList.size() > 0) {
                    ConnectionInfo conInfo = freeList.remove(freeList.size()-1);
                    freeConnection(conInfo.connection, true);
                }
            }
        }
    }

    private void createDatabase(Statement stat, IDataBaseConf conf) throws SQLException {
        StringBuilder sb = new StringBuilder(128);
        sb.append("CREATE DATABASE IF NOT EXISTS ");
        sb.append(conf.getDbName());
        sb.append(" DEFAULT CHARACTER SET ");
        sb.append(conf.getCharset());
        sb.append(" DEFAULT COLLATE ");
        sb.append(conf.getCollation());
        stat.executeUpdate(sb.toString());
    }
}
