package org.cronosx.wortsuche;

import java.util.Scanner;

public class Main
{
	public static void main(String[] args)
	{
		final ServerWortsuche server = new ServerWortsuche();
		server.start();
		final Thread t = new Thread()
		{
			public void run()
			{
				while(!isInterrupted())
				{
					try
					{
						Thread.sleep(server.getConfig().getInt("Export-Timeout", 3600)*1000);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					server.getLog().log("Starting scheduled export to database",50);
					server.save();	
				}
			}
		};
		t.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				server.getLog().log("Received CTRL+C, saving all remaining data");
				server.save();
				server.shutDown();
			}
		});
		final Thread t2 = new Thread()
		{
			public void run()
			{
				Scanner sc = new Scanner(System.in);
				while(!isInterrupted())
				{
					while(!sc.hasNextLine())
					{
						try
						{
							Thread.sleep(200);
						}
						catch(InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					String s = sc.nextLine();
					String[] args = s.split(" ");
					server.getLog().log("Running command \""+s+"\"");
					switch(args[0])
					{
						case "exit":
						{
							server.shutDown();
							t.interrupt();
							interrupt();
							break;
						}
						case "save":
						{
							server.save();
							break;
						}
						case "regenerate":
						{
							server.getGame().generateGame();
							break;
						}
						case "help":
						{
							System.out.println("Known commands are: exit, save, regenerate, help");
							break;
						}
					}
				}
				sc.close();
			}
		};
		t2.start();
	}
}
