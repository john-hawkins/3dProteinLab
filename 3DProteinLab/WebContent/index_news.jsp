<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>3D Protein Lab</title>

	<link rel="stylesheet" type="text/css" href="style.css"> 

	<link rel="shortcut icon" href="favicon.ico">
	<link rel="icon" type="image/ico" href="favicon.ico">
	
	<!-- SOME JAVASCRIPT LIBRARIES -->
	<script src="js/jquery-1.3.1.min.js" type="text/javascript"></script>
	<script src="js/forms.js" type="text/javascript"></script>
	<script src="js/ajaxscript.js" type="text/javascript"></script>
	<script src="js/table_effects.js" type="text/javascript"></script>

</head>
<body>

<form id='masterForm' method="post" action="" NAME="masterForm">
<div class='hiddenFields'>
	<input type="hidden" name="action" value="" />
	<input type="hidden" name="reqid" value="" />
	<input type="hidden" name="keywords" value="" />
	<input type="hidden" name="pdbid" value="" />
	<input type="hidden" name="scopid" value="" />
	<input type="hidden" name="annot" value="ALL" />
	<input type="hidden" name="resolution" value="" />
	<input type="hidden" name="operator" value="" />
	<input type="hidden" name="tech" value="ALL" />
	<input type="hidden" name="regex" value="" />
	<input type="hidden" name="interactors" value="" />
</div>
</form> 

<div id="wrapper">

<header id="header"> <!-- HTML5 header tag -->
	<div id="header">
		<div id="logo">
				<h1><img src="images/Logo.png"></img> <a href="">3D Protein Lab</a></h1>
		</div>

		<div id="status">
			<div class="progressMeter"> <div id="progressBar" class="progressBar"> &nbsp;&nbsp;&nbsp;Page Loading...   </div> </div>
		</div>
	</div>
</header>

<BR class="clear" />

<nav class="main floatL colSpan2"> <!-- HTML5 navigation tag -->

	<div id="navigation"> 

		<ul>
			<li><a href="" class="current">Home</a></li>
			<li><a href="#" class="search">Structure Search</a></li>
			<li><a href="#" class="contact" id="contactButton">Contact</a></li>
			<li><a href="#">References</a></li>
			<li><a href="#" class="help" id="helpButton">Help</a></li>
		</ul>
		
	</div>	
	<div id="navigation2"> 

		<ul>
			<li><a href="#" class="viewer" id="viewerButton">3D Viewer</a></li>
			<li><a href="#" class="help" id="helpButton">Help</a></li>
		</ul>
		
	</div>	


</nav>

<section id="contentcontainer"> <!-- HTML5 section tag for the content 'section' -->

<div id="content">

	<div id="messages">
		
		<h2>Welcome to the 3D Protein Lab</h2>
		
		<p>This site allows you to search for patterns in both the sequence and
		structure of proteins contained in the PDB collection of protein structures.</p>
		
		<p>Your search results will first be presented as a summary, telling you how many
		proteins matched the complete query, and how many matched the sequence constraints only.
		You will be informed about the coverage of the SCOP hierarchy that the pattern matches,
		and you will receive information about whether there are over-represented GO terms 
		associated with that pattern. </p>
		
		<p>In the full results for a query you can see the location of each match
		in the PDB files, as the Chain ID and sequence positions. Using these
		allowing you to quickly extract a set of protein structures with a specific set of features.</p>
	
	</div>	

	<div id="results">
		
	</div>		
	
</div>

</section>

<footer> <!-- HTML5 footer tag -->
	<!-- <div id="footer"> (c) 2010 John.C.Hawkins </div> -->
</footer>

</div>

</body>
</html>