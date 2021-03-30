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
			
			//Build socket objects for server interactions
			ServerSocket socket = new ServerSocket(port);
			Socket client = socket.accept();
			//Build input stream and buffered reader to read the server messages
			InputStream in = client.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			//Each line of the message
			String line;
			boolean term = false;
			//Until term instruction is sent
			while(term == false) {
				//Read the line
				line = bin.readLine();
				if(line != null && line.equals("TERM")) {
					term = true;
					break;
				}
				log.println("Client received msg: " + line);
				//Find the sleep time and the pcb bumber
				String sleepTimeString = findSleepTime(line);
				String pcbString = findPCB(line, sleepTimeString.length());
				//Parse the above strings for ints
				int sleepTime = Integer.parseInt(sleepTimeString);
				int pcbNumber = Integer.parseInt(pcbString);
				//Sleep the thread
				sleepTime(sleepTime*simOS.DEVICE_PAUSE_TIME);
				//System interrupt once thread is done sleeping
				interrupts.addInterrupt(simInterrupt.INTERRUPT.STRG_MGR_DONE, pcbNumber);
			}
			//Client close
			client.close();
			//Server close
			socket.close();		}
		catch(IOException ex) {
			log.println("Some error occurred: " + ex);
		}
	}
	
	//Purpose: Retrieve a string format of the sleep time
	//Assumptions: None
	//Inputs: @String line - line to be searched for sleep time
	//Post: @return num - string version of number
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
	
	//Purpose: Retrieve a string format of the pcb number
	//Assumptions: None
	//Inputs: @String line - line to be searched for pcb number
	//Post: @return num - string version of pcb number
	public static String findPCB(String line, int sleepTimeLength) {
		String num = "";
		for(int i = 5 + sleepTimeLength; i < line.length(); i++) {
			if(!line.substring(i,i+1).equals(" ")) {
				num += line.substring(i, i+1);
			}
		}
		return num;
	}
	
	//Purpose: Sleep the thread
	//Assumptions: None
	//Inputs: @int time - time to sleep (ms)
	//Post: none
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
