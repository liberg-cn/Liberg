package cn.liberg;

import cn.liberg.core.OperatorException;
import cn.liberg.core.Segment;
import cn.liberg.database.*;
import cn.liberg.database.join.JoinQuery;
import cn.liberg.database.join.JoinResult;
import cn.liberg.database.query.JoinQueryTest;
import cn.liberg.support.data.DBConfig;
import cn.liberg.support.data.DBImpl;
import cn.liberg.support.data.dao.UserDao;
import cn.liberg.support.data.entity.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DaoTest {

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
                UserDao.columnName.name+ Condition.EQ+ SqlDefender.format(name));
    }

    // 没有使用事务
    private void doSth() throws OperatorException {
        final int userCount = 3;
        User user = buildTestUser();
        UserDao dao = UserDao.self();
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
    }

    @Test
    public void testJoinQuery() throws OperatorException {
        final JoinQuery joinQuery = JoinQueryTest.testJoinQuery();
        JoinResult datas = joinQuery.all();
        System.out.println(datas);
    }

    @Test
    public void testTransaction() throws OperatorException {
        DBHelper.self().beginTransact();
        try {
            // 使用DBHelper提供的事务接口
            doSth();
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            e.printStackTrace();
        }
    }

    @Test
    public void testTransaction2() throws OperatorException {
        final int userCount = 3;
        User user = buildTestUser();
        UserDao dao = UserDao.self();

        final String result = dao.transaction(() -> {
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
            return "succ";
        });
        System.out.println(result);
    }

    @Test
    public void testTransaction3() throws OperatorException {
        UserDao dao = UserDao.self();
        dao.transaction(this::doSth);
        String result = dao.transaction(()->{doSth();return "11";});
        System.out.println(result);
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

    @Test
    public void testGetAllUsers() throws OperatorException {
        UserDao dao = UserDao.self();
        List<User> users = dao.getGts(dao.columnId, 0);

    }

    @Test
    public void test_SegementQuery() throws OperatorException {
        UserDao dao = UserDao.self();

        Segment<User> userSegment = dao.select(dao.columnId, UserDao.columnName)
                .whereGt(UserDao.columnAge, 0).one();
        System.out.println(userSegment);
        long id = userSegment.get(dao.columnId);
        String name = userSegment.get(UserDao.columnName);
        System.out.println("id="+id);
        System.out.println("name="+name);
    }
}
