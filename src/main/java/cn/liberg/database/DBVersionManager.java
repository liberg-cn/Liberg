package cn.liberg.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 完成数据库的创建、版本升级、数据初始化；维护数据库的版本。
 *
 * 框架自动维护一张名为"db_version"的表，
 * 记录各个数据库的版本
 * 不过，常见情况是一个Web项目只对应一个数据库
 *
 * @author Liberg
 */
public class DBVersionManager {
    private static final Logger logger = LoggerFactory.getLogger(DBVersionManager.class);
    private ArrayList<IDataBase> dbCreatorList;
    private DBConnector dbConnector;


    public DBVersionManager(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
        dbCreatorList = new ArrayList<>();
    }

    public void addDatabase(IDataBase creator) {
        dbCreatorList.add(creator);
    }

    public void dbInit() {
        Connection conn = null;
        Statement stat = null;
        List<IDataBase> initDataList = new ArrayList<>();
        try {
            conn = dbConnector.getConnect();
            conn.setAutoCommit(false);
            stat = conn.createStatement();
            DBVersion dbVer = new DBVersion(stat);
            for (IDataBase creator : dbCreatorList) {
                int currentVersion = creator.getCurrentVersion();
                String dbName = creator.getName();
                int oldVersion = DBVersion.getVersion(dbName);
                logger.info("connected to DB: {}, dbVer: {}", creator.getName(), oldVersion);
                if (oldVersion == -1) {
                    dbVer.saveVersion(dbName, currentVersion, false);
                    creator.createTable(stat);
                    initDataList.add(creator);
                } else {
                    if (oldVersion < currentVersion) {
                        int version = creator.upgrade(stat, oldVersion, currentVersion);
                        if (version >= currentVersion) {
                            dbVer.saveVersion(dbName, version, true);
                            logger.info("upgrade version, db: {}, from: {} to {}",
                                    creator.getName(), oldVersion, version);
                        } else {
                            logger.warn("upgrade error, db: {}, version: {}, expectedVersion: {}, oldVersion: {}",
                                    creator.getName(), version, currentVersion, oldVersion);
                        }
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("dbInit", e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
                if (stat != null) {
                    stat.close();
                }
            } catch (SQLException e) {
                logger.error("dbInit:finally", e);
            }
            if (conn != null) {
                dbConnector.freeConnection(conn, false);
            }
        }

        initData(initDataList);
    }

    private void initData(List<IDataBase> dbs) {
        for(IDataBase db : dbs) {
            db.initData();
        }
    }
}
