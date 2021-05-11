package tools.simpleCrossAggregate;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Vector;

import util.DataAndHeader;
import util.FileUtil;
import dbf.DBField;

public class Main {
	public static DataAndHeader blocks;
	public static DataAndHeader vtds;
	public static HashMap<String,District> vtd_districts = new HashMap<String,District>();
	public static HashMap<String,Integer> block_indices = new HashMap<String,Integer>();
	public static HashMap<String,Integer> vtd_indices = new HashMap<String,Integer>();
	
	public static int iblock_pop = 0;
	public static int iblock_vtd = 0;
	public static int ivtd_vtd = 0;
	public static int[] ivtd_elec_columns;
	public static int[] iblock_elec_columns;
	public static int field_length = 12;
	public static int field_precision = 6;

	
	public static void main(String[] params) {
		transfer_blocks_to_vtd();
		//transfer_vtds_to_blocks();
		//String vtd_source = Config.base_path+"VTDS\\2020\\geoid_only\\WI.dbf";
		//extractFields(vtd_source,new String[]{"Code-2"});
	}
	private static void extractFields(String filename, String[] strings) {
		blocks = FileUtil.readDBF(filename);
		
		HashMap<String,Integer> hashFields = new HashMap<String,Integer>();
		for( int i = 0; i < blocks.full_header.length; i++) {
			hashFields.put(blocks.full_header[i].name, i);
		}
		int[] indexes = new int[strings.length];
		DBField[] new_fields = new DBField[strings.length];
		for( int i = 0; i < indexes.length; i++) {
			indexes[i] = hashFields.get(strings[i]);
			new_fields[i] = blocks.full_header[indexes[i]].clone();
		}
		blocks.full_header = new_fields;
		String[][] new_data = new String[blocks.data.length][];
		for( int i = 0; i < new_data.length; i++) {
			String[] out = new String[strings.length];
			String[] in = blocks.data[i];
			for( int j = 0; j < indexes.length; j++) {
				out[j] = in[indexes[j]];
			}
			new_data[i] = out;
		}
		blocks.data = new_data;
		
		FileUtil.writeDBF(blocks,filename);
		
	}
	public static void transfer_blocks_to_vtd() {
		System.out.println("read blocks");
		blocks = FileUtil.readDBF(Config.block_target);
		System.out.println("read vtds");
		vtds = FileUtil.readDBF(Config.vtd_target);

		System.out.println("init");
		for( int i = 0; i < blocks.full_header.length; i++) {
			System.out.println("B: "+blocks.full_header[i].name);
			block_indices.put(blocks.full_header[i].name, i);
		}
		for( int i = 0; i < vtds.full_header.length; i++) {
			System.out.println("V: "+vtds.full_header[i].name);
			vtd_indices.put(vtds.full_header[i].name, i);
		}
		
		ivtd_elec_columns = new int[Config.columns_to_transfer.length];
		iblock_elec_columns = new int[Config.columns_to_transfer.length];

		
		System.out.println("determineColumnIndices");		
		determineColumnIndicesToVtd();
		
		System.out.println("instantiate");
		for(int i = 0; i < vtds.data.length; i++) {
			String[] ss = vtds.data[i];
			vtd_districts.put(ss[ivtd_vtd],new District(i));
		}
		
		int found = 0;
		int notfound = 0;
		
		System.out.println("accumulate");
		for(int i = 0; i < blocks.data.length; i++) {
			String[] bss = blocks.data[i];
			//double block_pop = new Double(bss[iblock_pop].replaceAll(",",""));
			String vtdid = bss[iblock_vtd];
			if( vtdid == null || vtdid.length() == 0) {
				continue;
			}
			District d = vtd_districts.get(vtdid); 
			if( d == null) {
				System.out.println("not found: "+vtdid);
				notfound++;
			} else {
				found++;
				d.addToTotals(bss, iblock_elec_columns);
			}
		}
		System.out.println("found: "+found);
		System.out.println("notfound: "+notfound);

		//write to ss
		System.out.println("writing");
		DecimalFormat df = new DecimalFormat("###########0.000000");
		for(int i = 0; i < vtds.data.length; i++) {
			String[] ss = vtds.data[i];
			District d = vtd_districts.get(ss[ivtd_vtd]);
			for( int j = 0; j < ivtd_elec_columns.length; j++) {
				ss[ivtd_elec_columns[j]] = df.format(d.totals[j]);
			}
		}

		//write back
		System.out.println("saving");
		FileUtil.writeDBF(vtds,Config.vtd_target);
	}
	public static void transfer_vtds_to_blocks() {
		System.out.println("read blocks");
		blocks = FileUtil.readDBF(Config.block_pop);
		System.out.println("read vtds");
		vtds = FileUtil.readDBF(Config.vtd_source);

		System.out.println("init");
		for( int i = 0; i < blocks.full_header.length; i++) {
			block_indices.put(blocks.full_header[i].name, i);
		}
		for( int i = 0; i < vtds.full_header.length; i++) {
			vtd_indices.put(vtds.full_header[i].name, i);
		}
		
		ivtd_elec_columns = new int[Config.columns_to_transfer.length];
		iblock_elec_columns = new int[Config.columns_to_transfer.length];

		
		System.out.println("determineColumnIndices");
		determineColumnIndices();
		
		System.out.println("instantiate");
		for(int i = 0; i < vtds.data.length; i++) {
			String[] ss = vtds.data[i];
			vtd_districts.put(ss[ivtd_vtd],new District(i));
		}
		
		System.out.println("accumulate");
		for(int i = 0; i < blocks.data.length; i++) {
			String[] bss = blocks.data[i];
			double block_pop = new Double(bss[iblock_pop].replaceAll(",",""));
			String vtdid = bss[iblock_vtd];
			if( vtdid == null || vtdid.length() == 0) {
				continue;
			}
			District d = vtd_districts.get(vtdid); 
			if( d == null) {
				System.out.println("not found: "+vtdid);
			}
			d.total_population += block_pop;
		}		

		System.out.println("distribute");
		DecimalFormat df = new DecimalFormat("##########0.000000");
		for(int i = 0; i < blocks.data.length; i++) {
			String[] bss = blocks.data[i];
			double block_pop = new Double(bss[iblock_pop].replaceAll(",",""));
			String vtdid = bss[iblock_vtd];
			if( vtdid == null || vtdid.length() == 0) {
				for(int j = 0; j < ivtd_elec_columns.length; j++) {
					bss[iblock_elec_columns[j]] = "0";
				}
				continue;
			}
			District d = vtd_districts.get(vtdid);
			if( d == null) {
				System.out.println("not found: "+vtdid);
			}
			String[] vss = vtds.data[d.vtd_index];
			for(int j = 0; j < ivtd_elec_columns.length; j++) {
				double value = new Double(vss[ivtd_elec_columns[j]].replaceAll(",",""));
				bss[iblock_elec_columns[j]] = d.total_population <= 0 ? "0" : df.format(value*block_pop / d.total_population);
			}
		}		
		
		System.out.println("save");
		FileUtil.writeDBF(blocks,Config.block_target);
		System.out.println("done");
	}

