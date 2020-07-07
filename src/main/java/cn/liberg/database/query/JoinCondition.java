package cn.liberg.database.query;

class JoinCondition implements IWhereMeta {
    JoinDao dao1;//join时第一张表
    JoinDao dao2;//为null时表示value是一个跟第二张表没关系的数据
    String name;
    String link;
    String value;

    public JoinCondition(JoinDao acc1, JoinDao acc2, String name, String mid, String value) {
        this.dao1 = acc1;
        this.dao2 = acc2;
        this.name = name;
        this.link = mid;
        this.value = value;
    }
    public JoinCondition(JoinDao acc1, String name, String mid, String value) {
        this.dao1 = acc1;
        this.dao2 = null;
        this.name = name;
        this.link = mid;
        this.value = value;
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder(dao1.alias +".");
        sb.append(name);
        sb.append(link);
        if(dao2 != null) {
            sb.append(dao2.alias +".");
        }
        sb.append(value);
        return sb.toString();
    }
}
