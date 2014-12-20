package mapCandidates;

import java.util.*;

public class DistrictMap implements iEvolvable, Comparable<DistrictMap> {
	
    public static int sorting_polarity = 1;
    public static boolean use_border_length_on_mutate_boundary = true;

    public static int num_districts = 0;
	public Vector<Block> blocks = new Vector<Block>();
	public Vector<District> districts = new Vector<District>();
    
    public int[] block_districts = new int[]{};
    public double[] fairnessScores = new double[5];
    public double fitness_score = 0;
    
    public static double[] metrics = new double[10];
    
    double[] dist_pops;
    double[] dist_pop_frac;
    double[] perfect_dists;
	

    //makeLike
    //sfads

    //always find the most identical version before spawning new ones!
    //this dramatically reduces convergence time!
    public int[] getGenome(int[] baseline) {
    	for( int i = 0; i < block_districts.length; i++) {
    		while(block_districts[i] >= Settings.num_districts) {
    			block_districts[i] = (int)Math.floor(Math.random()*(double)Settings.num_districts);
    		}
    	}
    	/*
    	for( int i = 0; i < baseline.length; i++) {
    		while(baseline[i] >= Settings.num_districts) {
    			baseline[i] = (int)Math.floor(Math.random()*(double)Settings.num_districts);
    		}
    	}*/
    	int[][] counts = new int[Settings.num_districts][];
    	for( int i = 0; i < counts.length; i++) {
    		counts[i] = new int[Settings.num_districts];
        	for( int j = 0; j < counts.length; j++) {
        		counts[i][j] = 0;
            	for( int k = 0; k < block_districts.length; k++) {
            		if( block_districts[k] == i && baseline[k] == j) {
            			counts[i][j]++;
            		}
            	}
        	}
    	}
    	int[] best_subst = new int[Settings.num_districts];
    	int best_subst_matches = 0;
    	for( int i = 0; i < counts.length; i++) {
    		best_subst[i] = i;
    	}  

    	//now iterate through perms
        for(Iterator<int[]> it = new PermIterator(Settings.num_districts); it.hasNext(); ) {
        	int[] test_subst = it.next();
        	int matches = 0;
            for( int i = 0; i < test_subst.length; i++) {
            	matches += counts[i][test_subst[i]];
            }
            if( matches > best_subst_matches) {
            	best_subst_matches = matches;
            	best_subst = test_subst;
            }
        }

    	int[] new_baseline = new int[block_districts.length];
    	for( int i = 0; i < block_districts.length; i++) {
    		new_baseline[i] = best_subst[block_districts[i]];
    	}
    	
    	return new_baseline;
    	
    	
/*
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
         */
    }

    public void mutate(double prob) {
        double max = Settings.num_districts;
        for( int i = 0; i < block_districts.length; i++) {
            if( Math.random() < prob) {
                block_districts[i] = (int)(Math.floor(Math.random()*max));
                while( block_districts[i] > Settings.num_districts) {
                    block_districts[i] = (int)(Math.floor(Math.random()*max));
                }
            }
        }
    }

