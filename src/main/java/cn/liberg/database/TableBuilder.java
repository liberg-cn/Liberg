package cn.liberg.database;


import java.util.ArrayList;
import java.util.List;

public class TableBuilder {
    private String tableName;
    private StringBuilder sb;
    private List<String> listIndex;

    public TableBuilder(String tableName) {
        this.tableName = tableName;
        sb = new StringBuilder(1024);
        sb.append("create table  IF NOT exists `"+tableName+"` (`");
        sb.append(IDao.TABLE_ID);
        sb.append("` BIGINT primary key AUTO_INCREMENT,");
        listIndex = new ArrayList<>();
    }

    public void add(String column, String type, String comment) {
        add(column, false, type, comment);
    }

    public void add(String column, boolean isIndex, String type, String comment) {
        sb.append("`" + column + "` ");
        sb.append(type);
        if(comment!=null) {
            sb.append(" COMMENT '");
            sb.append(comment);
            sb.append("'");
        }
        sb.append(",");
        if(isIndex) {
            listIndex.add(column);
        }
    }

    public String build() {
        for(String column : listIndex) {
            sb.append("KEY `"+tableName+column+"` (`"+column+"`),");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(");");
        return sb.toString();
    }
}
