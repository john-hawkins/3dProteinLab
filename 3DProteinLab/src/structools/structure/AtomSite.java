package structools.structure;

class Vector3D {
	public double x;   
	public double y;  
	public double z;  
}

public class AtomSite {

	private String PDB_ID;          
	private String ATOM_SITE_ID;
	private String A_AA;     
	
	private Double CARTN_X;
	private Double CARTN_Y;
	private Double CARTN_Z;

	private String ASYM_ID;
	private String ATOM_ID;
	
	private int ENTITY_ID;
	private int SEQ_ID;
	private int MODEL_NUM;
	
	private String TYPE_SYMBOL;
	
	// EXTRA FIELDS FOR AUTHOR DEFINED SEQ POS

	private String AUTH_ASYM_ID;
	private String AUTH_SEQ_ID;
	private String PDBX_PDB_INS_CODE;

	/*
	 * Constructor for data coming from XmlRPdb
	 * 
	 * NOTE : Not all of this is used, could be simplified
	 */
	public AtomSite(String pdb_id, String atom_site_id, String a_aa,
			Double cartn_x, Double cartn_y, Double cartn_z,
			String label_asym_id, String label_atom_id,
			int label_entity_id, int label_seq_id,
			int my_pdbx_pdb_model_num, String type_symbol, 
			String auth_asym_id, String auth_seq_id, String pdbx_pdb_ins_code) {
		super();
		PDB_ID = pdb_id;
		ATOM_SITE_ID = atom_site_id;
		A_AA = a_aa;
		CARTN_X = cartn_x;
		CARTN_Y = cartn_y;
		CARTN_Z = cartn_z;
		ASYM_ID = label_asym_id;
		ATOM_ID = label_atom_id;
		ENTITY_ID = label_entity_id;
		SEQ_ID = label_seq_id;
		MODEL_NUM = my_pdbx_pdb_model_num;
		TYPE_SYMBOL = type_symbol;
		AUTH_ASYM_ID = auth_asym_id;
		AUTH_SEQ_ID = auth_seq_id;
		PDBX_PDB_INS_CODE = pdbx_pdb_ins_code;
	}

	/*
	 * Simple Constructor
	 */
	public AtomSite(String pdb_id, String a_aa,
			Double cartn_x, Double cartn_y, Double cartn_z,
			String label_asym_id, String label_atom_id,
			int label_entity_id, int label_seq_id) {
		super();
		PDB_ID = pdb_id;
		A_AA = a_aa;
		CARTN_X = cartn_x;
		CARTN_Y = cartn_y;
		CARTN_Z = cartn_z;
		ASYM_ID = label_asym_id;
		ATOM_ID = label_atom_id;
		ENTITY_ID = label_entity_id;
		SEQ_ID = label_seq_id;
	}
	
	public String getPDB_ID() {
		return PDB_ID;
	}

	public void setPDB_ID(String pdb_id) {
		PDB_ID = pdb_id;
	}

	public String getATOM_SITE_ID() {
		return ATOM_SITE_ID;
	}

	public void setATOM_SITE_ID(String atom_site_id) {
		ATOM_SITE_ID = atom_site_id;
	}

	public String getA_AA() {
		if(A_AA==null)
			return "";
		else
			return A_AA;
	}

	public void setA_AA(String a_aa) {
		A_AA = a_aa;
	}

	public Double getCARTN_X() {
		return CARTN_X;
	}

	public void setCARTN_X(Double cartn_x) {
		CARTN_X = cartn_x;
	}

	public Double getCARTN_Y() {
		return CARTN_Y;
	}

	public void setCARTN_Y(Double cartn_y) {
		CARTN_Y = cartn_y;
	}

	public Double getCARTN_Z() {
		return CARTN_Z;
	}

	public void setCARTN_Z(Double cartn_z) {
		CARTN_Z = cartn_z;
	}

	public String getLABEL_ASYM_ID() {
		return ASYM_ID;
	}

	public void setLABEL_ASYM_ID(String label_asym_id) {
		ASYM_ID = label_asym_id;
	}

	public String getLABEL_ATOM_ID() {
		return ATOM_ID;
	}

	public void setLABEL_ATOM_ID(String label_atom_id) {
		ATOM_ID = label_atom_id;
	}

