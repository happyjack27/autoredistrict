package pmc_tools.visualize;

import jsonMap.ReflectJsonMap;

public class Config extends ReflectJsonMap {

	public static String base_path = "C:\\Users\\kbaas\\Documents\\autoredistrict_data\\mggg autoredistrict\\";
	public static String proposal_start_path = base_path+"assignment_files\\";
	public static String[] district_folders = new String[]{"state_assembly","state_senate","us_congress"};
	
	
	//public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\us_congress\\CON2.csv";
	public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\state_assembly\\ASM2.csv";
	//public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\state_senate\\SEN2.csv";
	//public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\us_congress\\assignment-db0a99b7.csv";
	public static String assignment_file_geoid_column = "id-wisconsin-wisconsin_wards-8-CongressionalDistricts";
	public static String assignment_file_assignment_column = "assignment";
	public static String vtd_file_path = base_path+"vtds\\2010\\WI_ltsb_corrected_final.dbf";
	public static String vtd_file_geoid_column = "GEOID10";
	/*
	public static String[][] vtd_file_election_columns = new String[][]{
		new String[]{ "IUSHDEM12","IUSHDEM14","IUSHDEM16"}
		,new String[]{"IUSHREP12","IUSHREP14","IUSHREP16"}
	};
	*/
	public static String[][] vtd_file_election_columns = new String[][]{
		new String[]{ "IWSADEM12","IWSADEM14","IWSADEM16"}
		,new String[]{"IWSAREP12","IWSAREP14","IWSAREP16"}
	};
	/*
	public static String[][] vtd_file_election_columns = new String[][]{
		new String[]{ "IWSSDEM12","IWSSDEM14","IWSSDEM16"}
		,new String[]{"IWSSREP12","IWSSREP14","IWSSREP16"}
	};
	*/
	
	/*
	public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\state_assembly\\assignment-1144f23f.csv";
	public static String assignment_file_geoid_column = "id-wisco2019acs-wisco2019acs_wards-99-StateAssemblyDistricts";
	public static String assignment_file_assignment_column = "assignment";
	public static String vtd_file_path = base_path+"vtds\\2010\\WI_ltsb_corrected_final.dbf";
	public static String vtd_file_geoid_column = "GEOID10";
	public static String[][] vtd_file_election_columns = new String[][]{
		new String[]{ "IWSADEM12","IWSADEM14","IWSADEM16"}
		,new String[]{"IWSAREP12","IWSAREP14","IWSAREP16"}
	};
	*/
	
	public static String vtd_file_path_2010 = base_path+"vtds\\2010\\WI_ltsb_corrected_final.dbf";
	public static String vtd_file_path_2020 = base_path+"vtds\\2020\\WI.dbf";
	public static String[] vtd_file_geoid_columns = new String[]{"GEOID10","Code-2"};
	public static String[][] vtd_file_election_columns_asm = new String[][]{
		new String[]{ "IWSADEM12","IWSADEM14","IWSADEM16"}
		,new String[]{"IWSAREP12","IWSAREP14","IWSAREP16"}
	};
	public static String[][] vtd_file_election_columns_sen = new String[][]{
		new String[]{ "IWSSDEM12","IWSSDEM14","IWSSDEM16"}
		,new String[]{"IWSSREP12","IWSSREP14","IWSSREP16"}
	};
	public static String[][] vtd_file_election_columns_ush = new String[][]{
		new String[]{ "IUSHDEM12","IUSHDEM14","IUSHDEM16"}
		,new String[]{"IUSHREP12","IUSHREP14","IUSHREP16"}
	};

}
