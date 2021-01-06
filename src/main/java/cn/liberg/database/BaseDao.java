package cn.liberg.database;

import cn.liberg.core.*;
import cn.liberg.database.select.*;
import cn.liberg.database.update.Update;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据访问对象(Data Access Object)的抽象父类
 *
 * @author Liberg
 */
public abstract class BaseDao<T> {
    public static final DBHelper dbHelper = DBHelper.self();
    protected final String tableName;

    /**
     * id列
     */
    public final Column<Long> columnId = new IdColumn();

    /**
     * 不含id的列，在子类中进行初始化
     */
    protected List<Column> columns;

    /**
     * 非id的其他字段，按顺序拼接的结果
     */
    public final String COLUMNS_STRING;

    /**
     * 非id的其他列，按照columnName=?顺序拼接的结果
     */
    public final String COLUMNS_UPDATE_STRING;

    /**
     * insert语句，不含id，因为id自动递增
     * eg:
     * insert into tableName(col1,col2,col3) values(?,?,?)
     */
    final String SQL0_SAVE;

    /**
     * 通过id进行查找的sql语句
     * eg:
     * select id,col1,col2,col3 from tableName where id=?
     */
    final String SQL0_GET_BY_ID;

    /**
     * 通过id进行update的sql语句
     * eg:
     * update tableName set col1=?,col2=?,col3=? where id=?
     */
    final String SQL0_UPDATE_BY_ID;

    /**
     * 字段总数
     */
    private int columnCount;

    /**
     * 供子类覆盖的钩子方法
     * 从数据库还原的entity，可能需要进行
     * 进一步字段填充或其他初始化处理。
     *
     * @param entity
     * @throws OperatorException
     */
    public T fillData(T entity) throws OperatorException {
        //可能需要填充一些未映射(保存)到数据库的成员
        return entity;
    }

    protected BaseDao(String tableName) {
        this.tableName = tableName;
        StringBuilder sbColumns = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        columnCount = 1;
        for (Column column : getColumns()) {
            columnCount++;
            sbColumns.append(column.name);
            sbColumns.append(',');

            sb1.append(column.name);
            sb1.append("=?,");

            sb2.append("?,");
        }
        sbColumns.deleteCharAt(sbColumns.length() - 1);
        sb1.deleteCharAt(sb1.length() - 1);
        sb2.deleteCharAt(sb2.length() - 1);

        COLUMNS_STRING = sbColumns.toString();
        COLUMNS_UPDATE_STRING = sb1.toString();
        SQL0_GET_BY_ID = "select id," + COLUMNS_STRING + " from " + tableName
                + " where id=?";
        SQL0_SAVE = "insert into " + tableName + "(" + sbColumns.toString() + ") values("
                + sb2.toString() + ")";
        SQL0_UPDATE_BY_ID = "update " + tableName + " set " + COLUMNS_UPDATE_STRING
                + " where id=?";
    }

    /**
     * notify us when entity updated by any other applications
     */
    public void notifyUpdated(long entityId) throws OperatorException {

    }

    /**
     * notify us when entity saved by any other applications
     */
    public void notifySaved(long entityId) throws OperatorException{

    }

    public String getTableName() {
        return tableName;
    }

    /**
     * 返回非id的其他字段，用,按顺序拼接的结果
     */
    public String getColumnsString() {
        return COLUMNS_STRING;
    }

    public int getColumnCount() {
        return columnCount;
    }

    /**
     * 在子类中实现，将id设置到entity
     */
    public abstract void setEntityId(T entity, long id);
    /**
     * 在子类中实现，从entity中取得id
     */
    public abstract long getEntityId(T entity);

    /**
     * 在子类中实现，根据查询结果ResultSet构建entity
     */
    public abstract T buildEntity(ResultSet rs) throws SQLException;
    /**
     * 在子类中实现，根据entity的成员变量填充PreparedStatement
     */
    protected abstract void fillPreparedStatement(T entity, PreparedStatement ps) throws SQLException;

    /**
     * 在子类实现，返回非id的其他有列
     */
    public abstract List<Column> getColumns();

    public long save(T entity) throws OperatorException {
        return dbHelper.save(entity, this);
    }

    public void update(T entity) throws OperatorException {
        dbHelper.update(entity, this);
    }

    /**
     * 仅仅将entity的部分字段(由第二个参数columns指定)更新到数据库
     * @param entity
     * @param columns
     * @throws OperatorException
     */
    public void update(T entity, Column... columns) throws OperatorException {
        dbHelper.update(entity, this, columns);
    }

    /**
     * 仅仅将entity的部分字段(除columns外的字段)更新到数据库
     * @param entity
     * @param columns
     * @throws OperatorException
     */
    public void updateExclusive(T entity, Column... columns) throws OperatorException {
        Set<Column> set = new HashSet<>();
        for (Column column : columns) {
            set.add(column);
        }
        dbHelper.updateExclusive(entity, this, set);
    }

