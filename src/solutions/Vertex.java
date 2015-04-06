package solutions;

import serialization.ReflectionJSONObject;

public class Vertex extends ReflectionJSONObject<Vertex> {
	public int id;
	public double x;
	public double y;
	
	public Vertex() {
		super();
	}

	
	public Vertex( double x, double y) {
		this();
		this.x = x;
		this.y = y;
	}
}
