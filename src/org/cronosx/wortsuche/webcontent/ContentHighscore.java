package org.cronosx.wortsuche.webcontent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentHighscore extends Content
{

	public ContentHighscore(Page page)
	{
		super(page);
		add(new ComponentHeadline("Highscore", 1));
		page.getWebserver().getServer().save();
		ArrayList<HighscoreEntry> list = new ArrayList<HighscoreEntry>();
		try
		{
			PreparedStatement stmt = page.getWebserver().getServer().getDatabaseConnection().getPreparedStatement("SELECT Username, Score, R, G, B FROM Users ORDER BY Score DESC");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while(rs.next())
			{
				String color = "rgba("+rs.getInt("R")+","+rs.getInt("G")+","+rs.getInt("B")+",1.0)";
				list.add(new HighscoreEntry(rs.getString("Username"), rs.getInt("Score"), color));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		ComponentTable table= new ComponentTable();
		int i = 0;
		for(HighscoreEntry e:list)
		{
			i++;
			table.addRow(new String[]{i+".", "<span style='color:"+e.color+";'>"+e.username+"</span>", e.score+" Punkte"});
		}
		add(table);
	}	
	
	class HighscoreEntry
	{
		String username;
		int score;
		String color;
		public HighscoreEntry(String username, int score, String color)
		{
			this.score = score;
			this.username = username;
			this.color = color;
		}
	}
}
