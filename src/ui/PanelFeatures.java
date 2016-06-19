package ui;

import geography.VTD;

import java.awt.*;
import java.util.Map.Entry;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

public class PanelFeatures extends JPanel {
	public JScrollPane scrollPane;
	public JTable table;
	public PanelFeatures() {
		initComponents();
		String[] dcolumns = new String[]{"Name","Value"};
		String[][] ddata = new String[1][2];
		TableModel tm1 = new DefaultTableModel(ddata,dcolumns);
		table.setModel(tm1);
	}
	public void setFeature(VTD vtd) {
		Set<Entry<String,Object>> es = vtd.properties.entrySet();
		String[][] sss = new String[es.size()][];
		int i = 0;
		for( Entry<String,Object> e : es) {
			sss[i]= new String[]{e.getKey(),e.getValue().toString()};
			i++;
		}
		
	}
	private void initComponents() {
		this.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(17, 307, 202, -282);
		add(scrollPane);
		
		table = new JTable();
		table.setBounds(117, 124, 1, 1);
		scrollPane.setViewportView(table);
	}
}
