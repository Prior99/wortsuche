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
	private int r;
	private int g;
	private int b;
	
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
		PreparedStatement stmt = server.getDatabaseConnection().getPreparedStatement("SELECT Score, R, G, B FROM Users WHERE Username = ?");
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
		server.getLog().log("Exporting user "+username+" to database",50);
		PreparedStatement stmt = server.getDatabaseConnection().getPreparedStatement("UPDATE Users SET Score = ? WHERE Username = ?");
		stmt.setInt(1, score);
		stmt.setString(2, username);
		stmt.executeUpdate();
	}
	
	public String getColor()
	{
		return "rgba("+r+","+g+","+b+",1)";
	}
	
	public String getColorOpaque()
	{
		return "rgba("+r+","+g+","+b+",0.18)";
	}
}
