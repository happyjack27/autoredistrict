/*
 * 

			double[] pop_by_dem = new double[dem_col_names.length];
			for( int i = 0; i < pop_by_dem.length; i++) { pop_by_dem[i] = 0; }
			double[] votes_by_dem = new double[dem_col_names.length];
			for( int i = 0; i < votes_by_dem.length; i++) { votes_by_dem[i] = 0; }
			double[] vote_margins_by_dem = new double[dem_col_names.length];



 * 
 * 
 * 
 */

package solutions;

import geography.*;
import ui.MainFrame;

import java.awt.Color;
import java.util.*;

public class DistrictMap implements iEvolvable, Comparable<DistrictMap> {
	
	public double area_multiplier = 1;//Math.pow(100,2)*10;
	
    public static int sorting_polarity = 1;
    public static boolean use_border_length_on_mutate_boundary = true;
    public Vector<double[]> seats_votes = new Vector<double[]>();  
    
    public static int num_districts = 0;
	public Vector<VTD> vtds = new Vector<VTD>();
	public Vector<District> districts = new Vector<District>();
    
    public int[] vtd_districts = new int[]{};
    public double[] fairnessScores = new double[11];
    public double fitness_score = 0;
    
    public static double[] metrics = new double[10];
    
    double[] dist_pops;
    double[] dist_pop_frac;
    double[] perfect_dists;
    
