package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;


public class translator {
	
	
	static int[] translation_from = new int[]{
		0,
		1,
		2,
		3,
		4,
		6,
		7,
		8,
		9,
		14,
		16,
		15,
		18,
		10,
		19,
		17,
		22,
		20,
		27,
		24,
		28,
		25,
		29,
		30,
		31,
		11,
		13,
		12,
		21,
		5,
		23,
		26,
		33,
		32,
		35,
		34,
		37,
		36,
		38,
		44,
		50,
		39,
		43,
		45,
		46,
		51,
		53,
		49,
		58,
		54,
		59,
		62,
		66,
		63,
		64,
		70,
		72,
		48,
		57,
		55,
		65,
		52,
		60,
		68,
		69,
		56,
		71,
		41,
		42,
		78,
		80,
		77,
		79,
		40,
		47,
		61,
		67,
		73,
		74,
		75,
		76,
	};
	static int[] translation_lookup = new int[]{};
	
	public static void make_translate() {
		translation_lookup = new int[translation_from.length];
		for( int i = 0; i < translation_lookup.length; i++) {
			translation_lookup[i] = 0;
		}
		
		int n = 0;
		for( int i = 1; i < translation_lookup.length; i++) {
			if( i % 2 == 1) {
				n++;
			}
			translation_lookup[translation_from[i]] = n;
		}
	}
	
	public static boolean fix_geoid = true;
	//GEOID10
	public static void main(String[] args) {
		make_translate();
		
		String delimiter = "\t";
		String infile = "C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\vtd_data.txt";
		String outfile = "C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\vtd_data_out.txt";
		
		Vector<String[]> records = readDelimited(new File(infile),delimiter,"\n");
		
		int source_col = 0;
		int geoid_col = 0;
		String[] hss = records.get(0);
		for( int i = 0; i < hss.length; i++) {
			if( hss[i].equals("SLDL_2010")) {
				source_col = i;
				break;
			}
		}
		for( int i = 0; i < hss.length; i++) {
			if( hss[i].equals("GEOID10")) {
				geoid_col = i;
				break;
			}
		}
		String[] hss2 = new String[hss.length+5];
		for( int i = 0; i < hss.length; i++) {
			hss2[i] = hss[i].trim();
		}
		
		hss2[hss.length+0] = "SLDL10_1";
		hss2[hss.length+1] = "SLDL10_2";
		hss2[hss.length+2] = "SLDL10_4";
		hss2[hss.length+3] = "SLDL10_8";
		hss2[hss.length+4] = "SLDL10_16";
		
		records.set(0,hss2);
		
		for( int j = 1; j < records.size(); j++) {
			String[] ss = records.get(j);
			
			String[] ss2 = new String[ss.length+5];
			for( int i = 0; i < ss.length; i++) {
				ss2[i] = ss[i].trim();
			}
			int n1 = Integer.parseInt(ss[source_col]);
			int n2 = translation_lookup[n1];
			int n4 = (n2 + n2 % 2)/2;
			int n8 = (n4 + n4 % 2)/2;
			int n16 = (n8 + n8 % 2)/2;
			
			ss2[ss.length+0] = ""+n1;
			ss2[ss.length+1] = ""+n2;
			ss2[ss.length+2] = ""+n4;
			ss2[ss.length+3] = ""+n8;
			ss2[ss.length+4] = ""+n16;
			
			if( fix_geoid) {
				ss2[geoid_col] = "0"+ss2[geoid_col];
			}
			records.set(j, ss2);
			
		}
		
		
		
		writeDelimited(new File(outfile),delimiter,"\n",records);
		
	}
	
	
	public static Vector<String[]> readDelimited(File f, String cell, String line) {
		StringBuffer sb = new StringBuffer(); 
		Vector<String[]> v = new Vector<String[]>();
		try {
			FileInputStream fis = new FileInputStream(f);
			while( fis.available() > 0) {
				byte[] bb = new byte[fis.available()];
				fis.read(bb);
				sb.append( new String(bb));
				Thread.sleep(10);
			}
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String s = sb.toString();
		String[] sss = s.split(line);
		for( int i = 0; i < sss.length; i++) {
			String[] ss = sss[i].split(cell);
			v.add(ss);
		}
		
		return v;
	}
	
	public static void writeDelimited(File f, String cell, String line,  Vector<String[]> v) {
		StringBuffer sb = new StringBuffer(); 
		for( int i = 0; i < v.size(); i++) {
			String[] ss = v.get(i);
			for( int j = 0; j < ss.length; j++) {
				if( j > 0) {
					sb.append(cell);
				}
				sb.append(ss[j]);
			}
			sb.append(line);
		}
		
		try {
			FileOutputStream fis = new FileOutputStream(f);
			fis.write(sb.toString().getBytes());
			fis.flush();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}