package geoJSON;

import java.util.*;

import mapCandidates.Block;
import mapCandidates.Edge;
import mapCandidates.Vertex;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class FeatureCollection extends ReflectionJSONObject<FeatureCollection> {
	public String type;
	public Vector<Feature> features;
	public Vector<Block> blocks;
	public HashMap<String,Block> precinctHash;
	
	HashMap<Double,HashMap<Double,Vertex>> vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
	HashMap<Vertex,HashMap<Vertex,Edge>> edgeHash = new HashMap<Vertex,HashMap<Vertex,Edge>>();

	@Override
	public void post_deserialize() {
		super.post_deserialize();
		if( containsKey("features")) {
			features = getVector("features");
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		if( key == null) {
			System.out. println("null key!");
		}
		if( key.equals("features")) {
			return new Feature();
		}
		return super.instantiateObject(key);
	}
	
	public void initBlocks() {
		blocks = new Vector<Block>();
		precinctHash = new HashMap<String,Block>();
		Block.id_enumerator = 0;
		for( Feature f : features) {
			f.block = new Block();
			f.block.name = f.properties.DISTRICT;
			blocks.add(f.block);
			precinctHash.put(f.block.name,f.block);
		}
		collectVertexes();
		collectEdges();
		for( Feature f : features) {
			f.block.collectNeighbors();
		}
		/*
		for( Feature f : features) {
			f.block.edges = new Vector<Edge>();
		}
		*/
		vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
		edgeHash = new HashMap<Vertex,HashMap<Vertex,Edge>>();
	}
	
	void collectEdges() {
		edgeHash = new HashMap<Vertex,HashMap<Vertex,Edge>>();
		for( Feature f : features) {
			f.block.edges = new Vector<Edge>();
			for( int i = 0; i < f.geometry.coordinates.length; i++) {
				double[][] c = f.geometry.coordinates[i];
				for( int j = 0; j < c.length; j++) {
					Vertex v1 = vertexHash.get(c[j][0]).get(c[j][1]);
					Vertex v2 = vertexHash.get(c[j+1 == c.length ? 0 : j+1][0]).get(c[j+1 == c.length ? 0 : j+1][1]);
					if( v1.id > v2.id) {
						Vertex t = v1;
						v1 = v2;
						v2 = t;
					}
					HashMap<Vertex,Edge> ve = edgeHash.get(v1);
					if( ve == null) {
						ve = new HashMap<Vertex,Edge>();
						edgeHash.put(v1, ve);
					}
					Edge e = ve.get(v2);
					if( e == null) {
						e = new Edge();
						e.vertex1_id = v1.id;
						e.vertex2_id = v2.id;
						e.vertex1 = v1;
						e.vertex2 = v2;
						e.block1_id = f.block.id;
						e.block1 = f.block;
						e.setLength();
						ve.put(v2,e);
					} else {
						e.block2_id = f.block.id;
						e.block2 = f.block;
					}
					f.block.edges.add(e);
					
				}
			}
		}
	}
	
	void collectVertexes() {
		vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
		int id = 0;
		for( Feature f : features) {
			for( int i = 0; i < f.geometry.coordinates.length; i++) {
				double[][] c = f.geometry.coordinates[i];
				for( int j = 0; j < c.length; j++) {
					HashMap<Double,Vertex> hm = vertexHash.get(c[j][0]);
					if( hm == null) {
						hm = new HashMap<Double,Vertex>();
						vertexHash.put(c[j][0], hm);
					}
					Vertex v = hm.get(c[j][1]);
					if( v == null) {
						v = new Vertex();
						v.x = c[j][0];
						v.y = c[j][1];
						v.id = id++;
						hm.put(c[j][1],v);
					}
				}
			}
		}
	}
}
