package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.database.SqlDefender;
import cn.liberg.database.Condition;
import cn.liberg.database.Joints;
import cn.liberg.database.WhereMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * where条件查询
 *
 * @author Liberg
 */
public class Where<T extends Where> {
    protected List<WhereMeta> metaList = new ArrayList<>();

    protected StringBuilder buildCondition() {
        StringBuilder sb = new StringBuilder();
        if(metaList.size() > 0) {
            for (WhereMeta e : metaList) {
                sb.append(e.value);
                sb.append(" ");
            }
        } else {
            sb.append("1=1 ");
        }
        return sb;
    }

    /**
     * equals or not
     */
    public T eq(Column<String> column, String value) {
        return _add(column, Condition.EQ, SqlDefender.format(value));
    }

    public T eq(Column<? extends Number> column, Number value) {
        return _add(column, Condition.EQ, "" + value);
    }

    public T ne(Column<String> column, String value) {
        return _add(column, Condition.NE, SqlDefender.format(value));
    }

    public T ne(Column<? extends Number> column, Number value) {
        return _add(column, Condition.NE, "" + value);
    }

    /**
     * like
     */
    public T like(Column<String> column, String value) {
        return _add(column, Condition.LIKE, SqlDefender.format(value));
    }

    /**
     * great equal or great than
     */
    public T ge(Column<? extends Number> column, Number value) {
        return _add(column, Condition.GE, "" + value);
    }

    public T gt(Column<? extends Number> column, Number value) {
        return _add(column, Condition.GT, "" + value);
    }

    /**
     * less equal or less than
     */
    public T le(Column<? extends Number> column, Number value) {
        return _add(column, Condition.LE, "" + value);
    }

    public T lt(Column<? extends Number> column, Number value) {
        return _add(column, Condition.LT, "" + value);
    }

    /**
     * and/or/not
     */
    public T and() {
        metaList.add(Joints.AND);
        return (T) this;
    }

    public T or() {
        metaList.add(Joints.OR);
        return (T) this;
    }

    public T not() {
        metaList.add(Joints.NOT);
        return (T) this;
    }

    public T bracketStart() {
        metaList.add(Joints.BRACKET_START);
        return (T) this;
    }

    public T bracketEnd() {
        metaList.add(Joints.BRACKET_END);
        return (T) this;
    }

    protected T _add(Column column, String link, String value) {
        // 两个Condition之间未指定逻辑运算符时，默认用and
        if (metaList.size() > 0 && metaList.get(metaList.size() - 1).isCondition()) {
            metaList.add(Joints.AND);
        }
        metaList.add(new Condition(column.name, link, value));
        return (T) this;
    }
}
