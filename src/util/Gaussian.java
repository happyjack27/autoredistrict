package util;

import java.util.Vector;


public class Gaussian {
	static boolean square_sigma = false;
	static boolean square_root_sigma = true;
	/*
	public double[] addGuassians() {
		
	}
	*/
	static double[] get_mu_and_sigma_and_n(double[] events, double[] successes) {
		double tn = 0;
		double tmu = 0;
		double tsigma = 0;
		for( int i = 0; i < events.length; i++) {
			double n = events[i];
			double p = (double)successes[i] / (double)events[i];
			double mu = n*p;
			double sigma = n*p*(1-p);
			tn += n;
			tmu += mu;
			tsigma += sigma;
			
		}
		return new double[]{tmu,tsigma,tn};
	}
	/*
	public static double[] getOddsFromwards(Vector<ward> wards) {
        double[] events = new double[wards.size()];
        double[][] successes = new double[Candidate.candidates.size()][wards.size()];
        double[] odds = new double[Candidate.candidates.size()];
        for( int i = 0; i < wards.size(); i++) {
        	ward ward = wards.get(i);
            for( Demographic d : ward.demographics) {
                for( int j = 0; j < d.vote_prob.length; j++) {
                	double n = d.population * d.vote_prob[j]*d.turnout_probability;
                	events[i] += n;
                	successes[j][i] += n;
                }
            }
        }
        for( int j = 0; j < odds.length; j++) {
        	odds[j] = getProbForMuSigmaN(get_mu_and_sigma_and_n(events,successes[j]));
        }
        return odds;
	}*/
	public static double getProbForMuSigmaN(double mu, double sigma, double n) {
		double z = n/2.0;
		
		if( square_root_sigma) {
			sigma = Math.sqrt(sigma);
		}
		double ret = 1.0-Phi(z,mu,sigma);
		return ret;
	}

	public static double getProbForMuSigmaN(double[] musigman) {
		double mu = musigman[0];
		double sigma = musigman[1];
		double n = musigman[2];
		double z = n/2.0;
		
		if( square_root_sigma) {
			sigma = Math.sqrt(sigma);
		}
		double ret = 1.0-Phi(z,mu,sigma);
		return ret;
	}
	
	
	public static double binomial_as_normal(double n, double k, double p) {
		double mu = n*p;
		double sigma = n*p*(1-p);
		double z = k;
		if( square_sigma) {
			sigma *= sigma;
		}
		if( square_root_sigma) {
			sigma = Math.sqrt(sigma);
		}
		double ret = 1.0-Phi(z,mu,sigma);
		System.out.println("binomial_as_normal n:"+n+" k:"+k+" p:"+p+" ret:"+ret);
		return ret;
				
	}

    // return phi(x) = standard Gaussian pdf
    public static double phi(double x) {
        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
    }

    // return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    public static double phi(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    // return Phi(z) = standard Gaussian cdf using Taylor approximation
    public static double Phi(double z) {
        if (z < -8.0) return 0.0;
        if (z >  8.0) return 1.0;
        if( z != z) {
        	return 1.0;
        }
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;
            /*if( i > 1000) {
            	System.out.println("exceeded 1000 "+z);
            	break;
            }*/
        }
        return 0.5 + sum * phi(z);
    }

    // return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    public static double Phi(double z, double mu, double sigma) {
    	if( sigma != sigma || sigma == 0 || mu != mu || z != z) {
    		System.out.println("bad guassian params "+z+" "+mu+" "+sigma);
    	}
        return Phi((z - mu) / sigma);
    } 

    // Compute z such that Phi(z) = y via bisection search
    public static double PhiInverse(double y) {
        return PhiInverse(y, .00000001, -8, 8);
    } 

    // bisection search
    private static double PhiInverse(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (Phi(mid) > y) return PhiInverse(y, delta, lo, mid);
        else              return PhiInverse(y, delta, mid, hi);
    }



    // test client
    public static void main(String[] args) {
        double z     = Double.parseDouble(args[0]);
        double mu    = Double.parseDouble(args[1]);
        double sigma = Double.parseDouble(args[2]);
        //StdOut.println(Phi(z, mu, sigma));
        double y = Phi(z);
        //StdOut.println(PhiInverse(y));
    }

}