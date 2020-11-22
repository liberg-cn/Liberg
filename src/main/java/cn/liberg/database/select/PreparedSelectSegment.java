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
 * @author Liberg
 */
public class PreparedSelectSegment extends PreparedSelect<Segment> {
    private Column[] columns;

    public PreparedSelectSegment(BaseDao dao, Column... columns) {
        super(dao);
        this.columns = columns;
    }

    @Override
    protected void appendColumns(StringBuilder sb) {
        for (Column column : columns) {
            sb.append(column.name);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1);
    }

    @Override
    public Segment readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            Segment segment = new Segment();
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
    public List<Segment> readAll(ResultSet resultSet) throws SQLException {
        List<Segment> list = new ArrayList<>();
        while (resultSet.next()) {
            Segment segment = new Segment();
            int index = 1;
            for (Column column : columns) {
                segment.put(column.shortName, column.getValue(resultSet, index++));
            }
            list.add(segment);
        }
        return list;
    }
}
