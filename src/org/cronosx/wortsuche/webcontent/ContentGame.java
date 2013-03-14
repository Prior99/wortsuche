package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentGame extends Content
{

	public ContentGame(Page page)
	{
		super(page);
		add(new ComponentCanvas("canvas", 10, 10));
		add(new ComponentDiv("selection","selBox"));
		add(new ComponentDiv("info","infoBoxSmall"));
		add(new ComponentDiv("words","infoBox"));
		add(new ComponentDiv("","clear"));
		add(new ComponentJavascript(
				"var search = new Wordsearch(document.getElementById('canvas'));"+
				"getWebsocket().addConnHandler( function() {search.initialize();} );"
			));
	}
}
