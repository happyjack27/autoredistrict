package solutions;

import java.util.Vector;

import serialization.ReflectionJSONObject;


public class Candidate extends ReflectionJSONObject<Vertex>  {
    public int index;
    public String id;
    public static Vector<Candidate> candidates = new Vector<Candidate>(); 
}
