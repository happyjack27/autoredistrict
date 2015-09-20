package ui;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.zip.*;

import javax.swing.*;

public class Download {
	public static void main(String[] args) {
		downloadState(1,2010,2012);
	}
	public static void downloadState(int state, int census_year, int election_year) {
		downloadState(state, census_year, election_year, null); 
	}
	public static void downloadState(int state, int census_year, int election_year, String start_path) {
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
		
		File ftest1 = new File(census_vtd_path+"vtds.zip");
		File ftest2 = new File(census_centroid_path+"block_centroids.zip");
		if( ftest1.exists() && ftest2.exists()) {
			if( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, "Files already exist.  Re-download?")) {
				return;
			}
		}
		
		download(census_vtd_url(state,census_year,election_year),census_vtd_path,"vtds.zip");
		download(census_pop_url(state,census_year),census_pop_path,"block_pops.zip");
		download(census_centroid_url(state,census_year),census_centroid_path,"block_centroids.zip");
		System.out.println("done downloading. extracting...");
		try {
			unzip(census_vtd_path+"vtds.zip", census_vtd_path);
			unzip(census_pop_path+"block_pops.zip", census_pop_path);
			unzip(census_centroid_path+"block_centroids.zip", census_centroid_path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done extracting.");
	}
	public static String census_centroid_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/pvs/tiger"+year+"st/"
				+num(state)+"_"+states[state]+"/"+num(state)+"/"
				+"tl_"+year+"_"+num(state)+"_tabblock"+shortyear(year)+".zip";
	}
	public static String census_pop_url(int state, int year) {
		return "ftp://ftp2.census.gov/geo/tiger/TIGER"+year+"BLKPOPHU/"
				+"tabblock2010_"+num(state)+"_pophu.zip";

	}
	public static String census_vtd_url(int state, int year, int elec_year) {
		return "http://www2.census.gov/geo/tiger/TIGER"+elec_year+"/VTD/"
				+"tl_"+elec_year+"_"+num(state)+"_vtd"+shortyear(year)+".zip";
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
	
	public static boolean download(String url, String dest_path, String dest_name) {
		System.out.println("downloading:");
		System.out.println("url :"+url);
		System.out.println("path:"+dest_path);
		System.out.println("file:"+dest_name);
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
			return false;
		}
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
