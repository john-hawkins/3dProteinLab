package db.db2;

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

import db.PdbDAO;
import db.StatementExecutor;
import db.search.DBQuery;
import db.search.MatchingComplex;
import db.search.MatchingStructure;
import db.search.PatternMatchSet;
import db.search.QueryResult;
import db.search.StructExpQuery;


public class XmlRPdbDAO implements PdbDAO {
	
	private String SQL_All = "SELECT * FROM xmlrpdb.entity_poly";
	private String SQL_AllKnown = "SELECT * FROM xmlrpdb.entity_poly WHERE pdb_id NOT IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_AllUnknown = "SELECT * FROM xmlrpdb.entity_poly WHERE pdb_id IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	private String SQL_SeqMatchAll = "SELECT * FROM xmlrpdb.entity_poly WHERE pdbx_seq_one_letter_code_can LIKE ?";
	private String SQL_SeqMatchAllKnown = "SELECT * FROM xmlrpdb.entity_poly WHERE pdbx_seq_one_letter_code_can LIKE ? AND pdb_id NOT IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_SeqMatchAllUnknown = "SELECT * FROM xmlrpdb.entity_poly WHERE pdbx_seq_one_letter_code_can LIKE ? AND pdb_id IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	private String SQL_SeqMatchStart = "SELECT * FROM xmlrpdb.entity_poly WHERE (";
	private String SQL_SeqMatchMiddle = " pdbx_seq_one_letter_code_can LIKE ?";
	private String SQL_SeqMatchEnd = " )";
	private String SQL_SeqMatchEndKnown = " ) AND pdb_id NOT IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_SeqMatchEndUnknown = " ) AND pdb_id IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	
	private String SQL_RestrictByKnown = " pdb_id NOT IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	private String SQL_RestrictByUnknown = " pdb_id IN (SELECT pdb_id FROM xmlrpdb.struct_keywords WHERE upper(pdbx_keywords) LIKE '%UNKNOWN FUNCTION%')";
	
	private Connection con;
	
	public XmlRPdbDAO(Connection con) {
		this.con = con;
	}
	
	/* getEntitiesCount()
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getEntitiesCount()
	 */
	public int getEntitiesCount() throws SQLException {
		PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM xmlrpdb.entity");
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
		PreparedStatement st = con.prepareStatement("select * from xmlrpdb.entity where pdb_id = ?");
		try {
			st.setString(1, pdbId);
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<Entity>() {
				public Entity createObject(ResultSet rs) throws SQLException {
					return new Entity(rs.getString("PDB_ID"), rs.getInt("ENTITY_ID"), rs.getString("TYPE"), rs.getString("DESCRIPTION"));
				}
			});
		}
		finally {
			st.close();
		}
	}
	
	/* getPolyEntities(String pdbId)
	 * 
	 * Get all polymer entities in the given PDB Entry
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntities(java.lang.String)
	 */
	public List<Protein> getProteins(String pdbId) throws SQLException {

		PreparedStatement st = con.prepareStatement("SELECT * FROM xmlrpdb.entity_poly WHERE pdb_id = ?");
		
		List<Protein> results = new LinkedList<Protein>();

		try {
			st.setString(1, pdbId);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {

				Protein e = new Protein(
						rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("TYPE"), 
						rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), 
						"", 
						"" 
						);
				this.setProteinDisBonds(e);
				this.setProteinSSE(e);
				results.add( e );
			}
		} finally {
			st.close();
		}
		
		return results;
	}
	
	
	/*
	 * Get all Proteins with a set of general requirements
	 */
	public List<Protein> getProteins(DBQuery query) throws SQLException {

		String queryString = "SELECT * FROM xmlrpdb.entity_poly WHERE pdb_id IN (SELECT pdb_id FROM pdb_file WHERE ";
		int params = 0;
		if(query.getKeywords()!=null) {
			queryString = queryString + "upper(keywords) LIKE ? ";
			params++;
		}
				
		if(query.getResolution()!=null) {
			if(params>0)
				queryString = queryString + " AND ";
			queryString = queryString + " resolution > ? ";
			params++;
		}
		if(query.getTech()!=null) {
			if(params>0)
				queryString = queryString + " AND ";
			queryString = queryString + " tech = ? ";
			params++;
		}
		if(query.getPdbID()!=null) {
			if(params>0)
				queryString = queryString + " AND ";
			queryString = queryString + " pdb_id = ? ";
			params++;
		}
		
		queryString = queryString + ")";
		
		if(params==0) { // Change the query
			queryString = "SELECT * FROM pdb_protein";
		}
		
		if( query.getFunctionKnown() != "" ) {
			if(params==0) {
				queryString = queryString + " WHERE ";
			} else {
				queryString = queryString + " AND ";
			}
			if(query.getFunctionKnown().equals("KNOWN")) {
				queryString = queryString +  this.SQL_RestrictByKnown;
			} else if(query.getFunctionKnown().equals("UNKNOWN")) {
				queryString = queryString +  this.SQL_RestrictByUnknown;
			} 
		}
			

		PreparedStatement st = con.prepareStatement(queryString);
		List<Protein> results = new LinkedList<Protein>();

		try {
			int paramIndex = 1;
			if(query.getKeywords()!=null) {
				st.setString(paramIndex, query.getKeywords());
				paramIndex++;
			}
			if(query.getResolution()!=null) {
				st.setDouble(paramIndex, query.getResolutionAsDouble() );
				paramIndex++;
			}
			if(query.getTech()!=null) {
				st.setString(paramIndex, query.getTech());
				paramIndex++;
			}
			if(query.getPdbID()!=null) {
				st.setString(paramIndex, query.getPdbID());
				paramIndex++;
			}
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {

				Protein e = new Protein(rs.getString("PDB_ID"), 
							rs.getInt("ENTITY_ID"), 
							rs.getString("TYPE"),  
							rs.getString("SEQ"),
							rs.getString("SSES"),
							rs.getString("SEQ_PLUS_SSE"),
							rs.getString("UNIPROT_AC") ,
							rs.getString("UNIPROT_CODE"));
				
				results.add( e );
			}
		} finally {
			st.close();
		}
		
		return results;	
	}
	
	
	/* getPolyEntitiesLike
	 * 
	 * This query will return a set of PolyEntities for which the
	 * sequence matches a given pattern.
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntitiesLike(java.lang.String, java.lang.String)

	public List<Protein> getProteinsLike(String query, String props) throws SQLException {
		
		PreparedStatement st = con.prepareStatement(this.SQL_SeqMatchAll);
		if(props.equals("KNOWN")) {
			st =  con.prepareStatement(this.SQL_SeqMatchAllKnown);
		} else if(props.equals("UNKNOWN")) {
			st =  con.prepareStatement(this.SQL_SeqMatchAllUnknown);
		}
		
		try {
			st.setString(1, query);
			
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<Protein>() {
				public Protein createObject(ResultSet rs) throws SQLException {
					return new Protein(
							rs.getString("PDB_ID"), 
							rs.getInt("ENTITY_ID"), 
							rs.getString("TYPE"), 
							rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), 
							"", 
							"" 
							);
				}
			});
		}
		finally {
			st.close();
		}
	}
		 */
	
	/* getAtomSite 
	 * 
	 * This query will return a set of atom sites for a particular atom 
	 * within a entity subcomponent of the specified structure. 
	 * Each of the instances of this entity in the structure will have
	 * a separate entry in the results.
	 * You can use all of them, or just the first.
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getAtomSite(java.lang.String, int, int, java.lang.String)
	 */
	public List<AtomSite> getAtomSite(String pdbID, int entityID, int residueNum, String atomID) throws SQLException {
		
		String theQuery = "select * from xmlrpdb.atom_site where " +
	    				"pdb_id = ? and label_entity_id= ? and " +
	    				"label_seq_id= ? and label_atom_id = ? " +
	    				"and MY_PDBX_PDB_MODEL_NUM=0 " + 
	    				"order by label_asym_id";

		PreparedStatement st = con.prepareStatement(theQuery);
		

		
		try {
			st.setString(1, pdbID);
			st.setInt(2,  entityID);
			st.setInt(3, residueNum);
			st.setString(4, atomID);
			
			//System.out.println("RUNNING QUERY");
			//System.out.println(theQuery + " : " + pdbID + " : " + entityID + " : " + residueNum + " : " + atomID);
			
			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<AtomSite>() {
				public AtomSite createObject(ResultSet rs) throws SQLException {
					return new AtomSite(rs.getString("PDB_ID"), 
										rs.getString("ATOM_SITE_ID"), 
										rs.getString("A_AA"), 
										rs.getDouble("CARTN_X"), 
										rs.getDouble("CARTN_Y"), 
										rs.getDouble("CARTN_Z"), 
										rs.getString("LABEL_ASYM_ID"), 
										rs.getString("LABEL_ATOM_ID"), 
										rs.getInt("LABEL_ENTITY_ID"), 
										rs.getInt("LABEL_SEQ_ID"), 
										rs.getInt("MY_PDBX_PDB_MODEL_NUM"), 
										rs.getString("TYPE_SYMBOL"),
										rs.getString("AUTH_ASYM_ID"), 
										rs.getString("AUTH_SEQ_ID"), 
										rs.getString("PDBX_PDB_INS_CODE")
							);
				}
			});
		}
		finally {
			st.close();
		}
	}
	

	/* getPolyEntsStructPatMatch 
	 * 
	 * This query will return a set of Polymer Entities that match the basic
	 * criteria of the structural query
	 * That is they contain each of the sequence components of the expression.
	 * 	 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntsStructPatMatch(db.search.QueryString, java.lang.String)
	 */
	public List<Protein> getProteinStructPatMatch( StructExpQuery q) throws SQLException {
		
		String props = q.getFunctionKnown();
		
		String myStatement = this.SQL_SeqMatchStart;
		String[] queryParts = q.getQueryStringsForSQL();
		for(int i=0; i<queryParts.length; i++) {
			if(i>0) myStatement = myStatement + " AND ";
			myStatement = myStatement +  this.SQL_SeqMatchMiddle;
		}
		
		if(props.equals("KNOWN")) {
			myStatement = myStatement +  this.SQL_SeqMatchEndKnown;
			//st =  con.prepareStatement(this.SQL_SeqMatchAllKnown);
		} else if(props.equals("UNKNOWN")) {
			myStatement = myStatement +  this.SQL_SeqMatchEndUnknown;
			//st =  con.prepareStatement(this.SQL_SeqMatchAllUnknown);
		} else {
			myStatement = myStatement +  this.SQL_SeqMatchEnd;
		}
		
		System.out.println("Query: " + myStatement );
		
		PreparedStatement st = con.prepareStatement( myStatement );
		
		List<Protein> results = new LinkedList<Protein>();
		
		int NumTested = 0;
		try {
			for(int i=0; i<queryParts.length; i++) {
				st.setString(i+1, "%" + queryParts[i] + "%");
				System.out.println(" Filled " + "%" + queryParts[i] + "%");
			}

			ResultSet rs = st.executeQuery();
			String seq;
			PatternMatchSet ms = new PatternMatchSet( q.getRegExPatternSet() );
			
			while(rs.next()) {
				seq = rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN");
				
				ms.matchSequence(seq);
				
				if(ms.getNumOfValidMatchSets() > 0 ) {
					Protein e = new Protein(
							rs.getString("PDB_ID"), 
							rs.getInt("ENTITY_ID"),
							rs.getString("TYPE"), 
							rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), 
							"", 
							"" );
					this.setProteinDisBonds(e);
					this.setProteinSSE(e);
					results.add( e );
				}
				NumTested++;
			}
			
		} finally {
			st.close();
		}
		
		return results;
	}
	
	
	/* setProteinSSE 
	 * 
	 * Retrieve the SSE string and set it for the specified Entity
	 */
	public void setProteinSSE( Protein e ) throws SQLException {
		e.setSSE(this.getSSEString(e.getPdbId(), e.getEntityId(), e.getSEQ().length()));	
	}

	/* setProteinDisBond 
	 * 
	 * Retrieve the SSE string and set it for the specified Entity
	 */
	public void setProteinDisBonds( Protein e ) throws SQLException {
		e.setDisBonds( this.getPDBDisBonds(e.getPdbId()) );	
	}
	/*
	 * 
	 */
	private void setProteinResolvedSeq(Protein e) {
		//e.setResolved( this.getResolvedSeq(e) );	
		e.setResolved( "" );	
	}
	
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

	private boolean positionResolved(String pdbID, String chainId, int pos) {

		boolean result =false;
		String q = "SELECT DISTINCT auth_seq_id from xmlrpdb.atom_site WHERE pdb_id = ? " +
		"AND label_asym_id = ? AND label_seq_id = ?";
		
		PreparedStatement st;
		try {
			st = con.prepareStatement(q);

			try {
				st.setString(1, pdbID);
				st.setString(2, chainId);
				st.setInt(2, pos);
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

	/* getPolyEntitySSE
	 * 
	 * Retrieve the SSE data for a specific chain within a PDB file
	 * Then convert it into a string with an entry for each residue 
	 * in the target polymer
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPolyEntitySSE(java.lang.String, int, int)
	 */
	public String getSSEString( String pdbid, int entid, int len) throws SQLException {
		
		String q = "SELECT * FROM xmlrpdb.dssp_sse WHERE pdb_id=? and entity_id=? ORDER BY label_asym_id";
		
		PreparedStatement st = con.prepareStatement( q );

		char[] sse = new char[len];
		for(int l=0; l<len; l++) {
			sse[l] =  'C';
		}
		
		int NumTested = 0;
		try {
			st.setString(1, pdbid);
			st.setInt(2, entid);
			
			//System.out.print("HERE: " + pdbid + " : " + entid + "\n");
			ResultSet rs = st.executeQuery();
			//System.out.print("DONE: " + pdbid + " : " + entid + "\n");
			
			char ref_asym_id= ' ', asym_id = ' ';
			
			while(rs.next()) {
			    
				asym_id = rs.getString("LABEL_ASYM_ID").charAt(0);

				if(NumTested==0)
					ref_asym_id = asym_id;
				
				//System.out.print(NumTested + " REF: " + ref_asym_id + " CUR: " + asym_id + "\n");
				
				if(ref_asym_id != asym_id)
					break;
				
				int start = rs.getInt("SSE_LABEL_SEQ_ID_START");
				int end = rs.getInt("SSE_LABEL_SEQ_ID_END");
				String ident = rs.getString("SSE_GROUP");
				//System.out.print(ident + " : " + start + " : " + end + "\n");
				for(int l=start; l<end; l++) {
					sse[l] = ident.charAt(0);
				}
				
				NumTested++;
			}

			//System.out.println("Tested: " + NumTested );
		} catch(Exception e) {
			System.out.print("PROBLEM : " + e);
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
					seq = rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN");
					
					Matcher matcher = regExPattern.matcher(seq);
					
					if( matcher.find() ) {
						Protein e = new Protein(
								rs.getString("PDB_ID"), 
								rs.getInt("ENTITY_ID"), 
								rs.getString("TYPE"), 
								rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), 
								"", 
								"" 
								);			
						this.setProteinDisBonds(e);
						this.setProteinSSE(e);
						results.add( e );
					}
					NumTested++;
				}

				System.out.println("Tested: " + NumTested );
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
					seq = rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN");
					
					Matcher matcher = regExPattern.matcher(seq);
					
					if( matcher.find() ) {
						/*
						EntityPoly e = new EntityPoly(rs.getString("PDB_ID"), 
								rs.getInt("ENTITY_ID"), 
								rs.getString("TYPE"), 
								rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), "", "" );
						this.setPolyEntitySSE(e);
						*/
						int[][] match = new int[1][2];
						match[0][0] = matcher.start();
						match[0][1] = matcher.end();
						results.add( new MatchingStructure(rs.getString("PDB_ID"),rs.getInt("ENTITY_ID"), "", match) );
					}
					NumTested++;
				}

				System.out.println("Tested: " + NumTested );
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
		String q = "SELECT a.pdb_id, b.pdbx_keywords, b.text, a.pdb_xml_file FROM xmlrpdb.pdb_xml a, xmlrpdb.struct_keywords b WHERE a.pdb_id=b.pdb_id and a.pdb_id LIKE '3E1A'"; // 
		PreparedStatement st = con.prepareStatement(q);
		try {

			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<PDBFile>() {
				public PDBFile createObject(ResultSet rs) throws SQLException {
					String xml = rs.getString("PDB_XML_FILE");
					int end = xml.indexOf("</PDBx:date_original>");
					String addedDate = "1970-01-01";
					if(end > 0) {
						addedDate = xml.substring(end-10, end);
					}
					end = xml.indexOf("</PDBx:ls_d_res_high>");
					double res = 0.0;
					if(end > 0) {
						int start =  xml.indexOf("<PDBx:ls_d_res_high>");
						res = Double.parseDouble(xml.substring(start+20, end));
					}			
					
					String title = "";
					end = xml.indexOf("</PDBx:title>");
					if(end > 0) {
						int start =  xml.indexOf("<PDBx:title>");
						title = xml.substring(start+12, end);
					}
					
					String tech = "MODEL";
					if(xml.indexOf("X-RAY DIFFRACTION") > 0) {
						tech = "X-RAY DIFFRACTION";
					} else if(xml.indexOf("SOLUTION NMR") > 0) {
						tech = "SOLUTION NMR";
					} else if(xml.indexOf("ELECTRON MICROSCOPY") > 0) {
						tech = "ELECTRON MICROSCOPY";	
					}
			
					return new PDBFile(rs.getString("PDB_ID"), title, rs.getString("PDBX_KEYWORDS"),  rs.getString("TEXT"), res, tech, addedDate, addedDate);
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
		String q = "SELECT a.pdb_id, b.pdbx_keywords, b.text, a.pdb_xml_file FROM xmlrpdb.pdb_xml a, xmlrpdb.struct_keywords b WHERE a.pdb_id=b.pdb_id and a.pdb_id='" + pdbID + "'"; // 
		PreparedStatement st = con.prepareStatement(q);
		try {

			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<PDBFile>() {
				public PDBFile createObject(ResultSet rs) throws SQLException {
					String xml = rs.getString("PDB_XML_FILE");
					int end = xml.indexOf("</PDBx:date_original>");
					String addedDate = "1970-01-01";
					if(end > 0) {
						addedDate = xml.substring(end-10, end);
					}
					end = xml.indexOf("</PDBx:ls_d_res_high>");
					double res = 0.0;
					if(end > 0) {
						int start =  xml.indexOf("<PDBx:ls_d_res_high>");
						res = Double.parseDouble(xml.substring(start+20, end));
					}
					String title = "";
					end = xml.indexOf("</PDBx:title>");
					if(end > 0) {
						int start =  xml.indexOf("<PDBx:title>");
						title = xml.substring(start+12, end);
					}
					
					String tech = "MODEL";
					if(xml.indexOf("X-RAY DIFFRACTION") > 0) {
						tech = "X-RAY DIFFRACTION";
					} else if(xml.indexOf("SOLUTION NMR") > 0) {
						tech = "SOLUTION NMR";
					} else if(xml.indexOf("ELECTRON MICROSCOPY") > 0) {
						tech = "ELECTRON MICROSCOPY";	// eg 1D3E
					}else if(xml.indexOf("ELECTRON CRYSTALLOGRAPHY") > 0) {
						tech = "ELECTRON CRYSTALLOGRAPHY";	// eg 1BRD
					}
					 
					return new PDBFile(rs.getString("PDB_ID"), title, rs.getString("PDBX_KEYWORDS"), rs.getString("TEXT"), res, tech, addedDate, addedDate);
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
		try{
		// String q = "SELECT a.pdb_id, a.type, a.entity_id, a.PDBX_SEQ_ONE_LETTER_CODE_CAN, " +
		//	"b.PDBX_DB_ACCESSION, b.DB_NAME FROM xmlrpdb.entity_poly a, xmlrpdb.struct_ref b " +
		//	"WHERE a.type LIKE 'polypeptide%' AND a.pdb_id=b.pdb_id AND " +
		//	"CAST(a.entity_id AS CHAR(10))=b.entity_id AND a.pdb_id='" + pdbID + "'";
		// System.out.println("PDB ID : " + pdbID);
		
		String q1 = "SELECT DISTINCT a.pdb_id, a.type, a.entity_id, a.PDBX_SEQ_ONE_LETTER_CODE_CAN, b.label_asym_id, b.auth_asym_id " +
		"FROM xmlrpdb.entity_poly a, xmlrpdb.atom_site b " +
		"WHERE a.entity_id=b.label_entity_id AND a.pdb_id=b.pdb_id " +
		"AND a.type LIKE 'polypeptide%' AND a.pdb_id='" + pdbID + "'";
		
		//System.err.println("QUERY 1 : " + q1);
		
		PreparedStatement st = con.prepareStatement(q1);
		
		List<Protein> results = new LinkedList<Protein>();
		try {
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				
				String entID = String.valueOf(rs.getInt("ENTITY_ID"));
				String q2 = "SELECT * FROM xmlrpdb.struct_ref b " +
				"WHERE b.entity_id='" + entID + "' AND b.pdb_id='" + pdbID + "'";
				
				//System.err.println("QUERY 2 : " + q2);
				
				String uniprot = "";
				String uniprot_name = "";
				
				PreparedStatement st2 = con.prepareStatement(q2);
				ResultSet rs2 = st2.executeQuery();
				while(rs2.next()) {
					if(rs2.getString("DB_NAME").trim().equals("UNP")) {
						uniprot = rs2.getString("PDBX_DB_ACCESSION");
						if(uniprot==null) uniprot = "";
						uniprot_name = rs2.getString("DB_CODE");
						if(uniprot_name==null) uniprot_name = "";
					}
				}
					
				//System.err.println("ASYM_ID : " + rs.getString("LABEL_ASYM_ID") );
				
				Protein e = new Protein(
					rs.getString("PDB_ID"), 
					rs.getInt("ENTITY_ID"), 
					rs.getString("LABEL_ASYM_ID").trim(), 
					rs.getString("AUTH_ASYM_ID").trim(), 
					rs.getString("TYPE"), 
					rs.getString("PDBX_SEQ_ONE_LETTER_CODE_CAN"), 
					uniprot, 
					uniprot_name );
				this.setProteinDisBonds(e);
				this.setProteinSSE(e);
				this.setProteinResolvedSeq(e); // LETS DO IT LATER
				results.add( e );
				
				st2.close();
			}
		} finally {
			st.close();
		}
		
		return results;
		
		} catch( SQLException e) {
			System.err.println("Problem with PDB Entry: " +pdbID);
			throw e;
		}
	}
	


	public List<SSE> getPDBFileProteinSSEs(String pdbID) throws SQLException{
		return getPDBFileProteinSSEs(pdbID, null);
	}
	
	/* getPDBFileProteinSSEs(String pdbID)
	 * 
	 * Method to extract objects for each secondary structure element
	 * within a given PDB file

	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinSSEs(java.lang.String)
	 */
	public List<SSE> getPDBFileProteinSSEs(String pdbID, Integer entID) throws SQLException{
		String q = "SELECT * from xmlrpdb.dssp_sse WHERE pdb_id = ? " +
				"AND entity_id = ? ";
		
		if(entID==null) {
			q = "SELECT * from xmlrpdb.dssp_sse WHERE pdb_id = ? " +
			"AND entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly " +
			"WHERE pdb_id = ? AND type LIKE 'polypeptide%')";
		}

		//EG
		//"SELECT * from xmlrpdb.dssp_sse WHERE pdb_id='1BB0' AND label_entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly WHERE pdb_id ='1BB0' AND type LIKE 'polypeptide%')";
	
		PreparedStatement st = con.prepareStatement(q);
		
		List<SSE> results = new LinkedList<SSE>();
		try {
			st.setString(1, pdbID);
			
			if(entID==null) {
				st.setString(2, pdbID);
			} else {
				st.setInt(2, entID);
			}
			ResultSet rs = st.executeQuery();
	
			while(rs.next()) {	
				SSE e = new SSE(rs.getString("PDB_ID"), 
						rs.getInt("ENTITY_ID"), 
						rs.getString("LABEL_ASYM_ID"), 
						rs.getString("SSE_TYPE"),
						rs.getString("SSE_GROUP"),
						rs.getInt("SSE_LABEL_SEQ_ID_START"), 
						rs.getInt("SSE_LABEL_SEQ_ID_END"));
				results.add( e );
			}
		} finally {
			st.close();
		}
		return results;
	}
	
	/*
	 * getPDBDisBonds
	 * 
	 * Get dis bonds for a PDB
	 */
	public List<DisBond> getPDBDisBonds(String pdbID) throws SQLException {
		String q = "SELECT * from xmlrpdb.dis_bonds WHERE pdb_id = ?";

		PreparedStatement st = con.prepareStatement(q);

		List<DisBond> results = new LinkedList<DisBond>();
		try {
			st.setString(1, pdbID);

			ResultSet rs = st.executeQuery();

			while(rs.next()) {	
				DisBond e = new DisBond(rs.getString("PDB_ID"), 
						rs.getString("LABEL_ASYM_ID1"), 
						rs.getInt("LABEL_SEQ_ID1"), 
						rs.getString("LABEL_ASYM_ID2"), 
						rs.getInt("LABEL_SEQ_ID2"));
				results.add( e );
			}
		} finally {
			st.close();
		}
		return results;
	}
	

	
	/* --------------------------------------------------------
	 * EXTRA STATEMENTS - NOT PART OF THE STANDARD INTERFACE
	 * --------------------------------------------------------
	 */
	
	
	/* getPDBFileProteinAlphaCs
	 * 
	 * Get a list of the alpha carbons for a given PDB
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinAlphaCs(java.lang.String)
	 */
	public List<AtomSite> getPDBFileProteinAlphaCs(String pdbID) throws SQLException{
		String q = "SELECT * from xmlrpdb.atom_site WHERE pdb_id = ? " +
				"AND MY_PDBX_PDB_MODEL_NUM=0 AND label_atom_id = 'CA' " + 
				"AND label_entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly " +
				"WHERE pdb_id = ? AND type LIKE 'polypeptide%')";

		//"SELECT * from xmlrpdb.atom_site WHERE pdb_id = ? AND MY_PDBX_PDB_MODEL_NUM=0 AND label_atom_id = 'CA' AND label_entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly WHERE pdb_id = ? AND type LIKE 'polypeptide%')"
			
		PreparedStatement st = con.prepareStatement(q);
		
		List<AtomSite> results = new LinkedList<AtomSite>();
		try {
			st.setString(1, pdbID);
			st.setString(2, pdbID);
			ResultSet rs = st.executeQuery();
			
			// NOTE: SOME OF THE ATOM SITES HAVE  NULL VALUES IN THEIR A_AA FIELD
			// EVEN THOUGH THE SEQUENCE DATA IS PRESENT IN ENTITY_POLY
			
			while(rs.next()) {	
				//System.out.print( rs.getString("PDB_ID") + ":" +rs.getInt("LABEL_ENTITY_ID") +" (" + rs.getInt("LABEL_SEQ_ID") + ") " + rs.getString("A_AA") ) ;
				AtomSite e = new AtomSite(rs.getString("PDB_ID"), 
						rs.getString("ATOM_SITE_ID"), 
						rs.getString("A_AA"), 
						rs.getDouble("CARTN_X"), 
						rs.getDouble("CARTN_Y"), 
						rs.getDouble("CARTN_Z"), 
						rs.getString("LABEL_ASYM_ID"), 
						rs.getString("LABEL_ATOM_ID"), 
						rs.getInt("LABEL_ENTITY_ID"), 
						rs.getInt("LABEL_SEQ_ID"), 
						rs.getInt("MY_PDBX_PDB_MODEL_NUM"), 
						rs.getString("TYPE_SYMBOL"),
						rs.getString("AUTH_ASYM_ID"), 
						rs.getString("AUTH_SEQ_ID"), 
						rs.getString("PDBX_PDB_INS_CODE"));
				results.add( e );
				//System.out.println(" In: " + e.getPDB_ID()+ ":" + e.getENTITY_ID() + " (" + e.getLABEL_SEQ_ID() + ") " + e.getA_AA());
			}
		} finally {
			st.close();
		}
		return results;
	}
	
	/* getPDBFileProteinBackbone
	 * 
	 * Get a list of Backbone atoms for a given PDB
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinBackbone(java.lang.String)
	 */
	public List<AtomSite> getPDBFileProteinBackbone(String pdbID) throws SQLException{
		String q = "SELECT * from xmlrpdb.atom_site WHERE pdb_id = ? " +
				"AND MY_PDBX_PDB_MODEL_NUM=0 AND my_label_asym_type = 'BB' " + 
				"AND label_entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly " +
				"WHERE pdb_id = ? AND type LIKE 'polypeptide%')";
	
		PreparedStatement st = con.prepareStatement(q);
		
		List<AtomSite> results = new LinkedList<AtomSite>();
		try {
			st.setString(1, pdbID);
			st.setString(2, pdbID);
			ResultSet rs = st.executeQuery();
			
			// NOTE: SOMNE OF THE OF THE ATOM SITES HAVE  NULL VALUES IN THEIR A_AA FIELD
			// EVEN THOUGH THE SEQUENCE DATA IS PRESENT IN ENTITY_POLY
			
			while(rs.next()) {	
				//System.out.print( rs.getString("PDB_ID") + ":" +rs.getInt("LABEL_ENTITY_ID") +" (" + rs.getInt("LABEL_SEQ_ID") + ") " + rs.getString("A_AA") ) ;
				AtomSite e = new AtomSite(rs.getString("PDB_ID"), 
						rs.getString("ATOM_SITE_ID"), 
						rs.getString("A_AA"), 
						rs.getDouble("CARTN_X"), 
						rs.getDouble("CARTN_Y"), 
						rs.getDouble("CARTN_Z"), 
						rs.getString("LABEL_ASYM_ID"), 
						rs.getString("LABEL_ATOM_ID"), 
						rs.getInt("LABEL_ENTITY_ID"), 
						rs.getInt("LABEL_SEQ_ID"), 
						rs.getInt("MY_PDBX_PDB_MODEL_NUM"), 
						rs.getString("TYPE_SYMBOL"), 
						rs.getString("AUTH_ASYM_ID"), 
						rs.getString("AUTH_SEQ_ID"), 
						rs.getString("PDBX_PDB_INS_CODE"));
				results.add( e );
				//System.out.println(" In: " + e.getPDB_ID()+ ":" + e.getENTITY_ID() + " (" + e.getLABEL_SEQ_ID() + ") " + e.getA_AA());
			}
		} finally {
			st.close();
		}
		return results;
	}
	

	/* getPDBFileProteinBackbone
	 * 
	 * Get a list of Backbone atoms for a given PDB
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileProteinBackbone(java.lang.String)
	 */
	public List<AtomSite> getPDBFileProteinNonBackbone(String pdbID) throws SQLException{
		String q = "SELECT * from xmlrpdb.atom_site WHERE pdb_id = ? " +
				"AND MY_PDBX_PDB_MODEL_NUM=0 AND my_label_asym_type <> 'BB' " + 
				"AND label_entity_id IN (SELECT entity_id FROM xmlrpdb.entity_poly " +
				"WHERE pdb_id = ? AND type LIKE 'polypeptide%')";
	
		PreparedStatement st = con.prepareStatement(q);
		
		List<AtomSite> results = new LinkedList<AtomSite>();
		try {
			st.setString(1, pdbID);
			st.setString(2, pdbID);
			ResultSet rs = st.executeQuery();
			
			// NOTE: SOMNE OF THE OF THE ATOM SITES HAVE  NULL VALUES IN THEIR A_AA FIELD
			// EVEN THOUGH THE SEQUENCE DATA IS PRESENT IN ENTITY_POLY
			
			while(rs.next()) {	
				//System.out.print( rs.getString("PDB_ID") + ":" +rs.getInt("LABEL_ENTITY_ID") +" (" + rs.getInt("LABEL_SEQ_ID") + ") " + rs.getString("A_AA") ) ;
				AtomSite e = new AtomSite(rs.getString("PDB_ID"), 
						rs.getString("ATOM_SITE_ID"), 
						rs.getString("A_AA"), 
						rs.getDouble("CARTN_X"), 
						rs.getDouble("CARTN_Y"), 
						rs.getDouble("CARTN_Z"), 
						rs.getString("LABEL_ASYM_ID"), 
						rs.getString("LABEL_ATOM_ID"), 
						rs.getInt("LABEL_ENTITY_ID"), 
						rs.getInt("LABEL_SEQ_ID"), 
						rs.getInt("MY_PDBX_PDB_MODEL_NUM"), 
						rs.getString("TYPE_SYMBOL"), 
						rs.getString("AUTH_ASYM_ID"), 
						rs.getString("AUTH_SEQ_ID"), 
						rs.getString("PDBX_PDB_INS_CODE"));
				results.add( e );
				//System.out.println(" In: " + e.getPDB_ID()+ ":" + e.getENTITY_ID() + " (" + e.getLABEL_SEQ_ID() + ") " + e.getA_AA());
			}
		} finally {
			st.close();
		}
		return results;
	}
	
	/*
	 * getPDBFileNames
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPDBFileNames()
	 */
	public List<String> getPDBFileNames() throws SQLException{
		String q = "SELECT pdb_id FROM xmlrpdb.pdb_xml "; // a WHERE a.pdb_id LIKE '1BB%' OR WHERE a.pdb_id LIKE '2IWI' 
		PreparedStatement st = con.prepareStatement(q);
		try {

			return StatementExecutor.executeQuery(st, new StatementExecutor.RowConverter<String>() {
				public String createObject(ResultSet rs) throws SQLException {
					return rs.getString("PDB_ID");
				}
			});
		}
		finally {
			st.close();
		}
	}


	/*
	 * getPdbID4UniprotID
	 * 
	 * @see db.xmlrpdb.db2.PdbDAO#getPdbID4UniprotID()
	 */
	public List<String> getPdbID4UniprotID(String in) throws SQLException {
		/*
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
		}*/
		List<String> q = new LinkedList<String>();
		q.add(in);
		return q;
	}

	public void fillSCOP4Match(MatchingStructure ma) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void fillPfam4Match(MatchingStructure ma) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void calcAuthPositions(MatchingStructure ma) throws SQLException {
		// TODO Auto-generated method stub
		
	}


	public List<QueryResult> getQueryList() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void fillCath4Match(MatchingStructure ma) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void calcAuthPositions(MatchingComplex maco) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void fillCath4Match(MatchingComplex maco) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void fillPfam4Match(MatchingComplex maco) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void fillSCOP4Match(MatchingComplex maco) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public List<AtomSite> getAtomSite(String pdbId, String chainId,
			int residueNum, String atomID) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ProteinComplex> getProteinComplex(String pdbID)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ProteinComplex> getProteinComplexStructPatMatch(
			StructExpQuery query) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ProteinComplex> getProteinComplexes(DBQuery query)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int saveQuery(DBQuery query, List<MatchingComplex> finalSet) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<MatchingComplex> retrieveSavedSearch(DBQuery query)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MatchingComplex> retrieveSavedSearch(int queryId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<AtomSite> getPseudoAtom(String pdbId, String chainId,
			int residueNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	
}
