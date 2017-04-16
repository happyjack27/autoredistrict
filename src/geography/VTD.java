package geography;

import java.util.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JOptionPane;

import dbf.*;
import serialization.JSONObject;
import serialization.ReflectionJSONObject;
import solutions.*;
import ui.MainFrame;
import util.Pair;

public class VTD extends ReflectionJSONObject<VTD> implements Comparable<VTD> {
	
	public static Color[] colors = new Color[]{Color.blue,Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};
	public static Color[] demo_colors = new Color[]{new Color(192,192,192),Color.red,Color.green,Color.cyan,Color.yellow,Color.magenta,Color.orange,Color.gray,Color.pink,Color.white,Color.black};

	
	public static final int DISPLAY_MODE_NORMAL = 0;
	public static final int DISPLAY_MODE_TEST1 = 1;
	public static final int DISPLAY_MODE_TEST2 = 2;
	public static final int DISPLAY_MODE_VOTES = 3;
	public static final int DISPLAY_MODE_DIST_POP = 4;
	public static final int DISPLAY_MODE_DIST_VOTE = 5;
	public static final int DISPLAY_MODE_COMPACTNESS = 6;
	public static final int DISPLAY_MODE_WASTED_VOTES = 7;
	public static final int DISPLAY_MODE_DEMOGRAPHICS = 8;
	public static final int DISPLAY_MODE_COUNTIES = 9;
	public static final int DISPLAY_MODE_DIST_DEMO = 12;
	public static final int DISPLAY_MODE_COUNTY_SPLITS = 13;
	public static final int DISPLAY_MODE_PARTISAN_PACKING = 10;
	public static final int DISPLAY_MODE_RACIAL_PACKING = 11;
	public static final int DISPLAY_MODE_PARTISAN_PACKING2 = 14;
	public static final int DISPLAY_MODE_RACIAL_PACKING2 = 15;
	public static final int DISPLAY_MODE_PARTISAN_PACKING_MEAN = 16;
	public static final int DISPLAY_MODE_RACIAL_PACKING_MEAN = 17;
	public static final int DISPLAY_MODE_PARTISAN_PACKING2_MEAN = 18;
	public static final int DISPLAY_MODE_RACIAL_PACKING2_MEAN = 19;


	public static final int DISPLAY_MODE_DIST_DESCR = 20;
	public static final int DISPLAY_MODE_DIST_SEATS = 21;


	public static boolean USE_DISCRETE_COLORS = false;
	public static boolean USE_GRAY = false;


	public static boolean show_seats = true;
	
	public Vector<DBField> dbfFields = new Vector<DBField>();
	
	public String type;
	public Properties properties;
	public Geometry geometry;
	//public Feature vtd = null;
	public Vector<double[]> points = new Vector<double[]>();
	
	public static boolean compare_centroid = true;
	
	public static boolean showPrecinctLabels = false;
	public static boolean showDistrictLabels = false;
	public static int display_mode = 0;
	public static boolean outline_vtds = false;
	public static boolean outline_state = false;
	public static boolean outline_districts = false;
	public static boolean outline_counties = false;
	public static boolean showPoints = true;
	
	public static boolean isOutlineActive() {
		return outline_vtds || outline_state || outline_districts || outline_counties;
	}
	
