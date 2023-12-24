package pareto;

import geography.FeatureCollection;
import pareto.prototypes.ParetoFrontier;
import pareto.prototypes.Scorer;

import java.util.HashMap;

public class ParetoFrontierDistrictMap {
	
	ParetoFrontier<SimpleDistrictMap> paretoFront = null;

	public void initialize(FeatureCollection fc) {
		SimpleDistrictMap.featureCollection = fc;
		
		HashMap<String, Scorer<SimpleDistrictMap>> scorers = new HashMap<String, Scorer<SimpleDistrictMap>>();
		
		scorers.put("competitiveness",new pareto.scorers.ScorerCompetitiveness());
		scorers.put("compactness",new pareto.scorers.ScorerCompactness());
		scorers.put("contiguity",new pareto.scorers.ScorerContiguity());
		scorers.put("descriptive representation",new pareto.scorers.ScorerDescriptiveRepresentation());
		scorers.put("population equality",new pareto.scorers.ScorerPopulationEquality());
		scorers.put("proportionality",new pareto.scorers.ScorerProportionality());
		scorers.put("splits",new pareto.scorers.ScorerSplits());
		scorers.put("total asymmetry",new pareto.scorers.ScorerTotalAsymmetry());
		
		paretoFront = new ParetoFrontier<SimpleDistrictMap>(scorers);
		paretoFront.generator = new SimpleDistrictMap(scorers);
	}
	public static void main(String[] ss) {
		//p.startIterateThreads(8);
		//p.iterate();
	}
}
