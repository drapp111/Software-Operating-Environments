import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class simPCB
{
	private String id;
	private int arrivalTime;
	private ArrayList<simCPUInstruction> instructions;
	private simLog log;
	private BigInteger imageSize;	//size of the executable image (which must be loaded into physical memory)
	private int instructionPointer;

	//Statistics on a process
	private int startExecuteTime;
	private int startWaitTime;
	private int totalExecuteTime;
	private int totalWaitTime;
	private int turnaroundTime;

	private int number;	//unique number within scenario
	private static int nextNumber = 1;	//class variable: used to gaurentee each PCB has unique number

	public simPCB(String id, int arrivalTime,
			ArrayList<simCPUInstruction> instructions, simLog log,
			BigInteger imageSize)
	{
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.instructions = instructions;
		this.log = log;
		this.imageSize = imageSize;
		instructionPointer = 0;
		startWaitTime = arrivalTime;
		totalExecuteTime = 0;
		totalWaitTime = 0;
		turnaroundTime = 0;
		number = nextNumber;
		nextNumber++;
	}

	//purpose: Add to the total execute time.
	//assumptions:  None.
	//inputs: currentTime - the current CPU clock time.
	//post-condition: time added to totalExecuteTime.
	public void addExecuteTime(int currentTime)
	{
		int time = currentTime - startExecuteTime;
		totalExecuteTime = totalExecuteTime + time;
		log.println("simPCB.addExecuteTime time=" + time + " totalExecuteTime=" + totalExecuteTime);
	}

	//purpose: Add to the total wait time.
	//assumptions:  None.
	//inputs: currentTime - the current CPU clock time.
	//post-condition: time added to totalWaitTime.
	public void addWaitTime(int currentTime)
	{
		int time = currentTime - startWaitTime;
		totalWaitTime = totalWaitTime + time;
		log.println("simPCB.addWaitTime time=" + time + " totalWaitTime=" + totalWaitTime);
	}

	public String getId()
	{
		return id;
	}

	public int getArrivalTime()
	{
		return arrivalTime;
	}

	public int getExecuteTime()
	{
		return totalExecuteTime;
	}

	public BigInteger getImageSize()
	{
		return imageSize;
	}

	public simCPUInstruction getInstruction()
	{
		simCPUInstruction instruction = null;
		if (instructions.size() > instructionPointer)
			instruction = instructions.get(instructionPointer);
		return instruction;
	}

	public int getInstructionPointer()
	{
		return instructionPointer;
	}

	public int getInstructionsSize()
	{
		return instructions.size();
	}

	public int getNumber()
	{
		return number;
	}

	public int getTurnaroundTime()
	{
		return turnaroundTime;
	}

	public int getWaitTime()
	{
		return totalWaitTime;
	}

	public void removeInstruction()
	{
		instructionPointer++;
	}

	//purpose: Modify arrivalTime so that totalWaitTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: arrivalTime == time;
	public void setArrivalTime(int time)
	{
		arrivalTime = time;
	}

	//purpose: Modify startExecuteTime so that totalExecuteTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: startExecuteTime == time;
	public void setStartExecuteTime(int time)
	{
		startExecuteTime = time;
	}

	//purpose: Modify startWaitTime so that totalWaitTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: startWaitTime == time;
	public void setStartWaitTime(int time)
	{
		startWaitTime = time;
	}

	//purpose: Set the turnarount time.
	//assumptions:  None.
	//inputs: time - 
	//post-condition: time added to turnaroundTime.
	public void setTurnaroundTime(int turnaroundTime)
	{
		this.turnaroundTime = turnaroundTime;
		log.println("simPCB.setTurnaroundTime turnaroundTime=" + this.turnaroundTime);
	}
	
	//purpose: Return string containing this PCB's statistics.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns string showing this PCB's statistics.
	public String statisticsToString()
	{
		return "Id= " + id + " arrivalTime=" + arrivalTime +
			" totalExecuteTime=" + totalExecuteTime +
			" totalWaitTime=" + totalWaitTime +
			" turnaroundTime=" + turnaroundTime;
	}

	//purpose: Return string containing this PCB's basic information.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns string containing this PCB's basic information.
	public String toString()
	{
		String result = "Id= " + id + " arrivalTime=" + arrivalTime +
			" imageSize=" + imageSize + " [";
		Iterator<simCPUInstruction> iter = instructions.iterator();
		while (iter.hasNext())
		{
			result = result + iter.next();
			if (iter.hasNext())
				result = result + ",";
		}
		result = result + "] instructionPointer=" + instructionPointer;
		return result;
	}
}
