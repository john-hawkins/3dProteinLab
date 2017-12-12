package db.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;

/*
 * This class will find all instances of a set of regular expression matches
 * within a given sequence. It contains methods that then allow you to iterate 
 * over all possible combinations of those matches so that you can check an
 * alternative set of features.
 */


public class PatternMatchSet {
	String seq;
	int numOfPatterns;
	int[] numOfMatches;
	int[][] startPositions;
	int[][] endPositions;
	
	Matcher[] matcherSet;
	Pattern[] setOfPats;
	
	int numOfValidMatchSets;
	int[][] validMatchSets;
	
	//Vector<int[]> validMatchingSets;
	
	boolean foundAll = false;
	
	// Limit the number of permutations of patterns
	// to be checked, more that 10,000 is absurd really
	protected int MAXMATCHSETS = 10000;
	
	protected PatternMatchSet() {
		// This is just for the sub-classes
		// DO NOT USE
	}
	
	public PatternMatchSet( Pattern[] sPats) {
		super();
		this.setOfPats = sPats;
		this.numOfPatterns = sPats.length;
		this.matcherSet = new Matcher[this.numOfPatterns];

		//this.startPositions = new int[this.numOfPatterns][1000];
		//this.endPositions = new int[this.numOfPatterns][1000];
		this.startPositions = new int[this.numOfPatterns][];
		this.endPositions = new int[this.numOfPatterns][];
		
		this.numOfMatches = new int[this.numOfPatterns];
		validMatchSets = new int[MAXMATCHSETS][this.numOfPatterns];
	}
	
	public PatternMatchSet(String seq, Pattern[] sPats) {
		
		super();
		//System.err.print("Searching for " + sPats[0] + " in " + seq);
		this.seq = seq;
		this.setOfPats = sPats;
		this.numOfPatterns = sPats.length;
		this.matcherSet = new Matcher[this.numOfPatterns];
		//this.startPositions = new int[this.numOfPatterns][200];
		//this.endPositions = new int[this.numOfPatterns][200];
		this.startPositions = new int[this.numOfPatterns][];
		this.endPositions = new int[this.numOfPatterns][];
		
		this.numOfMatches = new int[this.numOfPatterns];
		
		/*
		for(int i=0; i<this.numOfPatterns; i++) {

			matcherSet[i] = setOfPats[i].matcher(seq);
			while(matcherSet[i].find()) {
				this.startPositions[i][numOfMatches[i]] =  matcherSet[i].start();
				this.endPositions[i][numOfMatches[i]] =  matcherSet[i].end();
				numOfMatches[i]++;
			}
		}
	
		validMatchSets = new int[MAXMATCHSETS][this.numOfPatterns];
		calculateValidMatchsets();
		*/
		
		matchSequence(seq);
		
	}

	public void matchSequence(String seq) {

		this.seq = seq;
		
		for(int i=0; i<this.numOfPatterns; i++) {
			numOfMatches[i] = 0;
			matcherSet[i] = setOfPats[i].matcher(seq);
			Vector<Integer> starts = new Vector<Integer>();
			Vector<Integer> ends = new Vector<Integer>();
			while(matcherSet[i].find()) {
				//starts.add( matcherSet[i].start() );
				starts.add( matcherSet[i].start() + 1 ); // Not sure if we should do this here or not
				ends.add( matcherSet[i].end() );
				numOfMatches[i]++;
			}
			this.startPositions[i] = new int[numOfMatches[i]];
			this.endPositions[i] = new int[numOfMatches[i]];
			for(int m=0; m<numOfMatches[i];m++) {
				this.startPositions[i][m] =  starts.get(m);
				this.endPositions[i][m] =  ends.get(m);
			}
		}
		
		//validMatchingSets = new Vector<int[]>();

		validMatchSets = new int[MAXMATCHSETS][this.numOfPatterns];
		calculateValidMatchsets();
	}
	
	
	/*
	 * Determine all the valid combinations of individual
	 * pattern matches
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

	
	/*
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
	 * Check that all patterns are found and that there
	 * is no overlap in their positions in the sequence
	 * 
	 * THIS METHOD IS NOW REDUNDANT
	 */
	public boolean isFoundAllNoOverlap() {
		
		int [] startPs = new int[this.numOfPatterns];
		int [] endPs = new int[this.numOfPatterns];
		int [] indexes = new int[this.numOfPatterns];
		for(int i=0; i<this.numOfPatterns; i++) {
			indexes[i] = -1;
		}
		
		foundAll = false;
		
		for(int i=0; i<this.numOfPatterns; i++) {
			if(indexes[i] < numOfMatches[i]-1)
				indexes[i]++;
			else
				break;
			startPs[i] = this.startPositions[i][indexes[i]];
			endPs[i] = this.endPositions[i][indexes[i]];
				
			boolean independent = false;
			// Now check for a clash with previous matches
			if(i>0) {
				for(int j=i-1; j>-1; j--) {
				
					System.out.println("Checking for clash between matches " + 
						i + "[" +startPs[i] +"," + endPs[i]+ "] and " + 
						j + "[" +startPs[j] +"," + endPs[j]+ "]" );
					if( (startPs[i] <= startPs[j] && startPs[j] <= endPs[i]) ||
						(startPs[i] <= endPs[j] && endPs[j] <= endPs[i])   ) {
						// We have an overlap, so we check if this is the 
						// last possible match at this position
						System.out.print(" CLASHING");
						if(indexes[i] == numOfMatches[i]-1) {
							System.out.print(" REWIND");
							// It is so we rewind back to the conflict
							for(int p=i; p>j; p--) {
								indexes[p] = -1;
							}
							i = j;
						} else {
							System.out.print(" GO AGAIN");
							//We need to get the next match at the current i
							// so we decrement in order that we will return
							// to the same i next time through the main loop
							i--;
						}
						System.out.println();
						break;
					}
					independent = true;
				}
			}
			if( (i==this.numOfPatterns-1)&& independent) {
				foundAll = true;
			}
		}
		return foundAll;
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
