package db.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structools.structure.Protein;


/*
 * StructExpQuery
 * 
 * A pattern query class that permits the speification of SSE within a PROSITE style
 * regular expression. Use to match the 20 AMINO ACID ONE LETTER CODES IN UPPER CASE
 * as well as three lower case letter for  specifying secondary structure
 * h = Helix
 * s = Beta Sheet
 * c = coil (and anything else)
 */

/*
 * TODO I need to make the patterns dela with all of the PROSITE PATTERN constructs
 * 
 * Anchoring to ends of the sequence < and >, including spaces		= DONE
 * Multiple Character ranges (x,y)									= DONE
 * Open ended number of occurences with *							= DONE
 * Inverse Sets {X,Y,Z}												= DONE
 * 
 * Verify certain errors do not occur
 */

public class StructExpQuery extends DBQuery {

	private int maxCritRes = 0;
	
	private String[] structPats;

	private String[] conComponents;
	
	private double[] conMin;
	private double[] conMax;
	private char[] conStart;
	private char[] conEnd;
	
	private double[] angleMin;
	private double[] angleMax;
	
	private double[] cavityMin;
	private double[] cavityMax;
	
	/* A constructor method for creating a structure query
	 * Note that it will throw an exception if the query is invalid
	 */
	public StructExpQuery(String pattern) throws InvalidQueryException {
		parseQuery( pattern );
		createDatestamp();
	}
	
	/* Constructor
	 * 
	 * Convert a sql sequence pattern to ensure that it
	 * permits reordering of elements
	 */
	public StructExpQuery(String pattern, String user) throws InvalidQueryException  {		
		user_id = user;
		parseQuery( pattern );
		createDatestamp();
	}
	
	/*
	 * convertConnectors()
	 */
	protected void convertConnectors() throws InvalidQueryException  {
		// Connections between patterns
		conComponents = new String[numOfConnections];
		conMin = new double[numOfConnections];
		conMax = new double[numOfConnections];
		conStart = new char[numOfConnections];
		conEnd = new char[numOfConnections];
		
		angleMin = new double[numOfConnections];
		angleMax = new double[numOfConnections];
		cavityMin = new double[numOfConnections];
		cavityMax = new double[numOfConnections];
		
		for(int i=0; i<numOfConnections;i++) {

			//System.out.println("Converting: " + queryStringsConnectors[i] );
			
			conComponents[i] = " ";
			// Take the ends off
			String conPat = queryStringsConnectors[i].substring( 1, queryStringsConnectors[i].length()-1 );
			
			// Now evaluate its components
			if(conPat.equals("_")) {
				// THIS IS A NULL CONNECTOR
				conComponents[i] = conComponents[i] + "_";
			} else {
			  System.err.println("Connection to split: " + conPat);
			  String [] conArray = conPat.split("\\|");
			  System.err.println("Split result: " + conArray.length + " [" + conArray[0] + "]");
			
			  for(int c=0; c<conArray.length; c++) {
				
				if(conArray[c].charAt(0)=='^') {
					// We have an angle definition so store the MF
					String [] tempAr = conArray[c].substring(1).split(",");
					
					angleMin[i] = (new Double(tempAr[0])).doubleValue();
					angleMax[i] = (new Double(tempAr[1])).doubleValue();
					conComponents[i] = conComponents[i] + "^";
					
				} else if(conArray[c].charAt(0)=='~') {
					// We have a cavity definition
					// BUT THIS IS NOT YET IMPLEMENTED
					String [] tempAr = conArray[c].substring(1).split(",");
					cavityMin[i] = (new Double(tempAr[0])).doubleValue();
					cavityMax[i] = (new Double(tempAr[1])).doubleValue();
					conComponents[i] = conComponents[i] + "~";
				} else {
					// We have a distance definition
					// First check for chain order limitations
					String tempo = conArray[c];
					if(tempo.charAt(0)=='N' || tempo.charAt(0)=='C') {
						conStart[i] = tempo.charAt(0);
						tempo = tempo.substring(1);
					} else {
						conStart[i] = ' ';
					}
					
					if(tempo.charAt(tempo.length()-1)=='N' || tempo.charAt(tempo.length()-1)=='C') {
						conEnd[i] = tempo.charAt(tempo.length()-1);
						tempo = tempo.substring(0,tempo.length()-1);
					} else {
						conEnd[i] = ' ';
					}

					String [] tempAr = tempo.split(",");
					
					conMin[i] = (new Double(tempAr[0])).doubleValue();
					conMax[i] = (new Double(tempAr[1])).doubleValue();

					conComponents[i] = conComponents[i] + "-";
				}
			  }
			}
		}
	}
	
	
	/*
	 * convertPatterns() 
	 * Iterate over all the patterns and convert them to the required
	 * syntax for the query to be run
	 */
	protected void convertPatterns() throws InvalidQueryException  {
		structPats = new String[numOfPatterns];
		regExPats = new String[numOfPatterns];
		queryStringsForSQL = new String[numOfPatterns];
		for(int i=0; i<numOfPatterns;i++) {
			//System.out.println("Converting: " + i);
			convertPattern(i);
			queryStringsForSQL[i] = prosite2SQL( queryStringsPatterns[i] );
		}	
		regExPatternSet = new Pattern[numOfPatterns];
		for(int i=0; i<numOfPatterns; i++) {
			regExPatternSet[i] = Pattern.compile(regExPats[i]);
		}
	}
	
