package pareto.scorers;

import pareto.SimpleDistrictMap;
import pareto.prototypes.ParetoPoint;
import pareto.prototypes.Scorer;
import solutions.Settings;

public class ScorerCompetitiveness implements Scorer<SimpleDistrictMap> {

	@Override
	public double computeScore(ParetoPoint<SimpleDistrictMap> paretoPoint) {
		SimpleDistrictMap sdm = (SimpleDistrictMap) paretoPoint;
		return sdm.dm.fairnessScores[5];
	}
			/*
			dm.fairnessScores[8], //diag error (proportional)
			dm.fairnessScores[11], //descr. rep.
			(
					//Settings.square_root_compactness 
					dm.fairnessScores[0]
					//: (Math.sqrt(dm.fairnessScores[0])+Math.sqrt(dm2.fairnessScores[0])+Math.sqrt(dm3.fairnessScores[0]))*0.3333333
				), //BORDER LENGTH
			dm.fairnessScores[3], //DISCONNECTED POP
			dm.fairnessScores[2], //POP IMBALANCE
			dm.fairnessScores[9],//splits
			dm.fairnessScores[7], //seats / votes asymmetry
			dm.fairnessScores[12], //spec asymm


			dm.fairnessScores[5], //WASTED VOTES TOTAL
			//dm.fairnessScores[1]*conversion_to_bits, //REP IMBALANCE
			//dm.fairnessScores[6], //WASTED VOTES IMBALANCE
			dm.fairnessScores[10], //racial vote dilution
			*
			*/

}
