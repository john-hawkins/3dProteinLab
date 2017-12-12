package org.proteinlab;

import db.search.*;

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
 * Servlet implementation class for running admin applications
 */
public class AdminApp extends HttpServlet {
	
	private String[] dbprops;
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminApp() {
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
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		printHeader(out);
		
		String action = "";
		if( request.getParameter("action") != null ) {
			action = request.getParameter("action").toString();
		}
		String addr = request.getRemoteAddr();
		String host = request.getRemoteHost();
		DecimalFormat dfShort=new DecimalFormat("0");
		HttpSession session = request.getSession(true);

		boolean valid = false;
		
		if(action.equals("login")) {
			String username = "";
			if( request.getParameter("username") != null ) {
				username = request.getParameter("username").toString();
			}
			String password = "";
			if( request.getParameter("password") != null ) {
				password = request.getParameter("password").toString();
			}
			valid = checkLoginDetails(username, password, session);
			
		} else if(session.getAttribute("adminuser") != null && session.getAttribute("adminuser").equals("true")) {
			//valid = true;
		}

		if(action.equals("logout")) {
			session.removeAttribute("adminuser");
			valid = false;
		} 
		
		
		if( !valid ) { // LoG IN 
			if(action.equals("login")){
				out.println("<h3>Invalid Login details. Try again !</h3>");	
			}
			out.println("<FORM method='post' action='AdminApp' NAME='LOGIN'>");
			out.println("<input type='hidden' name='action' value='login'/>");	
			out.println("<LABEL>Username:</LABEL> <INPUT TYPE=TEXT NAME='username' class='login'/>");	
			out.println("<LABEL>Password:</LABEL> <INPUT TYPE=PASSWORD NAME='password'  class='login'/>");
			out.println("<INPUT TYPE=SUBMIT VALUE='Log In' class='login' />");
			out.println("</FORM>");
			
		} else {
		

			if(!action.equals("")) { // Parameter defined so we check it out

				out.println("<a href='AdminApp?action=logout'>Log Out</a><BR><BR>");

				out.println("<ul>");
				java.util.Enumeration<String> names = (java.util.Enumeration<String>) request.getHeaderNames();
				while (names.hasMoreElements()) {
				   String name = (String) names.nextElement();
				   String value = request.getHeader(name);
				   out.println(" <li>     <b>" + name + "=</b>" + value +"</li>");
				}
				out.println("</ul>");
				out.println("<BR><BR>");
				
				AdminRequest adminRequest = new AdminRequest(dbprops);
				

				try{

					List<QueryResult> finalSet = adminRequest.getListofQueries();


					out.println("<h3>Searches Made</h3>");

					out.println("<TABLE CLASS='results' id=\"resultsTable\">");
					out.println("<thead>");
					out.println("<TR>");
					out.println("<TH CLASS='results' NOWRAP>User</TH>");
					out.println("<TH CLASS='results' NOWRAP>Date</TH>");
					out.println("<TH CLASS='results' NOWRAP>Query String</TH>");
					out.println("<TH CLASS='results' NOWRAP># Results</TH>");
					out.println("</TR>");	
					out.println("</thead>");
					out.println("<TBODY>");
					int index = 0;
					/* NOW VIEW THEM */
					for (QueryResult q : finalSet) {
						index++;

						out.println("<TR>");

						out.println("<TD CLASS='results'> "+ q.getUser_id() + " </TD>");
						out.println("<TD CLASS='results'> "+ q.getDatestamp() +  " </TD>");
						out.println("<TD CLASS='results'> " + q.getQuery_string() + "</TD>");
						out.println("<TD CLASS='results' NOWRAP></TD>");
						out.println("</TR>");


					}
					out.println("</TBODY>");
					out.println("</TABLE>");

				} catch(Exception e) {
					out.println("<fieldset class='error'>");
					out.println("<legend>PROCESSING ERROR</legend>");
					out.println("<span class='css'>");
					out.println("	<strong>" + e.getMessage() + "</strong>");
					out.println("<p>Error:  " + e.toString() + "</p>");
					e.printStackTrace(out);
					out.println("</span>");
					out.println("</fieldset>");
				}
				
			}
		} 

		printFooter(out) ;
		out.close();
	}

	private void printHeader(PrintWriter out) {
		out.println("<HTML>");
		out.println("<HEAD>");
		out.println("  <TITLE>Admin App</TITLE>");
		out.println("  <link rel='stylesheet' type='text/css' href='style.css'> ");
		out.println("</HEAD>");
		out.println("<BODY>");
		out.println("<H2>Admin Application</H2><BR><BR>");
		out.println("<DIV id='wrapper'>");
	}
	
	private void printFooter(PrintWriter out) {
		out.println("</DIVY>");
		out.println("</BODY>");
		out.println("</HTML>");
	}

	private boolean checkLoginDetails(String username, String password, HttpSession session) {
		boolean validloggin = false;

        if (username == null || password == null) {
        	validloggin = false;
        } else if (username.toLowerCase().trim().equals("admin") && password.toLowerCase().trim().equals("b33lz3b@b")) {
            session.setAttribute("username", username);
            session.setAttribute("adminuser","true");
            validloggin = true;
        } else {
            validloggin = false;
        }
        
        return validloggin;
	}

}

