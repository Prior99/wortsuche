package org.cronosx.wortsuche;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;

public class User
{
	private final String username;
	private int score;
	private final ServerWortsuche server;
	private int r;
	private int g;
	private int b;
	private int loggedIn;
	
	public void incScore(int i)
	{
		score+=i;
	}
	
	public int getR()
	{
		return r;
	}
	
	public int getG()
	{
		return g;
	}
	
	public int getB()
	{
		return b;
	}
	
	public void setColor(int r, int g, int b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public User(String username, ServerWortsuche server) throws SQLException
	{
		this.username = username;
		this.server = server;
		importFromDB();
	}
	
	public ServerWortsuche getServer()
	{
		return server;
	}
	
	public void close()
	{
		try
		{
			this.exportToDB();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		server.getUserManager().removeCachedUser(this);
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public boolean isLoggedIn()
	{
		return loggedIn > 0;
	}
	
	public void clientConnected()
	{
		loggedIn++;
	}
	
	public void clientDisconnected()
	{
		loggedIn--;
	}
	
	public void importFromDB() throws SQLException
	{
		PreparedStatement stmt = server.getDatabase().getPreparedStatement("SELECT Score, R, G, B FROM Users WHERE Username = ?");
		stmt.setString(1, username);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		if(rs.next())
		{
			this.score = rs.getInt("Score");
			this.r = rs.getInt("R");
			this.g = rs.getInt("G");
			this.b = rs.getInt("B");
		}
	}
	
	public void exportToDB() throws SQLException
	{
		System.out.println("Exporting user "+username+" to database");
		PreparedStatement stmt = server.getDatabase().getPreparedStatement("UPDATE Users SET Score = ?, R = ?, G = ?, B = ? WHERE Username = ?");
		stmt.setInt(1, score);
		stmt.setInt(2, r);
		stmt.setInt(3, g);
		stmt.setInt(4, b);
		stmt.setString(5, username);
		stmt.executeUpdate();
	}
	
	public String getColor()
	{
		return "rgba("+r+","+g+","+b+",1)";
	}
	
	public String getColorOpaque()
	{
		return "rgba("+r+","+g+","+b+",0.075)";
	}
}
