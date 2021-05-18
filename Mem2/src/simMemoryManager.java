import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;

public class simMemoryManager
{
	private BigInteger RAM;
	private BigInteger pageSize;
	private BigInteger osSize;
	private simLog log;

	//added as part of solultion
	private simMemoryManagerFree memFree;
	private simVirtualMemoryManager virtMem;
	private ArrayList<Integer> dynamicallyAllocatedFrames;


	public simMemoryManager(scenario scen, simInterrupt interrupts, simLog log)
	{
		this.RAM = scen.getMemoryRAM();
		this.pageSize = scen.getMemoryPageSize();
		this.osSize = scen.getMemoryOSsize();
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.MEM_MGR_INSTR, this);
		this.log = log;

		//added as part of solution
		this.virtMem = new simVirtualMemoryManager(log);
		memFree = new simMemoryManagerFree(RAM, pageSize, osSize, log, virtMem);
		dynamicallyAllocatedFrames = new ArrayList<Integer>();
	}

	//purpose: Map pages to frames as part of process creation.
	//assumptions: None.
	//inputs: pcb - the simPCB for the process requesting creation of its process space.
	//post-conditions: Logical pages for pcb has been mapped to physical frames.
	public void createProcessMemorySpace(simPCB pcb)
	{
		//added as part of solution
		pcb.createPageTable(this);
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
			//Need to allocation/deallocate memory dynamically, based on the MEM instruction.
			if(instruction.getOpcode().equals(simCPUInstruction.OPCODE.MEMA)) {
				log.println("simMemoryManager.interruptServiceRoutine: dynamic memory allocation");
				pcb.dynamicMemoryAllocation(this, instruction);
			}
			else if(instruction.getOpcode().equals(simCPUInstruction.OPCODE.MEMF)) {
				log.println("simMemoryManager.interruptServiceRoutine: dynamic memory deallocation");
				pcb.dynamicMemoryDeallocation(virtMem, this, instruction);
			}
			else {
				log.println("simMemoryManager.interruptServiceRoutine: unknown instruction request");
			}
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
		//added as part of solution
		log.println("simMemoryManager.terminateProcess: number of free frames (before)=" + memFree.getNumberOfFreeFrames());
		pcb.freeAllProcessFrames(memFree);
		log.println("simMemoryManager.terminateProcess: number of free frames (after)=" + memFree.getNumberOfFreeFrames());
		
		//Need to clean up the swap file (i.e., virtual memory manager) for this terminating process.
		virtMem.clear();
	}

/*  ADDED as part of solution to memory manager assignment */

	public simMemoryManagerFree getMemoryManagerFree()
	{
		return memFree;
	}

	//purpose: Get one free frame number.
	//assumptions: memFree object exists; called by simPCB.
	//inputs: None.
	//post-conditions: Returns a free frame number.
	public Integer getFreeFrameNumber(simPCB pcb)
	{
		return memFree.getFreeFrameNumber(pcb);
	}

	//purpose: Return number of free frames.
	//assumptions: called by simPCB (multiple times).
	//inputs: None.
	//post-conditions: Return number of free frames.
	public int getNumberOfFreeFrames()
	{
		return memFree.getNumberOfFreeFrames();
	}

	//purpose: Return simulation page size.
	//assumptions: called by simPCB.
	//inputs: None.
	//post-conditions: Return simulation page size.
	public BigInteger getPageSize()
	{
		return pageSize;
	}
	
	public void addDynamicFrame(int frameNumber) {
		this.dynamicallyAllocatedFrames.add(frameNumber);
	}
	
	public Integer removeDynamicFrame() {
		if(dynamicallyAllocatedFrames.size() == 0) {
			return null;
		}
		return this.dynamicallyAllocatedFrames.remove(0);
	}
}
