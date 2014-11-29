package serializable;

import java.lang.reflect.*;

public class ReflectionJSONObject<T> extends JSONObject {

	@Override
	public void post_deserialize() {
		Class<T> t = (Class<T>) this.getClass();
		Field[] fields = t.getFields();
		for( int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String name = f.getName();
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
					
				}
			} catch (Exception ex) { }
			
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		// TODO Auto-generated method stub
		Class<T> t = (Class<T>) this.getClass();
		Field[] fields = t.getFields();
		for( int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String name = f.getName();
			if( name.indexOf("_json_") == 0 || !containsKey(name)) {
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
				}
			} catch (Exception ex) { }
		}
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
