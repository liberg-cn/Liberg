package cn.liberg.session;

import cn.liberg.core.IdMaker;

/**
 * @author Liberg
 */
public class SessionItem {
    public String uid;
    public long startTimeMillis;

    private AbstractSession session;
    private static IdMaker idMaker = new IdMaker();

    public SessionItem() {
        this.uid = idMaker.nextUid();
        this.startTimeMillis = System.currentTimeMillis();
    }

    public SessionItem(AbstractSession session) {
        this();
        session.uid = this.uid;
        this.session = session;
    }

    public <T extends AbstractSession> T getSession() {
        return (T)session;
    }
}
