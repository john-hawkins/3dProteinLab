package structools.structure;

import java.util.List;

import db.search.ComplexPatternMatchSet;


public class ProteinComplex {
	
	private String pdb_id;
	
	//private Protein[] components;
	private List<Protein> proteins;
	private String keywords;
	
	private String pdbFileName;
	
	private ComplexPatternMatchSet patternMatches;
	
	/*
	 * MAIN CONSTRUCTOR

	public ProteinComplex(String pdbId, Protein[] comp) {
		super();
		pdb_id = pdbId;
		components = comp;
	}
	 */
	
	/*
	 * GETTERS AND SETTERS 
	 */
	
	public ProteinComplex(String pdbid, String keys, List<Protein> prots) {
		pdb_id = pdbid;
		keywords = keys;
		proteins = prots;
	}

	public String getPdbId() {
		return pdb_id;
	}
	public void setPdbId(String pdbId) {
		this.pdb_id = pdbId;
	}

/*
	public Protein[] getComponents() {
		return components;
	}

	public void setComponents(Protein[] components) {
		this.components = components;
	}
*/
	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getPdbFileName() {
		return pdbFileName;
	}

	public void setPdbFileName(String pdbFileName) {
		this.pdbFileName = pdbFileName;
	}

	public void setPatternMatches(ComplexPatternMatchSet patternMatches) {
		this.patternMatches = patternMatches;
	}

	public ComplexPatternMatchSet getPatternMatches() {
		return patternMatches;
	}

	public Protein getProteinByChainId(String chainid) {
		for (Protein e : proteins) {
			if(e.getChainId().equals(chainid))
				return e;
		}
		return null;
	}
	
	
}
