package solutions;

import geography.*;

import java.util.*;

public class DistrictMap implements iEvolvable, Comparable<DistrictMap> {
	
    public static int sorting_polarity = 1;
    public static boolean use_border_length_on_mutate_boundary = true;

    public static int num_districts = 0;
	public Vector<Ward> wards = new Vector<Ward>();
	public Vector<District> districts = new Vector<District>();
    
    public int[] ward_districts = new int[]{};
    public double[] fairnessScores = new double[5];
    public double fitness_score = 0;
    
    public static double[] metrics = new double[10];
    
    double[] dist_pops;
    double[] dist_pop_frac;
    double[] perfect_dists;
    
    
	
	public boolean loadDistrictsFromProperties(FeatureCollection collection, String column_name) {
		boolean has_districts = true;
		for( int i = 0; i < collection.features.size(); i++) {
			Feature f = collection.features.get(i);
			if( !f.properties.containsKey(column_name)) {
				has_districts = false;
				ward_districts[i] = (int)(Math.random()*(double)Settings.num_districts);
			} else {
				ward_districts[i] = ((int)f.properties.getDouble(column_name))-1;
			}
		}
		fillDistrictwards();
		setGenome(ward_districts);
		fillDistrictwards();
		return has_districts;
	}
	
	public void storeDistrictsToProperties(FeatureCollection collection, String column_name) {
		for( int i = 0; i < collection.features.size(); i++) {
			Feature f = collection.features.get(i);
			f.properties.put(column_name, ward_districts[i]+1);
		}
	}
	
	public void randomizeDistricts() {
		resize_districts(Settings.num_districts);
		for( int i = 0; i < ward_districts.length; i++) {
			ward_districts[i] = (int)(Math.random()*(double)Settings.num_districts);
		}
		setGenome(ward_districts);
		fillDistrictwards();
	}

	

    //makeLike
    //sfads

    //always find the most identical version before spawning new ones!
    //this dramatically reduces convergence time!
    public int[] getGenome(int[] baseline) {
    	for( int i = 0; i < ward_districts.length; i++) {
    		while(ward_districts[i] >= Settings.num_districts) {
    			ward_districts[i] = (int)Math.floor(Math.random()*(double)Settings.num_districts);
    		}
    	}/*
    	int orig_matches = 0;
    	for( int i = 0; i < ward_districts.length; i++) {
			if( ward_districts[i] == baseline[i])
				orig_matches++;
    	}*/
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
            	for( int k = 0; k < ward_districts.length; k++) {
            		if( ward_districts[k] == i && baseline[k] == j) {
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
            	//matches += counts[test_subst[i]][i];
            }
            if( matches > best_subst_matches) {
            	best_subst_matches = matches;
                for( int i = 0; i < test_subst.length; i++) {
                	best_subst[i] = test_subst[i];
                }
            }
        }
        
        /*
        for( int i = 0; i < best_subst.length; i++) {
        	System.out.print(best_subst[i]);
        }
        System.out.print(" ");
*/
    	int[] new_baseline = new int[ward_districts.length];
    	for( int i = 0; i < ward_districts.length; i++) {
    		new_baseline[i] = best_subst[ward_districts[i]];
    	}
    	/*
    	int new_matches = 0;
    	for( int i = 0; i < new_baseline.length; i++) {
    			if( new_baseline[i] == baseline[i])
    				new_matches++;
    	}*/
    	//System.out.println("orig: "+orig_matches+" new: "+new_matches+" expected: "+best_subst_matches+" improv: "+(new_matches-orig_matches));

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
        for( int i = 0; i < ward_districts.length; i++) {
            if( Math.random() <= prob) {
                ward_districts[i] = (int)(Math.floor(Math.random()*max));
                while( ward_districts[i] >= Settings.num_districts) {
                    ward_districts[i] = (int)(Math.floor(Math.random()*max));
                }
            }
        }
    }
    
    int boundaries_tested = 2;
    int boundaries_mutated = 1;

