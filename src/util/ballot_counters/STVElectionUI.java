package util.ballot_counters;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.event.*;

import java.util.*;

public class STVElectionUI extends JFrame {
	public final JButton btnNewButton = new JButton("New button");
	public final JButton btnAddBallots = new JButton("Add ballots");
	public final JScrollPane scrollPane = new JScrollPane();
	public final JButton btnResetBallots = new JButton("Reset ballots");
	public final JTextField winnersTF = new JTextField();
	public final JScrollPane scrollPane_1 = new JScrollPane();
	public final JLabel lblWinners = new JLabel("Winners:");
	public final JLabel lblBallots = new JLabel("Ballots:");
	public final JTextField ballotsTF = new JTextField();
	public final JTextField choicesTF = new JTextField();
	public final JLabel lblAdd = new JLabel("Add");
	public final JLabel lblNewLabel = new JLabel("ballots for choices");
	public final JTextField seatTF = new JTextField();
	public final JLabel lblSeats = new JLabel("Seats:");
	public final JTextField candidatesTF = new JTextField();
	public final JLabel lblCandidates = new JLabel("Candidates:");
	public final JTextArea textArea = new JTextArea();
	public final JTable table = new JTable();
	public final JTextField availableTF = new JTextField();
	public final JLabel lblAvailableChoices = new JLabel("Available choices:");
	public final JLabel lblseparateByCommas = new JLabel("(separate by commas)");
	STVElection el = new STVElection();
	STVElection2 el2 = new STVElection2();
	Vector<STVBallot> ballots = new Vector<STVBallot>();
	Vector<String[]> vtable = new Vector<String[]>();
	public final JCheckBox cbHare = new JCheckBox("Hare quota");
	public final JCheckBox cbfullorder = new JCheckBox("Full elimination ordering");
	public final JCheckBox cbtemp = new JCheckBox("Temporary eliminations");
	
	
	public static void main(String[] args) {
		new STVElectionUI().show();
	}
	public STVElectionUI() {
		availableTF.setEditable(false);
		availableTF.setBounds(212, 97, 211, 20);
		availableTF.setColumns(10);
		seatTF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateResults();
			}
		});
		seatTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateResults();
			}
		});
		seatTF.setBounds(66, 8, 86, 20);
		seatTF.setColumns(10);
		ballotsTF.setBounds(44, 125, 46, 20);
		ballotsTF.setColumns(10);
		winnersTF.setEditable(false);
		winnersTF.setBounds(76, 314, 255, 20);
		winnersTF.setColumns(10);
		initComponents();
	}
	public void updateAvailable() {
		int num = Integer.parseInt(candidatesTF.getText());
		String sw = "";
		for( int i = 0; i < num; i++) {
			if( i > 0) {
				sw +=", ";
			}
			sw += (char)(i+'A');
		}
		availableTF.setText(sw);
	}
	public void updateResults() {
		boolean teir2 = true;//chckbxNewCheckBox.isSelected();
		el2.use_droop =  !cbHare.isSelected();
		el2.reset_ignores =  cbtemp.isSelected();
		el2.ignore_fewest_votes = !cbfullorder.isSelected();
		
		
		Vector<STVBallot> tballots = new Vector<STVBallot>();
		for( STVBallot b : ballots) {
			tballots.add(new STVBallot(b.weight, b.getChoicesAsArray()));
		}
		Vector<Integer> winners =
				teir2 ? el2.getWinners(tballots, Integer.parseInt(seatTF.getText()), Integer.parseInt(candidatesTF.getText()))
						: el.getWinners(tballots, Integer.parseInt(seatTF.getText()), Integer.parseInt(candidatesTF.getText()))
						;
		String sw = "";
		textArea.setText(teir2 ? el2.reasoning : el.reasoning);
		for( int i = 0; i < winners.size(); i++) {
			if( i > 0) {
				sw +=", ";
			}
			sw += (char)(winners.get(i)+'A');
		}
		winnersTF.setText(sw);
		
	}
	public void updateTable() {
		DefaultTableModel dataModel = new DefaultTableModel();//(new String[]{"Ballots","Choices"},vtable);
		String[][] sss = new String[vtable.size()][];
		for( int i = 0; i < sss.length; i++) {
			sss[i] = vtable.get(i);
		}
		dataModel.setDataVector(sss,new String[]{"Ballots","Choices"});
		table.setModel(dataModel);
		
	}
	private void initComponents() {
		this.setSize(488, 594);
		setTitle("Ranked choice ballot counter");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		getContentPane().setLayout(null);
		btnAddBallots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int num_ballots = Integer.parseInt(ballotsTF.getText());
					String schoices = choicesTF.getText().trim().toUpperCase();
					String[] choices = schoices.split(",");
					int[] ich = new int[choices.length];
					for( int i = 0; i < choices.length; i++) {
						choices[i] = choices[i].trim();
						ich[i] = choices[i].charAt(0)-'A';
					}
					ballots.add(new STVBallot(num_ballots,ich));
					vtable.add(new String[]{""+num_ballots,schoices});
					updateTable();
					updateResults();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,"You have typos.");
				}
			}
		});
		btnAddBallots.setBounds(341, 124, 112, 23);
		
		getContentPane().add(btnAddBallots);
		scrollPane.setBounds(10, 183, 321, 120);
		
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(table);
		btnResetBallots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ballots.clear();
				vtable.clear();
				updateTable();
			}
		});
		btnResetBallots.setBounds(341, 181, 112, 23);
		
		getContentPane().add(btnResetBallots);
		
		getContentPane().add(winnersTF);
		scrollPane_1.setBounds(10, 342, 443, 201);
		
		getContentPane().add(scrollPane_1);
		
		scrollPane_1.setViewportView(textArea);
		lblWinners.setFont(new Font("Arial", Font.PLAIN, 11));
		lblWinners.setBounds(20, 317, 46, 14);
		
		getContentPane().add(lblWinners);
		lblBallots.setFont(new Font("Arial", Font.PLAIN, 11));
		lblBallots.setBounds(10, 163, 46, 14);
		
		getContentPane().add(lblBallots);
		
		getContentPane().add(ballotsTF);
		choicesTF.setColumns(10);
		choicesTF.setBounds(212, 125, 119, 20);
		
		getContentPane().add(choicesTF);
		lblAdd.setFont(new Font("Arial", Font.PLAIN, 11));
		lblAdd.setBounds(10, 128, 36, 14);
		
		getContentPane().add(lblAdd);
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		lblNewLabel.setBounds(104, 128, 98, 14);
		
		getContentPane().add(lblNewLabel);
		
		getContentPane().add(seatTF);
		lblSeats.setFont(new Font("Arial", Font.PLAIN, 11));
		lblSeats.setBounds(10, 11, 46, 14);
		
		getContentPane().add(lblSeats);
		candidatesTF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateAvailable();
			}
		});
		candidatesTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateAvailable();
			}
		});
		candidatesTF.setColumns(10);
		candidatesTF.setBounds(268, 8, 86, 20);
		
		getContentPane().add(candidatesTF);
		lblCandidates.setFont(new Font("Arial", Font.PLAIN, 11));
		lblCandidates.setBounds(185, 11, 73, 14);
		
		getContentPane().add(lblCandidates);
		
		getContentPane().add(availableTF);
		lblAvailableChoices.setFont(new Font("Arial", Font.PLAIN, 11));
		lblAvailableChoices.setBounds(104, 100, 98, 14);
		
		getContentPane().add(lblAvailableChoices);
		lblseparateByCommas.setFont(new Font("Arial", Font.ITALIC, 10));
		lblseparateByCommas.setHorizontalAlignment(SwingConstants.CENTER);
		lblseparateByCommas.setBounds(212, 144, 119, 14);
		
		getContentPane().add(lblseparateByCommas);
		cbHare.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cbHare.setSelected(true);
		cbHare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable();
				updateResults();
			}
		});
		cbHare.setBounds(10, 35, 180, 23);
		
		getContentPane().add(cbHare);
		cbfullorder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable();
				updateResults();
			}
		});
		cbfullorder.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cbfullorder.setSelected(true);
		cbfullorder.setBounds(268, 35, 180, 23);
		
		getContentPane().add(cbfullorder);
		cbtemp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable();
				updateResults();
			}
		});
		cbtemp.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cbtemp.setSelected(true);
		cbtemp.setBounds(268, 61, 180, 23);
		
		getContentPane().add(cbtemp);
	}
}
