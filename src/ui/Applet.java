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
 * OCCASSIONALLY STOPPING AND STARTING SEEMS TO DO MAGIC - MAKE IT DO THAT AUTOMATICALLY AND FIND OUT WHY
 * 
 * MAKE IT JOIN ON GEOID BY DEFAULT
 * MAKE A SETTINGS.JSON FILE IN AUTOREDISTRICT_JSON THAT STORES THE STUFF LIKE RECENT FILES
 * ABILITY TO COMPARE RESULTS
 * ABILITY TO COPY COLUMN TO NEW COLUMN
 * ABILITY TO EXPORT RESULTS IN .DOC
 * 
 * on import census data - say no centroid (intplat,intplon) found, and ask if there's another file that contains centroid,
then join on geoid

add feature to download all data from census.gov and aggregate it all, given state name and census year.

then just need to import vtd-level election data

make a wizard - > simple vs advanced

in wizard add option to switch to tweaking (no mutate of elite) after so many generations, then save after so many more.

add selection mode: tournament (will have to add slider to that then)
 * 
 * 
 * -----
 * 
 * add feature to mutate towards competitiveness only (mutates towards reduces wasted votes between the two districts)
 * 
 * EVOLUTION
 * make mutate proportional to district score (pop imbalance, compactness, competitiveness...)
 * add show other measures on map - compactness (iso quotent), wasted votes
 * 
 * VISUAL
 * add option to show google map in background, and set transparency
 * add toolbar (zoom, view options) (zoom in, out, reset ("0"); force mutate, alias level, # maps, district labels; open / merge / save / export
 * 
 * add map color by population excess/deficit ( DISPLAY_MODE_DIST_POP ) add to menu - also add disp mode ("color by district") to menu, remove reset from demographcs checkbox, add separator
 * add map color by demographic ( DISPLAY_MODE_DIST_DEMO ) add to menu
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
