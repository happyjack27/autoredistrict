package util;

import java.util.HashMap;

import dbf.DBField;

public class DataAndHeader {
	public DBField[] full_header;
	public String[] header;
	public String[][] data;
	public HashMap<String, Integer> nameToIndex;
	
	public DataAndHeader() { super(); }
	public DataAndHeader(String[][] dd, String[] h) { data = dd; header = h; }
	public DataAndHeader(String[][] dd, String[] h, DBField[] full_header) { data = dd; header = h; this.full_header = full_header; }
}
