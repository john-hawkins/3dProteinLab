package org.proteinlab;

import db.search.InvalidQueryException;
import db.search.MatchingComplex;
import db.search.MatchingStructure;

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
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class MainSearch
 */
public class AjaxSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String[] dbprops;
	
	private int MaxResults = 200;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AjaxSearch() {
        super();
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
			System.err.println("### DBPROPS[0]: " + dbprops[0]);
			System.err.println("### DBPROPS[1]: " + dbprops[1]);
			System.err.println("### DBPROPS[2]: " + dbprops[2]);
			System.err.println("### DBPROPS[3]: " + dbprops[3]);
		}
		
        String action = request.getParameter("action").toString();

        String addr = request.getHeader("x-forwarded-for");
        if(addr==null) {
            addr = request.getRemoteAddr();
        } else {
        	String[] temp = addr.split(",");
        	addr = temp[temp.length-1];
        }
        String host = request.getRemoteHost();
        String user = request.getRemoteUser();
		HttpSession session = request.getSession(true);
		String sessionID = session.getId();
		
        DecimalFormat dfShort=new DecimalFormat("0");
        
		if(action.equals("progress") ) {

			/*
			 * This method is used by the AJAX progress bar code
			 * It returns an XML file containing just the percentage 
			 * progress of the current request.
			 */
			response.setContentType("text/xml");
			PrintWriter out = response.getWriter();
	
			String reqstr = request.getParameter("reqid").toString();
			int reqid;
			if(reqstr.equals("") || reqstr.equals("0") ) {
		        reqid = SearchRequest.getUserLatestRequestID(sessionID);
			} else {
		        reqid = Integer.parseInt( request.getParameter("reqid").toString() );
			}

			SearchRequest req = SearchRequest.getSearchRequest(reqid);
			int percentage = 100;
			if(req != null) {
				percentage = (int) req.getPercentageComplete();
			}
			
			out.println("<?xml version=\"1.0\"?>");
			out.println("<DOCUMENT><PROGRESS>"+ percentage + "</PROGRESS></DOCUMENT>");

	        out.close();
	        
		} else { // WE HAVE A NEW SEARCH REQUEST
			System.err.println("New Ajax Search Request");
			
			response.setContentType("text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
			
	        String pdbid = request.getParameter("pdbid").toString();
	        String scopid = request.getParameter("scopid").toString();
	        String keywords = request.getParameter("keywords").toString();
	        String redundancy = request.getParameter("redundancy").toString();
	        String annot = request.getParameter("annot").toString();
	        String resolution = request.getParameter("resolution").toString();
	        String resOp = request.getParameter("operator").toString();
	        String tech = request.getParameter("tech").toString();
	        String regex = request.getParameter("regex").toString();
	        String interactors = request.getParameter("interactors").toString();
	        String hits = request.getParameter("hits").toString();
	        String dist = request.getParameter("dist").toString();
	        

	        System.err.println("Ip Address:  " + addr + "  <BR>");
	        System.err.println("Host:  " + host + "  <BR>");
	        System.err.println("User:  " + user + "  <BR>");
	        System.err.println("User:  " + sessionID + "  <BR>");
	        System.err.println("PSEUDO POINTS:  " + dist + "  <BR>");
	        
	        /*
	         * First check if the user has something active and kill it
	         * we do not want the system clogged by redundant queries
	         */
	        SearchRequest.killRequstsByUser(sessionID);
	        
	        /*			*/
	        System.err.println("3D Protein Lab Search - PARAMETERS <BR>");
	        System.err.println("Ip Address:  " + addr + "  <BR>");
	        System.err.println("Host:  " + host + "  <BR>");
	        System.err.println("ACTION:  " + action + "  <BR>");
	        System.err.println("PDB ID:  " + pdbid + "  < BR>");
	        System.err.println("SCOP ID:  " + scopid + "  <BR>");
	        System.err.println("keywords:  " + keywords + "  <BR>");
	        System.err.println("redundancy:  " + redundancy + "  <BR>");
	        System.err.println("annot:  " + annot + "  <BR>");
	        System.err.println("resolution:  " + resolution + "  <BR>");
	        System.err.println("tech:  " + tech + "  <BR>");
	        System.err.println("regex:  " + regex + "  <BR>");
	        System.err.println("interactors:  " + interactors + "  <BR>");

			SearchRequest searchSearchRequest = new org.proteinlab.SearchRequest(sessionID, addr, pdbid,  scopid,  keywords, redundancy, annot,  resOp, resolution,   tech,   regex,   interactors, hits, dist);

			System.err.println("REQUEST CREATED ! ohh yeah  <BR>");

			try{
				searchSearchRequest.execute(dbprops);
				//System.err.println("REQUEST EXECUTED !  <BR>");
				List<MatchingComplex> finalSet = searchSearchRequest.getFinalSet();

            	out.println("<fieldset class='important'>");
            	out.println("<legend>Search Results</legend>");
            	out.println("<span class='css'>");
				if(finalSet.size() > MaxResults) {
					out.println("Total Matching PDBs:  " + finalSet.size() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Displaying the first " + MaxResults + " results. " + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				} else {
					out.println("Total Matching PDBs:  " + finalSet.size() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				}
				out.println("<a href='ExportCSV?queryid=" + searchSearchRequest.getQueryId() + "'>Export All Results as CSV File </a><BR><BR>");
            	out.println("</span>");
            	out.println("</fieldset>");

				//printOriginalTable(out, finalSet);
				//printGoogleTest(out, finalSet);
				printGoogleTable(out, finalSet);
				
			} catch(InvalidQueryException e) {	
            	out.println("<fieldset class='error'>");
            	out.println("<legend>QUERY ERROR</legend>");
            	out.println("<span class='css'>");
            	out.println("	<strong>" + e.getMessage() + "</strong>");
            	out.println("</span>");
            	out.println("</fieldset>");
            	searchSearchRequest.abandon();
			} catch(Exception e) {
            	out.println("<fieldset class='error'>");
            	out.println("<legend>PROCESSING ERROR</legend>");
            	out.println("<span class='css'>");
            	out.println("<strong>" + e.toString() + "</strong>");
				out.println("<p>Error:  " + e.getMessage() + "</p>");
				out.println("<p>This error has been logged in our bug tracker, with the query that generated it.</p>");
				out.println("<p>If you have further information which may help us, or if would like to know when we will be finished with this problem please contact us.</p>");
				out.println("<DIV style=\" z-index:1000; margin: 0px;  padding: 2px;  border: 1px solid gray;  font-size: 10px;  background-color: #FEFEFE;  color: #212121;\">");
				out.println("<b>Stack trace: </b>");
				e.printStackTrace(out);
				out.println("</DIV>");
            	out.println("</span>");
            	out.println("</fieldset>");
            	searchSearchRequest.abandon();
			}
			
			// For the moment I will delete the search request objects
			// in order to save memory. But we will change this so that
			// searches can be saved for people with accounts.
			searchSearchRequest.abandon();
			
	        out.close();
		}
	}

	private void printOriginalTable(PrintWriter out, List<MatchingStructure> finalSet) {
		out.println("<TABLE CLASS='results' id=\"resultsTable\">");
		out.println("<thead>");
		out.println("<TR>");
		out.println("<TH CLASS='results' COLSPAN=2 NOWRAP>Options</TH>");
		out.println("<TH CLASS='results' NOWRAP>PDB ID</TH>");
		out.println("<TH CLASS='results' NOWRAP>Match</TH>");
		out.println("<TH CLASS='results' NOWRAP>SCOP</TH>");
		out.println("<TH CLASS='results' NOWRAP>CATH</TH>");
		out.println("<TH CLASS='results' NOWRAP>Pfam</TH>");
		out.println("<TH CLASS='results' NOWRAP>PDB Keywords</TH>");
		out.println("<TH CLASS='results' NOWRAP></TH>");
		out.println("</TR>");	
		out.println("</thead>");
		out.println("<TBODY>");
		int index = 0;
		String lastPDB = "";
		for (MatchingStructure m : finalSet) {
			index++;
			String chain = m.getAuth_chain_id();
			int[][] matches = m.getAuth_matchPositions();
			String[][] insCodes = m.getAuth_matchPositionsInsCodes();
			
			String displayList = "";
			String scriptList = "";
			if(matches != null) {
				for(int k=0; k<matches.length;k++) {
					if(k!=0){
						displayList = displayList + " ";
						scriptList = scriptList + " OR ";
					}
					displayList = displayList + matches[k][0] + ":" + matches[k][1];
					String startpossie =  "" + matches[k][0];
					if(insCodes[k] != null && !insCodes[k][0].equals(""))
						startpossie = startpossie + "^" + insCodes[k][0];
					
					String endpossie =  "" + matches[k][1];
					if(insCodes[k] != null && !insCodes[k][1].equals(""))
						endpossie = endpossie + "^" + insCodes[k][1];
					
					scriptList = scriptList + startpossie + "-" + endpossie  + ":" + chain;
					
					//scriptList = scriptList + matches[k][0] + ":" + chain;
					//for(int x=matches[k][0]+1; x<matches[k][1]; x++ ) {
					//	scriptList = scriptList + " OR " +  x + ":" + chain;
					//}
				}
			}
			if( scriptList.equals("") ) {
				if( chain!=null) 
					scriptList = "*:" + chain;
				else
					scriptList = "*";
			}
			
			String mResidues = m.getMatchingResidues();
			String matchID = m.getPdb_id() + ":" +  chain;
			String alnimgID = "alnImage" + index;
			String threedimgID = "threedImage" + index;
			String imgID = "viewerImage" + index;
			
			String linkToALN = "toggleALN('"+ matchID +"', '"+ mResidues +"', '"+ imgID +"')";
			String linkToJmol = "loadAndHighlight('"+ m.getPdb_id() +"', '"+scriptList+"', '"+ matchID +"', '"+ mResidues +"', '"+ imgID +"')";
			

			String alnLink = "<img id=\""+alnimgID+"\" src=\"images/view_aln.png\" onclick=\""+linkToALN+"\" title=\"View in Alignment\" >";
			String threedLink = "<img id=\""+threedimgID+"\" src=\"images/view_3d.png\" onclick=\""+linkToJmol+"\"  title=\"View in Jmol\" >";
			String viewerLink = "<img id=\""+imgID+"\" src=\"images/box.png\" >";
			
			
			out.println("<TR>");

			out.print("<TD CLASS='results'> "+ threedLink  + " </TD>");
			out.print("<TD CLASS='results'> "+ alnLink +  " </TD>");
			
			if(lastPDB.equals(m.getPdb_id())) {
				out.print("<TD CLASS='results'>  </TD>");
			} else {
				out.print("<TD CLASS='results'> <a>" + m.getPdb_id() + "</a></TD>");
				lastPDB = m.getPdb_id();
			}
			String matchRollover = "Chain: " + chain + " Positions: " + displayList;

			// old version
			//out.print("<TD CLASS='results' NOWRAP>" + chain + "</TD>");
			//out.print("<TD CLASS='results' NOWRAP>" + displayList + "</TD>");
			// New Version
			String popUpControl = "<a href=# style=\"text-decoration:none;\" onmouseover=\"displayHelpBalloon('HELPER','on', '"+matchRollover+"');return false;\" onmouseout=\"displayHelpBalloon('HELPER','off');return false;\">";
			
			out.print("<TD CLASS='results' NOWRAP>" + popUpControl + mResidues + "</a></TD>");

			out.println("<TD CLASS='results' NOWRAP>");
			if(m.getScop_sccs()==null || m.getScop_sccs().equals("")) {
				out.println("&nbsp;");
			} else {
				out.println("<a href='http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?search_type=scop&key=" + m.getScop_sccs() + "'>" + m.getScop_sccs() + "</a>");
				//String val = dfShort.format( m.getScop_percent() );
				//String spacer="";
				//if(val.length()==2)
				//	spacer="&nbsp;";
				//if(val.length()==1)
				//	spacer="&nbsp;&nbsp;";
				//out.println("[" + spacer + val + "%]</TD>");
			}
			out.println("</TD>");

			out.println("<TD CLASS='results' NOWRAP>");
			if(m.getCathHierID()==null || m.getCathHierID().equals("")) {
				out.println("&nbsp;");
			} else {
				out.println("<a href='http://www.cathdb.info/cathnode/" + m.getCathHierID() + "'>" + m.getCathHierID() + "</a>");
				//String val = dfShort.format( m.getScop_percent() );
				//String spacer="";
				//if(val.length()==2)
				//	spacer="&nbsp;";
				//if(val.length()==1)
				//	spacer="&nbsp;&nbsp;";
				//out.println("[" + spacer + val + "%]</TD>");
			}
			out.println("</TD>");
			

			out.println("<TD CLASS='results' NOWRAP>");
			if(m.getPfam_id()==null || m.getPfam_id().equals("")) {
				out.println("&nbsp;");
			} else {
				out.println("<a href='http://pfam.sanger.ac.uk/family/" + m.getPfam_acc() + "'>" + m.getPfam_acc() + "</a>");
				//String val = dfShort.format( m.getPfam_percent() );
				//String spacer="";
				//if(val.length()==2)
				//	spacer="&nbsp;";
				//if(val.length()==1)
				//	spacer="&nbsp;&nbsp;";
				//out.println("[" + spacer + val + "%]</TD>");
			}
			out.println("</TD>");

			out.print("<TD CLASS='results'>" + m.getKeywords() + "</TD>"); 

			out.print("<TD CLASS='results'> "+ viewerLink + " </TD>");
			out.println("</TR>");


		}
		out.println("</TBODY>");
		out.println("</TABLE>");
		
		/*
		 * NOW WE ADD SOME JAVASCRIPT EFFECTS TO THE TABLE
		 */
		out.println("<script type=\"text/javascript\">");
		out.println("	addTableRolloverEffect('resultsTable','tableRollOverEffect','tableRowClickEffect');");
		out.println("</script>");
	}

	
	/*
	 * An output experiment.
	 * Use the google visualisation javascript tools so that the table is sortable within the browser
	 */
	
	private void printGoogleTable(PrintWriter out, List<MatchingComplex> finalSet) {
		int index;
		out.println("<script type='text/javascript'>");

		out.println("var data = new google.visualization.DataTable();");
		out.println("data.addColumn('string', 'VIEW');");
		out.println("data.addColumn('string', 'PDB');");
		out.println("data.addColumn('string', 'Match');");
		out.println("data.addColumn('string', 'SCOP');");
		out.println("data.addColumn('string', 'CATH');");
		out.println("data.addColumn('string', 'Pfam');");
		out.println("data.addColumn('string', 'Keywords');");
		out.println("data.addColumn('string', ' ');");

		if(finalSet.size() > MaxResults)
			out.println("data.addRows("+ MaxResults +");");
		else
			out.println("data.addRows("+ finalSet.size() +");");
		index = 0;
		
		System.err.println("Building Table for Display");
		for (MatchingComplex m : finalSet) {	
			String[] chains = m.getAuth_matchPositionsChainIds();
			String uniqueChains = m.getUniqueChainList();
			int[][] matches = m.getAuth_matchPositions();
			String[][] insCodes = m.getAuth_matchPositionsInsCodes();
			
			String displayList = "";
			String fetchList = "";
			String scriptList = "";
			
			if(matches != null) {
				for(int k=0; k<matches.length;k++) {
					boolean highlightResidues = true;

					String displayListTEMP = "";
					String fetchListTEMP = "";
					String scriptListTEMP = "";
					
					if(k!=0){
						fetchListTEMP = fetchListTEMP + "-";
						displayListTEMP = displayListTEMP + " ";
						scriptListTEMP = scriptListTEMP + " OR ";
					}
					int startPos = matches[k][0];
					int endPos = matches[k][matches[k].length-1];
					String startIns = insCodes[k][0];
					String endIns = insCodes[k][insCodes[k].length-1];
					// NOW FIRST MAKE SURE THAT NONE OF THE POSITIONS ARE UNSTRUCTURED
					if(startIns.equals("?") || endIns.equals("?")) {
						// WE NEED TO ADJUST THESE BASTARDS
						if(startIns.equals("?") && endIns.equals("?")) {
							// IT IS FUCKED SO SET THE OUTPUT TO IGNORE POSITIONS
							highlightResidues = false;
						} else if(startIns.equals("?")) {
							// WORK OUR WAY FORWARD
							int posIndex = 1;
							while(insCodes[k][posIndex]=="?") posIndex++;
							startPos = matches[k][posIndex];
							startIns = insCodes[k][posIndex];
						} else {
							// WORK OUR WAY BACKWARD
							int posIndex = insCodes[k].length - 1;
							while(insCodes[k][posIndex]=="?") posIndex--;
							endPos = matches[k][posIndex];
							endIns = insCodes[k][posIndex];
						}
					}
					
					fetchListTEMP = fetchListTEMP + chains[k] + ":" + startPos + ":" + endPos;
					displayListTEMP = displayListTEMP + startPos + ":" + endPos;
					String startpossie =  "" + startPos;
					if(startIns != null && !startIns.equals(""))
						startpossie = startpossie + "^" + startIns;
					
					String endpossie =  "" + endPos;
					if(endIns != null && !endIns.equals(""))
						endpossie = endpossie + "^" + endIns;
					
					scriptListTEMP = scriptListTEMP + startpossie + "-" + endpossie  + ":" + chains[k];
					
					if(highlightResidues) {
						fetchList = fetchList + fetchListTEMP ;
						displayList = displayList + displayListTEMP;
						scriptList = scriptList + scriptListTEMP;
					}
				}
			}
			if( scriptList.equals("") ) {

				scriptList = "";
				/*
				if( chain!=null) 
					scriptList = "*:" + chain;
				else
					scriptList = "*";
					*/
			}
			if( displayList.equals("") ) {
				displayList = "[Unresolved]";
			}
			
			String mResidues = m.getMatchingResidues().replaceAll(":", "").replaceAll(" <=> ", " ^ ");
			String matchID = m.getPdb_id() + ":" +  uniqueChains;
			String alnimgID = "alnImage" + index;
			String threedimgID = "threedImage" + index;
			String exportimgID = "exportImage" + index;
			String imgID = "viewerImage" + index;
			
			String matchRollover = "Chains: " + uniqueChains + " Positions: " + displayList;

			String disResidues = mResidues;
			if(mResidues.length() > 20) disResidues = mResidues.substring(0, 17) + "...";
			String matchWithPopUp = "<a href=# title='"+matchRollover+"'>"+disResidues+"</a>";

			String scopWithPopUp = "";
			if(m.getScop_sccs()!=null && !m.getScop_sccs().equals("")) {
				scopWithPopUp = "<a href='http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?search_type=scop&key=" + m.getScop_sccs() + "' title='"+m.getScop_descript().replaceAll("'", "\\\\\\'").replaceAll("\"", "\\\\\\\"") +"'>" + m.getScop_sccs() + "</a>";
			}
			
			String cathWithPopUp = "";
			if(m.getCathHierID()!=null && !m.getCathHierID().equals("")) {
				cathWithPopUp = "<a href='http://www.cathdb.info/cathnode/" + m.getCathHierID() + "' title='"+m.getCathDomainDescription().replaceAll("'", "\\\\\\'").replaceAll("\"", "\\\\\\\"") +"'>" + m.getCathHierID() + "</a>";
			}
			
			String pfamWithPopUp = "";
			if(m.getPfam_id()!=null && !m.getPfam_id().equals("")) {
				pfamWithPopUp = "<a href='http://pfam.sanger.ac.uk/family/" + m.getPfam_acc() + "' title='"+m.getPfam_descript().replaceAll("'", "\\\\\\'").replaceAll("\"", "\\\\\\\"") +"'>" + m.getPfam_acc() + "</a>";
			}
			
			String keywds = m.getKeywords().toUpperCase();
			int keywordLen = keywds.length();
			if(keywordLen > 10) keywordLen=10;
			String keywordsWithPopUp = "<a href=# title='"+keywds +"'>" + keywds.substring(0, keywordLen) + "...</a>";
			
			String linkToALN = "toggleALN('"+ matchID +"', '"+ mResidues +"', '"+ imgID +"')";
			String linkToJmol = "loadAndHighlight('"+ m.getPdb_id() +"', '"+scriptList+"', '"+ matchID +"', '"+ mResidues +"', '"+ imgID +"')";
			String linkToExport = "FetchPDB?action=partial&pdbid="+ m.getPdb_id() + "&residues="+fetchList;
			

			String alnLink = "<a HREF=\\\"javascript:"+linkToALN+"\\\"><img id='"+alnimgID+"' src='images/view_aln2.png' title='View in Alignment' > </a>";
			String threedLink = "<a HREF=\\\"javascript:"+linkToJmol+"\\\"><img id='"+threedimgID+"' src='images/view_3d2.png'  title='View in Jmol' > </a>";
			String exportLink = "<a HREF=\\\""+linkToExport+"\\\"><img id='"+exportimgID+"' src='images/export2.png'  title='Export matching residues' > </a>";
			String viewerLink = "<img id='"+imgID+"' src='images/box.png' >";
			

			//System.err.println("Entry: " + m.getPdb_id() );
			//System.err.println("mResidues: " + mResidues );
			//System.err.println("matchWithPopUp: " + matchWithPopUp );
			//System.err.println("scopWithPopUp: " + scopWithPopUp );
			//System.err.println("cathWithPopUp: " + cathWithPopUp );
			//System.err.println("pfamWithPopUp: " + pfamWithPopUp );
			//System.err.println("keywordsWithPopUp: " + keywordsWithPopUp );
			
			out.print("data.setCell("+index+", 0, \""+ threedLink + alnLink  + exportLink + "\");");
			out.print("data.setCell("+index+", 1, \""+ m.getPdb_id()  + "\");");
			out.print("data.setCell("+index+", 2, \""+ mResidues  + "\", \""+ matchWithPopUp  + "\", {style: 'font-family: Courier;font-size: 14px'} );");

			out.print("data.setCell("+index+", 3, \""+scopWithPopUp+"\");");
			out.print("data.setCell("+index+", 4, \""+cathWithPopUp+"\");");
			out.print("data.setCell("+index+", 5, \""+pfamWithPopUp+"\");");

			out.print("data.setCell("+index+", 6, \""+ keywordsWithPopUp  + "\");");
			out.print("data.setCell("+index+", 7, \""+ viewerLink  + "\");");
				
			index++;

			//if(index > 35)	
			if(index > (MaxResults-2))
				break;
		}

		out.println("var table = new google.visualization.Table(document.getElementById('results_table_div'));");
		out.println("table.draw(data, {allowHtml: true, enableTooltip:true});");

		//out.println("google.visualization.events.addListener(table, 'onmouseover', onmouseoverHandler);");
		//out.println("google.visualization.events.addListener(table, 'onmouseout', barMouseOut);");

		//out.println("function onmouseoverHandler(e) {");
		//out.println("	alert(\"HERE I IS\");");
		//out.println("	alert(\"DATA CELL:\" + e.row + \" \" + e.column );");
		//out.println("}");

		
		out.println("</script>");
	}

	private void printGoogleTest(PrintWriter out, List<MatchingStructure> finalSet) {

		out.println("<BR>GOOGLE VISUALISATION TEST<BR>");
		out.println("<script type='text/javascript'>");
		out.println("    var data = new google.visualization.DataTable();");
		out.println("    data.addColumn('string', 'Name'); ");
		out.println("    data.addColumn('number', 'Salary');");
		out.println("    data.addColumn('boolean', 'Full Time Employee');");
		out.println("    data.addRows(4);");
		out.println("    data.setCell(0, 0, 'Mike2');");
		out.println("    data.setCell(0, 1, 10000, '$10,000');");
		out.println("    data.setCell(0, 2, true);");
		out.println("    data.setCell(1, 0, 'Jim2');");
		out.println("    data.setCell(1, 1, 8000, '$8,000');");
		out.println("    data.setCell(1, 2, false);");
		out.println("    data.setCell(2, 0, 'Alice2');");
		out.println("    data.setCell(2, 1, 12500, '$12,500');");
		out.println("    data.setCell(2, 2, true);");
		out.println("    data.setCell(3, 0, 'Bob2');");
		out.println("    data.setCell(3, 1, 7000, '$7,000');");
		out.println("    data.setCell(3, 2, true);");
		out.println("    var table = new google.visualization.Table(document.getElementById('results_table_div'));");
		out.println("    table.draw(data, {allowHtml: true});");
		out.println("</script>");
		out.println("<BR>END GOOGLE VISUALISATION TEST<BR>");
	}
	
}

