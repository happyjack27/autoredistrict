package util.ballot_counters;

import java.util.Vector;

public class CS_MTV_AnalogAllocation extends CS_MTV {
	public String getName() {
		return "MTV AnalogAllocation";
	}
	@Override
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		Vector<double[]> dballots = new Vector<double[]>();
		for( MultiBallot m : multi_ballots) {
			double[] dd = new double[m.analog_allocation.length];
			for( int i = 0; i < dd.length; i++) {
				dd[i] = (double)m.analog_allocation[i];
			}
			dballots.add(dd);
		}
		return getWinnersGeneric(dballots, seats);
	}

}
