import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

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
			//CCC has the logical page number
			/*
			 * get page table
			 * check all values for valid translation
			 * 		search memMgr for frame # + offset
			 * 		if its there its good, if not, page fault
			 */
			
			//Get the page table
			PageTable table = executingPCB.getPageTable();
			int imageSize = executingPCB.getImageSize().intValue();
			int processPages = imageSize/memMgr.getPageSize();
			//Get the page number from the instruction
			int pageNumber = instruction.getOperand2();
			//Get the offset number from the instruction
			int offset = instruction.getOperand3();
			//Check if the page number is within the logical space by checking if the page number is possible and if offset is less than page size
			if(pageNumber >= processPages) {
				log.println("simMMU: page fault occurred, outside of logical address space");
			}
			else if(pageNumber > table.size()-1 || offset > memMgr.getPageSize()) {
				log.println("simMMU: page fault occurred, frame NOT mapped to logical address");
			}
			else {
				int frameNumber = table.get(pageNumber);
				log.println("simMMU: Page #" + pageNumber + " Offset #" + offset + " translated to Frame #" + frameNumber + " Offset #" + offset);
			}
		}
	}
}
