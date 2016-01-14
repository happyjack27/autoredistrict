package ui;

import geography.Feature;
import geography.FeatureCollection;

import javax.swing.*;
import javax.swing.table.*;

import solutions.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.text.*;
import java.util.*;
import java.awt.event.*;

public class PanelStats extends JPanel implements iDiscreteEventListener {
	DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	Color[] dmcolors = null;

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
				

				Settings.getAnnealingFloor( featureCollection.ecology.generation),

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
					

					Settings.getAnnealingFloor( featureCollection.ecology.generation),

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
		
		try {
			double total_pvi = 0;
			double counted_districts = 0;
			double grand_total_votes = 0;
			int num_competitive = 0;
			double wasted_0 = 0;
			double wasted_1 = 0;
			
			//=== by district
			String[] dcolumns = new String[11+Settings.num_candidates*2+dem_col_names.length*2];
			String[][] ddata = new String[dm.districts.size()][];
			if( dmcolors == null || dmcolors.length != dm.districts.size()) {
				dmcolors = new Color[dm.districts.size()];
			}
			dcolumns[0] = "District";
			dcolumns[1] = "Population";
			dcolumns[2] = "Winner";
			dcolumns[3] = "PVI"; //
			
			dcolumns[4] = "Vote gap";
			dcolumns[5] = "Wasted votes";
			
			//vote_gap_by_district
			
			dcolumns[6] = "Pop per seats";//"Self-entropy";
			dcolumns[7] = "Compactness";
			dcolumns[8] = "Area";
			dcolumns[9] = "Paired edge length";
			dcolumns[10] = "Unpaired edge length";
			
			
			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
			String[][] cdata = new String[Settings.num_candidates][];
			
			double[] elec_counts = new double[Settings.num_candidates];
			double[] vote_counts = new double[Settings.num_candidates];
			double tot_votes = 0;
			for( int i = 0; i < Settings.num_candidates; i++) {
				elec_counts[i] = 0;
				vote_counts[i] = 0;
				dcolumns[i+11] = ""+cands.get(i)+" vote %";
				dcolumns[i+11+Settings.num_candidates] = ""+cands.get(i)+" votes";
			}
			for( int i = 0; i < dem_col_names.length; i++) {
				dcolumns[i+11+Settings.num_candidates*2] = ""+dem_col_names[i]+" %";
				dcolumns[i+11+Settings.num_candidates*2+dem_col_names.length] = ""+dem_col_names[i]+" pop";
			}
			
			double total_population = 0;
			
			double[] pop_by_dem = new double[dem_col_names.length];
			for( int i = 0; i < pop_by_dem.length; i++) { pop_by_dem[i] = 0; }
			double[] votes_by_dem = new double[dem_col_names.length];
			for( int i = 0; i < votes_by_dem.length; i++) { votes_by_dem[i] = 0; }
			double[] vote_margins_by_dem = new double[dem_col_names.length];
			for( int i = 0; i < vote_margins_by_dem.length; i++) { vote_margins_by_dem[i] = 0; }
			double[][] demo = dm.getDemographicsByDistrict();
			double[][] demo_pct = new double[demo.length][];
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
	
			for( int i = 0; i < dm.districts.size(); i++) {
				try {
				dmcolors[i] = dm.getWastedVoteColor(i);
				ddata[i] = new String[dcolumns.length];
				District d = dm.districts.get(i);
				total_population += d.getPopulation();
				//String population = ""+(int)d.getPopulation();
				double[][] result = new double[2][];//d.getElectionResults();
				result[0] = d.getAnOutcome();
				result[1] = District.popular_vote_to_elected(result[0], i);
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
				//String winner = ""+iwinner;
				ddata[i][0] = ""+(i+1);
				ddata[i][1] = integer.format(d.getPopulation());
				ddata[i][2] = ""+winner;
				ddata[i][3] = ""+pviw;
				
				ddata[i][4] = ""+dm.vote_gap_by_district[i];
				ddata[i][5] = ""+dm.wasted_votes_by_district[i];
				
				ddata[i][6] = ""+integer.format(d.getPopulation()/Settings.seats_in_district(i));//  decimal.format(self_entropy*conversion_to_bits)+" bits";
				ddata[i][7] = ""+d.iso_quotient;
				ddata[i][8] = ""+d.area;
				ddata[i][9] = ""+d.paired_edge_length;
				ddata[i][10] = ""+d.unpaired_edge_length;
				for( int j = 11; j < ddata[i].length; j++) {
					ddata[i][j] = "";
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+11] = ""+(result[0][j]/total);
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+11+Settings.num_candidates] = ""+integer.format(result[0][j]);
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+11+Settings.num_candidates*2] = ""+decimal.format(demo_pct[i][j]);
					votes_by_dem[j] += total_votes*demo_pct[i][j];
					vote_margins_by_dem[j] += dm.vote_gap_by_district[i]*demo_pct[i][j];
				}	
				for( int j = 0; j < dem_col_names.length; j++) {
					ddata[i][j+11+Settings.num_candidates*2+dem_col_names.length] = ""+integer.format(demo[i][j]);
				}	
				} catch (Exception ex) {
					System.out.println("ex stats 1 "+ex);
					ex.printStackTrace();
				}
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
			
			String[] ecolumns = new String[]{"Ethnicity","Population","Vote dilution","% Wasted votes","Victory margins","Votes"};
			String[][] edata = new String[dem_col_names.length+1][];
			for( int i = 0; i < dem_col_names.length; i++) {
				edata[i] = new String[]{
						dem_col_names[i],
						integer.format(pop_by_dem[i]),
						decimal.format(ravg*vote_margins_by_dem[i]/votes_by_dem[i]),
						decimal.format(vote_margins_by_dem[i]/votes_by_dem[i]),
						decimal.format(vote_margins_by_dem[i]),
						decimal.format(votes_by_dem[i]),
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
				cdata[i] = new String[]{
						""+cands.get(i),
						""+integer.format(elec_counts[i]),
						""+integer.format(vote_counts[i]),//*total_population/tot_votes),
						""+(dm.wasted_votes_by_party[i]),
						""+(elec_counts[i]/((double)tot_seats)),
						""+(vote_counts[i]/tot_votes)
				};
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
			MainFrame.mainframe.frameRankedDist.setData(ranked_dists);
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
		lblSummary.setBounds(26, 15, 226, 14);
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
				excel.ExportThread thread = new excel.ExportThread();
				thread.export(summaryTable, districtsTable, partiesTable, ethnicityTable, MainFrame.mainframe.frameSeatsVotesChart.table);
			}
		});
		btnNewButton.setBounds(89, 8, 117, 29);
		
		add(btnNewButton);
		
		btnHtml = new JButton("html");
		btnHtml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyAsHtml(partiesTable);
			}
		});
		btnHtml.setBounds(629, 11, 89, 23);
		add(btnHtml);
		
		button_3 = new JButton("html");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyAsHtml(ethnicityTable);
			}
		});
		button_3.setBounds(629, 143, 89, 23);
		add(button_3);
		
		button_4 = new JButton("html");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyAsHtml(partiesTable);
			}
		});
		button_4.setBounds(629, 350, 89, 23);
		add(button_4);
		
		button_5 = new JButton("html");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyAsHtml(summaryTable);
			}
		});
		button_5.setBounds(228, 11, 89, 23);
		add(button_5);
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
	public final JButton btnNewButton = new JButton("To Excel");
	public JButton btnHtml;
	public JButton button_3;
	public JButton button_4;
	public JButton button_5;
	
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
    
    public void copyAsHtml(JTable t) {
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

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection strSel = new StringSelection(str);
		clipboard.setContents(strSel, null);    	
    }



	@Override
	public void eventOccured() {
		getStats();
		MainFrame.mainframe.progressBar.setString( featureCollection.ecology.generation+" iterations");
	}
}
