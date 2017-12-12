package db.search;

import structools.structure.*;

public class MatchingComplex {

	protected Protein[] structures;

	protected String pdb_id;
	protected String keywords;

	protected String[] matchChainIds; // Each seq component can be in a different chain

	protected int[][] matchPositions;
	protected char[][] matchResidues;
	protected char[][] matchSSEs;

	protected double[] matchAngles;
	protected double[] matchDistances;
	
	// NOT USED YET, BUT MAYBE INTRODUCED
	protected double score;

	/*
	 * Variables needed to map things to original Author positions
	 */
	protected int[][] auth_matchPositions;
	protected String[][] auth_matchPositionsInsCodes;
	protected String[] auth_matchPositionsChainIds;

	/*
	 * These variables will hold information
	 * about which known functional domains
	 * the match is overlaping with
	 */
	protected String scop_id;
	protected String scop_sccs;
	protected double scop_percent;
	protected String scop_descript;


	protected String Pfam_id;
	protected String Pfam_acc;
	protected double Pfam_percent;
	protected String Pfam_descript;


	protected String cathDomainID;
	protected String cathHierID;
	protected double cathDomainPercent;
	protected String cathDomainDescription;

	/*
	 * Massive constructor for reinitialising a match from a database record
	 */
	public MatchingComplex(
			double score,
			String pdbId, 
			String keywds,
			String match_Chains,
			String match_Possies,
			String match_Residues,
			String match_SSEs,
			String match_Angles,
			String match_Distances,
			String auth_match_Possies,
			String auth_match_insCodes,
			String auth_match_chains,
			String scopId, 
			String scopSccs, 
			double scopPercent,
			String cath_dom, 
			String cath_hier,
			double cath_per,
			String pfamId,
			String pfamAcc, 
			double pfamPercent) {
		super();
		this.score = score;
		this.pdb_id = pdbId;
		this.keywords = keywds;
		this.scop_id = scopId;
		this.scop_sccs = scopSccs;
		this.scop_percent = scopPercent;
		this.cathDomainID = cath_dom;
		this.cathHierID= cath_hier;
		this.cathDomainPercent = cath_per;
		this.Pfam_id = pfamId;
		this.Pfam_acc = pfamAcc;
		this.Pfam_percent = pfamPercent;
		this.matchChainIds = convertStringRepToSingleStringArray(match_Chains);
		this.matchPositions = convertStringRepToIntArray(match_Possies);
		this.matchResidues = convertStringRepToCharArray(match_Residues);
		this.matchSSEs = convertStringRepToCharArray(match_SSEs);
		//importMatchAngles(match_Angles);
		//importMatchDistances(match_Distances);
		this.auth_matchPositions = convertStringRepToIntArray(auth_match_Possies);
		this.auth_matchPositionsInsCodes = convertStringRepToStringArray(auth_match_insCodes);
		this.auth_matchPositionsChainIds = convertStringRepToSingleStringArray(auth_match_chains);
	}

	public MatchingComplex(ProteinComplex pc, int[][] matchPositions, String[] chainIds) {
		super();
		this.pdb_id = pc.getPdbId();
		this.keywords = pc.getKeywords();
		this.matchPositions = matchPositions;
		this.matchChainIds = chainIds;
		this.fillMatchResidues( pc,  matchPositions, matchChainIds);
	}


	/*
	 * An alternative contructor that assumes that all match postion information
	 * is stroed in a single string array of dimension 3
	 * 0 = Chain
	 * 1 = start pos
	 * 2 = end pos
	 */
	public MatchingComplex(ProteinComplex pc, String[][] matchPosArray) {
		super();
		this.pdb_id = pc.getPdbId();
		this.keywords = pc.getKeywords();
		this.matchPositions = new int[matchPosArray.length][2];
		this.matchChainIds = new String[matchPosArray.length];
		for(int m=0; m<matchPosArray.length; m++) {
			this.matchChainIds[m] = matchPosArray[m][0];
			this.matchPositions[m][0] = Integer.parseInt(matchPosArray[m][1]);
			this.matchPositions[m][1] = Integer.parseInt(matchPosArray[m][2]);
		}
		this.fillMatchResidues( pc,  matchPositions, matchChainIds);
	}

