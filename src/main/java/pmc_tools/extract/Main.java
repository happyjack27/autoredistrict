package pmc_tools.extract;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import util.*;

public class Main {
	public static String base_path = "C:\\Users\\kbaas\\Documents\\autoredistrict_data\\mggg autoredistrict\\";
	
	public static String vtd_source = base_path+"vtds\\autoredistrict\\tl_2012_55_vtd10.dbf";
	public static String vtd_target = base_path+"vtds\\autoredistrict\\";
	//public static String vtd_source = base_path+"vtds\\2010\\WI_ltsb_corrected_final.dbf";
	//public static String vtd_target = base_path+"vtds\\2010\\";
	public static String geoid_col = "GEOID10";
	public static String[] cols_to_extract = new String[]{"CD_2000","CD_2010","CD_BD","CD_NOW","CD_SM"};

	public static void main(String[] ss) {
		DataAndHeader dh = FileUtil.readDBF(vtd_source);
		int igeoid_col = dh.nameToIndex.get(geoid_col);
		
		for( int i = 0; i < cols_to_extract.length; i++) {
			String col = cols_to_extract[i];
			int idist = dh.nameToIndex.get(col);
			Vector<String[]> v = new Vector<String[]>(); 
			v.add(new String[]{geoid_col,"assignment"});
			for( int j = 0; j < dh.data.length; j++) {
				String[] dd = dh.data[j];
				v.add(new String[]{dd[igeoid_col],dd[idist]});
			}
			File f = new File(vtd_target+col+".csv");
		    try {
				if (f.createNewFile()) {
			      System.out.println("File created: " + f.getName());
			    } else {
			      System.out.println("File already exists. " + f.getName());
			    }
				FileUtil.writeDelimited(f, ",", "\n", v);
				System.out.println("File written: " + f.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

}
