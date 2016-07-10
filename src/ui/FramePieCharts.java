package ui;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import ui.FrameSeatsVotesChart.SeatPanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class FramePieCharts extends JFrame {

	PiePanel panel = null;
	
	public FramePieCharts() {
		super();
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(500,500);
		setTitle("Pie charts");
		
		panel = new PiePanel();
		panel.setBounds(0, 0, 1000, 1000);
		getContentPane().add(panel);
	}
	
	class PiePanel extends JPanel {
		
	    public void paintComponent(Graphics graphics0) {
	    	try {
	    		/*
	    		double FSAA = 2;
	    		int iFSAA = 2;
	            Dimension d = this.getSize();
	            */
	            Graphics2D graphics = (Graphics2D)graphics0;
		        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		        /*
		        BufferedImage off_Image =
		        		  new BufferedImage(
		        				  (int) (d.getWidth()*FSAA), 
		        				  (int) (d.getHeight()*FSAA), 
		        		          BufferedImage.TYPE_INT_ARGB
		        		          );
		        Graphics2D g = off_Image.createGraphics();
		        */
		        
			    super.paintComponent(graphics);
			    
			    graphics.setColor(Color.white);
			    graphics.fillRect(0, 0, 1000, 1000);
			    
			    /*
	BufferedImage pie_party_comp_digital;
	BufferedImage pie_party_comp_digital2;
	BufferedImage pie_party_votes;
	BufferedImage pie_party_seats;
	BufferedImage pie_party_seats_frac;
	BufferedImage pie_eth_pop;
	BufferedImage pie_eth_target;
	BufferedImage pie_eth_descr;
	BufferedImage pie_eth_power;
	
	BufferedImage pie_eth_packing;
	BufferedImage pie_party_packing;
	BufferedImage pie_eth_packingm;
	BufferedImage pie_party_packingm;
	
	BufferedImage pie_eth_packing_byvoter;
	BufferedImage pie_party_packing_byvoter;
	BufferedImage pie_eth_packingm_byvoter;
	BufferedImage pie_party_packingm_byvoter;
			     */
			    
			    int xs = 10;
			    int ys = 40;
			    int w = 100;
			    int h = 100;
			    
			    
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_party_votes,xs*2+w*0,5,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_party_seats,xs*4+w*1,5,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_party_packingm_byvoter,xs*6+w*2,5,100,100,null);
			    
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_pop,xs*2+w*0,5+h*1+ys*1,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_descr,xs*4+w*1,5+h*1+ys*1,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_packingm_byvoter,xs*6+w*2,5+h*1+ys*1,100,100,null);
			    
			    graphics.setColor(Color.black);
			    
			    graphics.drawString("Partisan votes", xs*2+w*0+w/2 - graphics.getFontMetrics().stringWidth("Partisan votes")/2, 5+h+20);
			    graphics.drawString("Partisan seats", xs*4+w*1+w/2 - graphics.getFontMetrics().stringWidth("Partisan seats")/2, 5+h+20);
			    graphics.drawString("Packing / cracking", xs*6+w*2+w/2 - graphics.getFontMetrics().stringWidth("Packing / cracking")/2, 5+h+20);
			    
			    graphics.drawString("Minority population", xs*2+w*0+w/2 - graphics.getFontMetrics().stringWidth("Minority population")/2, 5+h*2+ys*1+20);
			    graphics.drawString("Minority seats", xs*4+w*1+w/2 - graphics.getFontMetrics().stringWidth("Minority seats")/2, 5+h*2+ys*1+20);
			    graphics.drawString("Packing / cracking", xs*6+w*2+w/2 - graphics.getFontMetrics().stringWidth("Packing / cracking")/2, 5+h*2+ys*1+20);

			    /*
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_descr,0,0,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_target,0,0,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_descr,0,0,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_descr,0,0,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_packingm_byvoter,0,0,100,100,null);
			    graphics.drawImage(MainFrame.mainframe.panelStats.pie_party_packingm_byvoter,0,0,100,100,null);
			    */
			    //graphics.drawImage(MainFrame.mainframe.panelStats.pie_eth_power,100,0,100,100,null);

	
	    	} catch (Exception ex) {
	    		System.out.println("ex csafs "+ex);
	    		ex.printStackTrace();
	    		
	    	}
	    }
	}

}
