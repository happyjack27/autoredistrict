package mapCandidates;
import java.util.*;

import serialization.JSONObject;

public class District extends JSONObject {
    Vector<Block> blocks = new Vector<Block>();
    
    public static boolean adjust_vote_to_population = true;
    public static boolean self_entropy_by_vote_count = true;


    double[][] outcomes;
    double[][] pop_balanced_outcomes;
    
    private double population = -1;
    void resetPopulation() {
    	population = -1;
    }

    public double getPopulation() {
    	if( population >= 0) {
    		return population;
    	}
        double pop = 0;
        for( Block block : blocks) {
        	if( block.demographics == null || block.demographics.size() == 0) {
        		//System.out.println("no demographics!");
        	}
        	if( block.has_census_results) {
        		pop += block.population;
        	} else {
            	for(Demographic p : block.demographics) {
                    pop += p.population;
            	}
        	}
        }
        population = pop;
        return pop;
    }

    public double getSelfEntropy(double[][] outcomes) {
    	if( outcomes == null) {
    		outcomes = generateOutcomes(Settings.num_elections_simulated);
    	}

        double total = 0;
        double[] wins  = new double[Candidate.candidates.size()];
        for( int i = 0; i < wins.length; i++) {
        	wins[i] = 0;
        }
    	if( Settings.use_new_self_entropy_method) {
    		double[] mu = new double[wins.length];
    		double[] sigma = new double[wins.length];
    		double[] n = new double[wins.length];
    		
    		for( Block b : blocks) {
    			double[][] msn = b.getMuSigmaN();
                for( int i = 0; i < wins.length; i++) {
                	try {
            			mu[i] += msn[i][0];
            			sigma[i] += msn[i][1];
            			n[i] += msn[i][2];
                	} catch (Exception ex) {
                		System.out.println("i "+i+" block "+b.id);
                		ex.printStackTrace();
                		
                	}
        		}
            }
            for( int i = 0; i < wins.length; i++) {
            	double dn = n[i]*Settings.pct_turnover;
            	mu[i] *= (1.0-Settings.pct_turnover);
            	sigma[i] *= (1.0-Settings.pct_turnover);
            	//n[i] *= (1.0-Settings.pct_turnover);
            	double dp = 0.5;
    			double dmu = dn*dp;
    			double dsigma = dn*dp*(1-dp);
            	mu[i] += dmu;
            	sigma[i] += dsigma;
            }
    		
    		double t = 0;
            for( int i = 0; i < wins.length; i++) {
            	wins[i] = Gaussian.getProbForMuSigmaN(mu[i],sigma[i],n[i]);
            	if( wins[i] != wins[i] || wins[i] == 0) {
            		wins[i] = 0.00000001;
            	}
            	t += wins[i];
    		}
            if( t == 0) {
            	t = 1;
            }
            for( int i = 0; i < wins.length; i++) {
            	wins[i] /= t;
            }    		
    	} else {
            for( int i = 0; i < outcomes.length; i++) {
            	double[] outcome = outcomes[i];
            	if( self_entropy_by_vote_count) {
            		double[] sim_results;
                	if( Settings.use_new_self_entropy_method) {
                		sim_results = Gaussian.getOddsFromBlocks(blocks);
                	} else {

                    	double total_votes = 0;
                    	for( int n = 0; n < outcome.length; n++) {
                    		total_votes += outcome[n];
                    	}
                    	System.out.println("total_votes "+total_votes);
                		//double[] sim_results = StaticFunctions.convertProbsToResults(outcome, Settings.voters_to_simulate);
                		sim_results = StaticFunctions.convertProbsToResults(outcome,(int)total_votes);
                	}
                    for( int j = 0; j < outcome.length; j++) {
                    	wins[j] += sim_results[j];//outcome[j];
                    }
            	} else {
                	int best = -1;
                	double best_value = -1;
                    for( int j = 0; j < outcome.length; j++) {
                    	if( outcome[j] > best_value) {
                    		best = j;
                    		best_value = outcome[j];
                    	}
                    }
                    if( best >= 0) {
                    	wins[best]++;
                    }
            	}
            }
    	}
        for( int i = 0; i < wins.length; i++) {
            total += wins[i];
        }

        double H = 0;
        for( int i = 0; i < wins.length; i++) {
            double p = ((double)wins[i]) / total; 
            H -= p*Math.log(p);
        }

        return Math.pow(H, Settings.self_entropy_exponent);
    }
    public double[][] generateOutcomes(int num) {
    	if( adjust_vote_to_population) {
        	outcomes = new double[num][];
        	pop_balanced_outcomes = new double[num][];
        	for( int i = 0; i < num; i++) {
        		double[][] dd = getAnOutcomePair(); 
        		outcomes[i] = dd[0];
        		pop_balanced_outcomes[i] = dd[1];
        	}
        	return outcomes;
    	} else {
        	outcomes = new double[num][];
        	for( int i = 0; i < num; i++) {
        		outcomes[i] = getAnOutcome();
        	}
        	pop_balanced_outcomes = outcomes;
        	return outcomes;
    	}
    }
    
