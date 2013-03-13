package org.cronosx.wortsuche;

import org.cronosx.server.DefaultWebSocketListener;
import org.cronosx.websockets.WebSocket;

public class WebsocketListenerWortsuche extends DefaultWebSocketListener
{

	private ServerWortsuche server;
	public WebsocketListenerWortsuche(ServerWortsuche server)
	{
		super(server);
		this.server = server;
	}
	
	@Override
	public void onMessage(String s, WebSocket origin)
	{
		
	}

	@Override
	public void onOpen(WebSocket origin)
	{
		
	}

	@Override
	public void onHandshake(WebSocket origin)
	{
		
	}

	@Override
	public void onHandshakeSuccessfull(WebSocket origin)
	{
		
	}

	@Override
	public void onClose(WebSocket origin)
	{
		
	}

	@Override
	protected void parseMessage(String s, WebSocket origin)
	{
		
	}
	
}
