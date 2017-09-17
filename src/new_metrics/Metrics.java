package new_metrics;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.util.FastMath;

import util.Pair;


/*
 * I NEED TO TURNOUT-CENTER DISTRICT COUNTS (ADJUST THEM TO THE AVERAGE)
 * 
 * 
 */

public class Metrics {
	public int trials = 100000;
	
	//Expected_asymmetry: -0.4247375
	//Expected_asymmetry: -0.3091875
	//Expected_asymmetry: -0.13595
	//Expected_asymmetry: 0.1228875
	
	int num_districts = 0;
	
	double[][] dem_counts = null;
	double[][] rep_counts = null;

	double[][] centered_dem_counts = null;
	double[][] centered_rep_counts = null;
	
	double[][] district_totals = null;  
	double[][] centered_district_totals = null;  
	double[] election_totals = null;

	double[][] dem_pcts = null;
	double[] election_pcts = null;
	
	BetaDistribution election_betas = null;
	Vector<BetaDistribution> district_betas = null;
	Vector<BetaDistribution> centered_district_betas = null;

	GammaDistribution election_gammas = null;
	Vector<GammaDistribution> district_gammas = null;
	
	Vector<Double> asym_results = new Vector<Double>();


	
	double[] seat_probs = null;
	double[] inv_seat_probs = null;
	double seat_expectation = 0;

	public double asymmetry_90_low;
	public double expected_asymmetry;
	public double asymmetry_90_high;

	public double asymmetry_median;
	
