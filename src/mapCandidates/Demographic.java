package mapCandidates;

import java.util.Vector;

import serialization.ReflectionJSONObject;

public class Demographic extends ReflectionJSONObject<Demographic> {
	public int block_id;
	public int population;
	public double turnout_probability;
	double[] vote_prob;
	
	public void post_deserialize() {
		super.post_deserialize();
		Vector<String> v_vote_prob = getVector("vote_probs");
		vote_prob = new double[v_vote_prob.size()];
		
		for( int i = 0; i < v_vote_prob.size(); i++) {
			vote_prob[i] = new Double(v_vote_prob.get(i));
		}
	}
	public void pre_serialize() {
		super.pre_serialize();
		
	}
}
