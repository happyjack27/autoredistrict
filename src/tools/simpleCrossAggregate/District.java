package tools.simpleCrossAggregate;

import java.util.Vector;

public class District {
	double total_population = 0;
	int vtd_index;
	
	double[] totals = new double[Config.columns_to_transfer.length];
	
	District(int vtd_index) {
		this.vtd_index = vtd_index;
	}
	public void addToTotals(String[] row, int[] block_indices) {
		if( totals == null) {
			totals = new double[block_indices.length];
			for( int i = 0; i < block_indices.length; i++) {
				totals[i] = 0;
			}
		}
		for( int i = 0; i < block_indices.length; i++) {
			totals[i] += Double.parseDouble(row[block_indices[i]].replaceAll(",",""));
		}
	}
}
