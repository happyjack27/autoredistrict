package util;

import java.util.HashSet;
import java.util.Vector;

public class HashVector<T> extends Vector<T> {
	private static final long serialVersionUID = 1L;
	HashSet<T> hs = new HashSet<T>();
	
	@Override
	public boolean contains(Object t) {
		return hs.contains(t);
	}
	@Override
	public boolean add(T t) {
		if( hs.contains(t)) {
			return true;
		}
		hs.add(t);
		return super.add(t);
	}
	@Override
	public boolean remove(Object t) {
		hs.remove(t);
		return super.remove(t);
	}
	@Override
	public T remove(int i) {
		hs.remove(this.get(i));
		return super.remove(i);
	}
	

}
