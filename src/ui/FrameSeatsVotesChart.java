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
	double multiplier = 2;
	public JButton btnx_2;
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
	    		int l1 = 128+64+32;
	    		int l2 = 128+64;
	    		int l3 = 128;
	    		
				Graphics2D g = (Graphics2D)graphics0;
			    super.paintComponent(g);

			    //calculate balanced sesats-votes curve
			    double[] mid_x = new double[seats_votes.size()];
			    double[] mid_y = new double[seats_votes.size()];
			    for( int i = 0; i < seats_votes.size(); i++) {
			    	double[] dd = seats_votes.get(i);
			    	double[] dd2 =  seats_votes.get(seats_votes.size()-1-i);
			    	mid_x[i] = dd[1]; 
			    	mid_y[i] = (dd[0]+(1-dd2[0]))/2.0;
			    }
			    
			    //first do basic background
			    g.setColor(Color.white);
			    g.fillRect(0, 0, 200, 200);
			    g.setColor(new Color(l1,l1,255));
			    g.fillPolygon(new int[]{scale(0),scale(200),0}, new int[]{0,0,200}, 3);
			    g.setColor(new Color(255,l1,l1));
			    g.fillPolygon(new int[]{scale(0),scale(200),200}, new int[]{200,0,200}, 3);

			    
			    //now do excess seats background
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
				    			ys[xindex] = y;
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

			    //in development - seats imbalance background
			    boolean b = true;
			    if( b) {
			    	
			    	//mid_x
				    double last_cross_x = 0;//scale(0);
				    double last_cross_y = 0;//scale(200);
				    int last_cross_ndx = 0;
				    double x0 = 0;
				    double y0 = 0;
				    for( int i = 0; i < seats_votes.size(); i++) {
				    	double[] dd = seats_votes.get(i);
				    	double y3 = i == 0 ? 0 : mid_y[i-1];
				    	double y4 = mid_y[i];
				    			
				    	double x1 = dd[1];
				    	double y1 = dd[0];
				    	double[] intersect = lineIntersect(x0,y0,x1,y1,x0,y3,x1,y4);
				    	if( (intersect != null && intersect[0] > x0 && intersect[0] <= x1) || i == seats_votes.size()-1) {
				    		if( i == seats_votes.size()-1) {
				    			intersect[0] = 1;
				    			intersect[1] = 1;
				    		}
				    		System.out.println(""+i+" "+last_cross_ndx+" "+(2+i-last_cross_ndx));
				    		int[] xs = new int[2*(1+i-last_cross_ndx)];
				    		int[] ys = new int[2*(1+i-last_cross_ndx)];
				    		xs[0] = scale((int)Math.round(200.0*last_cross_x));
				    		ys[0] = (int)Math.round(200.0-200.0*last_cross_y);
				    		
				    		int ndx = 1;
				    		for(int j = last_cross_ndx; j < i; j++) {
				    			double[] ee = seats_votes.get(j);
				    			xs[ndx] = scale((int)Math.round(200.0*ee[1]));
				    			ys[ndx] = (int)Math.round(200.0-200.0*ee[0]);
				    			ndx++;
				    		}
				    		
				    		xs[ndx] = scale((int)Math.round(200.0*intersect[0]));
				    		ys[ndx] = (int)Math.round(200.0-200.0*intersect[1]);
				    		last_cross_x = intersect[0];
				    		last_cross_y = intersect[1];
				    		ndx++;
				    		
				    		for(int j = i-1; j >= last_cross_ndx; j--) {
				    			xs[ndx] = scale((int)(200.0*mid_x[j]));
				    			ys[ndx] = (int)(200.0-200.0*mid_y[j]);
				    			ndx++;
				    		}
				    		
				    		last_cross_ndx = i;
				    		
				    		g.setColor(y0 > mid_y[i-1] ? new Color(l3,l3,255) : new Color(255,l3,l3));
						    g.fillPolygon(xs,ys,xs.length);
				    	}
				    	
				    	x0 = x1;
				    	y0 = y1;
				    	
				    }
			    }

			    //draw diagonal line and mid line
			    g.setColor(Color.gray);
			    g.drawLine(scale(0),200, scale(200), 0);
			    g.drawLine(100,0, 100, 200);

			    //draw seats votes curve
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
			    
			    //draw balanced seats-votes curve
			    oldx = 1;
			    oldy = 199;
			    g.setColor(Color.gray);
			    for( int i = 0; i < seats_votes.size(); i++) {
			    	int x = (int)Math.round(mid_x[i]*200.0); 
			    	int y = (int)Math.round(200.0-mid_y[i]*200.0);
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
	
	public static double[] lineIntersect(
			double x00, double y00, double x01, double y01, 
			double x10, double y10, double x11, double y11) {
		   double denom = (y11 - y10) * (x01 - x00) - (x11 - x10) * (y01 - y00);
		   if (denom == 0.0) { // Lines are parallel.
		      return null;
		   }
		   double ua = ((x11 - x10) * (y00 - y10) - (y11 - y10) * (x00 - x10))/denom;
		   double ub = ((x01 - x00) * (y00 - y10) - (y01 - y00) * (x00 - x10))/denom;
		     if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
		         // Get the intersection point.
		         return new double[]{ (x00 + ua*(x01 - x00)), (y00 + ua*(y01 - y00))};
		     }

		   return null;
		  }
	
	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(300,524);
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
		btnx.setBorder(BorderFactory.createLineBorder(Color.black));
		btnx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multiplier = 1;
				panel.invalidate();
				panel.repaint();
			}
		});
		btnx.setBounds(241, 70, 33, 29);
		
		getContentPane().add(btnx);
		btnx_1.setBorder(BorderFactory.createLineBorder(Color.black));
		btnx_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multiplier = 5;
				panel.invalidate();
				panel.repaint();
			}
		});
		btnx_1.setBounds(241, 150, 33, 29);
		
		getContentPane().add(btnx_1);
		
		btnx_2 = new JButton("2x");
		btnx_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multiplier = 2;
				panel.invalidate();
				panel.repaint();
			}
		});
		btnx_2.setBorder(BorderFactory.createLineBorder(Color.black));
		btnx_2.setBounds(241, 110, 33, 29);
		getContentPane().add(btnx_2);
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
				//if uncontested, ignore.
				if( vote_count_districts[i][0] == 0 || vote_count_districts[i][1] == 0) {
					if( Settings.ignore_uncontested) {
						continue;
					}
				}
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
