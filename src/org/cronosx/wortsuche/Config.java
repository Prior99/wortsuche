package org.cronosx.wortsuche;

import java.io.*;

public class Config
{
	private File file;
	
	private int port = 0;
	private int score = 5;
	private int gameTime = 60 * 60 * 12;
	private int exportTimeout = 60;
	
	private String dbServer = "localhost";
	private String dbUser = "root";
	private String dbPassword = "1234";
	private String dbDatabase = "wortsuche";
	
	private String webUIFolder = "/path/to/folder/";
	
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
			System.out.println("Using game-time: " + gameTime);
		}
		if(key.equals("export-timeout"))
		{
			this.exportTimeout = Integer.parseInt(value);
			System.out.println("Using: " + exportTimeout + "s as timeout for export");
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
		if(key.equals("webui-folder"))
		{
			this.webUIFolder = value;
			System.out.println("Using: " + webUIFolder + " as folder to store config for WebUI in");
		}
	}
	private void save()
	{
		try
		{
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.println("#### General config");
			pw.println("# A free and public reachable port the clients will connect to. Will be set automaticly in client");
			pw.println("port=" + port);
			pw.println("# Basescore the users will get. The higher it is the higher the scores of the users will be");
			pw.println("score=" + score);
			pw.println("# The time one game lasts in seconds");
			pw.println("game-time=" + gameTime);
			pw.println("# Time between exports to the database in seconds");
			pw.println("export-timeout=" + exportTimeout);
			pw.println("# WebUI-Folder, the folder where the webui is hosted from");
			pw.println("webui-folder=" + webUIFolder);
			pw.println("");
			pw.println("#### Databaseconfiguration");
			pw.println("# Server the database is on (localhost, most likely)");
			pw.println("db-server=" + dbServer);
			pw.println("# User to connect to server with");
			pw.println("db-user=" + dbUser);
			pw.println("# Password to connect to server with");
			pw.println("db-password=" + dbPassword);
			pw.println("# Database to use on the server");
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
	
	public String getWebUIFolder()
	{
		return webUIFolder;
	}
}
