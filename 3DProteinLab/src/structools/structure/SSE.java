package structools.structure;

/* 
 * This class will be used for matching a simplified representaion
 * of a protein structure. Simply a set of vectors for the helices 
 * and sheets, with amino acid composition or sequence for pattern
 * matching. 
 */

public class SSE {

	// These identify each unique protein  in a structure
	private String pdb_id;
	private int entity_id;
	private String asym_id;
	
	// These identify the nature of the SSE
	private String sse_type;
	private String sse_group;
	
	// The position in sequence
	private int start_pos;
	private int end_pos;
	
	// To identify the position in space
	private double start_x;
	private double start_y;
	private double start_z;
	private double end_x;
	private double end_y;
	private double end_z;

	//To store the AA sequence for this SSE 
	private String seq;
	
	public static String alphabet = "HSC";
	
	public SSE(String pdb_id, int entity_id, String asym_id, String sse_type,
			String sse_group, int start_pos, int end_pos) {
		super();
		this.pdb_id = pdb_id;
		this.entity_id = entity_id;
		this.asym_id = asym_id;
		this.sse_type = sse_type;
		this.sse_group = sse_group;
		this.start_pos = start_pos;
		this.end_pos = end_pos;
	}
	
	public String getPdb_id() {
		return pdb_id;
	}
	public int getEntity_id() {
		return entity_id;
	}
	public String getAsym_id() {
		return asym_id;
	}
	public String getSse_type() {
		return sse_type;
	}
	public String getSse_group() {
		return sse_group;
	}
	public int getStart_pos() {
		return start_pos;
	}
	public int getEnd_pos() {
		return end_pos;
	}
	public double getStart_x() {
		return start_x;
	}
	public double getStart_y() {
		return start_y;
	}
	public double getStart_z() {
		return start_z;
	}
	public double getEnd_x() {
		return end_x;
	}
	public double getEnd_y() {
		return end_y;
	}
	public double getEnd_z() {
		return end_z;
	}

	
}
