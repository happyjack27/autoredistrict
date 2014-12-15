package mapCandidates;

import java.util.*;

import javax.swing.JOptionPane;

import serialization.*;
import ui.MapPanel;

public class Ecology extends ReflectionJSONObject<Ecology> {
	//geometry
	//demographics
	//settings
	//map_population (folder)
	//
	static public boolean evolve_paused = true;
	public static double invert = 1;
	int last_population = 0;
	int last_num_districts = 0;
	
	public HashMap<Integer,Block> blocks_by_id;
	
	public Vector<Block> blocks = new Vector<Block>();
	public Vector<Edge> edges = new Vector<Edge>();
	public Vector<Vertex> vertexes = new Vector<Vertex>();
	public MapPanel mapPanel;
	

	Settings settings = new Settings();
	
    public Vector<DistrictMap> population = new Vector<DistrictMap>();
    
    public EvolveThread evolveThread; 
    
    class EvolveThread extends Thread {
    	public void run() {
    		while( !evolve_paused) {
    			try {
    				//System.out.println("last_num_districts "+last_num_districts+" Settings.num_districts "+Settings.num_districts);
    				//System.out.println("population.size() "+population.size()+" Settings.population "+Settings.population);
        			if( last_num_districts != Settings.num_districts) {
        				//if( JOptionPane.showConfirmDialog(null, "resize districts?") == JOptionPane.YES_OPTION) {
            				System.out.println("Adjusting district count from "+last_num_districts+" to "+Settings.num_districts+"...");
            				resize_districts();
        				//}
        			}
        			if( population.size() != Settings.population) {
        				//if( JOptionPane.showConfirmDialog(null, "resize population?") == JOptionPane.YES_OPTION) {
            				System.out.println("Adjusting population from "+population.size()+" to "+Settings.population+"...");
                			resize_population();
        				//}
        			}
        			evolveWithSpeciation(); 
        			System.out.print(".");
        			
        			if( mapPanel != null) {
        				mapPanel.invalidate();
        				mapPanel.repaint();
        			}
        			
    			} catch (Exception ex) {
    				System.out.println("ex "+ex);
    				ex.printStackTrace();
    			}
    		}
    	}
    }
    
	public void startEvolving() {
		if( !evolve_paused) {
			return;
		}
		if( evolveThread != null) {
			try {
				evolveThread.stop();
				//evolveThread.destroy();
				evolveThread = null;
			} catch (Exception ex) { }
		}
		evolve_paused= false;
		evolveThread = new EvolveThread();
		evolveThread.start();
	}
    
	
	public void stopEvolving() {
		evolve_paused= true;
	}



