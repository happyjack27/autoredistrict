package ui;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import util.*;
import util.GenericClasses.BiMap;

//http://www2.census.gov/geo/docs/maps-data/data/baf/BlockAssign_ST06_CA.zip block assignment file
//http://www.census.gov/geo/maps-data/data/baf_description.html description
//http://www.census.gov/geo/maps-data/data/gazetteer2010.html try using these to get population data

//http://www2.census.gov/geo/docs/maps-data/data/gazetteer/census_tracts_list_01.txt
public class Download extends Thread {
	public static boolean census_merge_working = true;
	public static boolean census_merge_old = true;
	public static Thread nextThread = null;
	static boolean download_census = true;
	static boolean download_vtd = true;
	static String census_tract_path = null;
	static String census_centroid_path = null;
	static String census_pop_path = null;
	static String census_vtd_path = null;

	public static int istate = -1;
	public static int cyear = -1;
	public static int vyear = -1;
	
	public static File vtd_file = null;
	public static File vtd_dbf_file = null;
	public static File census_pop_file = null;
	public static File census_centroid_file = null;
	public static File census_tract_file = null;
	public static BiMap<String, String> state_to_abbr = null;
	public static BiMap<String, Integer> state_to_fips = null;
	
	public static boolean downloadAll = false;
	
	public static void init() {
		if( state_to_abbr != null) {
			return;
		}
		state_to_fips = new BiMap<String, Integer>();
		for( int i = 0; i < states.length; i++) {
			state_to_fips.put(states[i], i);
		}
		state_to_abbr = new BiMap<String, String>();
		state_to_abbr.put("Alabama","AL");
		state_to_abbr.put("Alaska","AK");
		state_to_abbr.put("Alberta","AB");
		state_to_abbr.put("American Samoa","AS");
		state_to_abbr.put("Arizona","AZ");
		state_to_abbr.put("Arkansas","AR");
		state_to_abbr.put("Armed Forces (AE)","AE");
		state_to_abbr.put("Armed Forces Americas","AA");
		state_to_abbr.put("Armed Forces Pacific","AP");
		state_to_abbr.put("British Columbia","BC");
		state_to_abbr.put("California","CA");
		state_to_abbr.put("Colorado","CO");
		state_to_abbr.put("Connecticut","CT");
		state_to_abbr.put("Delaware","DE");
		state_to_abbr.put("District Of Columbia","DC");
		state_to_abbr.put("Florida","FL");
		state_to_abbr.put("Georgia","GA");
		state_to_abbr.put("Guam","GU");
		state_to_abbr.put("Hawaii","HI");
		state_to_abbr.put("Idaho","ID");
		state_to_abbr.put("Illinois","IL");
		state_to_abbr.put("Indiana","IN");
		state_to_abbr.put("Iowa","IA");
		state_to_abbr.put("Kansas","KS");
		state_to_abbr.put("Kentucky","KY");
		state_to_abbr.put("Louisiana","LA");
		state_to_abbr.put("Maine","ME");
		state_to_abbr.put("Manitoba","MB");
		state_to_abbr.put("Maryland","MD");
		state_to_abbr.put("Massachusetts","MA");
		state_to_abbr.put("Michigan","MI");
		state_to_abbr.put("Minnesota","MN");
		state_to_abbr.put("Mississippi","MS");
		state_to_abbr.put("Missouri","MO");
		state_to_abbr.put("Montana","MT");
		state_to_abbr.put("Nebraska","NE");
		state_to_abbr.put("Nevada","NV");
		state_to_abbr.put("New Brunswick","NB");
		state_to_abbr.put("New Hampshire","NH");
		state_to_abbr.put("New Jersey","NJ");
		state_to_abbr.put("New Mexico","NM");
		state_to_abbr.put("New York","NY");
		state_to_abbr.put("Newfoundland","NF");
		state_to_abbr.put("North Carolina","NC");
		state_to_abbr.put("North Dakota","ND");
		state_to_abbr.put("Northwest Territories","NT");
		state_to_abbr.put("Nova Scotia","NS");
		state_to_abbr.put("Nunavut","NU");
		state_to_abbr.put("Ohio","OH");
		state_to_abbr.put("Oklahoma","OK");
		state_to_abbr.put("Ontario","ON");
		state_to_abbr.put("Oregon","OR");
		state_to_abbr.put("Pennsylvania","PA");
		state_to_abbr.put("Prince Edward Island","PE");
		state_to_abbr.put("Puerto Rico","PR");
		state_to_abbr.put("Quebec","QC");
		state_to_abbr.put("Rhode Island","RI");
		state_to_abbr.put("Saskatchewan","SK");
		state_to_abbr.put("South Carolina","SC");
		state_to_abbr.put("South Dakota","SD");
		state_to_abbr.put("Tennessee","TN");
		state_to_abbr.put("Texas","TX");
		state_to_abbr.put("Utah","UT");
		state_to_abbr.put("Vermont","VT");
		state_to_abbr.put("Virgin Islands","VI");
		state_to_abbr.put("Virginia","VA");
		state_to_abbr.put("Washington","WA");
		state_to_abbr.put("West Virginia","WV");
		state_to_abbr.put("Wisconsin","WI");
		state_to_abbr.put("Wyoming","WY");
		state_to_abbr.put("Yukon Territory","YT");

	}

	
	public static void main(String[] args) {
		int state = 55;
		System.out.println(census_pop_url(state,2010));
		System.out.println(census_centroid_url(state,2010));
	}

