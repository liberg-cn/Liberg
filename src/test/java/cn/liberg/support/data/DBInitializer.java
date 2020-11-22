package cn.liberg.support.data;

import cn.liberg.UserDaoTest;
import cn.liberg.core.OperatorException;
import cn.liberg.database.DBHelper;
import cn.liberg.support.data.dao.RoleDao;
import cn.liberg.support.data.dao.UserDao;
import cn.liberg.support.data.entity.Role;
import cn.liberg.support.data.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);

    /**
     * 这个方法由DBImpl自动调用，完成建库、建表之后的数据初始化工作
     */
    public void initData() {
        try {
            initUsers();
        } catch (OperatorException e) {
            logger.error("initData error", e);
        }
    }

    /**
     * 这里演示，如何在Liberg框架自动完成建库、建表之后，
     * 进行一些初始数据的写入
     */
    public void initUsers() throws OperatorException {
        Role role = new Role();
        role.name = "超级管理员";
        role.permissions = "all";

        User zhang = new User();
        zhang.name = "张三";
        zhang.password = UserDaoTest.getMd5WithSalt("123");
        zhang.createTime = System.currentTimeMillis();

        DBHelper.self().beginTransact();
        try {
            long roleId = RoleDao.self().save(role);
            zhang.roleId = roleId;
            UserDao.self().save(zhang);
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
            logger.error("initUsers error", e);
        }
    }
}
