package structools.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.StructureException;

public class Residue {

	protected char residueType;
	protected int position;
	protected String sse;
	protected double[] coords;
	protected Atom alphaCarbon;

	public static String alphabet = "ARNDCQEGHILKMFPSTWYVX";

    //D Distance between C-alpha and centroid of side chain (Levitt, 1976)
	protected static double[] sideChainHalfLengths = new double[]{0.77, 3.72, 1.98, 1.99, 1.38, 2.58, 2.63, 0.00, 2.76, 1.83, 2.08, 2.94, 2.34, 2.97, 1.42, 1.28, 1.43, 3.58, 3.36, 1.49};

	public static double getSideChainHalfLength(char res) {
		int index = Residue.getResidueIndex(res);
		
		if(index != 21 && index != -1) {
			return sideChainHalfLengths[index];
		} else {
			return 0.0;
		}
	}
	public double getSideChainHalfLength() {
		return Residue.getSideChainHalfLength(this.residueType);
	}
	
	public Residue(char a, int pos, String s) {
		this.position = pos;
		this.residueType = a;
		this.sse = s;
	}
	
	public Residue(AminoAcid a, int pos) {
		position = pos;
		try{
			this.alphaCarbon = a.getCA();
			this.residueType = a.getAminoType().charValue();
			this.coords = this.alphaCarbon.getCoords();
			Object[] stuff = a.getSecStruc().values().toArray();
			if(stuff.length > 0)
				sse = (String) stuff[0];
			else
				sse = "COIL";
		} catch(Exception e) {
			System.err.println("Error Parsing Amino Acid ! " + a);
			System.exit(3);
		}
	}
	
	public Residue(AminoAcid a) {
		try{
			this.alphaCarbon = a.getCA();
			this.residueType = a.getAminoType().charValue();
			this.coords = this.alphaCarbon.getCoords();
			//this.position = Integer.parseInt( a.getPDBCode() );
			this.position = a.getResidueNumber().getSeqNum();
			Object[] stuff = a.getSecStruc().values().toArray();
			if(stuff.length > 0)
				sse = (String) stuff[0];
			else
				sse = "COIL";
		} catch(Exception e) {
			System.err.println("Error Parsing Amino Acid ! " + a);
			System.exit(3);
		}
	}
	
	// TO BE DELETED
	public void setProperties(AminoAcid a) {
		try{
			this.alphaCarbon = a.getCA();
			this.residueType = a.getAminoType().charValue();
			this.coords = this.alphaCarbon.getCoords();
			//this.position = Integer.parseInt(a.getPDBCode());
			Object[] stuff = a.getSecStruc().values().toArray();
			if(stuff.length > 0)
				sse = (String) stuff[0];
			else
				sse = "COIL";
		} catch(Exception e) {
			System.err.println("Error Parsing Amino Acid ! " + a);
			System.exit(3);
		}
	}
	
	public void setProperties(Residue aa) {
		//this.alphaCarbon = aa.getCA();
		this.residueType = aa.getResidueType();
		this.coords = aa.getCoords();
		this.position = aa.getPosition();
		this.sse = aa.getSse();
	}
	
	public char getResidueType() {
		return residueType;
	}

	public int getPosition() {
		return position;
	}

	public String getSse() {
		return sse;
	}

	public char getSseOneLetterCode() {
		return sse.charAt(0);
	}
	
	public void setSse(String sse) {
		this.sse = sse;
	}

	public double getX() {
		return coords[0];
	}

	public double getY() {
		return coords[1];
	}

	public double getZ() {
		return coords[2];
	}

	public void setX(double x) {
		coords[0] = x;
	}

	public void setY(double y) {
		coords[1] = y;
	}

	public void setZ(double z) {
		coords[2] = z;
	}

	public double[] getCoords() {
		return coords;
	}

	public void setCoords(double[] coords) {
		this.coords = coords;
	}

