package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class PreparedColumnQueryBuilder extends PreparedCriteria<PreparedColumnQueryBuilder> {
    protected BaseDao dao;
    private Column orderBy = null;
    private boolean isAsc = true;
    private int limitLow = 0;
    private int limitHigh = 100;
    private volatile String sql = null;
    private Column column;

    public BaseDao getDao() {
        return dao;
    }

    public PreparedColumnQueryBuilder(BaseDao dao, Column column) {
        this.dao = dao;
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }

    public String buildColumns() {
        return column.getName();
    }

    @Override
    public String build() {
        if(sql == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append(buildColumns());
            sb.append(" from ");
            sb.append(dao.getTableName());
            if (elems.size() > 0) {
                sb.append(" where ");
                sb.append(super.build());
            }
            if (orderBy != null) {
                sb.append(" order by ");
                sb.append(orderBy.getName());
                if (!isAsc) {
                    sb.append(" desc");
                }
            }
            sb.append(" limit ");
            if (limitLow > 0) {
                sb.append(limitLow);
                sb.append(",");
            }
            sb.append(limitHigh);
            sql = sb.toString();
        }
        return sql;
    }

    public String getOrderBy() {
        String rt = "";
        if (orderBy != null) {
            rt += orderBy.getName();
            rt += isAsc ? "" : " desc ";
        }
        return rt;
    }

    /**
     * order and limit
     */
    public PreparedColumnQueryBuilder asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    public PreparedColumnQueryBuilder desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    public PreparedColumnQueryBuilder limit(int size) {
        limitHigh = size;
        return this;
    }

    public PreparedColumnQueryBuilder limit(int start, int size) {
        limitLow = start;
        limitHigh = size;
        return this;
    }

    @Override
    public String toString() {
        return "PreparedPartialQuerySql{" + build() + "}";
    }
}
