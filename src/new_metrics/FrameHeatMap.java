package new_metrics;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.math3.distribution.BetaDistribution;

import util.GenericClasses.Pair;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class FrameHeatMap extends JFrame {

	public static boolean draw_axis = true;
	public static boolean draw_grid = true;
	public static double zoom = 1.0;
	public double[][] hm = null;
	PiePanel panel = null;
	
	public FrameHeatMap() {
		super();
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(720,720);
		setTitle("Draw");
		
		panel = new PiePanel();
		panel.setBounds(0, 0, 1000, 1000);
		getContentPane().add(panel);
	}
	
	BetaDistribution dist = null;
	Vector<BetaDistribution> dists = null;
	double[] seats = null;
	public Vector<Pair<Double, Double>> bins = null;
	
	class PiePanel extends JPanel {
		
	    public void paintComponent(Graphics graphics0) {
	    	try {

	            Graphics2D graphics = (Graphics2D)graphics0;
		        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

			    super.paintComponent(graphics);
			    
			    graphics.setColor(Color.white);
			    graphics.fillRect(0, 0, 1000, 1000);

			    int xs = 5;//10-5;
			    int ys = 30;//40-5;
			    int w = 640;
			    int h = 640;

			    graphics.setColor(Color.gray);
			    if( hm != null) {
			    	for( int i = 0; i < hm.length; i++) {
			    		int x0 = xs+i*w/hm.length;
			    		int x1 = xs+(i+1)*w/hm.length;
			    		int w2 = w/2;
			    		int h2 = h/2;
			    		//zoom 2x
			    		x0 = (int) (w2+zoom*(x0-w2));
			    		x1 = (int) (w2+zoom*(x1-w2));
				    	for( int j = 0; j < hm[i].length; j++) {
				    		int div = hm[i].length;
				    		div += (div+1) % 2; //make sure it's odd
				    		int y0 = j*h/div;
				    		int y1 = (j+1)*h/div;
				    		//zoom 2x
				    		y0 = (int) (h2+zoom*(y0-h2));
				    		y1 = (int) (h2+zoom*(y1-h2));

				    		y0 = h-y0+ys;
				    		y1 = h-y1+ys;
				    		
				    		int c = 256-(int)(hm[i][j]*256.0);
				    		c = c < 0 ? 0 : c > 255 ? 255 : c;
				    		//System.out.println("c:"+c);
				    		Color clr = j > hm[i].length/2 ? new Color(255,c,c) : j == hm[i].length/2 ? new Color(c,c,c) : new Color(c,c,255);
				    		graphics.setColor(clr);
				    		//graphics.fillRect(w2+2*(x0-w2), h2+2*(y1-h2), 2*(x1-x0), 2*(y0-y1));
				    		graphics.fillRect(x0, y1, x1-x0, y0-y1);
				    		//System.out.println(""+x0+" "+y1+" "+(x1-x0)+" "+(y0-y1)+" "+c+" "+hm[i][j]);
				    	}
			    	}
			    }
			    
			    if( draw_grid) {
				    graphics.setColor(Color.lightGray);
				    for( int i = 0; i < 21; i++) {
				    	int xm = xs+w*i/20;
				    	int ym = ys+h*i/20;
					    graphics.drawLine(xs, ym, xs+w, ym);
					    graphics.drawLine(xm, ys, xm, ys+h);
				    }
				    graphics.setColor(Color.black);
				    for( int i = 0; i < 21; i++) {
				    	int xm = xs+w*i/20;
				    	int ym = ys+h*i/20;
					    graphics.drawLine(xs+w/2-5, ym, xs+w/2+5, ym);
					    graphics.drawLine(xm, ys+h/2-5, xm, ys+h/2+5);
				    }
			    }
			    if( draw_axis) {
				    graphics.drawLine(xs+w/2, ys, xs+w/2, ys+h);
				    graphics.drawLine(xs, ys+h/2, xs+w, ys+h/2);
			    }

	    	} catch (Exception ex) {
	    		System.out.println("ex csafs "+ex);
	    		ex.printStackTrace();
	    		
	    	}
	    	
	    }
    	int transformx(double x) {
    		return (int)Math.round(500-x*500);
    	}
    	int transformy(double y) {
    		return (int)Math.round(-y*5+400);
    	}
	}
	/*
	public int xd(int x) {
		x -= w;
		x *= 2;
		x += w;
		return xd;
	}*/

}
