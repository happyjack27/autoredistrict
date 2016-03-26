package ui;

import geography.Feature;
import geography.FeatureCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

import solutions.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class PanelStats extends JPanel implements iDiscreteEventListener {
	DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	Color[] dmcolors = null;
	double[] tot_seats = new double[5];
	
    private static BufferedImage imageToBufferedImage(Image image) {

    	BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2 = bufferedImage.createGraphics();
    	g2.drawImage(image, 0, 0, null);
    	g2.dispose();

    	return bufferedImage;

    }

    public static Image makeColorTransparent(BufferedImage im, final Color color) {
    	ImageFilter filter = new RGBImageFilter() {

    		// the color we are looking for... Alpha bits are set to opaque
    		public int markerRGB = color.getRGB() | 0xFF000000;

    		public final int filterRGB(int x, int y, int rgb) {
    			if ((rgb | 0xFF000000) == markerRGB) {
    				// Mark the alpha bits as zero - transparent
    				return 0x00FFFFFF & rgb;
    			} else {
    				// nothing to do
    				return rgb;
    			}
    		}
    	};

    	ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
    	return Toolkit.getDefaultToolkit().createImage(ip);
    }
	
	public void saveAsPng(JComponent component, String path) {
		saveAsPng( component, path,component.getWidth(), component.getHeight());
  	}
	public void saveAsPng(JComponent component, String path, int width, int height) {
		 System.out.println("path: "+path);
		 saveAsPng2( component,  path,  width,  height);
		 /*
		 String path2 = path.substring(0,path.indexOf(".png"))+"_small.png";
		 System.out.println("path2: "+path2);
		 System.out.println("index: "+path.indexOf(".png"));
		 saveAsPng2( component,  path2,  width/4,  height/4);
		 */
	}
	public void saveAsPng2(JComponent component, String path, int width, int height) {
		String path2 = path.substring(0,path.indexOf(".png"))+"_small.png";
        Dimension d = component.getSize();
        
        if( true) {
        	component.setSize(width,height);
        	component.doLayout();
        	
        } else {
        	width = d.width;
        	height = d.height;
        }
        //BufferedImage.TYPE_INT_ARGB
        
		BufferedImage image1 = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image2 = new BufferedImage(width/2,height/2, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image4 = new BufferedImage(width/4,height/4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics1 = image1.createGraphics(); 
        Graphics2D graphics2 = image2.createGraphics(); 
        Graphics2D graphics4 = image4.createGraphics(); 

        graphics1.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics1.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics4.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics4.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics4.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        graphics1.setComposite(AlphaComposite.Clear);
        graphics1.fillRect(0, 0, width, height);
        graphics1.setComposite(AlphaComposite.Src);

        MainFrame.mainframe.resetZoom();
        
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        component.paint(graphics1);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        component.print(graphics1);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        graphics2.drawImage(image1,0,0,width/2,height/2,null);
        graphics4.drawImage(image2,0,0,width/4,height/4,null);
 
        if( Settings.national_map) {
        	image1 = imageToBufferedImage(makeColorTransparent(image1,Color.WHITE));
        }
        try {
            ImageIO.write(image1,"png", new File(path));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
    	
        if( Settings.national_map) {
        	image4 = imageToBufferedImage(makeColorTransparent(image4,Color.WHITE));
        	image4 = imageToBufferedImage(makeColorTransparent(image4,Color.BLACK));
        }
        
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        try {
            ImageIO.write(image4,"png", new File(path2));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        
        component.setSize(d);
    	component.doLayout();
	}

	public void getNormalizedStats() {
		DistrictMap dm = featureCollection.ecology.population.get(0);
		double conversion_to_bits = 1.0/Math.log(2.0);


		Ecology.normalized_history.add(new double[]{
				featureCollection.ecology.generation,
				featureCollection.ecology.population.size(),
				Settings.num_districts,
				
				Settings.mutation_boundary_rate,
				
				dm.fairnessScores[8]*conversion_to_bits, //REP IMBALANCE
				dm.fairnessScores[4], //POWER IMBALANCE
				dm.fairnessScores[5], //WASTED VOTES TOTAL
				dm.fairnessScores[6], //WASTED VOTES IMBALANCE
				dm.fairnessScores[7], //seats votes asymmetry
				

				0,//Settings.getAnnealingFloor( featureCollection.ecology.generation),

				(
						//Settings.square_root_compactness 
						dm.fairnessScores[0]
						//: (Math.sqrt(dm.fairnessScores[0])+Math.sqrt(dm2.fairnessScores[0])+Math.sqrt(dm3.fairnessScores[0]))*0.3333333
					), //BORDER LENGTH
				dm.fairnessScores[3], //DISCONNECTED POP
				dm.fairnessScores[2], //POP IMBALANCE
				0,
				0,
				
		});
		
	}
	
	public void getStats() {
		try {
		Vector<Double> ranked_dists = new Vector<Double>();

		try {
		if( featureCollection == null || featureCollection.ecology == null || featureCollection.ecology.population.size() < 1) {
			System.out.println("no ecology attached "+featureCollection);
			return;
		}
		DistrictMap dm = featureCollection.ecology.population.get(0);
		dm.calcFairnessScores(); 
		boolean single =  featureCollection.ecology.population.size() < 3;
		DistrictMap dm2 = featureCollection.ecology.population.get(single ? 0 : 1);
		dm2.calcFairnessScores();
		DistrictMap dm3 = featureCollection.ecology.population.get(single ? 0 : 2);
		dm3.calcFairnessScores();
		double conversion_to_bits = 1.0/Math.log(2.0);
		DecimalFormat decimal = new DecimalFormat("###,##0.000000000");
		DecimalFormat integer = new DecimalFormat("###,###,###,###,##0");
		//        fairnessScores = new double[]{length,disproportional_representation,population_imbalance,disconnected_pops,power_fairness}; //exponentiate because each bit represents twice as many people disenfranched

		try {
		if( false) {
			Ecology.history.add(new double[]{
					featureCollection.ecology.generation,
					featureCollection.ecology.population.size(),
					Settings.num_districts,
					
					Settings.mutation_boundary_rate,
					
					(dm.fairnessScores[1]+dm2.fairnessScores[1]+dm3.fairnessScores[1])*0.3333333*conversion_to_bits, //REP IMBALANCE
					(dm.fairnessScores[4]+dm2.fairnessScores[4]+dm3.fairnessScores[4])*0.3333333, //POWER IMBALANCE
					(dm.fairnessScores[5]+dm2.fairnessScores[5]+dm3.fairnessScores[5])*0.3333333, //WASTED VOTES TOTAL
					(dm.fairnessScores[6]+dm2.fairnessScores[6]+dm3.fairnessScores[6])*0.3333333, //WASTED VOTES IMBALANCE
					0,
					

					0,//Settings.getAnnealingFloor( featureCollection.ecology.generation),

					(
							//Settings.square_root_compactness 
							(dm.fairnessScores[0]+dm2.fairnessScores[0]+dm3.fairnessScores[0])*0.3333333 
							//: (Math.sqrt(dm.fairnessScores[0])+Math.sqrt(dm2.fairnessScores[0])+Math.sqrt(dm3.fairnessScores[0]))*0.3333333
						), //BORDER LENGTH
					(dm.fairnessScores[3]+dm2.fairnessScores[3]+dm3.fairnessScores[3])*0.3333333, //DISCONNECTED POP
					(dm.fairnessScores[2]+dm2.fairnessScores[2]+dm3.fairnessScores[2])*0.3333333*conversion_to_bits, //POP IMBALANCE
					0,
					0,
					
			});
			
		} else {
			Ecology.history.add(new double[]{
					featureCollection.ecology.generation,
					featureCollection.ecology.population.size(),
					Settings.num_districts,
					
					Settings.mutation_boundary_rate,
					
					dm.fairnessScores[8], //diag error
					dm.fairnessScores[5], //WASTED VOTES TOTAL
					//dm.fairnessScores[1]*conversion_to_bits, //REP IMBALANCE
					//dm.fairnessScores[6], //WASTED VOTES IMBALANCE
					dm.fairnessScores[7], //seats / votes asymmetry
					dm.fairnessScores[10],
					dm.fairnessScores[4], //POWER IMBALANCE
					/*
					 *    		"Proportionalness (global)",
    		"Competitiveness (victory margin)",
    		"Partisan symmetry",
    		"Racial vote dilution",
    		"Voting power imbalance",

					 */
					

					Settings.getAnnealingFloor( featureCollection.ecology.generation),

					(
							//Settings.square_root_compactness 
							dm.fairnessScores[0]
							//: (Math.sqrt(dm.fairnessScores[0])+Math.sqrt(dm2.fairnessScores[0])+Math.sqrt(dm3.fairnessScores[0]))*0.3333333
						), //BORDER LENGTH
					dm.fairnessScores[3], //DISCONNECTED POP
					dm.fairnessScores[2], //POP IMBALANCE
					dm.fairnessScores[9],//dm.fairnessScores[8], //diag error
					0,
					
			});
			
		}
		
		} catch (Exception ex) {
			System.out.println("ex ad "+ex);
			ex.printStackTrace();
		}
		
		Vector<String> cands = MainFrame.mainframe.project.election_columns;
		String[] dem_col_names = MainFrame.mainframe.project.demographic_columns_as_array();
		double[] pop_by_dem = new double[dem_col_names.length];
		double[] votes_by_dem = new double[dem_col_names.length];
		double[] vote_margins_by_dem = new double[dem_col_names.length];
		double[][] demo = dm.getDemographicsByDistrict();
		double[][] demo_pct = new double[demo.length][];
		double[] winners_by_ethnicity = new double[dem_col_names.length];
		
		try {
			double total_pvi = 0;
			double counted_districts = 0;
			double grand_total_votes = 0;
			int num_competitive = 0;
			double wasted_0 = 0;
			double wasted_1 = 0;
			
			String[] dcolumns = new String[17+Settings.num_candidates*2+dem_col_names.length*2];
			String[][] ddata = new String[dm.districts.size()][];
			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
			String[][] cdata = new String[Settings.num_candidates][];
			double[] elec_counts = new double[Settings.num_candidates];
			double[] vote_counts = new double[Settings.num_candidates];
			double tot_votes = 0;
			//=== by district
			try {
			if( dmcolors == null || dmcolors.length != dm.districts.size()) {
				dmcolors = new Color[dm.districts.size()];
			}
			dcolumns[0] = "District";
			dcolumns[1] = "Population";
			dcolumns[2] = "Winner";
			dcolumns[3] = "PVI"; //
			dcolumns[4] = "FV PVI"; //
			
			dcolumns[5] = "Vote gap";
			dcolumns[6] = "Wasted votes";
			
			dcolumns[7] = "FV Safe D";
			dcolumns[8] = "FV Lean D";
			dcolumns[9] = "FV Tossup";
			dcolumns[10] = "FV Lean R";
			dcolumns[11] = "FV Safe R";			
			//vote_gap_by_district
			
			dcolumns[12] = "Pop per seats";//"Self-entropy";
			dcolumns[13] = "Compactness";
			dcolumns[14] = "Area";
			dcolumns[15] = "Paired edge length";
			dcolumns[16] = "Unpaired edge length";
			
			
			for( int i = 0; i < Settings.num_candidates; i++) {
				try {
					elec_counts[i] = 0;
					vote_counts[i] = 0;
					dcolumns[i+17] = ""+cands.get(i)+" vote %";
					dcolumns[i+17+Settings.num_candidates] = ""+cands.get(i)+" votes";
				} catch (Exception ex) { }
			}
			for( int i = 0; i < dem_col_names.length; i++) {
				try {
				dcolumns[i+17+Settings.num_candidates*2] = ""+dem_col_names[i]+" %";
				dcolumns[i+17+Settings.num_candidates*2+dem_col_names.length] = ""+dem_col_names[i]+" pop";
			} catch (Exception ex) { }
			}
			
			double total_population = 0;
			
			for( int i = 0; i < pop_by_dem.length; i++) { pop_by_dem[i] = 0; }
			for( int i = 0; i < votes_by_dem.length; i++) { votes_by_dem[i] = 0; }
			for( int i = 0; i < vote_margins_by_dem.length; i++) { vote_margins_by_dem[i] = 0; }
			for( int i = 0; i < demo_pct.length; i++) {
				double total = 0;
				for( int j = 0; j < demo[i].length; j++) {
					pop_by_dem[j] += demo[i][j];
					total += demo[i][j];
				}
				total = 1.0/total;
				demo_pct[i] = new double[demo[i].length];
				for( int j = 0; j < demo[i].length; j++) {
					demo_pct[i][j] = demo[i][j]*total;
				}
			}
			for( int j = 0; j < tot_seats.length; j++) {
				tot_seats[j] = 0 ;
			}

	
			for( int i = 0; i < dm.districts.size(); i++) {
				try {
					dm.calcDemographicStatistics();
				dmcolors[i] = dm.getWastedVoteColor(i);
				ddata[i] = new String[dcolumns.length];
				District d = dm.districts.get(i);
				total_population += d.getPopulation();
				//String population = ""+(int)d.getPopulation();
				double[][] result = new double[2][];//d.getElectionResults();
				result[0] = d.getAnOutcome();
				result[1] = District.popular_vote_to_elected(result[0], i);
				
				double[] demo_result = District.popular_vote_to_elected(demo[i], i);
				for( int j = 0; j < demo_result.length; j++) {
					winners_by_ethnicity[j] += demo_result[j];
				}
				
				double self_entropy = 1;//d.getSelfEntropy(result[Settings.self_entropy_use_votecount?2:1]);
				//String edge_length = ""+d.getEdgeLength();
				//double [] dd = d.getVotes();
				double total = 0;
				//double max = -1;
				//int iwinner  = -1;
				String winner = "";
				boolean is_uncontested = false;
				if( result[0].length >= 2) {
					double sum = result[0][0] + result[0][1];
					if( (result[0][0] < sum*Settings.uncontested_threshold || result[0][1] < sum*Settings.uncontested_threshold )) {
						is_uncontested = true;
					}
				}
				if( is_uncontested && Settings.ignore_uncontested) {
		   	    	for( int j = 0; j < result[0].length; j++) {
		   	    		result[0][j] = 0;
		   	    	}
		   	    	for( int j = 0; j < result[0].length; j++) {
		   	    		result[1][j] = 0;
		   	    	}
				}
				for( int j = 0; j < result[0].length; j++) {
					winner += ""+((int)result[1][j])+",";
					elec_counts[j] += result[1][j];
					vote_counts[j] += result[0][j];
					total += result[0][j];
					tot_votes += result[0][j];
				}
				if( total == 0) {
					total = 1;
				}
				double total_votes = 0;
				double pvi = 0;
				String pviw = ""; 
				String fv_pviw = "";
				try {
					total_votes = result[0][0]+result[0][1];
					if( total_votes == 0) {
						total_votes = 1;
					}
					grand_total_votes += total_votes;
					double needed = total_votes/2;
					wasted_0 += result[0][0] - (result[0][0] >= needed ? needed : 0);
					wasted_1 += result[0][1] - (result[0][1] >= needed ? needed : 0);
					pvi = 100.0*(result[0][0] >= needed ? result[0][0] - needed : result[0][1] - needed)/total_votes;
					double fv_pvi = (double)(result[0][0]-result[0][1])/(double)total_votes; 
					fv_pvi = (fv_pvi+Settings.fv_pvi_adjust)/2.0;
					fv_pviw = fv_pvi > 0 ? "D+"+integer.format((int)Math.round(100*fv_pvi)) : "R+"+integer.format((int)Math.round(-100*fv_pvi))  ;
					if( !is_uncontested) {
						if( pvi < 5) {
							num_competitive++;
						}
						total_pvi += pvi;
						counted_districts++;
						ranked_dists.add(pvi*(result[0][0] >= needed ? -1 : +1));
						pviw = (result[0][0] >= needed ? "D" : "R")+"+"+integer.format((int)Math.round(pvi));
					} else {
						pviw = "uncontested";
						if( !Settings.ignore_uncontested) {
							ranked_dists.add(pvi*(result[0][0] >= needed ? -1 : +1));
						}
					}
				} catch (Exception ex) {
					
				}
				double[] seats = this.getSeats_new(result[0][0]/(result[0][0]+result[0][1]),  Settings.seats_in_district(i));
				for( int j = 0; j < seats.length; j++) {
					tot_seats[j] += seats[j];
				}
				//String winner = ""+iwinner;
				ddata[i][0] = ""+(i+1);
				ddata[i][1] = integer.format(d.getPopulation());
				ddata[i][2] = ""+winner;
				ddata[i][3] = ""+pviw;
				ddata[i][4] = ""+fv_pviw;
				
				ddata[i][5] = ""+dm.vote_gap_by_district[i];
				ddata[i][6] = ""+dm.wasted_votes_by_district[i];
				
				ddata[i][7] = ""+seats[0];
				ddata[i][8] = ""+seats[1];
				ddata[i][9] = ""+seats[2];
				ddata[i][10] = ""+seats[3];
				ddata[i][11] = ""+seats[4];
				
				ddata[i][12] = ""+integer.format(d.getPopulation()/Settings.seats_in_district(i));//  decimal.format(self_entropy*conversion_to_bits)+" bits";
				ddata[i][13] = ""+d.iso_quotient;
				ddata[i][14] = ""+d.area;
				ddata[i][15] = ""+d.paired_edge_length;
				ddata[i][16] = ""+d.unpaired_edge_length;
				for( int j = 17; j < ddata[i].length; j++) {
					ddata[i][j] = "";
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+17] = ""+(result[0][j]/total);
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+17+Settings.num_candidates] = ""+integer.format(result[0][j]);
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+17+Settings.num_candidates*2] = ""+decimal.format(demo_pct[i][j]);
					votes_by_dem[j] += total_votes*demo_pct[i][j];
					vote_margins_by_dem[j] += dm.vote_gap_by_district[i]*demo_pct[i][j];
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+17+Settings.num_candidates*2+dem_col_names.length] = ""+integer.format(demo[i][j]);
				}	
				} catch (Exception ex) {
					System.out.println("ex stats 1 "+ex);
					ex.printStackTrace();
				}
			}
			} catch (Exception ex) {
				System.out.println("by district exception "+ex);
				ex.printStackTrace();
			}
			
			double tot_pop = 0;
			double tot_vote = 0;
			double tot_margin = 0;
			for( int i = 0; i < dem_col_names.length; i++) {
				tot_pop += pop_by_dem[i];
				tot_vote += votes_by_dem[i];
				tot_margin += vote_margins_by_dem[i];
			}
			if( tot_margin == 0) {
				tot_margin = 1;
			}
			if( tot_vote == 0) {
				tot_vote = 1;
			}
			double ravg = 1.0 / (tot_margin / tot_vote);
			
			String[] ecolumns = new String[]{"Ethnicity","Population","Vote dilution","% Wasted votes","Victory margins","Votes","Straight vote descr. rep."};
			String[][] edata = new String[dem_col_names.length+1][];
			for( int i = 0; i < dem_col_names.length; i++) {
				edata[i] = new String[]{
						dem_col_names[i],
						integer.format(pop_by_dem[i]),
						decimal.format(ravg*vote_margins_by_dem[i]/votes_by_dem[i]),
						decimal.format(vote_margins_by_dem[i]/votes_by_dem[i]),
						decimal.format(vote_margins_by_dem[i]),
						decimal.format(votes_by_dem[i]),
						integer.format(winners_by_ethnicity[i]),
				};
			}
			edata[dem_col_names.length] = new String[]{
					"TOTAL",
					integer.format(tot_pop),
					"1",
					decimal.format(1.0/ravg),
					decimal.format(tot_vote),
					decimal.format(tot_margin),
			};

			TableModel tm0 = new DefaultTableModel(edata,ecolumns);
			ethnicityTable.setModel(tm0);
			//ethnicityTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);

			
			//=== summary
			int wasted_votes = 0;
			for( int m : dm.wasted_votes_by_party) {
				wasted_votes += m;
			}
			double egap = 100.0*Math.abs(wasted_0-wasted_1)/grand_total_votes;

			String[] scolumns = new String[]{"Value","Measure"};
			String[][] sdata = new String[][]{
					new String[]{""+(1.0/dm.fairnessScores[0]),"Compactness (isoperimetric quotient)"},
					new String[]{""+integer.format(dm.fairnessScores[3]),"Disconnected population (count)"},
					new String[]{""+decimal.format(dm.getMaxPopDiff()*100.0),"Population max deviation (%)"},
					new String[]{""+decimal.format(dm.getMeanPopDiff()*100.0),"Population mean deviation (%)"},
					Settings.reduce_splits && dm.countCountySplitsInteger() > 0 ? new String[]{""+integer.format(dm.countCountySplitsInteger()),"County splits"} : new String[]{"",""},	
					Settings.reduce_splits && dm.countMuniSplitsInteger() > 0 ? new String[]{""+integer.format(dm.countMuniSplitsInteger()),"Muni splits"} : new String[]{"",""},	
					new String[]{"",""},					
					new String[]{""+decimal.format(dm.fairnessScores[7]),"Seats / vote asymmetry"},
					new String[]{""+integer.format(dm.total_vote_gap),"Competitiveness (victory margin)"},
					new String[]{""+decimal.format(dm.fairnessScores[8]),"Representation imbalance (global)"},
					new String[]{""+decimal.format(dm.getRacialVoteDilution()),"Racial vote dilution"},
					new String[]{"",""},
					//new String[]{""+integer.format(wasted_votes),"Wasted votes (count)"},
					//new String[]{""+decimal.format(dm.fairnessScores[1]*conversion_to_bits),"Representation imbalance (local)"},
					new String[]{""+decimal.format(egap),"Efficiency gap (pct)"},
					new String[]{""+decimal.format(0.01*egap*(double)Settings.num_districts),"Adj. efficiency gap (seats)"},
					//new String[]{""+decimal.format(total_pvi / counted_districts),"Avg. PVI"},
					//new String[]{""+integer.format(num_competitive),"Competitive elections (< 5 PVI)"},
					new String[]{""+decimal.format(dm.fairnessScores[4]*conversion_to_bits),"Voting power imbalance (relative entropy)"},
					new String[]{"",""},
					new String[]{""+tot_seats[0],"FV Safe D"},
					new String[]{""+tot_seats[1],"FV Lean D"},
					new String[]{""+tot_seats[2],"FV Tossup"},
					new String[]{""+tot_seats[3],"FV Lean R"},
					new String[]{""+tot_seats[4],"FV Safe R"},
					new String[]{"",""},
					new String[]{""+decimal.format(100.0*Settings.mutation_boundary_rate),"Mutation rate (%)"},		
					new String[]{""+decimal.format(100.0*Settings.elite_fraction),"Elitism (%)"},		
					new String[]{""+integer.format(featureCollection.ecology.generation),"Generation (count)"},
			};
			TableModel tm = new DefaultTableModel(sdata,scolumns);
			summaryTable.setModel(tm);
			
			summaryTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);



			double tot_seats = Settings.total_seats();
			//=== by party
			for( int i = 0; i < Settings.num_candidates; i++) {
				try {
				cdata[i] = new String[]{
						""+cands.get(i),
						""+integer.format(elec_counts[i]),
						""+integer.format(vote_counts[i]),//*total_population/tot_votes),
						""+(dm.wasted_votes_by_party[i]),
						""+(elec_counts[i]/((double)tot_seats)),
						""+(vote_counts[i]/tot_votes)
				};
				} catch (Exception ex) { }
			}

			
			TableModel tm1 = new DefaultTableModel(ddata,dcolumns);
			districtsTable.setModel(tm1);
			Enumeration<TableColumn> en = districtsTable.getColumnModel().getColumns();
	        while (en.hasMoreElements()) {
	            TableColumn tc = en.nextElement();
	            tc.setCellRenderer(new MyTableCellRenderer());
	        }			
			TableModel tm2 = new DefaultTableModel(cdata,ccolumns);
			partiesTable.setModel(tm2);
		} catch (Exception ex) {
			System.out.println("ex ad "+ex);
			ex.printStackTrace();
		}
		
		this.invalidate();
		this.repaint();
		} catch (Exception ex) {
			System.out.println("ex af "+ex);
			ex.printStackTrace();
		}
		if( featureCollection.ecology != null && featureCollection.ecology.population != null && featureCollection.ecology.population.size() > 0) {
			MainFrame.mainframe.frameSeatsVotesChart.setData(featureCollection.ecology.population.get(0));
			MainFrame.mainframe.frameSeatsVotesChart.panelRanked.setData(ranked_dists);
		}
		} catch (Exception ex) {
			System.out.println("ex in PanelStats.getStats: "+ex);
			ex.printStackTrace();
		}
	}
	public PanelStats() {

		initComponents();
	}
	private void initComponents() {
		this.setLayout(null);
		this.setSize(new Dimension(449, 510));
		this.setPreferredSize(new Dimension(838, 650));
		
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 384, 791, 223);
		add(scrollPane);
		
		districtsTable = new JTable();
		scrollPane.setViewportView(districtsTable);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(427, 45, 390, 87);
		add(scrollPane_1);
		
		partiesTable = new JTable();
		scrollPane_1.setViewportView(partiesTable);
		districtsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		btnCopy = new JButton("copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ActionEvent nev = new ActionEvent(districtsTable, ActionEvent.ACTION_PERFORMED, "copy");
				districtsTable.selectAll();
				districtsTable.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		btnCopy.setBounds(728, 350, 89, 23);
		add(btnCopy);
		
		button = new JButton("copy");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionEvent nev = new ActionEvent(partiesTable, ActionEvent.ACTION_PERFORMED, "copy");
				partiesTable.selectAll();
				partiesTable.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		button.setBounds(728, 11, 89, 23);
		add(button);
		
		button_1 = new JButton("copy");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ActionEvent nev = new ActionEvent(summaryTable, ActionEvent.ACTION_PERFORMED, "copy");
				summaryTable.selectAll();
				summaryTable.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		button_1.setBounds(327, 11, 89, 23);
		add(button_1);
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(26, 45, 390, 294);
		add(scrollPane_2);
		
		summaryTable = new JTable();
		scrollPane_2.setViewportView(summaryTable);
		
		lblSummary = new JLabel("Summary");
		lblSummary.setBounds(26, 15, 67, 14);
		add(lblSummary);
		
		lblByDistrict = new JLabel("By district");
		lblByDistrict.setBounds(26, 359, 226, 14);
		add(lblByDistrict);
		
		lblByParty = new JLabel("By party");
		lblByParty.setBounds(427, 20, 226, 14);
		add(lblByParty);
		
		lblByEthnicity = new JLabel("By ethnicity");
		lblByEthnicity.setBounds(427, 152, 226, 14);
		add(lblByEthnicity);
		
		scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(427, 177, 390, 162);
		add(scrollPane_3);
		
		ethnicityTable = new JTable();
		scrollPane_3.setViewportView(ethnicityTable);
		
		button_2 = new JButton("copy");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionEvent nev = new ActionEvent(ethnicityTable, ActionEvent.ACTION_PERFORMED, "copy");
				ethnicityTable.selectAll();
				ethnicityTable.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		button_2.setBounds(728, 143, 89, 23);
		add(button_2);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToHtml();
			}
		});
		btnNewButton.setBounds(89, 8, 117, 29);
		
		add(btnNewButton);
	}
	public FeatureCollection featureCollection;
	private JTable districtsTable;
	private JTable partiesTable;
	public JButton btnCopy;
	public JButton button;
	public JTable summaryTable;
	public JButton button_1;
	public JScrollPane scrollPane_2;
	public JLabel lblSummary;
	public JLabel lblByDistrict;
	public JLabel lblByParty;
	public JLabel lblByEthnicity;
	public JScrollPane scrollPane_3;
	public JButton button_2;
	public JTable ethnicityTable;
	public final JButton btnNewButton = new JButton("to html");
	
	public void saveURL(final String filename, final URL url) {
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try {
	        in = new BufferedInputStream(url.openStream());
	        fout = new FileOutputStream(filename);

	        final byte data[] = new byte[1024];
	        int count;
	        while ((count = in.read(data, 0, 1024)) != -1) {
	            fout.write(data, 0, count);
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    } finally {
            try {
		        if (in != null) {
					in.close();
		        }
		        if (fout != null) {
		            fout.close();
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
    public static String getURLtext(URL url) {
    	try {
	        URLConnection connection = url.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                    connection.getInputStream()));
	
	        StringBuilder response = new StringBuilder();
	        String inputLine;
	
	        while ((inputLine = in.readLine()) != null) 
	            response.append(inputLine+"\n");
	
	        in.close();
	
	        return response.toString();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return "";
    	}
    }
	
    public class MyTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

        @Override
        public JComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(null);
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(String.valueOf(value));
            if( dmcolors != null && dmcolors.length > row) {
            	setBackground(dmcolors[row]);
            }
            /*
            boolean interestingRow = row % 5 == 2;
            boolean secondColumn = column == 1;
            if (interestingRow && secondColumn) {
                setBackground(Color.ORANGE);
            } else if (interestingRow) {
                setBackground(Color.YELLOW);
            } else if (secondColumn) {
                setBackground(Color.RED);
            }
            */
            return this;
        }

    }

    public String getAsHtml(JTable t) {
    	String str = "";
    	int cc = t.getColumnCount();
    	int rc = t.getRowCount();

    	str += "<table>\n";
    	str += "  <tr>\n";
    	for( int i = 0; i < cc; i++) {
    		str += "    <th>"+t.getColumnName(i)+"</th>\n";
    	}
    	str +="  </tr>\n";
    	//String[][] rows = new String[t.getRowCount()][];
    	for( int i = 0; i < rc; i++) {
        	str +="  <tr>\n";
        	for( int j = 0; j < cc; j++) {
        		str += "    <td>"+(String)t.getValueAt(i, j)+"</td>\n";
        	}
        	str +="  </tr>\n";
    		
    	}
    	str += "</table>\n";
    	return str;
    }



	@Override
	public void eventOccured() {
		getStats();
		MainFrame.mainframe.progressBar.setString( featureCollection.ecology.generation+" iterations");
	}
	public void exportMaps(String write_folder, int res) {
		int num_maps_temp = Settings.num_maps_to_draw;
		int display_mode_temp = Feature.display_mode;
		Settings.num_maps_to_draw = 1;
		
		boolean maplines = Feature.outline_vtds;
		boolean draw_labels = Feature.showDistrictLabels;
		Feature.outline_vtds = true;
		MapPanel.FSAA = Feature.outline_vtds ? 4 : 1;
		
		
		///====begin insert
		Feature.showDistrictLabels = true;
		Feature.display_mode = Feature.DISPLAY_MODE_NORMAL;				
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts_labels.png",res,res);
		Feature.showDistrictLabels = false;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts.png",res,res);
		
		System.out.println("2");
	
	
		Feature.display_mode = Feature.DISPLAY_MODE_DIST_VOTE;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_votes.png",res,res);
		System.out.println("3");
	
		Feature.display_mode = Feature.DISPLAY_MODE_DIST_POP;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_pop.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_COMPACTNESS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_compactness.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_WASTED_VOTES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_wasted_votes.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_VICTORY_MARGIN;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_WASTED_VOTES_BY_DEM;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_racial_packing.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_COUNTY_SPLITS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_splits.png",res,res);
		System.out.println("4");
	
		Feature.display_mode = Feature.DISPLAY_MODE_VOTES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_votes.png",res,res);
		Feature.display_mode = Feature.DISPLAY_MODE_DEMOGRAPHICS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_demographics.png",res,res);
		System.out.println("5");
	
	
		Feature.display_mode = Feature.DISPLAY_MODE_COUNTIES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_counties.png",res,res);
	
	
		MainFrame.mainframe.saveData(new File(write_folder+"vtd_data.txt"), 1,false);
		MainFrame.mainframe.saveData(new File(write_folder+"vtd_data.dbf"), 2,false);
	
		Feature.showDistrictLabels = draw_labels;
		Feature.outline_vtds = maplines;
		MapPanel.FSAA = Feature.outline_vtds ? 4 : 1;
		Settings.num_maps_to_draw = num_maps_temp;
		Feature.display_mode = display_mode_temp;
		MapPanel.override_size = -1;
	}

	
	public void exportTransparent() {
		MainFrame.mainframe.ip.addHistory("EXPORT NATIONAL");
		String write_folder = Download.getStartPath()+MainFrame.mainframe.project.district_column+File.separator
				+"national"+File.separator;
		new File(write_folder).mkdirs();

		boolean t = Settings.national_map;
		
		Settings.national_map = true;
		for( Feature f : MainFrame.mainframe.featureCollection.features ) {
			f.geometry.makePolysFull();
			f.geometry.makePolys();
		}
		exportMaps(write_folder,1024);
		Settings.national_map = t;
		for( Feature f : MainFrame.mainframe.featureCollection.features ) {
			f.geometry.makePolysFull();
			f.geometry.makePolys();
		}
		
	}
	public void exportToHtml() {
		System.out.println("1");
		MainFrame.mainframe.ip.addHistory("EXPORT HTML");

		String partiesStr = getAsHtml(partiesTable);
		String raceStr = getAsHtml(ethnicityTable);
		String sumStr = getAsHtml(summaryTable);
		String districtsStr = getAsHtml(districtsTable);
		
		URL header_path = Applet.class.getResource("/resources/header.php");
		URL footer_path = Applet.class.getResource("/resources/footer.php");
		URL style_sheet = Applet.class.getResource("/resources/styles2.css");
		
		String write_folder = Download.getStartPath()+MainFrame.mainframe.project.district_column+File.separator;
		new File(write_folder).mkdirs();
		
		saveURL(write_folder+"style.css",style_sheet);
		
		System.out.println("1");

		//MapPanel.override_size = 1024;
		saveAsPng(MainFrame.mainframe.frameSeatsVotesChart.panel,write_folder+"seats_votes.png");
		saveAsPng(MainFrame.mainframe.frameSeatsVotesChart.panelRanked.panel,write_folder+"sorted_districts.png");

		exportMaps(write_folder,1024);
	
		System.out.println("6");


		String html = "";

		html += getURLtext(header_path);
		html +="<i>This page was generated on "+ new SimpleDateFormat("yyyy.MM.dd").format(new Date())+" by <a href='http://autoredistrict.org'>Auto-Redistrict</a>.</i><br/><br/>\n";

		html +="<h3>VTD district assignments</h3><br/>\n";
		html +="<a href='./vtd_data.txt'>vtd_data.txt (tab-delimited)</a><br/>\n";
		html +="<a href='./vtd_data.dbf'>vtd_data.dbf (dbase/ESRI)</a><br/>\n";
		html +="<br/><br/>\n";

		html +="<h3>Maps (click to enlarge)</h3><br/>\n";
		html +="<table>";
		html +="<tr>";
		html +="<td>Districts</td>";
		html +="<td align='center'><center><a href='./map_districts_labels.png'><img src='./map_districts_labels_small.png' width=100></br>Labeled districts</a></center></td>";
		html +="<td align='center'><center><a href='./map_districts.png'><img src='./map_districts_small.png' width=100></br>Unlabeled districts</a></center></td>";
		html +="<td align='center'><center><a href='./map_district_votes.png'><img src='./map_district_votes_small.png' width=100></br>Vote balance</a></center></td>";
		html +="</tr>";
		html +="<tr>";
		html +="<td>Geometry</td>";
		html +="<td align='center'><center><a href='./map_district_pop.png'><img src='./map_district_pop_small.png' width=100></br>Population difference</a></center></td>";
		html +="<td align='center'><center><a href='./map_district_compactness.png'><img src='./map_district_compactness_small.png' width=100></br>Compactness</a></center></td>";
		html +="<td align='center'><center><a href='./map_splits.png'><img src='./map_splits_small.png' width=100></br>Splits</a></center></td>";
		html +="</tr>";
		html +="<tr>";
		html +="<td>Fairness</td>";
		html +="<td align='center'><center><a href='./map_district_partisan_packing.png'><img src='./map_district_partisan_packing_small.png' width=100></br>Partisan vote packing</a></center></td>";
		html +="<td align='center'><center><a href='./map_district_racial_packing.png'><img src='./map_district_racial_packing_small.png' width=100></br>Racial vote packing</a></center></td>";
		html +="<td align='center'><center><a href='./map_district_wasted_votes.png'><img src='./map_district_wasted_votes_small.png' width=100></br>Wasted votes by party</a></center></td>";
		html +="</tr>";
		html +="<tr>";
		html +="<td>Counties</td>";
		html +="<td align='center'><center><a href='./map_counties.png'><img src='./map_counties_small.png' width=100></br>Counties</a></center></td>";
		html +="</tr>";
		html +="<tr>";
		html +="<td>VTDs</td>";
		html +="<td align='center'><center><a href='./map_vtd_votes.png'><img src='./map_vtd_votes_small.png' width=100></br>VTD vote balance</a></center></td>";
		html +="<td align='center'><center><a href='./map_vtd_demographics.png'><img src='./map_vtd_demographics_small.png' width=100></br>VTD demographics</a></center></td>";
		html +="</tr>";
		html +="</table>";
		html +="</br>";

		html +="<h3>Seats / votes curve - Vote packing</h3><br/>\n";
		html +="<center><img src='./seats_votes.png'> <img src='./sorted_districts.png'></center><br/>\n";

		html +="<h3>Summary</h3>\n";
		html += sumStr+"\n";
		html +="<br/><br/>\n";
		html +="<h3>By party</h3>\n";
		html += partiesStr+"\n";
		html +="<br/><br/>\n";
		html +="<h3>By district</h3>\n";
		html += districtsStr+"\n";
		html +="<br/><br/>\n";
		html +="<h3>By ethnicity</h3>\n";
		html += raceStr+"\n";
		html +="<br/><br/>\n";
		html +="<h3>Splits</h3><br/>\n";
		System.out.println("7");

		Hashtable<String,int[]> counties = featureCollection.ecology.population.get(0).getSplitCounties();
		Vector<String> vs = new Vector<String>();
		for( Entry<String,int[]> s : counties.entrySet()) {
			vs.add(s.getKey());
		}
		Collections.sort(vs);
		for( int i = 0; i < vs.size(); i++) {
			html += vs.get(i)+"<br/>\n";
		}

		html +="<br/><br/>\n";
		
		System.out.println("8");


		html += getURLtext(footer_path);


		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats.html");
			fos.write(html.getBytes());
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String url = "file:///"+(write_folder+"stats.html").replaceAll("\\\\", "/").replaceAll(" ", "%20");
		
		System.out.println("9");

		//url = URLEncoder.encode(url);
		Applet.browseTo(url);
		
		///=====end insert
		
		

		/*
		excel.ExportThread thread = new excel.ExportThread();
		thread.export(summaryTable, districtsTable, partiesTable, ethnicityTable, MainFrame.mainframe.frameSeatsVotesChart.table);
		*/
	}
	
	public static double[] getSeats(double pct_d, double seats) {
		double pct_r = 1.0-pct_d;
		double safe_d = Math.floor(pct_d*seats);
		double safe_r = Math.floor(pct_r*seats);
		
		double threshold_d = (safe_d+1.0)/(seats+1.0);
		double threshold_r = (safe_r+1.0)/(seats+1.0);
		double remainder_d = pct_d-threshold_d;
		double remainder_r = pct_r-threshold_r;

		safe_d += remainder_d >= 0.08 ? 1 : 0;
		safe_r += remainder_r >= 0.08 ? 1 : 0;
		double lean_d = remainder_d < 0.08 && remainder_d >= 0.04 ? 1 : 0;
		double lean_r = remainder_r < 0.08 && remainder_r >= 0.04 ? 1 : 0;
		double tossup = remainder_d < 0.04 && remainder_r < 0.04 ? 1 : 0;

		return new double[]{safe_d,lean_d,tossup,lean_r,safe_r};
	}

	public static double[] getSeats_alt(double pct_d, double seats) {
		double pct_r = 1.0-pct_d;
		double safe_d = Math.floor(pct_d*seats);
		double safe_r = Math.floor(pct_r*seats);
		
		double remainder_d = pct_d*seats-safe_d;
		double remainder_r = pct_r*seats-safe_r;

		remainder_d -= 0.5;
		remainder_r -= 0.5;

		safe_d += remainder_d >= 0.08 ? 1 : 0;
		safe_r += remainder_r >= 0.08 ? 1 : 0;
		double lean_d = remainder_d < 0.08 && remainder_d >= 0.04 ? 1 : 0;
		double lean_r = remainder_r < 0.08 && remainder_r >= 0.04 ? 1 : 0;
		double tossup = remainder_d < 0.04 && remainder_r < 0.04 ? 1 : 0;

		return new double[]{safe_d,lean_d,tossup,lean_r,safe_r};
	}
	public static double[] getSeats_new(double pct_d, double seats) {

		double safe_d = Math.floor((pct_d-0.08)*(seats+1));

		//System.out.println("pct_d "+pct_d+" seats "+seats+" safe_d "+safe_d+" (pct_d-0.08)*seats "+((pct_d-0.08)*(seats+1)));

		double threshold_d = Math.round((seats+1)*pct_d)/(seats+1);
		double remainder_d = pct_d-threshold_d;

		double lean_d = remainder_d <= 0.08 && remainder_d > 0.04 ? 1 : 0;
		double tossup = remainder_d <= 0.04 && remainder_d >= -0.04 ? 1 : 0;
		double lean_r = remainder_d >= -0.08 && remainder_d < -0.04 ? 1 : 0;

		double safe_r = seats - lean_r - tossup - lean_d - safe_d;

		return new double[]{safe_d,lean_d,tossup,lean_r,safe_r};
	}
	
}
