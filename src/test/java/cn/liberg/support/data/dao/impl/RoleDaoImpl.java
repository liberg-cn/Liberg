package cn.liberg.support.data.dao.impl;

import cn.liberg.core.Column;
import cn.liberg.core.IdColumn;
import cn.liberg.core.StringColumn;
import cn.liberg.database.BaseDao;
import cn.liberg.support.data.entity.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl extends BaseDao<Role> {
    public static final String TABLE_NAME = "role";

    public static final IdColumn<Role> columnId = new IdColumn<Role>() {
        @Override
        public Long get(Role entity) {
            return entity.id;
        }

        @Override
        public void set(Role entity, Long value) {
            entity.id = value;
        }
    };

    public static final Column<Role, String> columnName = new StringColumn<Role>("name", "n"){

        @Override
        public String get(Role entity) {
            return entity.name;
        }

        @Override
        public void set(Role entity, String value) {
            entity.name = value;
        }
    };
    public static final Column<Role, String> columnPermissions = new StringColumn<Role>("permissions", "p") {

        @Override
        public String get(Role entity) {
            return entity.permissions;
        }

        @Override
        public void set(Role entity, String value) {
            entity.permissions = value;
        }
    };

    protected RoleDaoImpl() {
        super(TABLE_NAME, null);
    }

    @Override
    public Class<Role> getEntityClazz() {
        return Role.class;
    }

    @Override
    public Role buildEntity(ResultSet rs) throws SQLException {
        Role entity = new Role();
        entity.id = rs.getLong(1);
        entity.name = rs.getString(2);
        entity.permissions = rs.getString(3);
        return entity;
    }

    @Override
    protected void fillPreparedStatement(Role entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.name);
        ps.setString(2, entity.permissions);
    }

    @Override
    public Column<Role, Long> getIdColumn() {
        return columnId;
    }

    @Override
    public List<Column> initColumns() {
        List<Column> columns = new ArrayList<>(4);
        columns.add(columnName);
        columns.add(columnPermissions);
        return columns;
    }

}