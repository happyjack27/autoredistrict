package solutions;

import geography.Feature;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import serialization.*;
import ui.MainFrame;
//import ui.MapPanel;
//import ui.PanelStats;

public class Ecology extends ReflectionJSONObject<Ecology> {
	
	public Vector<iDiscreteEventListener> evolveListeners = new Vector<iDiscreteEventListener>();
	
	public static Vector<double[]> history = new Vector<double[]>();

	static public DistrictMap bestMap = null;
	
	static int verbosity = 0;
	static boolean initial_mate_merge = false;
	public ScoringThread[] scoringThreads;
	public ExecutorService scoringThreadPool;
	public CountDownLatch scoringLatch;

	public MatingThread[] matingThreads;
	public ExecutorService matingThreadPool;
	public CountDownLatch matingLatch;

	static int num_threads = 8;

    int cutoff;
    int speciation_cutoff;

	static public boolean evolve_paused = true;
	public static double invert = 1;
	int last_population = 0;
	int last_num_districts = 0;
	
	public HashMap<Integer,Ward> wards_by_id;
	
	public Vector<Ward> wards = new Vector<Ward>();
	public Vector<Edge> edges = new Vector<Edge>();
	public Vector<Vertex> vertexes = new Vector<Vertex>();
	//public MapPanel mapPanel;
	//public PanelStats statsPanel;
	

	Settings settings = new Settings();
	
    public Vector<DistrictMap> population = new Vector<DistrictMap>();
    
