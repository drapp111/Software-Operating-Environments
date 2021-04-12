import java.math.BigInteger;

public class simMemoryManager
{
	private BigInteger RAM;
	private BigInteger pageSize;
	private BigInteger osSize;
	private simLog log;
	private BigInteger physicalFrames;
	
	public simMemoryManager(scenario scen, simInterrupt interrupts, simLog log)
	{
		this.RAM = scen.getMemoryRAM();
		this.pageSize = scen.getMemoryPageSize();
		this.osSize = scen.getMemoryOSsize();
		//Computer physical frames
		this.physicalFrames = (this.RAM.divide(this.pageSize)).subtract(BigInteger.valueOf(1));
		//Reserve frames for OS
		physicalFrames.subtract(osSize.divide(pageSize));
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.MEM_MGR_INSTR, this);
		this.log = log;
		log.println("simMemoryManager.constructor: free physical frames; allocate OS space.");
	}

	//purpose: Map pages to frames as part of process creation.
	//assumptions: None.
	//inputs: pcb - the simPCB for the process requesting creation of its process space.
	//post-conditions: Logical pages for pcb has been mapped to physical frames.
	public void createProcessMemorySpace(simPCB pcb)
	{
		
		log.println("simMemoryManager.createProcessMemorySpace: create page table; map pages to frames.");
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
		log.println("simMemoryManager.terminateProcess");
	}
}
