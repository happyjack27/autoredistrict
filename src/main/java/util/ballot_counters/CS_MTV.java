package util.ballot_counters;

import java.util.*;

public abstract class CS_MTV implements iCountingSystem {
	public int[] getWinnersGeneric(Vector<Double> weights, Vector<double[]> dballots, int seats) {
		
		Vector<AllocationBallot> ballots = new Vector<AllocationBallot>();
		for( int j = 0; j < dballots.size(); j++) {
			double[] dd = dballots.get(j);
			double tot = 0;
			for( int i = 0; i < dd.length; i++) {
				tot += dd[i];
			}
			tot = ((double)seats)/tot;
			for( int i = 0; i < dd.length; i++) {
				dd[i] *= tot;
			}

			ballots.add(new AllocationBallot(weights.get(j),dd));
		}		
		int[] ii = AllocationElection.getWinners(ballots,dballots.get(0).length,seats);
		
		Vector<Integer> v = new Vector<Integer>();
		for( int s = 0; s < seats; s++) {
			v.add(ii[s]);
		}
		Collections.sort(v);
		for( int s = 0; s < seats; s++) {
			ii[s] = v.get(s);
		}
		return ii;
	}

}
