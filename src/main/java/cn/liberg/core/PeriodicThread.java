package cn.liberg.core;

public class PeriodicThread {
    protected Thread thread;
    private boolean running = false;
    private final Runnable runnable;
    private final int intervalMillis;
    private String name;

    public PeriodicThread(String name, Runnable runnable, int intervalMillis) {
        this.runnable = runnable;
        this.intervalMillis = intervalMillis;
        this.name = name;
        thread = initThread(name);
    }

    public void start() {
        if(thread == null) {
            throw new RuntimeException("PeriodicThread["+name+"] has been destroyed.");
        }
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public void destroy() {
        stop();
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
