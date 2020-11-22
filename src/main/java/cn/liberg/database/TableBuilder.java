package cn.liberg.database;


import cn.liberg.core.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * 为了保持{@code XxxDao}更轻量级，实体中的字段除了名称外，其他信息并没有记录到相应的{@link Column}中，
 * 而是在构造建表语句时，由{@code TableBuilder.add(...)}方法中进行传入。
 * 传入的信息包括: 数据列的类型、长度、是否是索引字段、列注释信息。
 *
 * @author Liberg
 *
 * @see Column
 */
public class TableBuilder {
    private String tableName;
    private StringBuilder sb;
    private List<Column> listIndex;

    public TableBuilder(String tableName) {
        this.tableName = tableName;
        sb = new StringBuilder(1024);
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName);
        sb.append("(id BIGINT PRIMARY KEY AUTO_INCREMENT,");
        listIndex = new ArrayList<>();
    }

    public void add(Column column, String type) {
        add(column, false, type, null);
    }

    public void add(Column column, String type, String comment) {
        add(column, false, type, comment);
    }

    public void add(Column column, boolean isIndex, String type) {
        add(column, isIndex, type, null);
    }

    public void add(Column column, boolean isIndex, String type, String comment) {
        sb.append(column.name);
        sb.append(" ");
        sb.append(type);
        if(comment!=null) {
            sb.append(" COMMENT '");
            sb.append(comment);
            sb.append('\'');
        }
        sb.append(',');
        if(isIndex) {
            listIndex.add(column);
        }
    }

    public String build() {
        for(Column column : listIndex) {
            sb.append("KEY `");
            sb.append(tableName);
            sb.append('_');
            sb.append(column.name);
            sb.append("`(");
            sb.append(column.name);
            sb.append("),");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(");");
        return sb.toString();
    }
}
