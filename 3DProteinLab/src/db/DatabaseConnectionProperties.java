package db;

import java.util.Properties;

public class DatabaseConnectionProperties {
	private String driverClassName;
	private String connectionUrl;
	private Properties info;
	
	public DatabaseConnectionProperties(String driverClassName, String connectionUrl, Properties info) {
		this.connectionUrl = connectionUrl;
		this.driverClassName = driverClassName;
		this.info = info;
	}
	
	public DatabaseConnectionProperties(String[] args) {
		if (args.length < 2)
			throw new IllegalArgumentException("You must provide at least a driver class name and a connection URL");
		
		this.driverClassName = args[0];
		this.connectionUrl = args[1];
		this.info = new Properties();
		
		for (int i=2; i<args.length; i++) {
			String[] property = args[i].split("=", 2);
			if (property.length != 2)
				throw new IllegalArgumentException("Each property must be in a form <property_name>=<value>");
			info.put(property[0], property[1]);
		}
	}		

	public String getDriverClassName() {
		return driverClassName;
	}
	public String getConnectionUrl() {
		return connectionUrl;
	}
	public Properties getInfo() {
		return info;
	}	
}