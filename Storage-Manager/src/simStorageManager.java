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
			ServerSocket socket = new ServerSocket(80);
			Socket client = socket.accept();
			InputStream in = client.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			
			String line;
			boolean term = false;
			while(term == false) {
				line = bin.readLine();
				if(line != null && line.equals("TERM")) {
					log.println("Client received msg: " + line);
					term = true;
					break;
				}
				log.println("Client received msg: " + line);
				String sleepTimeString = findSleepTime(line);
				String pcbString = findPCB(line, sleepTimeString.length());
				int sleepTime = Integer.parseInt(sleepTimeString);
				int pcbNumber = Integer.parseInt(pcbString);
				sleepTime(sleepTime*simOS.DEVICE_PAUSE_TIME);
				interrupts.addInterrupt(simInterrupt.INTERRUPT.STRG_MGR_DONE, pcbNumber);
			}
			client.close();
		}
		catch(IOException ex) {
			
		}
	}
	
	public static String findSleepTime(String line) {
		String num = "";
		for(int i = 5; i < line.length(); i++) {
			if(!line.substring(i,i+1).equals(" ")) {
				num += line.substring(i, i+1);
			}
			else {
				break;
			}
		}
		return num;
	}
	
	public static String findPCB(String line, int sleepTimeLength) {
		String num = "";
		for(int i = 5 + sleepTimeLength; i < line.length(); i++) {
			if(!line.substring(i,i+1).equals(" ")) {
				num += line.substring(i, i+1);
			}
		}
		return num;
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
