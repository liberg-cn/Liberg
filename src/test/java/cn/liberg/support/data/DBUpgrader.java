package cn.liberg.support.data;

import cn.liberg.database.IDataBaseConf;
import cn.liberg.database.TableAlteration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUpgrader extends DBImpl {
    private static final Logger logger = LoggerFactory.getLogger(DBUpgrader.class);

    public DBUpgrader(IDataBaseConf dbConf) {
        super(dbConf);
    }

    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException {
        Class<? extends DBUpgrader> clazz = this.getClass();
        String clazzName = clazz.getSimpleName();
        int result = dbVersion;
        for (int i = dbVersion + 1; i <= newVersion; i++) {
            try {
                Method method = clazz.getDeclaredMethod("upgradeTo" + i, Statement.class);
                method.invoke(this, stat);
                result = i;
            } catch(NoSuchMethodException e) {
                //skip
                logger.warn(clazz.getSimpleName()+" upgradeTo" + i + ": no such method.");
            } catch (Exception e) {
                logger.error(clazzName+" failed:" + super.getName() +
                        ". version=" + result + ", expectedVersion=" + newVersion, e);
                break;
            }
        }
        return result;
    }

    private TableAlteration alter(String tableName) {
        return new TableAlteration(tableName);
    }

}