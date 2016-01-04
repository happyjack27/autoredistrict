package ui;

public class DataAndHeader {
	String[] header;
	String[][] data;
	int[] lengths;
	char[] types;
	
	DataAndHeader() { super(); }
	DataAndHeader(String[][] dd, String[] h) { data = dd; header = h; }
}
