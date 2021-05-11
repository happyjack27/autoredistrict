package util;

import java.util.Vector;

import solutions.Settings;

public class StaticFunctions {

	public static String[][] vectorToArray(Vector<String[]> v) {
		String[][] sss = new String[v.size()][];
		for( int i = 0; i < sss.length; i++) {
			sss[i] = v.get(i);
		}
		return sss;
	}

	//missing: alaska and lousianna!
	
	public static int LevenshteinDistance(String a, String b) {
	    int [] costs = new int [b.length() + 1];
	    for (int j = 0; j < costs.length; j++)
	        costs[j] = j;
	    for (int i = 1; i <= a.length(); i++) {
	        costs[0] = i;
	        int nw = i - 1;
	        for (int j = 1; j <= b.length(); j++) {
	            int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
	            nw = costs[j];
	            costs[j] = cj;
	        }
	    }
	    return costs[b.length()];
	}


}
