package jsonMap;

public interface iJsonMap{
	public JsonMap instantiateObject(String key);
	public void post_deserialize();
	public void pre_serialize();
}