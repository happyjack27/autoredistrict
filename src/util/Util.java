package util;
import geography.VTD;

import java.io.*;
import java.net.URL;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.Map.Entry;

import ui.Download;

/*
=======

MORE THAN 5 SEATS

LOAD 2010 <ENTER FIPS CODE HERE>
COPY FEATURE CD_FV2 CD_CB
SAVE
LOAD 2010 [FIPS]

SET PREFER4S TRUE
SET ELECTION COLUMNS PRES12_D50 PRES12_R50
SET DISTRICTS COLUMN CD_CB
SET DISTRICTS FAIRVOTE_SEATS [SEATS]

SET WEIGHT PARTISAN 0.0
SET WEIGHT RACIAL 0.0

SET WEIGHT GEOMETRY_FAIRNESS 0.25

SET EVOLUTION ANNEAL_RATE 0.80
SET EVOLUTION MUTATE_RATE 1.0
SET EVOLUTION ELITE_FRAC 0.50
SET EVOLUTION POPULATION 200

GO
WHEN MUTATE_RATE 0.5

SET WEIGHT CONTIGUITY 1.0
SET WEIGHT POPULATION 1.0

SET WEIGHT GEOMETRY_FAIRNESS 0.20

SET EVOLUTION ELITE_MUTATE_FRAC 0.5
SET EVOLUTION MUTATE_RATE 1.00

WHEN MUTATE_RATE 0.3

STOP
SAVE
EXPORT PIE
EXPORT HTMLONLY
EXPORT NATIONAL
EXIT
EXIT

==========

5 OR LESS SEATS

LOAD 2010 <ENTER FIPS CODE HERE>
COPY FEATURE CD_FV2 CD_CB
SAVE
LOAD 2010 [FIPS]

SET PREFER4S TRUE
SET ELECTION COLUMNS PRES12_D50 PRES12_R50
SET DISTRICTS COLUMN CD_CB
SET DISTRICTS FAIRVOTE_SEATS [SEATS]

EXPORT PIE
EXPORT HTMLONLY
EXPORT NATIONAL
EXIT
EXIT


 */

/*ftp://ftp2.census.gov/geo/tiger/TIGERrd13/SLDL/
 * 
EXTRACT "ftp://ftp2.census.gov/geo/tiger/TIGERrd13/SLDL/tl_rd13_[FIPS]_sldl.zip" SLDL
OPEN "SLDL/tl_rd13_[FIPS]_sldl.shp"
SET DISTRICTS COLUMN CD113FP

EXPORT BLOCKS "SLDL/blocks.txt"

LOAD [FIPS] 2010 2012
IMPORT BLOCKS "[START PATH]blocks.txt" TRUE TRUE sldlst SLDL_2010
SAVE


 * 
EXTRACT "ftp://ftp2.census.gov/geo/tiger/TIGERrd13/CD113/tl_rd13_[FIPS]_cd113.zip" CD113
OPEN "[START_PATH]CD113/tl_rd13_[FIPS]_cd113.shp"
SET DISTRICTS COLUMN CD113FP
EXPORT BLOCKS "CD113/blocks.txt"
LOAD [FIPS] 2010 2012
IMPORT BLOCKS "[START_PATH]CD113/CD113.txt" TRUE TRUE CD113FP CD_2010

IMPORT BLOCKS "blocks.txt" TRUE TRUE CD113FP CD_2010
 */
 
public class Util {
	/*
	 * 
	 * potential redos (descr rep):
California
Massacthuses
Michigan
New Jersey
New York
Ohio
Pennsylvania
Virginia
Wisconsin

	 
	 */
	static int apportionment_threshold = 15;
	static boolean only_redos = false;
	public static String[] redo_states = new String[]{
		"Oklahoma",
		//"Kentucky",
		//"Rhode Island",
		//"Pennsylvania",
		//"Tennessee",
		//"Alabama",
		//"New York",
		//"Oklahoma",
		//"Virginia",
		//"Texas",
		//"New York",
		//"Washington",
		//"Wisconsin",
		//"Michigan",
		//"Ohio",
		//"South Carolina",
		/*
		"Illinois",
		"California",
		"Florida",
		"Texas",
		"Massachusetts",
		"Virginia",
		"New Jersey",
		"New York",
		"Ohio",*/
	};
	
	public static String[] bad_states_round1 = new String[]{
		"Illinois",
		"Louisiana",
		"Maine",
		"Maryland",
		"Massachusetts",
		"Minnesota",//Minnesota
		"New Jersey",
		"Ohio",
		"Oklahoma",
		"Oregon",
		"Pennsylvania",
		"South Dakota",
		"Texas",
		"Vermont",
		"Virginia",
	};
	
	public static String[] bad_states = new String[]{
		"Massachusetts",
		"Ohio",
		"Texas",
		"Virginia",
		"Pennsylvania",
		"Oregon"
	};

	/*
	 Minnesota
Illinois
Ohio
Louisianna
Texas
Maine
California
Florida
South Dakota
Vermount
Maryland
Masschtuses
Nevada
New Mexico
	 * 
	 */
	
	/*

	"Illinois",
	"Louisiana",
	"Maine",
	"Maryland",
	"Massachusetts",
	"Minnessota",
	"New Jersey",
	"Ohio",
	"Oklahoma",
	"Pennsylvania",
	"South Dakota",
	"Texas",
	"Washington",//?

	 */
	


	public static void mergeTransparentImages(String path, String column, String image_name, int width, int height, String separator) {
		System.out.println("<?php ");
		System.out.println("\tdefine(\"WIDTH\","+width+");");
		System.out.println("\tdefine(\"HEIGHT\","+height+");");
		System.out.println("\t$dest_image = imagecreatetruecolor(WIDTH, HEIGHT);");
		System.out.println("\timagealphablending($dest_image, true);");
		System.out.println("\timagesavealpha($dest_image, true);");
		System.out.println("\t$trans_background = imagecolorallocatealpha($dest_image, 0, 0, 0, 127);");
		System.out.println("\timagefill($dest_image, 0, 0, $trans_background);");
		System.out.println("\t");
		for( int i = 0; i < states.length; i++) {
			String p = path+states[i].replaceAll(" ","%20")+separator+"2010"+separator+column+separator+"national"+separator+image_name;
			System.out.println("\t$image_"+i+" = imagecreatefrompng(file_get_contents('"+p+"'));");

			System.out.println("\timagecopy($dest_image, $image_"+i+", 0, 0, 0, 0, WIDTH, HEIGHT);");
		}
		System.out.println("\t");
		//System.out.println("\timagepng($dest_image);");
		System.out.println("\timagepng($dest_image,'"+image_name+"');");
		System.out.println("\techo('done');");
		System.out.println("?>");
	}	

	
	//missing: alaska and lousianna!
	
