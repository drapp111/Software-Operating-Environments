import java.util.LinkedList;

public class simSwapFile {
	
	private LinkedList<Integer> swapFile;
	private simPageTable pageTable;
	private simLog log;

	
	public simSwapFile(simPageTable pageTable, simLog log) {
		this.pageTable = pageTable;
		this.log = log;
		swapFile = new LinkedList<Integer>();
	}
	
	public int pageSwap() {
		int freedFrame = pageTable.removePage(0);
		pageTable.addPage(freedFrame);
		return freedFrame;
	}
	
}
