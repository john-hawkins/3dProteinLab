package org.proteinlab;

import java.util.*;

import db.search.InvalidQueryException;
import db.search.MatchingComplex;
import db.search.MatchingStructure;
import db.search.StructExpPDBSearch;

import sim.ProgressMeter;


/**
 * An object to store all information related to the Search Request being made by the user.
 * 
 * @author John Hawkins
 */

public class SearchRequest {
    // Length of time (in milliseconds) that a user's request and its results are stored in
    //    the system after it is last accessed : default is set to three days
    public static final long requestLifespan = 1000 * 60 * 60 * 24 * 3;
    
    // Used to find request objects given request IDs
    private static Hashtable<Integer, SearchRequest> searchRequestTable = new Hashtable<Integer, SearchRequest>();    
    
    // Unique identifier of this request
    private final int id;
    // Unique identifier of this query (many people can run the same query)
    private int queryId;

    // Time that this request was created (from System.currentTimeMillis())
    private long created;
    // Time that this request was last accessed (from System.currentTimeMillis())
    private long timestamp;

    // Progress of the request
    ProgressMeter pm = new ProgressMeter(1.0, System.err);

    // the default number of seconds to wait between refreshes on the 'processing' page
    // this can be overwritten with setRefreshTime
    private int refreshTime = 10;
    // whether or not the results are ready
    private boolean resultsAvailable; 
    
    // the IP address of the client that sent the request
    private String IPaddress; 

    // the Identifier of the client that sent the request
    private String sessionID; 
    
    
	// any error that may have occured during processing. null means all good.
	private String error;

	private boolean savedSearchOn = true;
    
	/*
	 * ALL THE PARAMETERS OF THE SEARCH REQUEST
	 */
	private String pdbid ;
	private String scopid ;
	private String keywords ;
	private String redundancy ;
	private String annot ;
	private String resolutionOperator ;
	private String resolution ;
	private String tech ;
	private String regex;
	private String interactors;

	private boolean multihits =false;
	private boolean pseudoatoms =false;

	private String addr;
	private String host;
	
	// Somewhere to put the results
	List<MatchingComplex> finalSet;
	
	/*
	 * Create a new Request Object
	 * 
	 * With all the parameters of the model and the data set for training
	 */
	
	public List<MatchingComplex> getFinalSet() {
		return finalSet;
	}


	public SearchRequest(String sID, String ip, String pdbid2, String scopid2, String keywords2, String redun,
			String annot2, String resOp, String resolution2, String tech2,
			String regex2, String interactors2, String hi, String di) {
		pdbid = pdbid2;
		scopid = scopid2;
		keywords =keywords2;
		redundancy = redun;
		annot =annot2 ;
		resolutionOperator = resOp;
		resolution = resolution2;
		tech =tech2 ;
		regex = regex2;
		interactors = interactors2;
        IPaddress = ip;
        sessionID = sID;
        id = getId();
        resultsAvailable = false;
        if(hi.equals("True") )
        		multihits = true;
        if(di.equals("Pseudo") )
        		pseudoatoms = true;

        created = System.currentTimeMillis();
        this.touch();
        System.err.println("org.proteinlab.SearchRequest Created: " + created);
	}

	
	protected synchronized int getId() {
        // Generates new 7 digit random numbers until an unused one is found
        Random r = new Random(System.currentTimeMillis());
        int newID;
        do {
            newID = r.nextInt(9000000) + 1000000;
        } while (searchRequestTable.contains(new Integer(newID)));
        searchRequestTable.put(new Integer(newID), this);
        return newID;
	}
	
	
	/*
	 * Execute the search
	 */
	public void execute(String[] dbProps) throws InvalidQueryException {
        System.err.println("EXECUTING , Saved Search is " + savedSearchOn + "<BR>");
        
        StructExpPDBSearch app = new StructExpPDBSearch(pm, dbProps, savedSearchOn);
        System.err.println("StructExpPDBSearch Created " );
        System.err.println("PSEUDO POINTS:  " + pseudoatoms );
    		app.prepareQuery( IPaddress, pdbid,  scopid,  keywords, redundancy, annot,  resolutionOperator, resolution, tech, regex, interactors, multihits, pseudoatoms);
    		System.err.println("Query Prepared " );
    		app.run();
    		System.err.println("Search Executed" );
    		queryId = app.getQueryId();
    		finalSet = app.getFinalSet();
		setResultsAvailable();
	}

	
    public synchronized static boolean isValidID(int id){
        return searchRequestTable.contains(new Integer(id));
    }
    
    public synchronized static SearchRequest getSearchRequest (int id){
        return (SearchRequest)searchRequestTable.get(new Integer(id));
    }
    
