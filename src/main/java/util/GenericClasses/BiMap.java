package util.GenericClasses;

import java.util.Hashtable;
import java.util.Map;

public class BiMap<K extends Object, V extends Object> {
	

	  private final Map<K,V> forward = new Hashtable<K, V>();
	  private final Map<V,K> backward = new Hashtable<V, K>();

	  public synchronized void put(K key, V value) {
	    forward.put(key, value);
	    backward.put(value, key);
	  }

	  public synchronized V get(K key) { return getForward(key); }
	  
	  public synchronized V getForward(K key) {
	    return forward.get(key);
	  }

	  public synchronized K getBackward(V key) {
	    return backward.get(key);
	  }
	  public int size() { return forward.size(); }

}