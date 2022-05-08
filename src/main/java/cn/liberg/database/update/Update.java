package cn.liberg.database.update;

import cn.liberg.core.Column;
import cn.liberg.core.Field;
import cn.liberg.database.BaseDao;
import cn.liberg.database.SqlDefender;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * update操作的入口类。
 *
 * <p>
 * 1、更新哪张表，由构造方法的参数{@code dao}传入。
 * 2、通过{@code set(...)}、{@code increment(...)}方法，指定将哪些列更新为什么值。
 * 3、通过{@code whereXxx(...)}系列方法将控制权转移给{@link UpdateWhere}。
 *
 * @param <T> 代表实体类的泛型参数
 *
 * @author Liberg
 * @see UpdateWhere
 */
public class Update<T> {
    LinkedHashMap<Field, String> pairs;
    BaseDao<T> dao;

    public Update(BaseDao<T> dao) {
        this.dao = dao;
        pairs = new LinkedHashMap<>(16);
    }

    public Update set(Field<String> column, String value) {
        pairs.put(column, SqlDefender.format(value));
        return this;
    }

    public Update<T> set(Field<? extends Number> column, Number value) {
        pairs.put(column, value.toString());
        return this;
    }

    public Update<T> increment(Field<? extends Number> column, int value) {
        StringBuilder sb = new StringBuilder(column.name);
        if (value >= 0) {
            sb.append('+');
        }
        sb.append(value);
        pairs.put(column, sb.toString());
        return this;
    }

    String build() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Field, String> entry : pairs.entrySet()) {
            sb.append(entry.getKey().name);
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(',');
        }

        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    /**
     * 1 = 1
     */
    public UpdateWhere<T> where() {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        return updateWhere;
    }
    /**
     * column = value:String
     */
    public UpdateWhere<T> whereEq(Field<String> column, String value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.eq(column, value);
        return updateWhere;
    }
    /**
     * column = value:Number
     */
    public UpdateWhere<T> whereEq(Field<? extends Number> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.eq(column, value);
        return updateWhere;
    }
    /**
     * column <> value:String
     */
    public UpdateWhere<T> whereNe(Field<String> column, String value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.ne(column, value);
        return updateWhere;
    }
    /**
     * column <> value:Number
     */
    public UpdateWhere<T> whereNe(Field<? extends Number> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.ne(column, value);
        return updateWhere;
    }
    /**
     * column like value:String
     */
    public UpdateWhere<T> whereLike(Field<String> column, String value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.like(column, value);
        return updateWhere;
    }
    /**
     * column > value:Number
     */
    public UpdateWhere<T> whereGt(Field<String> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.gt(column, value);
        return updateWhere;
    }
    /**
     * column >= value:Number
     */
    public UpdateWhere<T> whereGe(Field<String> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.ge(column, value);
        return updateWhere;
    }
    /**
     * column < value:Number
     */
    public UpdateWhere<T> whereLt(Field<String> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.lt(column, value);
        return updateWhere;
    }
    /**
     * column <= value:Number
     */
    public UpdateWhere<T> whereLe(Field<String> column, Number value) {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.le(column, value);
        return updateWhere;
    }
    /**
     * not - where后面的条件由not逻辑符开始
     */
    public UpdateWhere<T> whereNot() {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.not();
        return updateWhere;
    }
    /**
     * ( - where后面的条件由左括号开始
     */
    public UpdateWhere<T> whereBracketStart() {
        final UpdateWhere updateWhere = new UpdateWhere(this);
        updateWhere.bracketStart();
        return updateWhere;
    }
}
