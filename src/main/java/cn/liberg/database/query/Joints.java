package cn.liberg.database.query;

public class Joints {
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
}
