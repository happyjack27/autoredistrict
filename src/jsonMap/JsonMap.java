package jsonMap;

public class JsonMap extends aJsonMap{
	private static final long serialVersionUID = 1L;
	public JsonMap(){
		super();
	}
	public JsonMap(String from){
		this();
		if(from != null){
			fromJson(from);
		}
	}
	@Override
	public JsonMap instantiateObject(String key){
		return new JsonMap();
	}
	@Override
	public void post_deserialize(){}
	@Override
	public void pre_serialize(){}
	@Override
	public JsonMap getObject(String s){
		return (JsonMap) super.get(s);
	}
	public static JsonMap predicate(String logic, String key, String op, String value){
		return new JsonMap("{ logicOp:  \"" + logic + "\", key: \"" + key + "\", comparisonOp: \"" + op + "\", value: \"" + value + "\"}");
	}
	public static JsonMap predicate(String key, String op, String value){
		return JsonMap.predicate("AND", key, op, value);
	}
	public static JsonMap predicate(String key, String value){
		return JsonMap.predicate("AND", key, "=", value);
	}
}