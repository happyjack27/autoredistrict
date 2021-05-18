package pmc_tools.simpleCrossAggregate;

import java.util.Vector;

public class District {
	public double total_population = 0;
	public int index_in_file;
	
	double[] totals = new double[Config.columns_to_transfer.length];
	
	public District(int vtd_index) {
		this.index_in_file = vtd_index;
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
