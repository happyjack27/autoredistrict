package pareto.scorers;

import pareto.SimpleDistrictMap;
import pareto.prototypes.ParetoPoint;
import pareto.prototypes.Scorer;
import solutions.Settings;

public class ScorerProportionality implements Scorer<SimpleDistrictMap> {

	@Override
	public double computeScore(ParetoPoint<SimpleDistrictMap> paretoPoint) {
		SimpleDistrictMap sdm = (SimpleDistrictMap) paretoPoint;
		return sdm.dm.fairnessScores[8];
	}
}