    public void mutate_boundary(double prob) {
    	//start at 1 for regularization
    	boundaries_tested = 0;
    	boundaries_mutated = 0;
        for( int i = 0; i < ward_districts.length; i++) {
        	Ward ward = wards.get(i);
        	boolean border = false;
            for( Ward bn : ward.neighbors) {
            	if( ward_districts[bn.id] != ward_districts[i]) {
            		border = true;
            		break;
            	}
            }
            if( border == false) {
            	continue;
            }
            boundaries_tested++;
            if( Math.random() < prob) {
            	boundaries_mutated++;
            	try {
          			double total_length = 0;
        			for( int j = 0; j < ward.neighbor_lengths.length; j++) {
        				total_length += ward.neighbor_lengths[j];
        			}
        			double mutate_to = Math.random()*total_length;
           			for( int j = 0; j < ward.neighbor_lengths.length; j++) {
        				mutate_to -= ward.neighbor_lengths[j];
        				if( mutate_to < 0) {
        					Ward b = ward.neighbors.get(j);
        					ward_districts[i] = ward_districts[b.id];
        					break;
        				}
        			}
            	} catch (Exception ex) {
            		ex.printStackTrace();
            	}
            }
        }
        if( boundaries_tested == 0) {
        	boundaries_tested++;
        }
        if( boundaries_mutated == 0) {
        	boundaries_mutated+=2;
        	boundaries_tested+=4;
        }
    }
    
    public void crossover(int[] genome1, int[] genome2) {
        for( int i = 0; i < ward_districts.length; i++) {
            double r = Math.random();
            if( Settings.pso ) {
            	ward_districts[i] = r < 0.333333333333 ? genome1[i] : r < 0.6666666666666 ? genome2[i] : Ecology.bestMap.getGenome()[i];
            } else {
            	ward_districts[i] = r < 0.5 ? genome1[i] : genome2[i];
            }
        }
    }
    
    public void makeLike(int[] genome) {
    	setGenome(getGenome(genome));
    	/*
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
        */
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
    public DistrictMap(Vector<Ward> wards, int num_districts, int[] genome) {
        this(wards,num_districts);
        setGenome(genome);
    }
    
    public void fillDistrictwards() {
    	for( District d : districts) {
    		d.wards = new Vector<Ward>();
    	}
		while( Settings.num_districts > districts.size()) {
			districts.add(new District());
		}

    	for( int i = 0; i < ward_districts.length; i++) {
    		int district = ward_districts[i];
    		if( district >= Settings.num_districts) {
    			while( district  >= Settings.num_districts) {
    				district = (int)Math.floor(Math.random()*(double)Settings.num_districts);
    			}
    			ward_districts[i] = district;
    		}
    		while( district >= districts.size()) {
    			districts.add(new District());
    		}
    		districts.get(district).wards.add(wards.get(i));
    	}
    	//make sure each district always has at least 1 ward.
    	for( int i = 0; i < districts.size() && i < Settings.num_districts; i++) {
    		District d = districts.get(i);
    		if( d.wards.size() == 0) {
    			int num_to_get = wards.size() / (districts.size());
    			if( num_to_get < 1) {
    				num_to_get = 1;
    			}
    			for( int k = 0; k < num_to_get; k++) {
        			int j = (int) (Math.random()*(double)wards.size());
        			districts.get(ward_districts[j]).wards.remove(wards.get(j));
        			ward_districts[j] = i;
        			d.wards.add(wards.get(j));
    			}
    		}
    	}
    	for( int i = 0; i < districts.size(); i++) {
    		districts.get(i).resetPopulation();
    	}
		while( Settings.num_districts < districts.size()) {
			districts.remove(Settings.num_districts);
		}

    }
    public DistrictMap(Vector<Ward> wards, int num_districts) {
        this.num_districts = num_districts;
        this.wards = wards;
        //System.out.println(" districtmap constructor numdists "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        ward_districts = new int[wards.size()];
        mutate(1);
        fillDistrictwards();
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
    		for( int i = 0; i < ward_districts.length; i++) {
    			if( ward_districts[i] >= target) {
    				int x = (int)Math.floor(Math.random() * d);
    				ward_districts[i] = x;
    				while( ward_districts[i] >= target) {
    	   				x = (int)Math.floor(Math.random() * d);
        				ward_districts[i] = x;
        			}

    				districts.get(x).wards.add(wards.get(i));
    			}
    		}
    	}
        fillDistrictwards();
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
        return ward_districts;
    }
    public void setGenome(int[] genome) {
        ward_districts = genome;
        //System.out.println("setgenome districts "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        for( int i = 0; i < genome.length; i++)
            districts.get(genome[i]).wards.add(wards.get(i));
    }

