package util.ballot_counters;

import java.util.*;


// https://en.wikipedia.org/wiki/Consistency_criterion - LOTS!

/*
 * TODO:
 * A accumulate
 * B sort all combos
 * 
 * A fewest first choices
 * B fewest rank demotions
 * 
 * TODO:
 * C lowest total borda count
 * D fewest affected ballots, then fewest rank demotions
 * E fewest rank demotions, then fewest affected ballots 
 * 
 * a - most candidates affected <- minimizes the likilihood of one of them having won.
 * b - least candidates affected
 * c - ignore # of candidates
 * 
 * test hare with restore.
 * a - accumulate eliminates
 *  A - lowest borda count
 *  B - 
 * b - sort all combinations
 * 
 * 
 * 
 * sort criteria:
 * a - borda count (minimal)
 * b - rank demotions from first (similiar to borda) (same as # of first place votes affected) (minimal)
 * c - affected ballots (minimal)
 * d - candidates eliminated (maximal, n/a if eliminates are cumulative)
 * 
 * so, at most 4! combinations of sort criteria (first criteria, second criteria...) 
 * 
 */

public class STVElection2 {
	static boolean elected_is_demotion = false;
	static boolean use_new_ignore_method = true;
	static boolean include_ignores_on_dynamic_quota = false;
	
	static boolean use_droop = false;
	static boolean reset_ignores = true;
	static boolean ignore_fewest_votes = false;
	
	static boolean test_all = true;

	public static void main(String[] args) {
		if( true) {
			for( int i = 0; i < 43; i++) {
				main2(i);
				log2("");
			}
			System.out.println("--al--");
		} else {
			main2(0);
		}
		System.out.print(reasoning2);
	}

	
	public static int[] sort_orders = new int[]{3,2,0,1};
	//0321
	//3210 -prolly best
	//3201
	//3021
	//3>2>1 needed to pass borda
	public static double getSortRank(Vector<STVBallot> ballots, int[] elected, int[] ignored, int num_candidates) {
		double[] raw_scores = new double[]{
				countCandidatesForCombination( ballots, elected, ignored, num_candidates),
				countBordaValueForCombination( ballots, elected, ignored, num_candidates),
				countAffectedBallotsForCombination( ballots, elected, ignored, num_candidates),
				countDemotionsForCombination( ballots, elected, ignored, num_candidates),
				//countDemotionsForCombination( ballots, elected, ignored, num_candidates)+countAffectedBallotsForCombination( ballots, elected, ignored, num_candidates),
		};
		double num_ballots = 0;
		for( int i = 0; i < ballots.size(); i++) {
			num_ballots += ballots.get(i).weight;
		}
		double[] multipliers = new double[]{
				num_candidates,
				num_candidates*num_ballots,
				num_ballots,
				num_candidates*num_ballots,
				//num_ballots+num_candidates*num_ballots,
		};
		/*
		double[] multipliers = new double[]{
				num_candidates,
				num_candidates*ballots.size(),
				ballots.size(),
				num_candidates*ballots.size(),
		};
		*/
		//204.0
		/*
		for( int i = 0; i < ignored.length; i++) {
			System.out.print(ignored[i]+" ");
		}
		System.out.println(":" +raw_scores[0]+" "+raw_scores[1]+" "+raw_scores[2]+" "+raw_scores[3]+" ");
		*/
		double score = 0;
		for( int i = 0; i < sort_orders.length; i++) {
			int s = sort_orders[i];
			score = score*multipliers[s]+raw_scores[s];
			//score = (score+raw_scores[s])*multipliers[s];
		}
		
		//return raw_scores[3]*multipliers[3];
		return score;
	}

	public static String reasoning = "";
	public static String reasoning2 = "";
	
	public static void log(String s) {
		reasoning += s+"\n";
		//System.out.println(s);
	}
	public static void logw(String s) {
		reasoning += s;
		//System.out.print(s);
	}
	public static void log2(String s) {
		reasoning2 += s+"\n";
		//System.out.println(s);
	}
	public static void logw2(String s) {
		reasoning2 += s;
		//System.out.print(s);
	}


