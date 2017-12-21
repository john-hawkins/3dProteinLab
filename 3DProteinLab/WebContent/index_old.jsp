<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>3D Protein Lab</title>

	<link rel="stylesheet" type="text/css" href="style.css"> 
	<link rel="icon" type="image/ico" href="3d.proteinlab.org/favicon.ico">
	
	<!-- SOME JAVASCRIPT LIBRARIES -->
	<script src="js/browser_info.js" type="text/javascript"></script>
	<script src="js/jquery-1.3.1.min.js" type="text/javascript"></script>
	<script src="js/forms.js" type="text/javascript"></script>
	<script src="js/helpBalloons.js" type="text/javascript"></script>
	<script src="js/ajaxscript.js" type="text/javascript"></script>
	<script src="js/table_effects.js" type="text/javascript"></script>
	<script src="js/sidebarslide2.js" type="text/javascript"></script>
	<script src="js/jquery.alerts.js" type="text/javascript"></script>

	<script src="applets/jmol-11.8.25/Jmol.js" type="text/javascript"></script>
	
	<script src="js/site_specific.js" type="text/javascript"></script>
	
	<!--- GOOGLE SCRIPT FOR SORTABLE TABLES   --->
	<script type='text/javascript' src='http://www.google.com/jsapi'></script>
	<script src="js/loadGoogleAPIExample.js" type="text/javascript"></script>

	<!--[if IE]>
  	<script type="text/javascript">

  		(function(){

        	var html5elmeents = "address|article|aside|audio|canvas|command|datalist|details|dialog|figure|figcaption|footer|header|hgroup|keygen|mark|meter|menu|nav|progress|ruby|section|time|video".split('|');
			for(var i = 0; i < html5elmeents.length; i++){
				document.createElement(html5elmeents[i]);
  			}
  		})();

  	</script>
	<![endif]-->
	
</head>
<body onload="onLoadScript();">

<form id='masterForm' method="post" action="" NAME="masterForm">
<div class='hiddenFields'>
	<input type="hidden" name="action" value="" />
	<input type="hidden" name="reqid" value="" />
	<input type="hidden" name="keywords" value="" />
	<input type="hidden" name="redundancy" value="" />
	<input type="hidden" name="pdbid" value="" />
	<input type="hidden" name="scopid" value="" />
	<input type="hidden" name="annot" value="ALL" />
	<input type="hidden" name="resolution" value="" />
	<input type="hidden" name="operator" value="" />
	<input type="hidden" name="tech" value="ALL" />
	<input type="hidden" name="regex" value="" />
	<input type="hidden" name="interactors" value="" />
	<input type="hidden" name="hits" value="" />
	<input type="hidden" name="dist" value="" />
</div>
</form> 

<div id="wrapper">

<header id="header"> <!-- HTML5 header tag -->
	<div id="header">
		<div id="logo">
				<h1><img src="images/3DLogo.png"></img> <a href="">3D Protein Lab</a></h1>
		</div>

		<div id="status">
			<div class="progressMeter"> <div id="progressBar" class="progressBar"> &nbsp;&nbsp;&nbsp;Page Loading...   </div> </div>
		</div>
	</div>
</header>

<BR class="clear" />

<nav class="main"> <!-- HTML5 navigation tag -->

	<div id="navigation"> 

		<ul>
			<li><a href="#" class="current">3D Structure Search</a></li>
			<li><a href="">Refresh</a></li>
			<li><a href="#" class="help" id="helpButton">Help</a></li>
		</ul>
		
	</div>	

</nav>

<section id="contentcontainer"> <!-- HTML5 section tag for the content 'section' -->

	<div id="search-wrap">

		<jsp:include page="search-form.jsp" />

	</div>

