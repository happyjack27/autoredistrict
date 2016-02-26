package ui;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

import javax.swing.*;

import solutions.Gaussian;
import solutions.StaticFunctions;



/*
TOP PRIOIRTY TODO: multi-member isn't counting wasted votes right.  should look at how they'd be assigned if there were no wasted votes, and then take the difference, and then total the positive ones.		
*/
		
/*
 * 
 * convert shapefile to geojson:
 * for %f in (*.shp) do ogr2ogr -f "GeoJSON" "%~dpnf.json" "%f"

 */
/*
 * 
 * TODO: substitute uncontested should ask for district column, and then substitute it then, and then afterwards when a district column is selected (even after a new data merge?), that data isn't re-substituted.
 *    Does this means substitute uncontested should write back to the original data columns? (so it gets populated in a data save) 
 * 
 * TODO: make substitute columsn work with second eleciotn by haivng uncontested look at raw demographics, na dDistrict.uncontested by a vector matching demographcis
 * 
 * TODO: make reset al stuff on load.
 * 
 * TODO: add PVI ranked by district chart
 * 
 * TODO: add calculate seat-vote curve (graph)
 * 
 * TODO: add option to lock statewide result to 50/50?
 * 
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
/* ----
 * 
 * ADDD: CLEAN, DELETE, LOAD, SET, RUN
 * 
 * FAILED: CALIFORNIA, ONE OTHER MODE BEFORE IT, HAWAII, DISTRICT OF COLUMBIA, KENTUCKY
 * 
 * ownloadNextState run 21 Kentucky...
 * RAN OUT OF SPACE AT KENTUCKY
starting Kentucky...
Downloading vtd shapfile...
downloading:
url :http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
path:/Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/
file:vtds.zip
ex java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:424)
	at ui.Download.run(Download.java:230)

 * 
 * Exception in thread "Thread-3" java.lang.NumberFormatException: For input string: "<null>"
	at sun.misc.FloatingDecimal.readJavaFormatString(FloatingDecimal.java:2043)
	at sun.misc.FloatingDecimal.parseDouble(FloatingDecimal.java:110)
	at java.lang.Double.parseDouble(Double.java:538)
	at ui.MainFrame.writeDBF(MainFrame.java:3551)
	at ui.MainFrame.saveData(MainFrame.java:6262)
	at ui.MainFrame$ImportCountyLevel.run(MainFrame.java:2216)


 * 
 * 
 * 	at ui.MainFrame$ImportCensus2Thread.run(MainFrame.java:1415)
state_abbr: |AZ|
java.net.SocketException: Unexpected end of file from serverjava.net.SocketException: Unexpected end of file from server
	at sun.net.www.http.HttpClient.parseHTTPHeader(HttpClient.java:792)
	at sun.net.www.http.HttpClient.parseHTTP(HttpClient.java:647)
	at sun.net.www.http.HttpClient.parseHTTP(HttpClient.java:675)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1535)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.MainFrame.processVTDrenameFile(MainFrame.java:6329)
	at ui.MainFrame$ImportTranslations.run(MainFrame.java:2039)

port county level start
url: http://autoredistrict.org/county_level_stats/Merged%20--%20District of Columbia.txt
0
0.0
0.1
1
2
ex java.io.FileNotFoundException: http://autoredistrict.org/county_level_stats/Merged%20--%20District of Columbia.txtjava.io.FileNotFoundException: http://autoredistrict.org/county_level_stats/Merged%20--%20District of Columbia.txt
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.MainFrame$ImportCountyLevel.run(MainFrame.java:2093)
5..
Exception in thread "Thread-3" java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
	at java.util.Vector.remove(Vector.java:831)
	at ui.MainFrame$ImportCountyLevel.run(MainFrame.java:2121)


 * 
 */
public class Applet extends JApplet {
	public static MainFrame mainFrame = null;
	public static boolean no_gui = false;
	public static String open_project = null;
	
    public static void main( String[] args ) {
    	if( false) {
	    	for( int i = 0; i < Download.states.length; i++) {
	    		System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar download "+i);
	    		System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar clean "+i);
	    	}
			System.exit(0);
    	}
		
    	for( int i = 0; i < args.length; i++) {
    		System.out.println("arg: "+args[i]);
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
		if( args.length > 1 && args[0].equals("download")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			if( Download.states[Download.istate].length() == 0) {
				System.exit(0);
			}
			new Applet();
			mainFrame.downloadNextState();
		} else
		if( args.length > 1 && args[0].equals("delete")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			deleteRecursive(new File(Download.getStartPath()));
			System.exit(0);
		} else 
		if( args.length > 1 && args[0].equals("clean")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			deleteRecursive(new File(Download.getStartPath()+File.separator+"block_centroids"));
			deleteRecursive(new File(Download.getStartPath()+File.separator+"block_pop"));
			System.exit(0);
		}
		new Applet();
	}
	public static void deleteRecursive(File f)  {
		System.out.println("deleting "+f.getAbsolutePath());
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      deleteRecursive(c);
	  }
	  f.delete();
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


    	mainFrame = new MainFrame();
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
