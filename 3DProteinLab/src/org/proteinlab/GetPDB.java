package org.proteinlab;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetPDB extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public static void copy(InputStream in, OutputStream out) 
            throws IOException {
        final byte[] buffer = new byte[1024];
        for (int length; (length = in.read(buffer)) != -1;) {
            out.write(buffer, 0, length);
            
        }
        out.flush();
        out.close();
        in.close();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.err.println("doGet" );
		doPost(request,response);
	}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    		System.err.println("doPost" );
    		String pdbid = request.getParameter("pdbid").toString();
    		//String pathtofile = "https://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId=" + pdbid;
    		String pathtofile = "http://files.rcsb.org/download/" + pdbid +".pdb.gz";
    		System.err.println("Retrieving the file:" + pathtofile);
		final URL url = new URL(pathtofile);
		final URLConnection connection = url.openConnection(); 
        response.setContentType(connection.getContentType());
        response.setHeader( "Content-Disposition", "attachment; filename=\""+pdbid +".pdb.gz\"" );
        copy(connection.getInputStream(), response.getOutputStream());
        System.err.println("DONE" );
    }

}
