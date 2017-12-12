x  /*
   * A generic function to return the XML HTTP object
   */
  function CreateXmlHttpObject() { 
	var xmlhttp=false;	
	try {
		xmlhttp=new XMLHttpRequest();//creates a new ajax object
	} catch(e) {		
		try {			
			xmlhttp= new ActiveXObject("Microsoft.XMLHTTP");//this is for IE browser
		} catch(e2) {
			try {
				req = new ActiveXObject("Msxml2.XMLHTTP");//this is for IE browser
			} catch(e3) {
				xmlhttp=false;//error creating object
			}
		}
	}
		 	
	return xmlhttp;
  }
	
  
  /*
   * A function for processing the results of an AJAX call
   * 1) Parse out the script elements into an array
   * 2) Place the content on the page 
   * 3) Execute the script elements
   */
  function processContentAndExecuteScript(_source, targetElement) {
  		var source = _source;
  		var scripts = new Array();
  		
  		// Strip out tags
  		while(source.indexOf("<script") > -1 || source.indexOf("</script") > -1) {
  			var s = source.indexOf("<script");
  			var s_e = source.indexOf(">", s);
  			var e = source.indexOf("</script", s);
  			var e_e = source.indexOf(">", e);
  			
  			// Add to scripts array
  			scripts.push(source.substring(s_e+1, e));
  			// Strip from source
  			source = source.substring(0, s) + source.substring(e_e+1);
  		}
  		// Now place the content into the target elements

  		document.getElementById(targetElement).innerHTML=source;
  		
  		// Loop through every script collected and eval it
  		for(var i=0; i<scripts.length; i++) {
  			try {
  				eval(scripts[i]);
  			}
  			catch(ex) {
  				// do what you want here when a script fails
  			}
  		}
  		
  		// Return the cleaned source
  		// return source;
  }
    
  /*
   * Make an AJAX call to the specified URL and place the results in the 'Results' div
   */
  function GetFormResults(strURL, tempMessage)     {   

		var req = CreateXmlHttpObject(); // function to get xmlhttp object
	    if (req) {
			req.onreadystatechange = function() {
	      		if (req.readyState == 4) { //data is retrieved from server
	       			if (req.status == 200) { // which represents ok status 
	       				processContentAndExecuteScript(req.responseText, 'results');
	       			} else { 
	         			alert("There was a problem with GetFormResults: " + strURL);
	         			stopProgressMeter();
	      			}
	      		}            
	      	}        
			document.getElementById('results').innerHTML=tempMessage;//Clear out previous results
	    		req.open("GET", strURL, true); // Open url using get method
	    		req.send(null); // Send the results
	    	}
  }
  
  /*
   * Make an AJAX call to the specified URL and place the results in the 'Message' div
   */
  function GetFormMessage(strURL)     {         
		var req = CreateXmlHttpObject(); // function to get xmlhttp object
	     	if (req) {
			req.onreadystatechange = function() {
	      			if (req.readyState == 4) { //data is retrieved from server
	       				if (req.status == 200) { // which represents ok status                    
	         				document.getElementById('messages').innerHTML=req.responseText;//put the results of the requests in or element
	      				} else { 
	         				alert("There was with GetFormMessage: " + strURL);
	         				stopProgressMeter();
	      				}
	      			}            
	      		}        
	    		req.open("GET", strURL, true); //open url using get method
	    		req.send(null);//send the results
		}
  }
  
  /*
   * Make an AJAX call to the specified URL and place the results in the 'targetElement' div
   */
  function ajaxRequest(strURL, targetElement)     {         
		var req = CreateXmlHttpObject(); // function to get xmlhttp object
	     	if (req) {
			req.onreadystatechange = function() {
	      		if (req.readyState == 4) { //data is retrieved from server
	       			if (req.status == 200) { // which represents ok status     
	       					//alert("RESULTS: " + req.responseText );
	         				document.getElementById(targetElement).innerHTML=req.responseText;
	      			} else { 
	         				alert("There was a problem with ajaxRequest: " + strURL);
	         				stopProgressMeter();
	      			}
	      		}            
	      	}        
	    	req.open("GET", strURL, true); //open url using get method
	    	req.send(null);//send the results
		}
  }
  
  
 /*
  * PROGRESS METER CONTROL FUNCTIONS
  */
  var intervalID; // interval ID
  var progressUpdateURL; // This will be set before each request
  
  // Initiate and stop the AJAX polling for updating the progress
  function startProgressMeter(strURL) {
	  progressUpdateURL = strURL;
	  var progress = document.getElementById('progressBar');
	  progress.innerHTML="";
	  progress.style.width = '0%';
	  intervalID = window.setInterval('updateProgressMeter()',1000); 
	  $("#status").fadeToggle("slow");
  }
  
  function stopProgressMeter()  { 
	  window.clearInterval(intervalID); 
	  $("#status").fadeToggle("slow");
  }

  // Make an AJAX call to the server and update the progress meter
  function updateProgressMeter()     {    
  		var req = CreateXmlHttpObject(); // function to get xmlhttp object
  	     	if (req) {
  			req.onreadystatechange = function() {
  	      			if (req.readyState == 4) { //data is retrieved from server
  	       				if (req.status == 200) { // which represents ok status 
  	       					// get progress from the XML node and set progress bar width and innerHTML
  	       					var progress = document.getElementById('progressBar');
  	       					var level=req.responseXML.getElementsByTagName('PROGRESS')[0].firstChild;
  	       					progress.style.width = level.nodeValue + '%';
  	       					if(level.nodeValue==100) {
  	       						stopProgressMeter();
  	       					}
  	      				} else { 
  	         				alert("There was a problem while updating the progress meter");
  	      				}
  	      			}            
  	      		}        
  	    	req.open("GET", progressUpdateURL, true); //open url using get method
  	    	req.send(null);//send the results
  		}
  }