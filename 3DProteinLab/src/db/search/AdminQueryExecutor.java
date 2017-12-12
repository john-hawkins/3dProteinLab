package db.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/*
import structools.structure.AtomSite;
import structools.structure.Protein;


import db.db2.XmlRPdbDAO;
*/
import db.PdbDAO;
import db.mysql.minimalPdbDAO;
import db.DatabaseConnectionProvider;
import db.DatabaseConnectionProperties;
import db.SimpleDatabaseConnectionProviderImpl;


import sim.ProgressMeter;

public class AdminQueryExecutor {

	protected DatabaseConnectionProvider connectionProvider;
	protected Connection con;
	protected PdbDAO dao;
	
	/* 
	 * CONSTRUCTOR
	 */
	public AdminQueryExecutor(String[] dbprops) {
		connectionProvider = new SimpleDatabaseConnectionProviderImpl(new DatabaseConnectionProperties(dbprops));
		
		try {
			con = connectionProvider.getConnection();
		}
		catch(SQLException e) {
			System.err.println("SQL error while connecting to database: " + dbprops[0]);
			System.exit(0);
		}

		dao = new minimalPdbDAO(con);
	}
	
	/* 
	 * FINALIZE METHOD FOR CLEANING UP
	 */
	protected void finalize() throws Throwable {
	    try {
			if (con != null)
				con.close();
	    } finally {
	        super.finalize();
	    }
	}
	
	/* 
	 * executeQuery
	 * 
	 * @see db.search.DBQueryExecutor#executeQuery(db.search.StructExpQuery, db.search.ProgressMeter)
	 */
	public List<QueryResult> getQueryList() {
		try {
			List<QueryResult> finalSet = dao.getQueryList();
			return finalSet;
		}
		catch(SQLException e) {
			System.err.println("SQL error while retrieving list of queries ");
			System.exit(0);
		}
		return null;
	}
		
	
}
