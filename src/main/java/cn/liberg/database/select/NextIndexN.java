package cn.liberg.database.select;

import java.util.ArrayList;
import java.util.List;

/**
 * PreparedStatement中某个column只出现了不止一次
 * next()方法返回下一个占位符的序号（是第几个占位符）
 */
class NextIndexN implements NextIndex {
    private int _i = -1;
    private final List<Integer> list;

    public NextIndexN(NextIndex1 old, int newIndex) {
        list = new ArrayList<>();
        list.add(old.next());
        list.add(newIndex);
    }

    public NextIndexN add(int newIndex) {
        list.add(newIndex);
        return this;
    }

    public int next() {
        _i = (_i+1)%list.size();
        return list.get(_i);
    }
}
