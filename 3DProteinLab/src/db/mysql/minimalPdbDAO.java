package db.mysql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structools.structure.*;

import sim.ProgressMeter;

import db.DatabaseConnectionProperties;
import db.DatabaseConnectionProvider;
import db.PdbDAO;
import db.SimpleDatabaseConnectionProviderImpl;
import db.StatementExecutor;
import db.search.*;
import descriptor.CriticalResidue;


public class minimalPdbDAO  implements PdbDAO {
	

	// BASIC SQL STATEMENTS FOR SEARCHING PROTEINS
	private String SQL_All = "SELECT * FROM pdb_protein";
	private String SQL_AllKnown = "SELECT * FROM pdb_protein WHERE pdb_id NOT IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_AllUnknown = "SELECT * FROM pdb_protein WHERE pdb_id IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	private String SQL_SeqMatchAll = "SELECT * FROM pdb_protein WHERE seq LIKE ?";
	private String SQL_SeqMatchAllKnown = "SELECT * FROM pdb_protein WHERE seq LIKE ? AND pdb_id NOT IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_SeqMatchAllUnknown = "SELECT * FROM pdb_protein WHERE seq LIKE ? AND pdb_id IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	private String SQL_SeqMatchMiddle = " pdb_protein.seq LIKE ?";
	private String SQL_SeqMatchMiddleWithCysBonds  = " pdb_protein.seq_plus_dis_bonds LIKE ?";
	
	private String SQL_RestrictByKnown = " pdb_protein.pdb_id NOT IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_RestrictByUnknown = " pdb_protein.pdb_id IN (SELECT pdb_id FROM pdb_file WHERE upper(keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	// SQL STATEMENTS FOR INSERTING DATA

	private static final String PDB_FILE_INSERT = "INSERT INTO pdb_file VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String PDB_FILE_PROTEIN_INSERT = "INSERT INTO pdb_protein VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String PDB_FILE_ATOM_SITE_INSERT = "INSERT INTO pdb_atom_site VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String PDB_FILE_SSE_INSERT = "INSERT INTO pdb_sse_dssp VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String PDB_FILE_PSEUDO_POINT_INSERT = "INSERT INTO pdb_pseudo_atoms VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/*
	 * TO ERASE A PDB ENTRY
	 * 
DELETE FROM `cath_fold_positions` WHERE `pdb_id`='3H2L';
DELETE FROM `scop_fold_positions` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_file` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_protein` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_dis_bonds` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_atom_site` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_pseudo_atoms` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_sse_authors` WHERE `pdb_id`='3H2L';
DELETE FROM `pdb_sse_dssp` WHERE `pdb_id`='3H2L';
	 */
	
	private Connection con;
	
	public minimalPdbDAO(Connection con) {
		this.con = con;
	}
	
	/*
	 * GET BASIC QUERY STRING
	 * 
	 * Build up the basic query string on the basis of what is found in the query object
	 * 
	 * The fields are then populated by the sister method : insertQueryParams
	 * 
	 * Order in which fields need to be populated
	 * 
	 * KEYWORDS
	 * RESOLUTION
	 * TECH
	 * PDBID
	 * SEQ PATTERNS (1 or more)
	 */
	public String getQueryString(DBQuery query) throws SQLException {
		System.err.println("GENERATING QUERY FOR " + query.getUser_id());
		String queryString = "SELECT * FROM pdb_protein ";
		
		
		String queryString2 = " INNER JOIN (SELECT * FROM pdb_file WHERE ";
		int params = 0;
		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			queryString2 = queryString2 + " (upper(keywords) LIKE ? OR upper(title) LIKE ?) ";
			params++;
		}
		if( query.getFunctionKnown() != "" ) {
			if(params>0)
				queryString2 = queryString2 + " AND ";
			if(query.getFunctionKnown().equals("KNOWN")) {
				queryString2 = queryString2 + " (upper(keywords) NOT LIKE '%UNKNOWN FUNCTION%') ";
			} else if(query.getFunctionKnown().equals("UNKNOWN")) {
				queryString2 = queryString2 + " (upper(keywords) LIKE '%UNKNOWN FUNCTION%') ";
			} 
			params++;
		}
		
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			String oper = ">=";
			String theOp = query.getResolutionOperator();
			if(theOp!=null && theOp!="") {
				if( theOp.equals("GT") ){
					oper = ">";
				} else if( theOp.equals("GTE") ) {
					oper = ">=";
				} else if( theOp.equals("EQ") ) {
					oper = "=";
				} else if( theOp.equals("LTE") ) {
					oper = "<=";
				} else if( theOp.equals("LT") ) {
					oper = "<";
				}
			}
			if(params>0)
				queryString2 = queryString2 + " AND ";
			queryString2 = queryString2 + " resolution " + oper + " ? ";
			params++;
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			if(params>0)
				queryString2 = queryString2 + " AND ";
			queryString2 = queryString2 + " upper(tech) = ? ";
			params++;
		}
		if(query.getPdbID()!=null && !query.getPdbID().equals("")) {
			if(params>0)
				queryString2 = queryString2 + " AND ";
			queryString2 = queryString2 + " pdb_id = ? ";
			params++;
		}
		queryString2 = queryString2 + ") AS s2 ON pdb_protein.pdb_id = s2.pdb_id";
		
		if(params > 0)
			queryString = queryString + queryString2;
		else
			queryString = queryString +  " INNER JOIN (SELECT * FROM pdb_file) AS s2 ON pdb_protein.pdb_id = s2.pdb_id";
		
		// Check for a specificed redundancy reduction
		if(query.getRedundancy()!=null && !query.getRedundancy().equals("")  && !query.getRedundancy().equals("0")) {
			String redundancy = query.getRedundancy();
			queryString = queryString + ", pdb_clusters " +
			"WHERE pdb_clusters.threshold="+redundancy+" AND " +
			"pdb_protein.pdb_id=pdb_clusters.pdb_id AND " +
			"pdb_protein.auth_asym_id=pdb_clusters.auth_asym_id AND ";
			// TODO: ABOVE NEEDS TO CHANGE ONCE I GET THE 'auth_asym_id' INTO PROTEIN TABLE
		} else {
			queryString = queryString + " WHERE ";
		}
		
		int pats = 0;
		if(query instanceof StructExpQuery) {
			String[] queryParts = query.getQueryStringsForSQL();
			if(queryParts.length > 0) {
				queryString = queryString + " ( ";
				for(int i=0; i<queryParts.length; i++) {
					if(i>0) queryString = queryString + " AND ";
					if( query.usesCysteineBondCodes() ) {
						queryString = queryString +  this.SQL_SeqMatchMiddleWithCysBonds;
					} else {
						queryString = queryString +  this.SQL_SeqMatchMiddle;
					}
					pats++;
				}
				queryString = queryString + " ) ";
			}
		}
	
		String finalQueryString ="";
		
		if(params==0 && pats==0) { // Change the query
			finalQueryString = "SELECT * FROM pdb_protein";
		} else if(params>0 && pats>0) {
			finalQueryString = queryString;
		} else if(params>0 ) { // I need to work these last two option out, IF THEY OCCUR AT ALL
			finalQueryString = queryString;
		} else {
			finalQueryString = queryString;
		}
		
		System.err.println("BASIC QUERY STRUCTURE: " + finalQueryString);
		return finalQueryString;
	}
	