	public Atom getAtom() {
		return alphaCarbon;
	}

	public void setAtom(Atom atom) {
		this.alphaCarbon = atom;
	}

	public Residue() {
	}
	
	public static String getSSESequence(Chain c) {
		String sse = "";
		//System.out.println(" Sec Structure !!!");
		List<Group> groups = c.getAtomGroups( org.biojava.nbio.structure.GroupType.AMINOACID );
		for(int i=0; i<groups.size(); i++) {
			AminoAcid a = (AminoAcid) groups.get(i);
			Object[] stuff = a.getSecStruc().values().toArray();
			if(stuff.length > 0) {
				//System.out.print( "-" + ((String) stuff[0]).charAt(0) );
				sse = sse + ((String) stuff[0]).charAt(0);
			} else {
				sse = sse + "C";
				//System.out.print( "-C" );
			}
		}
		//System.out.println();
		return sse;
	}
	
	public static char getSSE(AminoAcid a) {
		Object[] stuff = a.getSecStruc().values().toArray();
		if(stuff.length > 0) {
			return ((String) stuff[0]).charAt(0);
		} else {
			return 'C';
		}
	}
	
	public double calcDistFromPoint(double[] p){
		try {
			return Math.sqrt( Math.pow(p[0]-this.coords[0], 2) + Math.pow(p[1]-this.coords[1], 2) + Math.pow(p[2]-this.coords[2], 2) );
		} catch(Exception e) {
			System.err.println("Problem calculating distance");
			System.exit(0);
		}
		return 0.0;
	}
	
	public static double calcDist(double[] p1, double[] p2){
		try {
			return Math.sqrt( Math.pow(p1[0]-p2[0], 2) + Math.pow(p1[1]-p2[1], 2) + Math.pow(p1[2]-p2[2], 2) );
		} catch(Exception e) {
			System.err.println("Problem calculating distance");
			System.exit(0);
		}
		return 0.0;
	}
	
	public static double calcAlphaCaDist(Residue r1, Residue r2){
		//System.err.print("Checking distance from : " + r1.getPosition() + " to " + r2.getPosition());
		try {
			return Math.sqrt( Math.pow(r1.getX()-r2.getX(), 2) + Math.pow(r1.getY()-r2.getY(), 2) + Math.pow(r1.getZ()-r2.getZ(), 2) );
		} catch(java.lang.NullPointerException e) {
			System.err.println("Problem with Residue: " + r1.getResidueType() + ":" + r1.getPosition());
			System.err.println("     or with Residue: " + r2.getResidueType() + ":" + r2.getPosition());
			System.exit(0);
		}
		return 0.0;
	}
	
	
	public static String getLooseSubs(char r) {
		if(r=='A') {
			return "AIVLM";
		} else if(r=='C') {
			return "C";
		} else if(r=='D') {
			return "EDNSTQ";
		} else if(r=='E') {
			return "EDNSTQ";
		} else if(r=='F') {
			return "FWYIL";
		} else if(r=='G') {
			return "GAP";
		} else if(r=='H') {
			return "HKR";
		} else if(r=='I') {
			return "AIVLM";
		} else if(r=='K') {
			return "HKR";
		} else if(r=='L') {
			return "AIVLM";
		} else if(r=='M') {
			return "AIVLM";
		} else if(r=='N') {
			return "EDNSTQ";
		} else if(r=='P') {
			return "P";
		} else if(r=='Q') {
			return "EDNSTQ";
		} else if(r=='R') {
			return "HKR";
		} else if(r=='S') {
			return "EDNSTQ";
		} else if(r=='T') {
			return "EDNSTQ";
		} else if(r=='V') {
			return "AIVLM";
		} else if(r=='W') {
			return "FWYIL";
		} else if(r=='Y') {
			return "FWYIL";
		}
		return "";
	}
	
