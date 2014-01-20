package org.cronosx.wortsuche;

import java.io.*;
import java.net.*;

public class WelcomeSocket extends Thread
{
	private ServerSocket socket;
	private final Game game;
	
	public WelcomeSocket(int port, Game g)
	{
		this.game = g;
		try
		{
			socket = new ServerSocket(port);
			start();
		}
		catch(Exception e)
		{
			System.out.println("Unable to open welcomesocket. Is the port maybe in use?");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		while(!isInterrupted())
		{
			try
			{
				Socket s = socket.accept();
				game.registerClient(s);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
		}
	}
}
