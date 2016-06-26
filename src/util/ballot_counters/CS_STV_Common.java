package util.ballot_counters;

import java.util.*;

public class CS_STV_Common implements iCountingSystem {
	public String getName() {
		return "STV Common Droop";
	}
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		STVElection2 e = new STVElection2();
		e.use_droop = true;
		e.reset_ignores = false;
		e.ignore_fewest_votes = true;
		
		Vector<STVBallot> ballots = new Vector<STVBallot>();
		for( MultiBallot m : multi_ballots) {
			ballots.add(new STVBallot(1,m.ranked_choice));
		}		
		Vector<Integer> v = e.getWinners(ballots,seats,multi_ballots.get(0).ranked_choice.length);

		Collections.sort(v);
		int[] ii = new int[seats];
		for( int s = 0; s < seats; s++) {
			ii[s] = v.get(s);
		}
		return ii;
	}


}
