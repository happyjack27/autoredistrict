package util.ballot_counters;

import java.util.Vector;

public class CS_MTV_Approval extends CS_MTV {
	public String getName() {
		return "MTV Approval";
	}
	@Override
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		Vector<double[]> dballots = new Vector<double[]>();
		Vector<Double> weights = new Vector<Double>();
		for( MultiBallot m : multi_ballots) {
			double[] dd = new double[m.n_votes.length];
			for( int i = 0; i < dd.length; i++) {
				dd[i] = (double)m.approval[i];
			}
			dballots.add(dd);
			weights.add(m.weight);
		}
		return getWinnersGeneric(weights,dballots, seats);
	}

}
