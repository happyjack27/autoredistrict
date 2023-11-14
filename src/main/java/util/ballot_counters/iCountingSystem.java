package util.ballot_counters;

import java.util.Vector;

public interface iCountingSystem {
	String getName();
	int[] getWinners(Vector<MultiBallot> multi_ballots, int seats);
}