	public static void main2(int s) {
		int scenario = s;
		//3 is a good example
		
		/*
		int[] nn = new int[20];
		for( int i = 0; i < nn.length; i++) {
			nn[i] = i;
		}
		getAllCombinations(nn);
		System.exit(0);
		*/
		Vector<STVBallot> ballots = new Vector<STVBallot>();
		int seats = 0;
		int candidates = 0;

		//a,b  a,r,b
		//a,b
		
		/* 
		 * 0: A,C,B
		 * 1: A,C,D,F,B
		 * 2: A,B,D
		 * 3: C,D,A - droop C,A,E
		 * 4: A,D - droop A,C
		 * 5: A,C,E - droop A,B,C
		 * 6: A,E,C - droop A,B,D
		 * 7: B - droop C
		 * 8: A,B,D - droop A,C,B
		 * 9: D,A,C
		 * 10: D,A,E
		 * 11: A,B
		 * 12: A,B - droop A,C
		 * 13: A, B, F, C, G - droop A, B, C, F, D
		 */
		
		
		/* 0: A,C,D
		 * 1: A,C,D,F,E
		 * 2: A,D,E
		 * 3: C,E,A
		 * 4: A,D
		 * 5: D,A,B
		 * 6: A,D,B
		 * 7: B
			//a,b,d (sgt:acd wigt:acb)
			//d,a,c = c1,a1,b
			//d,a,e = c1,a1,c2
		 */
		
		switch(scenario) {
		case 0:
			//scenario 0 (from http://www.accuratedemocracy.com/e_shares.htm ) 
			//result: 0: A,C,B
			seats = 3;
			candidates = 4;
			
			ballots.add(new STVBallot(12,new int[]{0,1}));
			ballots.add(new STVBallot(7,new int[]{1,0}));
			ballots.add(new STVBallot(9,new int[]{2,3}));
			ballots.add(new STVBallot(8,new int[]{3,2}));
			//result matches droop quota = goodf
			break;
		case 1:
			//scenario 1 (from https://en.wikipedia.org/wiki/Comparison_of_the_Hare_and_Droop_quotas )
			//result: 1: A,C,D,F,B
			//0=a,1=b,2=c,3d,4j,5s
			seats = 5;
			candidates = 6;
			
			ballots.add(new STVBallot(31,new int[]{0,2,1}));
			ballots.add(new STVBallot(30,new int[]{2,0,1}));
			ballots.add(new STVBallot(2,new int[]{1,0,2}));
			
			ballots.add(new STVBallot(20,new int[]{3,5,4}));
			ballots.add(new STVBallot(20,new int[]{5,3,4}));
			ballots.add(new STVBallot(17,new int[]{4,3,5}));
			//result: matches droop quota = good
			break;
		case 2:
			//scenario 2 (from https://en.wikipedia.org/wiki/Comparison_of_the_Hare_and_Droop_quotas )
			//result: 2: A,B,D
			//0=a,1=b,2=c,3d,4j,5s
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(50,new int[]{0,1,2}));
			ballots.add(new STVBallot(25,new int[]{3}));
			ballots.add(new STVBallot(24,new int[]{4}));
			//result: matches hare quota = good
			break;
		case 3:
			//scenario  (from https://en.wikipedia.org/wiki/CPO-STV )
			//result: 3: C,D,A
			//0=a,1=b,2=c,3d,4j,5s
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(25,new int[]{0}));
			ballots.add(new STVBallot(34,new int[]{2,1,3}));
			ballots.add(new STVBallot(7,new int[]{1,3}));
			ballots.add(new STVBallot(8,new int[]{3,1}));
			ballots.add(new STVBallot(5,new int[]{3,4}));
			ballots.add(new STVBallot(21,new int[]{4,3}));
			//result: good
			break;
		case 4:
			//scenario  (from https://en.wikipedia.org/wiki/Counting_single_transferable_votes#Quota )
			//result: 4: A,D
			//0=a,1=b,2=c,3d,4j,5s
			seats = 2;
			candidates = 4;
			