	/*
	 * InsertQueryParams: 
	 * 
	 * This will insert the parameters into a query that was generated using the method getQueryString(DBQuery query)
	 * 
	 * Order in which fields need to be populated
	 * 
	 * KEYWORDS
	 * RESOLUTION
	 * TECH
	 * PDBID
	 * SEQ PATTERNS (1 or more)
	 */
	private void insertQueryParams(DBQuery query, PreparedStatement st, int startIndex) throws SQLException {
		// TODO Auto-generated method stub
		
		int paramIndex = startIndex;

		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			// We do it twice because it checks the title of the PDB as well
			st.setString(paramIndex, query.getKeywordsForSQLLIKE());
			paramIndex++;
			st.setString(paramIndex, query.getKeywordsForSQLLIKE());
			paramIndex++;
		}
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			st.setDouble(paramIndex, query.getResolutionAsDouble() );
			paramIndex++;
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			st.setString(paramIndex, query.getTech());
			paramIndex++;
		}
		if(query.getPdbID()!=null && !query.getPdbID().equals("")) {
			st.setString(paramIndex, query.getPdbID());
			paramIndex++;
		}
		
		if(query instanceof StructExpQuery) {
			String[] queryParts = query.getQueryStringsForSQL();
			if(queryParts.length > 0) {
				for(int i=0; i<queryParts.length; i++) {
					st.setString(paramIndex,  queryParts[i] );
					System.err.println(" Filled " + queryParts[i] );
					paramIndex++;
				}
			}	
		}
	}
	
	/* getEntitiesCount()
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getEntitiesCount()
	 */
	public int getEntitiesCount() throws SQLException {
		PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM pdb_protein");
		try {
			return StatementExecutor.executeSingletonQuery(st, StatementExecutor.INTEGER_CONVERTER);
		}
		finally {
			st.close();
		}		
	}
	
	/* getEntities(String pdbId)
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getEntities(java.lang.String)
	 */
	public List<Entity> getEntities(String pdbId) throws SQLException {
		PreparedStatement st = con.prepareStatement("select * from pdb_protein where pdb_id = ?");
		try {
			st.setString(1, pdbId);
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<Entity>() {
				public Entity createObject(ResultSet rs) throws SQLException {
					return new Entity(rs.getString("PDB_ID"), rs.getInt("ENTITY_ID"), "", "");
				}
			});
		}
		finally {
			st.close();
		}
	}
	
	/*
	 * Get all polymer entities in the given PDB Entry
	 */
	public List<Protein> getProteins(String pdbId) throws SQLException {

		PreparedStatement st = con.prepareStatement("SELECT * FROM pdb_protein,pdb_file WHERE pdb_file.pdb_id = ? AND pdb_file.pdb_id=pdb_protein.pdb_id");
		
		List<Protein> results = new LinkedList<Protein>();

		try {
			st.setString(1, pdbId);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				Protein e = new Protein(
						rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("ASYM_ID"),
						rs.getString("AUTH_ASYM_ID"),  
						rs.getString("TYPE"), 
						rs.getString("SEQ"),
						rs.getString("SSES"),
						rs.getString("SEQ_PLUS_SSE"),
						rs.getString("UNIPROT_AC"),
						rs.getString("UNIPROT_CODE"),
						rs.getString("KEYWORDS"),
						rs.getString("SEQ_PLUS_DIS_BONDS"),
						rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS") ,
						rs.getString("SEQ_RESOLVED")/* */
				);
				results.add( e );
			}
		} finally {
			st.close();
		}
		return results;	
	}
	
	
	/*
	 * Get all polymer entities in the given PDB Entry
	 */
	public List<ProteinComplex> getProteinComplex(String pdbID) throws SQLException {
		List<Protein> prots = getProteins(pdbID);
		List<ProteinComplex> result = new  LinkedList<ProteinComplex>();
		String wds = prots.get(0).getKeywords();
		ProteinComplex e = new ProteinComplex(pdbID, wds, prots);
		result.add(e);
		return result;	
	}

	
	/*
	 * Get all Proteins with a set of general requirements
	 */
	public List<Protein> getProteins(DBQuery query) throws SQLException {

		String queryString = getQueryString( query );
		
		PreparedStatement st = con.prepareStatement(queryString);
		List<Protein> results = new LinkedList<Protein>();

		try {
			
			insertQueryParams(query, st, 1);
			
			ResultSet rs = st.executeQuery();

			while(rs.next()) {

				Protein e = new Protein(
						rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("ASYM_ID"),
						rs.getString("AUTH_ASYM_ID"),  
						rs.getString("TYPE"), 
						rs.getString("SEQ"),
						rs.getString("SSES"),
						rs.getString("SEQ_PLUS_SSE"),
						rs.getString("UNIPROT_AC"),
						rs.getString("UNIPROT_CODE"),
						rs.getString("KEYWORDS"),
						rs.getString("SEQ_PLUS_DIS_BONDS"),
						rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS") ,
						rs.getString("SEQ_RESOLVED") /**/
				);
				// TO BE REMOVED !!!
				setProteinResolvedSeq(e);
				results.add( e );
			}
		} finally {
			st.close();
		}
		
		return results;	
	}
	

	/* getAtomSite 
	 * 
	 * This query will return a set of atom sites for a particular atom 
	 * within an entity subcomponent of the specified structure. 
	 * Each of the instances of this entity in the structure will have
	 * a separate entry in the results.
	 * You can use all of them, or just the first.
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getAtomSite(java.lang.String, int, int, java.lang.String)
	 */
	public List<AtomSite> getAtomSite(String pdbID, int entityID, int residueNum, String atomID) throws SQLException {
	
		String theQuery = "SELECT * FROM pdb_atom_site WHERE " +
	    				"pdb_id = ? and entity_id= ? and " +
	    				"seq_id= ? and atom_id = ? " +
	    				"order by asym_id";

		PreparedStatement st = con.prepareStatement(theQuery);

		try {
			st.setString(1, pdbID);
			st.setInt(2,  entityID);
			st.setInt(3, residueNum);
			st.setString(4, atomID);
			
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<AtomSite>() {
				public AtomSite createObject(ResultSet rs) throws SQLException {

					return new AtomSite(rs.getString("PDB_ID"), 
										rs.getString("AA_ID"), 
										rs.getDouble("X"), 
										rs.getDouble("Y"), 
										rs.getDouble("Z"), 
										rs.getString("ASYM_ID"), 
										rs.getString("ATOM_ID"), 
										rs.getInt("ENTITY_ID"), 
										rs.getInt("SEQ_ID")
							);
				}
			});
		} finally {
			st.close();
		}
	}
	
	/* getAtomSite 
	 * 
	 * This query will return a set of atom sites for a particular atom 
	 * within an entity subcomponent of the specified structure. 
	 * Each of the instances of this entity in the structure will have
	 * a separate entry in the results.
	 * You can use all of them, or just the first.
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getAtomSite(java.lang.String, java.lang.String, int, java.lang.String)
	 */
	public List<AtomSite> getAtomSite(String pdbID, String chainId, int residueNum, String atomID) throws SQLException {
		
		String theQuery = "SELECT * FROM pdb_atom_site WHERE " +
	    				"pdb_id = ? and asym_id= ? and " +
	    				"seq_id= ? and atom_id = ? " +
	    				"order by asym_id";

		PreparedStatement st = con.prepareStatement(theQuery);

		try {
			st.setString(1, pdbID);
			st.setString(2,  chainId);
			st.setInt(3, residueNum);
			st.setString(4, atomID);
			
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<AtomSite>() {
				public AtomSite createObject(ResultSet rs) throws SQLException {

					return new AtomSite(rs.getString("PDB_ID"), 
										rs.getString("AA_ID"), 
										rs.getDouble("X"), 
										rs.getDouble("Y"), 
										rs.getDouble("Z"), 
										rs.getString("ASYM_ID"), 
										rs.getString("ATOM_ID"), 
										rs.getInt("ENTITY_ID"), 
										rs.getInt("SEQ_ID")
							);
				}
			});
		} finally {
			st.close();
		}
	}
	

	
	/* getPseudoAtom 
	 * 
	 * This query will return a set of psuedo atom sites for a particular residue.
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getAtomSite(java.lang.String, java.lang.String, int, java.lang.String)
	 */
	public List<AtomSite> getPseudoAtom(String pdbID, String chainId, int residueNum) throws SQLException {
		
		String theQuery = "SELECT * FROM pdb_pseudo_atoms WHERE " +
	    				"pdb_id = ? and asym_id= ? and " +
	    				"seq_id= ? " +
	    				"order by asym_id";

		PreparedStatement st = con.prepareStatement(theQuery);

		try {
			st.setString(1, pdbID);
			st.setString(2,  chainId);
			st.setInt(3, residueNum);
			
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<AtomSite>() {
				public AtomSite createObject(ResultSet rs) throws SQLException {

					return new AtomSite(rs.getString("PDB_ID"), 
										rs.getString("AA_ID"), 
										rs.getDouble("X"), 
										rs.getDouble("Y"), 
										rs.getDouble("Z"), 
										rs.getString("ASYM_ID"), 
										rs.getString("ATOM_ID"), 
										rs.getInt("ENTITY_ID"), 
										rs.getInt("SEQ_ID")
							);
				}
			});
		} finally {
			st.close();
		}
	}
	

	
	
	/* getPolyEntsStructPatMatch 
	 * 
	 * This query will return a set of Proteins that match the basic
	 * criteria of the structural query
	 * That is they contain each of the sequence components of the expression.
	 */
	public List<Protein> getProteinStructPatMatch( StructExpQuery q) throws SQLException {
				 
		String myStatement = getQueryString( q );
	
		System.out.println("THE NEW QUERY: " + myStatement );
		
		PreparedStatement st = con.prepareStatement( myStatement );
		
		List<Protein> results = new LinkedList<Protein>();
		
		int NumTested = 0;
		try {
			//for(int i=0; i<queryParts.length; i++) {
			//	st.setString(i+1, "%" + queryParts[i] + "%");
			//	System.out.println(" Filled " + "%" + queryParts[i] + "%");
			//}

			insertQueryParams(q, st, 1);
			
			ResultSet rs = st.executeQuery();
			
			System.err.println(" *** SQL Matching Done !!!! " );
			
			String seq_to_match;
			
			PatternMatchSet ms = new PatternMatchSet( q.getRegExPatternSet() );
			
			while(rs.next()) {
				
				if( q.usesCysteineBondCodes() ) {
					seq_to_match = rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS");
				} else {
					seq_to_match = rs.getString("SEQ_PLUS_SSE");
				}

				// System.err.println("Checking match : " +  seq_to_match);
				ms.matchSequence(seq_to_match);
				// System.err.println(" *** Done ***");
				
				if(ms.getNumOfValidMatchSets() > 0 ) {
					Protein e = new Protein(
							rs.getString("PDB_ID"), 
							rs.getInt("ENTITY_ID"), 
							rs.getString("ASYM_ID"), 
							rs.getString("AUTH_ASYM_ID"), 
							rs.getString("TYPE"), 
							rs.getString("SEQ"),
							rs.getString("SSES"),
							rs.getString("SEQ_PLUS_SSE"),
							rs.getString("UNIPROT_AC"),
							rs.getString("UNIPROT_CODE"),
							rs.getString("KEYWORDS"),
							rs.getString("SEQ_PLUS_DIS_BONDS"),
							rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS") , rs.getString("SEQ_RESOLVED")/* */ );
					// e.setSSE(rs.getString("SSES"));
					// this.setPolyEntitySSE(e);
					// TO BE REMOVED !!!

					// System.err.println("Checking resolved residues ");
					// setProteinResolvedSeq(e);
					// System.err.println(" *** Done ***");
					
					results.add( e );
					// System.err.println("Added new protein " + e.getPdbId() + ":" + e.getChainId());
				}
				NumTested++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st.close();
		}

		System.out.println("SQL + RegExp Matched: " + results.size() );
		
		return results;
	}
	
	
	
	/*
	 * GET QUERY STRING : getProteinComplexQueryString
	 * 
	 * Build up the query string on the basis of what is found in the query object
	 * 
	 * The fields are then populated by the sister method : insertProteinComplexQueryParams
	 * 
	 * Order in which fields need to be populated
	 * 
	 * 
	 * SEQ PATTERNS (1 or more)
	 * 
	 * KEYWORDS
	 * RESOLUTION
	 * TECH
	 * PDBID
	 */
	public String getProteinComplexQueryString(DBQuery query) throws SQLException {
		System.err.println("GENERATING QUERY FOR " + query.getUser_id());
		String queryString = "SELECT DISTINCT pdb_file.pdb_id FROM pdb_file ";
		
		int pats = 0;
		if(query instanceof StructExpQuery) {
			String[] queryParts = query.getQueryStringsForSQL();
			if(queryParts.length > 0) {

				for(int i=0; i<queryParts.length; i++) {
					// OR : Because we want any chain that matches part of the patterns
					// if(i>0) queryString = queryString + " OR "; 
					
					if( containsSpecificChars(queryParts[i]) ) {
						
						String asName = "s" + i;
	
						queryString = queryString + " INNER JOIN (SELECT * FROM pdb_protein WHERE ";
						
						if( query.usesCysteineBondCodes() ) {
							queryString = queryString +  this.SQL_SeqMatchMiddleWithCysBonds;
						} else {
							queryString = queryString +  this.SQL_SeqMatchMiddle;
						}
						pats++;
						queryString = queryString + " )  AS " + asName + " ON pdb_file.pdb_id = "+asName+".pdb_id ";
					}
				}
			}
		}

		int params = 0;
		// Check for a specificed redundancy reduction
		if(query.getRedundancy()!=null && !query.getRedundancy().equals("")  && !query.getRedundancy().equals("0")) {
			String redundancy = query.getRedundancy();
			queryString = queryString + ", pdb_clusters " +
			"WHERE pdb_clusters.threshold="+redundancy+" AND " +
			"pdb_file.pdb_id=pdb_clusters.pdb_id";
			
			// THIS IS BEING CHNAGED BECAUSE TO MATCH WHOLE COMPLEXES WE NEED TO GET JUST THE PDBIDs 
			//"pdb_file.pdb_id=pdb_clusters.pdb_id AND " +
			//"pdb_file.auth_asym_id=pdb_clusters.auth_asym_id ";

			params++;
		}
		
		
		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			if(params>0)
				queryString = queryString + " AND ";
			else
				queryString = queryString + " WHERE ";
			queryString = queryString + " (upper(keywords) LIKE ? OR upper(title) LIKE ?) ";
			params++;
		}
		if( query.getFunctionKnown() != "" ) {
			if(params>0)
				queryString = queryString + " AND ";
			else
				queryString = queryString + " WHERE ";
			if(query.getFunctionKnown().equals("KNOWN")) {
				queryString = queryString + " (upper(keywords) NOT LIKE '%UNKNOWN FUNCTION%') ";
			} else if(query.getFunctionKnown().equals("UNKNOWN")) {
				queryString = queryString + " (upper(keywords) LIKE '%UNKNOWN FUNCTION%') ";
			} 
			params++;
		}
		
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			String oper = ">=";
			String theOp = query.getResolutionOperator();
			if(theOp!=null && theOp!="") {
				if( theOp.equals("GT") ){
					oper = ">";
				} else if( theOp.equals("GTE") ) {
					oper = ">=";
				} else if( theOp.equals("EQ") ) {
					oper = "=";
				} else if( theOp.equals("LTE") ) {
					oper = "<=";
				} else if( theOp.equals("LT") ) {
					oper = "<";
				}
			}
			if(params>0)
				queryString = queryString + " AND ";
			else
				queryString = queryString + " WHERE ";

			queryString = queryString + " resolution " + oper + " ? ";
			params++;
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			if(params>0)
				queryString = queryString + " AND ";
			else
				queryString = queryString + " WHERE ";

			queryString = queryString + " upper(tech) = ? ";
			params++;
		}
		if(query.getPdbID()!=null && !query.getPdbID().equals("")) {
			if(params>0)
				queryString = queryString + " AND ";
			else
				queryString = queryString + " WHERE ";

			queryString = queryString + " pdb_id = ? ";
			params++;
		}

	
		String finalQueryString = queryString;

		System.err.println("BASIC QUERY STRUCTURE: " + finalQueryString);
		return finalQueryString;
	}
	
	
	/*
	 * HELPER FUNCTION
	 * 
	 * We only want to add seq matches to the SQL if the patterns contain searches
	 * for specific residues, otherise it is a waste of time.
	 */
	private boolean containsSpecificChars(String sqlStringMatch) {
		Pattern regExPattern = Pattern.compile("[A-Z]");	
		Matcher matcher = regExPattern.matcher(sqlStringMatch);
		if( matcher.find() )
			return true;
		else 
			return false;
	}

	/*
	 * InsertQueryParams: 
	 * 
	 * This will insert the parameters into a query that was generated using the method getQueryString(DBQuery query)
	 * 
	 * Order in which fields need to be populated
	 * 
	 * SEQ PATTERNS (1 or more)
	 * 
	 * KEYWORDS
	 * RESOLUTION
	 * TECH
	 * PDBID
	 */
	private void insertProteinComplexQueryParams(DBQuery query, PreparedStatement st, int startIndex) throws SQLException {
		// TODO Auto-generated method stub
		
		int paramIndex = startIndex;
		
		if(query instanceof StructExpQuery) {
			String[] queryParts = query.getQueryStringsForSQL();
			if(queryParts.length > 0) {
				for(int i=0; i<queryParts.length; i++) {
					if( containsSpecificChars(queryParts[i]) ) {
						st.setString(paramIndex,  queryParts[i] );
						System.err.println(" Filled " + queryParts[i] );
						paramIndex++;
					}
				}
			}	
		}
		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			// We do it twice because it checks the title of the PDB as well
			st.setString(paramIndex, query.getKeywordsForSQLLIKE());
			paramIndex++;
			st.setString(paramIndex, query.getKeywordsForSQLLIKE());
			paramIndex++;
		}
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			st.setDouble(paramIndex, query.getResolutionAsDouble() );
			paramIndex++;
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			st.setString(paramIndex, query.getTech());
			paramIndex++;
		}
		if(query.getPdbID()!=null && !query.getPdbID().equals("")) {
			st.setString(paramIndex, query.getPdbID());
			paramIndex++;
		}

	}
	
	
	
	/* getProteinComplexStructPatMatch 
	 * 
	 * This query will return a set of Proteins that match the basic
	 * criteria of the structural query
	 * That is they contain each of the sequence components of the expression.
	 */
	public List<ProteinComplex> getProteinComplexStructPatMatch( StructExpQuery q) throws SQLException {
				 
		String myStatement = getProteinComplexQueryString( q );
	
		System.out.println("THE NEW QUERY: " + myStatement );
		
		PreparedStatement st = con.prepareStatement( myStatement );
		
		List<ProteinComplex> results = new LinkedList<ProteinComplex>();
		
		int NumTested = 0;
		try {

			insertProteinComplexQueryParams(q, st, 1);
			
			ResultSet rs = st.executeQuery();
			
			System.err.println(" *** SQL Matching Done !!!! " );
			
			ComplexPatternMatchSet ms;
			
			while(rs.next()) {
				
				ms = new ComplexPatternMatchSet( q.getRegExPatternSet() );
				// NOW WE GET ALL THE PROTEIN CHAINS FOR THIS PDB
				String pdbid = rs.getString("PDB_ID");
				String keywords = "";
				
				//String[] theSeqs = getProteinSeqs(pdbid, q.usesCysteineBondCodes() );
				List<Protein> prots = getProteins( pdbid );
				String[] seqs = new String[prots.size()];
				String[] idents = new String[prots.size()];
				int index = 0;
				for (Protein e : prots) {
					idents[index] = e.getChainId();
					if(q.usesCysteineBondCodes()) {
						seqs[index++] = e.getSeq_and_sse_and_dis_bonds();
					} else {
						seqs[index++] = e.getSeq_and_sse();
					}
					keywords = e.getKeywords();
				}

				// System.err.println("Checking match : " +  seq_to_match);
				ms.matchSequences(seqs, idents);
				// System.err.println(" *** Done ***");
				
				if(ms.getNumOfValidMatchSets() > 0 ) {
					
					ProteinComplex pc = new ProteinComplex(pdbid, keywords, prots);
					pc.setPatternMatches(ms);
					results.add( pc );
				}
				NumTested++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st.close();
		}

		System.out.println("SQL + RegExp Matched: " + results.size() );
		
		return results;
	}
	
	
	/* insertPDBResolvedSeq 
	 * 
	 * Precalculate a string representation that depecits whether a sequence position is structurally resolved
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#setPolyEntitySSE(db.pdb.EntityPoly)
	 */
	public void updatePDBResolvedSeq(String pdbid) throws SQLException {
		// First we extract all of the protein entries for the given PDB
		List<Protein> prots = getProteins( pdbid );

		String update = "UPDATE pdb_protein SET seq_resolved=? WHERE pdb_id=? AND asym_id=?";
		for(Protein p : prots) {
			setProteinResolvedSeq(p);
			
			PreparedStatement st1 = con.prepareStatement(update);
			try {
				st1.setString(1, p.getResolved() );
				st1.setString(2, pdbid);
				st1.setString(3, p.getChainId() );
				st1.executeUpdate();
			} finally {
				st1.close();
			}
			
		}
	}
	
	/* setProteinResolvedSeq 
	 * 
	 * Set the resolved representation of the sequence.
	 * To be used when creating the database, or if it has not been precalculated.
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#setPolyEntitySSE(db.pdb.EntityPoly)
	 */
	private void setProteinResolvedSeq(Protein e) {
		e.setResolved( this.getResolvedSeq(e) );	
	}
	
	
	/* getResolvedSeq
	 * 
	 * Retrieve a string representation of the sequence showing only structurally resolved residues
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#setPolyEntitySSE(db.pdb.EntityPoly)
	 */
	private String getResolvedSeq(Protein e) {
		String seq = e.getSEQ();
		String result = "";
		for(int i=0; i< seq.length(); i++) {
			if( positionResolved(e.getPdbId(), e.getChainId(), (i+1) ) ) {
				result = result + seq.charAt(i);
			} else {
				result = result + "_";
			}
		}
		return result;
	}

	/* positionResolved
	 * 
	 * Determine if a certain seq position is resolved
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#setPolyEntitySSE(db.pdb.EntityPoly)
	 */
	private boolean positionResolved(String pdbID, String chainId, int pos) {

		boolean result =false;
		String q = "SELECT DISTINCT auth_seq_id from pdb_atom_site WHERE pdb_id = ? AND asym_id = ? AND seq_id = ?";
		
		PreparedStatement st;
		try {
			st = con.prepareStatement(q);

			try {
				st.setString(1, pdbID);
				st.setString(2, chainId);
				st.setInt(3, pos);
				ResultSet rs = st.executeQuery();

				while(rs.next()) {	
					result = true;
				}
			} finally {
				st.close();
			}
		} catch (SQLException e) {
			System.err.print("-- ERROR CREATING PREPARED STATEMENT --");
			e.printStackTrace();
		}
		return result;
	}
	
	
	/* setPolyEntitySSE 
	 * 
	 * Retrieve the SSE string and set it for the specified Entity
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#setPolyEntitySSE(db.pdb.EntityPoly)
	 */
	public void setProteinSSE( Protein e ) throws SQLException {
		e.setSSE(this.getSSEString(e.getPdbId(), e.getEntityId(), e.getSEQ().length()));
	}
	
	/* getPolyEntitySSE
	 * 
	 * Retrieve the SSE data for a specific chain within a PDB file
	 * Then convert it into a string with an entry for each residue 
	 * in the target polymer
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntitySSE(java.lang.String, int, int)
	 */
	public String getSSEString( String pdbid, int entid, int len) throws SQLException {
		
		String q = "SELECT * FROM pdb_sse_dssp WHERE pdb_id=? and entity_id=? ORDER BY asym_id";
		
		PreparedStatement st = con.prepareStatement( q );
		
		char[] sse = new char[len];
		for(int l=0; l<len; l++) {
			sse[l] =  'C';
		}
		
		int NumTested = 0;
		try {
			st.setString(1, pdbid);
			st.setInt(2, entid);
			
			ResultSet rs = st.executeQuery();
				
			char ref_asym_id= ' ', asym_id = ' ';
			
			while(rs.next()) {
			    
				asym_id = rs.getString("ASYM_ID").charAt(0);

				if(NumTested==0)
					ref_asym_id = asym_id;
				
				if(ref_asym_id != asym_id)
					break;
				
				int start = rs.getInt("START_POS");
				int end = rs.getInt("END_POS");
				String ident = rs.getString("SSEGROUP");
				for(int l=start; l<end; l++) {
					sse[l] = ident.charAt(0);
				}
				
				NumTested++;
			}
			
			
		} finally {
			st.close();
		}
		
		return new String(sse);
	}

	
	/* getPolyEntitiesRegExpMatch
	 * 
	 * Search the PDB using a regular expression across the sequences 
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntitiesRegExpMatch(java.lang.String, java.lang.String)
	 */
	public List<Protein> getProteinRegExpMatch(String regExpQuery, String props) throws SQLException {
			
			PreparedStatement st = con.prepareStatement(this.SQL_All);
			if(props.equals("KNOWN")) {
				st =  con.prepareStatement(this.SQL_AllKnown);
			} else if(props.equals("UNKNOWN")) {
				st =  con.prepareStatement(this.SQL_AllUnknown);
			}
		    
			Pattern regExPattern = Pattern.compile(regExpQuery);
			
			List<Protein> results = new LinkedList<Protein>();
			int NumTested = 0;
			try {
				ResultSet rs = st.executeQuery();
				String seq;
				
				while(rs.next()) {
					seq = rs.getString("SEQ");
					
					Matcher matcher = regExPattern.matcher(seq);
					
					if( matcher.find() ) {
						Protein e = new Protein(
								rs.getString("PDB_ID"), 
								rs.getInt("ENTITY_ID"), 
								rs.getString("ASYM_ID"),
								rs.getString("AUTH_ASYM_ID"), 
								rs.getString("TYPE"), 
								rs.getString("SEQ"),
								rs.getString("SSES"),
								rs.getString("SEQ_PLUS_SSE"),
								rs.getString("UNIPROT_AC"),
								rs.getString("UNIPROT_CODE"),
								rs.getString("KEYWORDS"),
								rs.getString("SEQ_PLUS_DIS_BONDS"),
								rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS") ,
								rs.getString("SEQ_RESOLVED")/* */
						);
						//e.setSSE(rs.getString("SSES"));
						//this.setPolyEntitySSE(e);
						// TO BE REMOVED !!!
						//setProteinResolvedSeq(e);
						results.add( e );
					}
					NumTested++;
				}
			} finally {
				st.close();
			}
			
			return results;
	}
	
	/* getPolyEntitiesRegExpMatch
	 * 
	 * Search the PDB using a regular expression across the sequences 
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntitiesRegExpMatch(java.lang.String, java.lang.String)
	 */
	public List<MatchingStructure> getPolyEntitiesRegExpMatch(String regExpQuery, String props, ProgressMeter pm) throws SQLException {
			
			PreparedStatement st = con.prepareStatement(this.SQL_All);
			if(props.equals("KNOWN")) {
				st =  con.prepareStatement(this.SQL_AllKnown);
			} else if(props.equals("UNKNOWN")) {
				st =  con.prepareStatement(this.SQL_AllUnknown);
			}
		    
			Pattern regExPattern = Pattern.compile(regExpQuery);
			
			List<MatchingStructure> results = new LinkedList<MatchingStructure>();
			
			int NumTested = 0;
			try {
				ResultSet rs = st.executeQuery();
				String seq;
				
				while(rs.next()) {
					seq = rs.getString("SEQ");
					
					Matcher matcher = regExPattern.matcher(seq);
					
					if( matcher.find() ) {
						/*
						EntityPoly e = new EntityPoly(rs.getString("PDB_ID"), 
							rs.getInt("ENTITY_ID"), 
							rs.getString("TYPE"), 
							rs.getString("SEQ"), 
							rs.getString("UNIPROT_AC"),
							rs.getString("UNIPROT_CODE"));
							e.setSSE(rs.getString("SSES"));
						*/
						int[][] match = new int[1][2];
						match[0][0] = matcher.start();
						match[0][1] = matcher.end();
						results.add( new MatchingStructure(rs.getString("PDB_ID"),rs.getInt("ENTITY_ID"), "", match) );
					}
					NumTested++;
				}
			} finally {
				st.close();
			}
			
			return results;
	}
	
	
	
	/* getPDBFiles
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFiles()
	 */
	public List<PDBFile> getPDBFiles() throws SQLException{
		PreparedStatement st = con.prepareStatement("SELECT * from pdb_file");
		try {
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<PDBFile>() {
				public PDBFile createObject(ResultSet rs) throws SQLException {
					return new PDBFile(rs.getString("PDB_ID"), rs.getString("TITLE"),  rs.getString("KEYWORDS"), rs.getString("DESCRIPTION"), rs.getDouble("RESOLUTION"), rs.getString("TECH"), rs.getDate("ADDED").toString(), rs.getDate("MODIFIED").toString());
				}
			});
		}
		finally {
			st.close();
		}
	}
	
	/* getPDBFiles
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFiles(java.lang.String)
	 */
	public List<PDBFile> getPDBFiles(String pdbID) throws SQLException{
		String q = "SELECT * from pdb_file WHERE pdb_id='" + pdbID + "'"; // 
		PreparedStatement st = con.prepareStatement(q);
		try {

			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<PDBFile>() {
				public PDBFile createObject(ResultSet rs) throws SQLException {
				
					return new PDBFile(rs.getString("PDB_ID"), rs.getString("TITLE"), rs.getString("KEYWORDS"), rs.getString("DESCRIPTION"), rs.getDouble("RESOLUTION"), rs.getString("TECH"), rs.getDate("ADDED").toString(), rs.getDate("MODIFIED").toString());
				}
			});
		}
		finally {
			st.close();
		}
	}
	
	/* getPDBFileProteins
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteins(java.lang.String)
	 */
	public List<Protein> getPDBFileProteins(String pdbID) throws SQLException{
	
		String q = "SELECT * FROM pdb_protein WHERE type LIKE 'polypeptide%' " +
			" AND pdb_id='" + pdbID + "'";

		PreparedStatement st = con.prepareStatement(q);
		
		List<Protein> results = new LinkedList<Protein>();
		try {
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
					
				Protein e = new Protein(
						rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("ASYM_ID"), 
						rs.getString("AUTH_ASYM_ID"), 
						rs.getString("TYPE"), 
						rs.getString("SEQ"),
						rs.getString("SSES"),
						rs.getString("SEQ_PLUS_SSE"),
						rs.getString("UNIPROT_AC"),
						rs.getString("UNIPROT_CODE"),
						rs.getString("KEYWORDS"),
						rs.getString("SEQ_PLUS_DIS_BONDS"),
						rs.getString("SEQ_PLUS_SSE_PLUS_DIS_BONDS") ,
						rs.getString("SEQ_RESOLVED" ) /* */
				);
				
				// TO BE REMOVED !!!
				//setProteinResolvedSeq(e);
				results.add( e );
			}
		} finally {
			st.close();
		}
		
		return results;
	}
	
	/* getPDBFileProteinAlphaCs
	 * 
	 * Get a list of the alpha carbons for a given PDB
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinAlphaCs(java.lang.String)
	 */
	public List<AtomSite> getPDBFileProteinAlphaCs(String pdbID) throws SQLException{
		String q = "SELECT * from pdb_atom_site WHERE pdb_id = ? " +
				"AND atom_id = 'CA' " + 
				"AND entity_id IN (SELECT entity_id FROM pdb_protein " +
				"WHERE pdb_id = ? AND type LIKE 'polypeptide%')";

		PreparedStatement st = con.prepareStatement(q);
		
		List<AtomSite> results = new LinkedList<AtomSite>();
		try {
			st.setString(1, pdbID);
			st.setString(2, pdbID);
			ResultSet rs = st.executeQuery();
			
			// NOTE: SOME OF THE OF THE ATOM SITES HAVE  NULL VALUES IN THEIR A_AA FIELD
			// EVEN THOUGH THE SEQUENCE DATA IS PRESENT IN ENTITY_POLY
			
			while(rs.next()) {	
				//System.out.print( rs.getString("PDB_ID") + ":" +rs.getInt("LABEL_ENTITY_ID") +" (" + rs.getInt("LABEL_SEQ_ID") + ") " + rs.getString("A_AA") ) ;
				AtomSite e = new AtomSite(
						rs.getString("PDB_ID"), 
						"" + rs.getInt("ID"), 
						rs.getString("AA_ID"), 
						rs.getDouble("X"), 
						rs.getDouble("Y"), 
						rs.getDouble("Z"), 
						rs.getString("ASYM_ID"), 
						rs.getString("ATOM_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getInt("SEQ_ID"), 
						0, // MODEL NUM
						"", // TYPE SYMBOL
						rs.getString("AUTH_ASYM_ID"), 
						rs.getString("AUTH_SEQ_ID"), 
						rs.getString("PDBX_PDB_INS_CODE")	
					);
				results.add( e );
				//System.out.println(" In: " + e.getPDB_ID()+ ":" + e.getENTITY_ID() + " (" + e.getLABEL_SEQ_ID() + ") " + e.getA_AA());
			}
		} finally {
			st.close();
		}
		return results;
	}

	
	/* getPDBFileProteinSSEs(String pdbID)
	 * 
	 * Method to extract objects for each secondary structure element
	 * within a given PDB file

	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinSSEs(java.lang.String)
	 */
	public List<SSE> getPDBFileProteinSSEs(String pdbID, Integer entID) throws SQLException{
		String q = "SELECT * FROM pdb_sse_dssp WHERE pdb_id=? and entity_id=? ";

		PreparedStatement st = con.prepareStatement(q);
		
		List<SSE> results = new LinkedList<SSE>();
		try {
			st.setString(1, pdbID);
			st.setInt(2, entID);
			ResultSet rs = st.executeQuery();
	
			while(rs.next()) {	
				SSE e = new SSE(rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("ASYM_ID"), 
						rs.getString("SSETYPE"),
						rs.getString("SSEGROUP"),
						rs.getInt("START_POS"), 
						rs.getInt("END_POS"));
				results.add( e );
			}
		} finally {
			st.close();
		}
		return results;
	}
	
	
	/* --------------------------------------------------------
	 * INSERT STATEMENTS - NOT PART OF THE STANDARD INTERFACE
	 * 
	 * --------------------------------------------------------
	 */
	
	/* insertPDBFiles
	 * 
	 * Insert the basic PDb file data
	 */
	public void insertPDBFiles(List<PDBFile> files, ProgressMeter pm) throws SQLException {
		PreparedStatement st = con.prepareStatement( PDB_FILE_INSERT );

		try {			
			for (structools.structure.PDBFile e : files ) {
				//System.out.println("Inserting: " + e.getPdbId() + "  " + e.getTechnology() + "  [" + e.getResolution() + "] (" + e.getModified() + ")");
				st.setInt(1, 0 );
				st.setString(2, e.getPdbId() );
				st.setString(3, e.getTitle() );
				st.setDouble(4, e.getResolution() );
				st.setString(5, e.getKeywords());
				st.setString(6, e.getTechnology());
				st.setString(7, e.getText() );
				st.setDate(8, java.sql.Date.valueOf(e.getAdded() ) );
				st.setDate(9, java.sql.Date.valueOf(e.getModified() ) );	

				st.executeUpdate();
				
				if(pm != null)
					pm.updateMeter();
			}
			
		} finally {
			st.close();
		}
	
	}

	
	/* insertPDBProteins
	 * 
	 * Insert the a set of proteins into the database
	 */
	public void insertPDBProteins(List<Protein> fileProteins,  ProgressMeter pm) throws SQLException {
		PreparedStatement st = con.prepareStatement( PDB_FILE_PROTEIN_INSERT );

		try {			
			for (structools.structure.Protein e : fileProteins ) {
				//System.err.println("Inserting: " + e.getPdbId() + "  (" + e.getUniprot() + ":"+e.getUniprotName()+")" + "  [" + e.getEntityId() + "]" + "  " + e.getType() );
				//System.err.println("             : " + e.getSEQ() );
				//System.err.println("             : " + e.getSSE() );
				//System.err.println("             : " + e.getSeq_and_dis_bonds() );
				//System.err.println("             : " + e.getSeq_and_sse() );
				//System.err.println(" Seq SSE DB  : " + e.getSeq_and_sse_and_dis_bonds() );
				//System.err.println("[" + e.getChainId() + "]" );
				
				st.setInt(1, 0 );
				st.setString(2, e.getPdbId() );
				st.setInt(3, e.getEntityId() );
				st.setString(4, e.getChainId());
				st.setString(5, e.getType());
				st.setString(6, e.getSEQ());
				st.setString(7, e.getSSE());
				st.setString(8, e.getSeq_and_sse() );
				st.setString(9, e.getUniprot() );	
				st.setString(10, e.getUniprotName() );	
				// Need Auth id - used by most other services e.g. SCOP
				st.setString(11, e.getAuthorChainId() );
				// Two new fields for string containing cysteine bonds
				st.setString(12, e.getSeq_and_dis_bonds() );	
				st.setString(13, e.getSeq_and_sse_and_dis_bonds() );	

				st.setString(14, e.getResolved() );
				
				st.executeUpdate();
				
				if(pm != null)
					pm.updateMeter();
			}
			
		} finally {
			st.close();
		}
	}

	public void insertPDBAtomSites(List<AtomSite> fileProteinAlphaCs,  ProgressMeter pm) throws SQLException {
		PreparedStatement st = con.prepareStatement( PDB_FILE_ATOM_SITE_INSERT );

		try {			
			for (structools.structure.AtomSite e : fileProteinAlphaCs ) {
				//System.out.println("Inserting: " + e.getPDB_ID() + "  (" + e.getLABEL_SEQ_ID() + ")" + "  [" + e.getA_AA() + "]" );
				st.setInt(1, 0 );
				st.setString(2, e.getPDB_ID() );
				st.setInt(3, e.getENTITY_ID() );
				st.setInt(4, e.getLABEL_SEQ_ID() );
				st.setString(5, e.getLABEL_ASYM_ID() );
				st.setString(6, e.getLABEL_ATOM_ID() );
				st.setString(7, e.getA_AA() );
				st.setDouble(8, e.getCARTN_X() );	
				st.setDouble(9, e.getCARTN_Y() );	
				st.setDouble(10, e.getCARTN_Z() );	
				st.setString(11, e.getAUTH_ASYM_ID() );
				st.setString(12, e.getAUTH_SEQ_ID() );
				st.setString(13, e.getPDBX_PDB_INS_CODE() );

				st.executeUpdate();
				
				if(pm != null)
					pm.updateMeter();
			}
			
		} finally {
			st.close();
		}
	}

	public void insertPDBSSEs(List<SSE> fileProteinSSEs,  ProgressMeter pm) throws SQLException {
		PreparedStatement st = con.prepareStatement( PDB_FILE_SSE_INSERT );

		try {			
			for (structools.structure.SSE e : fileProteinSSEs ) {
				//System.out.println("Inserting: " + e.getPDB_ID() + "  (" + e.getLABEL_SEQ_ID() + ")" + "  [" + e.getA_AA() + "]" );
				st.setInt(1, 0 );
				st.setString(2, e.getPdb_id() );
				st.setInt(3, e.getEntity_id() );
				st.setString(4, e.getAsym_id() );
				st.setString(5, e.getSse_type());
				st.setString(6, e.getSse_group() );
				st.setInt(7, e.getStart_pos() );
				st.setInt(8, e.getEnd_pos() );	
				st.executeUpdate();
				
				if(pm != null)
					pm.updateMeter();
			}
			
		} finally {
			st.close();
		}
	}

	/*
	 * getPdbID4UniprotID
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPdbID4UniprotID()
	 */
	public List<String> getPdbID4UniprotID(String in) throws SQLException {
		String q = "SELECT pdb FROM uniprot2pdb WHERE uniprot_ac LIKE '%" + in + "%'";  
		PreparedStatement st = con.prepareStatement(q);
		try {
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<String>() {
				public String createObject(ResultSet rs) throws SQLException {
					return rs.getString("PDB");
				}
			});
		}
		finally {
			st.close();
		}
	}

	
	public void calcAuthPositions(MatchingStructure ma) throws SQLException {
		int[][] matchPositions = ma.getMatchPositions();
		String chain = ma.getChain_id();
		String pdbid = ma.getPdb_id();
		
		int[][] auth_matchPositions = new int[matchPositions.length][];
		String[][] auth_matchInsCodes = new String[matchPositions.length][];
		String authChainId = "";
		
		for(int i=0; i<matchPositions.length; i++) {
			// We are looking at match i 
			// 
			int startPos = matchPositions[i][0];
			int endPos = matchPositions[i][1];
			int matchLen = endPos - startPos + 1;
			
			auth_matchPositions[i] = new int[matchLen];
			auth_matchInsCodes[i] = new String[matchLen];
			
			for(int j=0; j< matchLen; j++) {
				int thisPos = matchPositions[i][0] + j;
				//System.err.println("Finding AUTH POSITION FOR " + thisPos);
				if( positionResolved(pdbid, chain, thisPos) ) {
					String[] temp = getAuthPosChainInsertCode(pdbid, chain, thisPos ).split("\t");
					authChainId = temp[0];
					auth_matchPositions[i][j] = Integer.parseInt(temp[1]);
					auth_matchInsCodes[i][j] = temp[2].trim();
				} else {
					String[] temp = getAuthUnresolvedPosChain(pdbid, chain, thisPos ).split("\t");
					auth_matchPositions[i][j] = 0;
					auth_matchInsCodes[i][j] = "?";
				}
			}
		}
		// Now fill in that sucker
		if(authChainId != "")
			ma.setAuth_chain_id(authChainId);
		ma.setAuth_matchPositions(auth_matchPositions);
		ma.setAuth_matchPositionsInsCodes(auth_matchInsCodes);
	}
	
	/*
	 * SAME THING BUT FOR Matching Complexes
	 * (non-Javadoc)
	 * @see db.PdbDAO#calcAuthPositions(db.search.MatchingStructure)
	 */
	public void calcAuthPositions(MatchingComplex maco) throws SQLException {
		int[][] matchPositions = maco.getMatchPositions();
		String[] chains = maco.getMatchChainIds();
		String pdbid = maco.getPdb_id();
		
		int[][] auth_matchPositions = new int[matchPositions.length][];
		String[][] auth_matchInsCodes = new String[matchPositions.length][];
		String[] authChainIds = new String[chains.length];
		
		for(int i=0; i<matchPositions.length; i++) {
			// We are looking at match i 
			// 
			int startPos = matchPositions[i][0];
			int endPos = matchPositions[i][1];
			int matchLen = endPos - startPos + 1;
			
			auth_matchPositions[i] = new int[matchLen];
			auth_matchInsCodes[i] = new String[matchLen];
			
			for(int j=0; j< matchLen; j++) {
				int thisPos = matchPositions[i][0] + j;
				//System.err.println("Finding AUTH POSITION FOR " + thisPos);
				if( positionResolved(pdbid, chains[i], thisPos) ) {
					String[] temp = getAuthPosChainInsertCode(pdbid, chains[i], thisPos ).split("\t");
					authChainIds[i] = temp[0];
					auth_matchPositions[i][j] = Integer.parseInt(temp[1]);
					auth_matchInsCodes[i][j] = temp[2].trim();
				} else {
					String[] temp = getAuthUnresolvedPosChain(pdbid, chains[i], thisPos ).split("\t");
					auth_matchPositions[i][j] = 0;
					auth_matchInsCodes[i][j] = "?";
				}
			}
		}
		// Now fill in that sucker
		maco.setAuth_matchPositionsChainIds(authChainIds);
		maco.setAuth_matchPositions(auth_matchPositions);
		maco.setAuth_matchPositionsInsCodes(auth_matchInsCodes);
	}
	
	/*
	 * Method to retrive the probable SEQ ID for unresolved Positions
	 * 
	 * I.E What would the auth position number be if it was resolved.
	 */
	private String authPosBigger = "SELECT DISTINCT auth_asym_id, auth_seq_id, pdbx_pdb_ins_code, seq_id FROM pdb_atom_site  WHERE pdb_id=? AND asym_id=? AND seq_id>? ORDER BY auth_seq_id ASC";

	private String authPosSmaller = "SELECT DISTINCT auth_asym_id, auth_seq_id, pdbx_pdb_ins_code, seq_id FROM pdb_atom_site  WHERE pdb_id=? AND asym_id=? AND seq_id<? ORDER BY auth_seq_id DESC";
	
	private String getAuthUnresolvedPosChain(String pdbid, String chain, int pos) throws SQLException  {

		String results = "";
		String auth_asym_id_BIGGER = "";
		String auth_seq_id_BIGGER = "";
		String seq_id_BIGGER = "";
		String auth_asym_id_SMALLER = "";
		String auth_seq_id_SMALLER = "";
		String seq_id_SMALLER = "";
		PreparedStatement st = con.prepareStatement(authPosBigger);

		try {
			st.setString(1, pdbid );
			st.setString(2, chain );
			st.setInt(3, pos );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				auth_asym_id_BIGGER = rs.getString("auth_asym_id");
				auth_seq_id_BIGGER = rs.getString("auth_seq_id");
				seq_id_BIGGER = rs.getString("seq_id");
				break;
			}
		} finally {
			st.close();
		}
		st = con.prepareStatement(authPosSmaller);

		try {
			st.setString(1, pdbid );
			st.setString(2, chain );
			st.setInt(3, pos );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				auth_asym_id_SMALLER = rs.getString("auth_asym_id");
				auth_seq_id_SMALLER = rs.getString("auth_seq_id");
				seq_id_SMALLER = rs.getString("seq_id");
				break;
			}
		} finally {
			st.close();
		}
		
		if(seq_id_SMALLER != "" && seq_id_BIGGER != "" ) { // THEN WE CAN CALCULATE FROM AN INTERVAL
			int startPos = new Integer(seq_id_SMALLER);
			int endPos = new Integer(seq_id_BIGGER);
			int auth_startPos = new Integer(auth_seq_id_SMALLER);
			int auth_endPos = new Integer(auth_seq_id_BIGGER);
			if(startPos==auth_startPos && endPos==auth_endPos) { // IT IS THE SAME SEQUENCE ORDERING (95% of CASES)
				return auth_asym_id_BIGGER + "\t" + pos + "\t" + "?";
			} else {
				int diff = endPos - startPos;
				int auth_diff = auth_endPos - auth_startPos;
				if(diff==auth_diff || auth_diff > diff) { //THE INTERVAL IS THE SAME OF BIGGER SO WE JUST INTERPOLATE
					int index = pos - startPos;
					int newPos = auth_startPos + index;
					return auth_asym_id_BIGGER + "\t" + newPos + "\t" + "?";
				} else {// AUTH GAP SMALLER = FUCKED IF I KNOW
					return auth_asym_id_BIGGER + "\t" + 0 + "\t" + "?";
				}
			}
		} else if( seq_id_BIGGER != "" ) { // POSITION IS AT THE START - SO WIND BACK AS NEEDED
			
			if(seq_id_BIGGER==auth_seq_id_BIGGER) // THE SAME SO RETURN
				return auth_asym_id_BIGGER + "\t" + pos + "\t" + "?";
			else {
				int endPos = new Integer(seq_id_BIGGER);
				int auth_endPos = new Integer(auth_seq_id_BIGGER);
				int back = endPos - pos;
				int newPos = auth_endPos -back;
				return auth_asym_id_BIGGER + "\t" + newPos + "\t" + "?";
			}
			
		} else if(seq_id_SMALLER != "" ) { // POSITION IS AT THE END
			
			if(seq_id_SMALLER==auth_seq_id_SMALLER) // THE SAME SO RETURN
				return auth_asym_id_SMALLER + "\t" + pos + "\t" + "?";
			else {
				int startPos = new Integer(seq_id_SMALLER);
				int auth_startPos = new Integer(auth_seq_id_SMALLER);
				int forward = pos - startPos;
				int newPos = auth_startPos + forward;
				return auth_asym_id_SMALLER + "\t" + newPos + "\t" + "?";
			}
			
		} else { // Sonst nichts - THIS SHOULD NEVER HAPPEN
			return " " + "\t" + pos + "\t" + " ";
		}
		
		//if( results.equals("") )
		//	results = chain + "\t" + pos + "\t" + " ";
		//return results;
	}



	/*
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillSCOP4Match(db.search.MatchingStructure)
	 */
	private String authPos = "SELECT DISTINCT auth_asym_id, auth_seq_id, pdbx_pdb_ins_code FROM pdb_atom_site  WHERE pdb_id=? AND asym_id=? AND seq_id=?";
	
	private String getAuthPosChainInsertCode(String pdbid, String chain, int pos) throws SQLException  {
		PreparedStatement st = con.prepareStatement(authPos);

		//System.err.print("Looking For: [" + pdbid + "][" + chain + "][" + pos + "]");
		String results = "";
		try {
			st.setString(1, pdbid );
			st.setString(2, chain );
			st.setInt(3, pos );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				
				String auth_asym_id = rs.getString("auth_asym_id");
				String auth_seq_id = rs.getString("auth_seq_id");
				String pdbx_pdb_ins_code = rs.getString("pdbx_pdb_ins_code");
				results = auth_asym_id + "\t" + auth_seq_id + "\t " + pdbx_pdb_ins_code;
				//System.err.print("Returning: [" + results + "]");
			}
		} finally {
			st.close();
		}
		if( results.equals("") )
			results = chain + "\t" + pos + "\t" + " ";
		return results;
	}


	/*
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillSCOP4Match(db.search.MatchingStructure)
	 */
	private String getSCOPsql2 = "SELECT * FROM scop_cla, scop_fold_positions WHERE scop_cla.pdb_id=? AND scop_cla.sid=scop_fold_positions.sid";
	
	public void fillSCOP4Match(MatchingStructure ma) throws SQLException {

		PreparedStatement st = con.prepareStatement(getSCOPsql2);
		//System.err.println("RUNNING QUERY : " + getSCOPsql + " FOR " + ma.getPdb_id() );
		try {
			st.setString(1, ma.getPdb_id().toLowerCase() );
			ResultSet rs = st.executeQuery();
			String bestSCOP_ID = "";
			String bestSCOP_SCCS = "";
			double bestPercent = 0;
			
			while(rs.next()) {

				int startPoint = rs.getInt("SEQ_START");
				int endPoint = rs.getInt("SEQ_END");
				String chain = rs.getString("ASYM_ID");


				double thePercent = 0;
				String theSCOP_ID = "";
				String theSCOP_SCCS = "";
				if(chain.charAt(0) == ma.getChain_id().charAt(0)) {
					
						theSCOP_SCCS = rs.getString("SCCS");
						theSCOP_ID = rs.getString("SID");

						if(startPoint==0 && endPoint==0) {
							// The SCOP domain is the whole chain so percentage is 100
							thePercent = 100;
						} else {

							double in = 0;
							double out = 0;
							int[][] hits = ma.getMatchPositions();
							for(int i=0; i<hits.length; i++) {
								int hitStart =  hits[i][0];
								int hitEnd =  hits[i][1];
								int hitLen = hitEnd - hitStart + 1;
								if(hitStart >= startPoint) {
									if(endPoint==0)
										in = in + hitLen;
									else if(hitStart <= endPoint) {
										// now we work out the overlap
										int numIn = 0;
										for(int y=0; y<hitLen; y++) {
											if( (hitStart+y) <= endPoint) 
												numIn++;
										}
										in = in + numIn;
										out = out + (hitLen-numIn);
									} else
										out = out + hitLen;
								}

							}
							thePercent = (100 * (in/(in+out) ) );
						}	
					}
					if(thePercent > bestPercent) {
						bestPercent = thePercent;
						bestSCOP_ID = theSCOP_ID;
						bestSCOP_SCCS = theSCOP_SCCS;
					}

			}
			
			ma.setSCOP_ID(bestSCOP_ID);
			ma.setSCOP_SCCS(bestSCOP_SCCS);
			ma.setSCOPPercent(bestPercent);
			ma.setScop_descript(this.getScopDescription(bestSCOP_SCCS));

		} finally {
			st.close();
		}
	}
	
	public void fillSCOP4Match(MatchingComplex maco) throws SQLException {

		PreparedStatement st = con.prepareStatement(getSCOPsql2);
		//System.err.println("RUNNING QUERY : " + getSCOPsql + " FOR " + ma.getPdb_id() );
		try {
			st.setString(1, maco.getPdb_id().toLowerCase() );
			ResultSet rs = st.executeQuery();
			String bestSCOP_ID = "";
			String bestSCOP_SCCS = "";
			double bestPercent = 0;
			
			while(rs.next()) {

				int startPoint = rs.getInt("SEQ_START");
				int endPoint = rs.getInt("SEQ_END");
				String chain = rs.getString("ASYM_ID");


				double thePercent = 0;
				String theSCOP_ID = "";
				String theSCOP_SCCS = "";
				if( maco.coversChain(chain) ) {
					
						theSCOP_SCCS = rs.getString("SCCS");
						theSCOP_ID = rs.getString("SID");

						if(startPoint==0 && endPoint==0) {
							// The SCOP domain is the whole chain so percentage is 100
							thePercent = 100;
						} else {

							double in = 0;
							double out = 0;
						
							int[][] hits = maco.getMatchesOnChain(chain);
							// TODO WE NEED TO KNOW HOW MANY POSITIONS ARE NOT IN THE CHAIN 
							// SO THAT THE STATS ARE ACCURATE
							//int[][] outer = maco.getMatchesNotOnChain(chain);
							int outer = maco.getNumPosMatchesNotOnChain(chain);
							
							
							for(int i=0; i<hits.length; i++) {
								int hitStart =  hits[i][0];
								int hitEnd =  hits[i][1];
								int hitLen = hitEnd - hitStart + 1;
								if(hitStart >= startPoint) {
									if(endPoint==0)
										in = in + hitLen;
									else if(hitStart <= endPoint) {
										// now we work out the overlap
										int numIn = 0;
										for(int y=0; y<hitLen; y++) {
											if( (hitStart+y) <= endPoint) 
												numIn++;
										}
										in = in + numIn;
										out = out + (hitLen-numIn);
									} else
										out = out + hitLen;
								}

							}
							thePercent = (100 * (in/(in+out+outer) ) );
						}	
					}
					if(thePercent > bestPercent) {
						bestPercent = thePercent;
						bestSCOP_ID = theSCOP_ID;
						bestSCOP_SCCS = theSCOP_SCCS;
					}

			}
			
			maco.setSCOP_ID(bestSCOP_ID);
			maco.setSCOP_SCCS(bestSCOP_SCCS);
			maco.setSCOPPercent(bestPercent);
			maco.setScop_descript(this.getScopDescription(bestSCOP_SCCS));

		} finally {
			st.close();
		}
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillSCOP4Match(db.search.MatchingStructure)
	 */
	private String getSCOPDescSQL = "SELECT description FROM scop_des WHERE sccs=? AND type='dm'";
	private String getScopDescription(String bestSCOPSCCS)  throws SQLException {
		PreparedStatement st = con.prepareStatement(getSCOPDescSQL);
		try {
			st.setString(1, bestSCOPSCCS.trim() );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				return rs.getString("description");
			}
		} finally {
			st.close();
		}
		return "";
	}


	/*
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillSCOP4Match(db.search.MatchingStructure)
	 */
	private String getPfamSQL2 = "SELECT * FROM pfamA, pdb_pfamA_reg WHERE pdb_pfamA_reg.pdb_id=? AND pdb_pfamA_reg.auto_pfamA=pfamA.auto_pfamA";
	
	public void fillPfam4Match(MatchingStructure ma) throws SQLException {

		PreparedStatement st = con.prepareStatement(getPfamSQL2);

		try {
			st.setString(1, ma.getPdb_id().toLowerCase() );
			ResultSet rs = st.executeQuery();
			String best_ID = "";
			String best_ACC = "";
			double bestPercent = 0;
			
			while(rs.next()) {
 	
				int startPoint = rs.getInt("pdb_res_start");
				int endPoint = rs.getInt("pdb_res_end");
				String chain = rs.getString("chain");

				double thePercent = 0;
				String the_ID = "";
				String the_ACC = "";
				
				//if(chain.equals("") || ma.getChain_id().equals(""))
				//	System.err.println("For match to PDB " + ma.getPdb_id() + " chain is [" + ma.getChain_id() + "] and comparing to chain [" + chain + "]");
				
				if(!chain.equals("") && chain.charAt(0) == ma.getChain_id().charAt(0)) {
					
						the_ACC = rs.getString("pfamA_acc");
						the_ID = rs.getString("pfamA_id");

						if(startPoint==0 && endPoint==0) {
							// The SCOP domain is the whole chain so percentage is 100
							thePercent = 100;
						} else {

							double in = 0;
							double out = 0;
							int[][] hits = ma.getMatchPositions();
							for(int i=0; i<hits.length; i++) {
								int hitStart =  hits[i][0];
								int hitEnd =  hits[i][1];
								int hitLen = hitEnd - hitStart + 1;
								if(hitStart >= startPoint) {
									if(endPoint==0)
										in = in + hitLen;
									else if(hitStart <= endPoint) {
										// now we work out the overlap
										int numIn = 0;
										for(int y=0; y<hitLen; y++) {
											if( (hitStart+y) <= endPoint) 
												numIn++;
										}
										in = in + numIn;
										out = out + (hitLen-numIn);
									} else
										out = out + hitLen;
								}

							}
							thePercent = (100 * (in/(in+out) ) );
						}	
					}
					if(thePercent > bestPercent) {
						bestPercent = thePercent;
						best_ID = the_ID;
						best_ACC = the_ACC;
					}

			}
			
			ma.setPfam_id(best_ID);
			ma.setPfam_acc(best_ACC);
			ma.setPfam_percent(bestPercent);
			ma.setPfam_descript( this.getPfamDescription(best_ACC) );
		} finally {
			st.close();
		}
	}

	
	/*
	 * An alternative verison which works with matches to the entire
	 * PDB structure. This allows matching patterns across chains.
	 * 
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillPfam4Match(db.search.MatchingComplex)
	 */
	public void fillPfam4Match(MatchingComplex maco) throws SQLException {

		PreparedStatement st = con.prepareStatement(getPfamSQL2);

		try {
			st.setString(1, maco.getPdb_id().toLowerCase() );
			ResultSet rs = st.executeQuery();
			String best_ID = "";
			String best_ACC = "";
			double bestPercent = 0;
			
			while(rs.next()) {
 	
				int startPoint = rs.getInt("pdb_res_start");
				int endPoint = rs.getInt("pdb_res_end");
				String chain = rs.getString("chain");

				double thePercent = 0;
				String the_ID = "";
				String the_ACC = "";

				if(!chain.equals("") &&  maco.coversChain(chain) ) {
					
						the_ACC = rs.getString("pfamA_acc");
						the_ID = rs.getString("pfamA_id");

						if(startPoint==0 && endPoint==0) {
							// The SCOP domain is the whole chain so percentage is 100
							thePercent = 100;
						} else {

							double in = 0;
							double out = 0;
							
							int[][] hits = maco.getMatchesOnChain(chain);
							// TODO WE NEED TO KNOW HOW MANY POSITIONS ARE NOT IN THE CHAIN 
							// SO THAT THE STATS ARE ACCURATE
							//int[][] outer = maco.getMatchesNotOnChain(chain);
							int outer = maco.getNumPosMatchesNotOnChain(chain);
							for(int i=0; i<hits.length; i++) {
								int hitStart =  hits[i][0];
								int hitEnd =  hits[i][1];
								int hitLen = hitEnd - hitStart + 1;
								if(hitStart >= startPoint) {
									if(endPoint==0)
										in = in + hitLen;
									else if(hitStart <= endPoint) {
										// now we work out the overlap
										int numIn = 0;
										for(int y=0; y<hitLen; y++) {
											if( (hitStart+y) <= endPoint) 
												numIn++;
										}
										in = in + numIn;
										out = out + (hitLen-numIn);
									} else
										out = out + hitLen;
								}

							}
							thePercent = (100 * (in/(in+out+outer) ) );
						}	
					}
					if(thePercent > bestPercent) {
						bestPercent = thePercent;
						best_ID = the_ID;
						best_ACC = the_ACC;
					}

			}
			
			maco.setPfam_id(best_ID);
			maco.setPfam_acc(best_ACC);
			maco.setPfam_percent(bestPercent);
			maco.setPfam_descript( this.getPfamDescription(best_ACC) );
		} finally {
			st.close();
		}
	}

	
	private String getPfamDescSQL = "SELECT description FROM pfamA WHERE pfamA_acc=?";
	private String getPfamDescription(String bestACC)  throws SQLException {
		PreparedStatement st = con.prepareStatement(getPfamDescSQL);
		try {
			st.setString(1, bestACC.trim() );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				return rs.getString("description");
			}
		} finally {
			st.close();
		}
		return "";
	}


	/*
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillCath4Match(db.search.MatchingStructure)
	 */
	private String getCathSQL1 = "SELECT * FROM cath_fold_positions WHERE pdb_id=? AND chain_id=? ";
	private String getCathSQL2 = "SELECT * FROM CathDomainList, CathNames WHERE CathDomainList.domain_id=? AND CathNames.cath_id=CathDomainList.cath_id";
	
	public void fillCath4Match(MatchingStructure ma) throws SQLException {
		PreparedStatement st = con.prepareStatement(getCathSQL1);
		//System.err.println("FILLING CATH MATCH FOR " + ma.getPdb_id() +" CHAIN: " + ma.getChain_id() );
		try {
			st.setString(1, ma.getPdb_id().toUpperCase() );
			st.setString(2, ma.getAuth_chain_id() );
			ResultSet rs = st.executeQuery();
			String best_DomID = "";
			String best_HierID = "";
			double bestPercent = 0;
			
			while(rs.next()) {
				String the_DomID = rs.getString("domain_id");
				int startPoint = rs.getInt("seq_start");
				int endPoint = rs.getInt("seq_end");
				
				double thePercent = 0;

				int domNum = Integer.parseInt( the_DomID.substring(5, 7) );
				
				//System.err.println("CHECKING OVERLAP OF " + the_DomID + " : " );
				
				if( domNum == 0 ) {
					// The domain is the whole chain so percentage is 100
					thePercent = 100;
				} else {
					// Calculate the overlap
					double in = 0;
					double out = 0;
					int[][] hits = ma.getMatchPositions();
					for(int i=0; i<hits.length; i++) {
						int hitStart =  hits[i][0];
						int hitEnd =  hits[i][1];
						//System.err.println(" MATCH REGION" + hitStart +" - " + hitEnd );
						int hitLen = hitEnd - hitStart + 1;
						if(hitStart >= startPoint) {
							if(endPoint==0)
								in = in + hitLen;
							else if(hitStart <= endPoint) {
								// now we work out the overlap
								int numIn = 0;
								for(int y=0; y<hitLen; y++) {
									if( (hitStart+y) <= endPoint) 
										numIn++;
								}
								in = in + numIn;
								out = out + (hitLen-numIn);
							} else
								out = out + hitLen;
						}

					}
					thePercent = (100 * (in/(in+out) ) );
				}
				
				if(thePercent > bestPercent) {
					bestPercent = thePercent;
					best_DomID = the_DomID;
				}
			}
			

			// Now get the description of the domain
			PreparedStatement st2 = con.prepareStatement(getCathSQL2);
			st2.setString(1, best_DomID );
			ResultSet rs2 = st2.executeQuery();
			while(rs2.next()) {
				best_HierID = rs2.getString("cath_id");
				ma.setCathDomainDescription(rs2.getString("description"));
			}
			
			ma.setCathDomainID(best_DomID);
			ma.setCathDomainPercent(bestPercent);
			ma.setCathHierID(best_HierID);
			ma.setCathDomainDescription( this.getCathDescription(best_HierID) );
		} finally {
			st.close();
		}
	}

	/*
	 * As above but for matches to the entire structure
	 * (non-Javadoc)
	 * @see db.PdbDAO#fillCath4Match(db.search.MatchingStructure)
	 */
	public void fillCath4Match(MatchingComplex maco) throws SQLException {
		
		String[] chains = maco.getUniqueChains();

		String best_DomID = "";
		String best_HierID = "";
		double bestPercent = 0;
		
		for(int c=0; c<chains.length; c++ ) {
			
			PreparedStatement st = con.prepareStatement(getCathSQL1);
			//System.err.println("FILLING CATH MATCH FOR " + ma.getPdb_id() +" CHAIN: " + ma.getChain_id() );
			try {
				st.setString(1, maco.getPdb_id().toUpperCase() );
				st.setString(2, chains[c] );
				ResultSet rs = st.executeQuery();
				
				while(rs.next()) {
					String the_DomID = rs.getString("domain_id");
					int startPoint = rs.getInt("seq_start");
					int endPoint = rs.getInt("seq_end");
					
					double thePercent = 0;
	
					int domNum = Integer.parseInt( the_DomID.substring(5, 7) );
					
					//System.err.println("CHECKING OVERLAP OF " + the_DomID + " : " );
					
					if( domNum == 0 ) {
						// The domain is the whole chain so percentage is 100
						thePercent = 100;
					} else {
						// Calculate the overlap
						double in = 0;
						double out = 0;
						
						int[][] hits = maco.getMatchesOnChain(chains[c]);
						// TODO WE NEED TO KNOW HOW MANY POSITIONS ARE NOT IN THE CHAIN 
						// SO THAT THE STATS ARE ACCURATE
						//int[][] outer = maco.getMatchesNotOnChain(chain);
						int outer = maco.getNumPosMatchesNotOnChain(chains[c]);
						
						for(int i=0; i<hits.length; i++) {
							int hitStart =  hits[i][0];
							int hitEnd =  hits[i][1];
							//System.err.println(" MATCH REGION" + hitStart +" - " + hitEnd );
							int hitLen = hitEnd - hitStart + 1;
							if(hitStart >= startPoint) {
								if(endPoint==0)
									in = in + hitLen;
								else if(hitStart <= endPoint) {
									// now we work out the overlap
									int numIn = 0;
									for(int y=0; y<hitLen; y++) {
										if( (hitStart+y) <= endPoint) 
											numIn++;
									}
									in = in + numIn;
									out = out + (hitLen-numIn);
								} else
									out = out + hitLen;
							}
	
						}
						thePercent = (100 * (in/(in+out+outer) ) );
					}
					
					if(thePercent > bestPercent) {
						bestPercent = thePercent;
						best_DomID = the_DomID;
					}
				}

			} finally {
				st.close();
			}
		}
		
		PreparedStatement st2 = con.prepareStatement(getCathSQL2);
		
		try {
			// Now get the description of the domain
			st2.setString(1, best_DomID );
			ResultSet rs2 = st2.executeQuery();
			while(rs2.next()) {
				best_HierID = rs2.getString("cath_id");
				maco.setCathDomainDescription(rs2.getString("description"));
			}
		
			maco.setCathDomainID(best_DomID);
			maco.setCathDomainPercent(bestPercent);
			maco.setCathHierID(best_HierID);
			maco.setCathDomainDescription( this.getCathDescription(best_HierID) );
		} finally {
			st2.close();
		}
	}
	
	
	
	private String getCathDescSQL = "SELECT description FROM CathNames WHERE cath_id=?";
	private String getCathDescription(String bestHierID)  throws SQLException {
		PreparedStatement st = con.prepareStatement(getCathDescSQL);
		try {
			st.setString(1, bestHierID.trim() );
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				return rs.getString("description");
			}
		} finally {
			st.close();
		}
		return "";
	}

	/*
	 * A utility function to convert the SCOP descriptions into real seq positions
	 */
	public void convertSCOPPositions(String pdbid) throws SQLException {
		//PreparedStatement st = con.prepareStatement("SELECT * FROM cla");
		PreparedStatement st = con.prepareStatement("SELECT * FROM scop_cla WHERE pdb_id='" + pdbid +"'");

		try {
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				String scopid = rs.getString("SID");
				//String pdbid = rs.getString("PDB_ID");
				String[] descs = rs.getString("DESCRIPTION").split(",");
				for(int d=0; d<descs.length; d++) {
					String chainID = "";
					String startPos = "";
					String startInsCode = "";
					String endPos = "";
					String endInsCode = "";
					String desc = descs[d];
					
					chainID=desc.substring(0,1);
					//System.err.print("Dealing with description: " + desc);
					if(desc.length()>2) {
						String pos = desc.substring(2);
						if(pos.charAt(0)=='-') {
							startPos = "-";
							pos = pos.substring(1);
						}
						//System.err.print(" split into pos: " + pos + "\n");
						// Now we parse out the two positions
						String[] possies = pos.split("-");
						String[] posNIns = splitIntoPosAndInsCode(possies[0]);
						startPos = startPos + posNIns[0];
						startInsCode =  posNIns[1];
						posNIns = splitIntoPosAndInsCode(possies[1]);
						endPos = posNIns[0];
						endInsCode =  posNIns[1];
					}
					int seqStart = 0;
					int seqEnd = 0;
					
					//if(!startInsCode.equals("") || !endInsCode.equals(""))
					//	System.err.println(" PROCESSED DESCRIPTOR: [" + desc + "] SID " +  scopid);
					
					// Now we check if we need to convert the positions to real seq ids
					if(!startPos.equals("")) {
						seqStart = convertPosAndInsCodeToSeqPos(pdbid,chainID,startPos,startInsCode);
					}
					if(!endPos.equals("")) {
						seqEnd = convertPosAndInsCodeToSeqPos(pdbid,chainID,endPos,endInsCode);
					}

					String newChainid = convertChainId(pdbid,chainID);
					
					//if(!startInsCode.equals("") || !endInsCode.equals(""))
					//	System.err.println(" POSITIONS: [" + seqStart + "] - [" +  seqEnd + "]");
					// Now we update the SCOP table to contain the real seq positions
					// Using a nest ed query
					String query = "INSERT INTO scop_fold_positions VALUES(?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement st2 = con.prepareStatement(query);
					try {
						st2.setInt(1, 0);
						st2.setString(2, scopid);
						st2.setString(3, pdbid);
						st2.setString(4, chainID);
						st2.setString(5, newChainid);
						st2.setInt(6, seqStart);
						st2.setInt(7, seqEnd);

						st2.executeUpdate();
					}
					finally {
						st2.close();
					}
				}
			}
		} finally {
			st.close();
		}
	}


	/*
	 * A utility function to convert the CATH descriptions into real seq positions
	 */
	public void convertCATHPositions(String pdbid) throws SQLException {

		PreparedStatement st = con.prepareStatement("SELECT * FROM CathDomall WHERE pdb_id='" + pdbid.toLowerCase() +"'");
		//System.err.print("PROCESSING CATH DOMAINS FOR " + pdbid);
		try {
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				String domid = rs.getString("DOMAIN_ID");

				int segID =  rs.getInt("SEGMENT_ID");
				String authchainID =  rs.getString("CHAIN_ID");
				String startPos =  rs.getString("seq_start");
				String startInsCode =  rs.getString("start_ins_code");
				if(startInsCode.equals("-"))
					startInsCode="";
				String endPos =  rs.getString("seq_end");
				String endInsCode =  rs.getString("end_ins_code");
				if(endInsCode.equals("-"))
					endInsCode="";
				int seqStart = 0;
				int seqEnd = 0;
				// Convert the positions to real seq ids

				seqStart = convertPosAndInsCodeToSeqPos(pdbid,authchainID,startPos,startInsCode);
				seqEnd = convertPosAndInsCodeToSeqPos(pdbid,authchainID,endPos,endInsCode);

				String newChainid = convertChainId(pdbid,authchainID);
				
				// Now we update the REF CATH table to contain the real seq positions
				// Using a nest ed query
				String query = "INSERT INTO cath_fold_positions VALUES(?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement st2 = con.prepareStatement(query);
				try {
						st2.setInt(1, segID);
						st2.setString(2, domid);
						st2.setString(3, pdbid);
						st2.setString(4, authchainID);
						st2.setString(5, newChainid);
						st2.setInt(6, seqStart);
						st2.setInt(7, seqEnd);

						st2.executeUpdate();
				}
				finally {
					st2.close();
				}
			}
		} finally {
			st.close();
		}
	}



	private String convertChainId(String pdbid, String chainID) throws SQLException {
		String seqPosSQL = "SELECT DISTINCT asym_id FROM pdb_protein WHERE pdb_id=? AND auth_asym_id=?";
		PreparedStatement st = con.prepareStatement(seqPosSQL);
		String result = chainID;
		try {
			st.setString(1, pdbid);
			st.setString(2, chainID);
			ResultSet rs = st.executeQuery();

			while(rs.next()) {	
				result = rs.getString("asym_id");
			}
		}
		finally {
			st.close();
		}
		return result;
	}
	
	
	private int convertPosAndInsCodeToSeqPos(String pdbid, String chainID, String startPos, String startInsCode) throws SQLException {
		if(!startInsCode.equals(""))
			System.err.println(" > Getting seq pos for PDBID: " + pdbid + " Chain " + chainID + " pos [" + startPos + "] INS CODE=" + startInsCode);
		String seqPosSQL = "SELECT DISTINCT seq_id FROM pdb_atom_site WHERE pdb_id=? AND auth_asym_id=? AND auth_seq_id=? AND pdbx_pdb_ins_code=?";
		PreparedStatement st = con.prepareStatement(seqPosSQL);
		int result = 0;
		try {
			st.setString(1, pdbid);
			st.setString(2, chainID);
			st.setString(3, startPos);
			st.setString(4, startInsCode);

			ResultSet rs = st.executeQuery();
	
			while(rs.next()) {	
				result = rs.getInt("seq_id");
			}
		}
		finally {
			st.close();
		}
		return result;
	}

	private String[] splitIntoPosAndInsCode(String string) {
		String[] results = new String[2];
		results[0] = "";
		results[1] = "";
		char[] thestuff = string.toCharArray();
		boolean switched=false;
		for(int i=0; i<thestuff.length; i++) {
			if( (thestuff[i] >= 'A' && thestuff[i] <= 'Z') || (thestuff[i] >= 'a' && thestuff[i] <= 'z') ) {
				switched=true;
			}
			// Now we simply add characters to the two elements
			if(switched) // Once we see a letter it is added to the Ins Code
				results[1] = results[1] + thestuff[i];
			else // Until then it is a numeric character that is added to the position.
				results[0] = results[0] + thestuff[i];
		}
		
		return results;
	}
	
	private static String[] dbPropsMySQL = {"com.mysql.jdbc.Driver",
			"jdbc:mysql://localhost/proteinlab",
			"user=proteinlab",
			"password=proteinlab"};
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.err.println("Running Test on insertPROSITE() ");
		DatabaseConnectionProvider mysqlProvider = new SimpleDatabaseConnectionProviderImpl(new DatabaseConnectionProperties(dbPropsMySQL) );	
		
		try {
			minimalPdbDAO test = new minimalPdbDAO( mysqlProvider.getConnection() );
			test.insertPROSITE();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * A utility function that parse the PROSITE files and put the data in the database
	 */
	private String pathToPROSITE = "/Projects/3DStructRegExp/Database/Prosite/";
	public void insertPROSITE() throws SQLException {
		String listfile = pathToPROSITE + "prosite.lis";
		String mainfile = pathToPROSITE + "prosite.txt";

		String prosite_id="";
		String prosite_description="";
		String prosite_short="";
		String prosite_long="";
		String prosite_update="";
		String prosite_patterns="";
		String prosite_annotations="";
		boolean patternStarted = false;
		boolean patternComplete = true;
		int patternNum=0;
		int index = 0;
		int subindex = 0;
		String line="";
		try {
			BufferedReader reader=new BufferedReader(new FileReader(mainfile));

			line=reader.readLine();
			while (line!=null) {
				if(line.length()>4 && line.substring(0,5).equals("{END}") ) {
					if(index>0) {
						/*
						System.out.print("ID "+ prosite_id + " " + prosite_short + " " + prosite_long );
						System.out.print("\nDESCRIP: "+ prosite_description + "\n");
						if(prosite_patterns.equals("")) {
							System.out.print("    NO PATTERNS" );
						} else {
							System.out.print("   PATTERN: " + prosite_patterns.substring(0,prosite_patterns.length()));
						}
						System.out.print("\n");
						*/
						if( !prosite_patterns.equals("") ) {
							// We output THE ENTRY PATTERNS 
							int year = 0;
							
							Pattern yearPattern = Pattern.compile("[0-9][0-9][0-9][0-9]");
							Matcher matcher = yearPattern.matcher(prosite_update);
							
							if(matcher.find()) {
								year = Integer.parseInt( prosite_update.substring( matcher.start(),matcher.end() ) );
							}
							System.out.println("INSERT INTO prosite_entry VALUES (0, \""+ prosite_id + "\",\"" + prosite_short + "\",\"" + prosite_long + "\",\""  + prosite_update + "\",\"" + prosite_description + "\"," + year + ");");
							String[] patterns = prosite_patterns.split("\n");
							String[] annots = prosite_annotations.split("\n");
							for(int p=0; p<patterns.length;p++) {
								String theAnnot = "";
								if(annots.length>p)
									theAnnot = annots[p];
								System.out.println("  INSERT INTO prosite_pattern VALUES (0, \""+ prosite_id + "\"," + p + ",\"" + patterns[p] + "\",\""  + theAnnot + "\");");
							}
						}
					}
					//System.err.print("HERE: " + subindex + "\n");
					index++;
					subindex=1;
					// And start again.
					prosite_id="";
					prosite_description="";
					prosite_short="";
					prosite_long="";
					prosite_patterns="";
					prosite_annotations="";
					prosite_update="";
					patternStarted = false;
					patternComplete = true;
					patternNum = 0;
				} else if(index>0 ) {
					if(subindex==0 || subindex==-1) {
						if(line.length()>2){
							subindex++;
						}
					} else if(subindex==1) {
						if(line.length()>2){
							subindex++;
						}
					} else if(subindex==2) {
						//grab the ID and short name
						prosite_id=line.substring(1,8);
						prosite_short=line.substring(10,line.length()-1);
						subindex++;
					} else if(subindex==3) {
						// NOW WE WAIT UNTIL WE SEE "{BEGIN}" BEFORE INCREMENT
						if(line.length()>6 && line.subSequence(0, 7).equals("{BEGIN}")) {
							subindex++;
						}
					} else if(subindex==4) {
						// THIS SHOULD BE ***************************
						subindex++;
					} else if(subindex==5) {
						prosite_long=line.substring(2,line.length()-1).trim();
						subindex++;
					} else if(subindex==6 ||subindex==7) {
						subindex++;
					} else if(subindex==8) {
						// We collect stuff for the description until blank
						if(line.length()<1) {
							subindex++;
						} else {
							prosite_description=prosite_description+" "+line;
						}
					} else if(subindex==9) {
						// THE FINAL SUB INDEX
						// WE KEEP LOOKING FOR PATTERNS
						if(patternStarted) {
							if(line.length()>0 && line.subSequence(0, 1).equals("-")) {
								patternStarted=false;
								if(line.length()>13 && line.substring(0,13).equals("-Last update:") ) {
									// GRAB THE YEAR
									prosite_update = line.substring(14);
								}
							} else if(!patternComplete) {
								prosite_patterns = prosite_patterns + line.trim();
								if( ! (prosite_patterns.charAt(prosite_patterns.length()-1)=='-') ){
									patternComplete=true;
									patternNum++;
								}
							} else {
								prosite_annotations=prosite_annotations + line.trim();
							}
						} else if(line.length()>19 && line.substring(0,19).equals("-Consensus pattern:")) {
							// WE START A NEW PATTERN
							if( patternNum > 0 ) {
								prosite_patterns = prosite_patterns + "\n";
								prosite_annotations = prosite_annotations + "\n";
							}
							prosite_patterns = prosite_patterns + line.substring(19);
							patternStarted = true;
							if( prosite_patterns.charAt(prosite_patterns.length()-1)=='-' ){
								patternComplete=false;
							} else {
								patternComplete=true;
								patternNum++;
							}
						} else if(line.length()>13 && line.substring(0,13).equals("-Last update:") ) {
							// GRAB THE YEAR
							prosite_update = line.substring(14);
						}
					}
				}
				line=reader.readLine();
			}
		} catch (Exception e) {
			System.err.print("Problem reading the PROSITE File: " + mainfile + "\n");
			System.err.print("LINE : " + line + "\n");
			System.err.print("PROSITE ID : " + prosite_id + "\n");
			System.err.print("SUB INDEX: " + subindex + "\n");
			System.exit(3);
		}

	}
	
	
	
	/*
	 * A utility function that will go through the clustered sequence files
	 * and annotate each PDB protein to indicate if it is in the redundancy reduced
	 * versions of the PDB
	 */
	private String pathToClusters = "/Projects/3DStructRegExp/Database/clusters/";
	public void annotateRedundancy() throws SQLException {
		String[] filelist = new String[]{
					pathToClusters+"bc-100.out",
					pathToClusters+"bc-95.out", 
					pathToClusters+"bc-90.out", 
					pathToClusters+"bc-70.out", 
					pathToClusters+"bc-50.out", 
					pathToClusters+"bc-40.out", 
					pathToClusters+"bc-30.out"
					};
		
		int[] thresholds = new int[]{
				100, 
				95, 
				90, 
				70, 
				50, 
				40, 
				30
				};
		
		for(int f=0; f<filelist.length; f++) {
			String clusterfile = filelist[f];
			int index = 1;
			try {
				  BufferedReader reader=new BufferedReader(new FileReader(clusterfile));
				  String line=reader.readLine();
				  while (line!=null) {
					  if(line.length()>5) {
						  String pdb = line.substring(0,4);
						  String chain = line.substring(5,6);
						  String others = line.substring(6).trim();
						  insertPDBRedundancy(index, thresholds[f], pdb, chain, others);
						  index++;
					  }
				      line=reader.readLine();
				  }
		      } catch (Exception e) {
		    	  System.err.print("Problem reading the Cluster File: " + clusterfile + "\n");
				  System.exit(3);
		      }
			
		}
	}
	private static final String CLUSTER_INSERT = "INSERT INTO pdb_clusters VALUES(0, ?, ?, ?, ?, ? )";
	private void insertPDBRedundancy(int index, int thresh, String pdb,	String chain, String others) throws SQLException  {
		PreparedStatement st = con.prepareStatement( CLUSTER_INSERT );
		try {			
			st.setInt(1, index );
			st.setInt(2, thresh );
			st.setString(3, pdb );
			st.setString(4, chain );
			st.setString(5, others );
			st.executeUpdate();
		} finally {
			st.close();
		}
		
	}

	/*
	private static final String[] PDB_FILE_REDUNDANCY = new String[] {
		"UPDATE pdb_protein SET pdb95=1 WHERE pdb_id=? AND auth_asym_id=?",
		"UPDATE pdb_protein SET pdb90=1 WHERE pdb_id=? AND auth_asym_id=?",
		"UPDATE pdb_protein SET pdb70=1 WHERE pdb_id=? AND auth_asym_id=?",
		"UPDATE pdb_protein SET pdb50=1 WHERE pdb_id=? AND auth_asym_id=?",
		"UPDATE pdb_protein SET pdb40=1 WHERE pdb_id=? AND auth_asym_id=?",
		"UPDATE pdb_protein SET pdb30=1 WHERE pdb_id=? AND auth_asym_id=?",
		};

	private void updatePDBRedundancy(String pdb, String chain, int f) throws SQLException {
		PreparedStatement st = con.prepareStatement( PDB_FILE_REDUNDANCY[f] );

		try {			
			st.setString(1, pdb );
			st.setString(2, chain );
			st.executeUpdate();
		} finally {
			st.close();
		}
	}*/

	
	/*
	 * This function is exeuted so that the original Auth Asym Ids are
	 * available at the level of looking at the protein chains.
	 * In the original implementation, author annotations are only
	 * available at the atom site level of the data structure.
	 */
	public void updateProteinAuthAsymIds() throws SQLException {
		
		//First we retrieve all rows for the iterations
		String getALL = "SELECT * FROM pdb_protein";
		String getNEW = "SELECT DISTINCT auth_asym_id FROM pdb_atom_site WHERE pdb_id=? AND asym_id=?";
		String update = "UPDATE pdb_protein SET auth_asym_id=? WHERE pdb_id=? AND asym_id=?";
		
		PreparedStatement st1 = con.prepareStatement(getALL);
	
		try {
			ResultSet rs1 = st1.executeQuery();
	
			while(rs1.next()) {	
				String pdbid = rs1.getString("PDB_ID");
				String chain = rs1.getString("ASYM_ID");
				PreparedStatement st2 = con.prepareStatement(getNEW);
				try {
					st2.setString(1, pdbid);
					st2.setString(2, chain);
					ResultSet rs2 = st2.executeQuery();
					while(rs2.next()) {	
						String yeah = rs2.getString("AUTH_ASYM_ID");

						PreparedStatement st3 = con.prepareStatement(update);
						
						try {
							st3.setString(1, yeah);
							st3.setString(2, pdbid);
							st3.setString(3, chain);
							st3.executeUpdate();
						} finally {
							st3.close();
						}
					}
				} finally {
					st2.close();
				}
			}
		} finally {
			st1.close();
		}
	}

	private static final String INSERT_REQUEST = "INSERT INTO search_request VALUES(0, ?, ?, ?)";
	private static final String INSERT_QUERY = "INSERT INTO search_query VALUES(0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_RESULTS = "INSERT INTO search_result VALUES(0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public int saveQuery(DBQuery query, List<MatchingComplex> finalSet) throws SQLException  {
		System.err.println("Saving the query results");
		int queryId = query.getQueryId();
		boolean insertResults = true;
		if(queryId==0) { // queryID 0 means this is the first time it has been run
			// So we insert the details of the query and recover the new query ID
			PreparedStatement st = con.prepareStatement( INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS );
			try {			
				st.setString(1, query.getOriginalQuery() );
				st.setString(2, query.getFunctionKnown() );
				st.setString(3, query.getTech() );
				st.setString(4, query.getKeywords() );
				st.setString(5, query.getResolutionOperator());
				st.setDouble(6, query.getResolutionAsDouble() );
				st.setString(7, query.getRedundancy() );
				st.setInt(8, query.getMinChainLength() );
				st.setInt(9, query.getMaxChainLength() );
				st.setBoolean(10, query.isMultipleHitsPerStructure() );
				st.setBoolean(11, query.usePseudoAtoms() );
				st.executeUpdate();
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					queryId = rs.getInt(1);
				}
				rs.close();

			} finally {
				st.close();
			}
		} else {
			insertResults = false;
		}
	
		/*
		 * Now if we have a valid query ID, we insert the request
		 */
		if(queryId != 0) {
			PreparedStatement st2 = con.prepareStatement( INSERT_REQUEST );
			try {			
				st2.setInt(1, queryId );
				st2.setString(2, query.getUser_id() );
				st2.setString(3, query.getDatestamp() );
				st2.executeUpdate();
			} finally {
				st2.close();
			}
		}
		
		/*
		 * Now if we have a valid query ID, we insert all the results
		 */
		if( insertResults ) {
			
			PreparedStatement stmnt = con.prepareStatement( INSERT_RESULTS );
			try {
				for (MatchingComplex m : finalSet) {
							
					stmnt.setInt(1, queryId );
					stmnt.setDouble(2, m.getScore() );
					stmnt.setString(3, m.getPdb_id());
					stmnt.setString(4, m.getMatchChainsAsString() );
					stmnt.setString(5, m.getAuthMatchChainsAsString() );
					stmnt.setString(6, m.getMatchPositionsAsString() );
					stmnt.setString(7, m.getMatchResiduesAsString() );
					stmnt.setString(8, m.getMatchSSEsAsString() );
					stmnt.setString(9, m.getMatchAnglesAsString() );
					stmnt.setString(10, m.getMatchDistancesAsString() );
					stmnt.setString(11, m.getAuthMatchPositionsAsString() );
					stmnt.setString(12, m.getAuthMatchPInsCodesAsString() );
					stmnt.setString(13, m.getScop_id() );
					stmnt.setString(14, m.getScop_sccs() );
					stmnt.setDouble(15, m.getScop_percent() );
					stmnt.setString(16, m.getCathDomainID());
					stmnt.setString(17, m.getCathHierID() );
					stmnt.setDouble(18, m.getCathDomainPercent() );
					stmnt.setString(19, m.getPfam_id());
					stmnt.setString(20, m.getPfam_acc() );
					stmnt.setDouble(21, m.getPfam_percent());
					
					stmnt.executeUpdate();
				}
			} finally {
				stmnt.close();
			}
			
		}
		System.err.println("Saved ID: " + queryId);
		return queryId;
	}
	
	/*
	 * Admin Methods
	 */
	public List<QueryResult> getQueryList() throws SQLException {
		// First we insert the details of the query and recover the query ID
		PreparedStatement st = con.prepareStatement( "SELECT * FROM search_request, search_query WHERE search_request.query_id=search_query.id ORDER BY datestamp DESC" );
		
		List<QueryResult> results = new LinkedList<QueryResult>();
		try {

			ResultSet rs = st.executeQuery();
			while(rs.next()) {	
				QueryResult e = new QueryResult(
						rs.getInt("ID"), 
						rs.getString("QUERY_STRING"), 
						rs.getString("USER_ID"), 
						rs.getString("DATESTAMP"),
						rs.getString("TECHNOLOGY"),
						rs.getString("KEYWORDS"),
						rs.getString("RESOLUTION_OP"),
						rs.getString("RESOLUTION"),
						rs.getInt("CHAIN_LEN_MIN"), 
						rs.getInt("CHAIN_LEN_MAX"));
				results.add( e );
			}
		} finally {
			st.close();
		}
		
		return results;
	}

	/* OLDIE BUT A GOODIE
	public List<MatchingStructure> retrieveSavedSearch( DBQuery q ) throws SQLException {
		
		String myStatement = getSavedSearchQueryString( q );
		PreparedStatement st = con.prepareStatement( myStatement );
		
		try {
			insertSavedSearchQueryParams(q, st, 1);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				int queryID = rs.getInt("ID");
				q.setQueryId(queryID);
				
				return retrieveSavedSearch(queryID);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st.close();
		}
		
		return null;
	}*/
	
	public List<MatchingComplex> retrieveSavedSearch( DBQuery q ) throws SQLException {
		
		String myStatement = getSavedSearchQueryString( q );
		PreparedStatement st = con.prepareStatement( myStatement );
		
		try {
			insertSavedSearchQueryParams(q, st);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				/*
				 * Now we check and extract the matching structures for the query
				 */
				int queryID = rs.getInt("ID");
				q.setQueryId(queryID);
				
				return retrieveSavedSearch(queryID);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st.close();
		}
		
		return null;
	}
	
	/*
	 * GET THE QUERY STRING
	 */
	public String getSavedSearchQueryString(DBQuery query) {
		
		String queryString = "SELECT * FROM search_query WHERE multiplehits=? AND usepseudo=? ";
		if(query.getRedundancy()!=null && !query.getRedundancy().equals("") ) {
			queryString = queryString + " AND redundancy=? ";
		} 
		if(query.getOriginalQuery()!=null && !query.getOriginalQuery().equals("") ) {
			queryString = queryString + " AND query_string=? ";
		}
		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			queryString = queryString + " AND keywords=? ";
		}
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			queryString = queryString + " AND resolution=? ";
			queryString = queryString + " AND resolution_op=? ";
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			queryString = queryString + " AND technology=? ";
		}
		if( query.getFunctionKnown() != "" ) {
			queryString = queryString + " AND func=? ";
		}

		return queryString;
	}

	/*
	 * FILL THE QUERY STRING
	 */
	private void insertSavedSearchQueryParams(DBQuery query, PreparedStatement st) throws SQLException {
		
		int paramIndex = 3;

		st.setBoolean(1, query.isMultipleHitsPerStructure() );
		st.setBoolean(2, query.usePseudoAtoms() );
		
		if(query.getRedundancy()!=null && !query.getRedundancy().equals("") ) {
			st.setString(paramIndex, query.getRedundancy() );
			paramIndex++;
		}
		if(query.getOriginalQuery()!=null && !query.getOriginalQuery().equals("") ) {
			st.setString(paramIndex, query.getOriginalQuery() );
			paramIndex++;
		}
		if(query.getKeywords()!=null && ! query.getKeywords().equals("") ) {
			st.setString(paramIndex, query.getKeywords());
			paramIndex++;
		}
		if(query.getResolution()!=null && !query.getResolution().equals("")) {
			st.setDouble(paramIndex, query.getResolutionAsDouble() );
			paramIndex++;			
			st.setString(paramIndex, query.getResolutionOperator() );
			paramIndex++;
		}
		if(query.getTech()!=null && !query.getTech().equals("")) {
			st.setString(paramIndex, query.getTech() );
			paramIndex++;
		}
		if( query.getFunctionKnown() != "" ) {
			st.setString(paramIndex, query.getFunctionKnown() );
			paramIndex++;
		}
	}
	
	
	/*
	OLD VERSION BEFORE USING WHOLE COMPLEX MATCHING
	public List<MatchingStructure> retrieveSavedSearch(int queryId) throws SQLException {

		PreparedStatement st2 = con.prepareStatement( "SELECT * FROM search_result WHERE query_id=?" );
		PreparedStatement st3 = con.prepareStatement( "SELECT keywords FROM pdb_file WHERE pdb_id=?" );
		
		int NumFound = 0;
		List<MatchingStructure> results = new LinkedList<MatchingStructure>();
		try {

			st2.setInt(1, queryId );
		
			ResultSet rs2 = st2.executeQuery();
			while(rs2.next()) {

				st3.setString(1, rs2.getString("pdb_id") );
				ResultSet rs3 = st3.executeQuery();
				rs3.first();
				
				MatchingStructure ma = new MatchingStructure(
					rs2.getFloat("SCORE"), 
					rs2.getString("pdb_id"), 
					rs2.getString("asym_id"), 
					rs2.getString("auth_asym_id"),
					rs3.getString("keywords"),
					rs2.getString("match_positions"),
					rs2.getString("match_residues"),
					rs2.getString("match_sses"),
					rs2.getString("match_angles"),
					rs2.getString("match_distances"),
					rs2.getString("auth_match_positions"),
					rs2.getString("auth_match_inscodes"),
					rs2.getString("scop_id"),
					rs2.getString("scop_sccs"),
					rs2.getDouble("scop_percent"),
					rs2.getString("cath_dom_id"),
					rs2.getString("cath_hier_id"),
					rs2.getDouble("cath_percent"),
					rs2.getString("pfam_id"),
					rs2.getString("pfam_acc"),
					rs2.getDouble("pfam_percent"));
				

				ma.setScop_descript(this.getScopDescription( ma.getScop_sccs() ));
				ma.setPfam_descript( this.getPfamDescription( ma.getPfam_acc() ) );
				ma.setCathDomainDescription( this.getCathDescription( ma.getCathHierID() ) );
				
				results.add( ma );
				NumFound++;
			}	

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st2.close();
		}
		if(NumFound>0)
			return results;
		else 
			return null;
	}
*/
	
	public List<MatchingComplex> retrieveSavedSearch(int queryId) throws SQLException {

		PreparedStatement st2 = con.prepareStatement( "SELECT * FROM search_result WHERE query_id=?" );
		PreparedStatement st3 = con.prepareStatement( "SELECT keywords FROM pdb_file WHERE pdb_id=?" );
		
		int NumFound = 0;
		List<MatchingComplex> results = new LinkedList<MatchingComplex>();
		try {

			st2.setInt(1, queryId );
		
			ResultSet rs2 = st2.executeQuery();
			while(rs2.next()) {

				st3.setString(1, rs2.getString("pdb_id") );
				ResultSet rs3 = st3.executeQuery();
				rs3.first();
				
				MatchingComplex ma = new MatchingComplex(
					rs2.getFloat("SCORE"), 
					rs2.getString("pdb_id"), 
					rs3.getString("keywords"),
					rs2.getString("match_chains"),
					rs2.getString("match_positions"),
					rs2.getString("match_residues"),
					rs2.getString("match_sses"),
					rs2.getString("match_angles"),
					rs2.getString("match_distances"),
					rs2.getString("auth_match_positions"),
					rs2.getString("auth_match_inscodes"),
					rs2.getString("auth_match_chains"),
					rs2.getString("scop_id"),
					rs2.getString("scop_sccs"),
					rs2.getDouble("scop_percent"),
					rs2.getString("cath_dom_id"),
					rs2.getString("cath_hier_id"),
					rs2.getDouble("cath_percent"),
					rs2.getString("pfam_id"),
					rs2.getString("pfam_acc"),
					rs2.getDouble("pfam_percent"));
				

				ma.setScop_descript(this.getScopDescription( ma.getScop_sccs() ));
				ma.setPfam_descript( this.getPfamDescription( ma.getPfam_acc() ) );
				ma.setCathDomainDescription( this.getCathDescription( ma.getCathHierID() ) );
				
				results.add( ma );
				NumFound++;
			}	

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			st2.close();
		}
		if(NumFound>0)
			return results;
		else 
			return null;
	}
	
	
	private static final String DIS_BOND_INSERT = "INSERT INTO pdb_dis_bonds VALUES(0, ?, ?, ?, ?, ? )";
	
	public void insertPDBDisBonds(List<DisBond> pdbDisBonds, Object object) throws SQLException{
		PreparedStatement st = con.prepareStatement( DIS_BOND_INSERT );

		try {			
			for (structools.structure.DisBond e : pdbDisBonds ) {
				st.setString(1, e.getPdb_id() );
				st.setString(2, e.getAsym_id1() );
				st.setInt(3, e.getSeq_id1() );
				st.setString(4, e.getAsym_id2() );
				st.setInt(5, e.getSeq_id2() );
				st.executeUpdate();
			}
			
		} finally {
			st.close();
		}
		
	}

	/*
	 * THIS SHOULD BE PROBABLY DEPRECATED(non-Javadoc)
	 * 
	 * @see db.PdbDAO#getProteinComplexes(db.search.DBQuery)
	 */
	public List<ProteinComplex> getProteinComplexes(DBQuery query) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * We extract all the residues for the specified PDB and convert the 
	 * side chain atoms into a single psuedo point.
	 */
	public void convertPDBAtomSitesToPseudoPoints(String pdbID) throws SQLException {
		 List<AtomSite> allResidues =  getPDBFileProteinAlphaCs(pdbID);
		 for (AtomSite site : allResidues) {
			 double[] position = getPseudoPoint(pdbID, site.getLABEL_ASYM_ID(), site.getLABEL_SEQ_ID(), site.getA_AA() );
			 if(position != null) {
				 PreparedStatement st = con.prepareStatement( PDB_FILE_PSEUDO_POINT_INSERT );

					try {			
							//System.out.println("Inserting: " + e.getPDB_ID() + "  (" + e.getLABEL_SEQ_ID() + ")" + "  [" + e.getA_AA() + "]" );
							st.setInt(1, 0 );
							st.setString(2, site.getPDB_ID() );
							st.setInt(3, site.getENTITY_ID() );
							st.setInt(4, site.getLABEL_SEQ_ID() );
							st.setString(5, site.getLABEL_ASYM_ID() );
							st.setString(6, "PSEU" );
							st.setString(7, site.getA_AA() );
							st.setDouble(8, position[0] );	
							st.setDouble(9,  position[1] );	
							st.setDouble(10,  position[2] );	
							st.setString(11, site.getAUTH_ASYM_ID() );
							st.setString(12, site.getAUTH_SEQ_ID() );
							st.setString(13, site.getPDBX_PDB_INS_CODE() );
							st.executeUpdate();
						
					} finally {
						st.close();
					}
			 }
		 }
	}

	private double[] getPseudoPoint(String pdbID, String labelASYMID, int labelSEQID, String aAA) throws SQLException {
		String[] atomsToUse = getSideChainResidues(aAA);
		double[] results = new double[3];
		double count = 0;
		if(atomsToUse != null) {
			for(int a=0; a<atomsToUse.length; a++) {
				List<AtomSite> asites = this.getAtomSite(pdbID, labelASYMID, labelSEQID, atomsToUse[a]);
				for(AtomSite site : asites) {
					results[0] = results[0] + site.getCARTN_X();
					results[1] = results[1] + site.getCARTN_Y();
					results[2] = results[2] + site.getCARTN_Z();
					count = count + 1;
				}
			}
		}
		if(count > 0) {
			results[0] = results[0] / count;
			results[1] = results[1] / count;
			results[2] = results[2] / count;
			return results;
		} else {
			return null;
		}
	}

	
	/*
	 * getSideChainResidues(String resCode)
	 * 
	 * Retrive the set of atom ids used for pseudo point calculation in the method 
	 * of Bahar and Jernigan 1996
     *	http://www.ncbi.nlm.nih.gov/pubmed/9080182
	 *
	 */
	
	public String[] getSideChainResidues(String resCode) {
		if( resCode.equals("GLY") || resCode.equals("G") ) {
			return new String[] { "CA" };
		}
		if( resCode.equals("ALA") || resCode.equals("A") ) {
			return new String[] { "CB" };
		}
		if( resCode.equals("VAL") || resCode.equals("V") ) {
			return new String[] { "CG1","CG2" };
		}
		if( resCode.equals("ILE") || resCode.equals("I") ) {
			return new String[] { "CD1" };
		}
		if( resCode.equals("LEU") || resCode.equals("L") ) {
			return new String[] { "CD1","CD2" };
		}
		if( resCode.equals("SER") || resCode.equals("S") ) {
			return new String[] { "OG" };
		}
		if( resCode.equals("THR") || resCode.equals("T") ) {
			/* return new String[] {"OG1","CG2" };  THIS IS NOT RIGHT */
			return new String[] {"OG1" }; 
		}
		if( resCode.equals("ASP") || resCode.equals("D") ) {
			return new String[] {"OD1","OD2" }; /* DIFFERENT FROM PAPER BUT RIGHT */
		}		
		if( resCode.equals("ASN") ||  resCode.equals("N") ) {
			return new String[] {"OD1","ND2" };
		}
		if( resCode.equals("GLU") || resCode.equals("E") ) {
			return new String[] {"OE1","OE2" };
		}
		if( resCode.equals("GLN") || resCode.equals("Q")) {
			return new String[] {"OE1","NE2" };
		}
		if( resCode.equals("LYS") ||  resCode.equals("K") ) {
			return new String[] {"NZ" };
		}
		if( resCode.equals("ARG") || resCode.equals("R") ) {
			return new String[] {"NE","NH1", "NH2" };
		}
		if( resCode.equals("CYS") || resCode.equals("C")  ) {
			return new String[] {"SG" };
		}
		if( resCode.equals("MET") || resCode.equals("M")  ) {
			return new String[] {"SD" };
		}
		if( resCode.equals("MSE") ) {
			return new String[] {"SE" };
		}
		if( resCode.equals("PHE") || resCode.equals("F")) {
			return new String[] {"CG","CD1","CD2" ,"CE1" ,"CE2" ,"CZ"  };
		}
		if( resCode.equals("TYR") || resCode.equals("Y")) {
			return new String[] {"CG","CD1","CD2" ,"CE1" ,"CE2" ,"CZ","OH" };
		}
		if( resCode.equals("TRP") || resCode.equals("W")) {
			return new String[] {"CG","CD1","CD2" ,"NE1" ,"CE2" ,"CE3", "CZ2" ,"CZ3"  };
		}
		if( resCode.equals("HIS") || resCode.equals("H")) {
			return new String[] {"CG","ND1","CD2" ,"CE1" ,"NE2" };
		}
		if( resCode.equals("PRO") || resCode.equals("P")  ) {
			return new String[] {"CB","CG","CD" };
		}
		return null;
	}

	public boolean containsPDBRecord(String pdbId) throws SQLException {

		PreparedStatement st = con.prepareStatement("SELECT * FROM pdb_file WHERE pdb_file.pdb_id = ?");
		
		boolean results = false;

		try {
			st.setString(1, pdbId);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				return true;
			}
		} finally {
			st.close();
		}
		return false;
	}
	
}
