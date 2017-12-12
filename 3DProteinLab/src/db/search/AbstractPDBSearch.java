package db.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import java.util.regex.Pattern;
/*
import structools.structure.AtomSite;
import structools.structure.Protein;

import db.DatabaseConnectionProperties;
import db.DatabaseConnectionProvider;
import db.PdbDAO;
import db.SimpleDatabaseConnectionProviderImpl;
import db.db2.*;
import db.mysql.minimalPdbDAO;

*/
import db.search.DBQueryExecutor;

/*
 *  AbstractPDBSearch
 *  
 *  An abstract class for all of the core features used in each of the
 *  programs that search the PDB database
 *  
 *  EXAMPLE OF HOW TO USE AS A COMMAND LINE APP
 *  ADD A main METHOD LIKE THE FOLLOWING
	public static void main(String[] args) {
		ConcretePDBSearch app = new ConcretePDBSearch();
		app.run(args);
	}
 */
public abstract class AbstractPDBSearch {

	protected String db="MYSQL";

	protected DBQueryExecutor queryExecutor;
	
	protected boolean saveSearch = false;
	protected int queryId;


	protected boolean details = false;
	protected boolean validQuery = true;

	protected String pdbid="";
	protected String searchProperties = "";
	
	protected String KnownDataList;
	protected List<String> knownPositives;
	protected List<MatchingComplex> finalSet;
	
	protected QueryResult qResult;
	
	public List<MatchingComplex> getFinalSet() {
		return finalSet;
	}

	/*
	 * A version for running the command line app
	 */
	public void run(String[] args) {
		parseArguments(args);
		//prepareQueryExecutor();
		search();
		printResults();		
	}
	
	/* 
	 * A version for running otherwise
	 */
	public void run() {
		//prepareQueryExecutor();
		search();	
	}
	
	protected abstract void parseArguments(String[] args);
	
	protected abstract void printHelp();
	
	//protected abstract void prepareQueryExecutor();

	protected abstract void search();
	
	protected void loadKnownPositives() {
		knownPositives = new LinkedList<String>();
		try {
			  BufferedReader reader=new BufferedReader(new FileReader(KnownDataList));
			  String line=reader.readLine();
			  while (line!=null) {
			      String parse=line.trim();
			      if (parse.length()>0) {
			    	  String [] temp = parse.split(" ");
			    	  String pdbFile = getPdbIdForUnknownID(temp[0].trim());
			    	  knownPositives.add(pdbFile);
			      }
			      line=reader.readLine();
			  }
	      } catch (Exception e) {
	    	  System.err.print("Problem reading the list of known positives: " + KnownDataList + "\n");
				printHelp();
				System.exit(0);
	      }
		System.out.println("Loaded known structures : " + knownPositives.size());
	}

	
	protected void printResults() {
		System.out.println();
		int tp=0, tn=0, fp=0, fn =0;
		
		System.out.println("Number of Complete matches found: " + finalSet.size() );
		String spacer = "                            ";
		System.out.println("--------------------------------------------------------------------------------------------");
		System.out.println("PDB \tChain \tPositions "+spacer.substring(11)+"\tMatching Residues"+spacer.substring(18)+"\tKeywords");
		System.out.println("--------------------------------------------------------------------------------------------");
		
		List<String> found = new LinkedList<String>();
		for (MatchingComplex m : finalSet) {
			
			if(knownPositives != null) {
				if(knownPositives.contains(m.getPdb_id())) {
					if(details)
						System.out.print("T ");	
					if(found.contains(m.getPdb_id())) {
						//System.out.print("Duplicate for " + m.getPdb_id() + "\n");	
					} else {
						found.add(m.getPdb_id());
						tp++;
					}
				} else {
					if(details)
						System.out.print("F ");
					fp++;
				}
			} 

			
			if(details) {
				System.out.print(m.getPdb_id() + "\t" + m.getUniqueChains() + "\t");
				int[][] matches = m.getMatchPositions();
				String temp = ""; 
				for(int k=0; k<matches.length;k++) {
					temp = temp + " " + matches[k][0] + ":" + matches[k][1];
				}
				System.out.print(temp);
				if(temp.length()<spacer.length())
					System.out.print(spacer.substring(temp.length() ));
				
				temp= m.getMatchingResidues();
				System.out.print("\t" + temp);
				if(temp.length()<spacer.length())
					System.out.print(spacer.substring(temp.length() ));
					
				System.out.print("\t" + m.getKeywords());
				System.out.print(" \n");
			}
		}
		if(knownPositives != null) {
			fn=knownPositives.size()-tp;
			System.out.println("Performance Stats. TP: " + tp + " FP: " + fp + " FN: " + fn);
			System.out.println("          Sensitivity: " + ((double)tp/((double)tp+fn)) );
			System.out.println("                  PPV: " + ((double)tp/((double)tp+fp)) );
		}
	}

	protected void printMatchSet(int[][] matches) {
		for(int k=0; k<matches.length;k++) {
			System.out.print(" " + matches[k][0] + ":" + matches[k][1]);
		}

	}
	
	protected String getPdbIdForUnknownID(String in) {
		if(in.length()==4)
			return in;
		else
			return queryExecutor.getPdbID4Uniprot(in);
	}
	
	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
}