	/*
	 * convertPattern\
	 *
	 */
	protected void convertPattern(int patIndex) throws InvalidQueryException  {

		String regExpQuery = "";
		structPats[patIndex] = "";
		// System.out.println("Converting: " + queryStringsPatterns[patIndex]);
		
		// We create a temp version to work on 
		String tempPat = queryStringsPatterns[patIndex].replaceAll("X", "x");
		
		// Lets start with some checking to make sure the query is valid
		enforceEvenBrackets(tempPat);
		enforceNoBracketsInBrackets(tempPat);
		
		// Convert the PROSITE non-match character syntax into standard Reg Exp
		//System.err.println("Before Replace  : " + tempPat);
		tempPat = replacePrositeNonMatch(tempPat);
		//System.err.println("After Replace   : " + tempPat);
		
		// Now iterate over all occurences of 'x' and do the necessary replacement
		//System.err.println("Before Replace  : " + tempPat);
		tempPat = replaceAllXsWithCharRanges(tempPat);
		//System.err.println("After Replace   : " + tempPat);

		// Convert some of the PROSITE SYNTAX ELEMENTS INTO STARDARD REG EXP
		// GET RID OF WHITE SPACE
		tempPat = tempPat.replaceAll(" ", "");
		// CONVERT TO CORRECT SYNTAX FOR RANGES
		tempPat = tempPat.replaceAll("\\(", "{");
		tempPat = tempPat.replaceAll("\\)", "}");

		String [] temp =  tempPat.split("-");
		
		for(int i=0; i<temp.length; i++) {	
		
			if(i>0)
				regExpQuery = regExpQuery + '-';
			
			// First check for LOWER CASE LETTERS THAT INDICATE SSEs
			if(temp[i].charAt(0) >= 'a' && temp[i].charAt(0) <= 'z' ) {
				if(temp[i].charAt(0) == 'h' || temp[i].charAt(0) == 's' || temp[i].charAt(0) == 'c') {
					char sse = temp[i].charAt(0);
					// We have a secondary structure specification
					structPats[patIndex] = structPats[patIndex] + temp[i].charAt(0);
					
					// Iterate over the remaining positions and convert any specific residues to
					// their structure specified counterparts.
					for(int c=1; c<temp[i].length(); c++) {
						if(temp[i].charAt(c) >= 'A' && temp[i].charAt(c) <= 'Z') {
							regExpQuery = regExpQuery + Protein.combine_AA_with_SSE_code(temp[i].charAt(c), sse);
						} else {
							regExpQuery = regExpQuery + temp[i].charAt(c);
						}
					}	
					
				} else {
					throw new InvalidQueryException("Query contains an unrecognised SSE code ["+temp[i].charAt(0) + "] ");
				}
			} else {
			
				structPats[patIndex] = structPats[patIndex] + "_";
				// NOW iterate over all characters as set them to match
				// any possible SSE
				boolean setOpened=false;
				for(int c=0; c<temp[i].length(); c++) {
					if(temp[i].charAt(c) >= 'A' && temp[i].charAt(c) <= 'Z') {
						if(!setOpened) regExpQuery = regExpQuery + '[';
						regExpQuery = regExpQuery + Protein.combine_AA_with_SSE_code(temp[i].charAt(c), 'c');
						regExpQuery = regExpQuery + Protein.combine_AA_with_SSE_code(temp[i].charAt(c), 'h');
						regExpQuery = regExpQuery + Protein.combine_AA_with_SSE_code(temp[i].charAt(c), 's');
						if(!setOpened) regExpQuery = regExpQuery + ']';
					} else {
						regExpQuery = regExpQuery + temp[i].charAt(c);
						if(temp[i].charAt(c)=='[') 
							setOpened = true;
						else if(temp[i].charAt(c)==']') 
							setOpened = false;
					}
				}	
			}
		}
		regExPats[patIndex] = prosite2regExp(regExpQuery);

		//System.out.println("Reg Exp Query:  " + regExpQuery);
		//System.out.println("Converted to : " + regExPats[patIndex]);
		//System.out.println("Struture Pat : " + structPats[patIndex]);
	}


