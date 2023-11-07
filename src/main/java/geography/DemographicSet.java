package geography;

import java.util.Vector;

import jsonMap.JsonMap;
import jsonMap.ReflectJsonMap;
import solutions.Election;

public class DemographicSet extends ReflectJsonMap<DemographicSet> {
	public String name = "";
	public String year = "";
    public Vector<String> columns = new Vector<String>();
	
    //public Vector<Demographic> demographics = new Vector<Demographic>();

	public void post_deserialize() {
		super.post_deserialize();
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
	}

	@Override
	public JsonMap instantiateObject(String key) {
		return super.instantiateObject(key);
	}

}
