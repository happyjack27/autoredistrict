package solutions;

import java.util.Vector;

import serialization.JSONObject;

public class Settings extends serialization.ReflectionJSONObject<Settings> {
	public static double uncontested_threshold = 0.01;
	public static int num_maps_to_draw = 1;
	public static boolean use_annealing_floor = true;
	public static int RANK = 0;
	public static int EMA = 1;
	public static int LINEARIZE_MODE = EMA;
	public static int TRUNCATION_SELECTION = 0;
	public static int ROULETTE_SELECTION = 1;
	public static int RANK_SELECTION = 2;
	public static int TOURNAMENT_SELECTION = 3;
	public static int SELECTION_MODE = TRUNCATION_SELECTION;
	public static double tournament_exponent = 5;
	public static double tournament_exponent_max = 10;
	public static boolean mutate_disconnected = false;
	public static double mutate_disconnected_threshold = 0.25;
	public static boolean mutate_excess_pop = false;
	public static boolean mutate_competitive = false;
	public static boolean mutate_overpopulated = false;
	public static boolean mutate_compactness = false;
	public static boolean mutate_good = false;

	public static boolean mutate_compactness_working = false;

	public static boolean adjust_vote_to_population = false;

	public static boolean use_rectangularized_compactness = true;

	public static boolean squared_compactness = true;
	public static boolean squared_pop_variance = true;

	// public static boolean border_length_area_weighted = true;
	public static final boolean make_unique = false; // not the cost of this is
														// population times
														// population times
														// precinct count.
	public static long annealing_starts_at = 0;
	public static boolean annealing_has_started = false;
	public static int members_per_district = 1;
	public static int num_candidates = 2;

	public static void resetAnnealing() {
		annealing_has_started = false;
	}

	public static void startAnnealing(long generation) {
		if (annealing_has_started) {
			return;
		}
		annealing_starts_at = generation;
		annealing_has_started = true;
	}

	public static double max_mutation = 0.20;

	public static double getAnnealingFloor(long generation) {
		if (!use_annealing_floor) {
			return 0;
		}
		if (!annealing_has_started || generation < 0) {
			return max_mutation;
		}
		generation -= annealing_starts_at;
		double new_rate = 0;
		if (new_rate <= 0.0001) {
			new_rate = 0.0001;
		}
		double e = max_mutation * Math.exp(-0.000666 * (double) generation); // reaches
																				// -0.0005
																				// 0.000005
																				// at
																				// 4000
		if (new_rate < e) {
			new_rate = e;
		}
		if (new_rate > 0.5) {
			return 0.5;
		}
		return new_rate;
	}

	public static Vector<iChangeListener> populationChangeListeners = new Vector<iChangeListener>();
	public static Vector<iChangeListener> mutation_rateChangeListeners = new Vector<iChangeListener>();

	public static double pct_turnover = 0.15; // pct of voters leaving and
												// entering this election cycle.
												// (replaced with 50/50 voters)
												// (or persuadable voters)
	public static double voting_coalition_size = 100.0; // every x voters will
														// be considered 1
														// independant voter

	public static boolean self_entropy_use_votecount = true;
	public static boolean self_entropy_square_votecount = true;

	public static boolean use_new_self_entropy_method = true;
	public static double self_entropy_exponent = 1.0;// 2.0;
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

	// spatial metrics
	public static double geometry_weight = 1;
	public static double population_balance_weight = 1;
	public static double disconnected_population_weight = 0;

	// fairness metrics
	public static double max_pop_diff = 100;
	public static double disenfranchise_weight = 1;
	public static double voting_power_balance_weight = 1;
	public static double geo_or_fair_balance_weight = 1;

	public static double wasted_votes_total_weight = 1;
	public static double wasted_votes_imbalance_weight = 0;
	public static double seats_votes_asymmetry_weight = 1;

	// public static double replace_fraction = 0.5;

	public static double mutation_rate = 0.5;
	public static double mutation_boundary_rate = 0.5;
	public static int population = 64;
	public static int num_elections_simulated = 3;
	public static int num_districts = 4;
	// geometry
	// demographics
	// settings
	// map_population (folder)
	//
	public static int num_ward_outcomes = 16;
	public static double elite_fraction = 0.333;
	public static double unpaired_edge_length_weight = 0.5;
	public static boolean ignore_uncontested = false;
	public static boolean substitute_uncontested = false;

	public static void setPopulation(double i) {
		population = (int) i;
		for (iChangeListener c : populationChangeListeners) {
			c.valueChanged();
		}
	}

	public static void setMutationRate(double i) {
		Settings.mutation_boundary_rate = i;
		for (iChangeListener c : mutation_rateChangeListeners) {
			c.valueChanged();
		}
	}
}
