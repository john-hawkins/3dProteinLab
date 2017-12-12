package descriptor;

import structools.structure.Residue;

public class CriticalResidue extends Residue{
	
	private String subs;
	private String ssesubs;

	public boolean useSubs = true;
	public boolean useSecStruct = true;
	public boolean buildSubsFromData = true;
	
    public CriticalResidue(char residue, int pos, String s, boolean useSubs,
			boolean useSecStruct, boolean buildSubsFromData) {
		this.residueType = residue;
		this.position = pos;
		this.subs = s;
		this.ssesubs = "";
		this.useSubs = useSubs;
		this.useSecStruct = useSecStruct;
		this.buildSubsFromData = buildSubsFromData;
	}

	public CriticalResidue(char residue, int position, String substitutions) {
		super();
		this.residueType = residue;
		this.position = position;
		this.subs = substitutions;
		this.ssesubs = "";
	}
	
	
	public String getSubs() {
		return subs;
	}
	
	public void setSubs(String s) {
		subs = s;
	}
	
	public void addSub(char s) {
		if(!subs.contains(""+s))
			subs = subs + s;
	}
	
	public void addSubs(String newsubs) {
		for(int i=0; i<newsubs.length(); i++) {
			char s= newsubs.charAt(i);
			addSub(s);
		}
	}
	
	
	public String getSsesubs() {
		return ssesubs;
	}

	public void setSsesubs(String ssesubs) {
		this.ssesubs = ssesubs;
	}
	
	public void addSSESub(char sseCode) {
		if(!ssesubs.contains(""+sseCode))
			ssesubs = ssesubs + sseCode;
	}
	
	public boolean isUseSubs() {
		return useSubs;
	}

	public void setUseSubs(boolean useSubs) {
		this.useSubs = useSubs;
	}

	public boolean isUseSecStruct() {
		return useSecStruct;
	}

	public void setUseSecStruct(boolean useSecStruct) {
		this.useSecStruct = useSecStruct;
	}

	public void print() {
		System.out.println(position + " " + residueType + " [" + subs + "] ");
	}


}
