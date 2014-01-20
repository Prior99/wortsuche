package org.cronosx.wortsuche;

import java.io.*;

public class Config
{
	private File file;
	
	private int port;
	private int score;
	private int gameTime;
	private int exportTimeout;
	
	private String dbServer;
	private String dbUser;
	private String dbPassword;
	private String dbDatabase;
	
	public Config()
	{
		file = new File("server.conf");
		read();
		save();
	}
	
	private void read()
	{
		try
		{
			
			BufferedReader rd = new BufferedReader(new FileReader(file));
			String line;
			String[] parts;
			while((line = rd.readLine()) != null)
			{
				parts = line.split("=");
				if(parts.length > 0)
				{
					String key, value;
					key = parts[0].trim().toLowerCase();
					if(parts.length > 1)
						value = parts[1].trim();
					else 
						value = "";
					parse(key, value);
				}
			}
			rd.close();
		}
		catch(FileNotFoundException e)
		{
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void parse(String key, String value)
	{
		if(key.equals("port"))
		{
			this.port = Integer.parseInt(value);
			System.out.println("Using port: " + port);
		}
		if(key.equals("score"))
		{
			this.score = Integer.parseInt(value);
			System.out.println("Using score: " + score);
		}
		if(key.equals("game-time"))
		{
			this.gameTime = Integer.parseInt(value);
			System.out.println("Using score: " + gameTime);
		}
		if(key.equals("export-timeout"))
		{
			this.exportTimeout = Integer.parseInt(value);
			System.out.println("Using: " + exportTimeout + "ms as timeout for export");
		}
		if(key.equals("db-server"))
		{
			this.dbServer = value;
			System.out.println("Using: " + dbServer + " as server for database");
		}
		if(key.equals("db-user"))
		{
			this.dbUser = value;
			System.out.println("Using: " + dbUser + " as user for database");
		}
		if(key.equals("db-password"))
		{
			this.dbPassword = value;
			System.out.println("Using: ****** as password for database");
		}
		if(key.equals("db-database"))
		{
			this.dbDatabase = value;
			System.out.println("Using: " + dbDatabase + " as database");
		}
	}
	private void save()
	{
		try
		{
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.println("port=" + port);
			pw.println("score=" + score);
			pw.println("game-time=" + gameTime);
			pw.println("export-timeout=" + exportTimeout);
			pw.println("db-server=" + dbServer);
			pw.println("db-user=" + dbUser);
			pw.println("db-password=" + dbPassword);
			pw.println("db-database=" + dbDatabase);
			pw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getPort()
	{
		return port;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public int getGameTime()
	{
		return gameTime;
	}
	
	public int getExportTimeout()
	{
		return exportTimeout;
	}
	
	public String getDBServer()
	{
		return this.dbServer;
	}
	
	public String getDBUser()
	{
		return this.dbUser;
	}
	
	public String getDBPassword()
	{
		return this.dbPassword;
	}
	
	public String getDBDatabase()
	{
		return this.dbDatabase;
	}
}