	static final String path = "/Users/jimbrill/autoredistrict_data/county_stats";
	//"C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\all_state_elections_and_demo_from_google_drive";

    public static int LevenshteinDistance(String a, String b) {
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

	static final String[] states1 = new String[]{
		//"Louisiana",
		"Alaska",
	};
	static final String[] states_vtd = new String[]{
		"Alabama",
		"Arkansas",
		"California",
		"Connecticut",		
		"Delaware",
		"Florida",
		"Georgia",
		"Hawaii",
		"Idaho",
		"Illinois",
		"Indiana",
		"Iowa",
		"Kansas",
		"Kentucky",
		//"Louisiana",
		"Maine",
		//"Maryland",
		"Massachusetts",
		"Michigan",
		//"Minnesota",
		"Mississippi",
		"Missouri",
		"Montana",
		"Nebraska",
		//"Nevada",
		//"New Hampshire",
		"New Jersey",
		"New Mexico",
		"New York",
		"North Carolina",
		"North Dakota",
		"Ohio",
		"Oklahoma",
		//"Oregon",
		"Pennsylvania",
		//"Rhode Island",
		"South Carolina",
		"South Dakota",
		"Tennessee",
		//"Texas",
		"Utah",
		"Vermont",
		"Virginia",
		"Washington",
		"West Virginia",
		"Wisconsin",
		"Wyoming",
	};
	static final String[] states = new String[]{
		"Alabama",
		"Alaska",
		"Arizona",
		"Arkansas",
		"California",
		"Colorado",
		"Connecticut",
		"Delaware",
		"Florida",
		"Georgia",
		"Hawaii",
		"Idaho",
		"Illinois",
		"Indiana",
		"Iowa",
		"Kansas",
		"Kentucky",
		"Louisiana",
		"Maine",
		"Maryland",
		"Massachusetts",
		"Michigan",
		"Minnesota",
		"Mississippi",
		"Missouri",
		"Montana",
		"Nebraska",
		"Nevada",
		"New Hampshire",
		"New Jersey",
		"New Mexico",
		"New York",
		"North Carolina",
		"North Dakota",
		"Ohio",
		"Oklahoma",
		"Oregon",
		"Pennsylvania",
		"Rhode Island",
		"South Carolina",
		"South Dakota",
		"Tennessee",
		"Texas",
		"Utah",
		"Vermont",
		"Virginia",
		"Washington",
		"West Virginia",
		"Wisconsin",
		"Wyoming",
	};
	
	public static void writeHTML() {
		Download.init();
		for( int i = 0; i < states.length; i++) {
			System.out.println("<tr>");
			System.out.println("<td>"+states[i]+"</td>");
			//file:///Users/jimbrill/autoredistrict_data/Alabama/2010/map_districts.png
			System.out.println("<td><a href='fairvote/"+states[i]+"/2010/stats.html'>stats</a></td>");
			System.out.println("<td><img src='fairvote/"+states[i]+"/2010/map_districts.png' width=100></td>");
			System.out.println("</tr>");
		}
	}
	
	public static Vector processVTD() {
		String path = "http://www2.census.gov/geo/docs/reference/codes/files/national_vtd.txt";
	    URL url;
	    InputStream is = null;
	    BufferedReader br;
	    String line;
        Vector<String[]> v = new Vector<String[]>();

	    try {
	        url = new URL(path);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));
	        v.add( new String[]{"STATE","STATEFP","COUNTYFP","COUNTYNAME","VTDST","VTDNAME","CTYFP_FUL","VTDST_FUL","VTDST_HLF"});
	        int i = 0;

	        while ((line = br.readLine()) != null) {
	        	if( i == 0) { //skip first line
	        		i++;
	        		continue;
	        	}
	        	String[] row = line.split("\\|");//[|]");
	        	if( row.length < 6) {
	        		continue;
	        	}
	        	if( i > 10) {
	        		break;
	        	}
	            System.out.println(line);
	        	String[] ss = new String[]{
	        			row[0],
	        			row[1],
	        			row[2],
	        			row[3],
	        			row[4],
	        			row[5],
	        			row[1]+"-"+row[2],
	        			row[1]+"-"+row[2]+"-"+row[4],
	        			row[2]+"-"+row[4],
	        	};
	        	
	        	v.add(ss);
        		i++;
	        }
	    } catch (Exception mue) {
	         mue.printStackTrace();
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	    }
	    return v;
	}
	public static void make_scripts() {
		String prepend = "";
		//prepend = "xvfb-run -a -e xvfb.log  ";
		boolean gui = true;
		String base_dir = "/Users/jimbrill/git/autoredistrict/jar/";
		String script = "";
		String script2 = "";
		Download.init();
		File f2 = new File(base_dir+"sourcescript");
		try {
			script = util.Util.readStream(new FileInputStream(f2));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File f3 = new File(base_dir+"sourcescript2");
		try {
			script2 = util.Util.readStream(new FileInputStream(f3));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(script2);
		
		StringBuffer main = new StringBuffer();
		int i0 = 0;
		//while( !Download.states[i0].equals("Indiana")) { i0++; }
		for( int i = 0; i < Download.apportionments.length; i++){// && !Download.states[i].equals("Colorado"); i++) {
			if( i == 6) {
				//continue; //skip cali for now
			}
		
			if( Download.apportionments[i] < 6) {
				//continue;
			}
			if( Download.apportionments[i] <= 1) {
				continue;
			}
			if( Download.apportionments[i] > 5) {
				//continue;
			}
			if( i < 42) {
				//continue;
			} 
			String state = Download.states[i];
			if( !state.equals("Ohio") && !state.equals("Florida")) {
				//continue;
			}
			
			boolean hit = false;
			for( int j = 0; j < redo_states.length; j++) {
				if( redo_states[j].equals(state)) {
					hit = true;
					break;
				}
			}
			if( Download.apportionments[i] > apportionment_threshold) {
				//hit = true;
			}
			if( !hit && only_redos) {
				continue;
			}

			StringBuffer sb = new StringBuffer();
			/*
			sb.append("LOAD "+i+ " 2010 2012\n");
			sb.append("IMPORT DEMOGRAPHICS\n");
			
			if( Download.apportionments[i] == 1) {
				sb.append("COPY FEATURE CD_BD CD_NOW\n");
				
			}
			sb.append("COPY FEATURE CD_NOW CD_2000\n");
			sb.append("SAVE\n");
			*/
			/*
			sb.append("SAVE\n");
			if( state.equals("Alabama")) {
				sb.append("IMPORT BDISTRICTING\n");
				sb.append("SAVE\n");
			}*/
			/*
			if( Download.states[i].equals("New York")) {
				continue;
			}
			
			sb.append("LOAD "+i+ " 2010 2012\n");
			
			if( i > 37) {
				sb.append("IMPORT COUNTY\n");	
				if( !Download.states[i].equals("Louisiana") && !Download.states[i].equals("Texas")) {
					sb.append("IMPORT ELECTIONS\n");
				}
			}
			
			sb.append("SAVE\n");
			
			sb.append("IMPORT CURRENT_DISTRICTS\n");
			sb.append("IMPORT BDISTRICTING\n");
			sb.append("COPY FEATURE CD_NOW CD_2000\n");
			if( !hit) {
				sb.append("IMPORT URL http://autoredistrict.org/all50/version2/CD_PRES/[STATE]/2010/CD_FV/vtd_data.txt GEOID10 GEOID10 CD_FV\n".replaceAll("\\[STATE\\]",state.replaceAll(" ","%20")));
//	IMPORT URL http://autoredistrict.org/all50/version2/CD_PRES/Ohio/2010/CD_FV/vtd_data.txt GEOID10 GEOID10 CD_FV
			}
			
			sb.append("SAVE\n");
			sb.append("LOAD "+i+ " 2010 2012\n");

			//sb.append("IMPORT CURRENT_DISTRICTS\n");
			//if( Download.states[i].equals("Vermont") || Download.states[i].equals("New Mexico")) {
				sb.append("COPY FEATURE PRES12_DEM PRES12_D50\n");
				sb.append("COPY FEATURE PRES12_REP PRES12_R50\n");
				sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
				sb.append("RESCALE ELECTIONS\n");
				sb.append("SAVE\n");

				//sb.append("IMPORT URL http://autoredistrict.org/all50/version2/CD_PRES/[STATE]/2010/CD_FV/vtd_data.txt GEOID10 GEOID10 CD_FV\n".replaceAll("\\[STATE\\]",state.replaceAll(" ","%20")));
			//}
				*/
			if( Download.apportionments[i] != 1)  {
				//continue;
			}
			if( i < 6) {
				//continue;
			}
			if( 
					i != 9 &&
					i != 42 &&
					i != 17 &&
					i != 26
					) {
				continue;
			}
 			sb.append("LOAD "+i+ " 2010 2012\n");
 			
 			/*
LOAD 9 2010 2012
IMPORT URL ftp://autoredistrict.org/pub/2012%20Pres%20results%20by%202012%20CDs/[FIPS].csv CD_2010A CD_2010A PRES12_DEM PRES12_REP
COPY FEATURE PRES12_DEM PRES12_D50
COPY FEATURE PRES12_REP PRES12_R50
RESCALE ELECTIONS
SAVE
SET ELECTION COLUMNS PRES12_D50 PRES12_R50
SET DISTRICTS COLUMN CD113FP
SET DISTRICTS COLUMN CD_2010A
SET WEIGHT DESCRIPTIVE 0.50
EXPORT NATIONAL
EXPORT STATS
EXPORT PIE
CLEAN
EXIT
EXIT
STOP
SET DISTRICTS SEATS_PER_DISTRICT [SEATS]
SET DISTRICTS COLUMN CD_FV
SET DISTRICTS FAIRVOTE_SEATS [SEATS]
EXPORT STATS
EXIT
EXIT
 			 */
 			String s = ""
 					
 					//+"IMPORT POPULATION\n"
 					+"IMPORT URL ftp://autoredistrict.org/pub/2012%20Pres%20results%20by%202012%20CDs/[FIPS].csv CD_2010A CD_2010A PRES12_DEM PRES12_REP\n"
 					+"COPY FEATURE PRES12_DEM PRES12_D50\n"
 					+"COPY FEATURE PRES12_REP PRES12_R50\n"
 					+"RESCALE ELECTIONS\n"
 					+"SAVE\n"
 					
 					//+"RESCALE ELECTIONS\n"
 					//+"SAVE\n"

 					//+"OPEN "CD113/tl_rd13_[FIPS]_cd113.shp"
 					+"SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n"
 					+"SET DISTRICTS COLUMN CD113FP\n"
 					+"SET DISTRICTS COLUMN CD_2010A\n"
 					+"SET WEIGHT DESCRIPTIVE 0.50\n"
 					//+"EXPORT PIE\n"
 					+"EXPORT NATIONAL\n"
 					+"EXPORT STATS\n"
 					+"EXPORT PIE\n"
 					+"CLEAN\n"
 					+"EXIT\n"
 					+"EXIT\n";

 					;
 			sb.append(s);
 			//appendExportHare(sb,i);
 			
 			//appendExportEmbedded(sb,i);
 			//sb.append("COPY FEATURE CD_FV CD_FV2\n");
 			//sb.append("SAVE\n");
			//sb.append("LOAD "+i+ " 2010 2012\n");
			//newscript(sb,i);
			//import2010(sb,i);
			/*
			sb.append("COPY FEATURE PRES12_DEM PRES12_D50\n");
			sb.append("COPY FEATURE PRES12_REP PRES12_R50\n");
			sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
			sb.append("RESCALE ELECTIONS\n");
			sb.append("SAVE\n");
			sb.append("LOAD "+i+ " 2010 2012\n");

			appendExport(sb);
			sb.append("IMPORT DEMOGRAPHICS\n");
			if( Download.apportionments[i] == 1) {
				sb.append("COPY FEATURE CD_BD CD_2000\n");
				sb.append("COPY FEATURE CD_BD CD_NOW\n");
			}
			sb.append("SAVE\n");
			sb.append("EXIT\n");
			sb.append("EXIT\n");
			sb.append("LOAD "+i+ " 2010 2012\n");
			appendExport(sb);

			sb.append("IMPORT CY2000_DISTRICTS\n");
			sb.append("COPY FEATURE CD_NOW CD_2000\n");
			sb.append("SAVE\n");
			sb.append("EXIT\n");
			sb.append("EXIT\n");
			appendExport2000(sb);
			
			if( Download.apportionments[i] == 1) {
				sb.append("COPY FEATURE CD_BD CD_2000\n");
				sb.append("COPY FEATURE CD_BD CD_NOW\n");
			}
			//sb.append("IMPORT DEMOGRAPHICS\n");

			sb.append("SET DISTRICTS COLUMN CD_2000\n");
			sb.append("EXPORT\n");
			sb.append("EXPORT NATIONAL\n");

			sb.append("EXIT\n");
			sb.append("EXIT\n");
			
			sb.append("SAVE\n");
			sb.append("LOAD "+i+ " 2010 2012\n");
			appendExport(sb);
			*/

			
			
			//sb.append("COPY FEATURE CD_FV CD_FV2\n");
			if( i != 25 && i != 17) {
			//sb.append("COPY FEATURE PRES12_DEM PRES12_D50\n");
			//sb.append("COPY FEATURE PRES12_REP PRES12_R50\n");
			//sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
			//sb.append("RESCALE ELECTIONS\n");
			//sb.append("SAVE\n");
			//sb.append("LOAD "+i+ " 2010 2012\n");
			}


			/*
			sb.append("SET DISTRICTS COLUMN CD_NOW\n");
			sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
			sb.append("EXPORT\n");
			sb.append("EXPORT NATIONAL\n");
			sb.append("SET DISTRICTS COLUMN CD_2000\n");
			sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
			sb.append("EXPORT\n");
			sb.append("EXPORT NATIONAL\n");
			*/
			
			
			sb.append("STOP\n");

			if( Download.states[i].equals("Texas") || Download.states[i].equals("New York")  || Download.states[i].equals("Virginia") || Download.states[i].equals("New Jersey")) {
			if( false && Download.apportionments[i] > 5) {
				sb.append("SET WEIGHT DESCRIPTIVE 0.45\n");
				sb.append("SET EVOLUTION POPULATION 200\n");
				sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
				sb.append("SET DISTRICTS COLUMN CD_FV\n");
				sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");
				sb.append("SET WEIGHT GEOMETRY_FAIRNESS 0.5\n");
				sb.append("SET EVOLUTION POPULATION 200\n");
				sb.append("SET EVOLUTION MUTATE_RATE 1.0\n");
				sb.append("SET EVOLUTION ANNEAL_RATE 0.80\n");
				sb.append("SET EVOLUTION ELITE_FRAC 0.50\n");
				sb.append("SET WEIGHT POPULATION 0.5\n");
				sb.append("SET WEIGHT DESCRIPTIVE 0.5\n");
				sb.append("GO\n");
				sb.append("SET WEIGHT DESCRIPTIVE 1.0\n");
				sb.append("SET WEIGHT COMPETITION 0.25\n");
				sb.append("SET WEIGHT PROPORTIONAL 0.1\n");
				sb.append("SET WEIGHT PARTISAN 0.1\n");
				sb.append("SET WEIGHT RACIAL 0.1\n");
				sb.append("SET EVOLUTION MUTATE_RATE 1.0\n");
				sb.append("SET EVOLUTION ELITE_MUTATE_FRAC 1.0\n");
				//sb.append("SET EVOLUTION POPULATION 200\n");
				sb.append("SET WEIGHT CONTIGUITY 1.0\n");
				//sb.append("WHEN MUTATE_RATE 0.5\n");
				sb.append("SET WEIGHT GEOMETRY_FAIRNESS 0.9\n");
				//sb.append("SET MUTATE_RATE 0.80\n");
				
				sb.append("WHEN MUTATE_RATE 0.5\n");
				sb.append("SET WEIGHT GEOMETRY_FAIRNESS 0.25\n");
				sb.append("SET EVOLUTION MUTATE_RATE 0.80\n");
				sb.append("SET WEIGHT CONTIGUITY 1.0\n");
				sb.append("SET WEIGHT DESCRIPTIVE 1.0\n");
				sb.append("SET WEIGHT PROPORTIONAL 0.1\n");
				sb.append("SET WEIGHT PARTISAN 0.1\n");


				sb.append("WHEN MUTATE_RATE 0.5\n");
				sb.append("SET WEIGHT GEOMETRY_FAIRNESS 0.2\n");
				sb.append("SET EVOLUTION ELITE_MUTATE_FRAC 0.5\n");
				sb.append("SET WEIGHT POPULATION 1.0\n");
				sb.append("SET EVOLUTION ELITE_MUTATE_FRAC 0.5\n");
				sb.append("SET EVOLUTION MUTATE_RATE 1.00\n");
				sb.append("WHEN MUTATE_RATE 0.3\n");
				sb.append("STOP\n");
				sb.append("SAVE\n");
			}
			}			
			
			//sb.append("SET ELECTION COLUMNS CD12_DEM CD12_REP\n");
			//sb.append("COPY FEATURE CONGRESS_F CD_FV\n");
			//sb.append("COPY FEATURE AR_RESULT CONGRESS_F\n");
			//sb.append("MERGE\n");
			//sb.append("SAVE\n");
			//sb.append("EXIT\n");
			
			/*
			sb.append("SET DISTRICTS SEATS_PER_DISTRICT 1\n");		
			//if( state.equals("Texas")) {
				sb.append("SET DISTRICTS COLUMN CD_BD\n");
				//sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
				sb.append("EXPORT\n");
				sb.append("EXPORT NATIONAL\n");
	
				sb.append("SET DISTRICTS COLUMN CD_NOW\n");
				//sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
				sb.append("EXPORT\n");
				sb.append("EXPORT NATIONAL\n");
			//}
				*/
			

			if( hit || true) {
				if( Download.apportionments[i] <= 5) {
					sb.append("SET DISTRICTS SEATS_PER_DISTRICT [SEATS]\n");//"+Download.apportionments[i]+"\n"); 			
				} else {
					sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
				}
				sb.append("SET DISTRICTS COLUMN CD_FV\n");
				//if( Download.apportionments[i] <= 5) {
				//	sb.append("SET DISTRICTS SEATS_PER_DISTRICT [SEATS]\n");//"+Download.apportionments[i]+"\n"); 			
				//} else {
					sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
				//}
					/*
					 * 
					 * 
STOP
SET ELECTION COLUMNS PRES12_D50 PRES12_R50
SET DISTRICTS COLUMN CD_FV
SET DISTRICTS FAIRVOTE_SEATS [SEATS]
SET WEIGHT GEOMETRY_FAIRNESS 1.0
SET WEIGHT DESCRIPTIVE 0.50
SET EVOLUTION POPULATION 200
SET EVOLUTION MUTATE_RATE 1.0
SET EVOLUTION ANNEAL_RATE 0.76
SET EVOLUTION ELITE_FRAC 0.50
SET WEIGHT DESCRIPTIVE 1.0
SET WEIGHT POPULATION 0.5
GO
WHEN MUTATE_RATE 0.25
SET WEIGHT CONTIGUITY 1.0
SET WEIGHT GEOMETRY_FAIRNESS 0.5
SET MUTATE_RATE 1.00
WHEN MUTATE_RATE 0.25
SET WEIGHT CONTIGUITY 0.5
SET EVOLUTION ELITE_MUTATE_FRAC 0.5
SET MUTATE_RATE 1.00
WHEN MUTATE_RATE 0.25
STOP
SAVE
EXPORT
EXPORT_NATIONAL
EXIT
EXIT

					 */
					sb.append("EXPORT STATS\n");
				//sb.append("EXPORT\n");
				//sb.append("EXPORT NATIONAL\n");
			}
			
			//sb.append("SAVE\n");
			sb.append("EXIT\nEXIT\n\n");
/*
			if( false
					|| Download.apportionments[i] <= 7  //6=3+3,8=5+3
					//|| Download.apportionments[i] == 9 //9=3+3+3,10=5+5,11=3+3+5,12=3+3+3+3,13=5+5+3,14=3+3+3+5,15=5+5+5,16=3+3+5+5
					//
					) {
				sb.append("SET DISTRICTS ALLOW_4_SEATS TRUE\n");
			}
			sb.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			//sb.append(script+"\n");
			sb.append("SET DISTRICTS COLUMN CONGRESS_F\n");
			sb.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			sb.append("STOP\n");
			if( false
					|| Download.apportionments[i] <= 7  //6=3+3,8=5+3
					//|| Download.apportionments[i] == 9 //9=3+3+3,10=5+5,11=3+3+5,12=3+3+3+3,13=5+5+3,14=3+3+3+5,15=5+5+5,16=3+3+5+5
					//
					) {
				sb.append("SET DISTRICTS ALLOW_4_SEATS TRUE\n");
			}			
			sb.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			sb.append("SET DISTRICTS COLUMN CONGRESS_F\n");
			//sb.append("STOP\n");
			sb.append("\tEXPORT\n");
			//sb.append("\tSAVE\n");
			sb.append("\tEXIT\n");
			*/

			System.out.println(sb.toString());

			
			File f = new File(base_dir+"subscript"+i);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(f);
				fos.write(sb.toString().getBytes());
				fos.flush();
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( hit) {
				//main.append(prepend+"java -jar -Xmx4096M -Xms1024M autoredistrict.jar delete "+i+"\n");
			}
			main.append(prepend+"java -jar -Xmx4096M -Xms1024M autoredistrict.jar "+(gui?"":"nogui ")+"run subscript"+i+"\n");
			main.append(prepend+"java -jar -Xmx4096M -Xms1024M autoredistrict.jar clean "+i+"\n");
		}
		File f = new File(base_dir+"mainscript");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(main.toString().getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void import2010(StringBuffer sb, int i) {
		if( Download.apportionments[i] == 1) {
			sb.append(""
					+"\nCOPY FEATURE CD_BD CD_2010"
					+"\nSAVE"
					);			
		} else {
			sb.append(""
					+"\nEXTRACT \"ftp://ftp2.census.gov/geo/tiger/TIGERrd13/CD113/tl_rd13_[FIPS]_cd113.zip\" CD113"
					+"\nOPEN \"CD113/tl_rd13_[FIPS]_cd113.shp\""
					+"\nSET DISTRICTS COLUMN CD113FP"
					+"\nEXPORT BLOCKS \"CD113/blocks.txt\""
					+"\nLOAD [FIPS] 2010 2012"
					+"\nIMPORT BLOCKS \"[START PATH]blocks.txt\" TRUE TRUE CD113FP CD_2010"
					+"\nSAVE"

					/*
					+"\nEXTRACT \"ftp://ftp2.census.gov/geo/tiger/TIGERrd13/CD113/tl_rd13_[FIPS]_cd113.zip\" CD113"
					+"\nOPEN \"CD113/tl_rd13_[FIPS]_cd113.shp\""
					+"\nSET DISTRICTS COLUMN CD113FP"
					+"\nEXPORT BLOCKS \"CD113/blocks.txt\""
					+"\nLOAD [FIPS] 2010 2012"
					+"\nIMPORT BLOCKS [START PATH]blocks.txt TRUE TRUE CD113FP CD_2010"
					+"\nSAVE"
					*/
					);
		}
		sb.append(""
				+"\nSET ELECTION COLUMNS PRES12_D50 PRES12_R50"
				+"\nSET DISTRICTS COLUMN CD_2010"
				//+"\nSET ELECTION COLUMNS PRES12_D50 PRES12_R50"
				+"\nEXPORT"
				+"\nEXPORT NATIONAL"
				+"\nEXIT"
				+"\nEXIT"
				);

	}

/*
 * ./sc: line 3: cd: /home/autotrader/autoredistrict_data_cd113/Alaska/2010/CD_2010A/: No such file or directory
./sc: line 15: cd: /home/autotrader/autoredistrict_data_cd113/Delaware/2010/CD_2010A/: No such file or directory
./sc: line 51: cd: /home/autotrader/autoredistrict_data_cd113/Montana/2010/CD_2010A/: No such file or directory
./sc: line 67: cd: /home/autotrader/autoredistrict_data_cd113/North Dakota/2010/CD_2010A/: No such file or directory
./sc: line 81: cd: /home/autotrader/autoredistrict_data_cd113/South Dakota/2010/CD_2010A/: No such file or directory
./sc: line 89: cd: /home/autotrader/autoredistrict_data_cd113/Vermont/2010/CD_2010A/: No such file or directory
./sc: line 99: cd: /home/autotrader/autoredistrict_data_cd113/Wyoming/2010/CD_2010A/: No such file or directory
*/
	public static void main(String[] args) {
		System.out.println(""
				+"\nEXTRACT \"ftp://ftp2.census.gov/geo/tiger/TIGERrd13/CD113/tl_rd13_[FIPS]_cd113.zip\" CD113"
				+"\nOPEN \"CD113/tl_rd13_[FIPS]_cd113.shp\""
				+"\nSET DISTRICTS COLUMN CD113FP"
				+"\nEXPORT BLOCKS \"CD113/blocks.txt\""
				+"\nLOAD [FIPS] 2010 2012"
				+"\nIMPORT BLOCKS \"[START PATH]blocks.txt\" TRUE TRUE CD113FP CD_2010"
				+"\nSAVE"
			);
		System.exit(0);

		
		String[] ss = new String[]{
				"Alaska",
				"Delaware",
				"Montana",
				"North Dakota",
				"South Dakota",
				"Vermont",
				"Wyoming"
				//nh
				
				//ri
		};
		
		for( int i = 0; i < states.length; i++) {
			String state = states[i];
			//String from ="/home/autotrader/autoredistrict_data_cd113/";
			String from = "/Users/jimbrill/autoredistrict_data/";
			String to = "/var/www/html/autoredistrict/website/all50/CD_PRES/";
			String prefix = state+"/2010/";
			//System.out.println("mkdir \""+to+prefix+"CD_2010A\"");
			System.out.println("cd \""+from+prefix+"\"");
			System.out.println("rm -rf ./CD_2010/*");
			System.out.println("rm -rf ./CD_BD/*");
			System.out.println("rm -rf ./CD_FV/*");
			System.out.println("rm -rf ./CD_FV2/*");
			System.out.println("rm -rf ./CD_FVH/*");
			System.out.println("rm -rf ./CD_2000/*");
			System.out.println("rm -rf ./CD_SM/*");
			System.out.println("rm -rf ./CD_NOW/*");
			//System.out.println("/bin/cp -rf ./* \""+to+prefix+"CD_2010A/\"");
			//System.out.println("mkdir \""+to+prefix+"CD_2010A\"");
			//System.out.println("cd \""+from+prefix+"CD_2010A/\"");
			//System.out.println("/bin/cp -rf ./* \""+to+prefix+"CD_2010A/\"");
		}
		//System.exit(0);
		//mergeTransparentImages("/Users/jimbrill/autoredistrict_data/", "CD_BD", "map_districts.png", 1024, 1024, "/");
		//mergeTransparentImages("http:/autoredistrict.org/autoredistrict_data/", "CD_NOW", "map_vtd_votes.png", 1024, 1024, "/");

		//"http://localhost:8888/autoredistrict"
		//sfds
		//writeHTML();
		make_scripts();
		//processVTD();
		System.exit(0);
		
		for( int i = 0; i < states.length; i++) {
			try {
				String state = states[i];
				System.out.println("processing: "+state);
				process(state);
			} catch (Exception ex) {
				System.out.println("ex in main "+ex);
				ex.printStackTrace();
			}
		}
		System.out.println("done.");
	}
	public static void process(String state) {
		//System.out.println("vsum..");
		Vector<String[]> vsum = county_sum(state);
		//System.out.println("vdetail..");
		Vector<String[]> vdetail = county_detail(state);
		//System.out.println("merging..");
		Vector<String[]> vmerged = new Vector<String[]>();
		int s1 = vsum.get(0).length;
		int s2 = vdetail.get(0).length;
		System.out.println("v "+s1+" "+s2);
		for( int i = 0; i < vsum.size(); i++) {
			String[] out = new String[s1+s2];
			String[] vs = vsum.get(i);
			String[] vd = vdetail.get(i);
			for( int j = 0; j < s1; j++ ) {
				out[j] = vs[j];
			}
			for( int j = 0; j < s2; j++ ) {
				out[s1+j] = vd[j];
			}
			vmerged.add(out);
		}
		//System.out.println("showing...");
		/*
		for( int i = 0; i < 10; i++) {
			String[] ss = vmerged.get(i);
			for(int j = 0; j < ss.length; j++) {
				System.out.print("["+j+": "+ss[j].trim()+"]");
			}
			System.out.println();
		}
		*/
		
		File file = new File(path+File.separator+"Merged -- "+state+".txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			for( int i = 0; i < vmerged.size(); i++) {
				String s = "";
				String[] ss = vmerged.get(i);
				for(int j = 0; j < ss.length; j++) {
					if( j > 0) {
						s += "\t";
					}
					s += ss[j].trim();
				}
				s += "\r\n";
				fos.write(s.getBytes());
			}
			fos.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("merged");

	}
	public static Vector<String[]> county_detail(String state) {
		Vector<String[]> v = new Vector();
		File folder = new File(path);
		File file = new File(path+File.separator+"County Detail -- "+state+".txt");
		int[] useful_columns = new int[]{
				3,
				4,
				5,
				6,
				7,
				8,
				9,
				10,
		};
		String[] renames = new String[]{
				/*
				"CTY_PRES12_DEM",
				"CTY_PRES12_REP",
				"CTY_PRES08_DEM",
				"CTY_PRES08_REP",
				"CTY_PRES04_DEM",
				"CTY_PRES04_REP",*/
				"PRES12_DEM",
				"PRES12_REP",
				"PRES08_DEM",
				"PRES08_REP",
				"PRES04_DEM",
				"PRES04_REP",
				"CD12_DEM",
				"CD12_REP",
		};
		v.add(renames);
		try {
			String filestring = Util.readStream(new FileInputStream(file)).toString();
			String[] lines = filestring.split("\n");
			System.out.println("found "+lines.length+" lines");
			
			for(int i = 0; i < 3; i++) {
				String[] ss = lines[i].split("\t");
				for(int j = 0; j < ss.length; j++) {
					//System.out.print("["+j+": "+ss[j].trim()+"]");
				}
				//System.out.println();
			}
			//System.out.println();
			int detail_start = 4;
			for(int i = 4; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				if( ss.length == 0) {
					continue;
				}
				if( ss[0].trim().equals("Total:")) {
					detail_start = i+1;
					break;
				}
			}
			/*
			for(int i = detail_start; i < detail_start+10; i++) {
				String[] ss = lines[i].split("\t");
				for(int j = 0; j < ss.length; j++) {
					System.out.print("["+j+": "+ss[j].trim()+"]");
				}
				System.out.println();
			}
			*/
			for(int i = detail_start; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				/*for(int j = 0; j < ss.length; j++) {
					System.out.print("["+j+": "+ss[j].trim()+"]");
				}*/
				String[] sadd = new String[useful_columns.length];
				for(int j = 0; j < useful_columns.length; j++) {
					sadd[j] = ss.length > useful_columns[j] ? ss[useful_columns[j]] : "";
					//System.out.print("["+useful_columns[j]+": "+ss[useful_columns[j]].trim()+"]");
				}
				v.add(sadd);
				//System.out.println();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
			
		}
		return v;
	}
	public static Vector<String[]> county_sum(String state) {
		Vector<String[]> v = new Vector();
		File folder = new File(path);
		File sum_file = new File(path+File.separator+"County Summary -- "+state+".txt");
		int sum_start=3;
		int[] useful_columns = new int[]{
				0,
				22,
				31,
				32,
				33,
				34,
				35,
				36,
				37,
				//39,
				//40,
		};
		String[] renames = new String[]{
				"COUNTY_NAME",//0
				"COUNTY_FIPS",//22
				"VAP_TOT", //31
				"VAP_WHITE",
				"VAP_BLACK",
				"VAP_HISPANIC",
				"VAP_ASIAN",
				"VAP_INDIAN",
				"VAP_OTHER",
				//"PRES12_DEM", //39
				//"PRES12_REP",
		};
		v.add(renames);
		try {
			String filestring = Util.readStream(new FileInputStream(sum_file)).toString();
			String[] lines = filestring.split("\n");
			for(int j = 0; j < renames.length; j++) {
				//System.out.print("["+j+": "+renames[j]+"]");
			}
			//System.out.println();
			for(int i = 3; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				/*for(int j = 0; j < ss.length; j++) {
					System.out.print("["+j+": "+ss[j].trim()+"]");
				}*/
				String[] sadd = new String[useful_columns.length];
				for(int j = 0; j < useful_columns.length; j++) {
					
					sadd[j] = ss.length > useful_columns[j] ? ss[useful_columns[j]] : "";
					//System.out.print("["+useful_columns[j]+": "+ss[useful_columns[j]].trim()+"]");
				}
				v.add(sadd);
				//System.out.println();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
		/*
		String path = "C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\all_state_elections_and_demo_from_google_drive";
		File folder = new File(path);
		File[] ff = folder.listFiles();
		for(File source : ff) {
			String sourcename = source.getName();
			String[] ss = sourcename.split("2000");
			if( ss.length == 1) {
				ss = sourcename.split("2002");
			}
			if( ss.length == 1) {
				ss = sourcename.split("1992");
			}
			if( ss.length == 1) {
				ss = sourcename.split("1950");
			}
			String dest = ss[0].trim()+".xlsx";
			//System.out.println("renaming from: "+sourcename+" to: "+dest);
			System.out.println(dest);
			source.renameTo(new File(path+"\\"+dest));
		}
		*/
	}


	public static String readStream(InputStream is) {
	    StringBuilder sb = new StringBuilder(512);
	    try {
	        Reader r = new InputStreamReader(is, "UTF-8");
	        int c = 0;
	        while ((c = r.read()) != -1) {
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    return sb.toString();
	}
	
	public static Triplet<String,VTD,Integer> findBestMatch(String source_string, Hashtable<String,VTD> dictionary) {
		String test_source_string = source_string.toUpperCase().trim();

		//try for exact match
		VTD s = dictionary.get(test_source_string);
		if( s != null) {
			return new Triplet<String,VTD,Integer>(test_source_string,s,0);
		}


		//find closest match if no exact match.
		boolean tie = false;
		int ibest = -1;
		String sbest = "";
		VTD obest = null;
		for( Entry<String,VTD> entry: dictionary.entrySet()) {
			String dest_name = entry.getKey();
			String test_name = dest_name.toUpperCase().trim();
			int cur = LevenshteinDistance( test_source_string, test_name);
			if( ibest < 0 || cur < ibest) {
				ibest = cur;
				sbest = test_name;
				obest = entry.getValue();
				tie = false;
			} else if( cur == ibest && !test_name.equals(sbest)) {
				tie = true;	
			}
		}
		System.out.println("matched "+test_source_string+" to "+sbest+" distance: "+ibest+" tie?: "+tie);
		if( tie) {
			return null;
		}
		//max 6 changes, 1/4.
		if( ibest > 6 || ibest > sbest.length()/4) {
			return null;
		}
		System.out.println("matched "+source_string+" to "+sbest);
		return new Triplet<String,VTD,Integer>(sbest,obest,ibest);
	}
	public static void appendExport(StringBuffer sb) {
		sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
		sb.append("SET DISTRICTS COLUMN CD_BD\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT STATS\n");
		//sb.append("EXPORT NATIONAL\n");

		sb.append("SET DISTRICTS COLUMN CD_2000\n");
		sb.append("EXPORT STATS\n");
		//sb.append("EXPORT NATIONAL\n");

		/*
		sb.append("COPY FEATURE CD_2000 CD_NOW");
		sb.append("SET DISTRICTS COLUMN CD_NOW\n");
		sb.append("EXPORT\n");
		sb.append("EXPORT NATIONAL\n");
		*/

		sb.append("SET DISTRICTS COLUMN CD_FV\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("EXPORT STATS\n");
		//sb.append("EXPORT NATIONAL\n");

		sb.append("EXIT\n");
		sb.append("EXIT\n");
	}
	public static void appendExportHare(StringBuffer sb, int i) {
		String state = Download.states[i];
		/*
		sb.append("COPY FEATURE CD_FV2 CD_FVH\n");
		sb.append("SAVE\n");
		sb.append("LOAD [FIPS] 2010 2012\n");
		
		afsd
		*/
		sb.append("FFF\n");
		sb.append("EXIT\n");
		sb.append("EXIT\n");
		sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");

		
		sb.append("SET DISTRICTS COLUMN CD_SM\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");
		/*
		sb.append("SET DISTRICTS COLUMN CD_BD\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");
		
		sb.append("SET DISTRICTS COLUMN CD_2010\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");

		sb.append("SET DISTRICTS COLUMN CD_2000\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");

		//sb.append("EXPORT htmlonly\n");
		//sb.append("EXPORT PIE\n");
		

		sb.append("SET DISTRICTS COLUMN CD_FV2\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("SET WEIGHT DESCRIPTIVE 0.40\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");

		//sb.append("EXPORT htmlonly\n");
		//sb.append("EXPORT PIE\n");
		
		
		sb.append("SET QUOTA HARE\n");
		sb.append("SET DISTRICTS COLUMN CD_FVH\n");
		sb.append("SET QUOTA HARE\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("SET QUOTA HARE\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.40\n");
		sb.append("SET QUOTA HARE\n");

		//sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT NATIONAL\n");
		*/

		sb.append("EXIT\n");
		sb.append("EXIT\n");

	}
	/*
IMPORT URL http://autoredistrict.org/all50/version3/CD_PRES/[STATE]/2010/vtd_data.txt GEOID10 GEOID10 CD_SM
IMPORT URL http://autoredistrict.org/all50/version3/CD_PRES/[STATE]/2010/vtd_data.txt GEOID10 GEOID10 CD_FV2
IMPORT URL http://autoredistrict.org/all50/version3/CD_PRES/[STATE]/2010/vtd_data.txt GEOID10 GEOID10 CD_FVH
SAVE
LOAD [FIPS]] 2010 2012
*/
	
	public static void appendExportEmbedded(StringBuffer sb, int i) {
		String state = Download.states[i];
		/*
		sb.append("IMPORT URL \"http://autoredistrict.org/all50/version3/CD_PRES/[STATE]/2010/vtd_data.txt\" GEOID10 GEOID10 CD_SM\n".replaceAll("\\[STATE\\]",state.replaceAll(" ","%20")));
		sb.append("SAVE\n");
		sb.append("EXIT\n");
		//sb.append("EXIT\n");
		sb.append("LOAD [FIPS] 2010 2012\n");
		*/
		
		//sb.append("FIX CD_2010\n");
		//sb.append("SAVE\n");
		
		sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		/*
		sb.append("SET DISTRICTS COLUMN CD_SM\n");
		sb.append("EXPORT EMBEDDED\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT STATS\n");
		sb.append("EXPORT national\n");
		sb.append("EXIT\n");
		sb.append("EXIT\n");
		*/
		//sb.append("EXPORT\n");
		//sb.append("EXIT\n");
		//sb.append("EXIT\n");

		
		/*
		sb.append("SET DISTRICTS COLUMN CD_2010\n");
		sb.append("EXPORT EMBEDDED\n");
		sb.append("EXPORT PIE\n");
		sb.append("EXPORT html\n");
		sb.append("EXPORT national\n");
		sb.append("EXIT\n");
		sb.append("EXIT\n");
		*/

		
		sb.append("SET DISTRICTS COLUMN CD_SM\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");

		sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		//sb.append("EXPORT STATS\n");
		//sb.append("EXPORT national\n");
		
		sb.append("SET DISTRICTS COLUMN CD_BD\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");

		sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		//sb.append("EXPORT STATS\n");
		//sb.append("EXPORT national\n");

		
		sb.append("SET DISTRICTS COLUMN CD_2000\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");

		sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		//sb.append("EXPORT STATS\n");
		//sb.append("EXPORT national\n");
		
		sb.append("SET DISTRICTS COLUMN CD_2010\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");

		sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		//sb.append("EXPORT STATS\n");
		//sb.append("EXPORT national\n");

/*
		sb.append("SET DISTRICTS COLUMN CD_FV\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("EXPORT EMBEDDED\n");
		sb.append("EXPORT PIE\n");
		*/
		//sb.append("EXPORT HTMLONLY\n");

		sb.append("SET DISTRICTS COLUMN CD_FV2\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("SET WEIGHT DESCRIPTIVE 0.40\n");

		sb.append("EXPORT htmlonly\n");
		sb.append("EXPORT PIE\n");
		//sb.append("EXPORT STATS\n");
		//sb.append("EXPORT national\n");
	

		sb.append("EXIT\n");
		sb.append("EXIT\n");
	}
	public static void appendExport2000(StringBuffer sb) {
		sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");

		sb.append("SET DISTRICTS COLUMN CD_2000\n");
		sb.append("EXPORT\n");
		sb.append("EXPORT NATIONAL\n");

		sb.append("EXIT\n");
		sb.append("EXIT\n");
	}
	/*
		sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
		sb.append("SET DISTRICTS COLUMN CD_BD\n");
		sb.append("SET WEIGHT DESCRIPTIVE 0.50\n");
		sb.append("EXPORT\n");
		sb.append("EXPORT NATIONAL\n");

		sb.append("SET DISTRICTS COLUMN CD_2000\n");
		sb.append("EXPORT\n");
		sb.append("EXPORT NATIONAL\n");
		sb.append("SET DISTRICTS COLUMN CD_FV\n");
		sb.append("SET DISTRICTS FAIRVOTE_SEATS [SEATS]\n");//"+Download.apportionments[i]+"\n"); 
		sb.append("EXPORT\n");
		sb.append("EXPORT NATIONAL\n");

	 */
	public static void newscript(StringBuffer sb, int i) {
		String state = Download.states[i];
		sb.append("IMPORT URL http://autoredistrict.org/all50/version3/CD_PRES/[STATE]/2010/CD_FV/vtd_data.txt GEOID10 GEOID10 CD_FV2\n".replaceAll("\\[STATE\\]",state.replaceAll(" ","%20")));

		sb.append(""
				//+"\nCOPY FEATURE CD_FV CD_FV2"
				//+"\nCOPY FEATURE CD_BD CD_SM"
				+"\nSAVE"				
				//+"\nEXIT"
				
				+"\nSET ELECTION COLUMNS PRES12_D50 PRES12_R50"
				+"\nSET DISTRICTS COLUMN CD_FV2"
				+"\nSET DISTRICTS FAIRVOTE_SEATS [SEATS]"
			);
		
			if( Download.apportionments[i] > 5) {
					sb.append(""
				+"\nEXIT"
				+"\nEXIT"
					
					+"\nSET WEIGHT DESCRIPTIVE 1.0"
					+"\nSET WEIGHT PROPORTIONAL 0.0"
					+"\nSET WEIGHT POPULATION 1.0"
					+"\nSET WEIGHT PARTISAN 0.0"
					+"\nSET EVOLUTION ANNEAL_RATE 0.85"
					+"\nSET EVOLUTION MUTATE_RATE 1.0"
					+"\nGO"
					+"\nSET WEIGHT DESCRIPTIVE 1.0"
					+"\nSET WEIGHT PROPORTIONAL 0.0"
					+"\nSET WEIGHT PARTISAN 0.0"
					+"\nWHEN MUTATE_RATE 0.3"
					);
				}
	
	sb.append(""
				+"\nSTOP"
				+"\nSAVE"
				+"\nEXPORT"
				+"\nEXPORT NATIONAL"
				+"\nEXIT"
				+"\nEXIT"
			);
				
			sb.append(""
				+"\nSET ELECTION COLUMNS PRES12_D50 PRES12_R50"
				+"\nSET DISTRICTS COLUMN CD_SM"
				);
				if( Download.apportionments[i] > 1) {
					sb.append(""
					+"\nSET WEIGHT DESCRIPTIVE 0.5"
					+"\nSET WEIGHT PROPORTIONAL 0.0"
					+"\nSET WEIGHT PARTISAN 0.5"
					+"\nSET EVOLUTION MUTATE_RATE 1.0"
					+"\nSET EVOLUTION ANNEAL_RATE 0.7"
					+"\nSET EVOLUTION POPULATION 200"
					
					+"\nGO"
					+"\nSET WEIGHT DESCRIPTIVE 0.5"
					+"\nSET WEIGHT PROPORTIONAL 0.0"
					+"\nWHEN MUTATE_RATE 0.4"
					+"\nSET EVOLUTION MUTATE_RATE 1.0"
					+"\nWHEN MUTATE_RATE 0.4"
					+"\nSTOP"
					+"\nSAVE"
					);
				}
				sb.append(""
				+"\nEXPORT"
				+"\nEXPORT NATIONAL"
				);
				/*
				sb.append(""
				+"\nEXTRACT \"ftp://ftp2.census.gov/geo/tiger/TIGERrd13/CD113/tl_rd13_[FIPS]_cd113.zip\" CD113"
				+"\nOPEN \"CD113/tl_rd13_[FIPS]_cd113.shp\""
				+"\nSET DISTRICTS COLUMN CD113FP"
				+"\nEXPORT BLOCKS \"CD113/blocks.txt\""
				);
				*/
		sb.append(""
				+"\nEXIT"
				+"\nEXIT"
		);
	}
}