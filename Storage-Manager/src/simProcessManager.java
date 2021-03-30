import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class simProcessManager
{
	private simCPU cpu;
	private simInterrupt interrupts;
	private simLog log;
	private simCPUScheduler scheduler;
	private simPCB executingPCB;
	private LinkedList<simPCB> deviceQueue;
	private LinkedList<simPCB> readyQueue;
	private LinkedList<simPCB> termQueue;
	private LinkedList<simPCB> waitQueue;
	private int port;
	Socket socket;
	PrintWriter pout;

	public simProcessManager(scenario scen, simCPU cpu, int port, simInterrupt interrupts, simLog log)
	{
		this.cpu = cpu;
		this.interrupts = interrupts;
		//register interrupts to be service by (this) process manager.
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.STRG_MGR_DONE, this);
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.STRG_MGR_INSTR, this);
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.TERM_PROCESS, this);
		this.log = log;
		scheduler = createSchedulerObject(scen, cpu, interrupts, log);
		executingPCB = null;
		deviceQueue = new LinkedList<simPCB>();
		readyQueue = new LinkedList<simPCB>();
		termQueue = new LinkedList<simPCB>();
		waitQueue = new LinkedList<simPCB>();
		this.port = port;
		try {
			socket = new Socket(InetAddress.getLocalHost(), port);
			pout = new PrintWriter(socket.getOutputStream(), true);

		}
		catch(IOException ex) {
			System.err.println(ex);
			socket = null;
			pout = null;
		}
	}

	//purpose: Add PCB to device queue.
	//assumptions: None.
	//inputs: pcb - represents a simulated process.
	//post-conditions: pcb now at tail of the device queue.
	public void addDeviceQueue(simPCB pcb)
	{
		deviceQueue.addLast(pcb);
		log.println("simProcessManager.addDeviceQueue: added PCB (" +
			pcb.getId() + "," + pcb.getNumber() + ")");
	}

	//purpose: Add PCB to ready queue.
	//assumptions: None.
	//inputs: pcb - represents a simulated process.
	//post-conditions: pcb now at tail of the ready queue.
	public void addReadyQueue(simPCB pcb)
	{
		readyQueue.addLast(pcb);
		log.println("simProcessManager.addReadyQueue: added PCB (" +
			pcb.getId() + "," + pcb.getNumber() + ")");
	}

	//purpose: Add PCB to termination queue.
	//assumptions: None.
	//inputs: pcb - represents a simulated process.
	//post-conditions: pcb now at tail of the termination queue.
	public void addTermQueue(simPCB pcb)
	{
		termQueue.addLast(pcb);
		log.println("simProcessManager.addTermQueue: added PCB (" +
			pcb.getId() + "," + pcb.getNumber() + ")");
	}

	//purpose: Add PCB to wait queue.
	//assumptions: None.
	//inputs: pcb - represents a simulated process.
	//post-conditions: pcb now at tail of the wait queue.
	public void addWaitQueue(simPCB pcb)
	{
		waitQueue.addLast(pcb);
		log.println("simProcessManager.addWaitQueue: added PCB (" +
			pcb.getId() + "," + pcb.getNumber() + ")");
	}

	//purpose: Allocate a ready PCB to the CPU.
	//assumptions: ready queue contains at least one PCB.
	//inputs: timer - simTimer object.
	//post-conditions: executingPCB is null when no processes ready.
	//			Otherwise, executingPCB is set to first PCB in ready queue.
	public void allocateCPU(simTimer timer)
	{
		if (getReadyQueueSize() > 0)
		{
			executingPCB = getReadyQueue();
			log.println("simProcessManager.allocateCPU: executingPCB (" +
				executingPCB.getId() + "," +
				executingPCB.getNumber() + ")");
			scheduler.updateStartingPCBstatistics(executingPCB, timer);
		}
		else
		{
			executingPCB = null;
			log.println("simProcessManager.allocateCPU: no process is executing");
		}
	}

	//purpose: Create a PCB.
	//assumptions: None.
	//inputs: id, arrivalTime, instructions - data from the scenario XML file.
	//post-conditions: PCB returned.
	public simPCB createSimPCB(String id, int arrivalTime, ArrayList<simCPUInstruction> instructions)
	{
		return new simPCB(id, arrivalTime, instructions, log);
	}

	//purpose: Determine if a different process should start executing.
	//assumptions: Will be overridden in sub-classes that implement a particuluar scheduling algorithm.
	//inputs: timer - simTimer object.
	//post-conditions: a new simulated process is "executing" when scheduling algorithm determines a context switch is necessary.
	public void determineContextSwitchNeeded(simTimer timer)
	{
		if (executingPCB != null)
		{
			if (executingPCB.getInstructionPointer() >= executingPCB.getInstructionsSize())
				if (cpu.executedDEVinstruction())
					;	//do not force context switch, since storage manager now executing DEV instruction!
				else
				{
					//Process has no more instructions to execute; it has ended.
					interrupts.addInterrupt(simInterrupt.INTERRUPT.TERM_PROCESS, timer);
					//Force switch to next process in ready queue.
					interrupts.addInterrupt(simInterrupt.INTERRUPT.CPU_SCHED, timer);
				}
			else if (scheduler.preemptExecutingCPU())
			{
				//Force switch to next process in ready queue.
				interrupts.addInterrupt(simInterrupt.INTERRUPT.CPU_SCHED, timer);
			}
		}
	}

	//purpose: Return first PCB found in device queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns null when device queue is empty.
	//			Otherwise, returns first PCB in device queue.
	public simPCB getDeviceQueue()
	{
		simPCB pcb = null;
		if (getDeviceQueueSize() > 0)
			pcb = deviceQueue.removeFirst();
		log.println("simProcessManager.getDeviceQueue: removed PCB (" +
			(pcb == null ? pcb : pcb.getId() + "," + pcb.getNumber() + ")"));
		return pcb;
	}

	//purpose: Return size of device queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Size of device queue is returned.
	public int getDeviceQueueSize()
	{
		return deviceQueue.size();
	}

	//purpose: Getter method for executingPCB instance variable.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns the executing PCB, which could be null.
	public simPCB getExecutingPCB()
	{
		return executingPCB;
	}

	//purpose: Return first PCB found in ready queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns null when ready queue is empty.
	//			Otherwise, returns first PCB in ready queue.
	public simPCB getReadyQueue()
	{
		simPCB pcb = null;
		if (getReadyQueueSize() > 0)
			pcb = readyQueue.removeFirst();
		log.println("simProcessManager.getReadyQueue: removed PCB (" +
			(pcb == null ? pcb : pcb.getId() + "," + pcb.getNumber() + ")"));
		return pcb;
	}

	//purpose: Return size of ready queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Size of ready queue is returned.
	public int getReadyQueueSize()
	{
		return readyQueue.size();
	}

	//purpose: Return first PCB found in termination queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns null when device termination is empty.
	//			Otherwise, returns first PCB in termination queue.
	public simPCB getTermQueue()
	{
		simPCB pcb = null;
		if (getTermQueueSize() > 0)
			pcb = termQueue.removeFirst();
		//2017-01-22: Displaying next message causes process statistics report to be messier.
		//log.println("simProcessManager.getTermQueue: removed PCB " + (pcb == null ? pcb : pcb.getId()));
		return pcb;
	}

	//purpose: Return size of termination queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Size of termination queue is returned.
	public int getTermQueueSize()
	{
		return termQueue.size();
	}

	//purpose: Return first PCB found in wait queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns null when wait queue is empty.
	//			Otherwise, returns first PCB in wait queue.
	public simPCB getWaitQueue()
	{
		simPCB pcb = null;
		if (getWaitQueueSize() > 0)
			pcb = waitQueue.removeFirst();
		log.println("simProcessManager.getWaitQueue: removed PCB (" +
			(pcb == null ? pcb : pcb.getId() + "," + pcb.getNumber() + ")"));
		return pcb;
	}

	//purpose: Return size of wait queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Size of wait queue is returned.
	public int getWaitQueueSize()
	{
		return waitQueue.size();
	}

	//purpose: Process an interrupt assigned to the process manager.
	//	Interrupt STRG_MGR_DONE has Integer data (contains PCB number).
	//	Interrupt STRG_MGR_INSTR has simCPUInstruction data.
	//	Interrupt TERM_PROC has simTimer data.
	//assumptions: None.
	//inputs: data - an object associated with the interrupt.
	//post-conditions: 
	//	STRG_MGR_DONE: simPCB is removed from wait queue and put on ready queue.
	//	STRG_MGR_INSTR: simCPUInstruction data sent to storage manager; executingPCB put on wait queue.
	//	TERM_PROC: executingPCB now on termination queue.
	public void interruptServiceRoutine(Object data)
	{
		if (data instanceof Integer)
		{
			//Interrupt is from storage manager; a device instruction has completed for a simPCB.
			Integer pcbNumber = (Integer)data;
			//Move PCB with pcbNumber from wait queue to ready queue
			movePCBfromWaitToReady(pcbNumber);
		}
		else if (data instanceof simCPUInstruction)
		{
			//Interrupt is from cpu; a device instruction needs to be executed by storage manager.
			simCPUInstruction instruction = (simCPUInstruction)data;
			if (executingPCB == null)
				log.println("simProcessManager.interruptServiceRoutine (CPU instruction): " +
					"logic error - executingPCB should not be null!");
			else
			{
				log.println("simProcessManager.interruptServiceRoutine: instruction="
						+ instruction + " being sent to storage manager");
				//Put executingPCB on wait queue
				addWaitQueue(executingPCB);
				sendToStorageManager("" + instruction.getOpcode() + " " +
						instruction.getOperand() + " " + executingPCB.getNumber());
				//Start a context switch
				startContextSwitch();
			}
		}
		else if (data instanceof simTimer)
		{
			//Interrupt is from process manager (this object); executing PCB is terminating
			if (executingPCB == null)
				log.println("simProcessManager.interruptServiceRoutine (sim timer): " +
					"logic error - executingPCB should not be null!");
			else
			{
				log.println("simProcessManager.interruptServiceRoutine: terminating PCB " +
						executingPCB.getId());
				simTimer timer = (simTimer)data;
				addTermQueue(executingPCB);
				//Update scheduler statistics for this terminated PCB.
				scheduler.updateTerminatedPCBstatistics(executingPCB, timer);
				executingPCB = null;
			}
		}
		else
			log.println("simProcessManager.interruptServiceRoutine: unknown data");
	}

	//purpose: Return true when nothing is executing.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns true when executingPCB is null.
	//			Otherwise returns false.
	public boolean isIdle()
	{
		boolean idle = false;
		if (executingPCB == null)
			idle = true;
		return idle;
	}

	//purpose: Send message to storage manager using an IPC mechanism.
	//assumptions: None.
	//inputs: message - has the format "DEVR xx #" or "DEVW xx #" where
	//	one space separates each value in the message.
	//	xx is the integer value that immediately follows the DEVR/DEVW instruction
	//	# is the integer value representing the simPCB number of the process wanting to execute the DEVR/DEVW instruction
	//post-conditions: Message has been sent to the storage manager.
	public void sendToStorageManager(String message) {
		try {
			if(socket == null) {
				socket = new Socket(InetAddress.getLocalHost(), port);
				pout = new PrintWriter(socket.getOutputStream(), true);
			}
			//Use the PrintWriter to send the message over the socket
			pout.println(message);
		}
		//Catch an error
		catch (Exception e) {
			System.err.println(e);
		}
	}

	//purpose: Give CPU next instruction to be executed.
	//assumptions: A PCB is executing.
	//inputs: None.
	//post-conditions: CPU has next instruction to be executed.
	public void setCPUInstruction()
	{
		if (executingPCB != null)
			cpu.setInstruction(executingPCB.getInstruction());
	}

	//purpose: Setter method for executingPCB instance variable.
	//assumptions: None.
	//inputs: executingPCB - the PCB that is executing.
	//post-conditions: executingPCB instance variable updated.
	public void setExecutingPCB(simPCB executingPCB)
	{
		this.executingPCB = executingPCB;
	}

	//purpose: Force switch to next process in ready queue.
	//assumptions: None.
	//inputs: None.
	//post-conditions: No PCB is currently executing.
	public void startContextSwitch()
	{
		executingPCB = null;
		cpu.clearInstruction();
	}

	public String toString()
	{
		return "simProcessManager";
	}

	//purpose: Update instruction pointer in PCB.
	//assumptions: a PCB is executing.
	//inputs: None.
	//post-conditions: CPU has next instruction to execute.
	public void updateInstructionPointer()
	{
		if (executingPCB != null)
		{
			simCPUInstruction instruction = executingPCB.getInstruction();
			if (instruction != null)
			{
				//get instruction opcode
				simCPUInstruction.OPCODE opcode = instruction.getOpcode();
				//Determine what to do next based on instruction opcode.
				if (opcode == simCPUInstruction.OPCODE.CCC)
				{
					if (instruction.getOperand() == 0)
						fetchNextInstruction();
				}
				else if (opcode == simCPUInstruction.OPCODE.MEMA || opcode == simCPUInstruction.OPCODE.MEMF ||
					opcode == simCPUInstruction.OPCODE.DEVR || opcode == simCPUInstruction.OPCODE.DEVW ||
					opcode == simCPUInstruction.OPCODE.UNKNOWN)
					fetchNextInstruction();
			}
		}
	}

	//purpose: Create a CPU scheduler object based on scenario.
	//assumptions: None.
	//inputs: scen - the simScenario object.
	//		cpu - the simCPU object.
	//		interrupts - the simInterrupt object.
	//		log - the simLog object.
	//post-conditions: A CPU scheduler object now exists.
	private simCPUScheduler createSchedulerObject(scenario scen, simCPU cpu, simInterrupt interrupts, simLog log)
	{
		simCPUScheduler scheduler = null;
		scenario.SCHEDULER_ALG schedulerAlg = scen.getSchedulerAlgorithm();
		if (schedulerAlg == scenario.SCHEDULER_ALG.FCFS)
			scheduler = new simCPUSchedulerFCFS(cpu, this, interrupts, log);
		else if (schedulerAlg == scenario.SCHEDULER_ALG.PRIORITY)
			scheduler = new simCPUSchedulerPRIORITY(scen, cpu, this, interrupts, log);
		else if (schedulerAlg == scenario.SCHEDULER_ALG.RR)
			scheduler = new simCPUSchedulerRR(scen, cpu, this, interrupts, log);
		else if (schedulerAlg == scenario.SCHEDULER_ALG.SJF)
			scheduler = new simCPUSchedulerSJF(scen, cpu, this, interrupts, log);
		else if (schedulerAlg == scenario.SCHEDULER_ALG.SRTF)
			scheduler = new simCPUSchedulerSRTF(scen, cpu, this, interrupts, log);
		else
			scheduler = new simCPUScheduler(cpu, this, interrupts, log);

		return scheduler;
	}

	//purpose: Give CPU next instruction from the executing PCB.
	//assumptions: None.
	//inputs: None.
	//post-conditions: CPU ready to execute next instruction.
	private void fetchNextInstruction()
	{
		if (executingPCB != null)
		{
			executingPCB.removeInstruction();
			cpu.setInstruction(executingPCB.getInstruction());
		}
	}

	//purpose: Move PCB with pcbNumber from wait queue to ready queue.
	//assuptions: PDB with pcbNumber is first in wait queue.
	//inputs: pcbNumber - pcb number that should be moved from wait to ready.
	//post-condition: 
	private void movePCBfromWaitToReady(Integer pcbNumber)
	{
		if (getWaitQueueSize() == 0)
			log.println("simProcessManager.movePCBfromWaitToReady: " +
				"logic error - wait queue size should not be zero!");
		else
		{
			simPCB waitingPCB = getWaitQueue();
			if (waitingPCB.getNumber() != pcbNumber.intValue())
				log.println("simProcessManager.movePCBfromWaitToReady: " +
					"logic error - first PCB in wait queue (" +
					waitingPCB.getId() + "," + waitingPCB.getNumber() +
					" does not match pcbNumber=" + pcbNumber);
			else
			{
				addReadyQueue(waitingPCB);
				log.println("simProcessManager.interruptServiceRoutine: moved pcbNumber=" + pcbNumber + " from wait to ready");
			}
		}
	}
}
