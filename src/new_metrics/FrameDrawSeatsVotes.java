package new_metrics;

import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.math3.distribution.BetaDistribution;

import util.Pair;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FrameDrawSeatsVotes extends JFrame {

	PiePanel panel = null;
	
	public FrameDrawSeatsVotes() {
		super();
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(500,500);
		setTitle("Draw");
		
		panel = new PiePanel();
		panel.setBounds(0, 0, 1000, 1000);
		getContentPane().add(panel);
	}
	
	BetaDistribution dist = null;
	Vector<BetaDistribution> dists = null;
	double[] seats = null;
	public Vector<Pair<Double, Double>> bins = null;
	
	Stroke dotted = new BasicStroke(
		      2f, 
		      BasicStroke.CAP_ROUND, 
		      BasicStroke.JOIN_ROUND, 
		      1f, 
		      new float[] {2f,2f}, 
		      0f);
	
	Stroke dashed = new BasicStroke(
		      2f, 
		      BasicStroke.CAP_ROUND, 
		      BasicStroke.JOIN_ROUND, 
		      1f, 
		      new float[] {4f,4f}, 
		      0f);
	
	
	Stroke line = new BasicStroke(2f);
	/*new BasicStroke(
		      1f, 
		      BasicStroke.CAP_ROUND, 
		      BasicStroke.JOIN_ROUND, 
		      1f, 
		      null,//new float[] {}, 
		      0f);*/
	
	class PiePanel extends JPanel {
		
		public void draw(Graphics2D graphics, int x, int y, int w, int h, Vector<Double> districts, double popular) {
			Collections.sort(districts);
			
			graphics.setStroke(line);
		    graphics.setColor(Color.white);
		    graphics.fillRect(x, y, w, h);
		    graphics.setColor(Color.black);
		    graphics.drawRect(x, y, w, h);
		    double yinc = (double)h / (double)districts.size();
		    double xinc = (double)w;
		    graphics.setColor(Color.gray);
			graphics.setStroke(dotted);
		    graphics.drawLine((int)(x+xinc*popular), y, (int)(x+xinc*popular), y+h);
			graphics.setStroke(line);
		    graphics.setColor(Color.black);
		    double last_x = x;
		    double last_y = y+h;//+(yinc/2.0);
		    double new_x;
		    double new_y;
		    for( Double d : districts) {
		    	new_x = x+d*xinc;
		    	new_y = last_y-yinc;
		    	if( last_y < y) last_y = y;
		    	if( last_y > y+h) last_y = y+h;
		    	//graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)new_y);
		    	graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)last_y);
		    	graphics.drawLine((int)new_x, (int)last_y, (int)new_x, (int)new_y);
		    	last_x = new_x;
		    	last_y = new_y;
		    }
		    new_x=x+w;
		    new_y=y;
		    graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)new_y);

		    graphics.setStroke(dashed);
		    last_x = x+w;
		    last_y = y;//-(yinc/2.0);
		    int[] xs = new int[districts.size()*2+3];
		    int[] ys = new int[districts.size()*2+3];
		    xs[districts.size()*2+0] = x;
		    xs[districts.size()*2+1] = x;
		    xs[districts.size()*2+2] = x+w;
		    ys[districts.size()*2+0] = y+h;
		    ys[districts.size()*2+1] = y;
		    ys[districts.size()*2+2] = y;
		    int i = 0;
		    for( Double d : districts) {
		    	new_x = x+w-d*xinc;
		    	new_y = last_y+yinc;
		    	if( last_y < y) last_y = y;
		    	if( last_y > y+h) last_y = y+h;

		    	xs[i] = (int)new_x;
		    	ys[i] = (int)last_y;
		    	i++;
		    	xs[i] = (int)new_x;
		    	ys[i] = (int)new_y;
		    	i++;
		    	//graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)new_y);
		    	
		    	//graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)last_y);
		    	//graphics.drawLine((int)new_x, (int)last_y, (int)new_x, (int)new_y);
		    	
		    	last_x = new_x;
		    	last_y = new_y;
		    }
		    graphics.drawPolygon(xs, ys, xs.length);
		    new_x=x;
		    new_y=y+h;
		    graphics.drawLine((int)last_x, (int)last_y, (int)new_x, (int)new_y);
		    graphics.setStroke(line);

		}
		
	    public void paintComponent(Graphics graphics0) {
	    	try {

	            Graphics2D graphics = (Graphics2D)graphics0;
		        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

			    super.paintComponent(graphics);
			    
			    graphics.setColor(Color.white);
			    graphics.fillRect(0, 0, 1000, 1000);
			    int spacing = 20;
			    int size = 200;
			    int num = (1000-spacing)/(size+spacing);
			    for( int i = 0; i < num; i++) {
				    for( int j = 0; j < num; j++) {
				    	Vector<Double> pcts = new Vector<Double>();
				    	for( BetaDistribution bd : dists) {
				    		pcts.add(bd.sample());
				    	}

				    	draw(graphics, i*(size+spacing)+spacing,j*(size+spacing)+spacing,size,size,pcts,dist.sample());
				    }
			    }
			    /*
			    BufferedImage bufferedImage = (BufferedImage) createImage(1000, 1000);
			    System.out.println(new File("images/ImageAsTIFF.tiff").getAbsolutePath());
			    File f = new File("ImageAsTIFF.tiff");
			    ImageIO.write(bufferedImage, "TIFF", f);
			    */

			   	
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

}
