package db.search;

import java.util.List;

import sim.ProgressMeter;

/*
 *  StructExpPDBSearch
 *  
 *  Search the PDB using a regular expression style syntax that
 *  permits 3D connectors
 */
public class StructExpPDBSearch extends AbstractPDBSearch {

	//private StructExpQuery query;
	private DBQuery query;
	private String[] dbprops;
	
	public static void main(String[] args) {
		StructExpPDBSearch app = new StructExpPDBSearch();
		app.run(args);
	}
	
	ProgressMeter pm;
	
	public StructExpPDBSearch() {
		pm = new ProgressMeter(1.0, System.err);
	}
	
	public StructExpPDBSearch(ProgressMeter p) {
		pm = p;
	}
	public StructExpPDBSearch(ProgressMeter p, String[] dbs, boolean save) {
		pm = p;
		dbprops = dbs;
		saveSearch = save;
	}
	
	public StructExpPDBSearch( String[] dbs) {
		pm = new ProgressMeter(1.0, System.err);
		dbprops = dbs;
	}
	
	public void printHelp() {
		System.out.println();
		System.out.println(" Structural Pattern PDB Search:");
		System.out.println("     Query the PDB Database for structural patterns");
		System.out.println("     Using a pattern syntax that is an extension of PROSITE patterns ");
		System.out.println();
		System.out.println(" Usage:");
		System.out.println("     " + StructExpPDBSearch.class.getName() + " [OPTIONS] <PDB_STRUCT_PATTERN> ");
		System.out.println();
		System.out.println(" Options:");
		System.out.println("     --db=<db2|mysql> : Choose the DB (default MYSQL)");
		System.out.println("     --details : Print the PDB IDs and match positions (default false)");
		System.out.println("     --print   : Print the structural pattern data structure and exit");
		System.out.println("     --pdbid=<PDB_ID> : Search only the specified PDB structure");
		System.out.println("     --nounknown : Exclude structures annotated with UNKNOWN FUNCTION");
		System.out.println("     --unknown : Search only structures annotated with UNKNOWN FUNCTION");
		System.out.println("     --knownlist <LIST FILE>: Check correspondence to given list of pdb files");
		System.out.println();
		System.out.println(" Syntax:");
		System.out.println("   Each structural pattern is composed of a series of  ");
		System.out.println("   sequence (SEQ) and connection (CON) components,");
		System.out.println("   separated by a hyphens. E.g. SEQ1-SEQ2-CON1-SEQ3-CON2-SEQ4");
		System.out.println();
		System.out.println("   SEQ ELEMENTS SYNTAX");
		System.out.println("     x        :  Match any single residue");
		System.out.println("     A        :  (uppercase letter) A sspecific residue e.g. Alanine");
		System.out.println("     [AS]     :  A set of permissable residues");
		System.out.println("     s        :  (lowercase letter) Secondary structure, s-sheet h-helix, c-coil");
		System.out.println("     hA       :  Specific residue in secondary structure, e.g. Alanine in helix");
		System.out.println("     h[AS]    :  A set of permissable residues in a secondary structure");
		System.out.println();
		System.out.println("   CON ELEMENTS SYNTAX");
		System.out.println("     <_>           :  NULL connector, allow SEQ patterns to be anywhere");
		System.out.println("     <2,3>         :  Distance d in Angstroms between SEQ patterns, e.g. 2<d<3");
		System.out.println("     <C3,5N>       :  End to end distance d between SEQ patterns in angstroms");
		System.out.println("                      e.g. The C terminal end of the preceding SEQ is  3<d<5");
		System.out.println("                      from the N terminal end of the following SEQ pattern");
		System.out.println("     <^10,30>      :  Angle a in degrees between SEQ patterns, e.g. 10<a<30");
		System.out.println("     <3,5 ^10,30>  :  Combined connector with distance and angle thresholds");
		System.out.println();
		System.out.println(" Example: ");
		System.out.println("     " + StructExpPDBSearch.class.getName() +  " --unknown \"P-hP-h[KLR]-hA-<2,3>-sP-s-sP-sP-Q\"");
		System.out.println();
	}

