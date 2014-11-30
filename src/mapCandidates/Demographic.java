package mapCandidates;

import serialization.ReflectionJSONObject;

public class Demographic extends ReflectionJSONObject<Demographic> {
	public int block_id;
	public int population;
	public double turnout_probability;
	double[] vote_prob;
}
