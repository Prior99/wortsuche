package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentRegister extends Content
{

	public ContentRegister(Page page)
	{
		super(page);
		ComponentFormJavascript form = new ComponentFormJavascript();
		form.addEntry("Benutzername", new ComponentInputText("username"))
			.addValidator(new InputValidatorLength(3,16));
		ComponentInput pw1 =  new ComponentInputPassword("password1");
		form.addEntry("Passwort",pw1)
			.addValidator(new InputValidatorLength(5));
		form.addEntry("Wiederholen", new ComponentInputPassword("password2"))
			.addValidator(new InputValidatorEquals(pw1));
		form.addEntry("Registrieren", new ComponentInputButton("submit", "Okay", "register();"))
			.addValidator(new InputValidatorAll(form));
		add(form);
		add(new ComponentJavascript(
			"function register()" +
			"{" +
				"getWebsocket().runCommand('register', [$('#username').val(), $('#password2').val(), $('#password2').val()])" +
			"};" +
			
			"addWebsocketHandler('error', function (msg)" +
			"{" +
				"showMessage(MSG_ERROR, msg, 'removeMessage();')" +
			"});"+
			
			"addWebsocketHandler('success', function (msg)" +
			"{" +
				"showMessage(MSG_OK, msg, 'document.location.href = \"login\"')" +
			"});"
		));
	}
	
}
