package structools.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;

//import biostructure.entities.SSE;
//import biostructure.programs.DSSP;

public class Protein {
	private String pdb_id;
	private Integer entity_id;
	private String type;
	private String chainId;
	private String authorChainId;
	
	private String seq;
	private String sse;
	private String seq_and_dis_bonds;
	private String seq_and_sse;
	private String seq_and_sse_and_dis_bonds;
	
	private String resolved;

	public String getSeq_and_dis_bonds() {
		return seq_and_dis_bonds;
	}

	public String getSeq_and_sse_and_dis_bonds() {
		return seq_and_sse_and_dis_bonds;
	}

	private String uniprot_ac;	
	private String uniprot_name;

	private String keywords;
	
	private String pdbFileName;
	
	private String proteinClass;

	private Hashtable<Integer, Residue> residues;
	
	private int correction=0;
	
	public static char CysBoundChar = 'B';
	public static char CysUnBoundChar = 'U';
	
	public int getCorrection() {
		return correction;
	}

	public int getResdiuePosFromSeqPos(int seqPos) {
		return  seqPos + correction;
	}
	
	/*
	 * THIS CONSTRUCTOR ONLY EXISTS FOR DB2
	 * Because a simple query does not return
	 * chain info.
	 */
	public Protein(String pdbId, 
			Integer entityId, 
			String typel, 
			String seq_one_letter_code, 
			String unip, 
			String unipname) {
		super();
		pdb_id = pdbId;
		entity_id = entityId;
		type = typel;
		seq = seq_one_letter_code;
		uniprot_ac = unip;
		uniprot_name = unipname;
	}

	public Protein(String pdbId, 
			Integer entityId, 
			String chain, 
			String auth_chain, 
			String typel, 
			String seq_one_letter_code, 
			String unip, 
			String unipname) {
		super();
		pdb_id = pdbId;
		entity_id = entityId;
		chainId = chain;
		authorChainId = auth_chain;
		type = typel;
		seq = seq_one_letter_code;
		uniprot_ac = unip;
		uniprot_name = unipname;
	}
	
	/*
	 * ---------- MAIN CONSTRUCTOR ---------
	 */
	public Protein(String pdbId, 
			Integer entityId, 
			String chain, 
			String auth_chain, 
			String typel, 
			String seq_one_letter_code, 
			String sses, 
			String seq_con_sse, 
			String unip, 
			String unipname) {
		super();
		pdb_id = pdbId;
		entity_id = entityId;
		chainId = chain;
		authorChainId = auth_chain;
		type = typel;
		seq = seq_one_letter_code;
		sse = sses;
		seq_and_sse = seq_con_sse;
		uniprot_ac = unip;
		uniprot_name = unipname;
	}

	/*
	// TO BE DELETED !!!
	public Protein(String pdbId, 
			Integer entityId, 
			String chain, 
			String auth_chain, 
			String typel, 
			String seq_one_letter_code, 
			String sses, 
			String seq_con_sse, 
			String unip, 
			String unipname,
			String keywds, 
			String seq_con_dis, 
			String seq_con_sse_con_dis) {
		super();
		pdb_id = pdbId;
		entity_id = entityId;
		chainId = chain;
		authorChainId = auth_chain;
		type = typel;
		seq = seq_one_letter_code;
		sse = sses;
		seq_and_sse = seq_con_sse;
		uniprot_ac = unip;
		uniprot_name = unipname;
		keywords = keywds;
		seq_and_dis_bonds = seq_con_dis;
		seq_and_sse_and_dis_bonds = seq_con_sse_con_dis;
	}

	 */
	 public Protein(String pdbId, 
			Integer entityId, 
			String chain, 
			String auth_chain, 
			String typel, 
			String seq_one_letter_code, 
			String sses, 
			String seq_con_sse, 
			String unip, 
			String unipname,
			String keywds, 
			String seq_con_dis, 
			String seq_con_sse_con_dis,
			String seq_resolved) {
		super();
		pdb_id = pdbId;
		entity_id = entityId;
		chainId = chain;
		authorChainId = auth_chain;
		type = typel;
		seq = seq_one_letter_code;
		sse = sses;
		seq_and_sse = seq_con_sse;
		uniprot_ac = unip;
		uniprot_name = unipname;
		keywords = keywds;
		seq_and_dis_bonds = seq_con_dis;
		seq_and_sse_and_dis_bonds = seq_con_sse_con_dis;
		resolved = seq_resolved;
	}

	
	public Protein(Chain c) {
		this(c, null);
	}
	
