package pmc_tools.simpleImpute;

import dbf.DBField;
import util.DataAndHeader;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Main {

	public static double pct_turnout_thresh = 0.1;
	public static double pct_vote_thresh = 0.1;
	public static DataAndHeader dbfData;
	public static HashMap<String,Integer> fieldIndices = new HashMap<String,Integer>();
	public static HashMap<String,District> districts = new HashMap<String,District>();

	public static int idistrict;
	public static int[] ivote;
	public static int[] iimpute;
	public static int[] itarget;
	public static int[] vote_totals;
	public static int field_length = 12;
	public static int field_precision = 6;
	
	public static void main(String[] ss) {
		dbfData = FileUtil.readDBF(Config.path+Config.filename);
		System.out.println("read "+dbfData.data.length+" lines");
		for( DBField d : dbfData.full_header) {
			System.out.println(d.name+" "+d.type+" "+d.length+" "+d.decimalCount);
		}
		//System.exit(0);
		for( int i = 0; i < dbfData.full_header.length; i++) {
			fieldIndices.put(dbfData.full_header[i].name, i);
		}
		idistrict = fieldIndices.get(Config.district_column);
		ivote = new int[Config.vote_columns.length];
		iimpute = new int[Config.vote_columns.length];
		itarget = new int[Config.vote_columns.length];
		vote_totals = new int[Config.vote_columns.length];
		
		System.out.println("determineColumnIndices");
		determineColumnIndices();
		System.out.println("aggregateVotes");
		aggregateVotes();
		System.out.println("imputeUncontested");
		imputeUncontested();
		System.out.println("writeDBF");
		String outname = Config.filename;
		File f = new File(Config.path+outname);
		if( !f.exists()) {
			try {
				f.createNewFile();
				//f = null;
			} catch (IOException e) {
			}
		}
		FileUtil.writeDBF(dbfData,Config.path+outname);
	}
	private static void aggregateVotes() {
		for( String[] ss: dbfData.data) {
			District d = districts.get(ss[idistrict]);
			if( d == null) {
				d = new District(ss[idistrict],Config.vote_columns.length);
				districts.put(ss[idistrict],d);
			}
			for( int i = 0; i < ivote.length; i++) {
				double vote = Double.parseDouble(ss[ivote[i]].replaceAll(",","").replaceAll("\\+",""));
				Main.vote_total += vote;
				vote_totals[i] += vote;
				d.vote_totals[i] += vote;
				d.vote_total += vote;
				d.impute_totals[i] += Double.parseDouble(ss[iimpute[i]].replaceAll(",","").replaceAll("\\+",""));
			}
		}
	}
	private static void imputeUncontested() {
		
		//determine uncontested
		double num_districts = districts.size();
		double vote_total_thresh = Main.vote_total*pct_turnout_thresh/num_districts;
		double[] vote_totals_contested = new double[Config.vote_columns.length];
		double[] impute_totals_contested = new double[Config.vote_columns.length];
		double[] impute_multiplier = new double[Config.vote_columns.length];
		for( String name : districts.keySet()) {
			District d = districts.get(name);
			d.contested = true;
			d.contested &= d.vote_total > vote_total_thresh;
			for( double v : d.vote_totals) {
				d.contested &= v / d.vote_total > pct_vote_thresh;
			}
			if( d.contested) {
				for( int i = 0; i < Config.vote_columns.length; i++) {
					vote_totals_contested[i] += d.vote_totals[i];
					impute_totals_contested[i] += d.impute_totals[i];
				}
			}
		}
		
		//compute vote multipliers
		for( int i = 0; i < Config.vote_columns.length; i++) {
			impute_multiplier[i] = vote_totals_contested[i] / impute_totals_contested[i];
		}		

		//impute uncontested
		for( String[] ss: dbfData.data) {
			boolean contested = districts.get(ss[idistrict]).contested;
			int[] iis = contested ? ivote : iimpute;
			for( int i = 0; i < Config.vote_columns.length; i++) {
				ss[itarget[i]] = ss[iis[i]];
			}
		}
	}
	public static void determineColumnIndices() {
		for( int i = 0; i < Config.vote_columns.length; i++) {
			ivote[i] = fieldIndices.get(Config.vote_columns[i]);
			iimpute[i] = fieldIndices.get(Config.impute_columns[i]);
		}
		int fields_to_append = 0;
		for( int i = 0; i < Config.vote_columns.length; i++) {
			if( !fieldIndices.containsKey(Config.target_columns[i])) {
				DBField[] dbf = new DBField[dbfData.full_header.length+1];
                System.arraycopy(dbfData.full_header, 0, dbf, 0, dbfData.full_header.length);
				dbf[dbf.length-1] = dbf[ivote[i]].clone();
				dbf[dbf.length-1].name = Config.target_columns[i];
				fieldIndices.put(Config.target_columns[i], dbf.length-1);
				dbfData.full_header = dbf;
				fields_to_append++;
			}
			itarget[i] = fieldIndices.get(Config.target_columns[i]);
			DBField f = dbfData.full_header[itarget[i]];
			f.decimalCount = field_precision;
			f.length = field_length;
		}
		if( fields_to_append > 0) {
			String[][] new_data = new String[dbfData.data.length][];
			for( int i = 0; i < new_data.length; i++) {
				String[] ss = dbfData.data[i];
				new_data[i] = new String[dbfData.data[i].length+fields_to_append];

                System.arraycopy(ss, 0, new_data[i], 0, ss.length);
			}
			dbfData.data = new_data;
		}
	}
	public static double vote_total = 0;
}
