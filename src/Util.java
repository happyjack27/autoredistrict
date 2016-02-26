import java.io.*;
import java.net.URL;
import java.nio.*;
import java.nio.channels.*;
import java.util.Vector;

public class Util {
	
	//missing: alaska and lousianna!
	
	static final String path = "C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\all_state_elections_and_demo_from_google_drive";
	static final String[] states1 = new String[]{
		//"Louisiana",
		"Alaska",
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


	public static void main(String[] args) {
		//processVTD();
		//System.exit(0);
		
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
		};
		String[] renames = new String[]{
				"CTY_PRES12_DEM",
				"CTY_PRES12_REP",
				"CTY_PRES08_DEM",
				"CTY_PRES08_REP",
				"CTY_PRES04_DEM",
				"CTY_PRES04_REP",
		};
		v.add(renames);
		try {
			String filestring = readStream(new FileInputStream(file)).toString();
			String[] lines = filestring.split("\n");
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
				"CTY_VAP_TOT", //31
				"CTY_VAP_WHITE",
				"CTY_VAP_BLACK",
				"CTY_VAP_HISPANIC",
				"CTY_VAP_ASIAN",
				"CTY_VAP_INDIAN",
				"CTY_VAP_OTHER",
				//"PRES12_DEM", //39
				//"PRES12_REP",
		};
		v.add(renames);
		try {
			String filestring = readStream(new FileInputStream(sum_file)).toString();
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
}