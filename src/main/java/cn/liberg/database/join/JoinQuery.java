package cn.liberg.database.join;

import cn.liberg.core.*;
import cn.liberg.database.BaseDao;
import cn.liberg.database.DBHelper;
import cn.liberg.database.Condition;
import cn.liberg.database.Joints;
import cn.liberg.database.WhereMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持3张表的join查询
 *
 * @author Liberg
 */
public class JoinQuery {
    private static final String[] ALIAS = {"a", "b", "c", "d"};
    private int aliasIndex;

    private JoinDao baseDao;
    private JoinFields joinFields;
    private final List<JoinOn> joinOnList = new ArrayList<>();
    private Phase phase = Phase.ON_TWO;

    private JoinOn currentJoinOn;
    private JoinWhere joinWhere;
    private JoinDao lastDao;
    private JoinDao currentDao;
    private JoinDao opDao;

    private Map<BaseDao, JoinDao> daoMap = new HashMap<>();

    //order by
    private Column orderByColumn = null;
    private JoinDao orderByDao = null;
    private boolean isAsc = true;
    //limit
    private int limitLow = 0;
    private int limitHigh = 1000;

    private JoinQuery() {
    }

    //用于支持链式表达
    public JoinQuery innerJoin(BaseDao dao) {
        lastDao = currentDao;
        currentDao = nextJoinDao(dao);
        joinFields.addAll(currentDao);
        currentJoinOn = new JoinOn(JoinOn.TYPE_INNER, lastDao, currentDao);
        joinOnList.add(currentJoinOn);
        return this;
    }

    public JoinQuery leftJoin(BaseDao dao) {
        lastDao = currentDao;
        currentDao = nextJoinDao(dao);
        joinFields.addAll(currentDao);
        currentJoinOn = new JoinOn(JoinOn.TYPE_LEFT, lastDao, currentDao);
        joinOnList.add(currentJoinOn);
        return this;
    }

    public JoinQuery rightJoin(BaseDao dao) {
        lastDao = currentDao;
        currentDao = nextJoinDao(dao);
        joinFields.addAll(currentDao);
        currentJoinOn = new JoinOn(JoinOn.TYPE_RIGHT, lastDao, currentDao);
        joinOnList.add(currentJoinOn);
        return this;
    }

    public JoinQuery on() {
        phase = Phase.ON_TWO;
        return this;
    }

    public JoinQuery onLeft() {
        phase = Phase.ON_ONE;
        opDao = lastDao;
        return this;
    }

    public JoinQuery onRight() {
        phase = Phase.ON_ONE;
        opDao = currentDao;
        return this;
    }

    public JoinQuery where(BaseDao a1, BaseDao a2) {
        JoinDao[] arr = checkDao(a1, a2);
        phase = Phase.WHERE_TWO;
        lastDao = arr[0];
        currentDao = arr[1];
        return this;
    }

    //直接作用于最后的两张表
    public JoinQuery where() {
        phase = Phase.WHERE_TWO;
        return this;
    }

    public JoinQuery where(BaseDao dao) {
        opDao = checkDao(dao)[0];
        phase = Phase.WHERE_ONE;
        return this;
    }

    /**
     * equals or not
     */
    public JoinQuery eq(Column column1, Column column2) {
        return addCondition(column1, Condition.EQ, column2.name);
    }

    public JoinQuery eq(Field<String> column, String val) {
        if (phase.isOne()) {
            val = "'" + val + "'";
        }
        return addCondition(column, Condition.EQ, val);
    }

    public JoinQuery eq(Field<Integer> column, int val) {
        return addCondition(column, Condition.EQ, "" + val);
    }

    public JoinQuery eq(Field<Long> column, long val) {
        return addCondition(column, Condition.EQ, "" + val);
    }

    public JoinQuery ne(Field<String> column, String val) {
        if (phase.isOne()) {
            val = "'" + val + "'";
        }
        return addCondition(column, Condition.NE, val);
    }

    public JoinQuery ne(Field<Integer> column, int val) {
        return addCondition(column, Condition.NE, "" + val);
    }

