package jsonMap;

public interface iJsonMap{
	JsonMap instantiateObject(String key);
	void post_deserialize();
	void pre_serialize();
}