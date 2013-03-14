package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.ComponentJavascript;

public class ContentLogout extends Content
{

	public ContentLogout(Page page)
	{
		super(page);
		add(new ComponentJavascript(
			"logout();" +
			"window.location.href = 'login';"
		));
	}
	
}