	public int getENTITY_ID() {
		return ENTITY_ID;
	}

	public void setENTITY_ID(int label_entity_id) {
		ENTITY_ID = label_entity_id;
	}

	public int getLABEL_SEQ_ID() {
		return SEQ_ID;
	}

	public void setLABEL_SEQ_ID(int label_seq_id) {
		SEQ_ID = label_seq_id;
	}

	public int getMY_PDBX_PDB_MODEL_NUM() {
		return MODEL_NUM;
	}

	public void setMY_PDBX_PDB_MODEL_NUM(int my_pdbx_pdb_model_num) {
		MODEL_NUM = my_pdbx_pdb_model_num;
	}

	public String getTYPE_SYMBOL() {
		return TYPE_SYMBOL;
	}

	public void setTYPE_SYMBOL(String type_symbol) {
		TYPE_SYMBOL = type_symbol;
	}

	public String getAUTH_ASYM_ID() {
		return AUTH_ASYM_ID;
	}

	public String getAUTH_SEQ_ID() {
		return AUTH_SEQ_ID;
	}

	public String getPDBX_PDB_INS_CODE() {
		if(PDBX_PDB_INS_CODE==null )
			return "";
		return PDBX_PDB_INS_CODE;
	}

	/*
	 * UTILITY METHODS
	 */
	
	public static Double calcDistance(AtomSite a1, AtomSite a2) {
		Double xDifSqrd = Math.pow(a1.getCARTN_X() - a2.getCARTN_X(), 2);
		Double yDifSqrd = Math.pow(a1.getCARTN_Y() - a2.getCARTN_Y(), 2);
		Double zDifSqrd = Math.pow(a1.getCARTN_Z() - a2.getCARTN_Z(), 2);
		
		return Math.sqrt(xDifSqrd + yDifSqrd + zDifSqrd);
	}

	public static Double calcAngle(AtomSite a1, AtomSite a2, AtomSite a3, AtomSite a4) {
		/*		
		double vec1_x = a1.getCARTN_X() - a2.getCARTN_X();
		double vec1_y = a1.getCARTN_Y() - a2.getCARTN_Y();
		double vec1_z = a1.getCARTN_Z() - a2.getCARTN_Z();
		
		double vec2_x = a3.getCARTN_X() - a4.getCARTN_X();
		double vec2_y = a3.getCARTN_Y() - a4.getCARTN_Y();
		double vec2_z = a3.getCARTN_Z() - a4.getCARTN_Z();

		System.out.print("Calculate Angle between [" + vec1_x + ", " + vec1_y + ", " + vec1_z + "] and ");
		System.out.print(" [" + vec2_x + ", " + vec2_y + ", " + vec2_z + "]\n" );
		double len1 = Math.sqrt(Math.pow(vec1_x, 2) + Math.pow(vec1_y,2) + Math.pow(vec1_z,2) );
		double len2 = Math.sqrt(Math.pow(vec2_x,2) + Math.pow(vec2_y,2) + Math.pow(vec2_z,2) );
		System.out.print(" LEN 1 = " + len1);
		System.out.print(" LEN 2 = " + len2);
		double dotProduct = (vec1_x*vec2_x)+(vec1_y*vec2_y)+(vec1_z*vec2_z);
		System.out.print(" DP = " + dotProduct);
		double cosTheta = 	dotProduct/(len1*len2);
		System.out.print(" cosTheta = " + cosTheta);
		System.out.print(" Theta = " + Math.acos(cosTheta));
		return (Math.acos(cosTheta)*180/Math.PI);
		 */
		/*		*/
		Vector3D[] theVecs = getVectors( a1, a2, a3, a4);

		//System.out.print("Angle between [" + theVecs[0].x + ", " + theVecs[0].y + ", " + theVecs[0].z + "] and ");
		//System.out.print(" [" + theVecs[1].x + ", " + theVecs[1].y + ", " + theVecs[1].z + "]\n" );
		double len1 = Math.sqrt(Math.pow(theVecs[0].x, 2) + Math.pow(theVecs[0].y,2) + Math.pow(theVecs[0].z,2) );
		double len2 = Math.sqrt(Math.pow(theVecs[1].x,2) + Math.pow(theVecs[1].y,2) + Math.pow(theVecs[1].z,2) );
		//System.out.println(" LEN 1 = " + len1);
		//System.out.println(" LEN 2 = " + len2);
		double dotProduct = (theVecs[0].x*theVecs[1].x)+(theVecs[0].y*theVecs[1].y)+(theVecs[0].z*theVecs[1].z);
		//System.out.println(" DP = " + dotProduct);
		double cosTheta = 	dotProduct/(len1*len2);
		//System.out.println(" cosTheta = " + cosTheta);
		//System.out.println(" Theta = " + Math.acos(cosTheta));
		return (Math.acos(cosTheta)*180/Math.PI);

	}

