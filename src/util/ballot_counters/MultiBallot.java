package util.ballot_counters;

import java.util.*;

import util.Pair;

public class MultiBallot {
	public static int num_votes = 3;
	public static int num_allocs = 6;
	public static double approval_threshold = 1.0;
	
	double weight = 1;

	double[] scores;
	double[] analog_allocation;
	int[] digital_allocation;
	int[] approval;
	
	int[] n_votes;
	int[] ranked_choice;
	
	public MultiBallot(double w, double[] dd) {
		super();
		weight = w;
		scores = dd;
		createAllBallotTypesFromScores();
	}
	public String toString() {
		String s = "";
		s += "scores: "+arrayToString(scores);
		s += "analog_allocation: "+arrayToString(analog_allocation);
		s += "digital_allocation: "+arrayToString(digital_allocation);
		s += "approval: "+arrayToString(approval);
		s += "n_votes: "+arrayToString(n_votes);
		s += "ranked_choice: "+arrayToString(ranked_choice);
		return s;
	}
	
	public String arrayToString(double[] dd) {
		String s = "";
		for( int i = 0; i < dd.length; i++) {
			s +="["+dd[i]+"] ";
		}
		s += "\n";
		return s;
	}
	
	public String arrayToString(int[] dd) {
		String s = "";
		for( int i = 0; i < dd.length; i++) {
			s +="["+dd[i]+"] ";
		}
		s += "\n";
		return s;
	}
	
	public void createAllBallotTypesFromScores() {

		double[] dd = new double[scores.length];
		//MultiBallot multi_ballot = new MultiBallot();
		
		double tot0 = 0;
		for( int j = 0; j < dd.length; j++) {
			tot0 += scores[j];
		}
		if( tot0 == 0) {
			tot0 = 1;
		}
		tot0 = 1.0/tot0;
		for( int j = 0; j < dd.length; j++) {
			dd[j] = tot0 * scores[j];
		}

		this.analog_allocation = dd;
		
		Vector<Pair<Double,Integer>> vp = new Vector<Pair<Double,Integer>>();
		for( int j = 0; j < dd.length; j++) {
			vp.add( new Pair<Double,Integer>(-dd[j],j));
		}
		Collections.sort(vp);

		int[] ranked = new int[dd.length];
		for( int j = 0; j < dd.length; j++) {
			ranked[j] = vp.get(j).b;
		}
		ranked_choice = ranked;
		
		int[] votes = new int[dd.length];
		for( int j = 0; j < num_votes; j++) {
			votes[vp.get(j).b] = 1;
		}
		n_votes = votes;
		
		int[] approvals = new int[dd.length];
		double threshold = (approval_threshold)/(double)dd.length;
		for( int j = 0; j < dd.length; j++) {
			approvals[j] = dd[j] > threshold ? 1 : 0;
		}
		approval = approvals;
		
		
		int[] da = new int[dd.length];
		double[] dd_res = new double[dd.length];
		int tot = 0;
		for( int j = 0; j < dd.length; j++) {
			da[j] = (int)Math.floor(dd[j]*(double)num_allocs);
			dd_res[j] = dd[j]*(double)num_allocs - (double)da[j];
			tot += da[j];
		}
		while( tot < num_allocs) {
			int m_i = -1;
			double m = -10;
			for( int j = 0; j < dd.length; j++) {
				if( dd_res[j] > m) {
					m = dd_res[j];
					m_i = j;
				}
			}
			dd_res[m_i]--;
			da[m_i]++;
			tot++;
		}
		digital_allocation = da;
	}
}
