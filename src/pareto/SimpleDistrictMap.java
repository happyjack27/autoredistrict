package pareto;

import java.util.HashMap;
import java.util.Vector;

import geography.FeatureCollection;
import geography.VTD;
import pareto.prototypes.ParetoPoint;
import pareto.prototypes.Scorer;
import solutions.DistrictMap;
import solutions.Settings;

public class SimpleDistrictMap extends ParetoPoint<SimpleDistrictMap> {
	public SimpleDistrictMap(HashMap<String, Scorer<SimpleDistrictMap>> scorers) {
		super(scorers);
	}

	int[] vtd_assignments = new int[]{};
	public DistrictMap dm = null;
	public static FeatureCollection featureCollection = null;

	@Override
	public ParetoPoint<SimpleDistrictMap> clone(double mutation) {
		SimpleDistrictMap sdm = new SimpleDistrictMap(scorers);
		sdm.dm = new DistrictMap(featureCollection.vtds, Settings.num_districts);
		sdm.dm.setGenome(vtd_assignments);
		sdm.dm.mutate_boundary(mutation);
		sdm.mutation_amount = sdm.dm.actual_mutate_rate;
		sdm.vtd_assignments = sdm.dm.vtd_districts;
		
		return sdm;
	}

	@Override
	public ParetoPoint<SimpleDistrictMap> generate() {
		SimpleDistrictMap sdm = new SimpleDistrictMap(scorers);
		sdm.dm = new DistrictMap(featureCollection.vtds, Settings.num_districts);
		sdm.mutation_amount = 0;
		sdm.vtd_assignments = sdm.dm.vtd_districts;
		
		return sdm;
	}
	
	@Override
	public void computeScores() {
		dm.calcFairnessScores();
		super.computeScores();
		dm = null;
	}

}
