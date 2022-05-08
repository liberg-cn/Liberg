package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.Field;
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
 * @author Liberg
 */
public class PreparedSelectExecutor<T> implements AutoCloseable {
    private final PreparedSelect<T> select;
    private PreparedStatement preparedStatement;
    private Map<String, NextIndex> indexMap;
    //除去 limit ?,?部分，占位符的个数
    private final int $length;

    public PreparedSelectExecutor(PreparedSelect<T> select, PreparedSelectWhere selectWhere) throws OperatorException {
        this.select = select;
        preparedStatement = DBHelper.self().prepareStatement(selectWhere.buildSql());
        indexMap = selectWhere.indexMap;
        $length = selectWhere.$length;
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

    public void setParameter(Field<String> column, String value) throws OperatorException {
        NextIndex nIdx = indexMap.get(column.name);
        if (nIdx == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:" + column.name + " is not prepared.");
        }
        try {
            preparedStatement.setString(nIdx.next(), value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public void setParameter(Field<Byte> column, byte value) throws OperatorException {
        NextIndex nIdx = indexMap.get(column.name);
        if (nIdx == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:" + column.name + " is not prepared.");
        }
        try {
            preparedStatement.setByte(nIdx.next(), value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public void setParameter(Field<Integer> column, int value) throws OperatorException {
        NextIndex nIdx = indexMap.get(column.name);
        if (nIdx == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:" + column.name + " is not prepared.");
        }
        try {
            preparedStatement.setInt(nIdx.next(), value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public void setParameter(Field<Long> column, long value) throws OperatorException {
        NextIndex nIdx = indexMap.get(column.name);
        if (nIdx == null) {
            throw new OperatorException(StatusCode.PARAMS_INVALID, "Column:" + column.name + " is not prepared.");
        }
        try {
            preparedStatement.setLong(nIdx.next(), value);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    private void setLimit(long limitStart, int limitCount) throws OperatorException {
        try {
            preparedStatement.setLong($length + 1, limitStart);
            preparedStatement.setInt($length + 2, limitCount);
        } catch (SQLException e) {
            close();
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    private void setLimit(int limitCount) throws OperatorException {
        setLimit(0, limitCount);
    }


    public T one() throws OperatorException {
        setLimit(0, 1);
        try {
            ResultSet rs = preparedStatement.executeQuery();
            return select.readOne(rs);
        } catch (SQLException e) {
            close(DBHelper.isTxError(e));
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    private List<T> allWithLimit(int limitStart, int limitCount) throws OperatorException {
        setLimit(limitStart, limitCount);
        try {
            ResultSet rs = preparedStatement.executeQuery();
            return select.readAll(rs);
        } catch (SQLException e) {
            close(DBHelper.isTxError(e));
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    public List<T> all(int limitCount) throws OperatorException {
        return allWithLimit(0, limitCount);
    }

    public List<T> page(int pageNum, int pageSize) throws OperatorException {
        return allWithLimit((pageNum - 1) * pageSize, pageSize);
    }

    public List<T> allFill(int limitCount) throws OperatorException {
        List<T> list = allWithLimit(0, limitCount);
        for (T e : list) {
            select.dao.fillData(e);
        }
        return list;
    }

    public List<T> pageFill(int pageNum, int pageSize) throws OperatorException {
        List<T> list = allWithLimit((pageNum - 1) * pageSize, pageSize);
        for (T e : list) {
            select.dao.fillData(e);
        }
        return list;
    }

}
