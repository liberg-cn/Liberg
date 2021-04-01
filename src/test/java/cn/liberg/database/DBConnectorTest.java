package cn.liberg.database;

import cn.liberg.support.data.DBConfig;
import org.junit.Assert;
import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.assertEquals;

public class DBConnectorTest {
    private static int SIZE = 3;

    private boolean testConnection(Connection conn) throws SQLException {
        boolean result;
        Statement stat = conn.createStatement();
        result = stat.executeQuery("select 1") != null;
        stat.close();
        return result;
    }

    private void repeatTest(DBConnector connector, int curIdx) {
        Connection conn = null;
        boolean isTxError = false;
        try {
            conn = connector.getConnect();
            // 连接池中拿走一个连接后，预期剩余SIZE-1个
            assertEquals(SIZE-1, connector.getFreeCount());

            if (testConnection(conn)) {
                System.out.println("OK " + curIdx);
            }
        } catch (SQLException ex) {
            isTxError = DBHelper.isTxError(ex);
            System.out.println("Exception: " + ex.getClass().getName());
        } finally {
            if(isTxError) {
                assertEquals(SIZE-1, connector.getFreeCount());
                connector.freeAllConnection(conn);
                // 如果发生通信错误，释放所有连接后，空闲连接数应为0
                assertEquals(0, connector.getFreeCount());
                // 发生错误后，连接全部释放，再次调用connector.getConnect()会申请到1个连接
                // 为了能让测试继续跑，重置SIZE为1
                SIZE = 1;
            } else {
                connector.freeConnection(conn, false);
                // 如果没有发生通信错误，连接归还后还是SIZE个
                assertEquals(SIZE, connector.getFreeCount());
            }
        }
    }

    @Test
    public void test() {
        IDataBaseConf dbConfig = new DBConfig();
        DBConnector connector = new DBConnector();
        connector.init(dbConfig);

        Connection[] connArray = new Connection[SIZE];
        try {
            /**
             * 先初始化SIZE个连接
             */
            for (int i = 0; i < connArray.length; i++) {
                connArray[i] = connector.getConnect();
            }
            /**
             * SIZE个连接放回到连接池
             */
            for (int i = 0; i < connArray.length; i++) {
                connector.freeConnection(connArray[i], false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 预期连接池中有SIZE个空闲连接
        assertEquals(SIZE, connector.getFreeCount());


        int i = 10;
        while (i > 0) {
            /**
             * 多次执行的方法：
             * 1、MySQL服务端正常打开后，开始执行几次；
             * 2、接着，关掉MySQL服务端，继续执行几次，
             * 3、最后，再打开MySQL服务端，继续执行几次。
             *
             * 测试正确的控制台输出结果应该是：若干个OK + 若干个CommunicationsException + 若干个OK
             * OK
             * OK
             * OK
             * Exception: com.mysql.cj.jdbc.exceptions.CommunicationsException
             * Exception: com.mysql.cj.jdbc.exceptions.CommunicationsException
             * Exception: com.mysql.cj.jdbc.exceptions.CommunicationsException
             * OK
             * OK
             * OK
             */
            repeatTest(connector, i);

            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i--;
        }
    }
}