	// DEPRECATED METHOD
	public String getLooseSubs() {
		if(residueType=='A') {
			return "AIVLM";
		} else if(residueType=='C') {
			return "C";
		} else if(residueType=='D') {
			return "EDNSTQ";
		} else if(residueType=='E') {
			return "EDNSTQ";
		} else if(residueType=='F') {
			return "FWYIL";
		} else if(residueType=='G') {
			return "GAP";
		} else if(residueType=='H') {
			return "HKR";
		} else if(residueType=='I') {
			return "AIVLM";
		} else if(residueType=='K') {
			return "HKR";
		} else if(residueType=='L') {
			return "AIVLM";
		} else if(residueType=='M') {
			return "AIVLM";
		} else if(residueType=='N') {
			return "EDNSTQ";
		} else if(residueType=='P') {
			return "P";
		} else if(residueType=='Q') {
			return "EDNSTQ";
		} else if(residueType=='R') {
			return "HKR";
		} else if(residueType=='S') {
			return "EDNSTQ";
		} else if(residueType=='T') {
			return "EDNSTQ";
		} else if(residueType=='V') {
			return "AIVLM";
		} else if(residueType=='W') {
			return "FWYIL";
		} else if(residueType=='Y') {
			return "FWYIL";
		}
		return "";
	}
	
	public static String getSimpleSubs(char r) {
		if(r=='A') {
			return "AIVLM";
		} else if(r=='C') {
			return "C";
		} else if(r=='D') {
			return "DE";
		} else if(r=='E') {
			return "DE";
		} else if(r=='F') {
			return "FWY";
		} else if(r=='G') {
			return "GA";
		} else if(r=='H') {
			return "HKR";
		} else if(r=='I') {
			return "AIVLM";
		} else if(r=='K') {
			return "HKR";
		} else if(r=='L') {
			return "AIVLM";
		} else if(r=='M') {
			return "AIVLM";
		} else if(r=='N') {
			return "NSTQ";
		} else if(r=='P') {
			return "P";
		} else if(r=='Q') {
			return "NSTQ";
		} else if(r=='R') {
			return "HKR";
		} else if(r=='S') {
			return "NSTQ";
		} else if(r=='T') {
			return "NSTQ";
		} else if(r=='V') {
			return "AIVLM";
		} else if(r=='W') {
			return "FWY";
		} else if(r=='Y') {
			return "FWY";
		}
		return "";
	}

	public static String getStrictSubs(char r) {
		if(r=='A') {
			return "A";
		} else if(r=='C') {
			return "C";
		} else if(r=='D') {
			return "DE";
		} else if(r=='E') {
			return "DE";
		} else if(r=='F') {
			return "F";
		} else if(r=='G') {
			return "G";
		} else if(r=='H') {
			return "H";
		} else if(r=='I') {
			return "ILV";
		} else if(r=='K') {
			return "KR";
		} else if(r=='L') {
			return "ILV";
		} else if(r=='M') {
			return "M";
		} else if(r=='N') {
			return "NT";
		} else if(r=='P') {
			return "P";
		} else if(r=='Q') {
			return "Q";
		} else if(r=='R') {
			return "KR";
		} else if(r=='S') {
			return "S";
		} else if(r=='T') {
			return "NT";
		} else if(r=='V') {
			return "ILV";
		} else if(r=='W') {
			return "WY";
		} else if(r=='Y') {
			return "WY";
		}
		return "";
	}

	public static String getMissingSubs(String currentSubs) {
		String results = "";
		for(int a=0; a<alphabet.length()-1; a++) {
			if(!currentSubs.contains("" + alphabet.charAt(a)))
				results = results + alphabet.charAt(a);
		}
		return results;
	}
	
	
	//,"WMCFILVGRS", "EDKNQHY"
	protected static String[] superLooseLists = {"WFYMLIV", "PGCATS", "HNDEQRK", "WFYMLIVC", "EDNSTQHKR", "AGPTSHNDEQRK", "AIVLMFWYIL"};	
	
