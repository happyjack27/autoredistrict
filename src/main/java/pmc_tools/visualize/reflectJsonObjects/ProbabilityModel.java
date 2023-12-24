package pmc_tools.visualize.reflectJsonObjects;

import jsonMap.JsonMap;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Vector;

public class ProbabilityModel extends JsonMap {
	
	int num_districts = 0;
	
	double[][] dem_counts = null;
	double[][] rep_counts = null;

	BetaDistribution election_betas = null;
	Vector<BetaDistribution> district_betas = null;
	Vector<BetaDistribution> centered_district_betas = null;

	GammaDistribution election_gammas = null;
	Vector<GammaDistribution> district_gammas = null;

	@Override
	public void post_deserialize(){}
	@Override
	public void pre_serialize(){}

}
