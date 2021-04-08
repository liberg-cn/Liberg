package cn.liberg.database.query;

import cn.liberg.core.OperatorException;
import cn.liberg.database.join.JoinQuery;
import cn.liberg.support.data.dao.RoleDao;
import cn.liberg.support.data.dao.UserDao;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JoinQueryTest {

    public static JoinQuery testJoinQuery() throws OperatorException {
        UserDao userDao = UserDao.self();
        RoleDao roleDao = RoleDao.self();
        JoinQuery jq = JoinQuery.basedOn(userDao)
                .innerJoin(roleDao).eq(userDao.columnRoleId, roleDao.columnId)
                .where(userDao).eq(userDao.columnName, "张三")
                .asc(userDao.columnId).limit(10);
        System.out.println(jq.build());
        assertEquals(jq.build(), "from user a inner join role b on a._role_id=b.id where a._name='张三' order by a.id limit 10");
        return jq;
    }

    @Test
    public void testBuild() throws OperatorException {
        testJoinQuery();
    }

}
