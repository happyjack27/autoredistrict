package geography;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;
import solutions.Settings;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.*;

public class Geometry extends ReflectionJSONObject<Geometry> {
	public String type;
	public double[][][] coordinates;
	public Polygon[] polygons;
	public Polygon[] polygons_full;
	public Color outlineColor = Color.BLACK;
	public Color fillColor = FeatureCollection.DEFAULT_COLOR;
	public boolean isDistrict = true;
	public double[] full_centroid;
	public double color_multiplier = 1;

	
	public static boolean isLatLon = true; 
	public static double SCALELATLON = 1000000;
	public static double RADIUS_OF_EARTH = 6372.8; //in km


	
	public static double shiftx=0,shifty=0,scalex=1,scaley=1;
	
	public static double min_squared_distance = 1;
	public static double min_squared_distance_unsimplified = 0.1;
	public static int min_point_frac = 16;
	
	public static Polygon[] makePolys(double[][][] coordinates) {
		if( scalex == 0 || scalex != scalex) scalex = 1;
		if( scaley == 0 || scaley != scaley) scaley = 1;
		if( shiftx != shiftx) shiftx = 0;
		if( shifty != shifty) shifty = 0;
		
		//System.out.println("makepolys s "+shiftx+" "+shiftx);
		//System.out.println("makepolys r "+scalex+" "+scalex);
		
		int[] xpolys;
		int[] ypolys;
		
		Polygon[] polygons = new Polygon[coordinates.length];
		for( int i = 0; i < coordinates.length; i++) {
			if(Settings.b_make_simplified_polys || true ) {
				int point_count = coordinates[i].length/( min_point_frac );
				if( point_count < 1) {
					point_count = 1;
				}
				int last_i = 0;
				double first_x = ((coordinates[i][0][0]-shiftx)*scalex);
				double first_y = ((coordinates[i][0][1]-shifty)*scaley);
				double last_x = first_x;
				double last_y = first_y;
				Vector<double[]> points = new Vector<double[]>();
				points.add(new double[]{last_x,last_y});
				
				for( int j = 1; j < coordinates[i].length; j++) {
					double xtest = ((coordinates[i][j][0]-shiftx)*scalex);
					double ytest = ((coordinates[i][j][1]-shifty)*scaley);
					double xdelta = xtest-last_x;
					double ydelta = ytest-last_y;
					if( xdelta*xdelta+ydelta*ydelta >= (Settings.b_make_simplified_polys ? min_squared_distance : min_squared_distance_unsimplified) || i-last_i >= point_count) {
						last_x = xtest;
						last_y = ytest;
						points.add(new double[]{last_x,last_y});
					}
				}
				xpolys = new int[points.size()];
				ypolys = new int[points.size()];
				for( int j = 0; j < points.size(); j++) {
					double[] p  = points.get(j);
					xpolys[j] = (int)p[0];
					ypolys[j] = (int)p[1];
				}
			} else {
				xpolys = new int[coordinates[i].length];
				ypolys = new int[coordinates[i].length];
				for( int j = 0; j < coordinates[i].length; j++) {
					xpolys[j] = (int)((coordinates[i][j][0]-shiftx)*scalex);
				}
				for( int j = 0; j < coordinates[i].length; j++) {
					ypolys[j] = (int)((coordinates[i][j][1]-shifty)*scaley);
				}
			}
			polygons[i] = new Polygon(xpolys, ypolys, xpolys.length);
		}
		return polygons;
	}
	
	public void makePolys() {
		polygons = makePolys(coordinates);
	}
	public double[] getAvg() {
		double count = 0;
		double[] dd = new double[]{0,0};
		int[] xpolys;
		int[] ypolys;

		for( int i = 0; i < coordinates.length; i++) {
			xpolys = new int[coordinates[i].length];
			ypolys = new int[coordinates[i].length];
			for( int j = 0; j < coordinates[i].length; j++) {
				dd[0] += (coordinates[i][j][0]);
			}
			for( int j = 0; j < coordinates[i].length; j++) {
				dd[1] += (coordinates[i][j][1]);
			}
			count += coordinates[i].length;
		}
		return new double[]{dd[0]/count,dd[1]/count};
		
	}
	public void makePolysFull() {
		int[] xpolys;
		int[] ypolys;

		polygons_full = new Polygon[coordinates.length];
		for( int i = 0; i < coordinates.length; i++) {
			xpolys = new int[coordinates[i].length];
			ypolys = new int[coordinates[i].length];
			for( int j = 0; j < coordinates[i].length; j++) {
				xpolys[j] = (int)((coordinates[i][j][0])*SCALELATLON);
			}
			for( int j = 0; j < coordinates[i].length; j++) {
				ypolys[j] = (int)((coordinates[i][j][1])*SCALELATLON);
			}
			polygons_full[i] = new Polygon(xpolys, ypolys, xpolys.length);
		}
		double[] dd = new double[]{0,0};
		double div = 1.0/((double)polygons_full.length);
		for( int  i= 0; i < polygons_full.length; i++) {
			double[] d = compute2DPolygonCentroid(polygons_full[i]);
			dd[0] += d[0]*div;
			dd[1] += d[1]*div;
		}
		full_centroid = dd;//compute2DPolygonCentroid(polygons_full[0]);

	}
	public Geometry() {
		super();
		//int r = (int)Math.floor(Math.random()*256.0);
		//int g = (int)Math.floor(Math.random()*256.0);
		//int b = (int)Math.floor(Math.random()*256.0);
		//outlineColor = new Color(r,g,b);
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
	public double[] compute2DPolygonCentroid(Polygon p) {
		return compute2DPolygonCentroid(p.xpoints,p.ypoints);
	
	}
	public double[] compute2DPolygonCentroid(int[] xs, int[] ys) {
			    double signedArea = 0.0;
			    double x0 = 0.0; // Current vertex X
			    double y0 = 0.0; // Current vertex Y
			    double x1 = 0.0; // Next vertex X
			    double y1 = 0.0; // Next vertex Y
			    double a = 0.0;  // Partial signed area

			    double retx = 0;
			    double rety = 0;
			    for(int i=0; i < xs.length; i++) {
			        x0 = xs[i];
			        y0 = ys[i];
			        x1 = xs[i+1 == xs.length ? 0 : i+1];
			        y1 = ys[i+1 == xs.length ? 0 : i+1];
			        a = x0*y1 - x1*y0;
			        signedArea += a;
			        retx += (x0 + x1)*a;
			        rety += (y0 + y1)*a;
			    }

			    signedArea *= 0.5;
			    retx /= (6.0*signedArea);
			    rety /= (6.0*signedArea);
			    if( retx != retx || rety != rety) {
			    	retx = 0;
			    	rety = 0;
				    for(int i=0; i < xs.length; i++) {
				    	retx += xs[i];
				    	rety += ys[i];
				    }
				    retx /= (double) xs.length;
				    rety /= (double) xs.length;
			    }

			    return new double[]{retx,rety};
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
