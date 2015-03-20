package ui;
import geoJSON.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class DialogSelectLayers extends JDialog {
	JList list = new JList();
	JList list_1 = new JList();
	public boolean ok = false;

	
	FeatureCollection fc;
	String[] map_headers;
	String[][] map_data;

	public Vector<String> not_in = new Vector<String>();
	public Vector<String> in = new Vector<String>();

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
		setTitle("Select layers");
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(new Dimension(498, 479));
		getContentPane().setPreferredSize(new Dimension(500,600));
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				hide();
			}
		});
		btnOk.setBounds(122, 384, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		btnCancel.setBounds(243, 384, 89, 23);
		getContentPane().add(btnCancel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 182, 361);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(list);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(286, 11, 182, 361);
		getContentPane().add(scrollPane_1);
		
		scrollPane_1.setViewportView(list_1);
		
		JButton button = new JButton(">");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = list.getSelectedIndex();
				if( i < 0) return;
				in.add(not_in.get(i));
				not_in.remove(i);
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button.setBounds(214, 87, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = list_1.getSelectedIndex();
				if( i < 0) return;
				not_in.add(in.get(i));
				in.remove(i);
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button_1.setBounds(214, 271, 49, 29);
		getContentPane().add(button_1);
	}
}
