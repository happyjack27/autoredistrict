package ui;

import geography.FeatureCollection;

import javax.swing.*;
import javax.swing.table.*;

import solutions.*;

import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

public class PanelStats extends JPanel implements iDiscreteEventListener {
	
	JLabel lblNewLabel_1 = new JLabel();
	JLabel label_1 = new JLabel();
	JLabel label_3 = new JLabel();
	JLabel label_5 = new JLabel();
	JLabel label_7 = new JLabel();
	
	JLabel label_2 = new JLabel();
	JLabel label_4 = new JLabel();



	public void getStats() {
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
		lblNewLabel_1.setText(""+(1.0/dm.fairnessScores[0]));
		label_1.setText(""+integer.format(dm.fairnessScores[3]));
		label_3.setText(""+decimal.format(dm.getMaxPopDiff()*100.0)+" pct");
		label_5. setText(""+decimal.format(dm.fairnessScores[1]*conversion_to_bits)+" bits");
		label_7.setText(""+decimal.format(dm.fairnessScores[4]*conversion_to_bits)+" bits");
		label_2.setText(""+decimal.format(100.0*Settings.mutation_boundary_rate)+" pct");		
		label_4.setText(""+integer.format(featureCollection.ecology.generation));
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
					0,
					

					Settings.getAnnealingFloor( featureCollection.ecology.generation),

					(
							//Settings.square_root_compactness 
							dm.fairnessScores[0]
							//: (Math.sqrt(dm.fairnessScores[0])+Math.sqrt(dm2.fairnessScores[0])+Math.sqrt(dm3.fairnessScores[0]))*0.3333333
						), //BORDER LENGTH
					dm.fairnessScores[3], //DISCONNECTED POP
					dm.fairnessScores[2]*conversion_to_bits, //POP IMBALANCE
					0,
					0,
					
			});
			
		}
		
		} catch (Exception ex) {
			System.out.println("ex ad "+ex);
			ex.printStackTrace();
		}

		
		try {
			
			String[] dcolumns = new String[4+Candidate.candidates.size()*2];
			String[][] ddata = new String[dm.districts.size()][];
			dcolumns[0] = "District";
			dcolumns[1] = "Population";
			dcolumns[2] = "Winner";
			dcolumns[3] = "Self-entropy";
			
			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","Wasted votes","% del","% pop vote"};
			String[][] cdata = new String[Candidate.candidates.size()][];
			
			double[] elec_counts = new double[Candidate.candidates.size()];
			double[] vote_counts = new double[Candidate.candidates.size()];
			double tot_votes = 0;
			for( int i = 0; i < Candidate.candidates.size(); i++) {
				elec_counts[i] = 0;
				vote_counts[i] = 0;
				dcolumns[i+4] = ""+i+" vote %";
				dcolumns[i+4+Candidate.candidates.size()] = ""+i+" votes";
			}
			
			double total_population = 0;
			for( int i = 0; i < dm.districts.size(); i++) {
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
				for( int j = 0; j < result[0].length; j++) {
					winner += ""+((int)result[1][j])+",";
					elec_counts[j] += result[1][j];
					vote_counts[j] += result[0][j];
					total += result[0][j];
					tot_votes += result[0][j];
				}
				//String winner = ""+iwinner;
				ddata[i][0] = ""+i;
				ddata[i][1] = integer.format(d.getPopulation());
				ddata[i][2] = ""+winner;
				ddata[i][3] = ""+decimal.format(self_entropy*conversion_to_bits)+" bits";
				for( int j = 4; j < ddata[i].length; j++) {
					ddata[i][j] = "";
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+4] = ""+(result[0][j]/total);
				}
				for( int j = 0; j < result[0].length; j++) {
					ddata[i][j+4+Candidate.candidates.size()] = ""+integer.format(result[0][j]);
				}	
			}
			//System.out.println("tot votes "+tot_votes);
			//			String[] ccolumns = new String[]{"Party","Delegates","Pop. vote","% del","% pop vote"};

			for( int i = 0; i < Candidate.candidates.size(); i++) {
				cdata[i] = new String[]{
						""+i,
						""+integer.format(elec_counts[i]),
						""+integer.format(vote_counts[i]),//*total_population/tot_votes),
						""+(dm.wasted_votes_by_party[i]),
						""+(elec_counts[i]/((double)(dm.districts.size()*Settings.members_per_district))),
						""+(vote_counts[i]/tot_votes)
				};
			}

			
			TableModel tm = new DefaultTableModel(ddata,dcolumns);
			table.setModel(tm);
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
	}
	public PanelStats() {
		this.setLayout(null);
		this.setSize(new Dimension(449, 510));
		this.setPreferredSize(new Dimension(449, 612));
		
		JLabel lblNewLabel = new JLabel("Isoperimetric quotent (compactness):");
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(6, 6, 202, 16);
		add(lblNewLabel);
		
		lblNewLabel_1.setBounds(220, 5, 196, 16);
		add(lblNewLabel_1);
		
		JLabel lblDisconnectedPopulation = new JLabel("Disconnected population:");
		lblDisconnectedPopulation.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblDisconnectedPopulation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDisconnectedPopulation.setBounds(6, 34, 202, 16);
		add(lblDisconnectedPopulation);
		
		label_1.setBounds(220, 34, 196, 16);
		add(label_1);
		
		JLabel lblPopulationBalance = new JLabel("Population imbalance:");
		lblPopulationBalance.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblPopulationBalance.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPopulationBalance.setBounds(6, 62, 202, 16);
		add(lblPopulationBalance);
		
		label_3.setBounds(220, 62, 196, 16);
		add(label_3);
		
		JLabel lblDisproportionateRepresentation = new JLabel("Representation imbalance:");
		lblDisproportionateRepresentation.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblDisproportionateRepresentation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDisproportionateRepresentation.setBounds(6, 90, 202, 16);
		add(lblDisproportionateRepresentation);
		
		label_5.setBounds(220, 90, 196, 16);
		add(label_5);
		
		JLabel lblPowerImbalance = new JLabel("Voting power imbalance:");
		lblPowerImbalance.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblPowerImbalance.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPowerImbalance.setBounds(6, 118, 202, 16);
		add(lblPowerImbalance);
		
		label_7.setBounds(220, 118, 202, 16);
		add(label_7);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 194, 390, 223);
		add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(26, 426, 390, 155);
		add(scrollPane_1);
		
		table_1 = new JTable();
		scrollPane_1.setViewportView(table_1);
		
		JLabel lblBorderMutationRate = new JLabel("Mutation rate:");
		lblBorderMutationRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBorderMutationRate.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblBorderMutationRate.setBounds(6, 146, 202, 16);
		add(lblBorderMutationRate);
		
		label_2.setBounds(220, 146, 202, 16);
		add(label_2);
		
		JLabel lblGeneration = new JLabel("Generation:");
		lblGeneration.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGeneration.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblGeneration.setBounds(6, 174, 202, 16);
		add(lblGeneration);
		
		label_4.setBounds(220, 174, 202, 16);
		add(label_4);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	}
	public FeatureCollection featureCollection;
	private JTable table;
	private JTable table_1;



	@Override
	public void eventOccured() {
		getStats();
	}
}
