package cn.liberg.support.data;

import cn.liberg.database.IDataBase;
import cn.liberg.database.IDataBaseConf;
import cn.liberg.database.TableBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.SQLException;
import java.sql.Statement;

public class DBImpl implements IDataBase {
    private Log logger = LogFactory.getLog(getClass());
    protected final IDataBaseConf dbConf;
    protected final int dbVersion = 1;
    protected final String dbName;

    public DBImpl(IDataBaseConf dbConf) {
        this.dbConf = dbConf;
        dbName = dbConf.getDbName();
    }

    @Override
    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException {
        DBUpgrader dbUpgrader = new DBUpgrader(dbConf);
        return dbUpgrader.upgrade(stat, dbVersion, newVersion);
    }

    @Override
    public void initData() {
        try {
            DBInitializer initor = new DBInitializer();
            initor.initData();
        } catch (Exception e) {
            logger.error("initData failed...", e);
        }
    }

    @Override
    public String getName() {
        return dbName;
    }

    @Override
    public int getCurrentVersion() {
        return dbVersion;
    }

    @Override
    public IDataBaseConf getConfig() {
        return dbConf;
    }

    @Override
    public void createTable(Statement stat) throws SQLException {
        createTableRole(stat);
        createTableUser(stat);
    }

    protected void createTableRole(Statement stat) throws SQLException {
        TableBuilder tb = new TableBuilder("role");
        tb.add("_name", typeString(31), null);
        tb.add("_permissions", typeText(), null);
        stat.executeUpdate(tb.build());
    }

    protected void createTableUser(Statement stat) throws SQLException {
        TableBuilder tb = new TableBuilder("user");
        tb.add("_name", true, typeString(), null);
        tb.add("_password", typeString(), null);
        tb.add("_role_id", typeLong(), null);
        tb.add("_create_time", typeLong(), null);
        stat.executeUpdate(tb.build());
    }

}