	private static Vector3D[] getVectors(AtomSite S1_1, AtomSite S1_2, AtomSite S2_1, AtomSite S2_2) {
		
		Vector3D[] vecs = new Vector3D[2];
		
		// FIRST WORK OUT WHICH POINTS ARE CLOSEST 
		Double dist1 = AtomSite.calcDistance(S1_1, S2_1);
		Double dist2 = AtomSite.calcDistance(S1_1, S2_2);
		Double dist3 = AtomSite.calcDistance(S1_2, S2_1);
		Double dist4 = AtomSite.calcDistance(S1_2, S2_2);
		
		if(dist1 < dist2 && dist1 < dist3 && dist1 < dist4) {
			vecs[0] = new Vector3D();
			vecs[0].x = S1_2.getCARTN_X() - S1_1.getCARTN_X();
			vecs[0].y = S1_2.getCARTN_Y() - S1_1.getCARTN_Y();
			vecs[0].z = S1_2.getCARTN_Z() - S1_1.getCARTN_Z();
			vecs[1] = new Vector3D();
			vecs[1].x = S2_2.getCARTN_X() - S2_1.getCARTN_X();
			vecs[1].y = S2_2.getCARTN_Y() - S2_1.getCARTN_Y();
			vecs[1].z = S2_2.getCARTN_Z() - S2_1.getCARTN_Z();
			
		} else if(dist2 < dist1 && dist2 < dist3 && dist2 < dist4 ) {
			vecs[0] = new Vector3D();
			vecs[0].x = S1_2.getCARTN_X() - S1_1.getCARTN_X();
			vecs[0].y = S1_2.getCARTN_Y() - S1_1.getCARTN_Y();
			vecs[0].z = S1_2.getCARTN_Z() - S1_1.getCARTN_Z();
			vecs[1] = new Vector3D();
			vecs[1].x = S2_1.getCARTN_X() - S2_2.getCARTN_X();
			vecs[1].y = S2_1.getCARTN_Y() - S2_2.getCARTN_Y();
			vecs[1].z = S2_1.getCARTN_Z() - S2_2.getCARTN_Z();
			
		} else if(dist3 < dist1 && dist3 < dist2 && dist3 < dist4 ) {
			vecs[0] = new Vector3D();
			vecs[0].x = S1_1.getCARTN_X() - S1_2.getCARTN_X();
			vecs[0].y = S1_1.getCARTN_Y() - S1_2.getCARTN_Y();
			vecs[0].z = S1_1.getCARTN_Z() - S1_2.getCARTN_Z();
			vecs[1] = new Vector3D();
			vecs[1].x = S2_2.getCARTN_X() - S2_1.getCARTN_X();
			vecs[1].y = S2_2.getCARTN_Y() - S2_1.getCARTN_Y();
			vecs[1].z = S2_2.getCARTN_Z() - S2_1.getCARTN_Z();
			
		} else {
			vecs[0] = new Vector3D();
			vecs[0].x = S1_1.getCARTN_X() - S1_2.getCARTN_X();
			vecs[0].y = S1_1.getCARTN_Y() - S1_2.getCARTN_Y();
			vecs[0].z = S1_1.getCARTN_Z() - S1_2.getCARTN_Z();
			vecs[1] = new Vector3D();
			vecs[1].x = S2_1.getCARTN_X() - S2_2.getCARTN_X();
			vecs[1].y = S2_1.getCARTN_Y() - S2_2.getCARTN_Y();
			vecs[1].z = S2_1.getCARTN_Z() - S2_2.getCARTN_Z();
		}
		
		return vecs;
	}

	
}
