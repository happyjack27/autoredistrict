package pmc_tools.visualize;

import new_metrics.BetaStuff;
import new_metrics.FrameDrawDistribution;
import new_metrics.Metrics;
import pmc_tools.simpleCrossAggregate.District;
import util.DataAndHeader;
import util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

public class ReadCsvAndGatherMetrics {
	
	
	//public static String vtd_file_path = base_path+"vtds\\2020\\WI.dbf";
	//public static String vtd_file_geoid_column = "Code-2";
	
	DataAndHeader vtds;
	Vector<String[]> assignments;
	String[] assignment_headers;
	HashMap<String,District> vtd_districts;
	HashMap<String,District> election_districts;
	int iassignment_file_geoid_column = 0;
	int iassignment_file_assignment_column = 1;
	Metrics metrics;
	Config config = new Config();
	String explanation = "";
	int num_trials = 100*1000;
	
	public void iterateThroughFolders() {
		File f = new File(Config.base_path+"assignment_files");
		File[] ff2 = f.listFiles();
		for( File f2 : ff2) {
			File[] ff3 = f2.listFiles();
			if( ff3 == null) {
				continue;
			}
			for( File f3 : ff3) {
				String folder = f3.getAbsolutePath();
				System.out.println(folder);
				String configFile = folder+"\\Config.json";
				config.fromJson(FileUtil.readText(new File(configFile)));
				File[] ff4 = f3.listFiles();
				//System.exit(0);

				for( File f4 : ff4) {
					if( f4.isDirectory() || f4.getName().contains(".json") || !f4.getName().contains(".csv")) {
						continue;
					}
					System.out.println(f4.getAbsolutePath());
					Config.assignment_file_path = f4.getAbsolutePath();
					process();
				}				
			}
		}
	}
	
	public static void main(String[] ss) {
		ReadCsvAndGatherMetrics rcgm = new ReadCsvAndGatherMetrics();
		rcgm.explanation = FileUtil.readText(new File(Config.base_path+"assignment_files\\Explanation.txt"));
		//Metrics.show = true;
		//rcgm.doEvil();
		//rcgm.process();
		rcgm.iterateThroughFolders();
	}
	public void doEvil() {
		//wi_maxrep_dem,wi_maxrep_rep,
		String type = "AutoRedistrict_Fair";
		Metrics m = new Metrics(BetaStuff.wi_fair_dem,BetaStuff.wi_fair_rep,8);
		Config.assignment_file_path = Config.base_path+"assignment_files\\2011 wards\\us_congress\\"+type+".csv";
        this.displayInfoForMetric(m);
		
		
		type = "AutoRedistrict_Actual";
		m = new Metrics(BetaStuff.wi_actual_dem,BetaStuff.wi_actual_rep,8);
		Config.assignment_file_path = Config.base_path+"assignment_files\\2011 wards\\us_congress\\"+type+".csv";
        this.displayInfoForMetric(m);
		//System.exit(0);
		
		type = "AutoRedistrict_MaxRepGerrymander";
		m = new Metrics(BetaStuff.wi_maxrep_dem,BetaStuff.wi_maxrep_rep,8);
		Config.assignment_file_path = Config.base_path+"assignment_files\\2011 wards\\us_congress\\"+type+".csv";
        this.displayInfoForMetric(m);
		
		type = "AutoRedistrict_MaxDemGerrymander";
		m = new Metrics(BetaStuff.wi_maxdem_dem,BetaStuff.wi_maxdem_rep,8);
		Config.assignment_file_path = Config.base_path+"assignment_files\\2011 wards\\us_congress\\"+type+".csv";
        this.displayInfoForMetric(m);
		
	}
	public void process() {
		System.out.println("computing probability distributions...");
		//Config c = new Config();
		//FileUtil.writeText(new File(Config.base_path+"Config.json"), c.toString());
		//System.out.println(c.toString());
		//System.exit(0);
		Metrics m = this.getMetrics();
		this.displayInfoForMetric(m);
	}
	public Metrics getMetrics() {
		
		//read files
		vtds = util.FileUtil.readDBF(Config.vtd_file_path);
		assignments = util.FileUtil.readDelimited(new File(Config.assignment_file_path), ",","\n");
		assignment_headers = assignments.get(0);
		assignments.remove(0);
		for( int i = 0; i < assignment_headers.length; i++) {
			assignment_headers[i] = assignment_headers[i].replaceAll("\"","").trim();
			System.out.println(":"+assignment_headers[i]);
			if( assignment_headers[i].equals(Config.assignment_file_assignment_column)) {
				iassignment_file_assignment_column = i;				
			}
			if( assignment_headers[i].equals(Config.assignment_file_geoid_column)) {
				iassignment_file_geoid_column = i;
			}
		}
		
		//create vtd districts
		vtd_districts = new HashMap<String,District>();
		for( int i = 0; i < vtds.data.length; i++) {
			String[] ss  = vtds.data[i];
			vtd_districts.put(ss[vtds.nameToIndex.get(Config.vtd_file_geoid_column)],new District(i));
		}
		
		//create election districts
		election_districts = new HashMap<String,District>();
		int num_election_districts = 0;
		for( int i = 0; i < assignments.size(); i++) {
			String[] ss  = assignments.get(i);
			String id = ss[iassignment_file_assignment_column];
			if( !election_districts.containsKey(id)) {
				election_districts.put(id,new District(num_election_districts++));
			}
		}
		
		//accumulate election vote totals
		double[][][] elec_counts = new double[2][Config.vtd_file_election_columns[0].length][num_election_districts];
		for( int i = 0; i < 2; i++) {
			for(int j = 0; j < Config.vtd_file_election_columns[0].length; j++) {
				double[] vote_counts = elec_counts[i][j];
				int vtd_elec_col = vtds.nameToIndex.get(Config.vtd_file_election_columns[i][j]);
				for( int k = 0; k < assignments.size(); k++) {
					String[] ss = assignments.get(k);
					int vtd_index = vtd_districts.get(ss[iassignment_file_geoid_column]).index_in_file;
					int vote_index = election_districts.get(ss[iassignment_file_assignment_column]).index_in_file;
					vote_counts[vote_index] += Double.parseDouble(vtds.data[vtd_index][vtd_elec_col].replaceAll(",",""));
				}
			}
		}
		
		//instantiate and return
		metrics = new Metrics(elec_counts[0],elec_counts[1],num_election_districts);
		return metrics;
	}
	