    public EvolveThread evolveThread; 
    public long generation = 0;
    
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
        			evolveWithSpeciation(); 
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
    				System.out.println("ex "+ex);
    				ex.printStackTrace();
    			}
    		}
    	}
    }
    
	public void startEvolving() {
		resize_population();
		if( !evolve_paused) {
			return;
		}
		Feature.display_mode = 0;

		if( evolveThread != null) {
			try {
				evolveThread.stop();
				//evolveThread.destroy();
				evolveThread = null;
			} catch (Exception ex) { }
		}
		resize_population();
		evolve_paused = false;
		evolveThread = new EvolveThread();
		evolveThread.start();
	}
    
	
	public void stopEvolving() {
		evolve_paused= true;
	}



	@Override
	public void post_deserialize() {
		wards_by_id = new HashMap<Integer,Ward>();
		HashMap<Integer,Edge> edges_by_id = new HashMap<Integer,Edge>();
		HashMap<Integer,Vertex> vertexes_by_id = new HashMap<Integer,Vertex>();
		
		//geometry
		if( containsKey("wards")) {
			wards = getVector("wards");
			for( Ward ward: wards) {
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
		for( Ward b : wards) {
			b.collectNeighbors();
		}

		//stuff
		if( containsKey("candidates")) {
			Candidate.candidates = getVector("candidates");
		}
		
		if( containsKey("settings")) { settings = (Settings)get("settings"); }
	}

	@Override
	public void pre_serialize() {		
		put("wards",wards);
		put("edges",edges);
		put("vertexes",vertexes);
		
		put("candidates",Candidate.candidates);
	}

	@Override
	public JSONObject instantiateObject(String key) {
		if( key.equals("vertexes")) {
			return new Vertex();
		}
		if( key.equals("edges")) {
			return new Edge();
		}
		if( key.equals("wards")) {
			return new Ward();
		}
		
		if( key.equals("candidates")) {
			return new Candidate();
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
    	for( Ward b : wards) {
    		b.recalcMuSigmaN();
    	}
    }
    public void match_population() {
    	
    	int[] template = population.get(0).getGenome();
    	for( DistrictMap dm : population) {
    		dm.makeLike(template);
    	}
    }
    public void resize_population() {
    	if( population == null) {
    		population =  new Vector<DistrictMap>();
    	}
        while( population.size() < Settings.population) {
            population.add(new DistrictMap(wards,Settings.num_districts));
        }
        while( population.size() > Settings.population) {
            population.remove(Settings.population);
        }
        last_population = Settings.population;
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
                map.calcFairnessScores();
            }
            //System.out.print(".");
    		scoringLatch.countDown();
    		
    	}
    }
    int step = 0;
    public void evolveWithSpeciation() {
        cutoff = Settings.getCutoff();//population.size()-(int)((double)population.size()*Settings.replace_fraction);
        speciation_cutoff = (int)((double)cutoff*Settings.species_fraction);
        if( verbosity > 1) {
        	System.out.println("evolving {");
        } else if (verbosity == 1) {
        	System.out.print(".");
        	step++;
        	if( step % 100 == 0) {
        		System.out.println();
        	}
        }

        
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
    			System.out.println("ex");
    			// TODO Auto-generated catch ward
    			e.printStackTrace();
    		}
        }
        
        if( verbosity > 1)
        	System.out.println("");
    	
        if( verbosity > 1)
        	System.out.println("  renormalizing fairness...");
        for( int i = 0; i < 7; i++) {
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
                if( map.fairnessScores[i] != 0) {
                	map.fairnessScores[i] = ((double)j)*mult;
                }
            }
        }

        if( verbosity > 1)
        	System.out.println("  weighing fairness...");

        double[] weights = new double[]{
        		Settings.geometry_weight*3.0, 
        		Settings.disenfranchise_weight*1.00, 
        		Settings.population_balance_weight*1.0,//*2.0,
                Settings.disconnected_population_weight*3.0,//1.5,
                Settings.voting_power_balance_weight*1.0,
                Settings.wasted_votes_total_weight*1.0,
                Settings.wasted_votes_imbalance_weight*1.0,
        };

        for( int j = 0; j < population.size(); j++) {
            DistrictMap map = population.get(j);
            map.fitness_score = 0;
            for( int i = 0; i < 7; i++) {
            	if( map.fairnessScores[i] != map.fairnessScores[i] || weights[i] == 0) {
            		map.fairnessScores[i] = 0;
            	}
                map.fitness_score += map.fairnessScores[i]*weights[i]*invert;
                if( i == 2 && map.getMaxPopDiff()*100.0 >= Settings.max_pop_diff*0.98) {
                    map.fitness_score += map.fairnessScores[i]*weights[i]*invert*1.0;
                	map.fitness_score += 5;
                }
            }
        }

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
        if( Settings.auto_anneal) {
	        int total = 2;
	        int mutated = 0;
	        if( population.size() > 0) {
		        for(int i = 0; i < cutoff; i++) {
		            DistrictMap dm = population.get(i);
		            total += dm.boundaries_tested;
		            mutated += dm.boundaries_mutated;
		        }
	        }
	        //minimum 3 mutations
	        if( mutated < Settings.population*3.0 || mutated != mutated) {
	        	mutated = (int) (Settings.population*3.0);
	        }
        	double new_rate = ((double)mutated/(double)total)*0.995;
	        if( new_rate < 0.25) {
	        	Settings.startAnnealing(generation);
	        }

        	if( total != total || new_rate == 0 || new_rate != new_rate) {
        		new_rate = Settings.mutation_boundary_rate;
        	}
        	if( new_rate < Settings.getAnnealingFloor(generation) ){
        		new_rate = Settings.getAnnealingFloor(generation);
        	}
        	Settings.mutation_boundary_rate = Settings.mutation_boundary_rate*(1.0-Settings.auto_anneal_Frac) + new_rate*Settings.auto_anneal_Frac;
        	//grow population if under a threshold
        	if( Settings.mutation_boundary_rate < 0.33333/(double)Settings.population) {
        		Settings.mutation_boundary_rate = 0.33333/(double)Settings.population;
        		Settings.setPopulation(Settings.population+1);
        	}
        	Settings.setMutationRate(Settings.mutation_boundary_rate);
        }
        

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
		//System.out.print(""+step);
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
    			System.out.println("ex");
    			// TODO Auto-generated catch ward
    			e.printStackTrace();
    		}
    		if( Settings.replace_all) {
    			Vector<DistrictMap> temp = new Vector<DistrictMap>();
    			for(int j = cutoff; j < population.size(); j++) {
    				temp.add(population.get(j));
    				population.set(j, new DistrictMap(wards,Settings.num_districts));
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
        			System.out.println("ex");
        			// TODO Auto-generated catch ward
        			e.printStackTrace();
        		}
        		for( int j = 0; j < cutoff && j+cutoff < population.size(); j++) {
        			population.set(j, temp.get(j));
        		}
      
    		}
        	
        }

        if( verbosity > 1)
        	System.out.println("  applying mutation...");

        for(int i = Settings.mutate_all || Settings.replace_all ? 0 : cutoff; i < population.size(); i++) {
            DistrictMap dm = population.get(i);
            if(Settings.mutation_rate > 0)
            	dm.mutate(Settings.mutation_rate);
            if(Settings.mutation_boundary_rate > 0) {
            	dm.mutate_boundary(Settings.mutation_boundary_rate);
            }
            dm.fillDistrictwards();
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
