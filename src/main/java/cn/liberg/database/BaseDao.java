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
     * 不含id的列，在子类构造函数中进行初始化
     */
    protected List<Column> columns;
    /**
     * 所有实现{@link ICacheEntity}的CachedColumn和CachedColumnPair、CachedColumnTrio
     * 如果所有列都不是Cached，子类将此成员设为null
     */
    protected List<ICacheEntity> cacheEntityList;

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

    protected void afterConstructed() {

    }

    protected void beforePutToCache(T entity) {

    }

    /**
     * 在子类XxxDaoImpl中实现，返回Id列
     */
    public abstract Column<T, Long> getIdColumn();
    /**
     * 在子类XxxDaoImpl中实现，按顺序返回不含Id在内的其他列
     */
    public abstract List<Column> initColumns();

    /**
     * 供子类覆盖的钩子方法
     * 直接从数据库读取的<em>entity列表</em>，可能需要进行额外的数据填充
     * 或着是需要对数据字段进行脱敏处理等等。
     *
     * @see BaseDao#getPageFill
     * @see SelectWhere#pageFill(int,int)
     * @see SelectWhere#allFill(int)
     * @see PreparedSelectExecutor#pageFill(int,int)
     * @see PreparedSelectExecutor#allFill(int)
     *
     * @param entity
     * @throws OperatorException
     */
    public T fillData(T entity) throws OperatorException {
        // 可能需要填充一些未映射(保存)到数据库的成员
        return entity;
    }

    /**
     * notify us when entity updated by any other applications
     * 默认实现更新缓存
     */
    public void notifyUpdated(long entityId) throws OperatorException {
        if(isEntityCached()) {
            getById(entityId);
        }
    }

    /**
     * notify us when entity saved by any other applications
     * 默认实现更新缓存
     */
    public void notifySaved(long entityId) throws OperatorException{
        if(isEntityCached()) {
            getById(entityId);
        }
    }

    /**
     * Dao中是否用到了缓存列
     */
    public boolean isEntityCached() {
        return cacheEntityList != null;
    }

    public void putToCache(T entity) {
        if (cacheEntityList != null) {
            beforePutToCache(entity);
            for (ICacheEntity ce : cacheEntityList) {
                ce.put(entity);
            }
        }
    }

    public void removeFromCache(T entity) {
        if (cacheEntityList != null) {
            for (ICacheEntity ce : cacheEntityList) {
                ce.remove(entity);
            }
        }
    }

    public void clearCache() {
        if (cacheEntityList != null) {
            for (ICacheEntity ce : cacheEntityList) {
                ce.clear();
            }
        }
    }

    protected BaseDao(String tableName, List<ICacheEntity> cacheEntityList) {
        this.tableName = tableName;
        this.cacheEntityList = cacheEntityList;
        StringBuilder sbColumns = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        columnCount = 1;
        columns = initColumns();
        for (Column column : columns) {
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
        afterConstructed();
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * 返回非Id字段的其他字段列表
     */
    public final List<Column> getColumns() {
        return columns;
    }

    /**
     * 返回非id的其他字段，用,按顺序拼接的结果
     */
    public final String getColumnsString() {
        return COLUMNS_STRING;
    }

    public final int getColumnCount() {
        return columnCount;
    }

    /**
     * 返回对应实体类的class对象
     */
    public abstract Class<T> getEntityClazz();
    /**
     * 在子类中实现，根据查询结果ResultSet构建entity
     */
    public abstract T buildEntity(ResultSet rs) throws SQLException;
    /**
     * 在子类中实现，根据entity的成员变量填充PreparedStatement
     */
    protected abstract void fillPreparedStatement(T entity, PreparedStatement ps) throws SQLException;

    public long save(T entity) throws OperatorException {
        long id = dbHelper.save(entity, this);
        if(id > 0) {
            putToCache(entity);
        }
        return id;
    }

    public void update(T entity) throws OperatorException {
        dbHelper.update(entity, this);
    }

    public void incrementById(Field<? extends Number> field, int diff, long id) throws OperatorException {
        dbHelper.incrementById(this, field, diff, id);
        getById(id);
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
     * @param excludedColumns
     * @throws OperatorException
     */
    public void updateExclusive(T entity, Column... excludedColumns) throws OperatorException {
        Set<Column> set = new HashSet<>();
        for (Column column : excludedColumns) {
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
        final Column<T, Long> columnId = getIdColumn();
        dbHelper.beginTransact();
        try {
            for(T entity : list) {
                if(columnId.get(entity) > 0) {
                    update(entity);
                } else {
                    save(entity);
                }
            }
            dbHelper.endTransact();
        } catch (Exception e) {
            dbHelper.transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 批量新增
     * @param list
     * @throws OperatorException
     */
    public void batchSave(List<T> list) throws OperatorException {
        dbHelper.beginTransact();
        try {
            for(T entity : list) {
                save(entity);
            }
            dbHelper.endTransact();
        } catch (Exception e) {
            dbHelper.transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 批量更新
     * @param list
     * @throws OperatorException
     */
    public void batchUpdate(List<T> list) throws OperatorException {
        dbHelper.beginTransact();
        try {
            for(T entity : list) {
                update(entity);
            }
            dbHelper.endTransact();
        } catch (Exception e) {
            dbHelper.transactRollback();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    /**
     * 通过主键（id）进行查询
     * 没有符合条件的记录，返回null
     */
    public T getById(long id) throws OperatorException {
        T entity = null;
        Column<T, Long> columnId = getIdColumn();
        if(columnId instanceof CachedColumn) {
            entity = ((CachedColumn<T, Long>) columnId).getFromCache(id);
        }
        if(entity == null) {
            entity = dbHelper.getById(id, this);
            if(entity != null) {
                putToCache(entity);
            }
        }
        return entity;
    }

    /**
     * 通过where条件查询单条记录
     * 没有符合条件的记录，返回null
     */
    public T getOne(String where) throws OperatorException {
        T entity = dbHelper.getBySql(buildSQL(where, 0, 1), this);
        if (entity != null) {
            putToCache(entity);
        }
        return entity;
    }

    /**
     * 通过where条件查询所有记录
     * 没有符合条件的记录，返回null
     */
    public List<T> getAll(String where, int limitCount) throws OperatorException {
        return dbHelper.getAllBySql(buildSQL(where, 0, limitCount), this);
    }


    public List<T> getAll(int limitCount) throws OperatorException {
        return dbHelper.getAllBySql(buildSQL(null, 0, limitCount), this);
    }

    /**
     * 通过where条件查询指定页数范围内的记录
     * 没有符合条件的记录，长度为0的空列表
     */
    public List<T> getPage(String where, int pageNum, int pageSize) throws OperatorException {
        String sql = buildSQL(where, (pageNum - 1) * pageSize, pageSize);
        return dbHelper.getAllBySql(sql, this);
    }

    /**
     * 通过where条件查询指定页数范围内的记录
     * 没有符合条件的记录，长度为0的空列表
     * 返回的每一条记录，会调用{@code fillData()}进行数据填充
     */
    public List<T> getPageFill(String where, int pageNum, int pageSize) throws OperatorException {
        String sql = buildSQL(where, (pageNum - 1) * pageSize, pageSize);
        List<T> list = dbHelper.getAllBySql(sql, this);
        for(T entity : list) {
            fillData(entity);
        }
        return list;
    }

    public T getOneBySql(String sql) throws OperatorException {
        T entity = dbHelper.getBySql(sql, this);
        if(entity != null) {
            putToCache(entity);
        }
        return entity;
    }

    public List<T> getAllBySql(String sql) throws OperatorException {
        return dbHelper.getAllBySql(sql, this);
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
        removeFromCache(data);
        dbHelper.delete(this, data);
    }

    public void delete(long Id) throws OperatorException {
        T data = getById(Id);
        if(data != null) {
            delete(data);
        }
    }

    /**
     * 实体对象异步save到数据库
     */
    public void asyncSave(T entity) throws OperatorException {
        dbHelper.asyncSqlExecutor.save(this, entity);
    }

    /**
     * 实体对象异步update到数据库
     */
    public void asyncUpdate(T entity) {
        dbHelper.asyncSqlExecutor.update(this, entity);
    }

    /**
     * 异步执行sql
     */
    public void asyncExecuteSql(String sql) {
        dbHelper.asyncSqlExecutor.executeSql(sql);
    }

    /**
     * 查询 field=value的单条记录
     */
    public T getEq(Field<String> field, String value) throws OperatorException {
        return getOne(Condition.eq(field.name, value));
    }
    /**
     * 查询 field=value的单条记录
     * 并将查询到的记录缓存到field字段的LRU缓存中
     */
    public T getEq(CachedStringColumn<T> column, String value) throws OperatorException {
        T entity = column.getFromCache(value);
        if(entity == null) {
            entity = getOne(Condition.eq(column.name, value));
        }
        return entity;
    }

    public T getEq(CachedLongColumn<T> column, Long value) throws OperatorException {
        T entity = column.getFromCache(value);
        if(entity == null) {
            entity = getOne(Condition.eq(column.name, value));
        }
        return entity;
    }

    public <F1,F2> T getEq(CachedColumnPair<T,F1,F2> column, F1 value1, F2 value2) throws OperatorException {
        T entity = column.getFromCache(value1, value2);
        if(entity == null) {
            entity = getOne(column.build(value1, value2));
        }
        return entity;
    }

    public <F1,F2,F3> T getEq(CachedColumnTrio<T,F1,F2,F3> column, F1 value1, F2 value2, F3 value3) throws OperatorException {
        T entity = column.getFromCache(value1, value2, value3);
        if(entity == null) {
            entity = getOne(column.build(value1, value2, value3));
        }
        return entity;
    }

    /**
     * 查询 field=value的单条记录
     */
    public T getEq(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.eq(field.name, value));
    }

    public T getEq(Field<? extends Number> column1, Number value1, Field<? extends Number> column2, Number value2) throws OperatorException {
        return getOne(Condition.eq(column1.name, value1, column2.name, value2));
    }
    public T getEq(Field<String> column1, String value1, Field<? extends Number> column2, Number value2) throws OperatorException {
        return getOne(Condition.eq(column1.name, value1, column2.name, value2));
    }
    public T getEq(Field<? extends Number> column1, Number value1, Field<String> column2, String value2) throws OperatorException {
        return getOne(Condition.eq(column1.name, value1, column2.name, value2));
    }
    public T getEq(Field<String> column1, String value1, Field<String> column2, String value2) throws OperatorException {
        return getOne(Condition.eq(column1.name, value1, column2.name, value2));
    }

    /**
     * 查询 field<>value的单条记录
     */
    public T getNe(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.ne(field.name, value));
    }
    public T getNe(Field<String> field, String value) throws OperatorException {
        return getOne(Condition.ne(field.name, value));
    }
    /**
     * 查询 field>value的单条记录
     */
    public T getGt(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.gt(field.name, value));
    }

    /**
     * 查询 field>=value的记录
     */
    public T getGe(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.ge(field.name, value));
    }

    /**
     * 查询 field<value的记录
     */
    public T getLt(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.lt(field.name, value));
    }

    /**
     * 查询 field<=value的记录
     */
    public T getLe(Field<? extends Number> field, Number value) throws OperatorException {
        return getOne(Condition.le(field.name, value));
    }

    /**
     * 查询 field like 'value'的单条记录
     */
    public T getLike(Field<String> field, String what) throws OperatorException {
        return getOne(Condition.like(field.name, what));
    }

    /**
     * 查询 field=value的所有记录
     */
    public List<T> getEqs(Field<String> field, String value, int limitCount) throws OperatorException {
        return getAll(Condition.eq(field.name, value), limitCount);
    }
    /**
     * 查询 field=value的所有记录
     */
    public List<T> getEqs(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.eq(field.name, value), limitCount);
    }
    /**
     * 查询 field<>value的记录
     */
    public List<T> getNes(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.ne(field.name, value), limitCount);
    }

    /**
     * 查询 field>value的记录
     */
    public List<T> getGts(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.gt(field.name, value), limitCount);
    }

    /**
     * 查询 field>=value的记录
     */
    public List<T> getGes(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.ge(field.name, value), limitCount);
    }

    /**
     * 查询 field<value的记录
     */
    public List<T> getLts(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.lt(field.name, value), limitCount);
    }

    /**
     * 查询 field<=value的记录
     */
    public List<T> getLes(Field<? extends Number> field, Number value, int limitCount) throws OperatorException {
        return getAll(Condition.le(field.name, value), limitCount);
    }
    /**
     * 查询 begin<=field<=end的记录
     */
    public List<T> getRange(Field<? extends Number> field, Number begin, Number end, int limitCount) throws OperatorException {
        return getAll(Condition.range(field.name, begin, end), limitCount);
    }
    /**
     * 查询 field like 'value'的记录
     */
    public List<T> getLikes(Field<String> field, String what, int limitCount) throws OperatorException {
        return getAll(Condition.like(field.name, what), limitCount);
    }

    /**
     * 有返回结果的事务
     * @param callback
     * @param <R>
     * @return
     * @throws OperatorException
     */
    public <R> R transaction(TransactionCallWithResult<R> callback) throws OperatorException {
        return dbHelper.transaction(callback);
    }

    /**
     * 无返回结果的事务
     * @param callback
     * @throws OperatorException
     */
    public void transaction(TransactionCall callback) throws OperatorException {
        dbHelper.transaction(callback);
    }

    public String buildSQL(String where, int limitStart, int limitCount) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("select id,");
        sql.append(COLUMNS_STRING);
        sql.append(" from ");
        sql.append(tableName);
        if (where != null) {
            sql.append(" where ");
            sql.append(where);
        }
        sql.append(" limit ");
        if(limitStart > 0) {
            sql.append(limitStart);
            sql.append(',');
        }
        sql.append(limitCount);
        return sql.toString();
    }

    public Select<T> select() {
        return new Select(this);
    }
    public <CT> Select<CT> select(Field<CT> column) {
        return new SelectColumn<>(this, column);
    }
    public SelectSegment<T> select(Column... columns) {
        return new SelectSegment(this, columns);
    }

    /**
     * 如果dao中没有缓存数据，可以放心使用
     *
     * 如果dao中有缓存数据，更新缓存是很重的操作，甚至可能导致缓存失效，
     *     见{@link  cn.liberg.database.update.UpdateWhere#execute()}
     *     建议使用{@link cn.liberg.database.BaseDao#update(T) }方法
     */
    public Update<T> update() {
        return new Update(this);
    }

    public PreparedSelect<T> prepareSelect() {
        return new PreparedSelect(this);
    }
    public <CT> PreparedSelect<CT> prepareSelect(Field<CT> column) {
        return new PreparedSelectColumn(this, column);
    }
    public PreparedSelectSegment<T> prepareSelect(Column... columns) {
        return new PreparedSelectSegment(this, columns);
    }
}
