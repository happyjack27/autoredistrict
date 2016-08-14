package geography;

import java.awt.Polygon;

import geography.*;
import serialization.ReflectionJSONObject;
import solutions.*;

public class Edge {// extends ReflectionJSONObject<Edge> {
	public int id = -1;
	public int ward1_id = -1;
	public int ward2_id = -1;
	public int vertex1_id = -1;
	public int vertex2_id = -1;
    public double length;
    
	public VTD ward1 = null;
	public VTD ward2 = null;
	public Vertex vertex1;
	public Vertex vertex2;
	
	public static boolean isLatLon = true;
	
	public VTD otherFeature(VTD vtd) {
		return ward1 == vtd ? ward2 : ward1;
	}
	public Vertex otherVertex(Vertex vertex) {
		return vertex1 == vertex ? vertex2 : vertex1;
	}

	
    public boolean areBothSidesSameDistrict(int[] ward_districts) {
    	if( ward1 == null || ward2 == null) {
    		return false;
    	}
        return ward_districts[ward1.id] == ward_districts[ward2.id];
    }
    
	public void setLength() {
		try {
			if( !Settings.use_rectangularized_compactness) {
				length = haversine(vertex1.y,vertex1.x,vertex2.y,vertex2.x);
			} else {
				double xscale = FeatureCollection.dlonlat*Geometry.SCALELATLON;
				double yscale = Geometry.SCALELATLON;
				double dx = xscale*(vertex1.x-vertex2.x);
				double dy = yscale*(vertex1.y-vertex2.y);
				length = Math.sqrt(dx*dx+dy*dy);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		/*
		if( isLatLon) {
			//this is the haversine formula.
			length = haversine(vertex1.y,vertex1.x,vertex2.y,vertex2.x);//d;
		} else {
			double dx = vertex1.x-vertex2.x;
			double dy = vertex1.y-vertex2.y;
			length = Math.sqrt(dx*dx+dy*dy);
		}
		*/
	}
	
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    	if( !FeatureCollection.isLatLon) {
    		return 1;
    	}
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Geometry.RADIUS_OF_EARTH * c;
    }


}