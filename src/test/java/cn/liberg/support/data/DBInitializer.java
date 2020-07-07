package cn.liberg.support.data;

import cn.liberg.UserServiceTest;
import cn.liberg.core.OperatorException;
import cn.liberg.database.DBHelper;
import cn.liberg.support.data.dao.RoleDao;
import cn.liberg.support.data.dao.UserDao;
import cn.liberg.support.data.entity.Role;
import cn.liberg.support.data.entity.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBInitializer {
    public static final Log logger = LogFactory.getLog(DBInitializer.class);

    public void initData() {
        try {
            initUsers();
        } catch (OperatorException e) {
            logger.error("initData error", e);
        }
    }

    public void initUsers() throws OperatorException {
        Role role = new Role();
        role.name = "超级管理员";
        role.permissions = "all";

        User zhangsan = new User();
        zhangsan.name = "张三";
        zhangsan.password = UserServiceTest.getMd5WithSalt("123");
        zhangsan.createTime = System.currentTimeMillis();

        User lisi = new User();
        lisi.name = "李四";
        lisi.password = UserServiceTest.getMd5WithSalt("1234");
        lisi.createTime = System.currentTimeMillis();

        DBHelper.self().beginTransact();
        try {
            long roleId = RoleDao.self().save(role);
            zhangsan.roleId = roleId;
            lisi.roleId = roleId;
            UserDao.self().save(zhangsan);
            UserDao.self().save(lisi);
            DBHelper.self().endTransact();
        } catch (Exception e) {
            DBHelper.self().transactRollback();
        }
    }
}
