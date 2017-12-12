package org.proteinlab;

import db.search.InvalidQueryException;
import db.search.MatchingComplex;
import db.search.MatchingStructure;
import db.search.StructExpPDBSearch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MainSearch
 */
public class ExportCSV extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String[] dbprops;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExportCSV() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if(dbprops==null) {
			String realPathToProps = getServletContext().getRealPath("/WEB-INF/conf/database.properties");
			Properties properties = new Properties(); 
			properties.load(new FileInputStream(realPathToProps));
			dbprops = new String[4];
			dbprops[0] = properties.getProperty("driver");
			dbprops[1] = properties.getProperty("dbpath");
			dbprops[2] = "user=" + properties.getProperty("user");
			dbprops[3] = "password=" + properties.getProperty("pass");
		}
		
		int queryid = Integer.parseInt(request.getParameter("queryid").toString());

		DecimalFormat dfShort=new DecimalFormat("0");

		response.setContentType("text/csv");
		response.setHeader( "Content-Disposition", "attachment; filename=\"SearchResults.csv\"" );
		
		PrintWriter out = response.getWriter();

		out.print("PDB ID, Chain:Seq-Positions, Match Residues, SCOP Overlap,  SCOP Percent, Pfam Overlap, Pfam Percent, PDB Keywords");

		List<MatchingComplex> finalSet = SearchRequest.getResultsForQuery(queryid, dbprops);
		int index =0;
		for (MatchingComplex m : finalSet) {
			index++;

			//String chain = m.getAuth_chain_id();
			String[] chains = m.getAuth_matchPositionsChainIds();
			String uniqueChains = m.getUniqueChainList();
			
			int[][] matches = m.getAuth_matchPositions();
			String[][] insCodes = m.getAuth_matchPositionsInsCodes();

			String displayList = "";

			if(matches != null) {
				for(int k=0; k<matches.length;k++) {
					if(k!=0){
						displayList = displayList + " ";
					}
					displayList = displayList + chains[k] + ":" + matches[k][0] + "-" + matches[k][1];
					String startpossie =  "" + matches[k][0];
					if(insCodes[k] != null && !insCodes[k][0].equals(""))
						startpossie = startpossie + "^" + insCodes[k][0];

					String endpossie =  "" + matches[k][1];
					if(insCodes[k] != null && !insCodes[k][1].equals(""))
						endpossie = endpossie + "^" + insCodes[k][1];
				}
			}

			String mResidues = m.getMatchingResidues();

			out.print("\n");
			out.print( m.getPdb_id() + ", ");
			out.print( displayList + ", " );
			out.print( mResidues  + ", " );
			
			if(m.getScop_sccs()==null || m.getScop_sccs().equals("")) {
				out.print(", " + ", ");
			} else {
				String val = dfShort.format( m.getScop_percent() );
				out.print( m.getScop_sccs() + ", " + val + ", " );
			}
			if(m.getPfam_id()==null || m.getPfam_id().equals("")) {
				out.print(", " + ", ");
			} else {
				String val = dfShort.format( m.getPfam_percent() );
				out.print( m.getPfam_acc() + ", " + val + ", " );
			}

			out.print( "\"" + m.getKeywords() + "\"" ); 
		}

		out.close();
	}
}

