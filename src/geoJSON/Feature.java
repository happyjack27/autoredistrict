package geoJSON;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import mapCandidates.Ward;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class Feature extends ReflectionJSONObject<Feature> {
	public String type;
	public Properties properties;
	public Geometry geometry;
	public Ward ward = null;
	
	public static boolean showPrecinctLabels = false;
	public static int display_mode = 0;
	public static boolean draw_lines = true;
	
	public double calcArea() {
		double tot_area = 0;
		for( int i = 0; i < geometry.coordinates.length; i++) {
			double area = 0;
			double[][] coords = geometry.coordinates[i]; 
			int k = coords.length-1;
			for( int j = 0; j < coords.length; j++) {
				area += (coords[k][0]+coords[j][0])*(coords[k][1]-coords[j][1]);
				k = j;
			}
			tot_area += Math.abs(area)/2.0;
		}
		ward.area = tot_area;
		return tot_area;
	}
	
	public void toggleClicked() {
		try {

		if( ward.state == 0) {
			ward.state = 2;
			for( Ward b : ward.neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 0) {
					b.state = 1;
				}
			}
		} else if( ward.state == 2) { 
			ward.state = 0;
			for( Ward b : ward.neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 1) {
					b.state = 0;
				}
			}
		}
		} catch (Exception ex) {
			System.out.println(" ex "+ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void post_deserialize() {
		super.post_deserialize();
		if( containsKey("properties")) {
			properties = (Properties) getObject("properties");
		}
		if( containsKey("geometry")) {
			geometry = (Geometry) getObject("geometry");
		}
		/*
		if( properties.DISTRICT == null || properties.DISTRICT.toLowerCase().equals("null")) {
			geometry.outlineColor = Color.BLUE;
			geometry.isDistrict = false;
		}
		*/
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		if( key.equals("geometry")) {
			return new Geometry();
		}
		if( key.equals("properties")) {
			return new Properties();
		}
		return super.instantiateObject(key);
	}
	
	
	public void draw(Graphics g) {
		if( geometry.polygons == null) {
			geometry.makePolys();
		}
		if( geometry.fillColor != null || ward.state != 0 || display_mode != 0) {
			g.setColor(geometry.fillColor);
			if( display_mode == 1) {
				g.setColor(ward.demographics != null && ward.demographics.size() > 0 ? Color.white :  Color.black);
			} else if( display_mode == 2) {
				g.setColor(ward.has_census_results ? Color.white :  Color.black);
			} else if( display_mode == 3) {
				Color[] colors = new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};
				int max_col = -1;
				int max_num = 0;
				double tot = 0;
				double red = 0;
				double green = 0;
				double blue = 0;
				for( int i = 0; i < ward.demographics.size() && i < colors.length; i++) {
					int pop = ward.demographics.get(i).population;
					tot += pop;
					red += colors[i].getRed()*pop;
					green += colors[i].getGreen()*pop;
					blue += colors[i].getBlue()*pop;
				}
				red /= tot;
				green /= tot;
				blue /= tot;
				g.setColor(new Color((int)red,(int)green,(int)blue));
			} else if( display_mode == 4) {
				Color[] colors = new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};
				int max_col = -1;
				int max_num = 0;
				for( int i = 0; i < ward.demographics.size() && i < colors.length; i++) {
					int pop = ward.demographics.get(i).population;
					if( pop > max_num || max_col < 0) {
						max_num = pop;
						max_col = i;
					}
				}
				g.setColor(colors[max_col]);
			} else {
				if( ward.state == 1) {
					g.setColor(Color.blue);
				}
				if( ward.state == 2) {
					g.setColor(Color.white);
				}
			}
			for( int i = 0; i < geometry.polygons.length; i++) {
				g.fillPolygon(geometry.polygons[i]);
			}
		}
		if( geometry.outlineColor != null && draw_lines) {
			g.setColor(geometry.outlineColor);
			for( int i = 0; i < geometry.polygons.length; i++) {
				//Polygon p = new Polygon(xpolys[i],ypolys[i],xpolys[i].length);
				if( geometry.isDistrict && false) {
					g.fillPolygon(geometry.polygons[i]);
				} else {
					g.drawPolygon(geometry.polygons[i]);
				}
				
				double[] centroid = geometry.compute2DPolygonCentroid(geometry.polygons[i]);
				/*
				if( showPrecinctLabels) {
					FontMetrics fm = g.getFontMetrics();
					String name = this.properties.DISTRICT;
					centroid[0] -= fm.stringWidth(name)/2.0;
					centroid[1] += fm.getHeight()/2.0;
					g.drawString(name, (int)centroid[0],(int)centroid[1]);
				}
				*/
			}
		}
		/*
		if( showPrecinctLabels) {
			for( int i = 0; i < geometry.polygons.length; i++) {
				double[] centroid = geometry.compute2DPolygonCentroid(geometry.polygons[i]);
				FontMetrics fm = g.getFontMetrics();
				String name = this.properties.DISTRICT;
				centroid[0] -= fm.stringWidth(name)/2.0;
				centroid[1] += fm.getHeight()/2.0;
				g.drawString(name, (int)centroid[0],(int)centroid[1]);
			}
		}
		*/
	}
}
