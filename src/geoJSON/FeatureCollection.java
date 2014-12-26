package geoJSON;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import mapCandidates.Block;
import mapCandidates.DistrictMap;
import mapCandidates.Ecology;
import mapCandidates.Edge;
import mapCandidates.Settings;
import mapCandidates.Vertex;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class FeatureCollection extends ReflectionJSONObject<FeatureCollection> {
	public String type;
	public Vector<Feature> features;
	public Vector<Block> blocks;
	public HashMap<String,Block> precinctHash;
	public Ecology ecology = new Ecology();
	double snap_to_grid_resolution = 1000000.0;
	
	static int max_hues = 9;
	
	HashMap<Double,HashMap<Double,Vertex>> vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
	HashMap<Integer,HashMap<Integer,Edge>> edgeHash = new HashMap<Integer,HashMap<Integer,Edge>>();
	
	public void draw(Graphics g) {
		if( features == null) {
			return;
		}
		if( ecology.population != null && ecology.population.size() > 0) {
			DistrictMap dm  = ecology.population.get(0);
			//System.out.println("snd:"+Settings.num_districts+" dmbd:"+dm.block_districts.length);
			if( dm.block_districts != null) {
				Color[] c = new Color[Settings.num_districts];
				int saturations = (int) Math.ceil((double)Settings.num_districts / (double)(max_hues*4));
				int values = (int) Math.ceil((double)Settings.num_districts / (double)max_hues);
				int hues = (int) Math.ceil((double)Settings.num_districts / ((double)saturations*(double)values));
				if( hues > max_hues) hues = max_hues;
				if( values > 4) hues = 4;
				
				float hue_inc = (float)(1.0/(double)hues);
				float hue_start = 0;
				float val_inc = (float)(1.0/(double)values)/2;
				float val_start = 0.5f;
				float sat_inc = (float)(1.0/(double)saturations)/2;
				float sat_start = 1f;
				float hue = hue_start;
				float val = val_start;
				float sat = sat_start;
				for( int i = 0; i < c.length; i++) {
					c[i] = Color.getHSBColor(hue, (float)sat, (float)val);
					hue += hue_inc;
					if( hue >= 1.0) {
						hue = hue_start;
						val += val_inc;
						if( val >= 1.0) {
							val = val_start;
							sat -= sat_inc;
						}
					}
				}
				for( int i = 0; i < features.size(); i++) {
					Block b = features.get(i).block;
					Geometry geo = features.get(i).geometry;
					try {
						int color = dm.block_districts[b.id];
						if( color < c.length) {
							geo.fillColor = c[color];
						}
					} catch (Exception ex) {}
					
				}
			}
		}
        for( Feature f : features) {
        	f.geometry.makePolys();
        	f.draw(g);
        }		
	}

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
			if( f.properties.POPULATION > 0) {
				f.block.population = f.properties.POPULATION;
				f.block.has_census_results = true;
			}
			blocks.add(f.block);
			precinctHash.put(f.block.name,f.block);
		}
		collectVertexes();
		collectEdges();
		for( Feature f : features) {
			f.block.collectNeighbors();
		}
		for( Feature f : features) {
			f.block.syncNeighbors();
		}
		for( Feature f : features) {
			f.block.collectNeighborLengths();
		}
		
		/*
		for( Feature f : features) {
			f.block.edges = new Vector<Edge>();
		}
		*/
		//vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
		//edgeHash = new HashMap<Vertex,HashMap<Vertex,Edge>>();
	}
	
	public void initEcology() {
		ecology.blocks = blocks;
		//ecology.edges = edgeHash.values();
		//ecology.vertexes = vertexes;
	}
	
	public void recalcEdgeLengths() {
		for( Feature f : features) {
			f.block = new Block();
			for( Edge e : f.block.edges) {
				e.setLength();
			}
		}
		for( Feature f : features) {
			f.block.collectNeighborLengths();
		}

	}
	void collectEdges() {
		edgeHash = new HashMap<Integer,HashMap<Integer,Edge>>();
		for( Feature f : features) {
			f.block.edges = new Vector<Edge>();
			for( int i = 0; i < f.geometry.coordinates.length; i++) {
				double[][] c = f.geometry.coordinates[i];
				for( int j = 0; j < c.length; j++) {
					Vertex v1 = vertexHash.get(c[j][0]).get(c[j][1]);
					//System.out.println("v1 "+v1.id);
					Vertex v2 = vertexHash.get(c[j+1 == c.length ? 0 : j+1][0]).get(c[j+1 == c.length ? 0 : j+1][1]);
					//System.out.println("v2 "+v2.id);
					if( v1.id > v2.id) {
						Vertex t = v1;
						v1 = v2;
						v2 = t;
					}
					HashMap<Integer,Edge> ve = edgeHash.get(v1.id);
					//System.out.println("ve "+ve);
					if( ve == null) {
						ve = new HashMap<Integer,Edge>();
						edgeHash.put(v1.id, ve);
					}
					Edge e = ve.get(v2.id);
					if( e == null) {
						e = new Edge();
						e.vertex1_id = v1.id;
						e.vertex2_id = v2.id;
						e.vertex1 = v1;
						e.vertex2 = v2;
						e.block1_id = f.block.id;
						e.block1 = f.block;
						e.setLength();
						ve.put(v2.id,e);
						f.block.edges.add(e);
					} else {
						if( e.block1 != f.block) {
							e.block2_id = f.block.id;
							e.block2 = f.block;
							f.block.edges.add(e);
						}
					}
					
				}
			}
		}
		int paired_edges = 0;
		int unpaired_edges = 0;
		for( HashMap<Integer,Edge> ev : edgeHash.values()) {
			for( Edge e : ev.values()) {
				if( e.block2 == null) {
					unpaired_edges++;
				} else {
					paired_edges++;
				}
			}
		}
		System.out.println("unpaired edges: "+unpaired_edges);
		System.out.println("paired edges: "+paired_edges);
	}
	
	void collectVertexes() {
		double minx = 0;
		double maxx = 0;
		double miny = 0;
		double maxy = 0;
		boolean point_added = false;
		for( Feature f : features) {
			for( int i = 0; i < f.geometry.coordinates.length; i++) {
				double[][] c = f.geometry.coordinates[i];
				for( int j = 0; j < c.length; j++) {
					if( !point_added) {
						minx = maxx = c[0][0];
						miny = maxy = c[0][1];
						point_added = true;
					} else {
						 minx = minx > c[0][0] ? c[0][0] : minx;
						 maxx = maxx < c[0][0] ? c[0][0] : maxx;
						 miny = miny > c[0][1] ? c[0][1] : miny;
						 maxy = maxy < c[0][1] ? c[0][1] : maxy;
					}
				}
			}
		}
		double area = (maxx-minx)*(maxy-miny);
		double increment = Math.sqrt(area)/snap_to_grid_resolution; 
		double r_increment = 1.0/increment; 
		
		vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
		int id = 0;
		for( Feature f : features) {
			for( int i = 0; i < f.geometry.coordinates.length; i++) {
				double[][] c = f.geometry.coordinates[i];
				for( int j = 0; j < c.length; j++) {
					c[j][0] = Math.round(c[j][0]*r_increment)*increment;
					c[j][1] = Math.round(c[j][1]*r_increment)*increment;
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
		int vertex_x = 0;
		int vertex_all = 0;
		for( HashMap<Double,Vertex> ev : vertexHash.values()) {
			vertex_x++;
			for( Vertex e : ev.values()) {
				vertex_all++;
			}
		}
		System.out.println("vertex_xs: "+vertex_x);
		System.out.println("vertex_all: "+vertex_all);
	}
}
