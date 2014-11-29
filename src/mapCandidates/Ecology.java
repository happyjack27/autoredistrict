package mapCandidates;

import java.util.*;

import serializable.*;

public class Ecology extends JSONObject {
	
	Vector<District> districts = new Vector<District>();
	Vector<DistrictMap> districtmaps = new Vector<DistrictMap>();
	Vector<Candidate> candidates = new Vector<Candidate>();
	Vector<Block> blocks = new Vector<Block>();
	Vector<Edge> edges = new Vector<Edge>();
	Vector<Vertex> vertexes = new Vector<Vertex>();
	Settings settings = new Settings();

	@Override
	public void post_deserialize() {
		if( containsKey("districts")) { districts = getVector("districts"); }
		if( containsKey("candidates")) { candidates = getVector("candidates"); }
		if( containsKey("blocks")) { blocks = getVector("blocks"); }
		if( containsKey("edges")) { edges = getVector("edges"); }
		if( containsKey("vertexes")) { vertexes = getVector("vertexes"); }
		if( containsKey("settings")) { settings = (Settings)get("settings"); }
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		if( key.equals("vertexes")) {
			return new Vertex();
		}
		if( key.equals("edges")) {
			return new Edge();
		}
		if( key.equals("blocks")) {
			return new Block();
		}
		if( key.equals("districts")) {
			return new District();
		}
		if( key.equals("candidates")) {
			return new Candidate();
		}
		if( key.equals("settings")) {
			return new Settings();
		}
		return null;
	}

}
