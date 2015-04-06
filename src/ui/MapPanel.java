package ui;

import geography.Feature;
import geography.FeatureCollection;
import geography.Geometry;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import solutions.iDiscreteEventListener;

public class MapPanel extends JPanel implements MouseListener, MouseMotionListener, iDiscreteEventListener {
	double minx,maxx,miny,maxy;
	FeatureCollection featureCollection;
	boolean zooming = false;
	Rectangle selection = null;
	Stack<double[]> zoomStack = new Stack<double[]>();

	MapPanel() {
        // set a preferred size for the custom panel.
        setPreferredSize(new Dimension(200,200));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
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
        if( zooming) {
        	if( selection != null) {
        		//Color c = new Color();
        		g.setColor(new Color(128,128,128,128));
        		g.fillRect(selection.x, selection.y, selection.width, selection.height);
           		g.setColor(new Color(255,255,255,255));
           		g.drawRect(selection.x, selection.y, selection.width, selection.height);
        	}
        }
    }

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
		if( zooming) {
			selection = new Rectangle(arg0.getX(),arg0.getY(),0,0);
			
			return;
		}
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
		if( featureCollection == null || featureCollection.features == null) {
			return null;
		}
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
		if( zooming) {
			if( selection == null) {
				return;
			}
 			int w = arg0.getX()-selection.x;
 			int h = arg0.getY()-selection.y;
 			if( w < 0) {
 				selection.x = arg0.getX();
 				w = -w;
 			}
 			if( h < 0) {
 				selection.y = arg0.getY();
 				h = -h;
 			}
 			selection.width = w;
 			selection.height = h;
			zooming = false;
			zoomTo(selection);
			invalidate();
			repaint();
		}
	}
	public void zoomOut() {
		
		double[] dd = zoomStack.pop();
		if( dd == null) {
			return;
		}

        minx = dd[0];
        miny = dd[1];
        maxx = dd[2];
        maxy = dd[3];
        invalidate();
        repaint();
	}
	
	public void zoomTo(Rectangle r) {
        Dimension d = this.getSize();
        double scalex = ((double)d.getWidth())/(maxx-minx);
        double scaley = ((double)d.getHeight())/(maxy-miny);
        double x0 = minx+r.x/scalex;
        double y0 = miny+r.y/scaley;
        double x1 = minx+(r.x+r.width)/scalex;
        double y1 = miny+(r.y+r.height)/scaley;
		zoomStack.push(new double[]{minx,miny,maxx,maxy});

        minx = x0;
        miny = y0;
        maxx = x1;
        maxy = y1;

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if( zooming) {
			if( selection == null) {
				return;
			}
 			int w = e.getX()-selection.x;
 			int h = e.getY()-selection.y;
 			selection.width = w;
 			selection.height = h;
			invalidate();
			repaint();
		}

		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventOccured() {
		invalidate();
		repaint();
	}
}