    public void mutate_boundary(double prob) {
        boolean[] allow = new boolean[districts.size()];
        for( int i = 0; i < block_districts.length; i++) {
            if( Math.random() < prob) {
            	try {
            		
            		if( use_border_length_on_mutate_boundary) {
                        Block block = blocks.get(i);
            			double total_length = 0;
            			for( int j = 0; j < block.neighbor_lengths.length; j++) {
            				total_length += block.neighbor_lengths[j];
            			}
            			double mutate_to = Math.random()*total_length;
               			for( int j = 0; j < block.neighbor_lengths.length; j++) {
            				mutate_to -= block.neighbor_lengths[j];
            				if( mutate_to < 0) {
            					Block b = block.neighbors.get(j);
            					block_districts[i] = block_districts[b.id];
            					break;
            				}
            			}
            			
            		} else {
                        for( int j = 0; j < allow.length; j++) {
                            allow[j] = false;
                        }
                        allow[block_districts[i]] = true;
                        Block block = blocks.get(i);
                        for( Block other_block : block.neighbors) {
                        	if( block_districts[other_block.id] < allow.length) {
                                allow[block_districts[other_block.id]] = true;
                        	}
                        }
                        double count = 0;
                        for( int j = 0; j < allow.length; j++) {
                            if( allow[j])
                                count++;
                        }
                        int d = (int)Math.floor(Math.random()*count); 
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
            	} catch (Exception ex) {
            		ex.printStackTrace();
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
    
    public void fillDistrictBlocks() {
    	for( District d : districts) {
    		d.blocks = new Vector<Block>();
    	}
		while( Settings.num_districts >= districts.size()) {
			districts.add(new District());
		}

    	for( int i = 0; i < block_districts.length; i++) {
    		int district = block_districts[i];
    		while( district >= districts.size()) {
    			districts.add(new District());
    		}
    		districts.get(district).blocks.add(blocks.get(i));
    	}
    	//make sure each district always has at least 1 block.
    	for( int i = 0; i < districts.size(); i++) {
    		District d = districts.get(i);
    		if( d.blocks.size() == 0) {
    			int num_to_get = blocks.size() / (districts.size());
    			if( num_to_get < 1) {
    				num_to_get = 1;
    			}
    			for( int k = 0; k < num_to_get; k++) {
        			int j = (int) (Math.random()*(double)blocks.size());
        			districts.get(block_districts[j]).blocks.remove(blocks.get(j));
        			block_districts[j] = i;
        			d.blocks.add(blocks.get(j));
    			}
    		}
    	}
    	for( int i = 0; i < districts.size(); i++) {
    		districts.get(i).resetPopulation();
    	}
    }
    public DistrictMap(Vector<Block> blocks, int num_districts) {
        this.num_districts = num_districts;
        this.blocks = blocks;
        //System.out.println(" districtmap constructor numdists "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        block_districts = new int[blocks.size()];
        mutate(1);
        fillDistrictBlocks();
        //System.out.println(" districtmap constructor dist size "+districts.size());
    }
    public void resize_districts(int target) {
        dist_pops = new double[Settings.num_districts];
        dist_pop_frac = new double[Settings.num_districts];
        perfect_dists = new double[Settings.num_districts];
    	
    	if( num_districts == target) {
    		return;
    	}
		while( districts.size() > target) {
			districts.remove(target);
		}
		while( districts.size() < target) {
			District d = new District();
			districts.add(d);
		}
		
    	if( num_districts > target) {
    		double d = target;
    		for( int i = 0; i < block_districts.length; i++) {
    			if( block_districts[i] >= target) {
    				int x = (int)Math.floor(Math.random() * d);
    				block_districts[i] = x;
    				while( block_districts[i] >= target) {
    	   				x = (int)Math.floor(Math.random() * d);
        				block_districts[i] = x;
        			}

    				districts.get(x).blocks.add(blocks.get(i));
    			}
    		}
    	}
        fillDistrictBlocks();
    	if( num_districts < target) {
    	}
    	num_districts = target;
        dist_pops = new double[Settings.num_districts];
        dist_pop_frac = new double[Settings.num_districts];
        perfect_dists = new double[Settings.num_districts];
    	//System.out.println( "resize_districts target "+target+" districts.size() "+districts.size()); 
    }


    //genetic evolution primary functions
    public int[] getGenome() {
        return block_districts;
    }
    public void setGenome(int[] genome) {
        block_districts = genome;
        System.out.println("setgenome districts "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        for( int i = 0; i < genome.length; i++)
            districts.get(genome[i]).blocks.add(blocks.get(i));
    }

    //helper functions
    public double[][] getStandardResult() {
    	//System.out.println("num dists "+districts.size());
    	
        double[] popular_vote = new double[Candidate.candidates.size()]; //inited to 0
        double[] elected_vote = new double[Candidate.candidates.size()]; //inited to 0
        for(District district : districts) {
            double[] district_vote = district.getVotes();
            int winner_num = -1;
            double winner_vote_count = -1;
            for( int i = 0; i < district_vote.length; i++) {
            	if( district_vote[i] > winner_vote_count) {
            		winner_vote_count = district_vote[i];
            		winner_num = i;
            	}
                popular_vote[i] += district_vote[i];
            }
            if( winner_num >= 0) {
            	elected_vote[winner_num]++;
            }
        }
        return new double[][]{popular_vote,elected_vote};
    }

    
    public double[][] getRandomResultSample() {
    	//System.out.println("num dists "+districts.size());
    	
        double[] popular_vote = new double[Candidate.candidates.size()]; //inited to 0
        double[] elected_vote = new double[Candidate.candidates.size()]; //inited to 0
        for(District district : districts) {
            double[] district_vote = district.getAnOutcome();
            int winner_num = -1;
            double winner_vote_count = -1;
            for( int i = 0; i < district_vote.length; i++) {
            	if( district_vote[i] > winner_vote_count) {
            		winner_vote_count = district_vote[i];
            		winner_num = i;
            	}
                popular_vote[i] += district_vote[i];
            }
            if( winner_num >= 0) {
            	elected_vote[winner_num]++;
            }
        }
        return new double[][]{popular_vote,elected_vote};
    }

    //calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
    public double getKLDiv(double[] p, double[] q, double regularization_factor) {

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
            p[i]+=regularization_factor;  
        for( int i = 0; i < q.length; i++)
            q[i]+=regularization_factor;  

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
        for( int i = 0; i < q.length; i++) {
        	double kl = p[i]*(Math.log(q[i]) - Math.log(p[i]));
        	//System.out.println("i: "+i+" \tp: "+p[i]+" \tq:"+q[i]+" \tkl:"+kl);
            div += kl;
        }
        return -div;
    }

    //returns total edge length, unfairness, population imbalance
    //a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
    //all measures should be minimized.
    public void calcFairnessScores(int trials) {
    	
    	long time0 = System.currentTimeMillis();    	
    	//===fairness score: compactness
        double length = getEdgeLength();
        
    	long time1 = System.currentTimeMillis();
    	//===fairness score: population balance
        double total_population = 0;
        //System.out.println("districts size " +districts.size());
        if( dist_pops == null || dist_pops.length != Settings.num_districts) {
            dist_pops = new double[Settings.num_districts];
            dist_pop_frac = new double[Settings.num_districts];
            perfect_dists = new double[Settings.num_districts];
        }
        for( int i = 0; i < dist_pops.length; i++) {
        	dist_pop_frac[i] = 0;
        }
        for( int i = 0; i < dist_pops.length; i++) {
        	dist_pop_frac[i] = 0;
        }
        for( int i = 0; i < dist_pops.length; i++) {
        	perfect_dists[i] = 0;
        }

        double population_imbalance = 0;
        if( Settings.population_balance_weight > 0 || Settings.voting_power_balance_weight > 0) {
            for(int i = 0; i < dist_pops.length; i++) {
            	if( districts.size() <= i) {
                    dist_pops[i] = 0;
            		
            	} else {
                    District district = districts.get(i);
                    dist_pops[i] = district.getPopulation();
            	}
                total_population += dist_pops[i];
            }
        	double rtotpop = 1.0/ total_population;
            for(int i = 0; i < dist_pops.length; i++) {
                dist_pop_frac[i] = dist_pops[i] * rtotpop;
            }

            double exp_population = total_population/(double)dist_pops.length;
            //System.out.println("exp. pop. "+exp_population);
            for( int i = 0; i < perfect_dists.length; i++) {
                perfect_dists[i] = exp_population;
            }
            population_imbalance = getKLDiv(perfect_dists,dist_pops,1);
        }

    	long time2 = System.currentTimeMillis();
    	long time20 = System.currentTimeMillis();
    	
        double[] p = new double[Candidate.candidates.size()];
        double[] q = new double[Candidate.candidates.size()];
        double disproportional_representation = 0;
    	if( Settings.disenfranchise_weight > 0 || Settings.voting_power_balance_weight > 0) {
    		for(District d : districts) {
    			d.generateOutcomes(Settings.num_elections_simulated);
    		}
    		
    	}
    	if( Settings.disenfranchise_weight > 0) {
    		//System.out.println("num t "+trials);
        	//===fairness score: proportional representation
    		
            for( int i = 0; i < Settings.num_elections_simulated; i++) {
                double[] popular_vote = new double[Candidate.candidates.size()]; //inited to 0
                double[] elected_vote = new double[Candidate.candidates.size()]; //inited to 0
                for(District district : districts) {
                	try {
                		int n = (int)Math.floor(Math.random()*(double)district.outcomes.length);
                        double[] district_vote = district.outcomes[n];
                        double[] pop_district_vote = district.pop_balanced_outcomes[n];
                        int winner_num = -1;
                        double winner_vote_count = -1;
                        for( int j = 0; j < district_vote.length; j++) {
                            popular_vote[j] += pop_district_vote[j];
                        	if( district_vote[j] > winner_vote_count) {
                        		winner_vote_count = district_vote[j];
                        		winner_num = j;
                        	}
                        }
                        if( winner_num >= 0) {
                        	elected_vote[winner_num]++;
                        }
                	} catch (Exception ex) {
                		break;
                	}
                }
                //double[][] results = new double[][]{popular_vote,elected_vote};
            	
                for( int j = 0; j < Candidate.candidates.size(); j++) {
                    p[j] += popular_vote[j];
                    q[j] += elected_vote[j];
                }
            }
            time20 = System.currentTimeMillis();
            disproportional_representation = getKLDiv(p,q,1);
    	}

    	long time3 = System.currentTimeMillis();
    	//===fairness score: power fairness
        double[] voting_power = new double[dist_pops.length];
        double total_voting_power = 0;
        double power_fairness = 0; //1 = perfect fairness
        if( Settings.voting_power_balance_weight > 0) {
        	while( districts.size() < dist_pops.length) {
        		districts.add(new District());
        	}
            for(int i = 0; i < dist_pops.length; i++) {
                District district = districts.get(i);
                voting_power[i] = district.getSelfEntropy(district.outcomes);
                total_voting_power += voting_power[i];
            }

            for(int i = 0; i < dist_pops.length; i++) {
                voting_power[i] /= total_voting_power;
            }
            power_fairness = getKLDiv(dist_pop_frac,voting_power,0.01);
/*
            for(int i = 0; i < districts.size(); i++) {
                power_fairness += dist_pop_frac[i]*voting_power[i];
            }*/
        }

    	long time4 = System.currentTimeMillis();
    	//===fairness score: connectedness
        double disconnected_pops = 0;
        if( Settings.disconnected_population_weight > 0) {
            for(District district : districts) {
            	//int count = district.getRegionCount(block_districts);
            	//System.out.println("region count: "+count);
            	//disconnected_pops += count;
                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(block_districts));
            }
        }
        disconnected_pops /= total_population;
        
    	long time5 = System.currentTimeMillis();

        
        fairnessScores = new double[]{length,disproportional_representation,population_imbalance,disconnected_pops,power_fairness}; //exponentiate because each bit represents twice as many people disenfranched
    	long time6 = System.currentTimeMillis();
    	metrics[0] += time1-time0;
    	metrics[1] += time2-time1;
    	metrics[2] += time3-time2;
    	metrics[3] += time4-time3;
    	metrics[4] += time5-time4;
    	metrics[5] += time6-time5;

    	metrics[6] += time20-time2;//time10-time1;
    	metrics[7] += 0;//time11-time10; //
    	metrics[8] += 0;//time12-time11;
    	metrics[9] += 0;//time2-time12;
    }

    public int compareTo(DistrictMap o) {
        double d = (fitness_score-o.fitness_score)*sorting_polarity; 
        return  d > 0 ? 1 : d == 0 ? 0 : -1;
    }
    double getEdgeLength() {
        double length = 0;
        for( Block b : blocks) {
        	int d1 = block_districts[b.id];
        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
        		int b2id = b.neighbors.get(i).id;
            	int d2 = block_districts[b2id];
            	if( d1 != d2) {
            		length += b.neighbor_lengths[i];
            	}
        	}
        	
        	
        }
        /*
        Vector<Edge> outerEdges = getOuterEdges(block_districts);
        for( Edge edge : outerEdges)
            length += edge.length;
        */
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
