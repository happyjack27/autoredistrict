package util.ballot_counters;

public class Pair<A extends Comparable<A>,B> implements Comparable<Pair<A,B>> {
	A a;
	B b;
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public int compareTo(Pair<A, B> o) {
		return a.compareTo(o.a);
	}
}
