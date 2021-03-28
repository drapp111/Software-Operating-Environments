import java.lang.NumberFormatException;
import java.lang.SecurityException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(6013);
			
			Socket client = socket.accept();
			InputStream in = client.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			
			String line;
			while( (line = bin.readLine()) != null) {
				if(line.equals("TERM")) {
					break;
				}
				log.println("Client received msg: " + line);
				int sleepTime = Integer.parseInt(line.substring(5, 7));
				int pcbNumber = Integer.parseInt(line.substring(8));
				sleepTime(sleepTime*simOS.DEVICE_PAUSE_TIME);
				interrupts.addInterrupt(simInterrupt.INTERRUPT.STRG_MGR_DONE, pcbNumber);
			}
				
			client.close();
			socket.close();
		}
		catch(IOException ex) {
			
		}
	}
	
	public static void sleepTime(int time) {
		try {
	         Thread.sleep(time);
	    }
	    catch (InterruptedException e) {
	    	  
		}
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
