package cn.liberg.database;

/**
 * 用{@code Condition}定义单个表达比较语义的子条件。
 *
 * {@code Condition}和{@link Joints}一起组合出完整的查询where条件。
 *
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
}
