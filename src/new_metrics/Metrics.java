package new_metrics;

import java.util.*;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.util.FastMath;

public class Metrics {
	int trials = 100000;
	
	//Expected_asymmetry: -0.4247375
	//Expected_asymmetry: -0.3091875
	//Expected_asymmetry: -0.13595
	//Expected_asymmetry: 0.1228875
	
	double[][] dem_counts = null;
	double[][] rep_counts = null;

	double[][] centered_dem_counts = null;
	double[][] centered_rep_counts = null;
	
	double[][] dem_pcts = null;
	double[] election_pcts = null;
	
	BetaDistribution election_bd = null;
	Vector<BetaDistribution> district_bds = null;
	Vector<BetaDistribution> centerd_district_bds = null;
	double[] seat_probs = null;
	double seat_expectation = 0;

	public double asymmetry_90_low;
	public double expected_asymmetry;
	public double asymmetry_90_high;

	public double asymmetry_median;
	
	Metrics() { }
	Metrics(double[][] dem, double[][] rep, int num_districts) {
		compute( dem,  rep,  num_districts);
	}
	public void centerCounts(double[][] dem_all, double[][] rep_all) {
		centered_dem_counts = new double[rep_all.length][];
		centered_rep_counts = new double[rep_all.length][];
		for(int j = 0; j < dem_all.length; j++) {
			double[] dem = dem_all[j];
			double[] rep = rep_all[j];

			centered_dem_counts[j] = new double[dem.length];
			centered_rep_counts[j] = new double[dem.length];
			double dem_total = 0;
			double rep_total = 0;
			for( int i = 0; i < dem.length; i++) {
				dem_total += dem[i];
				rep_total += rep[i];
			}
			double target_total = (dem_total+rep_total)/2;
			for( int i = 0; i < dem.length; i++) {
				centered_dem_counts[j][i] = dem[i] * target_total/dem_total;
				centered_rep_counts[j][i] = rep[i] * target_total/rep_total;
			}
		}
	}
	
 	public void compute(double[][] dem, double[][] rep, int num_districts) {
		dem_counts = dem;
		rep_counts = rep;
		centerCounts(dem_counts,rep_counts);
		centerd_district_bds = getDistributions(centered_dem_counts,centered_rep_counts,num_districts);
		centerd_district_bds.remove(0);
		
		Vector<BetaDistribution> bds = getDistributions(dem,rep,num_districts);
		election_bd = bds.remove(0);
		district_bds = bds;
		seat_probs = getSeatProbs(bds,trials);
		seat_expectation = 0;
		for(int i = 0; i < seat_probs.length; i++) {
			System.out.println(""+i+" seats: "+seat_probs[i]);
			seat_expectation += seat_probs[i]*(double)i;
		}
		System.out.println("Expectation: "+seat_expectation);
		
		
		//now do asymmetry
		
		expected_asymmetry = 0;
		Vector<Double> asym = new Vector<Double>();
		for( int i = 0; i < trials; i++) {
			double d = scoreRandom();
			expected_asymmetry += d;
			asym.add(d);
			//System.out.println("running total "+expected_asymmetry);
		}
		expected_asymmetry /= (double)trials;

		double mad = 0;
		double var = 0;
		for( int i = 0; i < trials; i++) {
			double d = expected_asymmetry-asym.get(i);
			mad += Math.abs(d);
			var += d*d;
		}
		mad /= (double)(trials-1);
		var /= (double)(trials-1);
		Collections.sort(asym);
		asymmetry_90_low = asym.get(trials/20);
		asymmetry_median = asym.get(trials/2);
		asymmetry_90_high = asym.get(trials-trials/20);
		//System.out.println("expected_asymmetry: "+expected_asymmetry);
		System.out.println();
		System.out.println("asymmetry   low 5%: "+asymmetry_90_low);
		System.out.println("asymmetry   median: "+asymmetry_median);
		System.out.println("Expected_asymmetry: "+expected_asymmetry);
		System.out.println("asymmetry MAD     : "+mad);
		System.out.println("asymmetry SD      : "+Math.sqrt(var));
		System.out.println("asymmetry high  5%: "+asymmetry_90_high);
		System.out.println();
	}
	public void showBetas() {
		FrameDraw fd = new FrameDraw();
		fd.dist = election_bd;
		fd.dists = district_bds;
		
		fd.show();
		fd.repaint();
	}
	public void showSeats() {
		
		FrameDraw fd2 = new FrameDraw();
		fd2.seats = seat_probs;

		fd2.show();
		fd2.repaint();
		
	}
	public double[] getSeatProbs(Vector<BetaDistribution> bds, int trials) {
		double delta = 1.0/(double)trials;
		double[] seatProbs = new double[bds.size()+1];
		for( int i = 0; i < trials; i++) {
			int seats = 0;
			for(BetaDistribution bd : bds) {
				double r = FastMath.random();
				if( r <= bd.cumulativeProbability(0.5)) {
					seats++;
				}
			}
			seatProbs[seats] += delta;
		}
		
		return seatProbs;
	}
	
	public Vector<BetaDistribution> getDistributions(double[][] elections_district_dem_counts,double[][] elections_district_rep_counts, int num_districts) {
		Vector<BetaDistribution> bds = new Vector<BetaDistribution>();
		double[] dem_pop = new double[elections_district_rep_counts.length];
		double[] rep_pop = new double[elections_district_rep_counts.length];
		dem_pcts = new double[elections_district_rep_counts.length][num_districts];
		for(int j = 0; j < num_districts; j++) {
			double[] dd = new double[elections_district_rep_counts.length];
			for(int i = 0; i < elections_district_rep_counts.length; i++) {
				double d = elections_district_dem_counts[i][j];
				double r = elections_district_rep_counts[i][j];
				dd[i] = d/(d+r);
				dem_pcts[i][j] = dd[i];
				dem_pop[i] += d;
				rep_pop[i] += r;
			}
			bds.add(new BetaDistribution(dd));
		}
		election_pcts = new double[elections_district_rep_counts.length];
		for(int i = 0; i < elections_district_rep_counts.length; i++) {
			double d = dem_pop[i];
			double r = rep_pop[i];
			election_pcts[i] = d/(d+r);
		}
		bds.insertElementAt(new BetaDistribution(election_pcts),0);
		return bds;
	}
	
	public double scoreRandom() {
		Vector<Double> ds = new Vector<Double>();
		for( BetaDistribution bd : centerd_district_bds) {
			ds.add(bd.sample());
		}
		Collections.sort(ds);
		double sample_curve_at = election_bd.sample();
		//System.out.println("sample: "+sample_curve_at);
		
		double dem = getSeatsFromDistsAndVote(ds,sample_curve_at);
		double rep = 1-getSeatsFromDistsAndVote(ds,1-sample_curve_at);
		//System.out.println("dem: "+dem+" rep: "+rep+" "+(dem-rep));
		return (dem-rep);
	}
	
	//TODO: this is wrong.  need to rethink it. - do i have to use beta distributions from mean-centered vote counts?
	public double getSeatsFromDistsAndVote(Vector<Double> sorted_dists, double dem_vote) {
		
		double rep_vote = 1-dem_vote;
		double num_dem_seats = 0;
		for( int i = 0; i < sorted_dists.size(); i++) {
			double dist_dem_vote = sorted_dists.get(i);
			double dist_rep_vote = 1-dist_dem_vote;
			//System.out.println(" "+i+" "+dist_rep_vote+" "+dem_vote);
			if( dem_vote >= dist_rep_vote) {
				num_dem_seats++;
			}
		}
		return ((double)num_dem_seats)/(double)sorted_dists.size();

	}


}