	Metrics() { }
	public Metrics(double[][] dem, double[][] rep, int num_districts) {
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
	
	public void estimateDistributions() {
		Vector<BetaDistribution> bds = getBetaDistributions(dem_counts,rep_counts,num_districts);
		election_betas = bds.remove(0);
		district_betas = bds;

		centered_district_betas = getBetaDistributions(centered_dem_counts,centered_rep_counts,num_districts);
		centered_district_betas.remove(0);
		
		Vector<GammaDistribution> gds = getGammaDistributions(dem_counts,rep_counts,num_districts);
		election_gammas = gds.remove(0);
		district_gammas = gds;
		
	}
	public void initialize(double[][] dem, double[][] rep, int num_districts) {
		this.num_districts = num_districts;
		dem_counts = dem;
		rep_counts = rep;
		centerCounts(dem_counts,rep_counts);
	}
	
 	public void compute(double[][] dem, double[][] rep, int num_districts) {
 		initialize(dem, rep, num_districts);
 		estimateDistributions();
 	}
 	
 	public void computeAsymmetry(boolean use_gammas) {
		
		expected_asymmetry = 0;
		asym_results = new Vector<Double>();
		for( int i = -num_districts/2; i <= num_districts/2; i++) {
			asym_results.add((double)i/(double)num_districts);
		}
		for( int i = 0; i < trials; i++) {
			double d = use_gammas ? scoreRandom2() : scoreRandom();
			expected_asymmetry += d;
			asym_results.add(d);
			//System.out.println("running total "+expected_asymmetry);
		}
		expected_asymmetry /= (double)trials;

		double mad = 0;
		double var = 0;
		for( int i = 0; i < trials; i++) {
			double d = expected_asymmetry-asym_results.get(i);
			mad += Math.abs(d);
			var += d*d;
		}
		mad /= (double)(trials-1);
		var /= (double)(trials-1);
		Collections.sort(asym_results);
		asymmetry_90_low = asym_results.get(trials/20);
		asymmetry_median = asym_results.get(trials/2);
		asymmetry_90_high = asym_results.get(trials-trials/20);
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
 	
 	public void compute2(double[][] dem, double[][] rep, int num_districts) {
 		initialize(dem, rep, num_districts);
 		estimateDistributions();
 		//monteCarloIntegration2();
 	}

 	public void computeSeatProbs(boolean use_gammas) {
		seat_probs = use_gammas ? getSeatProbs2(trials) : getSeatProbs(trials);
		seat_expectation = 0;
		for(int i = 0; i < seat_probs.length; i++) {
			System.out.println(""+i+" seats: "+seat_probs[i]);
			seat_expectation += seat_probs[i]*(double)i;
		}
		System.out.println("Expectation: "+seat_expectation);
	}
	public void showBetas() {
		FrameDraw fd = new FrameDraw();
		fd.dist = election_betas;
		fd.dists = district_betas;
		System.out.println("beta params:");
		for( int i = 0; i < centered_district_betas.size(); i++) {
			BetaDistribution b = centered_district_betas.get(i);
			System.out.println(""+(i+1)+" alpha: "+b.getAlpha()+", beta: "+b.getBeta());
			
		}
		
		fd.show();
		fd.repaint();
	}
	public void showSeats() {
		
		FrameDraw fd2 = new FrameDraw();
		fd2.seats = seat_probs;

		fd2.show();
		fd2.repaint();
		
	}
	public double[][] getAnOutcome() {
		double[][] ddd = new double[num_districts][];
		double demt = 0;
		double rept = 0;
		for( int i = 0; i < num_districts; i++) {
			ddd[i] = new double[2];
			double turnout = district_gammas.get(i).inverseCumulativeProbability(Math.random());
			double pct = district_betas.get(i).inverseCumulativeProbability(Math.random());
			demt += (ddd[i][0] = turnout*pct);
			rept += (ddd[i][1] = turnout*(1-pct));
		}
		double turnout = election_gammas.inverseCumulativeProbability(Math.random());
		double pct = election_betas.inverseCumulativeProbability(Math.random());
		double mult_dem = turnout*pct/demt; 
		double mult_rep = turnout*(1-pct)/rept; 
		for( int i = 0; i < num_districts; i++) {
			ddd[i][0] *= mult_dem;
			ddd[i][1] *= mult_rep;
		}
		return ddd;
	}
	public double[] getSeatProbs(int trials) {
		double delta = 1.0/(double)trials;
		double[] seatProbs = new double[district_betas.size()+1];
		for( int i = 0; i < trials; i++) {
			int seats = 0;
			for(BetaDistribution bd : district_betas) {
				double r = FastMath.random();
				if( r <= bd.cumulativeProbability(0.5)) {
					seats++;
				}
			}
			seatProbs[seats] += delta;
		}
		
		return seatProbs;
	}
	public double[] getSeatProbs2(int trials) {
		double delta = 1.0/(double)trials;
		double[] seatProbs = new double[district_betas.size()+1];
		for( int i = 0; i < trials; i++) {
			int seats = 0;
			double[][] result = getAnOutcome();
			for(int j = 0; j < num_districts; j++) {
				if( result[j][0] < result[j][1]) {
					seats++;
				}
			}
			seatProbs[seats] += delta;
		}
		
		return seatProbs;
	}
	public Vector<GammaDistribution> getGammaDistributions(double[][] elections_district_dem_counts,double[][] elections_district_rep_counts, int num_districts) {
		Vector<GammaDistribution> bds = new Vector<GammaDistribution>();
		double[] dem_pop = new double[elections_district_rep_counts.length];
		double[] rep_pop = new double[elections_district_rep_counts.length];
		district_totals = new double[elections_district_rep_counts.length][num_districts];
		centered_district_totals = new double[elections_district_rep_counts.length][num_districts];
		
		//get district turnouts, election totals by party.
		for(int j = 0; j < num_districts; j++) {
			double[] dd = new double[elections_district_rep_counts.length];
			for(int i = 0; i < elections_district_rep_counts.length; i++) {
				double d = elections_district_dem_counts[i][j];
				double r = elections_district_rep_counts[i][j];
				district_totals[i][j] = (d+r);
				dd[i] = (d+r);
				dem_pop[i] += d;
				rep_pop[i] += r;
			}
			//bds.add(new GammaDistribution(dd));
		}
		
		//now get election turnouts, and avg election turnout
		election_totals = new double[elections_district_rep_counts.length];
		double eavg = 0;
		for(int i = 0; i < elections_district_rep_counts.length; i++) {
			double d = dem_pop[i];
			double r = rep_pop[i];
			election_totals[i] = (d+r);
			eavg += (d+r);
		}
		eavg /= elections_district_rep_counts.length;

		//now get the by-district turnouts, mean centered to the average election turnout
		//and create gamma distributions from that.
		for(int j = 0; j < num_districts; j++) {
			double[] dd = new double[elections_district_rep_counts.length];
			for(int i = 0; i < elections_district_rep_counts.length; i++) {
				centered_district_totals[i][j] = district_totals[i][j]*eavg/election_totals[i];
				dd[i] = centered_district_totals[i][j];
			}
			bds.add(new GammaDistribution(dd));
		}
		
		bds.insertElementAt(new GammaDistribution(election_totals),0);
		return bds;
	}
	
	public Vector<BetaDistribution> getBetaDistributions(double[][] elections_district_dem_counts,double[][] elections_district_rep_counts, int num_districts) {
		Vector<BetaDistribution> bds = new Vector<BetaDistribution>();
		double[] dem_pop = new double[elections_district_rep_counts.length];
		double[] rep_pop = new double[elections_district_rep_counts.length];
		double[][] dem_pcts = new double[elections_district_rep_counts.length][num_districts];
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
		double[] election_pcts = new double[elections_district_rep_counts.length];
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
		for( BetaDistribution bd : centered_district_betas) {
			ds.add(bd.sample());
		}
		Collections.sort(ds);
		double sample_curve_at = election_betas.sample();
		//System.out.println("sample: "+sample_curve_at);
		
		double dem = getSeatsFromDistsAndVote(ds,sample_curve_at);
		double rep = 1-getSeatsFromDistsAndVote(ds,1-sample_curve_at);
		//System.out.println("dem: "+dem+" rep: "+rep+" "+(dem-rep));
		return (dem-rep);
	}
	
	public double scoreRandom2() {
		double[][] dd = getAnOutcome();
		double demseats = 0;
		double repseats = 0;
		double demvotes = 0;
		double repvotes = 0;
		for( int i = 0; i < num_districts; i++) {
			demvotes += dd[i][0];
			repvotes += dd[i][1];
			demseats += dd[i][0] > dd[i][1] ? 1 : 0;
		}
		double dr = repvotes/demvotes; 
		double rr = demvotes/repvotes; 
		for( int i = 0; i < num_districts; i++) {
			double d = dd[i][0]*dr;
			double r = dd[i][1]*rr;
			repseats += r > d ? 1 : 0;
		}
		return (demseats-repseats)/(double)num_districts;
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
	public void binAndShow(Vector<Double> samples) {
		Hashtable<Double,Double> hash = new Hashtable<Double,Double>();
		double tot = 0;
		for( double d : samples) {
			Double bin = hash.get(d);
			if( bin == null) {
				bin = new Double(0);
			}
			bin++;
			hash.put(d, bin);
			tot++;
		}
		Vector<Pair<Double,Double>> bins = new Vector<Pair<Double,Double>>();
		for( Entry<Double,Double> e : hash.entrySet()) {
			bins.add(new Pair<Double,Double>(e.getKey(),e.getValue()/tot));
		}
		Collections.sort(bins);
		FrameDraw fd = new FrameDraw();
		fd.bins = bins;
		fd.show();

		
	}
	public void showAsymmetry() {
		//now do asymmetry
		binAndShow(asym_results);
		
	}

	public void showPacking() {
		Vector<Double> packing = new Vector<Double>();
		for( int i = 0; i < trials; i++) {
			double[][] o = getAnOutcome();
			double[] r = get_packing(o);
			packing.add(r[0]-r[1]);
			if( i % 100 == 0) {
				System.out.print(".");
			}
			if( i % 10000 == 0) {
				System.out.println();
			}
		}
		Collections.sort(packing);
		Vector<Double> dd = new Vector<Double>();
		double min = packing.get(0);
		double max = packing.get(packing.size()-1);
		double inc = (max-min)/100;
		double next = min+inc;
		for( int i = 0; i < packing.size(); i++) {
			if( packing.get(i) >= next) {
				next += inc;
			}
			packing.set(i,next-inc/2);
			
		}
		binAndShow(packing);
		
	}
	
	public double[] get_packing(double[][] district_votes) {
		
		//normalize votes to 50/50.
		double sum_d = 0;
		double sum_r = 0;
		double[][] centered_district_votes = new double[district_votes.length][2];
		for( int i = 0; i < district_votes.length; i++) {
			sum_d += district_votes[i][0];
			sum_r += district_votes[i][1];
		}
		double target = (sum_d+sum_r)/2;
		for( int i = 0; i < district_votes.length; i++) {
			centered_district_votes[i][0] = district_votes[i][0] * target / sum_d;
			centered_district_votes[i][1] = district_votes[i][1] * target / sum_r;
		}
		
		//count disenfranchisement
		double dem_sq = 0;
		double rep_sq = 0;
		double tot = 0;
		for( int i = 0; i < district_votes.length; i++) {
			double excess = centered_district_votes[i][0] - centered_district_votes[i][1];
			if( excess < 0) {
					rep_sq += excess * excess;
			} else {
					dem_sq += excess * excess;
			}
			tot += Math.abs(excess);
		}
		if( tot == 0) { tot = 1; }
		
		return new double[]{dem_sq/tot,rep_sq/tot,dem_sq/target,rep_sq/target,target};
	}
	
	public void showHistogram() {
		double res = 100;
		Vector<Double> vd = new Vector<Double>();
		for( int i = 0; i < 5*trials/res; i++) {
			double[][] dd = getAnOutcome();
			for( int j = 0; j < dd.length; j++) {
				double d = dd[j][0]/(dd[j][0]+dd[j][1]);
				d = Math.round(d*res)/res - 0.5;
				vd.add(d);
			}
		}
		binAndShow(vd);
	}
	
	public void showBetaParameters() {
		System.out.println("{");
		System.out.println("\tpopular_vote: { alpha: "+election_betas.getAlpha()+", beta: "+election_betas.getBeta()+" },");
		System.out.println("\tdistrict_vote: ["); 
		for( int i = 0; i < district_betas.size(); i++) {
			System.out.println("\t\t{ district: "+(i+1)+", alpha: "+district_betas.get(i).getAlpha()+", beta: "+district_betas.get(i).getBeta()+" },");
		}
		System.out.println("\t],"); 
		System.out.println("\tcentered_district_vote: ["); 
		for( int i = 0; i < centered_district_betas.size(); i++) {
			System.out.println("\t\t{ district: "+(i+1)+", alpha: "+district_betas.get(i).getAlpha()+", beta: "+district_betas.get(i).getBeta()+" },");
		}
		System.out.println("\t]"); 
		System.out.println("}");
	}
	public void showSeatsVotes() {
		FrameDrawSeatsVotes sv = new FrameDrawSeatsVotes();
		sv.dist = election_betas;
		sv.dists = district_betas;
		sv.show();
		// TODO Auto-generated method stub
		
	}

	/*
	public double[] get_packing(double[][] district_votes) {
		
		//normalize votes to 50/50.
		double sum_d = 0;
		double sum_r = 0;
		double[][] centered_district_votes = new double[district_votes.length][2];
		for( int i = 0; i < district_votes.length; i++) {
			sum_d += district_votes[i][0];
			sum_r += district_votes[i][1];
		}
		double target = (sum_d+sum_r)/2;
		for( int i = 0; i < district_votes.length; i++) {
			centered_district_votes[i][0] = district_votes[i][0] * target / sum_d;
			centered_district_votes[i][1] = district_votes[i][1] * target / sum_r;
		}
		
		//count disenfranchisement
		double dem_sq = 0;
		double rep_sq = 0;
		double tot = 0;
		for( int i = 0; i < district_votes.length; i++) {
			double excess = centered_district_votes[i][0] - centered_district_votes[i][1];
			if( excess < 0) {
					rep_sq += excess * excess;
			} else {
					dem_sq += excess * excess;
			}
			tot += Math.abs(excess);
		}
		if( tot == 0) { tot = 1; }
		
		return new double[]{dem_sq/tot,rep_sq/tot,dem_sq/target,rep_sq/target,target};
	}
	*/
}
