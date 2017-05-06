package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class DialogManageElections extends JDialog {
	JList<String> listAvailableColumns = new JList<String>();
	JList<String> listVoteColumns = new JList<String>();
	public JList<String> listImputatorColumns = new JList<String>();
	public JList<String> listElections = new JList<String>();

	public boolean ok = false;

	public int currentElection = 0;
	public Vector<String> electionNumbers = new Vector<String>();
	public Vector<String> available = new Vector<String>();
	//public Vector<String> in = new Vector<String>();
	public Vector<Vector<String>> elections = new Vector<Vector<String>>();
	public Vector<Vector<String>> imputators = new Vector<Vector<String>>();
	
	public JScrollPane scrollPane_2;
	public JButton button_2;
	public JButton button_3;
	public JScrollPane scrollPane_3;
	public JLabel lblImputeWithoptional;
	public JLabel lblAvailableColumns;
	public JLabel lblElections;
	public JLabel lblVoteCountColumns;
	public JButton removeElectionButton;
	public JButton addElectionButton;
	//public String[] right_side = null;
	
	public static void main(String[] ss) {
		Vector<String> all =  new Vector<String>();
		for( int i = 0; i < 20; i++) {
			all.add("col"+i);
		}
		DialogManageElections dme = new DialogManageElections("title",(String[])(all.toArray()),null,null);
		dme.show();
	}
	
	
	public void repaint() {
		System.out.println("repaint");
		super.repaint();
		paint(this.getGraphics());
		//this.p
	}
	
    public void paint(Graphics graphics0) {
		System.out.println("paint");
    	super.paint(graphics0);
    	try {
    		Graphics2D g = (Graphics2D)graphics0;
    		for(int i = 0; i < VTD.colors.length && i < 6; i++) {
    			/*
    			 * 286
    			 * 11
    			 * 182
    			 * 361
    			 */
    			g.setColor(VTD.colors[i]);
    			//g.fillOval(286+182+5, 11+2+i*12, 9, 9);
    			g.fillOval(286-12, 11+27+i*17+142, 10, 10);
    			g.fillOval(286-12, 11+27+i*17+261, 10, 10);
    		}
    	} catch (Exception ex) {
    		System.out.println("ex "+ex);
    		ex.printStackTrace();
    		
    	}
	}
	
	public DialogManageElections(String title, String[] all, Vector<Vector<String>> elections,Vector<Vector<String>> imputators) {
		setTitle(title);
		if( elections == null) {
			elections = new Vector<Vector<String>>();
		}
		if( imputators == null) {
			imputators = new Vector<Vector<String>>();
		}
		
		for( String s : all) {
			available.add(s);
		}
		for( Vector<String> election : elections) {
			for( String s : election) {
				available.remove(s);
			}
		}
		for( Vector<String> election : imputators) {
			for( String s : election) {
				available.remove(s);
			}
		}
		
		electionNumbers = new Vector<String>();
		for( int i = 0; i < elections.size(); i++) {
			electionNumbers.add("Election "+(i+1));
		}
		if( electionNumbers.size() == 0) {
			electionNumbers.add("Election 1");
			elections.add(new Vector<String>());
			imputators.add(new Vector<String>());
		}
		
		this.imputators = imputators;
		this.elections = elections;

		/*
		listElections.setListData(electionNumbers);
		listVoteColumns.setListData(new Vector<String>());
		listImputatorColumns.setListData(new Vector<String>());
		listAvailableColumns.setListData(available);
		*/

		initComponents();

		listElections.setListData(electionNumbers);
		listVoteColumns.setListData(new Vector<String>());
		listImputatorColumns.setListData(new Vector<String>());
		listAvailableColumns.setListData(available);
		
		
		listElections.setSelectedIndex(0);

	}
	private void initComponents() {
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
		scrollPane.setBounds(10, 29, 182, 343);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(listAvailableColumns);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(286, 153, 182, 100);
		getContentPane().add(scrollPane_1);
		
		scrollPane_1.setViewportView(listVoteColumns);
		
		JButton button = new JButton(">");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) listAvailableColumns.getSelectedValues();
				for( Object s : selected) {
					elections.get(currentElection).add((String)s);
					available.remove((String)s);
				}
				listAvailableColumns.setListData(available);
				listVoteColumns.setListData(elections.get(currentElection));
			}
		});
		button.setBounds(214, 167, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) listVoteColumns.getSelectedValues();
				for( Object s : selected) {
					available.add((String)s);
					elections.get(currentElection).remove((String)s);
				}
				listAvailableColumns.setListData(available);
				listVoteColumns.setListData(elections.get(currentElection));
			}
		});
		button_1.setBounds(214, 208, 49, 29);
		getContentPane().add(button_1);
		
		ok = false;
		
		listAvailableColumns.setListData(available);
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(286, 272, 182, 100);
		getContentPane().add(scrollPane_2);
		
		listImputatorColumns = new JList();
		scrollPane_2.setViewportView(listImputatorColumns);
		
		button_2 = new JButton("<");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) listImputatorColumns.getSelectedValues();
				for( Object s : selected) {
					available.add((String)s);
					imputators.get(currentElection).remove((String)s);
				}
				listAvailableColumns.setListData(available);
				listImputatorColumns.setListData(imputators.get(currentElection));
			}
		});
		button_2.setBounds(214, 327, 49, 29);
		getContentPane().add(button_2);
		
		button_3 = new JButton(">");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) listAvailableColumns.getSelectedValues();
				for( Object s : selected) {
					imputators.get(currentElection).add((String)s);
					available.remove((String)s);
				}
				listAvailableColumns.setListData(available);
				listImputatorColumns.setListData(imputators.get(currentElection));
			}
		});
		button_3.setBounds(214, 286, 49, 29);
		getContentPane().add(button_3);
		
		scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(286, 31, 182, 100);
		getContentPane().add(scrollPane_3);
		
		listElections = new JList();
		listElections.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				try {
					currentElection = listElections.getSelectedIndex();
				} catch (Exception ex) {
					return;
				}
				if( currentElection < 0) {
					return;
				}
				listVoteColumns.setListData(elections.get(currentElection));
				listImputatorColumns.setListData(imputators.get(currentElection));
			}
		});
		scrollPane_3.setViewportView(listElections);
		
		lblImputeWithoptional = new JLabel("Impute with (not implemented)");
		lblImputeWithoptional.setBounds(286, 254, 182, 16);
		getContentPane().add(lblImputeWithoptional);
		
		lblAvailableColumns = new JLabel("Available columns");
		lblAvailableColumns.setBounds(10, 6, 182, 16);
		getContentPane().add(lblAvailableColumns);
		
		lblElections = new JLabel("Elections");
		lblElections.setBounds(286, 6, 79, 16);
		getContentPane().add(lblElections);
		
		lblVoteCountColumns = new JLabel("Vote count columns");
		lblVoteCountColumns.setBounds(286, 132, 182, 16);
		getContentPane().add(lblVoteCountColumns);
		
		removeElectionButton = new JButton("-");
		removeElectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( elections.size() > 1) {					
					int removing = elections.size()-1;
					for( String s : elections.get(removing)) {
						available.add(s);
					}
					for( String s : imputators.get(removing)) {
						available.add(s);
					}
					listAvailableColumns.setListData(available);
					elections.remove(removing);
					imputators.remove(removing);
					electionNumbers.remove(removing);
					listElections.setListData(electionNumbers);
					listElections.setSelectedIndex(0);
				}
				
			}
		});
		removeElectionButton.setBounds(359, 1, 48, 29);
		getContentPane().add(removeElectionButton);
		
		addElectionButton = new JButton("+");
		addElectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				elections.add(new Vector<String>());
				imputators.add(new Vector<String>());
				electionNumbers.add("Election "+(elections.size()));
				int i = -1;
				try {
					i = listElections.getSelectedIndex();
				} catch (Exception ex) {
					
				}
				listElections.setListData(electionNumbers);
				/*
				if( i >= 0) {
					listElections.setSelectedIndex(i);
				}*/
				listElections.setSelectedIndex(electionNumbers.size()-1);
				
			}
		});
		addElectionButton.setBounds(419, 1, 49, 29);
		getContentPane().add(addElectionButton);
	}
}
