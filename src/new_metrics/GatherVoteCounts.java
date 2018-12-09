package new_metrics;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * 
 is beta method of moments variance?
 
 */

public class GatherVoteCounts {
	public static String state_filter = null;//"California";// null;//"Michigan";
	
	//skip alaska and hawaii?
	
	//public static String base_CD = "CD_BD";//missing file
	//public static String base_CD = "CD_SM"; //al
	//public static String base_CD = "CD_00"; //hawaii
	public static String base_CD = "CD_SM"; //hawaii
	
	public static String[] vote_columns1 = new String[] { "04", "08", "12" };
	public static String[] vote_columnsDEM = new String[] { "PRES04_DEM votes", "PRES08_DEM votes", "PRES12_DEM votes" };
	public static String[] vote_columnsREP = new String[] { "PRES04_REP votes", "PRES08_REP votes", "PRES12_REP votes" };
	//public static String[] vote_columnsREP = new String[] { "PRES04_DEM votes", "PRES08_DEM votes", "PRES12_DEM votes" };
	//public static String[] vote_columnsDEM = new String[] { "PRES04_REP votes", "PRES08_REP votes", "PRES12_REP votes" };

	public static String path_prefix = "C:\\Users\\Kevin Baas\\Documents\\";
	public static String path_postfix = ".txt";

	public static double[][] dem_elections;
	public static double[][] rep_elections;
	public static int[] seat_counts;

	public static void main(String[] args) {
		getVoteCounts();
	}

	public static void getVoteCounts() {
		try {
			// String surl =
			// "http://autoredistrict.org/all50/version3/CD_PRES/aggregate.php?col="+base_CD+vote_columns1[i]+"&file=stats_district";
			// String text = readURL(surl);
			dem_elections = new double[vote_columns1.length][];
			rep_elections = new double[vote_columns1.length][];
			for (int i = 0; i < vote_columns1.length; i++) {
				String text = readFile(path_prefix + base_CD + vote_columns1[i] + path_postfix);
				String[] rows0 = text.split("\n");
				String[] rows = applyStateFilter(rows0);
				System.out.println("rows: " + rows.length);
				seat_counts = new int[rows.length-1];
				double[] dem_votes = new double[rows.length - 1];
				double[] rep_votes = new double[rows.length - 1];
				int[] seats = new int[rows.length - 1];
				String[] header = rows[0].split("\t");
				int dem_header = 0;
				int rep_header = 0;
				int winner_header = 0;
				for (int h = 0; h < header.length; h++) {
					if (vote_columnsDEM[i].equals(header[h].trim())) {
						dem_header = h;
					}
					if (vote_columnsREP[i].equals(header[h].trim())) {
						rep_header = h;
					}
					if ("Winner".equals(header[h].trim())) {
						winner_header = h;
					}
				}
				String s = "";
				String last = "";
				for (int r = 1; r < rows.length; r++) {
					String[] row = rows[r].split("\t");
					if (!row[0].equals(s)) {
						System.out.println(s + ": " + last);
						if( s.equals("Alaska") && !last.equals("1")) {
							System.out.println("Alaska has wrong count");
							System.exit(0);
						}
						if( s.equals("Hawaii") && !last.equals("2")) {
							System.out.println("Hawaii has wrong count");
							System.exit(0);
						}
						s = row[0];
					}
					last = row[1];
					dem_votes[r - 1] = Double.parseDouble(row[dem_header].replaceAll(",", ""));
					rep_votes[r - 1] = Double.parseDouble(row[rep_header].replaceAll(",", ""));
					seats[r-1] = 1;
					String[] ws = row[winner_header].split(",");
					seats[r-1] = Integer.parseInt(ws[0])+Integer.parseInt(ws[1]);
					// System.out.println(""+r+": "+dem_votes[r-1]+" "+rep_votes[r-1]);
				}
				dem_elections[i] = dem_votes;
				rep_elections[i] = rep_votes;
				seat_counts = seats;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] applyStateFilter(String[] rows0) {
		if( state_filter == null) {
			return rows0;
		}
		Vector<String> vs = new Vector<String>();
		vs.add(rows0[0]);
		for( int i = 1; i < rows0.length; i++) {
			String row = rows0[i];
			if( row.startsWith(state_filter)) {
				vs.add(row);
			}
		}
		String [] ss = new String[vs.size()];
		for( int i = 0; i < ss.length; i++) {
			ss[i] = vs.get(i);
		}
		return ss;
	}

	public static String readURL(String surl) {
		try {
			URL url = new URL(surl);
			InputStream inputStream = url.openStream();
			StringBuilder textBuilder = new StringBuilder();
			Reader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())));
			int c = 0;
			while ((c = reader.read()) != -1) {
				textBuilder.append((char) c);
			}
			return textBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readFile(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);
			StringBuilder textBuilder = new StringBuilder();
			Reader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())));
			int c = 0;
			while ((c = reader.read()) != -1) {
				textBuilder.append((char) c);
			}
			return textBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}