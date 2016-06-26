package util.ballot_counters;

import java.io.*;
import java.util.*;

import util.Pair;

public class counter {
	public static iCountingSystem[] systems = new iCountingSystem[]{
		new CS_FPTP_AnalogAllocation(),
		new CS_FPTP_DigitalAllocation(),
		new CS_FPTP_Approval(),
		new CS_FPTP_NVotes(),
	};
	
	public static void main(String[] args) {
		Vector<MultiBallot> multi_ballots = new counter().getMultiBallots(readDelimited(new File(""), ",", "\n"), 1);
		
		for(int i = 0; i < systems.length; i++) {
			System.out.print(systems[i].getName()+": ");
			int[] winners = systems[i].getWinners( multi_ballots, 1);
			for( int j = 0; j < winners.length; j++) {
				System.out.print(winners[j]+",");
			}
			System.out.println();
		}
		
	}
	public static Vector<double[]> addNoise(double[] dd, int samples, int ballots) {
		double inc = 1.0/(double)samples;
		Vector<double[]> vdd = new Vector<double[]>();
		for(int i = 0; i < ballots; i++) {
			double[] de = new double[dd.length];
			for(int j = 0; j < samples; j++) {
				double r = Math.random();
				for(int k = 0; k < dd.length; k++) {
					r -= dd[k];
					if( r < 0) {
						de[k] += inc;
						break;
					}
				}		
			}
			vdd.add(de);
		}
		return vdd;
	}

	
	public Vector<MultiBallot> getMultiBallots(Vector<String[]> vs,int clones_per_party) {
		Vector<double[]> vd = new Vector<double[]>();
		for( int i = 0; i < vs.size(); i++) {
			String[] ss = vs.get(i);
			double[] dd = new double[ss.length*clones_per_party];
			for( int j = 0; j < ss.length; j++) {
				double d = Double.parseDouble(ss[j]);
				for( int k = 0; k < clones_per_party; k++) {
					dd[j*clones_per_party+k] = d/(double)clones_per_party;
				}
			}
			vd.add(dd);
		}
		
		int num_votes = 5;
		int num_allocs = 5;
		Vector<MultiBallot> mbs = new Vector<MultiBallot>();
		
		for( int i = 0; i < vd.size(); i++) {
			double[] dd = vd.get(i);
			MultiBallot multi_ballot = new MultiBallot();
			
			multi_ballot.analog_allocation = dd;
			
			Vector<Pair<Double,Integer>> vp = new Vector<Pair<Double,Integer>>();
			for( int j = 0; j < dd.length; j++) {
				vp.add( new Pair<Double,Integer>(dd[j],j));
			}
			Collections.sort(vp);

			int[] ranked = new int[dd.length];
			for( int j = 0; j < dd.length; j++) {
				ranked[j] = vp.get(j).b;
			}
			multi_ballot.ranked_choice = ranked;
			
			int[] votes = new int[dd.length];
			for( int j = 0; j < num_votes; j++) {
				votes[vp.get(j).b] = 1;
			}
			multi_ballot.n_votes = votes;
			
			int[] approvals = new int[dd.length];
			double threshold = 0.5*(double)dd.length;
			for( int j = 0; j < dd.length; j++) {
				approvals[j] = dd[j] > threshold ? 1 : 0;
			}
			multi_ballot.approval = approvals;
			
			
			int[] da = new int[dd.length];
			double[] dd_res = new double[dd.length];
			int tot = 0;
			for( int j = 0; j < dd.length; j++) {
				da[j] = (int)Math.floor(dd[j]*(double)num_allocs);
				dd_res[j] = dd[j]*(double)num_allocs - (double)da[j];
				tot += da[j];
			}
			while( tot < num_allocs) {
				int m_i = -1;
				double m = -10;
				for( int j = 0; j < dd.length; j++) {
					if( dd_res[j] > m) {
						m = dd_res[j];
						m_i = j;
					}
					dd_res[m_i]--;
					da[m_i]++;
					tot++;
				}
			}
			multi_ballot.digital_allocation = da;
			mbs.add(multi_ballot);
		}
		
		return mbs;
	}
	
	public static Vector<String[]> readDelimited(File f, String cell, String line) {
		StringBuffer sb = new StringBuffer(); 
		Vector<String[]> v = new Vector<String[]>();
		try {
			FileInputStream fis = new FileInputStream(f);
			while( fis.available() > 0) {
				byte[] bb = new byte[fis.available()];
				fis.read(bb);
				sb.append( new String(bb));
				Thread.sleep(10);
			}
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String s = sb.toString();
		String[] sss = s.split(line);
		for( int i = 0; i < sss.length; i++) {
			String[] ss = sss[i].split(cell);
			v.add(ss);
		}
		
		return v;
	}

}
