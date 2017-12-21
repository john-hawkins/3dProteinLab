package org.proteinlab;


import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FetchPDBFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int BUFFER_SIZE = 4096;
	
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

        String pdbid = request.getParameter("pdbid").toString();
        
		response.setHeader( "Content-Disposition", "attachment; filename=\""+pdbid +".pdb\"" );

		out.write(getPDBFile(pdbid));

        out.close();
	}
		
	/*
	 * 
	 */
	private String getPDBFile(String pdbid) {
		String result = "";
		String pathtofile = "https://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId=" + pdbid;
		
        try        {
        		URL                		url; 
        		HttpURLConnection      httpConn; 

        		url = new URL(pathtofile);

        		httpConn = (HttpURLConnection) url.openConnection(); 
        		
            int responseCode = httpConn.getResponseCode();
                
             // always check HTTP response code first
             if (responseCode == HttpURLConnection.HTTP_OK) {
                 String fileName = "";
                 String disposition = httpConn.getHeaderField("Content-Disposition");
                 String contentType = httpConn.getContentType();
                 int contentLength = httpConn.getContentLength();
      
                 if (disposition != null) {
                     // extracts file name from header field
                     int index = disposition.indexOf("filename=");
                     if (index > 0) {
                         fileName = disposition.substring(index + 10,
                                 disposition.length() - 1);
                     }
                 } else {
                     fileName = pdbid+".pdb";
                 }
      
                 System.out.println("Content-Type = " + contentType);
                 System.out.println("Content-Disposition = " + disposition);
                 System.out.println("Content-Length = " + contentLength);
                 System.out.println("fileName = " + fileName);
      
                 // opens input stream from the HTTP connection
                 InputStream inputStream = httpConn.getInputStream();
                 StringWriter sw = new StringWriter();
                 
                 int bytesRead = -1;
                 byte[] buffer = new byte[BUFFER_SIZE];
                 while ((bytesRead = inputStream.read(buffer)) != -1) {
                	 	sw.write( new String(buffer).substring(0, bytesRead) );
                 }
                 result = sw.toString();
                 sw.flush();
                 inputStream.close();
      
                 System.out.println("File downloaded");
            	 
             } else {
                 System.out.println("No file to download. Server replied HTTP code: " + responseCode);
             }
                
        }
        catch (MalformedURLException mue) {} 
        catch (IOException ioe) {} 
		
		return result;
	}

}
