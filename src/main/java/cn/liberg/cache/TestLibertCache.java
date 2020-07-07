package cn.liberg.cache;

public class TestLibertCache {
    Object obj = new Object();
    final int SIZE = 100000;
    final LibertCache<Object> cache = new LibertCache<>(SIZE);
//    final LimitHashtable<String, Object> cache = new LimitHashtable<>(SIZE);

    public static void main(String[] args) {
        TestLibertCache test = new TestLibertCache();
        //hitAll
        long t1 = System.currentTimeMillis();
        for(int i=0;i<3;i++) {
            test.run(test::hitAll);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("hitAll timeTake=" + (t2 - t1)/3 + "ms");
        //hitNone
        long t3 = System.currentTimeMillis();
        for(int i=0;i<3;i++) {
            test.run(test::hitNone);
        }
        long t4 = System.currentTimeMillis();
        System.out.println("hitAll timeTake=" + (t4 - t3)/3 + "ms");
        //hitHalf
        long t5 = System.currentTimeMillis();
        for(int i=0;i<3;i++) {
            test.run(test::hitHalf);
        }
        long t6 = System.currentTimeMillis();
        System.out.println("hitAll timeTake=" + (t6 - t5)/3 + "ms");

    }

    public void run(Runnable runnable) {
        int loopCount = 20;
        Thread[] threads = new Thread[loopCount];
        for (int i = 0; i < loopCount; i++) {
            threads[i] = new Thread(runnable);
        }
        for (int i = 0; i < loopCount; i++) {
            threads[i].start();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void hitAll() {
        for (int i = 11; i < 200010; i++) {
            cache.put("" + i, obj);
            int j = 10;
            while ((--j) >= 0) {
                cache.get("" + (i - j));
            }
        }
    }
    public void hitNone() {
        for (int i = 11; i < 200010; i++) {
            cache.put("" + i, obj);
            int j = 10;
            while ((--j) >= 0) {
                cache.get("" + (i - j));
            }
        }
    }
    public void hitHalf() {
        for (int i = 11; i < 200010; i++) {
            cache.put("" + i, obj);
            int j = 10;
            while ((--j) >= 0) {
                cache.get("" + (i - j));
            }
        }
    }
}
