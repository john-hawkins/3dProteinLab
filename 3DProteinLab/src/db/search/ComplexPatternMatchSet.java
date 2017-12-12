package db.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Vector;

/*
 * This class will find all instances of a set of regular expression matches
 * within a given sequence. It contains methods that then allow you to iterate 
 * over all possible combinations of those matches so that you can check an
 * alternative set of features.
 */


public class ComplexPatternMatchSet {
	
	String[] seqs;
	String[] identifiers;
	int numOfPatterns;
	int[] numOfMatches;
	String[][] seqIdentifier;
	int[][] startPositions;
	int[][] endPositions;
	
	Matcher[][] matcherSet;
	Pattern[] setOfPats;
	
	int numOfValidMatchSets;
	int[][] validMatchSets;
	
	// Variable used for iterating overt the matching positions
	int[] matchPosIterationCounter;
	
	/*
	 * ----------------------------------------------------------
	 * METHODS FOR THE NEW RECURSIVE BACTRACKING ITERATOR
	 */
	public void initCounter() {
		matchPosIterationCounter= new int[numOfPatterns];
		for(int i=0; i<numOfPatterns; i++) {
			matchPosIterationCounter[i] = 0;
		}
		previousPositions = new HashMap<Integer, String[]>();
		blackList = new HashMap<Integer, Vector<String[]>>();
	}

	public String[] getNextMatchPosition(int pos) {
		String[] result = null;
		if(pos < numOfPatterns) {
			if(matchPosIterationCounter[pos]==numOfMatches[pos]) {
				// We have reached the last possibility
				result =  new String[]{ "#", "", "" };
			} else {
				result = new String[3];
				result[0] = seqIdentifier[pos][matchPosIterationCounter[pos]];
				result[1] = "" + startPositions[pos][matchPosIterationCounter[pos]];
				result[2] = "" + endPositions[pos][matchPosIterationCounter[pos]];
				matchPosIterationCounter[pos]++;
				outputCounterToErrStream();
			}
			if( overlapsWithPreviousPositions(result, pos) || inBlackList( result ) ) {
				result = getNextMatchPosition(pos);
			} else {
				// Then this is the version that returns the result so remove the previous entry in hashmap
				previousPositions.remove(pos);
				previousPositions.put(pos, result);
			}
		}
		return result;
	}
	
	HashMap<Integer, String[]> previousPositions;
	HashMap<Integer, Vector<String[]>> blackList;
	
