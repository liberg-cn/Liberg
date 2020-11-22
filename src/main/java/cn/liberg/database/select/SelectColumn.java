package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 列查询
 *
 * @param <T> 泛型参数，代表查询列的数据类型
 *
 * @author Liberg
 */
public class SelectColumn<T> extends Select<T> {
    private Column<T> column;

    public SelectColumn(BaseDao dao, Column<T> column) {
        super(dao);
        this.column = column;
    }

    @Override
    protected void appendColumns(StringBuilder sb) {
        sb.append(column.name);
    }

    @Override
    public T readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            return column.getValue(resultSet, 1);
        } else {
            return null;
        }
    }

    @Override
    public List<T> readAll(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(column.getValue(resultSet, 1));
        }
        return list;
    }

}