	public String getPdb_id() {
		return pdb_id;
	}
	public void setPdb_id(String pdb_id) {
		this.pdb_id = pdb_id;
	}

	public int[][] getMatchPositions() {
		return matchPositions;
	}
	public void setMatchPositions(int[][] matchPositions) {
		this.matchPositions = matchPositions;
	}
	
	public String[] getMatchChainIds() {
		return matchChainIds;
	}

	public String[] getAuth_matchPositionsChainIds() {
		return auth_matchPositionsChainIds;
	}
	
	public void setAuth_matchPositionsChainIds(String[] authMatchPositionsChainIds) {
		auth_matchPositionsChainIds = authMatchPositionsChainIds;
	}

	
	public String getKeywords() {
		return keywords;
	}
	
	public String getScop_id() {
		return scop_id;
	}
	
	public String getScop_sccs() {
		return scop_sccs;
	}

	public double getScop_percent() {
		return scop_percent;
	}
	
	public String getScop_descript() {
		return scop_descript;
	}

	public void setScop_descript(String scopDescript) {
		scop_descript = scopDescript;
	}

	public void setSCOP_ID(String s) {
		scop_id = s;
	}
	
	public void setSCOP_SCCS(String s) {
		scop_sccs = s;
	}
	
	public void setSCOPPercent(double s) {
		scop_percent = s;
	}

	public String getPfam_id() {
		return Pfam_id;
	}

	public void setPfam_id(String pfamId) {
		Pfam_id = pfamId;
	}
	public String getPfam_acc() {
		return Pfam_acc;
	}

	public void setPfam_acc(String pfamAcc) {
		Pfam_acc = pfamAcc;
	}

	public double getPfam_percent() {
		return Pfam_percent;
	}

	public void setPfam_percent(double pfamPercent) {
		Pfam_percent = pfamPercent;
	}

	public String getPfam_descript() {
		return Pfam_descript;
	}

	public void setPfam_descript(String pfamDescript) {
		Pfam_descript = pfamDescript;
	}
	
	
	public String getCathDomainID() {
		return cathDomainID;
	}

	public void setCathDomainID(String cathDomainID) {
		this.cathDomainID = cathDomainID;
	}

	public String getCathHierID() {
		return cathHierID;
	}

	public void setCathHierID(String cathHierID) {
		this.cathHierID = cathHierID;
	}

	public double getCathDomainPercent() {
		return cathDomainPercent;
	}

	public void setCathDomainPercent(double cathDomainPercent) {
		this.cathDomainPercent = cathDomainPercent;
	}

	public String getCathDomainDescription() {
		return cathDomainDescription;
	}

	public void setCathDomainDescription(String cathDomainDescription) {
		this.cathDomainDescription = cathDomainDescription;
	}

	public int[][] getAuth_matchPositions() {
		return auth_matchPositions;
	}

	public void setAuth_matchPositions(int[][] authMatchPositions) {
		auth_matchPositions = authMatchPositions;
	}

	public String[][] getAuth_matchPositionsInsCodes() {
		return auth_matchPositionsInsCodes;
	}

