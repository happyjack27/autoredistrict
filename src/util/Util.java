package util;
import geography.Feature;

import java.io.*;
import java.net.URL;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.Map.Entry;

import ui.Download;

public class Util {
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
			if( Download.apportionments[i] < 6) {
				//continue;
			}
			if( Download.apportionments[i] < 1) {
				continue;
			}
			if( Download.apportionments[i] > 5) {
				//continue;
			}
			if( i < 35) {
				//continue;
			}
			String state = Download.states[i];
			
			
			if( false
					//|| state.equals("Alaska")
					//|| state.equals("California")
					|| state.equals("Texas")
					|| state.equals("Florida")
					|| state.equals("California")
					//|| state.equals("Louisianna")
					//|| state.equals("Rhode Island")
					//|| state.equals("Kentucky")
					) {
				continue;
			}
			
			if( true
					&& !state.equals("Texas")
					&& !state.equals("Florida")
					&& !state.equals("California")

					//&& !state.equals("New York")
					//&& !state.equals("Oklahoma")
					//&& !state.equals("Alaska")
					//&& !state.equals("Louisiana")
					//|| state.equals("California")
					//|| state.equals("Texas")
					
					//&& !state.equals("Rhode Island")
					//&& !state.equals("Kentucky")
					//&& !state.equals("Oregon") //missing election data!
					//&& !state.equals("Montana")
					/*
					&& !state.equals("Delaware")
					&& !state.equals("Hawaii")
					&& !state.equals("North Dakota")
					&& !state.equals("Vermont")
					&& !state.equals("Michigan")
					&& !state.equals("Indiana")
					&& !state.equals("Virginia")
					&& !state.equals("Wisconsin")*/
					) {
				//continue;
			}
			StringBuffer sb = new StringBuffer();
			sb.append("LOAD "+i+ " 2010 2012\n");
			
			
			/*
			sb.append("MERGE\n");
			sb.append("IMPORT COUNTY\n");
			
			for( int j = 0; j < states_vtd.length; j++) {
				if( states_vtd[j].equals(Download.states[i])) {
					sb.append("IMPORT ELECTIONS\n");
					sb.append("SAVE\n");
					break;
				}
			}
			
			
			//sb.append("IMPORT BDISTRICTING\n");
			//sb.append("IMPORT CURRENT_DISTRICTS\n");
			*/
			sb.append("COPY FEATURE PRES12_DEM PRES12_D50\n");
			sb.append("COPY FEATURE PRES12_REP PRES12_R50\n");
			sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
			sb.append("RESCALE ELECTIONS\n");
			//sb.append("SAVE\n");
			
			sb.append("SET POPULATION COLUMN POPULATION\n");
			sb.append("SET COUNTY COLUMN COUNTY_NAM\n");
			sb.append("SET WEIGHT COUNT_SPLITS TRUE\n");
			sb.append("SET COUNTY COLUMN COUNTY_NAM\n");
			sb.append("SET ETHNICITY COLUMNS VAP_WHITE VAP_BLACK VAP_HISPAN VAP_ASIAN VAP_INDIAN VAP_OTHER\n");
			//sb.append("SET ELECTION COLUMNS CD12_DEM CD12_REP\n");
			//sb.append("COPY FEATURE CONGRESS_F CD_FV\n");
			//sb.append("COPY FEATURE AR_RESULT CONGRESS_F\n");
			//sb.append("MERGE\n");
			//sb.append("SAVE\n");
			//sb.append("EXIT\n");
			
			
			sb.append("SET DISTRICTS COLUMN CD_BD\n");
			sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
			sb.append("EXPORT NATIONAL\n");

			sb.append("SET DISTRICTS COLUMN CD_NOW\n");
			sb.append("SET ELECTION COLUMNS PRES12_D50 PRES12_R50\n");
			sb.append("EXPORT NATIONAL\n");

			/*
			if( Download.apportionments[i] <= 5) {
				sb.append("SET DISTRICTS SEATS_PER_DISTRICT "+Download.apportionments[i]+"\n"); 			
			} else {
				sb.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			}
			sb.append("SET DISTRICTS COLUMN CD_FV\n");
			if( Download.apportionments[i] <= 5) {
				sb.append("SET DISTRICTS SEATS_PER_DISTRICT "+Download.apportionments[i]+"\n"); 			
			} else {
				sb.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			}
			sb.append("EXPORT\n");
			*/
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


	public static void main(String[] args) {
		//mergeTransparentImages("/Users/jimbrill/autoredistrict_data/", "CD_BD", "map_districts.png", 1024, 1024, "/");
		mergeTransparentImages("http:/autoredistrict.org/autoredistrict_data/", "CD_NOW", "map_vtd_votes.png", 1024, 1024, "/");

		//"http://localhost:8888/autoredistrict"
		//sfds
		//writeHTML();
		//make_scripts();
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
	
	public static Triplet<String,Feature,Integer> findBestMatch(String source_string, Hashtable<String,Feature> dictionary) {
		String test_source_string = source_string.toUpperCase().trim();

		//try for exact match
		Feature s = dictionary.get(test_source_string);
		if( s != null) {
			return new Triplet<String,Feature,Integer>(test_source_string,s,0);
		}


		//find closest match if no exact match.
		boolean tie = false;
		int ibest = -1;
		String sbest = "";
		Feature obest = null;
		for( Entry<String,Feature> entry: dictionary.entrySet()) {
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
		return new Triplet<String,Feature,Integer>(sbest,obest,ibest);
	}
}