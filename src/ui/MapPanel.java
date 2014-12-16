package ui;

import geoJSON.Feature;
import geoJSON.FeatureCollection;
import geoJSON.Geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JPanel;

public class MapPanel extends JPanel implements MouseListener {
	double minx,maxx,miny,maxy;
	FeatureCollection featureCollection;

	MapPanel() {
        // set a preferred size for the custom panel.
        setPreferredSize(new Dimension(200,200));
        this.addMouseListener(this);
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
<<<<<<< HEAD

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		//System.out.println("mouse pressed "+x+" "+y);
		Feature f = getFeature(x,y);
		if( f == null) {
			//System.out.println("no feature");
			return;
		}
		f.toggleClicked();
		invalidate();
		repaint();
		// TODO Auto-generated method stub
		
	}
	Feature getFeature(int x, int y) {
		for( Feature f : featureCollection.features) {
			for( Polygon p : f.geometry.polygons) {
				if( pnpoly(p,x,y)) {
					return f;
				}
			}
		}
		return null;
	}
	public boolean pnpoly(Polygon p, float testx, float testy) {
		int[] ivertx = p.xpoints;
		int[] iverty = p.ypoints;
		float[] vertx = new float[ivertx.length];
		for( int i = 0; i < vertx.length; i++) {
			vertx[i] = (float)ivertx[i];
		}
		float[] verty = new float[iverty.length];
		for( int i = 0; i < verty.length; i++) {
			verty[i] = (float)iverty[i];
		}
		
		int nvert = ivertx.length-1;
		  int i, j;
		  boolean c = false;
		  for (i = 0, j = nvert-1; i < nvert; j = i++) {
		    if ( ((verty[i]>testy) != (verty[j]>testy)) &&
			 (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
		       c = !c;
		  }
		  return c;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
=======
>>>>>>> refs/remotes/origin/master
}
