package cn.liberg.database.select;

import cn.liberg.core.Field;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 列查询
 *
 * @param <F> 泛型参数，代表查询列的数据类型
 *
 * @author Liberg
 */
public class SelectColumn<F> extends Select<F> {
    private Field<F> column;

    public SelectColumn(BaseDao dao, Field<F> column) {
        super(dao);
        this.column = column;
    }

    @Override
    protected void appendColumnsTo(StringBuilder sql) {
        sql.append(column.name);
    }

    @Override
    public F readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            return column.getValue(resultSet, 1);
        } else {
            return null;
        }
    }

    @Override
    public List<F> readAll(ResultSet resultSet) throws SQLException {
        List<F> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(column.getValue(resultSet, 1));
        }
        return list;
    }

}
