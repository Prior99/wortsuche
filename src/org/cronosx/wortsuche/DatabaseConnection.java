package org.cronosx.wortsuche;

import java.sql.*;

public class DatabaseConnection
{
	private Connection connection;
	private String server;
	private String user;
	private String password;
	private String database;
	
	public DatabaseConnection(String server, String user, String password, String database)
	{
		this.server = server;
		this.user = user;
		this.password = password;
		this.database = database;
		connect();
	}
	
	private void connect()
	{
		try
		{
			connection = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database, user, password);
		}
		catch(SQLException e)
		{
			System.out.println("Unable to connect to database");
			e.printStackTrace();
		}
	}
	
	public PreparedStatement getPreparedStatement(String s)
	{
		PreparedStatement stmt;
		try
		{
			stmt = connection.prepareStatement(s);
		}
		catch(SQLException e)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch(InterruptedException e2)
			{
				e2.printStackTrace();
			}
			connect();
			stmt = getPreparedStatement(s);
		}
		return stmt;
	}

	public void createDatabase(Statement stmt)
	{
		try
		{
			stmt.execute("CREATE TABLE IF NOT EXISTS Users(" +
					"ID 		INT			NOT NULL AUTO_INCREMENT PRIMARY KEY," +
					"Username	VARCHAR(16)," +
					"Password	VARCHAR(40)," +
					"Score		INT,"+
					"R			INT,"+
					"G			INT,"+
					"B			INT)");
			stmt.execute("CREATE TABLE IF NOT EXISTS Blog(" +
					"ID 		INT			NOT NULL AUTO_INCREMENT PRIMARY KEY," +
					"Created	INT," +
					"Content	TEXT," +
					"Headline	TEXT)");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
