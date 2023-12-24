package ui;

import geography.FeatureCollection;

import java.util.Collections;
import java.util.Vector;

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
        Collections.addAll(not_in, map_headers);
		
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