	/*
	public static void main(String[] args) {
		downloadState(1,2010,2012);
	}*/
	public static String getBasePath() {
		File f = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();
		String start_path = f.getAbsolutePath();
		if( !start_path.substring(start_path.length()-1).equals(File.separator)) {
			start_path += File.separator;
		}
		start_path += "autoredistrict_data"+File.separator;
		if( !start_path.substring(start_path.length()-1).equals(File.separator)) {
			start_path += File.separator;
		}
		
		return start_path;
	}
	public static String getStartPath() {
		if( istate < 0) return getBasePath();
		return getBasePath()+states[istate]+File.separator+cyear+File.separator;
	}
	public static boolean downloadData() {
		istate = -1;
		//String state = (String)JOptionPane.showInputDialog(MainFrame.mainframe, "Select the state", "Select state.", 0, null, states, states[0]);
		String state = (String)JOptionPane.showInputDialog(null, "Select the state", "Select state.", JOptionPane.QUESTION_MESSAGE, null, states, states[0]);
		if( state == null)
			return false;
		for( int i = 0; i < states.length; i++) {
			if( state.equals(states[i])) {
				istate = i;
				break;
			}
		}
		if( istate <= 0) {
			System.out.println("state not found!");
			return false;
		}
		Date d = new Date();
		long DAY = 24L*60L*60L*1000L;
		long YEAR = DAY*365L;
		d.setTime(d.getTime()-YEAR-DAY*30L*7L);
		int y = d.getYear()+1900;
		int y10 = y - y % 10;
		int y4 = y - y % 4;
		String[] cyears = new String[]{""+y10,""+(y10-10)};
		String[] eyears = new String[]{""+y4,""+(y4-4),""+(y4-8),""+(y4-12),""+(y4-16),""+(y4-20)};

		String scyear = (String)JOptionPane.showInputDialog(MainFrame.mainframe, "Select the census year.", "Select year.", 0, null, cyears, cyears[0]);
		if( scyear == null)
			return false;
		cyear =  Integer.parseInt(scyear);
		
		String svyear = (String)JOptionPane.showInputDialog(MainFrame.mainframe, "Select the election year for voting tabulation districts.", "Select election year.", 0, null, eyears, eyears[0]);
		if( svyear == null)
			return false;
		vyear = Integer.parseInt(svyear);
		
		JOptionPane.showMessageDialog(MainFrame.mainframe, "It may take a few minutes to download and extact the data.\n(hit okay)");

        return downloadState(istate, cyear, vyear);
    }
	public static void initPaths() {
		System.out.println("initing paths "+istate+" "+cyear+" "+vyear);
		String path = getStartPath();
		File f = new File(path);
		if( !f.exists()) { f.mkdirs(); }

		census_tract_path = path;
		census_centroid_path = path+"block_centroids"+File.separator;
		census_pop_path = path+"block_pop"+File.separator;
		census_vtd_path = path+vyear+File.separator+"vtd"+File.separator;	
		
		census_centroid_file = new File(census_centroid_path+census_centroid_filename(istate,cyear));
		census_pop_file = new File(census_pop_path+census_pop_filename(istate,cyear));
		vtd_file = new File(census_vtd_path+census_vtd_filename(istate,cyear,vyear));
		vtd_dbf_file = new File(census_vtd_path+census_vtd_dbf_filename(istate,cyear,vyear));
		census_tract_file = new File(census_tract_path+census_tract_filename(istate,cyear));
		System.out.println("vtd_file "+vtd_file);
	}
	public static boolean downloadState(int _state, int _census_year, int _election_year) {
		istate = _state;
		cyear = _census_year;
		vyear = _election_year;
		Download.init();

		MainFrame.mainframe.ip.addHistory("DOWNLOAD "
				+Download.state_to_abbr.get(Download.state_to_fips.getBackward(Download.istate))
				+" "+Download.cyear+" "+Download.vyear);


		initPaths();
		
		
		File ftest1 = new File(census_vtd_path+"vtds.zip");
		File ftest2 = new File(census_pop_path+"block_pops.zip");
		File ftest3 = new File(census_centroid_path+"block_centroids.zip");
		File ftest4 = new File(census_tract_path+census_tract_filename(istate,cyear));
		
		download_census = true;
		download_vtd = true;
		
		if( ftest1.exists()) {
			download_vtd = prompt && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "VTD shapefiles already exist.  Re-download?");
		}
		if( ftest2.exists() && ftest3.exists()) {
			download_census = prompt && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Census files already exist.  Re-download?");
		}
		new Download().start();
		return true;
	}






	
	public void run() {

		if( MainFrame.dlg != null) { MainFrame.dlg.show(); }
		try {
			if( download_vtd) {
				System.out.println("Downloading vtd shapfile...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Downloading vtd shapfile..."); }
				download(census_vtd_url(istate,cyear,vyear),census_vtd_path,"vtds.zip");
			}
			if( download_census && census_merge_working) {
				if( !census_merge_old) {
					System.out.println("Downloading census population...");
					if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Downloading census population..."); }
					download(census_tract_url(istate,cyear),census_tract_path,census_tract_filename(istate,cyear));
				} else {
					System.out.println("Downloading census population...");
					if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Downloading census population..."); }
					download(census_pop_url(istate,cyear),census_pop_path,"block_pops.zip");
					System.out.println("Downloading census block centroids...");
					if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Downloading census block centroids..."); }
					download(census_centroid_url(istate,cyear),census_centroid_path,"block_centroids.zip");
				}
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
			if( MainFrame.dlg != null) { MainFrame.dlg.hide(); }
			return;
		}
		System.out.println("done downloading. extracting...");
		try {
			File f = new File(census_vtd_path+census_vtd_dbf_filename(istate,cyear,vyear));
					
					///Users/jimbrill/autoredistrict_data/Illinois/2010/2012/vtd/tl_2012_17_vtd10.shp
			if( download_vtd || !f.exists()) {
				System.out.println("Extracting vtd shapefile...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting vtd shapfile..."); }
				FileUtil.unzip(census_vtd_path+"vtds.zip", census_vtd_path);
			}
			if( census_merge_working && census_merge_old && download_census) {
				System.out.println("Extracting census population...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting census population..."); }
				FileUtil.unzip(census_pop_path+"block_pops.zip", census_pop_path);
				System.out.println("Extracting census centroids...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting census block centroids..."); }
				FileUtil.unzip(census_centroid_path+"block_centroids.zip", census_centroid_path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if( MainFrame.dlg != null) { MainFrame.dlg.hide(); }
			return;
		}
		System.out.println("done extracting.");
		initPaths();

		if( MainFrame.dlg != null) { MainFrame.dlg.hide(); }
		System.out.println("done extracting.");
		if( nextThread != null) {
			System.out.println("Starting next thread.");
			nextThread.start();
		} else {
			System.out.println("no next thread, hittng event occured.");
			MainFrame.mainframe.ip.eventOccured();
		}
    }
	public static String census_districts_url() {
		String fips = ""+istate;
		if( fips.length()< 2) {
			fips = "0"+fips;
		}
		//"http://www2.census.gov/geo/docs/maps-data/data/baf/BlockAssign_ST04_AZ.zip"
		return "http://www2.census.gov/geo/docs/maps-data/data/baf/BlockAssign_ST"+fips+"_"+state_to_abbr.get(states[istate])+".zip";
		//then the file in the zip is:
		/*
		 BlockAssign_ST01_AL_[type].txt
		 type = 
		 CD: congress
		 SLDL: state house
		 SLDH: state senate
		 
		 comma separated, with header row
		 */
	}
	public static String bdistricting_congress_url() {
		return "http://bdistricting.com/"+cyear+"/"+state_to_abbr.get(states[istate])+"_Congress/solution.zip";
	}
	public static String bdistricting_senate_url() {
		return "http://bdistricting.com/"+cyear+"/"+state_to_abbr.get(states[istate])+"_Senate/solution.zip";
	}
	public static String[] bdistricting_house_urls() {
		String[] names = new String[]{"House","Assembly","General","Legislature"};
		String[] urls = new String[names.length];
		for( int i = 0; i < names.length; i++) {
			urls[i] = "http://bdistricting.com/"+cyear+"/"+state_to_abbr.get(states[istate])+"_"+names[i]+"/solution.zip";
		}
		return urls;
	}
	public static String census_tract_url(int state, int year) {
		return "http://www2.census.gov/geo/docs/maps-data/data/gazetteer/"
			+"census_tracts_list_"+num(state)+".txt";
	}
	public static String census_centroid_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/pvs/tiger"+year+"st/"
				+num(state)+"_"+states[state].replaceAll(" ","_")+"/"+num(state)+"/"
				+"tl_"+year+"_"+num(state)+"_tabblock"+shortyear(year)+".zip";
	}
	public static String census_pop_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/tiger/TIGER"+year+"BLKPOPHU/"
				+"tabblock"+year+"_"+num(state)+"_pophu.zip";

	}
	public static String census_vtd_url(int state, int year, int elec_year) {
		return "http://www2.census.gov/geo/tiger/TIGER"+elec_year+"/VTD/"
				+"tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".zip";
	}
	public static String census_tract_filename(int state, int year) {
		return "census_tracts_list_"+num(state)+".txt";
	}

	
	public static String census_centroid_filename(int state, int year) {
		return "tl_"+year+"_"+num(state)+"_tabblock"+shortyear(year)+".dbf";
	}
	public static String census_pop_filename(int state, int year) {
		return "tabblock"+year+"_"+num(state)+"_pophu.dbf";

	}
	public static String census_vtd_filename(int state, int year, int elec_year) {
		return "tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".shp";
	}
	public static String census_vtd_dbf_filename(int state, int year, int elec_year) {
		return "tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".dbf";
	}
	public static String shortyear(int year) {
		String s = ""+year;
		return s.substring(2);
	}
	public static String num(int i) {
		String s = ""+i;
		if( s.length() < 2) { s = "0"+s; }
		return s;
	}
	/*
	public static int[] apportionments = new int[]{
			-1,
			7,
			1,
			-1,
			9,
			4,
			53,
			-1,
			7,
			5,
			1,
			-1,
			27,
			14,
			-1,
			2,
			2,
			18,
			9,
			4,
			4,
			6,
			6,
			2,
			8,
			9,
			14,
			8,
			4,
			8,
			1,
			3,
			4,
			2,
			12,
			3,
			27,
			13,
			1,
			16,
			5,
			5,
			18,
			-1,
			2,
			7,
			1,
			9,
			36,
			4,
			1,
			11,
			-1,
			10,
			3,
			8,
			1,
		};
		*/
	public static int[] apportionments = new int[]{
			-1,
			9,
			1,
			-1,
			13,
			5,
			72,
			-1,
			10,
			7,
			2,
			-1,
			38,
			19,
			-1,
			3,
			3,
			23,
			12,
			6,
			5,
			8,
			8,
			2,
			11,
			12,
			18,
			10,
			5,
			11,
			2,
			4,
			5,
			2,
			16,
			4,
			36,
			19,
			1,
			21,
			7,
			8,
			23,
			-1,
			2,
			9,
			2,
			12,
			51,
			6,
			1,
			15,
			-1,
			13,
			3,
			10,
			1,
	};
		
	public static String[] states = new String[]{
			"",
			"Alabama",
			"Alaska",
			"",
			"Arizona",
			"Arkansas",
			"California",
			"",
			"Colorado",
			"Connecticut",
			"Delaware",
			"",//District of Columbia",
			"Florida",
			"Georgia",
			"",
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
			"",
			"Rhode Island",
			"South Carolina",
			"South Dakota",
			"Tennessee",
			"Texas",
			"Utah",
			"Vermont",
			"Virginia",
			"",
			"Washington",
			"West Virginia",
			"Wisconsin",
			"Wyoming",
			/*
			"American Samoa",
			"Guam",
			"Commonwealth Of The Northern Marianas Islands",
			"Puerto Rico",
			"Virgin Islands Of The United States",
			*/

			};
	public static boolean prompt = true;
	public static boolean exit_when_done = false;
	
	public static boolean download(String url, String dest_path, String dest_name) throws Exception {
		try {
			if( url.indexOf("ftp:") == 0) {
				new File(dest_path).mkdirs();
				return FTPDownload.download(url, dest_path+dest_name);
			}
		System.out.println("downloading:");
		System.out.println("url :"+url);
		System.out.println("path:"+dest_path);
		System.out.println("file:"+dest_name);

		File f = new File(dest_path);
		if( !f.exists()) { f.mkdirs(); }

		URL website;
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try {
			website = new URL(url);
			rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(dest_path+dest_name);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
			fos.close();
			rbc.close();
		} catch (Exception ex) {
			try {
				System.out.println("ex on download1, retrying: "+ex);
				System.out.println(url);
				ex.printStackTrace();
				rbc.close();
				fos.close();				
			} catch (Exception ex0) { }
			try {
				website = new URL(url);
				rbc = Channels.newChannel(website.openStream());
				fos = new FileOutputStream(dest_path+dest_name);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.flush();
				fos.close();
				rbc.close();
			} catch (Exception ex2) {
				ex2.printStackTrace();
				System.out.println("ex on download2: failed to open site "+ex);
				System.out.println(url);
				return false;
			}
		}
		} catch (Exception ex) {
			System.out.println("ex on download: "+ex);
			ex.printStackTrace();
		}

		return true;
	}
	
    public static void delete() {
		Applet.deleteRecursive(new File(getStartPath()));
	}
	public static void clean() {
		new File(getStartPath()+File.separator+"blocks.txt").delete();
		Applet.deleteRecursive(new File(getStartPath()+File.separator+"CD113"));
		Applet.deleteRecursive(new File(getStartPath()+File.separator+"block_centroids"));
		Applet.deleteRecursive(new File(getStartPath()+File.separator+"block_pop"));
		Applet.deleteRecursive(new File(getStartPath()+File.separator+"demographics"));
		Applet.deleteRecursive(new File(getStartPath()+File.separator+vyear+File.separator+"vtd"+File.separator+"vtds.zip"));
	}
	public static boolean checkForDoneFile() {
		String path = getStartPath()+vyear+File.separator+"done.txt";
		System.out.println("checking for file "+path);
		File f =  new File(path);
		System.out.println("status: "+f.exists());
		return f.exists();
	}
	public static void makeDoneFile() {
		File f = new File(getStartPath()+vyear+File.separator+"done.txt");
		try {
			FileOutputStream fos = new FileOutputStream(f);
			fos.write("done".getBytes());
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void downloadAndExtractCentroids() {
		downloadAndExtract(census_centroid_url(istate,cyear),census_centroid_path);
	}
	
	public static void downloadAndExtract(String source_url, String dest_folder) {
		String dest_folder2 = dest_folder;
		try {
			if( !dest_folder.substring(dest_folder.length()-1).equals(File.separator)) {
				dest_folder2 += File.separator;
			}
			if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Downloading "+source_url+"..."); }
			download(source_url,dest_folder2,"downloaded.zip");
			if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting "+source_url+"..."); }
			FileUtil.unzip(dest_folder2+"downloaded.zip", dest_folder);
			System.out.println("done extracting.");		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
