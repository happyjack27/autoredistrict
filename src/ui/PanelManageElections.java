package ui;
import geography.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PanelManageElections extends JDialog {
	JList<String> list = new JList<String>();
	JList<String> list_1 = new JList<String>();
	public boolean ok = false;

	public Vector<String> not_in = new Vector<String>();
	public Vector<String> in = new Vector<String>();
	public JScrollPane scrollPane_2;
	public JButton button_2;
	public JButton button_3;
	public JScrollPane scrollPane_3;
	public JLabel lblImputeWithoptional;
	public JLabel lblAvailableColumns;
	public JLabel lblElections;
	public JLabel lblVoteCountColumns;
	public JList list_2;
	public JList list_3;
	public JButton removeElectionButton;
	public JButton addElectionButton;
	//public String[] right_side = null;
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
    		for(int i = 0; i < VTD.colors.length; i++) {
    			/*
    			 * 286
    			 * 11
    			 * 182
    			 * 361
    			 */
    			g.setColor(VTD.colors[i]);
    			//g.fillOval(286+182+5, 11+2+i*12, 9, 9);
    			g.fillOval(286-12, 11+27+i*17, 10, 10);
    		}
    	} catch (Exception ex) {
    		System.out.println("ex "+ex);
    		ex.printStackTrace();
    		
    	}
	}
	
	public PanelManageElections(String title, String[] all, String[] right_side) {
		setTitle(title);
		for( String s : all) {
			not_in.add(s);
		}
		
		for( int i = 0; i < right_side.length; i++) {
			in.add(right_side[i]);
			not_in.remove(right_side[i]);
		}

		initComponents();
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
		
		scrollPane.setViewportView(list);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(286, 153, 182, 100);
		getContentPane().add(scrollPane_1);
		
		scrollPane_1.setViewportView(list_1);
		
		JButton button = new JButton(">");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) list.getSelectedValues();
				for( Object s : selected) {
					in.add((String)s);
					not_in.remove((String)s);
				}
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button.setBounds(214, 167, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selected = (Object[]) list_1.getSelectedValues();
				for( Object s : selected) {
					not_in.add((String)s);
					in.remove((String)s);
				}
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button_1.setBounds(214, 208, 49, 29);
		getContentPane().add(button_1);
		
				ok = false;
		
		not_in = new Vector<String>();
		in = new Vector<String>();
		
		list.setListData(not_in);
		list_1.setListData(in);
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(286, 272, 182, 100);
		getContentPane().add(scrollPane_2);
		
		list_2 = new JList();
		scrollPane_2.setViewportView(list_2);
		
		button_2 = new JButton("<");
		button_2.setBounds(214, 327, 49, 29);
		getContentPane().add(button_2);
		
		button_3 = new JButton(">");
		button_3.setBounds(214, 286, 49, 29);
		getContentPane().add(button_3);
		
		scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(286, 31, 182, 100);
		getContentPane().add(scrollPane_3);
		
		list_3 = new JList();
		scrollPane_3.setViewportView(list_3);
		
		lblImputeWithoptional = new JLabel("Impute with (optional)");
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
		removeElectionButton.setBounds(369, 1, 34, 29);
		getContentPane().add(removeElectionButton);
		
		addElectionButton = new JButton("+");
		addElectionButton.setBounds(434, 1, 34, 29);
		getContentPane().add(addElectionButton);
	}
}