	public void displayInfoForMetric(Metrics actual) {
		String file = Config.assignment_file_path;
		file = file.substring(0,file.indexOf("."));
		File f = new File(file);
		try { f.mkdir(); } catch (Exception ex) { }
		file += "\\";
		actual.output_folder = file;
		
		FileUtil.writeText(new File(actual.output_folder+"Explanation.txt"),explanation);
		
		// getGammasAsVectorString
		
		try {
			File f0 = new File(actual.output_folder+"probabilityModel.json");
			if( f0.exists()) {
				f0.delete();
			}
			//JsonMap pm = actual.getProbabilityModelJson();
			//FileUtil.writeText(f0, pm.toString());
		} catch (Exception ex) { ex.printStackTrace(); }
		{
			File f0 = new File(actual.output_folder+"probabiityModelGammas.csv");
			if( f0.exists()) {
				f0.delete();
			}
		}
		{
			File f0 = new File(actual.output_folder+"probabiityModelBetas.csv");
			if( f0.exists()) {
				f0.delete();
			}
		}

		try {
			Vector<String[]> vs = actual.getGammasAsVectorString();
			File f0 = new File(actual.output_folder+"probabilityModelGammas.csv");
			if( f0.exists()) {
				f0.delete();
			}
			FileUtil.writeDelimited(f0, ",", "\n", vs);
		} catch (Exception ex) { ex.printStackTrace(); }
		
		try {
			Vector<String[]> vs = actual.getBetasAsVectorString();
			File f0 = new File(actual.output_folder+"probabilityModelBetas.csv");
			if( f0.exists()) {
				f0.delete();
			}
			FileUtil.writeDelimited(f0, ",", "\n", vs);
		} catch (Exception ex) { ex.printStackTrace(); }
		
		
		try {
			Vector<Vector<String[]>> vvs = actual.getElections();
			for( int i = 0; i < vvs.size(); i++) {
				File f0 = new File(actual.output_folder+"election"+(i+1)+".csv");
				if( f0.exists()) {
					f0.delete();
				}
				Vector<String[]> vs = vvs.get(i);
				FileUtil.writeDelimited(f0, ",", "\n", vs);
			}
		} catch (Exception ex) { ex.printStackTrace(); }
		
		
		
		if( false) return;
		
		System.out.println("simulating elections...");
		actual.createElectionSamples(num_trials);
		System.out.println("drawing...");
		
		
		try {
			actual.showBetas();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("1");
		try {
			actual.computeSeatProbs(false);
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("2");
		try {
			actual.showSeats();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("4");
		
		try {
			actual.computeAsymmetry(false);
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("5");
		try {
			//actual.showAsymmetry();
			actual.showHistogram();
			//actual.showPacking();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("6");
		
		// public static String assignment_file_path = base_path+"assignment_files\\2011 wards\\state_senate\\SEN2.csv";
		actual.showHeatMap();
		System.out.println("8");
		actual.showDisproportionHeatMap(200,200);
		System.out.println("9");
		
		Vector<Double> res = actual.computeDisproportionalityStats();
		System.out.println("10");
		FrameDrawDistribution fdd = new FrameDrawDistribution(res);
		System.out.println("11");
		actual.saveToFile(fdd.panel, actual.output_folder+"disproportion.png", 500, 500);
		System.out.println("13");
		if( Metrics.show) {
			fdd.show();
		}
		actual.showAsymmetry();
		try {
			Vector<String[]> vs = actual.point_stats;
			File f0 = new File(actual.output_folder+"pointStatistics.csv");
			if( f0.exists()) {
				f0.delete();
			}
			FileUtil.writeDelimited(f0, ",", "\n", vs);
		} catch (Exception ex) { ex.printStackTrace(); }
		
		//System.exit(0);
		
	}
}
