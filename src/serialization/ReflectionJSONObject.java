package serialization;

import java.lang.reflect.*;
import java.util.Vector;

public class ReflectionJSONObject<T> extends JSONObject {

	@Override
	public void post_deserialize() {
		//System.out.println("post deserialize: ");

		Class<T> t = (Class<T>) this.getClass();
		Field[] fields = t.getFields();
		for( int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String name = f.getName();
			//System.out.println("name: "+name);
			if( name.indexOf("_json_") == 0 || !containsKey(name)) {
				continue;
			}
			Type type = f.getType();
			try {
				if( type.equals(int.class)) {
					f.set(this, (int)getDouble(name));
				} else if( type.equals(Double.class)) {
					f.set(this, (double)getDouble(name));	
				} else if( type.equals(Float.class)) {
					f.set(this, (float)getDouble(name));
				} else if( type.equals(String.class)) {
					f.set(this, getString(name));
				} else if( type.equals(boolean.class)) {
					f.set(this, getString(name).equals("true"));
				} else if( type.equals(double[].class)) {
					Vector<String> vd = this.getVector(name);
					double[] dd = new double[vd.size()];
					for( int j = 0; j < vd.size(); j++) {
						dd[j] = Double.parseDouble(vd.get(j));
					}
					f.set(this, dd);	
					
				}
			} catch (Exception ex) { }
			
		}
	}

	@Override
	public void pre_serialize() {
		Class<T> t = (Class<T>) this.getClass();
		Field[] fields = t.getFields();
		for( int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String name = f.getName();
			if( name.indexOf("_json_") == 0) {
				continue;
			}
			Type type = f.getType();
			try {
				if( type.equals(int.class)) {
					put(name,""+f.getInt(this));
				} else if( type.equals(Double.class)) {
					put(name,""+f.getDouble(this));
				} else if( type.equals(Float.class)) {
					put(name,""+f.getFloat(this));
				} else if( type.equals(String.class)) {
					put(name,""+(String)f.get(this));
				} else if( type.equals(boolean.class)) {
					put(name,""+f.getBoolean(this));
				} else {
					put(name,f.get(this));
				}
			} catch (Exception ex) { }
		}
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		return  null;
	}

}