	protected void parseArguments(String[] args) {
		if (args.length == 0) {
			printHelp();
			System.exit(0);
		}
		
		try {
			query = new StructExpQuery(args[args.length-1]);
		}
		catch(Exception e) {
			System.err.println("------------------------------------------------------------------------------------");
			System.err.println(" * ERROR: " + e.getMessage());
			System.err.println("------------------------------------------------------------------------------------");
			//e.printStackTrace();
			System.exit(0);
		}
		
		for(int i=0; i<args.length-1; i++) {
			if(args[i].equals("--print")) {
				query.printQueryData();
				System.exit(0);	
			} else if(args[i].substring(0, 7).equals("--pdbid")) {
				pdbid = args[i].substring(8).toUpperCase();
				query.setPdbID(pdbid);
			} else if(args[i].substring(0, 4).equals("--db")) {
				db = args[i].substring(5).toUpperCase();
			} else if(args[i].equals("--unknown") ) {
				searchProperties = "UNKNOWN";
				query.setSearchProperties(searchProperties);
			} else if(args[i].equals("--nounknown") ) {
				searchProperties = "KNOWN";
				query.setSearchProperties(searchProperties);
			} else if(args[i].equals("--details") ) {
				details = true;
			} else if(args[i].equals("--knownlist") ) {
				i++;
				KnownDataList = args[i];
				loadKnownPositives();
			} else {
				System.out.println("Unrecognised program option: " + args[i]);
				printHelp();
				System.exit(0);
			}
		}
	}
	
	/*
	protected void prepareQueryExecutor() {
		queryExecutor = new StructExpQueryExecutor(db);
	}*/

	protected void search() {
		if(validQuery) {
			queryExecutor = new StructExpQueryExecutor(dbprops);
			finalSet = queryExecutor.executeQuery(query, pm);
			System.err.println("QUERY EXECUTED , Saved Search is " + saveSearch + "<BR>");
			System.err.println("Number of elements " + finalSet.size() + "<BR>");
			if(saveSearch) {
				queryId = queryExecutor.saveSearch(query, finalSet);
			}
		} else {
			pm.finaliseMeter();
		}
	}


	public void prepareQuery(String userid, String pdbid, String scopid, String keywords, String redundancy, String annot, String resOp, String resolution,  String tech,  String regex,  String interactors,  boolean multihits,  boolean pseudoAtoms) throws InvalidQueryException {

		if(regex=="") {
				query = new DBQuery();
		} else {
				query = new StructExpQuery(regex, userid);
		}
		/*
		try {}
		catch(Exception e) {
			System.out.println("Error parsing query string: " + e);
			validQuery = false;
			printHelp();
			System.exit(0);
		}*/
		
		if(!pdbid.equals("")) {
			query.setPdbID(pdbid);
		}
		if(!scopid.equals("")) {
			query.setScopID(scopid);
		}
		if(!keywords.equals("")) {
			query.setKeywords(keywords);
		}
		if(!redundancy.equals("")) {
			query.setRedundancy(redundancy);
		}
		if(!annot.equals("ALL")) {
			query.setSearchProperties(annot);
		}
		if(!resOp.equals("")) {
			query.setResolutionOperator(resOp);
		}
		if(!resolution.equals("")) {
			query.setResolution(resolution);
		}
		if(!tech.equals("ALL")) {
			query.setTechnology(tech);
		}
		if(!interactors.equals("")) {
			query.setInteractors(interactors);
		}

		query.setMultipleHitsPerStructure(multihits);
		query.setusePseudoAtoms(pseudoAtoms);

        System.err.println("PSEUDO POINTS:  " + query.usePseudoAtoms() );
	}

	public List<MatchingComplex> getSavedSearchResults(int queryid) {
		DBQueryExecutor queryExecutor = new StructExpQueryExecutor(dbprops);
		return queryExecutor.getSavedSearchResults(queryid);
	}
}
