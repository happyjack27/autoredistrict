package ui;

import java.text.DecimalFormat;
import java.util.*;
import java.awt.*;

import javax.swing.*;

import java.io.*;
import java.awt.image.*;

import javax.imageio.*;

public class piechart extends JPanel {
	
	//public static boolean drawLabels = true;

	BufferedImage pie_eth_pop = null;
	BufferedImage pie_eth_target = null;
	BufferedImage pie_eth_descr = null;
	BufferedImage pie_eth_power = null;

	BufferedImage pie_party_votes = null;
	BufferedImage pie_party_seats = null;
	
	public static void makePieCharts() {
		
		/*

		pie_party_votes = drawPieChart(200,elec_counts[i],standard_colors); 
		pie_party_seats = drawPieChart(200,vote_counts[i],standard_colors); 

		pie_eth_pop    = drawPieChart(200,pop_by_dem[i],standard_colors); 
		pie_eth_target = drawPieChart(200,targets[i],standard_colors); 
		pie_eth_descr  = drawPieChart(200,winners_by_ethnicity[i],standard_colors);
		
		double[] dr = new double[votes_by_dem.length];
		for( int i = 0; i < dr.length; i++) {
			dr[i] = vote_margins_by_dem[i]/votes_by_dem[i];
		}
		pie_eth_power  = drawPieChart(200,dr[i],standard_colors); 

		//by ethnicity
		integer.format(pop_by_dem[i]), //pop %
		integer.format(targets[i]),		 //target descr. rep.
		integer.format(winners_by_ethnicity[i]), //descr. rep.
		decimal.format(ravg*vote_margins_by_dem[i]/votes_by_dem[i]), //voting power per person
		
		
					double[] elec_counts = new double[Settings.num_candidates];
			double[] vote_counts = new double[Settings.num_candidates];
		
		//by party

		 
		""+integer.format(elec_counts[i]), //votes
		""+integer.format(vote_counts[i]), //seats
		 * 
		 * 			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
		 * 				cdata[i] = new String[]{
						""+cands.get(i),
						""+integer.format(elec_counts[i]),
						""+integer.format(vote_counts[i]),//*total_population/tot_votes),
						""+(dm.wasted_votes_by_party[i]),
						""+(elec_counts[i]/((double)tot_seats)),
						""+(vote_counts[i]/tot_votes)
				};
		
		*/		
	}
	
	public static BufferedImage drawPieChart(int size, double[] vals, Color[] cols, boolean drawLabels) {
		BufferedImage image1 = new BufferedImage(size,size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics1 = image1.createGraphics(); 
		
        graphics1.setComposite(AlphaComposite.Clear);
        graphics1.fillRect(0, 0, size, size);
        graphics1.setComposite(AlphaComposite.Src);
        
		
		drawPieChart(graphics1,size,vals,cols, drawLabels);
		
		return image1;
		/*
        try {
            ImageIO.write(image1,"png", new File(path2));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }*/
	}
	public static void drawPieChart(Graphics g,int size, double[] vals, Color[] cols, boolean drawLabels) {
		drawPieChart(g,size/2,size/2,size,vals,cols, drawLabels);
	}
	public static void drawPieChart(Graphics g,int x, int y,int size, double[] vals, Color[] cols, boolean drawLabels) {
		 int width = size-2;
		 int height = size-2;
		 int xs = x-size/2+1;
		 int ys = y-size/2+1;
		 
			BufferedImage image1 = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		 
		 Graphics2D graphics = (Graphics2D)g;
		 graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		 graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		 graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		 
		 //g.setColor(Color.white);
		 //g.fillRect(0, 0, width, height);
		 double tot = 0;
		 for( int i = 0; i < vals.length; i++) {
			 tot += vals[i];
		 }
		 double cumarc = 0;
		 for( int i = 0; i < vals.length; i++) {
			 double arc = vals[i]*360.0/tot;
			 g.setColor(cols[i]);
			 g.drawArc(xs,ys,width,height,(int)cumarc,(int)(cumarc+arc)-(int)(cumarc));
			 g.fillArc(xs,ys,width,height,(int)cumarc,(int)(cumarc+arc)-(int)(cumarc));
			 g.setColor(Color.black);
			 //g.drawArc(0,0,width,height,(int)cumarc,(int)(arc));
			 cumarc += arc;
		 }
		 g.setColor(Color.black);
		 g.drawOval(xs,ys,width,height);
		 
		 if( drawLabels) {
			 cumarc = 0;
			 DecimalFormat df = new DecimalFormat("#0"); 
			 g.setColor(Color.BLACK);
			 Font f = new Font("Arial",0,24);
			 g.setFont(f);
			 if( vals.length < 20) {
				 for( int i = 0; i < vals.length; i++) {
					 double arc = vals[i]*2.0*Math.PI/tot;
	
	                  String pct = df.format(100.0*vals[i]/tot)+"%";

	                  double center_angle = cumarc + arc/2.0;
	                  double tx = ((double)width) * Math.cos(center_angle) / 3.0; 
	                  double ty = -((double)height) * Math.sin(center_angle) / 3.0;

	                  ty += g.getFontMetrics().getHeight()/3;
	                  tx -= g.getFontMetrics().stringWidth(pct)/2;
	                  tx = Math.round(tx+xs+width/2);
	                  ty = Math.round(ty+ys+height/2);

	                  if( vals[i]/tot > 0.03) {
	                      g.drawString(pct, (int)tx, (int)ty);
	                  }
	                  //g.drawArc(0,0,width,height,(int)cumarc,(int)(arc));
	                  cumarc += arc;
	              }
	          }
		 }
		 
		 
		 //g.setColor(Color.black);
		
	}
	 public void paintComponent(Graphics g) {
		 super.paintComponent(g);

		 g.setColor(Color.white);
		 g.fillRect(0,0,1000,1000);
		 /*
		 drawPieChart(g,100,100,100,new double[]{2,1}, new Color[]{Color.blue,Color.red});
		 drawPieChart(g,300,100,100,new double[]{6,2,1,0,0,0},new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta});
		 drawPieChart(g,100,300,100,new double[]{3,3,3,0,0,0},new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta});
		 drawPieChart(g,300,300,100,new double[]{6,2,1,1,3},new Color[]{
				 new Color(0x00,0,0xff),
				 new Color(0x40,0,0xb0),
				 new Color(0x80,0,0x80),
				 new Color(0xb0,0,0x40),
				 new Color(0xff,0,0x00),
		 });
		 */
	  }
	 
	 
	 public static void main(String[] args) {
		 piechart pc = new piechart();
		 pc.setSize(1000, 1000);
		 pc.setPreferredSize(new Dimension(1000, 1000));
		 
		 JDialog jd = new JDialog();
		 jd.setSize(1000, 1000);
		 jd.setPreferredSize(new Dimension(1000, 1000));
		 jd.setContentPane(pc);
		 jd.show();
	 }

}