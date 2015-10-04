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
import java.awt.Point;
import java.awt.event.ActionEvent;

public class FrameSeatsVotesChart extends JFrame {
	public JPanel panel;
	public JScrollPane scrollPane;
	public JLabel lblVotes;
	public JLabel lblSeats;
	public JTable table;
	Vector<double[]> seats_votes = new Vector<double[]>(); 
	public JButton btnCopy;
	public final JButton btnx = new JButton("1x");
	public final JButton btnx_1 = new JButton("5x");
	double multiplier = 1;
	//label.setUI(new VerticalLabelUI());
	public FrameSeatsVotesChart() {
		super();
		initComponents();
	}
	
	class SeatPanel extends JPanel {
		public int scale(int x) {
			return (int)((x-100)*multiplier+100);
		}
	    public void paintComponent(Graphics graphics0) {
	    	try {
	    		int l1 = 192+32;
	    		int l2 = 128+64;
				Graphics2D g = (Graphics2D)graphics0;
			    super.paintComponent(g);
			    g.setColor(Color.white);
			    g.fillRect(0, 0, 200, 200);
			    g.setColor(new Color(l1,l1,255));
			    g.fillPolygon(new int[]{scale(0),scale(200),0}, new int[]{0,0,200}, 3);
			    g.setColor(new Color(255,l1,l1));
			    g.fillPolygon(new int[]{scale(0),scale(200),200}, new int[]{200,0,200}, 3);

			    
			    //in development
			    boolean a = true;
			    if( a) {
				    int last_cross_x = scale(0);
				    int last_cross_y = 200;//scale(200);
				    int last_cross_ndx = 1;
				    double x0 = 0;
				    double y0 = 0;
				    for( int i = 0; i < seats_votes.size(); i++) {
				    	double[] dd = seats_votes.get(i);
				    	double x1 = dd[1];
				    	double y1 = dd[0]-dd[1];
				    	
				    	//if crossed
				    	if( y0*y1<0 || y1 == 0 || i == seats_votes.size()-1) {
				    		double dy = (y1-y0);
				    		double dx = (x1-x0);
				    		double frac = (0-y0)/dy;
				    		double crossx = x0+frac*dx;
				    		int new_cross_x = (int)(crossx*200.0);
				    		int new_cross_y = 200-new_cross_x;
				    		g.setColor(y0>0 ? new Color(l2,l2,255) : new Color(255,l2,l2));
				    		int[] xs = new int[i-last_cross_ndx+2];
				    		int[] ys = new int[i-last_cross_ndx+2];
				    		xs[0] = scale(last_cross_x);
				    		ys[0] = last_cross_y;//scale(last_cross_y);
				    		
				    		for( int j = last_cross_ndx; j <= i; j++) {
				    			int xindex = j-last_cross_ndx+1;
						    	double[] dd0 = seats_votes.get(j);
						    	int x = (int)(Math.round(dd0[1]*200.0)); 
						    	int y = (int)(Math.round(200.0-dd0[0]*200.0));				    			
				    			xs[xindex] = scale(x);
				    			ys[xindex] = scale(y);
				    		}
				    		xs[xs.length-1] = scale(new_cross_x);
				    		ys[xs.length-1] = new_cross_y;
	
						    g.fillPolygon(xs,ys,xs.length);
				    		last_cross_x = new_cross_x;
				    		last_cross_y = new_cross_y;
				    		last_cross_ndx = i;
				    	}
				    	
				    	x0 = x1;
				    	y0 = y1;
				    	
				    }
			    }

			    g.setColor(Color.gray);
			    g.drawLine(scale(0),200, scale(200), 0);
			    g.drawLine(100,0, 100, 200);

			    int oldx = scale(0);
			    int oldy = 199;
			    g.setColor(Color.black);
			    
			    for( int i = 0; i < seats_votes.size(); i++) {
			    	double[] dd = seats_votes.get(i);
			    	int x = (int)(Math.round(dd[1]*200.0)); 
			    	int y = (int)(Math.round(200.0-dd[0]*200.0));
			    	if( x == 0) { x++; }
			    	//if( y == 0) { y++; }
			    	//if( x == 200) { x--; }
			    	if( y == 200) { y--; }
			    	g.drawLine(scale(oldx),oldy,scale(x),y);
				    oldx = x;
				    oldy = y;
			    }
			    
			    oldx = 1;
			    oldy = 199;
			    g.setColor(Color.gray);
			    
			    for( int i = 0; i < seats_votes.size(); i++) {
			    	double[] dd = seats_votes.get(i);
			    	double[] dd2 =  seats_votes.get(seats_votes.size()-1-i);
			    	int x = (int)(Math.round(dd[1]*200.0)); 
			    	int y = (int)(Math.round(200.0-((dd[0]+(1-dd2[0]))/2.0)*200.0));
			    	if( x == 0) { x++; }
			    	//if( y == 0) { y++; }
			    	//if( x == 200) { x--; }
			    	if( y == 200) { y--; }
			    	g.drawLine(scale(oldx),oldy,scale(x),y);
				    oldx = x;
				    oldy = y;
			    }


	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	}
	
	public static Point lineIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		   double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		   if (denom == 0.0) { // Lines are parallel.
		      return null;
		   }
		   double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
		   double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
		     if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
		         // Get the intersection point.
		         return new Point((int) (x1 + ua*(x2 - x1)), (int) (y1 + ua*(y2 - y1)));
		     }

		   return null;
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
		lblVotes.setBounds(40, 222, 83, 14);
		getContentPane().add(lblVotes);
		
		lblSeats = new JLabel("Seats -->");
		lblSeats.setUI(new VerticalLabelUI());
		lblSeats.setBounds(10, 124, 20, 87);
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
		btnx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multiplier = 1;
				panel.invalidate();
				panel.repaint();
			}
		});
		btnx.setBounds(241, 116, 43, 29);
		
		getContentPane().add(btnx);
		btnx_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multiplier = 5;
				panel.invalidate();
				panel.repaint();
			}
		});
		btnx_1.setBounds(241, 150, 43, 29);
		
		getContentPane().add(btnx_1);
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
		for( double dempct = 0; dempct <= 1; dempct += 0.01) {
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
