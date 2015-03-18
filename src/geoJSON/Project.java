package geoJSON;

import java.awt.Color;
import java.util.*;

import serialization.*;


public class Project extends ReflectionJSONObject<Project> {
	public String source_file = "";
	public int number_of_districts = 10;
	public int members_per_district = 1;
	public boolean area_weigthed = true;
	public int initial_population = 128;
	public int simulated_elections = 4;
	public String population_column = "";
	public String result_column = "";
	public Vector<DemographicSet> demographics = new Vector<DemographicSet>();
	public String active_demographic_set = "";
	public double disconnected_weight = 0.5;
	public double population_balance_weight = 0.5;
	public double border_length_weight = 0.5;
	public double voting_power_weight = 0.5;
	public double representation_weight = 0.5;
	public boolean equalize_turnout = false;

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
