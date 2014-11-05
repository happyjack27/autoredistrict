package Options;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.net.URL;
import java.util.*;
import java.text.*;

import StatAnalysis.MultiLinearSolver2;
import Utils.*;
import Settings.*;

/*
 * need to reduce parameters in expand_quadratic.
 * try removing one at a time, remove the one that creates the least total error?
 * 
 * or do additive?
 * 
 * save html to disk?
 * 
 * 
 */

/*
 * normalize before solving
 * calculate bid and ask separately.
 *
 *
 *
 */

/*
 * must make options info download current price when it gets option info
 * check to make sure i did the linear regression correctly.
 *
 */
/*
 * do a linear regression of bid from ask based on distance from underlying price.
 * normalize to underlying.
 *
 */

/*
 * calculate time skew by taking this month and next month offset,
 * then dividing by difference in yte., then recalculating offset.
 *
 * optionsdata of second month using today's YTE!
 *
 * on opintsoinfor calculateor, get matrix - logmoneyness, yte, something, volatility
 *
 * then calculate quad regression from them for vol and spread.
 *
 *
 * solving needs to use t, and t^(-1/2) or t^(1/2)  - or t^-x (expontial
 *
 */

public class OptionsInfo {
	public static boolean skip_front_month = false;
	public static boolean use_cached_options_data = false;
	//bid price can have neg vol, ask can't.
	static boolean crop_neg_vol = true;
	
	static int min_expiries = 3;
	static boolean usecache = true;
	static Hashtable<String, double[]> cache = new Hashtable<String, double[]>();
	static boolean use_old_method = false;
	public static int max_year_look_ahead = 5;
	public static int min_samples = 20;

