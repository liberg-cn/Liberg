package cn.liberg.core;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 带前缀的唯一标识生成器
 * 可用于生成uid、token
 *
 * @author Liberg
 */
public class IdMaker {
    private String prefix = "";
    private Random rand = new Random();
    private AtomicLong sn = new AtomicLong(0);

    public void setPrefix(String newPrefix) {
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
