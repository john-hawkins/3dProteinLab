/************************************************************************************************************
LOAD GOOGLE VISUALIZATION API EXAMPLE RESULTS
************************************************************************************************************/	

google.load('visualization', '1', {packages:['table']});
google.setOnLoadCallback(drawTable);

/*
function drawTable() {
	        var data = new google.visualization.DataTable();

			data.addColumn('string', 'VIEW');
			data.addColumn('string', 'PDB');
			data.addColumn('string', 'Match');
			data.addColumn('string', 'SCOP');
			data.addColumn('string', 'CATH');
			data.addColumn('string', 'Pfam');
			data.addColumn('string', 'Keywords');
			data.addColumn('string', ' ');
			
	        data.addRows(2);
	        data.setCell(0, 0, '');
	        data.setCell(0, 1, '');
	        data.setCell(0, 2, '');
	        data.setCell(0, 3, '');
	        data.setCell(0, 4, '');
	        data.setCell(0, 5, '');
	        data.setCell(0, 6, '');
	        data.setCell(0, 7, '');
	        data.setCell(1, 0, '');
	        data.setCell(1, 1, '');
	        data.setCell(1, 2, '');
	        data.setCell(1, 3, '');
	        data.setCell(1, 4, '');
	        data.setCell(1, 5, '');
	        data.setCell(1, 6, '');
	        data.setCell(1, 7, '');
	        
	        var table = new google.visualization.Table(document.getElementById('results_table_div'));
	        table.draw(data, {allowHtml: true});
}*/