    public int[] wasted_votes_by_party;
    public int[] wasted_votes_by_district;
    public int[][] wasted_votes_by_district_and_party;
    public int[] vote_gap_by_district;
    public int total_vote_gap = 0;
    
    
    public void invalidate() {
    	for(District d : districts) {
    		d.invalidate();
    	}
    }
    
	
	public boolean loadDistrictsFromProperties(FeatureCollection collection, String column_name) {
		boolean has_districts = true;
		boolean zero_indexed = false;
		for( int i = 0; i < collection.features.size(); i++) {
			try {
				Feature f = collection.features.get(i);
				if( !f.properties.containsKey(column_name)) {
					System.out.println("key missing");
				} else {
					try {
						if( Integer.parseInt((String)f.properties.get(column_name)) == 0) {
							zero_indexed = true;
						}
					} catch (Exception ex) {
						System.out.println("parse error "+ex);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for( int i = 0; i < collection.features.size(); i++) {
			try {
				Feature f = collection.features.get(i);
				if( !f.properties.containsKey(column_name)) {
					System.out.println("district missing ("+column_name+"), randomizing");
					has_districts = false;
					vtd_districts[i] = (int)(Math.random()*(double)Settings.num_districts);
				} else {
					vtd_districts[i] = Integer.parseInt((String)f.properties.get(column_name))-(zero_indexed ? 0 : 1);//((int)f.properties.getDouble(column_name))-(zero_indexed ? 0 : 1);
				}
			} catch (Exception ex) {
				System.out.println("parse error2 "+ex);
			}
		}
		fillDistrictwards();
		try {
			setGenome(vtd_districts);
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		fillDistrictwards();
		return has_districts;
	}
	
	public void storeDistrictsToProperties(FeatureCollection collection, String column_name) {
		for( int i = 0; i < collection.features.size(); i++) {
			Feature f = collection.features.get(i);
			f.properties.put(column_name, vtd_districts[i]+1);
		}
	}
	
	public void randomizeDistricts() {
		resize_districts(Settings.num_districts);
		for( int i = 0; i < vtd_districts.length; i++) {
			vtd_districts[i] = (int)(Math.random()*(double)Settings.num_districts);
		}
		setGenome(vtd_districts);
		fillDistrictwards();
	}

	

    //makeLike
    //sfads

    //always find the most identical version before spawning new ones!
    //this dramatically reduces convergence time!
    public int[] getGenome(int[] baseline) {
    	for( int i = 0; i < vtd_districts.length; i++) {
    		while(vtd_districts[i] >= Settings.num_districts) {
    			vtd_districts[i] = (int)Math.floor(Math.random()*(double)Settings.num_districts);
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
            	for( int k = 0; k < vtd_districts.length; k++) {
            		if( vtd_districts[k] == i && baseline[k] == j) {
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
    	int[] new_baseline = new int[vtd_districts.length];
    	for( int i = 0; i < vtd_districts.length; i++) {
    		new_baseline[i] = best_subst[vtd_districts[i]];
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
    	reciprocal_iso_quotient = -1;
        double max = Settings.num_districts;
        for( int i = 0; i < vtd_districts.length; i++) {
        	/*
        	if( Settings.mutate_excess_pop) {
        		if(districts.get(vtd_districts[i]).excess_pop < 0) {
        			continue;
        		}
        	}
        	*/
            if( Math.random() <= prob) {
                vtd_districts[i] = (int)(Math.floor(Math.random()*max));
                while( vtd_districts[i] >= Settings.num_districts) {
                    vtd_districts[i] = (int)(Math.floor(Math.random()*max));
                }
            }
        }
        if( Settings.mutate_disconnected && prob >= 0) { 
        	mutate_all_disconnected(0.5);//prob > 0.1  ? (prob > 0.1 ? 0.1 : prob) : 0.05);
        }
    }
    public void mutate_all_disconnected(double prob) {
        int connected = 0;
        int disconnected = 0;
    	try {
	        boolean[] ward_connected = new boolean[vtd_districts.length];
	        for( int i = 0; i < vtd_districts.length; i++) {
	        	ward_connected[i] = false;
	        }
	        for( int i = 0; i < districts.size(); i++) {
	        	Vector<VTD> vw = districts.get(i).getTopPopulationRegion(vtd_districts);
	        	double pop = districts.get(i).getRegionPopulation(vw);
	        	if( pop < 20000 || vw == null || vw.size() < 5) {
	        		for(int j = 0; j < vtd_districts.length; j++) {
	        			ward_connected[j] |=  vtd_districts[j] == i;
	        		}
	        	} else {
		        	for( VTD w : vw) {
		        		ward_connected[w.id] = true;
		        	}
	        	}
	        }
	        for( int i = 0; i < vtd_districts.length; i++) {
	        	if( ward_connected[i] == false) {
	        		disconnected++;
	        	} else {
	        		connected++;
	        	}
	        }
	        if( disconnected > (disconnected+connected)*Settings.mutate_disconnected_threshold) {
        		//System.out.println("disconnected exceeds connected!");
	        	return;
	        }
    		mutating_disconnected = true;
    		reciprocal_iso_quotient = -1;
	        for( int i = 0; i < vtd_districts.length; i++) {
	        	if( ward_connected[i] == false) {
	        		mutate_ward_boundary(i,prob,false);
	        	} else {
	        	}
	        }
    		mutating_disconnected = false;
	    } catch (Exception ex) {
	    	System.out.println("ex mac "+ex);
	    	ex.printStackTrace();
	    }
        //System.out.println("connected: "+connected+" disconnected: "+disconnected);
    }    
    
    int boundaries_tested = 1;
    int boundaries_mutated = 0;
    int mutations_rejected = 0;
    boolean mutating_disconnected = false;
    public void mutate_ward_boundary(int i, double prob, boolean count) {
    	if( FeatureCollection.locked_wards[i]) {
    		return;
    	}
    	VTD ward = vtds.get(i);
    	boolean border = false;
        for( VTD bn : ward.neighbors) {
        	if( vtd_districts[bn.id] != vtd_districts[i]) {
        		border = true;
        		break;
        	}
        }
        if( border == false) {
        	return;
        }
        if( count)
        	boundaries_tested++;
        if( prob >= 1 || Math.random() < prob) {
            if( count)
            	boundaries_mutated++;
        	try {
      			double total_length = 0;
    			for( int j = 0; j < ward.neighbor_lengths.length; j++) {
    				total_length += ward.neighbor_lengths[j];
    			}
    			double mutate_to = Math.random()*total_length;
				int num_failures = 0;
				double from_paired_delta = 0;
				double from_unpaired_delta = 0;
				double from_area_delta = 0;
				double to_paired_delta = 0;
				double to_unpaired_delta = 0;
				double to_area_delta = 0;
				double from_next_iso = 0;
				double to_next_iso = 0; 
       			for( int j = 0; j < ward.neighbor_lengths.length; j++) {
    				mutate_to -= ward.neighbor_lengths[j];
    				if( mutate_to < 0) {
    					VTD b = ward.neighbors.get(j);
    					int d1 = vtd_districts[i];
    					int d2 = vtd_districts[b.id];
    					if( d1 >= districts.size()) {
    						d1 = (int)(Math.random()*(double)districts.size());
    					}
    					if( d2 >= districts.size()) {
    						d2 = (int)(Math.random()*(double)districts.size());
    					}
    					District dfrom = this.districts.get(d1); //coming from
						District dto = this.districts.get(d2); //going to
						
				    	//don't mutate to uncontested
						if( Settings.ignore_uncontested && FeatureCollection.buncontested1.length > vtd_districts[b.id] && FeatureCollection.buncontested1[vtd_districts[b.id]] ) {
				    		continue;
				    	}
						
    					
	   					if( !mutating_disconnected) {
    						if( Settings.mutate_excess_pop || Settings.mutate_good) {
	    						double cur_delta = Math.abs(districts.get(vtd_districts[i]).excess_pop - districts.get(vtd_districts[b.id]).excess_pop);
				        		double new_delta = Math.abs(
				        				(districts.get(vtd_districts[i]).excess_pop-ward.population/Settings.seats_in_district(vtd_districts[i])) 
				        				- (districts.get(vtd_districts[b.id]).excess_pop+ward.population/Settings.seats_in_district(vtd_districts[i])));
	    						if( new_delta > cur_delta) {
	    							if( Settings.mutate_good) {
	    								num_failures++;
	    							} else {
	    								mutations_rejected++;
	    								break;
	    							}
	    						}
				        		/*if(districts.get(vtd_districts[i]).excess_pop < districts.get(vtd_districts[b.id]).excess_pop) {
				        			break;
				        		}*/
				        	}
	    					/*
	    					if( Settings.mutate_overpopulated) {
	    						if( districts.get(vtd_districts[i]).excess_pop <= 0) {    							
	        						if( districts.get(vtd_districts[b.id]).excess_pop >= 0) {
	        							break;
	        						}        						
	    						}
	    						
	    					}*/
	    					if( Settings.mutate_competitive || Settings.mutate_good) {
	    						double[] o1 = this.districts.get(vtd_districts[i]).getAnOutcome(); //coming from
	    						double[] o2 = this.districts.get(vtd_districts[b.id]).getAnOutcome(); //going to
	    						double[] diff = this.vtds.get(i).getOutcome();
	    						double tot_now = 0;
	    						double tot_next = 0;
	    						for( int k = 0; k < o1.length; k++) {
	    							tot_now += Math.abs(o1[k]-o2[k]);
	    							o1[k] -= diff[k];
	    							o2[k] += diff[k];
	    							tot_next += Math.abs(o1[k]-o2[k]);
	    						}
	    						if( tot_next > tot_now) {
	    							if( Settings.mutate_good) {
	    								num_failures++;
	    							} else {
	    								mutations_rejected++;
	    								break;
	    							}
	    						}
	    					}
	    					if( Settings.mutate_compactness || (Settings.mutate_good && Settings.mutate_compactness_working)) {
	    						double length_from = 0;
	    						double length_to = 0;
	    						double length_other = 0;
	    						double length_unpaired = ward.unpaired_edge_length;
	    						VTD vtd = vtds.get(i);
	    						for( int k = 0; k < vtd.neighbors.size(); k++) {
	    							int neighbor_id = vtd_districts[vtd.neighbors.get(k).id];
	    							int neighbor_district = neighbor_id < 0 ? -1 : vtd_districts[neighbor_id];
	    							if( neighbor_district == vtd_districts[i]) {
	    								length_from += vtd.neighbor_lengths[k];
	    							}
	    							if( neighbor_district == vtd_districts[b.id]) {
	    								length_to += vtd.neighbor_lengths[k];
	    							}
	    							if( neighbor_district < 0) {
	    								length_unpaired += vtd.neighbor_lengths[k];
	    							}
	    							if( neighbor_district != vtd_districts[b.id] && neighbor_district != vtd_districts[i] && neighbor_district >= 0) {
	    								length_other += vtd.neighbor_lengths[k];
	    							}
	    						}
	    						
	    						from_paired_delta = -length_other-length_to+length_from;
	    						from_unpaired_delta = -length_unpaired;
	    						from_area_delta = -vtd.area;
	    						
	    						to_paired_delta = +length_other-length_to+length_from;
	    						to_unpaired_delta = length_unpaired;
	    						to_area_delta = vtd.area;
	    						
	    						from_next_iso = DistrictMap.iso_quotient(dfrom.area + from_area_delta, dfrom.paired_edge_length + from_paired_delta, dfrom.unpaired_edge_length + from_unpaired_delta);
	    						to_next_iso = DistrictMap.iso_quotient(dto.area + to_area_delta, dto.paired_edge_length + to_paired_delta, dto.unpaired_edge_length + to_unpaired_delta); 

	    						double tot_now = 1.0/dfrom.iso_quotient + 1.0/dto.iso_quotient; 
	    						double tot_next = 1.0/from_next_iso + 1.0/to_next_iso;
	    						
	    						//need to make sure that compactness has already been calculated!
	    						//if good, need to update.
	    						if( tot_next > tot_now) {
	    							if( Settings.mutate_good) {
	    								num_failures++;
	    							} else {
	    								mutations_rejected++;
	    								break;
	    							}
	    						}
	    					}
	    				}
	   					if( Settings.mutate_good && num_failures == (Settings.mutate_compactness_working ? 3 : 2)) {
							mutations_rejected++;
	   						break;
	   					}
	   					if( !mutating_disconnected && (Settings.mutate_good || Settings.mutate_compactness)) {
	   						dfrom.paired_edge_length += from_paired_delta;
	   						dfrom.unpaired_edge_length += from_unpaired_delta;
	   						dfrom.area += from_area_delta;
	   						dfrom.edge_length = this.edge_length(dfrom.paired_edge_length, dfrom.unpaired_edge_length);
	   						dfrom.iso_quotient = this.iso_quotient(dfrom.area, dfrom.paired_edge_length, dfrom.unpaired_edge_length);

	   						dto.paired_edge_length += to_paired_delta;
	   						dto.unpaired_edge_length += to_unpaired_delta;
	   						dto.area += to_area_delta;
	   						dto.edge_length = this.edge_length(dto.paired_edge_length, dto.unpaired_edge_length);
	   						dto.iso_quotient = this.iso_quotient(dto.area, dto.paired_edge_length, dto.unpaired_edge_length);

	   					}
    					districts.get(vtd_districts[i]).vtds.remove(ward);
    					districts.get(vtd_districts[i]).excess_pop -= ward.population;
    					
    					vtd_districts[i] = vtd_districts[b.id];
    					
    					districts.get(vtd_districts[i]).vtds.add(ward);
    					districts.get(vtd_districts[i]).excess_pop += ward.population;
    					
    					break;
    				}
    			}
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
    }

    public void mutate_boundary(double prob) {
    	reciprocal_iso_quotient = -1;
    	if( Settings.mutate_compactness || Settings.mutate_overpopulated || Settings.mutate_competitive || Settings.mutate_good) {
    		fillDistrictwards();
    	}
    	if( Settings.mutate_compactness || Settings.mutate_good) {
    		getReciprocalIsoPerimetricQuotient();
    	}
    	//start at 1 for regularization
    	boundaries_tested = 0;
    	boundaries_mutated = 0;
		mutations_rejected = 0;
        for( int i = 0; i < vtd_districts.length; i++) {
        	mutate_ward_boundary(i,prob,true);
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
    	reciprocal_iso_quotient = -1;
        for( int i = 0; i < vtd_districts.length; i++) {
            double r = Math.random();
            if( Settings.pso ) {
            	vtd_districts[i] = r < 0.333333333333 ? genome1[i] : r < 0.6666666666666 ? genome2[i] : Ecology.bestMap.getGenome()[i];
            } else {
            	vtd_districts[i] = r < 0.5 ? genome1[i] : genome2[i];
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
    public DistrictMap(Vector<VTD> wards, int num_districts, int[] genome) {
        this(wards,num_districts);
        setGenome(genome);
    }
    
    public void fillDistrictwards() {
    	for( District d : districts) {
    		d.vtds = new Vector<VTD>();
    	}
		while( Settings.num_districts > districts.size()) {
			districts.add(new District());
		}

    	for( int i = 0; i < vtd_districts.length; i++) {
    		int district = vtd_districts[i];
    		if( district >= Settings.num_districts) {
    			while( district  >= Settings.num_districts) {
    				district = (int)Math.floor(Math.random()*(double)Settings.num_districts);
    			}
    			vtd_districts[i] = district;
    		}
    		while( district >= districts.size()) {
    			districts.add(new District());
    		}
    		districts.get(district).vtds.add(vtds.get(i));
    	}
		while( Settings.num_districts < districts.size()) {
			districts.remove(Settings.num_districts);
		}
    	//make sure each district always has at least 1 ward.
    	for( int i = 0; i < districts.size() && i < Settings.num_districts; i++) {
    		District d = districts.get(i);
    		if( d.vtds.size() == 0) {
    			int num_to_get = vtds.size() / (districts.size());
    			if( num_to_get < 1) {
    				num_to_get = 1;
    			}
    			for( int k = 0; k < num_to_get; k++) {
        			int j = (int) (Math.random()*(double)vtds.size());
        			districts.get(vtd_districts[j]).vtds.remove(vtds.get(j));
        			vtd_districts[j] = i;
        			d.vtds.add(vtds.get(j));
    			}
    		}
    	}
    	
    	//make sure population >= 10.
    	if( districts.size() > 1000) {
    		System.out.println("too many districts "+districts.size());
    	} else {
    		int total = 0;
	    	for( int i = 0; i < districts.size()  && i < Settings.num_districts; i++) {
	    		districts.get(i).resetPopulation();
	    		int pop = (int)districts.get(i).getPopulation();
	    		if( pop <= 10) {
	    			int num = vtd_districts.length / Settings.num_districts;
	    			System.out.println("pop below 10 ("+pop+") for district "+i+" assigning "+num+" vtds");
	    			for( int j = 0; j < num; j++) {
	    				int m = (int)(Math.random()*(double)vtd_districts.length);
	    				vtd_districts[m] = i;
	    			}
	        		districts.get(i).resetPopulation();
	    		}
	    	}
    	}

    
    }
    public DistrictMap(Vector<VTD> wards, int num_districts) {
        this.num_districts = num_districts;
        this.vtds = wards;
        //System.out.println(" districtmap constructor numdists "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        vtd_districts = new int[wards.size()];
        mutate(1);
        fillDistrictwards();
        //System.out.println(" districtmap constructor dist size "+districts.size());
    }
    public void resize_districts(int target) {
    	//System.out.println("1");
        dist_pops = new double[Settings.num_districts];
        dist_pop_frac = new double[Settings.num_districts];
        perfect_dists = new double[Settings.num_districts];
    	//System.out.println("2");
    	
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
    	//System.out.println("3");
		
    	if( num_districts > target) {
    		double d = target;
    		for( int i = 0; i < vtd_districts.length; i++) {
    			if( vtd_districts[i] >= target) {
    				int x = (int)Math.floor(Math.random() * d);
    				vtd_districts[i] = x;
    				while( vtd_districts[i] >= target) {
    	   				x = (int)Math.floor(Math.random() * d);
        				vtd_districts[i] = x;
        			}

    				districts.get(x).vtds.add(vtds.get(i));
    			}
    		}
    	}
    	//System.out.println("4");
        fillDistrictwards();
    	//System.out.println("5");
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
        return vtd_districts;
    }
    public void setGenome(int[] genome) {
    	reciprocal_iso_quotient = -1;
    	if( num_districts < genome.length) {
    		num_districts = genome.length;
    	}
        vtd_districts = genome;
        //System.out.println("setgenome districts "+num_districts);
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++) {
            districts.add(new District());
        }
        if( vtds == null || vtds.size() < genome.length) {
        	fillDistrictwards();
        }
        for( int i = 0; i < genome.length; i++)
            districts.get(genome[i]).vtds.add(vtds.get(i));
    }

    //helper functions
    /*
    public double[][] getStandardResult() {
    	//System.out.println("num dists "+districts.size());
    	
        double[] popular_vote = new double[Settings.num_candidates]; //inited to 0
        double[] elected_vote = new double[Settings.num_candidates]; //inited to 0
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
    	
        double[] popular_vote = new double[Settings.num_candidates]; //inited to 0
        double[] elected_vote = new double[Settings.num_candidates]; //inited to 0
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
    
    public double[][] calcDemographicStatistics() {
    	String[] dem_col_names = MainFrame.mainframe.project.demographic_columns_as_array();
		
		double[] pop_by_dem = new double[dem_col_names.length];
		for( int i = 0; i < pop_by_dem.length; i++) { pop_by_dem[i] = 0; }
		double[] votes_by_dem = new double[dem_col_names.length];
		for( int i = 0; i < votes_by_dem.length; i++) { votes_by_dem[i] = 0; }
		double[] vote_margins_by_dem = new double[dem_col_names.length];
		for( int i = 0; i < vote_margins_by_dem.length; i++) { vote_margins_by_dem[i] = 0; }
		double[][] demo = getDemographicsByDistrict();
		double[][] demo_pct = new double[demo.length][];
		for( int i = 0; i < demo_pct.length; i++) {
			double total = 0;
			for( int j = 0; j < demo[i].length; j++) {
				pop_by_dem[j] += demo[i][j];
				total += demo[i][j];
			}
			total = 1.0/total;
			demo_pct[i] = new double[demo[i].length];
			for( int j = 0; j < demo[i].length; j++) {
				demo_pct[i][j] = demo[i][j]*total;
			}
		}
		
		//---insert vote and vote margin finding
		for( int i = 0; i < districts.size(); i++) {
			try {
			District d = districts.get(i);

			//double[][] result = d.getElectionResults();
			double[][] result = new double[2][];//d.getElectionResults();
			result[0] = d.getAnOutcome();
			result[1] = District.popular_vote_to_elected(result[0], i);

			double total_votes = result[0][0]+result[0][1];
			if( total_votes == 0) {
				total_votes = 1;
			}
			
			for( int j = 0; j < dem_col_names.length; j++) {
				votes_by_dem[j] += total_votes*demo_pct[i][j];
				vote_margins_by_dem[j] += vote_gap_by_district[i]*demo_pct[i][j];
			}	
			} catch (Exception ex) {
				System.out.println("ex stats 1 "+ex);
				ex.printStackTrace();
			}
		}
		//--end insert vote and vote margin finding
		
		double tot_pop = 0;
		double tot_vote = 0;
		double tot_margin = 0;
		for( int i = 0; i < dem_col_names.length; i++) {
			tot_pop += pop_by_dem[i];
			tot_vote += votes_by_dem[i];
			tot_margin += vote_margins_by_dem[i];
		}
		if( tot_margin == 0) {
			tot_margin = 1;
		}
		if( tot_vote == 0) {
			tot_vote = 1;
		}
		double ravg = 1.0 / (tot_margin / tot_vote);
		
		//String[] ecolumns = new String[]{"Ethnicity","Population","Vote dilution","% Wasted votes","Votes","Victory margins"};
		double[][] edata = new double[dem_col_names.length+1][];
		for( int i = 0; i < dem_col_names.length; i++) {
			edata[i] = new double[]{
					pop_by_dem[i],
					(ravg*vote_margins_by_dem[i]/votes_by_dem[i]),
					(vote_margins_by_dem[i]/votes_by_dem[i]),
					(votes_by_dem[i]),
					(vote_margins_by_dem[i]),
			};
		}
		edata[dem_col_names.length] = new double[]{
				tot_pop,
				1,
				(1.0/ravg),
				(tot_vote),
				(tot_margin),
		};
		
		return edata;
    }
    
    public double getRacialVoteDilution() {
    	//returns population-weighted mean absolute deviation.
    	double[][] ddd = calcDemographicStatistics();
    	double tot = 0;
    	double tot_score = 0;
    	for( int i = 0; i < ddd.length-1; i++) {
    		double pop = ddd[i][0];
    		double score = ddd[i][1];
    		score = Math.log(score);
    		tot_score += Math.abs(score)*pop;
    		tot += pop;
    	}
    	tot_score /= tot;
    	return tot_score;
    }
    
    public double[][] getDemographicsByDistrict() {
    	double[][] ddd = new double[districts.size()][];
    	for( int i = 0; i < districts.size(); i++) {
    		ddd[i] = districts.get(i).getDemographics();
    	}
    	return ddd;
    }
    
    public double countCountiesSplitInteger() {
		double splits = 0;
    	if( MainFrame.mainframe.project.county_column != null && MainFrame.mainframe.project.county_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
	    	try { 
			for( int i = 0; i < vtds.size(); i++) {
				VTD vtd = vtds.get(i);
				int[] dists = counties.get(vtd.county);
				if( dists == null) {
					dists = new int[Settings.num_districts];
					counties.put(vtd.county, dists);
				}
				int dist = vtd_districts[i];
				if( dist >= dists.length) {
					dist = (int)(Math.random()*(double)dists.length);
				}
				dists[dist]++;
			}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double total = 0;
				double nonzeros = 0;
				double least = -1;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
					total += ii[i];
					if( least < 0 || ii[i] < least) {
						least = ii[i];
					}
					
				}
				if( true || Settings.minimize_number_of_counties_split) {
					if( nonzeros > 1) {
						splits++;//splits += least;
					}
				} else {
					least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
					splits += nonzeros + least - 2;	
				}
			}
    	}
    	if( MainFrame.mainframe.project.muni_column != null && MainFrame.mainframe.project.muni_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
	    	try { 
			for( int i = 0; i < vtds.size(); i++) {
				VTD vtd = vtds.get(i);
				int[] dists = counties.get(vtd.muni);
				if( dists == null) {
					dists = new int[Settings.num_districts];
					counties.put(vtd.muni, dists);
				}
				int dist = vtd_districts[i];
				if( dist >= dists.length) {
					dist = (int)(Math.random()*(double)dists.length);
				}
				dists[dist]++;
			}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double total = 0;
				double nonzeros = 0;
				double least = -1;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
					total += ii[i];
					if( least < 0 || ii[i] < least) {
						least = ii[i];
					}
					
				}
				if( true || Settings.minimize_number_of_counties_split) {
					if( nonzeros > 1) {
						splits++;//splits += least/2.0; //count muni splits at half the value of a county split.	
					}
				} else {
					least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
					splits += (nonzeros + least - 2.0) / 2.0; //count muni splits at half the value of a county split.	
				}
			}
    	}
		return splits;

    }
    public double countSplits() {
		double splits = 0;
    	if( MainFrame.mainframe.project.county_column != null && MainFrame.mainframe.project.county_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
	    	try { 
	    		//colllect vtds into counties
				for( int i = 0; i < vtds.size(); i++) {
					VTD vtd = vtds.get(i);
					int[] dists = counties.get(vtd.county);
					if( dists == null) {
						dists = new int[Settings.num_districts];
						counties.put(vtd.county, dists);
					}
					int dist = vtd_districts[i];
					if( dist >= dists.length) {
						dist = (int)(Math.random()*(double)dists.length);
					}
					dists[dist]++;
				}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double total = 0;
				double nonzeros = 0;
				double least = -1;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
					total += ii[i];
					if( least < 0 || ii[i] < least) {
						least = ii[i];
					}
					
				}
				if( Settings.minimize_number_of_counties_split) {
					if( nonzeros > 1) {
						splits += least;
					}
				} else {
					least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
					splits += nonzeros + least - 2;	
				}
			}
    	}
    	if( MainFrame.mainframe.project.muni_column != null && MainFrame.mainframe.project.muni_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
	    	try { 
			for( int i = 0; i < vtds.size(); i++) {
				VTD vtd = vtds.get(i);
				int[] dists = counties.get(vtd.muni);
				if( dists == null) {
					dists = new int[Settings.num_districts];
					counties.put(vtd.muni, dists);
				}
				int dist = vtd_districts[i];
				if( dist >= dists.length) {
					dist = (int)(Math.random()*(double)dists.length);
				}
				dists[dist]++;
			}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double total = 0;
				double nonzeros = 0;
				double least = -1;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
					total += ii[i];
					if( least < 0 || ii[i] < least) {
						least = ii[i];
					}
					
				}
				if( Settings.minimize_number_of_counties_split) {
					if( nonzeros > 1) {
						splits += least/2.0; //count muni splits at half the value of a county split.	
					}
				} else {
					least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
					splits += (nonzeros + least - 2.0) / 2.0; //count muni splits at half the value of a county split.	
				}
			}
    	}
		return splits;
    }
    
    public double countSplitsInteger() {
    	return countMuniSplitsInteger()+countCountySplitsInteger();
    }
    public double countCountySplitsInteger() {
		double splits = 0;
    	if( MainFrame.mainframe.project.county_column != null && MainFrame.mainframe.project.county_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
			for( int i = 0; i < vtds.size(); i++) {
				VTD vtd = vtds.get(i);
				int[] dists = counties.get(vtd.county);
				if( dists == null) {
					dists = new int[Settings.num_districts];
					counties.put(vtd.county, dists);
				}
				dists[vtd_districts[i]]++;
			}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double nonzeros = 0;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
				}
				//least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
				splits += nonzeros - 1;	
			}
    	}
		return splits;
    }
    public double countMuniSplitsInteger() {
		double splits = 0;
    	if( MainFrame.mainframe.project.muni_column != null && MainFrame.mainframe.project.muni_column.length() > 0) {
	    	HashMap<String,int[]> counties = new HashMap<String,int[]>();
			for( int i = 0; i < vtds.size(); i++) {
				VTD vtd = vtds.get(i);
				int[] dists = counties.get(vtd.muni);
				if( dists == null) {
					dists = new int[Settings.num_districts];
					counties.put(vtd.muni, dists);
				}
				dists[vtd_districts[i]]++;
			}
			Collection<int[]> vii = counties.values();
			
			for(int[] ii : vii) {
				double nonzeros = 0;
				for( int i = 0; i < ii.length; i++) {
					if( ii[i] == 0) {
						continue;
					}
					nonzeros++;
				}
				//least = least*nonzeros/total; //normalizes this to a range of 0 to 1.
				splits += nonzeros - 1;	
			}
    	}
		return splits;
    }
    
    double deviation_from_diagonal = 0;
    public void calcSeatsVotesCurve() {
    	deviation_from_diagonal = 0;
		Vector<double[]> swap = new Vector<double[]>();
		double[] vote_count_totals = new double[2];
		vote_count_totals[0] = 0;
		vote_count_totals[1] = 0;
		double total = 0; 
		double[][] vote_count_districts = new double[Settings.num_districts][2];
		
		//aggregate all the votes
		for( int i = 0; i < districts.size() && i < Settings.num_districts; i++) {
			District d = districts.get(i);
			//double[][] result = d.getElectionResults();
			double[][] result = new double[2][];//d.getElectionResults();
			result[0] = d.getAnOutcome();
			//result[1] = District.popular_vote_to_elected(result[0], i);

			for( int j = 0; j < 2; j++) {
				vote_count_totals[j] += result[0][j];
				vote_count_districts[i][j] += result[0][j];
				total += result[0][j];
			}
		}
		
		//now normalize to 50/50
		double adjust = vote_count_totals[1]/vote_count_totals[0];
		for( int i = 0; i < districts.size() && i < Settings.num_districts; i++) {
			vote_count_districts[i][0] *= adjust;
		}
		
		//now sample it at different vote ratios
		for( double dempct = 0; dempct <= 1; dempct += 0.0025) {
			double reppct = 1-dempct;
			//double votes = dempct;
			double demseats = 0;
			double totseats = 0;
			for( int i = 0; i < districts.size() && i < Settings.num_districts; i++) {
				//if uncontested, ignore.
				if( FeatureCollection.buncontested1 != null && FeatureCollection.buncontested1.length > i && FeatureCollection.buncontested1[i] && Settings.ignore_uncontested) {
					continue;
				}
				totseats += Settings.seats_in_district(i);
				double[] pv = new double[]{vote_count_districts[i][0]*dempct,vote_count_districts[i][1]*reppct};
				double[] winners = District.popular_vote_to_elected( pv,i);
				demseats += winners[0];
			}
			double demseatpct = demseats/totseats;
			deviation_from_diagonal += Math.abs(demseatpct-dempct)/(double)totseats;
			swap.add(new double[]{demseatpct,dempct});
		}
		seats_votes = swap;
    }
    
    public double calcSeatsVoteAsymmetry() {
    	calcSeatsVotesCurve();
    	double total = 0;
    	//double rln2 = 1.0/Math.log(2);
    	double weight = 1.0/(double)seats_votes.size();
	    for( int i = 0; i < seats_votes.size(); i++) {
	    	double[] dd = seats_votes.get(i);
	    	
	    	//don't count if vote is <25% or >75%
	    	/*.
	    	if( dd[1] < 0.25|| dd[1] > 0.75) {
	    		weight = 0;
	    	}
	    	*/
	    	//weight = 1.0-2.0*Math.abs(dd[1]-0.5);
	    	
	    	//convolve with the binary entropy function
	    	/*
	    	weight = Math.abs(rln2*(dd[1]*Math.log(dd[1]) + (1.0-dd[1])*Math.log(1.0-dd[1])));
	    	if( weight != weight || dd[1] == 0 || dd[1] == 1) {
	    		weight = 0;
	    	}
	    	*/
	    	
	    	double[] dd2 =  seats_votes.get(seats_votes.size()-1-i);
	    	double mid_y = (dd[0]+(1-dd2[0]))/2.0;
	    	total += weight*Math.abs(dd[0]-mid_y);
	    }

    	return total;
    }

    //returns total edge length, unfairness, population imbalance
    //a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
    //all measures should be minimized.
    public void calcFairnessScores() {//border_length_area_weighted
    	try {
    		try {
	    		for( int i = 0; i < districts.size(); i++) {
	    			if( districts.get(i) != null) {
		    			districts.get(i).id = i;
		    		}
	    		}
    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    	
    	long time0 = System.currentTimeMillis();    
    	long wasted_votes = 0;
    	//===fairness score: compactness
        double length = getReciprocalIsoPerimetricQuotient();// : getEdgeLength();
        
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
    	
        double[] p = new double[Settings.num_candidates];
        double[] q = new double[Settings.num_candidates];
        double disproportional_representation = 0;
        double wasted_vote_imbalance = 0;
    	if( true) { //Settings.disenfranchise_weight > 0 || Settings.voting_power_balance_weight > 0 || Settings.seats_votes_asymmetry_weight > 0 || Settings.competitiveness_weight > 0 || Settings.wasted_votes_imbalance_weight > 0) {
    		for(District d : districts) {
    			d.generateOutcomes(Settings.num_elections_simulated);
    		}
    		
    	}
    	if( true) { //Settings.disenfranchise_weight > 0 || Settings.competitiveness_weight > 0 || Settings.wasted_votes_imbalance_weight > 0) {
    		//System.out.println("num t "+trials);
        	//===fairness score: proportional representation
    		wasted_votes_by_party = new int[Settings.num_candidates];
    	    wasted_votes_by_district = new int[districts.size()];
    	    vote_gap_by_district = new int[districts.size()];
    	    wasted_votes_by_district_and_party = new int[districts.size()][Settings.num_candidates];
    	    for( int i = 0; i < wasted_votes_by_party.length; i++) {
    	    	wasted_votes_by_party[i] = 0;
    	    }
    	    for( int i = 0; i < wasted_votes_by_district.length; i++) {
    	    	wasted_votes_by_district[i] = 0;
    	    }
    	    for( int i = 0; i < vote_gap_by_district.length; i++) {
    	    	vote_gap_by_district[i] = 0;
    	    }
    	    for( int i = 0; i < wasted_votes_by_district.length; i++) {
	    	    for( int j = 0; j < wasted_votes_by_party.length; j++) {
	    	    	wasted_votes_by_district_and_party[i][j] = 0;
	    	    }
    	    }
    	    if( !District.use_simulated_elections) {
    	    	Settings.num_elections_simulated = 1;
    	    }
    	    total_vote_gap = 0;
            for( int i = 0; i < Settings.num_elections_simulated; i++) {
                double[] popular_vote = new double[Settings.num_candidates]; //inited to 0
                double[] elected_vote = new double[Settings.num_candidates]; //inited to 0
                //double[][] residues = new double[districts.size()][];
                //double[] pops = new double[districts.size()];
                for(int k = 0; k < districts.size(); k++) {
                	/*
                	if( Settings.ignore_uncontested && District.uncontested.length > k && District.uncontested[k]) {
                		continue;
                	}*/
                	District district = districts.get(k);
                	//double[][] res = district.getElectionResults();
            		double[][] res = new double[2][];//d.getElectionResults();
            		res[0] = district.getAnOutcome();
            		res[1] = District.popular_vote_to_elected(res[0], k);
            		

                	//pops[k] = res[4][0];
                	//residues[k] = res[3];
                	for( int j = 0; j < popular_vote.length; j++) {
                		popular_vote[j] += res[0][j];
                		elected_vote[j] += res[1][j];
                	}
                	try {
 
                	//now count wasted votes
                    int tot = 0;
                    for(int j = 0; j < popular_vote.length; j++) {
                    	tot += res[0][j];
                    }
                    int unit = tot/Settings.seats_in_district(k);
                    
                    for(int j = 0; j < popular_vote.length; j++) {
                    	//make amt as if there was one vote w/pop 0 to unit
                    	if( res[0][j] < 0) {
                    		System.out.println("res < 0");
                    	}
                    	double amt = 0;
                    	if( Settings.seats_in_district(k) > 1) {
                    		int c = ((int)res[0][j]) / unit;
                        	amt = res[0][j] - c*unit;//% unit;//res[0][j] - (res[1][j] == 0 ? 0 : (res[1][j]-1)) * unit;
                        	if( res[1][j] > c) { //this means that we won the remaining majority election
                        		if( amt > unit/2) {
                        			amt -= unit/2;
                        		}
                            	vote_gap_by_district[k] = (int)amt*2;
                        	} else {
                        		
                        	}
                        	/*
                        	amt = res[0][j] - unit*res[1][j];//% unit;//res[0][j] - (res[1][j] == 0 ? 0 : (res[1][j]-1)) * unit;
	                    	if( amt < 0) {
	                    		amt += unit;
                        		amt -= unit/2; //overvote
	                    	} else {
                        		amt -= unit/2; //overvote
	                    	}
	                    	amt /= 2;
	                    	*/
	                    } else {
                        	amt = res[0][j];//res[0][j] - (res[1][j] == 0 ? 0 : (res[1][j]-1)) * unit;
                        	if (amt > unit/2){
                        		amt -= unit/2; //overvote
                        	} else {
                        		/*
                        		if( amt > unit/4) {
                            		amt = unit/2 - amt; //votes short
                            		amt = 0; //or just not count these...
                        		}
                        		*/
                        	}
	                    }
                    	/*
                    	if( amt < unit/4) {
                    		amt = amt; // if less than1/4, all votes are wasted.
                    	} else 
                    		*/
                    	
                    	

                    	wasted_votes += amt;
                    	wasted_votes_by_party[j] += amt;
                    	wasted_votes_by_district[k] += amt;
                    	wasted_votes_by_district_and_party[k][j] += amt; 
                    }
                    if( Settings.seats_in_district(k) == 1) {
                    	vote_gap_by_district[k] = (int)Math.abs(res[0][0]-res[0][1]);
                    }
                    total_vote_gap += vote_gap_by_district[k];
                	} catch (Exception ex) {
                		System.out.println("ex aa "+ex);
                		ex.printStackTrace();
                	}
 
                }
                /*
        		double[][] result = new double[2][];//d.getElectionResults();
        		result[0] = d.getAnOutcome();
        		result[1] = District.popular_vote_to_elected(result[0], i);
        		*/
        		/*
                double[][] prop_rep_results = District.getPropRepOutcome(popular_vote,Settings.total_seats());//Settings.seats_in_district*Settings.num_districts);
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
            	*/
            	

                for( int j = 0; j < Settings.num_candidates; j++) {
                    p[j] += popular_vote[j];
                    q[j] += elected_vote[j];
                }
            }
            
    	    double total_by_party = 0;
    	    for( int i = 0; i < wasted_votes_by_party.length; i++) {
    	    	wasted_votes_by_party[i] /= Settings.num_elections_simulated;;
    	    	total_by_party += wasted_votes_by_party[i];
    	    }
     	    for( int i = 0; i < wasted_votes_by_district.length; i++) {
    	    	wasted_votes_by_district[i] /= Settings.num_elections_simulated;
    	    }
      	    for( int i = 0; i < wasted_votes_by_district_and_party.length; i++) {
      	  	    for( int j = 0; j < wasted_votes_by_district_and_party[i].length; j++) {
      	  	    	wasted_votes_by_district_and_party[i][j] /= Settings.num_elections_simulated;
      	  	    }
    	    }
    	    
    	    double[] target_wasted = new double[wasted_votes_by_party.length];
    	    double[] actual_wasted = new double[wasted_votes_by_party.length];
    	    for( int i = 0; i < wasted_votes_by_party.length; i++) {
    	    	actual_wasted[i] = ((double)wasted_votes_by_party[i])/total_by_party;
    	    }
    	    for( int i = 0; i < wasted_votes_by_party.length; i++) {
    	    	target_wasted[i] = 1.0/((double)wasted_votes_by_party.length);
    	    }
    	    wasted_vote_imbalance = getKLDiv(target_wasted,actual_wasted,1.2);

            time20 = System.currentTimeMillis();
            disproportional_representation = getKLDiv(p,q,1.2);
            if( disproportional_representation != disproportional_representation) {
            	disproportional_representation = 0;
            	//System.out.println("disproportional_representation not a number");
            }
            
            wasted_votes /= Settings.num_elections_simulated;
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
                voting_power[i] = 1;//district.getSelfEntropy(district.getElectionResults()[Settings.self_entropy_use_votecount?2:1]);
                total_voting_power += voting_power[i];
            }

            for(int i = 0; i < dist_pops.length; i++) {
                voting_power[i] /= total_voting_power;
            }
            if( Settings.ignore_uncontested && FeatureCollection.buncontested1 != null) {
                int num_uncontested = 0;
                for( int i = 0; i < FeatureCollection.buncontested1.length; i++) {
                	num_uncontested += FeatureCollection.buncontested1[i] ? 1 : 0;
                }
                	
            	double[] dq = new double[dist_pop_frac.length-num_uncontested];
            	double[] dp = new double[dist_pop_frac.length-num_uncontested];
            	int ndx = 0;
            	double totp = 0;
            	double totq = 0;
                for( int i = 0; i < dist_pop_frac.length; i++) {
                	if(  FeatureCollection.buncontested1.length > i &&  FeatureCollection.buncontested1[i]) {
                		continue;
                	}
                	dq[ndx] = dist_pop_frac[i];
                	dp[ndx] = voting_power[i];
                	totp += dist_pop_frac[i];
                	totq += voting_power[i];
                	ndx++;
                }
                for(int i = 0; i < dp.length; i++) {
                    dp[i] /= totp;
                }
                for(int i = 0; i < dq.length; i++) {
                    dq[i] /= totq;
                }
            	power_fairness = getKLDiv(dp,dq,0.01);
            	
            } else {
            	power_fairness = getKLDiv(dist_pop_frac,voting_power,0.01);
            }
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
                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(vtd_districts));
            }
        }
        //disconnected_pops /= total_population;
        
    	long time5 = System.currentTimeMillis();
    	
        //System.out.println(""+wasted_votes+", "+wasted_vote_imbalance);
    	double sva = calcSeatsVoteAsymmetry();
    	/*
    	 * double[] weights = new double[]{
        		Settings.geometry_weight                *1.0, 
        		Settings.disenfranchise_weight          *1.0, 
        		Settings.population_balance_weight      *1.0,
                Settings.disconnected_population_weight *1.0,
                Settings.voting_power_balance_weight    *1.0,
                Settings.wasted_votes_total_weight      *1.0,
                Settings.wasted_votes_imbalance_weight  *1.0,
                Settings.seats_votes_asymmetry_weight   *1.0,
                Settings.diagonalization_weight   *1.0,
        };
    	 */
        fairnessScores = new double[]{
        		length
        		,disproportional_representation
        		,Settings.minimize_absolute_deviation ? getMeanPopDiff() : getPopVariance()/*population_imbalance*2.0*/ //getMaxPopDiff()
        		,disconnected_pops
        		,power_fairness
        		,total_vote_gap//wasted_votes
        		,wasted_vote_imbalance
        		,sva
        		,deviation_from_diagonal
        		,Settings.reduce_splits ? countSplits() : 0
        		,Settings.vote_dilution_weight == 0 ? 0 : getRacialVoteDilution()
        		}; //exponentiate because each bit represents twice as many people disenfranched
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
    	} catch (Exception ex) {
    		System.out.println("ex ab "+ex);
    		ex.printStackTrace();
    	}
    }
    public double getMaxPopDiff() {
    	double min = -1;
    	double max = -1;
   	  for(int i = 0; i < Settings.num_districts; i++) {
  		  District district = districts.get(i);
		  district.id = i;
        	double pop = district.getPopulation();
        	if( Settings.population_is_per_seat) {
        		pop /= Settings.seats_in_district(i);
        	}
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
    
    public double getMeanPopDiff() {
    	double tot = 0;
    	double mad = 0;
    	double seats = 0;
    	for(int i = 0; i < Settings.num_districts; i++) {
    		District district = districts.get(i);
    		district.id = i;
			double pop = district.getPopulation();
			tot += pop;
			seats +=  Settings.seats_in_district(i);
		}
    	tot /= seats;
    	//System.out.println("tot = "+tot);
    	for(int i = 0; i < Settings.num_districts; i++) {
    		District district = districts.get(i);
    		district.id = i;
			double pop = district.getPopulation();
			pop /= Settings.seats_in_district(i);
			mad += Math.abs(pop-tot)*Settings.seats_in_district(i);
		}
    	mad /= seats;
        return mad/tot;
    }
    
    //needs to be variance, not sqrt variance, so that delta is linear
    public double getPopVariance() {
    	double tot = 0;
    	double tot2 = 0;
    	double N = (double) districts.size();
 	  for(int i = 0; i < Settings.num_districts; i++) {
  		  District district = districts.get(i);
		  district.id = i;
  		  double pop = district.getPopulation();
        	if( Settings.population_is_per_seat) {
        		pop /= Settings.seats_in_district(i);
        	}
        	tot += pop;
        	tot2 += pop*pop;
        }
        double avg = tot / N;
   	  for(int i = 0; i < Settings.num_districts; i++) {
  		  District district = districts.get(i);
        	int pop = (int)district.getPopulation();
        	if( Settings.population_is_per_seat) {
        		pop /= Settings.seats_in_district(i);
        	}
        	district.excess_pop = (int) (pop-avg);
        }
        //tot /= (double) districts.size();
        double variance =  (tot2-tot*tot/N) / N;
        return Settings.squared_pop_variance ? variance*variance : variance;
    }

    public int compareTo(DistrictMap o) {
        double d = (fitness_score-o.fitness_score)*sorting_polarity; 
        return  d > 0 ? 1 : d == 0 ? 0 : -1;
    }
    double reciprocal_iso_quotient = -1;
	public static double edge_length(double paired_edge_length, double unpaired_edge_length) {
    	return paired_edge_length + unpaired_edge_length*Settings.unpaired_edge_length_weight;
	}
	public static double iso_quotient(double area, double paired_edge_length, double unpaired_edge_length) {
    	double edge_length = paired_edge_length + unpaired_edge_length*Settings.unpaired_edge_length_weight;
		double iso = (4.0*Math.PI*area) / (edge_length*edge_length);
		return Settings.squared_compactness ? iso : Math.sqrt(iso);
	}
    
    public double getReciprocalIsoPerimetricQuotient() {
    	if( reciprocal_iso_quotient >= 0) {
    		return reciprocal_iso_quotient;
    	}
    	//double[] lengths = new double[districts.size()]; 
    	double[] paired_lengths = new double[districts.size()]; 
    	double[] unpaired_lengths = new double[districts.size()]; 
    	double[] areas = new double[districts.size()]; 
        for( VTD b : vtds) {
        	int d1 = vtd_districts[b.id];
        	areas[d1] += b.area;
        	unpaired_lengths[d1] += b.unpaired_edge_length;
        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
        		int b2id = b.neighbors.get(i).id; 
            	int d2 = vtd_districts[b2id];
            	if( d1 != d2) {
            		paired_lengths[d1] += b.neighbor_lengths[i];
            		//lengths[d2] += b.neighbor_lengths[i];
            	}
        	}
        }
        
        double weighted_sum = 0;
        double weighted_sum_all_paired = 0;
        double num_all_paired = 0;
        for( int i = 0; i < paired_lengths.length; i++) {
        	areas[i] *= area_multiplier;
        	District d = districts.get(i);
        	d.paired_edge_length = paired_lengths[i];
        	d.unpaired_edge_length = unpaired_lengths[i];

        	/*
        	double a = paired_lengths[i];
        	double b = unpaired_lengths[i];
        	d.edge_length = paired_lengths[i];
        	d.area = areas[i]*(a*a)/((a+b)*(a+b));
        	*/
        	
        	d.edge_length = edge_length(d.paired_edge_length, d.unpaired_edge_length);
        	d.area = areas[i];
        	d.iso_quotient = iso_quotient(d.area, d.paired_edge_length,d.unpaired_edge_length);
        	
        	//weighted_sum += lengths[i] / Math.sqrt(areas[i]);
        	if( unpaired_lengths[i] == 0) {
        		weighted_sum_all_paired += 1.0/d.iso_quotient;
        		num_all_paired++;
        	}
        	weighted_sum += 1.0/d.iso_quotient;//Settings.squared_compactness ? 1.0/(d.iso_quotent*d.iso_quotent) : 1.0/d.iso_quotent;
        }
        //weighted_sum = Math.sqrt(weighted_sum);
        if( num_all_paired == 0) { num_all_paired = 1; }
        reciprocal_iso_quotient = (weighted_sum / (double)paired_lengths.length);
        double wel_all_paired = (weighted_sum_all_paired / num_all_paired);
        
        /*
        for( int i = 0; i < paired_lengths.length; i++) {
        	areas[i] *= area_multiplier;
        	District d = districts.get(i);
        	d.edge_length = paired_lengths[i];
        	d.unpaired_edge_length = unpaired_lengths[i];
        	d.area = areas[i];
        	double a = paired_lengths[i];
        	double b = unpaired_lengths[i];
        	d.area = areas[i]*(a*a)/((a+b)*(a+b));
        	
        	d.iso_quotent = Math.sqrt( (4.0*Math.PI*d.area) / (d.edge_length*d.edge_length) );
        	//weighted_sum += lengths[i] / Math.sqrt(areas[i]);
        	if( Settings.squared_compactness) {
        		d.iso_quotent *= d.iso_quotent;
        	}
        	if( unpaired_lengths[i] == 0) {
        		weighted_sum_all_paired += 1.0/d.iso_quotent;
        		num_all_paired++;
        	}
        	weighted_sum += 1.0/d.iso_quotent;//Settings.squared_compactness ? 1.0/(d.iso_quotent*d.iso_quotent) : 1.0/d.iso_quotent;
        }
        */
        
        
        //if( Settings.squared_compactness ) {
        	//wel = Math.sqrt(wel);
        //}
        return reciprocal_iso_quotient;
    }
    
    double getEdgeLength() {
        double length = 0;
        for( VTD b : vtds) {
        	int d1 = vtd_districts[b.id];
        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
        		int b2id = b.neighbors.get(i).id;
            	int d2 = vtd_districts[b2id];
            	if( d1 != d2) {
            		length += b.neighbor_lengths[i];
            	}
        	}
        	
        	
        }
        return length;
    }
    Vector<Edge> getOuterEdges(int[] ward_districts) {
        Vector<Edge> outerEdges = new Vector<Edge>();
        for( VTD ward : vtds)
            for( Edge edge : ward.edges)
                if( !edge.areBothSidesSameDistrict(ward_districts))
                    outerEdges.add(edge);
        return outerEdges;
    }


	public Color getWastedVoteColor(int i) {
		double[] rgb = new double[]{0,0,0};
		if( wasted_votes_by_district_and_party == null) {
			return Color.WHITE;
		}
		int[] amts0 = wasted_votes_by_district_and_party[i];
		double[] amts1 = districts.get(i).getAnOutcome();//getElectionResults()[0];

		double tot = 0;
		for( int j = 0; j < amts1.length; j++) {
			tot += amts1[j];
		}
		tot /= 3;
		if( tot == 0) { tot = 1; }
		
		for( int j = 0; j < amts0.length; j++) {
			if( amts1[j] == 0) {
				amts1[j] = 1;
			}
			Color c = getComplement(FeatureCollection.standard_district_colors[j]);
			double d = ((double)amts0[j]) / tot;//amts1[j];
			d /= 2;
			//System.out.println(""+j+": "+amts0[j]+" "+amts1[j]+" "+d+" "+tot);
			if( d > 1) { d = 1; }
			if( d < 0) { d = 0; }
			rgb[0] += d*c.getRed();
			rgb[1] += d*c.getGreen();
			rgb[2] += d*c.getBlue();
		}
		//System.out.print("c");
		for( int j = 0; j < 3; j++) {
			//rgb[j] /= 2;
			if( rgb[j] < 0) rgb[j] = 0;
			if( rgb[j] > 255) rgb[j] = 255;
			//System.out.print(" "+rgb[j]);
		}
		//System.out.println();
		return getComplement(new Color((int)rgb[0],(int)rgb[1],(int)rgb[2]));
	}
	public static Color getComplement(Color c) {
		return new Color(
				255-c.getRed(),
				255-c.getGreen(),
				255-c.getBlue()
				);
	}
}
