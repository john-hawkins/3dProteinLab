<form id='search' method="post" action="" NAME="searchForm" >
<div class='hiddenFields'>
</div>

<div class="clear">
	<label for="regex" class="" title='3d Regular Expression Descriptor (See Help)'>3D Reg Ex</label>
	<input type="text" name="regex" size="65" id="filter-regex">
	<!-- 
	<a href='#' title='Load the Example Pattern: Class I PDZ Binding Site' onclick="document.forms['search'].regex.value='cG-[LF]-G-[LFI]-[SN]-sI-<8,10>-h[LI]-[KR]'">example</a>
	 -->
</div>

<div class="clear">
	<label for="keywords" class=""  title='PDB File Keywords'>Keywords</label>
	<input type="text" name="keywords" size="40" id="search-keywords">

	<label for="annot" class=""  title='PDB Functionally Annotated'> &nbsp;&nbsp; Function</label>
	<select name='annot' id="search-annot">
		<option value='ALL'>ALL</option>
		<option value='KNOWN'>KNOWN</option>
		<option value='UNKNOWN'>UNKNOWN</option>
	</select>

	
</div>

<!-- 
<div class="clear">	
	<label for="scopid" class="">SCOP ID</label>
	<input type="text" name="scopid" size="8" id="search-scopid">
	<label for="blank" class=""></label>
	<label for="pdbid" class="">PDB ID</label>
	<input type="text" name="pdbid" size="4" id="search-pdbid">
	<label for="blank" class=""></label>
</div>
 -->
 
 <div class="clear">
 
	<label for="redundancy" class="" title='Dataset to search'> &nbsp;Dataset</label>
	<select name='redundancy' id="search-redundancy">
		<option value='0'>Whole PDB</option>
		<option value='100'>PDB-100</option>
		<option value='95' SELECTED>PDB-95</option>
		<option value='90'>PDB-90</option>
		<option value='70'>PDB-70</option>
		<option value='50'>PDB-50</option>
		<option value='40'>PDB-40</option>
		<option value='30'>PDB-30</option>
		<option value='10'>10</option>
	</select>

	<label for="hits" class=""  title='Extract Multiple Hits Per Structure'>Multiples</label>
	<select name='hits' id="search-hits">
		<option value='False'>False</option>
		<option value='True'>True</option>
	</select>
	
	<label for="dist" class=""  title='Distances Measured Between'>Distances</label>
	<select name='dist' id="search-dist">
		<option value='Alpha'>Alpha-Carbons</option>
		<option value='Pseudo'>Pseudo-Points</option>
	</select>

</div>
 
 
<div class="clear">

	<label for="tech" class=""  title='Structure Resolution Technology'>Technology</label>
	<select name='tech' id="search-tech">
		<option value='ALL'>ALL</option>
		<option value='X-RAY DIFFRACTION'>X-RAY DIFFRACTION</option>
		<option value='SOLUTION NMR'>SOLUTION NMR</option>
		<option value='ELECTRON MICROSCOPY'>ELECTRON MICROSCOPY</option>
		<option value='ELECTRON CRYSTALLOGRAPHY'>ELECTRON CRYSTALLOGRAPHY</option>
		<option value='MODEL'>MODEL</option>
	</select>
	
	<label for="resolution" class="" title='Structure Resolution Threshold'> &nbsp;Resolution</label>
	<select name='operator' id="search-operator">
		<option value='GT'>&gt;</option>
		<option value='GTE'>&gt;=</option>
		<option value='EQ'>=</option>
		<option value='LTE'>&lt;=</option>
		<option value='LT'>&lt;</option>
	</select>
	<input type="text" name="resolution" size="4" id="search-resolution"><b>&#x212b;</b>
	
	<input type="button" name="submit" value="Search" class="submit button" onclick="submitSearchForm();return false;">

</div>

<div class="clear">
</div>

</form> 