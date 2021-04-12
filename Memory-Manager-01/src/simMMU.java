public class simMMU
{
	//purpose: Translate CCC page#,offset into frame#,offset. Report page fault when one occurs.
	//assumptions: Method called first when instruction is fetched to be executed.
	//				All instructions call this method.
	//inputs: executingPCB - the PCB for the currently executing simulated process.
	//			instruction - the instruction that was just fetched.
	//			log - the simLog object.
	//post-conditions: For a CCC instruction: page#,offset translated to frame#,offset and reported via simLog.
	//					For a CCC instruction, page fault is reported to simLog when:
	//						(1) The page# cannot be translated to a frame#.
	//						(2) the page# and offset exceeds the logical address space for the simulated process.
	public static void MMU(simMemoryManager memMgr, simPCB executingPCB, simCPUInstruction instruction, simLog log)
	{
		if (executingPCB != null && instruction != null && instruction.getOpcode() == simCPUInstruction.OPCODE.CCC)
		{
			//Add code based on the method description above.
			//You should modify the println statements below based on the logic you add.
			log.println("simMMU: translate logical address to physical address");
			log.println("simMMU: report page fault if one would occur.");
		}
	}
}
