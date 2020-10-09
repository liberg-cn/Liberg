package cn.liberg.database;

import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Liberg
 */
public abstract class BaseDao<T> {
    private Log logger = LogFactory.getLog(getClass());

    public static final LongColumn columnId = new LongColumn(IDao.TABLE_ID);
    protected String tableName;
    private DBHelper dbHelper;

    protected List<Column> columns;

    /**
     * 含_id字段的字段总数
     */
    private int columnCount;

    /**
     * 不含_id的表字段拼接结果
     */
    public String COLUMNS_STRING;

    //insert into tableName(COLUMNS_STRING) values(?,?,?)
    public String SQL0_SAVE;

    //select _id,COLUMNS_STRING from tableName where _id=?
    public String SQL0_GET_BY_ID;
    //update tableName set col1=?,col2=?,col3=? where _id=?
    public String SQL0_UPDATE_BY_ID;

    private PreparedQueryBuilder getGtIdLimit;

    /**
     * 供子类覆盖的钩子方法
     * @param entity
     * @throws OperatorException
     */
    public void fillData(T entity) throws OperatorException {
        //可能需要填充一些未映射(保存)到数据库的成员
    }

    protected BaseDao(String tableName) {
        this.tableName = tableName;
        StringBuilder sbColumns = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        columnCount = 1;
        for (Column column : getColumns()) {
            columnCount++;
            sbColumns.append(column.getName());
            sbColumns.append(",");

            sb1.append(column.getName());
            sb1.append("=");
            sb1.append("?");
            sb1.append(",");

            sb2.append("?");
            sb2.append(",");
        }
        sbColumns.deleteCharAt(sbColumns.length() - 1);
        sb1.deleteCharAt(sb1.length() - 1);
        sb2.deleteCharAt(sb2.length() - 1);

        COLUMNS_STRING = sbColumns.toString();
        SQL0_GET_BY_ID = "select " + getFullColumnsString() + " from " + tableName
                + " where " + IDao.TABLE_ID + "=?";
        SQL0_SAVE = "insert into " + tableName + "(" + COLUMNS_STRING + ") values("
                + sb2.toString() + ")";
        SQL0_UPDATE_BY_ID = "update " + tableName + " set " + sb1.toString()
                + " where " + IDao.TABLE_ID + "=?";
        dbHelper = DBHelper.self();
    }

    /**
     * notify us when data modified by any other applications
     */
    public void notifyUpdated(long entityId) throws OperatorException {

    }
    public void notifySaved(long entityId) throws OperatorException{

    }

    public String getTableName() {
        return tableName;
    }

    public String getFullColumnsString() {
        if (COLUMNS_STRING.length() > 0) {
            return IDao.TABLE_ID + "," + COLUMNS_STRING;
        } else {
            return IDao.TABLE_ID;
        }
    }

    public int getColumnCount() {
        return columnCount;
    }

    public PreparedQueryBuilder buildQuery() {
        return new PreparedQueryBuilder(this);
    }

    public PreparedPartialQueryBuilder buildQuery(Column... columns) {
        return new PreparedPartialQueryBuilder(this, columns);
    }

    public PreparedColumnQueryBuilder buildQuery(Column column) {
        return new PreparedColumnQueryBuilder(this, column);
    }

    public PreparedPartialUpdateBuilder buildUpdate(Column... columns) {
        return new PreparedPartialUpdateBuilder(this, columns);
    }

    public PreparedColumnUpdateBuilder buildUpdate(Column column) {
        return new PreparedColumnUpdateBuilder(this, column);
    }

    protected PreparedQuery prepare(PreparedQueryBuilder preparedBuilder) throws SQLException {
        return dbHelper.prepare(preparedBuilder);
    }

    protected PreparedPartialQuery prepare(PreparedPartialQueryBuilder preparedBuilder) throws SQLException {
        return dbHelper.prepare(preparedBuilder);
    }

    protected PreparedColumnQuery prepare(PreparedColumnQueryBuilder preparedBuilder) throws SQLException {
        return dbHelper.prepare(preparedBuilder);
    }

    protected PreparedPartialUpdate prepare(PreparedPartialUpdateBuilder preparedBuilder) throws SQLException {
        return dbHelper.prepare(preparedBuilder);
    }

    protected PreparedColumnUpdate prepare(PreparedColumnUpdateBuilder preparedBuilder) throws SQLException {
        return dbHelper.prepare(preparedBuilder);
    }

    public abstract T buildEntity(ResultSet rs) throws SQLException;

    public abstract void setEntityId(T entity, long id);

    public abstract long getEntityId(T entity);

    protected abstract void fillPreparedStatement(T entity, PreparedStatement ps) throws SQLException;

    /**
     * 返回非id的其他字段
     */
    public abstract List<Column> getColumns();

    public long save(T entity) throws OperatorException {
        return dbHelper.save(entity, this);
    }

    public void update(T entity) throws OperatorException {
        dbHelper.update(entity, this);
    }

