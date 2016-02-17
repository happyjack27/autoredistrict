package ui;

import geography.FeatureCollection;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import solutions.District;
import solutions.DistrictMap;
import solutions.Settings;

import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class FrameSeatsVotesChart extends JFrame {
	public double wang = 0;
	public double grofman = 0;
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
	public JButton btnx_2;
	public PanelRankedDistricts panelRanked;
	public JTextField baasTF;
	public JLabel lblNewLabel;
	public JLabel lblGrofmankingAsymmetry;
	public JTextField grofmanTF;
	public JLabel lblMedianMinusMean;
	public JTextField wangTF;
	//label.setUI(new VerticalLabelUI());
	public FrameSeatsVotesChart() {
		super();
		initComponents();
	}
	
	class SeatPanel extends JPanel {
		public int scale(int x) {
			return (int)((x-100)*multiplier+100);
		}
		public int scale_width(double x) {
			return (int)Math.round(x*(double)multiplier);
		}
	    public void paintComponent(Graphics graphics0) {
	    	try {
	    		double FSAA = 2;
	    		int iFSAA = 2;
	            Dimension d = this.getSize();
                Graphics2D graphics = (Graphics2D)graphics0;
    	        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    	        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    	        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		        BufferedImage off_Image =
		        		  new BufferedImage(
		        				  (int) (d.getWidth()*FSAA), 
		        				  (int) (d.getHeight()*FSAA), 
		        		          BufferedImage.TYPE_INT_ARGB
		        		          );
		        Graphics2D g = off_Image.createGraphics();

	    		
	    		int l1 = 128+64+32;
	    		int l2 = 128+64;
	    		int l3 = 128;
	    		
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
			    g.fillRect(0, 0, 200*iFSAA, 200*iFSAA);
			    g.setColor(new Color(l1,l1,255));
			    g.fillPolygon(new int[]{scale(0)*iFSAA,scale(200)*iFSAA,0}, new int[]{0,0,200*iFSAA}, 3);
			    g.setColor(new Color(255,l1,l1));
			    g.fillPolygon(new int[]{scale(0)*iFSAA,scale(200)*iFSAA,200*iFSAA}, new int[]{200*iFSAA,0,200*iFSAA}, 3);

			    
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
				    		xs[0] = scale(last_cross_x)*iFSAA;
				    		ys[0] = last_cross_y*iFSAA;//scale(last_cross_y);
				    		
				    		for( int j = last_cross_ndx; j <= i; j++) {
				    			int xindex = j-last_cross_ndx+1;
						    	double[] dd0 = seats_votes.get(j);
						    	int x = (int)(Math.round(dd0[1]*200.0)); 
						    	int y = (int)(Math.round(200.0-dd0[0]*200.0));				    			
				    			xs[xindex] = scale(x)*iFSAA;
				    			ys[xindex] = y*iFSAA;
				    		}
				    		xs[xs.length-1] = scale(new_cross_x)*iFSAA;
				    		ys[xs.length-1] = new_cross_y*iFSAA;
	
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
			    	int red = 0;
			    	int blue = 0;
				    for( int i = 0; i < seats_votes.size(); i++) {
				    	
				    	double[] dd = seats_votes.get(i);
				    	double y3 = i == 0 ? 0 : mid_y[i-1];
				    	double y4 = mid_y[i];
				    			
				    	double x1 = dd[1];
				    	double y1 = dd[0];

				    	if( y1 > mid_y[i]) {
				    		blue++;
				    	} else if( y1 < mid_y[i]){
				    		red++;
				    	}

				    	double[] intersect = lineIntersect(x0,y0,x1,y1,x0,y3,x1,y4);
				    	if( (intersect != null && intersect[0] >= x0 && intersect[0] <= x1) || i == seats_votes.size()-1) {
				    		if( i == seats_votes.size()-1) {
				    			intersect = new double[]{1,1};
				    		}
				    		//System.out.println(""+i+" "+last_cross_ndx+" "+(2+i-last_cross_ndx));
				    		int[] xs = new int[2*(1+i-last_cross_ndx)];
				    		int[] ys = new int[2*(1+i-last_cross_ndx)];
				    		xs[0] = scale((int)Math.round(200.0*last_cross_x))*iFSAA;
				    		ys[0] = (int)Math.round(200.0-200.0*last_cross_y)*iFSAA;
				    		
				    		int ndx = 1;
				    		for(int j = last_cross_ndx; j < i; j++) {
				    			double[] ee = seats_votes.get(j);
				    			xs[ndx] = scale((int)Math.round(200.0*ee[1]))*iFSAA;
				    			ys[ndx] = (int)Math.round(200.0-200.0*ee[0])*iFSAA;
				    			ndx++;
				    		}
				    		
				    		xs[ndx] = scale((int)Math.round(200.0*intersect[0]))*iFSAA;
				    		ys[ndx] = (int)Math.round(200.0-200.0*intersect[1])*iFSAA;
				    		last_cross_x = intersect[0];
				    		last_cross_y = intersect[1];
				    		ndx++;
				    		
				    		for(int j = i-1; j >= last_cross_ndx; j--) {
				    			xs[ndx] = scale((int)Math.round(200.0*mid_x[j]))*iFSAA;
				    			ys[ndx] = (int)Math.round(200.0-200.0*mid_y[j])*iFSAA;
				    			ndx++;
				    		}
				    		
				    		last_cross_ndx = i;
				    		
				    		g.setColor(blue > red ? new Color(l3,l3,255) : new Color(255,l3,l3));
						    g.fillPolygon(xs,ys,xs.length);
					    	red = 0;
					    	blue = 0;

				    	}
				    	
				    	x0 = x1;
				    	y0 = y1;
				    	
				    }
			    }

			    //draw diagonal line and mid line
			    g.setColor(Color.gray);
			    g.drawLine(scale(0)*iFSAA,200*iFSAA, scale(200)*iFSAA, 0);
			    g.drawLine(100*iFSAA,0, 100*iFSAA, 200*iFSAA);

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
			    	g.drawLine(scale(oldx)*iFSAA,oldy*iFSAA,scale(x)*iFSAA,y*iFSAA);
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
			    	g.drawLine(scale(oldx)*iFSAA,oldy*iFSAA,scale(x)*iFSAA,y*iFSAA);
				    oldx = x;
				    oldy = y;
			    }
			    
			    /*
			    //draw grofman
			    g.setColor(Color.BLACK);
			    g.drawLine(100*iFSAA,100*iFSAA, 100*iFSAA, (100+(int)(grofman*200))*iFSAA);
			    
			    //draw wang
			    g.setColor(Color.BLACK);
			    g.drawLine(100*iFSAA,100*iFSAA, scale(100+(int)(wang*200))*iFSAA, 100*iFSAA);
			    */
			    g.setColor(Color.BLACK);
			    g.fillRect(99*iFSAA,100*iFSAA, 3, ((int)Math.round(grofman*200))*iFSAA);
			    
			    //draw wang
			    g.setColor(Color.BLACK);
			    g.fillRect(100*iFSAA,99*iFSAA, scale_width(wang*200.0)*iFSAA, 3);

			    
		        graphics.drawImage(off_Image,
		                0,
		                0,
		        		(int)d.getWidth(), (int)d.getHeight(), 
		        		null);



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
		setSize(570,575);
		setTitle("Seats / Votes");
		
		panel = new SeatPanel();
		panel.setBounds(40, 11, 200, 200);
		getContentPane().add(panel);
		panel.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/seats_votes_measures.png") + "\">");
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 329, 200, 200);
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
		
		panelRanked = new PanelRankedDistricts();
		panelRanked.lblSeats.setBounds(10, 67, 20, 144);
		panelRanked.lblSeats.setText("Sorted districts  -->");
		panelRanked.scrollPane.setLocation(40, 329);
		panelRanked.setBounds(284, 0, 267, 540);
		getContentPane().add(panelRanked);
		
		baasTF = new JTextField();
		baasTF.setHorizontalAlignment(SwingConstants.RIGHT);
		baasTF.setEditable(false);
		baasTF.setBounds(171, 250, 69, 25);
		getContentPane().add(baasTF);
		baasTF.setColumns(10);
		
		lblNewLabel = new JLabel("Baas asym.");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(13, 255, 148, 14);
		getContentPane().add(lblNewLabel);
		
		lblGrofmankingAsymmetry = new JLabel("Grofman/King asym.");
		lblGrofmankingAsymmetry.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGrofmankingAsymmetry.setBounds(13, 280, 148, 14);
		getContentPane().add(lblGrofmankingAsymmetry);
		
		grofmanTF = new JTextField();
		grofmanTF.setHorizontalAlignment(SwingConstants.RIGHT);
		grofmanTF.setEditable(false);
		grofmanTF.setColumns(10);
		grofmanTF.setBounds(171, 275, 69, 25);
		getContentPane().add(grofmanTF);
		
		lblMedianMinusMean = new JLabel("Median minus mean");
		lblMedianMinusMean.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMedianMinusMean.setBounds(10, 305, 151, 14);
		getContentPane().add(lblMedianMinusMean);
		
		wangTF = new JTextField();
		wangTF.setHorizontalAlignment(SwingConstants.RIGHT);
		wangTF.setEditable(false);
		wangTF.setColumns(10);
		wangTF.setBounds(171, 300, 69, 25);
		getContentPane().add(wangTF);
	}
	public void setData(DistrictMap dm) {
		dm.calcSeatsVotesCurve();
		double d = dm.calcSeatsVoteAsymmetry();
		seats_votes = dm.seats_votes;
		
		double min = 1;
		double median = 0.5;
		//now set the table
		String[][] sd = new String[seats_votes.size()][2];
		for( int i = 0; i < sd.length; i++) {
			double[] dd = seats_votes.get(i);
			sd[i] = new String[]{""+dd[0],""+dd[1]};
			if( Math.abs(dd[0]-0.5) < min) {
				min = Math.abs(dd[0]-0.5);
				median = dd[1];
			} else if (Math.abs(dd[0]-0.5) == min) {
				median = (median + dd[1])/2.0;
			}
		}
		try {
			
			DecimalFormat decimal = new DecimalFormat("#0.00000");
			baasTF.setText(decimal.format(d));
			double[] mid = seats_votes.get(seats_votes.size()/2);
			wang = median-0.5;
			grofman = 0.5-mid[0];

			grofmanTF.setText(decimal.format(grofman));
			wangTF.setText(decimal.format(wang)); 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		TableModel tm1 = new DefaultTableModel(sd,new String[]{"Seats","Votes"});
		table.setModel(tm1);
		panel.invalidate();
		panel.repaint();

	}
}
