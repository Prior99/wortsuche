package org.cronosx.wortsuche;

import org.cronosx.cgi.CGI;
import org.cronosx.cgi.PageHandler;

public class CGIWortsuche extends CGI
{
	private ServerWortsuche server;
	public CGIWortsuche(ServerWortsuche server, PageHandler page)
	{
		super(server.getWebserver(), page);
		this.server = server;
	}
	
}