	public Protein(Chain c, String proteinId) {
		// TODO Auto-generated constructor stub
		if(proteinId==null)
			pdb_id = c.getParent().getPDBCode();
		else
			pdb_id = proteinId;
		
		//chainId = c.getName();
		
		residues = new Hashtable<Integer, Residue>();

		int maxPos = 0;
		int minPos = 0;
		List<Group> gs = c.getAtomGroups( org.biojava.nbio.structure.GroupType.AMINOACID );
		for(int i=0; i< gs.size(); i++) {
			if(c.getAtomGroup(i).getType().equals("amino")) {
				AminoAcid aa = (AminoAcid) c.getAtomGroup(i);
				// TODO: NEED TO HANDLE EXCEPTIONS WHEN THE PDBcode contain insertion codes
				try {
					//int seqPos = Integer.parseInt( aa.getPDBCode() );
					int seqPos = aa.getResidueNumber().getSeqNum();
					residues.put(seqPos, new Residue(aa));
					//System.out.println(seqPos + " " + aa.getAminoType());
					if(seqPos > maxPos)
						maxPos = seqPos;
					if(seqPos < minPos)
						minPos = seqPos;
				} catch(Exception e) {
					// Just ignore it
				}
				//System.err.println(aa.getAminoType() + " " + seqPos);
			}
		}
		int adjust=0;
		if(minPos < 0) {
			 correction = minPos;
			 adjust = 0 - minPos;
		}
		int arrayLength = maxPos + 1 + adjust;
		char[] fullSeq = new char[arrayLength];
		char[] fullSse = new char[arrayLength];
		for(int i=0; i<(arrayLength); i++) {
			fullSeq[i] = '_';
			fullSse[i] = '_';
		}
		// Iterate over all the provided residues and fill in the details
		// for the string representation
		
		Enumeration<Integer> enumer = residues.keys();
		while(enumer.hasMoreElements()) {
			Integer theKey = enumer.nextElement();
			fullSeq[theKey + adjust] = residues.get(theKey).getResidueType();
			fullSse[theKey + adjust] = residues.get(theKey).getSse().charAt(0);
		}
		//seq = c.getAtomSequence();
		//sse = Residue.getSSESequence(c);
		seq = new String( fullSeq );
		sse = new String( fullSse );
		
		//System.out.println("PROTEIN SEQ: " + seq);
		//System.out.println("PROTEIN SSE: " + sse );
		//System.out.println("SEQ LENGTH: " + seq.length() );	
	}

