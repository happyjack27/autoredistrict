package mapCandidates;

import java.util.*;

import serializable.*;

public class Ecology extends ReflectionJSONObject<Ecology> {
	
	Vector<Candidate> candidates = new Vector<Candidate>();

	Vector<Block> blocks = new Vector<Block>();
	Vector<District> districts = new Vector<District>();
	Vector<DistrictMap> districtmaps = new Vector<DistrictMap>();

	Vector<Edge> edges = new Vector<Edge>();
	Vector<Vertex> vertexes = new Vector<Vertex>();

	Settings settings = new Settings();

	@Override
	public void post_deserialize() {
		if( containsKey("districts")) { districts = getVector("districts"); }
		if( containsKey("candidates")) { candidates = getVector("candidates"); }
		if( containsKey("blocks")) { blocks = getVector("blocks"); }
		if( containsKey("edges")) { edges = getVector("edges"); }
		if( containsKey("vertexes")) { vertexes = getVector("vertexes"); }
		if( containsKey("settings")) { settings = (Settings)get("settings"); }
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		put("districts",districts);
		put("candidates",candidates);
		put("blocks",blocks);
		put("edges",edges);
		put("vertexes",vertexes);
		// TODO Auto-generated method stub
		
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
		if( key.equals("districts")) {
			return new District();
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
        
        DistrictMap.candidates = candidates;
        
        population =  new Vector<DistrictMap>();
        for( int i = 0; i < population_size; i++) {
            population.add(new DistrictMap(blocks,districts.size()));
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
