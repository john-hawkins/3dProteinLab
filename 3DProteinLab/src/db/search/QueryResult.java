package db.search;

import java.util.List;

public class QueryResult {

	int id;
	String query_string;
	String user_id;
	String datestamp;
	String technology;
	String keywords;
	String resolution_op;
	String resolution;
	int chain_len_min;
	int chain_len_max;
	
	List<MatchingStructure> matches;

	public QueryResult(int iD, String qUERYSTRING, String uSERID,
			String dATESTAMP, String tECHNOLOGY, String kEYWORDS,
			String rESOLUTIONOP, String rESOLUTION, int cHAINLENMIN,
			int cHAINLENMAX) {
		super();
		id = iD;
		query_string = qUERYSTRING;
		user_id = uSERID;
		datestamp = dATESTAMP;
		technology = tECHNOLOGY;
		keywords = kEYWORDS;
		resolution_op = rESOLUTIONOP;
		resolution = rESOLUTION;
		chain_len_min = cHAINLENMIN;
		chain_len_max = cHAINLENMAX;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuery_string() {
		return query_string;
	}

	public void setQuery_string(String queryString) {
		query_string = queryString;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String userId) {
		user_id = userId;
	}

	public String getDatestamp() {
		return datestamp;
	}

	public void setDatestamp(String datestamp) {
		this.datestamp = datestamp;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getResolution_op() {
		return resolution_op;
	}

	public void setResolution_op(String resolutionOp) {
		resolution_op = resolutionOp;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public int getChain_len_min() {
		return chain_len_min;
	}

	public void setChain_len_min(int chainLenMin) {
		chain_len_min = chainLenMin;
	}

	public int getChain_len_max() {
		return chain_len_max;
	}

	public void setChain_len_max(int chainLenMax) {
		chain_len_max = chainLenMax;
	}

	public List<MatchingStructure> getMatches() {
		return matches;
	}

	public void setMatches(List<MatchingStructure> matches) {
		this.matches = matches;
	}
	
}
