package org.cronosx.wortsuche;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;

import org.cronosx.tools.LimitedQueue;

public class Game
{
	private final ServerWortsuche server;
	private char[][] array;
	private List<String> allWords;
	private List<Selection> selections;
	private List<User> users;
	private List<ClientWrapper> clients;
	private int width = 68;
	private int height = 43;
	private int maxWordLen;
	private int origWordCount = 0;
	private int dictSize = 0;
	private int games = 0;
	private int started = 0;
	private Thread timer;
	private final LimitedQueue<Message> chatBuffer;
	
	private final ReentrantLock clientsLock;
	private final ReentrantLock dataLock;
		
	public Game(ServerWortsuche server)
	{
		users = new ArrayList<>();
		clients = new LinkedList<>();
		dataLock = new ReentrantLock();
		clientsLock = new ReentrantLock();
		chatBuffer = new LimitedQueue<>(64);
		this.server = server;
		if(!this.loadFromDisk()) 
		generateGame();
	}
	
	public void registerClient(Socket s)
	{
		try
		{
			clientsLock.lock();
			clients.add(new ClientWrapper(s, server));
		}
		finally
		{
			clientsLock.unlock();
		}
	}
	
	public User[] getUsers()
	{
		User[] us;
		try
		{
			dataLock.lock();
			us = new User[this.users.size()];
			users.toArray(us);
		}
		finally
		{
			dataLock.unlock();
		}
		return us;
	}
	
