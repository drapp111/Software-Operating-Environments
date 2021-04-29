import java.util.ArrayList;

/*
 * Controller class for the PageTable structure
 * Gives basic functionality over an ArrayList that is to be used as a page table
 */

public class PageTable {
	
	//Page table ArrayList
	private ArrayList<Integer> pageTable;
	
	//Constructor (no args)
	public PageTable() {
		pageTable = new ArrayList<Integer>();
	}
	
	//Get the size of the table
	public int size() {
		return pageTable.size();
	}
	
	//Set the table
	public void setTable(ArrayList<Integer> table) {
		pageTable = table;
	}
	
	//Get the ArrayList
	public ArrayList<Integer> getPageTable() {
		return this.pageTable;
	}
	
	//Get an element
	public int get(int index) {
		return pageTable.get(index);
	}
	
	//String form of object
	public String toString() {
		String output = "";
		output += "{";
		for(int i = 0; i < pageTable.size(); i++) {
			output += pageTable.get(i);
			if(i != pageTable.size()) {
				output += ",";
			}
		}
		output += "}";
		return output;
	}

}
