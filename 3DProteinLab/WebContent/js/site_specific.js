/************************************************************************************************************
// A set of functions that are specific to this site only.
// All other JS files are shared between sites.
************************************************************************************************************/	
/*
 * CONTROL OVER THE SIDE BAR FOR JMOL VIEWER
 */

var standardSideBarWidth = 374;
var standardSideBarHeight = 374;

function getSideBarWidth() {
	newWidthForSidebar = standardSideBarWidth;
	var width = pageWidth();
	if(width < 1130) {
		var diff = 1230 - width;
		newWidthForSidebar = standardSideBarWidth - diff;
		if(newWidthForSidebar < 150)
			newWidthForSidebar = 150;
	}
	return newWidthForSidebar;
}

function getJmolHeight() {
	newJmolHeight = standardSideBarHeight;
	var height = pageHeight();
	if(height < 640) {
		var diff = 740 - height;
		newJmolHeight = standardSideBarHeight - diff;
		if(newJmolHeight < 100)
			newJmolHeight = 100;
	}
	return newJmolHeight;
}

function onLoadScript() {
	var width = pageWidth();
	if(width < 1130) {
		var diff = 1130 - width;
		var newWidthForSidebar = 374 - diff;
		if(newWidthForSidebar < 150)
			newWidthForSidebar = 150;
		var newFormWidthForSidebar = newWidthForSidebar -34;
		document.getElementById("rightsidebarcont").style.width=newWidthForSidebar+"px"; //d.style.width="1270px";
		document.getElementById("rightsidebarcont").style.right="-"+newWidthForSidebar+"px"; //d.style.width="1270px";
		document.getElementById("rightsidebar").style.width=newWidthForSidebar+"px";
		document.getElementById("jmolcontrol").style.width=newFormWidthForSidebar+"px";
		document.getElementById("alignmentViewer").style.width=newFormWidthForSidebar+"px";
	}

}


  /*
   * FUNCTION FOR SUBMITTING THE VARIOUS FORMS THAT MAKE THE SITE WORK
   */
  function submitMasterForm() {
	  
	  var urlString = "AjaxSearch?pdbid=" +document.masterForm.pdbid.value;
	  urlString = urlString + "&action=" + document.masterForm.action.value;
	  urlString = urlString + "&reqid=" + document.masterForm.reqid.value;
	  urlString = urlString + "&scopid=" + document.masterForm.scopid.value;
	  urlString = urlString + "&keywords=" + document.masterForm.keywords.value;
	  urlString = urlString + "&redundancy=" + document.masterForm.redundancy.value;
	  urlString = urlString + "&annot=" + document.masterForm.annot.value;
	  urlString = urlString + "&resolution=" + document.masterForm.resolution.value;
	  urlString = urlString + "&operator=" + document.masterForm.operator.value;
	  urlString = urlString + "&tech=" + document.masterForm.tech.value;
	  urlString = urlString + "&regex=" + document.masterForm.regex.value;
	  urlString = urlString + "&interactors=" + document.masterForm.interactors.value;
	  urlString = urlString + "&dist=" + document.masterForm.dist.value;
	  urlString = urlString + "&hits=" + document.masterForm.hits.value;
	  
	  theMessage = "Performing Search... <br><br>" +
	  	"<b>Do Not Worry</b> if the results do not appear immediately, the application is not broken.<br>"+
	  	"The amount of time it takes to respond depends on the complexity of your query!<br>"+
	  	"The <b>Progress Bar</b> at the top right of the page will begin once the first stage of the query is completed.<br><br>"+
	  	"Please note, if you start another query this one will be deleted.<br><br><b>"+
	  	"<b>Please be patient</b>. ";
	  GetFormResults( urlString, theMessage );
	  
	  startProgressMeter("AjaxSearch?action=progress&reqid=0&target=status");

	  document.getElementById('results_table_div').innerHTML="";
	  
	  return false;
  }
  
  function containsInvalidCharacters(inputString) {
	  for (p=0; p<inputString.length; p++) {
		  x=inputString.charAt(p);
		  if("ARNDCQEGHILKMFPSTWYVX[]BU{}()<>^0123456789shc-,.|".indexOf(x)==-1 )
			  return true;
	  }
	  return false;
  }
  
  function submitSearchForm() {

	  if(document.searchForm.regex.value=="") {
		  jAlert("Please provide a '3D Reg Ex' Pattern for your Search. If you need help please use the help option in the menu or begin with a pattern from the PROSITE libarary.", "'3D Reg Ex' Pattern Required!");  
		  //jAlert("Please provide a '3D Reg Ex' Pattern for your Search.\n If you need help please use the help option in the menu.", "'3D Reg Ex' Pattern Required!");  
		return false;
	  }
	  
	  if(document.searchForm.regex.value.length < 6 || document.searchForm.regex.value.split('-').length < 3) {
		  jAlert("We are sorry, but the query you have entered is so unspecific that it will take too much of our computing resources to calculate the matching structures. We suggest you try to be more specific.", "Be More Specific!");  
		  return false;
	  }
	  
	  if( containsInvalidCharacters(document.searchForm.regex.value) ) {
		  jAlert("We are sorry, but the query you have entered contains invalid characters. Please check the help page for information about the syntax", "Invalid Character!");  
		  return false;
	  }
	  
	  document.masterForm.regex.value = document.searchForm.regex.value;
	  //document.masterForm.pdbid.value = document.searchForm.pdbid.value;
	  //document.masterForm.scopid.value = document.searchForm.scopid.value;
	  document.masterForm.action.value = "search";
	  document.masterForm.keywords.value = document.searchForm.keywords.value;
	  document.masterForm.redundancy.value = document.searchForm.redundancy.value;
	  document.masterForm.annot.value = document.searchForm.annot.value;
	  document.masterForm.operator.value = document.searchForm.operator.value;
	  document.masterForm.resolution.value = document.searchForm.resolution.value;
	  document.masterForm.tech.value = document.searchForm.tech.value;
	  document.masterForm.hits.value = document.searchForm.hits.value;
	  document.masterForm.dist.value = document.searchForm.dist.value;
	  return submitMasterForm();
  }