	public void setAuth_matchPositionsInsCodes(String[][] authMatchPositionsInsCodes) {
		auth_matchPositionsInsCodes = authMatchPositionsInsCodes;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double[] getMatchAngles() {
		return matchAngles;
	}

	public void setMatchAngles(double[] matchAngles) {
		this.matchAngles = matchAngles;
	}

	public double[] getMatchDistances() {
		return matchDistances;
	}

	public void setMatchDistances(double[] matchDistances) {
		this.matchDistances = matchDistances;
	}

	public String getMatchingResidues() {
		String results = "";
		if(matchResidues!=null) {
		for(int i=0; i<matchResidues.length; i++) {
			if(i>0)
				results = results +" <=> ";
			for(int j=0; j<matchResidues[i].length; j++) {
				if(j>0)
					results = results +":";
				results = results + matchResidues[i][j];
			}
		}
		}
		return results;
	}
	
	public String getMatchingSSEs() {
		String results = "";
		if(matchSSEs!=null) {
		for(int i=0; i<matchSSEs.length; i++) {
			if(i>0)
				results = results +" <=> ";
			for(int j=0; j<matchSSEs[i].length; j++) {
				if(j>0)
					results = results +":";
				results = results + matchSSEs[i][j];
			}
		}
		}
		return results;
	}


	private void fillMatchResidues(ProteinComplex pc, int[][] matchPosies,	String[] chains) {
		if(matchPosies != null) {
			matchResidues = new char[matchPosies.length][];
			matchSSEs = new char[matchPosies.length][];
			for(int i=0; i<matchResidues.length; i++) {
				int length = 1 +  matchPosies[i][1] - matchPosies[i][0];
				matchResidues[i] = new char[length];
				matchSSEs[i] = new char[length];
				for(int j=0; j<length; j++) {
					Residue it  = pc.getProteinByChainId(chains[i]).getResidueAt(matchPositions[i][0] + j);
					matchResidues[i][j] = it.getResidueType();
					matchSSEs[i][j] = it.getSse().toLowerCase().charAt(0);
				}
			}
		}
		
	}

	/*
	 * Methods for returning String representations of the data
	 * for storing in the database
	 */
	
	public String getMatchPositionsAsString() {
		String results = "";
		if(matchPositions!=null) {
			for(int i=0; i<matchPositions.length; i++) {
				if(i>0)
					results = results +" <=> ";
				for(int j=0; j<matchPositions[i].length; j++) {
					if(j>0)
						results = results +":";
					results = results + matchPositions[i][j];
				}
			}
		}
		return results;
	}


	public String getMatchResiduesAsString() {
		return getMatchingResidues();
	}


	public String getMatchSSEsAsString() {
		return getMatchingSSEs();
	}


	public String getMatchAnglesAsString() {
		String results = "";
		if(matchAngles!=null) {
			for(int i=0; i<matchAngles.length; i++) {
				if(i>0)
					results = results +" <=> ";
				results = results + matchAngles[i];
			}
		}
		return results;
	}


	public String getMatchDistancesAsString() {
		String results = "";
		if(matchDistances!=null) {
			for(int i=0; i<matchDistances.length; i++) {
				if(i>0)
					results = results +" <=> ";
				results = results + matchDistances[i];
			}
		}
		return results;
	}


	public String getAuthMatchPositionsAsString() {
		String results = "";
		if(auth_matchPositions!=null) {
			for(int i=0; i<auth_matchPositions.length; i++) {
				if(i>0)
					results = results +" <=> ";
				for(int j=0; j<auth_matchPositions[i].length; j++) {
					if(j>0)
						results = results +":";
					results = results + auth_matchPositions[i][j];
				}
			}
		}
		//System.err.println("RETURNING AUTH MATCHES: [" + results + "]");
		return results;
	}


	public String getAuthMatchPInsCodesAsString() {
		String results = "";
		if(auth_matchPositionsInsCodes!=null) {
			for(int i=0; i<auth_matchPositionsInsCodes.length; i++) {
				if(i>0)
					results = results +" <=> ";
				for(int j=0; j<auth_matchPositionsInsCodes[i].length; j++) {
					if(j>0)
						results = results +":";
					results = results + auth_matchPositionsInsCodes[i][j];
				}
			}
		}
		//System.err.println("RETURNING AUTH INSCODES: [" + results + "]");
		return results;
	}
	
	
	/*
	 * Methods to parse string representation of the match positions
	 */
	
	private int[][] convertStringRepToIntArray(String stringRep) {
		if(stringRep==null || stringRep.equals(""))
			return null;
		String [] temp = stringRep.split("<=>");
		int[][] results = new int[temp.length][];
		for(int i=0; i<temp.length; i++) {
			String [] temp2 = temp[i].split(":");
			results[i] = new int[temp2.length];
			for(int j=0; j<temp2.length; j++) {
				results[i][j] = Integer.parseInt( temp2[j].trim() );
			}
		}
		return results;
	}
	
	private char[][] convertStringRepToCharArray(String stringRep) {
		if(stringRep==null || stringRep.equals(""))
			return null;
		String [] temp = stringRep.split("<=>");
		char[][] results = new char[temp.length][];
		for(int i=0; i<temp.length; i++) {
			String [] temp2 = temp[i].trim().split(":");
			results[i] = new char[ temp2.length ];
			for(int j=0; j<temp2.length; j++) {
				results[i][j] =  temp2[j].charAt(0);
			}
		}
		return results;
	}

	private String[][] convertStringRepToStringArray(String stringRep) {
		if(stringRep==null || stringRep.equals(""))
			return null;
		String [] temp = stringRep.split("<=>");
		String[][] results = new String[temp.length][];
		for(int i=0; i<temp.length; i++) {
			String [] temp2 = (" " + temp[i] + " ").split(":");
			results[i] = new String[ temp2.length ];
			for(int j=0; j<temp2.length; j++) {
				results[i][j] =  temp2[j].trim();
			}
		}
		
		return results;
	}
	
	private String[] convertStringRepToSingleStringArray(String stringRep) {
		if(stringRep==null || stringRep.equals(""))
			return null;
		String [] temp = stringRep.split("<=>");

		return temp;
	}

	public String getMatchChainsAsString() {
		String results = "";
		if(matchChainIds!=null) {
			for(int i=0; i<matchChainIds.length; i++) {
				if(i>0)
					results = results +"<=>";
					results = results + matchChainIds[i];
			}
		}
		return results;
	}

	public String getAuthMatchChainsAsString() {
		String results = "";
		if(auth_matchPositionsChainIds!=null) {
			for(int i=0; i<auth_matchPositionsChainIds.length; i++) {
				if(i>0)
					results = results +"<=>";
					results = results + auth_matchPositionsChainIds[i];
			}
		}
		return results;
	}

	public boolean coversChain(String chain) {
		if(matchChainIds!=null) {
			for(int i=0; i<matchChainIds.length; i++) {
				if(matchChainIds[i].charAt(0)== chain.charAt(0))
					return true;
			}
		}
		return false;
	}

	public int[][] getMatchesOnChain(String chain) {
		int numMatches=0;
		int[][] temp = new int[matchPositions.length][2];
		for(int i=0; i<matchChainIds.length; i++) {
			if(matchChainIds[i].charAt(0)== chain.charAt(0)) {
				temp[numMatches][0] = matchPositions[i][0];
				temp[numMatches][1] = matchPositions[i][1];
				numMatches++;
			}
		}
		int[][] results = new int[numMatches][2];
		for(int i=0; i<numMatches; i++) {
			results[i][0] = temp[i][0];
			results[i][1] = temp[i][1];
		}
		return results;
	}

	public int getNumPosMatchesNotOnChain(String chain) {
		int result = 0;
		for(int i=0; i<matchChainIds.length; i++) {
			if(matchChainIds[i].charAt(0) != chain.charAt(0)) {
				int len = matchPositions[i][1] - matchPositions[i][0] + 1;
				result = result + len;
			}
		}
		return result;
	}

	public String[] getUniqueChains() {
		String temp = ""; 
		for(int i=0; i<matchChainIds.length; i++) {
			if( temp.indexOf(matchChainIds[i].charAt(0))==-1 ) {
				if(temp.length()>0)
					temp = temp + "-";
				temp = temp + matchChainIds[i].charAt(0);
			}
		}
		return temp.split("-");
	}
	
	public String getUniqueChainList() {
		String temp = ""; 
		for(int i=0; i<matchChainIds.length; i++) {
			if( temp.indexOf(matchChainIds[i].charAt(0))==-1 ) {
				temp = temp + matchChainIds[i].charAt(0);
			}
		}
		return temp;
	}
	
}
