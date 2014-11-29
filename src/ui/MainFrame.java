package ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import mapCandidates.Ecology;
import mapCandidates.Settings;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


public class MainFrame extends JFrame {
	JTextField textField_1 = new JTextField();
	JTextField textField = new JTextField();
	JSlider slider = new JSlider();
	JSlider slider_1 = new JSlider();
	JSlider slider_2 = new JSlider();
	JSlider slider_3 = new JSlider();
	JSlider slider_4 = new JSlider();
	JSlider slider_5 = new JSlider();
	JSlider slider_6 = new JSlider();
	JSlider slider_7 = new JSlider();

	public Ecology ecology = new Ecology();
	public MainFrame() {
		Dimension d = new Dimension(800,1024);
		//this.setPreferredSize(d);
		this.setSize(d);
		//this.getContentPane().setPreferredSize(d);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		panel.setPreferredSize(new Dimension(200,100));
		panel_1.setPreferredSize(new Dimension(200,100));
		panel.setLayout(null);
		splitPane.setLeftComponent(panel);
		
		JLabel lblPopulationReplaced = new JLabel("% population replaced");
		lblPopulationReplaced.setBounds(6, 6, 172, 16);
		panel.add(lblPopulationReplaced);
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.replace_fraction = slider.getValue()/100.0;
			}
		});
		slider.setBounds(6, 27, 190, 29);
		panel.add(slider);
		JLabel lblBorderMutation = new JLabel("% border mutation");
		lblBorderMutation.setBounds(6, 68, 172, 16);
		panel.add(lblBorderMutation);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_boundary_rate = slider.getValue()/100.0;
			}
		});
		
		slider_1.setBounds(6, 89, 190, 29);
		panel.add(slider_1);
		
		JLabel lblScatterMutation = new JLabel("% scatter mutation");
		lblScatterMutation.setBounds(6, 130, 172, 16);
		panel.add(lblScatterMutation);
		slider_2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_rate = slider.getValue()/100.0;
			}
		});
		
		slider_2.setBounds(6, 151, 190, 29);
		panel.add(slider_2);
		
		JLabel lblPopulation = new JLabel("Population");
		lblPopulation.setBounds(6, 192, 104, 16);
		panel.add(lblPopulation);
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textField.postActionEvent();
			}
		});
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.population = new Integer(textField.getText());
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textField.setText("512");
		textField.setBounds(105, 186, 91, 28);
		panel.add(textField);
		textField.setColumns(10);
		textField_1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textField_1.postActionEvent();
			}
		});
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.trials = new Integer(textField.getText());
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textField_1.setText("128");
		textField_1.setColumns(10);
		textField_1.setBounds(105, 220, 91, 28);
		panel.add(textField_1);
		
		JLabel lblTrials = new JLabel("Trials");
		lblTrials.setBounds(6, 226, 104, 16);
		panel.add(lblTrials);
		
		JLabel lblCompactness = new JLabel("compactness");
		lblCompactness.setBounds(6, 315, 172, 16);
		panel.add(lblCompactness);
		slider_3.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.geometry_weight = slider_3.getValue()/100.0;
			}
		});
		
		slider_3.setBounds(6, 336, 190, 29);
		panel.add(slider_3);
		
		JLabel lblPopulationBalance = new JLabel("population balance");
		lblPopulationBalance.setBounds(6, 377, 172, 16);
		panel.add(lblPopulationBalance);
		slider_4.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.population_balance_weight = slider_3.getValue()/100.0;
			}
		});
		
		slider_4.setBounds(6, 398, 190, 29);
		panel.add(slider_4);
		
		JLabel lblProportionalRepresentation = new JLabel("proportional representation\n");
		lblProportionalRepresentation.setBounds(6, 439, 172, 16);
		panel.add(lblProportionalRepresentation);
		slider_5.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disenfranchise_weight = slider_5.getValue()/100.0;

			}
		});
		
		slider_5.setBounds(6, 460, 190, 29);
		panel.add(slider_5);
		
		JLabel lblVotingPowerBalance = new JLabel("voting power balance");
		lblVotingPowerBalance.setBounds(6, 501, 172, 16);
		panel.add(lblVotingPowerBalance);
		slider_6.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.voting_power_balance_weight = slider_3.getValue()/100.0;
			}
		});
		
		slider_6.setBounds(6, 522, 190, 29);
		panel.add(slider_6);
		slider_7.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disconnected_population_weight = slider_3.getValue()/100.0;
			}
		});
		
		slider_7.setBounds(6, 583, 190, 29);
		panel.add(slider_7);
		
		JLabel lblContiguency = new JLabel("contiguency");
		lblContiguency.setBounds(6, 562, 172, 16);
		panel.add(lblContiguency);
		
		splitPane.setRightComponent(panel_1);
	}
}