<div id="content">


	<div id="messages"></div>
		
	<!--  THE FOLLOWING DIV IS USED BY THE CODE IN helpBalloons.js : A very simple popup help window script -->
	<DIV id=HELPER style='width:100px;height:10px;background-color:#DDFFDD;border-style:solid;border-width:1px;border-color:#99DD99;padding:5px;text-align:left;position:absolute;display:none;text-decoration:none;'>HELP</DIV>
		
	<div id="results">
				
		<h2>Welcome to 3D Protein Lab</h2>
		
		<p>The goal of this project is to allow you to search through the PDB database of protein structures
		using a combination of sequence and structure constraints. We have developed a pattern syntax that is
		an extension of the PROSITE pattern syntax. The sequence components are defined using
		the PROSITE pattern syntax, and the structural features are implemented using our own syntax.
		Please see the help page for a description of all the elements of the expression language.
		The search language and engine are described in our forthcoming publication</p>
		
		<b>Hawkins & Pisabarro</b> (2010). <i>3D Regular Expressions for Protein Structure Search.</i>
		(In Preparation).
		
		<BR><BR>
		
		<p>To see an example of the results returned by 3D Protein Lab, 
		<a href='#' title='Load the Example Pattern: Class I PDZ Binding Site' onclick="document.forms['search'].regex.value='cG-[LF]-G-[LFI]-[SN]-sI-<8,10>-h[LI]-[KR]'">load this example</a>
		and run the query engine. This text will be replaced with the matching structures. Experiment with modifying the pattern to see how the results change.
		</p>
		
		<BR><BR>
		
		<!-- 
		<p>Search results will be listed as shown in the example below, showing you the residues
		that have matched query, and any known functional classification of those resdiues. 
		In addition you will be able use the built in viewer to check the PDB file with the matching 
		residues highlighted and then extract the matching residues into a seperate PDB file. </p>
		 -->
		<fieldset class='important'>
		<legend>Please Note</legend>
		<span class='css'>
		As the query language is open, the amount of time that is required to run a query can vary dramatically.
		The query status bar cannot start to show progress until after the SQL has returned the initial list of
		potential matches. We recommend that you include some specific residues in the pattern, and try and
		make the sequence components as long as possible. This will severely decrease the number of potential
		matches that need to be evaluated.</p>
		</span>
		</fieldset>
		<BR>
	</div>	

</div>
	
	<div id='results_table_div'>
	</div>	

</section>


<!-- RIGHT HAND FLOATING TOOL KIT -->

<section id="sidebarcontainer"> <!-- HTML5 section tag for the content 'section' -->

<div id="rightsidebarcont">

   <div id="rightsidebar">
  	
		<nav class="second"> <!-- HTML5 navigation tag -->
			<div id="navigation2"> 
				<ul>
					<li><a href="#" class="current">Jmol Viewer</a></li>
					<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</li>
					<li><b><div id="jmolmessages"></div></b></li>
				</ul>
			</div>	
		</nav>	

		<div id="jmolcontrol-wrap">
			<jsp:include page="jmolcontrol.jsp" />
		</div>
	
  		<div class="rsideblock">
  			<div id="jmol" class="box">
				<script type="text/javascript">
					jmolInitialize("./applets/jmol-11.8.25");
					var script = "background [xFFFFFF]";
					var widthForJmol = getSideBarWidth();
					var viewerHeight = getJmolHeight();
					var sizeArray = new Array();
					sizeArray[0]=widthForJmol;
					sizeArray[1]=viewerHeight;
					jmolApplet(sizeArray, script);  
				</script>			
			</div>
		</div>
	

 	
 	<div class="rsideblock" id="alignmentViewer">
	   <nav class="second"> <!-- HTML5 navigation tag -->
		<div id="navigation3"> 
			<ul>
				<li><a href="#" class="current">Alignment Viewer</a></li>
				<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</li>
				<li><b><div id="alnmessages"></div></b></li>
			</ul>
		</div>	
	   </nav>	
	   
	   <div id="reference">
		<TABLE CLASS='referenceSeq' id="referenceSeq">
		</TABLE>
	   </div>
	
	   <div id="alignment">
		<TABLE CLASS='alignmentSeq' id="alignmentSeq">
		</TABLE>
	   </div>

	</div>

   </div>

</div>


</section>
<!-- RIGHT HAND FLOATING TOOL KIT -->

<footer> <!-- HTML5 footer tag -->
	<!-- <div id="footer"> (c) 2010 John.C.Hawkins </div> -->
</footer>

</div>

</body>
</html>