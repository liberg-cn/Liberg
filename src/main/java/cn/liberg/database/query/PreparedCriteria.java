package cn.liberg.database.query;


import java.util.ArrayList;
import java.util.List;

public class PreparedCriteria<T extends PreparedCriteria> {
    protected List<IWhereMeta> elems = new ArrayList<>();

    public List<IWhereMeta> getElements() {
        return elems;
    }

    public String build() {
        StringBuilder sb = new StringBuilder(32);
        if (elems.size() > 0) {
            for (IWhereMeta e : elems) {
                sb.append(e.build());
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public T bracketLeft() {
        return _add(Joints.BRACKET_LEFT);
    }

    public T bracketRight() {
        return _add(Joints.BRACKET_RIGHT);
    }

    /**
     * equals or not
     */
    public T eq(Column column) {
        return _add(column.getName(), Joints.EQ);
    }

    public T ne(Column column) {
        return _add(column.getName(), Joints.NE);
    }

    /**
     * like
     */
    public T like(StringColumn column) {
        return _add(column.getName(), Joints.LIKE);
    }

    /**
     * great equal or great than
     */
    public T ge(IntegerColumn column) {
        return _add(column.getName(), Joints.GE);
    }

    public T ge(LongColumn column) {
        return _add(column.getName(), Joints.GE);
    }

    public T gt(IntegerColumn column) {
        return _add(column.getName(), Joints.GT);
    }

    public T gt(LongColumn column) {
        return _add(column.getName(), Joints.GT);
    }

    /**
     * less equal or less than
     */
    public T le(IntegerColumn column) {
        return _add(column.getName(), Joints.LE);
    }

    public T le(LongColumn column) {
        return _add(column.getName(), Joints.LE);
    }

    public T lt(IntegerColumn column) {
        return _add(column.getName(), Joints.LT);
    }

    public T lt(LongColumn column) {
        return _add(column.getName(), Joints.LT);
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


    private T _add(String name, String mid) {
        elems.add(new Condition(name, mid, "?"));
        return (T) this;
    }

    private T _add(IWhereMeta ele) {
        elems.add(ele);
        return (T) this;
    }

    @Override
    public String toString() {
        return "PreparedCriteria{" + build() + "}";
    }
}
