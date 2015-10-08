package ui;

import geography.Feature;
import geography.FeatureCollection;

import javax.swing.*;
import javax.swing.table.*;

import solutions.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
				
				dm.fairnessScores[1]*conversion_to_bits, //REP IMBALANCE
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
					
					dm.fairnessScores[1]*conversion_to_bits, //REP IMBALANCE
					dm.fairnessScores[4], //POWER IMBALANCE
					dm.fairnessScores[5], //WASTED VOTES TOTAL
					dm.fairnessScores[6], //WASTED VOTES IMBALANCE
					dm.fairnessScores[7], //seats / votes asymmetry
					

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
		
		} catch (Exception ex) {
			System.out.println("ex ad "+ex);
			ex.printStackTrace();
		}
		
		
		try {
			double total_pvi = 0;
			double counted_districts = 0;
			double grand_total_votes = 0;
			int num_competitive = 0;
			double wasted_0 = 0;
			double wasted_1 = 0;
			
			//=== by district
			String[] dcolumns = new String[9+Settings.num_candidates*2];
			String[][] ddata = new String[dm.districts.size()][];
			if( dmcolors == null || dmcolors.length != dm.districts.size()) {
				dmcolors = new Color[dm.districts.size()];
			}
			dcolumns[0] = "District";
			dcolumns[1] = "Population";
			dcolumns[2] = "Winner";
			dcolumns[3] = "PVI"; //
			dcolumns[4] = "Self-entropy";
			dcolumns[5] = "Compactness";
			dcolumns[6] = "Area";
			dcolumns[7] = "Paired edge length";
			dcolumns[8] = "Unpaired edge length";
			
			
			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
			String[][] cdata = new String[Settings.num_candidates][];
			
			double[] elec_counts = new double[Settings.num_candidates];
			double[] vote_counts = new double[Settings.num_candidates];
			double tot_votes = 0;
			for( int i = 0; i < Settings.num_candidates; i++) {
				elec_counts[i] = 0;
				vote_counts[i] = 0;
				dcolumns[i+9] = ""+i+" vote %";
				dcolumns[i+9+Settings.num_candidates] = ""+i+" votes";
			}
			
			double total_population = 0;
	
			for( int i = 0; i < dm.districts.size(); i++) {
				try {
				dmcolors[i] = dm.getWastedVoteColor(i);
				ddata[i] = new String[dcolumns.length];
				District d = dm.districts.get(i);
				total_population += d.getPopulation();
				//String population = ""+(int)d.getPopulation();
				double[][] result = d.getElectionResults();
				double self_entropy = d.getSelfEntropy(result[Settings.self_entropy_use_votecount?2:1]);
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
				ddata[i][4] = ""+decimal.format(self_entropy*conversion_to_bits)+" bits";
				ddata[i][5] = ""+d.iso_quotient;
				ddata[i][6] = ""+d.area;
				ddata[i][7] = ""+d.paired_edge_length;
				ddata[i][8] = ""+d.unpaired_edge_length;
				for( int j = 9; j < ddata[i].length; j++) {
					ddata[i][j] = "";
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+9] = ""+(result[0][j]/total);
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+9+Settings.num_candidates] = ""+integer.format(result[0][j]);
				}	
				} catch (Exception ex) {
					System.out.println("ex stats 1 "+ex);
					ex.printStackTrace();
				}
			}
			
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
					new String[]{""+decimal.format(dm.getMaxPopDiff()*100.0),"Population imbalance (%)"},
					new String[]{""+decimal.format(dm.fairnessScores[1]*conversion_to_bits),"Representation imbalance (relative entropy)"},
					new String[]{""+decimal.format(dm.fairnessScores[4]*conversion_to_bits),"Voting power imbalance (relative entropy)"},
					new String[]{""+integer.format(wasted_votes),"Wasted votes (count)"},
					new String[]{""+decimal.format(egap),"Efficiency gap (pct)"},
					new String[]{""+decimal.format(0.01*egap*(double)Settings.num_districts),"Adj. efficiency gap (seats)"},
					new String[]{""+decimal.format(total_pvi / counted_districts),"Avg. PVI"},
					new String[]{""+integer.format(num_competitive),"Competitive elections (< 5 PVI)"},
					new String[]{""+decimal.format(dm.fairnessScores[7]),"Seats / vote asymmetry"},
					new String[]{""+decimal.format(100.0*Settings.mutation_boundary_rate),"Mutation rate (%)"},		
					new String[]{""+decimal.format(100.0*Settings.elite_fraction),"Elitism (%)"},		
					new String[]{""+integer.format(featureCollection.ecology.generation),"Generation (count)"},
			};
			TableModel tm = new DefaultTableModel(sdata,scolumns);
			summaryTable.setModel(tm);
			
			summaryTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);

			Vector<String> cands = MainFrame.mainframe.project.demographic_columns;


			//=== by party
			for( int i = 0; i < Settings.num_candidates; i++) {
				cdata[i] = new String[]{
						""+cands.get(i),
						""+integer.format(elec_counts[i]),
						""+integer.format(vote_counts[i]),//*total_population/tot_votes),
						""+(dm.wasted_votes_by_party[i]),
						""+(elec_counts[i]/((double)(dm.districts.size()*Settings.members_per_district))),
						""+(vote_counts[i]/tot_votes)
				};
			}

			
			TableModel tm1 = new DefaultTableModel(ddata,dcolumns);
			table.setModel(tm1);
			Enumeration<TableColumn> en = table.getColumnModel().getColumns();
	        while (en.hasMoreElements()) {
	            TableColumn tc = en.nextElement();
	            tc.setCellRenderer(new MyTableCellRenderer());
	        }			
			TableModel tm2 = new DefaultTableModel(cdata,ccolumns);
			table_1.setModel(tm2);
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
	}
	public PanelStats() {

		initComponents();
	}
	private void initComponents() {
		this.setLayout(null);
		this.setSize(new Dimension(449, 510));
		this.setPreferredSize(new Dimension(443, 788));
		
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 332, 390, 223);
		add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(26, 604, 390, 155);
		add(scrollPane_1);
		
		table_1 = new JTable();
		scrollPane_1.setViewportView(table_1);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		btnCopy = new JButton("copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ActionEvent nev = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
				table.selectAll();
				table.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		btnCopy.setBounds(327, 298, 89, 23);
		add(btnCopy);
		
		button = new JButton("copy");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionEvent nev = new ActionEvent(table_1, ActionEvent.ACTION_PERFORMED, "copy");
				table_1.selectAll();
				table_1.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
			}
		});
		button.setBounds(327, 570, 89, 23);
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
		scrollPane_2.setBounds(26, 45, 390, 241);
		add(scrollPane_2);
		
		summaryTable = new JTable();
		scrollPane_2.setViewportView(summaryTable);
		
		lblSummary = new JLabel("Summary");
		lblSummary.setBounds(26, 15, 226, 14);
		add(lblSummary);
		
		lblByDistrict = new JLabel("By district");
		lblByDistrict.setBounds(26, 307, 226, 14);
		add(lblByDistrict);
		
		lblByParty = new JLabel("By party");
		lblByParty.setBounds(26, 579, 226, 14);
		add(lblByParty);
	}
	public FeatureCollection featureCollection;
	private JTable table;
	private JTable table_1;
	public JButton btnCopy;
	public JButton button;
	public JTable summaryTable;
	public JButton button_1;
	public JScrollPane scrollPane_2;
	public JLabel lblSummary;
	public JLabel lblByDistrict;
	public JLabel lblByParty;
	
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



	@Override
	public void eventOccured() {
		getStats();
		MainFrame.mainframe.progressBar.setString( featureCollection.ecology.generation+" iterations");
	}
}
