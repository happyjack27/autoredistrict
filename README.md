DONE:
1. after loading, fill in ecology data structures.
2. also initalize candidates in ecology on loading of election results. 
3. write parts to adjust population, number of districts on evolve function
4. write parts to start and stop evolution
5. write parts to color code blocks by district based on best scoring map. (make sure to invalidate and repaint each evolve iteration)
6. add inverters for score components - in drop down menu.
7. multithread testing, but reusable threads.
8. add exporting of results ( simple tab deliminted: precinct - district)
*. add listener to num of districts to adjust, should be a target and work together with evolve. 

TODO:
1. add zoom in feature
2. add load district info from geojson (they can choose the property, shift from 1-index)
3. add save district info frtoom geojson (they can choose the property, shift to 1-index)
4. create help text for all of the menu items, sliders, etc.
5. add tooltips for all of the menu items, sliders, etc.
6. move algorithm pieces into advanced help
7. implement show district labels


	/*
	block and length data can be taken from a standard dime file, which i presume any municiplality has. the census data, well i'm sure they have it electronically, cause hey, they draw districts. so getting the geolocation dataset is not a problem. you still have to add in code to make sure the districts are contiguous, though.
	
	basic idea is you do it gestalt, without worrying about contiguity a prioiri.
	
	any standard heuristic optimization algorithm will do fine; generic algorithm and swarm intelligence are the two that come most readily to mind.
	
	the gene of a genetic algorithm would just be a vector of which district each block belongs to, in order.  so one would have to add an ordering to the blocks.  (i.e. number them 1 through whatever).  
	
	the two non trivial things: calculating geometric complexity and calculating democratic representativeness are what are shown.  the first, by minimizing border length, the second, by minimizing kullback-leibler divergence of the election results from the popular vote. 
	*/
	
	//QUANTITIATIVE GERRYMANDER CALCULATOR 
	//for use in heuristic optimization (e.g. genetic algoritm / swarm ) for computer-automated redistricting.
	
	//language: Java

	/*
	 * TODO: need to add a metric for how evenly distributed voting power is.  preferably in units of bits or nats.
	 * 
	 * 	TODO: add persistence to business objects, ala json from/to.
	 * 
	 * measuring voting power as a posteri self-entropy of district?
 	*/
	
	import java.util.*;
	
	interface iEvolvable {
		
	    public int[] getGenome();
	    public int[] getGenome(int[] baseline);
	    public void setGenome(int[] genome);
	    public void calcFairnessScores(int trials);
	    public void crossover(int[] genome1,int[] genome2);
	    public void mutate( double prob);
	}
	
    
	class DistrictMap implements iEvolvable, Comparable<DistrictMap> {
		//static int num_parties = 0;
		int num_districts = 0;
		int[] block_districts = new int[]{};
		
		public static int sorting_polarity = 1;
		public static int hamming_distance_polarity = 1;
		
		public static boolean mutate_to_neighbor_only = false;
		public static double species_fraction = 0.25;
	
	    //spatial metrics
		public static double geometry_weight = 1;
	    public static double population_balance_weight = 1;
	    public static double disconnected_population_weight = 0; 

	    //fairness metrics
	    public static double disenfranchise_weight = 1;
	    public static double voting_power_balance_weight = 1; 
	    
	    
	    public double[] fairnessScores = new double[5];
	    public double fitness_score = 0;
	
	    Vector<Block> blocks;
	    Vector<District> districts;
	    Vector<Candidate> candidates;
	    
	    Vector<DistrictMap> population;
	    
	    public void init(
	    	    Vector<Block> blocks,
	    	    Vector<District> districts,
	    	    Vector<Candidate> candidates,
	    	    int population_size
	    		) {
	    	this.blocks = blocks;
	    	this.districts = districts;
	    	this.candidates = candidates;
	    	population =  new Vector<DistrictMap>();
	    	for( int i = 0; i < population_size; i++) {
	    		population.add(new DistrictMap(blocks,districts.size()));
	    	}
	    }
	    public void start_from_genome(int[] genome, double mutation_rate) {
	    	for( DistrictMap map : population) {
	    		map.setGenome(genome);
	    		map.mutate(mutation_rate);
	    	}
	    }
	    
	    
	    //makeLike
	    //sfads
	    public void evolveWithSpeciation(double replace_fraction, double mutation_rate, double mutation_boundary_rate, int trials, boolean score_all) {
	    	int cutoff = population.size()-(int)((double)population.size()*replace_fraction);
	    	int speciation_cutoff = (int)((double)cutoff*species_fraction);
	    	
	    	if( score_all) {
		    	for( DistrictMap map : population) {
		    		map.calcFairnessScores(trials);
		    	}
	    	} else {
		    	for( int i = cutoff; i < population.size(); i++) {
		    		population.get(i).calcFairnessScores(trials);
		    	}
	    	}
	    	for( int i = 0; i < 5; i++) {
		    	for( DistrictMap map : population) {
		    		map.fitness_score = map.fairnessScores[i];
		    	}
		    	Collections.sort(population);
		    	double mult = 1.0/population.size();
		    	for( int j = 0; j < population.size(); j++) {
		    		DistrictMap map = population.get(j);
		    		map.fairnessScores[i] = ((double)j)*mult; 
		    	}
	    	}
	    	
	    	double[] weights = new double[]{
	    	        geometry_weight, 
	    	        disenfranchise_weight, 
	    	        population_balance_weight,
	    	        disconnected_population_weight,
	    	        voting_power_balance_weight
	    	};
	    	
	    	for( int j = 0; j < population.size(); j++) {
	    		DistrictMap map = population.get(j);
	    		map.fitness_score = 0;
		    	for( int i = 0; i < 5; i++) {
		    		map.fitness_score += map.fairnessScores[i]*weights[i];
		    	}
	    	}
	    	
	    	Collections.sort(population);
	    	
	    	Vector<DistrictMap> available = new Vector<DistrictMap>();
	    	for(int i = cutoff; i < population.size(); i++) {
	    		available.add(population.get(i));
	    	}
	    	
	    	for(int i = cutoff; i < population.size(); i++) {
	    		int g1 = (int)(Math.random()*(double)cutoff);
	    		DistrictMap map1 = available.get(g1);
		    	for(DistrictMap m : available) {
		    		m.makeLike(map1.getGenome());
		    		m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(), map1.getGenome())*hamming_distance_polarity;
		    	}
		    	Collections.sort(available);
	    		int g2 = (int)(Math.random()*(double)speciation_cutoff);
	    		DistrictMap map2 = available.get(g2);
	    		
	    		population.get(i).crossover(map1.getGenome(), map2.getGenome());
	    		population.get(i).mutate(mutation_rate);
	    		population.get(i).mutate_boundary(mutation_rate);
	    	}
	    }
	    
	    
	    public void evolve(double replace_fraction, double mutation_rate, double mutation_boundary_rate, int trials, boolean score_all) {
	    	int cutoff = population.size()-(int)((double)population.size()*replace_fraction);
	    	
	    	if( score_all) {
		    	for( DistrictMap map : population) {
		    		map.calcFairnessScores(trials);
		    	}
	    	} else {
		    	for( int i = cutoff; i < population.size(); i++) {
		    		population.get(i).calcFairnessScores(trials);
		    	}
	    	}
	    	for( int i = 0; i < 5; i++) {
		    	for( DistrictMap map : population) {
		    		map.fitness_score = map.fairnessScores[i];
		    	}
		    	Collections.sort(population);
		    	double mult = 1.0/population.size();
		    	for( int j = 0; j < population.size(); j++) {
		    		DistrictMap map = population.get(j);
		    		map.fairnessScores[i] = ((double)j)*mult; 
		    	}
	    	}
	    	
	    	double[] weights = new double[]{
	    	        geometry_weight, 
	    	        disenfranchise_weight, 
	    	        population_balance_weight,
	    	        disconnected_population_weight,
	    	        voting_power_balance_weight
	    	};
	    	
	    	for( int j = 0; j < population.size(); j++) {
	    		DistrictMap map = population.get(j);
	    		map.fitness_score = 0;
		    	for( int i = 0; i < 5; i++) {
		    		map.fitness_score += map.fairnessScores[i]*weights[i];
		    	}
	    	}

	    	Collections.sort(population);

	    	
	    	for(int i = cutoff; i < population.size(); i++) {
	    		int g1 = (int)(Math.random()*(double)cutoff);
	    		int g2 = (int)(Math.random()*(double)cutoff);
	    		population.get(i).crossover(population.get(g1).getGenome(), population.get(g2).getGenome(population.get(g1).getGenome()));
	    		population.get(i).mutate(mutation_rate);
	    		population.get(i).mutate_boundary(mutation_rate);
	    	}
	    }
	    
	    //always find the most identical version before spawning new ones!
	    //this dramatically reduces convergence time!
	    public int[] getGenome(int[] baseline) {
	         Vector<int[]> versions =  getIdenticalGenomes(getGenome());
	         long closest = 9999999999999L;
	         int[] closest_version = null;
	         for( int[] version : versions) {
	            int test = getGenomeHammingDistance(version,baseline);
	            if( test < closest) {
	                closest = test;
	                closest_version = version;
	            }
	         }
	         return closest_version;
	    }
	    
	    public void mutate(double prob) {
	    	double max = candidates.size();
	    	for( int i = 0; i < block_districts.length; i++) {
	    		if( Math.random() < prob) {
    				block_districts[i] = (int)(Math.floor(Math.random()*max)+1.0);
	    		}
	    	}
	    }
	    
	    public void mutate_boundary(double prob) {
	    	boolean[] allow = new boolean[districts.size()+1];
	    	for( int i = 0; i < block_districts.length; i++) {
	    		if( Math.random() < prob) {
			    	for( int j = 0; j < allow.length; j++) {
			    		allow[j] = false;
			    	}
			    	allow[block_districts[i]] = true;
    				Block block = blocks.get(i);
    				for( Edge edge : block.edges) {
    					Block other_block = edge.block1 == block ? edge.block2 : edge.block1;
    					allow[block_districts[other_block.index]] = true;
    				}
    				double count = 0;
			    	for( int j = 0; j < allow.length; j++) {
			    		if( allow[j])
			    			count++;
			    	}
			    	int d = (int)(Math.random()*count); 
			    	for( int j = 0; j < allow.length; j++) {
			    		if( allow[j]) {
			    			if( d == 0) {
			    				block_districts[i] = j;
			    				break;
			    			}
			    			d--;
			    		}
			    	}
	    		}
	    	}
	    }
	    public void crossover(int[] genome1, int[] genome2) {
	    	for( int i = 0; i < block_districts.length; i++) {
	    		double r = Math.random();
	    		block_districts[i] = r < 0.5 ? genome1[i] : genome2[i];
	    	}
	    }
	    public void makeLike(int[] genome) {
	        Vector<int[]> identicals = getIdenticalGenomes(getGenome());
	        int lowest_score = 0;
	        int best_match = -1;
	        for( int i = 0; i < identicals.size(); i++) {
	        	int new_score = getGenomeHammingDistance(genome,identicals.get(i));
	        	if( best_match < 0 || new_score < lowest_score) {
	        		lowest_score = new_score;
	        		best_match = i;
	        	}
	        }
	        setGenome(identicals.get(best_match));
	    }

	    
	    public static Vector<int[]> getIdenticalGenomes(int[] genome) {
	        Vector<int[]> identicals = new Vector<int[]>();
	
	        int max = 0;
	        for( int i = 0; i < genome.length; i++) 
	            if( genome[i] > max)
	                max = genome[i];
	        int[] rename = new int[max+1];
	        for( int i = 0; i < rename.length; i++)
	            rename[i] = i;
	
	        //cycle through all permutations
	        Vector<int[]> permutations = new Vector<int[]>();
	        getPermutations(rename,0,permutations);
	        for( int[] permutation : permutations) 
	            identicals.add(getRenamed(genome,permutation));
	
	        return identicals;
	
	    }
	    public static void getPermutations(int[] a, int k, Vector<int[]> results) {
	        if(k==a.length) {
	            int[] result = new int[a.length];
	            for( int i = 0; i < a.length; i++)
	                result[i] = a[i];
	            results.add(result);
	            return;
	        }
	        for (int i = k; i < a.length; i++) {
	            int temp = a[k];
	            a[k]=a[i];
	            a[i]=temp;
	            getPermutations(a,k+1,results);
	            temp=a[k];
	            a[k]=a[i];
	            a[i]=temp;
	        }
	    }
	    public static int[] getRenamed(int[] source, int[] rename) {
	        int[] new_version = new int[source.length];
	        for( int i = 0; i < source.length; i++)
	            new_version[i] = rename[source[i]];
	        return new_version;
	    }
	    public static int getGenomeHammingDistance(int[] genome1, int[] genome2) {
	        int dist = 0;
	        for( int i = 0; i < genome1.length; i++)
	            dist += genome1[i] == genome2[i] ? 0 : 1;
	        return dist;
	    }
	
	    //constructors
	    public DistrictMap(Vector<Block> blocks, int num_districts, int[] genome) {
	        this(blocks,num_districts);
	        setGenome(genome);
	    }
	    public DistrictMap(Vector<Block> blocks, int num_districts) {
	        this.num_districts = num_districts;
	        this.blocks = blocks;
	        districts = new Vector<District>();
	        for( int i = 0; i < num_districts; i++)
	            districts.add(new District());
	        block_districts = new int[blocks.size()];
	        mutate(1);
	    }
	
	    //genetic evolution primary functions
	    public int[] getGenome() {
	        return block_districts;
	    }
	    public void setGenome(int[] genome) {
	        block_districts = genome;
	        districts = new Vector<District>();
	        for( int i = 0; i < num_districts; i++)
	            districts.add(new District());
	        for( int i = 0; i < genome.length; i++)
	            districts.get(genome[i]).blocks.add(blocks.get(i));
	    }

	    //helper functions
	    public double[][] getRandomResultSample() {
	        double[] popular_vote = new double[candidates.size()]; //inited to 0
	        double[] elected_vote = new double[candidates.size()]; //inited to 0
	        for(District district : districts) {
	            double[] district_vote = district.getVotes();
	            for( int i = 0; i < district_vote.length; i++) {
	            	popular_vote[i] += district_vote[i];
	            }
	            elected_vote[district.last_winner]++;
	        }
	        return new double[][]{popular_vote,elected_vote};
	    }
	
	    //calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
	    public double getKLDiv(double[] p, double[] q) {
	
	        //get totals
	        double totp = 0;
	        for( int i = 0; i < p.length; i++)
	            totp += p[i];  
	        double totq = 0;
	        for( int i = 0; i < q.length; i++)
	            totq += q[i];  
	
	        //make same ratio before regularizing.
	        double ratio = totp/totq;
	        for( int i = 0; i < q.length; i++)
	            q[i] *= ratio;  
	
	        //regularize (see "regularization" in statistics)
	        for( int i = 0; i < p.length; i++)
	            p[i]++;  
	        for( int i = 0; i < q.length; i++)
	            q[i]++;  
	
	        //normalize
	        totp = 0;
	        for( int i = 0; i < p.length; i++)
	            totp += p[i];  
	        for( int i = 0; i < p.length; i++)
	            p[i] /= totp;
	        totq = 0;
	        for( int i = 0; i < q.length; i++)
	            totq += q[i];  
	        for( int i = 0; i < q.length; i++)
	            q[i] /= totq;
	
	        //get kldiv
	        double div = 0;
	            for( int i = 0; i < q.length; i++)
	                div += -p[i]*(Math.log(q[i]) - Math.log(p[i]));
	        return div;
	    }
	
	    //returns total edge length, unfairness, population imbalance
	    //a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
	    public void calcFairnessScores(int trials) {
	        double length = getEdgeLength();
	        double total_population = 0;
	        double[] dist_pops = new double[districts.size()];
	        double[] dist_pop_frac = new double[districts.size()];
	        
	        for(int i = 0; i < districts.size(); i++) {
	        	District district = districts.get(i);
	        	district.resetWins();
	            dist_pops[i] = district.getPopulation();
	            total_population += dist_pops[i];
	        }
	        for(int i = 0; i < districts.size(); i++) {
	            dist_pop_frac[i] = 	dist_pops[i] / total_population;
	        }
	        
	        double exp_population = total_population/districts.size();
	        double[] perfect_dists = new double[districts.size()];
	        for( int i = 0; i < perfect_dists.length; i++)
	            perfect_dists[i] = exp_population;
	
	        //simulate trials elections and accumulate the results
	        double[] p = new double[candidates.size()];
	        double[] q = new double[candidates.size()];
	        for( int i = 0; i < trials; i++) {
	            double[][] results = getRandomResultSample();
	            for( int j = 0; j < candidates.size(); j++) {
	                p[j] += results[0][j];
	                q[j] += results[1][j];
	            }
	        }
	        
	        double[] voting_power = new double[districts.size()];
	        double total_voting_power = 0;
	        for(int i = 0; i < districts.size(); i++) {
	        	District district = districts.get(i);
	        	voting_power[i] = district.getSelfEntropy();
	        	total_voting_power += voting_power[i];
	        }
	        
	        for(int i = 0; i < districts.size(); i++) {
	        	voting_power[i] /= total_voting_power;
	        }
	        
	        double power_fairness = 0; //1 = perfect fairness
	        for(int i = 0; i < districts.size(); i++) {
	        	power_fairness += dist_pop_frac[i]*voting_power[i];
	        }
	        
	        double disconnected_pops = 0;
	        if( disconnected_population_weight > 0) {
	            for(District district : districts)
	                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(block_districts));
	        }
	        disconnected_pops /= total_population;
	        fairnessScores = new double[]{length,Math.exp(getKLDiv(p,q)),Math.exp(getKLDiv(perfect_dists,dist_pops)),disconnected_pops,power_fairness}; //exponentiate because each bit represents twice as many people disenfranched
	    }

		public int compareTo(DistrictMap o) {
			double d = (fitness_score-o.fitness_score)*sorting_polarity; 
			return  d > 0 ? 1 : d == 0 ? 0 : -1;
		}
	    double getEdgeLength() {
	        double length = 0;
	        Vector<Edge> outerEdges = getOuterEdges(block_districts);
	        for( Edge edge : outerEdges)
	            length += edge.length;
	        return length;
	    }
	    Vector<Edge> getOuterEdges(int[] block_districts) {
	        Vector<Edge> outerEdges = new Vector<Edge>();
	        for( Block block : blocks)
	            for( Edge edge : block.edges)
	                if( !edge.areBothSidesSameDistrict(block_districts))
	                    outerEdges.add(edge);
	        return outerEdges;
	    }
    }
	
	//buisness objects
	class District {
	    Vector<Block> blocks = new Vector<Block>();
	    public double[] wins;
	    public int last_winner = -1;
	    
	    double getPopulation() {
	        double pop = 0;
	        for( Block block : blocks)
	              pop += block.population;
	        return pop;
	    }
	    
	    public void resetWins() {
	    	if( blocks.size() == 0) {
	    		return;
	    	}
            wins = new double[blocks.get(0).prob_vote.length]; //inited to 0
	    }
	    public double getSelfEntropy() {
	    	double total = 0;
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
	    public static int getHighestIndex(double[] dd) {
            double most_value = 0;
            int most_index = 0;
            for( int i = 0; i < dd.length; i++) {
            	if( dd[i] > most_value) {
            		most_index = i;
            		most_value = dd[i];//district_vote[i];
            	}
            }
            return most_index;
	    }

	    public double[] getVotes() {
	    	if( blocks.size() == 0) {
	    		return null;
	    	}
            double[] district_vote = new double[blocks.get(0).prob_vote.length]; //inited to 0
            for( Block block : blocks) {
                double[] block_vote = block.getVotes();
                for( int i = 0; i < block_vote.length; i++) {//most_value) {
                	district_vote[i] += block_vote[i];
                }
            }
            last_winner = getHighestIndex(district_vote);
            wins[last_winner]++;

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
	        Hashtable<Block,Vector<Block>> region_hash = new Hashtable<Block,Vector<Block>>();
	        Vector<Vector<Block>> regions = new Vector<Vector<Block>>();
	        for( Block block : blocks) {
	            if( region_hash.get(block) != null)
	                continue;
	            Vector<Block> region = new Vector<Block>();
	            regions.add(region);
	            addAllConnected(block,region,region_hash,block_districts);
	        }
	        return regions;
	    }
	    //recursively insert connected blocks.
	    void addAllConnected( Block block, Vector<Block> region,  Hashtable<Block,Vector<Block>> region_hash, int[] block_districts) {
	        if( region_hash.get(block) != null)
	            return;
	        region.add(block);
	        region_hash.put(block,region);
	        for( Edge edge : block.edges)
	            if( edge.areBothSidesSameDistrict(block_districts))
	                addAllConnected( edge.block1 == block ? edge.block2 : edge.block1, region, region_hash, block_districts);
	    }
	    double getRegionPopulation(Vector<Block> region) {
	        double population = 0;
	        for( Block block : region)
	            population += block.population;
	        return population;
	    }
	
	
	}
	class Block {
	    int index;
	    double[] population;
	    double[] prob_turnout;
	    double[][] prob_vote = null;//new double[DistrictMap.candidates.size()];
	    double[] vote_cache = null;
	    double[][] vote_caches = null;
	    static boolean use_vote_caches = true;
	    static boolean use_vote_cache = true;
	    static int cache_reuse_times = 16;
	    static int vote_cache_size = 128;
	    int cache_reused = 0;
	    
	    Vector<Edge> edges = new Vector<Edge>();
	    double[] getVotes() {
	    	if( use_vote_caches) {
	    		if( vote_caches == null) {
	    			vote_caches = new double[vote_cache_size][];
	    			for( int i = 0; i < vote_caches.length; i++) {
	    				generateVotes();
	    				vote_caches[i] = vote_cache;
	    			}
	    		}
	    		return vote_caches[((int)Math.random()*(double)vote_caches.length)];
	    	} else if( vote_cache == null || cache_reused >= cache_reuse_times) {
    			generateVotes();
    			cache_reuse_times = 0;
    		}
    		cache_reuse_times++;
    		return vote_cache;
	    }
	    
	    void generateVotes() {
	        double[] votes = new double[prob_vote.length];
	        for(int i = 0; i < votes; i++) {
	        	votes[i] = 0;
	        }
	        for(int h = 0; h < population.length; h++) {
		        for(int i = 0; i < population[h]; i++) {
		        	double p = Math.random();
	        		if( p > prob_turnout[h]) {
	        			continue;
	        		}
		        	p = Math.random();
		        	for( int j = 0; j < prob_vote[h].length; j++) {
	        			p -= prob_vote[h][j];
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
	class Edge {
	    Block block1;
	    Block block2;
	    Vertex vertex1;
	    Vertex vertex2;
	    double length;
	    boolean areBothSidesSameDistrict(int[] block_districts) {
	        return block_districts[block1.index] == block_districts[block2.index];
	    }
	}
	class Vertex {
	}
    class Candidate {
    	int index;
    	String id;
    }

