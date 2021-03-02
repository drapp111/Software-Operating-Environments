

public class simCPUSchedulerRR extends simCPUScheduler
{
	protected scenario scen;
	protected int quantumValue;
	
	public simCPUSchedulerRR(scenario scen, simCPU cpu, simProcessManager procMgr, simInterrupt interrupts, simLog log)
	{
		super(cpu, procMgr, interrupts, log);
		this.scen = scen;
		this.quantumValue = 1;
		log.println("simCPUSchedulerRR started");
	}

	//purpose: return true when the executingPCB should be preempted.
	//assumptions: None.
	//inputs: None.
	//post-conditions: returns true if it is time to preempt the executing PCB.
	public boolean preemptExecutingCPU()
	{
		boolean preempt = false;
		if(scen.getSchedulerQuantum() == this.quantumValue) {
			System.out.println("Preempted");
			preempt = true;
		}
		else {
			quantumValue++;
		}
		//Add code to this method, based on the method comments
		return preempt;
	}

	//purpose: Update scheduler statistics for this preempted PCB.
	//assumptions: 
	//inputs: executingPCB - the PCB being preempted.
	//	timer - the simTimer object.
	//post-conditions: PCB.totalExecuteTime updated, PCB.startWaitTime updated.
	public void updatePreemptedPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.addExecuteTime(timer.getClock());
		executingPCB.setStartWaitTime(timer.getClock());
	}

	//purpose: Update two statistics in the PCB that is executing.
	//assumptions: Executing PCB just started using the CPU.
	//inputs: timer - simTimer object.
	//post-conditions: PCB.totalWaitTime updated, PCB.startExecuteTime updated, and
	//			quantum value reset for PCB just starting to execute.
	public void updateStartingPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.setStartExecuteTime(timer.getClock());
		executingPCB.addWaitTime(timer.getClock());
		quantumValue = 1;
	}

	//purpose: Update two statistics in the PCB that is executing.
	//assumptions: Executing PCB just started using the CPU.
	//inputs: timer - simTimer object.
	//post-conditions: PCB.totalExecuteTime updated, PCB.turnaroundTime updated.
	public void updateTerminatedPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.addExecuteTime(timer.getClock());
		executingPCB.setTurnaroundTime(timer.getClock());
	}
}
