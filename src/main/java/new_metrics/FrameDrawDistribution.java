package new_metrics;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class FrameDrawDistribution extends JFrame {

	public PiePanel panel = null;
	public static int method = 1;
	
	public Vector<Double> values = null;

	public FrameDrawDistribution(Vector<Double> values) {
		super();
		this.values = values;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setSize(1000,1000);
		setTitle("Draw");
		
		panel = new PiePanel();
		panel.setBounds(0, 0, 1000, 1000);
		getContentPane().add(panel);
	}
	
	
	class PiePanel extends JPanel {
		
	    public void paintComponent(Graphics graphics0) {
	    	try {

	            Graphics2D graphics = (Graphics2D)graphics0;
		        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

			    super.paintComponent(graphics);
			    
			    graphics.setColor(Color.white);
			    graphics.fillRect(0, 0, 1000, 1000);
			    
			    graphics.setColor(Color.gray);
			    
			    if( values == null) {
			    	return;
			    }
			    if( method == 0) {
			    	drawMethod0(graphics);
			    } else {
			    	drawMethod1(graphics);
			    }
			    //graphics.drawLine(transformx(last_midx),transformy(last_midy),transformx(1),transformy(0));
					
	    	} catch (Exception ex) {
	    		System.out.println("ex csafs "+ex);
	    		ex.printStackTrace();
	    		
	    	}
	    	
	    }
	    private void drawMethod1(Graphics2D graphics) {
		    int count = values.size();
		    double area_scale = (double)5000/(double)count;
		    double min = values.get(0);
		    double max = values.get(count-1);
		    double bounds = Math.abs(min) > Math.abs(max) ? Math.abs(min) : Math.abs(max);
		    double dx = bounds/50;
		    double last_x = 1;
		    double last_y = 0;
		    int lasti = 0;
		    int iyzero = transformy(0);
		    for(double d = -bounds; d < bounds; d+=dx) {
		    	int i = lasti;
		    	while( i < count && values.get(i) < d) {
		    		i++;
		    	}
		    	double new_y = area_scale*(double)(i-lasti);
		    	double new_x = d/bounds;

		    	if( new_x < 0) {
				    graphics.setColor(Color.blue);
	    		} else {
				    graphics.setColor(Color.red);
	    		}
		    	int ix0 = transformx(last_x);
		    	int ix1 = transformx(new_x);
		    	int iy0 = transformy(last_y);
		    	int iy1 = transformy(new_y);
	    		graphics.drawLine(ix0,iy0,ix1,iy1);
	    		Polygon poly = new Polygon(new int[]{ix0,ix1,ix1,ix0}, new int[]{iy0,iy1,iyzero,iyzero},4);
	    		graphics.fillPolygon(poly);
	    		graphics.drawPolygon(poly);
		    	
		    	last_y = new_y;
		    	last_x = new_x;
		    	lasti = i;
		    }
		    graphics.setColor(Color.blue);
		    
	    	int ix0 = transformx(last_x);
	    	int ix1 = transformx(1);
	    	int iy0 = transformy(last_y);
	    	int iy1 = transformy(0);
			graphics.drawLine(ix0,iy0,ix1,iy1);
			Polygon poly = new Polygon(new int[]{ix0,ix1,ix1,ix0}, new int[]{iy0,iy1,iyzero,iyzero},4);
			graphics.fillPolygon(poly);
		}
		public void drawMethod0(Graphics2D graphics) {
		    int averaging = 2;
		    int count = values.size();
		    double min = values.get(0);
		    double max = values.get(count-1);
		    double bounds = Math.abs(min) > Math.abs(max) ? Math.abs(min) : Math.abs(max);
		    int inc = count/200;
		    double last_x = -1;
		    double last_y = 0;
		    double last_midx = -1;
		    double last_midy = 0;
		    int iyzero = transformy(0);
		    for(int i = inc; i < count; i += inc) {
		    	double new_x = values.get(i);
		    	for( int j = 0; j < averaging; j++) {
		    		new_x += values.get(i+j);
		    		new_x += values.get(i-j);
		    	}
		    	new_x /= 1+averaging*2;
		    	new_x /= bounds;
	
		    	double area = 0.5;
		    	double delta_x = Math.abs(new_x - last_x);
		    	double avg_y = area/delta_x;
		    	double half_delta_y = avg_y - last_y;
		    	double new_y = last_y + half_delta_y*2;
	
	
		    	double new_midx = (new_x+last_x)/2;
		    	double new_midy = (new_y+last_y)/2;
	
	//	    	if( new_x < last_x) {
	//	    		new_midx = 1;
	//	    		new_midy = 0;
	//	    	}
	
		    	if( new_midx > 0) {
				    graphics.setColor(Color.blue);
	    		} else {
				    graphics.setColor(Color.red);
	    		}
		    	//transformx(last_midx),transformy(last_midy),transformx(new_midx),transformy(new_midy));
		    	int ix0 = transformx(last_midx);
		    	int ix1 = transformx(new_midx);
		    	int iy0 = transformy(last_midy);
		    	int iy1 = transformy(new_midy);
	    		graphics.drawLine(ix0,iy0,ix1,iy1);
	    		Polygon poly = new Polygon(new int[]{ix0,ix1,ix1,ix0}, new int[]{iy0,iy1,iyzero,iyzero},4);
	    		graphics.fillPolygon(poly);
	    		//graphics.drawLine(transformx(last_x),transformy(last_y),transformx(new_x),transformy(new_y));
	
		    	
		    	
		    	last_x = new_x;
		    	last_y = new_y;
		    	last_midx = new_midx;
		    	last_midy = new_midy;
		    }
		    graphics.setColor(Color.blue);
		    
	    	int ix0 = transformx(last_midx);
	    	int ix1 = transformx(1);
	    	int iy0 = transformy(last_midy);
	    	int iy1 = transformy(0);
			graphics.drawLine(ix0,iy0,ix1,iy1);
			Polygon poly = new Polygon(new int[]{ix0,ix1,ix1,ix0}, new int[]{iy0,iy1,iyzero,iyzero},4);
			graphics.fillPolygon(poly);
	    }
    	int transformx(double x) {
    		return (int)Math.round(x*200+250);
    	}
    	int transformy(double y) {
    		return (int)Math.round(-y+500);
    	}
}

}
