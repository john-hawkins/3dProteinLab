package db.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import structools.structure.AtomSite;
import structools.structure.Protein;
import structools.structure.ProteinComplex;

import db.DatabaseConnectionProperties;
import db.DatabaseConnectionProvider;
import db.SimpleDatabaseConnectionProviderImpl;
import db.db2.XmlRPdbDAO;
import db.mysql.minimalPdbDAO;

import sim.ProgressMeter;

public class StructExpQueryExecutor extends DBQueryExecutor {

	protected DatabaseConnectionProvider connectionProvider;
	protected Connection con;
	
	/* 
	 * CONSTRUCTOR
	 */
	public StructExpQueryExecutor(String[] dbprops) {
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
	public List<MatchingComplex> executeQuery(DBQuery query, ProgressMeter pm) {
		
		List<MatchingComplex> finalSet = new LinkedList<MatchingComplex>();
		//List<Protein> matchingEntities;
		List<ProteinComplex> matchingEntities;
		
		try {
			if(query.useSavedSearch() ) {
				finalSet = dao.retrieveSavedSearch( query );
				if(finalSet != null) {
					pm.initializeMeter(finalSet.size());
					pm.finaliseMeter();
					pm.printTimerResults();
					return finalSet;
				}
			}
			
			finalSet = new LinkedList<MatchingComplex>();
			

			if( query.getPdbID() == null || query.getPdbID().equals("")) {
					//matchingEntities = dao.getProteinStructPatMatch( (StructExpQuery) query );
					matchingEntities = dao.getProteinComplexStructPatMatch( (StructExpQuery) query );
			} else {
					//matchingEntities = dao.getProteins( query.getPdbID() );
					matchingEntities = dao.getProteinComplex( query.getPdbID() );
			}


			Pattern[] setOfPats = query.getRegExPatternSet();

			if(pm != null ) {
				pm.initializeMeter(matchingEntities.size());
			}
			
			System.err.println("Retrieved: " + matchingEntities.size() + " Protein Complexes from Database !");
			
			for (ProteinComplex e : matchingEntities) {

					// Now we match all of the regular expression patterns
					
					//String seq = e.getSeq_and_sse(); 
					// If the pattern looks for specific binding states of Cysteine then we need a different sequence
					//if( query.usesCysteineBondCodes() ) {
					//	seq = e.getSeq_and_sse_and_dis_bonds();
					//}
					
					//PatternMatchSet ms = new PatternMatchSet(seq, setOfPats);

					// THIS IS THE NEW VERSION : PATTERN MATCHES ARE DETERMINED IN THE DATABASE OBJECT
					ComplexPatternMatchSet ms = e.getPatternMatches();
					
					if( ms.getNumOfValidMatchSets() > 0 ) {
						/* WE NOW HAVE A COLLECTION OF POSITIONS THAT MATCH THE REGULAR EXPRESSION VERSION */
						/* ----- WE NEED TO CHECK WHETHER THE OTHER CRITERIA ALSO HOLD TRUE ------------ */ 

						if(query.getNumOfPatterns() == 1) {
							// We are already finished just add any matches to the list
							
							if(query.isMultipleHitsPerStructure() ) {
								int max = ms.getNumOfValidMatchSets();
								if(max > query.getMaxMultipleHits())
									max =query.getMaxMultipleHits();
								for(int i= 0; i<max; i++) {
									MatchingComplex ma = new MatchingComplex(e, ms.getMatchSet(new int[] {i} ), ms.getMatchChainSet(new int[] {i}) );
									//MatchingComplex(ProteinComplex pc, int[][] matchPositions, String[] chainIds)
									dao.fillSCOP4Match(ma);
									dao.fillPfam4Match(ma);
									dao.fillCath4Match(ma);
									dao.calcAuthPositions(ma);
									finalSet.add(ma);
								}
							} else {
								MatchingComplex ma = new MatchingComplex(e, ms.getMatchSet(new int[] {0} ), ms.getMatchChainSet(new int[] {0}) );
								//MatchingComplex(ProteinComplex pc, int[][] matchPositions, String[] chainIds)
								dao.fillSCOP4Match(ma);
								dao.fillPfam4Match(ma);
								dao.fillCath4Match(ma);
								dao.calcAuthPositions(ma);
								finalSet.add(ma);
							}

						
						} else {
							// System.err.println("Validating Connectors on Protein " + e.getPdbId() + ":" + e.getChainId());
							// Iterate over each pattern and check the connection between the components
							/* OLD VERSION
							int[][] match = validateConnectors( e, ms, (StructExpQuery) query);
							if(match != null) {
								MatchingStructure ma = new MatchingStructure(e, match);
								dao.fillSCOP4Match(ma);
								dao.fillPfam4Match(ma);
								dao.fillCath4Match(ma);
								dao.calcAuthPositions(ma);
								finalSet.add(ma);	
							}
							*/
							//System.err.println("CHECKING CONNECTORS ");
							MatchingComplex[] maCos = validateComplexConnectorsRec( e, ms, (StructExpQuery) query);
							if(maCos != null) {
								for(int m=0; m<maCos.length; m++) {
									//System.err.println("VALID CONNECTOR FOUND ");
									dao.fillSCOP4Match(maCos[m]);
									//System.err.println("FILLED SCOP");
									dao.fillPfam4Match(maCos[m]);
									//System.err.println("FILLED PFAM");
									dao.fillCath4Match(maCos[m]);
									//System.err.println("FILLED CATH");
									dao.calcAuthPositions(maCos[m]);
									//System.err.println("FILLED AUTH POSITIONS");
									finalSet.add(maCos[m]);	
								}
							}
						}

					}


				if(pm != null )
					pm.updateMeter();
			}
			if(pm != null ) {
				pm.finaliseMeter();
				pm.printTimerResults();
			}

		} catch(SQLException e) {
				System.err.println("SQL error while executing query: " + e);
				System.exit(0);
		}
		return finalSet;
	}

	/*
	 * 
	 * @see db.search.DBQueryExecutor#executeQuery(java.lang.String, java.lang.String, db.search.ProgressMeter)
	 */
	public List<MatchingStructure> executeQuery(String q, String searchProps, ProgressMeter pm) {
		try {
			List<MatchingStructure> finalSet = dao.getPolyEntitiesRegExpMatch( q, searchProps, pm );
			return finalSet;
			
		} catch(SQLException e) {
			System.err.println("SQL error while executing query: " + e);
			System.exit(0);
		}
		return null;
	}
	
	
	/*
	 * We check all of the potential matches until we find something that meets the 
	 * structural criteria.
	 * 
	 * A new version that uses a version of the recursive backtracking algorithm
	 */
	private MatchingComplex[] validateComplexConnectorsRec(ProteinComplex e, ComplexPatternMatchSet ms, StructExpQuery query) throws SQLException {
		int numChecked = 0;
		MatchingComplex[] theResults;
		int numHits = 0;
		if(query.isMultipleHitsPerStructure() ) {
			theResults = new MatchingComplex[query.getMaxMultipleHits()];
		} else {
			theResults = new MatchingComplex[1];
		}
			
		// Initialise the counters
		ms.initCounter();
		String[][] matchPositions = new String[ms.getNumberOfPatterns()][];
		// We begin an iteration over each of the potential match positions
		int pos = 0;
		while( pos<ms.getNumberOfPatterns() ) {
			matchPositions[pos] = ms.getNextMatchPosition(pos);
			numChecked++;
			// FIRST CHECK IF WE HAVE USED ALL AVAILABLE POSITIONS
			if(matchPositions[pos][0].equals("#")) {
				ms.resetCounter(pos);
				if(pos==0) { // Then we have no more options in the first positions so bail
					if(numHits==0) {
						System.err.println("Checked " + numChecked + " possibilities, and found zilch !!!!");
						return null;
					} else {
						MatchingComplex[] finalResults = new MatchingComplex[numHits];
						for(int h=0; h<numHits; h++) {
							finalResults[h]= theResults[h] ;
						}
						return finalResults;
					}
				} else {
					pos--;
				}
			} else {
				if(pos==0) {
					// We simply increment the positions and move on
					pos++;
				} else {
					// We need to check that the criteria hold for the two match positions
					if( positionsMatchConnector(e, matchPositions[pos-1], matchPositions[pos], pos-1, query ) ) {
						// Then we are successful for increment the position and move on
						pos++;
						if(pos==ms.getNumberOfPatterns()) { // We may have the answer
							
							if(query.isCircularConnectors()) {
								// Then we need to check the final loop connector
								System.err.println("Checking the final loop connector from : 0 to " + (pos-1) );
								if( ! positionsMatchConnector(e, matchPositions[pos-1], matchPositions[0], pos-1, query ) ) {
									pos--; // It didn't match so keep looking
								}
							} 
							
						}
					} 
					// Otherwise we leave the counter in the current position to re-enter the loop
					// The call to ms.getNextMatchPosition(pos) will get the next matching sequence at that position
				}
			}
			if(pos==ms.getNumberOfPatterns()) { 
				// IF THIS IS TRUE THEN WE HAVE FOUND A MATCH FOR THE FINAL POSITION
				MatchingComplex myresult = new MatchingComplex(e, matchPositions);
				if(query.isMultipleHitsPerStructure() ) {
						theResults[numHits] = myresult;
						numHits++;
						if(numHits<query.getMaxMultipleHits()) {
							// We can still search for more matches
							ms.blackListCurrent();
							pos--;
							while(pos > 0) {
								ms.resetCounter(pos);
								pos--;
							}
						}
				} else {
						theResults = new MatchingComplex[1];
						theResults[0] = myresult;
						return theResults;
				}

			}
		}
		// We should never arrive here
		System.err.println("IN-CON-CEIV-A-BLE !!!!! ");
		return null;
	}

	
	
	private boolean positionsMatchConnector(ProteinComplex e, String[] pos1, String[] pos2, int conNum, StructExpQuery query) throws SQLException {
		//System.out.print("Testing connection  " + conNum );

		String firstChainId = pos1[0];
		String secondChainId = pos2[0];
		int matchSiteOneN = Integer.parseInt(pos1[1]);
		int matchSiteOneC = Integer.parseInt(pos1[2]);
		int matchSiteTwoN = Integer.parseInt(pos2[1]);
		int matchSiteTwoC = Integer.parseInt(pos2[2]);

		//System.err.print(" Validating Match " +  firstChainId + ":" +matchSiteOneN + ":" +  matchSiteOneC + " to " + secondChainId+ ":" +matchSiteTwoN + ":" +  matchSiteTwoC +" \n");
		
		// THE PROPERTIES OF THE CONNECTIONS
		double[] conMaxDists =  query.getConMax();
		double[] conMinDists =  query.getConMin();
		double[] conMaxAngles =  query.getAngleMax();
		double[] conMinAngles =  query.getAngleMin();
		char[] conStartPep =  query.getConStartPeptBond();
		char[] conEndPep =  query.getConEndPeptBond();
		
		// First check if this connection is a NULL connection
		if(query.isNullConnection(conNum)) {
			return true;
			
		} else {
			// NOW WE CHECK EACH OF THE CONNECTION PROERTIES
			boolean matchedDistance = false;
			boolean matchedAngle = false;
			
			if(query.connnectionHasDistThreshold(conNum)) {
				
				//System.out.print(" : distance threshold  " + conMinDists[i] + " : " + conMaxDists[i] + "\n");
				int[] seqPat1Ends;
				int[] seqPat2Ends;
				// We need to check if the distance must be 
				// between specific points on the peptide chain
				// start with the first seq pattern
				if(conStartPep[conNum]=='N') {
					seqPat1Ends = new int[1];
					seqPat1Ends[0] = matchSiteOneN;
				} else if(conStartPep[conNum]=='C') {
					seqPat1Ends = new int[1];
					seqPat1Ends[0] = matchSiteOneC;
				} else {
					seqPat1Ends = new int[2];
					seqPat1Ends[0] = matchSiteOneN;
					seqPat1Ends[1] = matchSiteOneC;	
				}
				// Now the second seq pattern
				if(conEndPep[conNum]=='N') {
					seqPat2Ends = new int[1];
					seqPat2Ends[0] = matchSiteTwoN;
				} else if(conEndPep[conNum]=='C') {
					seqPat2Ends = new int[1];
					seqPat2Ends[0] = matchSiteTwoC;
				} else {
					seqPat2Ends = new int[2];
					seqPat2Ends[0] =  matchSiteTwoN;
					seqPat2Ends[1] =  matchSiteTwoC;	
				}
				
				boolean seqPatsWithinThreshold = false;
				// Now we iterate over all possible
				for(int x=0; x<seqPat1Ends.length; x++) {

					//System.out.print(" SEQ1 end " + x + " = " +  seqPat1Ends[x] + " \n");
					// GET ALPHA CARBON POSITION
					List<AtomSite> alphaPos1;
					if( query.usePseudoAtoms() ) {
						alphaPos1 = dao.getPseudoAtom(e.getPdbId(), firstChainId, seqPat1Ends[x]);
					} else {
						alphaPos1 = dao.getAtomSite(e.getPdbId(), firstChainId, seqPat1Ends[x], "CA");
					}
					if(!alphaPos1.isEmpty() ) {
						for(int y=0; y<seqPat2Ends.length; y++) {
							//System.out.print(" SEQ2 end  " + y + " = " + seqPat2Ends[y]);
							List<AtomSite> alphaPos2;
							if( query.usePseudoAtoms() ) {
								alphaPos2 = dao.getPseudoAtom(e.getPdbId(), secondChainId, seqPat2Ends[y] );
							} else {
								alphaPos2 = dao.getAtomSite(e.getPdbId(), secondChainId, seqPat2Ends[y], "CA");
							}
							if(!alphaPos2.isEmpty() ) {
								Double dist = AtomSite.calcDistance(alphaPos1.get(0), alphaPos2.get(0));
								//System.out.print(" Distance value: " + dist + " \n");
								//System.out.print(" Threshold  " + conMinDists[conNum] + " : " + conMaxDists[conNum] + "\n");
								if(dist >= conMinDists[conNum] && dist <= conMaxDists[conNum]) {
									//System.out.print(" Match Found \n");
									seqPatsWithinThreshold = true;
									matchedDistance = true;
									break;
								}
							}
						}
						if(seqPatsWithinThreshold)
							break;
					}
				}
				if(!seqPatsWithinThreshold) // THEN WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
					return false;
			} else {
				matchedDistance = true;
			}
			
			if(query.connnectionHasAngleThreshold(conNum)) {
				// We extract the positions of the end residues and use that 
				// to define vectors and calculate angles. 
				List<AtomSite> start1 = dao.getAtomSite(e.getPdbId(), firstChainId, matchSiteOneN, "N");
				List<AtomSite> end1 = dao.getAtomSite(e.getPdbId(), firstChainId, matchSiteOneC, "C");
				List<AtomSite> start2 = dao.getAtomSite(e.getPdbId(), secondChainId, matchSiteTwoN, "N");
				List<AtomSite> end2 = dao.getAtomSite(e.getPdbId(), secondChainId, matchSiteTwoC, "C");
				if(!start1.isEmpty() && !end1.isEmpty() && !start2.isEmpty() && !end2.isEmpty() ) {
					Double angle = AtomSite.calcAngle(start1.get(0), end1.get(0), start2.get(0), end2.get(0));
					//System.out.println("The Angle is " + angle);
					if( angle >= conMinAngles[conNum] && angle <= conMaxAngles[conNum] ) {
						matchedAngle = true;
					} else // WE DIDN'T SATISFY THE ANGLE REQUIREMENTS
						return false;
				} else {
					// WE DIDN'T SATISFY THE ANGLE REQUIREMENTS
					return false;
				}
			} else {
				matchedAngle = true;
			}
			
			// THIS IS NOT IMPLEMENTED YET
			if(query.connnectionHasCavityThreshold(conNum)) {
			}
			
			if(matchedDistance && matchedAngle)
				return true;
			else 
				return false;
			//System.out.println("Connection " + i + " distance " + matchedDistance + " angle " + matchedAngle );
		}
	}
	

	@Override
	public int saveSearch(DBQuery query, List<MatchingComplex> finalSet) {
		try {
			System.err.println("SAVING SEARCH");
			int qId = dao.saveQuery(  query, finalSet );
			return qId;
			
		} catch(SQLException e) {
			System.err.println("SQL error while saving the query results: " + e);
			e.printStackTrace(System.err);
			System.exit(0);
		}
		return 0;
	}

	@Override
	public List<MatchingComplex> getSavedSearchResults(int queryid) {
		try {
			List<MatchingComplex> finalSet = dao.retrieveSavedSearch( queryid );
			return finalSet;
			
		} catch(SQLException e) {
			System.err.println("SQL error while executing query: " + e);
			System.exit(0);
		}
		return null;
	}

	
	
	/*
	 *  -------------------------------- DEPRECATED METHODS ------------------------------------
	 */
	
	
	
	/*
	 * We check all of the potential matches until we find something that meets the 
	 * structural criteria.
	 * 
	 * BRUTE FORCE APPROACH
	 * 
	 * DEPRECATED
	 */
	private MatchingComplex validateComplexConnectors(ProteinComplex e, ComplexPatternMatchSet ms, StructExpQuery query) throws SQLException {
		
		int[][] validmatchSets =  ms.getValidMatchSets();
		int[][] matchSites;
		String[] matchChains;

		double[] conMaxDists =  query.getConMax();
		double[] conMinDists =  query.getConMin();
		double[] conMaxAngles =  query.getAngleMax();
		double[] conMinAngles =  query.getAngleMin();
		char[] conStartPep =  query.getConStartPeptBond();
		char[] conEndPep =  query.getConEndPeptBond();
		
		for(int s=0; s<validmatchSets.length; s++) {
			
			matchSites = ms.getMatchSet(validmatchSets[s]);
			matchChains = ms.getMatchChainSet(validmatchSets[s]);
				
			if(matchSites == null)
				break;
			
			// Now we iterate over the all the pairwise
			// combinations in the set
			boolean[] matchConnectors = new boolean[query.getNumOfConnections()];
			
			for(int i=0; i<query.getNumOfConnections(); i++) {
			
				//System.out.print("Testing connection  " + i );
				String firstChainId = matchChains[i];
				String secondChainId = matchChains[i+1];
				
				// First check if this connection is a NULL connection
				if(query.isNullConnection(i)) {
					matchConnectors[i] = true;
				} else {
					matchConnectors[i] = false;
					// NOW WE CHECK EACH OF THE CONNECTION PROERTIES
					boolean matchedDistance = false;
					boolean matchedAngle = false;
					
					if(query.connnectionHasDistThreshold(i)) {
						//System.out.print(" : distance threshold  " + conMinDists[i] + " : " + conMaxDists[i] + "\n");
						int[] seqPat1Ends;
						int[] seqPat2Ends;
						// We need to check if the distance must be 
						// between specific points on the peptide chain
						// start with the first seq pattern
						if(conStartPep[i]=='N') {
							seqPat1Ends = new int[1];
							seqPat1Ends[0] = matchSites[i][0];
						} else if(conStartPep[i]=='C') {
							seqPat1Ends = new int[1];
							seqPat1Ends[0] = matchSites[i][1];
						} else {
							seqPat1Ends = new int[2];
							seqPat1Ends[0] = matchSites[i][0];
							seqPat1Ends[1] = matchSites[i][1];	
						}
						// Now the second seq pattern
						if(conEndPep[i]=='N') {
							seqPat2Ends = new int[1];
							seqPat2Ends[0] = matchSites[i+1][0];
						} else if(conEndPep[i]=='C') {
							seqPat2Ends = new int[1];
							seqPat2Ends[0] = matchSites[i+1][1];
						} else {
							seqPat2Ends = new int[2];
							seqPat2Ends[0] = matchSites[i+1][0];
							seqPat2Ends[1] = matchSites[i+1][1];	
						}
						
						boolean seqPatsWithinThreshold = false;
						// Now we iterate over all possible
						for(int x=0; x<seqPat1Ends.length; x++) {

							//System.out.print(" SEQ1 end " + x + " = " +  seqPat1Ends[x] + " \n");
							// GET ALPHA CARBON POSITION
							List<AtomSite> alphaPos1 = dao.getAtomSite(e.getPdbId(), firstChainId, seqPat1Ends[x], "CA");
							if(!alphaPos1.isEmpty() ) {
								for(int y=0; y<seqPat2Ends.length; y++) {
									//System.out.print(" SEQ2 end  " + y + " = " + seqPat2Ends[y]);
									List<AtomSite> alphaPos2 = dao.getAtomSite(e.getPdbId(), secondChainId, seqPat2Ends[y], "CA");
									if(!alphaPos2.isEmpty() ) {
										Double dist = AtomSite.calcDistance(alphaPos1.get(0), alphaPos2.get(0));
										//System.out.print(" Distance value: " + dist + " \n");
										//System.out.print(" Threshold  " + conMinDists[i] + " : " + conMaxDists[i] + "\n");
										if(dist >= conMinDists[i] && dist <= conMaxDists[i]) {
											//System.out.print(" Match Found \n");
											seqPatsWithinThreshold = true;
											matchedDistance = true;
											break;
										}
									}
								}
								if(seqPatsWithinThreshold)
									break;
							}
						}
						if(!seqPatsWithinThreshold) // THEN WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
							break;
					} else {
						matchedDistance = true;
					}
					
					if(query.connnectionHasAngleThreshold(i)) {
						// We extract the positions of the end residues and use that 
						// to define vectors and calculate angles. 
						int[] seqPat1Ends;
						int[] seqPat2Ends;
						seqPat1Ends = new int[2];
						seqPat1Ends[0] = matchSites[i][0];
						seqPat1Ends[1] = matchSites[i][1];	
						seqPat2Ends = new int[2];
						seqPat2Ends[0] = matchSites[i+1][0];
						seqPat2Ends[1] = matchSites[i+1][1];
						// Calculate the first vector
						List<AtomSite> start1 = dao.getAtomSite(e.getPdbId(), firstChainId, seqPat1Ends[0], "N");
						List<AtomSite> end1 = dao.getAtomSite(e.getPdbId(), firstChainId, seqPat1Ends[1], "C");
						List<AtomSite> start2 = dao.getAtomSite(e.getPdbId(), secondChainId, seqPat2Ends[0], "N");
						List<AtomSite> end2 = dao.getAtomSite(e.getPdbId(), secondChainId, seqPat2Ends[1], "C");
						if(!start1.isEmpty() && !end1.isEmpty() && !start2.isEmpty() && !end2.isEmpty() ) {
							Double angle = AtomSite.calcAngle(start1.get(0), end1.get(0), start2.get(0), end2.get(0));
							//System.out.println("The Angle is " + angle);
							if( angle >= conMinAngles[i] && angle <= conMaxAngles[i] ) {
								matchedAngle = true;
							} else //WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
								break;
						} else {
							// THEN WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
							break;
						}
					} else {
						matchedAngle = true;
					}
					
					if(query.connnectionHasCavityThreshold(i)) {
						
					}
					
					//System.out.println("Connection " + i + " distance " + matchedDistance + " angle " + matchedAngle );

				}
				// IF WE ARE HERE AND THE VALUE OF i IS FOR THE LAST CONNECTION
				// IT IS BECAUSE WE HAVE SATISFIED ALL THE CONDITIONS
				if (i == query.getNumOfConnections() - 1) {	
					return new MatchingComplex(e, matchSites, matchChains);
				}
			}
		}
		return null;
	}



	/*
	 * Iterate over this list of combination of SEQ patterns
	 * and check whether they satisfy the conditions for
	 * the connections between them.
	 * Return the sequence coordinates for the matching position
	 * 
	 * BRUTE FORCE ATTACK ON STRUCTURE MATCHES ONLY
	 * 
	 * DEPRECATED
	 */
	private int[][] validateConnectors(Protein e, PatternMatchSet ms, StructExpQuery query) throws SQLException {
		
		int[][] validmatchSets =  ms.getValidMatchSets();
		int[][] matchSites;

		double[] conMaxDists =  query.getConMax();
		double[] conMinDists =  query.getConMin();
		double[] conMaxAngles =  query.getAngleMax();
		double[] conMinAngles =  query.getAngleMin();
		char[] conStartPep =  query.getConStartPeptBond();
		char[] conEndPep =  query.getConEndPeptBond();
		
		for(int s=0; s<validmatchSets.length; s++) {
			
			matchSites = ms.getMatchSet(validmatchSets[s]);
			
			if(matchSites == null)
				break;
			
			//System.out.print("testing set " + s );
			//printMatchSet(matchSites);
			//System.out.print(" \n");
			// Now we iterate over the all the pairwise
			// combinations in the set
			boolean[] matchConnectors = new boolean[query.getNumOfConnections()];
			
			for(int i=0; i<query.getNumOfConnections(); i++) {
			
				//System.out.print("Testing connection  " + i );
				
				// First check if this connection is a NULL connection
				if(query.isNullConnection(i)) {
					matchConnectors[i] = true;
				} else {
					matchConnectors[i] = false;
					// NOW WE CHECK EACH OF THE CONNECTION PROERTIES
					boolean matchedDistance = false;
					boolean matchedAngle = false;
					
					if(query.connnectionHasDistThreshold(i)) {
						//System.out.print(" : distance threshold  " + conMinDists[i] + " : " + conMaxDists[i] + "\n");
						int[] seqPat1Ends;
						int[] seqPat2Ends;
						// We need to check if the distance must be 
						// between specific points on the peptide chain
						// start with the first seq pattern
						if(conStartPep[i]=='N') {
							seqPat1Ends = new int[1];
							seqPat1Ends[0] = matchSites[i][0];
						} else if(conStartPep[i]=='C') {
							seqPat1Ends = new int[1];
							seqPat1Ends[0] = matchSites[i][1];
						} else {
							seqPat1Ends = new int[2];
							seqPat1Ends[0] = matchSites[i][0];
							seqPat1Ends[1] = matchSites[i][1];	
						}
						// Now the second seq pattern
						if(conEndPep[i]=='N') {
							seqPat2Ends = new int[1];
							seqPat2Ends[0] = matchSites[i+1][0];
						} else if(conEndPep[i]=='C') {
							seqPat2Ends = new int[1];
							seqPat2Ends[0] = matchSites[i+1][1];
						} else {
							seqPat2Ends = new int[2];
							seqPat2Ends[0] = matchSites[i+1][0];
							seqPat2Ends[1] = matchSites[i+1][1];	
						}
						
						boolean seqPatsWithinThreshold = false;
						// Now we iterate over all possible
						for(int x=0; x<seqPat1Ends.length; x++) {

							//System.out.print(" SEQ1 end " + x + " = " +  seqPat1Ends[x] + " \n");
							// GET ALPHA CARBON POSITION
							List<AtomSite> alphaPos1 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat1Ends[x], "CA");
							if(!alphaPos1.isEmpty() ) {
								for(int y=0; y<seqPat2Ends.length; y++) {
									//System.out.print(" SEQ2 end  " + y + " = " + seqPat2Ends[y]);
									List<AtomSite> alphaPos2 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat2Ends[y], "CA");
									if(!alphaPos2.isEmpty() ) {
										Double dist = AtomSite.calcDistance(alphaPos1.get(0), alphaPos2.get(0));
										//System.out.print(" Distance value: " + dist + " \n");
										//System.out.print(" Threshold  " + conMinDists[i] + " : " + conMaxDists[i] + "\n");
										if(dist >= conMinDists[i] && dist <= conMaxDists[i]) {
											//System.out.print(" Match Found \n");
											seqPatsWithinThreshold = true;
											matchedDistance = true;
											break;
										}
									}
								}
								if(seqPatsWithinThreshold)
									break;
							}
						}
						if(!seqPatsWithinThreshold) // THEN WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
							break;
					} else {
						matchedDistance = true;
					}
					
					if(query.connnectionHasAngleThreshold(i)) {
						// We extract the positions of the end residues and use that 
						// to define vectors and calculate angles. 
						int[] seqPat1Ends;
						int[] seqPat2Ends;
						seqPat1Ends = new int[2];
						seqPat1Ends[0] = matchSites[i][0];
						seqPat1Ends[1] = matchSites[i][1];	
						seqPat2Ends = new int[2];
						seqPat2Ends[0] = matchSites[i+1][0];
						seqPat2Ends[1] = matchSites[i+1][1];
						// Calculate the first vector
						List<AtomSite> start1 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat1Ends[0], "N");
						List<AtomSite> end1 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat1Ends[1], "C");
						List<AtomSite> start2 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat2Ends[0], "N");
						List<AtomSite> end2 = dao.getAtomSite(e.getPdbId(), e.getEntityId(), seqPat2Ends[1], "C");
						if(!start1.isEmpty() && !end1.isEmpty() && !start2.isEmpty() && !end2.isEmpty() ) {
							Double angle = AtomSite.calcAngle(start1.get(0), end1.get(0), start2.get(0), end2.get(0));
							//System.out.println("The Angle is " + angle);
							if( angle >= conMinAngles[i] && angle <= conMaxAngles[i] ) {
								matchedAngle = true;
							} else //WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
								break;
						} else {
							// THEN WE DIDN'T SATISFY THE THRESHOLD SO TRY NEXT COMBINATION
							break;
						}
					} else {
						matchedAngle = true;
					}
					
					if(query.connnectionHasCavityThreshold(i)) {
						
					}
					
					//System.out.println("Connection " + i + " distance " + matchedDistance + " angle " + matchedAngle );

				}
				// IF WE ARE HERE AND THE VALUE OF i IS FOR THE LAST CONNECTION
				// IT IS BECAUSE WE HAVE SATISFIED ALL THE CONDITIONS
				if (i == query.getNumOfConnections() - 1) {	
					return matchSites;
				}

			}
			
		}
		return null;
	}


	
}
