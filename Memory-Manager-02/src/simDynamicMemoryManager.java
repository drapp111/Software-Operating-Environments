import java.util.ArrayList;

public class simDynamicMemoryManager
{
	private simLog log;
	private ArrayList<Integer> dynamicFrameTracker;

	//purpose: 
	//assumptions: this.RAM % this.pageSize == 0;
	//			this.RAM > this.osSize; 
	//			8GB / 512 (largest RAM / smallest pageSize) == 16,777,216 (fits within 32-bit int)
	//			8GB == 8,589,934,592 == 8 * 1024 * 1024 * 1024
	//inputs: None.
	//post-conditions: firstFreeFrame and lastFreeFrame are set.
	//			freeFrameQueue created but empty.
	public simDynamicMemoryManager(simLog log)
	{
		this.log = log;
		dynamicFrameTracker = new ArrayList<Integer>();
	}

	//purpose: Add free frame to list of free frames.
	//assumptions: freeFrameQueue object exists; called by simMemoryManager and simPCB.
	//inputs: frameNumber - frame number that is now free.
	//post-conditions: frameNumber added to list of free frames.
	public void addDynamicallyAllocatedFrame(Integer frameNumber)
	{
		dynamicFrameTracker.add(frameNumber);
	}
	
	//removes the first frame of the list
	
	public Integer removeDynamicallyAllocatedFrame() {
		if(this.dynamicFrameTracker.isEmpty()) {
			return null;
		}
		int value = dynamicFrameTracker.get(0);
		dynamicFrameTracker.remove(0);
		return value;
	}

	public int size()
	{
		return dynamicFrameTracker.size();
	}
	
	public String toString() {
		String value = "";
		for(int i = 0; i < this.dynamicFrameTracker.size(); i++) {
			value += this.dynamicFrameTracker.get(i) + "  ";
		}
		return value;
	}
}
