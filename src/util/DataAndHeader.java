package util;

public class DataAndHeader {
	public String[] header;
	public String[][] data;
	
	public DataAndHeader() { super(); }
	public DataAndHeader(String[][] dd, String[] h) { data = dd; header = h; }
}
