import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class simMemoryManager
{
	private BigInteger RAM;
	private BigInteger pageSize;
	private BigInteger osSize;
	private simLog log;
	private int physicalFrames;
	private LinkedList<Integer> freeFrames;
	private HashSet<Integer> reservedFrames;
	
	public simMemoryManager(scenario scen, simInterrupt interrupts, simLog log)
	{
		this.RAM = scen.getMemoryRAM();
		this.pageSize = scen.getMemoryPageSize();
		this.osSize = scen.getMemoryOSsize();
		//Computer physical frames
		this.physicalFrames = (this.RAM.divide(this.pageSize)).intValue();
		//LinkedList of free frames
		this.freeFrames = new LinkedList<Integer>();
		//Initialize the memory with all free frames.reserved frames
		initializeMemory(physicalFrames);
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.MEM_MGR_INSTR, this);
		this.log = log;
		log.println("simMemoryManager.constructor: Physical Frames: " + physicalFrames);
		log.println("simMemoryManager.constructor: First free frame: " + freeFrames.peek());
		log.println("simMemoryManager.constructor: free physical frames; allocate OS space.");
	}
	
	//Public getter for the free frames list
	public LinkedList<Integer> getFreeFrames() {
		return this.freeFrames;
	}
	
	public int getPageSize() {
		return this.pageSize.intValue();
	}
	
	/*
	 * Purpose: Add free frames to the free frames list and then reserve OS frames
	 * Inputs: int physicalFrames - number of physical frames available
	 * Post-Conditions: All memory structures are initialized
	 */
	private void initializeMemory(int physicalFrames) {
		for(int i = 0; i < physicalFrames; i++) {
			freeFrames.add(i);
		}
		int osFrames;
		if(osSize.compareTo(pageSize) == -1) {
			osFrames = 1;
		}
		else {
			osFrames = osSize.divide(pageSize).intValue();
		}
		System.out.println("simMemoryManager.constructor: OSFrames: " +  osFrames);
		for(int i = 0; i < osFrames; i++) {
			freeFrames.poll();
		}
	}

	//purpose: Map pages to frames as part of process creation.
	//assumptions: None.
	//inputs: pcb - the simPCB for the process requesting creation of its process space.
	//post-conditions: Logical pages for pcb has been mapped to physical frames.
	public void createProcessMemorySpace(simPCB pcb)
	{
		log.println("Free frames available: " + freeFrames.size());
		if(freeFrames.size() != 0) {
			//Total pages for process size
			int numPages = pcb.getImageSize().divide(pageSize).intValue();
			log.println("simMemoryManager.createProcessMemorySpace: nbrPages: " + numPages);
			//Build the ArrayList
			ArrayList<Integer> pageTable = new ArrayList<Integer>(numPages);
			//Fill the list with pages
			log.println("simMemoryManager.constructor: Physical Frames: " + physicalFrames);
			for(int i = 0; i < numPages; i++) {
				if(freeFrames.isEmpty()) {
					break;
				}
				//Take the first free frame
				int currentFrame = freeFrames.poll();
				//Add page to the table
				pageTable.add(currentFrame);
			}
			//Set the page table for the pcb
			pcb.setPageTable(pageTable);
			log.println("simMemoryManager.createProcessMemorySpace: create page table; map pages to frames.");
		}
	}

	//purpose: Allocate or free memory.
	//assumptions: None.
	//inputs: data - a simCPUInstruction object that contains either a MEMA or MEMF instruction.
	//post-conditions: For MEMA, frames have been dynamically allocated to the executingPCB.
	//		For MEMF, frames have been dynamically freed for the executingPCB.
	public void interruptServiceRoutine(Object data)
	{
		if (data instanceof simInterruptMEM)
		{
			simInterruptMEM interruptMEM = (simInterruptMEM)data;
			simCPUInstruction instruction = interruptMEM.getInstruction();
			
			simPCB pcb = interruptMEM.getPCB();
			log.println("simMemoryManager.interruptServiceRoutine " + interruptMEM.getInstruction() +
				" pcbNumber=" + pcb.getNumber());
		}
		else
			log.println("simMemoryManager.interruptServiceRoutine unknown data");
	}

	//purpose: Process is being terminated, return all allocated frames back to free list.
	//assumptions: Called by process manager when process is being terminated.
	//inputs: None.
	//post-conditions: Any frames allocated to this process now on free list.
	public void terminateProcess(simPCB pcb)
	{
		log.println("Free frames before termination: " + freeFrames.size());
		//Free the frames
		PageTable table = pcb.getPageTable();
		//Remove all frames found in page table
		for(int p: table.getPageTable()) {
			freeFrames.add(p);
		}
		log.println("Free frames after termination: " + freeFrames.size());
	}
}
