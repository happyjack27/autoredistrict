package util.ballot_counters;

import java.util.*;

public abstract class CS_MTV implements iCountingSystem {
	public int[] getWinnersGeneric(Vector<double[]> dballots, int seats) {
		
		Vector<AllocationBallot> ballots = new Vector<AllocationBallot>();
		for( double[] dd : dballots) {
			double tot = 0;
			for( int i = 0; i < dd.length; i++) {
				tot += dd[i];
			}
			for( int i = 0; i < dd.length; i++) {
				dd[i] /= tot;
			}

			ballots.add(new AllocationBallot(1,dd));
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
