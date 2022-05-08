package cn.liberg.database.select;

import cn.liberg.core.Field;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepare方式select数据表部分列的入口类。
 *
 * 继承自{@link PreparedSelect}
 *
 * @author Liberg
 * @see PreparedSelect
 */
public class PreparedSelectColumn<F> extends PreparedSelect<F> {
    private Field<F> field;

    public PreparedSelectColumn(BaseDao dao, Field<F> column) {
        super(dao);
        this.field = column;
    }

    @Override
    protected void appendColumns(StringBuilder sb) {
        sb.append(field.name);
    }


    @Override
    protected F readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            return field.getValue(resultSet, 1);
        } else {
            return null;
        }
    }

    @Override
    protected List<F> readAll(ResultSet resultSet) throws SQLException {
        List<F> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(field.getValue(resultSet, 1));
        }
        return list;
    }
}
