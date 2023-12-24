package solutions;

import geography.Edge;
import geography.VTD;
import geography.Vertex;
import jsonMap.JsonMap;
import jsonMap.ReflectJsonMap;
import paretoFront.ScoreArray;
import ui.MainFrame;
import util.AdaptiveMutation;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import ui.MapPanel;
//import ui.PanelStats;

public class Ecology extends ReflectJsonMap<Ecology> {
	
	public static AdaptiveMutation adaptiveMutation = new AdaptiveMutation(); 
	
	public Vector<iDiscreteEventListener> evolveListeners = new Vector<iDiscreteEventListener>();
	
	public static Vector<double[]> history = new Vector<double[]>();
	public static Vector<double[]> normalized_history = new Vector<double[]>();

	static public DistrictMap bestMap = null;
	
	static int verbosity = 0;
	static boolean initial_mate_merge = false;
	public ScoringThread[] scoringThreads;
	public ExecutorService scoringThreadPool;
	public CountDownLatch scoringLatch;

	public MatingThread[] matingThreads;
	public ExecutorService matingThreadPool;
	public CountDownLatch matingLatch;
	public static final int NUM_FAIRNESS_SCORES = 13;

	static int num_threads = Runtime.getRuntime().availableProcessors()*4<=256 ? Runtime.getRuntime().availableProcessors()*4 : 256;

	public static double[] fairnessScoreEmaVars = new double[NUM_FAIRNESS_SCORES];
	public static double[] fairnessScoreEmaMeans = new double[NUM_FAIRNESS_SCORES];

    int cutoff;
    int speciation_cutoff;

	static public boolean evolve_paused = true;
	public static double invert = 1;

	public static String initMethod = "Contiguous";
	int last_population = 0;
	int last_num_districts = 0;
	
	public HashMap<Integer,VTD> wards_by_id;
	
	public Vector<VTD> vtds = new Vector<VTD>();
	public Vector<Edge> edges = new Vector<Edge>();
	public Vector<Vertex> vertexes = new Vector<Vertex>();
	//public MapPanel mapPanel;
	//public PanelStats statsPanel;
	

	Settings settings = new Settings();
	
    public Vector<DistrictMap> population = new Vector<DistrictMap>();
    public Vector<DistrictMap> swap_population = new Vector<DistrictMap>();
    
    public EvolveThread evolveThread; 
    public long generation = 0;
    public double seconds_per_iter = -1;
    public long last_iter_time = -1;
    
    public void make_unique() {
    	for(DistrictMap dm : population) {
    		boolean dupe = true;
    		boolean duped = false;
    		while( dupe) {
    			dupe = false;
    	       	for(DistrictMap dm2 : population) {
            		if( dm == dm2) {
            			continue;
            		}
                	if( DistrictMap.getGenomeHammingDistance(dm.getGenome(), dm2.getGenome()) == 0) {
                		dupe = true;
                		duped = true;
                		System.out.println("duplicate removed");
                		dm.mutate_boundary(Settings.mutation_boundary_rate);
                	}
            	}
    		}
    		if( duped) {
    			dm.fillDistrictwards();
    		}
        		
    	}
    }


