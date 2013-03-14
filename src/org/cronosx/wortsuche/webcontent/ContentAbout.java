package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.ComponentHeadline;
import org.cronosx.cgi.components.ComponentText;

public class ContentAbout extends Content
{

	public ContentAbout(Page page)
	{
		super(page);
		add(new ComponentHeadline("WSWS - websocketWortsuche",1));
		add(new ComponentText(
			"wsWS - websocketWortsuche ist ein Klon des beliebten und sicherlich jedem bekannten Spiel \"Wortsuche\"." +
			"Ziel des Spieles ist es, im dem Buchstabensalat alle angegebenen Wörter zu finden und zu markieren.\n" +
			"In Dieser Version des Spiels spielen alle miteinander an einem Rätsel. Je mehr Wörter man findet, desto höher " +
			"steigt man in der Highscore.\n" +
			"Zum Spielen ist nur ein moderner Browser (Firefox, Chrome, ...) von Nöten. Wer kein Fan von technischen Details oder" +
			"einführenden Worten ist, sollte sich einfach <a style='color: red; font-weight: bold;' href='register'>hier</a> registrieren und gleich loslegen.\n" +
			"\n" +
			"wsWS ist auf der Browserseite komplett in HTML5 und Javascript geschrieben, für die Kommunikation zwischen Browser und " +
			"Server kommen sogenannte Websockets zum einsatz. Auf Serverseite läuft eine eigene Java-Konstruktion. Der gesamte Quellcode ist offen und kann unter " +
			"<a href='http://hg.cronosx.de/'>hg.cronosx.de</a> heruntergeladen werden.\n" +
			"wsWS ist ein Projekt der CronosX - Developmentgroup"));
	}
	
}