	@Override
	public void post_deserialize() {
		blocks_by_id = new HashMap<Integer,Block>();
		HashMap<Integer,Edge> edges_by_id = new HashMap<Integer,Edge>();
		HashMap<Integer,Vertex> vertexes_by_id = new HashMap<Integer,Vertex>();
		
		//geometry
		if( containsKey("blocks")) {
			blocks = getVector("blocks");
			for( Block block: blocks) {
				blocks_by_id.put(block.id,block);
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
				edge.block1 = blocks_by_id.get(edge.block1_id);
				edge.block2 = blocks_by_id.get(edge.block2_id);
				edge.vertex1 = vertexes_by_id.get(edge.vertex1_id);
				edge.vertex2 = vertexes_by_id.get(edge.vertex2_id);
				edge.block1.edges.add(edge);
				edge.block2.edges.add(edge);
			}
		}
		for( Block b : blocks) {
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
		put("blocks",blocks);
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
		if( key.equals("blocks")) {
			return new Block();
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
    	population =  new Vector<DistrictMap>();
    }
    public void resize_population() {
    	if( population == null) {
    		population =  new Vector<DistrictMap>();
    	}
        while( population.size() < Settings.population) {
            population.add(new DistrictMap(blocks,Settings.num_districts));
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

    public void evolveWithSpeciation() {
        int cutoff = population.size()-(int)((double)population.size()*Settings.replace_fraction);
        int speciation_cutoff = (int)((double)cutoff*Settings.species_fraction);

        for( DistrictMap map : population) {
            map.calcFairnessScores(Settings.trials);
        }
        for( int i = 0; i < 5; i++) {
            for( DistrictMap map : population) {
                map.fitness_score = map.fairnessScores[i];
            }
            Collections.sort(population);
            double mult = 1.0/(double)population.size();
            for( int j = 0; j < population.size(); j++) {
                DistrictMap map = population.get(j);
                map.fairnessScores[i] = ((double)j)*mult; 
            }
        }

        double[] weights = new double[]{
        		Settings.geometry_weight, 
        		Settings.disenfranchise_weight, 
        		Settings.population_balance_weight,
                Settings.disconnected_population_weight,
                Settings.voting_power_balance_weight
        };

        for( int j = 0; j < population.size(); j++) {
            DistrictMap map = population.get(j);
            map.fitness_score = 0;
            for( int i = 0; i < 5; i++) {
            	if( map.fairnessScores[i] != map.fairnessScores[i] || weights[i] == 0) {
            		map.fairnessScores[i] = 0;
            	}
                map.fitness_score += map.fairnessScores[i]*weights[i]*invert;
            }
        }

        Collections.sort(population);

        Vector<DistrictMap> available_mate = new Vector<DistrictMap>();
        for(int i = 0; i < cutoff; i++) {
            available_mate.add(population.get(i));
        }

        for(int i = cutoff; i < population.size(); i++) {
            int g1 = (int)(Math.random()*(double)cutoff);
            DistrictMap map1 = available_mate.get(g1);
            for(DistrictMap m : available_mate) {
                m.makeLike(map1.getGenome());
                m.fitness_score = DistrictMap.getGenomeHammingDistance(m.getGenome(), map1.getGenome());
            }
            Collections.sort(available_mate);
            int g2 = (int)(Math.random()*(double)speciation_cutoff);
            DistrictMap map2 = available_mate.get(g2);

            population.get(i).crossover(map1.getGenome(), map2.getGenome());
        }
        for(int i = 0; i < population.size(); i++) {
            DistrictMap dm = population.get(i); 
            dm.mutate(Settings.mutation_rate);
            dm.mutate_boundary(Settings.mutation_rate);
            dm.fillDistrictBlocks();
        }
    }


    public void evolve() {
    	boolean score_all = true;
        int cutoff = population.size()-(int)((double)population.size()*Settings.replace_fraction);
        System.out.println("replace fraction "+Settings.replace_fraction+" cutoff "+cutoff+" population "+population.size());

        if( score_all) {
            for( DistrictMap map : population) {
                map.calcFairnessScores(Settings.trials);
            }
        } else {
            for( int i = cutoff; i < population.size(); i++) {
                population.get(i).calcFairnessScores(Settings.trials);
            }
        }
        
        double mult = 1.0/(double)population.size();
        for( int i = 0; i < 5; i++) {
            for( DistrictMap map : population) {
                map.fitness_score = map.fairnessScores[i];
            }
            Collections.sort(population);
            for( int j = 0; j < population.size(); j++) {
                DistrictMap map = population.get(j);
                map.fairnessScores[i] = ((double)j)*mult; 
            }
        }

        double[] weights = new double[]{
        		Settings.geometry_weight, 
        		Settings.disenfranchise_weight, 
        		Settings.population_balance_weight,
        		Settings.disconnected_population_weight,
        		Settings.voting_power_balance_weight
        };

        for( int j = 0; j < population.size(); j++) {
            DistrictMap map = population.get(j);
            map.fitness_score = 0;
            for( int i = 0; i < 5; i++) {
            	if( map.fairnessScores[i] != map.fairnessScores[i] || weights[i] == 0) {
            		map.fairnessScores[i] = 0;
            	}
                map.fitness_score += map.fairnessScores[i]*weights[i]*invert;
            }
        }

        Collections.sort(population);
        System.out.println("best: "+population.get(0).fitness_score);
        System.out.println("worst: "+population.get(population.size()-1).fitness_score);


        for(int i = cutoff; i < population.size(); i++) {
            int g1 = (int)(Math.random()*(double)cutoff);
            int g2 = (int)(Math.random()*(double)cutoff);
            DistrictMap dm = population.get(i); 
            dm.crossover(population.get(g1).getGenome(), population.get(g2).getGenome(population.get(g1).getGenome()));
            //dm.mutate(Settings.mutation_rate);
            //dm.mutate_boundary(Settings.mutation_rate);
            //dm.fillDistrictBlocks();
        }
        for(int i = 0; i < population.size(); i++) {
            DistrictMap dm = population.get(i); 
            dm.mutate(Settings.mutation_rate);
            dm.mutate_boundary(Settings.mutation_rate);
            dm.fillDistrictBlocks();
        }
    }
    public void start_from_genome(int[] genome, double mutation_rate) {
        for( DistrictMap map : population) {
            map.setGenome(genome);
            map.mutate(mutation_rate);
        }
    }



}
