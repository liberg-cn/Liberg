package cn.liberg.database.query;

import cn.liberg.core.Column;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnTest {

    @Test
    public void testParseColumnName() {
        assertEquals("id", Column.parseColumnName("id"));
        assertEquals("_user_name", Column.parseColumnName("userName"));
        assertEquals("_user_name", Column.parseColumnName("mUserName"));
        assertEquals("_user_ppps_name", Column.parseColumnName("mUserPPPsName"));
        assertEquals("_name", Column.parseColumnName("name"));
        assertEquals("_mobile", Column.parseColumnName("mobile"));
    }
}
