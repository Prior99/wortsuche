package org.cronosx.wortsuche.webcontent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentBlog extends Content
{

	public ContentBlog(Page page)
	{
		super(page);
		add(new ComponentHeadline("Entwicklerblog", 1));
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy - HH:mm");
			PreparedStatement stmt = page.getWebserver().getServer().getDatabaseConnection().getPreparedStatement("SELECT ID, Content, Created, Headline FROM Blog ORDER BY Created DESC LIMIT 0,15");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while(rs.next())
			{
				String head = rs.getString("Headline");
				String content = rs.getString("Content");
				Date date = new Date(rs.getInt("Created")*(long)1000);
				ComponentDiv entry = new ComponentDiv("id"+rs.getInt("ID"), "blogentry");
				ComponentDiv time = new ComponentDiv("blogtime");
				time.add(new ComponentRaw(sdf.format(date)));
				entry.add(time);
				entry.add(new ComponentHeadline(head, 2));
				ComponentDiv cont = new ComponentDiv("blogcontent");
				cont.add(new ComponentText(content));
				entry.add(cont);
				add(entry);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

}