    class EvolveThread extends Thread {
    	public void run() {
    		for( int i = 0; i < DistrictMap.metrics.length; i++) {
    			DistrictMap.metrics[i] = 0;
    		}
    		scoringThreadPool = Executors.newFixedThreadPool(num_threads);
    		scoringThreads = new ScoringThread[num_threads];
    		matingThreadPool = Executors.newFixedThreadPool(num_threads);
    		matingThreads = new MatingThread[num_threads];
    		for( int i = 0; i < matingThreads.length; i++) {
    			matingThreads[i] = new MatingThread();
    			matingThreads[i].id = i;
    		}
    		
    		for( int i = 0; i < scoringThreads.length; i++) {
    			scoringThreads[i] = new ScoringThread();
    			scoringThreads[i].population = new Vector<DistrictMap>();
    		}
    		{
        		int i = 0;
        		for( DistrictMap d : population) {
        			scoringThreads[i].population.add(d);
        			i++;
        			i %= num_threads;
        		}
    		}

    		while( !evolve_paused) {
    			long new_time = new Date().getTime();
    			if( last_iter_time > 0) {
    				double cur = ((double)(new_time-last_iter_time)/1000.0);
    				if (seconds_per_iter < 0) {
    					seconds_per_iter = cur;
    				} else {
    					seconds_per_iter = (seconds_per_iter*4.0 + cur)/5.0;
    				}
    			}
				last_iter_time = new_time;
    			MainFrame.mainframe.ip.eventOccured();
    			try {
    				//System.out.println("last_num_districts "+last_num_districts+" Settings.num_districts "+Settings.num_districts);
    				//System.out.println("population.size() "+population.size()+" Settings.population "+Settings.population);
        			if( last_num_districts != Settings.num_districts) {
        				//if( JOptionPane.showConfirmDialog(null, "resize districts?") == JOptionPane.YES_OPTION) {
            				System.out.println("Adjusting district count from "+last_num_districts+" to "+Settings.num_districts+"...");
            				resize_districts();
        				//}
            	    		for( int i = 0; i < scoringThreads.length; i++) {
            	    			scoringThreads[i] = new ScoringThread();
            	    			scoringThreads[i].population = new Vector<DistrictMap>();
            	    		}
            	    		int i = 0;
            	    		for( DistrictMap d : population) {
            	    			scoringThreads[i].population.add(d);
            	    			i++;
            	    			i %= num_threads;
            	    		}
        			}
        			if( population.size() != Settings.population) {
        				//if( JOptionPane.showConfirmDialog(null, "resize population?") == JOptionPane.YES_OPTION) {
            				System.out.println("Adjusting population from "+population.size()+" to "+Settings.population+"...");
                			resize_population();
                			if( initial_mate_merge) {
                				match_population();
                			}
            	    		for( int i = 0; i < scoringThreads.length; i++) {
            	    			scoringThreads[i] = new ScoringThread();
            	    			scoringThreads[i].population = new Vector<DistrictMap>();
            	    		}
            	    		int i = 0;
            	    		for( DistrictMap d : population) {
            	    			scoringThreads[i].population.add(d);
            	    			i++;
            	    			i %= num_threads;
            	    		}
        				//}
        			}
        			try {
        				evolveWithSpeciation(); 
        			} catch (Exception ex) {
        				System.out.println("ex evolveWithSpeciation "+ex);
        				ex.printStackTrace();
        			}
        			if( verbosity > 0) {
            			System.out.print("time metrics: ");
            			for( int i = 0; i < DistrictMap.metrics.length; i++) {
            				System.out.print(DistrictMap.metrics[i]+", ");
            			}
            			System.out.println();
        			}
        			generation++;
        			for( iDiscreteEventListener ev : evolveListeners) {
        				ev.eventOccured();
        			}
        			/*
        			if( mapPanel == null)  {
           				mapPanel = MainFrame.mainframe.mapPanel;
        			}
        			if( mapPanel != null) {
        				mapPanel.invalidate();
        				mapPanel.repaint();
        			} else {
        				System.out.println("mappanel is null");
        			}
           			if( statsPanel != null) {
        				statsPanel.getStats();
        				MainFrame.mainframe.panelGraph.update();
        			} else {
        				System.out.println("stats panel is null");
        			}*/
    			} catch (Exception ex) {
    				System.out.println("ex abc "+ex);
    				ex.printStackTrace();
    			}
    		}
    		last_iter_time = -1;
    	}
    }
    
	public void startEvolving() {
		resize_population();
		if( !evolve_paused) {
			return;
		}
		//Feature.display_mode = 0;

		if( evolveThread != null) {
			try {
				evolveThread.stop();
				//evolveThread.destroy();
				evolveThread = null;
			} catch (Exception ex) { }
		}
		resize_population();
		for(District d : population.get(0).districts) {
			d.generateOutcomes(Settings.num_elections_simulated);
		}
		evolve_paused = false;
		evolveThread = new EvolveThread();
		evolveThread.start();
	}
    
	
	public void stopEvolving() {
		evolve_paused= true;
		last_iter_time = -1;
	}



