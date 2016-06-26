package util.ballot_counters;

import java.util.*;

public class STVElection {
	public static String reasoning = "";
	
	public static void log(String s) {
		reasoning += s+"\n";
		System.out.println(s);
	}
	public static void logw(String s) {
		reasoning += s;
		System.out.print(s);
	}

	public static void main(String[] args) {
		Vector<STVBallot> ballots = new Vector<STVBallot>();
		int seats = 0;
		int candidates = 0;
		
		int scenario = 12;
		
		/* 0: A,C,B
		 * 1: A,C,D,F,B
		 * 2: A,D,E
		 * 3: C,E,A
		 * 4: A,D
		 * 5: D,A,B
		 * 6: A,D,B
		 * 7: C
		 */
		
		switch(scenario) {
		case 0:
			//scenario 0 (from http://www.accuratedemocracy.com/e_shares.htm ) 
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
			//scenario  (from https://en.wikipedia.org/wiki/CPO-STV )
			//0=a,1=b,2=c,3d,4j,5s
			seats = 2;
			candidates = 4;
			
			ballots.add(new STVBallot(16,new int[]{0,1,2,3}));
			ballots.add(new STVBallot(24,new int[]{0,2,1,3})); 
			ballots.add(new STVBallot(17,new int[]{3,0,1,2}));
			//result: good
			break;
		case 5:
			//scenario  (from https://en.wikipedia.org/wiki/CPO-STV )
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
			//0=a,1=b,2=c,3d,4j,5s
			seats = 1;
			candidates = 3;
			
			ballots.add(new STVBallot(40,new int[]{0,1,2}));
			ballots.add(new STVBallot(35,new int[]{2,1,0}));
			ballots.add(new STVBallot(25,new int[]{1,2,0}));
			break;
		case 8:
			//scenario  (from https://en.wikipedia.org/wiki/Issues_affecting_the_single_transferable_vote )
			//a,b,d
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
			//a1 0 a2 1 b 2 c1 3 c2 4
			//d,a,c
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
			//a1 0 a2 1 b 2 c1 3 c2 4
			//d,a,e
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
			seats = 2;
			candidates = 3;
			ballots.add(new STVBallot(12,new int[]{0,1,2}));
			ballots.add(new STVBallot(26,new int[]{0,2,1}));
			ballots.add(new STVBallot(12,new int[]{2,0,1}));
			ballots.add(new STVBallot(13,new int[]{2,0,1}));
			ballots.add(new STVBallot(27,new int[]{1}));
			break;
		} 
		
		//compute winners
		Vector<Integer> winners = getWinners(ballots,seats,candidates);
		
		//show winners
		for( int i = 0; i < winners.size(); i++) {
			logw( i > 0 ? ", " : "");
			logw( ""+(char)(winners.get(i).intValue()+'A'));
		}
		log("");
	}

	static Vector<Integer> getWinners( Vector<STVBallot> STVBallots, int num_seats, int num_candidates) {
		reasoning = "";
		

		Vector<Integer> winners = new Vector<Integer>();
		
		double num_votes_cast = 0;
		for( int i = 0; i < STVBallots.size(); i++) {
			num_votes_cast += STVBallots.get(i).weight;
		}
		
		
		for( int i = 0; i < num_seats; i++) {
			int winner = getNextWinner( STVBallots, num_candidates, num_votes_cast, num_seats);
			if( winner < 0) {
				break;
			}
			winners.add(winner);
		}
		
		return winners;
	}
	
	static int getNextWinner( Vector<STVBallot> STVBallots, int num_candidates, double num_votes_cast, int num_seats) {
		double quota = num_votes_cast/(double)num_seats;
		
		log("     ballots: "+num_votes_cast);
		log("     quota: "+quota);
		
		log("     resetting ignores");
		for( int i = 0; i < STVBallots.size(); i++) {
			STVBallots.get(i).resetIgnores();
		}
		while( true) {
			double temp_quota = quota;
			log("     resetting temp quota to "+temp_quota);
			
			double[] counts = new double[num_candidates];
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst();
				if( ch == null) {
					//if a STVBallot is out of choices, adjust the temporary quota as if the STVBallot was never cast
					temp_quota -= b.weight/num_candidates;
					log("     "+b.weight+" ballots out of choices, adjusting temp quota to "+temp_quota);
					continue;
				}
				counts[ch.candidate] += b.weight;
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
				log("elected "+(char)(max_index+'A')+" (above quota)");
				log("");
				elect(STVBallots,max_index,max,quota);
				return max_index;
			}
			if( candidates_left == 0) {
				return -1;
			}
			if( candidates_left == 1) {
				log("elected "+(char)(max_index+'A')+" (below quota)");
				log("");
				elect(STVBallots,max_index,max,quota);
				return max_index;
			}
			log("     temporarily ignoring "+(char)(min_index+'A'));
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst();
				if( ch == null) {
					continue;
				}
				if( ch.candidate == min_index) {
					ch.ignored = true;
				}
			}
		}
		
	}
	static int elect(Vector<STVBallot> STVBallots, int c, double votes, double quota) {
		double new_weight = (votes-quota)/votes;
		if( new_weight <= 0) {
			for( int i = 0; i < STVBallots.size(); i++) {
				STVBallot b = STVBallots.get(i);
				Choice ch = b.getFirst();
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
				Choice ch = b.getFirst();
				if( ch == null) {
					continue;
				}
				
				if( ch.candidate == c) {
					b.weight *= new_weight;
				}
			}
		}

		for( int i = 0; i < STVBallots.size(); i++) {
			STVBallots.get(i).resetIgnores();
		}
		for( int i = 0; i < STVBallots.size(); i++) {
			STVBallots.get(i).candidateElected(c);
		}
		return c;
	}
}
