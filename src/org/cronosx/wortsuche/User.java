package org.cronosx.wortsuche;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;

public class User
{
	private final String username;
	private int score;
	private final ServerWortsuche server;
	private Color color;
	private int loggedIn;
	
	public void incScore(int i)
	{
		score+=i;
	}
	
	public void setColor(Color c)
	{
		this.color = c;
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
			this.color = new Color(rs.getShort("R"), rs.getShort("G"), rs.getShort("B"));
		}
	}
	
	public void exportToDB() throws SQLException
	{
		PreparedStatement stmt = server.getDatabase().getPreparedStatement("UPDATE Users SET Score = ?, R = ?, G = ?, B = ? WHERE Username = ?");
		stmt.setInt(1, score);
		stmt.setInt(2, color.getR());
		stmt.setInt(3, color.getG());
		stmt.setInt(4, color.getB());
		stmt.setString(5, username);
		stmt.executeUpdate();
	}
	
	public Color getColor()
	{
		return color;
	}
}
