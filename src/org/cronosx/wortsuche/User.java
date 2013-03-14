package org.cronosx.wortsuche;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;

public class User
{
	private WebsocketListenerUser websocket;
	private String username;
	private int score;
	private ServerWortsuche server;
	
	public User(String username, int score, ServerWortsuche server)
	{
		this.server = server;
		this.username = username;
		this.score = score;
	}
	
	public void incScore()
	{
		score++;
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
	
	public void openWebsocket(WebsocketListenerUser websocket)
	{
		this.websocket = websocket;
	}
	
	public void closeWebsocket()
	{
		this.websocket = null;
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
		return websocket != null;
	}
	
	public WebsocketListenerUser getListener()
	{
		return websocket;
	}
	
	public void importFromDB() throws SQLException
	{
		PreparedStatement stmt = server.getDatabaseConnection().getPreparedStatement("SELECT Score FROM Users WHERE Username = ?");
		stmt.setString(1, username);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		if(rs.next())
		{
			this.score = rs.getInt("Score");
		}
	}
	
	public void exportToDB() throws SQLException
	{
		PreparedStatement stmt = server.getDatabaseConnection().getPreparedStatement("UPDATE Users SET Score = ? WHERE Username = ?");
		stmt.setInt(1, score);
		stmt.setString(2, username);
		stmt.executeUpdate();
	}
}
