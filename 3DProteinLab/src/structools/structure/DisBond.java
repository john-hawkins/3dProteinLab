package structools.structure;

/* 
 * Simple class to store disulfide bond info
 */

public class DisBond {

	// These identify each unique protein  in a structure
	private String pdb_id;

	private String asym_id1;
	private String asym_id2;
	
	// The position in sequence
	private int seq_id1;
	private int seq_id2;
	
	public DisBond(String pdbId, String asymId1, int seqId1,
			String asymId2, int seqId2) {
		super();
		pdb_id = pdbId;
		asym_id1 = asymId1;
		seq_id1 = seqId1;
		asym_id2 = asymId2;
		seq_id2 = seqId2;
	}
	
	public String getPdb_id() {
		return pdb_id;
	}
	public void setPdb_id(String pdbId) {
		pdb_id = pdbId;
	}
	public String getAsym_id1() {
		return asym_id1;
	}
	public void setAsym_id1(String asymId1) {
		asym_id1 = asymId1;
	}
	public String getAsym_id2() {
		return asym_id2;
	}
	public void setAsym_id2(String asymId2) {
		asym_id2 = asymId2;
	}
	public int getSeq_id1() {
		return seq_id1;
	}
	public void setSeq_id1(int seqId1) {
		seq_id1 = seqId1;
	}
	public int getSeq_id2() {
		return seq_id2;
	}
	public void setSeq_id2(int seqId2) {
		seq_id2 = seqId2;
	}

	
}
