package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class PreparedQueryBuilder extends PreparedCriteria<PreparedQueryBuilder> {
    protected BaseDao dao;
    private Column orderBy = null;
    private boolean isAsc = true;
    private int limitLow = 0;
    private int limitHigh = 100;
    private volatile String sql = null;


    public BaseDao getDao() {
        return dao;
    }

    public PreparedQueryBuilder(BaseDao dao) {
        this.dao = dao;
    }

    @Override
    public String build() {
        if(sql == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append(dao.getFullColumnsString());
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
    public PreparedQueryBuilder asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    public PreparedQueryBuilder desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    public PreparedQueryBuilder limit(int size) {
        limitHigh = size;
        return this;
    }

    public PreparedQueryBuilder limit(int start, int size) {
        limitLow = start;
        limitHigh = size;
        return this;
    }

    @Override
    public String toString() {
        return "PreparedQuerySql{" + build() + "}";
    }
}
