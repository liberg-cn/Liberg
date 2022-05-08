package cn.liberg.database;

/**
 * 用{@code Condition}定义单个表达比较语义的子条件。
 * <p>
 * {@code Condition}和{@link Joints}一起组合出完整的查询where条件。
 *
 * @author Liberg
 * @see Joints
 */
public class Condition extends WhereMeta {
    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String GE = ">=";
    public static final String GT = ">";
    public static final String LE = "<=";
    public static final String LT = "<";
    public static final String LIKE = " like ";

    public Condition(String name, String link, String value) {
        super(name + link + value);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    public static String eq(String column, String value) {
        return column + EQ + SqlDefender.format(value);
    }

    public static String eq(String column, Number value) {
        return column + EQ + value;
    }

    public static String eq(String column1, Number value1, String column2, Number value2) {
        return eq(column1, value1) + AND + eq(column2, value2);
    }

    public static String eq(String column1, String value1, String column2, String value2) {
        return eq(column1, value1) + AND + eq(column2, value2);
    }

    public static String eq(String column1, String value1, String column2, Number value2) {
        return eq(column1, value1) + AND + eq(column2, value2);
    }

    public static String eq(String column1, Number value1, String column2, String value2) {
        return eq(column1, value1) + AND + eq(column2, value2);
    }

    public static String ne(String column, String value) {
        return column + NE + SqlDefender.format(value);
    }

    public static String ne(String column, Number value) {
        return column + NE + value;
    }

    public static String gt(String column, Number value) {
        return column + GT + value;
    }

    public static String lt(String column, Number value) {
        return column + LT + value;
    }

    public static String ge(String column, Number value) {
        return column + GE + value;
    }

    public static String le(String column, Number value) {
        return column + LE + value;
    }

    public static String range(String column, Number begin, Number end) {
        return column + GE + begin + AND + column + LE + end;
    }

    public static final String like(String column, String value) {
        return column + LIKE + SqlDefender.format(value);
    }

}
