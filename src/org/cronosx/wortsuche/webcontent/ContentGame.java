package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentGame extends Content
{

	public ContentGame(Page page)
	{
		super(page);
		ComponentDiv div = new ComponentDiv("wrapper", "wrapper");
		div.add(new ComponentCanvas("canvas", 10, 10));
		add(div);
		add(new ComponentDiv("selection","selBox"));
		add(new ComponentDiv("info","infoBoxSmall"));
		add(new ComponentDiv("words","infoBox"));
		add(new ComponentDiv("timer","infoBoxSmall"));
		add(new ComponentDiv("clear","clear"));
		ComponentDiv loading = new ComponentDiv("loading", "loading");
		loading.addSubComponent(new ComponentImage("style/img/loading.gif"));
		loading.addSubComponent(new ComponentText("Laden..."));
		loading.addSubComponent(new ComponentDiv("status", "status"));
		add(loading);
		add(new ComponentJavascript(
			"var search = new Wordsearch(document.getElementById('canvas'));"+
			"getWebsocket().addConnHandler( function() {search.initialize();} );"
		));
		add(new ComponentJavascript(
			"addWebsocketHandler('error', function (msg)" +
			"{" +
				"showMessage(MSG_ERROR, msg, 'removeMessage();')" +
			"});"
		));
	}
}
