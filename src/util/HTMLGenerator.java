package util;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;

public class HTMLGenerator {
	public static String DEM = "<b><font color=#4040C0>Democratic gerrymander</font></b>";
	public static String NEUTRAL = "<b><font color=#808080>Neutral</font></b>";
	public static String REP = "<b><font color=#C04040>Republican gerrymander</font></b>";
	
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
	static final String[] state_notes = new String[]{
		"Alabama","",
		"Alaska","",
		"Arizona","",
		"Arkansas","",
		"California","",
		"Colorado","",
		"Connecticut","",
		"Delaware","",
		"Florida","",
		"Georgia","",
		"Hawaii","",
		"Idaho","",
		"Illinois","",
		"Indiana","",
		"Iowa","",
		"Kansas","",
		"Kentucky","",
		"Louisiana","Poor quality vote data",
		"Maine","import bad",
		"Maryland","",
		"Massachusetts","",
		"Michigan","",
		"Minnesota","",
		"Mississippi","",
		"Missouri","",
		"Montana","",
		"Nebraska","",
		"Nevada","",
		"New Hampshire","",
		"New Jersey","",
		"New Mexico","",
		"New York","",
		"North Carolina","",
		"North Dakota","",
		"Ohio","",
		"Oklahoma","Poor quality vote data",
		"Oregon","",
		"Pennsylvania","",
		"Rhode Island","import bad",
		"South Carolina","",
		"South Dakota","",
		"Tennessee","",
		"Texas","Poor quality vote data",
		"Utah","",
		"Vermont","",
		"Virginia","",
		"Washington","",
		"West Virginia","",
		"Wisconsin","",
		"Wyoming","",
	};
	
