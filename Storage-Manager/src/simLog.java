import java.io.File;
import java.io.PrintWriter;

public class simLog
{
	private PrintWriter log;
	private String logType;
	public final static String CONSOLE = "console";
	public final static String FILE = "file";

	public simLog(String filename, String logType)
	{
		this.logType = logType;
		try
		{
			if (logType.equals(FILE))
			{
				log = new PrintWriter(new File(filename));
				System.out.println("simLog: " + filename);
			}
			else
			{
				log = null;
				logType = CONSOLE;
				System.out.println("simLog: System.out");
			}
		}
		catch (Exception ex)
		{
			log = null;
		}
	}

	public void close()
	{
		if (log != null && logType.equals(FILE))
			log.close();
	}

	public synchronized void println(String entry)
	{
		if (logType.equals(CONSOLE))
			System.out.println(entry);
		else if (log != null)
			log.println(entry);
	}
}
