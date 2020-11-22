package cn.liberg.database;

import cn.liberg.core.OperatorException;
import cn.liberg.support.data.DBConfig;
import cn.liberg.support.data.DBImpl;
import cn.liberg.support.data.dao.UserDao;
import cn.liberg.support.data.entity.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DBHelperTest {

    @Before
    public void testInit() {
        IDataBaseConf dbConfig = new DBConfig();
        DBHelper.self().init(new DBImpl(dbConfig));
    }

    private User buildTestUser() {
        User user = new User();
        user.createTime = System.currentTimeMillis();
        user.name = "";
        user.password = "1";
        user.roleId = 1;
        return user;
    }

    private void deleteUserByName(String name) throws OperatorException {
        DBHelper.self().delete(UserDao.self(),
                UserDao.columnName.name+Condition.EQ+SqlDefender.format(name));
    }

    @Test
    public void testTransaction() throws OperatorException {
        final int userCount = 3;
        User user = buildTestUser();
        UserDao dao = UserDao.self();

        DBHelper.self().beginTransact();
        try {
            String userName;
            for (int i = 0; i < userCount; i++) {
                userName = "Transact" + i;
                // 先删除，如果存在的话
                deleteUserByName(userName);

                user.age = (byte) i;
                user.name = userName;
                // 再添加
                dao.save(user);
            }
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            e.printStackTrace();
        }
    }

    @Test
    public void test_saveOrUpdateBatch() throws OperatorException {
        final int userCount = 10;
        String userName;
        UserDao dao = UserDao.self();
        List<User> list = new ArrayList<>(userCount);
        for (int i = 0; i < userCount; i++) {
            userName = "saveOrUpdateBatch_" + i;
            User user = dao.getByName1_getEq(userName);
            if(user == null) {
                user = buildTestUser();
                user.name = userName;
            } else {
                user.password = "newPassword";
            }
            list.add(user);
        }

        DBHelper.self().saveOrUpdateBatch(UserDao.self(), list);
    }
}
