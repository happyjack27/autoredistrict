package util;

public class Pair<T1 extends Comparable<T1>, T2> implements Comparable<Pair<T1, T2>> {
	public T1 a;
	public T2 b;
	public Pair(T1 _a, T2 _b) {
		a = _a;
		b = _b;
	}
	@Override
	public int compareTo(Pair<T1, T2> o) {
		return a.compareTo(o.a);
	}

}
