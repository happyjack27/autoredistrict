package geography;

import javax.swing.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;

import com.hexiong.jdbf.DBFReader;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;
import solutions.District;
import solutions.DistrictMap;
import solutions.Ecology;
import solutions.Edge;
import solutions.Settings;
import solutions.Vertex;
import solutions.Ward;
import ui.MainFrame;
import ui.MapPanel;

public class FeatureCollection extends ReflectionJSONObject<FeatureCollection> {
	public String type;
	public Vector<Feature> features = new Vector<Feature>();
	public Vector<Ward> precincts;
	public HashMap<String,Ward> wardHash;
	public Ecology ecology = new Ecology();
	double snap_to_grid_resolution = 10000.0*10.0;
	public static double xy = 1;
	public static double dlonlat = 1;
	
	public static boolean[] locked_wards = null;
	public static int shown_map = 0;
	
	static int max_hues = 9;
	
	HashMap<Double,HashMap<Double,Vertex>> vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
	HashMap<Integer,HashMap<Integer,Edge>> edgeHash = new HashMap<Integer,HashMap<Integer,Edge>>();	
	
	Vector<String> locked_counties = new Vector<String>();
	
	public String[] getHeaders() {
		String[] headers;
		if( features == null || features.size() < 1) {
			return null;
		}
		Set<String> keyset = features.get(0).properties.keySet(); 
		headers = new String[keyset.size()];
		
		Vector<String> v_headers = new Vector<String>();
		for( String s : keyset) {
			v_headers.add(s);
		}
		
		Collections.sort(v_headers);
		
		for( int i = 0; i < v_headers.size(); i++) {
			headers[i] = v_headers.get(i);
		}
		return headers;
	}
	
	public String[][] getData(String[] headers) {
		String[][] data = new String[features.size()][headers.length];
		for( int j = 0; j < features.size(); j++) {
			Feature f = features.get(j);
			for( int k = 0; k < headers.length; k++) {
				try {
					data[j][k] = f.properties.get(headers[k]).toString();
				} catch (Exception ex) {
					data[j][k] = "<null>";
				}
			}
		}
		return data;
	}
	public static void recalcDlonlat() {
		//lock aspect ratio
		double lat0 = MainFrame.mainframe.miny;//features.get(0).geometry.coordinates[0][0][1];
		double lat1 = MainFrame.mainframe.maxy;//features.get(0).geometry.coordinates[0][0][1];
		System.out.println("lat0"+lat0);
		System.out.println("lat1"+lat1);
		
		dlonlat = Math.cos((lat0+lat1)/2);
	}
		
	public void draw(Graphics g) {
		if( features == null) {
			return;
		}
		
		//lock aspect ratio
		double lon0 = MapPanel.minx;//Geometry.features.get(0).geometry.coordinates[0][0][0];
		double lat0 = MapPanel.miny;//features.get(0).geometry.coordinates[0][0][1];
		double lon1 = MapPanel.maxx;//Geometry.features.get(0).geometry.coordinates[0][0][0];
		double lat1 = MapPanel.maxy;//features.get(0).geometry.coordinates[0][0][1];
		
		dlonlat = Math.cos((lat0+lat1)/2);
		double x0 = lon0 * dlonlat;
		double y0 = lat0;
		double x1 = lon1 * dlonlat;
		double y1 = lat1;
		xy = Math.abs((x1-x0)/(y1-y0));
		//System.out.println(lon0+" "+lat1+" "+x0+" "+y1+" "+xy);
		
		if( MapPanel.zoomStack.empty()) {
			//System.out.println(Geometry.scalex +" "+Geometry.scaley );
			double sign = Geometry.scaley*Geometry.scalex > 0 ? 1 : -1;
			if( Math.abs(xy*Geometry.scaley) > Math.abs(Geometry.scalex)) {
				Geometry.scaley = sign * Geometry.scalex/xy;
			} else if( Math.abs(xy*Geometry.scaley) < Math.abs(Geometry.scalex)) {
				Geometry.scalex = sign * xy*Geometry.scaley;
			}
		}
	
		//System.out.println(Geometry.scalex +" "+Geometry.scaley );
		

		double[] dxs = new double[Settings.num_districts];
		double[] dys = new double[Settings.num_districts];
		double[] dcs = new double[Settings.num_districts];
		for( int i = 0; i < Settings.num_districts; i++) {
			dxs[i] = 0;
			dys[i] = 0;
			dcs[i] = 0;
		}
		if( ecology.population != null && ecology.population.size() > shown_map) {
			DistrictMap dm  = ecology.population.get(shown_map);
			//System.out.println("snd:"+Settings.num_districts+" dmbd:"+dm.ward_districts.length);
			if( dm.vtd_districts != null) {
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
					Feature f = features.get(i);
					Ward b = f.ward;
					Geometry geo = features.get(i).geometry;
					int di = dm.vtd_districts[b.id];
					if( di >= Settings.num_districts) {
						System.out.println("district out of range! "+di);
						continue;
					}
					try {
				       	f.geometry.makePolys();
						double area = features.get(i).ward.area;
				       	for( int p = 0; p < f.geometry.polygons.length; p++) {
					       	double[] centroid = f.geometry.compute2DPolygonCentroid(f.geometry.polygons[p]);
							dxs[di] += centroid[0]*area;
							dys[di] += centroid[1]*area;
							dcs[di] += area;
						}
					} catch (Exception ex) {
						System.out.println("ex d "+ex);
						ex.printStackTrace();
					}
					try {
						int color = di;
						if( color < c.length) {
							geo.fillColor = c[color];
						}
					} catch (Exception ex) {
						System.out.println("ex e "+ex);
						ex.printStackTrace();
					}
					
				}
			}
		}

