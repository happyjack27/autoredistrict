package serializable;

public interface iJSONObject {
	public void post_deserialize();
	public void pre_serialize() ;
	public JSONObject instantiateObject(String key);

}