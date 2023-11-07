package util.ballot_counters;

import java.util.Vector;


public class AllocationBallot {
	public static double total_avail_points;
	
	public double weight = 0;
	public double[] allocs = new double[]{};
	public double total_points_used = 0;
	
	public AllocationBallot(double w, double[] a) {
		weight = w;
		allocs = a.clone();
		calcTotalPointsUsed();
	}
	public void calcTotalPointsUsed() {
		total_points_used = 0;
		for( int i = 0; i < allocs.length; i++) {
			total_points_used += allocs[i];
		}
	}
	
	public AllocationBallot clone() {
		return new AllocationBallot(weight,allocs);
	}
	
	public double[] getWeightsIgnored(Vector<Integer> ignores) {
		double[] w = allocs.clone();
		double redistrib = 0;
		for( int i = 0; i < ignores.size(); i++) {
			int ig = ignores.get(i);
			redistrib += w[ig];
			w[ig] = 0;
		}
		double sum = 0;
		for( int i = 0; i < w.length; i++) {
			sum += w[i];
		}
		//fix divide by 0 bug
		if( sum == 0) {
			return w;
		}
		
		double mult = (sum+redistrib)/sum;
		for( int i = 0; i < w.length; i++) {
			w[i] *= mult*weight;
		}
		return w;
	}
	
	public void elected(int e, double used, double quota, Vector<Integer> ignores) {
		double[] weights_ignored = getWeightsIgnored(ignores);
		double votes_used = weights_ignored[e]*quota/used;
		
		calcTotalPointsUsed();
		if( votes_used == 0 || total_points_used == 0) {
			return;
		}
		
		double target_points = total_points_used - votes_used/weight;
		if( target_points < 0) {
			target_points = 0;
		}
		allocs[e] = 0;
		
		calcTotalPointsUsed();
		double mult = target_points/total_points_used;

		for( int i = 0; i < allocs.length; i++) {
			allocs[i] *= mult;
		}
		
		calcTotalPointsUsed();
	}
}