	public static void determineColumnIndices() {
		
		iblock_pop = block_indices.get(Config.block_pop_column);
		iblock_vtd = block_indices.get(Config.block_vtd_column);
		ivtd_vtd = vtd_indices.get(Config.vtd_vtd_column);
		//ivtdcols;
		
		for( int i = 0; i < Config.columns_to_transfer.length; i++) {
			ivtd_elec_columns[i] = vtd_indices.get(Config.columns_to_transfer[i]);
		}
		
		int fields_to_append = 0;
		for( int i = 0; i < Config.columns_to_transfer.length; i++) {
			if( !block_indices.containsKey(Config.columns_to_transfer[i])) {
				DBField[] dbf = new DBField[blocks.full_header.length+1];
				for(int j = 0; j < blocks.full_header.length; j++) {
					dbf[j] = blocks.full_header[j];
				}
				dbf[dbf.length-1] = vtds.full_header[ivtd_elec_columns[i]].clone();//dbf[iblock_elec_columns[i]].clone();
				block_indices.put(Config.columns_to_transfer[i], dbf.length-1);
				blocks.full_header = dbf;
				fields_to_append++;
			}
			iblock_elec_columns[i] = block_indices.get(Config.columns_to_transfer[i]);
			DBField f = blocks.full_header[iblock_elec_columns[i]];
			f.decimalCount = field_precision;
			f.length = field_length;
		}
		if( fields_to_append > 0) {
			String[][] new_data = new String[blocks.data.length][];
			for( int i = 0; i < new_data.length; i++) {
				String[] ss = blocks.data[i];
				new_data[i] = new String[blocks.data[i].length + fields_to_append];
				
				for( int j = 0; j < ss.length; j++) {
					new_data[i][j] = ss[j];
				}				
			}
			blocks.data = new_data;
		}
	}
	public static void determineColumnIndicesToVtd() {
		
		iblock_pop = block_indices.get(Config.block_pop_column);
		iblock_vtd = block_indices.get(Config.block_vtd_column);
		ivtd_vtd = vtd_indices.get(Config.vtd_vtd_column);
		//ivtdcols;
		
		for( int i = 0; i < Config.columns_to_transfer.length; i++) {
			iblock_elec_columns[i] = block_indices.get(Config.columns_to_transfer[i]);
		}
		
		int fields_to_append = 0;
		for( int i = 0; i < Config.columns_to_transfer.length; i++) {
			if( !vtd_indices.containsKey(Config.columns_to_transfer[i])) {
				DBField[] dbf = new DBField[vtds.full_header.length+1];
				for(int j = 0; j < vtds.full_header.length; j++) {
					dbf[j] = vtds.full_header[j];
				}
				dbf[dbf.length-1] = blocks.full_header[iblock_elec_columns[i]].clone();//dbf[ivtd_elec_columns[i]].clone();
				vtd_indices.put(Config.columns_to_transfer[i], dbf.length-1);
				vtds.full_header = dbf;
				fields_to_append++;
			}
			ivtd_elec_columns[i] = vtd_indices.get(Config.columns_to_transfer[i]);
			DBField f = vtds.full_header[ivtd_elec_columns[i]];
			f.decimalCount = field_precision;
			f.length = field_length;
		}
		if( fields_to_append > 0) {
			String[][] new_data = new String[vtds.data.length][];
			for( int i = 0; i < new_data.length; i++) {
				String[] ss = vtds.data[i];
				new_data[i] = new String[vtds.data[i].length + fields_to_append];
				
				for( int j = 0; j < ss.length; j++) {
					new_data[i][j] = ss[j];
				}				
			}
			vtds.data = new_data;
		}
	}

}
