
package cn.liberg.database.join;


public class JoinOn {
    public static final String TYPE_INNER = "inner join";
    public static final String TYPE_LEFT = "left join";
    public static final String TYPE_RIGHT = "right join";

    //第一张表
    private JoinDao acc1;
    //join类型
    private final String type;
    //第二张表
    public final JoinDao acc2;
    //join的条件
    public final JoinWhere joinWhere;

    public JoinOn(String joinType, JoinDao acc1, JoinDao acc2) {
        this.acc1 = acc1;
        this.acc2 = acc2;
        this.type = joinType;
        joinWhere = new JoinWhere();
    }

    public JoinWhere getJoinWhere() {
        return joinWhere;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append(type + " ");
        sb.append(acc2.dao.getTableName()+" ");
        sb.append(acc2.alias);
        sb.append(" on ");
        sb.append(joinWhere.build());
        return sb.toString();
    }

}
