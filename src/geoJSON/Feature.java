package geoJSON;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import mapCandidates.Block;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;

public class Feature extends ReflectionJSONObject<Feature> {
	public String type;
	public Properties properties;
	public Geometry geometry;
	public Block block = null;
	
	public static boolean showPrecinctLabels = false;
	public static int display_mode = 0;
	
	public void toggleClicked() {
		try {

		if( block.state == 0) {
			block.state = 2;
			for( Block b : block.neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 0) {
					b.state = 1;
				}
			}
		} else if( block.state == 2) { 
			block.state = 0;
			for( Block b : block.neighbors) {
				
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
		if( properties.DISTRICT == null || properties.DISTRICT.toLowerCase().equals("null")) {
			geometry.outlineColor = Color.BLUE;
			geometry.isDistrict = false;
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
		if( geometry.fillColor != null || block.state != 0 || display_mode != 0) {
			g.setColor(geometry.fillColor);
			if( display_mode == 1) {
				g.setColor(block.demographics != null && block.demographics.size() > 0 ? Color.white :  Color.black);
			} else if( display_mode == 2) {
				g.setColor(block.has_census_results ? Color.white :  Color.black);
			} else {
				if( block.state == 1) {
					g.setColor(Color.blue);
				}
				if( block.state == 2) {
					g.setColor(Color.white);
				}
			}
			for( int i = 0; i < geometry.polygons.length; i++) {
				g.fillPolygon(geometry.polygons[i]);
			}
		}
		if( geometry.outlineColor != null) {
			g.setColor(geometry.outlineColor);
			for( int i = 0; i < geometry.polygons.length; i++) {
				//Polygon p = new Polygon(xpolys[i],ypolys[i],xpolys[i].length);
				if( geometry.isDistrict && false) {
					g.fillPolygon(geometry.polygons[i]);
				} else {
					g.drawPolygon(geometry.polygons[i]);
				}
				
				double[] centroid = geometry.compute2DPolygonCentroid(geometry.polygons[i]);
				if( showPrecinctLabels) {
					FontMetrics fm = g.getFontMetrics();
					String name = this.properties.DISTRICT;
					centroid[0] -= fm.stringWidth(name)/2.0;
					centroid[1] += fm.getHeight()/2.0;
					g.drawString(name, (int)centroid[0],(int)centroid[1]);
				}
			}
		}
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
	}
}
