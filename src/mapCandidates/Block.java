package mapCandidates;
import java.util.*;

import serialization.*;

public class Block extends ReflectionJSONObject<Block> {
    public int id;
	public static int id_enumerator = 0;
	public int state = 0;
	
	public String name = "";


    public Vector<Edge> edges = new Vector<Edge>();
    public Vector<Block> neighbors = new Vector<Block>();
    public double[] neighbor_lengths;
    public Vector<Demographic> demographics = new Vector<Demographic>();

    //double[] population;
    //double[] prob_turnout;
    //double[][] prob_vote = null;//new double[DistrictMap.candidates.size()];
    double[] vote_cache = null;
    double[][] vote_caches = null;
    
    static boolean use_vote_caches = true; 
    static boolean use_vote_cache = true;
    static int cache_reuse_times = 16;
    static int vote_cache_size = 128;
    
    int cache_reused = 0;
    
    public Block() {
    	super();
    	id = id_enumerator++;
    }
    public boolean equals(Block b) {
    	return b != null && b.id == this.id;
    }
    public void syncNeighbors() {
		for(Block b : neighbors) {
			boolean is_in = false;
			for(Block b2 : b.neighbors) {
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
    	neighbor_lengths = new double[neighbors.size()];
    	for( int i = 0; i < neighbor_lengths.length; i++) {
    		neighbor_lengths[i] = 0;
    	}
		for(Edge e : edges) {
	    	for( int i = 0; i < neighbor_lengths.length; i++) {
	    		Block b = neighbors.get(i);
	    		if( e.block1_id == b.id || e.block2_id == b.id){
	    			neighbor_lengths[i] += e.length;	
	    		}
	    	}
		}
    	
    }
    
    public void collectNeighbors() {
		//HashSet<Block> hashBlocks = new HashSet<Block>(); 
		neighbors = new Vector<Block>();
		//System.out.println("edges: "+edges.size());
		//System.out.print("block "+id+" neighbors: ");
		for( Edge e : edges) {
			Block b = e.block1.id == this.id ? e.block2 : e.block1;
			if( b != null && b.id != this.id) {
				boolean is_in = false;
				for(Block b2 : neighbors) {
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
	
    double[] getVotes() {
        if( use_vote_caches) {
            if( vote_caches == null) {
                vote_caches = new double[vote_cache_size][];
                for( int i = 0; i < vote_caches.length; i++) {
                    generateVotes();
                    vote_caches[i] = vote_cache;
                }
            }
            return vote_caches[(int)(Math.random()*(double)vote_caches.length)];
        } else if( vote_cache == null || cache_reused >= cache_reuse_times) {
            generateVotes();
            cache_reuse_times = 0;
        }
        cache_reuse_times++;
        return vote_cache;
    }

    void generateVotes() {
        double[] votes = new double[Candidate.candidates.size()];
        for(int i = 0; i < votes.length; i++) {
            votes[i] = 0;
        }
        for( Demographic d : demographics) {
            for(int i = 0; i < d.population; i++) {
                double p = Math.random();
                if( p > d.turnout_probability) {
                    continue;
                }
                p = Math.random();
                for( int j = 0; j < d.vote_prob.length; j++) {
                    p -=  d.vote_prob[j];
                    if( p <= 0) {
                        votes[j]++;
                        break;
                    }
                }
            }
        	
        }
        vote_cache = votes;
    }


}
