package ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import solutions.*;
import ui.DialogDownload.EventThread;
import ui.MainFrame.OpenShapeFileThread;
import util.Util;

public class InstructionProcessor extends JDialog implements iDiscreteEventListener {
	MainFrame mainFrame;
	Vector<String> instructions = new Vector<String>();
	Vector<String> instruction_history = new Vector<String>();
	int instruction_pointer = 0;
	public JScrollPane scrollPane;
	public JScrollPane scrollPane_1;
	public JTextArea historyTA;
	public JTextArea scriptTA;
	public JButton btnLoad;
	public JButton btnSave;
	public JLabel lblNewLabel;
	public JLabel lblInstructions;

	public InstructionProcessor() {
		setTitle("Scripts");
		initComponents();
	}
	
	public void initComponents() {
		getContentPane().setLayout(null);		
		this.setSize(new Dimension(500, 360));
		getContentPane().setPreferredSize(new Dimension(500,600));
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 39, 230, 252);
		getContentPane().add(scrollPane);
		
		historyTA = new JTextArea();
		historyTA.setFont(new Font("Courier New", Font.PLAIN, 9));
		scrollPane.setViewportView(historyTA);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(248, 39, 231, 252);
		getContentPane().add(scrollPane_1);
		
		scriptTA = new JTextArea();
		scriptTA.setFont(new Font("Courier New", Font.PLAIN, 9));
		scrollPane_1.setViewportView(scriptTA);
		
