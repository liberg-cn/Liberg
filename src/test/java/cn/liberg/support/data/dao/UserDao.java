package cn.liberg.support.data.dao;

import cn.liberg.core.*;
import cn.liberg.database.BaseDao;
import cn.liberg.database.SqlDefender;
import cn.liberg.database.select.PreparedSelectExecutor;
import cn.liberg.database.select.PreparedSelectWhere;
import cn.liberg.database.select.SelectWhere;
import cn.liberg.support.data.entity.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 实际开发中，XxxDao类由LibergCoder IDEA插件进行创建和维护，
 * 开发人员只需要新增具体的业务方法。
 */
public class UserDao extends BaseDao<User> {
    private static volatile UserDao selfInstance;

    public static final String TABLE_NAME = "user";
    public static final Column<String> columnName = new StringColumn("name", "n");
    public static final Column<String> columnPassword = new StringColumn("password", "p");
    public static final Column<Byte> columnAge = new StringColumn("age", "a");
    public static final Column<Long> columnRoleId = new LongColumn("roleId",  "ri");
    public static final Column<Long> columnCreateTime = new LongColumn("createTime", "ct");

    private UserDao() {
        super(TABLE_NAME);
        init();
    }

    /**
     * 演示返回数据给客户端之前进行{@link cn.liberg.annotation.dbmap#isIndex}
     * {@code @dbmap(isIndex=false}字段的填充，
     * 或者进行其他额外处理。
     *
     * @param entity
     * @return
     * @throws OperatorException
     */
    @Override
    public User fillData(User entity) throws OperatorException {
        if(entity != null && entity.role == null) {
            entity.role = RoleDao.self().getById(entity.roleId);
        }
        return entity;
    }

    /**
     * update演示
     * @throws OperatorException
     */
    public void update(long id, String newName, String newPassword, int ageIncrement) throws OperatorException {
        update().set(columnName, newName)
                .set(columnPassword, newPassword)
                .increment(columnAge, ageIncrement)
                .whereEq(columnId, id)
                .execute();
    }

    /**
     * 查询方式1
     *
     * 单一条件、单条记录查询，可以直接调用BaseDao中的getXx系列方法
     */
    public User getByName1_getEq(String name) throws OperatorException {
        final User user = getEq(columnName, name);

        return fillData(user);
    }

    /**
     * 查询方式2
     *
     * prepare方式的条件查询
     */
    public User getByName2_prepareSelect(String name) throws OperatorException {
        User user = null;

        final PreparedSelectWhere<User> prepareSelect = prepareSelect()
                .whereEq$(columnName)
                .asc(columnId)
                .limit(1);

        final PreparedSelectExecutor<User> prepare = prepareSelect.prepare();

        // 可以复用prepare进行多次查询
        prepare.setParameter(columnName, name);
        user = prepare.one();

        // 避免手动调用close，推荐try-with-resources写法
        prepare.close();

        return user;
    }

    /**
     * 查询方式3
     *
     * 普通查询
     */
    public User getByName3_select(String name) throws OperatorException {
return select()
        .whereEq(columnName, name)
        .or()
        .eq(columnPassword, "123")
        .gt(columnAge, 30)
        .one();
    }

    /**
     * 查询方式4
     *
     * String.format构建where条件，然后通过{@link BaseDao#getByWhere}进行查询
     */
    public User getByName4_StringFormat(String name) throws OperatorException {
        String where = "%1$s=%2$s and %3$s>%4$s";
        where = String.format(where, columnName.name, SqlDefender.format(name), columnId.name, 0);
        return getOneByWhere(where);
    }

    /**
     * 只查询某一列
     */
    public List<String> getUserNameList(String name) throws OperatorException {
        // whereGt(columnId, 0)，即id>0这个条件当然是没必要的，这里只是为了演示
        final List<String> list = select(columnName)
                .whereGt(columnId, 0)
                .eq(columnName, name)
                .asc(columnId)
                .all();
        return list;
    }

    /**
     * 只查询一条记录的某一列
     */
    public String getUserName(String name) throws OperatorException {
        return select(columnName)
                .whereEq(columnName, name)
                .asc(columnId)
                .one();
    }

    /**
     * 查询某些列
     *
     * 本例演示查询name和password两列
     */
    public Segment<User> getUserSegment(String name) throws OperatorException {
        final SelectWhere<Segment<User>> selectWhere = select(columnName, columnPassword)
                .whereEq(columnName, name);
        return selectWhere.one();
    }

    /**
     * 查询某些列
     *
     * 本例演示查询name和password两列
     */
    public Segment<User> getUserSegment_Prepared(String name) throws OperatorException {
        final PreparedSelectWhere<Segment<User>> preparedSelectWhere = prepareSelect(columnName, columnPassword).whereEq$(columnName);
        final PreparedSelectExecutor<Segment<User>> prepare = preparedSelectWhere.prepare();
        final Segment<User> one = prepare.one();
        prepare.close();

        return one;
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
        entity.age = rs.getByte(4);
        entity.roleId = rs.getLong(5);
        entity.createTime = rs.getLong(6);
        return entity;
    }

    @Override
    protected void fillPreparedStatement(User entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.name);
        ps.setString(2, entity.password);
        ps.setByte(3, entity.age);
        ps.setLong(4, entity.roleId);
        ps.setLong(5, entity.createTime);
    }

    @Override
    public List<Column> getColumns() {
        if(columns == null) {
            columns = new ArrayList<>(8);
            columns.add(columnName);
            columns.add(columnPassword);
            columns.add(columnAge);
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