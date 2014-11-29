package mapCandidates;

import serializable.JSONObject;

public class Edge extends JSONObject {
	public Block block1;
	public Block block2;
	public Vertex vertex1;
	public Vertex vertex2;
    public double length;
    boolean areBothSidesSameDistrict(int[] block_districts) {
        return block_districts[block1.index] == block_districts[block2.index];
    }
	@Override
	public void post_deserialize() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void pre_serialize() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}