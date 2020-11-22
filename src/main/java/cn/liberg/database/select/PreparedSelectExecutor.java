package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Prepare方式select的实际执行类。
 *
 * @param <T>
 *
 * @author Liberg
 */
public class PreparedSelectExecutor<T> implements AutoCloseable {
    private final PreparedSelect<T> select;
    private PreparedStatement preparedStatement = null;
    private Map<Column, Integer> indexMap = null;
    private boolean limited = false;
    private long defaultLimitStart;
    private int defaultLimitCount;

    public PreparedSelectExecutor(PreparedSelect<T> select, PreparedSelectWhere selectWhere) throws OperatorException {
        this.select = select;
        preparedStatement = DBHelper.self().prepareStatement(selectWhere.buildSql());
        indexMap = selectWhere.indexMap;
        defaultLimitStart = selectWhere.limitStart;
        defaultLimitCount = selectWhere.limitCount;
    }

    @Override
    public void close() {
        // 前面都执行成功了，可以认为跟MySQL服务端通信是没问题的
        // 因此，close第二个参数传入false
        DBHelper.self().close(preparedStatement, false);
        preparedStatement = null;
    }

    public void close(boolean isTxError) {
        DBHelper.self().close(preparedStatement, isTxError);
        preparedStatement = null;
    }

    public void setParameter(Column<String> column, String value) throws OperatorException {
        Integer index = indexMap.get(column);
        if(index == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:"+column.name+" is not prepared.");
        }
        try {
            preparedStatement.setString(index, value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }
    public void setParameter(Column<Byte> column, byte value) throws OperatorException {
        Integer index = indexMap.get(column);
        if(index == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:"+column.name+" is not prepared.");
        }
        try {
            preparedStatement.setByte(index, value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }
    public void setParameter(Column<Integer> column, int value) throws OperatorException {
        Integer index = indexMap.get(column);
        if(index == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:"+column.name+" is not prepared.");
        }
        try {
            preparedStatement.setInt(index, value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }
    public void setParameter(Column<Long> column, long value) throws OperatorException {
        Integer index = indexMap.get(column);
        if(index == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:"+column.name+" is not prepared.");
        }
        try {
            preparedStatement.setLong(index, value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    private void setLimit(long limitStart, int limitCount) throws OperatorException {
        try {
            int index = indexMap.size();
            preparedStatement.setLong(++index, limitStart);
            preparedStatement.setInt(++index, limitCount);
            limited = true;
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }
    private void setLimit(int limitCount) throws OperatorException {
        setLimit(defaultLimitStart, limitCount);
    }


    public T one() throws OperatorException {
        setLimit(1);
        try {
            ResultSet rs = preparedStatement.executeQuery();
            return select.readOne(rs);
        } catch (SQLException e) {
            close(DBHelper.isTxError(e));
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public List<T> all() throws OperatorException {
        if(!limited) {
            setLimit(defaultLimitStart, defaultLimitCount);
        }
        try {
            ResultSet rs = preparedStatement.executeQuery();
            return select.readAll(rs);
        } catch (SQLException e) {
            close(DBHelper.isTxError(e));
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public List<T> page(int pageNum, int pageSize) throws OperatorException {
        setLimit((pageNum-1)*pageSize, pageSize);
        return all();
    }

}
