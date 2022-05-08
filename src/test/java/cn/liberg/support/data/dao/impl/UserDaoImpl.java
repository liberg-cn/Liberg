package cn.liberg.support.data.dao.impl;

import cn.liberg.core.*;
import cn.liberg.database.BaseDao;
import cn.liberg.support.data.entity.Role;
import cn.liberg.support.data.entity.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本文件由LibergCoder代码插件自动生成，请勿手动修改！！
 */
public class UserDaoImpl extends BaseDao<User> {
    public static final String TABLE_NAME = "user";
    public static final CachedColumn<User, Long> columnId = new CachedIdColumn<User>(TenThousands.X1) {
        @Override
        public void set(User entity, Long value) {
            entity.id = value;
        }
        @Override
        public Long get(User entity) {
            return entity.id;
        }
    };
    public static final CachedStringColumn<User> columnName = new CachedStringColumn<User>("name", "n", 4){
        @Override
        public String get(User entity) {
            return entity.name;
        }
        @Override
        public void set(User entity, String value) {
            entity.name = value;
        }
    };
    public static final Column<User, String> columnPassword = new StringColumn<User>("password", "p") {

        @Override
        public void set(User entity, String value) {
            entity.password = value;
        }

        @Override
        public String get(User entity) {
            return entity.password;
        }
    };
    public static final Column<User, Byte> columnAge = new ByteColumn<User>("age", "a"){

        @Override
        public void set(User entity, Byte value) {
            entity.age = value;
        }

        @Override
        public Byte get(User entity) {
            return entity.age;
        }
    };
    public static final Column<User, Long> columnRoleId = new LongColumn<User>("roleId",  "ri"){

        @Override
        public void set(User entity, Long value) {
            entity.roleId = value;
        }
        @Override
        public Long get(User entity) {
            return entity.roleId;
        }
    };
    public static final CachedLongColumn<User> columnCreateTime = new CachedLongColumn<User>("createTime", "ct", 0){

        @Override
        public Long get(User entity) {
            return entity.createTime;
        }

        @Override
        public void set(User entity, Long value) {
            entity.createTime = value;
        }
    };

    public static final CachedColumnPair<User, String, Long> $name$createTime = new CachedColumnPair(columnName, columnCreateTime, 6);

    protected UserDaoImpl() {
        super(TABLE_NAME, Arrays.asList(
                columnId,
                columnName,
                $name$createTime
        ));
    }

    @Override
    public Class<User> getEntityClazz() {
        return User.class;
    }

    @Override
    public final User buildEntity(ResultSet rs) throws SQLException {
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
    protected final void fillPreparedStatement(User entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.name);
        ps.setString(2, entity.password);
        ps.setByte(3, entity.age);
        ps.setLong(4, entity.roleId);
        ps.setLong(5, entity.createTime);
    }

    @Override
    public Column<User, Long> getIdColumn() {
        return columnId;
    }

    @Override
    public final List<Column> initColumns() {
        List<Column> columns = new ArrayList<>(8);
        columns.add(columnName);
        columns.add(columnPassword);
        columns.add(columnAge);
        columns.add(columnRoleId);
        columns.add(columnCreateTime);
        return columns;
    }
}