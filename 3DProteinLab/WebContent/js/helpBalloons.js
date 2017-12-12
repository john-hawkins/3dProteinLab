
	// -- Begin X Y GRABBER
	var IE = document.all?true:false;
	if (!IE) document.captureEvents(Event.MOUSEMOVE)
	document.onmousemove = getMouseXY;
	var tempX = 0;
	var tempY = 0;
	function getMouseXY(e) {
		if (IE) { // grab the x-y pos.s if browser is IE
			tempX = event.clientX + document.body.scrollLeft;
			tempY = event.clientY + document.body.scrollTop;
		} else {  // grab the x-y pos.s if browser is NS
			tempX = e.pageX;
			tempY = e.pageY;
		}  
		if (tempX < 0){tempX = 0;}
		if (tempY < 0){tempY = 0;}  
		return true;
	}
	//  End -->

	function displayHelpBalloon(id,toggle,displaytext) {
		if (toggle == "on") {
			var theDiv = document.getElementById(id);
			document.getElementById(id).innerHTML = displaytext;
			document.getElementById(id).style.width = (displaytext.length * 8 ) + 'px';
			document.getElementById(id).style.top=  tempY - 20 + 'px';
			document.getElementById(id).style.left= tempX - 20 + 'px';
			//alert("COORDS: " + tempX + " " + tempY);
			document.getElementById(id).style.display = "inline";
		}
		else {
			document.getElementById(id).style.display = "none";
		}
	}
