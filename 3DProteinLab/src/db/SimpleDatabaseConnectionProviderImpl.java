package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SimpleDatabaseConnectionProviderImpl implements DatabaseConnectionProvider {
	private String url;
	private Properties info;
	
	public SimpleDatabaseConnectionProviderImpl(DatabaseConnectionProperties properties) {
		try {
			Class.forName(properties.getDriverClassName());
		}
		catch(ClassNotFoundException e) {
			throw new IllegalArgumentException("Class " + properties.getDriverClassName() + " was not found in the current classpath");
		}
		
		this.url = properties.getConnectionUrl();
		this.info = properties.getInfo();
		
		
	}
	
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, info);		
	}
}
