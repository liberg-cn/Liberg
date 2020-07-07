package cn.liberg.database.query;


import cn.liberg.database.BaseDao;

public class JoinDao {
    public final String alias;
    public final BaseDao dao;

    public JoinDao(BaseDao dao, String alias) {
        this.dao = dao;
        this.alias = alias;
    }

}
