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
			geometry.c = Color.BLUE;
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
		g.setColor(geometry.c);
		for( int i = 0; i < geometry.polygons.length; i++) {
			//Polygon p = new Polygon(xpolys[i],ypolys[i],xpolys[i].length);
			if( geometry.isDistrict && false) {
				g.fillPolygon(geometry.polygons[i]);
			} else {
				g.drawPolygon(geometry.polygons[i]);
			}
			
			double[] centroid = geometry.compute2DPolygonCentroid(geometry.polygons[i]);
			FontMetrics fm = g.getFontMetrics();
			String name = this.properties.DISTRICT;
			centroid[0] -= fm.stringWidth(name)/2.0;
			centroid[1] += fm.getHeight()/2.0;
			g.drawString(name, (int)centroid[0],(int)centroid[1]);
		}
	}
	
}
