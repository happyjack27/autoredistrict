package ui;

import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class Download {
	public void downloadState(int state, int census_year, int election_year) {
		downloadState(state, census_year, election_year, null); 
	}
	public void downloadState(int state, int census_year, int election_year, String start_path) {
		if( start_path == null) {
			File f = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();
			start_path = f.getAbsolutePath();
			if( !start_path.substring(start_path.length()-1).equals(File.separator)) {
				start_path += File.separator;
			}
			start_path += "autoredistrict_data"+File.separator;
		}
		if( !start_path.substring(start_path.length()-1).equals(File.separator)) {
			start_path += File.separator;
		}

		String path = start_path+states[state]+File.separator+census_year+File.separator;
		File f = new File(path);
		if( !f.exists()) { f.mkdirs(); }
		
		String census_centroid_path = path+"block_centroids"+File.separator;
		String census_pop_path = path+"block_pop"+File.separator;
		String census_vtd_path = path+election_year+File.separator+"vtd"+File.separator;
		download(census_centroid_url(state,census_year),census_centroid_path,"block_centroids.zip");
		download(census_pop_url(state,census_year),census_pop_path,"block_pops.zip");
		download(census_vtd_url(state,census_year,election_year),census_vtd_path,"vtds.zip");
	}
	public String census_centroid_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/pvs/tiger"+year+"st/"
				+num(state)+"_"+states[state]+"/"+num(state)+"/"
				+"tl_"+year+"_"+state+"_tabblock"+shortyear(year)+".zip";
	}
	public String census_pop_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/tiger/TIGER"+year+"BLKPOPHU/"
				+"tabblock2010_"+num(state)+"_pophu.zip";

	}
	public String census_vtd_url(int state, int year, int elec_year) {
		return "http://www2.census.gov/geo/tiger/TIGER"+elec_year+"/VTD/"
				+"tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".zip";
	}
	public String shortyear(int year) {
		String s = ""+year;
		return s.substring(2);
	}
	public String num(int i) {
		String s = ""+i;
		if( s.length() < 2) { s = "0"+s; }
		return s;
	}
	
	String[] states = new String[]{
			"",
			"Alabama",
			"Alaska",
			"American Samoa",
			"Arizona",
			"Arkansas",
			"California",
			"Colorado",
			"Connecticut",
			"Delaware",
			"District of Columbia",
			"Florida",
			"Georgia",
			"Guam",
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
			"American Samoa",
			"Guam",
			"Commonwealth Of The Northern Marianas Islands",
			"Puerto Rico",
			"Virgin Islands Of The United States",
			};
	
	public void download(String url, String dest_path, String dest_name) {
		try {
			File f = new File(dest_path);
			if( !f.exists()) { f.mkdirs(); }

			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(dest_path+dest_name);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
