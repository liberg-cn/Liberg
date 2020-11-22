package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepare方式select操作的入口类。
 *
 * <p>
 * 1、查询哪张表，由构造方法的参数{@code dao}传入。
 * 2、通过{@code whereXxx(...)}系列方法将控制权转移给{@link PreparedSelectWhere}。
 *
 * @param <T>
 *           代表查询结果实体类，如果查询数据表的所有列
 *           代表Segment，如果查询数据表的部分列
 *           代表String/Number，如果只查询数据表的某一列
 *
 * 子类包括：
 * {@link PreparedSelectColumn} 查询某一列
 * {@link PreparedSelectSegment} 查询部分列
 *
 * @author Liberg
 * @see PreparedSelectWhere
 */
public class PreparedSelect<T> {
    BaseDao<T> dao;

    public PreparedSelect(BaseDao<T> dao) {
        this.dao = dao;
    }

    protected StringBuilder build() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        appendColumns(sb);
        sb.append(" from ");
        sb.append(dao.getTableName());
        return sb;
    }

    protected void appendColumns(StringBuilder sb) {
        sb.append("id,");
        sb.append(dao.getColumnsString());
    }

    /**
     * column = ?
     */
    public PreparedSelectWhere<T> whereEq$(Column column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.eq$(column);
        return psw;
    }
    /**
     * column <> ?
     */
    public PreparedSelectWhere<T> whereNe$(Column column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.ne$(column);
        return psw;
    }
    /**
     * column like ?
     */
    public PreparedSelectWhere<T> whereLike$(Column column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.like$(column);
        return psw;
    }
    /**
     * column > ?:Number
     */
    public PreparedSelectWhere<T> whereGt$(Column<? extends Number> column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.gt$(column);
        return psw;
    }
    /**
     * column >= ?:Number
     */
    public PreparedSelectWhere<T> whereGe$(Column<? extends Number> column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.ge$(column);
        return psw;
    }
    /**
     * column < ?:Number
     */
    public PreparedSelectWhere<T> whereLt$(Column<? extends Number> column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.lt$(column);
        return psw;
    }
    /**
     * column <= ?:Number
     */
    public PreparedSelectWhere<T> whereLe$(Column<? extends Number> column) {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.le$(column);
        return psw;
    }
    /**
     * not - where后面的条件由not逻辑符开始
     */
    public PreparedSelectWhere<T> whereNot() {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.not();
        return psw;
    }
    /**
     * ( - where后面的条件由左括号开始
     */
    public PreparedSelectWhere<T> whereBracketStart() {
        final PreparedSelectWhere psw = new PreparedSelectWhere(this);
        psw.bracketStart();
        return psw;
    }

    protected T readOne(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            return dao.buildEntity(resultSet);
        } else {
            return null;
        }
    }

    protected List<T> readAll(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(dao.buildEntity(resultSet));
        }
        return list;
    }
}
