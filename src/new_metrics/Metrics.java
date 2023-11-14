package new_metrics;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import jsonMap.JsonMap;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.util.FastMath;

import util.GenericClasses.Pair;


/*
 * I NEED TO TURNOUT-CENTER DISTRICT COUNTS (ADJUST THEM TO THE AVERAGE)
 * 
 * 
 */
/*
00: presidential
dem: 50999897
rep: 50456002

16: presidential
dem: 65853514
rep: 62984828
*/


public class Metrics {
	public int trials = 100*1000;
	public static boolean show = false;
	public static boolean force_centered_popular_vote = false;
	
	public static boolean for_national = false;
	public static boolean for_national_center = false;
	public static boolean use_binomials = true;
	
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
	
	public Vector<String[]> point_stats = new Vector<String[]>();
	
	double[][][] election_samples = null;
	
	double[] seat_probs = null;
	double[] inv_seat_probs = null;
	double seat_expectation = 0;

	public double asymmetry_90_low;
	public double expected_asymmetry;
	public double asymmetry_90_high;

	public double asymmetry_median;
	private int[] seat_counts;
	public String output_folder;
	
	Metrics() { }
	public Metrics(double[][] dem, double[][] rep, int num_districts, int[] seat_counts) {
		this.seat_counts = seat_counts;
		compute( dem,  rep,  num_districts);
		point_stats = new Vector<String[]>();
		point_stats.add(new String[]{"Measurement","Statistic","Value"});
	}
	public Metrics(double[][] dem_elections, double[][] rep_elections, int length) {
		this(dem_elections,rep_elections,length,null);
	}
	public Vector<Vector<String[]>> getElections() {
		Vector<Vector<String[]>> vvs = new Vector<Vector<String[]>>();
		for( int i = 0; i < this.dem_counts.length; i++) {
			Vector<String[]> vs = new Vector<String[]>();
			vvs.add(vs);
			double[] dvotes = this.dem_counts[i];
			double[] rvotes = this.rep_counts[i];
			String[] ss0 = new String[]{"District","Dem Votes","Rep Votes"};//,"Dem Pct","Rep Pct"};
			vs.add(ss0);
			for( int j = 0; j < this.dem_counts[i].length; j++) {
				double pct = dvotes[j]/(dvotes[j]+rvotes[j]);
				String[] ss = new String[]{""+(j+1),""+(int)dvotes[j],""+(int)rvotes[j]};//,""+pct,""+(1.0-pct)};
				vs.add(ss);
			}
		}
		return vvs;
	}
	public Vector<String[]> getBetasAsVectorString() {
		Vector<String[]> vvs = new Vector<String[]>();
		vvs.add(new String[]{"District","Alpha","Beta","Mean","Variance"});
		BetaDistribution bd0 = this.election_betas;
		String[] ss0 = new String[]{
			"Total"
			,""+bd0.getAlpha()
			,""+bd0.getBeta()
			,""+bd0.getNumericalMean()
			,""+bd0.getNumericalVariance()
		};
		vvs.add(ss0);
		for( int i = 0; i < this.district_betas.size(); i++) {
			BetaDistribution bd = this.district_betas.get(i);
			String[] ss = new String[]{
				""+(i+1)
				,""+bd.getAlpha()
				,""+bd.getBeta()
				,""+bd.getNumericalMean()
				,""+bd.getNumericalVariance()
			};
			vvs.add(ss);
		}
		return vvs;
	}
	public Vector<String[]> getGammasAsVectorString() {
		Vector<String[]> vvs = new Vector<String[]>();
		vvs.add(new String[]{"District","Shape","Scale","Mean","Variance"});
		GammaDistribution bd0 = this.election_gammas;
		String[] ss0 = new String[]{
				"Total"
				,""+bd0.getShape()
				,""+bd0.getScale()
				,""+bd0.getNumericalMean()
				,""+bd0.getNumericalVariance()
			};
		vvs.add(ss0);
		for( int i = 0; i < this.district_gammas.size(); i++) {
			GammaDistribution bd = this.district_gammas.get(i);
			String[] ss = new String[]{
				""+(i+1)
				,""+bd.getShape()
				,""+bd.getScale()
				,""+bd.getNumericalMean()
				,""+bd.getNumericalVariance()
			};
			vvs.add(ss);
		}
		return vvs;
	}
	public JsonMap getProbabilityModelJson() {
		JsonMap m = new JsonMap();
		m.put("StatewideBetaDistribution", getBetaAsJson(election_betas));
		m.put("StatewideGammaDistribution", getGammaAsJson(election_gammas));
		
		Vector<JsonMap> db = new Vector<JsonMap>();
		for( BetaDistribution bd : district_betas) {
			db.add(getBetaAsJson(bd));
		}
		m.put("DistrictBetaDistribution", db);
		
		Vector<JsonMap> dg = new Vector<JsonMap>();
		for( GammaDistribution bd : district_gammas) {
			dg.add(getGammaAsJson(bd));
		}
		m.put("DistrictGammaDistribution", dg);
		
		return m;
	}
	public JsonMap getBetaAsJson(BetaDistribution bd) {
		JsonMap dist = new JsonMap();
		dist.put("Type","Beta");
		dist.put("Alpha",bd.getAlpha());
		dist.put("Beta",bd.getBeta());
		dist.put("Mean",bd.getNumericalMean());
		dist.put("Variance",bd.getNumericalVariance());
		return dist;
	}
	
