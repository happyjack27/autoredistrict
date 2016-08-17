package stats;

import solutions.*;
import geography.*;

import java.util.*;

public class StatSpeech extends Stat {
	
	public double[][] getScoresByDistrict(DistrictMap dm, FeatureCollection fc) {
		return null;//transpose(get_stats(Vector<double[][]> elections, int num_districts, double mean_regression) );
	}
	public double[][] getScoresByParty(DistrictMap dm, FeatureCollection fc) { 
		/*
		double[] dd = get_speech(get_stats(Vector<double[][]> elections, int num_districts, double mean_regression) );
		return new double[][]{
				new double[]{dd[0]/dd[2],dd[0],dd[2]},
				new double[]{dd[1]/dd[3],dd[1],dd[3]},
		}; 
		*/
		return null;
	}
	public double[][] transpose(double[][] t) {
		double[][] r = new double[t[0].length][t.length];
		
		for( int i = 0; i < r.length; i++) {
			for( int j = 0; j < r[i].length; j++) {
				r[i][j] = t[j][i];
			}
			
		}
		
		return r;
	}

	
	public static double pdf(double mu,double sigma) {
		double exp = mu/sigma;
		exp = -exp*exp/2;
		return Math.exp(exp)/Math.sqrt(2*Math.PI*sigma*sigma);
	}
	public static double[][] get_stats(Vector<double[][]> elections, int num_districts, double mean_regression) {
		
		double avgsigma = 0;
		double[] mu = new double[num_districts];
		double[] sigma = new double[num_districts];
		double[] party0 = new double[num_districts];
		double[] party1 = new double[num_districts];
		double[] p = new double[num_districts];
	
		return new double[][]{
			mu,sigma,p,party0,party1	
		};
		
	}
	
	public static double[] get_speech(double[][] ddd) {
		int num_districts = ddd[0].length;

		double[] mu = ddd[0];
		double[] sigma = ddd[1];
		double[] p = ddd[2];
		double[] party0 = ddd[3];
		double[] party1 = ddd[4];
		

		double speech0 = 0, speech1 = 0;
		double sum0 = 0, sum1 = 0;
		for(int j = 0; j < num_districts; j++) {
			speech0 += p[j]*party0[j];
			speech1 += p[j]*party1[j];

			sum0 += party0[j];
			sum1 += party1[j];
		}
		
		//speech0 /= sum0;
		//speech1 /= sum1;
	
		return new double[]{
				speech0, speech1, sum0, sum1
		};
	}

}

