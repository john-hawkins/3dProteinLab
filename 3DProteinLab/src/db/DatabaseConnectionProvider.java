package db;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnectionProvider {
	public Connection getConnection() throws SQLException;
}