package ui;

import geoJSON.Feature;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.JPanel;

public class MapPanel extends JPanel {
	double minx,maxx,miny,maxy;
	Vector<Feature> features;

	MapPanel() {
        // set a preferred size for the custom panel.
        setPreferredSize(new Dimension(200,200));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = this.getSize();
        double scalex = ((double)d.getWidth())/(maxx-minx);
        double scaley = ((double)d.getHeight())/(maxy-miny);
        if( features != null) {
            for( Feature f : features) {
            	double[][] coordinates = f.geometry.coordinates;
            	for( int i = 0; i < coordinates.length; i++) {
            		double[] first = coordinates[i];
            		double[] secon = coordinates[i == coordinates.length ? 0 : i];
            		g.setColor(Color.BLUE);
                	g.drawLine(
                			(int)((first[0]-minx)*scalex),(int)((first[1]-miny)*scaley), 
                			(int)((secon[0]-minx)*scalex),(int)((secon[1]-miny)*scaley) 
                			);
            	}
            }
        }
    }

}
