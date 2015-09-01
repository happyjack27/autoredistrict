package solutions;

import geography.Geometry;
import serialization.ReflectionJSONObject;

public class Edge extends ReflectionJSONObject<Edge> {
	public int id;
	public int ward1_id;
	public int ward2_id;
	public int vertex1_id;
	public int vertex2_id;
    public double length;
    
	public Ward ward1 = null;
	public Ward ward2 = null;
	public Vertex vertex1;
	public Vertex vertex2;
	
	public static boolean isLatLon = true;
	
    boolean areBothSidesSameDistrict(int[] ward_districts) {
    	if( ward1 == null || ward2 == null) {
    		return false;
    	}
        return ward_districts[ward1.id] == ward_districts[ward2.id];
    }
    
	public void setLength() {
		if( isLatLon) {
			//this is the haversine formula.
			/*
			double lat1 = vertex1.y;
			double lat2 = vertex2.y;
			double lon1 = vertex1.x;
			double lon2 = vertex2.x;
			double dlon = toRad(lon2 - lon1); 
			double dlat = toRad(lat2 - lat1); 
			double a = Math.sin(dlat/2)*Math.sin(dlat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon/2)*Math.sin(dlon/2); 
			double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
			double R = 6378100; //radius of earth in meters
			double d = R * c;
			*/
			length = haversine(vertex1.y,vertex1.x,vertex2.y,vertex2.x);//d;
		} else {
			double dx = vertex1.x-vertex2.x;
			double dy = vertex1.y-vertex2.y;
			length = Math.sqrt(dx*dx+dy*dy);
		}
	}
	
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Geometry.RADIUS_OF_EARTH * c;
    }


}