package org.cronosx.websockets;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class Client implements WebSocketListener
{
	private WebSocket s;
	private boolean ready;
	private final Map<Integer, ResponseHandler> responses;
	private final Map<String, RequestHandler> requests;
	private int currentID;
	private boolean closed;
	private boolean okay;
	
	public Client(Socket s)
	{
		okay = false;
		currentID = 1;
		requests = new TreeMap<>();
		responses = new TreeMap<>();
		try
		{
			this.s = new WebSocket(s);
			this.s.setWebSocketListener(this);
			ready = true;
		}
		catch(Exception e)
		{
			ready = false;
			System.out.println("Unable to open stream to newly opened socket");
		}
	}
	
	public boolean isClosed()
	{
		return closed;
	}
	
	public boolean isReady()
	{
		return ready;
	}
	
	public boolean isOkay()
	{
		return okay;
	}
	
	public void addRequestHandler(String request, RequestHandler handler)
	{
		requests.put(request, handler);
	}
	
	private void process(JSONObject jObj)
	{
		if(jObj.has("isRequest") && jObj.getBoolean("isRequest"))
		{
			String req = jObj.getString("requestID");
			if(requests.containsKey(req))
			{
				RequestHandler handler = requests.get(req);
				JSONObject answer = handler.invoke(jObj);
				answer.put("isResponse", true);
				answer.put("responseID", jObj.getInt("responseID"));
				if(!closed && okay)
					s.send(answer.toString());
			}
			else
				System.out.println("Received request with invalid ID \"" + req + "\"");
		}
		else if(jObj.has("isResponse") && jObj.getBoolean("isResponse"))
		{
			int id = jObj.getInt("responseID");
			if(responses.containsKey(id))
			{
				ResponseHandler handler = responses.get(id);
				if(handler != null)
					handler.invoke(jObj);
				responses.remove(id);
			}
			else
				System.out.println("Received response with invalid ID " + id);
		}
		else
			System.out.println("Received packet that was neither request nor response");
	}
	
	public void sendRequest(String request, JSONObject obj, ResponseHandler handler)
	{
		obj.put("responseID", currentID);
		obj.put("isRequest", true);
		obj.put("requestID", request);
		responses.put(currentID, handler);
		if(!closed && okay)
			s.send(obj.toString());
		currentID++;
	}
	
	public void sendRequest(String request, JSONObject obj)
	{
		sendRequest(request, obj, null);
	}

	@Override
	public void onMessage(String string, WebSocket origin)
	{
		try
		{
			JSONObject jObj = new JSONObject(string);
			process(jObj);
		}
		catch(JSONException e)
		{
			System.out.println("Received corrupted package: " + string);
		}
	}
	
	public void shutdown()
	{
		this.closed = true;
		try
		{
			s.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onOpen(WebSocket origin) { }

	@Override
	public void onHandshake(WebSocket origin) { }

	@Override
	public void onHandshakeSuccessfull(WebSocket origin) 
	{ 
		okay = true;
	}

	@Override
	public void onClose(WebSocket origin) 
	{
		System.out.println("Socket closed");
		shutdown();
	}
	
}
