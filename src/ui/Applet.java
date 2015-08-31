package ui;

import java.io.InputStream;

import javax.swing.*;

import solutions.Gaussian;
import solutions.StaticFunctions;

/*
 * 
 * convert shapefile to geojson:
 * for %f in (*.shp) do ogr2ogr -f "GeoJSON" "%~dpnf.json" "%f"

 */
/*
 * TODO:
 *  
 * VISUAL
 * add option to show google map in background, and set transparency
 * add toolbar (zoom, view options) (zoom in, out, reset ("0"); force mutate, alias level, # maps, district labels; open / merge / save / export
 * 
 * 
 * FEATURE
 * add button to force mutate on toolbar
 * add option to lock together a set of districts together (under constraints)
 * 
 */

public class Applet extends JApplet {
	public static boolean no_gui = false;
	public static String open_project = null;
	
    public static void main( String[] args ) {
    	for( int i = 0; i < args.length; i++) {
    		String arg = args[i];
    		if( arg.contains("nogui") || arg.contains("headless")) {
    			no_gui = true;
    		}
    		if( arg.contains("project") || arg.contains("file")) {
    			if( i+1 < args.length) {
    				open_project = args[i+1];
    			}
    		}
    	}
		new Applet();
	}
    public Applet() {
		//InputStream in = getClass().getResourceAsStream("/resources/Wards_111312_ED_110612.json"); 
		//System.out.println("in: "+in);
		//System.exit(0);

    	StaticFunctions.binomial(1, 1);
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.51));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.52));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.55));
    	/*
    	 * binomial cache created.
binomial_as_normal n:1001.0 k:500.0 p:0.51 ret:0.7468185279694906
0.7468185279694906
binomial_as_normal n:1001.0 k:500.0 p:0.52 ret:0.902888853982651
0.902888853982651
binomial_as_normal n:1001.0 k:500.0 p:0.55 ret:0.9993399261462654
0.9993399261462654
pct to hide: 0.0
    	 * 
    	 */

    	MainFrame mainFrame = new MainFrame();
    	if( !no_gui) {
    		mainFrame.show();
    	}
    }


}
