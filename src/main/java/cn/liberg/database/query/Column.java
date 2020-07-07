package cn.liberg.database.query;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Column {
    protected String name;
    protected Field entityField = null;

    public Column(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getValue(ResultSet rs) throws SQLException {
        return rs.getObject(name);
    }

    public Object getEntityValue(Object entity) throws Exception {
        if(entityField == null) {
            entityField = entity.getClass().getDeclaredField(name);
        }
        return entityField.get(entity);
    }

    public void setEntityValue(Object entity, Object value) throws Exception {
        if(entityField == null) {
            entityField = entity.getClass().getDeclaredField(name);
        }
        entityField.set(entity, value);
    }
}
