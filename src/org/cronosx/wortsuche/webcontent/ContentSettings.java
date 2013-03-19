package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.*;

public class ContentSettings extends Content
{

	public ContentSettings(Page page)
	{
		super(page);
		add(new ComponentHeadline("Einstellungen", 1));
		add(new ComponentText("Hier kannst du die Farbe einstellen, in der du erscheinst"));
		ComponentDiv color = new ComponentDiv("colorwrapper", "colorwrapper");
		color.add(new ComponentDiv("red", "red"));
		color.add(new ComponentDiv("green", "green"));
		color.add(new ComponentDiv("blue", "blue"));
		color.add(new ComponentDiv("color", "color"));
		color.add(new ComponentDiv("clear","clear"));
		color.add(new ComponentRaw("Farbauswahl speichern: "));
		color.add(new ComponentInputButton("csub", "Speichern", "saveColor();"));
		add(color);
		add(new ComponentJavascript("settings();"));
	}
	
}
