package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.OperatorException;

/**
 * prepare预编译查询方式，查询条件类。
 *
 * @param <T>
 *
 * @author Liberg
 */
public class PreparedSelectWhere<T> extends PreparedWhere<PreparedSelectWhere<T>> {

    private final PreparedSelect<T> select;
    private Column orderBy = null;
    private boolean isAsc = true;
    int limitStart = 0;
    int limitCount = 1000;

    public PreparedSelectWhere(PreparedSelect<T> select) {
        this.select = select;
    }

    public PreparedSelectExecutor<T> prepare() throws OperatorException {
        return new PreparedSelectExecutor<>(select, this);
    }

    public PreparedSelectWhere<T> limit(int start, int count) {
        limitStart = start;
        limitCount = count;
        return this;
    }

    public PreparedSelectWhere<T> limit(int count) {
        limitCount = count;
        return this;
    }

    /**
     * 按指定column升序
     */
    public PreparedSelectWhere<T> asc(Column column) {
        orderBy = column;
        isAsc = true;
        return this;
    }

    /**
     * 按指定column降序
     */
    public PreparedSelectWhere<T> desc(Column column) {
        orderBy = column;
        isAsc = false;
        return this;
    }

    protected String buildSql() {
        StringBuilder sb = select.build();
        sb.append(" where ");
        sb.append(buildWhere());
        return sb.toString();
    }

    protected String buildWhere() {
        StringBuilder sb = buildCondition();
        if (orderBy != null) {
            sb.append(" order by ");
            sb.append(orderBy.name);
            if (!isAsc) {
                sb.append(" desc ");
            }
        }
        sb.append(" limit ?,?;");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + buildSql() + "}";
    }


}
