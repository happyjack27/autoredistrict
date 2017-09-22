package paretoFront;

import java.util.*;

//http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=DB2C0FB4F2159957393DAD16E5B755B6?doi=10.1.1.542.385&rep=rep1&type=pdf
public class ScoreArray implements Comparable<ScoreArray> {
	public static int sortBy = 0;
	public double[] scores;
	
	public double crowding;
	public double dominates;
	public double dominated_by;
	//public double age = 0;
	
	public static String listAllNonDominated( Vector<ScoreArray> scoreArray) {
		StringBuffer sb = new StringBuffer();
		for( ScoreArray s : scoreArray) {
			if( s.dominated_by == 0) {
				for( int i = 0; i < s.scores.length; i++) {
					if( i > 0) {
						sb.append(",");
					}
					sb.append(s.scores[i]);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void sortByParetoFitness(Vector<ScoreArray> scoreArray, int num_scores) {
		computeDomination(scoreArray,num_scores);
		computeCrowding(scoreArray,num_scores);
		sortBy = -1;
		Collections.sort(scoreArray);
	}
	
	public boolean dominates(ScoreArray s) {
		for( int i = 0; i < scores.length; i++) {
			if( s.scores[i] > this.scores[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	//1 if we are better
	public int compareTo(ScoreArray o) {
		// < 0 means we want to sort by domination.
		if( sortBy < 0) {
			if( this.dominated_by < o.dominated_by) {
				return 1;
			}
			if( this.dominated_by > o.dominated_by) {
				return -1;
			}
			if( this.dominates > o.dominates) {
				return 1;
			}
			if( this.dominates < o.dominates) {
				return -1;
			}
			if( this.crowding < o.crowding) {
				return 1;
			}
			if( this.crowding > o.crowding) {
				return -1;
			}
			return 0;
		}

		double d = this.scores[sortBy] - o.scores[sortBy];
		return d > 0 ? 1 : d < 0 ? -1 : 0;
	}
	
	//O(NlnN)
	public static void computeCrowding(Vector<ScoreArray> scoreArray, int num_scores) {
		for( ScoreArray s : scoreArray) {
			s.crowding = 0;
		}
		for( int i = 0; i < num_scores; i++) {
			ScoreArray.sortBy = i;
			Collections.sort(scoreArray);
			double normalizer = 1.0/(scoreArray.get(scoreArray.size()-1).scores[i] - scoreArray.get(0).scores[i]);
			
			scoreArray.get(0).crowding = -9999999999999.99;
			scoreArray.get(scoreArray.size()-1).crowding = -99999999999.99;

			for( int j = 1; j < scoreArray.size()-1; j++) {
				for( ScoreArray s : scoreArray) {
					double distance = normalizer*(scoreArray.get(j+1).scores[i] - scoreArray.get(j-1).scores[i]);
					scoreArray.get(j).crowding += Math.abs(distance);
				}
			}
		}
	}

	//O(NN)
	public static void computeDomination(Vector<ScoreArray> scoreArray, int num_scores) {
		for( ScoreArray s : scoreArray) {
			s.dominates = 0;
			s.dominated_by = 0;
		}
		for( ScoreArray s : scoreArray) {
			for( ScoreArray s2 : scoreArray) {
				if( s.dominates(s2)) {
					s.dominates++;
					s2.dominated_by++;
				}
			}
		}
	}
}
