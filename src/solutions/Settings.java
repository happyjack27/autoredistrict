package solutions;

import java.util.Vector;

import serialization.JSONObject;

public class Settings extends serialization.ReflectionJSONObject<Settings> {
	public static int num_maps_to_draw = 1;
	public static boolean use_annealing_floor = true;
	public static int RANK = 0;
	public static int EMA = 1;
	public static int LINEARIZE_MODE = EMA;
	public static int TRUNCATION_SELECTION = 0;
	public static int ROULETTE_SELECTION = 1;
	public static int RANK_SELECTION = 2;
	public static int SELECTION_MODE = TRUNCATION_SELECTION;
    public static boolean mutate_disconnected = false;
    public static double mutate_disconnected_threshold = 0.10;
	public static boolean mutate_excess_pop = false;

	public static boolean adjust_vote_to_population = false;

	public static boolean use_rectangularized_compactness = true;

	
	public static boolean squared_compactness = true;
	public static boolean squared_pop_variance = true;
	
	
	//public static boolean border_length_area_weighted = true;
	public static final boolean make_unique = false; //not the cost of this is population times population times precinct count.
	public static long annealing_starts_at = 0;
	public static boolean annealing_has_started = false;
	public static int members_per_district = 1;
	public static void resetAnnealing() { annealing_has_started = false; }
	public static void startAnnealing(long generation) { if( annealing_has_started) { return; } annealing_starts_at = generation; annealing_has_started = true; }
	public static double getAnnealingFloor(long generation) {
		if( !use_annealing_floor) {
			return 0;
		}
		if( !annealing_has_started) {
			return 0.25;
		}
		generation -= annealing_starts_at;
		double new_rate = 0;
       	if( new_rate <= 0.001) {
    		new_rate = 0.001;
    	}
    	double e = 0.25*Math.exp(-0.001*(double)generation); // reaches -0.0005 0.000005 at 4000
    	if( new_rate < e) {
    		new_rate = e;
    	}
    	return new_rate;
	}
	public static Vector<iChangeListener> populationChangeListeners = new Vector<iChangeListener>();
	public static Vector<iChangeListener> mutation_rateChangeListeners = new Vector<iChangeListener>();
	
	public static double pct_turnover = 0.15; //pct of voters leaving and entering this election cycle. (replaced with 50/50 voters) (or persuadable voters)
	public static double voting_coalition_size = 100.0; //every x voters will be considered 1 independant voter

	public static boolean self_entropy_use_votecount = true;
	public static boolean self_entropy_square_votecount = true;
	
	public static boolean use_new_self_entropy_method = true;
	public static double self_entropy_exponent = 1.0;//2.0;
	public static int voters_to_simulate = 64;
	public static int approximate_binomial_as_normal_at_n_equals = 32;
	public static int binomial_cache_size = 64;
	public static boolean pso = false;
	public static boolean auto_anneal = true;
    public static double auto_anneal_Frac = 0.025;
	public static boolean mate_merge = false;
    public static double species_fraction = 1.0;

	public static boolean multiThreadScoring = true;
	public static boolean multiThreadMating = true;
	public static boolean multiThreadMutatting = true;
	public static boolean mutate_all = true;

    public static boolean mutate_to_neighbor_only = false;

    //spatial metrics
    public static double geometry_weight = 1;
    public static double population_balance_weight = 1;
    public static double disconnected_population_weight = 0; 

    //fairness metrics
    public static double max_pop_diff = 9;
    public static double disenfranchise_weight = 1;
    public static double voting_power_balance_weight = 1;
    public static double geo_or_fair_balance_weight = 1;
    
    public static double wasted_votes_total_weight = 1;
    public static double wasted_votes_imbalance_weight = 1;
    
    //public static double replace_fraction = 0.5;
    public static int getCutoff() {
    	int d = (int)(Math.sqrt(Settings.population)*4.0); 
    	return d < Settings.population/2 ? d : Settings.population/2;
    }
    public static double mutation_rate = 0.5;
    public static double mutation_boundary_rate = 0.5;
    public static int num_elections_simulated = 3;
    public static int population = 64;
    public static int num_districts = 3;
	//geometry
	//demographics
	//settings
	//map_population (folder)
	//
	public static int num_ward_outcomes = 16;
	public static boolean replace_all = false;
	public static void setPopulation(double i) {
		population = (int)i;
		for( iChangeListener c : populationChangeListeners) {
			c.valueChanged();
		}
	}
	public static void setMutationRate(double i) {
		Settings.mutation_boundary_rate = i;
		for( iChangeListener c : mutation_rateChangeListeners) {
			c.valueChanged();
		}
	}
}
