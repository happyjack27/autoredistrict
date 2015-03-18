package geoJSON;

import java.awt.Color;
import java.util.*;

import serialization.*;


public class Project extends ReflectionJSONObject<Project> {
	String source_file = "";
	int number_of_districts = 10;
	int members_per_district = 1;
	boolean area_weigthed = true;
	int initial_population = 128;
	int simulated_elections = 4;
	String population_column = "";
	String result_column = "";
	Vector<DemographicSet> demographics = new Vector<DemographicSet>();
	String active_demographic_set = "";
	double disconnected_weight = 0.5;
	double population_balance_weight = 0.5;
	double border_length_weight = 0.5;
	double voting_power_weight = 0.5;
	double representation_weight = 0.5;

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
