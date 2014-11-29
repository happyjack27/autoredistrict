package serializable;
import java.util.*;

//this class is for parsing a json string into an object, and formatting an object into a json string.
//objects that you want to be able to save and load from/to a file should extend this object and implement the methods in iJSONObject.
//loading from a file will create key-value pairs in the hashmap it extends.  saving will create a json string from same.
//hence, move all data to it via pre_serialize before you save, and from it via post_deserialize after you load.
//that's it!
//check out the "jsonObjects" package for examples.
//note: this is a single-pass in-place serializer / de-serializer!   that is, all work, including object instantiation and member initialization, is done in one pass!     
public abstract class JSONObject extends HashMap<String,Object> implements iJSONObject {
	static final String _json_tab = "  ";
	static final String _json_quote = "\"";
	static int _json_verbosity = 0;
	static int _json_indents = 0;
	
	
	   public String toJSON() {
		   StringBuffer sb = new StringBuffer();
		   serialize(sb,"");
		   return sb.toString();
	   }
	   public void fromJSON(String s) {
		   deserialize(s,0);
	   }
	
	
		public int deserialize(String s, int index) {
			if( index < 0) index = 0;
			String key = null;
			int mode = 0;
			int last_index = index;
			Vector v = null;
			int array = 0;
			JSONObject o = null;
			while( index < s.length()) {
				char c = s.charAt(index);
				if( _json_verbosity > 0) System.out.print(c);
				switch(c) {
				case '{':
					_json_indents++;

					o = instantiateObject(key);
					index = o.deserialize(s, ++index);
					break;
				case '}':
					if( mode == 1) {
						if( array == 2) {
							array = 0;
							mode = 0;
							this.put(key, v);
							if( _json_verbosity > 0) System.out.println("|putting array "+key+" "+v+"| "+this.size());
							last_index = index+1;					
						} else {
							mode = 0;
							if( o != null) {
								this.put(key, o);
								if( _json_verbosity > 0) System.out.println("|putting "+key+" object| "+this.size());
								o = null;
							} else  {
								String ss = s.substring(last_index,index).replaceAll("\"","").trim();
								this.put(key, ss);
								if( _json_verbosity > 0) System.out.println("|putting "+key+" "+ss+"| "+this.size());
							}
							last_index = index+1;					
						}
					}
					_json_indents--;
					//System.out.println("returning1 "+size()+" "+mode+" "+array+" "+indents+" "+this);
					post_deserialize();
					return index;
				case '"':
					//System.out.println("open quote "+index);
					index++;
					for(; index < s.length(); index++) {
						char c2 = s.charAt(index);
						if( c2 == '"') {
							break;
						}
					}
					//System.out.println("close quote "+index);
					break;
				case ',':
					if( mode == 0)
						break;
					if( array == 1) {
						if( o != null) {
							v.add(o);
							o = null;
						} else  {
							v.add(s.substring(last_index,index).replaceAll("\"","").trim());
						}
						last_index = index+1;
					} else if( array == 2) {
						array = 0;
						mode = 0;
						if( _json_verbosity > 0) System.out.println("|putting array3 "+key+" "+v.size()+" "+v+"|");
						this.put(key, v);
						last_index = index+1;					
					} else {
						mode = 0;
						if( o != null) {
							this.put(key, o);
							if( _json_verbosity > 0) System.out.println("|putting1 "+key+" object| "+this.size());
							o = null;
						} else  {
							String ss = s.substring(last_index,index).replaceAll("\"","").trim();
							this.put(key, ss);
							if( _json_verbosity > 0) System.out.println("|putting2 "+key+" "+ss+"| "+this.size());
						}
						last_index = index+1;
					}
					break;
				case ':':
					key = s.substring(last_index,index).replaceAll("\"","").replaceAll("\\,", "").trim();
					mode = 1;
					last_index = index+1;
					break;
				case '[':
					v = new Vector();
					array = 1;
					break;
				case ']':
					if( array == 1 && last_index+1 < index) {
						if( o != null) {
							v.add(o);
							o = null;
						} else  {
							String s2 = s.substring(last_index,index).replaceAll("\"","").trim();
							if( s2.length() > 0) {
								v.add(s2);
							}
						}
					}				
					mode = 0;
					array = 2;
					if( _json_verbosity > 0) System.out.println("|putting array2 "+key+" "+v.size()+" "+v+"|");
					this.put(key, v);
					array = 0;
					last_index = index+1;
					break;
				default:
				}
				index++;
			}
			
			
			if( mode != 0) {
				if( array == 1) {
					if( o != null) {
						v.add(o);
						o = null;
					} else  {
						v.add(s.substring(last_index,index).replaceAll("\"","").trim());
					}
					last_index = index+1;
				} else if( array == 2) {
					array = 0;
					mode = 0;
					if( _json_verbosity > 0) System.out.println("|putting array3 "+key+" "+v.size()+" "+v+"|");
					this.put(key, v);
					last_index = index+1;					
				} else {
					mode = 0;
					if( o != null) {
						this.put(key, o);
						if( _json_verbosity > 0) System.out.println("|putting1 "+key+" object| "+this.size());
						o = null;
					} else  {
						String ss = s.substring(last_index,index).replaceAll("\"","").trim();
						this.put(key, ss);
						if( _json_verbosity > 0) System.out.println("|putting2 "+key+" "+ss+"| "+this.size());
					}
					last_index = index+1;
				}
			}
			//this.put(key, o);
			/*
			System.out.println("key: "+key);
			System.out.println("o: "+o);
			
			System.out.println("this: "+this);
			System.out.println("size: "+this.size()+" "+mode+" "+array);
			*/
			post_deserialize();
			return index;
		}
	
	public void serialize(StringBuffer sb, String prepend) {
		pre_serialize();
		//sb.append(prepend+"{\n");
		for( Map.Entry<String, Object> t: entrySet() ) {
			writeObject(t.getKey(),t.getValue(),sb,prepend+_json_tab);
			
		}
		//sb.append(prepend+"},\n");
	}
	private void writeObject(String name, Object obj, StringBuffer sb, String prepend) {
		if( name != null) {
			sb.append(prepend+_json_quote+name+_json_quote+":");
		}
		if(obj instanceof JSONObject) {
			JSONObject o = (JSONObject)obj;
			o.pre_serialize();
			if( name == null) {
				sb.append(prepend);
			}
			sb.append("{\n");
			o.serialize(sb,prepend+_json_tab);
			sb.append(prepend+"},\n");
		} else {
		if(obj instanceof Collection) {
			Collection<Object> v = (Collection<Object>)obj;
			sb.append("[\n");
			for( Object o : v) {
				writeObject(null,o,sb,prepend+_json_tab);
			}
			sb.append(prepend+"],\n");	
		} else
		if(obj instanceof String) {
			String o = (String)obj;
			sb.append(_json_quote+o+_json_quote+",\n");
		} else
		if(obj instanceof Integer) {
			Integer o = (Integer)obj;
			sb.append(o.intValue()+",\n");
		} else
		if(obj instanceof Float) {
			Float o = (Float)obj;
			sb.append(o.doubleValue()+",\n");
		} else
			sb.append(obj+",\n");
		}
		
	}
	
	public String getString(String key) {
		return (String)get(key);
	}
	public JSONObject getObject(String key) {
		return (JSONObject)get(key);
	}
	public Vector getVector(String key) {
		return (Vector)get(key);
	}
	public double getDouble(String key) {
		if( !containsKey(key)) 
			return 0;
		return new Double((String)get(key));
	}
}