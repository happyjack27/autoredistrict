package ui;
import geoJSON.*;

import javax.swing.*;
import java.awt.*;

public class DialogShowProperties extends JDialog {
	private JTable table;
	
	FeatureCollection fc;
	
	public void setTableSource(FeatureCollection fc) {
		this.fc = fc;
		
	}
	public DialogShowProperties() {
		setTitle("Properties table");
		//getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(254, 136, 61, 64);
		getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);

	}
}
