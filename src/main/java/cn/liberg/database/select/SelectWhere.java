package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.BaseDao;
import cn.liberg.database.DBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 数据表查询操作的执行类。
 *
 * @param <T>
 *
 * @author Liberg
 */
public class SelectWhere<T> extends Where<SelectWhere<T>> {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private final Select<T> select;
    private Column orderBy = null;
    private boolean isAsc = true;

    public SelectWhere(Select<T> select) {
        this.select = select;
    }

    /**
     * 按指定column升序
     */
    public SelectWhere<T> asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    /**
     * 按指定column降序
     */
    public SelectWhere<T> desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    public T one() throws OperatorException {
        DBHelper dbHelper = BaseDao.dbHelper;
        Statement statement = dbHelper.createStatement();
        boolean isTxError = false;
        try {
            final StringBuilder sql = buildSql();
            sql.append(" limit 1;");
            ResultSet rs = statement.executeQuery(sql.toString());
            return select.readOne(rs);
        } catch (SQLException e) {
            isTxError = DBHelper.isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            dbHelper.close(statement, isTxError);
        }
    }

    public List<T> all(int limitCount) throws OperatorException {
        final StringBuilder sb = buildSql();
        sb.append(" limit ");
        sb.append(limitCount);
        return executeQuery(sb.toString());
    }

    public List<T> allFill(int limitCount) throws OperatorException {
        List<T> list = all(limitCount);
        for(T e : list) {
            select.dao.fillData(e);
        }
        return list;
    }

    public List<T> page(int pageNum, int pageSize) throws OperatorException {
        final StringBuilder sql = buildSql();
        sql.append(" limit ");
        sql.append((pageNum - 1) * pageSize);
        sql.append(',');
        sql.append(pageSize);
        return executeQuery(sql.toString());
    }

    public List<T> pageFill(int pageNum, int pageSize) throws OperatorException {
        List<T> list = page(pageNum, pageSize);
        for (T e : list) {
            select.dao.fillData(e);
        }
        return list;
    }

    public int count() throws OperatorException {
        final StringBuilder where = new StringBuilder();
        appendConditionTo(where);
        return select.dao.getCount(where.toString());
    }

    private List<T> executeQuery(String sql) throws OperatorException {
        if(logger.isDebugEnabled()) {
            logger.debug(sql);
        }
        System.out.println("query: " + sql);
        DBHelper dbHelper = BaseDao.dbHelper;
        Statement statement = dbHelper.createStatement();
        boolean isTxError = false;
        try {
            ResultSet rs = statement.executeQuery(sql);
            return select.readAll(rs);
        } catch (SQLException e) {
            isTxError = DBHelper.isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            dbHelper.close(statement, isTxError);
        }
    }

    private StringBuilder buildSql() {
        StringBuilder sql = select.build();
        sql.append(" where ");
        appendConditionTo(sql);
        if (orderBy != null) {
            sql.append(" order by ");
            sql.append(orderBy.name);
            if (!isAsc) {
                sql.append(" desc ");
            }
        }
        return sql;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + buildSql() + "}";
    }
}
