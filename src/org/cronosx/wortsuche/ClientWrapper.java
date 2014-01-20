package org.cronosx.wortsuche;

import java.net.*;
import org.cronosx.websockets.*;
import org.json.*;


public class ClientWrapper
{
	private final Client client;
	private final ServerWortsuche server;
	private User user;
	public ClientWrapper(final Socket s, final ServerWortsuche server)
	{
		this.server = server;
		client = new Client(s);
		client.addRequestHandler("login", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				boolean okay = false;
				if(jObj.has("username") && jObj.has("password"))
				{
					String username = jObj.getString("username");
					String password = jObj.getString("password");
					if(server.getUserManager().isLoginCorrect(username, password))
					{
						user = server.getUserManager().getUser(username);
						server.getGame().join(user);
						user.clientConnected();
						okay = true;
						registerUserHandlers();
					}
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				return answer;
			}
		});
		client.addRequestHandler("register", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				boolean okay = false;
				if(jObj.has("username") && jObj.has("password"))
				{
					String username = jObj.getString("username");
					String password = jObj.getString("password");
					if(server.getUserManager().isUsernameAvailable(username))
					{
						server.getUserManager().registerUser(username, password);
						okay = true;
					}
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				return answer;
			}
		});
	}
	
	private void registerUserHandlers()
	{
		final ClientWrapper self = this;
		client.addRequestHandler("requestColor", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				sendColor();
				JSONObject answer = new JSONObject();
				answer.put("okay", true);
				return answer;
			}
		});
		client.addRequestHandler("color", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				boolean okay = false;
				if(jObj.has("r") && jObj.has("g") && jObj.has("b"))
				{
					user.setColor(jObj.getInt("r"), jObj.getInt("g"), jObj.getInt("b"));
					sendColor();
					okay = true;
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				return answer;
			}
		});
		client.addRequestHandler("getGame", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				sendGame();
				JSONObject answer = new JSONObject();
				answer.put("okay", true);
				return answer;
			}
		});
		client.addRequestHandler("getGameMeta", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				
				for(Game.Selection sel:user.getServer().getGame().getSelections())
				{
					sendSelect(sel.x1, sel.y1, sel.x2, sel.y2, sel.color);
				}
				sendUsers();
				
				for(Game.Message m: user.getServer().getGame().getChatBuffer())
				{
					sendChat(m.user, m.color, m.msg, m.time);
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", true);
				return answer;
			}
		});
		client.addRequestHandler("remove", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				boolean okay = false;
				if(jObj.has("word") && jObj.has("x1") && jObj.has("y1") && jObj.has("x2") && jObj.has("y2"))
				{
					String word = jObj.getString("word");
					int x1 = jObj.getInt("x1");
					int y1 = jObj.getInt("y1");
					int x2 = jObj.getInt("x2");
					int y2 = jObj.getInt("y2");
					user.getServer().getGame().removeWord(word, x1, y1, x2, y2, self);
					okay = true;
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				return answer;
			}
		});
		client.addRequestHandler("chat", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				boolean okay = false;
				if(jObj.has("msg"))
				{
					okay = true;
					user.getServer().getGame().chat(jObj.getString("msg"), user.getUsername(), user.getColor());
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				return answer;
			}
		});
	}
	
	public void sendSelect(int x1, int y1, int x2, int y2, String color)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("x1", x1);
		jObj.put("y1", y1);
		jObj.put("x2", x2);
		jObj.put("y2", y2);
		jObj.put("color", color);
		client.sendRequest("select", jObj);
	}
	
	public User getUser()
	{
		return user;
	}
	
	public void sendScoreInc(int i)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("score", i);
		client.sendRequest("scoreinc", jObj);
	}
	
	public void sendScore()
	{
		JSONObject jObj = new JSONObject();
		jObj.put("score", user.getScore());
		client.sendRequest("score", jObj);
	}
	
	public void sendGame()
	{
		JSONObject jObj = new JSONObject();
		jObj.put("width", server.getGame().getWidth());
		jObj.put("height", server.getGame().getHeight());
		jObj.put("gameStarted", server.getGame().getStartTime());
		char[][] array = server.getGame().getArray();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < array.length; i++)
		{
			for(int j = 0; j < array[i].length; j++)
			{
				sb.append(array[i][j]);
			}
		}
		jObj.put("data", sb.toString());
		client.sendRequest("game", jObj);
	}
	
	public void notifyGameChange()
	{
		client.sendRequest("newGame", new JSONObject());
	}
	
	public void sendUsers()
	{
		JSONObject jObj = new JSONObject();
	}
	
	public void sendChat(String user, String color, String text, int time)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("username", user);
		jObj.put("time", time);
		jObj.put("color", color);
		jObj.put("msg", text);
		client.sendRequest("chat", jObj);
	}
	
	public void sendRemoveWord(String word, int x1, int y1, int x2, int y2, String color)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("x1", x1);
		jObj.put("y1", y1);
		jObj.put("x2", x2);
		jObj.put("y2", y2);
		jObj.put("color", color);
		jObj.put("word", word);
		client.sendRequest("remove", jObj);
	}
	
	public void sendColor()
	{
		JSONObject jObj = new JSONObject();
		jObj.put("r", user.getR());
		jObj.put("g", user.getG());
		jObj.put("b", user.getB());
		client.sendRequest("color", jObj);
	}
}
