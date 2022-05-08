package cn.liberg.database;

/**
 * {@code WhereMeta}抽象出构成{@code where}条件的各种元素。
 *
 * 分为几类：
 * 1、比较类，{@link Condition}
 * 2、逻辑运算符类，{@link Joints#AND}/{@link Joints#OR}/{@link Joints#NOT}
 * 3、括号类，{@link Joints#BRACKET_START}/{@link Joints#BRACKET_END}
 *
 * 子类（对象）包括：
 * @see Condition
 * @see Joints#AND
 * @see Joints#OR
 * @see Joints#NOT
 * @see Joints#BRACKET_START
 * @see Joints#BRACKET_END
 * @author Liberg
 *
 */
public class WhereMeta {
    public static final String AND = " and ";
    public static final String OR = " or ";
    public static final String NOT = " not ";
    public static final String BRACKET_START = "(";
    public static final String BRACKET_END = ")";

    /**
     * String值
     */
    public final String value;

    protected WhereMeta(String value) {
        this.value = value;
    }

    /**
     * 是否是左括号：{@link Joints#BRACKET_START}
     *
     * @return
     */
    public boolean isStartBracket() {
        return false;
    }

    /**
     * 是否是右括号：{@link Joints#BRACKET_END}
     *
     * @return
     */
    public boolean isEndBracket() {
        return false;
    }

    /**
     * 是否是查询子条件：{@link Condition}
     *
     * @return
     */
    public boolean isCondition() {
        return false;
    }

    /**
     * 是否是逻辑运算符：
     * {@link Joints#AND}
     * {@link Joints#OR}
     * {@link Joints#NOT}
     *
     * @return
     */
    public boolean isLogicalOperator() {
        return false;
    }
}
