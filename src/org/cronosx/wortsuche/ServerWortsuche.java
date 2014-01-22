package org.cronosx.wortsuche;

import java.io.*;
import java.security.*;
import java.util.*;

public class ServerWortsuche
{
	private final Usermanager users;
	private final Game game;
	private final Config config;
	private final DatabaseConnection dbConn;
	private MessageDigest sha1;
	private WelcomeSocket ws;
	
	private boolean okay;
	
	public ServerWortsuche()
	{
		okay = true;
		this.config = new Config();
		if(!exportPortFile()) okay = false;
		try
		{
			sha1 = MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println("No algorithm for SHA-1");
			okay = false;
		}
		if(okay)
		{
			this.users = new Usermanager(this);
			this.game = new Game(this);
			this.ws = new WelcomeSocket(config.getPort(), this.game);
			this.dbConn = new DatabaseConnection(config.getDBServer(), config.getDBUser(), config.getDBPassword(), config.getDBDatabase());
		}
		else
		{
			this.users = null;
			this.game = null;
			this.ws = null;	
			this.dbConn = null;
		}
	}
	
	public boolean isOkay()
	{
		return okay;
	}
	
	private boolean exportPortFile()
	{
		File f = new File(config.getWebUIFolder() + File.separatorChar + "port.conf");
		if(!f.isDirectory())
		{
			try
			{
				PrintWriter pw = new PrintWriter(f);
				pw.println("# This is an autogenertaed configfile");
				pw.println("# It is used by the webui to determine the port the server is running on");
				pw.println("# Do not change the content of this file or the webinterface wont be able to connect");
				pw.println("# File will be overwritten at each start of the server");
				pw.println(config.getPort());
				pw.close();
				return true;
			}
			catch(FileNotFoundException e)
			{
				System.out.println("\"" + f.getName() + "\" is not writable!");
				return false;
			}
		}
		else
		{
			System.out.println("\"" + f.getName() + "\" is a Directory. Please set it to /path/to/webinterface/");
			return false;
		}
	}
	
	public Config getConfig()
	{
		return config;
	}
	
	public DatabaseConnection getDatabase()
	{
		return dbConn;
	}
	
	public void shutdown()
	{
		System.out.println("Shutting down...");
		game.shutdown();
		ws.shutdown();
		dbConn.shutdown();
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public Usermanager getUserManager()
	{
		return users;
	}
	
	public void save()
	{
		try
		{
			game.saveToDisk();
			users.save();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Encrypts the string with an unsalted SHA1-checksum
	 * <p>
	 * @param toEncrypt string that should be encrypted
	 * @return encrypted string
	 */
	public String getSHA1(String toEncrypt)
	{
		byte[] enc;
		if(sha1 == null)
		{
			enc = toEncrypt.getBytes();
			System.out.println("Warning: As no SHA-1 Algorithm was available, string is stored unencrypted in HEX");
		}
		else
			enc = sha1.digest(toEncrypt.getBytes());
		String result = "";
		for(int i=0; i < enc.length; i++) 
		{
			result += Integer.toString((enc[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	public static void main(String[] args)
	{
		final ServerWortsuche server = new ServerWortsuche();
		if(!server.isOkay())
		{
			System.out.println("Something went wrong! Exitting...");
			return;
		}
		final Thread t = new Thread()
		{
			public void run()
			{
				while(!isInterrupted())
				{
					try
					{
						Thread.sleep(server.getConfig().getExportTimeout());
					}
					catch(InterruptedException e)
					{
						return;
					}
					server.save();	
				}
			}
		};
		t.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				System.out.println("Received CTRL+C, saving all remaining data");
				server.save();
				server.shutdown();
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
							e.printStackTrace();
						}
					}
					String s = sc.nextLine();
					String[] args = s.split(" ");
					System.out.println("Running command \""+s+"\"");
					switch(args[0])
					{
						case "exit":
						{
							server.shutdown();
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