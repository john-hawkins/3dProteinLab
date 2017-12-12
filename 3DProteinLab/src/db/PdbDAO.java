package db;

import java.sql.SQLException;
import java.util.List;

import structools.structure.AtomSite;
import structools.structure.Entity;
import structools.structure.PDBFile;
import structools.structure.Protein;
import structools.structure.ProteinComplex;
import structools.structure.SSE;

import db.search.DBQuery;
import db.search.MatchingComplex;
import db.search.MatchingStructure;
import db.search.QueryResult;
import db.search.StructExpQuery;

import sim.ProgressMeter;

public interface PdbDAO {

	public abstract int getEntitiesCount() throws SQLException;

	public abstract List<Entity> getEntities(String pdbId) throws SQLException;

	/*
	 * Get all polymer entities in the given PDB Entry
	 */
	public abstract List<Protein> getProteins(String pdbId)throws SQLException;

	/* getPolyEntitiesLike
	 * 
	 * This query will return a set of PolyEntities for which the
	 * sequence matches a given pattern.
	REMOVED
	
	public abstract List<Protein> getProteinsLike(String query, String props) throws SQLException;
		 */
	
	public abstract List<Protein> getProteins(DBQuery query) throws SQLException;

	public abstract List<AtomSite> getAtomSite(String pdbID, int entityID, int residueNum, String atomID) throws SQLException;

	public abstract List<AtomSite> getAtomSite(String pdbId, String chainId, int residueNum, String atomID) throws SQLException;
	
	/* getPseudoAtom 
	 * 
	 * This query will return a set of psuedo atom sites for a particular residue.
	 */
	public abstract List<AtomSite> getPseudoAtom(String pdbId, String chainId, int residueNum) throws SQLException;
		
	/* getPolyEntsStructPatMatch 
	 * 
	 * This query will return a set of Polymer Entities that match the basic
	 * criteria of the structural query
	 * That is they contain each of the sequence components of the expression.
	 * 	 
	 */
	public abstract List<Protein> getProteinStructPatMatch(StructExpQuery q ) throws SQLException;

	/* setPolyEntitySSE 
	 * 
	 * Retrieve the SSE string and set it for the specified Entity
	 * 	 
	 */
	public abstract void setProteinSSE(Protein e) throws SQLException;

	/* getPolyEntitySSE
	 * 
	 * Retrieve the SSE data for a specific chain within a PDB file
	 * Then convert it into a string with an entry for each residue 
	 * in the target polymer
	 */
	public abstract String getSSEString(String pdbid, int entid, int len)
			throws SQLException;

	/* getPolyEntitiesRegExpMatch
	 * 
	 * Search the PDB using a regular expression across the sequences 
	 */
	public abstract List<Protein> getProteinRegExpMatch(String regExpQuery, String props) throws SQLException;

	public abstract List<MatchingStructure> getPolyEntitiesRegExpMatch(String regExpQuery, String props, ProgressMeter pm) throws SQLException;
	/*
	 * Get a list of all PDBfiles in the database
	 */
	public abstract List<PDBFile> getPDBFiles() throws SQLException;
	
	/*
	 * Get a specific PDB file
	 */
	public abstract List<PDBFile> getPDBFiles(String pdbID) throws SQLException;

	/*
	 * Get a list of proteins in a specific PDB file
	 */
	public abstract List<Protein> getPDBFileProteins(String pdbID)
			throws SQLException;



	/* getPDBFileProteinSSEs(String pdbID, int entID)
	 * Method to extract objects for each secondary structure element
	 * for a specific protein within a given PDB file
	 */
	public List<SSE> getPDBFileProteinSSEs(String pdbID, Integer  entID) throws SQLException;

	public List<String> getPdbID4UniprotID(String in) throws SQLException;
	
	
	/*
	 * METHODS FOR RETRIEVING SCOP NAD PFAM INFORMATION ABOUT A MATCH WITHIN A STRUCTURE
	 */
	public abstract void fillSCOP4Match(MatchingStructure ma) throws SQLException ;
	public abstract void fillSCOP4Match(MatchingComplex maco)throws SQLException ;
	
	public abstract void fillPfam4Match(MatchingStructure ma) throws SQLException;
	public abstract void fillPfam4Match(MatchingComplex maco)throws SQLException ;
	
	/*
	 * Calculate the Auth defined positions for a matching structure
	 */
	public abstract void calcAuthPositions(MatchingStructure ma) throws SQLException;
	public abstract void calcAuthPositions(MatchingComplex maco) throws SQLException;
	
	/*
	 * Save the query and results into the database for future use
	 */
	public abstract int saveQuery(DBQuery query, List<MatchingComplex> finalSet) throws SQLException;

	public abstract List<QueryResult> getQueryList() throws SQLException;

	public abstract List<MatchingComplex> retrieveSavedSearch(DBQuery query) throws SQLException;
	public abstract List<MatchingComplex> retrieveSavedSearch(int queryId) throws SQLException;

	public abstract void fillCath4Match(MatchingStructure ma) throws SQLException;
	public abstract void fillCath4Match(MatchingComplex maco) throws SQLException;
	
	/*
	 * NEW METHODS RETURN THE WHOLE COMPLEX
	 */
	public abstract List<ProteinComplex> getProteinComplex(String pdbID) throws SQLException;

	public abstract List<ProteinComplex> getProteinComplexes(DBQuery query) throws SQLException;

	public abstract List<ProteinComplex> getProteinComplexStructPatMatch(StructExpQuery query) throws SQLException;



}