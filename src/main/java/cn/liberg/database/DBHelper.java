package cn.liberg.database;

import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBHelper {
    Log logger = LogFactory.getLog(getClass());

    public static final String VERSION = "1.2.0";
    private static volatile DBHelper selfInstance = null;
    boolean initialized = false;

    public static DBHelper self() {
        if (selfInstance == null) {
            synchronized (DBHelper.class) {
                if (selfInstance == null) {
                    selfInstance = new DBHelper();
                }
            }
        }
        return selfInstance;
    }

    private DBHelper() {
        dbConnector = new DBConnector();
        dbVersionMgr = new DBVersionManager(dbConnector);
    }

    public synchronized void init(IDataBase dbImpl) {
        if (initialized == false) {
            System.out.println(
                    "  _      _ _                    \n" +
                            " | |    (_| |                   \n" +
                            " | |     _| |__   ___ _ __ __ _ \n" +
                            " | |    | | '_ \\ / _ | '__/ _` |\n" +
                            " | |____| | |_) |  __| | | (_| |\n" +
                            " |______|_|_.__/ \\___|_|  \\__, |\n" +
                            "                           __/ |\n" +
                            " Liberg (v"+VERSION+")          |___/ \n");
            createDatabaseIfAbsent(dbImpl.getConfig());
            ArrayList<IDataBase> dBCreators = new ArrayList<>();
            dBCreators.add(dbImpl);
            initDatabase(dBCreators);
        }
        initialized = true;
    }

    private static final long ERR_WAIT_TIME = 1 * 60 * 1000;
    private boolean mHaveSocketError = false;
    private int mSocketErrorCount = 0;
    private Date mLastSocketErrorTime = null;


    private byte[] mLock = new byte[0];
    public static Pattern mEmoji = Pattern.compile(
            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    private DBVersionManager dbVersionMgr;
    private DBConnector dbConnector;
    private AsyncDbOperator mAsyncOPer = null;
    public IDataBaseConf dbConf = null;

    protected void createDatabaseIfAbsent(IDataBaseConf dbInfo) {
        if (this.isCanUseDBStore()) {
            dbConf = dbInfo;
            dbConnector.init(dbConf);
        }
    }

    public void initDatabase(ArrayList<IDataBase> dbs) {
        for (IDataBase creator : dbs) {
            dbVersionMgr.addDatabase(creator);
        }
        dbVersionMgr.dbInit();
        //开启异步Sql执行线程
        mAsyncOPer = new AsyncDbOperator(this);
        mAsyncOPer.start();
    }

    protected PreparedQuery prepare(PreparedQueryBuilder preparedBuilder) throws SQLException {
        Connection conn = openConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(preparedBuilder.build());
            return new PreparedQuery(preparedBuilder.getDao(), ps);
        } catch (SQLException e) {
            logger.error("prepare failed.");
            closeConnect(conn, true);
            throw e;
        }
    }
    protected PreparedPartialQuery prepare(PreparedPartialQueryBuilder preparedBuilder) throws SQLException {
        Connection conn = openConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(preparedBuilder.build());
            return new PreparedPartialQuery(preparedBuilder.getColumns(), ps);
        } catch (SQLException e) {
            logger.error("prepare failed.");
            closeConnect(conn, true);
            throw e;
        }
    }
    protected PreparedColumnQuery prepare(PreparedColumnQueryBuilder preparedBuilder) throws SQLException {
        Connection conn = openConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(preparedBuilder.build());
            return new PreparedColumnQuery(preparedBuilder.getColumn(), ps);
        } catch (SQLException e) {
            logger.error("prepare failed.");
            closeConnect(conn, true);
            throw e;
        }
    }
    protected PreparedPartialUpdate prepare(PreparedPartialUpdateBuilder preparedBuilder) throws SQLException {
        Connection conn = openConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(preparedBuilder.build());
            return new PreparedPartialUpdate(preparedBuilder.getColumns(), ps);
        } catch (SQLException e) {
            logger.error("prepare failed.");
            closeConnect(conn, true);
            throw e;
        }
    }
    protected PreparedColumnUpdate prepare(PreparedColumnUpdateBuilder preparedBuilder) throws SQLException {
        Connection conn = openConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(preparedBuilder.build());
            return new PreparedColumnUpdate(preparedBuilder.getColumn(), ps);
        } catch (SQLException e) {
            logger.error("prepare failed.");
            closeConnect(conn, true);
            throw e;
        }
    }

    public long save(Object entity, BaseDao dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnector.getConnect();
            ps = conn.prepareStatement(dao.SQL0_SAVE, PreparedStatement.RETURN_GENERATED_KEYS);
            dao.fillPreparedStatement(entity, ps);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            long generatedId = 0;
            if(rs.next()) {
                generatedId = rs.getLong(1);
                dao.setEntityId(entity, generatedId);
            }
            return generatedId;
        } catch (SQLException e) {
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn);
        }
    }
    public <T> void update(T entity, BaseDao dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dbConnector.getConnect();
            ps = conn.prepareStatement(dao.SQL0_UPDATE_BY_ID);
            dao.fillPreparedStatement(entity, ps);
            ps.setLong(dao.getColumnCount(), dao.getEntityId(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn);
        }
    }
    public <T> T getById(long id, BaseDao dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T entity = null;
        try {
            conn = dbConnector.getConnect();
            ps = conn.prepareStatement(dao.SQL0_GET_BY_ID);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if(rs.next()) {
                entity = (T) dao.buildEntity(rs);
            }
            return entity;
        } catch (SQLException e) {
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn);
        }
    }

    public <T> T getBySql(String sql, BaseDao dao) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        T entity = null;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            if(rs.next()) {
                entity = (T) dao.buildEntity(rs);
            }
            return entity;
        } catch (SQLException e) {
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn);
        }
    }

    public <T> List<T> getAllBySql(String sql, BaseDao dao) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<>();
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            while (rs.next()) {
                T entity = (T) dao.buildEntity(rs);
                list.add(entity);
            }
            return list;
        } catch (SQLException e) {
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn);
        }
    }

    private boolean isCanUseDBStore() {
        boolean result = false;
        if (mHaveSocketError == false) {
            result = true;
        } else if (mLastSocketErrorTime != null) {
            long internal = (new Date()).getTime() - mLastSocketErrorTime.getTime();
            if (internal > ERR_WAIT_TIME) {
                result = true;
                mHaveSocketError = false;
                mLastSocketErrorTime = null;
            }
        }
        return result;
    }

    private void setDBException(Exception ex) {
        if (mHaveSocketError == false) {
            //TODO 连接异常断开后的恢复机制
            /*if (ex instanceof CommunicationsException || (ex.getCause() != null
                    && (ex.getCause() instanceof SocketException || ex.getCause() instanceof
                    ConnectException)) || ex instanceof SocketException || ex instanceof
                    ConnectException || ex instanceof MySQLNonTransientConnectionException) {
                mHaveSocketError = true;
                mLastSocketErrorTime = new Date();
            }*/

        }
    }

    private Connection openConnect() throws SQLException {
        return dbConnector.getConnect();
    }

    public void closeConnect(Connection connect, boolean forceClose) {
        if (connect != null) {
            try {
                if (forceClose == false) {
                    mSocketErrorCount = 0;
                    dbConnector.freeConnection(connect, forceClose);
                } else {
                    synchronized (mLock) {
                        dbConnector.freeAllConnection(connect);
                        mSocketErrorCount++;
                        if (mSocketErrorCount < 100) {
                            mHaveSocketError = false;
                            mLastSocketErrorTime = null;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("db error", e);
            }
        } else if (forceClose) {
            synchronized (mLock) {
                mSocketErrorCount++;
                if (mSocketErrorCount < 100) {
                    mHaveSocketError = false;
                    mLastSocketErrorTime = null;
                }
            }
        }
    }

    public void close(Statement statement, Connection conn) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.error("db error", e);
        }
        closeConnect(conn, mHaveSocketError);
    }

    public AsyncDbOperator newAsyncOperator() {
        AsyncDbOperator asyncOPer = new AsyncDbOperator(this);
        asyncOPer.start();
        return asyncOPer;
    }

    public void asyncSave(Object obj) throws OperatorException {
        asyncSave(obj, mAsyncOPer);
    }

    public void asyncUpdate(Object obj) {
        asyncUpdate(obj, mAsyncOPer);
    }

    public void asyncExecuteSql(String sql) {
        asyncExecuteSql(sql, mAsyncOPer);
    }

    public void asyncSave(Object obj, AsyncDbOperator op) throws OperatorException {
        op.saveObject(obj);
    }

    public void asyncUpdate(Object obj, AsyncDbOperator op) {
        if (isCanUseDBStore()) {
            op.updateObj(obj);
        }
    }

    public void asyncExecuteSql(String sql, AsyncDbOperator op) {
        if (isCanUseDBStore()) {
            op.ExecuteSql(sql);
        }
    }

    public int getAsyncWaitCount() {
        if (mAsyncOPer != null) {
            return mAsyncOPer.getWaitCount();
        } else {
            return 0;
        }
    }

    public void updateExclude(BaseDao dao, Object obj, Set<Column> excludes) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        final List<Column> columns = dao.getColumns();
        try {
            for (Column column : columns) {
                if (!excludes.contains(column)) {
                    Object val = column.getEntityValue(obj);
                    if (val != null) {
                        sb.append(column.getName());
                        sb.append("=");
                        val = getDBValue(val) + ",";
                    }
                    sb.append(val);
                }
            }
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_SERVER, e);
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        String sql = String.format("update %1$s set %2$s where %3$s=%4$s",
                dao.getTableName(), sb.toString(),
                IDao.TABLE_ID, dao.getEntityId(obj));
        executeSql(sql);
    }

    public void delete(BaseDao dao, String where) throws OperatorException {
        String sql = String.format("delete from  %1$s where %2$s", dao.getTableName(), where);
        executeSql(sql);
    }

    public void delete(BaseDao dao, Object obj) throws OperatorException {
        long id = dao.getEntityId(obj);
        delete(dao, id);
    }

    public void delete(BaseDao dao, long id) throws OperatorException {
        delete(dao, IDao.TABLE_ID + "=" + id);
    }

    public void clear(BaseDao dao) throws OperatorException {
        executeSql("delete from " + dao.getTableName());
    }

    public void executeQuery(String sql, IDataReader reader) throws OperatorException {
        Statement stat = null;
        Connection conn = null;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            reader.read(rs);
        } catch (SQLException e) {
            setDBException(e);
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn);
        }
    }

    public int executeSql(String sql) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        int rc = 0;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rc = stat.executeUpdate(sql);
        } catch (SQLException e) {
            setDBException(e);
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn);
        }
        return rc;
    }

    public long queryLong(String sql) throws OperatorException {
        long rc = 0;
        Statement stat = null;
        if (sql != null) {
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = openConnect();
                stat = conn.createStatement();
                rs = stat.executeQuery(sql);
                if (rs.next()) {
                    rc = rs.getLong(1);
                }
            } catch (SQLException e) {
                setDBException(e);
                logger.error("db error", e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn);
            }
        }
        return rc;
    }

    public String queryString(String sql) throws OperatorException {
        String rt = null;
        Statement stat = null;
        if (sql != null) {
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = openConnect();
                stat = conn.createStatement();
                rs = stat.executeQuery(sql);
                if (rs.next()) {
                    rt = rs.getString(1);
                }
            } catch (SQLException e) {
                setDBException(e);
                logger.error("db error", e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn);
            }
        }
        return rt;
    }

    public static Object getDBValue(Object objValue) {
        if (objValue != null) {
            if (objValue instanceof Boolean) {
                if (((Boolean) objValue).booleanValue()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (objValue instanceof Date) {
                return ((Date) objValue).getTime();
            } else if (objValue instanceof String) {
                String value = (String) objValue;
                value = formatSqlValue(value);
                return "'" + value + "'";
            } else {
                throw new RuntimeException("Unsupported type: " + objValue.getClass().getName());
            }
        }
        return null;
    }

    public static String formatSqlValue(String value) {
        String result = value;
        result = result.replace("\\", "\\\\");
        result = result.replace("'", "\\'");
        result = filterEmoji(result);

        return result;
    }

    public static String filterEmoji(String source) {
        if (source != null) {
            Pattern emoji = mEmoji;
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                source = emojiMatcher.replaceAll("");
                return source;
            }
            return source;
        }
        return null;
    }

    public void saveOrUpdateBatch(BaseDao dao, List<?> objs) throws OperatorException {
        if (objs != null && objs.size() > 0) {
            //TODO 使用PreparedStatement方式效率更高
            long beginTime = System.currentTimeMillis();
            final List<String> sqls = buildSaveOrUpdateSqls(dao, objs);
            executeSqlBatch(sqls);
            long endTime = System.currentTimeMillis();
            logger.debug("beginTime:" + beginTime + ", endTime:"
                    + endTime + ", timeTake: " + (endTime - beginTime) + ", size:" + objs.size());
        }
    }

    public void executeSqlBatch(Collection<String> sqls) throws OperatorException {
        if (sqls != null && sqls.size() > 0) {
            Statement stat = null;
            Connection conn = null;
            try {
                beginTransact();
                conn = openConnect();
                stat = conn.createStatement();
                for (String sql : sqls) {
                    stat.addBatch(sql);
                }
                stat.executeBatch();
                endTransact();
            } catch (SQLException e) {
                transactRollback();
                setDBException(e);
                logger.error("db error", e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn);
            }
        }
    }

    private List<String> buildSaveOrUpdateSqls(BaseDao dao, List<?> objs) throws OperatorException {
        List<String> sqls = new ArrayList<String>(objs.size());
        String sql;
        for (Object obj : objs) {
            long id = dao.getEntityId(obj);
            if (id > 0) {
                sql = buildUpdateSql(dao, obj);
            } else {
                sql = buildSaveSql(dao, obj);
            }
            if (sql != null) {
                sqls.add(sql);
            }
        }
        return sqls;
    }

    public static String buildUpdateSql(BaseDao dao, Object obj) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        long id = dao.getEntityId(obj);
        final List<Column> columns = dao.getColumns();
        if(columns.size() > 0) {
            try {
                for (Column column : columns) {
                    Object val = column.getEntityValue(obj);
                    if (val != null) {
                        sb.append(column.getName());
                        sb.append("=");
                        val = getDBValue(val) + ",";
                    }
                    sb.append(val);
                }
            } catch (Exception e) {
                throw new OperatorException(StatusCode.ERROR_SERVER, e);
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            return String.format("update %1$s set %2$s where %3$s=%4$s",
                    dao.getTableName(), sb.toString(), IDao.TABLE_ID, id);
        } else {
            return null;
        }
    }


    public static String buildSaveSql(BaseDao dao, Object obj) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        final List<Column> columns = dao.getColumns();
        if(columns.size()> 0) {
            try {
                for (Column column : columns) {
                    Object objValue = column.getEntityValue(obj);
                    if (objValue != null) {
                        sb.append(getDBValue(objValue) + ",");
                    } else {
                        sb.append("null,");
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                return String.format("insert into %1$s(%2$s) values (%3$s)",
                        dao.getTableName(), dao.COLUMNS_STRING, sb.toString());
            } catch (Exception e) {
                throw new OperatorException(StatusCode.ERROR_SERVER, e);
            }
        } else {
            return null;
        }
    }

    /**
     * * 事务开始
     */
    public void beginTransact() throws OperatorException {
        try {
            dbConnector.beginTransact();
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * * 事务回滚
     */
    public void transactRollback() throws OperatorException {
        try {
            dbConnector.transactRollback();
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * * 事务结束
     */
    public void endTransact() throws OperatorException {
        try {
            dbConnector.endTransact();
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 可用于关联查询
     */
    public TableData getTableData(String sql) throws OperatorException {
        Connection conn = null;
        ResultSet rs = null;
        Statement stat = null;
        TableData td = null;

        try {
            conn = openConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            td = new TableData();
            td.heads = new String[colCount];
            td.datas = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                td.heads[i - 1] = meta.getColumnLabel(i);
            }

            Object[] row;
            while (rs.next()) {
                row = new Object[colCount];
                for (int i = 1; i <= colCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                td.datas.add(row);
            }
        } catch (SQLException e) {
            setDBException(e);
            logger.error("db error", e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn);
        }
        return td;
    }
}