		btnLoad = new JButton("load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("text file","txt"));
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null)  {
					return;
				}
				FileInputStream fos = null;
				try {
					fos = new FileInputStream(f);
					String s = Util.readStream(fos);
					scriptTA.setText(s);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					fos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLoad.setBounds(404, 4, 75, 29);
		getContentPane().add(btnLoad);
		
		btnSave = new JButton("save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("text file","txt"));
				jfc.showSaveDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null)  {
					return;
				}
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(f);
					fos.write(historyTA.getText().toString().getBytes());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					fos.flush();
					fos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnSave.setBounds(161, 4, 75, 29);
		getContentPane().add(btnSave);
		
		lblNewLabel = new JLabel("History");
		lblNewLabel.setBounds(6, 11, 118, 16);
		getContentPane().add(lblNewLabel);
		
		lblInstructions = new JLabel("Instructions");
		lblInstructions.setBounds(248, 11, 118, 16);
		getContentPane().add(lblInstructions);
		
		btnApplyChanges = new JButton("apply changes");
		btnApplyChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetInstructions();
			}
		});
		btnApplyChanges.setBounds(345, 303, 134, 29);
		getContentPane().add(btnApplyChanges);
	}
	/*
	 * TODO: download,load
	 * done: have finish saving fire event, have each generation fire event. 
	 * TODO: test
	 */

	/* ===== INSTRUCTION PROCESSING
	 
	TO BE LOADED AS COMMAND LINE ARGUMENTS:
	java -Xmx4096M -Xms1024M -jar autoredistrict.jar RUN "./ar_script.txt"
	
	or can do via menu: run script.
	 
	SAMPLE PROGRAM:

	LOAD WI 2010
	SET DISTRICTS COLUMN AR_RESULT
	SET DISTRICTS NUM_DISTRICTS 8
	SET EVOLUTION ANNEAL_RATE 0.75
	GO
	WHEN MUTATE_RATE 5
		SET EVOLUTION MUTATE_RATE 10
	WHEN MUTATE_RATE 5
		SET EVOLUTION MUTATE_RATE 10
		SET EVOLUTION ELITE_MUTATE_FRAC 0.5
	WHEN MUTATE_RATE 0.25
		STOP
		SAVE
		EXPORT
		EXIT

	 */
	boolean indent = false;
	public JButton btnApplyChanges;
	public void addHistory( String s) {
		String prefix = "";
		if( mainFrame.evolving && !s.equals("GO")) {
			prefix = ""
					+"WHEN MUTATE_RATE "+Settings.mutation_boundary_rate+"\n";
			indent = true;
		}
		
		//if last line is same type of command, replace it instead of appending
		
		String hist = historyTA.getText();
		String[] hist_lines = hist.split("\n");
		String last_line = hist_lines[hist_lines.length-1].trim();
		//System.out.println("checking for match - last_line: "+last_line+" new line: "+s); 
		String[] last_words = last_line.split(" ");
		String[] s_words =  s.split(" ");
		//System.out.println("length "+last_words.length +" "+ s_words.length);
		boolean match = true;
		if( last_words.length != s_words.length) {
			match = false;
		} else {
			for( int i = 0; i < last_words.length - 1; i++) {
				//System.out.println("comparing "+last_words[i]+" "+s_words[i]);
				if( !last_words[i].equals(s_words[i])) {
					match = false;
					break;
				}
			}
		}
		//System.out.println(" matched? "+match);
		StringBuffer sb = new StringBuffer();
		if( !match) {
			sb.append(hist);
		} else {
			for( int i = 0; i < hist_lines.length-2; i++) {
				sb.append(hist_lines[i]+"\n");
			}
			if( !hist_lines[hist_lines.length-2].contains("WHEN")) { //if second last line is a when, don't do that one either.
				sb.append(hist_lines[hist_lines.length-2]+"\n");
			}
		}
		
		//now add it.
		sb.append(prefix+(indent ? "\t" : "")+s+"\n");
		historyTA.setText(sb.toString());			
	}
	public void resetInstructions() {
		String[] new_instructions = scriptTA.getText().toString().split("\n");
		instructions.clear();
		for( int i = 0; i < new_instructions.length; i++) {
			instructions.add(new_instructions[i]);
		}
		eventOccured();
	}
	
	public void queueInstructions( String s) {
		scriptTA.setText(scriptTA.getText().toString()+s);
		String[] new_instructions = s.split("\n");
		for( int i = 0; i < new_instructions.length; i++) {
			instructions.add(new_instructions[i]);
		}
		eventOccured();
	}

	@Override
	public void eventOccured() {
		Download.init();
		if( instruction_pointer >= instructions.size()) {
			return;
		}
		
		//get instruction words
		String current_instruction = instructions.get(instruction_pointer).trim();
		String[] instruction_words = current_instruction.split(" ");
		for( int i = 0; i < instruction_words.length; i++) {
			instruction_words[i] = instruction_words[i].toUpperCase().trim();
		}
		String command = instruction_words[0];
		
		int istate = -1;
		if( instruction_words.length > 0) {
			try {
				istate = Integer.parseInt(instruction_words[1]);
			} catch (Exception ex) {
				try { 
					istate = Download.state_to_fips.get(Download.state_to_abbr.getBackward(instruction_words[1]));
				} catch (Exception ex2) {
				
				}
			}
		}
		
		if( command.equals("LOAD")) {
			Download.prompt = false;
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			if( instruction_words.length > 2) { Download.cyear = Integer.parseInt(instruction_words[2]); }
			if( instruction_words.length > 3) { Download.vyear = Integer.parseInt(instruction_words[3]); }
			Download.initPaths();
			if( Download.checkForDoneFile()) {
				System.out.println("found prepared data.  opening...");
				OpenShapeFileThread ost = MainFrame.mainframe.createOpenShapeFileThread(Download.vtd_file);
				ost.f = Download.vtd_file;
				Download.nextThread = null;
				ost.nextThread = new DialogDownload.EventThread();
				ost.start();
			} else {
				Download.downloadState(Download.istate,Download.cyear,Download.vyear);
			}
			instruction_pointer++;
			return;
		} else
		if( command.equals("DOWNLOAD")) {
			Download.prompt = false;
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			if( instruction_words.length > 2) { Download.cyear = Integer.parseInt(instruction_words[2]); }
			if( instruction_words.length > 3) { Download.vyear = Integer.parseInt(instruction_words[3]); }
			Download.downloadState(Download.istate,Download.cyear,Download.vyear);
			instruction_pointer++;
			return;
		} else
		if( command.equals("DELETE")) {
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			if( instruction_words.length > 2) { Download.cyear = Integer.parseInt(instruction_words[2]); }
			if( instruction_words.length > 3) { Download.vyear = Integer.parseInt(instruction_words[3]); }
			Download.delete();
		} else 
		if( command.equals("CLEAN")) {
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			if( instruction_words.length > 2) { Download.cyear = Integer.parseInt(instruction_words[2]); }
			if( instruction_words.length > 3) { Download.vyear = Integer.parseInt(instruction_words[3]); }
			Download.clean();
		} else

		//instruction select statement
		//LOAD,SET,GO,STOP,WHEN,SAVE,EXPORT,EXIT
		if(command.equals("EXIT")) {
			System.exit(0);
		} else		
		if( command.equals("SET") && instruction_words.length > 3 && instruction_words[1].equals("ELECTION")  && instruction_words[2].equals("COLUMNS")) {
			mainFrame.project.election_columns.clear();
			for( int i = 3; i < instruction_words.length; i++) {
				mainFrame.project.election_columns.add(instruction_words[i]);
			}
			mainFrame.setElectionColumns();
		} else 
		if( command.equals("SET") && instruction_words.length > 3 && instruction_words[1].equals("ETHNICITY")  && instruction_words[2].equals("COLUMNS")) {
			mainFrame.project.demographic_columns.clear();
			for( int i = 3; i < instruction_words.length; i++) {
				mainFrame.project.demographic_columns.add(instruction_words[i]);
			}
			mainFrame.setDemographicColumns();
		} else 
		if(command.equals("SET")) {
			if( instruction_words.length > 3) { 
				set(instruction_words[1],instruction_words[2],instruction_words[3]);
			}			
		} else
		if(command.equals("GO")) {
			for(ActionListener a: mainFrame.goButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(mainFrame.goButton, 0, ""));
			}
		} else
		if(command.equals("STOP")) {
			for(ActionListener a: mainFrame.stopButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(mainFrame.stopButton, 0, ""));
			}
		} else
		if(command.equals("SAVE")) {
			mainFrame.saveData(Download.vtd_dbf_file, 2,false);
		} else
		if(command.equals("EXPORT")) {
			for(ActionListener a: mainFrame.panelStats.btnNewButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(mainFrame.panelStats.btnNewButton, 0, ""));
			}
		} else
		if(command.equals("WHEN")) {
			if( instruction_words[1].equals("MUTATE_RATE")) {
				double threshold = Double.parseDouble(instruction_words[2]);
				double current_value = Settings.mutation_boundary_rate;
				if( threshold >= current_value) {
					return;
				}
			} else
			if( instruction_words[1].equals("GENERATION")) {
				double threshold = Double.parseDouble(instruction_words[2]);
				double current_value = mainFrame.featureCollection.ecology.generation;//todo: get this
				if( threshold <= current_value) {
					return;
				}
			}
		}
		
		instruction_pointer++;
		eventOccured();
	}

	public void set(String category, String item, String value) {
		double d = 0;
		boolean parsed = false;
		try {
			d = Double.parseDouble(value);
			parsed = true;
		} catch (Exception ex) { }
		if( category.equals("POPULATION") && item.equals("COLUMN")) {
			mainFrame.setPopulationColumn(value);
		} else
		if( category.equals("COUNTY") && item.equals("COLUMN")) {
			mainFrame.project.county_column = value;
			mainFrame.setCountyColumn();
		} else
		if( category.equals("MUNI") && item.equals("COLUMN")) {
			mainFrame.project.muni_column = value;
			mainFrame.setMuniColumn();
		} else
		if( category.equals("EVOLUTION")) {
			if( parsed && item.equals("POPULATION")) { mainFrame.textField.setText(value); }
			if( parsed && item.equals("MUTATE_RATE")) { mainFrame.slider_mutation.setValue((int)(d*100)); }
			if( parsed && item.equals("ANNEAL_RATE")) { mainFrame.slider_anneal.setValue((int)(d*100)); }
			if( parsed && item.equals("ELITE_FRAC")) { mainFrame.sliderElitism.setValue((int)(d*100)); }
			if( parsed && item.equals("ELITE_MUTATE_FRAC")) { mainFrame.sliderElitesMutated.setValue((int)(d*100)); }
		} else
		if( category.equals("CONSTRAIN")) {
			if( item.equals("CONTIGUITY")) { mainFrame.chckbxNewCheckBox_1 .setSelected(value.equals("TRUE")); }
			if( item.equals("POPULATION")) { mainFrame.chckbxConstrain.setSelected(value.equals("TRUE")); }
			if( item.equals("COMPETITION")) { mainFrame.chckbxConstrain_1.setSelected(value.equals("TRUE")); }
			
		} else
		if( category.equals("WEIGHT")) {
			if( parsed && item.equals("GEOMETRY_FAIRNESS")) { mainFrame.sliderBalance.setValue((int)(d*100)); }

			if( parsed && item.equals("POPULATION")) { mainFrame.sliderPopulationBalance.setValue((int)(d*100)); }
			if( parsed && item.equals("CONTIGUITY")) { mainFrame.sliderDisconnected.setValue((int)(d*100)); }
			if( parsed && item.equals("COMPACTNESS")) { mainFrame.sliderBorderLength.setValue((int)(d*100)); }
			if( parsed && item.equals("SPLITS")) { mainFrame.sliderSplitReduction.setValue((int)(d*100)); }
			
			if( parsed && item.equals("COMPETITION")) { mainFrame.sliderWastedVotesTotal.setValue((int)(d*100)); }
			if( parsed && item.equals("PROPORTIONAL")) { mainFrame.sliderRepresentation.setValue((int)(d*100)); }
			if( parsed && item.equals("PARTISAN")) { mainFrame.sliderSeatsVotes.setValue((int)(d*100)); }
			if( parsed && item.equals("RACIAL")) { mainFrame.sliderVoteDilution.setValue((int)(d*100)); }
			
			if( item.equals("COUNT_SPLITS")) { mainFrame.chckbxReduceSplits.setSelected(value.equals("TRUE")); }
		} else
		if( category.equals("DISTRICTS")) {
			if( item.equals("NUM_DISTRICTS")) { mainFrame.lblMembersPerDistrict.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldNumDistricts.setText(value); }
			if( item.equals("SEATS_PER_DISTRICT")) { mainFrame.lblMembersPerDistrict.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldSeatsPerDistrict.setText(value); }
			if( item.equals("FAIRVOTE_SEATS")) { mainFrame.lblTotalSeats.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldTotalSeats.setText(value); }
			if( item.equals("ALLOW_4_SEATS")) { mainFrame.chckbxNewCheckBox.setSelected(value.equals("TRUE")); }
			if( item.equals("COLUMN")) { mainFrame.setDistrictColumn(value); }
		}
	}


	public void queueInstructionsFromFile(String string) {
		try {
			queueInstructions(Util.readStream(new FileInputStream(string)).toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		
	}
}

