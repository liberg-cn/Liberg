package cn.liberg.session;

import cn.liberg.cache.LRUCache;
import cn.liberg.core.PeriodicThread;

import java.util.Iterator;
import java.util.Map;

/**
 * 会话管理器
 *
 * @author Liberg
 */
public class SessionManager {
    private static volatile SessionManager selfInstance = null;
    /**
     *  默认24小时后session自动过期，需要重新登录
     */
    private long expiredMillis = 24 * 60 * 60 * 1000L;
    /**
     *  session过期前10分钟内，设置为不再可用，需要重新登录
     */
    private long reserveMillis = 10 * 60 * 1000L;
    private long usableDuration = expiredMillis - reserveMillis;
    /**
     *  每5分钟清理一次过期的session
     */
    private int evictIntervalMillis = 5 * 60 * 1000;

    private final byte[] lock = new byte[0];
    private final LRUCache<String, SessionItem> cache;
    private final PeriodicThread pThread;
    private int maxCount = 0;

    public void setMillis(long expired, long reserve, int evictInterval) {
        expiredMillis = expired;
        reserveMillis = reserve;
        usableDuration = expired - reserve;
        evictIntervalMillis = evictInterval;
    }

    public SessionManager() {
        cache = new LRUCache<>(Integer.MAX_VALUE >> 1);
        pThread = new PeriodicThread("SessionManagerThread", () -> {
            Map.Entry<String, SessionItem> entry;
            SessionItem session;
            long nowTime = System.currentTimeMillis();
            synchronized (lock) {
                final Iterator<Map.Entry<String, SessionItem>> iterator = cache.entrySet().iterator();
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    session = entry.getValue();
                    session.uid = null;
                    if (nowTime - session.startTimeMillis >= expiredMillis) {
                        iterator.remove();
                    } else {
                        break;
                    }
                }
            }
        }, evictIntervalMillis);
        pThread.start();
    }

    public static SessionManager self() {
        if (selfInstance == null) {
            synchronized (SessionManager.class) {
                if (selfInstance == null) {
                    selfInstance = new SessionManager();
                }
            }
        }
        return selfInstance;
    }

    public void put(SessionItem session) {
        synchronized (lock) {
            cache.put(session.uid, session);
            if (cache.size() > maxCount) {
                maxCount = cache.size();
            }
        }
    }

    public void remove(String uid) {
        SessionItem session;
        synchronized (lock) {
            session = cache.remove(uid);
        }
        if (session != null) {
            session.uid = null;
        }
    }

    public int getCount() {
        return cache.size();
    }

    public int getMaxCount() {
        return maxCount;
    }

    public SessionItem get(String uid) {
        SessionItem session;
        synchronized (lock) {
            session = cache.get(uid);
        }
        return session;
    }

    public void renew(String uid) {
        SessionItem session = cache.get(uid);
        if (session != null) {
            session.startTimeMillis = System.currentTimeMillis();
        }
    }

    public boolean isUsable(String uid) {
        SessionItem item = get(uid);
        return item != null
                && (System.currentTimeMillis() - item.startTimeMillis < usableDuration);
    }
}
