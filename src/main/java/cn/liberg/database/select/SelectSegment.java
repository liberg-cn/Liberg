package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.Segment;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询数据表的一部分列
 *
 * <T> 泛型，用于标记实体类型
 * @author Liberg
 */
public class SelectSegment<T> extends Select<Segment<T>> {
    private Column[] columns;

    public SelectSegment(BaseDao dao, Column... columns) {
        super(dao);
        this.columns = columns;
    }

    @Override
    protected void appendColumnsTo(StringBuilder sql) {
        for (Column column : columns) {
            sql.append(column.name);
            sql.append(',');
        }
        sql.deleteCharAt(sql.length()-1);
    }

    @Override
    public Segment<T> readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            Segment<T> segment = new Segment(dao);
            int index = 1;
            for (Column column : columns) {
                segment.put(column.shortName, column.getValue(resultSet, index++));
            }
            return segment;
        } else {
            return null;
        }
    }

    @Override
    public List<Segment<T>> readAll(ResultSet resultSet) throws SQLException {
        List<Segment<T>> list = new ArrayList<>();
        while (resultSet.next()) {
            Segment<T> segment = new Segment(dao);
            int index = 1;
            for (Column column : columns) {
                segment.put(column.shortName, column.getValue(resultSet, index++));
            }
            list.add(segment);
        }
        return list;
    }
}
