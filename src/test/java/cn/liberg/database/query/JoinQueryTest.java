package cn.liberg.database.query;

import cn.liberg.support.data.dao.RoleDao;
import cn.liberg.support.data.dao.UserDao;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class JoinQueryTest {

    @Test
    public void testJoinQuery() {
        UserDao d1 = UserDao.self();
        RoleDao d2 = RoleDao.self();
        JoinQuery jq = JoinQuery.basedOn(d1)
                .innerJoin(d2).eq(d1.columnRoleId, d2.columnId)
                .where(d1).eq(d1.columnName, "张三")
                .asc(d1.columnId).limit(10);
        assertEquals(jq.build(), "from user a inner join role b on a._role_id=b._id where a._name='张三' order by a._id limit 10");
    }

}
