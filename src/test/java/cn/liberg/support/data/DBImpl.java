package cn.liberg.support.data;

import cn.liberg.database.IDataBase;
import cn.liberg.database.IDataBaseConf;
import cn.liberg.database.TableBuilder;
import cn.liberg.support.data.dao.RoleDao;
import cn.liberg.support.data.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

public class DBImpl implements IDataBase {
    private static final Logger logger = LoggerFactory.getLogger(DBImpl.class);

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
            DBInitializer initializer = new DBInitializer();
            initializer.initData();
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
        TableBuilder tb = new TableBuilder(RoleDao.self().getTableName());
        tb.add(RoleDao.columnName, typeString(31));
        tb.add(RoleDao.columnPermissions, typeText());
        stat.executeUpdate(tb.build());
    }

    protected void createTableUser(Statement stat) throws SQLException {
        UserDao dao = UserDao.self();
        TableBuilder tb = new TableBuilder(dao.getTableName());
        tb.add(dao.columnName, true, typeString());
        tb.add(dao.columnPassword, typeString());
        tb.add(dao.columnAge, typeByte());
        tb.add(dao.columnRoleId, typeLong());
        tb.add(dao.columnCreateTime, typeLong());
        stat.executeUpdate(tb.build());
    }

}