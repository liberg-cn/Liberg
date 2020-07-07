package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class PreparedColumnUpdateBuilder extends PreparedCriteria<PreparedColumnUpdateBuilder> {
    protected BaseDao dao;
    private volatile String sql = null;
    private Column column;

    public BaseDao getDao() {
        return dao;
    }

    public PreparedColumnUpdateBuilder(BaseDao dao, Column column) {
        this.dao = dao;
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }

    public String buildColumns() {
        return column.getName()+"=?";
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
