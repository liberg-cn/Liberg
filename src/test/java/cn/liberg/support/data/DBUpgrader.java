package cn.liberg.support.data;

import cn.liberg.database.IDataBaseConf;
import cn.liberg.database.TableAlteration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Statement;

public class DBUpgrader extends DBImpl {
    private static final Logger logger = LoggerFactory.getLogger(DBUpgrader.class);


    public DBUpgrader(IDataBaseConf dbConf) {
        super(dbConf);
    }

    @Override
    public int upgrade(Statement stat, int dbVersion, int newVersion) {
        Class clazz = this.getClass();
        int version = dbVersion;
        try {
            while(version<newVersion) {
                version++;
                Method method = clazz.getDeclaredMethod("upgradeTo" + version);
                if(method != null) {
                    method.invoke(this, stat);
                }
            }
        } catch (Exception e) {
            version--;
            logger.error("DBUpgrader failed:" + super.getName() +
                    ". version=" + version + ", expectedVersion=" + newVersion, e);
        }
        return version;
    }

    private TableAlteration alter(String tableName) {
        return new TableAlteration(tableName);
    }

}