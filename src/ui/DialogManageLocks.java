package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DialogManageLocks extends JDialog {
	public JList<String> list = new JList<String>();
	public boolean ok = false;

	public final JButton btnAddLock = new JButton("Add Lock");
	public final JButton btnRemoveLock = new JButton("Remove lock");
	//public String[] right_side = null;
	
	public DialogManageLocks() {
		setTitle("Manage Locks");
		setModal(true);
				
		
		getContentPane().setLayout(null);
		this.setSize(new Dimension(319, 479));
		getContentPane().setPreferredSize(new Dimension(500,600));
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				hide();
			}
		});
		btnOk.setBounds(41, 384, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		btnCancel.setBounds(162, 384, 89, 23);
		getContentPane().add(btnCancel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 40, 283, 332);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(list);

		ok = false;

		btnAddLock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnAddLock.setBounds(10, 6, 140, 29);
		
		getContentPane().add(btnAddLock);
		btnRemoveLock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnRemoveLock.setBounds(153, 6, 140, 29);
		
		getContentPane().add(btnRemoveLock);

	}
}
