import java.lang.NumberFormatException;
import java.lang.SecurityException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class simStorageManager extends Thread
{
	private int port;
	private simInterrupt interrupts;
	private simLog log;

	public simStorageManager(int port, simInterrupt interrupts, simLog log)
	{
		this.port = port;
		this.interrupts = interrupts;
		this.log = log;
	}
	//purpose: Run the storage manager.
	//assumptions: None.
	//inputs: None.
	//post-conditions: simulated OS environment has been stopped.
	public void run()
	{
	}

	//purpose: Return name of this class.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Return name of this class.
	public String toString()
	{
		return "simStorageManager";
	}
}
