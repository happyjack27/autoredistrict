package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DialogSelectLayers extends DialogMultiColumnSelect {	
	FeatureCollection fc;
	String[] map_headers;
	String[][] map_data;

	public void setData(FeatureCollection fc) {
		setData(fc,null);
		
	}
	public void setData(FeatureCollection fc, Vector<String> current) {
		ok = false;
		this.fc = fc;
		map_headers = fc.getHeaders();
		map_data = fc.getData(map_headers);
		
		not_in = new Vector<String>();
		in = new Vector<String>();
		for( String s : map_headers) {
			not_in.add(s);
		}
		
		if( current != null) {
			for( int i = 0; i < current.size(); i++) {
				String s = current.get(i);
				in.add(s);
				not_in.remove(s);
			}
		}
		
		list.setListData(not_in);
		list_1.setListData(in);
		
	}
	
	public DialogSelectLayers() {
		super("select layers",new String[]{},new String[]{});
	}
}