	public static String getSuperGroup(String currentSubs) {
		boolean foundAll = true;
		for(int lDex = 0; lDex<superLooseLists.length; lDex++ ) {
			foundAll = true;
			for(int c=0; c<currentSubs.length(); c++) {
				if( !superLooseLists[lDex].contains(""+currentSubs.charAt(c)) ) {
					foundAll = false;
					break;
				}
			}
			if(foundAll)
				return superLooseLists[lDex];
		}
		return null;
	}
	

	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the residues in a String
	 * Scaled to be between 0 and 1
	 */
	public static double calculateScaledCompositeInfoContent(String aligned, String alignedSSE) {
		double totalcontent =  calculateInfoContent( aligned );
		//System.out.println("Basic Info Content of '" + aligned + "' is " +  totalcontent );
		totalcontent +=  calculate6PropertyInfoContent( aligned );
		totalcontent +=  calculate8PropertyInfoContent( aligned );
		totalcontent +=  calculateSSEInfoContent( alignedSSE );
		
		double totalMax = (0 - ( Math.log(0.05) / Math.log(2) ) );
		totalMax += (0 - ( Math.log(0.166666667) / Math.log(2) ) );
		totalMax += (0 - ( Math.log(0.125) / Math.log(2) ) );
		totalMax += (0 - ( Math.log(0.3333333333) / Math.log(2) ) );
		
		return totalcontent / totalMax;
	}
	
	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the residues in a String
	 * Scaled to be between 0 and 1
	 */
	private static double calculateScaledInfoContent(String aligned) {
		double content =  calculateInfoContent( aligned );
		return content / (0 - ( Math.log(0.05) / Math.log(2) ) );
	}
	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the residues in a String
	 */
	private static double calculateInfoContent(String aligned) {
		double[] resProbs = new double[20];
		for(int i=0; i<aligned.length(); i++) {
			resProbs[ getResidueIndex(aligned.charAt(i))] += 1.0;
		}
		double uncertainty = 0;
		for(int i=0; i<resProbs.length; i++) {
			resProbs[i] = resProbs[i]/(double)aligned.length();
			if(resProbs[i]>0)
				uncertainty = uncertainty - ( resProbs[i] * ( Math.log(resProbs[i]) / Math.log(2) ) );
		}
		
		return (0 - ( Math.log(0.05) / Math.log(2) ) ) - uncertainty;
	}
	
	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the Secondary Structure
	 */
	private static double calculateSSEInfoContent(String alignedSSE) {
		double[] probs = new double[3];
		for(int i=0; i<alignedSSE.length(); i++) {
			probs[ getSSEIndex(alignedSSE.charAt(i))] += 1.0;
		}
		double uncertainty = 0;
		for(int i=0; i<probs.length; i++) {
			probs[i] = probs[i]/(double)alignedSSE.length();
			if(probs[i]>0)
				uncertainty = uncertainty - ( probs[i] * ( Math.log( probs[i]) / Math.log(2) ) );
		}
		
		return (0 - ( Math.log(0.3333333333) / Math.log(2) ) ) - uncertainty;
	}
	
	
	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the properties of residues in a String
	 * Using the set of 6 properties, charged, polar, hydrophobic and 3 special
	 */
	private static double calculate6PropertyInfoContent(String aligned) {
		double[] resProbs = new double[6];
		for(int i=0; i<aligned.length(); i++) {
			resProbs[ get6SetIndex(aligned.charAt(i))] += 1.0;
		}
		double uncertainty = 0;
		for(int i=0; i<resProbs.length; i++) {
			resProbs[i] = resProbs[i]/(double)aligned.length();
			if(resProbs[i]>0)
				uncertainty = uncertainty - ( resProbs[i] * ( Math.log(resProbs[i]) / Math.log(2) ) );
		}
		
		return (0 - ( Math.log(0.166666667) / Math.log(2) ) ) - uncertainty;
	}

