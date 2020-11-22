package cn.liberg.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SqlDefenderTest {

    @Test
    public void testFormat() {
        /**
         * 常见的sql注入方式
         * 1、 select * from user where _name='' or 1=1#' and _password='abc'
         *     _name传入"' or 1=1#"
         *
         * 2、 select * from user where _name='admin' or '1' and _password='admin' or '1'
         *    _name和_password都传入"admin' or '1"
         */
        String test;

        test = "' or 1=1#";
        assertEquals(SqlDefender.format(test), "'\\' or 1=1#'");
        test = "admin' or '1";
        assertEquals(SqlDefender.format(test), "'admin\\' or \\'1'");
    }
}
