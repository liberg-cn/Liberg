package cn.liberg.database.update;

import cn.liberg.core.OperatorException;
import cn.liberg.database.DBHelper;
import cn.liberg.database.select.Where;

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

    private String buildSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(update.dao.getTableName());
        sb.append(" set ");
        sb.append(update.buildSql());
        sb.append(" where ");
        sb.append(buildCondition());
        return sb.toString();
    }

    public void execute() throws OperatorException {
        DBHelper.self().executeSql(buildSql());
    }
}
