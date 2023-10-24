package jsonMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

//this class is for parsing a json string into an object, and formatting an object into a json string.
//objects that you want to be able to save and load from/to a file should extend this object and implement the methods in iJSONObject.
//loading from a file will create key-value pairs in the hashmap it extends.  saving will create a json string from same.
//hence, move all data to it via pre_serialize before you save, and from it via post_deserialize after you load.
//that's it!
//check out the "jsonObjects" package for examples.
//note: this is a single-pass in-place serializer / de-serializer!   that is, all work, including object instantiation and member initialization, is done in one pass!
public abstract class aJsonMap extends HashMap<String, Object> implements iJsonMap, Serializable{
	private static final long serialVersionUID = 1L;
	public static int _json_verbosity = 0;
	private static final String _json_quote = "\"";
	private static final String _json_tab = "  ";
	public boolean _json_add_comma = false;
	
	public int deserialize(String s, int index){
		if(aJsonMap._json_verbosity > 0)
			System.out.println("deserialize "+s+" "+index);
		if(index < 0){
			index = 0;
		}
		String key = null;
		int mode = 0;
		int last_index = index;
		Stack<Vector<Object>> svo = new Stack<>();
		// Vector v = null;
		int array = 0;
		aJsonMap o = null;
		while(index < s.length()){
			char c = s.charAt(index);
			if(aJsonMap._json_verbosity > 0){
				//ItmdLog.trace(String.valueOf(c));
			}
			if(Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
				//if unicode, replace with space
				c = ' ';
			} 
			switch(c){
			case '\\':
				index++;
				index++;
				continue;
			case '{':
				// ignore if no key encountered yet.
				if((key == null) || (key.length() == 0)){
					//index++;
					last_index = index+1;
				}
				else{
					o = instantiateObject(key);
					index = o.deserialize(s, ++index);
				}
				break;
			case '}':
				if(mode == 1){
					if(array == 2){
						array = 0;
						mode = 0;
						put(key, svo.pop());
						if(aJsonMap._json_verbosity > 0){
						}
						last_index = index + 1;
					}
					else{
						mode = 0;
						if(o != null){
							put(key, o);
							if(aJsonMap._json_verbosity > 0){
							}
							o = null;
						}
						else{
							String ss = deEscapeControlCharacters(s.substring(last_index, index))/*.replaceAll("\"", "")*/.trim();
							put(key, ss);
							if(aJsonMap._json_verbosity > 0){
							}
						}
						last_index = index + 1;
					}
				}
				post_deserialize();
				return index;
			case '"':
				if(aJsonMap._json_verbosity > 1){
					System.out.println("skipping quotes "+index);
				}
				index++;
				for(; index < s.length(); index++){
					char c2 = s.charAt(index);
					if(aJsonMap._json_verbosity > 0){
						//ItmdLog.trace(String.valueOf(c2));
					}
					if(c2 == '\\'){
						if(aJsonMap._json_verbosity > 0)
							System.out.println("skipping slash "+index);
						index++;
					}
					if(c2 == '"'){
						if(aJsonMap._json_verbosity > 0)
							System.out.println("found matching quote "+index);
						//index--;
						break;
					}
				}
				break;
			case ',':
				if(aJsonMap._json_verbosity > 0)
					System.out.println("comma "+mode+" "+o);
				if(mode == 0){
					break;
				}
				if(array == 1){
					if(o != null){
						svo.peek().add(o);
						o = null;
					}
					else{
						svo.peek().add(deEscapeControlCharacters(s.substring(last_index, index))/*.replaceAll("\"", "")*/.trim());
					}
					last_index = index + 1;
				}
				else if(array == 2){
					array = 0;
					mode = 0;
					if(aJsonMap._json_verbosity > 0){
					}
					put(key, svo.pop());
					last_index = index + 1;
				}
				else{
					mode = 0;
					if(o != null){
						put(key, o);
						if(aJsonMap._json_verbosity > 0){
						}
						o = null;
					}
					else{
						String ss = deEscapeControlCharacters(s.substring(last_index, index))/*.replaceAll("\"", "")*/.trim();
						put(key, ss);
						if(aJsonMap._json_verbosity > 0){
						}
					}
					last_index = index + 1;
				}
				break;
			case ':':
				//no quotes allowed in keys
				key = s.substring(last_index, index).replaceAll("\"", "").replaceAll("\\,", "").trim();
				if(aJsonMap._json_verbosity > 0)
					System.out.println("key "+key+" mode "+mode);
				mode = 1;
				last_index = index + 1;
				break;
			case '[':
				svo.push(new Vector<>());
				last_index = index + 1;
				array = 1;
				break;
			case ']':
				if((array == 1) && ((last_index + 1) < index)){
					if(o != null){
						svo.peek().add(o);
						o = null;
					}
					else{
						String s2 = deEscapeControlCharacters(s.substring(last_index, index))/*.replaceAll("\"", "")*/.trim();
						if(s2.length() > 0){
							svo.peek().add(s2);
						}
					}
				}
				array = 2;
				if(aJsonMap._json_verbosity > 0){
				}
				Vector<Object> v = svo.pop();
				if(svo.size() > 0){
					svo.peek().add(v);
					// v = svo.pop();
					array = 1;
				}
				else{
					put(key, v);
					mode = 0;
					array = 0;
					v = null;
				}
				last_index = index + 1;
				break;
			default:
			}
			index++;
		}
		if(mode != 0){
			if((array == 1) && (svo.size() > 0)){
				if(o != null){
					svo.peek().add(o);
					o = null;
				}
				else{
					svo.peek().add(deEscapeControlCharacters(s.substring(last_index, index)/*.replaceAll("\"", "")*/).trim());
				}
				Vector<Object> v = svo.pop();
				while(svo.size() > 0){
					svo.peek().add(v);
					v = svo.pop();
				}
				// this.put(key, v);
				last_index = index + 1;
			}
			else if(array == 2){
				array = 0;
				mode = 0;
				if(aJsonMap._json_verbosity > 0){
				}
				Vector<Object> v = svo.pop();
				while(svo.size() > 0){
					svo.peek().add(v);
					v = svo.pop();
				}
				put(key, v);
				last_index = index + 1;
			}
			else{
				mode = 0;
				if(o != null){
					put(key, o);
					if(aJsonMap._json_verbosity > 0){
					}
					o = null;
				}
				else{
					if(index > s.length()){
						index = s.length();
					}
					if(last_index <= s.length()){
						String ss = deEscapeControlCharacters(s.substring(last_index, index))/*.replaceAll("\"", "")*/.trim();
						put(key, ss);
						if(aJsonMap._json_verbosity > 0){
						}
					}
				}
				last_index = index + 1;
			}
		}
		post_deserialize();
		return index;
	}
	public aJsonMap fromJson(String s){
		deserialize(s, 0);
		return this;
	}
	public boolean getBoolean(String key){
		if(!containsKey(key) || get(key) == null){
			return false;
		}
		try {
			return Boolean.valueOf(get(key).toString());
		} catch (Exception ex) {
			return false;
		}
	}
	public double getDouble(String key){
		if(!containsKey(key) || get(key) == null){
			return 0;
		}
		try {
			return new Double(get(key).toString());
		} catch (Exception ex) {
			return 0;
		}
	}
	public long getLong(String key){
		if(!containsKey(key) || get(key) == null){
			return 0;
		}
		try {
			String tempValue = get(key).toString();//FormatUtil.asString(get(key));
			tempValue = tempValue.replaceAll(",","");
			return Long.valueOf(tempValue);
		} catch (Exception ex) {
			return 0;
		}
	}
	public aJsonMap getObject(String key){
		return (aJsonMap) get(key);
	}
	
