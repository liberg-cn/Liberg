
package cn.liberg.database.query;

import java.util.ArrayList;
import java.util.List;

public class Where<T extends Where> {
    protected List<IWhereMeta> elems = new ArrayList<>();

    public List<IWhereMeta> getElements() {
        return elems;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        for (IWhereMeta e : elems) {
            sb.append(e.build());
            sb.append(" ");
        }
        return sb.toString();
    }

    public T bracketLeft() {
        return (T) _add(Joints.BRACKET_LEFT);
    }

    public T bracketRight() {
        return (T) _add(Joints.BRACKET_RIGHT);
    }

    /**
     * equals or not
     */
    public T eq(StringColumn column, String val) {
        val = "'" + val + "'";
        return _add(column, Joints.EQ, val);
    }

    public T eq(StringColumn column, int val) {
        return _add(column, Joints.EQ, "" + val);
    }

    public T eq(LongColumn column, long val) {
        return _add(column, Joints.EQ, "" + val);
    }

    public T ne(StringColumn column, String val) {
        val = "'" + val + "'";
        return _add(column, Joints.NE, val);
    }

    public T ne(IntegerColumn column, int val) {
        return _add(column, Joints.NE, "" + val);
    }

    public T ne(LongColumn column, long val) {
        return _add(column, Joints.NE, "" + val);
    }

    /**
     * like
     */
    public T like(StringColumn column, String val) {
        return _add(column, Joints.LIKE, "'" + val + "'");
    }


    /**
     * great equal or great than
     */
    public T ge(IntegerColumn column, int val) {
        return _add(column, Joints.GE, "" + val);
    }

    public T ge(LongColumn column, long val) {
        return _add(column, Joints.GE, "" + val);
    }

    public T gt(IntegerColumn column, int val) {
        return _add(column, Joints.GT, "" + val);
    }

    public T gt(LongColumn column, long val) {
        return _add(column, Joints.GT, "" + val);
    }

    /**
     * less equal or less than
     */
    public T le(IntegerColumn column, int val) {
        return _add(column, Joints.LE, "" + val);
    }

    public T le(LongColumn column, long val) {
        return _add(column, Joints.LE, "" + val);
    }

    public T lt(IntegerColumn column, int val) {
        return _add(column, Joints.LT, "" + val);
    }

    public T lt(LongColumn column, long val) {
        return _add(column, Joints.LT, "" + val);
    }

    /**
     * and/or/not
     */
    public T and() {
        return _add(Joints.AND);
    }

    public T or() {
        return _add(Joints.OR);
    }

    public T not() {
        return _add(Joints.NOT);
    }


    private T _add(Column column, String mid, String value) {
        elems.add(new Condition(column.getName(), mid, value));
        return (T) this;
    }

    private T _add(IWhereMeta ele) {
        elems.add(ele);
        return (T) this;
    }

    @Override
    public String toString() {
        return "Where{" + build() + "}";
    }
}
