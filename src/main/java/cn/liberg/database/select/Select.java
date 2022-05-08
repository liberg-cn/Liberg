package cn.liberg.database.select;

import cn.liberg.core.Field;
import cn.liberg.database.BaseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * select操作的入口类。
 *
 * <p>
 * 1、查询哪张表，由构造方法的参数{@code dao}传入。
 * 2、通过{@code whereXxx(...)}系列方法将控制权转移给{@link SelectWhere}。
 *
 * @param <T>
 *           代表查询结果实体类，如果查询数据表的所有列
 *           代表Segment，如果查询数据表的部分列
 *           代表String/Number，如果只查询数据表的某一列
 *
 * 子类包括：
 * {@link SelectColumn} 查询某一列
 * {@link SelectSegment} 查询部分列
 *
 * @author Liberg
 * @see SelectWhere
 */
public class Select<T> {
    BaseDao<T> dao;

    public Select(BaseDao<T> dao) {
        this.dao = dao;
    }

    protected StringBuilder build() {
        StringBuilder sql = new StringBuilder(256);
        sql.append("select ");
        appendColumnsTo(sql);
        sql.append(" from ");
        sql.append(dao.getTableName());
        return sql;
    }

    protected void appendColumnsTo(StringBuilder sql) {
        sql.append("id,");
        sql.append(dao.getColumnsString());
    }

    /**
     * 1 = 1
     */
    public SelectWhere<T> where() {
        final SelectWhere selectWhere = new SelectWhere(this);
        return selectWhere;
    }
    /**
     * column = value:String
     */
    public SelectWhere<T> whereEq(Field<String> column, String value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.eq(column, value);
        return selectWhere;
    }
    /**
     * column = value:Number
     */
    public SelectWhere<T> whereEq(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.eq(column, value);
        return selectWhere;
    }
    /**
     * column <> value:String
     */
    public SelectWhere<T> whereNe(Field<String> column, String value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.ne(column, value);
        return selectWhere;
    }
    /**
     * column <> value:Number
     */
    public SelectWhere<T> whereNe(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.ne(column, value);
        return selectWhere;
    }
    /**
     * column like value:String
     */
    public SelectWhere<T> whereLike(Field<String> column, String value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.like(column, value);
        return selectWhere;
    }
    /**
     * column > value:Number
     */
    public SelectWhere<T> whereGt(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.gt(column, value);
        return selectWhere;
    }
    /**
     * column >= value:Number
     */
    public SelectWhere<T> whereGe(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.ge(column, value);
        return selectWhere;
    }
    /**
     * column < value:Number
     */
    public SelectWhere<T> whereLt(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.lt(column, value);
        return selectWhere;
    }
    /**
     * column <= value:Number
     */
    public SelectWhere<T> whereLe(Field<? extends Number> column, Number value) {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.le(column, value);
        return selectWhere;
    }
    /**
     * not - where后面的条件由not逻辑符开始
     */
    public SelectWhere<T> whereNot() {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.not();
        return selectWhere;
    }
    /**
     * ( - where后面的条件由左括号开始
     */
    public SelectWhere<T> whereBracketStart() {
        final SelectWhere selectWhere = new SelectWhere(this);
        selectWhere.bracketStart();
        return selectWhere;
    }

    public T readOne(ResultSet resultSet) throws SQLException {
        T entity = null;
        if(resultSet.next()) {
            entity = dao.buildEntity(resultSet);
            dao.putToCache(entity);
        }
        return entity;
    }

    public List<T> readAll(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(dao.buildEntity(resultSet));
        }
        return list;
    }
}
