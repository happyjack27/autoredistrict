package util;

import java.io.*;
import java.net.*;
import java.util.*;

public class HTMLGenerator {
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
	public static String[] parse(String s) {
		String[] ss = new String[4];
		s = s.split("By party")[1];
		String[] rows = s.split("<tr>");
		for( int i = 2; i < 4f; i++) {
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
		
		
		return ss;
	}
	public static String urlToString(String str) {
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
			ex.printStackTrace();
		}
        
        return sb.toString();
	}
	public static void comparison(String[] cols) {
		String html = "";
		//for( int i = 0; i < 25; i++) {
		for( int i = 25; i < states.length; i++) {
			String state = states[i];
			html += "<tr>\n";
			html += "\t<td>"+state+"</td>\n";
			for( int j = 0; j < cols.length; j++) {
				html += "\t<td><center>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/stats.html'>stats</a><br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/map_districts.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/map_districts_small.png' width=100></a><br/>Districts<br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/map_district_partisan_packing.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/map_district_partisan_packing_small.png' width=100></a><br/>Vote packing<br/><br/>";
				html += "<a href='fairvote/"+state+"/2010/"+cols[j]+"/seats_votes.png'><img src='fairvote/"+state+"/2010/"+cols[j]+"/seats_votes.png' width=100></a><br/>Seats vs votes<br/><br/>";
				html += "\t</center></td>\n";
			}
			html += "</tr>\n";
		}
		System.out.print(html);
	}
	
	public static void main(String[] args) {
		Vector<String[]> v = new Vector<String[]>();
		int[] ii = new int[4];
		String html = "";
		for( int i = 0; i < states.length; i++) {
			String state = states[i];
			
			String[] ss = parse(urlToString("http://autoredistrict.org/fairvote/"+state.replaceAll(" ","%20")+"/2010/stats.html"));
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


}