		if( Feature.display_mode == Feature.DISPLAY_MODE_DIST_POP) {
			if( shown_map < ecology.population.size()) {
				DistrictMap dm  = ecology.population.get(shown_map);
				int total = 0;
				for( int i = 0; i < Settings.num_districts; i++) {
					total += dm.districts.get(i).getPopulation();
				}
				total /= Settings.num_districts;
				
				int max = -1000000000; 
				int min = +1000000000; 
				for( int i = 0; i < Settings.num_districts; i++) {
					if( dm.districts.get(i).getPopulation()-total > max) {
						max = (int)dm.districts.get(i).getPopulation()-total;
					}
					if( dm.districts.get(i).getPopulation()-total < min) {
						min = (int)dm.districts.get(i).getPopulation()-total;
					}
				}
				Color[] district_colors = new Color[Settings.num_districts];
				for( int i = 0; i < Settings.num_districts; i++) {
					double amt = dm.districts.get(i).getPopulation()-total;
					if( amt < 0) {
						amt /= min;
						int shade = (int)(255.0*amt); 
						district_colors[i] = new Color(255,255-shade,255-shade);
					} else if (amt > 0) {
						amt /= max;
						int shade = (int)(255.0*amt); 
						district_colors[i] = new Color(255-shade,255,255-shade);
					} else {
						district_colors[i] = Color.WHITE;
					}
				}
				for( int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					Ward b = f.ward;
					Geometry geo = features.get(i).geometry;
					int di = dm.vtd_districts[b.id];
					geo.fillColor = district_colors[di];
				}
			}
		}
		double mavg_min = 0; 
		double mavg_max = 0; 
		if( Feature.display_mode == Feature.DISPLAY_MODE_COMPACTNESS) {
			if( shown_map < ecology.population.size()) {
				DistrictMap dm  = ecology.population.get(shown_map);
				dm.getWeightedEdgeLength();
				
				double max = -1000000000; 
				double min = +1000000000; 
				for( int i = 0; i < Settings.num_districts; i++) {
					if( Math.sqrt(dm.districts.get(i).iso_quotent) > max) {
						max = Math.sqrt(dm.districts.get(i).iso_quotent);
					}
					if( Math.sqrt(dm.districts.get(i).iso_quotent) < min) {
						min = Math.sqrt(dm.districts.get(i).iso_quotent);
					}
				}
				mavg_min += (min-mavg_min)/(mavg_min == 0 ? 1 : 20); 
				mavg_max += (max-mavg_max)/(mavg_max == 0 ? 1 : 20); 
				if( mavg_min < min) min = mavg_min;
				if( mavg_max > max) max = mavg_max;
				
				Color[] district_colors = new Color[Settings.num_districts];
				for( int i = 0; i < Settings.num_districts; i++) {
					double amt = 1.0-Math.abs((Math.sqrt(dm.districts.get(i).iso_quotent)-min)/(max-min));
					amt = amt*2.0-1.0;
					if( amt < 0) {
						amt *= -1;
						int shade = (int)(255.0*amt); 
						district_colors[i] = new Color(255-shade,255,255-shade);
					} else if (amt > 0) {
						int shade = (int)(255.0*amt); 
						district_colors[i] = new Color(255,255-shade,255-shade);
					} else {
						district_colors[i] = Color.WHITE;
					}
					/*
					
					int shade = (int)(255.0*amt); 
					if( shade < 0) {
						shade = 0;
					}
					if( shade > 255) {
						shade = 255;
					}
					district_colors[i] = new Color(255,255-shade,255-shade);
					*/
				}
				for( int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					Ward b = f.ward;
					Geometry geo = features.get(i).geometry;
					int di = dm.vtd_districts[b.id];
					geo.fillColor = district_colors[di];
				}
			}
		}
		if( Feature.display_mode == Feature.DISPLAY_MODE_DIST_DEMO) {
			if( shown_map < ecology.population.size()) {
				DistrictMap dm  = ecology.population.get(shown_map);
				int total = 0;
				for( int i = 0; i < Settings.num_districts; i++) {
					total += dm.districts.get(i).getPopulation();
				}
				total /= Settings.num_districts;
				
				Color[] district_colors = new Color[Settings.num_districts];
				Color[] colors = new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};
				//double[] district_colors = new Color[Settings.num_districts];
				double[] tot = new double[Settings.num_districts];
				double[] red = new double[Settings.num_districts];
				double[] green = new double[Settings.num_districts];
				double[] blue = new double[Settings.num_districts];
				
				for( int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					Ward b = f.ward;
					Geometry geo = features.get(i).geometry;
					int di = dm.vtd_districts[b.id];
					
					for( int j = 0; j < b.demographics.size() && j < colors.length; j++) {
						int pop = b.demographics.get(j).population;
						tot[di] += pop;
						red[di] += colors[j].getRed()*pop;
						green[di] += colors[j].getGreen()*pop;
						blue[di] += colors[j].getBlue()*pop;
					}
				}
				for( int i = 0; i < Settings.num_districts; i++) {
					district_colors[i] = new Color(
							(int)(red[i] / tot[i]),
							(int)(green[i] / tot[i]),
							(int)(blue[i] / tot[i])
					);
				}			
				for( int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					Ward b = f.ward;
					Geometry geo = features.get(i).geometry;
					int di = dm.vtd_districts[b.id];
					geo.fillColor = district_colors[di];
				}
			}
		}
        for( Feature f : features) {
        	f.geometry.makePolys();
        	f.draw(g);
        }
		if( ecology.population != null && ecology.population.size() > 0) {
	        if( Feature.showDistrictLabels) {
	        	DecimalFormat sdm = new DecimalFormat("###,###,##0.00000");  
	        	DecimalFormat sdm0 = new DecimalFormat("###,###,##0");  
				int total = 0;
				DistrictMap dm = null;
				if( shown_map < ecology.population.size()) {
					dm = ecology.population.get(shown_map);
					for( int i = 0; i < Settings.num_districts; i++) {
						total += dm.districts.get(i).getPopulation();
					}
					total /= Settings.num_districts;
					dm.getWeightedEdgeLength();
				}	        	
				try {
					g.setColor(Color.BLACK);
					g.setFont(new Font("Arial",Font.BOLD,12*MapPanel.FSAA));
					for( int i = 0; i < Settings.num_districts; i++) {
						District d = dm == null ? null : dm.districts.get(i);
						//dm.districts.get(i).getPopulation();
						dxs[i] /= dcs[i];
						dys[i] /= dcs[i];
						FontMetrics fm = g.getFontMetrics();
						String name =""+i;
						//dys[1] += fm.getHeight()/2.0;
	
						g.setFont(new Font("Arial",Font.BOLD,12*MapPanel.FSAA));
						g.drawString(name, (int)dxs[i] - (int)(fm.stringWidth(name)/2.0), (int)dys[i]);
						int amt = dm == null ? 0 : (int) (d.getPopulation() - total);
						g.setFont(new Font("Arial",Font.PLAIN,10*MapPanel.FSAA));
						if( amt > 0) {
							g.drawString("+"+amt, (int)dxs[i] - (int)(fm.stringWidth("+"+amt)/2.0), (int)dys[i]+fm.getHeight());
						} else {
							g.drawString(""+amt, (int)dxs[i] - (int)(fm.stringWidth(""+amt)/2.0), (int)dys[i]+fm.getHeight());
						}
						
						double iso = dm == null ? 0 : (d.iso_quotent);
						String siso = sdm.format(iso);
	
						g.drawString(siso, (int)dxs[i] - (int)(fm.stringWidth(siso)/2.0), (int)dys[i]+fm.getHeight()*2);

						double len = dm == null ? 0 : (d.edge_length);
						String slen = sdm.format(len);
						
						double area = dm == null ? 0 : (d.area);
						String sarea = sdm.format(area);
						//g.drawString(slen, (int)dxs[i] - (int)(fm.stringWidth(slen)/2.0), (int)dys[i]+fm.getHeight()*3);
						//g.drawString(sarea, (int)dxs[i] - (int)(fm.stringWidth(sarea)/2.0), (int)dys[i]+fm.getHeight()*4);
						
						//System.out.println("name "+name+" x "+(int)dxs[i]+" y "+(int)dys[i]+" size "+dcs[i]);
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
	
	        }
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
	
	public void initwards() {
		precincts = new Vector<Ward>();
		wardHash = new HashMap<String,Ward>();
		Ward.id_enumerator = 0;
		for( Feature f : features) {
			f.ward = new Ward();
			//f.ward.name = f.properties.DISTRICT;
			if( f.properties.POPULATION > 0) {
				f.ward.population = f.properties.POPULATION;
				f.ward.has_census_results = true;
			}
			precincts.add(f.ward);
			//precinctHash.put(f.ward.name,f.ward);
		}
		collectVertexes();
		collectEdges();
		for( Feature f : features) {
			f.ward.collectNeighbors();
		}
		for( Feature f : features) {
			f.ward.syncNeighbors();
		}
		for( Feature f : features) {
			f.ward.collectNeighborLengths();
		}
		for( Feature f : features) {
			f.calcArea();
		}
		
		Geometry.shiftx = Geometry.shifty = 0;
		Geometry.scalex = Geometry.scaley = 1;
		
		//now if has no neighbors, add nearest.
		for( Feature f : features) {
			if( f.ward.neighbors.size() == 0) {
				if( f.geometry == null || f.geometry.polygons == null) {
					f.geometry.makePolys();
				}
				if( f.geometry == null || f.geometry.polygons == null) {
					continue;
				}

				Feature nearest = getNearestFeature(f);

				//new way, just add the unpaired edge length of the no-neighbors feature.
				double[] new_neighbbor_lengths = new double[nearest.ward.neighbor_lengths.length+1];
				for( int i = 0; i < nearest.ward.neighbor_lengths.length; i++) {
					new_neighbbor_lengths[i] = nearest.ward.neighbor_lengths[i]; 
				}
				new_neighbbor_lengths[new_neighbbor_lengths.length-1] = f.ward.unpaired_edge_length;
				
				nearest.ward.neighbors.add(f.ward);
				nearest.ward.neighbor_lengths = new_neighbbor_lengths;
				
				f.ward.neighbors.add(nearest.ward);
				f.ward.neighbor_lengths = new double[]{f.ward.unpaired_edge_length};
				f.ward.unpaired_edge_length = 0;

				/*
				 //old way - grab a fraction of the new neighbors edges.

				Feature nearest = getNearestFeature(f);
				double total_length = 0;
				double[] new_neighbbor_lengths = new double[nearest.ward.neighbor_lengths.length+1];
				for( int i = 0; i < nearest.ward.neighbor_lengths.length; i++) {
					total_length += nearest.ward.neighbor_lengths[i];
					new_neighbbor_lengths[i] = nearest.ward.neighbor_lengths[i]; 
				}
				total_length /= (double)nearest.ward.neighbor_lengths.length;
				new_neighbbor_lengths[new_neighbbor_lengths.length-1] = total_length;
				
				nearest.ward.neighbors.add(f.ward);
				nearest.ward.neighbor_lengths = new_neighbbor_lengths;
				
				f.ward.neighbors.add(nearest.ward);
				f.ward.neighbor_lengths = new double[]{total_length};
				*/
			}
		}
		
		//initialize locked_wards array.
		locked_wards = new boolean[precincts.size()];
		for( int i = 0; i < locked_wards.length; i++) {
			locked_wards[i] = false;
		}
		
		/*
		for( Feature f : features) {
			f.ward.edges = new Vector<Edge>();
		}
		*/
		//vertexHash = new HashMap<Double,HashMap<Double,Vertex>>();
		//edgeHash = new HashMap<Vertex,HashMap<Vertex,Edge>>();
	}
	public static double[] getAvgCentroid(Feature f) {
		double[] source = new double[]{0,0};
		if( f.geometry == null || f.geometry.polygons == null) {
			f.geometry.makePolys();
		}
		if( f.geometry == null || f.geometry.polygons == null) {
			return new double[]{0,0};
		}
		for( int i = 0; i < f.geometry.coordinates.length; i++) {
			int[] xpolys = new int[f.geometry.coordinates[i].length];
			int[] ypolys = new int[f.geometry.coordinates[i].length];
			for( int j = 0; j < f.geometry.coordinates[i].length; j++) {
				xpolys[j] = (int)f.geometry.coordinates[i][j][0];
			}
			for( int j = 0; j <f.geometry. coordinates[i].length; j++) {
				ypolys[j] = (int)f.geometry.coordinates[i][j][1];
			}
			
			double[] dd = f.geometry.compute2DPolygonCentroid(xpolys,ypolys);//.polygons[i]);
			//double[] dd = f.geometry.compute2DPolygonCentroid(f.geometry.coordinates[i]);//.polygons[i]);
			//coordinates[i]
			source[0] += dd[0];
			source[1] += dd[1];
		}
		source[0] /= (double)f.geometry.coordinates.length;//f.geometry.polygons.length;
		source[1] /= (double)f.geometry.coordinates.length;//f.geometry.polygons.length;
		return source;
	}
	public Feature getNearestFeature(Feature f) {
		double[] target = getAvgCentroid(f);
		Feature best = null;
		double best_r = 0;
		for( int i = 0; i < features.size(); i++) {
			Feature test = features.get(i);
			if( test == f || test.ward.neighbors.size() == 0) {
				continue;
			}
			if( test.geometry == null || test.geometry.polygons == null) {
				test.geometry.makePolys();
			}
			if( test.geometry == null || test.geometry.polygons == null) {
				continue;
			}
			double[] cur = getAvgCentroid(test);
			double dx = cur[0]-target[0];
			double dy = cur[1]-target[1];
			double r = dx*dx+dy*dy;
			if( best == null || r < best_r) {
				best_r = r;
				best = test;
			}
		}
		return best;
	}
	
	public void initEcology() {
		ecology.wards = precincts;
		locked_wards = new boolean[precincts.size()];
		for( int i = 0; i < locked_wards.length; i++) {
			locked_wards[i] = false;
		}

		//ecology.edges = edgeHash.values();
		//ecology.vertexes = vertexes;
	}
	public void storeDistrictsToProperties(String column_name) {
		if( ecology.population == null) {
			ecology.population = new Vector<DistrictMap>();
		}
		if( ecology.population.size() < 1) {
			ecology.population.add(new DistrictMap(precincts,Settings.num_districts));
		}
		ecology.population.get(0).storeDistrictsToProperties(this, column_name);
	}

	public void loadDistrictsFromProperties(String column_name) {
		
		if( ecology.population == null) {
			ecology.population = new Vector<DistrictMap>();
		}
		if( ecology.population.size() < 1) {
			ecology.population.add(new DistrictMap(precincts,Settings.num_districts));
		}
		while( ecology.population.size() < Settings.population) {
			ecology.population.add(new DistrictMap(precincts,Settings.num_districts));
		}
		for( DistrictMap dm : ecology.population) {
			dm.loadDistrictsFromProperties(this, column_name);
			//dm.setGenome(new_ward_districts);
			//dm.fillDistrictwards();
		
		}
		
	}
	
	public void recalcEdgeLengths() {
		for( Feature f : features) {
			f.ward = new Ward();
			for( Edge e : f.ward.edges) {
				e.setLength();
			}
		}
		for( Feature f : features) {
			f.ward.collectNeighborLengths();
		}

	}
	void collectEdges() {
		edgeHash = new HashMap<Integer,HashMap<Integer,Edge>>();
		for( Feature f : features) {
			f.ward.edges = new Vector<Edge>();
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
						e.ward1_id = f.ward.id;
						e.ward1 = f.ward;
						e.setLength();
						ve.put(v2.id,e);
						f.ward.edges.add(e);
					} else {
						if( e.ward1 != f.ward) {
							e.ward2_id = f.ward.id;
							e.ward2 = f.ward;
							f.ward.edges.add(e);
						}
					}
					
				}
			}
		}
		int paired_edges = 0;
		int unpaired_edges = 0;
		for( HashMap<Integer,Edge> ev : edgeHash.values()) {
			for( Edge e : ev.values()) {
				if( e.ward2 == null) {
					unpaired_edges++;
				} else {
					paired_edges++;
				}
			}
		}
		System.out.println("unpaired edges: "+unpaired_edges);
		System.out.println("paired edges: "+paired_edges);
		double pct = 100.0*((double)paired_edges)/((double)(paired_edges+unpaired_edges));
		System.out.println("percent: "+new DecimalFormat("##0.000").format(pct)+"%");
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
