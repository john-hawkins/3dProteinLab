package db.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structools.structure.Protein;

public class DBQuery {
	// Some fields that will hold the string representations
	// of the original query
	protected String originalQuery;

	// The above is broken into constituent patterns
	protected String[] queryStringsPatterns;
	// and connectors
	protected String[] queryStringsConnectors;
	
	// The patterns are then converted into regular expressions
	protected String[] regExPats;
	// and simplified SQL patterns for a LIKE statement
	protected String[] queryStringsForSQL;

	protected int numOfPatterns = 0;
	protected int numOfConnections = 0;

	protected Pattern[] regExPatternSet;
	
	// Parameters that affect the running of the query
	protected String pdbID = "";
	protected String functionKnown = "";
	protected String keywords = "";
	protected String redundancy = "";
	protected String resolution = "";
	protected String resolutionOperator = "";

	protected String tech = "";
	protected String scopid = "";
	protected String interactors = "";
	
	protected int minChainLength= 0;
	protected int maxChainLength= 0;	
	/*
	 * Admin parameters for web apps
	 */
	protected String user_id = "";
	protected String datestamp = "";
	
	protected boolean useSavedSearch = true;
	protected int queryID = 0;
	
	protected boolean multipleHitsPerStructure = true;
	protected int maxMultipleHits = 100;

	protected boolean circularConnectors = false;

	protected boolean usePseudoAtoms = false;

	public int getQueryID() {
		return queryID;
	}

	/* Default Constructor
	 * 
	 * For subclasses
	 */
	public DBQuery() {
		createDatestamp();
	}
	
	/* Constructor
	 * 
	 * Convert a sql sequence pattern to ensure that it
	 * permits reordering of elements
	 */
	public DBQuery(String seqpattern) throws InvalidQueryException  {
		parseQuery(seqpattern);
		createDatestamp();
	}
	
	/* Constructor
	 * 
	 * Convert a sql sequence pattern to ensure that it
	 * permits reordering of elements
	 */
	public DBQuery(String seqpattern, String user) throws InvalidQueryException  {
		user_id = user;
		parseQuery(seqpattern);
		createDatestamp();
	}
	
