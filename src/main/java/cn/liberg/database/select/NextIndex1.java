package cn.liberg.database.select;

/**
 * PreparedStatement中某个column只出现了一次
 * next()方法返回占位符的序号（是第几个占位符）
 */
class NextIndex1 implements NextIndex {
    private final int index;

    public NextIndex1(int index) {
        this.index = index;
    }

    public NextIndexN add(int newIndex) {
        return new NextIndexN(this, newIndex);
    }

    public int next() {
        return index;
    }
}
