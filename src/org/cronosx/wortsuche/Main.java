package org.cronosx.wortsuche;

public class Main
{
	public static void main(String[] args)
	{
		final ServerWortsuche server = new ServerWortsuche();
		server.start();
		Thread t = new Thread()
		{
			public void run()
			{
				try
				{
					Thread.sleep(server.getConfig().getInt("Export-Timeout", 3600)*1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				server.getLog().log("Starting scheduled export to database",50);
				server.save();
			}
		};
		t.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				server.getLog().log("Received CTRL+C, saving all remaining data");
				server.save();
				server.shutDown();
			}
		});
	}
}
