package geoJSON;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.*;

public class Geometry extends ReflectionJSONObject<Geometry> {
	public String type;
	public double[][][] coordinates;
	public int[] xpolys;
	public int[] ypolys;
	public Polygon[] polygons;
	public Color c = Color.BLACK;
	public boolean isDistrict = true;
	
	public static double shiftx,shifty,scalex,scaley;
	public void makePolys() {
		polygons = new Polygon[coordinates.length];
		for( int i = 0; i < coordinates.length; i++) {
			xpolys = new int[coordinates[i].length];
			ypolys = new int[coordinates[i].length];
			for( int j = 0; j < coordinates[i].length; j++) {
				xpolys[j] = (int)((coordinates[i][j][0]-shiftx)*scalex);
			}
			for( int j = 0; j < coordinates[i].length; j++) {
				ypolys[j] = (int)((coordinates[i][j][1]-shifty)*scaley);
			}
			polygons[i] = new Polygon(xpolys, ypolys, xpolys.length);
		}

	}
	public void draw(Graphics g) {
		if( polygons == null) {
			makePolys();
		}
		g.setColor(c);
		for( int i = 0; i < polygons.length; i++) {
			//Polygon p = new Polygon(xpolys[i],ypolys[i],xpolys[i].length);
			if( isDistrict) {
				g.fillPolygon(polygons[i]);
			} else {
				g.drawPolygon(polygons[i]);
			}
		}
	}

	@Override
	public void post_deserialize() {
		super.post_deserialize();
		
		if( containsKey("coordinates")) {
			Vector<Vector<Vector<Object>>> vvvo = (Vector<Vector<Vector<Object>>>)getVector("coordinates");
			coordinates = new double[vvvo.size()][][];
			int i2 = 0;
			for( int i = 0; i < vvvo.size(); i++) {
				try {
					Vector<Vector<Object>> vvo = vvvo.get(i);
					coordinates[i2] = new double[vvo.size()][];
					int k2;
					k2 = 0;
					for( int k = 0; k < vvo.size(); k++) {
						try {
							Vector<Object> vo = vvo.get(k);

							coordinates[i2][k2] = new double[]{
									Double.parseDouble((String)vo.get(0)),	
									Double.parseDouble((String)vo.get(1)),
							};
						k2++;
						} catch (Exception ex) { 
							//System.out.println("ex1 "+vvo.get(k));
						}
					}
					double[][] dd = new double[k2][];
					for( int k = 0; k < dd.length; k++) {
						dd[k] = coordinates[i2][k];
					}
					coordinates[i2] = dd;
					i2++;
				} catch (Exception ex) { 
					//System.out.println("ex "+vvvo.get(i));
					//ex.printStackTrace();
				}
			}
			double[][][] dd = new double[i2][][];
			for( int i = 0; i < dd.length; i++) {
				dd[i] = coordinates[i];
			}
			coordinates = dd;
			this.remove("coordinates");
			
		}
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre_serialize() {
		super.pre_serialize();
		if( coordinates != null) {
			Vector<Vector<String>> v = new Vector<Vector<String>>();

			Vector v3 = new Vector();
			for( int j = 0; j < coordinates.length; j++) {
				for( int i = 0; i < coordinates.length; i++) {
					Vector v2 = new Vector();
					v2.add(""+coordinates[j][i][0]);
					v2.add(""+coordinates[j][i][1]);
					v.add(v2);
				}
				v3.add(v);
			}
			put("coordinates",v3);
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject instantiateObject(String key) {
		// TODO Auto-generated method stub
		return super.instantiateObject(key);
	}
}
