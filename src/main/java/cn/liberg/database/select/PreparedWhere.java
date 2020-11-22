package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.database.SqlDefender;
import cn.liberg.database.Condition;
import cn.liberg.database.Joints;
import cn.liberg.database.WhereMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepare方式的where条件查询
 *
 * @author Liberg
 */
public class PreparedWhere<T extends PreparedWhere> {
    protected List<WhereMeta> criteriaList = new ArrayList<>();
    protected Map<Column, Integer> indexMap = new HashMap<>();
    public static final String $ = "?";

    protected StringBuilder buildCondition() {
        StringBuilder sb = new StringBuilder();
        if(criteriaList.size() > 0) {
            for (WhereMeta e : criteriaList) {
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
     * 带'$'后缀的方法，用于提醒开发者，当前列置入了一个占位符'?'，
     * 在实际执行sql之前，需要调用{@code setParameter}进行参数填充。
     */
    public T eq$(Column column) {
        return _add(column, Condition.EQ);
    }

    public T ne$(Column column) {
        return _add(column, Condition.NE);
    }

    /**
     * like
     */
    public T like(Column<String> column, String value) {
        return _add(column, Condition.LIKE, SqlDefender.format(value));
    }

    public T like$(Column<String> column) {
        return _add(column, Condition.LIKE);
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

    public T ge$(Column<? extends Number> column) {
        return _add(column, Condition.GE);
    }
    public T gt$(Column<? extends Number> column) {
        return _add(column, Condition.GT);
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

    public T le$(Column<? extends Number> column) {
        return _add(column, Condition.LE);
    }
    public T lt$(Column<? extends Number> column) {
        return _add(column, Condition.LT);
    }

    /**
     * and/or/not
     */
    public T and() {
        criteriaList.add(Joints.AND);
        return (T) this;
    }

    public T or() {
        criteriaList.add(Joints.OR);
        return (T) this;
    }

    public T not() {
        criteriaList.add(Joints.NOT);
        return (T) this;
    }

    public T bracketStart() {
        criteriaList.add(Joints.BRACKET_START);
        return (T) this;
    }

    public T bracketEnd() {
        criteriaList.add(Joints.BRACKET_END);
        return (T) this;
    }

    protected T _add(Column column, String link, String value) {
        // 两个Condition之间未指定逻辑运算符时，默认用and
        if (criteriaList.size() > 0 && criteriaList.get(criteriaList.size() - 1).isCondition()) {
            criteriaList.add(Joints.AND);
        }
        criteriaList.add(new Condition(column.name, link, value));
        return (T) this;
    }
    protected T _add(Column column, String link) {
        // 两个Condition之间未指定逻辑运算符时，默认用and
        if (criteriaList.size() > 0 && criteriaList.get(criteriaList.size() - 1).isCondition()) {
            criteriaList.add(Joints.AND);
        }
        criteriaList.add(new Condition(column.name, link, $));
        if(!indexMap.containsKey(column)) {
            indexMap.put(column, indexMap.size()+1);
        }
        return (T) this;
    }
}
