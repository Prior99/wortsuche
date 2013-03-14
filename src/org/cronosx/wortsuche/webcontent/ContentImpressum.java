package org.cronosx.wortsuche.webcontent;

import org.cronosx.cgi.Content;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.components.ComponentHeadline;
import org.cronosx.cgi.components.ComponentText;

public class ContentImpressum extends Content
{

	public ContentImpressum(Page page)
	{
		super(page);
		add(new ComponentHeadline("Impressum",1));
		add(new ComponentText(
			"Verantwortlicher und Entwickler: Frederick Gnodtke\n"+
			"Bei Fragen oder Problemen können Sie unter (info@cronosx.de) mit mir Kontakt aufnehmen\n"+
			"Ich übernehme für die Inhalte dieser Webseite die Haftung jedoch nicht für den Inhalt von verlinkten Webseiten, " +
			"zufällig generierten Wörtern oder Ausdrücken oder von Spielern erzeugtem Inhalt.\n" +
			"\n" +
			"Dies ist ein quelloffenes Projekt, der Quellcode kann unter <a href='http://hg.cronosx.de/'>hg.cronosx.de</a> heruntergeladen werden " +
			"und ist mit <a href='http://www.gnu.org/licenses/gpl.html'>GNU GPL 3.0</a> lizensiert\n" +
			"\n" +
			"Die verwendete Schriftart ist <a href='http://www.exljbris.com/fontinsans.html'>FontinSans</a> unter \"exljbris Font Foundry Free Font License Agreement\" in der Version vom 14.03.2013 lizensiert\n" +
			"\n" +
			"Mit der registration akzeptiert der Spieler, nicht durch Manipulation des Quellcodes oder anderen Mitteln den Spielablauf zu verändern, \n" +
			"oder mit böswilligen Absichten zu versuchen, dem Betreiber zu schaden.\n" +
			"\n" +
			"Über Bugmeldungen oder Vorschläge freue ich mich unter (issues@cronosx.de)"));
	}
	
}
