package ui;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import solutions.District;
import solutions.DistrictMap;
import solutions.Settings;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;

public class FrameSeatsVotesChart extends JFrame {
	public JPanel panel;
	public JScrollPane scrollPane;
	public JLabel lblVotes;
	public JLabel lblSeats;
	public JTable table;
	Vector<double[]> seats_votes = new Vector<double[]>(); 
	public JButton btnCopy;
	//label.setUI(new VerticalLabelUI());
	public FrameSeatsVotesChart() {
		super();
		initComponents();
	}
	
	class SeatPanel extends JPanel {
	    public void paintComponent(Graphics graphics0) {
	    	try {
				Graphics2D g = (Graphics2D)graphics0;
			    super.paintComponent(g);
			    g.setColor(Color.white);
			    g.fillRect(0, 0, 200, 200);
			    g.setColor(Color.gray);
			    g.drawLine(0,200, 200, 0);

			    
			    int oldx = 1;
			    int oldy = 199;
			    g.setColor(Color.black);
			    for( int i = 0; i < seats_votes.size(); i++) {
			    	double[] dd = seats_votes.get(i);
			    	int x = (int)(Math.round(dd[1]*200.0)); 
			    	int y = (int)(Math.round(200.0-dd[0]*200.0));
			    	if( x == 0) { x++; }
			    	if( y == 0) { y++; }
			    	if( x == 200) { x--; }
			    	if( y == 200) { y--; }
			    	g.drawLine(oldx,oldy,x,y);
				    oldx = x;
				    oldy = y;
			    }
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	}
	
	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(284,524);
		setTitle("Seats / Votes");
		
		panel = new SeatPanel();
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
		
		btnCopy = new JButton("copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionEvent nev = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
				table.selectAll();
				table.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		btnCopy.setBounds(151, 222, 89, 23);
		getContentPane().add(btnCopy);
	}
	public void setData(DistrictMap dm) {
		
		Vector<double[]> swap = new Vector<double[]>();
		double[] vote_count_totals = new double[2];
		vote_count_totals[0] = 0;
		vote_count_totals[1] = 0;
		double total = 0; 
		double[][] vote_count_districts = new double[Settings.num_districts][2];
		
		//aggregate all the votes
		for( int i = 0; i < dm.districts.size() && i < Settings.num_districts; i++) {
			District d = dm.districts.get(i);
			double[][] result = d.getElectionResults();
			for( int j = 0; j < 2; j++) {
				vote_count_totals[j] += result[0][j];
				vote_count_districts[i][j] += result[0][j];
				total += result[0][j];
			}
		}
		
		//now normalize to 50/50
		double adjust = vote_count_totals[1]/vote_count_totals[0];
		for( int i = 0; i < dm.districts.size() && i < Settings.num_districts; i++) {
			vote_count_districts[i][0] *= adjust;
		}
		
		//now sample it at different vote ratios
		for( double dempct = 0; dempct <= 1; dempct += 0.02) {
			double reppct = 1-dempct;
			double votes = dempct;
			double demseats = 0;
			double totseats = 0;
			for( int i = 0; i < dm.districts.size() && i < Settings.num_districts; i++) {
				totseats++;
				if( vote_count_districts[i][0]*dempct > vote_count_districts[i][1]*reppct) {
					demseats++;
				}
			}
			double demseatpct = demseats/totseats;
			swap.add(new double[]{demseatpct,dempct});
		}
		seats_votes = swap;
		
		//now set the table
		String[][] sd = new String[seats_votes.size()][2];
		for( int i = 0; i < sd.length; i++) {
			double[] dd = seats_votes.get(i);
			sd[i] = new String[]{""+dd[0],""+dd[1]};
		}
		TableModel tm1 = new DefaultTableModel(sd,new String[]{"Seats","Votes"});
		table.setModel(tm1);
		panel.invalidate();
		panel.repaint();

	}
}
