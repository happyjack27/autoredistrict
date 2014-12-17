package mapCandidates;
import java.util.*;

import serialization.JSONObject;

class District extends JSONObject {
    Vector<Block> blocks = new Vector<Block>();

    double getPopulation() {
        double pop = 0;
        for( Block block : blocks) {
        	if( block.demographics == null || block.demographics.size() == 0) {
        		System.out.println("no demographics!");
        	}
        	for(Demographic p : block.demographics)
              pop += p.population;
        }
        return pop;
    }

    public double getSelfEntropy() {
        double total = 0;
        double[] wins  = new double[Candidate.candidates.size()];
        for( int i = 0; i < wins.length; i++) {
        	wins[i] = 0;
        }
        for( int i = 0; i < Settings.num_district_outcomes; i++) {
        	double[] outcome = getAnOutcome();
        	int best = -1;
        	double best_value = -1;
            for( int j = 0; j < outcome.length; j++) {
            	if( outcome[j] > best_value) {
            		best = j;
            		best_value = outcome[j];
            	}
            }
            wins[best]++;
        }
        for( int i = 0; i < wins.length; i++) {
            total += wins[i];
        }

        double H = 0;
        for( int i = 0; i < wins.length; i++) {
            double p = ((double)wins[i]) / total; 
            H -= p*Math.log(p);
        }

        return H;
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

        /*
        for( Edge edge : block.edges)
            if( edge.areBothSidesSameDistrict(block_districts))
                addAllConnected( edge.block1 == block ? edge.block2 : edge.block1, region, region_hash, block_districts);
                */
    }
    double getRegionPopulation(Vector<Block> region) {
        double population = 0;
        if( region == null) {
        	return 0;
        }
        for( Block block : region) {
        	for(Demographic p : block.demographics)
                population += p.population;
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
