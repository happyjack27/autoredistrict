package util.ballot_counters;

import java.util.Vector;

public class CS_MTV_AnalogAllocation extends CS_MTV {
	public String getName() {
		return "MTV AnalogAllocation";
	}
	@Override
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		Vector<double[]> dballots = new Vector<double[]>();
		Vector<Double> weights = new Vector<Double>();
		for( MultiBallot m : multi_ballots) {
			double[] dd = new double[m.n_votes.length];
            System.arraycopy(m.analog_allocation, 0, dd, 0, dd.length);
			dballots.add(dd);
			weights.add(m.weight);
		}
		return getWinnersGeneric(weights,dballots, seats);
	}

}
