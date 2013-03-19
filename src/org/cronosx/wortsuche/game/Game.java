package org.cronosx.wortsuche.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import org.cronosx.wortsuche.ServerWortsuche;
import org.cronosx.wortsuche.User;

public class Game
{
	private ServerWortsuche server;
	private char[][] array;
	private ArrayList<String> allWords;
	private ArrayList<Selection> selections;
	private ArrayList<User> users;
	private int width = 75;
	private int height = 50;
	private int maxWordLen;
	private int origWordCount = 0;
	private int dictSize = 0;
	private int games = 0;
	private int started = 0;
	private Thread timer;
	private Game game;
	
	public Game(ServerWortsuche server)
	{
		game = this;
		this.server = server;
		if(!this.loadFromDisk()) 
		generateGame();
		users = new ArrayList<User>();
	}
	
	public User[] getUsers()
	{
		return users.toArray(new User[]{});
	}
	
	public ArrayList<String> getWords()
	{
		return allWords;
	}
	
	public int getGamesGenerated()
	{
		return games;
	}
	
	public int getStartTime()
	{
		return started;
	}
	
	public void generateGame()
	{
		started = (int) (System.currentTimeMillis()/1000);
		maxWordLen = 0;
		origWordCount = 0;
		dictSize = 0;
		selections = new ArrayList<Selection>();
		allWords = new ArrayList<String>();
		server.getLog().log("Generating new game...");
		games++;
		int allWords = 0;
		int wordsFailed = 0;
		array = new char[width][height];
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
				array[i][j] = ' ';
		}
		HashMap<Integer, ArrayList<String>> wwords = loadDictonary();
		for(int i = maxWordLen; i >= 2; i--)
		{
			Possibility bestPos = null;
			ArrayList<String> words = wwords.get(i);
			if(words != null)
			for(int j = 0; j < words.size(); j++)
			{	
				bestPos = null;
				allWords ++;
				String word = words.get(j);
				int sx = (int) (Math.random()*width);
				int sy = (int) (Math.random()*height);
				for(int x = 0; x < width; x++)
				{
					for(int y = 0; y < height; y++)
					{
						int sdx, edx, cdx, sdy, edy, cdy;
						sx += x;
						sy += y;
						sx = sx >= width ? sx - width : sx;
						sy = sy >= width ? sy - width : sy;
						if(Math.random() >= 0.5) { sdx = -1; edx = 1; cdx = 1;} else { sdx = 1; edx = -1; cdx = -1;}
						if(Math.random() >= 0.5) { sdy = -1; edy = 1; cdy = 1;} else { sdy = 1; edy = -1; cdy = -1;}
						for(int xdir= sdx; xdir != edx; xdir+=cdx)
							for(int ydir= sdy; ydir != edy; ydir+=cdy)
								if(xdir!= 0 || ydir!=0)
								{
									int cors = match(word, sx, sy, xdir, ydir);
									if(cors != -1)
									{
										if(bestPos == null || bestPos.cors < cors) 
										{
											bestPos = new Possibility(sx, sy, xdir, ydir, cors);
										}
									}
								}
					}
				}
				if(bestPos == null) wordsFailed++;
				else
				{
					if(bestPos.cors != 0 || Math.random() >= 0.7)
					{
						this.apply(word, bestPos.x, bestPos.y, bestPos.xdir, bestPos.ydir);
						this.allWords.add(word);
					}
					else  wordsFailed++;
				}
			}
		}
		origWordCount = this.allWords.size();
		Collections.sort(this.allWords);
		server.getLog().log(wordsFailed+"/"+allWords);
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
				if(this.array[i][j] == ' ')
				{
					this.array[i][j] = chars.charAt((int)(Math.random()*chars.length()));
				}
		}
		if(users != null)
		for(User u:users)
			if(u != null && u.getListener() != null && u.getListener().getOrigin() != null)
				u.getListener().notifyGameChange();
		timer = new Thread()
		{
			public void run()
			{
				try
				{
					Thread.sleep(1000 * getRuntime());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				game.generateGame();
			}
		};
		timer.start();
	}
	
	public int getRuntime()
	{
		return server.getConfig().getInt("Gametime", 12 * 60 * 60);
	}
	
	public int getDictonarySize()
	{
		return dictSize;
	}
	
	public int getOriginalWordCount()
	{
		return this.origWordCount;
	}
	
	public void removeWord(String word, int x1, int y1, int x2, int y2, User u)
	{
		if(word.equals(getSelection(x1, y1, x2, y2)) && allWords.contains(word))
		{
			int inc = (int)(server.getConfig().getInt("Score", 5)*(this.origWordCount - this.allWords.size())/(float)(this.origWordCount) + 1);
			u.incScore(inc);
			broadcast("remove:"+word+";"+x1+";"+y1+";"+x2+";"+y2+";"+u.getColorOpaque());
			this.allWords.remove(word);
			this.selections.add(new Selection(x1, y1, x2, y2, u.getColorOpaque()));
			if(this.allWords.isEmpty())
			{
				generateGame();
				for(User user : users) user.getListener().sendGame();
			}
			if(u != null && u.getListener() != null && u.getListener().getOrigin() != null)
			{
				u.getListener().getOrigin().send("score:"+u.getScore());
				u.getListener().getOrigin().send("scoreinc:"+inc);
			}
		}
	}
	
	public void broadcast(String s)
	{
		for(User u:users)
		{
			if(u != null && u.getListener() != null && u.getListener().getOrigin() != null)
				u.getListener().getOrigin().send(s);
		}
	}
	
	private String getSelection(int x1, int y1, int x2, int y2)
	{
		String string = "";
		if(x1 >= 0 && y1 >= 0 && x1 < this.width && y1 < this.height &&
		   x2 >= 0 && y2 >= 0 && x2 < this.width && y2 < this.height)
		{
			int xdir = getDir(x1, x2);
			int ydir = getDir(y1, y2);
			int mX = x1;
			int mY = y1;
			while(mX != x2 || mY != y2)
			{
				string += this.array[mX][mY];
				mX += xdir;
				mY += ydir;
			}
			string += this.array[x2][y2];	
		}
		return string;
	}
	
	private int getDir(int x1, int x2)
	{
		return  x1 < x2 ? 1 : x1 == x2 ? 0 : -1;
	};
	
	private HashMap<Integer, ArrayList<String>> loadDictonary()
	{
		HashMap<Integer, ArrayList<String>> dict = new HashMap<Integer, ArrayList<String>>();
		try
		{
			Scanner sc = new Scanner(new File("dictonary.txt"));
			while(sc.hasNextLine())
			{
				String s = sc.nextLine();
				if(s.length() > this.maxWordLen) this.maxWordLen = s.length();
				if(!dict.containsKey(s.length())) dict.put(s.length(), new ArrayList<String>());
				dict.get(s.length()).add(s);
				this.dictSize++;
			}
			sc.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		for(int i:dict.keySet())
		{
			shuffle(dict.get(i));
		}
		return dict;
	}
	
	private void shuffle(ArrayList<String> array)
	{
		int i = array.size(), j;
		String tempi, tempj;
		if(i == 0) return;
		while(--i > 0) 
		{
			j = (int) Math.floor(Math.random() * (i + 1));
			tempi = array.get(i);
			tempj = array.get(j);
			array.set(i,tempj);
			array.set(j,tempi);
		}
	}
	
	private int match(String word, int x, int y, int xdir, int ydir)
	{
		int cor = 0;
		for(int i = 0; i < word.length(); i++)
		{
			int cx = x + xdir * i;
			int cy = y + ydir * i;
			if(cx < 0 || cx >= this.width || cy < 0 || cy >= this.height) return -1;
			if(this.array[cx][cy] != ' ' && this.array[cx][cy] != word.charAt(i)) return -1;
			if(this.array[cx][cy] == word.charAt(i)) cor++;
		}
		return cor;
	}
	
	private void apply(String word, int x, int y, int xdir, int ydir)
	{
		for(int i = 0; i < word.length(); i++)
		{
			this.array[x + xdir * i][y + ydir * i] = word.charAt(i);
		}	
	}
	
	class Possibility
	{
		int x;
		int y;
		int xdir;
		int ydir;
		int cors;
		public Possibility(int x, int y, int xdir, int ydir, int cors)
		{
			this.x = x;
			this.y = y;
			this.xdir = xdir;
			this.ydir = ydir;
			this.cors = cors;
		}
	}
	
	public void join(User user)
	{
		if(!users.contains(user))
			users.add(user);
		for(User u: users)
		{
			if(u != null && u.getListener() != null && u.getListener().getOrigin() != null)
				u.getListener().sendUsers();
		}
	}
	
	public void leave(User user)
	{
		users.remove(user);
		for(User u: users)
		{
			if(u != null && u.getListener() != null && u.getListener().getOrigin() != null)
				u.getListener().sendUsers();
		}
	}
	
	public void chat(String msg, String user, String userColor)
	{
		broadcast("chat:"+user+";"+userColor+";"+msg);
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public char[][] getArray()
	{
		return array;
	}
	
	public class Selection
	{
		public int x1;
		public int x2;
		public int y1;
		public int y2;
		public String color;
		public Selection(int x1, int y1, int x2, int y2, String color)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.color = color;
		}
	}
	
	public ArrayList<Selection> getSelections()
	{
		return selections;
	}

	
	public void saveToDisk()
	{
		server.getLog().log("Saving game...");
		try
		{
			File f = new File("game.sav");
			if(f.exists()) f.delete();
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(f));
			stream.writeInt(width);
			stream.writeInt(height);
			stream.writeInt(maxWordLen);
			stream.writeInt(origWordCount);
			stream.writeInt(dictSize);
			stream.writeInt(games);
			for(int x = 0; x < width; x++)
				for(int y = 0; y < height; y++)
					stream.writeChar(array[x][y]);
			stream.writeInt(allWords.size());
			stream.writeInt(selections.size());
			for(int i = 0; i < allWords.size();i++) stream.writeUTF(allWords.get(i));
			for(int i = 0; i < selections.size();i++) 
			{
				stream.writeInt(selections.get(i).x1);
				stream.writeInt(selections.get(i).y1);
				stream.writeInt(selections.get(i).x2);
				stream.writeInt(selections.get(i).y2);
				stream.writeUTF(selections.get(i).color);
			}
			stream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
	}
	
	public boolean loadFromDisk()
	{
		File f = new File("game.sav");
		if(f.exists())
		{
			DataInputStream stream;
			try
			{
				server.getLog().log("Importing game...");
				stream = new DataInputStream(new FileInputStream(f));
				width = stream.readInt();
				height = stream.readInt();
				maxWordLen = stream.readInt();
				origWordCount = stream.readInt();
				dictSize = stream.readInt();
				games = stream.readInt();
				array = new char[width][height];
				for(int x = 0; x < width; x++)
					for(int y = 0; y < height; y++)
						array[x][y] = stream.readChar();
				allWords = new ArrayList<String>();
				int l = stream.readInt();
				int l2 = stream.readInt();
				for(int i = 0; i < l; i++)
					allWords.add(stream.readUTF());
				selections = new ArrayList<Selection>();
				for(int i = 0; i < l2; i++)
				{
					selections.add(new Selection(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt(), stream.readUTF()));
				}
				stream.close();
				f.delete();
				started = (int) (System.currentTimeMillis()/1000);
				timer = new Thread()
				{
					public void run()
					{
						try
						{
							Thread.sleep(1000 * getRuntime());
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						game.generateGame();
					}
				};
				timer.start();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		else return false;
	}
}
