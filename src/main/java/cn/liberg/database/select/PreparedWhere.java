package cn.liberg.database.select;

import cn.liberg.core.Column;
import cn.liberg.core.Field;
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
    protected Map<String, NextIndex> indexMap = new HashMap<>();
    // 占位符
    public static final String $ = "?";
    // 占位符的个数
    protected int $length = 0;

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
    public T eq(Field<String> column, String value) {
        return _add(column, Condition.EQ, SqlDefender.format(value));
    }

    public T eq(Field<? extends Number> column, Number value) {
        return _add(column, Condition.EQ, "" + value);
    }

    public T ne(Field<String> column, String value) {
        return _add(column, Condition.NE, SqlDefender.format(value));
    }

    public T ne(Field<? extends Number> column, Number value) {
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
    public T like(Field<String> column, String value) {
        return _add(column, Condition.LIKE, SqlDefender.format(value));
    }

    public T like$(Field<String> column) {
        return _add(column, Condition.LIKE);
    }

    /**
     * great equal or great than
     */
    public T ge(Field<? extends Number> column, Number value) {
        return _add(column, Condition.GE, "" + value);
    }

    public T gt(Field<? extends Number> column, Number value) {
        return _add(column, Condition.GT, "" + value);
    }

    public T ge$(Field<? extends Number> column) {
        return _add(column, Condition.GE);
    }
    public T gt$(Field<? extends Number> column) {
        return _add(column, Condition.GT);
    }

    /**
     * less equal or less than
     */
    public T le(Field<? extends Number> column, Number value) {
        return _add(column, Condition.LE, "" + value);
    }

    public T lt(Field<? extends Number> column, Number value) {
        return _add(column, Condition.LT, "" + value);
    }

    public T le$(Field<? extends Number> column) {
        return _add(column, Condition.LE);
    }
    public T lt$(Field<? extends Number> column) {
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

    protected T _add(Field column, String link, String value) {
        // 两个Condition之间未指定逻辑运算符时，默认用and
        if (criteriaList.size() > 0 && criteriaList.get(criteriaList.size() - 1).isCondition()) {
            criteriaList.add(Joints.AND);
        }
        criteriaList.add(new Condition(column.name, link, value));
        return (T) this;
    }

    //value用占位符代替
    protected T _add(Field column, String link) {
        // 两个Condition之间未指定逻辑运算符时，默认用and
        if (criteriaList.size() > 0 && criteriaList.get(criteriaList.size() - 1).isCondition()) {
            criteriaList.add(Joints.AND);
        }
        criteriaList.add(new Condition(column.name, link, $));
        //index是从1开始的，先++
        $length++;
        NextIndex nIdx = indexMap.get(column.name);
        if(nIdx == null) {
            indexMap.put(column.name, new NextIndex1($length));
        } else {
            indexMap.put(column.name, nIdx.add($length));
        }
        return (T) this;
    }
}