	public static void main(String[] args) {
		String ticker = "AAPL";
		
		double d = getLogError("/stochpredictor/", ticker);
		System.out.println(" log error:"+d);
		System.exit(0);
		double[] dd = getOptionsInfo(ticker);
		/*
		Date d = OptionCalendar.adjustTime(d);
		int year = (int) dd[1];
		int month = (int) (dd[2] + 1);
		double yte = OptionCalendar.getYearsToExpiryMonth(d, year, month);
		*/
		Date id = new Date();
		int iyear = id.getYear() + 1900;
		int max_year = iyear + max_year_look_ahead;
		int imonth = id.getMonth()+1;
		if( id.getDate() > 15 && skip_front_month) { //too close to expiration, skipping front month.
			System.out.println("incrementing month! "+imonth);
			imonth++;
			if( imonth > 12) {
				iyear++;
				imonth -= 12;
			}
		}		
		
		double underlying = 134.61;//0.32;

		for (int i = 0; i < dd.length; i++)
			System.out.print("" + dd[i] + ", ");
		System.out.println();
		//Date last = OptionCalendar.getLastMarketDay(new Date());
		//System.out.println(" last: " + last);
		//double yte = OptionCalendar.getYearsToNextExpiry(last, 0);
		double yte = OptionCalendar.getYearsToExpiryMonth(new Date(), iyear, imonth);
		double bid, ask;
		System.out.println("first "+yte);
		System.out.println("month "+imonth+"  year "+iyear+"  idate "+id.getDate());
		for (int i = 100; i < 150; i += 5) {
			bid = getPrice(i, underlying, yte, dd, -1);
			ask = getPrice(i, underlying, yte, dd, 1);
			System.out.println("strike: " + i + " bid: " + bid + " ask: " + ask);
		}
		double[][] dd2 = getOptionsInfoV2(ticker, 0.50, 4);
		for (int i = 0; i < dd.length; i++)
			System.out.print("" + dd[i] + ", ");
		System.out.println();
		System.out.println("v2");
		for (int i = 100; i < 150; i += 5) {
			bid = getPrice2(i, underlying, yte, dd2, -1);
			ask = getPrice2(i, underlying, yte, dd2, 1);
			System.out.println("strike: " + i + " bid: " + bid + " ask: " + ask);
		}
	}
	public static void deleteOptFiles(String ticker_folder,String ticker) {
		File dir = new File(ticker_folder+ticker);

		  String[] chld = dir.list();
		  if(chld == null)
			  return ;
		  for( int i = 0; i < chld.length; i++) {
			  if( chld[i].length() < 5)
				  continue;
			  if( chld[i].substring(0,4).equals("opt_")) {
				  File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+chld[i]);
				  if( f != null && f.exists())
					  f.delete();
			  }
		  }
	}
	public static Vector<String> getOptExiriesFromCache(String ticker_folder,String ticker) {
		 Vector<String> ret = new  Vector<String>();
		
		File dir = new File(ticker_folder+ticker);

		  String[] chld = dir.list();
		  if(chld == null)
			  return ret;
		  for( int i = 0; i < chld.length; i++) {
			  if( chld[i].substring(0,4).equals("opt_")) {
				  String year = chld[i].substring(4,8);
				  String month = chld[i].substring(9);
				  month = month.substring(0,month.indexOf("."));
				  ret.add(year+"-"+month);
			  }
		  }
		  return ret;
	}
	/*
	 * 			data[0] = new double[] { undprice, expiry.getYear() + 1900, expiry.getMonth() };
			for (int i = 1; i < data.length; i++) {
				String[] ss = lines[i + 4 - 1].split(", ");
				/*
				for( int j = 0; j < ss.length; j++) {
					System.out.print(j+":"+ss[j]+"  ");
				}
				System.out.println();
				System.exit(0);
				
				data[i] = new double[] { ss[1].equals("N/A") ? 0 : Double.parseDouble(ss[1]), // strike
						ss[3].equals("N/A") ? 0 : Double.parseDouble(ss[3]), // last
						ss[4].equals("N/A") ? 0 : Double.parseDouble(ss[4]), // chg
						ss[5].equals("N/A") ? 0 : Double.parseDouble(ss[5]), // bid
						ss[6].equals("N/A") ? 0 : Double.parseDouble(ss[6]), // ask
						ss[7].equals("N/A") ? 0 : Double.parseDouble(ss[7].replaceAll(",", "")), // vol
						ss[8].equals("N/A") ? 0 : Double.parseDouble(ss[8].replaceAll(",", "")), // open
																		// interest
				};
	 */
	public static double getLogError(String ticker_folder,String ticker) {
		//use_cached_options_data = true;
		//			double[][] ret = new double[][] { new double[] { info[0], info[1] }, voldata, pricedata, spreaddata, mvoldata,avoldata };
		//double[] optionsInfo = getOptionsInfo(ticker_folder,ticker);
		double[][] optionsInfo2 = getOptionsInfoV2(ticker_folder,ticker,1,3);
		if( optionsInfo2 == null)
			return 0;
		double undprice = optionsInfo2[0][0];
		double total_error = 0;
		double total_strikes = 0;
		File dir = new File(ticker_folder+ticker);

		  Date d = new Date();
		  String[] chld = dir.list();
		  if(chld == null)
			  return total_error;
		  for( int i = 0; i < chld.length; i++) {
			  if( chld[i].length() < 5)
				  continue;
			  if( chld[i].substring(0,4).equals("opt_")) {
				  hkjh
				  String year = chld[i].substring(4,8);
				  String month = chld[i].substring(9);
				  if( month.indexOf(".") > 0) {
					  month = month.substring(0,month.indexOf("."));
				  }
				  File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+chld[i]);
				  double[][] actuals = getOptionsData(ticker_folder,ticker, year + "-" + month);
					//optionsdata2[i] = getOptionsData(ticker_folder,ticker, year + "-" + month);
				  if( actuals == null)
					  continue;
					d.setTime((long)(actuals[0][3]));
					double underlying = actuals[0][0];
					double yte = OptionCalendar.getYearsToExpiryMonth(d, Integer.parseInt(year), Integer.parseInt(month));
				  for( int j = 1; j < actuals.length; j++) {
					  double[] row = actuals[j];
					  double strike = row[0];
						double moneyness = Math.log(strike / underlying);
						double bidvol = apply_quadratic(new double[] { moneyness, yte }, optionsInfo2[1]);
						double midvol = apply_quadratic(new double[] { moneyness, yte }, optionsInfo2[4]);
						double askvol = apply_quadratic(new double[] { moneyness, yte }, optionsInfo2[5]);
						double est_bid = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, bidvol, yte);
						double est_mid = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, midvol, yte);
						double est_ask = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, askvol, yte);
					  //double est_bid = getPrice2(row[0],underlying,yte,optionsInfo2,-1);
					  double act_bid = row[3];
					  double act_ask = row[4];
					  double act_mid = (row[3]+row[4])/2;
					  if( est_bid <= 0.02)
						  est_bid = 0.02;
					  if( act_bid <= 0.02)
						  act_bid = 0.02;
					  if( est_ask <= 0.02)
						  est_ask = 0.02;
					  if( act_ask <= 0.02)
						  act_ask = 0.02;
					  if( est_mid <= 0.02)
						  est_mid = 0.02;
					  if( act_mid <= 0.02)
						  act_mid = 0.02;
					  total_error += Math.abs(Math.log(act_bid)-Math.log(est_bid));
					  total_error += Math.abs(Math.log(act_mid)-Math.log(est_mid));
					  total_error += Math.abs(Math.log(act_ask)-Math.log(est_ask));

					  total_strikes += 1;
					  
					  //0 = strike, 3 = bid, 4 = ask
					  //	public static double getPrice2(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask, boolean round, double vol_correction) {

				  }
			  }
		  }
		  return total_error/total_strikes;
		//optionsdata2[i] = getOptionsData(ticker_folder,ticker, year + "-" + month);
	}
	public static double GCD(double[] dd) {
		return dd[1] - dd[0] < dd[2] - dd[1] ? dd[1] - dd[0] : dd[2] - dd[1];
	}
	public static boolean isInExpiries(int month, int year, Vector<String> expiries) {
		for( int i = 0; i < expiries.size(); i++) {
			int expyear = Integer.parseInt(expiries.get(i).substring(0,4));
			int expmonth = Integer.parseInt(expiries.get(i).substring(5));
			//System.out.println(" comparing "+year)
			if( year == expyear && month == expmonth)
				return true;
		}
		return false;
	}

	public static double[][] getOptionsInfoV2(String ticker, double moneyness_bounds, int num_samples) {
		return getOptionsInfoV2(null, ticker,  moneyness_bounds,  num_samples);
	}
	//EEE, MMM dd, yyyy, h:mma z
	//need to get <span id="yfs_market_time">Fri, Aug 24, 2012, 7:47PM EDT - U.S. Markets closed</span>
	public static double[][] getOptionsInfoV2(String ticker_folder,String ticker, double moneyness_bounds, int num_samples) {
		try {
			if(!use_cached_options_data && ticker_folder != null) 
				deleteOptFiles(ticker_folder,ticker);

			Vector<String[]> expiries = getOptionExpiries(ticker_folder,ticker);
			if( expiries == null || expiries.size() == 0)
				return null;
			int current_sample = 0;
			int l = 0;

			
			Date d = new Date();
			int max_year = d.getYear() + 1900 + max_year_look_ahead;
			double underlying;
			int year;
			int month;
			double[][][] optionsdata1 = new double[num_samples][][];
			double[][][] optionsdata2 = new double[num_samples][][];
			double[] yte = new double[num_samples];

			//optionsdata1[0] = getOptionsData(0,ticker_folder,ticker);
			for( int i = 0; i < expiries.size() && current_sample < num_samples; i++) {
				String[] expiry_info = expiries.get(i);
				optionsdata1[0] = getOptionsDataFromLink(ticker_folder,ticker, expiry_info[0]);
				if( optionsdata1[0] != null)
					break;
			}
			
			System.out.println("p0");
			//d = optionsdata1[0][0][0];
			underlying = optionsdata1[0][0][0];
			d = OptionCalendar.adjustTime(d);
			Date d2 = new Date();
			year = d2.getYear()+1900;
			month = d2.getMonth()+1; // java months are 0 to 11. fixes
			if( d2.getDate() > 10 && skip_front_month) { //too close to expiration, skipping front month.
				month++;
				if (month > 12) {
					month -= 12;
					year++;
				}
			}

			//year = (int) optionsdata1[0][0][1];
			//month = (int) (optionsdata1[0][0][2]);// + 1);
			//yte[0] = OptionCalendar.getYearsToExpiryMonth(d, year, month);
			double[] info = getOptionsInfo(optionsdata1[0]);
			//optionsdata2[0] = convertStrikePremiumToMoneynessVolatility(optionsdata1[0], underlying, yte[0], moneyness_bounds);

			
			for( int i = 0; i < expiries.size() && current_sample < num_samples; i++) {
				String[] expiry_info = expiries.get(i);
				/*
				for( int j = 0; j < expiry_info.length; j++) {
					System.out.println(j+": "+expiry_info[j]);
				}
				System.out.println("done ab");
				System.exit(0);
				*/
				optionsdata2[current_sample] = getOptionsDataFromLink(ticker_folder,ticker, expiry_info[0]);//year + "-" + month);
				if (optionsdata2[current_sample] == null) {
					continue;
				} else {
					d.setTime((long)(optionsdata2[current_sample][0][3]));
					yte[current_sample] = OptionCalendar.getYTE(d,OptionCalendar.parseExpiryDate(expiry_info[1]));//getYearsToExpiryMonth(d, year, month);
					optionsdata2[current_sample] = convertStrikePremiumToMoneynessVolatility(optionsdata2[current_sample], underlying, yte[current_sample], moneyness_bounds);
					if(optionsdata2[current_sample] == null || optionsdata2[current_sample].length == 0)
						continue;
					l += optionsdata2[current_sample].length;
					i++;
				}
				
				
				current_sample++;
			}
			/*
			month--;
			int l = 0;
			for (int i = 0; i < optionsdata2.length; ) {
				String[] expiry_data = expiries.get(i);
				!!REPLACE THIS TO USE NEW STRING[] EXPIRY METHOD
				month++;
				if (month > 12) {
					month -= 12;
					year++;
					if (year >= max_year)
						break;
				}
				if( !isInExpiries(month,year,expiries))
					continue;
				optionsdata2[i] = getOptionsData(ticker_folder,ticker, year + "-" + month);
				if (optionsdata2[i] == null) {
					continue;
				} else {
					d.setTime((long)(optionsdata2[i][0][3]));
					yte[i] = OptionCalendar.getYearsToExpiryMonth(d, year, month);
					optionsdata2[i] = convertStrikePremiumToMoneynessVolatility(optionsdata2[i], underlying, yte[i], moneyness_bounds);
					if(optionsdata2[i] == null || optionsdata2[i].length == 0)
						continue;
					l += optionsdata2[i].length;
					i++;
				}
			}
			*/
			l = 0;
			int count = 0;
			for (int i = 0; i < optionsdata2.length; i++) {
				if( optionsdata2[i] == null) {
					System.out.println("od2 is null:"+i);
					continue;
				}
				l += optionsdata2[i].length;
				count++;
			}
			if( count < min_expiries)
				return null;
			double[][] solve = new double[l][];
			//double[][] solve2 = new double[l][];
			double[] vol = new double[l];
			double[] mvol = new double[l];
			double[] avol = new double[l];
			double[] spread = new double[l];

			l = 0;
			// System.out.println("od: "+optionsdata.length);
			for (int i = 0; i < optionsdata2.length; i++) {
				if( optionsdata2[i] == null)
					continue;
				// System.out.println("od[i]: "+optionsdata[i].length);
				for (int j = 0; j < optionsdata2[i].length; j++) {
					double[] od = optionsdata2[i][j];
					// System.out.println(od[0]+", "+od[1]+", "+od[2]);
					// System.out.println("od[i][j]: "+optionsdata[i][j].length);
					// System.out.println(i+", "+j+", "+l);
					solve[l] = new double[] { optionsdata2[i][j][0], optionsdata2[i][j][1], };
					vol[l] = optionsdata2[i][j][2];
					spread[l] = optionsdata2[i][j][3];
					mvol[l] = optionsdata2[i][j][4];
					avol[l] = optionsdata2[i][j][5];
					l++;
				}
			}
			//must be an overcomplete system, otherwise the solution is indeterminate.
			if( l < min_samples || l <= expand_quadratic(new double[]{1,1}).length)
				return null;
			//or model bid vs ask?
			double[] voldata = solve_quadratic(solve, vol);
			System.out.println("(volatility)");
			double[] mvoldata = solve_quadratic(solve, mvol);
			System.out.println("(mid vol)");
			double[] avoldata = solve_quadratic(solve, avol);
			System.out.println("(ask vol)");
			double[] spreaddata = solve_quadratic(solve, spread);
			System.out.println("(spread)");
			double[] pp = new double[vol.length];
			for (int i = 0; i < pp.length; i++) {
				double strike = Math.exp(solve[i][0]) * underlying;// =
																	// ln(s/underlying)
				double estvol = apply_quadratic(solve[i], voldata);
				pp[i] = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, vol[i], solve[i][1]) - BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, estvol, solve[i][1]);
				pp[i] *= 100;
			}
			double[] pricedata = solve_quadratic(solve, pp);
			System.out.println("(price)");

			double[][] ret = new double[][] { new double[] { info[0], info[1] }, voldata, pricedata, spreaddata, mvoldata,avoldata };
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ex in get optionsinforv2: "+ex);
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static double[][] convertStrikePremiumToMoneynessVolatility(double[][] data, double underlying, double yte, double bounds) {
		Vector<double[]> v = new Vector<double[]>();
		for (int i = 1; i < data.length; i++) {
			double moneyness = Math.log(data[i][0] / underlying);
			if (moneyness < -bounds || moneyness > bounds)
				continue;
			if (data[i][3] == 0 && i > 2 && data[i - 1][3] == 0)// || data[i][3]
																// > 50)
				break;
			double bidvol = BlackScholes.getVolatility(data[i][0], underlying, BlackScholes.riskfree, data[i][3], yte);
			double midvol = BlackScholes.getVolatility(data[i][0], underlying, BlackScholes.riskfree, (data[i][3] + data[i][4]) / 2, yte);
			double askvol = BlackScholes.getVolatility(data[i][0], underlying, BlackScholes.riskfree, data[i][4], yte);
			v.add(new double[] { moneyness, yte, bidvol, data[i][4] - data[i][3], midvol,askvol});
			// components: 1, moneyness, moneynesssqaured, 1/yte,
			// 1/sqrt(yte),moneyness/sqrt(yte)
			// try guassian distribution function of strike price as part of the
			// function! (need a center and variance, then, or at least a
			// variance( center being 0)

			// want: 1, moneyness, monesssquared, X 1, yte, 1/sqrt(yte)
		}
		double[][] ret = new double[v.size()][];
		for (int i = 0; i < v.size(); i++)
			ret[i] = v.get(i);
		return ret;
	}

	public static double[] getOptionsInfo(String ticker) {
		return getOptionsInfo(null, ticker);
	}
	public static double[] getOptionsInfo(String ticker_folder,String ticker) {
		System.out.println("===get options info " + ticker);
		try {
		if(!use_cached_options_data && ticker_folder != null)
			deleteOptFiles(ticker_folder,ticker);
		if (usecache) {
			double[] test = cache.get(ticker);
			if (test != null) {
				System.out.println("===returning cached! " + ticker);

				return test;
			}
		}
		double[][] optionsdata = null;
		int year = 0;
		int month = 0;
		Date d = null;
		double yte1 = 0, yte2 = 0;
		String nextexpiry = null;
		double[] info = null;
		Date id = new Date();
		int iyear = id.getYear() + 1900;
		int max_year = iyear + max_year_look_ahead;
		int imonth = id.getMonth()+1;
		if( id.getDate() > 10 && skip_front_month) { //too close to expiration, skipping front month
			imonth++;
			if( imonth > 12) {
				iyear++;
				imonth -= 12;
			}
		}
			
		//if (id.getDate() > 10 && id.getDate() < 22) // if too close to
			//imonth++;										// expiration, add a month.
		//imonth++; // zero-index vs 1-indexed
		year = iyear;
		month = imonth;
		if (month > 12) {
			year++;
			month -= 12;
		}
		Vector<String[]> expiries = getOptionExpiries(ticker_folder,ticker);
		if( expiries == null || expiries.size() == 0) {
			System.out.println("no expires found for "+ticker);
			return null;
		}
		int current_sample = 0;
		for( ; current_sample < expiries.size(); current_sample++) {
			String[] expiry_info = expiries.get(current_sample);
			/*
			for( int  j= 0; j < expiry_info.length; j++) {
				System.out.println("j:"+expiry_info[j]);
			}
			System.out.println("done cc");
			System.exit(0);
			*/
			optionsdata = getOptionsDataFromLink(ticker_folder,ticker, expiry_info[0]);//nextexpiry);
			if( optionsdata != null) {
				//year = (int) optionsdata[0][1];
				//month = (int) (optionsdata[0][2] + 1);
				d = new Date();
				Date expiry_date = OptionCalendar.parseExpiryDate(expiry_info[1]);
				yte1 = OptionCalendar.getYTE(d,expiry_date);//getYearsToExpiryMonth(d, year, month);
				System.out.println("nextexpiry: " + expiry_info[1]);
				info = getOptionsInfo(optionsdata);
				break;
			}
		}
		if (info == null) {
			System.out.println("info is null");
		}
		
/*
		while (info == null) {
			//System.out.println("aa");
			month++;
			if (month > 12) {
				year++;
				month -= 12;
				if (year >= max_year)
					break;
			}
			if( !isInExpiries(month,year,expiries))
				continue;
			nextexpiry = year + "-" + month;
			optionsdata = getOptionsData(ticker_folder,ticker, nextexpiry);
			while (optionsdata == null) {
				month++;
				if (month > 12) {
					year++;
					month -= 12;
					if (year >= max_year)
						break;
				}
				if( !isInExpiries(month,year,expiries))
					continue;
				
				nextexpiry = year + "-" + month;
				System.out.println("next expiry: " + nextexpiry);
				optionsdata = getOptionsData(ticker_folder,ticker, nextexpiry);
			}
			if (year >= max_year)
				break;
			if (optionsdata == null || optionsdata.length == 0) {
				System.out.println("===failed to get first options data! " + ticker);
				return new double[] { 0 };
			}
			year = (int) optionsdata[0][1];
			month = (int) (optionsdata[0][2] + 1);
			d = new Date();
			yte1 = OptionCalendar.getYearsToExpiryMonth(d, year, month);
			System.out.println("nextexpiry: " + nextexpiry);
			info = getOptionsInfo(optionsdata);
			if (info == null)
				System.out.println("info is null");
		}
		*/
		//!!REPLACE THIS TO USE NEW STRING[] EXPIRY METHOD

		double[] info2 = null;
		double[][] optionsdata2 = null;
		
		for( ; current_sample < expiries.size(); current_sample++) {
			String[] expiry_info = expiries.get(current_sample);
			
			optionsdata2 = getOptionsDataFromLink(ticker_folder,ticker, expiry_info[0]);
			if( optionsdata2 != null) {
				//int year2 = (int) optionsdata2[0][1];
				//int month2 = (int) (optionsdata2[0][2] + 1);
				Date d2 = new Date();
				Date expiry_date = OptionCalendar.parseExpiryDate(expiry_info[1]);
				yte2 = OptionCalendar.getYTE(d2,expiry_date);//getYearsToExpiryMonth(d, year, month);
				System.out.println("nextexpiry: " + expiry_info[1]);
				
				//yte2 = OptionCalendar.getYearsToExpiryMonth(d2, year2, month2);
				//System.out.println("nextexpiry: " + nextexpiry);
				info2 = getOptionsInfo(optionsdata2);
				break;
			}
		}
		if (info2 == null) {
			System.out.println("info2 is null");
		}
		//System.out.println("done afds");
		//System.exit(0);
		/*
		while (info2 == null) {
			month++;
			if (month > 12) {
				year++;
				month -= 12;
				if (year >= max_year)
					break;
			}
			if( !isInExpiries(month,year,expiries))
				continue;
			nextexpiry = year + "-" + month;
			optionsdata2 = getOptionsData(ticker_folder,ticker, nextexpiry);
			while (optionsdata2 == null) {
				month++;
				if (month > 12) {
					year++;
					month -= 12;
					if (year >= max_year)
						break;
				}
				if( !isInExpiries(month,year,expiries))
					continue;
				nextexpiry = year + "-" + month;
				System.out.println("next expiry2: " + nextexpiry);
				optionsdata2 = getOptionsData(ticker_folder,ticker, nextexpiry);
			}
			if (year >= max_year)
				break;
			if (optionsdata2 == null || optionsdata2.length < 1) {
				System.out.println("===failed to get second options data! " + ticker);
				return new double[] { 0 };
			}
			int year2 = (int) optionsdata2[0][1];
			int month2 = (int) (optionsdata2[0][2] + 1);
			Date d2 = new Date();
			yte2 = OptionCalendar.getYearsToExpiryMonth(d2, year2, month2);

			info2 = getOptionsInfo(optionsdata2);
			if (info2 == null)
				System.out.println("info2 is null");
		}
		*/
		if (info == null) {
			System.out.println("info is null");
			return null;
		}
		if (info2 == null) {
			System.out.println("info2 is null");
			return null;
		}
		double tsb = (info2[3] - info[3]) / (yte2 - yte1);
		// double tsa = ((info[3]*2+info2[3])-(yte1*2+yte2)*tsb)/3;
		double tsa = (info[3] - yte1 * tsb);
		info[11] = tsa;
		info[12] = tsb;
		// double[11];
		/*
		 * double[] info = new double[]{ inc,round_to,err1 < err2 ? 1 : 0
		 * ,br[0], br[1],ar[0],ar[1],mr[0], mr[1],sr[0],sr[1],0,0 //last one is
		 * new: change in vol per change in yte };
		 */
		System.out.println("====got options info for " + ticker);
		if (usecache)
			cache.put(ticker, info);
		return info;
		} catch (Exception ex) {
			System.out.println("====get options info threw exception " + ticker);
			System.err.println("====get options info threw exception " + ticker);
			ex.printStackTrace();
		}
		System.out.println("====get options info returning null " + ticker);
		return null;
	}

	static double[] getOptionsInfo(double[][] optionsdata) {
		Date day = OptionCalendar.getLastMarketDay(new Date());
		double underlying = optionsdata[0][0];
		int year = (int) optionsdata[0][1];
		int month = (int) (optionsdata[0][2] + 1);

		System.out.println("underlying: " + underlying);
		double[][] temp2 = new double[optionsdata.length - 1][];
		for (int i = 0; i < temp2.length; i++) {
			temp2[i] = optionsdata[i + 1];
			// System.out.println(" od "+i+":"+temp2[i]);
		}
		optionsdata = temp2;
		System.out.println("temp2: " + temp2.length);
		Vector<double[]> temp = new Vector<double[]>();
		double down_strikes = 6.2;
		double up_strikes = 8.2;
		double inc;
		if (optionsdata.length < 6)
			return null;
		else if (optionsdata.length < 7)
			inc = GCD(new double[] { optionsdata[3][0], optionsdata[4][0], optionsdata[5][0] });
		else if (optionsdata.length < 8)
			inc = GCD(new double[] { optionsdata[4][0], optionsdata[5][0], optionsdata[6][0] });
		else if (optionsdata.length < 9)
			inc = GCD(new double[] { optionsdata[4][0], optionsdata[5][0], optionsdata[6][0], optionsdata[7][0] });
		else
			inc = GCD(new double[] { optionsdata[5][0], optionsdata[6][0], optionsdata[7][0], optionsdata[8][0] });
		System.out.println("inc: " + inc);
		double min = underlying - down_strikes * inc;
		double max = underlying + up_strikes * inc;
		for (int i = 0; i < optionsdata.length; i++)
			if (optionsdata[i][0] > min && optionsdata[i][0] < max)
				temp.add(optionsdata[i]);
		optionsdata = new double[temp.size()][];
		for (int i = 0; i < temp.size(); i++)
			optionsdata[i] = temp.get(i);

		double yte = OptionCalendar.dateToYear(OptionCalendar.getExpiryForMonth(year, month)) - OptionCalendar.dateToYear(day);
		System.out.println("step 2");
		// .getYearsToNextExpiry(day,0);
		// System.out.println(day.toGMTString()+" years: "+yte);
		double[] rounds = new double[] { 1.0, 0.5, 0.25, 0.1, 0.05, 0.01 };
		int[] round_res = new int[] { 0, 0, 0, 0, 0, 0 };
		for (int i = 0; i < optionsdata.length; i++) {
			double[] data = optionsdata[i];
			// double bidvol = BlackScholes.getVolatility(data[0], underlying,
			// BlackScholes.riskfree, data[3], yte);
			// double askvol = BlackScholes.getVolatility(data[0], underlying,
			// BlackScholes.riskfree, data[4], yte);
			for (int j = 0; j < rounds.length; j++) {
				if (Math.abs(Math.round(data[3] / rounds[j]) * rounds[j] - data[3]) < 0.0001 * rounds[j])
					round_res[j]++;
				if (Math.abs(Math.round(data[4] / rounds[j]) * rounds[j] - data[4]) < 0.0001 * rounds[j])
					round_res[j]++;
			}
		} // until 70% divisible by
		System.out.println("step 3");
		int m = 0;
		double minr = .90;
		for (m = 0; m < round_res.length; m++) {
			double d = (round_res[m] / (double) (optionsdata.length * 2));
			// System.out.println(""+rounds[m]+": "+d);
			if (d > minr)
				break;
		}
		if (m == round_res.length)
			m--;
		double round_to = rounds[m];
		double[][] sources = new double[9][];
		for (int i = 0; i < sources.length; i++)
			sources[i] = new double[optionsdata.length];
		System.out.println("step 4 " + optionsdata.length);
		for (int i = 0; i < optionsdata.length; i++) {
			try {
				System.out.println("strike " + i);
				double[] data = optionsdata[i];
				System.out.print(".");
				double bidvol = BlackScholes.getVolatility(data[0], underlying, BlackScholes.riskfree, data[3], yte);
				System.out.print(".");
				double midvol = BlackScholes.getVolatility(data[0], underlying, BlackScholes.riskfree, (data[3] + data[4]) / 2, yte);
				System.out.print(".");
				double askvol = BlackScholes.getVolatility(data[0], underlying, BlackScholes.riskfree, data[4], yte);
				sources[0][i] = data[0] - underlying;
				sources[1][i] = bidvol;
				sources[2][i] = midvol;
				sources[3][i] = askvol;
				sources[4][i] = data[4] - data[3];
				sources[5][i] = data[3];
				sources[6][i] = data[4];
				System.out.print(".");
			} catch (Exception ex) {
				System.out.println("ex 5:" + ex);
				ex.printStackTrace();
			}
			// System.out.println((data[0]-underlying)+": "+data[3]+" "+data[4]+" : "+midvol);
		}
		System.out.println("step 5");
		double[] br = linear_regression(sources[0], sources[1]);
		double[] mr = linear_regression(sources[0], sources[2]);
		double[] ar = linear_regression(sources[0], sources[3]);
		double[] sr = linear_regression(sources[0], sources[4]);
		System.out.println("step 6");
		double err1 = 0, err2 = 0;
		for (int i = 0; i < optionsdata.length; i++) {
			double[] data = optionsdata[i];
			double diff = data[0] - underlying;
			double bvol = br[0] + br[1] * diff;
			double mvol = mr[0] + mr[1] * diff;
			double avol = ar[0] + ar[1] * diff;
			double spr = sr[0] + sr[1] * diff;
			double bid1 = BlackScholes.call(data[0], underlying, BlackScholes.riskfree, bvol, yte);
			double ask1 = BlackScholes.call(data[0], underlying, BlackScholes.riskfree, avol, yte);
			double mid = BlackScholes.call(data[0], underlying, BlackScholes.riskfree, mvol, yte);
			double bid2 = mid - spr / 2;
			if (bid2 < 0)
				bid2 = 0;
			double ask2 = mid + spr / 2;
			bid1 -= data[3];
			ask2 -= data[4];
			bid1 -= data[3];
			ask2 -= data[4];
			err1 += bid1 * bid1 + ask1 * ask1;
			err2 += bid2 * bid2 + ask2 * ask2;
		}
		System.out.println("step 7");

		// increment, rounding, method, 1, 2
		// double[] br = linear_regression(sources[0],sources[1]);
		// for( int i = 0; i < optionsdata.length; i++) {
		double[] info = new double[] { inc, round_to, err1 < err2 ? 1 : 0, br[0], br[1], ar[0], ar[1], mr[0], mr[1], sr[0], sr[1], 0, 0, underlying // last
																																					// yte
		};
		System.out.println("step 8");

		return info;
	}

	// -1 = bid, 1 = ask

	public static double getPrice(String ticker, double strike, double underlying, double yte, int bid_or_ask) {
		return getPrice(strike, underlying, yte, getOptionsInfo(ticker), bid_or_ask);

	}
	public static double getPrice(double strike, double underlying, double yte, double[] optionsinfo, int bid_or_ask) {
		return getPrice(strike, underlying, yte, optionsinfo, bid_or_ask,0);
	}
	public static double getPrice(double strike, double underlying, double yte, double[] optionsinfo, int bid_or_ask, double vol_correction) {
		double diff = strike - underlying;
		double vol1 = 0, vol2 = 0, vol3 = 0;
		double sr = (optionsinfo[9] + diff * optionsinfo[10]);
		if (use_old_method)
			vol1 = optionsinfo[3] + diff * optionsinfo[4];
		else
			vol1 = optionsinfo[11] + diff * optionsinfo[4] + yte * optionsinfo[12];
		vol1+=vol_correction;
		/*
		if( vol1 <= 0 && vol_correction != 0) {
			//System.out.println("!!!!non-positive volatility!!!!");
		}
		vol1 = optionsinfo[11];
		if( vol1 < 0) {
			neg++;
			if( neg % 100 == 0)
				System.out.println("-");
		}
		else if( vol1 > 0) {
			pos++;
			if( pos % 100 == 0)
				;//System.out.println("+");
		}
		else {
			zer++;
			if( zer % 100 == 0)
				System.out.println("0");
		}		
		*/
		if( vol1 < 0 && crop_neg_vol)
			vol1 = 0;
			
		// vol2 = optionsinfo[7]+diff*optionsinfo[8];
		// vol3 = optionsinfo[5]+diff*optionsinfo[6];
		// System.out.println("vol1: "+vol1+" vol2: "+vol2+" vol3: "+vol3);

		double bid = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, vol1, yte);
		// double mid = BlackScholes.callPrice(underlying, strike,
		// BlackScholes.riskfree, vol2, yte);
		// double ask = BlackScholes.callPrice(underlying, strike,
		// BlackScholes.riskfree, vol3, yte);
		// System.out.println("bid: "+bid+" mid: "+mid+" ask: "+ask);

		// bid = (bid+mid-sr)/2;
		// ask = (ask+bid+sr*2)/2;
		double ask = bid + sr;

		// if( optionsinfo[2] == 0)
		// price += sr*(double)bid_or_ask;

		if (bid < 0)
			bid = 0;
		bid = Math.round(bid / optionsinfo[1]) * optionsinfo[1];
		if (ask < 0)
			ask = 0;
		ask = Math.round(ask / optionsinfo[1]) * optionsinfo[1];

		if (ask - bid < 0.0001)
			ask = bid + optionsinfo[1];
		// System.out.println("ask "+ask+" bid "+bid+" ask-bid "+(ask-bid)+" rounding "+optionsinfo[1]);

		return bid_or_ask < 0 ? bid : ask;
	}

	public static double getBidVolATM(double yte, double[][] optionsinfo) {
		double moneyness = 0;
		double bidvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[1]);
		return bidvol;
	}
	public static double getPrice2(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask) {
		return getPrice2(strike, underlying, yte, optionsinfo, bid_or_ask, true);
	}
	public static double getPrice2(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask, boolean round) {
		return getPrice2( strike,  underlying,  yte, optionsinfo,  bid_or_ask, round,0);

	}
	public static double getPrice2(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask, double vol_correction) {
		return getPrice2( strike,  underlying,  yte, optionsinfo,  bid_or_ask, true,vol_correction);

	}	
	static int neg = 0;
	static int pos = 0;
	static int zer = 0;
	public static double[] getIV(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask, double vol_correction) {
		double moneyness = Math.log(strike / underlying);
		double bidvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[1]);
		bidvol += vol_correction;
		double midvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[4]);
		midvol += vol_correction;
		double askvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[5]);
		askvol += vol_correction;
		return new double[]{bidvol,midvol,askvol};
	}
	
	public static double getPrice2(double strike, double underlying, double yte, double[][] optionsinfo, int bid_or_ask, boolean round, double vol_correction) {
		double moneyness = Math.log(strike / underlying);
		double bidvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[1]);
		bidvol += vol_correction;
		double midvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[4]);
		midvol += vol_correction;
		double askvol = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[5]);
		askvol += vol_correction;
		/*
		if( bidvol <= 0 && vol_correction != 0) {
			//System.out.println("!!!!non-positive volatility!!!!");
		}
		if( bidvol < 0) {
			neg++;
			if( neg % 100 == 0)
				System.out.println("-");
		}
		else if( bidvol > 0) {
			pos++;
			if( pos % 100 == 0)
				;//System.out.println("+");
		}
		else {
			zer++;
			if( zer % 100 == 0)
				System.out.println("0");
		}
		*/
		if( bidvol < 0 && crop_neg_vol)
			bidvol = 0;
		if( askvol < 0 && crop_neg_vol)
			askvol = 0;
		if( midvol < 0 && crop_neg_vol)
			midvol = 0;

		double price_correction = 0.01 * apply_quadratic(new double[] { moneyness, yte }, optionsinfo[2]);
		double spread = apply_quadratic(new double[] { moneyness, yte }, optionsinfo[3]);

		if( optionsinfo == null) {
			System.out.println("optinfo is null");
			return 0;
		}
		if( optionsinfo[1] == null) {
			System.out.println("optinfo[1] is null");
			if( optionsinfo[2] == null) {
				System.out.println("optinfo[2] is null");
			}
			return 0;
		}
		if( optionsinfo[2] == null) {
			System.out.println("optinfo[2] is null");
			return 0;
		}
		//System.out.println("good!");

		double bid = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, bidvol, yte);// + price_correction;
		double ask = BlackScholes.callPrice(underlying, strike, BlackScholes.riskfree, askvol, yte);// + price_correction;
		/*
		if( bid != bid)
			System.out.println("bid is nan "+bidvol);
		if( ask != ask)
			System.out.println("ask is nan "+askvol);
			*/
		//double ask = bid + spread;

		if (bid < 0)
			bid = 0;
		if (ask < 0)
			ask = 0;
		if( bid > ask) {
			bid = (ask+bid/2.0);
			ask = bid;
		}
		if (round) {
			bid = Math.floor(bid / optionsinfo[0][1]) * optionsinfo[0][1];
			ask = Math.ceil(ask / optionsinfo[0][1]) * optionsinfo[0][1];
		}

		if (ask - bid < 0.0001)
			ask = bid + optionsinfo[0][1];

		return bid_or_ask < 0 ? bid : ask;
	}

	public static double[] solve_quadratic(double[][] x, double[] y) {

		double[][] x2 = new double[x.length < y.length ? x.length : y.length][];
		for (int i = 0; i < x2.length; i++)
			x2[i] = expand_quadratic(x[i]);

		MultiLinearSolver2 mls = new MultiLinearSolver2();
		double[][] solutions = new double[8][];
		for (int i = 0; i < solutions.length; i++)
			solutions[i] = mls.solve(x2, y);
		double[] solution = new double[solutions[0].length];
		for (int i = 0; i < solution.length; i++) {
			solution[i] = 0;
			for (int j = 0; j < solutions.length; j++) {
				solution[i] += solutions[j][i];
			}
			solution[i] /= solutions.length;
		}
		double[][] dd = ArrayFuncs.transpose(x2);
		for (int n = 0; n < 0; n++) {
			double[] res = new double[x.length];
			double d = 0;
			double d2 = 0;
			for (int i = 0; i < x.length; i++) {
				res[i] = y[i] - apply_quadratic(x[i], solution);
				d += res[i];
				d2 += res[i] * res[i];
			}
			/*
			double[] solll = mls.solve(x2, res); 
			for( int j = 0; j < solll.length; j++) {
				solution[j] += solll[j];
			}*/
				//solutions[i] += 
			if (n % 20 == 0)
				System.out.println("err: " + Math.sqrt(d2 / x.length));
			
			solution[0] += d / (double) (x.length);
			for (int i = 1; i < dd.length; i++) {
				for (int j = 0; j < x.length; j++)
					res[j] = y[j] - apply_quadratic(x[j], solution);
				double[] sol = linear_regression(dd[i], res);
				solution[i] += sol[1]*1.0;
				// System.out.println("sol: "+sol[0]+" "+sol[1]+" "+sol[2]);
			}
		}

		double[] test = apply_quadratic(x, solution);
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			double r = test[i] - y[i];
			d += r * r;
		}
		d /= x.length;
		System.out.println(" rmse quadratic fit: " + Math.sqrt(d));
		return solution;
	}

	public static double[] apply_quadratic(double[][] x, double[] quadratic) {
		double[] d = new double[x.length];
		for (int i = 0; i < x.length; i++)
			d[i] = apply_quadratic(x[i], quadratic);
		return d;
	}

	public static double apply_quadratic(double[] x, double[] quadratic) {
		double[] x2 = expand_quadratic(x);
		double d = 0;
		for (int i = 0; i < x2.length && i < quadratic.length; i++)
			d += x2[i] * quadratic[i];
		return d;
	}

	static double[] expand_quadratic(double[] x) {
		double s = x[0];
		double t = x[1];
		double sqrtt = Math.sqrt(t);
		double recsqrtt = 1.0 / sqrtt;
		// s*s = 0.7877 0.773 exp = 0.76544 0.75834
		double norms = Math.exp(-s * s); // 0.6307 0.6418
		double adjs = s * recsqrtt;
		double norms2 = Math.exp(-adjs * adjs); // 0.62865 //0.6162315
		double skewadjs = adjs * adjs * (adjs - 3);
		double kurtadjs = adjs * adjs * (adjs * adjs - 6) + 3;
		double norms3 = norms2 * skewadjs;
		double norms4 = norms2 * kurtadjs;
		//math.exp(-strike*time)
		//cubic model:
		//1,M,MM,MMM,T,TT
		/*
		 * just 2: 0.65509 0.680609 +3: 0.600564 0.6129266 +4: 0.584719
		 * 0.6021411 +both: 0.5055374 0.5154722 +cbrt: 0.508364 0.519
		 */
		//TRY REMOVING ELEMENTS FROM A.
		double[] a = new double[] { 1, s, norms2, norms3, norms4 };// sqrttt};
		// double[] b = new double[]{1,Math.pow(t,-0.5),t};//{1,recsqrtt,t};
		double[] b = new double[] { 1, recsqrtt, t };
		double[] y = null;
		/*
		 * add hyptan instead of cbrt(s)? then need to also find what to mulitpy
		 * s by before taking hyptan
		 * 
		 * find change in vol per log return of underlying price?
		 * 
		 * adjust norm for skew and kurt - by multiplying by adjs 1,2,3, and 4
		 * times.
		 */
		double lnt = Math.log(t);
		double rlnt = 1.0/lnt;

		if (false)
			y = new double[] { 1 };
		else if (false)
			y = new double[] { 1, adjs, // skew correction
					adjs * adjs, // kurtosis correction

			};
		else if (true)
			/*
			46.51158851852085: 
							1,
				s,
				t,
				s*s,
				t*t,
				s*s*s,
				
				 43.53128070260056
				 				1,
				s,
				t,
				s*s,
				t*t,
				s*s*s,
				a[0] * b[1],
				a[1] * b[1],
				
				 total error: 60.300666226513336
				 			1,
				s,
				t,
				s*s,
				t*t,
				s*s*s,
				recsqrtt,
				s * recsqrtt,
				s*t,
				norms,
				norms * recsqrtt,
				norms * t,
				
				 total error: 44.259001793354344
				1,
				s,
				t,
				s*s,
				t*t,
				s*s*s,
				recsqrtt,
				s * recsqrtt,
				s*t,
				
				 total error: 48.07029779350291
				 				1,
				s,
				t,
				s*s,
				t*t,
				
				total error: 43.54862209498517
								1,
				s,
				t,
				
				999
 total error: 142.5704376671681
 1,s,t,s*s,
 
 od2 is null:2
999
 total error: 144.27010377610065:
 1
 
  total error: 137.26952702193026
  1,s

 total error: 144.0638897878636
 1,s,ss
999
 total error: 142.5704376671681
 1,s,t
 999
 total error: 135.90398427311877
 1,s,recsqrtt
 999 
 total error: 458.5198639507509
 1,s,recsqrtt 
 
 999
 total error: 466.1969361413072
 1
 w/svd
  log error:11.460413519786224
  og error:11.924270195291783
  w/out svd
           11.924270195291783
           
           999
 total error: 469.84562549368746
				1,
				s,
				s*s,
				recsqrtt,
				s*recsqrtt,
				s*s*recsqrtt
				
				999
 total error: 445.5494400873646
				1,
				s,
				recsqrtt,
				s*recsqrtt,
				
				999
 total error: 440.5820858917468
				1,
				s,
				recsqrtt,
				s*recsqrtt,
				Math.abs(s),
				Math.abs(s)*recsqrtt,
	999
 total error: 668.7925272521506			
				1,
				s,
				Math.abs(s),
				recsqrtt,
				s*recsqrtt,
				Math.abs(s)*recsqrtt,
				t,
				s*t,
				Math.abs(s)*t,
				
			999
 total error: 456.16360268258444
				1,
				s,
				Math.abs(s),
				lnt,
				s*lnt,
				Math.abs(s)*lnt,
				//487.5650122509606
				 total error: 487.8595686682622
								1,
				s,
				Math.abs(s),
				recsqrtt,
				s*recsqrtt, //total error: 450.48511303931383
				Math.abs(s)*recsqrtt, //total error: 444.3694953620135
				norms,
				norms*recsqrtt
				
				all:440.63
				
				//421.30514784447956
*/
			y = new double[] { // 12 params //orig: 0.635667

				1,
				s*recsqrtt, //total error: 450.48511303931383 -460
				Math.abs(s)*recsqrtt, //total error: 444.3694953620135 - 442.7285000820409
				//Math.exp(s)*recsqrtt, //+total error: 420.4360867475495
				//s*rlnt,
				//Math.abs(s)*rlnt,
				//Math.exp(s)
				//Math.abs(Math.exp(s))*recsqrtt
				//s, //438.3209809943954 -434.66671971118717 --out
				//Math.abs(s), // total error: 436.6469405107742 -442.200894140411
				//recsqrtt, // total error: 434.2812904461781 --out
				//norms,
				//norms*recsqrtt
				//Math.abs(s)*s,
				//Math.abs(s)*s*recsqrtt,
				//t,
				//s*t,
				//t,
				//s*t,
				//Math.abs(s)*t,
				//rlnt,
				//s*rlnt,
				//Math.abs(s)*rlnt,
				
				//s*s*recsqrtt
				
				//s,
				//t,
				//s*s,
				//t*t,
				//s*s*s,
				//recsqrtt,
				//s * recsqrtt,
				//s*t,

				//a[1] * b[2]
				
				//a[0] * b[0], 
				/*
				a[0] * b[1], // 0.77345
				//a[0] * b[2], // 8
				//a[1] * b[0], // /8 0.61
				a[1] * b[1],// /// //0.56100149
				a[1] * b[2], // 606
				//a[2] * b[0], //
				a[2] * b[1], //
				a[2] * b[2], // 0.5959
				a[3] * b[0], // 0.58789
				a[3] * b[1], // 0.5897
				a[3] * b[2], // ////0.561923
				a[4] * b[0],
				a[4] * b[1], 
				a[4] * b[2],// ///// //0.60
				*/


			// a[5]*b[0],
			// a[5]*b[1],
			// a[5]*b[2],
			};
		else
			/*
			 * 0.09358312 0.09125182
			 */

			// add gaussian normal to smile? (calc var)
			// 1,norms,s X 1,recsqrtt,t
			y = new double[] {
				1 * recsqrtt, 
				norms,
				norms * recsqrtt,
				norms * t,
				// s*s,
				// s*s*recsqrtt,
				// s*s*t,
				s * recsqrtt,
				1, 
				s,
				t, 
				s * t, 
				};
		/*
		 * double[] y = new double[]{ 1, s, s*s, s*sqrtt, s*s*recsqrtt,
		 * 
		 * s*s*t, s*t, recsqrtt, sqrtt, t };
		 */
		return y;

		/*
		 * MedveDev-Scailet: 1,s,s*s (1/sqrtt),1,sqrtt,t, order too
		 * high->t*sqrtt
		 * 
		 * double[] y = new double[]{ 1, s, s*s, s*sqrtt, s*s*recsqrtt, };
		 */
	}

	public static double[] linear_regression(double[] x, double[] y) {
		double a = 0;
		double b = 0;
		double sd = 0;
		double sx = 0, sy = 0, xx = 0, xy = 0, yy = 0, sxx = 0, sxy = 0, syy = 0;
		for (int i = 0; i < x.length; i++) {
			sx += x[i];
			sy += y[i];
			xx += x[i] * x[i];
			xy += x[i] * y[i];
			yy += y[i] * y[i];
		}
		sx /= x.length;
		sy /= x.length;
		xx /= x.length;
		xy /= x.length;
		yy /= x.length;
		sxx = xx - sx * sx;
		sxy = xy - sx * sy;
		syy = yy - sy * sy;
		// System.out.println("sxx: "+sxx+" sxy: "+sxy+" syy: "+syy);

		b = sxy / sxx;
		a = sy - b * sx;

		for (int i = 0; i < x.length; i++) {
			double d = y[i] - (a + b * x[i]);
			sd += d * d;
		}
		sd /= x.length;
		sd = Math.sqrt(sd);

		return new double[] { a, b, sd };
	}

	public static double[][] getOptionsData(int dummy,String ticker_folder,String ticker) {
		Date d = new Date();
		int year = d.getYear() + 1900;
		int max_year = d.getYear() + 1900 + max_year_look_ahead;
		int month = d.getMonth() + 1;
		
		if(d.getDate() > 10 && skip_front_month) {  //too close to expiration, skipping front month.
			System.out.println("too close to expiration, skipping front month");
			month++;
			if (month > 12) {
				month -= 12;
				year++;
			}
		}
		double[][] test = getOptionsData(ticker_folder,ticker, year + "-" + month);
		while (test == null) {
			month++;
			if (month > 12) {
				month -= 12;
				year++;
				if (year >= max_year)
					break;
			}
			test = getOptionsData(ticker_folder,ticker, year + "-" + month);
		}
		return test;
		// return getOptionsData(ticker,null);

	}
	public static boolean hasOptionsData(String ticker_folder,String ticker) {
		System.out.println("hasoptionsdata "+ticker_folder+ticker);
		if( use_cached_options_data && ticker_folder != null) {
			System.out.println("..using cache");
			File dir = new File(ticker_folder+ticker);

			  String[] chld = dir.list();
			  if(chld == null)
				  return false;
			  for( int i = 0; i < chld.length; i++) {
				  if( chld[i].length() < 5)
					  continue;
				  if( chld[i].substring(0,4).equals("opt_")) {
					  return true;
				  }
			  }
			  return false;
		}
			
		String month = null;
		String url = "http://finance.yahoo.com/q/op?s=" + ticker.toUpperCase() + "+Options";
		if (month != null && month.length() > 0)
			url += "&m=" + month;
		System.out.println("url: " + url);
		String html = readHTML(url);
		String raw = parse(html);
		if( raw.indexOf("There is no Options data available") > -1) {
			return false;
		}
		return true;
	}

	public static double[][] getOptionsData(String ticker, String month) {
		return getOptionsData(null,  ticker,  month);
	}
	public static double[][] getOptionsDataFromLink(String ticker_folder, String ticker, String link) {
		try {
			String html = "";
			String month = link.substring(link.indexOf("date="));

			if( !use_cached_options_data || ticker_folder == null) {
				
				String url = "http://finance.yahoo.com"+link;//q/op?s=" + ticker.toUpperCase() + "+Options";
				System.out.println("url: " + url);
				html = readHTML(url);
				System.out.println("test "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html "+html.length());
				if(!use_cached_options_data && ticker_folder != null) {
					System.out.println("writing "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html "+html.length());
					File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(html.getBytes());
					fos.flush();
					fos.close();
					System.out.println("done writting");
				}
			} 
			if(use_cached_options_data && ticker_folder != null) {
				File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
				if( f == null || !f.exists())
					return null;
				FileInputStream fos = new FileInputStream(f);

				byte[] bb = new byte[fos.available()];
				fos.read(bb);
				fos.close();
				html = new String(bb);
			}
			//System.exit(0);
			SimpleDateFormat ydf = new SimpleDateFormat("EEE, MMM dd, yyyy, h:mma z");
			SimpleDateFormat ydf2 = new SimpleDateFormat("EEE, MMM dd yyyy, h:mma z");
			SimpleDateFormat ydf3 = new SimpleDateFormat("MMM dd, yyyy");
			String dt;
			Date d = null;
			try {
				dt = html.substring(html.indexOf("<span id=\"yfs_market_time\">"));
				dt = dt.substring(0,dt.indexOf("</span>"));
				dt = dt.substring(dt.indexOf(">")+1);
				if( dt.indexOf(" - ") > 0)
					dt = dt.substring(0,dt.indexOf(" - "));
				dt = dt.trim();
				d = new Date();
				try {
					d = ydf.parse(dt);
				} catch (Exception ex) {
					try {
						//String[] dtt = dt.split(",");
						d = ydf2.parse(dt);//t[1].trim());
					} catch (Exception ex2) {
						System.out.println("unable to parse yahoo date: "+dt+" "+ex2);
						//System.exit(0);
					}
				}
			} catch (Exception ex) {
				System.err.println("failed to parse on "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
				//System.err.println("html: "+html);
				ex.printStackTrace();
				System.out.println("index out of range: "+html);
				//System.exit(0);
			}
			


			
			String raw = parse(html);
			if( raw.indexOf("There is no Options data available") > -1) {
				return null;
			}
			//System.out.println(raw);
			/*
			String page_says_expires = "";
			try {
				//page_says_expires = raw.split("Expire at close ")[1].split("\n")[0].trim();
				page_says_expires = raw.split("selected >")[1];
				System.out.println("split 1 success: "+page_says_expires.length());
				page_says_expires = page_says_expires.split("</option>")[0].trim();
				System.out.println("split 2 success");
				System.out.println("page_says_expires: "+page_says_expires);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("failed to parse page says expired date: "+page_says_expires);
				//System.exit(0);
				//System.out.println("raw: "+raw);
				//System.exit(0);
			}
			*/
			//page_says_expires = page_says_expires.substring(0, page_says_expires.length() - 1).trim();
/*
			String myear = month.substring(0, 4);
			String mmonth = month.substring(5).trim();
			System.out.println("myear: " + myear);
			System.out.println("mmonth: " + mmonth);
			Date does_expire = OptionCalendar.getExpiryForMonth(Integer.parseInt(myear), Integer.parseInt(mmonth));
			Date does_expire2 = new Date(does_expire.getTime() - OptionCalendar.daylength);
			//String sdoes_expire = new SimpleDateFormat("EEEEEEEEEEEE, MMMMMMMMMMMMM d, yyyy").format(does_expire).trim();
			//String sdoes_expire2 = new SimpleDateFormat("EEEEEEEEEEEE, MMMMMMMMMMMMM d, yyyy").format(does_expire2).trim();
			String sdoes_expire = new SimpleDateFormat("MMMMMMMMMMMMM d, yyyy").format(does_expire).trim();
			String sdoes_expire2 = new SimpleDateFormat("MMMMMMMMMMMMM d, yyyy").format(does_expire2).trim();
			System.out.println(" page says expires: " + page_says_expires);
			System.out.println(" does expire: " + sdoes_expire);
			System.out.println(" does expire2: " + sdoes_expire2);
			if (!sdoes_expire.equals(page_says_expires) && !sdoes_expire2.equals(page_says_expires)) {
				System.out.println(" wrong expiry!");
				return null;
			}
			*/
	//</span></span>
			//String underlying = raw.split("Search for share pricesSearch for share pricesFinance")[1].split("QuotesSummaryReal-TimeOptionsHistorical")[0];
			String underlying = html.split("data-sq=\""+ticker.toUpperCase()+":value\">")[1]
							.split("</span>")[0];
			double undprice = 0;
			try {
				// underlying = underlying.split(":")[2].split("&")[0];
				undprice = Double.parseDouble(underlying);//underlying.split(":")[2].split("&")[0].trim());
			} catch (Exception ex) {
				//undprice = Double.parseDouble(underlying.split(":")[3].split("&")[0].trim());
			}
			System.out.println("st 1");
			System.out.println(undprice);
			//System.exit(0);
			// System.out.println("underlying: "+underlying);
			String orig_html = html;
			try {
			html = html.split("optionsCallsTable")[1].split("optionsPutsTable")[0];
			} catch (Exception ex) {
				System.out.println(html);
				System.out.println("link: "+link);
				ex.printStackTrace();
				Thread.sleep(200L);
				//System.exit(0);
			}
			String[] lines = html.split("<tr data-row=");//"\n");
			if( lines.length < 5) {
				System.out.println(" not enough strike prices");
				return null;
			}
			System.out.println(" - st 4");
			double[][] data = new double[lines.length][];
			if (data.length == 0)
				data = new double[1][];
			
			
			String sexp = orig_html.substring(orig_html.indexOf("Grid-U options_menu"));
			sexp = sexp.substring(sexp.indexOf("selected")+"selected".length());
			sexp = sexp.substring(0,sexp.indexOf("</option>"));
			sexp = sexp.substring(sexp.indexOf(">")+1).trim();
			Date expiry = new SimpleDateFormat("MMMMMMMMM dd, yyyy").parse(sexp);
			System.out.println("expiry: "+expiry);
			
			data[0] = new double[] { undprice, expiry.getYear() + 1900, expiry.getMonth(), d.getTime() };
			
			for (int i = 1; i < data.length; i++) {
				String[] ss = lines[i].split("<td");
				ss[1] = ss[1].substring(ss[1].indexOf("<a href"));
				ss[1] = ss[1].substring(ss[1].indexOf(">")+1);
				ss[1] = ss[1].substring(0,ss[1].indexOf("</a>"));

				ss[2] = ss[2].substring(ss[2].indexOf("<a href"));
				ss[2] = ss[2].substring(ss[2].indexOf(">")+1);
				ss[2] = ss[2].substring(0,ss[2].indexOf("</a>"));
				for( int j = 3; j < 7; j++) {
					ss[j] = ss[j].substring(ss[j].indexOf("<div"));
					ss[j] = ss[j].substring(ss[j].indexOf(">")+1);
					ss[j] = ss[j].substring(0,ss[j].indexOf("</div>"));
				}
				

				/*
				for( int j = 0; j < ss.length; j++) {
					System.out.print(j+":"+ss[j]+"  ");
				}
				System.out.println();
				System.exit(0);
				*/
				data[i] = new double[] { 
						ss[1].equals("N/A") ? 0 : Double.parseDouble(ss[1].replaceAll(",", "")), // strike
						ss[3].equals("N/A") ? 0 : Double.parseDouble(ss[3].replaceAll(",", "")), // last
						ss[6].equals("N/A") ? 0 : Double.parseDouble(ss[6].replaceAll(",", "")), // chg
						ss[4].equals("N/A") ? 0 : Double.parseDouble(ss[4].replaceAll(",", "")), // bid
						ss[5].equals("N/A") ? 0 : Double.parseDouble(ss[5].replaceAll(",", "")), // ask
						0,//ss[7].equals("N/A") ? 0 : Double.parseDouble(ss[7].replaceAll(",", "")), // vol
						0,//ss[8].equals("N/A") ? 0 : Double.parseDouble(ss[8].replaceAll(",", "")), // open
																		// interest
				};
/*
				data[i] = new double[] { 
						ss[1].equals("N/A") ? 0 : Double.parseDouble(ss[1].replaceAll(",", "")), // strike
						ss[3].equals("N/A") ? 0 : Double.parseDouble(ss[3].replaceAll(",", "")), // last
						ss[4].equals("N/A") ? 0 : Double.parseDouble(ss[4].replaceAll(",", "")), // chg
						ss[5].equals("N/A") ? 0 : Double.parseDouble(ss[5].replaceAll(",", "")), // bid
						ss[6].equals("N/A") ? 0 : Double.parseDouble(ss[6].replaceAll(",", "")), // ask
						ss[7].equals("N/A") ? 0 : Double.parseDouble(ss[7].replaceAll(",", "")), // vol
						ss[8].equals("N/A") ? 0 : Double.parseDouble(ss[8].replaceAll(",", "")), // open
																		// interest
				};
				*/
				/*
				 * for( int j = 0; j < data[i].length; j++)
				 * System.out.print(data[i][j]+", "); System.out.println();
				 */
			}
			System.out.println("st 5");
			return data;
		} catch (Exception ex) {
			System.out.println("exception on getOptionsData "+ex);
			ex.printStackTrace();
			return new double[][] {};
		}
	}

	public static double[][] getOptionsData(String ticker_folder, String ticker, String month) {
		try {
			if( month.indexOf(".") > 0) {
				month = month.substring(0,month.indexOf(".") );
			}
			if (month != null)
				System.out.println("month: " + month);
			else {
				System.out.println("month is null");
				Date d = new Date();
				
				int iyear = d.getYear() + 1900;
				int imonth = d.getMonth() + 1;
				month = "" + iyear + "-" + imonth;
			}
			String html = "";
			if( !use_cached_options_data || ticker_folder == null) {
				
				String url = "http://finance.yahoo.com/q/op?s=" + ticker.toUpperCase() + "+Options";
				if (month != null && month.length() > 0)
					url += "&m=" + month;
				System.out.println("url: " + url);
				html = readHTML(url);
				System.out.println("test "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html "+html.length());
				if(!use_cached_options_data && ticker_folder != null) {
					System.out.println("writing "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html "+html.length());
					File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(html.getBytes());
					fos.flush();
					fos.close();
					System.out.println("done writting");
				}
			} 
			if(use_cached_options_data && ticker_folder != null) {
				File f = new File(ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
				if( f == null || !f.exists())
					return null;
				FileInputStream fos = new FileInputStream(f);

				byte[] bb = new byte[fos.available()];
				fos.read(bb);
				fos.close();
				html = new String(bb);
			}
			//System.exit(0);
			SimpleDateFormat ydf = new SimpleDateFormat("EEE, MMM dd, yyyy, h:mma z");
			SimpleDateFormat ydf2 = new SimpleDateFormat("EEE, MMM dd yyyy, h:mma z");
			SimpleDateFormat ydf3 = new SimpleDateFormat("MMM dd, yyyy");
			String dt;
			Date d = null;
			try {
				dt = html.substring(html.indexOf("<span id=\"yfs_market_time\">"));
				dt = dt.substring(0,dt.indexOf("</span>"));
				dt = dt.substring(dt.indexOf(">")+1);
				if( dt.indexOf(" - ") > 0)
					dt = dt.substring(0,dt.indexOf(" - "));
				dt = dt.trim();
				d = new Date();
				try {
					d = ydf.parse(dt);
				} catch (Exception ex) {
					try {
						//String[] dtt = dt.split(",");
						d = ydf2.parse(dt);//t[1].trim());
					} catch (Exception ex2) {
						System.out.println("unable to parse yahoo date: "+dt+" "+ex2);
						System.exit(0);
					}
				}
			} catch (Exception ex) {
				System.err.println("failed to parse on "+ticker_folder+ticker+Settings.FILE_SEPARATOR+"opt_"+month+".html");
				//System.err.println("html: "+html);
				ex.printStackTrace();
				System.out.println("index out of range: "+html);
				//System.exit(0);
			}
			


			
			String raw = parse(html);
			if( raw.indexOf("There is no Options data available") > -1) {
				return null;
			}
			//System.out.println(raw);
			String page_says_expires = "";
			try {
				//page_says_expires = raw.split("Expire at close ")[1].split("\n")[0].trim();
				page_says_expires = raw.split("selected >")[1];
				System.out.println("split 1 success: "+page_says_expires.length());
				page_says_expires = page_says_expires.split("</option>")[0].trim();
				System.out.println("split 2 success");
				System.out.println("page_says_expires: "+page_says_expires);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("failed to parse page says expired date: "+page_says_expires);
				//System.exit(0);
				System.out.println("raw: "+raw);
				//System.exit(0);
			}
			//page_says_expires = page_says_expires.substring(0, page_says_expires.length() - 1).trim();

			String myear = month.substring(0, 4);
			String mmonth = month.substring(5).trim();
			System.out.println("myear: " + myear);
			System.out.println("mmonth: " + mmonth);
			Date does_expire = OptionCalendar.getExpiryForMonth(Integer.parseInt(myear), Integer.parseInt(mmonth));
			Date does_expire2 = new Date(does_expire.getTime() - OptionCalendar.daylength);
			//String sdoes_expire = new SimpleDateFormat("EEEEEEEEEEEE, MMMMMMMMMMMMM d, yyyy").format(does_expire).trim();
			//String sdoes_expire2 = new SimpleDateFormat("EEEEEEEEEEEE, MMMMMMMMMMMMM d, yyyy").format(does_expire2).trim();
			String sdoes_expire = new SimpleDateFormat("MMMMMMMMMMMMM d, yyyy").format(does_expire).trim();
			String sdoes_expire2 = new SimpleDateFormat("MMMMMMMMMMMMM d, yyyy").format(does_expire2).trim();
			System.out.println(" page says expires: " + page_says_expires);
			System.out.println(" does expire: " + sdoes_expire);
			System.out.println(" does expire2: " + sdoes_expire2);
			if (!sdoes_expire.equals(page_says_expires) && !sdoes_expire2.equals(page_says_expires)) {
				System.out.println(" wrong expiry!");
				return null;
			}
//</span></span>
			//String underlying = raw.split("Search for share pricesSearch for share pricesFinance")[1].split("QuotesSummaryReal-TimeOptionsHistorical")[0];
			String underlying = html.split("><span id=\"yfs_l84_"+ticker.toLowerCase()+"\">")[1].split("</span></span>")[0];
			double undprice = 0;
			try {
				// underlying = underlying.split(":")[2].split("&")[0];
				undprice = Double.parseDouble(underlying);//underlying.split(":")[2].split("&")[0].trim());
			} catch (Exception ex) {
				//undprice = Double.parseDouble(underlying.split(":")[3].split("&")[0].trim());
			}
			System.out.println("st 1");
			System.out.println(undprice);
			//System.exit(0);
			// System.out.println("underlying: "+underlying);
			raw = raw.split("Call Options")[1].split("Put Options")[0];
			// System.out.println(raw);

			// System.out.println(raw);
			String[] lines = raw.split("\n");
			String[] ss2 = lines[0].split(", ");
			// skip the month
			System.out.println("st 2: " + ss2.length);
			if (ss2.length < 4) {
				System.out.println(" not enough strike prices");
				return null;
			}
			System.out.println("ss2: " + lines[0]);
			String exp = ss2[2] + ", " + ss2[3];
			Date expiry = new Date();
			try {
				expiry = new SimpleDateFormat("MMMM dd, yyyy").parse(ss2[2] + ", " + ss2[3]);

				/*
				 * SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
				 * try { Date d = sdf.parse(exp);(
				 */
				System.out.println(expiry);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// new SimpleDateFormat("").parse(exp);
			System.out.println(" - st 4");
			double[][] data = new double[lines.length - 4 - 2 + 1][];
			if (data.length == 0)
				data = new double[1][];
			data[0] = new double[] { undprice, expiry.getYear() + 1900, expiry.getMonth(), d.getTime() };
			for (int i = 1; i < data.length; i++) {
				String[] ss = lines[i + 4 - 1].split(", ");
				/*
				for( int j = 0; j < ss.length; j++) {
					System.out.print(j+":"+ss[j]+"  ");
				}
				System.out.println();
				System.exit(0);
				*/
				data[i] = new double[] { ss[1].equals("N/A") ? 0 : Double.parseDouble(ss[1].replaceAll(",", "")), // strike
						ss[3].equals("N/A") ? 0 : Double.parseDouble(ss[3].replaceAll(",", "")), // last
						ss[4].equals("N/A") ? 0 : Double.parseDouble(ss[4].replaceAll(",", "")), // chg
						ss[5].equals("N/A") ? 0 : Double.parseDouble(ss[5].replaceAll(",", "")), // bid
						ss[6].equals("N/A") ? 0 : Double.parseDouble(ss[6].replaceAll(",", "")), // ask
						ss[7].equals("N/A") ? 0 : Double.parseDouble(ss[7].replaceAll(",", "")), // vol
						ss[8].equals("N/A") ? 0 : Double.parseDouble(ss[8].replaceAll(",", "")), // open
																		// interest
				};
				/*
				 * for( int j = 0; j < data[i].length; j++)
				 * System.out.print(data[i][j]+", "); System.out.println();
				 */
			}
			System.out.println("st 5");
			return data;
		} catch (Exception ex) {
			System.out.println("exception on getOptionsData "+ex);
			ex.printStackTrace();
			return new double[][] {};
		}
	}
	

	public static Vector<String[]> getOptionExpiries(String ticker_folder,String ticker) {
		try {
			if( use_cached_options_data)
				return null;//getOptExiriesFromCache(ticker_folder,ticker);

			String url = "http://finance.yahoo.com/q/op?s=" + ticker.toUpperCase() + "+Options";
			System.out.println("url: " + url);
			String html = readHTML(url);
			String raw = parse(html);
			if( raw.indexOf("There is no Options data available") > -1) {
				return null;
			}
			int index = html.indexOf("Grid-U options_menu");//"View By Expiration:");
			if( index < 0) {
				System.out.println("no view by expiration");
				System.out.println(html.length()+" "+raw.length());
				return null;
			}
			String expiries = html.substring(index);
			expiries = expiries.substring(0, expiries.indexOf("Grid-U options-menu-item symbol_lookup"));//"table"));
			//System.out.println("expiries: "+expiries);
			Vector<String[]> vexpiries = new Vector<String[]>();
			/*
			int s = expiries.indexOf("</option>");
			//Sep 12
			//012345
			if( s < 6) {
				System.out.println("less than 6");
			}
			if( s >= 6) {
				String text = expiries.substring(s-6,s);
				System.out.println(text);
				int tyear = 2000+Integer.parseInt(text.substring(4));
				String txm = text.substring(0,4).trim().toLowerCase();
				int tmonth = 
						txm.equals("jan") ? 1 :
						txm.equals("feb") ? 2 :
						txm.equals("mar") ? 3 :
						txm.equals("apr") ? 4 :
						txm.equals("may") ? 5 :
						txm.equals("jun") ? 6 :
						txm.equals("jul") ? 7 :
						txm.equals("aug") ? 8 :
						txm.equals("sep") ? 9 :
						txm.equals("oct") ? 10 :
						txm.equals("nov") ? 11 :
						txm.equals("dec") ? 12 :
						0;
				String out = ""+tyear+"-"+(tmonth < 10 ? "0" :"")+tmonth;
				vexpiries.add(out);
				System.out.println("exp: "+out);
			}
			*/
			
			String[] months = new String[]{
					"nullary",
					"jan",
					"feb",
					"mar",
					"apr",
					"may",
					"jun",
					"jul",
					"aug",
					"sep",
					"oct",
					"nov",
					"dec"
					};
			while( true) {
				try {
					int n = expiries.indexOf("</option>");//expiries.indexOf("m=");
					if( n < 0 || n+10 > expiries.length())
						break;
					String link = expiries.substring(expiries.indexOf("data-selectbox-link=\"")+"data-selectbox-link=\"".length());
					link = link.substring(0,link.indexOf("\""));
					System.out.println("link: "+link);
					//System.exit(0);
					
					String text = expiries.substring(n-40 > 0 ? n-40 : 0,n);
					text = text.substring(text.indexOf(">")+1).trim().toLowerCase();
					System.out.println("text: "+text);
					String tyear = text.substring(text.length()-4);
					int tmonth = -1;
					//&date=1415923200
					for( int i = 0; i < months.length; i++) {
						if( text.indexOf(months[i]) > -1) {
							tmonth = i;
							break;
						}
					}
					String out = ""+tyear+"-"+(tmonth < 10 ? "0" :"")+tmonth;
					System.out.println("out: "+out);
					//System.exit(0);
					vexpiries.add(new String[]{link,text,out});
					//vexpiries.add(expiries.substring(0,7));
					expiries = expiries.substring(n+11);
					//System.out.println("exp: "+expiries.substring(0,7));
				} catch (Exception ex) {
					break;
				}
				
				/*
				 * <strong>Aug 12</strong> | <a href="/q/op?s=GMCR&amp;m=2012-09">Sep 12</a> | <a href="/q/op?s=GMCR&amp;m=2012-12">Dec 12</a> | <a href="/q/op?s=GMCR&amp;m=2013-01">Jan 13</a> | <a href="/q/op?s=GMCR&amp;m=2013-03">Mar 13</a> | <a href="/q/op?s=GMCR&amp;m=2014-01">Jan 14</a>
				 * <table cellpadding="0" cellspacing="0" border="0"><tr><td height="2"></td></tr></table><table class="yfnc_mod_table_title1" width="100%" cellpadding="2" cellspacing="0" border="0"><tr valign="top">
				 */
			}
			/*
			String[] expiry_info = vexpiries.get(1);
			for( int  j= 0; j < expiry_info.length; j++) {
				System.out.println(j+":"+expiry_info[j]);
			}
			System.out.println("done cec");
			*/
			//System.exit(0);
			
			return vexpiries;
		} catch (Exception ex) {
			System.out.println("exception in get expires: "+ex);
			ex.printStackTrace();
			return null;
		}
	}


	public static String readHTMLasCSV(String url) {
		try {
		    Thread.sleep(100L);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex3) {
		    Thread.currentThread().interrupt();
		}			

		String ret = "";
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			byte[] data = new byte[1024];
			int x = 0;
			StringBuffer sb = new StringBuffer();
			while ((x = in.read(data, 0, 1024)) >= 0) {
				String s = new String(data).substring(0, x);
				sb.append(s);
			}
			in.close();

			return parse(sb.toString());
		} catch (Exception ex) {
			try {
			    Thread.sleep(1000L*10L);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex3) {
			    Thread.currentThread().interrupt();
			}
			return "";
		}
	}
	public static String readHTML(String url) {
		String ret = "";
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			try {
			    Thread.sleep(100L);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex3) {
			    Thread.currentThread().interrupt();
			}			
			//byte[] data = new byte[1024];
			//int x = 0;
			StringBuffer sb = new StringBuffer();
			int sleep_count = 0;
			while( true) {
				if( in.available() > 0) {
					byte[] bb = new byte[in.available()];
					in.read(bb);
					sb.append(new String(bb));
				} else {
					String s = sb.toString();
					if( s.toLowerCase().indexOf("</html>")> -1 && in.available() < 1) {
						break;
					} else {
						try {
						    Thread.sleep(1L);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex3) {
						    Thread.currentThread().interrupt();
						}			
						Thread.sleep(1L);
						sleep_count++;
						if( sleep_count > 1000L*3L) {
							System.out.println("took too long! "+url);
							//System.exit(0);
							break;
						}
					}
				}
			}
			/*
			while ((x = in.read(data, 0, 1024)) >= 0) {
				String s = new String(data).substring(0, x);
				sb.append(s);
			}*/
			in.close();

			return sb.toString();
		} catch (Exception ex) {
			System.out.println("readhtml exception "+ex);
			ex.printStackTrace();
			if( ex.toString().indexOf("999") > 0) {
				try {
				    Thread.sleep(1000L*10L);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex3) {
				    Thread.currentThread().interrupt();
				}			}
			return "";
		}
	}
	public static String parse(String s) {
		StringBuffer sout = new StringBuffer();
		String[] rows = s.split("<tr");
		for (int i = 0; i < rows.length; i++) {
			String[] cells = rows[i].split("<td");
			for (int j = 0; j < cells.length; j++) {
				String val = "";
				String[] pieces = cells[j].split(">");
				for (int k = 1; k < pieces.length; k++) {
					String[] pp = pieces[k].split("<");
					val += pp[0];
				}
				sout.append(val + ", ");
			}
			sout.append("\n");
		}

		// System.out.print(sout.toString());
		return sout.toString();
	}

}