    public void update(Map<String, Object> values, String where) throws OperatorException {
        StringBuilder sb = new StringBuilder(32);
        for (Entry<String, Object> entry : values.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            Object val = entry.getValue();
            if (val != null) {
                sb.append(DBHelper.getDBValue(val));
            } else {
                sb.append("null,");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            String sql = String.format("update %1$s set %2$s where %3$s", tableName, sb.toString(), where);
            dbHelper.executeSql(sql);
        }
    }

    public final T getById(long id) throws OperatorException {
        return dbHelper.getById(id, this);
    }

    public T getByWhere(String where) throws OperatorException {
        StringBuilder sb = buildWhere(where);
        sb.append(" limit 1");
        return dbHelper.getBySql(sb.toString(), this);
    }

    public List<T> getPageByWhere(int pageNum, int pageSize, String where) throws OperatorException {
        StringBuilder sb = buildWhere(where);
        sb.append(" limit ");
        sb.append((pageNum - 1) * pageSize);
        sb.append(",");
        sb.append(pageSize);
        return dbHelper.getAllBySql(sb.toString(), this);
    }

    /**
     * 查询Id比gtId大的limit条数据
     * @param gtId 查询Id比gtId大的数据
     * @param limit 查询记录条数上限
     * @return 数据记录列表，没有足够的数据时，返回数据列表的长度会小于limit
     * @throws OperatorException
     */
    public List<T> getGtIdLimit(long gtId, int limit) throws OperatorException {
        //TODO limit可以动态填充？
        if(getGtIdLimit == null) {
            getGtIdLimit = buildQuery().gt(columnId).limit(limit).asc(columnId);
        }

        try(PreparedQuery preparedQuery = prepare(getGtIdLimit)) {
            preparedQuery.set(columnId, gtId);
            return preparedQuery.all();
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public List<T> getAllByWhere(String where) throws OperatorException {
        StringBuilder sb = buildWhere(where);
        return dbHelper.getAllBySql(sb.toString(), this);
    }

    public List<T> getAllBySql(String sql) throws OperatorException {
        return dbHelper.getAllBySql(sql, this);
    }

    public List<T> getAll() throws OperatorException {
        StringBuilder sb = buildWhere(null);
        return dbHelper.getAllBySql(sb.toString(), this);
    }

    public int getCount() throws OperatorException {
        String sql = "select count(1) from " + tableName;
        return (int) dbHelper.queryLong(sql);
    }

    public int getCount(String where) throws OperatorException {
        String sql = "select count(1) from " + tableName;
        if (where != null && where.length() > 0) {
            sql += " where " + where;
        }
        return (int) dbHelper.queryLong(sql);
    }

    public long getMaxId() throws OperatorException {
        String sql = "select max(" + IDao.TABLE_ID + ") from " + tableName;
        return dbHelper.queryLong(sql);
    }

    public long getMinId() throws OperatorException {
        String sql = "select min(" + IDao.TABLE_ID + ") from " + tableName;
        return dbHelper.queryLong(sql);
    }

    public void delete(T data) throws OperatorException {
        dbHelper.delete(this, data);
    }

    public void delete(long Id) throws OperatorException {
        dbHelper.delete(this, Id);
    }

    public void clearAll() throws OperatorException {
        dbHelper.clear(this);
    }

    public T getEq(StringColumn field, String value) throws OperatorException {
        return getByWhere(field.getName() + "='" + value + "'");
    }

    public T getEq(IntegerColumn field, int value) throws OperatorException {
        return getByWhere(field.getName() + "=" + value);
    }

    public T getEq(LongColumn field, long value) throws OperatorException {
        return getByWhere(field.getName() + "=" + value);
    }

    public T getNe(StringColumn field, String value) throws OperatorException {
        return getByWhere(field.getName() + "<>'" + value + "'");
    }

    public T getNe(IntegerColumn field, int value) throws OperatorException {
        return getByWhere(field.getName() + "<>" + value);
    }

    public T getNe(LongColumn field, long value) throws OperatorException {
        return getByWhere(field.getName() + "<>" + value);
    }

    public T getLike(StringColumn field, String what) throws OperatorException {
        return getByWhere(field.getName() + " like '" + what + "'");
    }

    public T getGe(IntegerColumn field, int value) throws OperatorException {
        return getByWhere(field.getName() + ">=" + value);
    }

    public T getGt(IntegerColumn field, int value) throws OperatorException {
        return getByWhere(field.getName() + ">" + value);
    }

    public T getGe(LongColumn field, long value) throws OperatorException {
        return getByWhere(field.getName() + ">=" + value);
    }

    public T getGt(LongColumn field, long value) throws OperatorException {
        return getByWhere(field.getName() + ">" + value);
    }

    public StringBuilder buildWhere(String where) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("select ");
        sb.append(getFullColumnsString());
        sb.append(" from ");
        sb.append(tableName);
        if (where != null) {
            sb.append(" where ");
            sb.append(where);
        }
        return sb;
    }
}