			ballots.add(new STVBallot(16,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(24,new int[]{0,2,1,3})); 
			ballots.add(new STVBallot(17,new int[]{3,0,1,2}));
			//result: good
			break;
		case 5:
			//scenario  (from ?)
			//result: 5: A,C,E
			//0=a,1=b,2=c,3d,4j,5s
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(12,new int[]{0,1}));
			ballots.add(new STVBallot(7,new int[]{1,0}));
			ballots.add(new STVBallot(9,new int[]{2,3}));
			ballots.add(new STVBallot(8,new int[]{3,2}));
			ballots.add(new STVBallot(3,new int[]{4,1}));
			ballots.add(new STVBallot(2,new int[]{4,3}));
			break;
		case 6:
			//scenario  (from https://en.wikipedia.org/wiki/CPO-STV )
			//result: 6: A,E,C
			//0=a,1=b,2=c,3d,4j,5s
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(12,new int[]{0,1}));
			ballots.add(new STVBallot(7,new int[]{1,0}));
			ballots.add(new STVBallot(9,new int[]{2}));
			ballots.add(new STVBallot(8,new int[]{3}));
			ballots.add(new STVBallot(3,new int[]{4,1}));
			ballots.add(new STVBallot(2,new int[]{4,3}));
			break;
		case 7:
			//scenario  (from http://www.nytimes.com/2016/05/01/opinion/sunday/how-majority-rule-might-have-stopped-donald-trump.html?_r=0 )
			//result: 7: B
			//1=b should win
			seats = 1;
			candidates = 3;
			
			ballots.add(new STVBallot(40,new int[]{0,1,2}));
			ballots.add(new STVBallot(35,new int[]{2,1,0}));
			ballots.add(new STVBallot(25,new int[]{1,2,0}));
			break;
		case 8:
			//scenario  (from https://en.wikipedia.org/wiki/Issues_affecting_the_single_transferable_vote )
			//result: 8: A,B,D
			//a,b,d (sgt:acd wigt:acb)
			seats = 3;
			candidates = 5;
			
			
			ballots.add(new STVBallot(105,new int[]{0,3,1}));
			ballots.add(new STVBallot(90,new int[]{1}));
			ballots.add(new STVBallot(80,new int[]{2,1}));
			ballots.add(new STVBallot(75,new int[]{3,1}));
			ballots.add(new STVBallot(50,new int[]{4,0,2,3}));
			break;
		case 9:
			//scenario  (from https://en.wikipedia.org/wiki/Issues_affecting_the_single_transferable_vote )
			//result: 9: D,A,C
			//a1 0 a2 1 b 2 c1 3 c2 4
			//d,a,c = c1,a1,b
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(220,new int[]{0,1,2}));
			ballots.add(new STVBallot(200,new int[]{1,0,2}));
			ballots.add(new STVBallot(190,new int[]{2,3,4}));
			ballots.add(new STVBallot(250,new int[]{3,4,2}));
			ballots.add(new STVBallot(140,new int[]{4,3,2}));
			break;
		case 10:
			//scenario  (from https://en.wikipedia.org/wiki/Issues_affecting_the_single_transferable_vote )
			//result: 10: D,A,E
			//a1 0 a2 1 b 2 c1 3 c2 4
			//d,a,e = c1,a1,c2
			seats = 3;
			candidates = 5;
			
			ballots.add(new STVBallot(220,new int[]{0,1,2}));
			ballots.add(new STVBallot(200,new int[]{1,0,2}));
			ballots.add(new STVBallot(190,new int[]{2,3,4}));
			ballots.add(new STVBallot(195,new int[]{3,4,2}));
			ballots.add(new STVBallot(195,new int[]{4,3,2}));
			break;
		case 11:
			//https://en.wikipedia.org/wiki/Schulze_STV
			//result: 11: A,B
			seats = 2;
			candidates = 3;
			ballots.add(new STVBallot(12,new int[]{0,1,2}));
			ballots.add(new STVBallot(26,new int[]{0,2,1}));
			ballots.add(new STVBallot(12,new int[]{0,2,1}));
			ballots.add(new STVBallot(13,new int[]{2,0,1}));
			ballots.add(new STVBallot(27,new int[]{1}));
			break;
		case 12:
			//https://en.wikipedia.org/wiki/Schulze_STV
			//result: 12: A,B
			seats = 2;
			candidates = 3;
			ballots.add(new STVBallot(12,new int[]{0,1,2}));
			ballots.add(new STVBallot(26,new int[]{0,2,1}));
			ballots.add(new STVBallot(12,new int[]{2,0,1}));
			ballots.add(new STVBallot(13,new int[]{2,0,1}));
			ballots.add(new STVBallot(27,new int[]{1}));
			break;
		case 13:
			//result: 13: A, B, F, C, G
			seats = 5;
			candidates = 10;
			ballots.add(new STVBallot(690,new int[]{0,1,2,3,4,5,6,7,8,9,}));
			ballots.add(new STVBallot(310,new int[]{5,6,7,8,9,0,1,2,3,4,}));
			break;
		case 14:
			//httpsb
			//b should win
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(35,new int[]{0,1,2}));
			ballots.add(new STVBallot(34,new int[]{2,1,0}));
			ballots.add(new STVBallot(31,new int[]{1,2,0}));
			break;
		case 15:
			//https://en.wikipedia.org/wiki/Independence_of_irrelevant_alternatives#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(2,new int[]{0,1,2}));
			ballots.add(new STVBallot(2,new int[]{2,1,0}));
			ballots.add(new STVBallot(1,new int[]{1,0,2}));
			break;
		case 16:
			//https://en.wikipedia.org/wiki/Independence_of_irrelevant_alternatives#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(2,new int[]{0,1,2}));
			ballots.add(new STVBallot(2,new int[]{1,2,0}));
			ballots.add(new STVBallot(1,new int[]{1,0,2}));
			break;
		case 17:
			//https://en.wikipedia.org/wiki/Later-no-harm_criterion
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(4,new int[]{0}));
			ballots.add(new STVBallot(1,new int[]{1,0,2}));
			ballots.add(new STVBallot(1,new int[]{1,2,0}));
			ballots.add(new STVBallot(1,new int[]{2,0,1}));
			ballots.add(new STVBallot(1,new int[]{2,1,0}));
			break;
		case 18:
			//https://en.wikipedia.org/wiki/Later-no-harm_criterion
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(4,new int[]{0,2,1}));
			ballots.add(new STVBallot(1,new int[]{1,0,2}));
			ballots.add(new STVBallot(1,new int[]{1,2,0}));
			ballots.add(new STVBallot(1,new int[]{2,0,1}));
			ballots.add(new STVBallot(1,new int[]{2,1,0}));
			break;
		case 19:
			//https://en.wikipedia.org/wiki/Monotonicity_criterion
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(28,new int[]{2,1}));
			ballots.add(new STVBallot( 5,new int[]{2,0}));
			ballots.add(new STVBallot(30,new int[]{0,1}));
			ballots.add(new STVBallot( 5,new int[]{0,2}));
			ballots.add(new STVBallot(16,new int[]{1,0}));
			ballots.add(new STVBallot(16,new int[]{1,2}));
			break;
		case 20:
			//https://en.wikipedia.org/wiki/Monotonicity_criterion
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(28,new int[]{2,1}));
			ballots.add(new STVBallot( 3,new int[]{2,0}));
			ballots.add(new STVBallot(30,new int[]{0,1}));
			ballots.add(new STVBallot( 7,new int[]{0,2}));
			ballots.add(new STVBallot(16,new int[]{1,0}));
			ballots.add(new STVBallot(16,new int[]{1,2}));
			break;
		case 21:
			//https://en.wikipedia.org/wiki/Participation_criterion#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(2,new int[]{0,1,2}));
			ballots.add(new STVBallot(3,new int[]{0,1,2}));
			ballots.add(new STVBallot(4,new int[]{1,2,0}));
			ballots.add(new STVBallot(6,new int[]{2,0,1}));
			break;
		case 22:
			//https://en.wikipedia.org/wiki/Participation_criterion#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			//ballots.add(new STVBallot(2,new int[]{0,1,2}));
			ballots.add(new STVBallot(3,new int[]{0,1,2}));
			ballots.add(new STVBallot(4,new int[]{1,2,0}));
			ballots.add(new STVBallot(6,new int[]{2,0,1}));
			break;
		case 23:
			//https://en.wikipedia.org/wiki/Reversal_symmetry
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(5,new int[]{0,1,2}));
			ballots.add(new STVBallot(4,new int[]{1,2,0}));
			ballots.add(new STVBallot(2,new int[]{2,0,1}));
			break;
		case 24:
			//https://en.wikipedia.org/wiki/Reversal_symmetry
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(5,new int[]{2,1,0}));
			ballots.add(new STVBallot(4,new int[]{0,2,1}));
			ballots.add(new STVBallot(2,new int[]{1,0,2}));
			break;
		case 25:
			//https://en.wikipedia.org/wiki/Independence_of_irrelevant_alternatives#Schulze_method
			seats = 1;
			candidates = 4;
			ballots.add(new STVBallot(4,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(2,new int[]{2,1,3,0}));
			ballots.add(new STVBallot(3,new int[]{2,3,0,1}));
			ballots.add(new STVBallot(2,new int[]{3,0,1,2}));
			ballots.add(new STVBallot(1,new int[]{3,1,2,0}));
			break;
		case 26:
			//https://en.wikipedia.org/wiki/Independence_of_irrelevant_alternatives#Schulze_method
			seats = 1;
			candidates = 4;
			ballots.add(new STVBallot(4,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(2,new int[]{1,2,3,0}));
			ballots.add(new STVBallot(3,new int[]{2,3,0,1}));
			ballots.add(new STVBallot(2,new int[]{3,0,1,2}));
			ballots.add(new STVBallot(1,new int[]{3,1,2,0}));
			break;
		case 27:
			//https://en.wikipedia.org/wiki/Consistency_criterion#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(4,new int[]{0,1,2}));
			ballots.add(new STVBallot(2,new int[]{1,0,2}));
			ballots.add(new STVBallot(4,new int[]{2,1,0}));
			ballots.add(new STVBallot(4,new int[]{0,1,2}));
			ballots.add(new STVBallot(6,new int[]{1,0,2}));
			ballots.add(new STVBallot(3,new int[]{2,1,0}));
			break;
		case 28:
			//https://en.wikipedia.org/wiki/Consistency_criterion#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(4,new int[]{0,1,2}));
			ballots.add(new STVBallot(2,new int[]{1,0,2}));
			ballots.add(new STVBallot(4,new int[]{2,1,0}));
			break;
		case 29:
			//https://en.wikipedia.org/wiki/Consistency_criterion#Instant-runoff_voting
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(4,new int[]{0,1,2}));
			ballots.add(new STVBallot(6,new int[]{1,0,2}));
			ballots.add(new STVBallot(3,new int[]{2,1,0}));
			break;
		case 30:
			//http://rangevoting.org/CondCoursera.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(14,new int[]{0,1,2}));
			ballots.add(new STVBallot(20,new int[]{1,0,2}));
			break;
		case 31:
			//http://rangevoting.org/CondCoursera.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(35,new int[]{0,1,2}));
			ballots.add(new STVBallot(21,new int[]{1,0,2}));
			ballots.add(new STVBallot(21,new int[]{1,2,0}));
			ballots.add(new STVBallot(21,new int[]{2,0,1}));
			ballots.add(new STVBallot(1,new int[]{0,2,1}));
			ballots.add(new STVBallot(1,new int[]{2,1,0}));
			break;
		case 32:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 4;
			ballots.add(new STVBallot(3,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(2,new int[]{1,2,0,3}));
			ballots.add(new STVBallot(2,new int[]{2,0,1,3}));
			break;
		case 33:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 4;
			ballots.add(new STVBallot(3,new int[]{0,3,2,1}));
			ballots.add(new STVBallot(2,new int[]{3,2,0,1}));
			ballots.add(new STVBallot(2,new int[]{2,0,3,1}));
			break;
		case 34:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 4;
			ballots.add(new STVBallot(3,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(2,new int[]{1,2,0,3}));
			ballots.add(new STVBallot(2,new int[]{2,0,1,3}));
			ballots.add(new STVBallot(3,new int[]{0,3,2,1}));
			ballots.add(new STVBallot(2,new int[]{3,2,0,1}));
			ballots.add(new STVBallot(2,new int[]{2,0,3,1}));
			break;
		case 35:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(30,new int[]{0,1,2}));
			ballots.add(new STVBallot(20,new int[]{1,2,0}));
			ballots.add(new STVBallot(20,new int[]{2,0,1}));
			ballots.add(new STVBallot(20,new int[]{1,0,2}));
			break;
		case 36:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(21,new int[]{0,2,1}));
			ballots.add(new STVBallot(20,new int[]{2,1,0}));
			ballots.add(new STVBallot(20,new int[]{1,0,2}));
			break;
		case 37:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(21,new int[]{0,2,1}));
			ballots.add(new STVBallot(20,new int[]{2,1,0}));
			ballots.add(new STVBallot(20,new int[]{1,0,2}));
			ballots.add(new STVBallot(30,new int[]{0,1,2}));
			ballots.add(new STVBallot(20,new int[]{1,2,0}));
			ballots.add(new STVBallot(20,new int[]{2,0,1}));
			ballots.add(new STVBallot(20,new int[]{1,0,2}));
			break;
		case 38:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 2;
			ballots.add(new STVBallot(66,new int[]{0,1}));
			ballots.add(new STVBallot(34,new int[]{1,0}));
			break;
		case 39:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(66,new int[]{0,1,2}));
			ballots.add(new STVBallot(34,new int[]{1,2,0}));
			break;
		case 40:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(3,new int[]{0,1,2}));
			ballots.add(new STVBallot(2,new int[]{1,2,0}));
			break;
		case 41:
			//http://rangevoting.org/CondNonAdd.html
			seats = 1;
			candidates = 3;
			ballots.add(new STVBallot(3,new int[]{0}));
			ballots.add(new STVBallot(2,new int[]{1,2,0}));
			break;
		} 
		
		//compute winners
		Vector<Integer> winners = getWinners(ballots,seats,candidates);
		
		ignore_fewest_votes = false;
		use_droop = false;
		reset_ignores = true;

		printWinners(winners);
		/*
		if( test_all) {
			Vector<int[]> combs = permutations(new int[]{0,1,2,3});
			
			Vector<Vector<Integer>> vwinners = new Vector<Vector<Integer>>();
			for( int i = 0; i < combs.size(); i++) {
				sort_orders = combs.get(i);
				vwinners.add(getWinners(ballots,seats,candidates));
			}
			for( int i = 0; i < combs.size(); i++) {
				int[] c = combs.get(i);
				System.out.print("{");
				for( int j = 0; j < c.length; j++) {
					System.out.print(c[j]);
				}
				System.out.print("}, ");
			}
			System.out.println();
			for( int i = 0; i < combs.size(); i++) {
				printWinners2(vwinners.get(i));
			}
		}
		if( true) {
			return;
		}
		*/
		
		if( test_all) {
	
			ignore_fewest_votes = false;
			use_droop = false;
			reset_ignores = true;
			
			Vector<Integer> winners00 = getWinners(ballots,seats,candidates);
			reset_ignores = false;
			Vector<Integer> winners01 = getWinners(ballots,seats,candidates);
			
			use_droop = true;
			reset_ignores = true;
			Vector<Integer> winners10 = getWinners(ballots,seats,candidates);
			reset_ignores = false;
			Vector<Integer> winners11 = getWinners(ballots,seats,candidates);

			ignore_fewest_votes = true;
			
			use_droop = false;
			reset_ignores = true;
			Vector<Integer> winners100 = getWinners(ballots,seats,candidates);
			reset_ignores = false;
			Vector<Integer> winners101 = getWinners(ballots,seats,candidates);
			
			use_droop = true;
			reset_ignores = true;
			Vector<Integer> winners110 = getWinners(ballots,seats,candidates);
			reset_ignores = false;
			Vector<Integer> winners111 = getWinners(ballots,seats,candidates);

			printWinners2(winners00);
			printWinners2(winners01);
			printWinners2(winners10);
			printWinners2(winners11);
			
			printWinners2(winners100);
			printWinners2(winners101);
			printWinners2(winners110);
			printWinners2(winners111);
		}

		//show winners
	}
	static void printWinners(Vector<Integer> winners) {
		for( int i = 0; i < winners.size(); i++) {
			logw( i > 0 ? ", " : "");
			logw( ""+(char)(winners.get(i).intValue()+'A'));
		}
		log("");
	}

	static void printWinners2(Vector<Integer> winners) {
		for( int i = 0; i < winners.size(); i++) {
			//logw( i > 0 ? ", " : "");
			logw2( ""+(char)(winners.get(i).intValue()+'a'));
		}
		logw2(", ");
	}

	static Vector<Integer> getWinners( Vector<STVBallot> _STVBallots, int num_seats, int num_candidates) {
		si = 0;
		reasoning = "";
		
		Vector<STVBallot> STVBallots = new Vector<STVBallot>();
		for( int i = 0; i < _STVBallots.size(); i++) {
			STVBallots.add(_STVBallots.get(i).clone());
		}

		Vector<Integer> winners = new Vector<Integer>();
		
		double num_votes_cast = 0;
		for( int i = 0; i < STVBallots.size(); i++) {
			num_votes_cast += STVBallots.get(i).weight;
		}
		sorted_ignores = null; 
		old_way_ignores = null;

		
		for( int i = 0; i < num_seats; i++) {
			int winner =
					use_new_ignore_method 
					? getNextWinner( STVBallots, num_candidates, num_votes_cast, use_droop?(num_seats+1):num_seats, winners)
					: getNextWinner_old( STVBallots, num_candidates, num_votes_cast, use_droop?(num_seats+1):num_seats, winners)
					;
			if( winner < 0) {
				break;
			}
		}
		
		return winners;
	}
	
	static int getNextWinner_old( Vector<STVBallot> STVBallots, int num_candidates, double num_votes_cast, int num_seats, Vector<Integer> winners) {
		num_votes_cast = 0;
		for( int i = 0; i < STVBallots.size(); i++) {
			STVBallot b = STVBallots.get(i);
			Choice ch = b.getFirst(winners,new Vector<Integer>());
			if( ch == null) {
				//out_of_choices_count += b.weight;
			} else {
				num_votes_cast += b.weight;
			}
		}

		
		double quota = num_votes_cast/(double)num_seats;
		Vector<Integer> ignores = new Vector<Integer>();
		Vector<Integer> tried_ignores = new Vector<Integer>();
		
		log("     ballots: "+num_votes_cast);
		log("     quota: "+quota);
		
		log("     resetting ignores");
		while( true) {
			double temp_quota = quota;
			
			//recounting ballots
			double[] counts = new double[num_candidates];
			double new_ballot_count = 0;
			double out_of_choices_count = 0;
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst(winners,ignores);
				if( ch == null) {
					out_of_choices_count += b.weight;
				} else {
					new_ballot_count += b.weight;
					counts[ch.candidate] += b.weight;
				}
			}
			if( include_ignores_on_dynamic_quota) {
				temp_quota = new_ballot_count/(num_seats-winners.size()); 
				log("     "+out_of_choices_count+" ballots out of choices, adjusting temp quota to "+temp_quota);
			}
			/*
			log("     resetting temp quota to "+temp_quota);
			
			double[] counts = new double[num_candidates];
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst(winners,ignores);
				if( ch == null) {
					//if a STVBallot is out of choices, adjust the temporary quota as if the STVBallot was never cast
					temp_quota -= b.weight/num_candidates;
					log("     "+b.weight+" ballots out of choices, adjusting temp quota to "+temp_quota);
					continue;
				}
				counts[ch.candidate] += b.weight;
			}
			*/
			double min = -1;
			int min_index = -1;
			double max = -1;
			int max_index = -1;
			int candidates_left = 0;
			logw("leading choices:");
			for( int i = 0; i < counts.length; i++) {
				logw(" "+(char)(i+'A')+": "+counts[i]);
				if( counts[i] > 0) {
					candidates_left++;
				} else {
					continue;
				}
				if( counts[i] > max || max_index < 0) {
					max = counts[i];
					max_index = i;
				}
				if( counts[i] < min || min_index < 0) {
					min = counts[i];
					min_index = i;
				}
			}
			log("");
			if( max >= (include_ignores_on_dynamic_quota?temp_quota:quota)) {
				log("elected "+(char)(max_index+'A')+" (above quota: "+max+" : "+quota+" "+temp_quota+")");
				log("");
				elect(STVBallots,max_index,max,quota,winners,ignores);
				return max_index;
			}
			if( candidates_left == 0) {
				return -1;
			}
			if( candidates_left == 1) {
				log("elected "+(char)(max_index+'A')+" (below quota: "+max+" : "+quota+" "+temp_quota+")");
				log("");
				elect(STVBallots,max_index,max,quota,winners,ignores);
				return max_index;
			}

			
			//ignore logic
			boolean tried = false;
			for( int i = 0; i < tried_ignores.size(); i++) {
				if( tried_ignores.get(i).intValue() == min_index) {
					tried = true;
					break;
				}
			}
			
			if( !tried) {
				tried_ignores.add(min_index);
				if( ignores.size() > 0) {
					ignores.remove(ignores.size()-1);
				}
			} else {
				tried_ignores.clear();
			}
			ignores.add(min_index);
			
			
			
			String s = "";
			for( int i = 0; i < ignores.size(); i++) {
				if( i > 0) {
					s += ",";
				}
				s += (char)(ignores.get(i)+'A');
			}
			String s2 = "";
			for( int i = 0; i < tried_ignores.size()-1; i++) {
				if( i > 0) {
					s2 += ",";
				}
				s2 += (char)(tried_ignores.get(i)+'A');
			}
			log("     tried ignores "+s2);
			log("     temporarily ignoring "+s);
		}
		
	}
	static int elect(Vector<STVBallot> STVBallots, int c, double votes, double quota, Vector<Integer> winners, Vector<Integer> ignores) {
		double new_weight = (votes-quota)/votes;
		if( new_weight <= 0) {
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst(winners,ignores);
				if( ch == null) {
					continue;
				}
				if( ch.candidate == c) {
					b.weight = 0;
					//STVBallots.remove(i--);
				}
			}
		} else {
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst(winners,ignores);
				if( ch == null) {
					continue;
				}
				
				if( ch.candidate == c) {
					b.weight *= new_weight;
				}
			}
		}
		winners.add(c);

		return c;
	}
	
	//====================
	
	static int si = 0;
	public static Vector<Pair<Double,int[]>> sorted_ignores = null; 
	public static Vector<Integer> old_way_ignores = null;
	static int getNextWinner( Vector<STVBallot> STVBallots, int num_candidates, double num_votes_cast, int num_seats, Vector<Integer> winners) {
		double quota = num_votes_cast/(double)num_seats;
		int[] elected = new int[winners.size()];
		for( int i = 0; i < elected.length; i++) {
			elected[i] = winners.get(i);
		}
		
		log("     ballots: "+num_votes_cast);
		log("     quota: "+quota);
		
		if( sorted_ignores == null && !ignore_fewest_votes) {
			sorted_ignores = getSortedIgnoreCombos(STVBallots, elected, num_candidates);
		}
		if( old_way_ignores == null) {
			old_way_ignores = new Vector<Integer>(); 
		}
		
		
		if( reset_ignores) {
			si = 0;
			if( !ignore_fewest_votes) {
				sorted_ignores = getSortedIgnoreCombos(STVBallots, elected, num_candidates);
			}
			old_way_ignores = new Vector<Integer>(); 
		} else {
			
		}
		boolean first_time = true;
		for( ; si < (ignore_fewest_votes ? num_candidates : sorted_ignores.size()); si++) {
			
			Vector<Integer> ignores = new Vector<Integer>();
			
			if( !ignore_fewest_votes) {
				int[] a_ignores = sorted_ignores.get(si).b;
				for( int ii = 0; ii < a_ignores.length; ii++) {
					ignores.add(a_ignores[ii]);
				}
			} else {
				for( int ii = 0; ii < old_way_ignores.size(); ii++) {
					ignores.add(old_way_ignores.get(ii));
				}
				if( !first_time) {
					double[] temp_counts = new double[num_candidates];
					for( int i = 0; i < STVBallots.size(); i++) {
						STVBallot b = STVBallots.get(i);
						Choice ch = b.getFirst(winners,ignores);
						if( ch == null) {
						} else {
							temp_counts[ch.candidate] += b.weight;
						}
					}
					double  lowest = -1;
					int lowest_index = -1;
					for( int i = 0; i < temp_counts.length; i++) {
						if( temp_counts[i] <= 0) {
							continue;
						}
						if( lowest_index < 0 || temp_counts[i] < lowest) {
							lowest = temp_counts[i];
							lowest_index = i;
						}
					}
					if( lowest_index >= 0) {
						ignores.add(lowest_index);
						old_way_ignores.add(lowest_index);
					}
				}
				
			}
			first_time = false;
			
			
			String s = "";
			for( int i = 0; i < ignores.size(); i++) {
				if( i > 0) {
					s += ",";
				}
				s += (char)(ignores.get(i)+'A');
			}
			logw("     temporarily ignoring "+s);
			if( sorted_ignores != null) {
				logw(" (");
				int[] ii = sorted_ignores.get(si).b;
				double[] raw_scores = new double[]{
						countCandidatesForCombination( STVBallots, elected, ii, num_candidates),
						countBordaValueForCombination( STVBallots, elected, ii, num_candidates),
						countAffectedBallotsForCombination( STVBallots, elected, ii, num_candidates),
						countDemotionsForCombination( STVBallots, elected, ii, num_candidates),
				};
				logw("demotions: "+raw_scores[3]+" ballots: "+raw_scores[2]+" candidates: "+raw_scores[0]);
	
				log(")");
			} else {
				log("");
			}
			
			
			double temp_quota = quota;
			//log("     resetting temp quota to "+temp_quota);
			
			//recounting ballots
			double[] counts = new double[num_candidates];
			double new_ballot_count = 0;
			double out_of_choices_count = 0;
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst(winners,ignores);
				if( ch == null) {
					out_of_choices_count += b.weight;
				} else {
					new_ballot_count += b.weight;
					counts[ch.candidate] += b.weight;
				}
			}
			if( include_ignores_on_dynamic_quota) {
				temp_quota = new_ballot_count/(num_seats-winners.size()); 
				log("     "+out_of_choices_count+" ballots out of choices, adjusting temp quota to "+temp_quota);
			}
			
			double min = -1;
			int min_index = -1;
			double max = -1;
			int max_index = -1;
			int candidates_left = 0;
			logw("leading choices:");
			for( int i = 0; i < counts.length; i++) {
				logw(" "+(char)(i+'A')+": "+counts[i]);
				if( counts[i] > 0) {
					candidates_left++;
				} else {
					continue;
				}
				if( counts[i] > max || max_index < 0) {
					max = counts[i];
					max_index = i;
				}
				if( counts[i] < min || min_index < 0) {
					min = counts[i];
					min_index = i;
				}
			}
			log("");
			if( max >= temp_quota) {
				log("elected "+(char)(max_index+'A')+" (above quota) "+temp_quota);
				log("");
				elect(STVBallots,max_index,max,quota,winners,ignores);
				return max_index;
			}
			if( candidates_left == 0) {
				return -1;
			}
			if( candidates_left == 1) {// && si == sorted_ignores.size() - 2) {
				log("elected "+(char)(max_index+'A')+" (below quota) "+temp_quota);
				log("");
				elect(STVBallots,max_index,max,quota,winners,ignores);
				return max_index;
			}
		}
		
		return -1;
	}
	
	
	
	
	public static Vector<Pair<Double,int[]>> getSortedIgnoreCombos(Vector<STVBallot> ballots, int[] elected, int num_candidates) {
		int[] avail = new int[num_candidates-elected.length];
		int n = 0;
		for( int i = 0; i < avail.length; i++) {
			while( isin(n,elected)) { n++; }
			avail[i] = n;
			n++;
		}
		
		Vector<int[]> ignore_combos = getAllCombinations(avail);
		Vector<Pair<Double,int[]>> sorted = new Vector<Pair<Double,int[]>>();
		sorted.add(new Pair<Double,int[]>(0.0,new int[]{}));
		
		for( int i = 0; i < ignore_combos.size(); i++) {
			int[] combo = ignore_combos.get(i);
			//sorted.add(new Pair<Double,int[]>(countDemotionsForCombination(ballots,elected,combo),combo));
			sorted.add(new Pair<Double,int[]>(getSortRank(ballots,elected,combo,num_candidates),combo));
		}
		
		Collections.sort(sorted);
		/*
		for( int i = 0; i < sorted.size(); i++) {
			Pair<Double,int[]> p = sorted.get(i);
			System.out.print(p.a+": ");
			for( int j = 0; j < p.b.length; j++) {
				System.out.print(p.b[j]+" ");
			}
			System.out.println();			
		}
		*/
		return sorted;
	}
	
	public static double countDemotionsForCombination(Vector<STVBallot> ballots, int[] elected, int[] ignored, int num_candidates) {
		//System.out.println("countDemotionsForCombination "+ignored.length);
		double demotions = 0;
		for( STVBallot b : ballots) {
			for( int i = 0; i < b.choices.length; i++) {
				if( isin(b.choices[i].candidate, elected)) {
					if( elected_is_demotion) {
						demotions += b.weight;
						
					}
					continue;
				}
				if( isin(b.choices[i].candidate, ignored)) {
					demotions += b.weight;
					continue;
				}
				break;
			}
		}
		//System.out.println("countDemotionsForCombination demotions: "+demotions);
		return demotions;
	}
	public static double countCandidatesForCombination(Vector<STVBallot> ballots, int[] elected, int[] ignored, int num_candidates) {
		if( num_candidates == 0) {
			return 0;
		}
		//want to maximize this, so additive inverse.
		return num_candidates-ignored.length;
		//return ignored.length;
	}
	public static double countBordaValueForCombination(Vector<STVBallot> ballots, int[] elected, int[] ignored, int num_candidates) {
		return 0;
		/*
		//System.out.println("countDemotionsForCombination "+ignored.length);
		double demotions = 0;
		for( STVBallot b : ballots) {
			double borda_value = num_candidates;
			for( int i = 0; i < b.choices.length; i++) {
				if( isin(b.choices[i].candidate, elected)) {
					continue;
				}
				if( isin(b.choices[i].candidate, ignored)) {
					demotions += b.weight*borda_value;
				}
				borda_value--;
			}
		}
		//System.out.println("countDemotionsForCombination demotions: "+demotions);
		return demotions;
		*/
	}

	public static double countAffectedBallotsForCombination(Vector<STVBallot> ballots, int[] elected, int[] ignored, int num_candidates) {
		//System.out.println("countDemotionsForCombination "+ignored.length);
		double demotions = 0;
		for( STVBallot b : ballots) {
			for( int i = 0; i < b.choices.length; i++) {
				if( isin(b.choices[i].candidate, elected)) {
					continue;
				}
				if( isin(b.choices[i].candidate, ignored)) {
					demotions += b.weight;
				}
				break;
			}
		}
		//System.out.println("countDemotionsForCombination demotions: "+demotions);
		return demotions;
	}


	public static boolean isin(int j, int[] jj) {
		for( int i = 0; i < jj.length; i++) {
			if( jj[i] == j) {
				return true;
			}
		}
		return false;
	}
	
	public static Vector<int[]> getAllCombinations(int arr[])	{
		Vector<int[]> combs = new Vector<int[]>();
		for( int i = 0; i < arr.length; i++) {
			addCombinations(arr,i+1,combs);
		}
		//System.out.println("found "+combs.size()+" combinations");
		return combs;
	}
	public static void addCombinations(int arr[],  int r, Vector<int[]> combs)	{
	    int[] data = new int[r];
	    combinationUtil(arr, data, 0, arr.length-1, 0, r, combs);
		//System.out.println("added combinations of length "+r+" new size "+combs.size());
	}
	public static void combinationUtil(int arr[], int data[], int start, int end, int index, int r, Vector<int[]> combs) {
	    if (index == r) {
	    	combs.add(data.clone());
	        return;
	    }
	    for (int i=start; i<=end && end-i+1 >= r-index; i++) {
	        data[index] = arr[i];
	        combinationUtil(arr, data, i+1, end, index+1, r, combs);
	    }
	}	
	
    static Vector<int[]> permutations(int[] js) {
        final Vector<int[]> resultList = new Vector<int[]>();
        final int l = js.length;
        if ( l == 0 ) return resultList;
        if ( l == 1 )
        {
            resultList.add( js );
            return resultList;
        }

        int[] subClone = Arrays.copyOf( js, l - 1);
        System.arraycopy( js, 1, subClone, 0, l - 1 );

        for ( int i = 0; i < l; ++i ){
            int e = js[i];
            if ( i > 0 ) subClone[i-1] = js[0];
            final Vector<int[]> subPermutations = permutations( subClone );
            for ( int[] sc : subPermutations )
            {
                int[] clone = Arrays.copyOf( js, l );
                clone[0] = e;
                System.arraycopy( sc, 0, clone, 1, l - 1 );
                resultList.add( clone );
            }
            if ( i > 0 ) subClone[i-1] = e;
        }
        return resultList;
    }	
}
