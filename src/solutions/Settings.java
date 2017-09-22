package solutions;

import geography.VTD;

import java.util.Vector;

import serialization.JSONObject;
import ui.MainFrame;

public class Settings extends serialization.ReflectionJSONObject<Settings> {
	
	public static boolean paretoMode = false;
	public static boolean national_map = false;
	public static double uncontested_threshold = 0.02;
	public static int num_maps_to_draw = 1;
	public static boolean use_annealing_floor = true;
	public static int RANK = 0;
	public static int EMA = 1;
	public static int NORMALIZE_MODE = RANK;
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
	public static final double density_multiplier = 2.522667664609363E11/(double)1000000.0;
														// population times
														// population times
														// precinct count.
	public static long annealing_starts_at = 0;
	public static boolean annealing_has_started = false;
	public static int seats_in_district(int n) {
		if( seats_mode == 0) {
			//System.out.println(" seats_in_district 0 mode: "+seats_number_per_district);
			return seats_number_per_district;
		} else {
			int test = 0;
			//System.out.println("seats_number_total: "+seats_number_total+" looking for district: "+n);
			int[] sc = getSeatDistribution(seats_number_total);
			for(int i = sc.length-1; i > 0; i--) {
				test += sc[i];
				//System.out.println("n: "+n+" i: "+i+" sc[i]: "+sc[i]+" test: "+test+" n: "+n);
				if( n < test) {
					//System.out.println(" seats_in_district test: "+test+" "+n+": "+i);
					return i;//sc[i];
				}
			}
			//System.out.println(" seats_in_district fall "+seats_number_total+" "+test+" "+n+" : 1");
			return 1;
		}
	}
	public static int total_seats() {
		if( seats_mode == 1) {
			//System.out.println("total_seats 1 mode: "+seats_number_total);
			return seats_number_total;
		} else {
			//System.out.println("total_seats 0 mode: "+(num_districts*seats_number_per_district));
			return num_districts*seats_number_per_district;
		}
		/*
		int seats = 0;
		for( int i = 0; i < num_districts; i++) {
			seats += seats_in_district(i);
		}
		return seats;
		*/
	}
	public static int SEATS_MODE_PER_DISTRICT = 0;
	public static int SEATS_MODE_TOTAL = 1;
	public static int seats_mode = SEATS_MODE_PER_DISTRICT;
	public static int seats_number_per_district = 1;
	public static int seats_number_total = 1;
	
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
	public static double anneal_rate = 1.0;
	public static double getAnnealingCeiling(long generation) {
		return getAnnealingFloor(generation)*1.2;
	}
	public static double getAnnealingPerGeneration() {
		double e = Math.exp(-0.0010 * anneal_rate ); // reaches
		return e;
	}
	public static double getAnnealingFloor(long generation) {
		if( true) {
			return 0;
		}
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
		
		//adjust anneal rate to be proportional to the logarithm of the number of total possible maps.
		double num_features = MainFrame.mainframe.featureCollection.features.size();
		double combinations = num_features * Math.log((double)Settings.num_districts);
		double g = generation;
		g *= (30484.02508579287/combinations);

		double e = max_mutation * Math.exp(-0.0006 * (double) g); // reaches
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
	public static double disenfranchise_weight = 0;
	public static double voting_power_balance_weight = 1;
	public static double geo_or_fair_balance_weight = 1;

	public static double competitiveness_weight = 1;
	public static double wasted_votes_imbalance_weight = 0;
	public static double seats_votes_asymmetry_weight = 1;
	public static double diagonalization_weight = 0;

	public static double split_reduction_weight = 0;
	public static double vote_dilution_weight = 0;
	// public static double replace_fraction = 0.5;

	public static double mutation_rate = 0.5;
	public static double mutation_boundary_rate = 0.5;
	public static int population = 200;
	public static int num_elections_simulated = 1;
	public static int num_districts = 4;
	// geometry
	// demographics
	// settings
	// map_population (folder)
	//
	public static int num_ward_outcomes = 16;
	public static double elite_fraction = 0.33;
	public static double unpaired_edge_length_weight = 0.75;
	public static boolean ignore_uncontested = false;
	public static boolean substitute_uncontested = false;
	public static boolean reduce_splits;

	public static void setPopulation(double i) {
		population = (int) i;
		for (iChangeListener c : populationChangeListeners) {
			c.valueChanged();
		}
	}

	public static void setMutationRate(double i) {
		Settings.mutation_boundary_rate = i;
		hush_mutate_rate = true;
		for (iChangeListener c : mutation_rateChangeListeners) {
			c.valueChanged();
		}
		hush_mutate_rate = false;
	}
	public static int[] getSeatDistribution_cached = null;
	public static int getSeatDistribution_last_seats = -1;
	
	private static boolean no4s = true;
	public static boolean population_is_per_seat = true;
	public static boolean b_make_simplified_polys = false;
	public static boolean minimize_number_of_counties_split = false;
	public static double elite_mutate_fraction = 1;
	public static double exp_mutate_factor = 10.0;
	public static boolean minimize_absolute_deviation = false;
	public static boolean hush_mutate_rate = false;
	public static double fv_pvi_adjust = -0.0385;
	public static int QUOTA_METHOD_DROOP = 1;
	public static int QUOTA_METHOD_HARE = 2;
	public static int quota_method = QUOTA_METHOD_DROOP;
	public static double descr_rep_weight;
	public static boolean divide_packing_by_area = true;
	public static boolean prefer4s = false;
	public static boolean recombination_on = true;
	public static void setNo4s(boolean b) {
		if( no4s == b) {
			return;
		}
		no4s = b;
		getSeatDistribution_last_seats = -1;
	}
	public static int[] getSeatDistribution(int seats) {
		if( seats == 7) {
			return new int[]{0,0,0,1,1,0};
		}
		if( seats <= 5) {
			int[] iseats =  new int[]{0,0,0,0,0,0};
			iseats[seats] = 1;
			return iseats;
		}
		if( seats != getSeatDistribution_last_seats) {
			int sm5 = seats % 5;
			int s5 = (seats-sm5) / 5;
			int s4 = 0;
			int s3 = 0;
			if( (no4s && seats != 7) || (true && seats != 7)) {
				switch( sm5) {
				case 0:
					s5 -= 0;
					s3 = 0;
					break;
				case 1:
					s5 -= 1;
					s3 = 2;
					break;
				case 2:
					s5 -= 2;
					s3 = 4;
					break;
				case 3:
					s5 -= 0;
					s3 = 1;
					break;
				case 4:
					s5 -= 1;
					s3 = 3;
					break;
				}
			} else {
				switch( sm5) {
				case 0:
					s5 -= 0;
					s4 = 0;
					s3 = 0;
					break;
				case 1:
					s5 -= 1;
					s4 = 0;
					s3 = 2;
					break;
				case 2:
					s5 -= 1;
					s4 = 1;
					s3 = 1;
					break;
				case 3:
					s5 -= 0;
					s4 = 0;
					s3 = 1;
					break;
				case 4:
					s5 -= 0;
					s4 = 1;
					s3 = 0;
					break;
				}				
			}
			if( prefer4s ) {
				if( s5 > 0 && s3 > 0) {
					s5--;
					s3--;
					s4+=2;
				}
			}
			int tot = s5+s4+s3;
			int totseat = s5*5+s4*4+s3*3;
			getSeatDistribution_cached = new int[]{0,0,0,s3,s4,s5};
			getSeatDistribution_last_seats = seats;
			//System.out.println("s5: "+s5+" s4: "+s4+" s3: "+s3+" tot: "+tot+" totseat: "+totseat);
		}
		return getSeatDistribution_cached;
	}
	public static void setNationalMap(boolean national) {
		national_map = national;
		MainFrame.mainframe.getMinMaxXY();
		MainFrame.mainframe.resetZoom();
		for( VTD f : MainFrame.mainframe.featureCollection.features) {
			f.geometry.makePolys();
		}
		MainFrame.mainframe.mapPanel.invalidate();
		MainFrame.mainframe.mapPanel.repaint();
	}
}