	static final String[] states1 = new String[]{
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
	
	//TOTALS CUR: 
	//TOTALS BD:
	//TOTALS FV:
	static final String[] state_statuses = new String[]{
		"Alabama",REP,REP,NEUTRAL,
		"Alaska",NEUTRAL,NEUTRAL,NEUTRAL,
		"Arizona",NEUTRAL,NEUTRAL,NEUTRAL,
		"Arkansas",NEUTRAL,NEUTRAL,NEUTRAL,
		"California",NEUTRAL,NEUTRAL,NEUTRAL,
		"Colorado",NEUTRAL,NEUTRAL,NEUTRAL,
		"Connecticut",REP,NEUTRAL,NEUTRAL,
		"Delaware",NEUTRAL,NEUTRAL,NEUTRAL,
		"Florida",REP,NEUTRAL,NEUTRAL,
		"Georgia",REP,NEUTRAL,NEUTRAL,
		"Hawaii",NEUTRAL,NEUTRAL,NEUTRAL,
		"Idaho",NEUTRAL,NEUTRAL,NEUTRAL,
		"Illinois",REP,REP,NEUTRAL,
		"Indiana",REP,REP,NEUTRAL,
		"Iowa",NEUTRAL,NEUTRAL,NEUTRAL,
		"Kansas",NEUTRAL,NEUTRAL,NEUTRAL,
		"Kentucky",REP,NEUTRAL,NEUTRAL,
		"Louisiana",REP,REP,NEUTRAL,
		"Maine",NEUTRAL,NEUTRAL,NEUTRAL,
		"Maryland",NEUTRAL,REP,NEUTRAL,
		"Massachusetts",NEUTRAL,NEUTRAL,NEUTRAL,
		"Michigan",REP,REP,NEUTRAL,
		"Minnesota",NEUTRAL,NEUTRAL,NEUTRAL,
		"Mississippi",REP,NEUTRAL,NEUTRAL,
		"Missouri",REP,REP,NEUTRAL,
		"Montana",NEUTRAL,NEUTRAL,NEUTRAL,
		"Nebraska",DEM,DEM,NEUTRAL,
		"Nevada",NEUTRAL,NEUTRAL,NEUTRAL,
		"New Hampshire",NEUTRAL,NEUTRAL,NEUTRAL,
		"New Jersey",NEUTRAL,NEUTRAL,NEUTRAL,
		"New Mexico",NEUTRAL,NEUTRAL,NEUTRAL, //??
		"New York",REP,REP,NEUTRAL,
		"North Carolina",NEUTRAL,NEUTRAL,NEUTRAL,
		"North Dakota",NEUTRAL,NEUTRAL,NEUTRAL,
		"Ohio",REP,REP,NEUTRAL,
		"Oklahoma",REP,NEUTRAL,NEUTRAL,
		"Oregon",REP,REP,NEUTRAL,
		"Pennsylvania",REP,REP,NEUTRAL,
		"Rhode Island",NEUTRAL,NEUTRAL,NEUTRAL,
		"South Carolina",NEUTRAL,NEUTRAL,NEUTRAL,
		"South Dakota",NEUTRAL,NEUTRAL,NEUTRAL,
		"Tennessee",REP,REP,NEUTRAL,
		"Texas",NEUTRAL,NEUTRAL,NEUTRAL,//MISSING
		"Utah",NEUTRAL,REP,NEUTRAL,
		"Vermont",NEUTRAL,NEUTRAL,NEUTRAL,
		"Virginia",REP,NEUTRAL,NEUTRAL,
		"Washington",REP,REP,NEUTRAL,
		"West Virginia",NEUTRAL,NEUTRAL,NEUTRAL,
		"Wisconsin",REP,REP,NEUTRAL,
		"Wyoming",NEUTRAL,NEUTRAL,NEUTRAL,
	};
	
	public static String[] parse(String s0) {
		String[] ss = new String[10];
		try {
		String s = s0.split("By party")[1].split("</table>")[0];
		String[] rows = s.split("<tr>");
		for( int i = 2; i < 4; i++) {
			//System.out.print(" "+i+": ");
			String[] cols = rows[i].split("<td>");
			for( int j = 0; j < cols.length; j++) {
				cols[j] = cols[j].split("<")[0];
				//System.out.print(" "+j+": "+cols[j]);
				if( i == 2 && j == 2) {
					ss[0] = cols[j];
				}
				if( i == 2 && j == 3) {
					ss[1] = cols[j];
				}
				if( i == 3 && j == 2) {
					ss[2] = cols[j];
				}
				if( i == 3 && j == 3) {
					ss[3] = cols[j];
				}
			}
			//System.out.println();
			
		}
		
		s = s0.split("Summary")[1].split("</table>")[0];
		rows = s.split("<tr>");
		for( int i = 1; i < rows.length; i++) {
			//System.out.println(" "+i+": "+rows[i]);
			String[] cols = rows[i].split("<td>");
			if( cols.length < 3) {
				continue;
			}
			for( int j = 0; j < cols.length; j++) {
				cols[j] = cols[j].split("<")[0];
				//System.out.print(" "+j+":"+cols[j]);
			}
			//System.out.println();
			if( cols[2].contains("Competitiveness")) {
				ss[4] = cols[1];//.length() > 10 ? cols[1].substring(0,10) : cols[1];
			}
			if( cols[2].contains("asymmetry")) {
				ss[5] = cols[1].length() > 7 ? cols[1].substring(0,7) : cols[1];
			}
			if( cols[2].contains("Compactness")) {
				ss[6] = cols[1].length() > 7 ? cols[1].substring(0,7) : cols[1];
			}
			if( cols[2].contains("Racial")) {
				ss[7] = cols[1].length() > 7 ? cols[1].substring(0,7) : cols[1];
			}
		}
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
//Competitiveness 		
		
		
		return ss;
	}
	public static String urlToString(String str) {
		//System.out.println("urlToString: "+str);
        StringBuffer sb = new StringBuffer();
		try {
	        URL oracle = new URL(str);
	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(oracle.openStream()));
	
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	sb.append(inputLine);
	            //System.out.println(inputLine);
	        }
	        in.close();
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
        
        return sb.toString();
	}
	public static void comparison(String[] cols) {
		String html = "";
		int[][] counts = new int[][]{
				new int[]{0,0,0},
				new int[]{0,0,0},
				new int[]{0,0,0},
		};
		//for( int i = 0; i < 25; i++) {
		for( int i = 0; i < states.length; i++) {
			String state = states[i];
			html += "<tr>\n";
			html += "\t<td>"+state+"</td>\n";
			for( int j = 0; j < cols.length; j++) {
				String[] ss;
				try  {
					ss = parse(urlToString("http://autoredistrict.org/fairvote/"+state.replaceAll(" ","%20")+"/2010/"+cols[j]+"/stats.html"));
				} catch (Exception ex) {
					ss = new String[]{"0","0","0","0","0","0","0","0","0","0","0"};
				}
				/*
				 * competitiveness 4
				 * asymmetry 5
				 * compactness 6
				 * racial 7
				 */
				String status = state_statuses[i*4+(j+1)];
				if( status.equals(NEUTRAL)) {
					counts[j][1]++;
				}
				if( status.equals(REP)) {
					counts[j][0]++;
				}
				if( status.equals(DEM)) {
					counts[j][2]++;
				}
				html += "\t<td><center>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/stats.html'>stats</a><br/><br/>";
				//html += "<b><font color=#808080>Neutral</font></b><br/><br/>";
				html += status+"<br/><br/>";
				html += "<table>";
				html += "<tr><td>Over-votes:</td><td>"+ss[4]+"</td></tr>";
				html += "<tr><td>Partisan packing:</td><td>"+ss[5]+"</td></tr>";
				html += "<tr><td>Racial packing:</td><td>"+ss[7]+"</td></tr>";
				html += "<tr><td>Compactness:</td><td>"+ss[6]+"</td></tr>";
				html += "</table><br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/map_districts.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/map_districts_small.png' width=150></a><br/>Districts<br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/map_district_partisan_packing.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/map_district_partisan_packing_small.png' width=150></a><br/>Vote packing<br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/sorted_districts.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/sorted_districts.png' width=150></a><br/>Vote packing<br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/seats_votes.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/seats_votes.png' width=150></a><br/>Seats vs votes<br/><br/>";
				html += "\t</center></td>\n";
			}
			html += "\t<td><b>"+state_notes[i*2+1]+"</b></td>\n";
			html += "</tr>\n";
		}
		System.out.print(html);
		for( int i = 0; i < 3; i++) {
			System.out.println(""+counts[i][0]+" "+counts[i][1]+" "+counts[i][2]+" ");
		}
		
	}
	public static void main(String[] args) {
		String col = "CD_BD/";
		if( false) {
			comparison(new String[]{"CD_NOW","CD_BD","CD_FV"});
			System.exit(0);
		}
		
		Vector<String[]> v = new Vector<String[]>();
		double[] ii = new double[10];
		String html = "";
		for( int i = 0; i < states.length; i++) {
			String state = states[i];
			
			String[] ss;
			try  {
				ss = parse(urlToString("http://autoredistrict.org/fairvote/"+state.replaceAll(" ","%20")+"/2010/"+col+"stats.html"));
			} catch (Exception ex) {
				ss = new String[]{"0","0","0","0","0","0","0","0","0","0","0"};
				
			}
			html += "<tr>\n";
			html += "\t<td>"+state+"</td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"stats.html'>stats</a></td>\n";
			html += "\t<td><a href=\"ftp://autoredistrict.org/pub/shapefiles/"+state+"/2010/2012/vtd/\">shapefile</a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"map_districts.png'><img src='fairvote/"+state+"/2010/"+col+"map_districts_small.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"map_vtd_votes.png'><img src='fairvote/"+state+"/2010/"+col+"map_vtd_votes_small.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"map_vtd_demographics.png'><img src='fairvote/"+state+"/2010/"+col+"map_vtd_demographics_small.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"map_district_partisan_packing.png'><img src='fairvote/"+state+"/2010/"+col+"map_district_partisan_packing_small.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/"+col+"seats_votes.png'><img src='fairvote/"+state+"/2010/"+col+"seats_votes_small.png' width=100></a></td>\n";
			//html += "\t<td><img src='fairvote/"+state+"/2010/2012/vtd' width=100></td>\n";
			html += "\t<td>"+ss[0]+"</td>\n";
			html += "\t<td>"+ss[2]+"</td>\n";
			html += "\t<td>"+ss[1]+"</td>\n";
			html += "\t<td>"+ss[3]+"</td>\n";
			html += "</tr>\n";
			
			v.add(ss);
			for( int j = 0; j < ii.length; j++) {
				try {
					ii[j] += Double.parseDouble(ss[j].replaceAll(",",""));
				} catch (Exception ex) { }
			}
		}
		DecimalFormat df = new DecimalFormat("###,###,###,##0");
		html += "<tr>\n";
		html += "\t<td><B>TOTAL</B></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td></td>\n";
		html += "\t<td>"+df.format(ii[0])+"</td>\n";
		html += "\t<td>"+df.format(ii[2])+"</td>\n";
		html += "\t<td>"+df.format(ii[1])+"</td>\n";
		html += "\t<td>"+df.format(ii[3])+"</td>\n";
		html += "</tr>\n";
		System.out.print(html);
		for( int j = 0; j < ii.length; j++) {
			System.out.println(ii[j]);
		}

		//html();
		System.exit(0);
		//processVTD();
		//System.exit(0);
		/*
		for( int i = 0; i < states.length; i++) {
			try {
				String state = states[i];
				System.out.println("processing: "+state);
				process(state);
			} catch (Exception ex) {
				System.out.println("ex in main "+ex);
				ex.printStackTrace();
			}
		}
		*/
		System.out.println("done.");
	}

	
	/*
	public static void main(String[] args) {
		String col = "CD_FV/";
		Vector<String[]> v = new Vector<String[]>();
		int[] ii = new int[4];
		String html = "";
		for( int i = 0; i < states.length; i++) {
			String state = states[i];
			
			String[] ss = parse(urlToString("http://autoredistrict.org/fairvote/"+state.replaceAll(" ","%20")+"/2010/"+col+"stats.html"));
			html += "<tr>\n";
			html += "\t<td>"+state+"</td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/stats.html'>stats</a></td>\n";
			html += "\t<td><a href=\"ftp://autoredistrict.org/pub/shapefiles/"+state+"/2010/2012/vtd/\">shapefile</a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/map_districts.png'><img src='fairvote/"+state+"/2010/map_districts.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/map_vtd_votes.png'><img src='fairvote/"+state+"/2010/map_vtd_votes.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/map_vtd_demographics.png'><img src='fairvote/"+state+"/2010/map_vtd_demographics.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/map_district_partisan_packing.png'><img src='fairvote/"+state+"/2010/map_district_partisan_packing.png' width=100></a></td>\n";
			html += "\t<td><a href='fairvote/"+state+"/2010/seats_votes.png'><img src='fairvote/"+state+"/2010/seats_votes.png' width=100></a></td>\n";
			//html += "\t<td><img src='fairvote/"+state+"/2010/2012/vtd' width=100></td>\n";
			html += "\t<td>"+ss[0]+"</td>\n";
			html += "\t<td>"+ss[2]+"</td>\n";
			html += "\t<td>"+ss[1]+"</td>\n";
			html += "\t<td>"+ss[3]+"</td>\n";
			html += "</tr>\n";
			
			v.add(ss);
			for( int j = 0; j < ii.length; j++) {
				ii[j] += Integer.parseInt(ss[j].replaceAll(",",""));
			}
		}
		String total = "";
		total += "<tr>\n";
		total += "\t<td><B>TOTAL</B></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td></td>\n";
		total += "\t<td>"+ii[0]+"</td>\n";
		total += "\t<td>"+ii[2]+"</td>\n";
		total += "\t<td>"+ii[1]+"</td>\n";
		total += "\t<td>"+ii[3]+"</td>\n";
		total += "</tr>\n";
		System.out.print(total+html+total);
		System.exit(0);
	}
	*/


}
