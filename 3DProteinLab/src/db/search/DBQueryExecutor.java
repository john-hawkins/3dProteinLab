package db.search;

import java.sql.SQLException;
import java.util.List;

import sim.ProgressMeter;

import db.PdbDAO;

public abstract class DBQueryExecutor {

	protected PdbDAO dao;
	/* 
	 * executeQuery
	 */
	public abstract List<MatchingComplex> executeQuery(DBQuery query, ProgressMeter pm);

	/*
	 * 
	 */
	public abstract List<MatchingStructure> executeQuery(String q, String searchProps, ProgressMeter pm);

	/* getPdbID4Uniprot
	 * 
	 * @see db.search.DBQueryExecutor#getPdbID4Uniprot(java.lang.String)
	 */
	public String getPdbID4Uniprot(String in) {
		try {
			return dao.getPdbID4UniprotID(in).get(0);
		} catch(SQLException e) {
			System.err.println("SQL error while executing query: " + e);
			System.exit(0);
		}
		return null;
	}

	public abstract int saveSearch(DBQuery query, List<MatchingComplex> finalSet);

	public abstract List<MatchingComplex> getSavedSearchResults(int queryid);


}