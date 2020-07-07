/*
 * First Created by LibergCoder@1.2.0
 */
package cn.liberg;

import cn.liberg.core.OperatorException;
import cn.liberg.database.DBHelper;
import cn.liberg.database.IDataBaseConf;
import cn.liberg.support.DigestUtils;
import cn.liberg.support.RandomString;
import cn.liberg.support.data.DBConfig;
import cn.liberg.support.data.DBImpl;
import cn.liberg.support.data.dao.UserDao;
import cn.liberg.support.data.entity.User;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserServiceTest {
    public static final String PWD_SALT = "$high_performance_liberg$";

    public static String getMd5WithSalt(String source) {
        return DigestUtils.md5DigestAsHex((source+PWD_SALT).getBytes());
    }

    @BeforeClass
    public static void testInit() {
        IDataBaseConf dbConfig = new DBConfig();
        DBHelper.self().init(new DBImpl(dbConfig));
    }

    @Test
    public void test01() throws OperatorException {
        RandomString randStr = new RandomString(20, 20);
        String userName = randStr.next();
        String password = randStr.next(8,16);
        User user = new User();
        user.name = userName;
        user.password = getMd5WithSalt(password);
        user.createTime = System.currentTimeMillis();
        UserDao.self().save(user);

        final User loginUser = UserDao.self().getByName(userName);
        assertNotNull(loginUser);

        String md5Password = getMd5WithSalt(password);
        assertEquals(md5Password, loginUser.password);
    }

}