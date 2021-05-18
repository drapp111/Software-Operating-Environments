import java.math.BigInteger;
import java.util.LinkedList;

public class simMemoryManagerFree
{
	private simLog log;
	private int firstFreeFrame;
	private int numberRAMFrames;
	private LinkedList<Integer> freeFrameQueue;
	
	//Added
	simVirtualMemoryManager virtMem;

	//purpose: 
	//assumptions: this.RAM % this.pageSize == 0;
	//			this.RAM > this.osSize; 
	//			8GB / 512 (largest RAM / smallest pageSize) == 16,777,216 (fits within 32-bit int)
	//			8GB == 8,589,934,592 == 8 * 1024 * 1024 * 1024
	//inputs: None.
	//post-conditions: firstFreeFrame and lastFreeFrame are set.
	//			freeFrameQueue created but empty.
	public simMemoryManagerFree(BigInteger RAM, BigInteger pageSize, BigInteger osSize, simLog log, simVirtualMemoryManager virtMem)
	{
		this.log = log;
		this.virtMem = virtMem;
		freeFrameQueue = new LinkedList<Integer>();
		BigInteger nbrRAMFrames, nbrOSFrames, nbrFreeFrames;
		BigInteger[] divideRemainder;
		
		try
		{
			//Compute total number of RAM frames
			divideRemainder = RAM.divideAndRemainder(pageSize);
			if (divideRemainder[1].compareTo(BigInteger.ZERO) != 0)
				log.println("simMemoryManagerFree.constructor Logic error: RAM not factor of pageSize.");
			nbrRAMFrames = divideRemainder[0];

			//Compute number of frames to reserve for OS
			divideRemainder = osSize.divideAndRemainder(pageSize);
			if (divideRemainder[1].compareTo(BigInteger.ZERO) == 1)
				nbrOSFrames = divideRemainder[0].add(BigInteger.ONE);
			else
				nbrOSFrames = divideRemainder[0];

			//Save first free frame number and total number of RAM frames.
			firstFreeFrame = nbrOSFrames.intValueExact();
			numberRAMFrames = nbrRAMFrames.intValueExact();
			
			log.println("simMemoryManagerFree.constructor: nbrRAMFrames=" + nbrRAMFrames.toString() +
					" nbrOSFrames=" + nbrOSFrames.toString());
			log.println("simMemoryManagerFree.constructor: numberRAMFrames=" + numberRAMFrames +
					" firstFreeFrame=" + firstFreeFrame +
					" number of free frames=" + (numberRAMFrames-firstFreeFrame));
		}
		catch (Exception ex)
		{
			log.println("simMemoryManagerFree.constructor Exception: " + ex);
		}
	}

	//purpose: Add free frame to list of free frames.
	//assumptions: freeFrameQueue object exists; called by simMemoryManager and simPCB.
	//inputs: frameNumber - frame number that is now free.
	//post-conditions: frameNumber added to list of free frames.
	public void addFreeFrame(Integer frameNumber)
	{
		freeFrameQueue.add(frameNumber);
	}

	//purpose: Get one free frame number.
	//assumptions: memFree object exists; called by simMemoryManager.
	//inputs: None.
	//post-conditions: Returns a free frame number OR null when no free frame is available.
	public Integer getFreeFrameNumber(simPCB pcb)
	{
		Integer frameNumber = null;
		if (firstFreeFrame < numberRAMFrames)
		{
			//Use next free frame created from original allocation of OS memory space.
			frameNumber = Integer.valueOf(firstFreeFrame);
			firstFreeFrame++;
		}
		else if (freeFrameQueue.size() > 0)
			//Original list of free frames has been used.
			//Get free frame from linked list.
			frameNumber = freeFrameQueue.removeFirst();
		else
		{
			//No free frames exist.
			//Need to use virtual memory manager to obtain a free frame.
			log.println("simMemoryManagerFree.getFrameNumber: no more free frames; need to use virtual memory");
			//Following statement is temporary. Should be replaced with use of virtual memory manager logic.
			frameNumber = virtMem.pageSwap(pcb);
		}
		return frameNumber;
	}

	//purpose: Return number of free frames.
	//assumptions: called by simMemoryManager.
	//inputs: None.
	//post-conditions: Return number of free frames.
	public int getNumberOfFreeFrames()
	{
		return (numberRAMFrames - firstFreeFrame) + freeFrameQueue.size();
	}
}
