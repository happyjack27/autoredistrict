package solutions;

import java.util.HashSet;
import java.util.Vector;

import geography.VTD;
import util.HashVector;
import util.StaticFunctions;

public class Region extends Vector<VTD> {
	
	DistrictMap dm;
	District d;
	int num;
	
	public Region(DistrictMap dm, District d, int num) {
		this.dm = dm;
		this.d = d;
		this.num = num;
	}

	public static HashVector<Integer> borders_districts = new HashVector<Integer>();
	public void giveToBorderingDistrict() {
		if( borders_districts.size() == 0) {
			return;
		}
		double lowest_pop = -1;
		int to = -1;
		for( int i = 0; i < borders_districts.size(); i++) {
			int test = borders_districts.get(i);
			if( lowest_pop < 0 || dm.districts.get(test).getPopulation() < lowest_pop) {
				lowest_pop = dm.districts.get(test).getPopulation();
				to = test;
			}
		}
		//to = StaticFunctions.selectRandom(borders_districts);
		if( to < 0) {
			return;
		}
		for( VTD v : this) {
			double vtd_pop = dm.vtd_districts[v.id];
			dm.districts.get(dm.vtd_districts[v.id]).population -= vtd_pop; 
			dm.vtd_districts[v.id] = to;
			dm.districts.get(to).population += vtd_pop; 
		}
	}

}