	// added conversion from base64 encoded string to byte[]
	public byte[] getBytes(String key){
		if( get(key) == null) { return null; }
		if(get(key) instanceof java.io.ByteArrayInputStream bis) {
			return byteStreamToByteArray(bis);
		}
		if( get(key) instanceof byte[]) {
			return (byte[])get(key);
		}
		if( get(key) instanceof String) {
			return Base64.getDecoder().decode(((String)get(key)).getBytes());
		}
		return get(key).toString().getBytes();
	}
	public byte[] byteStreamToByteArray(java.io.ByteArrayInputStream is){
		java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while((nRead = is.read(data, 0, data.length)) != -1){
			buffer.write(data, 0, nRead);
			try{
				Thread.sleep(10);
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		try{
			buffer.flush();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public java.io.ByteArrayInputStream getBytesArrayInputStream(String key){
		if( get(key) == null) { return null; }
		if( get(key) instanceof java.io.ByteArrayInputStream) {
			return (java.io.ByteArrayInputStream)get(key);
		}
		return null;
	}
	// added conversion from byte[] to base64 encoded string
	public String getString(String key){
		if( get(key) == null) { return null; }
		if(get(key) instanceof byte[] bb) {
			return new String(Base64.getEncoder().encode(bb));
		}
		return get(key).toString();
	}
	@SuppressWarnings("unchecked")
	public Vector<JsonMap> getVectorOfJsonMaps(String key){
		return (Vector<JsonMap>) get(key);
	}
	@SuppressWarnings("rawtypes")
	public Vector getVector(String key){
		return (Vector) get(key);
	}
	public JsonMap getJsonMap(String key){
		return (JsonMap) get(key);
	}
	public static void main(String[] ss) {
		String s;
		s = "\"";
		//ItmdLog.trace(s);
		System.out.println("----");
		s = escapeControlCharacters(s);
		//ItmdLog.trace(s);
		System.out.println("----");
		s = deEscapeControlCharacters(s);
		//ItmdLog.trace(s);
		System.out.println("----");
		System.exit(0);
	}
	public static String escapeControlCharacters(String s) {
		s = s.replaceAll("\\\\","\\\\\\\\");
		//s = s.replaceAll("\\b","\\\\b");
		s = s.replaceAll("\\f","\\\\f");
		s = s.replaceAll("\\n","\\\\n");
		s = s.replaceAll("\\r","\\\\r");
		s = s.replaceAll("\\t","\\\\t");
		s = s.replaceAll("\\\"","\\\\\"");
		return s;
	}
	public static String deEscapeControlCharacters(String s) {
		if( s == null) {
			return null;
		}
		s = s.trim();
		if( s.length() == 0) {
			return s;
		}
		if( s.charAt(0) == '"' && s.charAt(s.length()-1) == '"') {
			s = s.substring(1, s.length()-1);
		}
		s = s.replaceAll("\\\\\\\\",":escape slashes:");
		//s = s.replaceAll("\\\\b","\b");
		s = s.replaceAll("\\\\f","\f");
		s = s.replaceAll("\\\\n","\n");
		s = s.replaceAll("\\\\r","\r");
		s = s.replaceAll("\\\\t","\t");
		s = s.replaceAll("\\\\\"","\\\"");
		s = s.replaceAll(":escape slashes:","\\\\");
		return s;
	}
	public void serialize(StringBuffer sb, String prepend){
		pre_serialize();
		_json_add_comma = false;
		// sb.append(prepend+"{\n");
		for(Map.Entry<String, Object> t : entrySet()){
			/*
			if( t.getValue() instanceof String) {
				String s = escapeControlCharacters((String) t.getValue());
				writeObject(t.getKey(), s, sb, prepend + aJsonMap._json_tab, _json_add_comma);
			} else {
				writeObject(t.getKey(), t.getValue(), sb, prepend + aJsonMap._json_tab, _json_add_comma);
			}*/
			writeObject(t.getKey(), t.getValue(), sb, prepend + aJsonMap._json_tab, _json_add_comma);
			_json_add_comma = true;
		}
		// sb.append(prepend+"},\n");
	}
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		serialize(sb, "");
		return "{\n" + sb + "}";
	}

//	@Deprecated
//	public String toJson(){
//		StringBuffer sb = new StringBuffer();
//		serialize(sb, "");
//		return sb.toString();
//	}
	@SuppressWarnings("unchecked")
	private void writeObject(String name, Object obj, StringBuffer sb, String _prepend, boolean comma){
		if(aJsonMap._json_verbosity > 0)
			System.out.println("writeObject "+name+" : "+obj);
		if(name != null){
			sb.append(_prepend + (comma ? "," : "") + aJsonMap._json_quote + name + aJsonMap._json_quote + ":");
		}
		if(obj instanceof aJsonMap){
			aJsonMap o = (aJsonMap) obj;
			o.pre_serialize();
			if(name == null){
				sb.append(_prepend + (comma ? "," : ""));
			}
			sb.append("{\n");
			o.serialize(sb, _prepend + aJsonMap._json_tab);
			sb.append(_prepend + "}\n");
		}
		else{
			if(name == null){ sb.append((comma ? "," : "")); }
			if(obj instanceof Collection){
				sb.append("[\n");
				boolean acomma = false;
				for(Object o : (Collection<Object>) obj){
					writeObject(null, o, sb, _prepend + aJsonMap._json_tab, acomma);
					acomma = true;
				}
				sb.append(_prepend + "]\n");
			}
			if(obj instanceof double[]){
				sb.append("[\n");
				boolean acomma = false;
				for(double o : (double[]) obj){
					writeObject(null, o, sb, _prepend + aJsonMap._json_tab, acomma);
					acomma = true;
				}
				sb.append(_prepend + "]\n");
			}
			if( obj instanceof Object[]){
				sb.append("[\n");
				boolean acomma = false;
				for(Object o : (Object[]) obj){
					writeObject(null, o, sb, _prepend + aJsonMap._json_tab, acomma);
					acomma = true;
				}
				sb.append(_prepend + "]\n");
			}
			else if(obj instanceof Long){
				sb.append(((Long) obj).longValue() + "\n");
			}
			else if(obj instanceof Double){
				sb.append(((Double) obj).doubleValue() + "\n");
			}
			else if(obj instanceof Integer){
				sb.append(((Integer) obj).intValue() + "\n");
			}
			else if(obj instanceof Float){
				sb.append(((Float) obj).floatValue() + "\n");
			}
			else if(obj instanceof String){
				String s = escapeControlCharacters((String)obj);
				sb.append(aJsonMap._json_quote + s + aJsonMap._json_quote + "\n");
			}
			else if(obj instanceof byte[] bb) {
				String s = new String(Base64.getEncoder().encode(bb));
				sb.append( aJsonMap._json_quote + escapeControlCharacters(s) + aJsonMap._json_quote + "\n");
			}
			else if(obj instanceof Boolean) {
				sb.append( obj + "\n");
			}
			else{
				sb.append( obj + "\n");
				/*
				if(name != null){
					sb.append(aJsonMap._json_quote + escapeControlCharacters(getString(name)) + aJsonMap._json_quote + "\n");
				} else {
					sb.append(aJsonMap._json_quote + obj  + aJsonMap._json_quote + "\n");
				}
				*/
			}
		}
	}
	@Override
	public Object put(String s, Object o) {
		o = o == null ? "" : o;
		if(aJsonMap._json_verbosity > 0){
			System.out.println("put "+s+" "+o);
		}
		return super.put(s, o);
	}
}