    public JoinQuery ne(Field<Long> column, long val) {
        return addCondition(column, Condition.NE, "" + val);
    }

    /**
     * like
     */
    public JoinQuery like(Field<String> column, String val) {
        if (phase.isOne()) {
            val = "'" + val + "'";
        }
        return addCondition(column, Condition.LIKE, val);
    }


    /**
     * great equal or great than
     */
    public JoinQuery ge(Field<Integer> column, int val) {
        return addCondition(column, Condition.GE, "" + val);
    }

    public JoinQuery ge(Field<Long> column, long val) {
        return addCondition(column, Condition.GE, "" + val);
    }

    public JoinQuery gt(Field<Integer> column, int val) {
        return addCondition(column, Condition.GT, "" + val);
    }

    public JoinQuery gt(Field<Long> column, long val) {
        return addCondition(column, Condition.GT, "" + val);
    }

    /**
     * less equal or less than
     */
    public JoinQuery le(Field<Integer> column, int val) {
        return addCondition(column, Condition.LE, "" + val);
    }

    public JoinQuery le(Field<Long> column, long val) {
        return addCondition(column, Condition.LE, "" + val);
    }

    public JoinQuery lt(Field<Integer> column, int val) {
        return addCondition(column, Condition.LT, "" + val);
    }

    public JoinQuery lt(Field<Long> column, long val) {
        return addCondition(column, Condition.LT, "" + val);
    }

    public JoinQuery bracketLeft() {
        return addWhereMeta(Joints.BRACKET_START);
    }

    public JoinQuery bracketRight() {
        return addWhereMeta(Joints.BRACKET_END);
    }

    /**
     * and/or/not
     */
    public JoinQuery and() {
        return addWhereMeta(Joints.AND);
    }

    public JoinQuery or() {
        return addWhereMeta(Joints.OR);
    }

    public JoinQuery not() {
        return addWhereMeta(Joints.NOT);
    }


    private JoinQuery addCondition(Field column, String mid, String value) {
        String name = column.name;
        switch (phase) {
            case ON_TWO:
                currentJoinOn.getJoinWhere().add(new JoinCondition(lastDao, currentDao, name, mid, value));
                break;
            case ON_ONE:
                currentJoinOn.getJoinWhere().add(new JoinCondition(opDao, name, mid, value));
                break;
            case WHERE_TWO:
                if (joinWhere == null) {
                    joinWhere = new JoinWhere();
                }
                joinWhere.add(new JoinCondition(lastDao, currentDao, name, mid, value));
                break;
            case WHERE_ONE:
                if (joinWhere == null) {
                    joinWhere = new JoinWhere();
                }
                joinWhere.add(new JoinCondition(opDao, name, mid, value));
                break;
            default:
                break;
        }
        return this;
    }

    private JoinQuery addWhereMeta(WhereMeta meta) {
        switch (phase) {
            case ON_TWO:
            case ON_ONE:
                currentJoinOn.getJoinWhere().add(meta);
                break;
            case WHERE_TWO:
            case WHERE_ONE:
                if (joinWhere == null) {
                    joinWhere = new JoinWhere();
                }
                joinWhere.add(meta);
                break;
            default:
                break;
        }
        return this;
    }

    private JoinDao nextJoinDao(BaseDao dao) {
        String alias;
        if (aliasIndex >= ALIAS.length) {
            alias = ALIAS[aliasIndex & 3] + (aliasIndex >>> 2);
        } else {
            alias = ALIAS[aliasIndex++];
        }
        JoinDao acc = new JoinDao(dao, alias);
        daoMap.put(dao, acc);
        return acc;
    }

    public JoinDao[] checkDao(BaseDao... list) {
        JoinDao[] arr = new JoinDao[list.length];
        for (int i = 0; i < list.length; i++) {
            JoinDao acc = daoMap.get(list[i]);
            if (acc == null) {
                throw new IllegalArgumentException("Invalid access for current JoinQuery instance.");
            }
            arr[i] = acc;
        }
        return arr;
    }

