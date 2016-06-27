package util.ballot_counters;

import java.util.Vector;

public class CS_MTV_DigitalAllocation extends CS_MTV {
	public String getName() {
		return "MTV DigitalAllocation";
	}
	@Override
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		Vector<double[]> dballots = new Vector<double[]>();
		Vector<Double> weights = new Vector<Double>();
		for( MultiBallot m : multi_ballots) {
			double[] dd = new double[m.n_votes.length];
			for( int i = 0; i < dd.length; i++) {
				dd[i] = (double)m.digital_allocation[i];
			}
			dballots.add(dd);
			weights.add(m.weight);
		}
		return getWinnersGeneric(weights,dballots, seats);
	}

}
