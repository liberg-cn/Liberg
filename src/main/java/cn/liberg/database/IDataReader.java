package cn.liberg.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Liberg
 */
@FunctionalInterface
public interface IDataReader  {
	/**
	 * 由实现类完成从ResultSet中读取记录
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	void read(ResultSet resultSet) throws SQLException;
}
