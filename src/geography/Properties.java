package geography;

import java.util.Hashtable;

import serialization.ReflectionJSONObject;

public class Properties extends ReflectionJSONObject<Properties> {
	public boolean from_shape_file = false;
	public int esri_rec_num = -1;
	public double AREA;
	public int POPULATION;
	public  Hashtable<String,Integer> temp_hash = new Hashtable<String,Integer>();
	
	public void post_deserialize() {
		super.post_deserialize();
		if( containsKey("REC_NUM")) {
			from_shape_file = true;
		}
		if( !containsKey("POPULATION")) {
			if( containsKey("POP18")) {
				POPULATION = (int) getDouble("POP18");
			} else
			if( containsKey("PERSONS")) {
				POPULATION = (int) getDouble("PERSONS");
			}
		}
	}
	public void pre_serialize() {
		if( from_shape_file) {
			put("REC_NUM",esri_rec_num);
		}
		super.pre_serialize();
	}
}