	public JsonMap getGammaAsJson(GammaDistribution bd) {
		JsonMap dist = new JsonMap();
		dist.put("Type","Gamma");
		dist.put("Shape",bd.getShape());
		dist.put("Scale",bd.getScale());
		dist.put("Mean",bd.getNumericalMean());
		dist.put("Variance",bd.getNumericalVariance());
		return dist;
	}
	
	
	public void createElectionSamples(int num) {
		election_samples = new double[num][][];
		for( int i = 0; i < election_samples.length; i++) {
			election_samples[i] = getAnOutcome();
		}
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
		trials = election_samples.length;
		for( int i = 0; i < trials; i++) {
			double d = calculate_asymmetry(this.election_samples[i]);//use_gammas ? scoreRandom2() : scoreRandom();
			expected_asymmetry += d;
			asym_results.add(d);
			//System.out.println("running total "+expected_asymmetry);
		}
		expected_asymmetry /= trials;

		double mad = 0;
		double var = 0;
		for( int i = 0; i < trials; i++) {
			double d = expected_asymmetry-asym_results.get(i);
			mad += Math.abs(d);
			var += d*d;
		}
		mad /= trials-1;
		var /= trials-1;
		Collections.sort(asym_results);
		asymmetry_90_low = asym_results.get(trials/20);
		asymmetry_median = asym_results.get(trials/2);
		asymmetry_90_high = asym_results.get(trials-trials/20);
		double asymmetry_50_low = asym_results.get(trials/4);
		double asymmetry_50_high = asym_results.get(trials-trials/4);
		
		this.point_stats.add(new String[]{"asymmetry","90% upper bound",""+asymmetry_90_high});
		this.point_stats.add(new String[]{"asymmetry","50% upper bound",""+asymmetry_50_high});
		this.point_stats.add(new String[]{"asymmetry","median",""+asymmetry_median});
		this.point_stats.add(new String[]{"asymmetry","mean",""+expected_asymmetry});
		this.point_stats.add(new String[]{"asymmetry","50% lower bound",""+asymmetry_50_low});
		this.point_stats.add(new String[]{"asymmetry","90% lower bound",""+asymmetry_90_low});
		this.point_stats.add(new String[]{"asymmetry","mean absolute deviation",""+mad});
		this.point_stats.add(new String[]{"asymmetry","standard deviation",""+Math.sqrt(var)});
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
		double threshold = (double)(seat_probs.length)/2.0;
		double rep_majority = 0;
		double dem_majority = 0;
		for(int i = 0; i < seat_probs.length; i++) {
			System.out.println(i+" seats: "+seat_probs[i]);
			seat_expectation += seat_probs[i]*(double)i;
			if( (double)i > threshold) {
				rep_majority += seat_probs[i];
			} else {
				dem_majority += seat_probs[i];
			}
		}
		System.out.println("Expectation: "+seat_expectation);
		System.out.println("Dem majority: "+dem_majority);
		System.out.println("Rep majority: "+rep_majority);
		this.point_stats.add(new String[]{"seats","expected Democratic seats",""+((double)this.num_districts-seat_expectation)});
		this.point_stats.add(new String[]{"seats","expected Republican seats",""+seat_expectation});
		this.point_stats.add(new String[]{"seats","chance of Democratic majority",""+dem_majority});
		this.point_stats.add(new String[]{"seats","chance of Republican majority",""+rep_majority});
		
		//System.exit(0);
	}
	public void showBetas() {
		FrameDraw fd = new FrameDraw();
		fd.dist = election_betas;
		fd.dists = district_betas;
		System.out.println("beta params:");
		double total = 0;
		boolean nanfound = false;
		for( int i = 0; i < centered_district_betas.size(); i++) {
			BetaDistribution b = centered_district_betas.get(i);
			//avg_log_likelihood(double[] xs, double a, double b)
			System.out.println((i+1)+" alpha: "+b.getAlpha()+", beta: "+b.getBeta()+", ll: "+b.loglikelihood);
			if( b.loglikelihood == b.loglikelihood) {
				total += b.loglikelihood;
			} else {
				nanfound = true;
			}
			
		}
		System.out.println("total ll: "+total);
		if( nanfound) {
			System.out.println("nan found!");
			System.exit(0);
		}
		
		saveToFile(fd.panel,this.output_folder+"betas.png",500,500);
		//System.exit(0);
		if( show) {
			fd.show();
			fd.repaint();
		}
	}
	public void showSeats() {
		
		FrameDraw fd2 = new FrameDraw();
		fd2.seats = seat_probs;

		saveToFile(fd2.panel,this.output_folder+"seats.png",500,500);

		if( show) {
			fd2.show();
			fd2.repaint();
		}		
	}
	public double[][] getAnOutcome() {
		double[][] ddd = new double[num_districts][];
		double demt = 0;
		double rept = 0;
		for( int i = 0; i < num_districts; i++) {
			ddd[i] = new double[2];
			boolean pass = false;
			double turnout = 0;
			double pct = 0;
			while (!pass) {
				try {
					turnout = district_gammas.get(i).inverseCumulativeProbability(Math.random());
					pct = district_betas.get(i).inverseCumulativeProbability(Math.random());
				} catch (Exception ex) {
					pass = false;
				}
				pass = true;
			}
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
		if( use_binomials) {
			for( int i = 0; i < num_districts; i++) {
				int dturnout = (int)(ddd[i][1]+ddd[i][0]);
				double dpct = ddd[i][0]/(ddd[i][1]+ddd[i][0]);
				BinomialDistribution b = new BinomialDistribution(dturnout,dpct); 
				int dem = b.inverseCumulativeProbability(Math.random());
				int rep = dturnout - dem;
				ddd[i][0] = dem;
				ddd[i][1] = rep;
			}			
		}
		return ddd;
	}
	public double[][] getAnOutcomeBetasOnly() {
		double[][] ddd = new double[num_districts][];
		double demt = 0;
		double rept = 0;
		for( int i = 0; i < num_districts; i++) {
			ddd[i] = new double[2];
			boolean pass = false;
			double pct = 0;
			while (!pass) {
				try {
					pct = district_betas.get(i).inverseCumulativeProbability(Math.random());
				} catch (Exception ex) {
					pass = false;
				}
				pass = true;
			}
			demt += (ddd[i][0] = pct);
			rept += (ddd[i][1] = (1-pct));
		}
		double pct = election_betas.inverseCumulativeProbability(Math.random());
		double mult_dem = pct/demt; 
		double mult_rep = (1-pct)/rept; 
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
		double delta = 1.0/(double)election_samples.length;
		double[] seatProbs = new double[district_betas.size()+1];
		for( int i = 0; i < election_samples.length; i++) {
			double[][] result = election_samples[i];
			int seats = 0;
			for(int j = 0; j < num_districts; j++) {
				if( result[j][0] > result[j][1]) {
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
		double[] election_pcts = new double[elections_district_rep_counts.length+(for_national ? 3 : 0)];
		for(int i = 0; i < elections_district_rep_counts.length; i++) {
			double d = dem_pop[i];
			double r = rep_pop[i];
			election_pcts[i] = d/(d+r);
		}
		if( for_national) {
			election_pcts[3] = 50999897.0/(50999897.0 + 50456002.0); //2000 election
			election_pcts[4] = 65853514.0/(65853514.0 + 62984828.0); //2016 election
			election_pcts[5] = 47401185.0/(47401185.0 + 39197469.0); //1996 election
			
			// 47401185
			// 39197469 
			if( for_national_center) {
				double d = 0;
				for( int i = 0; i < 6; i++) {
					d += election_pcts[i];
				}
				d /= 6.0;
				for( int i = 0; i < 6; i++) {
					election_pcts[i] *= 0.5/d;
				}
			}
		}
				/*
				 * 
				// 47401185
				// 39197469 
				 
				00: presidential
				dem: 50999897
				rep: 50456002

				16: presidential
				dem: 65853514
				rep: 62984828
				*/

		bds.insertElementAt(new BetaDistribution(election_pcts,force_centered_popular_vote),0);
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
	public Vector<Double> computeDisproportionalityStats() {
		int trials = election_samples.length;
		Vector<Double> results = new Vector<Double>();
		double expected_mean = 0;
		double expected_abs = 0;
		double dem_adv = 0;
		double rep_adv = 0;
		for(int i = 0; i < trials; i++) {
			double d = computeDisproportionality(this.election_samples[i]) * (double)num_districts;
            results.add(d);
			expected_mean += d;
			expected_abs += Math.abs(d);
			dem_adv += (d < 0) ? 1 : 0;
			rep_adv += (d > 0) ? 1 : 0;
		}
		expected_mean /= trials;
		expected_abs /= trials;
		dem_adv /= trials;
		rep_adv /= trials;
		//expected_mean *= (double)num_districts;
		//expected_abs *= (double)num_districts;
		Collections.sort(results);
		point_stats.add(new String[]{"disproportionality","90% upper bound",""+results.get(trials-trials/20)});
		point_stats.add(new String[]{"disproportionality","50% upper bound",""+results.get(trials-trials/4)});
		point_stats.add(new String[]{"disproportionality","mean",""+expected_mean});
		point_stats.add(new String[]{"disproportionality","50% lower bound",""+results.get(trials/4)});
		point_stats.add(new String[]{"disproportionality","90% lower bound",""+results.get(trials-trials/20)});
		point_stats.add(new String[]{"disproportionality","chance of Democratic advantage",""+dem_adv});
		point_stats.add(new String[]{"disproportionality","chance of Republican advantage",""+rep_adv});
		System.out.println("Excepted absolute disproportionality: "+expected_abs+" seats");
		System.out.println("Excepted signed disproportionality: "+expected_mean+(expected_mean < 0 ? " (dem seat advantage)" : " (rep seat advantage)"));
		System.out.println("50% chance of being between: "+results.get(trials/4)+" and "+results.get(trials-trials/4)+" seats");
		System.out.println("90% chance of being between: "+results.get(trials/20)+" and "+results.get(trials-trials/20)+" seats");
		System.out.println("Chance of dem advantage: "+dem_adv);
		System.out.println("Chance of rep advantage: "+rep_adv);
		for(int i = 0; i < trials; i+= trials/10) {
			System.out.println(i+": "+results.get(i));
		}
		System.out.println((trials-1)+": "+results.get(trials-1));
		return results;
		//binAndShow(results);
	}
	public double computeDisproportionality(double[][] dd) {
		//double[][] dd = betasOnly ? getAnOutcomeBetasOnly() : getAnOutcome();
		double demseats = 0;
		double repseats = 0;
		double demvotes = 0;
		double repvotes = 0;
		for( int i = 0; i < num_districts; i++) {
			demvotes += dd[i][0];
			repvotes += dd[i][1];
			demseats += dd[i][0] > dd[i][1] ? 1 : dd[i][1] == 0 ? 0.5 : 0;
			repseats += dd[i][0] < dd[i][1] ? 1 : dd[i][1] == 0 ? 0.5 : 0;
		}
		double pctvotes = repvotes / (demvotes+repvotes);
		double pctseats = repseats / (double)num_districts;
		return pctseats - pctvotes;
	}

	public double calculate_asymmetry(double[][] dd) {
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
		return (repseats-demseats)/(double)num_districts;
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
		return num_dem_seats /(double)sorted_dists.size();

	}
	public void binAndShow(Vector<Double> samples, String name) {
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
		if( bins.size() < 20) {
			fd.interpolate = false;
		}
		//fd.interpolate
		fd.bins = bins;
		
		saveToFile(fd.panel,this.output_folder+name+".png",500,500);

		if( show) {
			fd.show();
			fd.repaint();
		}
	}
	public void showAsymmetry() {
		//now do asymmetry
		binAndShow(asym_results,"partisan_asymmetry");
		
	}
	public double[][] showHeatMap() {
		return showHeatMap(200,200);
	}
	public double[][] showHeatMap(int x,int y) {
		double inc = 1.0/(double)trials;
		double[][] dd0 = getAnOutcome();
		//if( y > dd0.length/2) {
			y = dd0.length;
		//}
		double[][] hm = new double[x][y];
		//double inc = 1;//(double)(x*y)/(double)(trials*10);
		double max = 0;
		double[] seat_probs = new double[y+1];
		double seats_dem = 0;
		double seats_rep = 0;
		for( int i = 0; i < election_samples.length; i++) {
			double[][] dd = election_samples[i];
			double[] tallied = tallyVotes(dd);
			double pop_d = tallied[0];
			double pop_r = tallied[1];
			double seats_d = tallied[2];
			double seats_r = tallied[3];
			double tot_pop = pop_d+pop_r;
			pop_d /= tot_pop;
			pop_r /= tot_pop;
			//seats_d /= (double)dd.length;
			//seats_r /= (double)dd.length;
			int dx = (int)(Math.floor(pop_r*(double)(x)));
			int dy = (int)seats_r;//(int)(Math.floor(seats_d*(double)(y)));
			seat_probs[dy] += inc;
			if( (double)dy > ((double)y/2.0) ) {
				seats_rep += inc;
			}
			if( (double)dy < ((double)y/2.0) ) {
				seats_dem += inc;
			}
			dx = dx < 0 ? 0 : dx >= x ? x-1 : dx;
			dy = dy < 0 ? 0 : dy >= y ? y-1 : dy;
			hm[dx][dy] += inc;
			if( hm[dx][dy] > max) {
				max = hm[dx][dy];
			}
		}
		for( int i = 0; i < x; i++) {
			for( int j = 0; j < y; j++) {
				hm[i][j] /= max;
			}
		}
		for( int i = 0; i < seat_probs.length; i++) {
			System.out.println(i+": "+seat_probs[i]);
		}
		System.out.println("dem majority likelihood: "+seats_dem);
		System.out.println("rep majority likelihood: "+seats_rep);
		FrameHeatMap fd = new FrameHeatMap();
		fd.hm = hm;
		
		saveToFile(fd.panel,this.output_folder+"seats-votes.png",640,640);
		
		if( show) {
			fd.show();
			fd.repaint();
		}

		return hm;
	}
	public void saveToFile(JComponent component, String filename, int w, int h) {
		File f = new File(filename);
		BufferedImage image4 = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics4 = image4.createGraphics(); 
        graphics4.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics4.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics4.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics4.setComposite(AlphaComposite.Clear);
        graphics4.fillRect(0, 0, w, h);
        graphics4.setComposite(AlphaComposite.Src);
        component.paint(graphics4);

        try {
			ImageIO.write(image4,"png", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public double[][] showDisproportionHeatMap(int x,int y) {
		double inc = 1.0/(double)trials;
		double[][] dd0 = getAnOutcome();
		//if( y > dd0.length/2) {
			y = dd0.length;
		//}
		double[][] hm = new double[x][y];
		//double inc = 1;//(double)(x*y)/(double)(trials*10);
		double max = 0;
		double[] seat_probs = new double[y+1];
		double seats_dem = 0;
		double seats_rep = 0;
		for( int i = 0; i < election_samples.length; i++) {
			double[][] dd = election_samples[i];
			double[] tallied = tallyVotes(dd);
			double pop_d = tallied[0];
			double pop_r = tallied[1];
			double seats_d = tallied[2];
			double seats_r = tallied[3];
			double tot_pop = pop_d+pop_r;
			pop_d /= tot_pop;
			pop_r /= tot_pop;
			//seats_d /= (double)dd.length;
			//seats_r /= (double)dd.length;
			
			pop_r = (seats_r /(double)dd.length) - pop_r + 0.5;
			
			int dx = (int)(Math.floor(pop_r*(double)(x)));
			int dy = (int)seats_r;//(int)(Math.floor(seats_d*(double)(y)));
			seat_probs[dy] += inc;
			if( (double)dy > ((double)y/2.0) ) {
				seats_rep += inc;
			}
			if( (double)dy < ((double)y/2.0) ) {
				seats_dem += inc;
			}
			dx = dx < 0 ? 0 : dx >= x ? x-1 : dx;
			dy = dy < 0 ? 0 : dy >= y ? y-1 : dy;
			hm[dx][dy] += inc;
			if( hm[dx][dy] > max) {
				max = hm[dx][dy];
			}
		}
		for( int i = 0; i < x; i++) {
			for( int j = 0; j < y; j++) {
				hm[i][j] /= max;
			}
		}
		for( int i = 0; i < seat_probs.length; i++) {
			System.out.println(i+": "+seat_probs[i]);
		}
		System.out.println("dem majority likelihood: "+seats_dem);
		System.out.println("rep majority likelihood: "+seats_rep);
		FrameHeatMap fd = new FrameHeatMap();
		fd.hm = hm;

		saveToFile(fd.panel,this.output_folder+"seats-disproportion.png",640,640);

		if( show) {
			fd.show();
			fd.repaint();
		}

		return hm;
	}
	private double[] tallyVotes(double[][] dd) {
		double pop_d = 0;
		double pop_r = 0;
		double seats_d = 0;
		double seats_r = 0;
		for( int j = 0; j < dd.length; j++) {
			pop_d += dd[j][0];
			pop_r += dd[j][1];
			if( seat_counts == null || seat_counts[j] == 1) {
				if( dd[j][0] > dd[j][1]) {
					seats_d++;
				} else {
					seats_r++;
				}
			} else {
				double tot_pop = dd[j][0] + dd[j][1]; 
				double pct_dem = dd[j][0] / tot_pop;
				int seats_dem = (int)Math.round(pct_dem*(double)seat_counts[j]);
				int seats_rep = seat_counts[j] - seats_dem;
				seats_d += seats_dem;
				seats_r += seats_rep;
			}
		}
		double [] ret = new double[]{pop_d,pop_r,seats_d,seats_r};
		return ret;
	}
	public void showPacking() {
		Vector<Double> packing = new Vector<Double>();
		for( int i = 0; i < election_samples.length; i++) {
			double[][] o = election_samples[i];
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
		binAndShow(packing,"packing");
		
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
				d = 0.5 - Math.round(d*res)/res;
				vd.add(d);
			}
		}
		binAndShow(vd,"histogram");
	}
	
	public void showBetaParameters() {
		System.out.println("{");
		System.out.println("\tpopular_vote: { alpha: "+election_betas.getAlpha()+", beta: "+election_betas.getBeta()+", log-likelihood: "+election_betas.loglikelihood+" },");
		System.out.println("\tdistrict_vote: ["); 
		for( int i = 0; i < district_betas.size(); i++) {
			System.out.println("\t\t{ district: "+(i+1)+", alpha: "+district_betas.get(i).getAlpha()+", beta: "+district_betas.get(i).getBeta()+", log-likelihood: "+district_betas.get(i).loglikelihood+" },");
		}
		System.out.println("\t],"); 
		System.out.println("\tcentered_district_vote: ["); 
		for( int i = 0; i < centered_district_betas.size(); i++) {
			System.out.println("\t\t{ district: "+(i+1)+", alpha: "+centered_district_betas.get(i).getAlpha()+", beta: "+centered_district_betas.get(i).getBeta()+", log-likelihood: "+centered_district_betas.get(i).loglikelihood+" },");
		}
		System.out.println("\t]"); 
		System.out.println("}");
	}
	public void showSeatsVotes() {
		FrameDrawSeatsVotes sv = new FrameDrawSeatsVotes();
		sv.dist = election_betas;
		sv.dists = district_betas;
		if( show) {
			sv.show();
			sv.repaint();
		}
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
