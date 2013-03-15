package org.cronosx.wortsuche.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
	private int height = 40;
	private int maxWordLen;
	private int origWordCount = 0;
	private int dictSize = 0;
	private int games = 0;
	
	public Game(ServerWortsuche server)
	{
		selections = new ArrayList<Selection>();
		allWords = new ArrayList<String>();
		this.server = server;
		generateGame();
		users = new ArrayList<User>();
	}
	
	public ArrayList<String> getWords()
	{
		return allWords;
	}
	
	public int getGamesGenerated()
	{
		return games;
	}
	
	public void generateGame()
	{
		server.getLog().log("Generating new game...");
		games++;
		int allWords = 0;
		int wordsFailed = 0;
		array = new char[width][height];
		/*** Array vorbelegen ***/
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
				array[i][j] = ' ';
		}
		HashMap<Integer, ArrayList<String>> wwords = loadDictonary();
		for(int i = maxWordLen; i >= 2; i--)
		{
			/*** Absteigend der grösse nach wörter auswählen ***/
			Possibility bestPos = null;
			ArrayList<String> words = wwords.get(i); //Alle W�rter dieser L�nge
			int index = 0;
			int lastProgress=-1;
			if(words != null)
			for(int j = 0; j < words.size(); j++) //Alle W�rter durchlaufen
			{	
				bestPos = null;
				allWords ++;
				String word = words.get(j); //Aktuelles Wort
				int sx = (int) (Math.random()*width);
				int sy = (int) (Math.random()*height);
				int progress = (int)((index/(float)dictSize)*100);
				if(progress%2 == 0 && progress != lastProgress)
				{
					System.out.print("\rProgress: "+progress+"%\r");
					lastProgress = progress;
				}
				index++;
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
										/*** Wort hat hierhin gepasst ***/
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
						//this.allSolutions.push(new Solution(word, bestPos.x, bestPos.y, bestPos.x + bestPos.xdir * (word.length-1), bestPos.y + bestPos.ydir * (word.length-1)));
					}
					else  wordsFailed++;
				}
			}
			origWordCount = this.allWords.size();
		}
		server.getLog().log(wordsFailed+"/"+allWords);
		/*** Array füllen ***/
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
				if(this.array[i][j] == ' ')
				{
					this.array[i][j] = chars.charAt((int)(Math.random()*chars.length()));
				}
		}
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
			int inc = (int)(25*(this.origWordCount - this.allWords.size())/(float)(this.origWordCount) + 1);
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
			// TODO Auto-generated catch block
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
		users.add(user);
	}
	
	public void leave(User user)
	{
		users.remove(user);
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
}
