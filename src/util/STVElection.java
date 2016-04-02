package util;

import java.util.*;

public class STVElection {

	public static void main(String[] args) {
		Vector<Ballot> ballots = new Vector<Ballot>();
		int seats = 0;
		int candidates = 0;
		
		int scenario = 0;
		
		switch(scenario) {
		case 0:
			//scenario 0 (from http://www.accuratedemocracy.com/e_shares.htm ) 
			seats = 3;
			candidates = 4;
			
			addBallots(ballots,12,new int[]{0,1});
			addBallots(ballots,7,new int[]{1,0});
			addBallots(ballots,9,new int[]{2,3});
			addBallots(ballots,8,new int[]{3,2});
			//result matches droop quota = good
			break;
		case 1:
			//scenario 1 (from https://en.wikipedia.org/wiki/Comparison_of_the_Hare_and_Droop_quotas )
			//0=a,1=b,2=c,3d,4j,5s
			seats = 5;
			candidates = 6;
			
			addBallots(ballots,31,new int[]{0,2,1});
			addBallots(ballots,30,new int[]{2,0,1});
			addBallots(ballots,2,new int[]{1,0,2});
			
			addBallots(ballots,20,new int[]{3,5,4});
			addBallots(ballots,20,new int[]{5,3,4});
			addBallots(ballots,17,new int[]{4,3,5});
			//result: matches droop quota = good
			break;
		case 2:
			//scenario 2 (from https://en.wikipedia.org/wiki/Comparison_of_the_Hare_and_Droop_quotas )
			//0=a,1=b,2=c,3d,4j,5s
			seats = 3;
			candidates = 5;
			
			addBallots(ballots,50,new int[]{0,1,2});
			addBallots(ballots,25,new int[]{3});
			addBallots(ballots,24,new int[]{4});
			//result: matches hare quota = good
			break;
		} 
		
		System.out.println("ballots: "+ballots.size());
		System.out.println("quota: "+((double)ballots.size())/(double)seats);
		
		//compute winners
		Vector<Integer> winners = getWinners(ballots,seats,candidates);
		
		//show winners
		for( int i = 0; i < winners.size(); i++) {
			System.out.print( i > 0 ? ", " : "");
			System.out.print( ""+winners.get(i).intValue());
		}
		System.out.println();
	}


	
	static void addBallots(Vector<Ballot> ballots, int count, int[] choices) {
		for( int i = 0; i < count; i++) {
			Ballot b = new Ballot();
			for( int j = 0; j < choices.length; j++) {
				b.add(new Choice(choices[j]));
			}
			ballots.add(b);
		}
	}
	
	static Vector<Integer> getWinners( Vector<Ballot> _ballots, int num_seats, int num_candidates) {
		for( int i = 0; i < _ballots.size(); i++) {
			_ballots.get(i).weight = 1;
		}

		Vector<Ballot> ballots = new Vector<Ballot>();
		for( Ballot ballot : _ballots) {
				ballots.add(ballot);
		}
		Vector<Integer> winners = new Vector<Integer>();
		double quota = ((double)ballots.size()) / ((double)num_seats);
		for( int i = 0; i < num_seats; i++) {
			int winner = getNextWinner( ballots, quota, num_candidates);
			if( winner < 0) {
				break;
			}
			winners.add(winner);
		}
		
		for( int i = 0; i < _ballots.size(); i++) {
			_ballots.get(i).weight = 1;
			_ballots.get(i).resetIgnores();
		}
		
		return winners;
	}
	
	static int getNextWinner( Vector<Ballot> ballots, double quota, int num_candidates) {
		System.out.println("resetting ignores");
		for( int i = 0; i < ballots.size(); i++) {
			ballots.get(i).resetIgnores();
		}
		while( true) {
			double[] counts = getCounts(ballots, num_candidates);
			
			double min = -1;
			int min_index = -1;
			double max = -1;
			int max_index = -1;
			int candidates_left = 0;
			for( int i = 0; i < counts.length; i++) {
				System.out.print(" "+counts[i]);
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
			System.out.println();
			if( max >= quota) {
				elect(ballots,max_index,max,quota);
				System.out.println("chose above quota "+max_index);
				return max_index;
			}
			if( candidates_left == 0) {
				return -1;
			}
			if( candidates_left == 1) {
				elect(ballots,max_index,max,quota);
				System.out.println("chose below quota "+max_index);
				return max_index;
			}
			System.out.println("temporarily ignoring "+min_index);
			for( int i = 0; i < ballots.size(); i++) {
				Ballot b = ballots.get(i);
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
	static int elect(Vector<Ballot> ballots, int c, double votes, double quota) {
		double new_weight = (votes-quota)/votes;
		if( new_weight <= 0) {
			for( int i = 0; i < ballots.size(); i++) {
				Ballot b = ballots.get(i);
				Choice ch = b.getFirst();
				if( ch == null) {
					continue;
				}
				if( ch.candidate == c) {
					b.weight = 0;
					ballots.remove(i);
					i--;
				}
			}
		} else {
			for( int i = 0; i < ballots.size(); i++) {
				Ballot b = ballots.get(i);
				Choice ch = b.getFirst();
				if( ch == null) {
					continue;
				}
				
				if( ch.candidate == c) {
					b.weight *= new_weight;
				}
			}
		}

		for( int i = 0; i < ballots.size(); i++) {
			ballots.get(i).resetIgnores();
		}
		for( int i = 0; i < ballots.size(); i++) {
			ballots.get(i).candidateElected(c);
		}
		return c;
	}

	static double[] getCounts( Vector<Ballot> ballots, int num_candidates) {
		double[] counts = new double[num_candidates];
		for( int i = 0; i < ballots.size(); i++) {
			Ballot b = ballots.get(i);
			Choice ch = b.getFirst();
			if( ch == null) {
				continue;
			}
			counts[ch.candidate] += b.weight;
		}
		return counts;
	}
}

class Ballot extends Vector<Choice> {
	double weight = 1;
	Choice getFirst() {
		for( int i = 0; i < size(); i++) {
			Choice c = get(i);
			if( !c.ignored && !c.elected) {
				return c;
			}
		}
		return null;
	}
	void resetIgnores() {
		for( int i = 0; i < size(); i++) {
			get(i).ignored = false;
		}
	}
	void resetElecteds() {
		for( int i = 0; i < size(); i++) {
			get(i).ignored = false;
		}
	}
	void candidateElected(int c) {
		for( int i = 0; i < size(); i++) {
			if( get(i).candidate == c) {
				get(i).elected = true;
			}
		}
	}
}

class Choice {
	int candidate = -1;
	boolean ignored = false;
	boolean elected = false;
	public Choice(int i) { candidate = i; }
}