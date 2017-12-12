<script type="text/javascript">

		var loaded = "NULL";
		var rowIndex = 0;
		var inALN = new Array();
		var rotation = "OFF";
		var animation = "OFF";
		
		function unloadPDB() {  

			//alert("UNLOADING: " + loaded );
			if(loaded=="NULL") {
			} else {
				jmolScript("zap; background [xFFFFFF]", 'all');
				document.getElementById('jmolmessages').innerHTML=" ";
				var theTable = document.getElementById('referenceSeq');
				theTable.deleteRow(0);
				document.getElementById(loaded).src='images/box.png';
				loaded="NULL";
			}
		} 
		
		function loadPDB(pdbid) {  
			unloadPDB();
			var script = "load 'FetchPDB?pdbid=" + pdbid + "'; background [x000000]; wireframe off; spacefill off; ribbon ON; color TRANSLUCENT";
			jmolScript(script, 'all');
			document.getElementById('jmolmessages').innerHTML="PDB: " + pdbid;
			loaded=pdbid;
		}  

		function applyUserPreferences() {  
			var script = "animation mode loop; animation " + animation + "; spin " + rotation;
			jmolScript(script, 'all');
		}  

		function toggleRotation(value) {
			rotation = value;
			applyUserPreferences()
		}  
		
		function toggleAnimation(value) {  
			animation = value;
			applyUserPreferences();
		}      
			  
		function highlightResdiues(values) {  
				var script = "select " + values + "; wireframe 20; spacefill 40; color [xFF3333]";
				jmolScript(script, 'all');
		} 

		function loadAndHighlight(pdbid, values, matchID, residues, targetImg) {  

				//alert("You want to LOAD: " + matchID + " currently " + loaded );
				unloadPDB();
				var script = "load 'FetchPDB?pdbid=" + pdbid + "'; background [xFFFFFF]; wireframe off; spacefill off; ribbon ON; color TRANSLUCENT";
				if(values!="") {
					var script2 = "select " + values + "; wireframe 20; spacefill 40; color [xFF3333]; center " + values;
					script = script + ";" + script2;
				}
				jmolScript(script, 'all');
				applyUserPreferences();
				
				document.getElementById('jmolmessages').innerHTML="PDB: " + pdbid;
				document.getElementById(targetImg).src='images/redfill_box.png';
				
				var theTable = document.getElementById('referenceSeq');
				var newRow = theTable.insertRow(0);
				var theCell = newRow.insertCell(0);
				theCell.innerHTML = matchID;
				theCell.width = 80;
				theCell.setAttribute('class', 'referenceSeq');
				var theCell2 = newRow.insertCell(1);
				theCell2.innerHTML = residues;
				theCell2.setAttribute('class', 'referenceSeq');
				var theCell3 = newRow.insertCell(2);
				var func = "unloadPDB()";
				var deleteButton = "<img src='images/delete.png' onclick=\""+func+"\" title='Remove from Jmol' >";
				theCell3.innerHTML = deleteButton;
				theCell3.align="right";
				theCell3.setAttribute('class', 'referenceSeq');

				loaded=targetImg;
		} 

		function toggleALN(pdbid, values, targetImg) { 
				if( inALN[pdbid] == undefined || inALN[pdbid] == "OFF" ) {
					addToALN(pdbid, values, targetImg);
				} else {
					var theIndex = inALN[pdbid];
					var theTable = document.getElementById('alignmentSeq');
					theTable.deleteRow(theIndex);
					// Iterate over all rows in the index array and 
					// decrement if they are greater than the current position
					for( var i in inALN ) {
						var value = inALN[i];
						if(value > theIndex) {
							inALN[i] = (value-1);
						}
					}
					inALN[pdbid] = "OFF";
					document.getElementById(targetImg).src='images/box.png';
					// OIIIII
					rowIndex--;
				}
		}
			
		function addToALN(pdbid, values, targetImg) {  
				document.getElementById(targetImg).src='images/bluefill_box.png';
				var theTable = document.getElementById('alignmentSeq');
				var newRow = theTable.insertRow(rowIndex);
				var theCell = newRow.insertCell(0);
				theCell.innerHTML = pdbid;
				theCell.width = 80;
				theCell.setAttribute('class', 'alignmentSeq');
				var theCell2 = newRow.insertCell(1);
				theCell2.innerHTML = values;
				theCell2.setAttribute('class', 'alignmentSeq');
				var theCell3 = newRow.insertCell(2);
				var func = "toggleALN('"+pdbid+"', '"+values+"', '"+targetImg+"')";
				var deleteButton = "<img src='images/delete.png' onclick=\""+func+"\" title='Remove from Alignment'>";
				theCell3.innerHTML = deleteButton;
				theCell3.align="right";
				theCell3.setAttribute('class', 'alignmentSeq');
				
				inALN[pdbid] =  rowIndex;
				rowIndex++;
		} 
</script>

<form id='jmolcontrol' method="post" action="" NAME="jmolcontrolForm" >

	<div class='hiddenFields'></div>

	<div class="clear">
			<label for="rotate" class='long'>Rotation</label>
			<select name="rotate">
        		<option onclick="toggleRotation('OFF');">Off</option>
        		<option onclick="toggleRotation('ON');">On</option>
  			</select>
	</div>
	<div class="clear">
			<label for="animate" class='long'>Animate NMR</label>
			<select name="animate">
        		<option onclick="toggleAnimation('OFF');">Off</option>
        		<option onclick="toggleAnimation('ON');">On</option>
  			</select>
	</div>
	<!--  
	<div class="clear">
			<label for="load" class="">Load </label>
			<select name="load">
        		<option onclick="loadPDB('2PDZ');">2PDZ</option>
        		<option onclick="loadPDB('2GA5');">2GA5</option>
  			</select>
	</div>
	<div class="clear">
			<label for="highlite" class="">Highlite </label>
			<select name="highlite">
        		<option onclick="highlightResdiues('50:A');">50:A</option>
        		<option onclick="highlightResdiues('40:A OR 41:A');">40:A OR 41:A</option>
  			</select>
	</div>
	-->
</form> 