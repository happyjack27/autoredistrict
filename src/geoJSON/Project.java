package geoJSON;

import java.awt.Color;

import serialization.*;


public class Project extends ReflectionJSONObject<Project> {

	public void post_deserialize() {
		super.post_deserialize();
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
	}

	@Override
	public JSONObject instantiateObject(String key) {
		if( key.equals("demographics")) {
			return new DemographicSet();
		}
		return super.instantiateObject(key);
	}
	
}
