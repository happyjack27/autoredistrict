package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class DialogDownload extends JDialog {
	JList<String> list = new JList<String>();
	public boolean ok = false;

	public JLabel lblSelectState;
	public JComboBox comboBoxCensusYear;
	public JComboBox comboBoxElectionYear;
	public JLabel lblSelectElectionYear;
	public JLabel lblSelectCensusYear;
	//public String[] right_side = null;
	
	public DialogDownload() {
		setTitle("Download vtd shapefile & population");
		initComponents();
		int y = new Date().getYear()+1900-1;
		int y10 = y - y % 10;
		int y4 = y - y % 4;
		String[] cyears = new String[]{""+y10,""+(y10-10)};
		String[] eyears = new String[]{""+y4,""+(y4-4),""+(y4-8),""+(y4-12),""+(y4-16),""+(y4-20)};
		for( int i = 0; i < cyears.length; i++ ) {
			comboBoxCensusYear.addItem(cyears[i]);
		}
		for( int i = 0; i < eyears.length; i++ ) {
			comboBoxElectionYear.addItem(eyears[i]);
		}
		comboBoxCensusYear.setSelectedIndex(0);
		comboBoxElectionYear.setSelectedIndex(0);
	}
	private void initComponents() {
		setModal(true);
		
		
		getContentPane().setLayout(null);
		this.setSize(new Dimension(314, 438));
		getContentPane().setPreferredSize(new Dimension(500,600));
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				hide();
				JOptionPane.showMessageDialog(MainFrame.mainframe, "It may take a few minutes to download and extact the data.\n(hit okay)");
				
				Download.downloadState(
						list.getSelectedIndex(), Integer.parseInt((String)comboBoxCensusYear.getSelectedItem()), Integer.parseInt((String)comboBoxElectionYear.getSelectedItem()) 
						);
			}
		});
		btnOk.setBounds(164, 349, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		btnCancel.setBounds(164, 315, 89, 23);
		getContentPane().add(btnCancel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 38, 144, 334);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(list);
		
		ok = false;
		
		list.setListData(Download.states);
		
		lblSelectState = new JLabel("Select state");
		lblSelectState.setBounds(10, 13, 109, 14);
		getContentPane().add(lblSelectState);
		
		comboBoxCensusYear = new JComboBox();
		comboBoxCensusYear.setBounds(164, 102, 89, 20);
		getContentPane().add(comboBoxCensusYear);
		
		comboBoxElectionYear = new JComboBox();
		comboBoxElectionYear.setBounds(164, 34, 89, 20);
		getContentPane().add(comboBoxElectionYear);
		
		lblSelectElectionYear = new JLabel("Select election year");
		lblSelectElectionYear.setBounds(164, 13, 121, 14);
		getContentPane().add(lblSelectElectionYear);
		
		lblSelectCensusYear = new JLabel("Select census year");
		lblSelectCensusYear.setBounds(164, 78, 121, 14);
		getContentPane().add(lblSelectCensusYear);
	}
}
