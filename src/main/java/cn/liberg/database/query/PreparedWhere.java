package cn.liberg.database.query;


import java.util.ArrayList;
import java.util.List;

public class PreparedWhere<T extends PreparedWhere> {
    protected List<IWhereMeta> elems = new ArrayList<>();

    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String GE = ">=";
    public static final String GT = ">";
    public static final String LE = "<=";
    public static final String LT = "<";
    public static final String LIKE = " like ";

    public static final IWhereMeta OR = new Operation("or");
    public static final IWhereMeta AND = new Operation("and");
    public static final IWhereMeta NOT = new Operation("not");

    public static final IWhereMeta BRACKET_LEFT = new IWhereMeta() {
        @Override
        public boolean isLeftBracket() {
            return true;
        }

        public String build() {
            return "(";
        }
    };
    public static final IWhereMeta BRACKET_RIGHT = new IWhereMeta() {
        @Override
        public boolean isRightBracket() {
            return true;
        }

        @Override
        public String build() {
            return ")";
        }
    };

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
        return (T) _add(BRACKET_LEFT);
    }
    public T bracketRight() {
        return (T) _add(BRACKET_RIGHT);
    }

    /**
     * equals or not
     */
    public T eq(Column column) {
        return (T) _add(column.getName(), EQ);
    }
    public T ne(Column column) {
        return (T) _add(column.getName(), NE);
    }

    /**
     * like
     */
    public T like(Column column) {
        return (T) _add(column.getName(), LIKE);
    }

    /**
     * great equal or great than
     */
    public T ge(Column column) {
        return (T) _add(column.getName(), GE);
    }
    public T gt(Column column) {
        return (T) _add(column.getName(), GT);
    }

    /**
     * less equal or less than
     */
    public T le(Column column) {
        return (T) _add(column.getName(), LE);
    }
    public T lt(Column column) {
        return (T) _add(column.getName(), LT);
    }

    /**
     * and/or/not
     */
    public T and() {
        return (T) _add(AND);
    }
    public T or() {
        return (T) _add(OR);
    }
    public T not() {
        return (T) _add(NOT);
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
        return "Where{" + build() + "}";
    }
}
