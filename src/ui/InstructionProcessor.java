package ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.*;

import solutions.*;

public class InstructionProcessor implements iDiscreteEventListener {
	MainFrame mainFrame;
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
	
	Vector<String> instructions = new Vector<String>();
	int instruction_pointer = 0;

	public void queueInstructions( String s) {
		String[] new_instructions = s.split("\n");
		for( int i = 0; i < new_instructions.length; i++) {
			instructions.add(new_instructions[i]);
		}
		eventOccured();
	}

	@Override
	public void eventOccured() {
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
				istate = Download.state_to_fips.get(Download.state_to_abbr.getBackward(instruction_words[1]));
			}
		}
		
		if( command.equals("LOAD")) {
			Download.prompt = false;
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			mainFrame.downloadNextState();			
			instruction_pointer++;
			return;
		} else
		if( command.equals("DOWNLOAD")) {
			Download.prompt = false;
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			mainFrame.downloadNextState();
			instruction_pointer++;
			return;
		} else
		if( command.equals("DELETE")) {
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			Applet.deleteRecursive(new File(Download.getStartPath()));
		} else 
		if( command.equals("CLEAN")) {
			Download.cyear = 2010;
			Download.vyear = 2012;
			Download.istate = istate;
			Applet.deleteRecursive(new File(Download.getStartPath()+File.separator+"block_centroids"));
			Applet.deleteRecursive(new File(Download.getStartPath()+File.separator+"block_pop"));
			Applet.deleteRecursive(new File(Download.getStartPath()+File.separator+"2012"+File.separator+"vtd"+File.separator+"vtds.zip"));
		} else

		//instruction select statement
		//LOAD,SET,GO,STOP,WHEN,SAVE,EXPORT,EXIT
		if(command.equals("EXIT")) {
			System.exit(0);
		} else			
		if(command.equals("SET")) {
			if( instruction_words.length > 3) { 
				set(instruction_words[1],instruction_words[2],instruction_words[3]);
			}			
		} else
		if(command.equals("GO")) {
			for(ActionListener a: mainFrame.goButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(null, 0, ""));
			}
		} else
		if(command.equals("STOP")) {
			for(ActionListener a: mainFrame.stopButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(null, 0, ""));
			}
		} else
		if(command.equals("SAVE")) {
			mainFrame.saveData(Download.vtd_dbf_file, 2,false);
		} else
		if(command.equals("EXPORT")) {
			for(ActionListener a: mainFrame.panelStats.btnNewButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(null, 0, ""));
			}
		} else
		if(command.equals("WHEN")) {
			if( instruction_words[1].equals("MUTATE_RATE")) {
				double threshold = Double.parseDouble(instruction_words[2]);
				double current_value = Settings.mutation_rate;
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
		if( category.equals("DISTRICT")) {
			if( item.equals("NUM_DISTRICTS")) { mainFrame.lblMembersPerDistrict.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldNumDistricts.setText(value); }
			if( item.equals("SEATS_PER_DISTRICT")) { mainFrame.lblMembersPerDistrict.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldSeatsPerDistrict.setText(value); }
			if( item.equals("FAIRVOTE_SEATS")) { mainFrame.lblTotalSeats.setSelected(true); mainFrame.setSeatsMode(); mainFrame.textFieldTotalSeats.setText(value); }
			if( item.equals("ALLOW_4_SEATS")) { mainFrame.chckbxNewCheckBox.setSelected(value.equals("TRUE")); }
			if( item.equals("COLUMN")) { mainFrame.setDistrictColumn(value); }
		}
	}


	public void queueInstructionsFromFile(String string) {
		try {
			queueInstructions(Applet.readStream(new FileInputStream(string)).toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		
	}
}

