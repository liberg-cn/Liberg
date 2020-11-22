package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.BaseDao;
import cn.liberg.database.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prepare方式select数据表部分列的入口类。
 *
 * 继承自{@link PreparedSelect}
 *
 * @author Liberg
 * @see PreparedSelect
 */
public class PreparedSelectColumn<T> extends PreparedSelect<T> {
    private Column<T> column;

    public PreparedSelectColumn(BaseDao dao, Column<T> column) {
        super(dao);
        this.column = column;
    }

    @Override
    protected void appendColumns(StringBuilder sb) {
        sb.append(column.name);
    }


    @Override
    protected T readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            return column.getValue(resultSet, 1);
        } else {
            return null;
        }
    }

    @Override
    protected List<T> readAll(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(column.getValue(resultSet, 1));
        }
        return list;
    }
}
