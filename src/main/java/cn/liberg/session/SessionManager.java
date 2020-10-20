package cn.liberg.session;

import cn.liberg.cache.LRUCache;
import cn.liberg.core.PeriodicThread;

import java.util.Iterator;
import java.util.Map;

public class SessionManager {
    private static SessionManager self = null;
    /* 15天后session自动过期，需要重新登录 */
    private static long EXPIRED_MILLIS = 15 * 24 * 60 * 60 * 1000L;
    /* session过期前10分钟内，告诉前端session不可用，需要重新登录 */
    private static long RESERVE_MILLIS = 10 * 60 * 1000L;
    private static long USABLE_DURATION = EXPIRED_MILLIS - RESERVE_MILLIS;
    /* 每10分钟清理一次过期的session */
    private static int EVICT_INTERVAL_MILLIS = 10 * 60 * 1000;

    private final LRUCache<String, SessionItem> cache;
    private final byte[] lock = new byte[0];
    private int maxCount = 0;
    private PeriodicThread pThread;

    public static void resetParams(long expiredMillis, long reserveMillis, int evictIntervalMillis) {
        EXPIRED_MILLIS = expiredMillis;
        RESERVE_MILLIS = reserveMillis;
        USABLE_DURATION = expiredMillis - reserveMillis;
        EVICT_INTERVAL_MILLIS = evictIntervalMillis;
    }

    public SessionManager() {
        cache = new LRUCache<>(Integer.MAX_VALUE >> 1);
        pThread = new PeriodicThread("AbstractSessionManagerThread", () -> {
            Map.Entry<String, SessionItem> entry;
            SessionItem session;
            long nowTime = System.currentTimeMillis();
            synchronized (lock) {
                final Iterator<Map.Entry<String, SessionItem>> iterator = cache.entrySet().iterator();
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    session = entry.getValue();
                    session.uid = null;
                    if (nowTime - session.startTimeMillis >= EXPIRED_MILLIS) {
                        iterator.remove();
                    } else {
                        break;
                    }
                }
            }
        }, EVICT_INTERVAL_MILLIS);
        pThread.start();
    }

    public static SessionManager self() {
        if (self == null) {
            self = getInstanceSingle();
        }
        return self;
    }

    private static synchronized SessionManager getInstanceSingle() {
        if (self == null) {
            self = new SessionManager();
        }
        return self;
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
        return item != null && (System.currentTimeMillis() - item.startTimeMillis < USABLE_DURATION);
    }
}
