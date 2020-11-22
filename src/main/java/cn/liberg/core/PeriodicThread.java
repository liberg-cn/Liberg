package cn.liberg.core;

/**
 * 对线程的简单封装，按照某个时间间隔{@code intervalMillis}，
 * 周期性地执行任务{@code runnable}
 *
 * @author Liberg
 */
public class PeriodicThread {
    protected Thread thread;
    private boolean running = false;
    private final Runnable runnable;
    private int intervalMillis;
    private String name;

    /**
     * @param name 为线程指定一个有区分度的名字
     * @param runnable 周期性执行的runnable
     * @param intervalMillis 调度间隔ms时间
     */
    public PeriodicThread(String name, Runnable runnable, int intervalMillis) {
        this.runnable = runnable;
        this.intervalMillis = intervalMillis;
        this.name = name;
        thread = initThread(name);
    }

    public void setIntervalMillis(int intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public void start() {
        if(thread == null) {
            throw new RuntimeException("PeriodicThread["+name+"] has been destroyed.");
        }
        running = true;
        thread.start();
    }

    public void destroy() {
        running = false;
        thread = null;
    }

    private Thread initThread(String name) {
        return new Thread(()->{
            while (running) {
                runnable.run();
                try {
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) { }
            }
        }, name);
    }
}