	/*
	 * Static Method calculates the information content of a set of residues
	 * on the basis of conservation of the properties of residues in a String
	 * Using the set of 8 properties
	 */
	private static double calculate8PropertyInfoContent(String aligned) {
		double[] resProbs = new double[8];
		for(int i=0; i<aligned.length(); i++) {
			resProbs[ get8SetIndex(aligned.charAt(i))] += 1.0;
		}
		double uncertainty = 0;
		for(int i=0; i<resProbs.length; i++) {
			resProbs[i] = resProbs[i]/(double)aligned.length();
			if(resProbs[i]>0)
				uncertainty = uncertainty - ( resProbs[i] * ( Math.log(resProbs[i]) / Math.log(2) ) );
		}
		
		return (0 - ( Math.log(0.125) / Math.log(2) ) ) - uncertainty;
	}
	
	char[][] res6Sets = new char[][] {{'R','H','K','D','E'}, {'P'}, {'G'}, {'C'}, {'Q','N','T','S'}, {'A','I','L','M','F','W','Y','V'} };
	char[][] res8Sets = new char[][] {{'R','K'}, {'H','F','W','Y'}, {'D','E'}, {'P'}, {'Q','N','T'}, {'C','S','A','G'}, {'I','L','V'}, {'M'} };
	
	static String[] res6SetStrings = new String[] {"RHKDE", 
											"P", 
											"G", 
											"C", 
											"QNTS", 
											"AILMFWYV" };
	
	private static int get6SetIndex(char res) {
		for( int i=0; i < res6SetStrings .length; i++ ) {
			if( res6SetStrings[i].contains(""+res))
				return i;
		}
		return -1;
	}
	
	static String[] res8SetStrings = new String[] {"RK", 
			"HFWY", 
			"DE", 
			"P",
			"QNT", 
			"CSAG", 
			"ILV", 
			"M" };
	private static int get8SetIndex(char res) {
		for( int i=0; i < res8SetStrings.length; i++ ) {
			if( res8SetStrings[i].contains(""+res))
				return i;
		}
		return -1;
	}
	
	private static int getSSEIndex(char sse) {
		return SSE.alphabet.indexOf(sse);
	}

	public static int getResidueIndex(char res) {
		int result = Residue.alphabet.indexOf(res);
		if(result > -1)
			return result;
		else
			return 20;
	}
	
	public static char getResidueChar(int index) {
		if(index < 20)
			return Residue.alphabet.charAt(index);
		else
			return 'X';
	}