    public static JoinQuery basedOn(BaseDao dao) {
        JoinQuery jq = new JoinQuery();
        JoinDao ja = jq.nextJoinDao(dao);
        jq.baseDao = ja;
        jq.currentDao = ja;
        jq.lastDao = ja;
        jq.joinFields = JoinFields.of(ja);
        jq.opDao = ja;
        return jq;
    }

    public <T> List<T> allOf(BaseDao<T> dao) throws OperatorException {
        JoinDao joinDao = checkDao(dao)[0];
        String sql = "select " + joinDao.alias + ".* " + build();
        return DBHelper.self().getAllBySql(sql, joinDao.dao);
    }

    public <T> T oneOf(BaseDao<T> dao) throws OperatorException {
        JoinDao joinDao = checkDao(dao)[0];
        limit(1);
        String sql = "select " + joinDao.alias + ".* " + build();
        return (T)DBHelper.self().getBySql(sql, joinDao.dao);
    }

    public JoinResult all() throws OperatorException {
        String sql = "select " + joinFields.build() + build();
        return DBHelper.self().getTableData(sql);
    }

    public JoinResult one() throws OperatorException {
        limit(1);
        String sql = "select " + joinFields.build() + build();
        System.out.println(sql);
        return DBHelper.self().getTableData(sql);
    }

    public String showSql() {
        return "select " + joinFields.build() + build();
    }

    public JoinQuery asc(BaseDao dao, Column column) {
        this.orderByDao = checkDao(dao)[0];
        this.orderByColumn = column;
        this.isAsc = true;
        return this;
    }

    public JoinQuery desc(BaseDao dao, Column column) {
        this.orderByDao = checkDao(dao)[0];
        this.orderByColumn = column;
        this.isAsc = false;
        return this;
    }

    public JoinQuery asc(Column column) {
        this.orderByDao = baseDao;
        this.orderByColumn = column;
        this.isAsc = true;
        return this;
    }

    public JoinQuery desc(Column column) {
        this.orderByDao = baseDao;
        this.orderByColumn = column;
        this.isAsc = false;
        return this;
    }

    public JoinQuery limit(int low, int high) {
        this.limitLow = low;
        this.limitHigh = high;
        return this;
    }

    public JoinQuery limit(int size) {
        this.limitLow = 0;
        this.limitHigh = size;
        return this;
    }

    public JoinQuery where(JoinWhere joinWhere) {
        this.joinWhere = joinWhere;
        return this;
    }

    private JoinQuery join(JoinOn jo) {
        currentDao = jo.acc2;
        joinOnList.add(jo);
        return this;
    }


    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(baseDao.dao.getTableName() + " ");
        sb.append(baseDao.alias + " ");
        JoinOn jo;
        for (int i = 0; i < joinOnList.size(); i++) {
            jo = joinOnList.get(i);
            sb.append(jo.build());
        }
        if (joinWhere != null) {
            sb.append("where ");
            sb.append(joinWhere.build());
        }
        if (orderByDao != null) {
            sb.append("order by " + orderByDao.alias);
            sb.append("." + orderByColumn.name + " ");
            if (!isAsc) {
                sb.append("desc ");
            }
        }
        sb.append("limit ");
        if (limitLow > 0) {
            sb.append(limitLow + ",");
        }
        sb.append(limitHigh);
        return sb.toString();
    }


    enum Phase {
        ON_TWO(1),
        ON_ONE(2),
        WHERE_TWO(3),
        WHERE_ONE(4);

        private int value;

        Phase(int value) {
            this.value = value;
        }

        public boolean isOn() {
            return value == ON_TWO.value || value == ON_ONE.value;
        }

        public boolean isWhere() {
            return value == WHERE_TWO.value || value == WHERE_ONE.value;
        }

        public boolean isTwo() {
            return value == ON_TWO.value || value == WHERE_TWO.value;
        }

        public boolean isOne() {
            return value == ON_ONE.value || value == WHERE_ONE.value;
        }
    }
}
