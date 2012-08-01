package db;

import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Pair;

import db.util.ISetter;
import db.util.PreparedStatementExecutionItem;
import db.util.ISetter.IntSetter;
import db.util.ISetter.StringSetter;

public class DatabaseConnector extends DbConnection
{	
	public void createDatabaseEmpty(String dbName) {
		try {
			// Drop the DB if it already exists
			String query = "DROP DATABASE IF EXISTS " + dbName;
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			
			// First create the DB.
			query = "CREATE DATABASE " + dbName + ";";
			ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			
			// Reconnect to our new database.
			close();
			connect(dbName.toLowerCase());
			
			// load our schema			
			runScript(new InputStreamReader(this.getClass().getResourceAsStream("schema.sql")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createFinalDatabase(String dbName) {
		try {
			// Drop the DB if it already exists
			String query = "DROP DATABASE IF EXISTS " + dbName;
			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			
			// First create the DB.
			query = "CREATE DATABASE " + dbName + ";";
			ei = new PreparedStatementExecutionItem(query, null);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			
			// Reconnect to our new database.
			close();
			connect(dbName.toLowerCase());
			
			// load our schema			
			runScript(new InputStreamReader(this.getClass().getResourceAsStream("finalSchema.sql")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return all the networks inside the DB.
	 * @return
	 */
	public List<Pair<String, Integer>> getAllNetworkCommits() {
		try {
			List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
			String query = "SELECT new_commit_id, network_id FROM networks";
			ISetter[] params = {};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			while(rs.next()) {
				result.add(new Pair<String, Integer>(rs.getString("new_commit_id"), 
						rs.getInt("network_id")));
			}
			
			return result;
		}
		catch (SQLException e) {
			
		}
		return null;
	}
	
	/**
	 * Return a list of all edges inside a network for a commit
	 * @param networkID
	 * @return
	 */
	public List<Pair<String, String>> getEdgesFromNetwork(int networkID) {
		try {
			List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
			String query = "SELECT distinct source, target FROM edges WHERE" +
					" network_id=?";
			ISetter[] params = {
					new IntSetter(4,networkID)
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			while(rs.next()) {
				result.add(new Pair<String, String>(rs.getString("source"), 
						rs.getString("target")));
			}
			
			return result;
		}
		catch (SQLException e) {
			
		}
		return null;
	}
	
	/** 
	 * Returns if the commit was a bug or not.
	 * @param commitID
	 * @return
	 */
	public boolean getCommitStatus(String commitID) {
		try {
			List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
			String query = "SELECT bug FROM fix_inducing WHERE" +
					" bug=?";
			ISetter[] params = {
					new StringSetter(4,commitID)
			};

			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(eifirst);
			eifirst.waitUntilExecuted();
			ResultSet rs = eifirst.getResult();

			if(rs.next())
				return true;
			
			return false;
		}
		catch (SQLException e) {
			
		}
		return false;
	}
	
	/**
	 * Upsert the pattern into the results table.
	 * @param pattern
	 * @param fail
	 */
	public void upsertPattern(Pair<String, String> pattern, boolean fail) {
		try {
			String query = "SELECT p_id1, p_id2 FROM patterns WHERE" +
					" (p_id1=? AND p_id2=?) OR (p_id1=? AND p_id2=?)";
			ISetter[] params = {
					new StringSetter(1,pattern.getFirst()),
					new StringSetter(1,pattern.getSecond()),
					
					new StringSetter(1,pattern.getSecond()),
					new StringSetter(1,pattern.getFirst()),
			};

			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
			addExecutionItem(ei);
			ei.waitUntilExecuted();
			ResultSet rs = ei.getResult();
			if(rs.next()) {
				query = "UPDATE patterns SET ";
				if(fail)
					query += "failed = failed + 1";
				else
					query += "passed = passed + 1";
				query += " WHERE (p_id1=? AND p_id2=?) OR (p_id1=? AND p_id2=?)";
				ISetter[] params2 = {
						new StringSetter(1,pattern.getFirst()),
						new StringSetter(1,pattern.getSecond()),
						
						new StringSetter(1,pattern.getSecond()),
						new StringSetter(1,pattern.getFirst()),
				};

				ei = new PreparedStatementExecutionItem(query, params2);
				addExecutionItem(ei);
				ei.waitUntilExecuted();
			}
			else {
				query = "INSERT INTO patterns (p_id1, p_id2, passed failed) VALUES" +
						" (?, ?, ";
				if(fail)
					query += "0, 1)";
				else
					query += "1, 0)";
				
				ISetter[] params2 = {
						new StringSetter(1,pattern.getFirst()),
						new StringSetter(1,pattern.getSecond()),
				};

				ei = new PreparedStatementExecutionItem(query, params2);
				addExecutionItem(ei);
				ei.waitUntilExecuted();
			}
		}
		catch(Exception e) {
			
		}
	}
}
