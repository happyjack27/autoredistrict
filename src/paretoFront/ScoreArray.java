package paretoFront;

import java.util.*;

//http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=DB2C0FB4F2159957393DAD16E5B755B6?doi=10.1.1.542.385&rep=rep1&type=pdf
public class ScoreArray<T> implements Comparable<ScoreArray<T>> {
	public static int NUM_SCORES = 0;
	public static boolean first_criteria_dominates = true;
	public static boolean first_criteria_dominates_crowding = true;
	
	public static int sortBy = 0;
	public double[] scores = null;
	public T scoredObject = null;
	
	public double crowding;
	public double dominates;
	public double dominated_by;
	//public double age = 0;
	
	public ScoreArray(T scoredObject, double[] scores) {
		this.scoredObject = scoredObject;
		this.scores = scores;
	}
	public static String listScores( Vector<ScoreArray> scores2) {
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < scores2.size(); i++) {
			ScoreArray s = scores2.get(i);
			sb.append(""+i+": "+s.dominated_by+"   "+s.dominates+"   "+s.crowding);
			
			sb.append("\n");
		}
		
		return sb.toString();
		
	}
	
	public static String listAllNonDominated( Vector<ScoreArray> scores2) {
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < scores2.size(); i++) {
			ScoreArray s = scores2.get(i);
			if( s.dominated_by <= 0) {
				sb.append(""+i+": "+s.dominated_by+", "+s.dominates+", "+s.crowding);
				for( int j = 0; j < s.scores.length; j++) {
					sb.append(", ");
					sb.append(s.scores[j]);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void sortByParetoFitness(Vector<ScoreArray> scores2) {
		computeDomination(scores2);
		computeCrowding(scores2);
		sortBy = -1;
		Collections.sort(scores2);
	}
	
	//lower is better
	public boolean dominates(ScoreArray<T> s) {
		for( int i = 0; i < NUM_SCORES; i++) {
			if( s.scores[i] < this.scores[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	//-1 if we are better
	public int compareTo(ScoreArray<T> o) {
		// < 0 means we want to sort by domination.
		if( sortBy < 0) {
			if( this.dominated_by < o.dominated_by) {
				return -1;
			}
			if( this.dominated_by > o.dominated_by) {
				return 1;
			}
			if( first_criteria_dominates) {
				if( this.scores[0] < o.scores[0]) {
					return -1;
				}
				if( this.scores[0] > o.scores[0]) {
					return 1;
				}
			}


			if( this.dominates > o.dominates) {
				return -1;
			}
			if( this.dominates < o.dominates) {
				return 1;
			}
			if( first_criteria_dominates_crowding) {
				if( this.scores[0] < o.scores[0]) {
					return -1;
				}
				if( this.scores[0] > o.scores[0]) {
					return 1;
				}
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
	public static void computeCrowding(Vector<ScoreArray> scores2) {
		for( ScoreArray s : scores2) {
			s.crowding = 0;
		}
		for( int i = 0; i < NUM_SCORES; i++) {
			ScoreArray.sortBy = i;
			Collections.sort(scores2);
			double normalizer = (scores2.get(scores2.size()-1).scores[i] - scores2.get(0).scores[i]);
			if( normalizer == 0) { normalizer = 1; }
			normalizer = 1.0/normalizer;
			
			scores2.get(0).crowding += 9999999999999.99;
			scores2.get(scores2.size()-1).crowding += 99999999999.99;

			for( int j = 1; j < scores2.size()-1; j++) {
				for( ScoreArray s : scores2) {
					double distance = normalizer*(scores2.get(j+1).scores[i] - scores2.get(j-1).scores[i]);
					scores2.get(j).crowding += Math.abs(distance);
				}
			}
		}
	}

	//O(NN)
	public static void computeDomination(Vector<ScoreArray> scores2) {
		for( ScoreArray s : scores2) {
			s.dominates = 0;
			s.dominated_by = 0;
		}
		for( ScoreArray s : scores2) {
			for( ScoreArray s2 : scores2) {
				if( s == s2) {
					continue;
				}
				if( s.dominates(s2)) {
					s.dominates++;
					s2.dominated_by++;
				}
			}
		}
	}
}
