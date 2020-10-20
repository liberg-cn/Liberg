package cn.liberg.core;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class IdMaker {
    private static String prefix = "";
    private static Random rand = new Random();
    private AtomicLong sn = new AtomicLong(0);

    public static void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }

    public IdMaker() {
    }

    public IdMaker(String prefix) {
        this.prefix = prefix;
    }

    public String nextTempId() {
        return System.currentTimeMillis() + "_" + rand.nextInt(100000);
    }

    public String nextUid() {
        return prefix + nextTempId() + "_" + sn.incrementAndGet();
    }
}