	@Override
	public void post_deserialize() {
		wards_by_id = new HashMap<Integer,VTD>();
		HashMap<Integer,Edge> edges_by_id = new HashMap<Integer,Edge>();
		HashMap<Integer,Vertex> vertexes_by_id = new HashMap<Integer,Vertex>();
		
		//geometry
		if( containsKey("wards")) {
			vtds = getVector("wards");
			for( VTD ward: vtds) {
				wards_by_id.put(ward.id,ward);
			}
		}
		if( containsKey("edges")) { 
			edges = getVector("edges"); 
			for( Edge edge: edges) {
				edges_by_id.put(edge.id,edge);
			}
		}
		if( containsKey("vertexes")) {
			vertexes = getVector("vertexes");
			for( Vertex vertex: vertexes) {
				vertexes_by_id.put(vertex.id,vertex);
			}
		}
		if( edges != null) {
			for( Edge edge: edges) {
				edge.ward1 = wards_by_id.get(edge.ward1_id);
				edge.ward2 = wards_by_id.get(edge.ward2_id);
				edge.vertex1 = vertexes_by_id.get(edge.vertex1_id);
				edge.vertex2 = vertexes_by_id.get(edge.vertex2_id);
				edge.ward1.edges.add(edge);
				edge.ward2.edges.add(edge);
			}
		}
		for( VTD b : vtds) {
			b.collectNeighbors();
		}

		//stuff
		if( containsKey("candidates")) {
			//Candidate.candidates = getVector("candidates");
		}
		
		if( containsKey("settings")) { settings = (Settings)get("settings"); }
	}

	@Override
	public void pre_serialize() {		
		put("wards",vtds);
		put("edges",edges);
		put("vertexes",vertexes);
		
		//put("candidates",Candidate.candidates);
	}

	@Override
	public JsonMap instantiateObject(String key) {
		if( key.equals("vertexes")) {
			return null;//new Vertex();
		}
		if( key.equals("edges")) {
			return null;//new Edge();
		}
		if( key.equals("wards")) {
			return new VTD();
		}
		
		if( key.equals("candidates")) {
			//return new Candidate();
		}
		if( key.equals("settings")) {
			return new Settings();
		}
		return null;
	}
	
    //static int num_parties = 0;

 
    public void reset() {
    	population = new Vector<DistrictMap>();
    	this.generation = 0;
    	history = new Vector<double[]>();
    	normalized_history = new Vector<double[]>();
        for( int i = 0; i < fairnessScoreEmaVars.length; i++) {
        	fairnessScoreEmaMeans[i] = 0;
        	fairnessScoreEmaVars[i] = 0;
        }
        adaptiveMutation = new AdaptiveMutation();
    }
    public void match_population() {
    	
    	int[] template = population.get(0).getGenome();
    	for( DistrictMap dm : population) {
    		dm.makeLike(template);
    	}
    }
    public void resize_population() {
		System.out.println("resize_population start "+ (population == null ? "null" : population.size()));
		try {

    	if( population == null) {
    		population =  new Vector<DistrictMap>();
    	}
        while( population.size() < Settings.population) {
            population.add(new DistrictMap(vtds,Settings.num_districts));
        }
        while( population.size() > Settings.population) {
            population.remove(Settings.population);
        }
        last_population = Settings.population;
		} catch (Exception ex) {
			System.out.println("resize fail: "+ex);
			ex.printStackTrace();
			
		}

		System.out.println("resize_population done" + population.size());
    }
    public void resize_districts() {
    	if( population == null) {
    		population = new Vector<DistrictMap>();
    	}
        for( int i = 0; i < population.size(); i++) {
            population.get(i).resize_districts(Settings.num_districts);
        }
        last_num_districts = Settings.num_districts;
    }
    
