package simpleImpute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Vector;

import util.DataAndHeader;
import util.FileUtil;
import dbf.DBField;

public class Main {

	public static String path = "C:\\Users\\kbaas\\Documents\\autoredistrict_data\\mggg autoredistrict\\2011\\";
	public static String filename = "WI_ltsb_corrected_final.dbf";
	public static String district_column = "ASM"; //asm con
	// missing USSDEM14
	public static String[] vote_columns = new String[]{"WSADEM12","WSAREP12"};
	public static String[] impute_columns = new String[]{"PREDEM12","PREREP12"};
	public static String[] target_columns = new String[]{"IWSADEM12","IWSAREP12"};
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
	public static double vote_total = 0;

	
	public static void main(String[] ss) {
		dbfData = FileUtil.readDBF(path+filename);
		System.out.println("read "+dbfData.data.length+" lines");
		for( int i = 0; i < dbfData.full_header.length; i++) {
			fieldIndices.put(dbfData.full_header[i].name, i);
		}
		idistrict = fieldIndices.get(district_column);
		ivote = new int[vote_columns.length];
		iimpute = new int[vote_columns.length];
		itarget = new int[vote_columns.length];
		vote_totals = new int[vote_columns.length];
		
		System.out.println("determineColumnIndices");
		determineColumnIndices();
		System.out.println("aggregateVotes");
		aggregateVotes();
		System.out.println("imputeUncontested");
		imputeUncontested();
		System.out.println("writeDBF");
		String outname = ""+filename;
		File f = new File(path+outname);
		if( !f.exists()) {
			try {
				f.createNewFile();
				//f = null;
			} catch (IOException e) {
			}
		}
		FileUtil.writeDBF(dbfData,path+outname);
	}
	private static void aggregateVotes() {
		for( String[] ss: dbfData.data) {
			District d = districts.get(ss[idistrict]);
			if( d == null) {
				d = new District(ss[idistrict],vote_columns.length);
				districts.put(ss[idistrict],d);
			}
			for( int i = 0; i < ivote.length; i++) {
				double vote = Double.parseDouble(ss[ivote[i]].replaceAll(",","").replaceAll("\\+",""));
				vote_total += vote;
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
		double vote_total_thresh = vote_total*pct_turnout_thresh/num_districts;
		double[] vote_totals_contested = new double[vote_columns.length];
		double[] impute_totals_contested = new double[vote_columns.length];
		double[] impute_multiplier = new double[vote_columns.length];
		for( String name : districts.keySet()) {
			District d = districts.get(name);
			d.contested = true;
			d.contested &= d.vote_total > vote_total_thresh;
			for( double v : d.vote_totals) {
				d.contested &= v / d.vote_total > pct_vote_thresh;
			}
			if( d.contested) {
				for( int i = 0; i < vote_columns.length; i++) {
					vote_totals_contested[i] += d.vote_totals[i];
					impute_totals_contested[i] += d.impute_totals[i];
				}
			}
		}
		
		//compute vote multipliers
		for( int i = 0; i < vote_columns.length; i++) {
			impute_multiplier[i] = vote_totals_contested[i] / impute_totals_contested[i];
		}		

		//impute uncontested
		for( String[] ss: dbfData.data) {
			boolean contested = districts.get(ss[idistrict]).contested;
			int[] iis = contested ? ivote : iimpute;
			for( int i = 0; i < vote_columns.length; i++) {
				ss[itarget[i]] = ss[iis[i]];
			}
		}
	}
	public static void determineColumnIndices() {
		for( int i = 0; i < vote_columns.length; i++) {
			ivote[i] = fieldIndices.get(vote_columns[i]);
			iimpute[i] = fieldIndices.get(impute_columns[i]);
		}
		int fields_to_append = 0;
		for( int i = 0; i < vote_columns.length; i++) {
			if( !fieldIndices.containsKey(target_columns[i])) {
				DBField[] dbf = new DBField[dbfData.full_header.length+1];
				for(int j = 0; j < dbfData.full_header.length; j++) {
					dbf[j] = dbfData.full_header[j];
				}
				dbf[dbf.length-1] = dbf[ivote[i]].clone();
				dbf[dbf.length-1].name = target_columns[i];
				fieldIndices.put(target_columns[i], dbf.length-1);
				dbfData.full_header = dbf;
				fields_to_append++;
			}
			itarget[i] = fieldIndices.get(target_columns[i]);
		}
		if( fields_to_append > 0) {
			String[][] new_data = new String[dbfData.data.length][];
			for( int i = 0; i < new_data.length; i++) {
				String[] ss = dbfData.data[i];
				new_data[i] = new String[dbfData.data[i].length+fields_to_append];
				
				for( int j = 0; j < ss.length; j++) {
					new_data[i][j] = ss[j];
				}				
			}
			dbfData.data = new_data;
		}
	}
}
