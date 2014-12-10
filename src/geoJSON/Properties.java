package geoJSON;

import serialization.ReflectionJSONObject;

public class Properties extends ReflectionJSONObject<Properties> {
	public int ID;
	public double AREA;
	public String DISTRICT;
	public String NAME;
	public int POPULATION;
	
	public void post_deserialize() {
		super.post_deserialize();
		if( !containsKey("DISTRICT")) {
			if( containsKey("PCT")) {
				DISTRICT = getString("PCT");
			}
		}
	}
}
