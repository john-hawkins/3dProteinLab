package structools.structure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.io.PDBFileReader;

//import biostructure.entities.SSE;


public class PDBFile {
	private String pdbId;
	private String title;
	private String keywords;
	private String text;
	private double resolution;
	private String technology;
	private String added = "1970-01-01";
	private String modified = "1970-01-01";
	
	public PDBFile(String pdbId, String theTitle, String keywords, String theText, double resolution, String tech, String added, String modified) {
		super();
		this.pdbId = pdbId;
		this.title = theTitle;
		this.keywords = keywords;
		this.text = theText;
		this.resolution = resolution;
		this.technology = tech;
		this.added = added;
		this.modified = modified;
	}
	
	public PDBFile(String pdbId, String keywords, double resolution) {
		super();
		this.pdbId = pdbId;
		this.keywords = keywords;
		this.resolution = resolution;
	}

	public PDBFile() {
	}
	

	public String getPdbId() {
		return pdbId;
	}
	public void setPdbId(String pdbId) {
		this.pdbId = pdbId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public String getAdded() {
		return added;
	}

	public void setAdded(String added) {
		this.added = added;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public static Protein readProteinChainFromFile(String pdbFile, String chainId) {
		PDBFileReader pdbreader = new PDBFileReader();
		// the following parameters are optional: 
		// the parser can read the secondary structure
		// assignment from the PDB file header and add it to the amino acids
		//pdbreader.setParseSecStruc(true);
		// align the SEQRES and ATOM records, default = true   
		// slows the parsing speed slightly down, so if speed matters turn it off.
		//pdbreader.setAlignSeqRes(true);
		// parse the C-alpha atoms only, default = false
		//pdbreader.setParseCAOnly(false);
		// download missing PDB files automatically from EBI ftp server, default = false
		//pdbreader.setAutoFetch(false);

		String pdbName = pdbFile.substring(0, pdbFile.length()-4);
		int indexOfSlash = pdbName.indexOf('/');
		while(indexOfSlash > 0) {
			pdbName = pdbName.substring(indexOfSlash + 1);
			indexOfSlash = pdbName.indexOf('/');
		}
		
		try {
			  if(chainId == null) {
				Chain c = pdbreader.getStructure(pdbFile).getChain(0);
		    	  	return new Protein(c, pdbName);
			  } else {
				Chain c = pdbreader.getStructure(pdbFile).getChainByPDB(chainId);
	    		  	return new Protein(c, pdbName);
			  }

	      } catch (Exception e) {
	    	  System.err.print("Problem reading the PDB dataset file: " + pdbFile + "\n" + e);
	    	  e.printStackTrace();
			  System.exit(3);
	      }
	      return null;
	}

	/*
	public static String writePDBwithSSEOnly(  String outputFileName, ArrayList<SSE> ssEs, StringBuffer splitedPdbInfo,  boolean ignoreNonSSEresidues ) {
		
		String[] lines = splitedPdbInfo.toString().split("\n");
			
		HashMap<Integer, String> codes = new HashMap<Integer, String>();
		int max = 0;
		for(int i=0; i<lines.length; i++) {
			if(lines[i].length()>3 && lines[i].substring(0,4).equals("ATOM")) { 
				int pos =  Integer.parseInt( lines[i].substring(22, 26).trim() );
				String code = lines[i].substring(17, 20).trim();	
				codes.put(new Integer(pos), code);
				if(pos > max)
					max = pos;
			}
		}
		
		String helixInfo = "";
		String sheetInfo = "";
		
		int helixIndex = 0;
		int sheetIndex = 0;
		
		HashSet<Integer> ssePositions = new HashSet<Integer>();
		
		for (SSE see : ssEs) {
			int start =  see.getBegin();
			String startPos = "" + start;
			if(start<10) startPos = " " + startPos;
			if(start<100) startPos = " " + startPos;
			if(start<1000) startPos = " " + startPos;
			int end = see.getEnd();
			String endPos = "" + end;
			if(end<10) endPos = " " + endPos;
			if(end<100) endPos = " " + endPos;
			if(end<1000) endPos = " " + endPos;
			int length = end - start + 1;

			//System.err.println("Looking for pos " + start + " and " + end + " in the set of codes ");
			String startCode = codes.get(start);
			String endCode = codes.get(end);	
			//System.err.println(" codes" + startCode + " and " + endCode + " ");
			
			for(int i=start;i<=end; i++)
				ssePositions.add(i);
			
			if (see.getSseElement().equals("H")) {
				helixIndex++;
				String hxNum = "" + helixIndex;
				if(helixIndex<10) hxNum = " " + hxNum;
				if(helixIndex<100) hxNum = " " + hxNum;
				helixInfo = helixInfo + "HELIX  " + hxNum + " " + hxNum + " "+ startCode +" " + see.getChain() + " ";
				helixInfo = helixInfo + startPos + "  "+ endCode +" " + see.getChain() + " " + endPos + "  1";
				helixInfo = helixInfo + "                                  " + length + "\n";
			}
			if (see.getSseElement().equals("E")) {
				sheetIndex++;
				String sheetID = "" +  sheetIndex;
				if(sheetIndex<10) sheetID = "A" + sheetID;
				sheetInfo = sheetInfo + "SHEET    1  " + sheetID + " 1 "+startCode +" " + see.getChain() ;
				sheetInfo = sheetInfo + startPos + "  "+ endCode +" " + see.getChain() + endPos + "  0";
				sheetInfo = sheetInfo + "                                        \n";	
			}
		}
		
		//String pdbcontent = "";
		//String[] temp = splitedPdbInfo.toString().split("\n");
		//for(int i=0; i<temp.length-1; i++) {
		//		int num = Integer.parseInt( temp[i].substring(22, 26).trim() );
		//		if(ssePositions.contains(num)) {
		//			pdbcontent = pdbcontent + temp[i] + "\n";
		//		}
		//}
		//pdbcontent = pdbcontent + "TER";
		
		String pdbcontent = splitedPdbInfo.toString();
		if( ignoreNonSSEresidues ) {
			pdbcontent = "";
			String[] temp = splitedPdbInfo.toString().split("\n");
			for(int i=0; i<temp.length-1; i++) {
				int num = Integer.parseInt( temp[i].substring(22, 26).trim() );
				if(ssePositions.contains(num)) {
					pdbcontent = pdbcontent + temp[i] + "\n";
				}
			}
			pdbcontent = pdbcontent + "TER";
		} 
		
		writeFile(outputFileName, helixInfo + sheetInfo + pdbcontent);
		return outputFileName;
	}
*/	

	/**
	 * Giving the right param it creates a file
	 * @param fileName (the name of the file you want with the correct PATH)
	 * @param information (all the file content in a String)
	 */
	public static void writeFile(String fileName, String information){
		try {		// e37. Writing to a File
	        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
	        out.write(information);
	        out.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
}
