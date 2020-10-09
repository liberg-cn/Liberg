package cn.liberg.database.query;


import cn.liberg.core.OperatorException;
import cn.liberg.database.BaseDao;

import java.util.List;

/**
 * 查询帮助类，如不想写sql可以试试
 */
public class Query extends Where<Query> {
    private BaseDao dao;
    private Column orderBy = null;
    private boolean isAsc = true;
    private int limitLow = 0;
    private int limitHigh = 1000;


    public static Query of(BaseDao dao) {
        return new Query(dao);
    }

    public Query(BaseDao dao) {
        this.dao = dao;
    }

    public <T> T one() throws OperatorException {
        return (T) dao.getByWhere(build());
    }

    public <T> List<T> all() throws OperatorException {
        return (List<T>) dao.getAllByWhere(build() + getLimit());
    }

    public <T> List<T> page(int pageNum, int pageSize) throws OperatorException {
        return (List<T>) dao.getPageByWhere(pageNum, pageSize, build());
    }

    public int count() throws OperatorException {
        return dao.getCount(super.build());
    }

    public BaseDao getDao() {
        return dao;
    }

    public String getOrderBy() {
        String rt = "";
        if(orderBy != null) {
            rt += orderBy.getName();
            if(!isAsc) {
                rt += " desc ";
            }
        }
        return rt;
    }
    public String getLimit() {
        return "limit "+limitLow+","+limitHigh + ";";
    }

    /**
     * order and limit
     */
    public Query asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    public Query desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    public Query limit(int size) {
        limitHigh = size;
        return this;
    }

    public Query limit(int start, int size) {
        limitLow = start;
        limitHigh = size;
        return this;
    }

    @Override
    public String build() {
        return super.build() + getOrderBy();
    }

    public String showSql() {
        return "select * from " + dao.getTableName() + " where "+ build() + getLimit();
    }

    @Override
    public String toString() {
        return "Query{" + build()+getLimit() + "}";
    }
}
