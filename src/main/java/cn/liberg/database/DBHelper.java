package cn.liberg.database;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 数据库访问类，ORM的核心类
 *
 * @author Liberg
 */
public class DBHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private static volatile DBHelper selfInstance = null;

    public static final String VERSION = "1.3.2";
    public IDataBaseConf dbConf = null;
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

    /**
     * DB初始化入口
     *
     * 控制台ANSI颜色编码说明：
     * 编码开始(ESC_START): \033[
     * 编码结束(ESC_END):   m
     * -------------------------
     * 前景黑色(BLACK_FG):  30
     * 前景红色(RED_FG):    31
     * 前景绿色(GREEN_FG):  32
     * 前景黄色(YELLOW_FG): 33
     * 前景蓝色(BLUE_FG):   34
     */
    public synchronized void init(IDataBase dbImpl) {
        if (initialized == false) {
            System.out.println("\033[32m"+
                    "  _      _ _                    \n" +
                    " | |    (_| |                   \n" +
                    " | |     _| |__   ___ _ __ __ _ \n" +
                    " | |    | | '_ \\ / _ | '__/ _` |\n" +
                    " | |____| | |_) |  __| | | (_| |\n" +
                    " |______|_|_.__/ \\___|_|  \\__, |\n" +
                    "                           __/ |\n" +
                    " Liberg (\033[31mv" + VERSION + "\033[32m)          |___/ \n\033[39m");
            createDatabaseIfAbsent(dbImpl.getConfig());
            ArrayList<IDataBase> dBCreators = new ArrayList<>();
            dBCreators.add(dbImpl);
            initDatabase(dBCreators);
        }
        initialized = true;
    }

    private DBVersionManager dbVersionMgr;
    private DBConnector dbConnector;
    AsyncSqlExecutor asyncSqlExecutor;

    public int getAsyncWaitCount() {
        return asyncSqlExecutor.getWaitCount();
    }

    protected void createDatabaseIfAbsent(IDataBaseConf dbInfo) {
        dbConf = dbInfo;
        dbConnector.init(dbConf);
    }

    public void initDatabase(ArrayList<IDataBase> dbs) {
        for (IDataBase creator : dbs) {
            dbVersionMgr.addDatabase(creator);
        }
        dbVersionMgr.dbInit();
        //开启异步Sql执行线程
        asyncSqlExecutor = new AsyncSqlExecutor(this);
        asyncSqlExecutor.start();
    }


    /**
     * 判断SQLException是否是通信错误所致
     *
     * 如果是通信错误，有必要释放连接池中所有的空闲连接
     * @param e {@link SQLException}
     */
    public static boolean isTxError(SQLException e) {
        if (e instanceof CommunicationsException
                || e instanceof SQLNonTransientConnectionException) {
            return true;
        } else {
            return false;
        }
    }

    public <T> long save(T entity, BaseDao<T> dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        boolean isTxError = false;
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
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn, isTxError);
        }
    }

    public <T> T getById(long id, BaseDao<T> dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        boolean isTxError = false;
        T entity = null;
        try {
            conn = dbConnector.getConnect();
            ps = conn.prepareStatement(dao.SQL0_GET_BY_ID);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if(rs.next()) {
                entity = dao.buildEntity(rs);
            }
            return entity;
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn, isTxError);
        }
    }

    public <T> T getBySql(String sql, BaseDao<T> dao) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        boolean isTxError = false;
        T entity = null;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            if(rs.next()) {
                entity = dao.buildEntity(rs);
            }
            return entity;
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn, isTxError);
        }
    }

    public <T> List<T> getAllBySql(String sql, BaseDao<T> dao) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        List<T> list = new ArrayList<>();
        boolean isTxError = false;
        ResultSet rs;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            while (rs.next()) {
                T entity = dao.buildEntity(rs);
                list.add(entity);
            }
            return list;
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn, isTxError);
        }
    }

    public void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("db error", e);
            }
        }
    }

    public void closeConnect(Connection connect, boolean isTxError) {
        try {
            if (isTxError) {
                dbConnector.freeAllConnection(connect);
            } else {
                dbConnector.freeConnection(connect, false);
            }
        } catch (Exception e) {
            logger.error("db error", e);
        }
    }

    public void close(Statement statement, Connection conn, boolean isTxError) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.error("db error", e);
        }
        closeConnect(conn, isTxError);
    }

    public void close(Statement statement, boolean isTxError) {
        try {
            if (statement != null) {
                Connection conn = statement.getConnection();
                statement.close();
                closeConnect(conn, isTxError);
            }
        } catch (SQLException e) {
            logger.error("db error", e);
        }
    }

    public <T> void update(T entity, BaseDao<T> dao) throws OperatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isTxError = false;
        try {
            conn = dbConnector.getConnect();
            ps = conn.prepareStatement(dao.SQL0_UPDATE_BY_ID);
            dao.fillPreparedStatement(entity, ps);
            ps.setLong(dao.getColumnCount(), dao.getEntityId(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(ps, conn, isTxError);
        }
    }

    /**
     * 更新entity中指定的列到数据库
     */
    public <T> void update(T entity, BaseDao dao, Column... columns) throws OperatorException {
        StringBuilder sb = new StringBuilder(32);
        for (Column column : columns) {
            sb.append(column.name);
            sb.append('=');
            sb.append(SqlDefender.format(column.getEntityValue(entity)));
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            String sql = String.format("update %1$s set %2$s where id=%3$s",
                    dao.getTableName(), sb.toString(), dao.getEntityId(entity));
            executeSql(sql);
        }
    }

    /**
     * 更新entity的未在excludes中指定的列到数据库
     */
    public <T> void updateExclusive(T entity, BaseDao dao, Set<Column> excludes) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        final List<Column> columns = dao.getColumns();
        for (Column column : columns) {
            if (!excludes.contains(column)) {
                sb.append(column.name);
                sb.append('=');
                sb.append(SqlDefender.format(column.getEntityValue(entity)));
                sb.append(',');
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String sql = String.format("update %1$s set %2$s where id=%3$s",
                dao.getTableName(), sb.toString(), dao.getEntityId(entity));
        executeSql(sql);
    }

    public void delete(BaseDao dao, String where) throws OperatorException {
        String sql = String.format("delete from  %1$s where %2$s", dao.getTableName(), where);
        executeSql(sql);
    }

    public <T> void delete(BaseDao<T> dao, T obj) throws OperatorException {
        long id = dao.getEntityId(obj);
        delete(dao, id);
    }

    public <T> void delete(BaseDao<T> dao, long id) throws OperatorException {
        delete(dao, "id=" + id);
    }

    /**
     * 清空表
     */
    public void clear(BaseDao dao) throws OperatorException {
        executeSql("delete from " + dao.getTableName());
    }

    public void executeQuery(String sql, IDataReader reader) throws OperatorException {
        Statement stat = null;
        Connection conn = null;
        boolean isTxError = false;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            reader.read(rs);
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn, isTxError);
        }
    }

    public int executeSql(String sql) throws OperatorException {
        Connection conn = null;
        Statement stat = null;
        boolean isTxError = false;
        int rc;
        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rc = stat.executeUpdate(sql);
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn, isTxError);
        }
        return rc;
    }

    public long queryLong(String sql) throws OperatorException {
        long rc = 0;
        Statement stat = null;
        boolean isTxError = false;
        if (sql != null) {
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = dbConnector.getConnect();
                stat = conn.createStatement();
                rs = stat.executeQuery(sql);
                if (rs.next()) {
                    rc = rs.getLong(1);
                }
            } catch (SQLException e) {
                isTxError = isTxError(e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn, isTxError);
            }
        }
        return rc;
    }

    public String queryString(String sql) throws OperatorException {
        String rt = null;
        Statement stat = null;
        boolean isTxError = false;
        if (sql != null) {
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = dbConnector.getConnect();
                stat = conn.createStatement();
                rs = stat.executeQuery(sql);
                if (rs.next()) {
                    rt = rs.getString(1);
                }
            } catch (SQLException e) {
                isTxError = isTxError(e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn, isTxError);
            }
        }
        return rt;
    }

    public <T> void saveOrUpdateBatch(BaseDao<T> dao, List<T> entityList) throws OperatorException {
        if (entityList == null || entityList.size() == 0) {
            return;
        }
        PreparedStatement psSave = null;
        PreparedStatement psUpdate = null;
        boolean isTxError = false;
        Connection conn = null;
        ResultSet rs;
        try {
            conn = dbConnector.getConnect();
            long id;
            for (T entity : entityList) {
                if((id = dao.getEntityId(entity)) > 0) {
                    if(psUpdate == null) {
                        psUpdate = conn.prepareStatement(dao.SQL0_UPDATE_BY_ID);
                    }
                    dao.fillPreparedStatement(entity, psUpdate);
                    psUpdate.setLong(dao.getColumnCount(), id);
                    psUpdate.executeUpdate();
                } else {
                    if(psSave == null) {
                        psSave = conn.prepareStatement(dao.SQL0_SAVE, PreparedStatement.RETURN_GENERATED_KEYS);
                    }
                    dao.fillPreparedStatement(entity, psSave);
                    psSave.executeUpdate();
                    rs = psSave.getGeneratedKeys();
                    if (rs.next()) {
                        dao.setEntityId(entity, rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            closeStatement(psSave);
            closeStatement(psUpdate);
            closeConnect(conn, isTxError);
        }
    }

    public void executeSqlBatch(Collection<String> sqls) throws OperatorException {
        if (sqls != null && sqls.size() > 0) {
            Statement stat = null;
            Connection conn = null;
            boolean isTxError = false;
            try {
                beginTransact();
                conn = dbConnector.getConnect();
                stat = conn.createStatement();
                for (String sql : sqls) {
                    stat.addBatch(sql);
                }
                stat.executeBatch();
                endTransact();
            } catch (SQLException e) {
                transactRollback();
                isTxError = isTxError(e);
                throw new OperatorException(StatusCode.ERROR_DB, e);
            } finally {
                close(stat, conn, isTxError);
            }
        }
    }

    private List<String> buildSaveOrUpdateSqls(BaseDao dao, List<?> objs) throws OperatorException {
        List<String> sqls = new ArrayList<>(objs.size());
        String sql;
        for (Object obj : objs) {
            long id = dao.getEntityId(obj);
            if (id > 0) {
                sql = buildUpdateSql(obj, dao);
            } else {
                sql = buildSaveSql(obj, dao);
            }
            if (sql != null) {
                sqls.add(sql);
            }
        }
        return sqls;
    }

    public static <T> String buildUpdateSql(T entity, BaseDao<T> dao) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        long id = dao.getEntityId(entity);
        final List<Column> columns = dao.getColumns();
        if(columns.size() > 0) {
            for (Column column : columns) {
                sb.append(column.name);
                sb.append('=');
                sb.append(SqlDefender.format(column.getEntityValue(entity)));
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            return String.format("update %1$s set %2$s where id=%3$s",
                    dao.getTableName(), sb.toString(), id);
        } else {
            return null;
        }
    }

    public static <T> String buildSaveSql(T entity, BaseDao<T> dao) throws OperatorException {
        StringBuilder sb = new StringBuilder();
        final List<Column> columns = dao.getColumns();
        if(columns.size()> 0) {
            for (Column column : columns) {
                sb.append(SqlDefender.format(column.getEntityValue(entity)));
                sb.append(',');
            }
            sb.deleteCharAt(sb.length()-1);
            return String.format("insert into %1$s(%2$s) values (%3$s)",
                    dao.getTableName(), dao.COLUMNS_STRING, sb.toString());
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
        boolean isTxError = false;

        try {
            conn = dbConnector.getConnect();
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            td = new TableData();
            td.heads = new String[columnCount];
            td.datas = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                td.heads[i - 1] = meta.getColumnLabel(i);
            }

            Object[] row;
            while (rs.next()) {
                row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                td.datas.add(row);
            }
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            close(stat, conn, isTxError);
        }
        return td;
    }

    public PreparedStatement prepareStatement(String sql) throws OperatorException {
        Connection conn = null;
        boolean isTxError = false;
        try {
            conn = dbConnector.getConnect();
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            if(isTxError) {
                closeConnect(conn, true);
            }
        }
    }

    public Statement createStatement() throws OperatorException {
        Connection conn = null;
        boolean isTxError = false;
        try {
            conn = dbConnector.getConnect();
            return conn.createStatement();
        } catch (SQLException e) {
            isTxError = isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            if(isTxError) {
                closeConnect(conn, true);
            }
        }
    }
}
