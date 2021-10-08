package pareto.prototypes;

import java.util.HashMap;
import java.util.concurrent.locks.*;

public abstract class ParetoPoint<T extends ParetoPoint<T>> {
	HashMap<String,Double> scores = new HashMap<String,Double>();
	protected HashMap<String,Scorer<T>> scorers = new HashMap<String,Scorer<T>>();

	boolean dominated = false;
	
	protected double mutation_amount = 0;
	
	Lock linkedList = new ReentrantLock();
	ParetoPoint<T> next = null;
	ParetoPoint<T> prev = null;
	
	public synchronized void remove() {
		linkedList.lock();
		if( next != null) {
			next.linkedList.lock();
		}
		if( prev != null) {
			prev.linkedList.lock();
		}

		dominated = true;
		if( next != null) {
			next.prev = prev;
		}
		if( prev != null) {
			prev.next = next;
		}
		
		if( next != null) {
			next.linkedList.unlock();
		}
		if( prev != null) {
			prev.linkedList.unlock();
		}
		linkedList.unlock();
	}
	public synchronized void add(ParetoPoint<T> s) {
		s.linkedList.lock();
		linkedList.lock();
		if( next != null) {
			next.linkedList.lock();
		}
		
		if( next != null) {
			next.prev = s;
		}
		s.next = next;
		s.prev = this;
		next = s;
		
		s.linkedList.unlock();
		linkedList.unlock();
		if( s.next != null) {
			s.next.linkedList.unlock();
		}
	}
	public abstract ParetoPoint<T> clone(double mutation);
	public abstract ParetoPoint<T> generate();
	public ParetoPoint(HashMap<String,Scorer<T>> scorers) {
		this.scorers = scorers;
	}
	public void computeScores() {
		for( String s : scorers.keySet()) {
			Scorer<T> scorer = scorers.get(s);
			this.scores.put(s, scorer.computeScore(this));
		}
	}

}
