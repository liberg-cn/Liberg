package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.BaseDao;
import cn.liberg.database.DBHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * select 操作的执行类。
 *
 * @param <T>
 *
 * @author Liberg
 */
public class SelectWhere<T> extends Where<SelectWhere<T>> {

    private final Select<T> select;
    private Column orderBy = null;
    private boolean isAsc = true;
    private int limitStart = 0;
    private int limitCount = 1000;

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

    public SelectWhere<T> limit(int count) {
        limitCount = count;
        return this;
    }

    public SelectWhere<T> limit(int start, int count) {
        limitStart = start;
        limitCount = count;
        return this;
    }

    public T one() throws OperatorException {
        DBHelper dbHelper = BaseDao.dbHelper;
        Statement statement = dbHelper.createStatement();
        boolean isTxError = false;
        try {
            limit(1);
            ResultSet rs = statement.executeQuery(buildSql());
            return select.readOne(rs);
        } catch (SQLException e) {
            isTxError = DBHelper.isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            dbHelper.close(statement, isTxError);
        }
    }

    public List<T> all() throws OperatorException {
        DBHelper dbHelper = BaseDao.dbHelper;
        Statement statement = dbHelper.createStatement();
        boolean isTxError = false;
        try {
            ResultSet rs = statement.executeQuery(buildSql());
            return select.readAll(rs);
        } catch (SQLException e) {
            isTxError = DBHelper.isTxError(e);
            throw new OperatorException(StatusCode.ERROR_DB, e);
        } finally {
            dbHelper.close(statement, isTxError);
        }
    }

    public List<T> page(int pageNum, int pageSize) throws OperatorException {
        int start = (pageNum-1)*pageSize;
        limit(start, pageSize);
        return all();
    }

    public int count() throws OperatorException {
        return select.dao.getCount(buildCondition().toString());
    }

    private String buildSql() {
        StringBuilder sb = select.build();
        sb.append(" where ");
        sb.append(buildWhere());
        return sb.toString();
    }

    protected String buildWhere() {
        StringBuilder sb = buildCondition();
        if (orderBy != null) {
            sb.append(" order by ");
            sb.append(orderBy.name);
            if (!isAsc) {
                sb.append(" desc ");
            }
        }
        sb.append(" limit ");
        sb.append(limitStart);
        sb.append(',');
        sb.append(limitCount);
        sb.append(';');
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + buildSql() + "}";
    }
}
