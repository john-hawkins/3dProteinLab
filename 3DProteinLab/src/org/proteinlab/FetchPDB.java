package org.proteinlab;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FetchPDB extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
        
		String mimeType = "text";
        response.setContentType(mimeType);
        PrintWriter out = response.getWriter();
        
        String action = "whole";
        String pdbid = request.getParameter("pdbid").toString();
        if( request.getParameterMap().containsKey("action") )
        	 action = request.getParameter("action").toString();
        
		if(action.equals("partial") ) {

			//String chain = request.getParameter("chain").toString();
			String residueRange = request.getParameter("residues").toString();
			
			String[] ranges = residueRange.split("-");
			String[] chains = new String[ranges.length] ;
			int[][] startNEnds = new int[ranges.length][2];
			for(int r=0; r<ranges.length; r++) {
				String[] ends = ranges[r].split(":");
				chains[r] = ends[0];
				startNEnds[r][0] = Integer.parseInt(ends[1]);
				startNEnds[r][1] = Integer.parseInt(ends[2]);
			}
		
			response.setHeader( "Content-Disposition", "attachment; filename=\""+pdbid +"-"+residueRange +".pdb\"" );
			
			String thePDB = getPDBFile(pdbid);
			
			String[] lines = thePDB.split("\n");
			for(int i=0; i<lines.length; i++) {
				if(lines[i].startsWith("HEADER")) {
					out.write(lines[i] + "\n");
				} else if(lines[i].startsWith("TITLE")) {
					out.write(lines[i] + "\n");
				} else if(lines[i].startsWith("ATOM")) {
					// SPLIT THE ATOM ( HA HA HA ! )
					String[] tempie = lines[i].split(" +");
					String chained =  tempie[4].trim() ;

					int resNum = Integer.parseInt( tempie[5].trim() );
					for(int r=0; r<ranges.length; r++) {
							if(resNum >= startNEnds[r][0] && resNum <= startNEnds[r][1] && chained==chains[r]) {
								out.write(lines[i] + "\n");
								break;
							}
					}	
				}
			}
	        
	        
		} else { // WHOLE PDB

			response.setHeader( "Content-Disposition", "attachment; filename=\""+pdbid +".pdb\"" );

			out.write(getPDBFile(pdbid));
		}

        out.close();
	}
		
	private String getPDBFile(String pdbid) {
		String result = "";
		String pathtofile = "http://www.pdb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId="+ pdbid ;

        try        {
        	URL                url; 
        	URLConnection      urlConn; 

        	url = new URL(pathtofile);

        	urlConn = url.openConnection(); 
        	urlConn.setDoInput(true); 
        	urlConn.setUseCaches(false);

            BufferedReader d = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            // Copy the contents of the stream to the output stream
            char[] buf = new char[1024];
            int count = 0;
            while ((count = d.read(buf)) >= 0) {
            	result = result + new String(buf).substring(0, count);
            }

        	d.close(); 
        }

        catch (MalformedURLException mue) {} 
        catch (IOException ioe) {} 
		
		return result;
	}
}
