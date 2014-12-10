package mapCandidates;

import serialization.ReflectionJSONObject;

public class Edge extends ReflectionJSONObject<Edge> {
	public int id;
	public int block1_id;
	public int block2_id;
	public int vertex1_id;
	public int vertex2_id;
    public double length;
    
	public Block block1;
	public Block block2;
	public Vertex vertex1;
	public Vertex vertex2;
	
	public static boolean isLatLon = false;
	
    boolean areBothSidesSameDistrict(int[] block_districts) {
        return block_districts[block1.id] == block_districts[block2.id];
    }
    
	public void setLength() {
		if( isLatLon) {
			//this is the haversine formula.
			double lat1 = vertex1.x;
			double lat2 = vertex2.x;
			double lon1 = vertex1.y;
			double lon2 = vertex2.y;
			double dlon = lon2 - lon1; 
			double dlat = lat2 - lat1; 
			double a = Math.sin(dlat/2)*Math.sin(dlat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon/2)*Math.sin(dlon/2); 
			double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
			double R = 6378100; //radius of earth in meters
			double d = R * c;
			length = d;
		} else {
			double dx = vertex1.x-vertex2.x;
			double dy = vertex1.y-vertex2.y;
			length = Math.sqrt(dx*dx+dy*dy);
		}
	}

}