import java.util.ArrayList;

public class simSwapFile {
	
	private ArrayList<Integer> swapFile;
	private simPageTable pageTable;
	private simLog log;
	private int swapNumber;
	
	public simSwapFile(simPageTable pageTable, simLog log) {
		this.pageTable = pageTable;
		this.log = log;
		swapFile = new ArrayList<Integer>();
		swapNumber = 0;
	}
	
	public int getUnswappedPage() {
		return this.swapNumber;
	}
	
	public void pageSwap(int pageNumber, int frameNumber) {
		swapFile.set(pageNumber, frameNumber);
		swapNumber++;
	}
	
}
