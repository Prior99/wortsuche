package org.cronosx.wortsuche;

import java.sql.SQLException;
import java.sql.Statement;

import org.cronosx.cgi.CGI;
import org.cronosx.server.DefaultWebSocketListener;
import org.cronosx.server.Server;
import org.cronosx.webserver.Webserver;
import org.cronosx.wortsuche.game.Game;

public class ServerWortsuche extends Server
{
	private Usermanager users;
	private Game game;
	public ServerWortsuche()
	{
		this.users = new Usermanager(this);
		this.game = new Game(this);
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public Usermanager getUserManager()
	{
		return users;
	}
	
	public void save()
	{
		try
		{
			users.save();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected CGI getDefaultCGIHandler()
	{
		// TODO Auto-generated method stub
		return new CGI(getWebserver(), new PageHandlerWortsuche(this));
	}
	
	public Webserver getWebserver()
	{
		return this.webserver;
	}

	@Override
	public DefaultWebSocketListener getDefaultWebSocketListener()
	{
		return new WebsocketListenerWortsuche(this);
	}

	@Override
	public Webserver getDefaultWebserver()
	{
		return new Webserver(this.getLog(), this.getConfig(), this);
	}

	@Override
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
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
