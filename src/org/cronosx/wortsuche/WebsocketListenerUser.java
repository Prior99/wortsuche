package org.cronosx.wortsuche;

import org.cronosx.websockets.WebSocket;
import org.cronosx.websockets.WebSocketListener;
import org.cronosx.wortsuche.game.Game;
import org.cronosx.wortsuche.game.Game.Selection;

public class WebsocketListenerUser implements WebSocketListener
{
	private User user;
	private WebSocket origin;
	public WebsocketListenerUser(User user)
	{
		this.user = user;
		user.openWebsocket(this);
		user.getServer().getGame().join(user);
	}
	@Override
	public void onMessage(String s, WebSocket origin)
	{
		this.origin = origin;
		if(s.equals("close:"))
		{
			origin.close();
		}
		else parseMessage(s, origin);
	}

	@Override
	public void onOpen(WebSocket origin)
	{
		this.origin = origin;
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
		user.closeWebsocket();
		user.getServer().getGame().leave(user);
	}
	
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
		else if(s.length() >= 1)command = s.substring(0, s.length() -1);
		switch(command)
		{
			case "requestColor":
			{
				origin.send("color:"+user.getR()+";"+user.getG()+";"+user.getB());
				break;
			}
			case "color":
			{
				if(param.length == 3)
				{
					user.setColor(Integer.parseInt(param[0]), Integer.parseInt(param[1]), Integer.parseInt(param[2]));
					origin.send("color:"+user.getR()+";"+user.getG()+";"+user.getB());
				}
				break;
			}
			case "getGame":
			{
				sendGame();
				break;
			}
			case "getGameMeta":
			{
				for(Selection sel:user.getServer().getGame().getSelections())
				{
					origin.send("select:"+sel.x1+";"+sel.y1+";"+sel.x2+";"+sel.y2+";"+sel.color);
				}
				break;
			}
			case "remove":
			{
				if(param.length == 5)
				{
					String word = param[0];
					int x1 = Integer.parseInt(param[1]);
					int y1 = Integer.parseInt(param[2]);
					int x2 = Integer.parseInt(param[3]);
					int y2 = Integer.parseInt(param[4]);
					user.getServer().getGame().removeWord(word, x1, y1, x2, y2, user);
				}
				break;
			}
		}
	}
	
	public void notifyGameChange()
	{
		origin.send("newGame:");
	}
	
	public void sendGame()
	{
		Game game = user.getServer().getGame();
		String gameString = "";
		for(int i = 0; i < game.getWidth(); i++)
		{
			for(int j = 0; j < game.getHeight(); j++)
			{
				gameString += game.getArray()[i][j];
			}	
		}
		origin.send("game:"+game.getWidth()+";"+game.getHeight()+";"+gameString+";"+(game.getRuntime() - (System.currentTimeMillis()/1000 - game.getStartTime())));
		String words = "";
		for(String word:game.getWords())
		{
			words += word+",";
		}
		origin.send("words:"+words.substring(0,words.length()-1)+";"+game.getOriginalWordCount());
		origin.send("color:"+user.getColorOpaque());
		origin.send("ready");
	}
	
	public WebSocket getOrigin()
	{
		return origin;
	}
}
