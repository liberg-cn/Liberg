package cn.liberg.database.join;

import cn.liberg.database.WhereMeta;

class JoinCondition extends WhereMeta {
    /**
     * join时第一张表
     */
    JoinDao dao1;
    /**
     * join时第二张表，
     * 若为null，则表示value是一个跟第二张表没关系的数据
     */
    JoinDao dao2;


    public JoinCondition(JoinDao dao1, JoinDao dao2, String name, String link, String value) {
        super(build(dao1, dao2, name, link, value));
        this.dao1 = dao1;
        this.dao2 = dao2;
    }

    public JoinCondition(JoinDao dao1, String name, String link, String value) {
        super(build(dao1, null, name, link, value));
        this.dao1 = dao1;
        this.dao2 = null;
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    private static String build(JoinDao dao1, JoinDao dao2, String name, String link, String value) {
        StringBuilder sb = new StringBuilder(dao1.alias + ".");
        sb.append(name);
        sb.append(link);
        if (dao2 != null) {
            sb.append(dao2.alias + ".");
        }
        sb.append(value);
        return sb.toString();
    }
}
