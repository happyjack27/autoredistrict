package util;


public class Triplet<T1 extends Comparable<T1>, T2, T3> implements Comparable<Triplet<T1,T2,T3>>{
	public T1 a;
	public T2 b;
	public T3 c;
	public Triplet(T1 _a, T2 _b, T3 _c) {
		a = _a;
		b = _b;
		c = _c;
	}
	@Override
	public int compareTo(Triplet<T1, T2, T3> o) {
		return a.compareTo(o.a);
	}

}
