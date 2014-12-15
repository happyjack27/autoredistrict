package ui;

import geoJSON.Feature;
import geoJSON.FeatureCollection;
import geoJSON.Geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.JPanel;

public class MapPanel extends JPanel {
	double minx,maxx,miny,maxy;
	FeatureCollection featureCollection;

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
        Geometry.shiftx = minx;
        Geometry.shifty = miny;
        Geometry.scalex = scalex;
        Geometry.scaley = scaley;
        if( featureCollection != null) {
        	featureCollection.draw(g);
        }
    }
}
