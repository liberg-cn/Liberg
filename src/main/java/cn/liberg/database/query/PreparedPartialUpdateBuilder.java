package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class PreparedPartialUpdateBuilder extends PreparedCriteria<PreparedPartialUpdateBuilder> {
    protected BaseDao dao;
    private volatile String sql = null;
    private Column[] columns;

    public BaseDao getDao() {
        return dao;
    }

    public PreparedPartialUpdateBuilder(BaseDao dao, Column[] columns) {
        this.dao = dao;
        this.columns = columns;
    }

    public Column[] getColumns() {
        return columns;
    }

    public String buildColumns() {
        final StringBuilder sb = new StringBuilder();
        if(columns.length>0) {
            for(Column column : columns) {
                sb.append(column.getName());
                sb.append("=?,");
            }
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public String build() {
        if(sql == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("update ");
            sb.append(dao.getTableName());
            sb.append(" set ");
            sb.append(buildColumns());
            if (elems.size() > 0) {
                sb.append(" where ");
                sb.append(super.build());
            }
            sql = sb.toString();
        }
        return sql;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+ "{" + build() + "}";
    }
}