	/* prosite2SQL(String prositeQuery)
	 * 
	 * Take the EXTENDED PROSITE regular expression and generate
	 * a simple SQL pattern for a LIKE statement
	 * 
	 * Overridden from the base class because it needs to
	 * support the specification of secondary structure elements
	 */
	protected String prosite2SQL(String prositeQuery) {
		
		//System.out.println("Converting to SQL : " + prositeQuery );
		String queryForSQL = "";
		
		String [] temp = prositeQuery.split("-");
		
		if(temp[0].charAt(0)!='^')
			queryForSQL = "%";
		
		for(int i=0; i<temp.length; i++) {	
			
			String current = temp[i];
			
			if(current.charAt(0)=='^')
				current = current.substring(1);
			if(current.charAt(current.length()-1)=='$')
				current = current.substring(0,current.length()-1);
			
			
			if(current.charAt(0) == 'h' || current.charAt(0) == 's' || current.charAt(0) == 'c') {
				current = current.substring(1);
			}
			
			if(current.length() == 1) {
				if(current.charAt(0) == 'x' || current.charAt(0) == 'X') 
					queryForSQL = queryForSQL + "_";
				else
					queryForSQL = queryForSQL + current;
			} else {
				if(current.charAt(0) == '[' && current.charAt(current.length()-1) == ']') {
					queryForSQL = queryForSQL + "_";
				} else {
					queryForSQL = queryForSQL + "%";
				}
			}
		}
		
		if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)!='$')
			queryForSQL = queryForSQL + "%";
		
		System.out.println("Query for SQL : " + queryForSQL );
		
		return queryForSQL;
	}
	
	
	/* A simple method for dumping out the contents that
	 * have been parsed out of the query string
	 */
	public void printQueryData() {

		System.out.println(" ARRAY OF STRUCTURED REG EXPs ");
		for(int i=0; i<numOfPatterns; i++) {
			System.out.println(" ] " + regExPats[i]);
			System.out.println(" > " + structPats[i]);
			System.out.println();
		}

		System.out.println(" CONNECTIONs ");
		for(int i=0; i<numOfConnections; i++) {
			System.out.println(" - " + queryStringsConnectors[i] );
			System.out.println("    Dist   " + conMin[i] + " : " + conMax[i] + " from " + conStart[i] + " to " + conEnd[i]) ;
			System.out.println("    Angle  " + angleMin[i] + " : " + angleMax[i] );
			//System.out.println("    Cavity " + cavityMin[i] + " : " + cavityMax[i] );
		}
	}

	public String getOriginalQuery() {
		return originalQuery;
	}

	public int getNumOfConnections() {
		return numOfConnections;
	}

	public double[] getConMin() {
		return conMin;
	}

	public double[] getConMax() {
		return conMax;
	}

	public Pattern[] getRegExPatternSet() {
		return regExPatternSet;
	}

	public String[] getSecStructPats() {
		return structPats;
	}

	public char[] getConStartPeptBond() {
		return conStart;
	}

	public char[] getConEndPeptBond() {
		return conEnd;
	}

	public double[] getAngleMin() {
		return angleMin;
	}

	public double[] getAngleMax() {
		return angleMax;
	}

	public double[] getCavityMin() {
		return cavityMin;
	}

	public double[] getCavityMax() {
		return cavityMax;
	}

	public boolean isNullConnection(int i) {
		return (conComponents[i].indexOf('_') > 0);
	}

	public boolean connnectionHasDistThreshold(int i) {
		return (conComponents[i].indexOf('-') > 0);
	}

	public boolean connnectionHasAngleThreshold(int i) {
		return (conComponents[i].indexOf('^') > 0);
	}

	public boolean connnectionHasCavityThreshold(int i) {
		return (conComponents[i].indexOf('~') > 0);
	}


}
