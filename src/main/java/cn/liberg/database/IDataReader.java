package cn.liberg.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IDataReader  {
	void read(ResultSet dataSet) throws SQLException;
}
