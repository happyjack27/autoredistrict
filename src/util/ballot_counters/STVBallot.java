package util.ballot_counters;

import java.util.Vector;

public class STVBallot {
	Choice[] choices;
	double weight = 1;

	public STVBallot clone() {
		return new STVBallot(weight,getChoicesAsArray());
	}
	

	
	public STVBallot(double count, int[] choices) {
		super();
		weight = count;
		this.choices = new Choice[choices.length];
		for( int j = 0; j < choices.length; j++) {
			this.choices[j] = new Choice(choices[j]);
		}
	}
	
	Choice getFirst(Vector<Integer> electeds, Vector<Integer> ignores) {
		for( int i = 0; i < choices.length; i++) {
			Choice c = choices[i];
			boolean pass = true;
			for( int j = 0; j < electeds.size(); j++) {
				if( c.candidate == electeds.get(j).intValue()) {
					pass = false;
					break;
				}
			}
			if( !pass) {
				continue;
			}
			for( int j = 0; j < ignores.size(); j++) {
				if( c.candidate == ignores.get(j).intValue()) {
					pass = false;
					break;
				}
			}
			if( !pass) {
				continue;
			}
			return c;
		}
		return null;
	}
	
	Choice getFirst() {
		for( int i = 0; i < choices.length; i++) {
			Choice c = choices[i];
			if( !c.ignored && !c.elected) {
				return c;
			}
		}
		return null;
	}
	void resetIgnores() {
		for( int i = 0; i < choices.length; i++) {
			choices[i].ignored = false;
		}
	}
	void resetElecteds() {
		for( int i = 0; i < choices.length; i++) {
			choices[i].elected = false;
		}
	}
	void candidateElected(int c) {
		for( int i = 0; i < choices.length; i++) {
			if( choices[i].candidate == c) {
				choices[i].elected = true;
			}
		}
	}

	public int[] getChoicesAsArray() {
		int[] ichoices = new int[choices.length];
		for( int i = 0; i < choices.length; i++) {
			ichoices[i] = choices[i].candidate;
		}
		return ichoices;
	}
}


class Choice {
	int candidate = -1;
	boolean ignored = false;
	boolean elected = false;
	public Choice(int i) { candidate = i; }
}
