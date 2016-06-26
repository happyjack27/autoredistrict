package util.ballot_counters;

import java.util.Vector;

public interface iCountingSystem {
	public String getName();
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats);
}
