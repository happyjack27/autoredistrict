package ui;
import geoJSON.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.*;

public class DialogShowProperties extends JDialog {
	private JTable table;
	
	FeatureCollection fc;
	
	public void setTableSource(FeatureCollection fc) {
		this.fc = fc;
		String[] headers;
		Vector<Feature> vf = fc.features;
		if( vf == null || vf.size() < 1) {
			return;
		}
		Set<String> keyset = vf.get(0).properties.keySet(); 
		headers = new String[keyset.size()];
		int i = 0;
		
		for( String s : keyset) {
			headers[i] = s;
			System.out.println(s);
			i++;
		}
		System.out.println("found "+headers.length+" headers and "+vf.size()+" rows");
		
		String[][] data = new String[vf.size()][headers.length];
		for( int j = 0; j < vf.size(); j++) {
			Feature f = vf.get(j);
			for( int k = 0; k < headers.length; k++) {
				data[j][k] = f.properties.get(headers[k]).toString();
			}
		}
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new DefaultTableModel(data,headers));
		table.invalidate();
		table.repaint();
		
	}
	public DialogShowProperties() {
		setTitle("Properties table");
		//getContentPane().setLayout(null);
		this.setSize(new Dimension(400,400));
		getContentPane().setPreferredSize(new Dimension(400,400));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(254, 136, 61, 64);
		getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);

	}
}
