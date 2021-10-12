package pareto.prototypes;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.AdaptiveMutation;

public class ParetoFrontier<T extends ParetoPoint<T>> extends ParetoPoint<T> {
	public ParetoFrontier(HashMap<String,Scorer<T>> scorers) {
		super(scorers);
	}

	int max_size = 500;
	int initial_size = 100;
	int cur_size = 0;
	
	//Vector<SimpleDistrictMap> frontier = new Vector<SimpleDistrictMap>();
	//HashMap<String,TreeMap<Double,SimpleDistrictMap>> scoreSorted = new HashMap<String,TreeMap<Double,SimpleDistrictMap>>();
	AdaptiveMutation mutationRate = new AdaptiveMutation();
	
	Lock threadsLock = new ReentrantLock();

	
	//ConcurrentLinkedQueue<Integer> recycle = new ConcurrentLinkedQueue<Integer>();
	//ConcurrentLinkedQueue<Integer> recycle = new ConcurrentLinkedQueue<Integer>();
	
	ParetoPoint<T> cur = this;
	public ParetoPoint<T> generator = null;
	
	Vector<IterateThread> iterateThreads = new Vector<IterateThread>();
	class IterateThread extends Thread {
		public boolean on = true;
		public void run() {
			while( on) {
				iterate();
			}
		}
	}
	public void startIterateThreads(int n) {
		threadsLock.lock();
		for( int i = 0; i < n; i++) {
			IterateThread it = new IterateThread(); 
			iterateThreads.add(it);
			it.start();
		}
		threadsLock.unlock();
	}
	public void stopAllIterateThreads() {
		threadsLock.lock();
		for( IterateThread it : iterateThreads) {
			it.on = false;
		}
		while( iterateThreads.size() > 0) {
			try {
				iterateThreads.get(0).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			iterateThreads.remove(0);
		}
		threadsLock.unlock();
	}
	
	public void iterate() {
		ParetoPoint<T> test = null;
		if( cur_size <= initial_size) {
			test = generator.generate();
		} else {
			cur = cur.next;
			if( cur == null) {
				cur = next;
			}
			test = cur.clone(mutationRate.getSample());
			//test = new T(cur,mutationRate.getSample());
		}
		test.computeScores();
		for( ParetoPoint<T> now = next; now != null && now != this; now = now.next) {
			if( now.dominated) {
				continue;
			}
			if( isDominated( now, test)) {
				now.dominated = true;
				now.remove();
				cur_size--;
				if( test.mutation_amount > 0) {
					mutationRate.addSample(test.mutation_amount);
				}
			}
			if( isDominated( test, now)) {
				test.dominated = true;
				cur_size--;
				continue;
			}
			if( cur_size < max_size) {
				cur_size++;
				add(test);
			}
		}
	}
	
	//lower is better
	public boolean isDominated(ParetoPoint<T> a, ParetoPoint<T> b) {
		for( String score: scorers.keySet()) {
			if( a.scores.get(score) < b.scores.get(score)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ParetoPoint<T> clone(double mutation) {
		return null;
	}

	@Override
	public ParetoPoint<T> generate() {
		return null;
	}
}
