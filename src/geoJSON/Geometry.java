package geoJSON;

import mapCandidates.Block;
import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class Geometry extends ReflectionJSONObject<Geometry> {
	public String type;
	public double[][] coordinates;

	@Override
	public void post_deserialize() {
		super.post_deserialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}
