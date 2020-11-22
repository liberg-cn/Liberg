package cn.liberg.database.join;


import cn.liberg.database.BaseDao;

/**
 * join操作的参与方
 *
 * @author Liberg
 */
public class JoinDao {
    public final String alias;
    public final BaseDao dao;

    public JoinDao(BaseDao dao, String alias) {
        this.dao = dao;
        this.alias = alias;
    }

}
