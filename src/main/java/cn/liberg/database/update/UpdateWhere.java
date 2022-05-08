package cn.liberg.database.update;

import cn.liberg.core.OperatorException;
import cn.liberg.database.BaseDao;
import cn.liberg.database.DBHelper;
import cn.liberg.database.select.Where;

import java.util.List;

/**
 * update操作的执行类。
 *
 * @param <T> 代表实体类的泛型参数
 *
 * @author Liberg
 * @see Update
 */
public class UpdateWhere<T> extends Where<UpdateWhere<T>> {

    private Update<T> update;

    public UpdateWhere(Update<T> update) {
        this.update = update;
    }

    private String buildSql(StringBuilder where) {
        StringBuilder sql = new StringBuilder();
        sql.append("update ");
        sql.append(update.dao.getTableName());
        sql.append(" set ");
        sql.append(update.build());
        sql.append(" where ");
        sql.append(where);
        return sql.toString();
    }

    /**
     * 如果dao中没有缓存数据，可以放心使用
     *
     * 如果dao中有缓存数据，更新缓存是很重的操作，甚至可能导致缓存失效，
     *     尽量少用。建议使用{@link cn.liberg.database.BaseDao#update(T)}方法
     */
    public void execute() throws OperatorException {
        StringBuilder where = new StringBuilder();
        appendConditionTo(where);
        // 更新数据库
        DBHelper.self().executeSql(buildSql(where));

        final BaseDao<T> dao = update.dao;
        if(dao.isEntityCached()) {
            // 更新缓存，或者让缓存失效
            List<T> list = dao.getPage(where.toString(), 0, 31);
            if(list.size() <= 30) {
                for(T entity : list) {
                    dao.putToCache(entity);
                }
            } else {
                dao.clearCache();
            }
        }
    }
}
