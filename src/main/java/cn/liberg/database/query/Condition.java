package cn.liberg.database.query;

class Condition implements IWhereMeta {
    String name;
    String link;
    String value;

    Condition(String name, String mid, String value) {
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
        return name+link+value;
    }
}
