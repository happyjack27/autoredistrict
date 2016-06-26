package util.ballot_counters;

public class Triplet<A extends Comparable<A>,B,C> implements Comparable<Triplet<A,B,C>> {
	A a;
	B b;
	C c;
	public Triplet(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	@Override
	public int compareTo(Triplet<A, B, C> o) {
		return a.compareTo(o.a);
	}
}
