package ui;
import geography.*;

import javax.swing.*;

import solutions.DistrictMap;
import solutions.Settings;

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
				
				Vector<String> sortable = new Vector<String>();
				for( int i = 0; i < ss.length; i++) {
					sortable.add(ss[i]);
				}
				Collections.sort(sortable);
				sortable.toArray(ss);
				
				
				String key = (String) JOptionPane.showInputDialog(null, "Select column",
						"Select column", JOptionPane.QUESTION_MESSAGE, null, 
							        ss, // Array of choices
							        ss[0]); // Initial choice
				Hashtable<String,String> hash = new Hashtable<String,String>();
				for( Feature f : MainFrame.mainframe.featureCollection.features) {
					hash.put(f.properties.get(key).toString(),"");
				}
				String[] keyoptions = new String[hash.size()];
				hash.keySet().toArray(keyoptions);
				
				sortable = new Vector<String>();
				for( int i = 0; i < keyoptions.length; i++) {
					sortable.add(keyoptions[i]);
				}
				Collections.sort(sortable);
				sortable.toArray(keyoptions);

				String keychoice = (String) JOptionPane.showInputDialog(null, "Select value",
						"Select value", JOptionPane.QUESTION_MESSAGE, null, 
						keyoptions, // Array of choices
						keyoptions[0]); // Initial choice

				
				String[] options = new String[]{"Current value","Majority vote"};
				int ACCUMULATE = 0;
				int OVERWRITE = 1;
				int opt = JOptionPane.showOptionDialog(MainFrame.mainframe, "Lock to current values or majority vote?", "Select option", 0,0,null,options,options[0]);
				if( opt < 0) {
					System.out.println("aborted.");
					return;
				}

				locks.add(key+","+keychoice);
				list.setListData(locks);
				resetLocks();
				
				if( opt == OVERWRITE) {
					majority_vote(key,keychoice);
				}
				
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
		FeatureCollection featureCollection = MainFrame.mainframe.featureCollection;
		boolean[] locked_districts = new boolean[ featureCollection.vtds.size()];
		String[] keys = new String[locks.size()];
		String[] keychoices = new String[locks.size()];
		for( int i = 0; i < keys.length; i++) {
			int split  = locks.get(i).indexOf(",");
			keys[i] = locks.get(i).substring(0,split);
			keychoices[i] = locks.get(i).substring(split+1);
		}
		for( int i = 0; i < locked_districts.length; i++) {
			locked_districts[i] = false;
		}
		for( int i = 0; i < locked_districts.length; i++) {
			for( int j = 0; j < keys.length; j++) {
				if( featureCollection.features.get(i).properties.get(keys[j]).toString().equals(keychoices[j])) {
					locked_districts[i] = true;
				}
			}
		}
		FeatureCollection.locked_wards = locked_districts;
	}
	public void majority_vote(String key, String keychoice) {
		FeatureCollection featureCollection = MainFrame.mainframe.featureCollection;

		int[] counts = new int[Settings.num_districts];
		for( int i = 0; i < counts.length; i++) {
			counts[i] = 0;
		}
		for( int i = 0; i < featureCollection.features.size(); i++) {
			Feature feat = featureCollection.features.get(i);
			if( feat.properties.get(key).toString().equals(keychoice)) {
				int dist = featureCollection.ecology.population.get(0).vtd_districts[i];
				counts[dist]++;
			}
		}
		
		int max_count = 0;
		int max_index = 0;
		for( int i = 0; i < counts.length; i++) {
			if( counts[i] > max_count) {
				max_count = counts[i];
				max_index = i;
			}
		}
		
		for( int i = 0; i < featureCollection.features.size(); i++) {
			Feature feat = featureCollection.features.get(i);
			if( feat.properties.get(key).toString().equals(keychoice)) {
				for( DistrictMap dm : featureCollection.ecology.population) {
					dm.vtd_districts[i] = max_index;
				}
			}
		}
		
	}
	

}
