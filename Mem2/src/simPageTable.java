import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

public class simPageTable
{
	private simPCB pcb;
	private simMemoryManager memMgr;
	private simLog log;

	private ArrayList<Integer> pageTable;
	private int largestUnusedPage;

	public simPageTable(simPCB pcb, simMemoryManager memMgr, simLog log)
	{
		this.memMgr = memMgr;
		this.log = log;
		this.pcb = pcb;
		this.largestUnusedPage = 0;

		BigInteger pageSize = memMgr.getPageSize();
		BigInteger nbrProcessPages;
		BigInteger[] divideRemainder;
		int numberProcessPages;

		divideRemainder = this.pcb.getImageSize().divideAndRemainder(pageSize);
		if (divideRemainder[1].compareTo(BigInteger.ZERO) == 1)
			nbrProcessPages = divideRemainder[0].add(BigInteger.ONE);
		else
			nbrProcessPages = divideRemainder[0];

		try
		{
			numberProcessPages = nbrProcessPages.intValueExact();
		}
		catch (Exception ex)
		{
			numberProcessPages = Integer.MAX_VALUE;
		}
		pageTable = new ArrayList<Integer>(numberProcessPages);

		log.println("simPageTable.constructor: created page table with capacity of " + numberProcessPages);

		mapPagesToFrames(memMgr, nbrProcessPages);
		log.println("simPageTable.constructor: number of free frames (after mapping process pages to frames)=" + memMgr.getNumberOfFreeFrames());
	}

	//purpose: Process is being terminated, return all dynamically allocated frames back to free list.
	//assumptions: Called by simPCB.
	//inputs: None.
	//post-conditions: Any frames dynamically allocated to this process now on free list.
	public void freeAllProcessFrames(simMemoryManagerFree memFree)
	{
		Integer frameNbr;
		log.println("simPageTable.freeAllProcessFrames: number of frames in page table=" + pageTable.size());
		for (int pageNbr=0; pageNbr < pageTable.size(); pageNbr++)
		{
			frameNbr = pageTable.get(pageNbr);
			memFree.addFreeFrame(frameNbr);
		}
	}

	//purpose: Obtain free frames for each logical page associated with this process.
	//assumptions: 
	//inputs: memMgr - the simMemoryManager; used to obtain free frames for this process.
	//	nbrProcessPages - the number of pages to be mapped to frames.
	//post-conditions:
	private void mapPagesToFrames(simMemoryManager memMgr, BigInteger nbrProcessPages)
	{
		//log.println("simPageTable.mapPagesToFrames: mapping pages 0..." + nbrProcessPages.toString() + " to frames");
		BigInteger counter = BigInteger.ZERO;
		Integer firstFrameNumber = null;
		Integer lastFrameNumber = null;
		while (counter.compareTo(nbrProcessPages) == -1)
		{
			Integer frameNumber = memMgr.getFreeFrameNumber(pcb);
			if (frameNumber != null)
			{
				pageTable.add(frameNumber);
				this.largestUnusedPage++;
				
				counter = counter.add(BigInteger.ONE);
				//Save first and last frame number used in mapping pages.
				if (firstFrameNumber == null)
					firstFrameNumber = frameNumber;
				lastFrameNumber = frameNumber;
			}
			else
				//No more free frames, stop mapping pages to frames.
				counter = nbrProcessPages;
		}
		//Subtract 1 to accurately display largest page number.
		BigInteger temp = nbrProcessPages.subtract(BigInteger.ONE);
		if (firstFrameNumber == null)
			log.println("simPageTable.mapPagesToFrames: mapping pages 0..." +
					temp.toString() + " to zero frames (no frames are free).");
		else
			log.println("simPageTable.mapPagesToFrames: mapping pages 0..." +
					(largestUnusedPage-1) + " to frames " +
					firstFrameNumber.toString() + "..." + lastFrameNumber.toString());
	}

	//purpose: Get frame number for the logical page number.
	//assumptions: None.
	//inputs: pageNumber - a logical page number.
	//post-conditions: Returns frame number that pageNumber is mapped to.
	//					Returns null (to indicate page fault) when pageNumber is larger than size of page table.
	public Integer getFrameNumber(int pageNumber)
	{
		Integer frameNumber = null;
		if (pageNumber < pageTable.size())
			frameNumber = new Integer(pageTable.get(pageNumber));
		
		return frameNumber;
	}
	
	public void allocateMemory(simMemoryManager memMgr, int nbrPages) {
		log.println("simPageTable.allocateMemory: dynamically mapping " + nbrPages + " pages");
		int counter = 0;
		Integer firstFrameNumber = null;
		Integer lastFrameNumber = null;
		while (counter < nbrPages)
		{
			Integer frameNumber = memMgr.getFreeFrameNumber(pcb);
			if (frameNumber != null)
			{
				memMgr.addDynamicFrame(frameNumber);
				counter++;
				//Save first and last frame number used in mapping pages.
				if (firstFrameNumber == null)
					firstFrameNumber = frameNumber;
				lastFrameNumber = frameNumber;
			}
			else
				//No more free frames, stop mapping pages to frames.
				counter = nbrPages;
		}
	}
	
	public void setFrameNumber(int pageNumber, int frameNumber) {
		pageTable.set(pageNumber, frameNumber);
	}
	
	public void freeMemory(simVirtualMemoryManager virtMem, simMemoryManager memMgr, int nbrPages) {
		log.println("simPageTable.freeMemory: dynamically freeing frames");
		int counter = 0;
		int freedFrames = 0;
		int swappedFrames = 0;
		while (counter < nbrPages)
		{
			Integer frameNumber = memMgr.removeDynamicFrame();
			if (frameNumber != null) {
				pageTable.remove(frameNumber);
				if(virtMem.contains(frameNumber)) {
					int index = virtMem.getOriginalPage(frameNumber);
					this.setFrameNumber(index, frameNumber);
					virtMem.remove(frameNumber);
					swappedFrames++;
					log.println("simPageTable.freeMemory: freed frame was a swap frame, frame allocated back to original page but not completely free");
				}
				else {
					memMgr.getMemoryManagerFree().addFreeFrame(frameNumber);
					freedFrames++;
				}
			}
			else {
				log.println("simPageTable.freeMemory: no dynamically allocated frames to free");
				break;
			}
			counter++;
		}
		log.println("simPageTable.freeMemory: freed " + freedFrames + " frames, unswapped " + swappedFrames);
	}
	
	public int size() {
		return this.pageTable.size();
	}
}
