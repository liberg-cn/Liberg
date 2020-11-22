package cn.liberg.database;


import cn.liberg.core.OperatorException;
import cn.liberg.core.PeriodicThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Liberg
 *
 * 异步sql/save/update的执行线程，
 * 默认每3毫秒执行一次批量写数据库。
 */
public class AsyncSqlExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncSqlExecutor.class);

    private static int BATCH_SIZE = 300;
    private static int INTERVAL_MILLIS = 3;
    private static int EXECUTE_SAVE = 1;
    private static int EXECUTE_UPDATE = 2;
    private static int EXECUTE_SQL = 4;

    private final LinkedList<OpInfo> opList;
    private DBHelper dbHelper;
    private PeriodicThread periodicThread;

    public AsyncSqlExecutor(DBHelper dbController) {
        opList = new LinkedList<>();
        dbHelper = dbController;

        final List<OpInfo> infos = new ArrayList<>();
        final List<String> sqls = new ArrayList<>();

        periodicThread = new PeriodicThread("AsyncSqlExecutor", () -> {
            synchronized (opList) {
                while (opList.size() > 0 && infos.size() < BATCH_SIZE) {
                    infos.add(opList.removeFirst());
                }
            }
            if (infos.size() <= 0) {
                try {
                    Thread.sleep(INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    logger.warn("AsyncSqlExecutor sleep interrupted...");
                }
            } else {
                String sql;
                try {
                    for (OpInfo info : infos) {
                        if (info.type == EXECUTE_SAVE) {
                            sql = DBHelper.buildSaveSql(info.target, info.dao);
                        } else if (info.type == EXECUTE_UPDATE) {
                            sql = DBHelper.buildUpdateSql(info.target, info.dao);
                        } else {
                            sql = (String) info.target;
                        }
                        if(sql != null) {
                            sqls.add(sql);
                        }
                    }
                    dbHelper.executeSqlBatch(sqls);
                } catch (OperatorException e) {
                    logger.error(e.getMessage(), e);
                }
                sqls.clear();
                infos.clear();
            }
        }, INTERVAL_MILLIS);
    }

    public void start() {
        periodicThread.start();
    }

    public void save(BaseDao dao, Object entity) {
        OpInfo info = new OpInfo(EXECUTE_SAVE, dao, entity);
        synchronized (opList) {
            opList.add(info);
        }
    }

    public void update(BaseDao dao, Object entity) {
        OpInfo info = new OpInfo(EXECUTE_UPDATE, dao, entity);
        synchronized (opList) {
            opList.add(info);
        }
    }

    public void executeSql(String sql) {
        final OpInfo info = new OpInfo(EXECUTE_SQL, null, sql);
        synchronized (opList) {
            opList.add(info);
        }
    }

    private static class OpInfo {
        private int type;
        private Object target;
        private BaseDao dao;

        public OpInfo(int type, BaseDao dao, Object target) {
            this.type = type;
            this.target = target;
            this.dao = dao;
        }
    }

    public int getWaitCount() {
        int count;
        synchronized (opList) {
            count = opList.size();
        }
        return count;
    }
}

