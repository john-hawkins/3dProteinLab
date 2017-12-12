package db.search;

public class InvalidQueryException extends Exception {

	String message;
	
	public InvalidQueryException(String string) {
		// TODO Auto-generated constructor stub
		this.message = string;
	}

	public String getMessage() {
		return message;
	}

}
