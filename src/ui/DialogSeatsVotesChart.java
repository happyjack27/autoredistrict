package ui;

import javax.swing.*;

public class DialogSeatsVotesChart extends JDialog {
	public JPanel panel;
	public JScrollPane scrollPane;
	public JLabel lblVotes;
	public JLabel lblSeats;
	public JTable table;
	//label.setUI(new VerticalLabelUI());
	public DialogSeatsVotesChart() {
		super();
		initComponents();
	}
	private void initComponents() {
		getContentPane().setLayout(null);
		setTitle("Seats / Votes");
		
		panel = new JPanel();
		panel.setBounds(40, 11, 200, 200);
		getContentPane().add(panel);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 257, 200, 200);
		getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		lblVotes = new JLabel("Votes -->");
		lblVotes.setBounds(40, 222, 61, 14);
		getContentPane().add(lblVotes);
		
		lblSeats = new JLabel("Seats -->");
		lblSeats.setUI(new VerticalLabelUI());
		lblSeats.setBounds(10, 136, 20, 75);
		getContentPane().add(lblSeats);
	}
}
