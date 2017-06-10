package new_metrics;
import java.util.Vector;

import org.apache.commons.math3.*;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.util.FastMath;

public class BetaStuff implements VoteCounts {
		
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
		
		//maxrep.showBetas();
		//fair.showBetas();
		//actual.showBetas();
		//maxdem.showBetas();
		
		actual.showBetas();
		actual.computeSeatProbs(false);
		double[][] dd = actual.getAnOutcome();
		for( int i = 0; i < dd.length; i++) {
			System.out.println(""+i+": "+dd[i][0]+" "+dd[i][1]);
		}
		actual.showSeats();
		actual.computeAsymmetry(false);
		actual.showAsymmetry();
		actual.showSeatsVotes();
	}



}
