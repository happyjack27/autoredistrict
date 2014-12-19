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
			} else if( containsKey("BLOCK")) {
				DISTRICT = getString("BLOCK");
			} else if( containsKey("WARD")) {
				DISTRICT = getString("WARD");
			} else if( containsKey("NAME")) {
				DISTRICT = getString("NAME");
			} else if( containsKey("MCD")) {
				DISTRICT = getString("MCD");
			} else if( containsKey("MCD_NAME")) {
				DISTRICT = getString("MCD_NAME");
			}
			if( containsKey("GEOID10")) {
				DISTRICT = getString("GEOID10");
			}
		}
		if( !containsKey("POPULATION")) {
			if( containsKey("PERSONS")) {
				POPULATION = (int) getDouble("PERSONS");
			}
		}
		if( DISTRICT != null) {
			DISTRICT = DISTRICT.trim();
		}
	}
}
