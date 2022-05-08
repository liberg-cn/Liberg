package cn.liberg.database.select;

/**
 * PreparedStatement中某个column=?
 * next()方法返回占位符的序号（是第几个占位符）
 */
interface NextIndex {
    public int next();
    public NextIndex add(int newIndex);
}