	public String getPdbId() {
		return pdb_id;
	}
	public void setPdbId(String pdbId) {
		this.pdb_id = pdbId;
	}
	public Integer getEntityId() {
		return entity_id;
	}
	public void setEntityId(Integer entityId) {
		this.entity_id = entityId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getChainId() {
		return chainId;
	}
	public void setChainId(String chainId) {
		this.chainId = chainId;
	}
	
	
	public String getResolved() {
		return resolved;
	}

	public void setResolved(String resolved) {
		this.resolved = resolved;
	}

	
	public String getSEQ() {
		return seq;
	}

	public void setSEQ(String pdbx_seq_one_letter_code) {
		seq = pdbx_seq_one_letter_code;
	}

	public String getSSE() {
		return sse;
	}

	public void setSSE(String predicted_SSE) {
		sse = predicted_SSE; 
		seq_and_sse = "";
		for(int i=0; i<seq.length(); i++) {
			seq_and_sse = seq_and_sse + combine_AA_with_SSE_code(seq.charAt(i), sse.charAt(i));
		}
		
		seq_and_sse_and_dis_bonds = "";
		if(seq_and_dis_bonds.equals(null)) {
			seq_and_sse_and_dis_bonds = seq_and_sse;
		} else {
			for(int i=0; i<seq_and_dis_bonds.length(); i++) {
				seq_and_sse_and_dis_bonds = seq_and_sse_and_dis_bonds + combine_AA_with_SSE_code(seq_and_dis_bonds.charAt(i), sse.charAt(i));
			}
		}
	}

	public static char combine_AA_with_SSE_code(char aa, char sse) {
		char result = aa;
		if(sse=='c' || sse=='C') {
			result = ( (char) (100 + (int) aa ) );
		} else if(sse=='s' || sse=='S') {
			result =  ( (char) (32 + (int) aa ) );
		} 
		return result;
	}
	
	public String getSeq_and_sse() {
		return seq_and_sse;
	}

	public String getUniprot() {
		return uniprot_ac;
	}

	public void setUniprot(String uniprot) {
		this.uniprot_ac = uniprot;
	}

	public String getUniprotName() {
		return uniprot_name;
	}
	
	public String getKeywords() {
		return keywords;
	}

	public Residue getResidueAt(int position) {
		if(residues != null)
			return residues.get(position);
		else {
			return new Residue(seq.charAt(position-1), position, ""+ sse.charAt(position-1));
		}
	}

	public Collection<Residue> getResidues() {
		return residues.values();
	}

	public String getName() {
		return pdb_id + ":" + chainId;
	}
	
	public String getPdbFileName() {
		return pdbFileName;
	}

	public void setPdbFileName(String pdbFileName) {
		this.pdbFileName = pdbFileName;
	}

	/*
	public void readInDSSP(DSSP dssp) {
		//System.err.println("SEQ: " + seq);
		//System.err.println("SSE: " + sse);
		//System.err.println("Length: " + seq.length());
		char[] temp = new char[seq.length()];
		for(int t=0; t<temp.length; t++) {
			temp[t] = 'C';
		}
		int adjust = 0 - correction;
		ArrayList<SSE> ssEs = dssp.getSSEs();
		for (SSE see : ssEs) {
			int start =  see.getBegin() + adjust;
			int end = see.getEnd() + adjust;
			for(int p=start; p<end; p++) {
				temp[p] = see.getSseElement().charAt(0);
			}
		}
		this.sse = new String(temp);
	}
	*/
	
	public static Residue findClosestResidue(Residue ref, Protein p) {
		
		Collection<Residue> residues = p.getResidues();
		Iterator<Residue> iter = residues.iterator();
		
		double distance = 1000;
		
		Residue result = new Residue();
		
		while(iter.hasNext()) {
			Residue r = iter.next();
			
			// First calculate the distance
			double dist = Residue.calcAlphaCaDist(ref, r);
			// Now see if it makes it into the list of potentials
			if(dist < distance) { 
				distance = dist;
				result = r;
			}		
		}
		
		return result;
	}
	
	
	public static Residue findClosestResidueWithPropertyMatch(Residue ref, Protein p) {
		Collection<Residue> residues = p.getResidues();
		Iterator<Residue> iter = residues.iterator();

		// We keep a list of the 2 closest residues
		int numToConsider = 2;
		double[] distances = new double[numToConsider];
		for(int i=0; i<numToConsider; i++) {
			distances[i] = 1000;
		}
		int maxDist = 100;
		Residue[] potentials = new Residue[numToConsider];
		
		while(iter.hasNext()) {
			Residue r = iter.next();
			
			// First calculate the distance
			double dist = Residue.calcAlphaCaDist(ref, r);
			// Now see if it makes it into the list of potentials
			if(dist < maxDist) { 
				for(int i=0; i<numToConsider; i++) {
					if(dist<distances[i]) {
						//Insert the new entry and bubble the rest up
						for(int j=numToConsider-1; j>i; j--) {
							distances[j] = distances[j-1];
							potentials[j] = potentials[j-1];
						}
						distances[i] = dist;
						potentials[i] = r;
						break;
					}
				}
			}		
		}
		int bestMatch = 0;
		double bestPropMatch = Residue.calculatePropertyDistance(ref, potentials[0]);
		for(int i=1; i<numToConsider; i++) {
			double dist = Residue.calculatePropertyDistance(ref, potentials[i]);
			if(dist < bestPropMatch) {
				bestPropMatch = dist;
				bestMatch = i;
			}
		}
		return potentials[bestMatch];

	}

	public void setClass(String string) {
		// TODO Auto-generated method stub
		proteinClass = string;
	}

	public String getAuthorChainId() {
		return authorChainId;
	}

	public void setDisBonds(List<DisBond> pdbDisBonds) {
		// Start with the normal sequence
		seq_and_dis_bonds = seq;
		
		if( pdbDisBonds != null ) {
			for (DisBond e : pdbDisBonds ) {
				if( e.getAsym_id1().equals(this.getChainId() ))	{
					seq_and_dis_bonds = seq_and_dis_bonds.substring(0, e.getSeq_id1()-1 ) + CysBoundChar + seq_and_dis_bonds.substring(e.getSeq_id1() );
				}
				if( e.getAsym_id2().equals(this.getChainId() ))	{
					seq_and_dis_bonds = seq_and_dis_bonds.substring(0, e.getSeq_id2()-1 ) + CysBoundChar + seq_and_dis_bonds.substring(e.getSeq_id2() );
				}
			}
		}
		
		// Now replace all non bound with the other char
		seq_and_dis_bonds = seq_and_dis_bonds.replaceAll("C", ""+CysUnBoundChar);
	}

	public static boolean isThreeLetterCodeSeq(String theSeq) {
		// A crude test
		if( (theSeq.indexOf('1')>-1) || (theSeq.indexOf('-')>-1) || theSeq.length()>3 && theSeq.charAt(3)=='-') {
			return true;
		}
		return false;
	}

	public static String convert3LetterToSingleLetter(String theSeq) {
		String[] theCodes = theSeq.split("-");
		String result = "";
		for(int i=0; i<theCodes.length; i++) {
			result = result + Residue.convert3LetterToSingleLetter(theCodes[i]);
		}
		return result;
	}
	
	
	
}
