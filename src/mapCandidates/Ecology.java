package mapCandidates;

import java.util.*;

import serializable.*;

public class Ecology extends ReflectionJSONObject<Ecology> {
	//geometry
	//demographics
	//settings
	//map_population (folder)
	//
	int num_districts = 0;
	
	Vector<Candidate> candidates = new Vector<Candidate>();

	Vector<Block> blocks = new Vector<Block>();
	Vector<Edge> edges = new Vector<Edge>();
	Vector<Vertex> vertexes = new Vector<Vertex>();
	
	HashMap<Integer,Block> blocks_by_id = new HashMap<Integer,Block>();
	HashMap<Integer,Edge> edges_by_id = new HashMap<Integer,Edge>();
	HashMap<Integer,Vertex> vertexes_by_id = new HashMap<Integer,Vertex>();

	Settings settings = new Settings();
	
    Vector<DistrictMap> population = new Vector<DistrictMap>();
    


	@Override
	public void post_deserialize() {
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

		//stuff
		if( containsKey("candidates")) {
			candidates = getVector("candidates");
			Block.candidates = candidates;
			DistrictMap.candidates = candidates;
			District.candidates = candidates;
		}
		
		if( containsKey("settings")) { settings = (Settings)get("settings"); }
	}

	@Override
	public void pre_serialize() {		
		put("blocks",blocks);
		put("edges",edges);
		put("vertexes",vertexes);
		
		put("candidates",candidates);
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

    public static int hamming_distance_polarity = 1;

 
    public void reset() {
        DistrictMap.candidates = candidates;
    	population =  new Vector<DistrictMap>();
    }
    public void resize_population() {    	
    	population =  new Vector<DistrictMap>();
        for( int i = population.size(); i < Settings.population; i++) {
            population.add(new DistrictMap(blocks,num_districts));
        }
        for( int i = Settings.population; i > population.size(); i++) {
            population.remove(Settings.population);
        }

    }

    public void evolveWithSpeciation(double replace_fraction, double mutation_rate, double mutation_boundary_rate, int trials, boolean score_all) {
        int cutoff = population.size()-(int)((double)population.size()*replace_fraction);
        int speciation_cutoff = (int)((double)cutoff*Settings.species_fraction);

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


    public void evolve() {
    	boolean score_all = true;
        int cutoff = population.size()-(int)((double)population.size()*Settings.replace_fraction);

        if( score_all) {
            for( DistrictMap map : population) {
                map.calcFairnessScores(Settings.trials);
            }
        } else {
            for( int i = cutoff; i < population.size(); i++) {
                population.get(i).calcFairnessScores(Settings.trials);
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
                map.fitness_score += map.fairnessScores[i]*weights[i];
            }
        }

        Collections.sort(population);


        for(int i = cutoff; i < population.size(); i++) {
            int g1 = (int)(Math.random()*(double)cutoff);
            int g2 = (int)(Math.random()*(double)cutoff);
            population.get(i).crossover(population.get(g1).getGenome(), population.get(g2).getGenome(population.get(g1).getGenome()));
            population.get(i).mutate(Settings.mutation_rate);
            population.get(i).mutate_boundary(Settings.mutation_rate);
        }
    }
    public void start_from_genome(int[] genome, double mutation_rate) {
        for( DistrictMap map : population) {
            map.setGenome(genome);
            map.mutate(mutation_rate);
        }
    }



}
