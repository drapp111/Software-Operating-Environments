import java.math.BigInteger;

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
	//						(1) the page# and offset exceeds the logical address space for the simulated process.
	//						(2) The page# cannot be translated to a frame#.
	public static void MMU(simMemoryManager memMgr, simPCB executingPCB,
										simCPUInstruction instruction, simLog log)
	{
		if (executingPCB != null && instruction != null && instruction.getOpcode() == simCPUInstruction.OPCODE.CCC)
		{
			//Get page number and offset from instruction.
			BigInteger pageNbr = new BigInteger(Integer.toString(instruction.getOperand2()));
			BigInteger offset = new BigInteger(Integer.toString(instruction.getOperand3()));
			BigInteger imageSize = executingPCB.getImageSize();
			BigInteger pageSize = memMgr.getPageSize();
			//Compute logical address (pageNbr * pageSize + offset).
			BigInteger logicalAddress = pageNbr.multiply(pageSize);
			logicalAddress = logicalAddress.add(offset);
			if (logicalAddress.compareTo(imageSize) > 0)
				//if page#,offset larger than imageSize
				//	display memory violation; trying to access memory beyond end of logical address space.
				log.println("simMMU.MMU: memory protection violation; logical address " +
						logicalAddress.toString() + " or (" +
						pageNbr.toString() + "," + offset.toString() +
						") exceeds size of process address space.");
			else
			{
				//get frame number using page number (new pcb method)
				Integer frameNbr = executingPCB.getFrameNumber(pageNbr.intValue());
				if (frameNbr == null)
					//	if frame# null:
					//		display page fault
					log.println("simMMU.MMU: page fault; page number " + pageNbr.toString() +
							" is NOT mapped to a physical frame.");
				else
					//	otherwise:
					//		display page#,offset -> frame#,offset
					log.println("simMMU.MMU: logical address (" + pageNbr.toString() + "," + offset.toString() +
							") mapped to physical address (" + frameNbr.toString() + "," + offset.toString() + ").");
			}
			//log.println("simMMU: translate logical address to physical address");
			//log.println("simMMU: report page fault if one would occur.");
		}
	}
}
