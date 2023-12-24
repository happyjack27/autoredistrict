package ui;

import util.DataAndHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DialogShowProperties extends JDialog {
	private final JTable table;
	
	public void setTableSource(DataAndHeader dh) {
		

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new DefaultTableModel(dh.data,dh.header));
		table.invalidate();
		table.repaint();
		
	}
	public DialogShowProperties() {
		setTitle("Data table");
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