    /*
     * Retrieve search requests for a user with a particular Session ID
     * 
     * @param sID The Session ID for the user
     */
    public synchronized static Vector<SearchRequest> getUserSearchRequests (String sID) {
    	Vector<SearchRequest> results = new Vector<SearchRequest>();
    	Enumeration<SearchRequest> enumer = searchRequestTable.elements();
        while (enumer.hasMoreElements()){
            SearchRequest searchRequest = (SearchRequest) enumer.nextElement();
            if (searchRequest.getUserSessionID().equals(sID)) {
            	results.add(searchRequest);
            }
        }
        return results;
    }
    
    
    /**
     * Checks each request in the system for expired requests
     * and deletes them. This should be called regularly,
     * but there is no need to call it very frequently.
     *
     */
    public synchronized static void deleteExpired () {
        Enumeration<SearchRequest> enumer = searchRequestTable.elements();
        while (enumer.hasMoreElements()){
            SearchRequest searchRequest = (SearchRequest) enumer.nextElement();
            if (System.currentTimeMillis() - searchRequest.timestamp> requestLifespan) {
                searchRequest.abandon();
            }
        }
    }
    
    public synchronized static int getNumberActive (){
        return searchRequestTable.size();
    }
    
    public synchronized static Enumeration<SearchRequest> getTableEnum (){
        return searchRequestTable.elements();
    }
    
    public int getID(){
        return id;
    }
    
    public int getQueryId() {
		return queryId;
	}
    
    public String getUserIP (){
        return IPaddress;
    }
    public String getUserSessionID (){
        return sessionID;
    }
    
    public double getPercentageComplete() {
		return pm.getPercentageProcessed();
	}
   
    public void setRefreshTime (int refreshTime){
        this.refreshTime = refreshTime;
    }
    
    public int getRefreshTime (){
        return refreshTime;
    }
    
    public long getLastAccessTime (){
        return timestamp;
    }
    
    
    public long getCreatedTime() {
		return created;
	}
	public void setCreatedTime(long created) {
		this.created = created;
	}

    /**
     * Signals the availability of the results. Because a request is designed
     * to be used by a signal request (ie no re-use), it wouldn't make sense
     * to be able to change this to false from true.
     *
     */
    public void setResultsAvailable (){
        resultsAvailable = true;
    }
    
    /**
     * Whether or not processing is complete and results are available.
     * Ok, so "is results available" doesn't make sense, but "are" is the
     * same verb in a different form - I think adhering to naming conventions
     * is more important in this context than grammatical accuracy. (While
     * we're on the topic of grammar, the first sentence above is not a complete
     * sentence)
     */
    public boolean isResultsAvailable (){
        return error == null && resultsAvailable;
    }
    
    /**
     * Notify that the request has been accessed, so that it doesn't
     * time out.
     *
     */
    public void touch(){
        timestamp = System.currentTimeMillis();
    }
    
    /**
     * Removes the request from the system
     *
     */
    public void abandon() {
        searchRequestTable.remove(new Integer(id));
    }
    
	/**
	 * Set the error message. This doesn't stop the other methods
	 * from being accessed, but relies on accessors of this class checking
	 * that getError isn't null before accessing it...
	 * Also, erroneous requests are NOT deleted any sooner (though this
	 * could easily be changed in deleteExpired). This is so that users
	 * who bookmark the page and check back later will still be able to
	 * view the error message.
	 * @param error the error
	 */
	public synchronized void setError (String error){
	    this.error = error;
	}
	
	public synchronized String getError (){
	    return error;
	}


	public static int getUserLatestRequestID(String sID) {
		Vector<SearchRequest> reqs = SearchRequest.getUserSearchRequests(sID);
		long latestTime = 0;
		int bestID = 0;
		for(int r=0; r<reqs.size(); r++) {
        	SearchRequest req = reqs.get(r);
        	if(req.getCreatedTime() > latestTime) {
        		bestID = req.getID();
        		latestTime = req.getCreatedTime();
        	}
		}
		return bestID;
	}


	public static List<MatchingComplex> getResultsForQuery(int queryid, String[] dbProps) {
		// TODO Auto-generated method stub
		StructExpPDBSearch app = new StructExpPDBSearch( dbProps );
		return app.getSavedSearchResults(queryid);
	}


	public static void killRequstsByUser(String sessID) {
		Vector<SearchRequest> reqs = SearchRequest.getUserSearchRequests(sessID);

		for(int r=0; r<reqs.size(); r++) {
        	SearchRequest req = reqs.get(r);
        	req.abandon();
		}
	}
	

}
