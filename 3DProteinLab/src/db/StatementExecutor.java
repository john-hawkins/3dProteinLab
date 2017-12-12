package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class StatementExecutor {
	public static interface RowConverter<T> {
		public T createObject(ResultSet rs) throws SQLException;
	}
	
	public static interface RowHandler<T> {
		public void handleRow(ResultSet rs) throws SQLException;
		public T getFinalResult();
	}
		
	public static final RowConverter<Integer> INTEGER_CONVERTER = new RowConverter<Integer>() {
		public Integer createObject(ResultSet rs) throws SQLException {
			int val = rs.getInt(1);
			if (rs.wasNull())
				return null;
			return val;
		}		
	};

	public static final RowConverter<String> STRING_CONVERTER = new RowConverter<String>() {
		public String createObject(ResultSet rs) throws SQLException {
			return rs.getString(1);
		}		
	};
	
	public static <T> List<T> executeQuery(PreparedStatement st, RowConverter<T> converter) throws SQLException {
		ResultSet rs = st.executeQuery();
		try {
			List<T> data = new LinkedList<T>();
			while (rs.next()) {
				data.add(converter.createObject(rs));
			}
			return data;
		}
		finally {
			rs.close();
		}
	}
	
	public static <T> T executeQuery(PreparedStatement st, RowHandler<T> handler) throws SQLException {
		ResultSet rs = st.executeQuery();
		try {
			while (rs.next()) {
				handler.handleRow(rs);
			}
			return handler.getFinalResult();
		}
		finally {
			rs.close();
		}
	}	

	public static <T> T executeSingletonQuery(PreparedStatement st, RowConverter<T> converter) throws SQLException {
		ResultSet rs = st.executeQuery();
		try {
			if (rs.next()) {
				return converter.createObject(rs);
			}
			return null;
		}
		finally {
			rs.close();
		}
	}
}
