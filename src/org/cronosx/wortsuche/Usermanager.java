package org.cronosx.wortsuche;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.locks.*;

public class Usermanager
{
	private ServerWortsuche	server;
	private List<User>	users;
	private ReentrantLock mutex;
	
	public Usermanager(ServerWortsuche server)
	{
		mutex = new ReentrantLock();
		users = new ArrayList<>();
		this.server = server;
	}
	
	public int getUsers()
	{
		return users.size();
	}
	
	public User getUser(String username)
	{
		User user;
		try
		{
			mutex.lock();
			user = getLoadedUser(username);
			if(user == null)
			{
				try
				{
					user = new User(username, server);
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					return null;
				}
				addLoadedUser(user);
			}
		}
		finally
		{
			mutex.unlock();
		}
		return user;
	}
	
	public boolean isUsernameAvailable(String username)
	{
		try
		{
			PreparedStatement stmt = server.getDatabase()
					.getPreparedStatement(
							"SELECT ID FROM Users WHERE Username = ?");
			stmt.setString(1, username);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			return !rs.next();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void registerUser(String username, String password)
	{
		try
		{
			PreparedStatement stmt = server
					.getDatabase()
					.getPreparedStatement(
							"INSERT INTO Users(Username, Password, Score, R, G, B) VALUES (?, ?, 0, ?, ?, ?)");
			stmt.setString(1, username);
			stmt.setString(2, server.getSHA1(password));
			stmt.setInt(3, (int) (Math.random() * 200) + 55);
			stmt.setInt(4, (int) (Math.random() * 200) + 55);
			stmt.setInt(5, (int) (Math.random() * 200) + 55);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isLoginCorrect(String username, String password)
	{
		try
		{
			PreparedStatement stmt = server
					.getDatabase()
					.getPreparedStatement(
							"SELECT ID FROM Users WHERE Username = ? AND Password = ?");
			stmt.setString(1, username);
			stmt.setString(2, server.getSHA1(password));
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			return rs.next();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private User getLoadedUser(String username)
	{
		for(User user : users)
		{
			if(user.getUsername().equals(username)) return user;
		}
		return null;
	}
	
	private void addLoadedUser(User user)
	{
		try
		{
			mutex.lock();
			users.add(user);
		}
		finally
		{
			mutex.unlock();
		}
	}
	
	public void removeCachedUser(User user)
	{
		try
		{
			mutex.lock();
			users.remove(user);
		}
		finally
		{
			mutex.unlock();
		}
	}
	
	public void save() throws SQLException
	{
		try
		{
			mutex.lock();
			for(User user : users)
			{
				user.exportToDB();
			}
		}
		finally
		{
			mutex.unlock();
		}
	}
}
