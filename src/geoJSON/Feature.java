package geoJSON;

import mapCandidates.Block;
import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class Feature extends ReflectionJSONObject<Feature> {
	public String type;
	public JSONObject properties;
	public Geometry geometry;

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
		if( key.equals("geometry")) {
			return new Geometry();
		}
		if( key.equals("properties")) {
			return new Properties();
		}
		super.instantiateObject(key);
		// TODO Auto-generated method stub
		return null;
	}
}