	/*
	 * getChemicalGroupEncoding
	 * 
	 * Convert an amino acid into an encoding that indicates the set of
	 * functional chemical groups it contains :
		acyl
		amide
		amino 
		ammonium
		aromatic
		charged
		glycine
		guanidinium
		hydroxyl
		non-polar
		proline
		thiol
		thioether
	 */
	private static double[] getChemicalGroupEncoding(char res) {
		Map<String, double[]> chemencodings = new HashMap<String, double[]>();
		//									acyl amid amin ammo arom char glyc guan hydr nonp prol thio t-eth
		chemencodings.put("A", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Alanine
		chemencodings.put("R", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Arginine
		chemencodings.put("N", new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Asparagine
		chemencodings.put("D", new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Aspartate
		chemencodings.put("C", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0}); // Cysteine
		chemencodings.put("E", new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Glutamate
		chemencodings.put("Q", new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Glutamine
		chemencodings.put("G", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Glycine
		chemencodings.put("H", new double[]{0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Histidine
		chemencodings.put("I", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Isoleucine
		chemencodings.put("L", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Leucine
		chemencodings.put("K", new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Lysine
		chemencodings.put("M", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}); // Methionine
		chemencodings.put("F", new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Phenylalanine
		chemencodings.put("P", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0}); // Proline
		chemencodings.put("S", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0}); // Serine
		chemencodings.put("T", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0}); // Threonine
		chemencodings.put("W", new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Tryptophan
		chemencodings.put("Y", new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0}); // Tyrosine
		chemencodings.put("V", new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}); // Valine
		return chemencodings.get(""+res);
	}
	

	private static String[] getChemicalGroupHeadings() {
		return new String[] {
				"acyl",
				"amide",
				"amino ",
				"ammonium",
				"aromatic",
				"charged",
				"glycine",
				"guanidinium",
				"hydroxyl",
				"non-polar",
				"proline",
				"thiol",
				"thioether"
		};
	}
	public static double calculatePropertyDistance(Residue ref, Residue residue) {
		double[] props1 = getChemicalGroupEncoding(ref.residueType);
		double[] props2 = getChemicalGroupEncoding(residue.residueType);
		double diff = 0;
		for(int i=0; i <props1.length; i++) {
			diff += Math.pow( (props1[i]-props2[i]) , 2);
		}
		return diff;
	}

	/*
	 * A set of residue frequencies taken from the paper
	 *  Robinson A B, Robinson L R
	 *	(1991) Proc Natl Acad Sci USA 88:8880–8884,
	 */
	
	protected static int[] frequencyCounts = new int[]{
	 35155,// Ala
	 23105,// Arg
	 20212,// Asn
     24161,// Asp
	 8669,// Cys
	 19208,// Gln
	 28354,// Glu
	 33229,// Gly
	 9906,// His
	 23161,// Ile
	 40625,// Leu
	 25872,// Lys
	 10101,// Met
	 17367,// Phe
	 23435,// Pro
	 32070,// Ser
	 26311,// Thr
	 5990,// Trp
	 14488,// Tyr
	 29012,// Val
	 };

	
	public static double[] getBackgroundFreqs() {
		double[] results = new double[frequencyCounts.length];
		double sum = 0;
		for(int i=0; i<frequencyCounts.length; i++){
			sum = sum + frequencyCounts[i];
		}
		for(int i=0; i<frequencyCounts.length; i++){
			results[i] = (double) frequencyCounts[i] / sum;
		}
		return results;
	}
	
	public static boolean isHydrophobic(char res) {
		if(res=='A'||res=='F'||res=='I'||res=='L'||res=='M'||res=='V'||res=='W'||res=='Y'||res=='G'||res=='C'||res=='P')
				return true;
		return false;
	}
	public static boolean isAromatic(char res) {
		if(res=='H'||res=='F'||res=='W'||res=='Y')
				return true;
		return false;
	}
	
	
	public static char[] getResidueCodesSingleLetter() {
		String alpha = "ARNDCQEGHILKMFPSTWYV";
		return alpha.toCharArray();
	}
	public static char[] getResidueCodesSingleLetterPlusX() {
		String alpha = "ARNDCQEGHILKMFPSTWYVX";
		return alpha.toCharArray();
	}
	/*
	 * STATIC METHODS FOR BASIC TASKS
	 */
	protected static char[] residues = new char[] {'A','R','N','D','C','Q','E','G','H','I','L','K','M','F','P','S','T','W','Y','V'};
	
	
	
	protected static String[] threeLetter = new String[] {"ALA","ARG","ASN","ASP","CYS","GLN","GLU","GLY","HIS","ILE","LEU","LYS","MET","PHE","PRO","SER","THR","TRP","TYR","VAL"};
	public static char convert3LetterToSingleLetter(String code) {
		for(int i=0; i<threeLetter.length;i++) {
			if(threeLetter[i].equals(code))
				return residues[i];
		}
		return 'X';
	}
	public static String convertSingleLetterTo3Letter(char code) {
		for(int i=0; i<residues.length;i++) {
			if(residues[i]==code )
				return threeLetter[i];
		}
		return "XXX";
	}
}
