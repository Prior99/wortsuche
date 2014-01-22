package org.cronosx.wortsuche;

import java.net.*;
import java.sql.*;
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
						if(!user.isLoggedIn())
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
		client.addRequestHandler("highscore", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				JSONObject answer = new JSONObject();
				try
				{
					PreparedStatement stmt = server.getDatabase().getPreparedStatement("SELECT Rank, Username, Score FROM `UserRanks` ORDER BY Score DESC LIMIT 7");
					ResultSet rs = stmt.executeQuery();
					while(rs.next())
					{
						JSONObject jUser = new JSONObject();
						jUser.put("username", rs.getString("Username"));
						jUser.put("score", rs.getString("Score"));
						jUser.put("rank", rs.getString("Rank"));
						answer.append("top", jUser);
					}
					stmt.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return answer;
			}
		});
	}
	
	public void shutdown()
	{
		client.shutdown();
	}
	
	private void registerUserHandlers()
	{
		client.addCloseHandler(new CloseHandler()
		{
			@Override
			public void onClose()
			{
				user.clientDisconnected();
				if(!user.isLoggedIn())
				{
					server.getGame().leave(user);
					server.getUserManager().removeCachedUser(user);
				}
			}
		});
		final ClientWrapper self = this;
		client.addRequestHandler("profile", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				JSONObject jObj2 = new JSONObject();
				jObj2.put("username", user.getUsername());
				jObj2.put("score", user.getScore());
				return jObj2;
			}
		});
		client.addRequestHandler("requestColor", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				JSONObject answer = new JSONObject();
				answer.put("okay", true);
				answer.put("color", user.getColor().toJSONObject());
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
					user.setColor(new Color((short)jObj.getInt("r"), (short)jObj.getInt("g"), (short)jObj.getInt("b")));
					sendColor();
					okay = true;
				}
				JSONObject answer = new JSONObject();
				answer.put("okay", okay);
				answer.put("r", user.getColor().toJSONObject());
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
		client.addRequestHandler("highscore", new RequestHandler()
		{
			@Override
			public JSONObject invoke(JSONObject jObj)
			{
				JSONObject answer = new JSONObject();
				try
				{
					PreparedStatement stmt = server.getDatabase().getPreparedStatement("SELECT user.Rank, user.Username, user.Score FROM `UserRanks` AS user, (SELECT ID, Score FROM `UserRanks` WHERE `Username` = ?) AS me WHERE user.Score > me.score ORDER BY user.Score ASC LIMIT 3");
					stmt.setString(1, user.getUsername());
					ResultSet rs = stmt.executeQuery();
					rs.afterLast();
					while(rs.previous())
					{
						JSONObject jUser = new JSONObject();
						jUser.put("username", rs.getString("user.Username"));
						jUser.put("score", rs.getString("user.Score"));
						jUser.put("rank", rs.getString("user.Rank"));
						answer.append("around", jUser);
					}
					stmt.close();
					stmt = server.getDatabase().getPreparedStatement("SELECT user.Rank, user.Username, user.Score FROM `UserRanks` AS user, (SELECT ID, Score FROM `UserRanks` WHERE `Username` = ?) AS me WHERE user.Score <= me.score ORDER BY user.Score DESC LIMIT 4");
					stmt.setString(1, user.getUsername());
					rs = stmt.executeQuery();
					while(rs.next())
					{
						JSONObject jUser = new JSONObject();
						jUser.put("username", rs.getString("user.Username"));
						jUser.put("score", rs.getString("user.Score"));
						jUser.put("rank", rs.getString("user.Rank"));
						answer.append("around", jUser);
					}
					stmt.close();
					stmt = server.getDatabase().getPreparedStatement("SELECT user.Rank, Username, Score FROM `UserRanks` user ORDER BY Score DESC LIMIT 7");
					rs = stmt.executeQuery();
					while(rs.next())
					{
						JSONObject jUser = new JSONObject();
						jUser.put("username", rs.getString("Username"));
						jUser.put("score", rs.getString("Score"));
						jUser.put("rank", rs.getString("user.Rank"));
						answer.append("top", jUser);
					}
					stmt.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
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
	
	public void sendSelect(int x1, int y1, int x2, int y2, Color color)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("x1", x1);
		jObj.put("y1", y1);
		jObj.put("x2", x2);
		jObj.put("y2", y2);
		jObj.put("color", color.toJSONObject());
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
		sendWords();
		sendColor();
		sendUsers();
	}
	
	public void sendWords()
	{
		JSONObject jObj = new JSONObject();
		for(String word : server.getGame().getWords())
			jObj.append("words", word);
		jObj.append("count", server.getGame().getOriginalWordCount());
		client.sendRequest("words", jObj);
	}
	
	public void notifyGameChange()
	{
		client.sendRequest("newGame", new JSONObject());
	}
	
	public void sendUsers()
	{
		JSONObject jObj = new JSONObject();
		for(User u : server.getGame().getUsers())
		{
			JSONObject jU = new JSONObject();
			jU.put("color", u.getColor().toJSONObject());
			jU.put("username", u.getUsername());
			jObj.append("users", jU);
		}
		client.sendRequest("users", jObj);
	}
	
	public void sendChat(String user, Color color, String text, int time)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("username", user);
		jObj.put("time", time);
		jObj.put("color", color.toJSONObject());
		jObj.put("msg", text);
		client.sendRequest("chat", jObj);
	}
	
	public void sendRemoveWord(String word, int x1, int y1, int x2, int y2, Color color)
	{
		JSONObject jObj = new JSONObject();
		jObj.put("x1", x1);
		jObj.put("y1", y1);
		jObj.put("x2", x2);
		jObj.put("y2", y2);
		jObj.put("color", color.toJSONObject());
		jObj.put("word", word);
		client.sendRequest("remove", jObj);
	}
	
	public void sendColor()
	{
		JSONObject jObj = new JSONObject();
		jObj.put("color", user.getColor().toJSONObject());
		client.sendRequest("color", jObj);
	}
}
