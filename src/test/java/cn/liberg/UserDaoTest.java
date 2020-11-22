package cn.liberg;

import cn.liberg.core.OperatorException;
import cn.liberg.core.Segment;
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserDaoTest {
    public static final String PWD_SALT = "$high_performance_liberg$";
    public UserDao userDao = UserDao.self();

    public static String getMd5WithSalt(String source) {
        return DigestUtils.md5DigestAsHex((source + PWD_SALT).getBytes());
    }

    @BeforeClass
    public static void testInit() {
        IDataBaseConf dbConfig = new DBConfig();
        DBHelper.self().init(new DBImpl(dbConfig));
    }


    private static class InsertThread extends Thread {
        final CountDownLatch latch;
        final PreparedStatement statement;
        final int code;

        InsertThread(CountDownLatch latch, PreparedStatement statement, int code) {
            super();
            this.latch = latch;
            this.statement = statement;
            this.code = code;
        }

        @Override
        public void run() {
            try {
                statement.setString(1, "name"+code);
                statement.setLong(2, 1000 + code);
                if(code == 1) {
                    /**
                     * 让其中一个线程休眠200ms
                     * 预期结果是插入的两条记录都是 name2 1002
                     */
                    Thread.currentThread().sleep(200);
                }
                statement.executeUpdate();
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMultiThread() throws OperatorException {
        String sql;
        sql ="INSERT INTO user(_name,_password,_age,_role_id,_create_time) VALUES (?, '', 100, 1, ?)";
        PreparedStatement statement = DBHelper.self().prepareStatement(sql);

        CountDownLatch latch = new CountDownLatch(2);

        Thread t1 = new InsertThread(latch, statement, 1);
        Thread t2 = new InsertThread(latch, statement, 2);
        t1.start();
        t2.start();

        try {
            /**
             * 等待2个线程执行结束
             */
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testAsync() throws Exception {
        long nowMills = System.currentTimeMillis();

        /**
         * 测试异步save
         */
        User user = new User();
        user.name = "Aysnc_"+nowMills;
        user.password = "1";
        user.roleId = 1;
        user.createTime = nowMills;
        user.age = 100;
        userDao.asyncSave(user);
        Thread.sleep(10);
        Thread.sleep(10);
        /**
         * 检查是否保存成功
         */
        User selectedUser = userDao.getByName2_prepareSelect(user.name);
        assertEquals(selectedUser.age, user.age);
        assertEquals(selectedUser.name, user.name);

        /**
         * 异步update
         */
        selectedUser.age = 10;
        selectedUser.name = "AysncUpdated_"+nowMills;
        userDao.asyncUpdate(selectedUser);
        Thread.sleep(10);
        Thread.sleep(10);
        /**
         * 检查是否更新成功
         */
        User updatedUser = userDao.getByName3_select(selectedUser.name);
        assertEquals(updatedUser.age, selectedUser.age);
        assertEquals(updatedUser.name, selectedUser.name);
    }

    @Test
    public void testBasic() throws Exception {
        RandomString randStr = new RandomString(20, 20);

        /**
         * 构造一个随机用户名、密码的user
         */
        final String userName = randStr.next();
        final String password = getMd5WithSalt(randStr.next(8, 16));
        User user = new User();
        user.name = userName;
        user.password = password;
        user.age = 30;
        user.roleId = 1L;
        user.createTime = System.currentTimeMillis();

        /**
         * 保存user到数据库
         */
        userDao.save(user);

        User selectedUser;
        /**
         * 查询方式1
         */
        selectedUser = userDao.getByName1_getEq(userName);
        assertEquals(selectedUser.name, userName);
        /**
         * 查询方式2
         */
        selectedUser = userDao.getByName2_prepareSelect(userName);
        assertEquals(selectedUser.name, userName);
        /**
         * 查询方式3
         */
        selectedUser = userDao.getByName3_select(userName);
        assertEquals(selectedUser.name, userName);
        /**
         * 查询方式4
         */
        selectedUser = userDao.getByName4_StringFormat(userName);
        assertEquals(selectedUser.name, userName);

        /**
         * 只查询user表的用户名一列
         */
        final List<String> userNames = userDao.getUserNameList(userName);
        assertEquals(userNames.get(0), userName);
        /**
         * 只查询user表某条记录的用户名
         */
        final String selectedUserName = userDao.getUserName(userName);
        assertEquals(selectedUserName, userName);
        /**
         * 查询用户名、密码这两列
         */
        final Segment userSegment = userDao.getUserSegment(userName);
        String keyName = UserDao.columnName.shortName;
        String keyPassword = UserDao.columnPassword.shortName;
        assertEquals(userSegment.get(keyName), userName);
        assertEquals(userSegment.get(keyPassword), password);
        System.out.println(userSegment);

        /**
         * 更新用户名、密码，年龄减10岁
         */
        long id = selectedUser.id;
        String newMd5Password = getMd5WithSalt("12345");
        String newName = "王五"+selectedUser.name;
        int age = selectedUser.age;
        int ageIncrement = -10;
        userDao.update(id, newName, newMd5Password, ageIncrement);

        User updatedUser = userDao.getEq(userDao.columnId, id);
        assertEquals(updatedUser.name, newName);
        assertEquals(updatedUser.password, newMd5Password);
        assertEquals(updatedUser.age, age + ageIncrement);
    }

}