    /**
     * 批量新增或更新
     * @param list
     * @throws OperatorException
     */
    public void batchSaveOrUpdate(List<T> list) throws OperatorException {
        DBHelper.self().beginTransact();
        try {
            for(T entity : list) {
                if(getEntityId(entity) > 0) {
                    update(entity);
                } else {
                    save(entity);
                }
            }
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 批量新增
     * @param list
     * @throws OperatorException
     */
    public void batchSave(List<T> list) throws OperatorException {
        DBHelper.self().beginTransact();
        try {
            for(T entity : list) {
                save(entity);
            }
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 批量更新
     * @param list
     * @throws OperatorException
     */
    public void batchUpdate(List<T> list) throws OperatorException {
        DBHelper.self().beginTransact();
        try {
            for(T entity : list) {
                update(entity);
            }
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }


    public final T getById(long id) throws OperatorException {
        return dbHelper.getById(id, this);
    }

    public T getOneByWhere(String where) throws OperatorException {
        StringBuilder sb = buildWhere(where);
        sb.append(" limit 1");
        return dbHelper.getBySql(sb.toString(), this);
    }

    public List<T> getPageByWhere(int pageNum, int pageSize, String where) throws OperatorException {
        StringBuilder sb = buildWhere(where);
        sb.append(" limit ");
        sb.append((pageNum - 1) * pageSize);
        sb.append(',');
        sb.append(pageSize);
        return dbHelper.getAllBySql(sb.toString(), this);
    }

    public List<T> getByWhere(String where) throws OperatorException {
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
        String sql = "select count(*) from " + tableName;
        return (int) dbHelper.queryLong(sql);
    }

    public int getCount(String where) throws OperatorException {
        String sql = "select count(*) from " + tableName;
        if (where != null && where.length() > 0) {
            sql += " where " + where;
        }
        return (int) dbHelper.queryLong(sql);
    }

    public long getMaxId() throws OperatorException {
        String sql = "select max(id) from " + tableName;
        return dbHelper.queryLong(sql);
    }

    public long getMinId() throws OperatorException {
        String sql = "select min(id) from " + tableName;
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

    /**
     * 实体对象异步save到数据库
     */
    public void asyncSave(Object obj) throws OperatorException {
        dbHelper.asyncSqlExecutor.save(this, obj);
    }

    /**
     * 实体对象异步update到数据库
     */
    public void asyncUpdate(Object obj) {
        dbHelper.asyncSqlExecutor.update(this, obj);
    }

    /**
     * 异步执行sql
     */
    public void asyncExecuteSql(String sql) {
        dbHelper.asyncSqlExecutor.executeSql(sql);
    }

    /**
     * 查询 field=value的一条记录
     */
    public T getOneEq(Column<String> field, String value) throws OperatorException {
        return getOneByWhere(field.name + "=" + SqlDefender.format(value));
    }
    /**
     * 查询 field=value的所有记录
     */
    public List<T> getEq(Column<String> field, String value) throws OperatorException {
        return getByWhere(field.name + "=" + SqlDefender.format(value));
    }

    /**
     * 查询 field=value的一条记录
     */
    public T getOneEq(Column<? extends Number> field, Number value) throws OperatorException {
        return getOneByWhere(field.name + Condition.EQ + value);
    }
    /**
     * 查询 field=value的所有记录
     */
    public List<T> getEq(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.EQ + value);
    }

    /**
     * 查询 field like 'value'的记录
     */
    public List<T> getLike(Column<String> field, String what) throws OperatorException {
        return getByWhere(field.name + Condition.LIKE + SqlDefender.format(what));
    }

    /**
     * 查询 field<>value的记录
     */
    public List<T> getNe(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.NE + value);
    }

    /**
     * 查询 field>value的记录
     */
    public List<T> getGt(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.GT + value);
    }

    /**
     * 查询 field>=value的记录
     */
    public List<T> getGe(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.GE + value);
    }

    /**
     * 查询 field<value的记录
     */
    public List<T> getLt(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.LT + value);
    }

    /**
     * 查询 field<=value的记录
     */
    public List<T> getLe(Column<? extends Number> field, Number value) throws OperatorException {
        return getByWhere(field.name + Condition.LE + value);
    }

    public StringBuilder buildWhere(String where) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("select id,");
        sb.append(COLUMNS_STRING);
        sb.append(" from ");
        sb.append(tableName);
        if (where != null) {
            sb.append(" where ");
            sb.append(where);
        }
        return sb;
    }

    public Select<T> select() {
        return new Select(this);
    }
    public <CT> Select<CT> select(Column<CT> column) {
        return new SelectColumn<>(this, column);
    }
    public SelectSegment<T> select(Column... columns) {
        return new SelectSegment(this, columns);
    }
    public Update<T> update() {
        return new Update(this);
    }

    public PreparedSelect<T> prepareSelect() {
        return new PreparedSelect(this);
    }
    public <CT> PreparedSelect<CT> prepareSelect(Column<CT> column) {
        return new PreparedSelectColumn(this, column);
    }
    public PreparedSelectSegment<T> prepareSelect(Column... columns) {
        return new PreparedSelectSegment(this, columns);
    }
}
