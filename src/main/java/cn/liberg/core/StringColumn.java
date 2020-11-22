package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体String字段-->
 *                  数据库VARCHAR列，长度小于4096时
 *                  数据库TEXT列，长度达到/超过4096时
 *
 * @author Liberg
 */
public class StringColumn extends Column {

    public StringColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public ColumnType type() {
        return ColumnType.String;
    }

    @Override
    public String getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }
}
