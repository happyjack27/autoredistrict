package solutions;
import geography.Feature;

import java.util.*;

import serialization.*;

public class VTD extends ReflectionJSONObject<VTD> {
    public int id;
	public static int id_enumerator = 0;
	public Feature feature = null;
	public int state = 0;
	public int temp = -1;
	public double area = 0;
	public String county = "";
	
	//public String name = "";


    public Vector<Edge> edges = new Vector<Edge>();
    public Vector<VTD> neighbors = new Vector<VTD>();
    public double[] neighbor_lengths;
    public double unpaired_edge_length = 0;
    public Vector<Vector<Election>> elections = new Vector<Vector<Election>>();
    //private double[][] mu_sigma_n = null;
    
    public boolean has_census_results = false;
    public boolean has_election_results = false;
    public double population=1;
    
    public void resetOutcomes() {
    	outcomes = null;    	
    }
    /*
    public void recalcMuSigmaN() {
    	try {
   		double[] successes = new double[Settings.num_candidates];
		double total = 0;
        for( Demographic d : demographics) {
            for( int j = 0; j < d.vote_prob.length; j++) {
            	double n = d.population * d.vote_prob[j]*d.turnout_probability;
            	n /= Settings.voting_coalition_size;
            	total += n;
            	successes[j] += n;
            }
        }
        mu_sigma_n = new double[Settings.num_candidates][];
        for( int j = 0; j < successes.length; j++) {
        	double n = total;
        	double p = successes[j] / total;
			double mu = n*p;
			double sigma = n*p*(1-p);
        	mu_sigma_n[j] = new double[]{mu,sigma,n};
        	if( total == 0) {
        		mu_sigma_n[j] = new double[]{0,0,0};
        	}
        }   
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    public double[][] getMuSigmaN() {
    	if( mu_sigma_n == null) {
    		recalcMuSigmaN();    		
    	}
    	return mu_sigma_n;
    }
    */
    
	double[][] outcomes;
	public double[] demographics = new double[]{};
	public String muni;
	public boolean temp_bool;

    public VTD() {
    	super();
    	id = id_enumerator++;
    }
    public boolean equals(VTD b) {
    	return b != null && b.id == this.id;
    }
    public void syncNeighbors() {
		for(VTD b : neighbors) {
			boolean is_in = false;
			for(VTD b2 : b.neighbors) {
				if( b2.id == this.id){
					is_in = true;
					break;
				}
			}
			if( !is_in) {
				b.neighbors.add(this);
			}
		}
    	
    }
    public void collectNeighborLengths() {
    	unpaired_edge_length = 0;
    	neighbor_lengths = new double[neighbors.size()];
    	for( int i = 0; i < neighbor_lengths.length; i++) {
    		neighbor_lengths[i] = 0;
    	}
		for(Edge e : edges) {
			boolean found = false;
	    	for( int i = 0; i < neighbor_lengths.length; i++) {
	    		VTD b = neighbors.get(i);
	    		if( e.ward1_id == b.id || e.ward2_id == b.id){
	    			neighbor_lengths[i] += e.length;
	    			found = true;
	    			break;
	    		}
	    	}
	    	if( !found) {
	    		unpaired_edge_length += e.length;
	    	}
		}
    }
    
    public void collectNeighbors() {
    	//this gets a list of distinct neighbors.
    	
		neighbors = new Vector<VTD>();
		for( Edge e : edges) {
			VTD b = e.ward1.id == this.id ? e.ward2 : e.ward1;
			if( b != null && b.id != this.id) {
				boolean is_in = false;
				for(VTD b2 : neighbors) {
					if( b2.id == b.id){
						is_in = true;
						break;
					}
				}
				if( !is_in) {
					neighbors.add(b);
					//System.out.print(""+b.id+", ");
				}
			}
		}
		//System.out.println();
    }

    
	@Override
	public void post_deserialize() {
		super.post_deserialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
    double[] getOutcome() {
    	if( outcomes == null) {
    		generateOutComes();
    	}
    	int i = (int)Math.floor(Math.random()*(double)outcomes.length);
    	return outcomes[i];
    }
    
    public void generateOutComes() {
    	if( !District.use_simulated_elections) {
        	outcomes = new double[1][];
    		outcomes[0] = new double[Settings.num_candidates];
    	    
    		//aggregate and normalize voting probs
            for(int i = 0; i < outcomes[0].length; i++) {
            	outcomes[0][i] = 0;
            }
            for( int elec = 0; elec < elections.size(); elec++) {
		        for( Election d : elections.get(elec)) {
		            for( int j = 0; j < d.vote_prob.length; j++) {
		            	outcomes[0][j] += d.population * d.vote_prob[j]*d.turnout_probability;//d.vote_prob[j];
		            }
		        }
            }
            for(int i = 0; i < outcomes[0].length; i++) {
            	outcomes[0][i] /= elections.size();
            }    		
    	} else {
	    	outcomes = new double[Settings.num_ward_outcomes][];
	    	for( int out = 0; out < outcomes.length; out++) {
	    		outcomes[out] = new double[Settings.num_candidates];
	    	    
	    		//aggregate and normalize voting probs
	        	double[] probs = new double[Settings.num_candidates];
	            for(int i = 0; i < probs.length; i++) {
	            	probs[i] = 0;
	            }
	            int elec = (int)(Math.random()*(double)elections.size());
	            for( Election d : elections.get(elec)) {
	                for( int j = 0; j < d.vote_prob.length; j++) {
	                	probs[j] += d.population * d.vote_prob[j]*d.turnout_probability;
	                }
	            }
	            double total_population = 0;
	            for(int i = 0; i < probs.length; i++) {
	            	total_population += probs[i];
	            }
	            double r_tot_prob  = 1.0/total_population;
	            for(int i = 0; i < probs.length; i++) {
	            	probs[i] *= r_tot_prob;
	            }
	
	            for(int j = 0; j < total_population; j++) {
	                double p = Math.random();
	                for( int k = 0; k < probs.length; k++) {
	                    p -=  probs[k];
	                    if( p <= 0) {
	                    	outcomes[out][k]++;
	                        break;
	                    }
	                }
	    		}
	    	}
    	}
    }
}
