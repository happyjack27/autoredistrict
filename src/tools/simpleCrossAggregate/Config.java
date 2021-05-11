package tools.simpleCrossAggregate;

public class Config {
	public static String base_path = "C:\\Users\\kbaas\\Documents\\autoredistrict_data\\mggg autoredistrict\\";
	
	public static String vtd_source = base_path+"vtds\\2010\\WI_ltsb_corrected_final.dbf";
	public static String vtd_target = base_path+"vtds\\2020\\WI.dbf";
	//public static String block_pop = base_path+"blocks\\2010\\joined_simple.dbf";// C:\Users\kbaas\Documents\autoredistrict_data\mggg autoredistrict\blocks\2010\tabblock2010_55_pophu\

	
	public static String block_pop = base_path+"blocks\\2010\\blocks_w_join_only\\blocks_w_join_only.dbf";
	public static String block_target = base_path+"blocks\\2010\\blocks_w_join_only\\blocks_w_join_and_imputed.dbf";;
	//public static String block_pop = base_path+"blocks\\2010\\blocks_w_imputed_and_join\\blocks_w_imputed_and_join.dbf";
	//public static String block_target = base_path+"blocks\\2010\\blocks_w_imputed_and_join\\blocks_w_imputed_and_join.dbf";
//	public static String block_target = base_path+"blocks\\2010\\block_with_imputed.dbf";// C:\Users\kbaas\Documents\autoredistrict_data\mggg autoredistrict\blocks\2010\tabblock2010_55_pophu\
	public static String vtd_vtd_column = "Code-2";
	public static String block_vtd_column = "Code-2";
	//public static String vtd_vtd_column = "GEOID10";
	//public static String block_vtd_column = "GEOID10";
	public static String block_pop_column = "POP10";
	
	public static String[] columns_to_transfer = new String[]{
		"IWSADEM12"
		,"IWSAREP12"
		,"IWSADEM14"
		,"IWSAREP14"
		,"IWSADEM16"
		,"IWSAREP16"

		,"IWSSDEM12"
		,"IWSSREP12"
		,"IWSSDEM14"
		,"IWSSREP14"
		,"IWSSDEM16"
		,"IWSSREP16"

		,"IUSHDEM12"
		,"IUSHREP12"
		,"IUSHDEM14"
		,"IUSHREP14"
		,"IUSHDEM16"
		,"IUSHREP16"
	};

}