    //helper functions
    /*
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
    */

    /*
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
    */

    //calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
    public double getKLDiv(double[] p, double[] q, double regularization_factor) {
    	boolean verbose = false;
    	if( regularization_factor == 1.2 || regularization_factor == 0.01) {
    		if( false) {
    			verbose = true;
    			System.out.println(" reg: "+regularization_factor);
    		}
    		//regularization_factor = 1;
    	}
    	if( verbose) {
            for( int i = 0; i < p.length; i++) {
            	System.out.println(" "+i+" p: "+p[i]+" q: "+q[i]);
            }
    		
    	}
        //regularize (see "regularization" in statistics)
        for( int i = 0; i < p.length; i++)
            p[i]+=regularization_factor;  
        for( int i = 0; i < q.length; i++)
            q[i]+=regularization_factor;  
        
        //get totals
        double totp = 0;
        for( int i = 0; i < p.length; i++)
            totp += p[i];  
        double totq = 0;
        for( int i = 0; i < q.length; i++)
            totq += q[i];  

        //make same ratio.
        double ratio = totp/totq;
        for( int i = 0; i < q.length; i++)
            q[i] *= ratio;  


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
        	if( p[i] == 0) {
        		continue;
        	}
        	double kl = p[i]*(Math.log(q[i]) - Math.log(p[i]));
        	if( verbose) {
        		System.out.println("i: "+i+" \tp: "+p[i]+" \tq:"+q[i]+" \tkl:"+kl);
        	}
            div += kl;
        }
        return -div;
    }

    //returns total edge length, unfairness, population imbalance
    //a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
    //all measures should be minimized.
    public void calcFairnessScores() {//border_length_area_weighted
    	
    	long time0 = System.currentTimeMillis();    	
    	//===fairness score: compactness
        double length = Settings.border_length_area_weighted ? getWeightedEdgeLength() : getEdgeLength();
        
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
                double[][] residues = new double[districts.size()][];
                double[] pops = new double[districts.size()];
                for(int k = 0; k < districts.size(); k++) {
                	District district = districts.get(k);
                	double[][] res = district.getElectionResults();
                	pops[k] = res[4][0];
                	residues[k] = res[3];
                	for( int j = 0; j < popular_vote.length; j++) {
                		popular_vote[j] += res[0][j];
                		elected_vote[j] += res[1][j];
                	}
                }
                
                double[][] prop_rep_results = District.getPropRepOutcome(popular_vote,Settings.members_per_district*Settings.num_districts);
                double[] ideal_vote = prop_rep_results[0];
                int max_diff = -1, min_diff = -1;
                double max_diff_amt = -1, min_diff_amt = 1;
            	for( int j = 0; j < popular_vote.length; j++) {
            		ideal_vote[j] -= elected_vote[j];
            		if( ideal_vote[j] < min_diff_amt || min_diff < 0) {
            			min_diff = j;
            			min_diff_amt = ideal_vote[j];
            		}
            		if( ideal_vote[j] > max_diff_amt || max_diff < 0) {
            			max_diff = j;
            			max_diff_amt = ideal_vote[j];
            		}
            	}
            	// min = negative = overrepresented
            	// max = positive = underepresented
            	double max = 0;
            	double min = 0;
            	if( max_diff_amt > 0) {
            		for( int j = 0; j < residues.length; j++) {
            			double amt = residues[j][max_diff] / (pops[j] == 0 || pops[j] != pops[j] ? 1 : pops[j]);
            			if( amt != amt || amt > 1) {
            				System.out.println("bad residue! "+amt);
            				amt = 0; 
            			}
            			if( amt > max) {
            				amt = max;
            			}
            			double amt2 = residues[j][min_diff] / (pops[j] == 0 || pops[j] != pops[j] ? 1 : pops[j]);
            			if( amt2 != amt2 || amt2 > 1) {
            				System.out.println("bad residue! "+amt2);
            				amt2 = 0; 
            			}
            			if( amt2 < min) {
            				amt2 = min;
            			}
            		}
            		double avg = (Math.abs(max)+Math.abs(min))/2;
            		elected_vote[max_diff] += avg;
            		elected_vote[min_diff] -= avg;
            	}
            	

                for( int j = 0; j < Candidate.candidates.size(); j++) {
                    p[j] += popular_vote[j];
                    q[j] += elected_vote[j];
                }
            }
            time20 = System.currentTimeMillis();
            disproportional_representation = getKLDiv(p,q,1.2);
            if( disproportional_representation != disproportional_representation) {
            	System.out.println("disproportional_representation not a number");
            }
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
                voting_power[i] = district.getSelfEntropy(district.getElectionResults()[Settings.self_entropy_use_votecount?2:1]);
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
            	//int count = district.getRegionCount(ward_districts);
            	//System.out.println("region count: "+count);
            	//disconnected_pops += count;
                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(ward_districts));
            }
        }
        //disconnected_pops /= total_population;
        
    	long time5 = System.currentTimeMillis();
    	
        
        fairnessScores = new double[]{length,disproportional_representation,getPopVariance()/*population_imbalance*2.0*/,disconnected_pops,power_fairness}; //exponentiate because each bit represents twice as many people disenfranched
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
    public double getMaxPopDiff() {
    	double min = -1;
    	double max = -1;
        for(District district : districts) {
        	double pop = district.getPopulation();
        	if( min < 0 || pop < min) {
        		min = pop;
        	}
        	if( max < 0 || pop > max) {
        		max = pop;
        	}
        }
        double d1 = max/min-1.0;
        double d2 = 1.0-min/max;
        return d1 > d2 ? d1 : d2;
    }
    public double getPopVariance() {
    	double tot = 0;
    	double tot2 = 0;
        for(District district : districts) {
        	double pop = district.getPopulation();
        	tot += pop;
        	tot2 += pop*pop;
        }
        tot /= (double) districts.size();
        tot2 /= (double) districts.size();
        return Math.sqrt(tot2-tot*tot);
    }

    public int compareTo(DistrictMap o) {
        double d = (fitness_score-o.fitness_score)*sorting_polarity; 
        return  d > 0 ? 1 : d == 0 ? 0 : -1;
    }
    double getWeightedEdgeLength() {
    	double[] lengths = new double[districts.size()]; 
    	double[] areas = new double[districts.size()]; 
        for( Ward b : wards) {
        	int d1 = ward_districts[b.id];
        	areas[d1] += b.area;
        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
        		int b2id = b.neighbors.get(i).id; 
            	int d2 = ward_districts[b2id];
            	if( d1 != d2) {
            		lengths[d1] += b.neighbor_lengths[i];
            		lengths[d2] += b.neighbor_lengths[i];
            	}
        	}
        }
        
        double weighted_sum = 0;
        for( int i = 0; i < lengths.length; i++) {
        	//weighted_sum += lengths[i] / Math.sqrt(areas[i]);
        	double isop = (lengths[i]*lengths[i]) / (4.0*Math.PI*areas[i]);
        	weighted_sum += Settings.square_root_compactness ? Math.sqrt(isop) : isop;
        }
        return 1.0 - 1.0/(weighted_sum / (double)lengths.length);
    }
    
    double getEdgeLength() {
        double length = 0;
        for( Ward b : wards) {
        	int d1 = ward_districts[b.id];
        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
        		int b2id = b.neighbors.get(i).id;
            	int d2 = ward_districts[b2id];
            	if( d1 != d2) {
            		length += b.neighbor_lengths[i];
            	}
        	}
        	
        	
        }
        return length;
    }
    Vector<Edge> getOuterEdges(int[] ward_districts) {
        Vector<Edge> outerEdges = new Vector<Edge>();
        for( Ward ward : wards)
            for( Edge edge : ward.edges)
                if( !edge.areBothSidesSameDistrict(ward_districts))
                    outerEdges.add(edge);
        return outerEdges;
    }
}
