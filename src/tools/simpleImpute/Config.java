package tools.simpleImpute;

public class Config {

	public static String path = "C:\\Users\\kbaas\\Documents\\autoredistrict_data\\mggg autoredistrict\\vtds\\2010\\";
	public static String filename = "WI_ltsb_corrected_final.dbf";
	public static String district_column = "ASM"; //asm con
	public static String[] vote_columns = new String[]{"WSADEM12","WSAREP12"};
	public static String[] target_columns = new String[]{"IWSADEM12","IWSAREP12"};
	public static String[] impute_columns = new String[]{"PREDEM12","PREREP12"};

}