	public boolean setDistFromPoints( String colname, boolean one_indexed) {
		if( points == null || points.size() == 0) {
			return false;
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
		points = null;
		if( one_indexed) {
			maxindex--;
		}
		if( maxindex < 0 || max == 0) {
			return false;
		}
		//System.out.println();
		//System.out.println(" "+colname+": "+maxindex+" "+max);
		properties.put(colname,""+maxindex);
		return true;
	}
	
	@Override
	public int compareTo(VTD o) {
		if( compare_centroid) {
			return this.geometry.full_centroid[0] > o.geometry.full_centroid[0] ? 1 : 
				 this.geometry.full_centroid[0] < o.geometry.full_centroid[0]  ? -1 :
					 0
					 ;
		} else {
			return 
					this.id > o.id ? 1 :
						this.id < o.id ? -1 :
							0;
		}
	}

	
	
	public double calcArea() {
		System.out.print(".");
    	if( !FeatureCollection.isLatLon) {
    		double tot_area = 0;
			for( int i = 0; i < geometry.coordinates.length; i++) {
				double[][] coords = geometry.coordinates[i];
				double[] xpoints = new double[coords.length];
				double[] ypoints = new double[coords.length];
				int npoints = coords.length;
				
				for( int j = 0; j < coords.length; j++) {
					xpoints[j] = coords[j][0];
					ypoints[j] = coords[j][1];
				}
				tot_area += Math.abs(flatArea(xpoints,ypoints,npoints));
			}
			area = tot_area;
			return area;

    	}

		
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
		area = tot_area;
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
		/*
		String s = "";
		
		Vector<Pair<String,String>> vps = new Vector<Pair<String,String>>();
		for( String key : this.properties.keySet()) {
			String value = this.properties.getString(key);
			vps.add(new Pair<String,String>(key,value));
			//s += key+": "+value+"\n";
		}
		Collections.sort(vps);
		int n = 0;
		for( Pair<String,String> ps: vps) {
			n++;
			s += ps.a+": "+ps.b+(n % 2 == 0 ? "\n": "    ");
		}
		*/
		
		try {

		if( state == 0) {
			state = 2;
			/*
			for( VTD b : neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 0) {
					b.state = 1;
				}
			}
			*/
		} else if( state == 2) { 
			state = 0;
			/*
			for( VTD b : neighbors) {
				
				if( b == null) {
					continue;
				}
				if( b.state == 1) {
					b.state = 0;
				}
			}
			*/
		}
		} catch (Exception ex) {
			System.out.println(" ex "+ex);
			ex.printStackTrace();
		}
		//JOptionPane.showMessageDialog(null, s);
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
		if( geometry.fillColor != null || state != 0 || display_mode != DISPLAY_MODE_NORMAL) {
			g.setColor(geometry.fillColor);
			if( display_mode == DISPLAY_MODE_TEST1) {
				g.setColor(elections != null && elections.size() > 0 ? FeatureCollection.DEFAULT_COLOR :  Color.black);
			} else if( display_mode == DISPLAY_MODE_TEST2) {
				g.setColor(has_census_results ? FeatureCollection.DEFAULT_COLOR :  Color.black);
				//getVoteGapPct(k);
			} else 
			if( display_mode == DISPLAY_MODE_RACIAL_PACKING2 || display_mode == DISPLAY_MODE_RACIAL_PACKING2_MEAN) {
				double tot = 0;
				double red = 0;
				double green = 0;
				
				double blue = 0;
				//for( int k = 0; k < elections.size(); k++) {
					//Vector<Election> dem = elections.get(k);
					for( int i = 0; i < demographics.length && i < demo_colors.length; i++) {
						int pop = (int)demographics[i];
						tot += pop;
						red += demo_colors[i].getRed()*pop;
						green += demo_colors[i].getGreen()*pop;
						blue += demo_colors[i].getBlue()*pop;
					}
				//}
				red /= tot;
				green /= tot;
				blue /= tot;
				double multiplier = 1;
				if( Settings.divide_packing_by_area) {
					multiplier = Settings.density_multiplier*(double)population/(double)area;
					if( multiplier > 1) {
						multiplier = 1;
					}
				}
				
				red = 255-(255-red)*geometry.color_multiplier;
				green = 255-(255-green)*geometry.color_multiplier;
				blue = 255-(255-blue)*geometry.color_multiplier;
				
				Color c = new Color((int)red,(int)green,(int)blue);
				g.setColor(c);
			} else if( display_mode == DISPLAY_MODE_PARTISAN_PACKING2 || display_mode == DISPLAY_MODE_PARTISAN_PACKING2_MEAN) {
				double tot = 0;
				double red = 0;
				double green = 0;
				double blue = 0;
				for( int k = 0; k < elections.size(); k++) {
					Vector<Election> dem = elections.get(k);
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
				
				red = 255-(255-red)*geometry.color_multiplier;
				green = 255-(255-green)*geometry.color_multiplier;
				blue = 255-(255-blue)*geometry.color_multiplier;
				
				Color c = new Color((int)red,(int)green,(int)blue);
				g.setColor(c);
			} else if( display_mode == DISPLAY_MODE_DEMOGRAPHICS) {
				double tot = 0;
				double red = 0;
				double green = 0;
				
				double blue = 0;
				//for( int k = 0; k < elections.size(); k++) {
					//Vector<Election> dem = elections.get(k);
					for( int i = 0; i < demographics.length && i < demo_colors.length; i++) {
						int pop = (int)demographics[i];
						tot += pop;
						red += demo_colors[i].getRed()*pop;
						green += demo_colors[i].getGreen()*pop;
						blue += demo_colors[i].getBlue()*pop;
					}
				//}

				double multiplier = 1;
				if( Settings.divide_packing_by_area) {
					multiplier = Settings.density_multiplier*(double)population/(double)area;
					if( multiplier > 1) {
						multiplier = 1;
					}
				}
				red = (255.0-(red/tot))*multiplier;
				blue = (255.0-(blue/tot))*multiplier;
				green = (255.0-(green/tot))*multiplier;
				red = 255.0-red;
				blue = 255.0-blue;
				green = 255.0-green;

				g.setColor(new Color((int)red,(int)green,(int)blue));
			} else if( display_mode == DISPLAY_MODE_VOTES) {
				double tot = 0;
				double red = 0;
				double green = 0;
				double blue = 0;
				for( int k = 0; k < elections.size(); k++) {
					Vector<Election> dem = elections.get(k);
					for( int i = 0; i < dem.size() && i < colors.length; i++) {
						int pop = dem.get(i).population;
						tot += pop;
						red += colors[i].getRed()*pop;
						green += colors[i].getGreen()*pop;
						blue += colors[i].getBlue()*pop;
					}
				}
				double multiplier = 1;
				if( Settings.divide_packing_by_area) {
					multiplier = Settings.density_multiplier*(double)population/(double)area;
					if( multiplier > 1) {
						multiplier = 1;
					}
				}
				red = (255.0-(red/tot))*multiplier;
				blue = (255.0-(blue/tot))*multiplier;
				green = (255.0-(green/tot))*multiplier;
				red = 255.0-red;
				blue = 255.0-blue;
				green = 255.0-green;

				g.setColor(new Color((int)red,(int)green,(int)blue));
			} else {
				if( state == 1) {
					g.setColor(Color.blue);
				}
				if( state == 2) {
					g.setColor(Color.gray);
				}
			}
			for( int i = 0; i < geometry.polygons.length; i++) {
				g.fillPolygon(geometry.polygons[i]);
			}
			if( !outline_vtds) {
				for( int i = 0; i < geometry.polygons.length; i++) {
					g.drawPolygon(geometry.polygons[i]);
				}
			}
		}
		if( geometry.outlineColor != null && outline_vtds && !MainFrame.mainframe.evolving) {
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
	   public int id;
		public static int id_enumerator = 0;


		public VTD feature = null;
		public int state = 0;
		public int temp = -1;
		public double area = 0;
		public String county = "";
		
		//public String name = "";


	    public Vector<Edge> edges = new Vector<Edge>();
	    public Vector<VTD> neighbors = new Vector<VTD>();
	    public double[] neighbor_lengths;
	    public double unpaired_edge_length = 0;
	    public Vector<Vector<Election>> elections = new Vector<Vector<Election>>();
	    //private double[][] mu_sigma_n = null;
	    
	    public boolean has_census_results = false;
	    public boolean has_election_results = false;
	    public double population=1;
	    
	    public void resetOutcomes() {
	    	outcomes = null;    	
	    }
	    /*
	    public void recalcMuSigmaN() {
	    	try {
	   		double[] successes = new double[Settings.num_candidates];
			double total = 0;
	        for( Demographic d : demographics) {
	            for( int j = 0; j < d.vote_prob.length; j++) {
	            	double n = d.population * d.vote_prob[j]*d.turnout_probability;
	            	n /= Settings.voting_coalition_size;
	            	total += n;
	            	successes[j] += n;
	            }
	        }
	        mu_sigma_n = new double[Settings.num_candidates][];
	        for( int j = 0; j < successes.length; j++) {
	        	double n = total;
	        	double p = successes[j] / total;
				double mu = n*p;
				double sigma = n*p*(1-p);
	        	mu_sigma_n[j] = new double[]{mu,sigma,n};
	        	if( total == 0) {
	        		mu_sigma_n[j] = new double[]{0,0,0};
	        	}
	        }   
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	    
	    public double[][] getMuSigmaN() {
	    	if( mu_sigma_n == null) {
	    		recalcMuSigmaN();    		
	    	}
	    	return mu_sigma_n;
	    }
	    */
	    
		double[][] outcomes;
		public double[] demographics = new double[]{};
		public String muni;
		public boolean temp_bool;

	    public VTD() {
	    	super();
	    	id = id_enumerator++;
	    }
	    public boolean equals(VTD b) {
	    	return b != null && b.id == this.id;
	    }
	    public void syncNeighbors() {
			for(VTD b : neighbors) {
				boolean is_in = false;
				for(VTD b2 : b.neighbors) {
					if( b2.id == this.id){
						is_in = true;
						break;
					}
				}
				if( !is_in) {
					b.neighbors.add(this);
				}
			}
	    	
	    }
	    public void collectNeighborLengths() {
	    	unpaired_edge_length = 0;
	    	neighbor_lengths = new double[neighbors.size()];
	    	for( int i = 0; i < neighbor_lengths.length; i++) {
	    		neighbor_lengths[i] = 0;
	    	}
			for(Edge e : edges) {
				boolean found = false;
		    	for( int i = 0; i < neighbor_lengths.length; i++) {
		    		VTD b = neighbors.get(i);
		    		if( e.ward1_id == b.id || e.ward2_id == b.id){
		    			neighbor_lengths[i] += e.length;
		    			found = true;
		    			break;
		    		}
		    	}
		    	if( !found) {
		    		unpaired_edge_length += e.length;
		    	}
			}
	    }
	    
	    public void collectNeighbors() {
	    	//this gets a list of distinct neighbors.
	    	
			neighbors = new Vector<VTD>();
			for( Edge e : edges) {
				VTD b = e.ward1.id == this.id ? e.ward2 : e.ward1;
				if( b != null && b.id != this.id) {
					boolean is_in = false;
					for(VTD b2 : neighbors) {
						if( b2.id == b.id){
							is_in = true;
							break;
						}
					}
					if( !is_in) {
						neighbors.add(b);
						//System.out.print(""+b.id+", ");
					}
				}
			}
			//System.out.println();
	    }

	    
		@Override
		public void post_deserialize() {
			super.post_deserialize();
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

			// TODO Auto-generated method stub
			
		}
		public double[] getElectionOutcome(int election) {
			Vector<String> cols = MainFrame.mainframe.project.multiElections.get(election);
			double[] dd = new double[cols.size()];
			for( int i = 0; i < cols.size(); i++) {
				try {
					dd[i] = Double.parseDouble(properties.getString(cols.get(i)));
				} catch (Exception ex) {

				}
			}
			return dd;
		}
	    public double[] getOutcome() {
	    	if( outcomes == null) {
	    		generateOutComes();
	    	}
	    	if( outcomes == null) {
	    		return new double[]{0,0};
	    	}
	    	int i = (int)Math.floor(Math.random()*(double)outcomes.length);
	    	return outcomes[i];
	    }
	    
	    public void generateOutComes() {
	    	if( !District.use_simulated_elections) {
	        	outcomes = new double[1][];
	    		outcomes[0] = new double[Settings.num_candidates];
	    	    
	    		//aggregate and normalize voting probs
	            for(int i = 0; i < outcomes[0].length; i++) {
	            	outcomes[0][i] = 0;
	            }
	            double[] totals = new double[elections.size()];
	            for( int i = 0; i < totals.length; i++);
	            for( int elec = 0; elec < elections.size(); elec++) {
			        for( Election d : elections.get(elec)) {
			            for( int j = 0; j < d.vote_prob.length; j++) {
			            	outcomes[0][j] += d.population * d.vote_prob[j]*d.turnout_probability;//d.vote_prob[j];
			            	totals[elec] += d.population * d.vote_prob[j]*d.turnout_probability;
			            }
			        }
	            }
	            double c = 0;
	            
	            for( int i = 0; i < elections.size(); i++) {
	            	if( elections.get(i).size() > 1 && totals[i] > 2) {
	            		if( 
	            				(i == 0 ? MainFrame.mainframe.project.election_columns.size() : 
	            					i == 1 ? MainFrame.mainframe.project.election_columns_2.size() :
	                					i == 2 ? MainFrame.mainframe.project.election_columns_3.size() :
	            					0) 
	            				> 1) {
	            			c++;
	            		}
	            	}
	            }
	            /*
	            for(int i = 0; i < outcomes[0].length; i++) {
	            	outcomes[0][i] /= c;
	            }
	            */
	    	} else {
		    	outcomes = new double[Settings.num_ward_outcomes][];
		    	for( int out = 0; out < outcomes.length; out++) {
		    		outcomes[out] = new double[Settings.num_candidates];
		    	    
		    		//aggregate and normalize voting probs
		        	double[] probs = new double[Settings.num_candidates];
		            for(int i = 0; i < probs.length; i++) {
		            	probs[i] = 0;
		            }
		            int elec = (int)(Math.random()*(double)elections.size());
		            for( Election d : elections.get(elec)) {
		                for( int j = 0; j < d.vote_prob.length; j++) {
		                	probs[j] += d.population * d.vote_prob[j]*d.turnout_probability;
		                }
		            }
		            double total_population = 0;
		            for(int i = 0; i < probs.length; i++) {
		            	total_population += probs[i];
		            }
		            double r_tot_prob  = 1.0/total_population;
		            for(int i = 0; i < probs.length; i++) {
		            	probs[i] *= r_tot_prob;
		            }
		
		            for(int j = 0; j < total_population; j++) {
		                double p = Math.random();
		                for( int k = 0; k < probs.length; k++) {
		                    p -=  probs[k];
		                    if( p <= 0) {
		                    	outcomes[out][k]++;
		                        break;
		                    }
		                }
		    		}
		    	}
	    	}
	    }

}
