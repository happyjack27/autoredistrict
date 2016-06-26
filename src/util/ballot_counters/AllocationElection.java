package util.ballot_counters;

import java.util.*;

public class AllocationElection {
	public static String reasoning = "";
	
	public static void log(String s) {
		reasoning += s;
	}
	
	public static void logl(String s) {
		reasoning += s+"\n";
	}
	public static void logl() {
		reasoning += "\n";
	}
	
	public static void main(String[] args) {
		Vector<AllocationBallot> _ballots = new Vector<AllocationBallot>();
		int candiates = 3;
		int seats = 1;
		
		_ballots.add(new AllocationBallot(40,new double[]{20,50,30}));
		_ballots.add(new AllocationBallot(41,new double[]{20,30,50}));
		_ballots.add(new AllocationBallot(25,new double[]{99,1,0}));
		
		int[] winners = getWinners( _ballots, candiates, seats);
		System.out.println(reasoning);
	}
	
	public static int[] getWinners(Vector<AllocationBallot> _ballots, int num_candidates, int seats) {
		reasoning = "";
		int[] winners = new int[seats];
		
		Vector<AllocationBallot> ballots = new Vector<AllocationBallot>();
		
		for( int i = 0; i < _ballots.size(); i++) {
			ballots.add(_ballots.get(i).clone());
		}
		
		for( int i = 0 ; i < winners.length; i++) {
			for( int j = 0; j < ballots.size(); j++) {
				log(ballots.get(j).weight+": [");
				double tot = 0;
				for( int k = 0; k < ballots.get(j).allocs.length; k++) {
					log(ballots.get(j).allocs[k]+", ");
					tot+= ballots.get(j).allocs[k];
				}
				logl("] "+tot);
				
			}
			
			
			Vector<Integer> ignores = new Vector<Integer>();
			
			Triplet<Integer,Double,Double>  winner = getWinner(ballots,ignores,num_candidates,seats-i);
			while( winner.a < 0) {
				int next = getNextIgnore(ballots,ignores,num_candidates);
				if( next < 0) {
					break;
				}
				ignores.add(next);
				
				log(" ignoring: ");
				for( int j = 0; j < ignores.size(); j++) {
					log(""+ignores.get(j)+", ");
				}
				logl();
				
				winner = getWinner(ballots,ignores,num_candidates,seats-i);
			}
			logl("   elected: "+winner.a+" "+winner.b+" "+winner.c);
			logl();
			winners[i] = winner.a;
			if( winner.a > -1) {
				for( int j = 0; j < ballots.size(); j++) {
					ballots.get(j).elected(winner.a, winner.b, winner.c, ignores);  //is this right? (the surplus vote count / reallocation)
				}
			}
			
		}
		log(" winners:");
		for( int i = 0; i < winners.length; i++) {
			log(""+winners[i]+", ");
		}
		logl();
		
		return winners;
	}
	
	public static Triplet<Integer,Double,Double> getWinner(Vector<AllocationBallot> ballots, Vector<Integer> ignores, int num_candidates, int seats_left) {
		double[] ws = getTotals( ballots, ignores, num_candidates);
		log(" totals: ");
		for( int j = 0; j < ws.length; j++) {
			log(""+ws[j]+", ");
		}
		logl();

		
		double quota = 0;
		for( int j = 0; j < ws.length; j++) {
			quota += ws[j];
		}
		quota /= (double)seats_left;
		logl(" quota: "+quota);
		
		int max_index = 0;
		double max = ws[0];
		for( int j = 0; j < ws.length; j++) {
			if( ws[j] > max) {
				max = ws[j];
				max_index = j;
			}
		}
		if( max >= quota) {
			return new Triplet<Integer,Double,Double>(max_index,max,quota);
		} else {
			return new Triplet<Integer,Double,Double>(-1,0.0,0.0);
		}
	}
	
	public static double[] getTotals(Vector<AllocationBallot> ballots, Vector<Integer> ignores, int num_candidates) {
		double[] sum = new double[num_candidates];
		for( int i = 0; i < ballots.size(); i++) {
			double[] dd = ballots.get(i).getWeightsIgnored(ignores);
			for( int j = 0; j < dd.length; j++) {
				sum[j] += dd[j];
			}
		}
		return sum;
	}
	
	public static int getNextIgnore(Vector<AllocationBallot> ballots, Vector<Integer> ignores, int num_candidates) {
		double[] tots = getTotals(ballots,ignores,num_candidates);
		Vector<Pair<Double,Integer>> s = new Vector<Pair<Double,Integer>>();
		for( int i = 0; i < tots.length; i++) {
			s.add(new Pair<Double,Integer>(tots[i],i));
		}
		Collections.sort(s);
		for( int i = 0; i < s.size(); i++) {
			if( s.get(i).a > 0) {
				return s.get(i).b;
			}
		}
		return -1;
	}

}
