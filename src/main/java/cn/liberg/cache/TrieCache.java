package cn.liberg.cache;

import java.util.*;

/**
 * 通过HashMap实现的字典树Trie
 * 优点：速度快
 * 缺点：HashMap占用内存大
 * 适用于特定行业的词库，总词数不太大（比如3000以内，视可用内存而定）
 *
 * @author Liberg
 */
public class TrieCache {
    private final Map<Character, Unit> root;
    private Set<Character> ignoredSet;

    public TrieCache() {
        root = new HashMap<>();
        ignoredSet = new HashSet<>();
        initIgnoredChars();
    }

    private void initIgnoredChars() {
        //默认过滤掉换行符、标点符号
        String ignore = "\r\n,.:;?!，。：；！";
        for(int i=0;i<ignore.length();i++) {
            ignoredSet.add(ignore.charAt(i));
        }
    }

    public synchronized void put(final String keyword) {
        char c;
        Unit unit = null;
        Map<Character, Unit> cur = root;
        for (int i = 0; i < keyword.length(); i++) {
            c = keyword.charAt(i);
            if (cur == null) {
                cur = new HashMap<>();
                unit.child = cur;
            }
            unit = cur.get(c);
            if (unit == null) {
                unit = new Unit(c, null);
            }
            cur.put(c, unit);
            cur = unit.child;
            if (i == keyword.length() - 1) {
                unit.keyword = keyword;
            }
        }
    }

    public void remove(String word) {
        char c;
        Unit unit = null;
        Stack<Unit> stack = new Stack<>();
        Map<Character, Unit> cur = root;
        for (int i = 0; i < word.length(); i++) {
            c = word.charAt(i);
            if (cur == null) {
                break;
            }
            unit = cur.get(c);
            if (unit == null) {
                break;
            }
            stack.push(unit);
            cur = unit.child;
        }
        //完整地找到了要删的词
        if (stack.size() >= word.length()) {
            Unit leaf = stack.pop();
            leaf.keyword = null;
            Unit rm = leaf;
            Unit lastRm = null;
            boolean skip = false;
            while (stack.size() > 0) {
                lastRm = rm;
                rm = stack.pop();
                if (rm.child.size() <= 1) {
                    rm.child = null;
                } else {
                    skip = true;
                    rm.child.remove(lastRm.c);
                    break;
                }
                if(rm.keyword!=null) {
                    break;
                }
            }
            if(!skip) {
                root.remove(rm.c);
            }
        }
    }

    public List<String> match(String text) {
        List<String> list = new ArrayList<>();
        char c;
        Unit unit = null;
        Map<Character, Unit> cur = root;
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if(ignoredSet.contains(c)) {
                cur = root;
                continue;
            }
            unit = cur.get(c);
            if (unit != null) {
                cur = unit.child;
                if (cur == null) {
                    cur = root;
                }
                if (unit.keyword != null) {
                    list.add(unit.keyword);
                }
            } else {
                cur = root;
            }
        }
        return list;
    }

    public static void main(String[] args) {
//        String[] lexicon = {"二批商","促销","促销员","二维码"};
        String[] lexicon = {"二批商", "经销商", "促销", "促销员","促销人", "二维码", "经销门店", "门店", "门店二维码"};
        TrieCache cache = new TrieCache();
        for (int i = 0; i < lexicon.length; i++) {
            cache.put(lexicon[i]);
        }
        String test = "我们行业很缺少促销员，\r所以怎样呢？促销人员，经销\r\n门店可以使用吗？有没有二维码";
        List<String> list = cache.match(test);
        for (String keyword : list) {
            System.out.println("find:" + keyword);
        }
        //remove test
        cache.remove("促销人");
        cache.remove("促销员");
        System.out.println("------------------");
        list = cache.match(test);
        for (String keyword : list) {
            System.out.println("find:" + keyword);
        }

        System.out.println("end");
    }

    private static class Unit {
        String keyword;
        Character c;
        Map<Character, Unit> child;

        public Unit(Character c, Map<Character, Unit> child) {
            this.keyword = null;
            this.c = c;
            this.child = child;
        }
    }
}
