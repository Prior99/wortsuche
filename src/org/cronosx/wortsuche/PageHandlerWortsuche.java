package org.cronosx.wortsuche;

import java.io.File;
import java.util.HashMap;

import org.cronosx.cgi.CGI;
import org.cronosx.cgi.DefaultPage;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.PageHandler;
import org.cronosx.cgi.components.ComponentDiv;
import org.cronosx.webserver.Webserver;
import org.cronosx.wortsuche.webcontent.*;

public class PageHandlerWortsuche implements PageHandler
{
	ServerWortsuche server;
	public PageHandlerWortsuche(ServerWortsuche server)
	{
		this.server = server;
	}
	
	@Override
	public Page getPage(String request, String pageID, HashMap<String, String> params, String browserName, HashMap<String, String> cookies, String ip, Webserver webserver,CGI cgi)
	{
		DefaultPage page = new DefaultPage(request, pageID, params, browserName, cookies, ip, webserver, cgi);

		page.getBody().addJSFolderToInclude(new File("lib/"));
		page.getBody().addJSFolderToInclude(new File("config.js"));
		page.getBody().addCSSFolderToInclude(new File("style/"));
		page.getMenu().addSubComponent(new ComponentDiv("userinfo", "userinfo"));
		if(cookies.containsKey("username"))
		{
			page.addPage("game", "Spielen", ContentGame.class);
			page.addPage("logout", "Logout", ContentLogout.class);
			page.addPage("settings", "Einstellungen", ContentSettings.class);
		}
		else
		{
			page.addPage("login", "Login", ContentLogin.class);
			page.addPage("register", "Registrieren", ContentRegister.class);
		}
		page.addPage("highscore", "Highscore", ContentHighscore.class);
		page.addPage("blog", "Blog", ContentBlog.class);
		page.addPage("impressum", "Impressum", ContentImpressum.class);
		page.addPage("stats", "Statistiken", ContentStatistics.class);
		page.addPage("about", "Über", ContentAbout.class);
		
		page.finalize();
		
		return page;
	}
	
}