    public double[] getAnOutcome() {
        double[] district_vote = new double[Candidate.candidates.size()]; //inited to 0
        if( blocks.size() == 0) {
            for( int i = 0; i < district_vote.length; i++) {//most_value) {
                district_vote[i] = 0;
            }
            return district_vote;
        }
        for( Block block : blocks) {
            double[] block_vote = block.getOutcome();
            if( block_vote != null) {
                for( int i = 0; i < block_vote.length; i++) {//most_value) {
                    district_vote[i] += block_vote[i];
                }
            }
        }
        return district_vote;
    }
    public double[][] getAnOutcomePair() {
        double[] district_vote = new double[Candidate.candidates.size()]; //inited to 0
        double[] pop_district_vote = new double[Candidate.candidates.size()]; //inited to 0
        if( blocks.size() == 0) {
            for( int i = 0; i < district_vote.length; i++) {
                district_vote[i] = 0;
            }
            return new double[][]{district_vote,district_vote};
        }
        for( Block block : blocks) {
            double[] block_vote = block.getOutcome();
            if( block_vote != null) {
            	double tot = 0;
                for( int i = 0; i < block_vote.length; i++) {
                	tot += block_vote[i];
                    district_vote[i] += block_vote[i];
                }
                if( tot == 0) {
                	tot = 0;
                } else {
                	tot = block.population/tot;
                }
                
                for( int i = 0; i < block_vote.length; i++) {
                    pop_district_vote[i] += block_vote[i]*tot;
                }
            }
        }
        return new double[][]{district_vote,pop_district_vote};
    }



    public double[] getVotes() {
        double[] district_vote = new double[Candidate.candidates.size()]; //inited to 0
        if( blocks.size() == 0) {
            for( int i = 0; i < district_vote.length; i++) {//most_value) {
                district_vote[i] = 0;
            }
            return district_vote;
        }
        for( Block block : blocks) {
            double[] block_vote = block.getVotes();
            if( block_vote != null) {
                for( int i = 0; i < block_vote.length; i++) {//most_value) {
                    district_vote[i] += block_vote[i];
                }
            }
        }
        return district_vote;
    }

    //getRegionCount() counts the number of contiguous regions by counting the number of vertex cycles.  a proper map will have exactly 1 contiguous region per district.
    //this is a constraint to apply _AFTER_ a long initial optimization.  as a final tuning step.
    int getRegionCount(int[] block_districts) {
        return getRegions(block_districts).size();
    }

    Vector<Block> getTopPopulationRegion(int[] block_districts) {
        Vector<Vector<Block>> regions = getRegions(block_districts);
        Vector<Block> high = null;
        double max_pop = 0;
        for( Vector<Block> region : regions) {
            double pop = getRegionPopulation(region);
            if( pop > max_pop || high == null) {
                max_pop = pop;
                high = region;
            }
        }
        return high;
    }
    Vector<Vector<Block>> getRegions(int[] block_districts) {
        Hashtable<Integer,Vector<Block>> region_hash = new Hashtable<Integer,Vector<Block>>();
        Vector<Vector<Block>> regions = new Vector<Vector<Block>>();
        for( Block block : blocks) {
            if( region_hash.get(block.id) != null)
                continue;
            Vector<Block> region = new Vector<Block>();
            regions.add(region);
            addAllConnected(block,region,region_hash,block_districts);
        }
        return regions;
    }
    //recursively insert connected blocks.
    void addAllConnected( Block block, Vector<Block> region,  Hashtable<Integer,Vector<Block>> region_hash, int[] block_districts) {
        if( region_hash.get(block.id) != null)
            return;
        region.add(block);
        region_hash.put(block.id,region);
        for( Block other_block : block.neighbors) {
        	if( block_districts[other_block.id] == block_districts[block.id]) {
        		addAllConnected( other_block, region, region_hash, block_districts);
        	}
        }
    }
    double getRegionPopulation(Vector<Block> region) {
        double population = 0;
        if( region == null) {
        	return 0;
        }
        for( Block block : region) {
        	if( block.has_census_results) {
        		population += block.population;
        	} else {
            	for(Demographic p : block.demographics) {
            		population += p.population;
            	}
        	}
        }
        return population;
    }

	@Override
	public void post_deserialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}


}
