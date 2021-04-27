package new_metrics;
import java.util.Vector;

import org.apache.commons.math3.*;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.util.FastMath;

public class BetaStuff implements VoteCounts {
	/*
	 * 
	issues

	check:
	-alaska count (1)
	-hawaii count (2)
	-wyoming there (1)
	-total: 435


	SM ===========
	ALASKA - WRONG number
	04
	alaska
	08
	12

	BD ===========
	04
	alaska
	08
	missing
	12

	00 ===========
	04
	08
	12

	10 ===========
	04
	08
	12

	 */

	public static void main(String[] ss) {
		Metrics maxrep = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics actual = new Metrics(
				wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				//wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics fair = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				wi_fair_dem,wi_fair_rep,
				//wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics maxdem = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				wi_maxdem_dem,wi_maxdem_rep,
				8
				);
		maxdem.computeDisproportionalityStats();
	}
	void nothing() {
		Metrics maxrep = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics actual = new Metrics(
				wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				//wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics fair = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				wi_fair_dem,wi_fair_rep,
				//wi_maxrep_dem,wi_maxrep_rep,
				8
				);
		Metrics maxdem = new Metrics(
				//wi_actual_dem,wi_actual_rep,
				//wi_fair_dem,wi_fair_rep,
				wi_maxdem_dem,wi_maxdem_rep,
				8
				);
		
		GatherVoteCounts.getVoteCounts();
		/*
		boolean good = true;
		for(int i = 0; i < 3; i++) {
			if( GatherVoteCounts.dem_elections[i].length != 435) {
				System.out.println("wrong district count: "+i+" "+GatherVoteCounts.dem_elections[i].length);
				good = false;
			}
		}
		if( !good) {
			System.exit(0);
		}
		*/
		Metrics d10 = new Metrics(
				GatherVoteCounts.dem_elections
				,GatherVoteCounts.rep_elections
				,GatherVoteCounts.dem_elections[0].length
				,GatherVoteCounts.seat_counts
				);
		actual = d10;
		/*
		 * if( actual.centered_district_betas.size() != 435) {
			System.out.println("wrong district count!");
			System.exit(0);
		}
		*/
		//maxrep.showBetas();
		//fair.showBetas();
		//actual.showBetas();
		//maxdem.showBetas();
		try {
		actual.showBetas();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("1");
		try {
		actual.computeSeatProbs(false);
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("2");
		try {
		double[][] dd = actual.getAnOutcome();
		for( int i = 0; i < dd.length; i++) {
			System.out.println(""+i+": "+dd[i][0]+" "+dd[i][1]);
		}
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("3");
		try {
		actual.showSeats();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("4");
		try {
		actual.computeAsymmetry(false);
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("5");
		try {
		actual.showAsymmetry();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("6");
		try {
		actual.showSeatsVotes();
		} catch (Exception ex) { ex.printStackTrace(); }
		System.out.println("7");
		
		actual.showHeatMap();
		System.out.println("8");
	}



}