	public String[] getWords()
	{
		String[] ws = new String[allWords.size()];
		try
		{
			allWords.toArray(ws);
			dataLock.lock();
			
		}
		finally
		{
			dataLock.unlock();
		}
		return ws;
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
		try
		{
			dataLock.lock();
			clientsLock.lock();
			started = (int) (System.currentTimeMillis()/1000);
			maxWordLen = 0;
			origWordCount = 0;
			dictSize = 0;
			selections = new ArrayList<Selection>();
			allWords = new ArrayList<String>();
			System.out.println("Generating new game...");
			games++;
			int allWords = 0;
			int wordsFailed = 0;
			array = new char[width][height];
			for(int i = 0; i < width; i++)
			{
				for(int j = 0; j < height; j++)
					array[i][j] = ' ';
			}
			Map<Integer, ArrayList<String>> wwords = loadDictonary();
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
			System.out.println(wordsFailed+"/"+allWords);
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
			for(ClientWrapper  cw : clients)
			{
				cw.notifyGameChange();
			}
			final Game self = this;
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
						return;
					}
					self.generateGame();
				}
			};
			timer.start();
		}
		finally
		{
			dataLock.unlock();
			clientsLock.unlock();
		}
	}
	
	public void shutdown()
	{
		try
		{
			clientsLock.lock();
			for(ClientWrapper cw : clients)
				cw.shutdown();
		}
		finally
		{
			clientsLock.unlock();
		}
		timer.interrupt();
	}
	
	public int getRuntime()
	{
            return server.getConfig().getGameTime();
	}
	
	public int getDictonarySize()
	{
		return dictSize;
	}
	
	public int getOriginalWordCount()
	{
		return this.origWordCount;
	}
	
	public void removeWord(String word, int x1, int y1, int x2, int y2, ClientWrapper cw)
	{
		try
		{
			clientsLock.lock();
			dataLock.lock();
			if(word.equals(getSelection(x1, y1, x2, y2)) && allWords.contains(word))
			{
				int inc = (int)(server.getConfig().getScore()*(this.origWordCount - this.allWords.size())/(float)(this.origWordCount) + 1);
				for(ClientWrapper cw2 : clients)
				{
					cw2.sendRemoveWord(word, x1, y1, x2, y2, cw.getUser().getColor());
				}
				this.allWords.remove(word);
				this.selections.add(new Selection(x1, y1, x2, y2, cw.getUser().getColor()));
				if(this.allWords.isEmpty())
				{
					generateGame();
					for(ClientWrapper cw2 : clients) 
						cw2.sendGame();
				}
				cw.getUser().incScore(inc);
				cw.sendScore();
				cw.sendScoreInc(inc);
			}
		}
		finally
		{
			dataLock.unlock();
			clientsLock.unlock();
		}
	}
	
	private String getSelection(int x1, int y1, int x2, int y2)
	{
		String string = "";
		try
		{
			dataLock.lock();
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
		}
		finally
		{
			dataLock.unlock();
		}
		return string;
	}
	
	private int getDir(int x1, int x2)
	{
		return  x1 < x2 ? 1 : x1 == x2 ? 0 : -1;
	};
	
	private Map<Integer, ArrayList<String>> loadDictonary()
	{
		Map<Integer, ArrayList<String>> dict = new HashMap<>();
		try
		{
			dataLock.lock();
			try
			{
				BufferedReader rd = new BufferedReader(new FileReader(new File("dictonary.txt")));
				String s;
				while((s = rd.readLine()) != null)
				{
					if(s.length() > this.maxWordLen) this.maxWordLen = s.length();
					if(!dict.containsKey(s.length())) dict.put(s.length(), new ArrayList<String>());
					dict.get(s.length()).add(s);
					this.dictSize++;
				}
				rd.close();
			}
			catch(FileNotFoundException e)
			{
				System.out.println("WARNING! dictonary.txt not found. unable to generate game!");
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}

			for(int i:dict.keySet())
			{
				shuffle(dict.get(i));
			}
		}
		finally
		{
			dataLock.unlock();
		}
		return dict;
	}
	
	private void shuffle(List<String> array)
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
		try
		{
			dataLock.lock();
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
		finally
		{
			dataLock.unlock();
		}
	}
	
	private void apply(String word, int x, int y, int xdir, int ydir)
	{
		try
		{
			dataLock.lock();
			for(int i = 0; i < word.length(); i++)
			{
				this.array[x + xdir * i][y + ydir * i] = word.charAt(i);
			}
		}
		finally
		{
			dataLock.unlock();
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
		try
		{
			dataLock.lock();
			if(!users.contains(user))
				users.add(user);
		}
		finally
		{
			dataLock.unlock();
		}
		try
		{
			clientsLock.lock();
			for(ClientWrapper cw : clients)
			{
				cw.sendUsers();
			}
		}
		finally
		{
			clientsLock.unlock();
		}
	}
	
	public Message[] getChatBuffer()
	{
		Message[] msgs = new Message[chatBuffer.size()];
		try
		{
			dataLock.lock();
			chatBuffer.toArray(msgs);
		}
		finally
		{
			dataLock.unlock();
		}
		return msgs;
	}
	
	public void leave(User user)
	{
		try
		{
			dataLock.lock();
			users.remove(user);
		}
		finally
		{
			dataLock.unlock();
		}
		try
		{
			clientsLock.lock();
			for(ClientWrapper cw : clients)
			{
				cw.sendUsers();
			}
		}
		finally
		{
			clientsLock.unlock();
		}
	}
	
	public void chat(String msg, String user, Color userColor)
	{
		try
		{
			dataLock.lock();
			int time = (int)(System.currentTimeMillis() / 1000);
			chatBuffer.add(new Message(msg, user, userColor, time));
			for(ClientWrapper cw : clients)
			{
				cw.sendChat(user, userColor, msg, time);
			}
		}
		finally
		{
			dataLock.unlock();
		}
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
		char[][] copy;
		try
		{
			dataLock.lock();
			copy = Arrays.copyOf(array, array.length);
		}
		finally
		{
			dataLock.unlock();
		}
		return copy;
	}
	
	public class Selection
	{
		public int x1;
		public int x2;
		public int y1;
		public int y2;
		public Color color;
		public Selection(int x1, int y1, int x2, int y2, Color color)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.color = color;
		}
	}
	
	public Selection[] getSelections()
	{
		Selection[] sels = new Selection[selections.size()];
		try
		{
			dataLock.lock();
			selections.toArray(sels);
			
		}
		finally
		{
			dataLock.unlock();
		}
		return sels;
	}

	
	public void saveToDisk()
	{
		try
		{
			dataLock.lock();
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
				stream.writeShort(selections.get(i).color.getR());
				stream.writeShort(selections.get(i).color.getG());
				stream.writeShort(selections.get(i).color.getB());
			}
			stream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
		finally
		{
			dataLock.unlock();
		}
	}
	
	public boolean loadFromDisk()
	{
		boolean ret;
		File f = new File("game.sav");
		if(f.exists())
		{
			try
			{
				dataLock.lock();
				DataInputStream stream;
				try
				{
					System.out.println("Importing game...");
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
						selections.add(new Selection(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt(), 
								new Color(stream.readShort(), stream.readShort(), stream.readShort())));
					}
					stream.close();
					f.delete();
					started = (int) (System.currentTimeMillis()/1000);
					final Game self = this;
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
								return;
							}
							self.generateGame();
						}
					};
					timer.start();
					System.out.println("Done!");
				}
				catch(IOException e)
				{
					e.printStackTrace();
					ret = false;
				}
				ret = true;
			}
			finally
			{
				dataLock.unlock();
			}
		}
		else ret = false;
		return ret;
	}
	
	class Message
	{
		public String msg;
		public String user;
		public Color color;
		public int time;
		public Message(String msg, String user, Color color, int time)
		{
			this.msg = msg;
			this.user = user;
			this.color = color;
			this.time = time;
		}
	}
}
