package cn.liberg.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBConnector {
    private Log logger = LogFactory.getLog(getClass());
    private static int MAX_CONNECTION_COUNT = 1500;
    private static int MAX_CONNECTION_FREE_TIME = 1 * 60 * 60 * 1000;
    private final ArrayList<ConnectionInfo> mFreeConnections;
    private final HashMap<String, Connection> mTranMap;

    private IDataBaseConf dbConf = null;
    private int mCurrentCount = 0;
    private int mMaxCount = 0;
    private String connectUrl;

    public DBConnector() {
        mFreeConnections = new ArrayList<>();
        mTranMap = new HashMap<>();
    }

    private class ConnectionInfo {
        public Connection mConnection = null;
        public Long mLastUseTime = null;
    }

    public int getConnectCount() {
        int result = 0;
        synchronized (mFreeConnections) {
            result = mCurrentCount;
        }
        return result;
    }

    public int getMaxConnectCount() {
        return mMaxCount;
    }

    public void init(IDataBaseConf conf) {
        dbConf = conf;

        String url = dbConf.getUrl();
        StringBuilder sb = new StringBuilder(256);
        sb.append(url);
        if (!url.endsWith("/")) {
            sb.append("/");
        }
        sb.append(dbConf.getDbName());
        sb.append("?useSSL=false&serverTimezone=UTC&characterEncoding=");
        sb.append(dbConf.getCharset());
        connectUrl = sb.toString();

        // Load driver class, and try to create database if not exist
        tryConnect(dbConf);
    }


    public Connection getConnect() throws SQLException {
        Connection con = null;
        long threadId = Thread.currentThread().getId();
        synchronized (mTranMap) {
            con = mTranMap.get(Long.toString(threadId));
        }
        if (con == null) {
            synchronized (mFreeConnections) {
                while (mFreeConnections.size() > 0) {
                    ConnectionInfo conInfo = mFreeConnections.remove(0);
                    while (mFreeConnections.size() > 200) { //free too much
                        ConnectionInfo freeConInfo = mFreeConnections.remove(0);
                        freeConnection(freeConInfo.mConnection, true);
                    }
                    if (!conInfo.mConnection.isClosed()) {
                        long now = (new Date()).getTime();
                        if (now - conInfo.mLastUseTime <= MAX_CONNECTION_FREE_TIME) {
                            con = conInfo.mConnection;
                            break;
                        } else {
                            freeConnection(conInfo.mConnection, true);
                        }
                    }
                }
                if (con == null) {
                    if (mCurrentCount < MAX_CONNECTION_COUNT) {
                        con = DriverManager.getConnection(connectUrl, dbConf.getUserName(), dbConf.getPassword());
                        mCurrentCount++;
                        if (mCurrentCount > mMaxCount) {
                            mMaxCount = mCurrentCount;
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
            throw new IllegalArgumentException("Failed to load driver: " + conf.getDriverName(), e);
        }

        SQLException exception = null;
        try {
            Connection conn = DriverManager.getConnection(connectUrl, dbConf.getUserName(), dbConf.getPassword());
            conn.close();
        } catch (SQLException e) {
            exception = e;
        }
        //1049 Unknown database
        if (exception != null && exception.getErrorCode() == 1049) {
            String dbName = conf.getDbName();

            //kick off the database-name
            String url = connectUrl.replace(dbName, "");
            Connection conn = null;
            Statement stat = null;
            try {
                conn = DriverManager.getConnection(url, dbConf.getUserName(), dbConf.getPassword());
                stat = conn.createStatement();
                createDatabase(stat, conf);
                logger.info("Database created: " + dbName);
            } catch (SQLException e) {
                logger.error(e);
                throw new IllegalArgumentException("Failed to connect: " + url);
            } finally {
                try {
                    if (conn != null) stat.close();
                    if (stat != null) conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void freeConnection(Connection connection, boolean forceClose) {
        long threadId = Thread.currentThread().getId();
        synchronized (mTranMap) {
            if (mTranMap.containsKey(Long.toString(threadId))) {
                return;
            }
        }
        synchronized (mFreeConnections) {
            if (forceClose) {
                try {
                    connection.close();
                    mCurrentCount--;
                } catch (SQLException e) {
                    logger.error("connection close error", e);
                }
            } else {
                ConnectionInfo info = new ConnectionInfo();
                info.mConnection = connection;
                info.mLastUseTime = (new Date()).getTime();
                mFreeConnections.add(info);
            }
        }
    }

    public void beginTransact() throws SQLException {
        Connection connect = getConnect();
        connect.setAutoCommit(false);
        long threadId = Thread.currentThread().getId();
        synchronized (mTranMap) {
            mTranMap.put(Long.toString(threadId), connect);
        }

    }

    public void transactRollback() throws SQLException {
        Connection con = null;
        long threadId = Thread.currentThread().getId();
        synchronized (mTranMap) {
            con = mTranMap.remove(Long.toString(threadId));
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
        long threadId = Thread.currentThread().getId();
        synchronized (mTranMap) {
            con = mTranMap.remove(Long.toString(threadId));
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
        synchronized (mFreeConnections) {
            while (mFreeConnections.size() > 0) {
                ConnectionInfo conInfo = mFreeConnections.remove(0);
                freeConnection(conInfo.mConnection, true);
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
