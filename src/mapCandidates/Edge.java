package mapCandidates;

import serialization.ReflectionJSONObject;

public class Edge extends ReflectionJSONObject<Edge> {
	public int id;
	public int block1_id;
	public int block2_id;
	public int vertex1_id;
	public int vertex2_id;
    public double length;
    
	Block block1;
	Block block2;
	Vertex vertex1;
	Vertex vertex2;
	
    boolean areBothSidesSameDistrict(int[] block_districts) {
        return block_districts[block1.id] == block_districts[block2.id];
    }
}