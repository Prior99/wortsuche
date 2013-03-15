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
		parseMessage(s, origin);
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
		String command = "";
		String param[] = null;
		int iOf = s.indexOf(':');
		if(iOf != -1 && iOf != s.length() -1) 
		{
			command = s.substring(0, iOf);
			param = s.substring(iOf + 1, s.length()).split(";");
		}
		else if(command.length() >= 1)command = s.substring(0, s.length() -1);
		switch(command)
		{
			case "login":
			{
				if(param.length == 2)
					if(!server.getUserManager().isUsernameAvailable(param[0]))
						if(server.getUserManager().isLoginCorrect(param[0], param[1]))
						{
							User u = server.getUserManager().getUser(param[0]);
							WebsocketListenerUser ul = new WebsocketListenerUser(u);
							ul.onOpen(origin);
							origin.setWebSocketListener(ul);
							origin.send("success:Erfolgreich eingeloggt");
							origin.send("score:"+u.getScore());
						}
						else origin.send("error:Das Passwort ist falsch");
					else origin.send("error:Diesen Benutzernamen gibt es nicht");
				else origin.send("error:Internal Error: Wrong number of arguments supplied");
				break;
			}
			case "register":
			{
				if(param.length == 3)
					if(param[1].equals(param[2]))
						if(param[0].length() >= 3)
							if(server.getUserManager().isUsernameAvailable(param[0]))
								if(param[1].length() > 4 && param[1].length() < 16)
								{
									server.getUserManager().registerUser(param[0], param[1]);
									origin.send("success:Erfolgreich registriert");
								}
								else origin.send("error:Das Passwort muss mindestens 5 Zeichen lang sein");
							else origin.send("error:Der Benutzername ist bereits vergeben");
						else origin.send("error:Der Benutzername muss mindestens 3 Zeichen lang sein und darf nicht länger sein als 16 Zeichen");
					else origin.send("error:Die Passwörter stimmen nicht überein");
				else origin.send("error:Internal Error: Wrong number of arguments supplied");
				break;
			}
			case "usernameTaken":
			{
				if(param.length == 1)
					origin.send(""+!server.getUserManager().isUsernameAvailable(param[0]));
				else origin.send("true");
			}
		}
	}
	
}
