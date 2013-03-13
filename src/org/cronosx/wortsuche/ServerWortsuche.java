package org.cronosx.wortsuche;

import java.sql.Statement;

import org.cronosx.cgi.CGI;
import org.cronosx.server.DefaultWebSocketListener;
import org.cronosx.server.Server;
import org.cronosx.webserver.Webserver;

public class ServerWortsuche extends Server
{

	@Override
	protected CGI getDefaultCGIHandler()
	{
		// TODO Auto-generated method stub
		return new CGIWortsuche(this, new PageHandlerWortsuche(this));
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
		return new WebserverWortsuche(this.getLog(), this.getConfig(), this);
	}

	@Override
	public void createDatabase(Statement stmt)
	{
		
	}
	
}
