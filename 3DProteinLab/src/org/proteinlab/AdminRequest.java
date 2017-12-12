package org.proteinlab;

import java.io.FileInputStream;
import java.util.*;

import db.search.QueryResult;
import db.search.AdminQueryExecutor;

import sim.ProgressMeter;


/**
 * An object for making admin database requests
 * 
 * @author John Hawkins
 */

public class AdminRequest {

	protected String[] dbProps;
	protected AdminQueryExecutor app;
	/*
	 * Create a new ADMIN Request Object
	 * 
	 * With all the parameters of the model and the data set for training
	 */
	public AdminRequest(String[] dbprops2) {
		dbProps = dbprops2;
		app = new  AdminQueryExecutor(dbProps);
	}


	/*
	 * Get the complete list of queries that have been run on this server.
	 */
	public List<QueryResult> getListofQueries() {
		return app.getQueryList();
	}
	
}