	private boolean overlapsWithPreviousPositions(String[] result, int pos) {
		if( result[0].equals("#") )
			return false;
		int myStart = Integer.parseInt(result[1]); 
		int myEnd = Integer.parseInt(result[2]); 
		for(int i=0; i<pos; i++) {
			String[] prev = previousPositions.get(i);
			if(prev[0].equals(result[0]) ) {
				int prevStart = Integer.parseInt(prev[1]);
				int prevEnd = Integer.parseInt(prev[2]);
				if(		(myStart>=prevStart && myStart<=prevEnd) || 
						(myEnd>=prevStart && myEnd<=prevEnd) || 
						(prevStart>=myStart && prevStart<=myEnd) || 
						(prevEnd>=myStart && prevEnd<=myEnd)
						) { // Then we have an overlap 
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean inBlackList(String[] result) {
		if( result[0].equals("#") )
			return false;
		int myStart = Integer.parseInt(result[1]); 
		int myEnd = Integer.parseInt(result[2]); 
		for(int i=0; i<numOfPatterns; i++) {
			Vector<String[]> theList = blackList.get(i);
			
			if(theList==null) {
				return false;
			} else {
				for(int k=0; k<theList.size(); k++) {
					String[] prev = theList.get(k);
					if(prev[0].equals(result[0]) ) {
						int prevStart = Integer.parseInt(prev[1]);
						int prevEnd = Integer.parseInt(prev[2]);
						if(		(myStart>=prevStart && myStart<=prevEnd) || 
								(myEnd>=prevStart && myEnd<=prevEnd) || 
								(prevStart>=myStart && prevStart<=myEnd) || 
								(prevEnd>=myStart && prevEnd<=myEnd)
								) { // Then we have an overlap 
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	/*
	 * This method is called when a valid match is found.
	 * It stores all matching positions in the black list array.
	 * So that we can be certain that further matches do not overlap with the current one.
	 */
	public void blackListCurrent() {
		for(int i=0; i<numOfPatterns; i++) {
			String[] result = new String[3];
			result[0] = seqIdentifier[i][matchPosIterationCounter[i]-1];
			result[1] = "" + startPositions[i][matchPosIterationCounter[i]-1];
			result[2] = "" + endPositions[i][matchPosIterationCounter[i]-1];
			Vector<String[]> theList = blackList.get(i);
			if(theList==null)
				theList = new Vector<String[]>();
			theList.add(result);
			blackList.put(i, theList);
		}

	}
	
	
	private void outputCounterToErrStream() {
		System.err.print(" COUNTER = ");
		for(int i=0; i<numOfPatterns; i++) {
			if((matchPosIterationCounter[i]-1) > -1)
				System.err.print(seqIdentifier[i][matchPosIterationCounter[i]-1]);
			else 
				System.err.print("_");
			System.err.print( "[" + (matchPosIterationCounter[i]-1) + "]");
		}
		System.err.print("\n");
	}

	public void resetCounter(int pos) {
		matchPosIterationCounter[pos] = 0;
	}
	/*
	 * END METHODS FOR THE NEW RECURSIVE BACTRACKING ITERATOR
	 * -----------------------------------------------------------
	 */
	
	boolean foundAll = false;

	
	protected ComplexPatternMatchSet() {
		// This is just for the sub-classes
		// DO NOT USE
	}
	
	public ComplexPatternMatchSet( Pattern[] sPats) {
		super();
		this.setOfPats = sPats;
		this.numOfPatterns = sPats.length;
		this.matcherSet = new Matcher[this.numOfPatterns][];

		this.startPositions = new int[this.numOfPatterns][];
		this.endPositions = new int[this.numOfPatterns][];
		this.seqIdentifier =  new String[this.numOfPatterns][];
		
		this.numOfMatches = new int[this.numOfPatterns];
		validMatchSets = new int[MAXMATCHSETS][this.numOfPatterns];
	}
	

	public void matchSequences(String[] sqs, String[] ids) {

		this.seqs = sqs;
		this.identifiers = ids;
		// Iterate over each of the patterns
		for(int i=0; i<this.numOfPatterns; i++) {
			// Now iterate over each of the sequences 

			numOfMatches[i] = 0;
			Vector<Integer> starts = new Vector<Integer>();
			Vector<Integer> ends = new Vector<Integer>();
			Vector<String> idents = new Vector<String>();

			matcherSet[i] = new Matcher[seqs.length];
			
			for(int s=0; s<this.seqs.length; s++) {
				matcherSet[i][s] = setOfPats[i].matcher(seqs[s]);
				while(matcherSet[i][s].find()) {
					starts.add( matcherSet[i][s].start() + 1 ); // Not sure if we should do this here or not
					ends.add( matcherSet[i][s].end() );
					idents.add(identifiers[s]);
					numOfMatches[i]++;
				}
			}
			
			this.startPositions[i] = new int[numOfMatches[i]];
			this.endPositions[i] = new int[numOfMatches[i]];
			this.seqIdentifier[i] = new String [numOfMatches[i]];
			for(int m=0; m<numOfMatches[i];m++) {
				this.startPositions[i][m] =  starts.get(m);
				this.endPositions[i][m] =  ends.get(m);
				this.seqIdentifier[i][m] = idents.get(m);
			}
		}

		validMatchSets = new int[MAXMATCHSETS][this.numOfPatterns];
		calculateValidMatchsets();
	}
	
	
	
	/*
	 * isFoundAll()
	 * 
	 * Check that a match is found for all patterns
	 */
	public boolean isFoundAll() {
		foundAll = true;
		for(int i=0; i<this.numOfPatterns; i++) {
			if(numOfMatches[i]==0)
				foundAll = false;
		}
		return foundAll;
	}
	
	/*
	 * Return the number of patterns matched in this match set
	 */
	public int getNumberOfPatterns() {
		// TODO Auto-generated method stub
		return numOfPatterns;
	}

	
	/*
	 * THESE METHODS WILL ALL BE REMOVED BECAUSE THEY WERE PART OF THE INITIAL BRUTE FORCE METHOD
	 * 
	 * DEPRECATED
	 */
	
	// Limit the number of permutations of patterns
	// to be checked, more that 10,000 is absurd really
	protected int MAXMATCHSETS = 10000;
	/*
	 * Determine all the valid combinations of individual
	 * pattern matches
	 * 
	 * DEPRECATED
	 */
	public void calculateValidMatchsets() {

		numOfValidMatchSets = 0;
		initIterations();
		int[][] matchSites = nextPermutation();
		while(matchSites != null ) {
			if( ! testForOverlap(matchSites) ) {
				//int[] newMatchSet = new int[this.numOfPatterns];
				for(int i=0; i<numOfPatterns; i++) {
					validMatchSets[numOfValidMatchSets][i] = iterationCounter[i];
					//newMatchSet[i]= iterationCounter[i];
				}     
				//validMatchingSets.add(newMatchSet);
				numOfValidMatchSets++;
			}
			if(numOfValidMatchSets==MAXMATCHSETS)
				break;
			matchSites = nextPermutation();
		}
	}
	
	/*
	 * Check this particular set of start and end positions for overlap
	 */
	private boolean testForOverlap(int[][] matchSites) {
		// Iterate over the set of matches and test for clashes in position
		for(int i=1; i<matchSites.length; i++) {
			for(int j=0; j<i; j++) {
				//System.out.println("Checking for clash between matches " + 
				//		i + "[" + matchSites[i][0] + "," + matchSites[i][1] + "] and " + 
				//		j + "[" + matchSites[j][0] +"," + matchSites[j][1] + "]" );
				
				if( (matchSites[i][0] <= matchSites[j][0] && matchSites[j][0] <= matchSites[i][1]) ||
						(matchSites[i][0] <= matchSites[j][1] && matchSites[j][1] <= matchSites[i][1]) )
					return true;
			}
		}
		
		return false;
	}

	int[] iterationCounter;
	
	/* getMatchSet
	 * A method for extracting the start and end site of a
	 * particular set of matches.
	 */
	public int[][] getMatchSet(int[] matchNums) {
		
		if(matchNums !=null && matchNums.length == numOfPatterns) {
			
			int[][] results = new int[numOfPatterns][2];
			for(int i=0; i<this.numOfPatterns; i++) {
				results[i][0] = startPositions[i][matchNums[i]];
				results[i][1] = endPositions[i][matchNums[i]];
			}
			return results;
			
		} else {
			return null;
		}
	}
	
	
	/* getMatchSet
	 * A method for extracting the start and end site of a
	 * particular set of matches.
	 */
	public String[] getMatchChainSet(int[] matchNums) {
		
		if(matchNums !=null && matchNums.length == numOfPatterns) {
			
			String[] results = new String[numOfPatterns];
			for(int i=0; i<this.numOfPatterns; i++) {
				results[i] = seqIdentifier[i][matchNums[i]];
			}
			return results;
			
		} else {
			return null;
		}
	}
	
	
	/*
	 * Set up method for beginning an iteration over all
	 * permutations of combinations of 
	 * of matches 
	 */
	public void initIterations() {
		iterationCounter = null;
	}
	
	
	/* 
	 * Increment
	 * 
	 * We want to increment the permutation counter variable
	 * in a systematic fashion to make sure that we are checking
	 * all possible combinations of matching elements
	 * 
	 */
	public void increment() {
		// We increment if we can
		boolean foundIncrement = false;
		for(int i=0; i<this.numOfPatterns; i++) {
			if( iterationCounter[i] < (numOfMatches[i]-1) ) {
				iterationCounter[i]++;
				for(int j=i-1; j>=0; j--)
					iterationCounter[j] = 0;
				foundIncrement = true;
				break;
			}
		}
		// Otherwise we set to null
		if(foundIncrement!=true)
			iterationCounter = null;
	}
	
	/* nextPermutation
	 * 
	 * get the actual start stop sites for the next permutation
	 * of the combination of pattern match sites.
	 * 
	 * The integer array first index is across patterns
	 * second index gives the start=0 and end=1 sites
	 */
	public int[][] nextPermutation() {
		
		if(iterationCounter == null) {
			// NULL Value means that this is the first call on
			// this iteration so we initialise the counter
			// but first check that we have hits on all patterns
			if(isFoundAll()) {
				iterationCounter = new int[numOfPatterns];
				for(int i=0; i<this.numOfPatterns; i++) {
					iterationCounter[i] = 0;
				}
			}
		} else {
			// Increment the counter
			increment();
		}
		
		if(iterationCounter == null) {
			// A null here means that we have already done the
			// last permutation and the counter is now reset
			return null;
		} else {
			int[][] results = getMatchSet(iterationCounter);
			return results;
		}
	}

	public int getNumOfValidMatchSets() {
		return numOfValidMatchSets;
	}

	public int[][] getValidMatchSets() {
		int[][] returnVal = new int[numOfValidMatchSets][];
		for(int i=0; i<numOfValidMatchSets; i++){
			returnVal[i] = validMatchSets[i];
		}
		return returnVal;
	}
	/*
	public int[][] getValidMatchSets() {
		int[][] returnVal = new int[validMatchingSets.size()][];
		for(int i=0; i<validMatchingSets.size(); i++){
			returnVal[i] = validMatchingSets.elementAt(i);
		}
		return returnVal;
	}*/


}
