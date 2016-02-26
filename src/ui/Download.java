package ui;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

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
	public static File census_pop_file = null;
	public static File census_centroid_file = null;
	public static File census_tract_file = null;
	public static HashMap<String, String> state_to_abbr = null;
	
	public static void init() {
		if( state_to_abbr != null) {
			return;
		}
		state_to_abbr = new HashMap<String, String>();
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
		System.out.println(census_pop_url(51,2010));
		System.out.println(census_centroid_url(51,2010));
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
		int y = new Date().getYear()+1900;
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

		if( !downloadState( istate,cyear,vyear)) {
			return false;
		} 
		return true;
	}
	public static boolean downloadState(int _state, int _census_year, int _election_year) {
		istate = _state;
		cyear = _census_year;
		vyear = _election_year;

		String path = getStartPath();
		File f = new File(path);
		if( !f.exists()) { f.mkdirs(); }
		
		census_tract_path = path;
		census_centroid_path = path+"block_centroids"+File.separator;
		census_pop_path = path+"block_pop"+File.separator;
		census_vtd_path = path+vyear+File.separator+"vtd"+File.separator;
		
		File ftest1 = new File(census_vtd_path+"vtds.zip");
		File ftest2 = new File(census_pop_path+"block_pops.zip");
		File ftest3 = new File(census_centroid_path+"block_centroids.zip");
		File ftest4 = new File(census_tract_path+census_tract_filename(istate,cyear));
		
		download_census = true;
		download_vtd = true;
		
		if( ftest1.exists()) {
			download_vtd = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "VTD shapefiles already exist.  Re-download?");
		}
		if( ftest2.exists() && ftest3.exists()) {
			download_census = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Census files already exist.  Re-download?");
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
			if( download_vtd) {
				System.out.println("Extracting vtd shapfile...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting vtd shapfile..."); }
				unzip(census_vtd_path+"vtds.zip", census_vtd_path);
			}
			if( census_merge_working && census_merge_old && download_census) {
				System.out.println("Extracting census population...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting census population..."); }
				unzip(census_pop_path+"block_pops.zip", census_pop_path);
				System.out.println("Extracting census centroids...");
				if( MainFrame.dlbl != null) { MainFrame.dlbl.setText("Extracting census block centroids..."); }
				unzip(census_centroid_path+"block_centroids.zip", census_centroid_path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if( MainFrame.dlg != null) { MainFrame.dlg.hide(); }
			return;
		}
		System.out.println("done extracting.");
		census_centroid_file = new File(census_centroid_path+census_centroid_filename(istate,cyear));
		census_pop_file = new File(census_pop_path+census_pop_filename(istate,cyear));
		vtd_file = new File(census_vtd_path+census_vtd_filename(istate,cyear,vyear));
		census_tract_file = new File(census_tract_path+census_tract_filename(istate,cyear));

		if( MainFrame.dlg != null) { MainFrame.dlg.hide(); }
		System.out.println("done extracting.");
		if( nextThread != null) {
			System.out.println("Starting next thread.");
			nextThread.start();
		}
		return;
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
		return ""
			+"census_tracts_list_"+num(state)+".txt";
	}

	
	public static String census_centroid_filename(int state, int year) {
		return ""
				+"tl_"+year+"_"+num(state)+"_tabblock"+shortyear(year)+".dbf";
	}
	public static String census_pop_filename(int state, int year) {
		return ""
				+"tabblock"+year+"_"+num(state)+"_pophu.dbf";

	}
	public static String census_vtd_filename(int state, int year, int elec_year) {
		return ""
				+"tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".shp";
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
			"District of Columbia",
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
	
	public static boolean download(String url, String dest_path, String dest_name) throws Exception {
		System.out.println("downloading:");
		System.out.println("url :"+url);
		System.out.println("path:"+dest_path);
		System.out.println("file:"+dest_name);

		File f = new File(dest_path);
		if( !f.exists()) { f.mkdirs(); }

		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(dest_path+dest_name);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		return true;
	}
	
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
    	System.out.println("unzipping "+zipFilePath);
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
