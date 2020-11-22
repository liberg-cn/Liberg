package cn.liberg.database;

/**
 * 定义查询子条件{@link Condition}的各种连接方式。
 * <p>
 * {@link Joints}和{@link Condition}一起组合出完整的查询条件。
 *
 * @author Liberg
 * @see Condition
 */
public class Joints {
    public static final WhereMeta AND = new LogicalOperator("and");
    public static final WhereMeta OR = new LogicalOperator("or");
    public static final WhereMeta NOT = new LogicalOperator("not");

    public static final WhereMeta BRACKET_START = new WhereMeta("(") {
        @Override
        public boolean isStartBracket() {
            return true;
        }
    };
    public static final WhereMeta BRACKET_END = new WhereMeta(")") {
        @Override
        public boolean isEndBracket() {
            return true;
        }
    };

    private static class LogicalOperator extends WhereMeta {
        public LogicalOperator(String op) {
            super(op);
        }

        @Override
        public boolean isLogicalOperator() {
            return true;
        }
    }
}
