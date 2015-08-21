package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DialogManageLocks extends JDialog {
	public Vector<String> locks = new Vector<String>();
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
				ok = false;
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
				Feature feat = MainFrame.mainframe.featureCollection.features.get(0);
				String[] ss = new String[feat.properties.keySet().size()];
				feat.properties.keySet().toArray(ss);
				
				
				String[] options = new String[]{"Current value","Majority vote"};
				int ACCUMULATE = 0;
				int OVERWRITE = 1;
				int opt = JOptionPane.showOptionDialog(MainFrame.mainframe, "Lock to current values or makority vote?", "Select option", 0,0,null,options,options[0]);
				if( opt < 0) {
					System.out.println("aborted.");
					return;
				}

				if(opt == OVERWRITE) {
						
				}
				
				resetLocks();
				
				//MainFrame.mainframe.featureCollection.locked_wards
				
				//"lock to current"," lock to majority vote'
			}
		});
		btnAddLock.setBounds(10, 6, 140, 29);
		
		getContentPane().add(btnAddLock);
		btnRemoveLock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( list.getSelectedIndex() < 0) {
					return;
				}
				locks.remove(list.getSelectedIndex());
				list.setListData(locks);
				resetLocks();
			}
		});
		btnRemoveLock.setBounds(153, 6, 140, 29);
		
		getContentPane().add(btnRemoveLock);

	}
	public void resetLocks() {
		
	}
	

}
