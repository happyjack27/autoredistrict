package geoJSON;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;
import java.util.*;

public class Geometry extends ReflectionJSONObject<Geometry> {
	public String type;
	public double[][] coordinates;

	@Override
	public void post_deserialize() {
		super.post_deserialize();
		
		if( containsKey("coordinates")) {
			Vector<Vector<Object>> vvo = ((Vector<Vector<Vector<Object>>>)getVector("coordinates")).get(0);
			coordinates = new double[vvo.size()][];
			int j = 0;
			for( int i = 0; i < vvo.size(); i++) {
				try {
				Vector<Object> vo = vvo.get(i);
				coordinates[j] = new double[]{
						Double.parseDouble((String)vo.get(0)),	
						Double.parseDouble((String)vo.get(1)),	
				};
				j++;
				} catch (Exception ex) { 
					System.out.println("ex "+vvo.get(i));
				}
			}
			double[][] dd = new double[j][];
			for( int i = 0; i < dd.length; i++) {
				dd[i] = coordinates[i];
			}
			coordinates = dd;
		}
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		if( coordinates != null) {
			Vector<Vector<String>> v = new Vector<Vector<String>>();

			for( int i = 0; i < coordinates.length; i++) {
				Vector v2 = new Vector();
				v2.add(""+coordinates[i][0]);
				v2.add(""+coordinates[i][1]);
				v.add(v2);
			}
			Vector v3 = new Vector();
			v3.add(v);
			put("coordinates",v3);
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return super.instantiateObject(key);
	}
}
