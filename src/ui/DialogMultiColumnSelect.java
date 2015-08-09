package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DialogMultiColumnSelect extends JDialog {
	JList<String> list = new JList<String>();
	JList<String> list_1 = new JList<String>();
	public boolean ok = false;

	public Vector<String> not_in = new Vector<String>();
	public Vector<String> in = new Vector<String>();
	//public String[] right_side = null;
	
	public DialogMultiColumnSelect(String title, String[] all, String[] right_side) {
		setTitle(title);
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
				String[] selected = (String[]) list.getSelectedValues();
				for( String s : selected) {
					in.add(s);
					not_in.remove(s);
				}
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button.setBounds(214, 87, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] selected = (String[]) list_1.getSelectedValues();
				for( String s : selected) {
					not_in.add(s);
					in.remove(s);
				}
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button_1.setBounds(214, 271, 49, 29);
		getContentPane().add(button_1);

		ok = false;
		
		not_in = new Vector<String>();
		in = new Vector<String>();
		for( String s : all) {
			not_in.add(s);
		}
		
		for( int i = 0; i < right_side.length; i++) {
			in.add(right_side[i]);
			not_in.remove(right_side[i]);
		}
		
		list.setListData(not_in);
		list_1.setListData(in);

	}
}
