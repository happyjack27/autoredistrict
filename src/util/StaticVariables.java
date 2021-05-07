package util;

public class StaticVariables {

	/*
		 * 
		 * potential redos (descr rep):
	California
	Massacthuses
	Michigan
	New Jersey
	New York
	Ohio
	Pennsylvania
	Virginia
	Wisconsin
	
		 
		 */
		static int apportionment_threshold = 15;
	static boolean only_redos = false;
	public static String[] redo_states = new String[]{
		"Oklahoma",
		//"Kentucky",
		//"Rhode Island",
		//"Pennsylvania",
		//"Tennessee",
		//"Alabama",
		//"New York",
		//"Oklahoma",
		//"Virginia",
		//"Texas",
		//"New York",
		//"Washington",
		//"Wisconsin",
		//"Michigan",
		//"Ohio",
		//"South Carolina",
		/*
		"Illinois",
		"California",
		"Florida",
		"Texas",
		"Massachusetts",
		"Virginia",
		"New Jersey",
		"New York",
		"Ohio",*/
	};
	public static String[] bad_states_round1 = new String[]{
		"Illinois",
		"Louisiana",
		"Maine",
		"Maryland",
		"Massachusetts",
		"Minnesota",//Minnesota
		"New Jersey",
		"Ohio",
		"Oklahoma",
		"Oregon",
		"Pennsylvania",
		"South Dakota",
		"Texas",
		"Vermont",
		"Virginia",
	};
	public static String[] bad_states = new String[]{
		"Massachusetts",
		"Ohio",
		"Texas",
		"Virginia",
		"Pennsylvania",
		"Oregon"
	};
	static final String path = "/Users/jimbrill/autoredistrict_data/county_stats";
	//"C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\all_state_elections_and_demo_from_google_drive";
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

}