	protected void createDatestamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		datestamp = dateFormat.format(date);
	}

	/* parseQuery(String pattern)
	 * 
	 * This is the core function that converts the given string
	 * into the required data structures for matching against
	 * the content of the database.
	 */
	protected void parseQuery(String pattern) throws InvalidQueryException {
		System.err.print("QUERY: " + pattern);
		originalQuery = pattern.trim();
		
		// Check for anchor characters and change
		if(originalQuery.charAt(0) == '<') {
			originalQuery = "^" + originalQuery.substring(1);
		}
		if(originalQuery.charAt(originalQuery.length()-1) == '>') {
			originalQuery = originalQuery.substring(0,originalQuery.length()-1) + "$";
		}

		originalQuery = cleanPattern(pattern);
		System.err.print("CLEAN QUERY: " + originalQuery);
		
		// MAKE SURE THAT THE NUMBER OF CONNECTORS IS VALID
		enforceValidConnectors(originalQuery);
		
		originalQuery = enforceCysteineConsistency(originalQuery);
		originalQuery = enforceConsistentSSEUsage(originalQuery);
		
		breakQueryIntoPatternsAndConnectors(originalQuery);
		
		// Now we convert the patterns and connectors into
		// implementation specific data structures for the query
		this.convertPatterns();
		this.convertConnectors();
	}


	private String cleanPattern(String pattern) {
		String[] temps = pattern.split("-");
		String result = "";
		for(int i=0; i<temps.length; i++) {
			if(i>0)
				result = result + "-";
			result = result + temps[i].trim();
		}
		return result;
		/*
		String spacePattern = " - ";
		Pattern pat = Pattern.compile(spacePattern);
		Matcher matcher = pat.matcher(pattern);
		String result = "";
		int lastIndex = 0;
		while(matcher.find()) {
			result = result + pattern.substring(lastIndex, matcher.start()-1);
			lastIndex = matcher.end()+1;
		}
		result = result + pattern.substring(lastIndex, pattern.length());
		return result; */
	}

	private void breakQueryIntoPatternsAndConnectors(String temp) throws InvalidQueryException {
		originalQuery = temp;
		// We break the overall pattern into its constituents, so we must locate all of the connectors.
		String conPat = "<[^>]*>";
		Pattern pat = Pattern.compile(conPat);
		Matcher matcher = pat.matcher(originalQuery);
		int hits = 0;
		while(matcher.find()) hits++;
		numOfPatterns = hits+1;
		numOfConnections = hits;
		queryStringsPatterns = new String[numOfPatterns];
		queryStringsConnectors = new String[numOfConnections];
		matcher.reset();
		int index = 0;
		int patStart=0;
		while(matcher.find()) {
			queryStringsPatterns[index] = originalQuery.substring(patStart, matcher.start()-1);
			queryStringsConnectors[index] = originalQuery.substring(matcher.start(), matcher.end());
			patStart=matcher.end()+1;
			//System.out.println("Parsed: " + queryStringsPatterns[index]);
			//System.out.println("Parsed: " + queryStringsConnectors[index]);
			index++;
		}
		// Now check if there is anything left at the end
		if(patStart < originalQuery.length()) { 
			String lastbit = originalQuery.substring(patStart, originalQuery.length());
			queryStringsPatterns[index] = lastbit;
			//System.out.println("Parsed: " + queryStringsPatterns[index])
		} else {
			// We have a situation in which the pattern ends with a connector
			// So one less pattern than expected.
			numOfPatterns--;
			// And set the circle back parameter.
			this.circularConnectors = true;
		}
	}

	/*
	 * 
	 */
	protected void convertConnectors() throws InvalidQueryException  {
		// This is not implemented in this version
		// So it will treat any query as merely a set of 
		// regular expressions
	}

	/*
	 * convertPatterns() 
	 * Iterate over all the patterns and convert them to the required
	 * syntax for the query to be run
	 */
	protected void convertPatterns() throws InvalidQueryException  {
		regExPats = new String[numOfPatterns];
		queryStringsForSQL = new String[numOfPatterns];
		for(int i=0; i<numOfPatterns;i++) {
			regExPats[i] = prosite2regExp( queryStringsPatterns[i] );
			queryStringsForSQL[i] = prosite2SQL( queryStringsPatterns[i] );
		}	
		regExPatternSet = new Pattern[numOfPatterns];
		for(int i=0; i<numOfPatterns; i++) {
			regExPatternSet[i] = Pattern.compile(regExPats[i]);
		}
	}
	
	
	/* prosite2SQL(String prositeQuery)
	 * 
	 * Take the PROSITE regular expression and generate
	 * a simple SQL pattern for a LIKE statement
	 */
	protected String prosite2SQL(String prositeQuery) {
		
		//System.out.print("Converting to SQL : " + prositeQuery );
		String queryForSQL = "";
		
		String [] temp = prositeQuery.split("-");
		if(temp[0].charAt(0)!='^')
			queryForSQL = "%";
		
		for(int i=0; i<temp.length; i++) {	
			
			if(temp[i].charAt(0)=='^')
				temp[i] = temp[i].substring(1);
			if(temp[i].charAt(temp[i].length()-1)=='$')
				temp[i] = temp[i].substring(0,temp[i].length()-1);
			
			if(temp[i].length() == 1) {
				if(temp[i].charAt(0) == 'x' || temp[i].charAt(0) == 'X') 
					queryForSQL = queryForSQL + "_";
				else
					queryForSQL = queryForSQL + temp[i];
			} else {
				if(temp[i].charAt(0) == '[' && temp[i].charAt(temp[i].length()-1) == '[') {
					queryForSQL = queryForSQL + "_";
				} else {
					queryForSQL = queryForSQL + "%";
				}
			}
		}

		if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)!='$')
			queryForSQL = queryForSQL + "%";
		
		System.out.print("Query for SQL RESULT : " + queryForSQL );
		
		return queryForSQL;
	}

	/*
	 * prosite2regExp
	 */
	protected String prosite2regExp(String prositeQuery){
		String regExpQuery = "";
		
		String [] temp = prositeQuery.split("-");
		
		for(int i=0; i<temp.length; i++) {	
			if(temp[i].charAt(0) == 'x') {
				regExpQuery = regExpQuery + "." + temp[i].substring(1);
			} else if(temp[i].charAt(0) == '{') {
				int pos = temp[i].indexOf('}');
				regExpQuery = regExpQuery + "[^" + temp[i].substring(1,pos-1) + "]" + temp[i].substring(pos+1);
			} else {
				regExpQuery = regExpQuery + temp[i];
			}
		}
		// GET RID OF WHITE SPACE
		regExpQuery = regExpQuery.replaceAll(" ", "");
		// CONVERT TO CORRECT SYNTAX FOR RANGES
		regExpQuery = regExpQuery.replaceAll("\\(", "{");
		regExpQuery = regExpQuery.replaceAll("\\)", "}");
		// CONVERT TO LINE ANCHORING SYNTAX
		regExpQuery = regExpQuery.replaceAll("\\<", "^");
		regExpQuery = regExpQuery.replaceAll("\\>", "$");
		
		return regExpQuery;
	}
	
	
	
	/* printQueryData
	 * 
	 * A simple method for dumping out the query contents that
	 * will be used to identify proteins in the database
	 */
	public void printQueryData() {
		System.out.println("SQL Query Pattern: " + numOfPatterns);
		
		System.out.println(" ARRAY OF STRUCTURED REG EXPs ");
		for(int i=0; i<numOfPatterns; i++) {
			System.out.println(" ] " + regExPats[i]);
			System.out.println();
		}
	}

	/*
	 * Getters and Setters
	 */
	
	public String getOriginalQuery() {
		return originalQuery;
	}
	
	public int getNumOfPatterns() {
		return numOfPatterns;
	}

	public Pattern[] getRegExPatternSet() {
		return regExPatternSet;
	}


	public String[] getQueryStringsForSQL() {
		return queryStringsForSQL;
	}

	public String getPdbID() {
		return pdbID;
	}

	public void setPdbID(String pdbid) {
		pdbID = pdbid;
	}

	public String getFunctionKnown() {
		if(functionKnown == null)
			return "";
		else
			return functionKnown;
	}

	public void setSearchProperties(String searchProperties) {
		this.functionKnown = searchProperties;
	}
	
	/* 
	 * NEW METHODS FOR WEB QUERIES 
	 */
	
	public String getTech() {
		return tech;
	}

	public void setTech(String tech) {
		this.tech = tech;
	}

	public String getScopid() {
		return scopid;
	}

	public void setScopid(String scopid) {
		this.scopid = scopid;
	}
	public void setScopID(String sc) {
		scopid = sc;
	}
	
	public String getKeywords() {
		return keywords;
	}
	public String getKeywordsForSQLLIKE() {
		return "%" + keywords + "%";
	}

	public void setKeywords(String keys) {
		keywords = keys;
	}
	
	public String getResolution() {
		return resolution;
	}

	public double getResolutionAsDouble() {
		if(resolution==null || resolution.equals(""))
			return 0.0;
		return Double.parseDouble(resolution);
	}

	public String getInteractors() {
		return interactors;
	}
	
	public String getRedundancy() {
		return redundancy;
	}
	public void setRedundancy(String redun) {
		redundancy = redun;
	}

	public void setResolution(String res) {
		resolution = res;
	}

	public void setTechnology(String t) {
		tech = t;
	}

	public void setInteractors(String ints) {
		interactors = ints;
	}

	public void setResolutionOperator(String resOp) {
		resolutionOperator = resOp;
	}

	public String getResolutionOperator() {
		return resolutionOperator;
	}
	
	public int getMinChainLength() {
		return minChainLength;
	}

	public void setMinChainLength(int minChainLength) {
		this.minChainLength = minChainLength;
	}

	public int getMaxChainLength() {
		return maxChainLength;
	}

	public void setMaxChainLength(int maxChainLength) {
		this.maxChainLength = maxChainLength;
	}


	public boolean isMultipleHitsPerStructure() {
		return multipleHitsPerStructure;
	}

	public void setMultipleHitsPerStructure(boolean val) {
		this.multipleHitsPerStructure = val;
	}
	
	public int getMaxMultipleHits() {
		return maxMultipleHits;
	}

	public void setMaxMultipleHits(int maxMultipleHits) {
		this.maxMultipleHits = maxMultipleHits;
	}
	
	public boolean isCircularConnectors() {
		return circularConnectors;
	}

	public void setCircularConnectors(boolean circularConnectors) {
		this.circularConnectors = circularConnectors;
	}
	
	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String userId) {
		user_id = userId;
	}

	public String getDatestamp() {
		return datestamp;
	}

	public void setDatestamp(String datestamp) {
		this.datestamp = datestamp;
	}
	
	
	/*
	 * UTILITY FUNCTIONS - USEFUL FOR PARSING THE QUERY STRING
	 */
	protected boolean hasEvenBrackets(String temp) {
		boolean even = true;
		int numLBracket = 0;
		int numRBracket = 0;
		int numLCurly = 0;
		int numRCurly = 0;
		int numLSquare = 0;
		int numRSquare = 0;
		for(int i=0; i<temp.length(); i++) {
			if(temp.charAt(i)=='(')
				numLBracket++;
			if(temp.charAt(i)==')')
				numRBracket++;
			if(temp.charAt(i)=='{')
				numLCurly++;
			if(temp.charAt(i)=='}')
				numRCurly++;
			if(temp.charAt(i)=='[')
				numLSquare++;
			if(temp.charAt(i)==']')
				numRSquare++;
		}
		if(numLBracket != numRBracket)
			even = false;
		if(numLCurly != numRCurly)
			even = false;
		if(numLSquare != numRSquare)
			even = false;
		
		return even;
	}
	
	protected boolean hasBalancedNumbers(String temp, char char1, char char2) {
		boolean even = true;
		int numChar1 = 0;
		int numChar2 = 0;
		
		for(int i=0; i<temp.length(); i++) {
			if(temp.charAt(i)==char1)
				numChar1++;
			if(temp.charAt(i)==char2)
				numChar2++;
		}
		if(numChar1 != numChar2)
			even = false;
		
		return even;
	}

	
	private void enforceValidConnectors(String temp) throws InvalidQueryException {
		if( ! hasBalancedNumbers(temp, '<', '>') )
			throw new InvalidQueryException("Uneven number of Connector brackets < and > in the Query !");
		if( hasConnectionProblem(temp ) )
			throw new InvalidQueryException("Malformed Connections in your query string !");
	}

	protected void enforceEvenBrackets(String temp) throws InvalidQueryException {
		if( ! hasBalancedNumbers(temp, '(', ')') )
			throw new InvalidQueryException("Uneven number of brackets ( and ) in the Query !");
		if( ! hasBalancedNumbers(temp, '[', ']') )
			throw new InvalidQueryException("Uneven number of brackets [ and ] in the Query !");
		if( ! hasBalancedNumbers(temp, '{', '}') )
			throw new InvalidQueryException("Uneven number of brackets { and } in the Query !");
	}
	
	protected void enforceNoBracketsInBrackets(String temp) throws InvalidQueryException {
		if( hasBracketProblem(temp ) )
			throw new InvalidQueryException("Brackets must come in ordered pairs without nesting !");
	}
	
	protected void enforceValidCharacters(String temp) throws InvalidQueryException {
		if( hasInvalidCharacter(temp ) )
			throw new InvalidQueryException("Your query contains an invalid character !");
	}
	
	private boolean hasConnectionProblem(String temp) {
		// TODO : If Required (Currently this check is done in the function convertConnectors() of subclasses )
		return false;
	}
	
	
	protected boolean hasBracketProblem(String temp) {
		boolean openBrackets = false;
		for(int i=0; i<temp.length(); i++) {
			if(temp.charAt(i)==')' || temp.charAt(i)=='}' || temp.charAt(i)==']') {
				if(!openBrackets) {
					return true;
				} else {
					 openBrackets = false;
				}
				
			} else if(temp.charAt(i)=='(' || temp.charAt(i)=='{' || temp.charAt(i)=='[') {
				if(openBrackets) {
					return true;
				} else {
					openBrackets = true;
				}
			}

		}
		return false;
	}
	
	protected boolean hasInvalidCharacter(String temp) {
		String allowedChars = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZxshl[]{}()<>*,^- ";
		for(int i=0; i<temp.length(); i++) {
			char t = temp.charAt(i);
			if(allowedChars.indexOf(t)==-1)
				return true;
		}
		return false;
	}
	
	protected boolean isLocatedInOpenBrackets(String temp, int position) {
		boolean inBrackets = false;
		int numLBracket = 0;
		int numRBracket = 0;
		int numLCurly = 0;
		int numRCurly = 0;
		int numLSquare = 0;
		int numRSquare = 0;
		for(int i=0; i<position; i++) {
			if(temp.charAt(i)=='(')
				numLBracket++;
			if(temp.charAt(i)==')')
				numRBracket++;
			if(temp.charAt(i)=='{')
				numLCurly++;
			if(temp.charAt(i)=='}')
				numRCurly++;
			if(temp.charAt(i)=='[')
				numLSquare++;
			if(temp.charAt(i)==']')
				numRSquare++;
		}
		if(numLBracket != numRBracket)
			inBrackets = true;
		if(numLCurly != numRCurly)
			inBrackets = true;
		if(numLSquare != numRSquare)
			inBrackets = true;
		
		return inBrackets;
	}

	protected String enforceCysteineConsistency(String tempPat) {
		// If the pattern uses the cysteine binding syntax then we 
		// need to enforce consistency in its usage
		if(tempPat.indexOf('B')>0 || tempPat.indexOf('U')>0) {
			return replaceAllEnforceBrackets( tempPat,  "C", "BU");
		}
		return tempPat;
	}
	
	protected String replaceAllEnforceBrackets(String completeString, String before, String after) {
		String newbie = "";	
		int lastPos = 0;
		Pattern pat = Pattern.compile(before);
		Matcher matcher = pat.matcher(completeString);
		while(matcher.find()) {
			int thisPos = matcher.start();
			if(isLocatedInOpenBrackets(completeString, thisPos) ) {
				newbie = newbie + completeString.substring(lastPos,thisPos ) + after;
			} else {
				newbie = newbie + completeString.substring(lastPos,thisPos ) + "[" + after + "]";
			}
			lastPos = thisPos+before.length();
		}
		newbie = newbie + completeString.substring(lastPos, completeString.length() );
		return newbie;
	}
	
	protected String replaceAllXsWithCharRanges(String temp) {
		String xpat = "x";
		Pattern pat = Pattern.compile(xpat);
		Matcher matcher = pat.matcher(temp);
		String newbie = "";
		String allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int lastPos = 0;
		while(matcher.find()) {
			int thisPos = matcher.start();
			if(isLocatedInOpenBrackets(temp, thisPos) ) {
				newbie = newbie + temp.substring(lastPos,thisPos ) + allChars;
			} else {
				newbie = newbie + temp.substring(lastPos,thisPos ) + "[" + allChars + "]";
			}
			lastPos = thisPos+1;
		}
		newbie = newbie + temp.substring(lastPos, temp.length() );
		return newbie;
	}
	
	protected String replacePrositeNonMatch(String temp) {
		String xpat = "\\{";
		Pattern pat = Pattern.compile(xpat);
		Matcher matcher = pat.matcher(temp);
		String newbie = "";
		int lastPos = 0;
		while(matcher.find()) {
			int thisPos = matcher.start();
			String endbit  = temp.substring(thisPos +1);
			int endpos = endbit.indexOf('}');
			// First add the remains of the string up to that point
			newbie = newbie + temp.substring(lastPos,thisPos);
			newbie = newbie + "[^" +endbit.substring(0,endpos) + "]";
			
			lastPos = thisPos+endpos+2;
		}
		newbie = newbie + temp.substring(lastPos, temp.length() );
		return newbie;
	}

	public boolean useSavedSearch() {
		return useSavedSearch;
	}

	public void setUseSavedSearch(boolean useSavedSearch) {
		this.useSavedSearch = useSavedSearch;
	}

	public void setQueryId(int q) {
		queryID = q;
	}
	
	public int getQueryId() {
		return queryID;
	}
	
	public void setusePseudoAtoms(boolean pseu) {
		usePseudoAtoms = pseu;
	}
	public boolean usePseudoAtoms() {
		return usePseudoAtoms;
	}
	
	public boolean usesCysteineBondCodes() {
		if( this.getOriginalQuery().indexOf(Protein.CysBoundChar)>-1 || this.getOriginalQuery().indexOf(Protein.CysUnBoundChar)>-1 ) {
			return true;
		} else 
			return false;
	}

	private String enforceConsistentSSEUsage(String originalQuery2) {
		// If the pattern uses SSEs then we want to be sure that SSE
		// without residues are always followed by an X
		// ie -s- is converted to -sX-
		// This helps makes sure that each query has a single representation
		
		String xpat = "-[a-z]-";
		Pattern pat = Pattern.compile(xpat);
		Matcher matcher = pat.matcher(originalQuery2);
		String newbie = "";
		int lastPos = 0;
		while(matcher.find()) {
			int thisPos = matcher.start();
			String thechar  = originalQuery2.substring(thisPos+1, thisPos+2);
			//String endbit  = originalQuery2.substring(thisPos+3);
			// First add the remains of the string up to that point
			newbie = newbie + originalQuery2.substring(lastPos,thisPos);
			newbie = newbie + "-" +thechar + "X-";
			
			lastPos = thisPos+3;
		}
		newbie = newbie +originalQuery2.substring(lastPos, originalQuery2.length() );
		
		// CHECK FOR THE ENDS SEPARATELY
		if(newbie.substring(0,2).equals("s-"))
			newbie = "sX-" + newbie.substring(2);
		if(newbie.substring(0,2).equals("h-"))
			newbie = "hX-" + newbie.substring(2);
		if(newbie.substring(0,2).equals("c-"))
			newbie = "cX-" + newbie.substring(2);
		
		int thelength = newbie.length();
		
		if(newbie.substring(thelength-2).equals("-s"))
			newbie = newbie.substring(0, thelength-2) + "-sX";
		if(newbie.substring(thelength-2).equals("-h"))
			newbie = newbie.substring(0, thelength-2) + "-hX";
		if(newbie.substring(thelength-2).equals("-c"))
			newbie = newbie.substring(0, thelength-2) + "-cX";
		
		return newbie;
		
	}
}
