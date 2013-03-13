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
		add(new ComponentDiv("selection","infoBox"));
		add(new ComponentDiv("words","infoBox"));
		add(new ComponentJavascript(
				"var search = new Wordsearch(document.getElementById('canvas'));"+
				"search.initialize();"
			));
	}
}
