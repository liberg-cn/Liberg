package cn.liberg.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBVersionManager {
    Log logger = LogFactory.getLog(getClass());

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
                if (oldVersion == -1) {
                    dbVer.saveVersion(dbName, currentVersion, false);
                    creator.createTable(stat);
                    initDataList.add(creator);
                } else {
                    if (oldVersion < currentVersion) {
                        int version = creator.upgrade(stat, oldVersion, currentVersion);
                        if (version >= currentVersion) {
                            dbVer.saveVersion(dbName, version, true);
                            logger.debug("upgrade db: " + creator.getName() + ", to version: "+version);
                        } else {
                            logger.warn("upgrade db:" + creator.getName() + " error, version=" + version + ", expectedVersion=" + currentVersion);
                        }
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
                if (stat != null) {
                    stat.close();
                }
            } catch (SQLException e) {
                logger.error(e);
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
