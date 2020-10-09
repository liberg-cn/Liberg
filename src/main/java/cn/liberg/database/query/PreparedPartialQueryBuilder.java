package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class PreparedPartialQueryBuilder extends PreparedCriteria<PreparedPartialQueryBuilder> {
    protected BaseDao dao;
    private Column orderBy = null;
    private boolean isAsc = true;
    private int limitLow = 0;
    private int limitHigh = 100;
    private volatile String sql = null;
    private Column[] columns;

    public BaseDao getDao() {
        return dao;
    }

    public PreparedPartialQueryBuilder(BaseDao dao, Column... columns) {
        this.dao = dao;
        this.columns = columns;
    }

    public Column[] getColumns() {
        return columns;
    }

    public String buildColumns() {
        StringBuilder sb = new StringBuilder();
        if(columns.length > 0) {
            for(Column column : columns) {
                sb.append(column.getName());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
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
    public PreparedPartialQueryBuilder asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    public PreparedPartialQueryBuilder desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    public PreparedPartialQueryBuilder limit(int size) {
        limitHigh = size;
        return this;
    }

    public PreparedPartialQueryBuilder limit(int start, int size) {
        limitLow = start;
        limitHigh = size;
        return this;
    }

    @Override
    public String toString() {
        return "PreparedPartialQuerySql{" + build() + "}";
    }
}
