/*
 * First Created by LibergCoder@1.2.0
 */
package cn.liberg.support.data.dao;

import cn.liberg.core.OperatorException;
import cn.liberg.core.StatusCode;
import cn.liberg.database.BaseDao;
import cn.liberg.database.query.*;
import cn.liberg.support.data.entity.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao<User> {
    private static volatile UserDao selfInstance;
    public static final StringColumn columnName = new StringColumn("_name");
    public static final StringColumn columnPassword = new StringColumn("_password");
    public static final LongColumn columnRoleId = new LongColumn("_role_id");
    public static final LongColumn columnCreateTime = new LongColumn("_create_time");

    public static PreparedQueryBuilder getByName;

    private UserDao() {
        super("user");
        getByName = buildQuery().eq(columnName);
        init();
    }

    //推荐写法：使用PreparedStatement，稍麻烦，但性能高。
    public User getByName(String name) throws OperatorException {
        try(PreparedQuery prepared = prepare(getByName)) {
            prepared.set(columnName, name);
            User user = prepared.one();
            if(user != null) {
                user.role = RoleDao.self().getById(user.roleId);
            }
            return user;
        } catch (Exception e) {
            throw new OperatorException(StatusCode.ERROR_DB, e);
        }
    }

    //另一种写法，写起来方便些，但性能相对差
    public User getByName2(String name) throws OperatorException {
        User user = Query.of(this).eq(columnName, name).one();
        if(user != null) {
            user.role = RoleDao.self().getById(user.roleId);
        }
        return user;
    }


    private void init() {
    }

    @Override
    public void setEntityId(User entity, long id) {
        entity.id = id;
    }

    @Override
    public long getEntityId(User entity) {
        return entity.id;
    }

    @Override
    public User buildEntity(ResultSet rs) throws SQLException {
        User entity = new User();
        entity.id = rs.getLong(1);
        entity.name = rs.getString(2);
        entity.password = rs.getString(3);
        entity.roleId = rs.getLong(4);
        entity.createTime = rs.getLong(5);
        return entity;
    }

    @Override
    protected void fillPreparedStatement(User entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.name);
        ps.setString(2, entity.password);
        ps.setLong(3, entity.roleId);
        ps.setLong(4, entity.createTime);
    }

    @Override
    public List<Column> getColumns() {
        if(columns == null) {
            columns = new ArrayList<>(8);
            columns.add(columnName);
            columns.add(columnPassword);
            columns.add(columnRoleId);
            columns.add(columnCreateTime);
        }
        return columns;
    }

    public static UserDao self() {
		if (selfInstance == null) {
		    synchronized (UserDao.class) {
                if (selfInstance == null) {
                    selfInstance = new UserDao();
                }
            }
		}
		return selfInstance;
    }

}