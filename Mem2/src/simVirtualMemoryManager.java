import java.util.ArrayList;
import java.util.HashMap;

public class simVirtualMemoryManager {
	private HashMap<Integer, Integer> swapFile;
	private int unswappedPage;
	private simLog log;
	
	public simVirtualMemoryManager(simLog log) {
		this.swapFile = new HashMap<Integer, Integer>();
		this.unswappedPage = 0;
		this.log = log;
	}
	
	public Integer pageSwap(simPCB pcb) {
		try {
			int swapFrame = pcb.getFrameNumber(unswappedPage);
			swapFile.put(swapFrame, unswappedPage);
			pcb.setFrameNumber(unswappedPage, -1);
			unswappedPage++;
			log.println("simVirtualMemoryManager.pageSwap: successfully freed frame " + swapFrame);
			return swapFrame;
		} catch(NullPointerException exception) {
			log.println("simVirtualMemoryManager.pageSwap: pageTable not yet done initializing! Swapping not available!");
			return null;
		}
	}
	
	public boolean contains(int frame) {
		return swapFile.containsKey(frame);
	}
	
	public int getOriginalPage(int frameNumber) {
		return swapFile.get(frameNumber);
	}
	
	public void remove(int swappedFrame) {
		swapFile.remove(swappedFrame);
	}
	
	public boolean swapIsEmpty() {
		return swapFile.size() == 0;
	}
	
	public void clear() {
		swapFile = null;
		unswappedPage = 0;
	}
	
 	
	
}
