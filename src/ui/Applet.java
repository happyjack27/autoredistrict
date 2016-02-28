package ui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;

import javax.swing.*;

import util.Gaussian;
import util.StaticFunctions;


// failed california connectioin reset
//failed kentucky , new hampshire - states with spaces, rhode island, texas
//oklahoma
/*
ownloading vtd shapfile...
downloading:
url :http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
path:/Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/
file:vtds.zip
no history found! []
ex on download1, retrying: java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:436)
	at ui.Download.run(Download.java:237)
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
*/

/*
 * failed: arkansas, 06 (california), kentucky, michigan missipi
 * 
 * downloadNextState run 28 Mississippi...
starting Mississippi...
done downloading. extracting...
done extracting.
done extracting.
Starting next thread.
Processing tl_2012_28_vtd10.shp...
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Mississippi/2010/2012/vtd/tl_2012_28_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)

 * 
 * path:/Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/
file:vtds.zip
ex on download1, retrying: java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:436)
	at ui.Download.run(Download.java:237)
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:452)
	at ui.Download.run(Download.java:237)
ex on download2: failed to open site java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
Downloading census population...

 * 
 * downloading:
url :ftp://ftp2.census.gov/geo/tiger/TIGER2010BLKPOPHU/tabblock2010_06_pophu.zip
path:/Users/jimbrill/autoredistrict_data/California/2010/block_pop/
file:block_pops.zip
ex on download: java.net.SocketException: Connection reset
java.net.SocketException: Connection reset
	at java.net.SocketInputStream.read(SocketInputStream.java:209)
	at java.net.SocketInputStream.read(SocketInputStream.java:141)

 * 
 * ers/jimbrill/autoredistrict_data/Arkansas/2010/block_centroids//tl_2010_05_tabblock10.shp.xml...
extracting /Users/jimbrill/autoredistrict_data/Arkansas/2010/block_centroids//tl_2010_05_tabblock10.shx...
done extracting.
done extracting.
Starting next thread.
Processing tl_2012_05_vtd10.shp...
Exception in thread "Thread-0" java.lang.NoClassDefFoundError: org/nocrala/tools/gis/data/esri/shapefile/ValidationPreferences
	at ui.MainFrame.loadShapeFile(MainFrame.java:3633)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1989)
 * 
 * 
 */


/*
 * failed: california,kentucky,louisianna,maine, massachtuses, michigan, minnesota
 * 
 * corrrupt vtd.zip download
 * 
 * vtds.zip: Zip archive data, at least v2.0 to extract
Jims-MacBook-Air:vtd jimbrill$ unzip vtds.zip 
Archive:  vtds.zip
  End-of-central-directory signature not found.  Either this file is not
  a zipfile, or it constitutes one disk of a multi-part archive.  In the
  latter case the central directory and zipfile comment will be found on
  the last disk(s) of this archive.
unzip:  cannot find zipfile directory in one of vtds.zip or
        vtds.zip.zip, and cannot find vtds.zip.ZIP, period.

 * 
 * Processing tl_2012_25_vtd10.shp...
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Massachusetts/2010/2012/vtd/tl_2012_25_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)
	at ui.MainFrame.getFile(MainFrame.java:6132)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1975)
exception in processing shapefile: java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Massachusetts/2010/2012/vtd/tl_2012_25_vtd10.shp (No such file or directory)
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Massachusetts/2010/2012/vtd/tl_2012_25_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)
	at ui.MainFrame.loadShapeFile(MainFrame.java:3626)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1987)
0 precincts loaded.

 * 
 * Starting next thread.
Processing tl_2012_23_vtd10.shp...
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Maine/2010/2012/vtd/tl_2012_23_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)
	at ui.MainFrame.getFile(MainFrame.java:6132)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1975)
exception in processing shapefile: java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Maine/2010/2012/vtd/tl_2012_23_vtd10.shp (No such file or directory)
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Maine/2010/2012/vtd/tl_2012_23_vtd10.shp (No such file or directory)

 * Processing tl_2012_22_vtd10.shp...
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Louisiana/2010/2012/vtd/tl_2012_22_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)
	at ui.MainFrame.getFile(MainFrame.java:6132)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1975)
exception in processing shapefile: java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Louisiana/2010/2012/vtd/tl_2012_22_vtd10.shp (No such file or directory)
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Louisiana/2010/2012/vtd/tl_2012_22_vtd10.shp (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
	at java.io.FileInputStream.<init>(FileInputStream.java:138)
	at ui.MainFrame.loadShapeFile(MainFrame.java:3626)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1987)
0 precincts loaded.

 * 
 * downloading:
url :http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
path:/Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/
file:vtds.zip
no history found! []
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:428)
	at ui.Download.run(Download.java:230)
java.io.FileNotFoundException: http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1835)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1440)
	at java.net.URL.openStream(URL.java:1038)
	at ui.Download.download(Download.java:434)
	at ui.Download.run(Download.java:230)
failed to open get site http://www2.census.gov/geo/tiger/TIGER2012/VTD/tl_2012_21_vtd10.zip
unzipping /Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/vtds.zip
java.io.FileNotFoundException: /Users/jimbrill/autoredistrict_data/Kentucky/2010/2012/vtd/vtds.zip (No such file or directory)



SKIPPED: ARIZONA

Processing tl_2012_04_vtd10.shp...
Exception in thread "Thread-0" java.lang.NoClassDefFoundError: org/nocrala/tools/gis/data/esri/shapefile/ValidationPreferences
	at ui.MainFrame.loadShapeFile(MainFrame.java:3627)
	at ui.MainFrame$OpenShapeFileThread.run(MainFrame.java:1987)
Caused by: java.lang.ClassNotFoundException: org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences
	at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:331)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	... 2 more


*/
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
	public static String[] args = null;
	
    public static void main( String[] _args ) {
    	args = _args;
    	if( false) {
	    	for( int i = 27; i < Download.states.length; i++) {
	    		//System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar delete "+i);
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
			new Applet();
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			if( Download.states[Download.istate].length() == 0) {
				System.exit(0);
			}
			Download.istate--;
			mainFrame.downloadNextState();
		} else
		if( args.length > 1 && args[0].equals("delete")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			Download.delete();
			System.exit(0);
		} else 
		if( args.length > 1 && args[0].equals("clean")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			Download.clean();
			System.exit(0);
		} else {
			new Applet();
		}
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
    	if( args.length > 1 && args[0].equals("run")) {
			mainFrame.ip.queueInstructionsFromFile(args[1]);
    	}
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
