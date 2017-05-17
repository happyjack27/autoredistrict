package ui;

import geography.VTD;
import geography.FeatureCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

import solutions.*;
import util.Triplet;

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
	
	public static String stats_descriptions_url = "https://docs.google.com/spreadsheets/d/1PlhLg9j9TCu0lkLWQJE7KBPRe618Si62JQo7ubvkKO8/edit?usp=sharing";
	
	DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	Color[] dmcolors = null;
	double[] tot_seats = new double[5];
	
	BufferedImage pie_party_comp_digital;
	BufferedImage pie_party_comp_digital2;
	BufferedImage pie_party_votes;
	BufferedImage pie_party_seats;
	BufferedImage pie_party_seats_frac;
	BufferedImage pie_eth_pop;
	BufferedImage pie_eth_target;
	BufferedImage pie_eth_descr;
	BufferedImage pie_eth_power;
	
	BufferedImage pie_eth_packing;
	BufferedImage pie_party_packing;
	BufferedImage pie_eth_packingm;
	BufferedImage pie_party_packingm;
	
	BufferedImage pie_eth_packing_byvoter;
	BufferedImage pie_party_packing_byvoter;
	BufferedImage pie_eth_packingm_byvoter;
	BufferedImage pie_party_packingm_byvoter;
	
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
		System.out.println("saveAsPng2 mode "+VTD.display_mode+" "+Settings.national_map+" "+VTD.outline_vtds);
		String path2 = path.substring(0,path.indexOf(".png"))+"_small.png";
        Dimension d = component.getSize();
        MainFrame.mainframe.mapPanel.invalidate();
        MainFrame.mainframe.mapPanel.repaint();
        if( true) {
        	component.setSize(width,height);
        	component.doLayout();
        	
        } else {
        	width = d.width;
        	height = d.height;
        }
        MainFrame.mainframe.mapPanel.invalidate();
        MainFrame.mainframe.mapPanel.repaint();
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
        
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        //component.invalidate();
        //component.repaint();
        component.paint(graphics1);
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        //component.invalidate();
        component.print(graphics1);
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        component.print(graphics1);
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
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
					dm.fairnessScores[10], //racial vote dilution
					dm.fairnessScores[11], //descr. rep.
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
		double[] elec_counts = new double[Settings.num_candidates];
		double[] vote_counts = new double[Settings.num_candidates];
		double[] targets = new double[]{};

		try {
			double total_pvi = 0;
			double counted_districts = 0;
			double grand_total_votes = 0;
			int num_competitive = 0;
			double wasted_0 = 0;
			double wasted_1 = 0;
			
			String[] dcolumns = new String[16+Settings.num_candidates*2+dem_col_names.length*2];
			String[][] ddata = new String[dm.districts.size()][];
			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
			String[][] cdata = new String[Settings.num_candidates][];
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
			//dcolumns[4] = "FV PVI"; //
			
			dcolumns[4] = "Vote gap";
			dcolumns[5] = "Wasted votes";
			
			dcolumns[6] = "FV Safe D";
			dcolumns[7] = "FV Lean D";
			dcolumns[8] = "FV Tossup";
			dcolumns[9] = "FV Lean R";
			dcolumns[10] = "FV Safe R";			
			//vote_gap_by_district
			
			dcolumns[11] = "Pop per seats";//"Self-entropy";
			dcolumns[12] = "Compactness";
			dcolumns[13] = "Area";
			dcolumns[14] = "Paired edge length";
			dcolumns[15] = "Unpaired edge length";
			
			
			for( int i = 0; i < Settings.num_candidates; i++) {
				try {
					elec_counts[i] = 0;
					vote_counts[i] = 0;
					dcolumns[i+16] = ""+cands.get(i)+" vote %";
					dcolumns[i+16+Settings.num_candidates] = ""+cands.get(i)+" votes";
				} catch (Exception ex) { }
			}
			for( int i = 0; i < dem_col_names.length; i++) {
				try {
				dcolumns[i+16+Settings.num_candidates*2] = ""+dem_col_names[i]+" %";
				dcolumns[i+16+Settings.num_candidates*2+dem_col_names.length] = ""+dem_col_names[i]+" pop";
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
				result[1] = District.popular_vote_to_elected(result[0], i,0);
				
				double[] demo_result = District.popular_vote_to_elected(demo[i], i, 0);
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
				double[] seats = new double[]{};
				if( result[0].length > 0) {
					seats = DistrictMap.getSeats_new(result[0][0]/(result[0][0]+result[0][1]),  Settings.seats_in_district(i));
				}
				for( int j = 0; j < seats.length; j++) {
					tot_seats[j] += seats[j];
				}
				//String winner = ""+iwinner;
				ddata[i][0] = ""+(i+1);
				ddata[i][1] = integer.format(d.getPopulation());
				ddata[i][2] = ""+winner;
				ddata[i][3] = ""+pviw;
				//ddata[i][4] = ""+fv_pviw;
				
				ddata[i][4] = ""+dm.vote_gap_by_district[i];
				ddata[i][5] = ""+dm.wasted_votes_by_district[i];
				if( seats.length == 0) {
					seats = new double[5];
				}
				
				ddata[i][6] = ""+seats[0];
				ddata[i][7] = ""+seats[1];
				ddata[i][8] = ""+seats[2];
				ddata[i][9] = ""+seats[3];
				ddata[i][10] = ""+seats[4];
				
				ddata[i][11] = ""+integer.format(d.getPopulation()/Settings.seats_in_district(i));//  decimal.format(self_entropy*conversion_to_bits)+" bits";
				ddata[i][12] = ""+d.iso_quotient;
				ddata[i][13] = ""+d.area;
				ddata[i][14] = ""+d.paired_edge_length;
				ddata[i][15] = ""+d.unpaired_edge_length;
				for( int j = 16; j < ddata[i].length; j++) {
					ddata[i][j] = "";
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+16] = ""+(result[0][j]/total);
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+16+Settings.num_candidates] = ""+integer.format(result[0][j]);
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+16+Settings.num_candidates*2] = ""+decimal.format(demo_pct[i][j]);
					votes_by_dem[j] += total_votes*demo_pct[i][j];
					vote_margins_by_dem[j] += dm.vote_gap_by_district[i]*demo_pct[i][j];
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+16+Settings.num_candidates*2+dem_col_names.length] = ""+integer.format(demo[i][j]);
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
			double total_seats = Settings.total_seats();
			targets = District.popular_vote_to_elected_for_seats(pop_by_dem,(int)total_seats,-1,false);
			/*for( int i = 0; i < targets.length; i++) {
				targets[i] = Math.round(total_seats*pop_by_dem[i]/tot_pop);
			}*/
			
			double pop_per_seat = tot_pop/total_seats;
			double pop_per_seat_wrong = tot_pop/(total_seats+1);
			if( Settings.quota_method == Settings.QUOTA_METHOD_HARE) {
				pop_per_seat_wrong = pop_per_seat;
			}
			double[] min_votes_needed_for_seat = dm.getMinVotesNeededForSeat(demo, pop_per_seat_wrong);
			
			String[] ecolumns = new String[]{"Ethnicity","Population","Vote dilution","% Wasted votes","Victory margins","Votes","Straight vote descr. rep.","Target seats","Votes for next seat"};
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
						integer.format(targets[i]),
						integer.format(min_votes_needed_for_seat[i]),
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
					new String[]{""+decimal.format(dm.get_partisan_gerrymandering()),"Packing/cracking asymmetry (%)"},
					new String[]{""+integer.format(dm.total_vote_gap),"Competitiveness (victory margin)"},
					new String[]{""+decimal.format(dm.calcDisproporionality()),"Disproporionality"},
					//new String[]{""+decimal.format(dm.fairnessScores[8]),"Representation imbalance (global)"},
					new String[]{""+decimal.format(dm.getRacialVoteDilution()),"Racial vote dilution"},
					new String[]{"",""},
					//new String[]{""+integer.format(wasted_votes),"Wasted votes (count)"},
					//new String[]{""+decimal.format(dm.fairnessScores[1]*conversion_to_bits),"Representation imbalance (local)"},
					new String[]{""+decimal.format(egap),"Efficiency gap (pct)"},
					new String[]{""+decimal.format(0.01*egap*(double)Settings.num_districts),"Adj. efficiency gap (seats)"},
					//new String[]{""+decimal.format(total_pvi / counted_districts),"Avg. PVI"},
					//new String[]{""+integer.format(num_competitive),"Competitive elections (< 5 PVI)"},
					//new String[]{""+decimal.format(dm.fairnessScores[4]*conversion_to_bits),"Voting power imbalance (relative entropy)"},
					new String[]{""+integer.format(dm.getDescrVoteImbalance()),"Undescribed voters"},
					//Descriptive representation
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
			
			populateCountiesTable();
		} catch (Exception ex) {
			System.out.println("ex ad "+ex);
			ex.printStackTrace();
		}
		
		Color[] district_colors = new Color[Settings.num_districts];
		Color tossup = new Color(128,0,128);
		Color lean = new Color(255,128,255);
		Color safe = new Color(192,192,192);
		
		double[] dd0 = new double[3];

		for( int i = 0; i < Settings.num_districts; i++) {
			dd0[dm.getFVColorIndex(i)]++;
			
			//double vote_gap = Math.abs(dm.getVoteGapPct(i, VTD.display_mode == VTD.DISPLAY_MODE_PARTISAN_PACKING_MEAN));
			/*
			if( vote_gap < 0.04) {
				dd0[0]++;
			} else 
			if( vote_gap < 0.08) {
				dd0[1]++;
			} else {
				dd0[2]++;
			}
			*/
		}
		pie_party_comp_digital2 = piechart.drawPieChart(200,dd0,new Color[]{
				 tossup,
				 lean,
				 safe,
		}, true);
		pie_party_comp_digital = piechart.drawPieChart(200,new double[]{
				tot_seats[2],
				tot_seats[1]+tot_seats[3],
				tot_seats[0]+tot_seats[4],
		},new Color[]{
				 tossup,
				 lean,
				 safe,
		}, true);

		pie_party_votes = piechart.drawPieChart(200,vote_counts,FeatureCollection.standard_district_colors, true); 
		pie_party_seats = piechart.drawPieChart(200,elec_counts,FeatureCollection.standard_district_colors, true); 
		pie_party_seats_frac = piechart.drawPieChart(200,new double[]{
				tot_seats[0],
				tot_seats[1],
				tot_seats[2],
				tot_seats[3],
				tot_seats[4],
		},new Color[]{
				 new Color(0x00,0,0xff),
				 new Color(0x40,0,0xb0),
				 new Color(0x80,0,0x80),
				 new Color(0xb0,0,0x40),
				 new Color(0xff,0,0x00),
		}, true);
		pie_eth_pop    = piechart.drawPieChart(200,pop_by_dem,FeatureCollection.demo_district_colors, true); 
		pie_eth_target = piechart.drawPieChart(200,targets,FeatureCollection.demo_district_colors, true); 
		pie_eth_descr  = piechart.drawPieChart(200,winners_by_ethnicity,FeatureCollection.demo_district_colors, true);
		
		double[] dr = new double[votes_by_dem.length];
		double tot = 0;
		for( int i = 0; i < votes_by_dem.length; i++) {
			tot += votes_by_dem[i];
		}
		tot *= 0.003;
		//System.out.println("tot: "+tot);
		for( int i = 0; i < dr.length; i++) {
			//System.out.println("i votes: "+votes_by_dem[i]);
			if( votes_by_dem[i] < tot) { //2000
				dr[i] = 0;
				
				continue;
			}
			dr[i] = 1.0-vote_margins_by_dem[i]/votes_by_dem[i];
			//System.out.println("i dr: "+dr[i]);
			if( dr[i] < 0) {
				dr[i] = 0;
			}
		}
		pie_eth_power  = piechart.drawPieChart(200,dr,FeatureCollection.demo_district_colors, true); 

		boolean t = Settings.divide_packing_by_area;
		Settings.divide_packing_by_area = false;
		
		boolean use_vote_gap_for_arc = false;

		Vector<Triplet<Double,Color,Integer>> triplet  = new Vector<Triplet<Double,Color,Integer>>();
		for( int i = 0; i < Settings.num_districts; i++) {
			Color c = dm.getVoteGapByPartyColor(i,false);
			double p = c.getRed()-c.getBlue();
			triplet.add(new Triplet<Double,Color,Integer>(p,c,i));
		}
		Collections.sort(triplet);
		double[] dd = new double[triplet.size()];
		Color[] cc = new Color[triplet.size()];
		double[] dd_r = new double[triplet.size()];
		Color[] cc_r = new Color[triplet.size()];
		for( int i = 0; i < dd.length; i++) {
			int dist = triplet.get(i).c;
			dd[i] = use_vote_gap_for_arc ? Math.abs(dm.getVoteGapForDistrict(dist,false)) : Settings.seats_in_district(dist);
			cc[i] = triplet.get(i).b;
			dd_r[i] = dd[i];
			cc_r[i] = dm.getVoteGapByDemoColor(dist,false);
		}
		//getVoteGapForDistrict
		pie_party_packing = piechart.drawPieChart(200,dd,cc,false);
		pie_eth_packing = piechart.drawPieChart(200,dd_r,cc_r,false);


		triplet  = new Vector<Triplet<Double,Color,Integer>>();
		for( int i = 0; i < Settings.num_districts; i++) {
			Color c = dm.getVoteGapByPartyColor(i,true);
			double p = c.getRed()-c.getBlue();
			triplet.add(new Triplet<Double,Color,Integer>(p,c,i));
		}
		Collections.sort(triplet);
		dd = new double[triplet.size()];
		cc = new Color[triplet.size()];
		dd_r = new double[triplet.size()];
		cc_r = new Color[triplet.size()];
		for( int i = 0; i < dd.length; i++) {
			int dist = triplet.get(i).c;
			dd[i] = use_vote_gap_for_arc ? Math.abs(dm.getVoteGapForDistrict(dist,true)) : Settings.seats_in_district(dist);
			cc[i] = triplet.get(i).b;
			dd_r[i] = dd[i];
			cc_r[i] = dm.getVoteGapByDemoColor(dist,true);
		}	
		pie_party_packingm = piechart.drawPieChart(200,dd,cc,false);
		pie_eth_packingm = piechart.drawPieChart(200,dd_r,cc_r,false);
		
		use_vote_gap_for_arc = true;

		triplet  = new Vector<Triplet<Double,Color,Integer>>();
		for( int i = 0; i < Settings.num_districts; i++) {
			Color c = dm.getVoteGapByPartyColor(i,false);
			double p = c.getRed()-c.getBlue();
			triplet.add(new Triplet<Double,Color,Integer>(p,c,i));
		}
		Collections.sort(triplet);
		dd = new double[triplet.size()];
		cc = new Color[triplet.size()];
		dd_r = new double[triplet.size()];
		cc_r = new Color[triplet.size()];
		for( int i = 0; i < dd.length; i++) {
			int dist = triplet.get(i).c;
			dd[i] = use_vote_gap_for_arc ? Math.abs(dm.getVoteGapForDistrict(dist,false)) : Settings.seats_in_district(dist);
			cc[i] = triplet.get(i).b;
			dd_r[i] = dd[i];
			cc_r[i] = dm.getVoteGapByDemoColor(dist,false);
		}
		//getVoteGapForDistrict
		pie_party_packing_byvoter = piechart.drawPieChart(200,dd,cc,false);
		pie_eth_packing_byvoter = piechart.drawPieChart(200,dd_r,cc_r,false);


		triplet  = new Vector<Triplet<Double,Color,Integer>>();
		for( int i = 0; i < Settings.num_districts; i++) {
			Color c = dm.getVoteGapByPartyColor(i,true);
			double p = c.getRed()-c.getBlue();
			triplet.add(new Triplet<Double,Color,Integer>(p,c,i));
		}
		Collections.sort(triplet);
		dd = new double[triplet.size()];
		cc = new Color[triplet.size()];
		dd_r = new double[triplet.size()];
		cc_r = new Color[triplet.size()];
		for( int i = 0; i < dd.length; i++) {
			int dist = triplet.get(i).c;
			dd[i] = use_vote_gap_for_arc ? Math.abs(dm.getVoteGapForDistrict(dist,true)) : Settings.seats_in_district(dist);
			cc[i] = triplet.get(i).b;
			dd_r[i] = dd[i];
			cc_r[i] = dm.getVoteGapByDemoColor(dist,true);
		}	
		pie_party_packingm_byvoter = piechart.drawPieChart(200,dd,cc,false);
		pie_eth_packingm_byvoter = piechart.drawPieChart(200,dd_r,cc_r,false);
		
		
		Settings.divide_packing_by_area = t;

		

		
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
	
	public void populateCountiesTable() {
		
		String pop_col = Applet.mainFrame.project.population_column;
		String county_col = Applet.mainFrame.project.county_column;
		if( county_col == null || county_col.length() == 0) {
			return;
		}
		String[] demonames = Applet.mainFrame.project.demographic_columns_as_array();
		Vector<String> c_names = Applet.mainFrame.project.election_columns;
		String[] ecolumns = new String[2+demonames.length+c_names.size()];
		ecolumns[0] = "COUNTY";
		ecolumns[1] = "POPULATION";
		for( int i = 0; i < demonames.length; i++) {
			ecolumns[i+2] = demonames[i];
		}
		for( int i = 0; i < c_names.size(); i++) {
			ecolumns[i+2+demonames.length] = c_names.get(i);
		}
		Vector<String> countynames = new Vector<String>();
		Hashtable<String,Vector<VTD>> hash = new Hashtable<String,Vector<VTD>>();
		for( int i = 0; i < featureCollection.features.size(); i++) {
			VTD f = featureCollection.features.get(i);
			String s = f.properties.get(county_col).toString();
			Vector<VTD> vf = hash.get(s);
			if( vf == null) {
				vf = new Vector<VTD>();
				hash.put(s,vf);
				countynames.add(s);
			}
			vf.add(f);
		}
		Collections.sort(countynames);
		String[][] edata = new String[countynames.size()][];
		for( int i = 0; i < countynames.size(); i++) {
			String scounty = countynames.get(i);
			double pop = 0;
			double[] dd = new double[demonames.length];
			for( int j = 0; j < dd.length; j++) {
				dd[j] = 0;
			}
			double[] cc = new double[c_names.size()];
			for( int j = 0; j < cc.length; j++) {
				cc[j] = 0;
			}
			edata[i] = new String[2+dd.length+c_names.size()];
			edata[i][0] = scounty;
			Vector<VTD> vf = hash.get(scounty);
			for( int k = 0; k < vf.size(); k++) {
				geography.Properties p = vf.get(k).properties;
				try { 
					if( p.get(pop_col) != null) {
						pop += Double.parseDouble(p.get(pop_col).toString().replaceAll(",",""));
					}
				} catch (Exception ex) {
					
				}
				for( int j = 0; j < dd.length; j++) {
					double d = Double.parseDouble(p.get(demonames[j]).toString().replaceAll(",",""));
					dd[j] += d;
				}
				for( int j = 0; j < c_names.size(); j++) {
					double d = Double.parseDouble(p.get(c_names.get(j)).toString().replaceAll(",",""));
					cc[j] += d;
				}
			}
			edata[i][1] = ""+pop;
			for( int j = 0; j < demonames.length; j++) {
				edata[i][j+2] = ""+dd[j];
			}
			for( int j = 0; j < c_names.size(); j++) {
				edata[i][j+2+demonames.length] = ""+cc[j];
			}
		}

		TableModel tm0 = new DefaultTableModel(edata,ecolumns);
		countyTable.setModel(tm0);
	}
	public PanelStats() {

		initComponents();
	}
	private void initComponents() {
		this.setLayout(null);
		this.setSize(new Dimension(449, 882));
		this.setPreferredSize(new Dimension(838, 801));
		
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
		ethnicityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

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
		toHTMLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToHtml(true);
			}
		});
		toHTMLButton.setBounds(89, 8, 117, 29);
		
		add(toHTMLButton);
		
		lblByCounty = new JLabel("By county");
		lblByCounty.setBounds(26, 620, 226, 14);
		add(lblByCounty);
		
		scrollPane_4 = new JScrollPane();
		scrollPane_4.setBounds(26, 646, 791, 136);
		add(scrollPane_4);
		
		countyTable = new JTable();
		scrollPane_4.setViewportView(countyTable);
		
		button_3 = new JButton("copy");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionEvent nev = new ActionEvent(countyTable, ActionEvent.ACTION_PERFORMED, "copy");
				countyTable.selectAll();
				countyTable.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);				
			}
		});
		button_3.setBounds(728, 614, 89, 23);
		add(button_3);
		
		btnExplainStats = new JButton("Explain stats");
		btnExplainStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Applet.browseTo(stats_descriptions_url);
			}
		});
		btnExplainStats.setBounds(205, 8, 117, 29);
		add(btnExplainStats);
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
	public final JButton toHTMLButton = new JButton("to html");
	public JLabel lblByCounty;
	public JScrollPane scrollPane_4;
	public JTable countyTable;
	public JButton button_3;
	public JButton btnExplainStats;
	
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
	    	System.out.println("url: "+url);
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
        		String val = (String)t.getValueAt(i, j);
        		if( val == null || val.equals("null")) {
        			val = "";
        		}
        		str += "    <td>"+val+"</td>\n";
        	}
        	str +="  </tr>\n";
    		
    	}
    	str += "</table>\n";
    	return str;
    }



	@Override
	public void eventOccured() {
		getStats();
		MainFrame.mainframe.pie.invalidate();
		MainFrame.mainframe.pie.repaint();
		MainFrame.mainframe.progressBar.setString( featureCollection.ecology.generation+" iterations");
	}
	public void exportMaps(String write_folder, int res,boolean outline) {
		int num_maps_temp = Settings.num_maps_to_draw;
		int display_mode_temp = VTD.display_mode;
		Settings.num_maps_to_draw = 1;
		
		boolean divide_packing = Settings.divide_packing_by_area;
		boolean maplines = VTD.outline_vtds;
		boolean outline_state = VTD.outline_state;
		boolean outline_county = VTD.outline_counties;
		boolean draw_labels = VTD.showDistrictLabels;
		VTD.outline_vtds = false;//outline;
		VTD.outline_state = true;
		VTD.outline_counties = false;
		MapPanel.FSAA = 4;//Feature.outline_vtds ? 4 : 1;
		
		
		///====begin insert
		VTD.outline_districts = true;
		VTD.showDistrictLabels = true;
		Settings.divide_packing_by_area = false;
		VTD.showDistrictLabels = false;
		
		VTD.USE_DISCRETE_COLORS = true;
		Settings.divide_packing_by_area = false;
		

		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing_digital.png",res,res);

		if( false) {
			return;
		}

		Settings.divide_packing_by_area = true;
		VTD.display_mode = VTD.DISPLAY_MODE_DEMOGRAPHICS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts_demographics_vtd.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_VOTES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts_votes_vtd.png",res,res);
		Settings.divide_packing_by_area = false;
		
		//VTD.outline_districts = false;

		VTD.display_mode = VTD.DISPLAY_MODE_NORMAL;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts.png",res,res);
		VTD.showDistrictLabels = true;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts_labels.png",res,res);
		VTD.showDistrictLabels = false;

		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing_digital.png",res,res);
		


		VTD.display_mode = VTD.DISPLAY_MODE_DIST_SEATS;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_seats_won_digital.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DESCR;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_descr_rep_digital.png",res,res);

		VTD.USE_GRAY = true;
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DESCR;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_descr_rep_digital_gray.png",res,res);
		VTD.USE_GRAY = false;

		Settings.divide_packing_by_area = false;
		VTD.USE_DISCRETE_COLORS = false;

		
		VTD.display_mode = VTD.DISPLAY_MODE_NORMAL;		
		VTD.showDistrictLabels = true;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts_labels.png",res,res);
		VTD.showDistrictLabels = false;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_districts.png",res,res);
		
		Settings.divide_packing_by_area = false;
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_SEATS;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_seats_won.png",res,res);
		Settings.divide_packing_by_area = true;
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_SEATS;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_seats_won_area.png",res,res);
		Settings.divide_packing_by_area = false;

		
		System.out.println("2");
		Settings.divide_packing_by_area = false;
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DESCR;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_descr_rep.png",res,res);
		Settings.divide_packing_by_area = true;
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DESCR;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_descr_rep_area.png",res,res);
		Settings.divide_packing_by_area = false;

		
		
		
		Settings.divide_packing_by_area = false;		
		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING_MEAN;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing_mean.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING_MEAN;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_racial_packing_mean.png",res,res);

		Settings.divide_packing_by_area = true;		
		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING_MEAN;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing_mean_area.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING_MEAN;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_racial_packing_mean_area.png",res,res);
		Settings.divide_packing_by_area = false;

		VTD.display_mode = VTD.DISPLAY_MODE_DIST_POP;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_pop.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_COMPACTNESS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_compactness.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_WASTED_VOTES;		
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_wasted_votes.png",res,res);
		

		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_racial_packing.png",res,res);
		
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_VOTE;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_votes.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DEMO;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_demographics.png",res,res);
				
		Settings.divide_packing_by_area = true;
		VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_partisan_packing_area.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_racial_packing_area.png",res,res);

		VTD.display_mode = VTD.DISPLAY_MODE_DIST_VOTE;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_votes_area.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_DIST_DEMO;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_district_demographics_area.png",res,res);
		System.out.println("3");
		Settings.divide_packing_by_area = false;
		
		VTD.outline_districts = false;

		VTD.display_mode = VTD.DISPLAY_MODE_COUNTY_SPLITS;		
		VTD.outline_counties = true;
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_splits.png",res,res);
		VTD.outline_counties = false;
		System.out.println("4");
	
		VTD.display_mode = VTD.DISPLAY_MODE_VOTES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_votes.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_DEMOGRAPHICS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_demographics.png",res,res);
		System.out.println("5");
		
		Settings.divide_packing_by_area = true;
		VTD.display_mode = VTD.DISPLAY_MODE_VOTES;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_votes_area.png",res,res);
		VTD.display_mode = VTD.DISPLAY_MODE_DEMOGRAPHICS;			
		saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_vtd_demographics_area.png",res,res);
	
	
		VTD.outline_counties = true;
		VTD.display_mode = VTD.DISPLAY_MODE_COUNTIES;			
		//saveAsPng(MainFrame.mainframe.mapPanel,write_folder+"map_counties.png",res,res);
		VTD.outline_counties = false;
	
	
		VTD.showDistrictLabels = draw_labels;
		VTD.outline_vtds = maplines;
		VTD.outline_counties = outline_county;
		VTD.outline_state = outline_state;
		MapPanel.FSAA = VTD.outline_vtds ? 4 : 1;
		Settings.num_maps_to_draw = num_maps_temp;
		VTD.display_mode = display_mode_temp;
		MapPanel.override_size = -1;
		Settings.divide_packing_by_area = divide_packing;
	}

	
	public void exportTransparent() {
		MainFrame.mainframe.ip.addHistory("EXPORT NATIONAL");
		String write_folder = Download.getStartPath()+MainFrame.mainframe.project.district_column+File.separator
				+"national"+File.separator;
		new File(write_folder).mkdirs();

		boolean t = Settings.national_map;
		
		Settings.setNationalMap( true);
		
		exportMaps(write_folder,1024,false);

		Settings.setNationalMap( t);
	}
	public void exportToHtml(boolean images) {
		exportToHtml(images,false);
	}
	public void exportPieCharts() {
		MainFrame.mainframe.ip.addHistory("EXPORT PIE");

		String write_folder = Download.getStartPath()+MainFrame.mainframe.project.district_column+File.separator;

		/*
		BufferedImage pie_party_votes;
		BufferedImage pie_party_seats;
		BufferedImage pie_party_seats_frac;
		BufferedImage pie_eth_pop;
		BufferedImage pie_eth_target;
		BufferedImage pie_eth_descr;
		BufferedImage pie_eth_power;
		*/
		//pie_eth_packing

        try { ImageIO.write(pie_party_comp_digital,"png", new File(write_folder+"pie_party_comp_digital.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_comp_digital2,"png", new File(write_folder+"pie_party_comp_digital_district.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_votes,"png", new File(write_folder+"pie_party_votes.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_seats,"png", new File(write_folder+"pie_party_seats.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_seats_frac,"png", new File(write_folder+"pie_party_seats_frac.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_eth_pop,"png", new File(write_folder+"pie_eth_pop.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_eth_target,"png", new File(write_folder+"pie_eth_target.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_eth_descr,"png", new File(write_folder+"pie_eth_descr.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_eth_power,"png", new File(write_folder+"pie_eth_power.png")); }
        catch(Exception ex) { ex.printStackTrace(); }

        try { ImageIO.write(pie_eth_packing,"png", new File(write_folder+"pie_eth_packing.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_packing,"png", new File(write_folder+"pie_party_packing.png")); }
        catch(Exception ex) { ex.printStackTrace(); }

        try { ImageIO.write(pie_eth_packingm,"png", new File(write_folder+"pie_eth_packingm.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_packingm,"png", new File(write_folder+"pie_party_packingm.png")); }
        catch(Exception ex) { ex.printStackTrace(); }

        try { ImageIO.write(pie_eth_packing_byvoter,"png", new File(write_folder+"pie_eth_packing_byvoter.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_packing_byvoter,"png", new File(write_folder+"pie_party_packing_byvoter.png")); }
        catch(Exception ex) { ex.printStackTrace(); }

        try { ImageIO.write(pie_eth_packingm_byvoter,"png", new File(write_folder+"pie_eth_packingm_byvoter.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
        try { ImageIO.write(pie_party_packingm_byvoter,"png", new File(write_folder+"pie_party_packingm_byvoter.png")); }
        catch(Exception ex) { ex.printStackTrace(); }
	
	}
	
	public void exportToHtml(boolean images, boolean embedded) {
		System.out.println("1");
		boolean national = Settings.national_map;
		Settings.setNationalMap(false);
		MainFrame.mainframe.ip.addHistory("EXPORT HTML");

		String partiesStr = getAsHtml(partiesTable);
		String raceStr = getAsHtml(ethnicityTable);
		String sumStr = getAsHtml(summaryTable);
		String districtsStr = getAsHtml(districtsTable);
		String seatsStr = getAsHtml(MainFrame.mainframe.frameSeatsVotesChart.table);
		
		URL header_path = Applet.class.getResource("/resources/header.php");
		URL footer_path = Applet.class.getResource("/resources/footer.php");
		URL style_sheet = Applet.class.getResource("/resources/styles2.css");
		
		String write_folder = Download.getStartPath()+MainFrame.mainframe.project.district_column+File.separator;
		String write_folder2 = Download.getStartPath();
		new File(write_folder).mkdirs();
		
		saveURL(write_folder+"style.css",style_sheet);
		
		System.out.println("1");

		//MapPanel.override_size = 1024;
		if( images) {
			saveAsPng(MainFrame.mainframe.frameSeatsVotesChart.panel,write_folder+"seats_votes.png");
			saveAsPng(MainFrame.mainframe.frameSeatsVotesChart.panelRanked.panel,write_folder+"sorted_districts.png");
	
			exportMaps(write_folder,1024,true);
		
			System.out.println("6");
			MainFrame.mainframe.saveData(new File(write_folder2+"vtd_data.txt"), 1,false);
			MainFrame.mainframe.saveData(new File(write_folder2+"vtd_data.dbf"), 2,false);
			System.out.println("7");
		}
	

		String prepend = Download.states[Download.istate]+"/"+Download.cyear+"/"+Applet.mainFrame.project.district_column+"/";
		prepend = prepend.replaceAll(" ", "%20");

		String html = "";
		if( !embedded) {
			prepend = "";
			html += getURLtext(header_path);
			html +="<i>This page was generated on "+ new SimpleDateFormat("yyyy.MM.dd").format(new Date())+" by <a href='http://autoredistrict.org'>Auto-Redistrict</a>.</i><br/><br/>\n";
		}
		if( !embedded) {
			html +="<h3>VTD district assignments</h3><br/>\n";
			html +="<a href='"+prepend+"../vtd_data.txt'>vtd_data.txt (tab-delimited)</a><br/>\n";
			html +="<a href='"+prepend+"../vtd_data.dbf'>vtd_data.dbf (dbase/ESRI)</a><br/>\n";
			html +="<br/><br/>\n";
		}

		html +="<h3>Maps (click to enlarge)</h3><br/>\n";
		html +="<table>\n";
		html +="<tr>\n";
		html +="<td>Districts</td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_districts_labels.png'><img src='"+prepend+"./map_districts_labels_small.png' width=100></br>Labeled districts</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_districts.png'><img src='"+prepend+"./map_districts_small.png' width=100></br>Unlabeled districts</a></center></td>\n";
		html +="</tr>\n";
		html +="<tr>\n";
		html +="<td>Geometry</td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_pop.png'><img src='"+prepend+"./map_district_pop_small.png' width=100></br>Population difference</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_compactness.png'><img src='"+prepend+"./map_district_compactness_small.png' width=100></br>Compactness</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_splits.png'><img src='"+prepend+"./map_splits_small.png' width=100></br>Splits</a></center></td>\n";
		html +="</tr>\n";
		html +="<tr>\n";
		html +="<td>Fairness - by district</td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_partisan_packing.png'><img src='"+prepend+"./map_district_partisan_packing_small.png' width=100></br>Partisan vote packing</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_racial_packing.png'><img src='"+prepend+"./map_district_racial_packing_small.png' width=100></br>Racial vote packing</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_votes.png'><img src='"+prepend+"./map_district_votes_small.png' width=100></br>District vote balance</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_demographics.png'><img src='"+prepend+"./map_district_demographics_small.png' width=100></br>District demographics</a></center></td>\n";
		html +="</tr>\n";
		html +="<td>Fairness - by density</td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_partisan_packing_area.png'><img src='"+prepend+"./map_district_partisan_packing_area_small.png' width=100></br>Partisan vote packing</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_racial_packing_area.png'><img src='"+prepend+"./map_district_racial_packing_area_small.png' width=100></br>Racial vote packing</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_votes_area.png'><img src='"+prepend+"./map_district_votes_area_small.png' width=100></br>District vote balance</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_district_demographics_area.png'><img src='"+prepend+"./map_district_demographics_area_small.png' width=100></br>District demographics</a></center></td>\n";
		html +="</tr>\n";
		/*
		html +="<tr>\n";
		html +="<td>Counties</td>\n";
		html +="<td align='center'><center><a href='./map_counties.png'><img src='"+prepend+"./map_counties_small.png' width=100></br>Counties</a></center></td>\n";
		html +="</tr>\n";
		*/
		html +="<tr>\n";
		html +="<td>VTDs</td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_vtd_votes.png'><img src='"+prepend+"./map_vtd_votes_small.png' width=100></br>VTD vote balance</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_vtd_demographics.png'><img src='"+prepend+"./map_vtd_demographics_small.png' width=100></br>VTD demographics</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_vtd_votes_area.png'><img src='"+prepend+"./map_vtd_votes_area_small.png' width=100></br>VTD vote balance<br/>(density)</a></center></td>\n";
		html +="<td align='center'><center><a target='_blank' href='"+prepend+"./map_vtd_demographics_area.png'><img src='"+prepend+"./map_vtd_demographics_area_small.png' width=100></br>VTD demographics<br/>(density)</a></center></td>\n";
		html +="</tr>\n";
		html +="</table>\n";
		html +="</br>\n";

		html +="<h3>Seats / votes curve - Vote packing</h3><br/>\n";
		html +="<center><img src='"+prepend+"./seats_votes.png'> <img src='"+prepend+"./sorted_districts.png'></center><br/>\n";

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
		if( embedded) {
			html +="<br/><br/>\n";
			html +="<h3>VTD district assignments</h3><br/>\n";
			html +="<a href='"+prepend+"../vtd_data.txt'>vtd_data.txt (tab-delimited)</a><br/>\n";
			html +="<a href='"+prepend+"../vtd_data.dbf'>vtd_data.dbf (dbase/ESRI)</a><br/>\n";
			html +="<br/><br/>\n";
		}

		html +="<br/><br/>\n";
		
		System.out.println("8");

		if( !embedded) {
			html += getURLtext(footer_path);
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats_summary.html");
			fos.write(sumStr.getBytes());
			fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats_party.html");
			fos.write(partiesStr.getBytes());
			fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats_district.html");
			fos.write(districtsStr.getBytes());
			fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats_race.html");
			fos.write(raceStr.getBytes());
			fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
		try {
			FileOutputStream fos = new FileOutputStream(write_folder+"stats_seats.html");
			fos.write(seatsStr.getBytes());
			fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }


		try {
			FileOutputStream fos = new FileOutputStream(write_folder+(embedded?"embedded_":"")+"stats.html");
			fos.write(html.getBytes());
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String url = "file:///"+(write_folder+(embedded?"embedded_":"")+"stats.html").replaceAll("\\\\", "/").replaceAll(" ", "%20");
		
		System.out.println("9");

		//url = URLEncoder.encode(url);
		if( !embedded) {
			//Applet.browseTo(url);
		}
		
		///=====end insert
		
		

		/*
		excel.ExportThread thread = new excel.ExportThread();
		thread.export(summaryTable, districtsTable, partiesTable, ethnicityTable, MainFrame.mainframe.frameSeatsVotesChart.table);
		*/
		
		Settings.setNationalMap(national);
	}
}
