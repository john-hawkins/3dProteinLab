<h3>Help Page</h3>

Use our search form to make a query over the PDB Database for patterns of sequence and structure.
Our pattern syntax is designed to be a direct extension of the PROSITE Pattern syntax. You can query
using any PROSITE pattern, or you can extend that pattern to describe structural aspects of the 
protein. The syntax of these sequence and structural aspects is described below.
<br>
<br>
<b>Pattern Syntax</b>
<br>
Each structural pattern is composed of a series of sequence (SEQ) elements and connection (CON) components,
separated by a hyphens. e.g. SEQ1 - SEQ2 - SEQ3 - CON1 - SEQ4 - SEQ5 - CON2 - SEQ6
<br><br>
<b>SEQ Elements SYNTAX</b>
<br>
<br>
	<!-- 
	<TABLE CLASS="help">
		<TR> <TH CLASS="help">Element</TH>  <TH CLASS="help">Usage</TH> </TR>
     	<TR> <TD CLASS="help" nowrap>x</TD> 		<TD CLASS="help">Match any single residue<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>A</TD> 		<TD CLASS="help">(uppercase letter) A specific residue e.g. A for Alanine<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>B or U</TD> 	<TD CLASS="help">Cysteines: In the IUPAC code C is for Cysteine, we use two additional codes B and U to represent Cysteines in disulfide bonds and in a reduced state (respectively)<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>[AS]</TD> 		<TD CLASS="help">A set of allowed residues,  e.g. Alanine OR Serine<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>{AS}</TD> 		<TD CLASS="help">A set of NOT allowed residues, e.g. NOT Alanine OR Serine<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>s</TD> 		<TD CLASS="help">Secondary Structure (lowercase letter), s-<i>sheet</i> h-<i>helix</i>, c-<i>coil</i><TD> </TR>
     	<TR> <TD CLASS="help" nowrap>hA</TD> 		<TD CLASS="help">Specific residue in secondary structure, e.g. Alanine in <i>helix</i><TD> </TR>
     	<TR> <TD CLASS="help" nowrap>h[AS]</TD> 	<TD CLASS="help">Set of allowed residues in Secondary Structure, e.g. Alanine OR Serine in <i>helix</i><TD> </TR>	
     	<TR> <TD CLASS="help" nowrap>&lt;A</TD> 	<TD CLASS="help">Pattern Anchored to N-terminus (Can only be used in the first SEQ pattern)<TD> </TR>	
     	<TR> <TD CLASS="help" nowrap>A&gt;</TD> 	<TD CLASS="help">Pattern Anchored to C-terminus (Can only be used in the last SEQ pattern)<TD> </TR>
	</TABLE> -->
	
<TABLE CLASS="help">
		<TR> <TH CLASS="help">Element</TH>  <TH CLASS="help">Example</TH> <TH CLASS="help">Usage</TH> </TR>
     	<TR> <TD CLASS="help" nowrap>X</TD> <TD CLASS="help" nowrap>X</TD> <TD CLASS="help">Match any single residue<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Uppercase letter</TD>  <TD CLASS="help" nowrap>A</TD> 		<TD CLASS="help">A specific residue e.g. A for Alanine<TD> </TR>
     	
     	<TR> <TD CLASS="help" nowrap>Uppercase B or U</TD> 	<TD CLASS="help" nowrap>B</TD> 		<TD CLASS="help">Cysteines in disulfide bonds and in a reduced state, respectively.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Square Brackets</TD>  	<TD CLASS="help" nowrap>[AS]</TD> 	<TD CLASS="help">A set of allowed residues,  e.g. Alanine OR Serine<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Curly braces</TD>  	<TD CLASS="help" nowrap>{AS}</TD> 	<TD CLASS="help">A set of NOT allowed residues, e.g. NOT Alanine OR Serine<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Lowercase letter</TD>  <TD CLASS="help" nowrap>s</TD> 		<TD CLASS="help">Secondary Structure, s-<i>sheet</i> h-<i>helix</i>, c-<i>coil</i><TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>  				<TD CLASS="help" nowrap>hA</TD> 	<TD CLASS="help">Specific residue in secondary structure, e.g. Alanine in <i>helix</i><TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>  				<TD CLASS="help" nowrap>h[AS]</TD> 	<TD CLASS="help">Set of allowed residues in Secondary Structure, e.g. Alanine OR Serine in <i>helix</i><TD> </TR>	
     	<TR> <TD CLASS="help" nowrap>Less than</TD>  		<TD CLASS="help" nowrap>&lt;A</TD> 	<TD CLASS="help">Pattern Anchored to N-terminus (Only valid in the first SEQ pattern) <TD> </TR>	
     	<TR> <TD CLASS="help" nowrap>Greater than</TD>  	<TD CLASS="help" nowrap>A&gt;</TD> 	<TD CLASS="help">Pattern Anchored to C-terminus (Only valid in the last SEQ pattern)<TD> </TR>
</TABLE>

<b>CON Elements SYNTAX</b>
<br>
<br>
	<TABLE CLASS="help">
		<TR> <TH CLASS="help" nowrap>Element</TH> <TH CLASS="help">Example</TH>  <TH CLASS="help">Usage</TH> </TR>
     	<TR> <TD CLASS="help" nowrap>Underscore	</TD>		<TD CLASS="help" nowrap>&lt;_&gt;</TD> 		<TD CLASS="help">NULL connector, allow SEQ patterns to be anywhere.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Numeric Range</TD>		<TD CLASS="help" nowrap>&lt;2,3&gt;</TD> 		<TD CLASS="help">Set distance range. Distance d in Angstroms between SEQ patterns, e.g. 2&lt;d&lt;3<TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>					<TD CLASS="help" nowrap></TD> 					<TD CLASS="help">This expression places no restrictions on which end-to-end matches the distance.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Uppercase C or N</TD>	<TD CLASS="help" nowrap>&lt;C2,3N&gt;</TD> 	<TD CLASS="help">End to end distance d between SEQ patterns in angstroms.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>					<TD CLASS="help" nowrap></TD> 					<TD CLASS="help">e.g. The C terminal end of the preceding SEQ is  2&lt;d&lt;3<TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>					<TD CLASS="help" nowrap></TD> 					<TD CLASS="help">from the N terminal end of the following SEQ pattern.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap>Caret</TD>				<TD CLASS="help" nowrap>&lt;^10,30&gt;</TD> 	<TD CLASS="help">Angle a in degrees between SEQ patterns, e.g. 10&lt;a&lt;30<TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>					<TD CLASS="help" nowrap>&lt;3,5|^10,30&gt;</TD> <TD CLASS="help">Combined connector with distance and angle thresholds.<TD> </TR>
     	<TR> <TD CLASS="help" nowrap></TD>					<TD CLASS="help" nowrap></TD> 					<TD CLASS="help">Multiple connector criteria separated by a pipe symbol.<TD> </TR>
	</TABLE>
