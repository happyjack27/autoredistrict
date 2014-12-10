package mapCandidates;

import serialization.ReflectionJSONObject;

public class Edge extends ReflectionJSONObject<Edge> {
	public int id;
	public int block1_id;
	public int block2_id;
	public int vertex1_id;
	public int vertex2_id;
    public double length;
    
	public Block block1;
	public Block block2;
	public Vertex vertex1;
	public Vertex vertex2;
	
    boolean areBothSidesSameDistrict(int[] block_districts) {
        return block_districts[block1.id] == block_districts[block2.id];
    }
}