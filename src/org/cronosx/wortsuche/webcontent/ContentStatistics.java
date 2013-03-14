package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;
import org.cronosx.wortsuche.ServerWortsuche;

public class ContentStatistics extends Content
{

	public ContentStatistics(Page page)
	{
		super(page);
		add(new ComponentHeadline("Statistiken",1));
		add(new ComponentText(
			"Seiten generiert: " + page.getCGI().getPagesGenerated() + "\n" +
			"Generieren dieser Seite dauerte: " + page.getCGI().getLastTimeSpentgenerating() + "\n" +
			"Generieren dauerte im Schnitt: " + page.getCGI().getAverageTimeSpentgenerating() + "\n" +
			"Spiele generiert: " + ((ServerWortsuche)page.getWebserver().getServer()).getGame().getGamesGenerated() + "\n" +
			"Spieler online: " + ((ServerWortsuche)page.getWebserver().getServer()).getUserManager().getUsers() + "\n"+
			"Wörterbuchgrösse: " + ((ServerWortsuche)page.getWebserver().getServer()).getGame().getDictonarySize()
		));
	}
	
}
