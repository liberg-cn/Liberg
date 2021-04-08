package cn.liberg;

import cn.liberg.database.DBHelper;
import cn.liberg.database.IDataBaseConf;
import cn.liberg.database.query.JoinQueryTest;
import cn.liberg.support.data.DBConfig;
import cn.liberg.support.data.DBImpl;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({UserDaoTest.class, DaoTest.class})
public class MainTest {

    @BeforeClass
    public static void testInit() {
        IDataBaseConf dbConfig = new DBConfig();
        DBHelper.self().init(new DBImpl(dbConfig));
    }


}
