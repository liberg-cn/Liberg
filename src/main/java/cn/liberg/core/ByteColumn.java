package cn.liberg.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体byte字段-->数据库TINYINT列
 *
 * @author Liberg
 */
public abstract class ByteColumn<E> extends Column<E, Byte> {

    public ByteColumn(String entityFieldName, String shortName) {
        super(entityFieldName, shortName);
    }

    @Override
    public Byte getValue(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getByte(columnIndex);
    }
}
