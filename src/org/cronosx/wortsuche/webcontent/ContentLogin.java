package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentLogin extends Content
{

	public ContentLogin(Page page)
	{
		super(page);
		// TODO Auto-generated constructor stub
		ComponentFormJavascript form = new ComponentFormJavascript();
		form.addEntry("Benutzername", new ComponentInputText("username"));
		form.addEntry("Passwort", new ComponentInputPassword("password"));
		form.addEntry("Anmelden", new ComponentInputButton("submit", "Okay", "login();"));
		add(form);
		add(new ComponentJavascript(
				"function login()" +
				"{" +
					"getWebsocket().runCommand('login',[$('#username').val(), $('#password').val()]);" +
				"}" +
				
				"addWebsocketHandler('error', function (msg)" +
				"{" +
					"showMessage(MSG_ERROR, msg, 'removeMessage();')" +
				"});"+
				
				"addWebsocketHandler('success', function (msg)" +
				"{" +
					"showMessage(MSG_OK, msg, \"getUser().performLogin($('#username').val(), $('#password').val()); window.location.href = 'game';\");" +
				"});"
		));
	}
	
}
