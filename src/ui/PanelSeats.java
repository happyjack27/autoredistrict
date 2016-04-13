package ui;

import geography.FeatureCollection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import solutions.*;

public class PanelSeats extends JPanel {
	public MapPanel mapPanel;
	public PanelSeats() {
		this.setLayout(null);
		this.setSize(new Dimension(50, 50));
		this.setPreferredSize(new Dimension(50, 50));
		
	}
    public void paintComponent(Graphics graphics0) {
    	try {
	    	Graphics2D g = (Graphics2D)graphics0;
	        super.paintComponent(g);
	        Dimension dim = this.getSize();
	        if( mapPanel != null) {
	        	dim = mapPanel.getSize();
	        }
	        double w = dim.width-10;
	        
	        if( mapPanel == null || mapPanel.featureCollection == null || mapPanel.featureCollection.ecology.population.size() == 0) {
	        	return;
	        }
	        DistrictMap dm = mapPanel.featureCollection.ecology.population.get(0);
	        double[] votes = new double[Settings.num_candidates];
	        double[] seats = new double[Settings.num_candidates];
	        double tot_votes = 0;
	        double tot_seats = 0;
	        for( District d : dm.districts) {
				double[][] result = new double[2][];//d.getElectionResults();
				result[0] = d.getAnOutcome();
				result[1] = District.popular_vote_to_elected(result[0], d.id, 0);

	        	//double[][] result = d.getElectionResults();
	        	for( int i = 0; i < result[0].length; i++) {
	        		votes[i] += result[0][i];
	        		tot_votes += result[0][i];
	        	}
	        	for( int i = 0; i < result[1].length; i++) {
	        		seats[i] += result[1][i];
	        		tot_seats += result[1][i];
	        	}
	        }

	        double x = 5;
	        for( int i = 0; i < votes.length; i++) {
	        	g.setColor(FeatureCollection.standard_district_colors[i]);
	        	double width = w * (votes[i]/tot_votes);
	        	g.fillRect((int)x, 30, (int)width, 15);
	        	x += width;
	        }
	        
	        double seat_width = w/tot_seats;
	        double padding = 1;
	        int height = 15;
	        
	        if( seat_width-padding*2 > height) {
	        	if( seat_width/2 > height) {
	        		padding = seat_width/4;
	        	} else {
	        		padding = (seat_width-height)/2;
	        	}
	        	
	        }
	        
	        x = 5;
	        for( int i = 0; i < seats.length; i++) {
		        if( seat_width > 5) {
		        	for( int j = 0; j < seats[i]; j++) {
			        	g.setColor(FeatureCollection.standard_district_colors[i]);
			        	g.fillRect((int)(x+padding), 5, (int)(seat_width-padding*2), height);
		        		g.setColor(Color.black);
			        	g.drawRect((int)(x+padding), 5, (int)(seat_width-padding*2), height);
			        	x += seat_width;
		        	}
	        	} else {
		        	g.setColor(FeatureCollection.standard_district_colors[i]);
		        	double width = w * (seats[i]/tot_seats);
		        	g.fillRect((int)x, 5, (int)width, height);
		        	x += width;
	        	}
	        }
	        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

}
