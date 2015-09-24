package ui;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;

import javax.swing.*;

import solutions.Gaussian;
import solutions.StaticFunctions;

/*
 * 
 * convert shapefile to geojson:
 * for %f in (*.shp) do ogr2ogr -f "GeoJSON" "%~dpnf.json" "%f"

 */
/*
 * 
 * TODO: add ability to automaticaly load in congressional shape file (deaggregate it)
 * 
 * TODO: add ability to merge in using another file for centroids 
 * 
 * TODO: process florida data
 * 
 * OCCASSIONALLY STOPPING AND STARTING SEEMS TO DO MAGIC - MAKE IT DO THAT AUTOMATICALLY AND FIND OUT WHY
 * 
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
    	
    	String version = System.getProperty("java.version");
    	System.out.println("jre version: "+version);
    	if( versionCompare(version,"1.5") < 0) {
    		JOptionPane.showMessageDialog(null, ""
    				+"You are running an out-of-date version of Java."
    				+"\nWith this current installed version of Java, the program will not be able to allocate enough memory."
    				+"\n"
    				+"\nPlease upgrade your java version."
    				+"\nTo find the latest release, google \"java jre download\"."
    				+"\n"
    				+"\nOnce you hit okay, you will be taken automatically to the download page."
    				+"\n"
    				+"\nAfter you've updated your Java version, run this program again."
    				);
    		browseTo("http://www.google.com/search?q=java+jre+download&btnI");
        	System.exit(0);
    	}
    	
    	StaticFunctions.binomial(1, 1);
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.51));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.52));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.55));


    	MainFrame mainFrame = new MainFrame();
    	if( !no_gui) {
    		mainFrame.show();
    	}
    }
	public static void browseTo(String s) {
		try {
			Desktop.getDesktop().browse(new URI(s));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println("failed "+e1);
			e1.printStackTrace();
    		try {
				//Desktop.getDesktop().open(htmlFile.toURI());
			} catch (Exception e2) {
				System.out.println("failed "+e2);
				e1.printStackTrace();
				
			}
		}
	}
    
    public int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))  {
          i++;
        }
        if (i < vals1.length && i < vals2.length)  {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        } else  {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

}