    class ScoringThread implements Runnable {
    	Vector<DistrictMap> population = new Vector<DistrictMap>();
    	public void run() {
            for( DistrictMap map : population) {
            	//System.out.print(".");
            	try {
            		map.calcFairnessScores();
            	} catch (Exception ex) { 
            		
            	}
            }
            //System.out.print(".");
    		scoringLatch.countDown();
    		
    	}
    }
    int step = 0;
    public void evolveWithSpeciation() {
		cutoff = (int)(Settings.elite_fraction*(double)population.size());
    	speciation_cutoff = cutoff;

    	if( verbosity > 1) {
        	System.out.println("evolving {");
        } else if (verbosity == 1) {
        	System.out.print(".");
        	step++;
        	if( step % 100 == 0) {
        		System.out.println();
        	}
        }

        
        //STEP 1: SCORE
    	if( verbosity > 1)
        	System.out.print("  calculating fairness");
        if( !Settings.multiThreadScoring) { //single threaded
            for( DistrictMap map : population) {
            	//System.out.print(".");
                map.calcFairnessScores();
            }
        } else {
		//System.out.print(""+step);
    		scoringLatch = new CountDownLatch(num_threads);
    		for( int j = 0; j < scoringThreads.length; j++) {
    			scoringThreadPool.execute(scoringThreads[j]);
    			//iterationThreads[j].start();
    		}
    		try {
    			scoringLatch.await();
    		} catch (InterruptedException e) {
    			System.out.println("ex abd"+e);
    			// TODO Auto-generated catch ward
    			e.printStackTrace();
    		}
        }
        
        if( verbosity > 1)
        	System.out.println();
    	
        //STEP 2: NORMALIZE
        if( !Settings.paretoMode) {
	        if( verbosity > 1)
	        	System.out.println("  renormalizing fairness...");
	        if( Settings.NORMALIZE_MODE == Settings.RANK) {
		        for( int i = 0; i < fairnessScoreEmaVars.length; i++) {
		        	//pre-randomize so that ties are treated unbiased.
		            for( DistrictMap map : population) {
		                map.fitness_score = Math.random();
		            }
		            Collections.sort(population);
		            
		            for( DistrictMap map : population) {
		                map.fitness_score = map.fairnessScores[i];
		            }
		            Collections.sort(population);
		            double mult = 1.0/(double)population.size();
		            for( int j = 0; j < population.size(); j++) {
		                DistrictMap map = population.get(j);
		                //if( map.fairnessScores[i] != 0 || i == 11) {
		                	map.fairnessScores[i] = ((double)j)*mult;
		                //}
		            }
		        }
	        } else
	        if( Settings.NORMALIZE_MODE == Settings.EMA) {
		        for( int i = 0; i < fairnessScoreEmaVars.length; i++) {
	
		        	double avg = 0;
		            for( DistrictMap map : population) {
		            	avg += map.fairnessScores[i];
		            }
		            avg /= population.size();
	
		            double var = 0;
		            for( DistrictMap map : population) {
		            	var += Math.abs(map.fairnessScores[i]-avg);
		            }
		            var /= ((double)population.size()-1.0); //subtract 1 to make it an "unbiased estimator".
	
		            if( var == 0) { var = 0.001; }
		            if( var != var) { var = fairnessScoreEmaVars[i]; }
		            if( avg != avg) { avg = fairnessScoreEmaMeans[i]; }
		            if( fairnessScoreEmaVars[i] == 0) {
		            	fairnessScoreEmaVars[i] = var;
		            	fairnessScoreEmaMeans[i] = avg;
		            } else {
		            	fairnessScoreEmaVars[i] += (var-fairnessScoreEmaVars[i])/100.0;
		            	fairnessScoreEmaMeans[i] += (avg-fairnessScoreEmaMeans[i])/10.0;
		            }
		        }
	            for( DistrictMap map : population) {
	    	        for( int i = 0; i < fairnessScoreEmaVars.length; i++) {
	    	        	map.fairnessScores[i] = (map.fairnessScores[i] - fairnessScoreEmaMeans[i])/fairnessScoreEmaVars[i];
	    	        }
	            }
	        }
	    }
        
        
        //STEP 3: SUMMARIZE SCORES
        if( Settings.paretoMode) {
        	Vector<ScoreArray> scores = new Vector<ScoreArray>();
        	for( DistrictMap dm : population) {
        		double[] dd = dm.fairnessScores;
        		double popdiff = dm.getMaxPopDiff();
        		double csplits = dm.getSplitCounties().size();

        		scores.add(new ScoreArray(dm,new double[]{
        				dd[3] + (popdiff > 0.5 ? popdiff*100000.0 : 0.0), //contiguous
        				dd[0], //compact
        				popdiff,//dd[2], //equal pop
        				dd[5], //competition
        				dd[7], //fairness
        				dd[8], //proptionality
        				csplits, //splits
        				dd[11], //descr rep
        				dd[12], //spec. asym
        						}));
        	}
        	ScoreArray.NUM_SCORES = 8;
        	ScoreArray.sortByParetoFitness(scores);
        	for( int i = 0; i < scores.size(); i++) {
        		((DistrictMap)scores.get(i).scoredObject).fitness_score = i;
        	}
        	Collections.sort(population);
        	//String s = ScoreArray.listScores(scores);
        	//System.out.println("scores:");
        	//System.out.println(s);
        	String s = ScoreArray.listAllNonDominated(scores);
        	System.out.println("non-dominated:");
        	System.out.println(s);
        
        	
        } else {
	        if( verbosity > 1)
	        	System.out.println("  weighing fairness...");
	        
	        double fairness_weight_multiplier = 1;//0.5;
	        double geometry_weight_multiplier = 1;
	        
	
	        double[] weights = new double[]{
                    Settings.geometry_weight,  //0
                    Settings.disenfranchise_weight,
                    Settings.population_balance_weight, //2
                    Settings.disconnected_population_weight,
                    Settings.voting_power_balance_weight, //4
                    Settings.competitiveness_weight,
                    Settings.wasted_votes_imbalance_weight, //6
                    Settings.seats_votes_asymmetry_weight,
                    Settings.diagonalization_weight, //8
	                Settings.reduce_splits ? Settings.split_reduction_weight : 0,
	                MainFrame.mainframe.project.demographic_columns.size() == 0 ? 0 : Settings.vote_dilution_weight, //10
	                MainFrame.mainframe.project.demographic_columns.size() == 0 ? 0 : Settings.descr_rep_weight, //10
	                0.0,
	        };
	        double geo_total = weights[0]+weights[2]+weights[3]+weights[9];
	        double fair_total = weights[1]+weights[4]+weights[5]+weights[6]+weights[7]+weights[8]+weights[10]+weights[11];
	        
	        double geometric_mult = 2.0*(geometry_weight_multiplier*(1.0-Settings.geo_or_fair_balance_weight)/geo_total);
	        double fairness_mult = fairness_weight_multiplier*(Settings.geo_or_fair_balance_weight)/fair_total;
	        
	        weights = new double[]{
	        		weights[0]*geometric_mult, 
	        		weights[1]*fairness_mult, 
	        		weights[2]*geometric_mult,
	        		weights[3]*geometric_mult,
	        		weights[4]*fairness_mult,
	        		weights[5]*fairness_mult,
	        		weights[6]*fairness_mult,
	        		weights[7]*fairness_mult,
	        		weights[8]*fairness_mult,
	        		weights[9]*geometric_mult,
	        		weights[10]*fairness_mult,
	        		weights[11]*fairness_mult,
	        		weights[12]*fairness_mult,
	        };
	
	        for( int j = 0; j < population.size(); j++) {
	            DistrictMap map = population.get(j);
	            map.fitness_score = 0;
	            for( int i = 0; i < map.fairnessScores.length; i++) {
	            	if( map.fairnessScores[i] != map.fairnessScores[i] || weights[i] == 0) {
	            		map.fairnessScores[i] = 0;
	            	}
	            	map.fairnessScores[i] = map.fairnessScores[i]*weights[i]*invert;
	            	/*
	                if( i == 2 && map.getMaxPopDiff()*100.0 >= Settings.max_pop_diff*0.99) {
	                	map.fairnessScores[i] += map.fairnessScores[i];
	                	//map.fairnessScores[i] += 10;
	                }*/
	                map.fitness_score += map.fairnessScores[i];
	            }
	        }
	        MainFrame.mainframe.panelStats.getNormalizedStats();
	
	        if( verbosity > 1)
	        	System.out.println("  sorting population...");
	
	        Collections.sort(population);
	
	        if( verbosity > 0) {
		        System.out.print("  top score:");
		        DistrictMap top = population.get(0);
				for( int i = 0; i < top.fairnessScores.length; i++) {
					System.out.print(top.fairnessScores[i]+", ");
				}
				System.out.println();
			}
        }
        if( Settings.adaptive_mutation) {
        	for( int i = 0; i < cutoff; i++ ) {
        		DistrictMap dm = population.get(i);
        		if( !dm.wasMutationCounted) {
        			adaptiveMutation.addSample(dm.actual_mutate_rate);
        			dm.wasMutationCounted = true;
        		}
        	}
        	adaptiveMutation.recalculate();
        	Settings.mutation_boundary_rate = Math.exp(-adaptiveMutation.gamma.getNumericalMean());
        	System.out.println("new mutation rate: "+Settings.mutation_boundary_rate
        			+" var "+Math.exp(-Math.sqrt(adaptiveMutation.gamma.getNumericalVariance()))
        			+" ex "+ adaptiveMutation.getSample()
        	);
        	Settings.setMutationRate(Settings.mutation_boundary_rate);
        	
        } else
        
        //INTERMISSION: UPDATE ANNEAL RATE
        if( Settings.auto_anneal) {
	        int total = 2;
	        int mutated = 0;
	        if( population.size() > 0) {
		        for(int i = 0; i < population.size()/3; i++) {
		            DistrictMap dm = population.get(i);
		            total += dm.boundaries_tested;
		            mutated += dm.boundaries_mutated;
		        }
	        }
	        //minimum 3 mutations avg
	        if( mutated < Settings.population || mutated != mutated) {
	        	mutated = Settings.population;
	        }
	        
        	double new_rate = ((double)mutated/(double)total)*0.999; //always at least go down a little (0.1%)
	        if( new_rate < Settings.max_mutation) {
	        	Settings.startAnnealing(generation);
	        }
	        /*
        	if( total != total || new_rate == 0 || new_rate != new_rate) {
        		new_rate = Settings.mutation_boundary_rate;
        	}
        	if( new_rate < Settings.getAnnealingFloor(generation) ){
        		new_rate = Settings.getAnnealingFloor(generation);
        	}
        	if( new_rate > Settings.getAnnealingCeiling(generation) ){
        		new_rate = Settings.getAnnealingCeiling(generation);
        	}
        	Settings.mutation_boundary_rate += (new_rate-Settings.mutation_boundary_rate)*Settings.auto_anneal_Frac;
        	*/
	        //System.out.println("old rate: "+Settings.mutation_boundary_rate);
        	Settings.mutation_boundary_rate *= Settings.getAnnealingPerGeneration();
	        //System.out.println("new rate: "+Settings.mutation_boundary_rate);
        	/*
        	//grow population if under a threshold
        	if( Settings.mutation_boundary_rate < 0.33333/(double)Settings.population) {
        		Settings.mutation_boundary_rate = 0.33333/(double)Settings.population;
        		Settings.setPopulation(Settings.population+1);
        	}
        	*/
        	Settings.setMutationRate(Settings.mutation_boundary_rate);
	        //System.out.println("new rate2:"+Settings.mutation_boundary_rate);
        }
    
        
        //STEP 4: SELECT AND RECOMBINE
        Vector<DistrictMap> available_mate = new Vector<DistrictMap>();
        for(int i = 0; i < cutoff; i++) {
            available_mate.add(population.get(i));
        }
        
        bestMap = population.get(0);

        if( verbosity > 1)
        	System.out.println("  selecting mates... (cutoff: "+cutoff+"  spec_cutoff: "+speciation_cutoff+")");
        if( !Settings.multiThreadMating || cutoff != speciation_cutoff) {
            for(int i = cutoff; i < population.size(); i++) {
                int g1 = (int)(Math.random()*(double)cutoff);
                DistrictMap map1 = available_mate.get(g1);
                if( speciation_cutoff != cutoff) {
                    for(DistrictMap m : available_mate) {
                        if( Settings.mate_merge) {
                            m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(map1.getGenome()), map1.getGenome());
                        } else {
                            m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(), map1.getGenome());
                        }
                    	
                    }
                    Collections.sort(available_mate);
                }
                int g2 = (int)(Math.random()*(double)speciation_cutoff);
                DistrictMap map2 = available_mate.get(g2);

                if( Settings.mate_merge) {
                    population.get(i).crossover(map1.getGenome(), map2.getGenome(map1.getGenome()));
                } else {
                    population.get(i).crossover(map1.getGenome(), map2.getGenome());
                }
            }
        } else {
        	if( Settings.SELECTION_MODE == Settings.RANK_SELECTION) {
        		double inc = 1.0/(double)population.size();
        		double current = 0;
        		double total = 0;
    	        for(int i = population.size()-1; i >= 0; i--) {
    	        	DistrictMap dm = population.get(i);
    	        	current += inc;
    	        	total += current;
    	        	dm.fitness_score = total;
    	        }
    	        for(int i = 0; i < population.size(); i++) {
    	        	DistrictMap dm = population.get(i);
    	        	dm.fitness_score /= total;
    	        }
        	}
        	if( Settings.SELECTION_MODE == Settings.TOURNAMENT_SELECTION) {
        		double exp_per = Settings.tournament_exponent/((double)population.size());
        		double survival_prob = Math.pow(2.0,-exp_per);
        		double select_prob = 1.0-survival_prob;
        		//double current = 1.0;
        		double remainder = 1.0;
        		double total = 0;
    	        for(int i = 0; i < population.size(); i++) {
    	        	DistrictMap dm = population.get(i);
    	        	remainder = Math.pow(2.0,-Settings.tournament_exponent*((double)i)/((double)population.size()));
    	        	double selected = remainder*select_prob;
    	        	remainder = remainder*survival_prob;
    	        	total += selected;
    	        	dm.fitness_score = total;
    	        }
    	        //total += remainder; //should be 1 now.
    	        for(int i = 0; i < population.size(); i++) {
    	        	DistrictMap dm = population.get(i);
    	        	dm.fitness_score /= total;
    	        }
        	}
    		if( Settings.SELECTION_MODE == Settings.ROULETTE_SELECTION) {
        		//reverse it, we want it maximal instead of minimal
        		for( DistrictMap dm : population) {
        			dm.fitness_score = -dm.fitness_score;
        		}
        		
        		double min = population.get(population.size()-1).fitness_score;
        		double total = 0;
        		for( DistrictMap dm : population) {
        			dm.fitness_score -= min;
        			total += dm.fitness_score;
        		}
        		double current = 0;
    	        for(int i = population.size()-1; i >= 0; i--) {
    	        	DistrictMap dm = population.get(i);
    	        	current += dm.fitness_score;
    	        	dm.fitness_score = current/total;
    	        }
        	}
        	if( Settings.SELECTION_MODE != Settings.TRUNCATION_SELECTION) {
    	        while( swap_population.size() < population.size()) {
    	        	swap_population.add(new DistrictMap(vtds,Settings.num_districts));
    	        }
    	        while( swap_population.size() > population.size()) {
    	        	swap_population.remove(0);
    	        }
        	}
		//System.out.print(""+step);
    		for( int j = 0; j < matingThreads.length; j++) {
    			matingThreads[j].available_mate.clear();
    			if( Settings.SELECTION_MODE == Settings.TRUNCATION_SELECTION) {
	    	        for(int i = 0; i < cutoff; i++) {
	    	        	matingThreads[j].available_mate.add(population.get(i));
	    	        }
    			}
    		}
    		matingLatch = new CountDownLatch(num_threads);
    		for( int i = 0; i < matingThreads.length; i++) {
    			matingThreadPool.execute(matingThreads[i]);
    			//iterationThreads[j].start();
    		}
    		try {
    			matingLatch.await();
    		} catch (InterruptedException e) {
    			System.out.println("ex abe "+e);
    			// TODO Auto-generated catch ward
    			e.printStackTrace();
    		}
    		
    		if( Settings.SELECTION_MODE != Settings.TRUNCATION_SELECTION) {
	    		if( cutoff == 0) {
	    			Vector<DistrictMap> temp = population;
	    			population = swap_population;
	    			swap_population = temp;
	    		} else {
	    			for( int i = cutoff; i < population.size(); i++) {
	    				DistrictMap temp1 = population.get(i);
	    				DistrictMap temp2 = swap_population.get(i);
	    				population.set(i, temp2);
	    				swap_population.set(i, temp1);
	    			}
	    			
	    		}
    			
    		} else {
	    		if( cutoff == 0) {
	    			Vector<DistrictMap> temp = new Vector<DistrictMap>();
	    			for(int j = cutoff; j < population.size(); j++) {
	    				temp.add(population.get(j));
	    				population.set(j, new DistrictMap(vtds,Settings.num_districts));
	    			}
	    			
	    	  		for( int j = 0; j < matingThreads.length; j++) {
	        			matingThreads[j].available_mate.clear();
	        	        for(int i = 0; i < cutoff; i++) {
	        	        	matingThreads[j].available_mate.add(population.get(i));
	        	        }
	        		}
	        		matingLatch = new CountDownLatch(num_threads);
	        		for( int i = 0; i < matingThreads.length; i++) {
	        			matingThreadPool.execute(matingThreads[i]);
	        			//iterationThreads[j].start();
	        		}
	        		try {
	        			matingLatch.await();
	        		} catch (InterruptedException e) {
	        			System.out.println("ex abf "+e);
	        			// TODO Auto-generated catch ward
	        			e.printStackTrace();
	        		}
	        		for( int j = 0; j < cutoff && j+cutoff < population.size(); j++) {
	        			population.set(j, temp.get(j));
	        		}
	      
	    		}
    		}
        	
        }

        if( verbosity > 1)
        	System.out.println("  applying mutation...");

        for(int i = /*Settings.mutate_all ? 0 :*/ (int)(((double)cutoff)*(1.0-Settings.elite_mutate_fraction)); i < population.size(); i++) {
            DistrictMap dm = population.get(i);
            if(Settings.mutation_rate > 0)
            	dm.mutate(Settings.mutation_rate);
            if(Settings.mutation_boundary_rate > 0) {
            	if( Settings.adaptive_mutation) {
            		dm.mutate_boundary(adaptiveMutation.getSample());
            		dm.wasMutationCounted = false;
            	} else {
            		dm.mutate_boundary(Settings.mutation_boundary_rate);
            	}
            }
        }
        if( Settings.mutate_disconnected) {
	        for(int i =  0; i < population.size(); i++) {
	            DistrictMap dm = population.get(i);
	        	dm.mutate_all_disconnected(0.25);
	            dm.fillDistrictwards();
	        }
        } else {
        	Thread[] threads = new Thread[population.size()];
	        for(int i =  0; i < population.size(); i++) {
	            DistrictMap dm = population.get(i);
	        	threads[i] = dm.startInitThread();
	        }
	        for(int i =  0; i < population.size(); i++) {
	        	try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
        	
        }
        if( Settings.make_unique) {
        	make_unique();
        }
        if( verbosity > 1)
        	System.out.println("}");
    }
    
    class MatingThread implements Runnable {
    	public int id = 0;
    	public Vector<DistrictMap> available_mate = new Vector<DistrictMap>();
    	public void run() {
    		try {
	    		if( Settings.SELECTION_MODE == Settings.TOURNAMENT_SELECTION) {
		            for(int i = id; i < population.size(); i+=num_threads) {
		            	double d1 = Math.random();
		            	DistrictMap map1 = population.get(0);
		            	for( int j = 0; j <  population.size(); j++) {
		            		if( population.get(j).fitness_score >= d1) {
		            			map1 = population.get(j);
		            			break;
		            		}
		            	}
		            	DistrictMap map2 = population.get(1);
		            	
		            	//no clones
		            	while( map2 == null || map2 == map1) {
			            	double d2 = Math.random();
			            	for( int j = 0; j < population.size(); j++) {
			            		if( population.get(j).fitness_score >= d2) {
			            			map2 = population.get(j);
			            			break;
			            		}
			            	}
		            	}
	                    swap_population.get(i).crossover(map1.getGenome(), map2.getGenome());
		            }
	    		} else 
	    		if( Settings.SELECTION_MODE != Settings.TRUNCATION_SELECTION) {
		            for(int i = id; i < population.size(); i+=num_threads) {
		            	double d1 = Math.random();
		            	DistrictMap map1 = null;
		            	for( int j = population.size()-1; j >=0; j--) {
		            		if( population.get(j).fitness_score >= d1) {
		            			map1 = population.get(j);
		            			break;
		            		}
		            	}
		            	DistrictMap map2 = null;
		            	
		            	//no clones
		            	while( map2 == null || map2 == map1) {
			            	double d2 = Math.random();
			            	for( int j = population.size()-1; j >=0; j--) {
			            		if( population.get(j).fitness_score >= d2) {
			            			map2 = population.get(j);
			            			break;
			            		}
			            	}
		            	}
	                    swap_population.get(i).crossover(map1.getGenome(), map2.getGenome());
		            }
	    		} else {
		            for(int i = cutoff+id; i < population.size(); i+=num_threads) {
		                int g1 = (int)(Math.random()*(double)cutoff);
		                DistrictMap map1 = available_mate.get(g1);
		                if( speciation_cutoff != cutoff) {
		                    for(DistrictMap m : available_mate) {
		                        //m.makeLike(map1.getGenome());
		                        if( Settings.mate_merge) {
		                            m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(map1.getGenome()), map1.getGenome());
		                        } else {
		                            m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(), map1.getGenome());
		                        }
		                    }
		                    try {
		                    	Collections.sort(available_mate);
		                    } catch (Exception ex) {
		                    	ex.printStackTrace();
		                    }
		                }
		                int g2 = (int)(Math.random()*(double)speciation_cutoff);
		                DistrictMap map2 = available_mate.get(g2);
		
		                if( Settings.mate_merge) {
		                    population.get(i).crossover(map1.getGenome(), map2.getGenome(map1.getGenome()));
		                } else {
		                    population.get(i).crossover(map1.getGenome(), map2.getGenome());
		                }
		            }
	    		}
    		} catch (Exception ex) {
    			System.out.println("mating thread ex "+ex);
    			ex.printStackTrace();
    		}

            //System.out.print("o");
    		matingLatch.countDown();
    		
    	}
    }
    public void start_from_genome(int[] genome, double mutation_rate) {
        for( DistrictMap map : population) {
            map.setGenome(genome);
            map.mutate(mutation_rate);
        }
    }



}
