package util.ballot_counters;

import java.util.*;

public class CS_STV_Correct implements iCountingSystem {
	public String getName() {
		return "STV Correct Hare";
	}
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		STVElection2 e = new STVElection2();
		e.use_droop = false;
		e.reset_ignores = true;
		e.ignore_fewest_votes = false;
		
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
