package com.openkm.util;

import com.openkm.dao.LegacyDAO;
import org.hibernate.jdbc.Work;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Hibernate worker helper
 */
public class WorkerUpdate implements Work {
	private List<HashMap<String, String>> errors = new ArrayList<>();
	private int rows = 0;
	private byte[] data;

	public List<HashMap<String, String>> getErrors() {
		return this.errors;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getRows() {
		return this.rows;
	}

	@Override
	public void execute(Connection con) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();
			InputStreamReader is = new InputStreamReader(new ByteArrayInputStream(data));
			BufferedReader br = new BufferedReader(is);
			String sql;
			int ln = 0;

			while ((sql = br.readLine()) != null) {
				String tk = sql.trim();
				ln++;

				if (tk.toUpperCase().startsWith("--") || tk.equals("") || tk.equals("\r")) {
					// Is a comment, so ignore it
				} else {
					if (tk.endsWith(";")) {
						tk = tk.substring(0, tk.length() - 1);
					}

					try {
						rows += stmt.executeUpdate(tk);
					} catch (SQLException e) {
						HashMap<String, String> error = new HashMap<>();
						error.put("ln", Integer.toString(ln));
						error.put("sql", tk);
						error.put("msg", e.getMessage());
						errors.add(error);
					}
				}
			}
		} catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		} finally {
			LegacyDAO.close(rs);
			LegacyDAO.close(stmt);
		}
	}
}
