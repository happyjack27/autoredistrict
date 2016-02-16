package geography;

import java.util.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

import com.hexiong.jdbf.JDBField;

import serialization.JSONObject;
import serialization.ReflectionJSONObject;
import solutions.Election;
import solutions.Settings;
import solutions.VTD;
import ui.MainFrame;

public class Feature extends ReflectionJSONObject<Feature> implements Comparable<Feature> {
	
	public static Color[] colors = new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};

	
	public static final int DISPLAY_MODE_NORMAL = 0;
	public static final int DISPLAY_MODE_TEST1 = 1;
	public static final int DISPLAY_MODE_TEST2 = 2;
	public static final int DISPLAY_MODE_VOTES = 3;
	public static final int DISPLAY_MODE_DIST_POP = 4;
	public static final int DISPLAY_MODE_DIST_VOTE = 5;
	public static final int DISPLAY_MODE_COMPACTNESS = 6;
	public static final int DISPLAY_MODE_WASTED_VOTES = 7;
	public static final int DISPLAY_MODE_DEMOGRAPHICS = 8;
	public static boolean show_seats = true;
	
	public Vector<JDBField> dbfFields = new Vector<JDBField>();
	
	public String type;
	public Properties properties;
	public Geometry geometry;
	public VTD vtd = null;
	public Vector<double[]> points = new Vector<double[]>();
	
	public static boolean compare_centroid = true;
	
	public static boolean showPrecinctLabels = false;
	public static boolean showDistrictLabels = false;
	public static int display_mode = 0;
	public static boolean draw_lines = false;
	public static boolean showPoints = true;
	
	public void setDistFromPoints( String colname) {
		if( points == null || points.size() == 0) {
			return;
		}
		int[] counts = new int[200];
		for( int i = 0; i < counts.length; i++) {
			counts[i] = 0;
		}
		int maxindex = -1;
		int max = 0;
		for( int i = 0; i < points.size(); i++) {
			int p = (int)points.get(i)[2];
			//System.out.print(p+" ");
			counts[p]++;
			if( counts[p] > max) {
				max = counts[p];
				maxindex = p;
			}
		}
		//System.out.println();
		//System.out.println(" "+colname+": "+maxindex+" "+max);
		properties.put(colname,""+maxindex);
		points = null;
	}
	
	@Override
	public int compareTo(Feature o) {
		if( compare_centroid) {
			return this.geometry.full_centroid[0] > o.geometry.full_centroid[0] ? 1 : 
				 this.geometry.full_centroid[0] < o.geometry.full_centroid[0]  ? -1 :
					 0
					 ;
		} else {
			return 
					this.vtd.id > o.vtd.id ? 1 :
						this.vtd.id < o.vtd.id ? -1 :
							0;
		}
	}

	
	
	public double calcArea() {
		System.out.print(".");
		
		double tot_area = 0;
		try {
		if( !Settings.use_rectangularized_compactness) {
			for( int i = 0; i < geometry.coordinates.length; i++) {
				double[][] coords = geometry.coordinates[i];
				double[] lons = new double[coords.length];
				double[] lats = new double[coords.length];
				for( int j = 0; j < coords.length; j++) {
					lons[j] = Math.toRadians(coords[j][0]);
					lats[j] = Math.toRadians(coords[j][1]);
				}
				tot_area += SphericalPolygonArea(lats,lons,Geometry.RADIUS_OF_EARTH);
			}
		} else {
			FeatureCollection.recalcDlonlat();
			double xscale = FeatureCollection.dlonlat*(double)Geometry.SCALELATLON;
			double yscale = Geometry.SCALELATLON;
			//System.out.println("dlatlon "+FeatureCollection.dlonlat);
			//System.out.println("xscale "+xscale);
			//System.out.println("yscale "+yscale);
			
			for( int i = 0; i < geometry.coordinates.length; i++) {
				double[][] coords = geometry.coordinates[i];
				double[] xpoints = new double[coords.length];
				double[] ypoints = new double[coords.length];
				int npoints = coords.length;
				
				for( int j = 0; j < coords.length; j++) {
					xpoints[j] = coords[j][0]*xscale;
					ypoints[j] = coords[j][1]*yscale;
				}
				tot_area += Math.abs(flatArea(xpoints,ypoints,npoints));
			}
			
		}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		vtd.area = tot_area;
		return tot_area;
	}
	
	public double flatArea(double[] X,double[] Y,int N) {
	        double sum = 0.0;
	        int j = N-1;  // The last vertex is the 'previous' one to the first

	        for (int i = 0; i < N; i++) {
	            //sum += (X[i] * Y[(i+1)%N]) - (Y[i] * X[(i+1)%N]);
	            sum += (X[j]+X[i]) * (Y[j]-Y[i]);
	            j = i;
	        }
	        return sum/2.0;
	        //return Math.sqrt(0.5 * sum);
	}
	
	double getPolygonArea(double[] X,double[] Y,int N) {
		int i,j;
		double area = 0;
		for( i=0; i < N; i++) {
			j = (i + 1) % N;
			area += X[i] * Y[j];
			area -= Y[i] * X[j];
	    }
		area /= 2;
		return(area < 0 ? -area : area);
	}
	
	/// <summary>
	/// Haversine function : hav(x) = (1-cos(x))/2
	/// </summary>
	/// <param name="x"></param>
	/// <returns>Returns the value of Haversine function</returns>
	public double Haversine( double x )
	{
	        return ( 1.0 - Math.cos( x ) ) / 2.0;
	}

	/// <summary>
	/// Compute the Area of a Spherical Polygon
	/// </summary>
	/// <param name="lat">the latitudes of all vertices(in radian)</param>
	/// <param name="lon">the longitudes of all vertices(in radian)</param>
	/// <param name="r">spherical radius</param>
	/// <returns>Returns the area of a spherical polygon</returns>
	public double SphericalPolygonArea( double[ ] lat , double[ ] lon , double r )
	{
	       double lam1 = 0, lam2 = 0, beta1 =0, beta2 = 0, cosB1 =0, cosB2 = 0;
	       double hav = 0;
	       double sum = 0;

	       for( int j = 0 ; j < lat.length ; j++ )
	       {
		int k = j + 1;
		if( j == 0 )
		{
		     lam1 = lon[j];
		     beta1 = lat[j];
		     lam2 = lon[j + 1];
		     beta2 = lat[j + 1];
		     cosB1 = Math.cos( beta1 );
		     cosB2 = Math.cos( beta2 );
	             }
		else
		{
		     k = ( j + 1 ) % lat.length;
		     lam1 = lam2;
		     beta1 = beta2;
		     lam2 = lon[k];
	                  beta2 = lat[k];
		     cosB1 = cosB2;
		     cosB2 = Math.cos( beta2 );
		}
		if( lam1 != lam2 )
		{
		     hav = Haversine( beta2 - beta1 ) + 
	                          cosB1 * cosB2 * Haversine( lam2 - lam1 );
		     double a = 2 * Math.asin( Math.sqrt( hav ) );
		     double b = Math.PI / 2 - beta2;
		     double c = Math.PI / 2 - beta1;
		     double s = 0.5 * ( a + b + c );
		     double t = Math.tan( s / 2 ) * Math.tan( ( s - a ) / 2 ) *  
	                                Math.tan( ( s - b ) / 2 ) * Math.tan( ( s - c ) / 2 );

		     double excess = Math.abs( 4 * Math.atan( Math.sqrt( 
	                                        Math.abs( t ) ) ) );

		     if( lam2 < lam1 )
		     {
			excess = -excess;
		     }

		     sum += excess;
		}
	      }
	      return Math.abs( sum ) * r * r;
	}	
	
	public void toggleClicked() {
		try {

		if( vtd.state == 0) {
			vtd.state = 2;
			for( VTD b : vtd.neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 0) {
					b.state = 1;
				}
			}
		} else if( vtd.state == 2) { 
			vtd.state = 0;
			for( VTD b : vtd.neighbors) {
				
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
		if( geometry.fillColor != null || vtd.state != 0 || display_mode != DISPLAY_MODE_NORMAL) {
			g.setColor(geometry.fillColor);
			if( display_mode == DISPLAY_MODE_TEST1) {
				g.setColor(vtd.elections != null && vtd.elections.size() > 0 ? FeatureCollection.DEFAULT_COLOR :  Color.black);
			} else if( display_mode == DISPLAY_MODE_TEST2) {
				g.setColor(vtd.has_census_results ? FeatureCollection.DEFAULT_COLOR :  Color.black);
			} else if( display_mode == DISPLAY_MODE_DEMOGRAPHICS) {
				double tot = 0;
				double red = 0;
				double green = 0;
				double blue = 0;
				//for( int k = 0; k < vtd.elections.size(); k++) {
					//Vector<Election> dem = vtd.elections.get(k);
					for( int i = 0; i < vtd.demographics.length && i < colors.length; i++) {
						int pop = (int)vtd.demographics[i];
						tot += pop;
						red += colors[i].getRed()*pop;
						green += colors[i].getGreen()*pop;
						blue += colors[i].getBlue()*pop;
					}
				//}
				red /= tot;
				green /= tot;
				blue /= tot;
				g.setColor(new Color((int)red,(int)green,(int)blue));
			} else if( display_mode == DISPLAY_MODE_VOTES) {
				double tot = 0;
				double red = 0;
				double green = 0;
				double blue = 0;
				for( int k = 0; k < vtd.elections.size(); k++) {
					Vector<Election> dem = vtd.elections.get(k);
					for( int i = 0; i < dem.size() && i < colors.length; i++) {
						int pop = dem.get(i).population;
						tot += pop;
						red += colors[i].getRed()*pop;
						green += colors[i].getGreen()*pop;
						blue += colors[i].getBlue()*pop;
					}
				}
				red /= tot;
				green /= tot;
				blue /= tot;
				g.setColor(new Color((int)red,(int)green,(int)blue));
			} else {
				if( vtd.state == 1) {
					g.setColor(Color.blue);
				}
				if( vtd.state == 2) {
					g.setColor(Color.gray);
				}
			}
			for( int i = 0; i < geometry.polygons.length; i++) {
				g.fillPolygon(geometry.polygons[i]);
			}
		}
		if( geometry.outlineColor != null && draw_lines && !MainFrame.mainframe.evolving) {
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
					String name = this.properties.getString("NAME");//.DISTRICT;
					if( name == null) {
						continue;
					}
					centroid[0] -= fm.stringWidth(name)/2.0;
					centroid[1] += fm.getHeight()/2.0;
					g.drawString(name, (int)centroid[0],(int)centroid[1]);
				}
				
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
