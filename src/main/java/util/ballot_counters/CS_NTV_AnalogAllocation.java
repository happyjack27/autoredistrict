package util.ballot_counters;

import java.util.*;

public class CS_NTV_AnalogAllocation implements iCountingSystem {
	public String getName() {
		return "NTV Analog Allocation";
	}
	public int[] getWinners(Vector<MultiBallot> multi_ballots, int seats) {
		double[] tots = new double[multi_ballots.get(0).analog_allocation.length];
		for( MultiBallot m : multi_ballots) {
			for(int i = 0; i < tots.length; i++) {
				tots[i] += m.analog_allocation[i]*m.weight;
			}
		}
		
		int[] ii = new int[seats];
		
		Vector<Integer> v = new Vector<Integer>();

		for( int s = 0; s < seats; s++) {
			double m = -1;
			int m_i = -1;
			for(int i = 0; i < tots.length; i++) {
				if( tots[i] > m) {
					m_i = i;
					m = tots[i];
				}
			}
			v.add(m_i);
			//ii[s] = m_i;
			tots[m_i] = 0;
		}
		Collections.sort(v);
		for( int s = 0; s < seats; s++) {
			ii[s] = v.get(s);
		}
		return ii;
	}


}
