package ui;
import geoJSON.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class DialogSelectLayers extends JDialog {
	JCheckBox lblSelectDemographicelectionResult = new JCheckBox("Select demographic / election result columns");
	JCheckBox lblLoadPopulationFrom = new JCheckBox("Load population from");
	JComboBox comboBoxFilePopulationColumn = new JComboBox();
	JList list = new JList();
	JList list_1 = new JList();
	public boolean ok = false;

	
	FeatureCollection fc;
	String[] map_headers;
	String[][] map_data;

	public Vector<String> not_in = new Vector<String>();
	public Vector<String> in = new Vector<String>();
	
	public void setData(FeatureCollection fc) {
		ok = false;
		this.fc = fc;
		map_headers = fc.getHeaders();
		map_data = fc.getData(map_headers);
		
		not_in = new Vector<String>();
		in = new Vector<String>();
		for( String s : map_headers) {
			not_in.add(s);
		}
		list.setListData(not_in);
		list_1.setListData(in);
		
		comboBoxFilePopulationColumn.removeAllItems();
		for( int i = 0; i < map_headers.length; i++) {
			comboBoxFilePopulationColumn.addItem(map_headers[i]);
		}
		comboBoxFilePopulationColumn.setSelectedIndex(0);
		
	}
	
	public DialogSelectLayers() {
		setTitle("Select layers");
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(new Dimension(500,600));
		getContentPane().setPreferredSize(new Dimension(500,600));
		comboBoxFilePopulationColumn.setEnabled(false);
		
		comboBoxFilePopulationColumn.setBounds(10, 27, 137, 20);
		getContentPane().add(comboBoxFilePopulationColumn);
		lblLoadPopulationFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxFilePopulationColumn.setEnabled(lblLoadPopulationFrom.isSelected());
			}
		});
		
		lblLoadPopulationFrom.setBounds(10, 6, 182, 14);
		getContentPane().add(lblLoadPopulationFrom);
		lblSelectDemographicelectionResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		lblSelectDemographicelectionResult.setBounds(150, 62, 318, 14);
		getContentPane().add(lblSelectDemographicelectionResult);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				if( lblLoadPopulationFrom.isSelected() ) {
					int pop_index = comboBoxFilePopulationColumn.getSelectedIndex();
					for( Feature f : fc.features) {
						String pop = f.properties.get(map_headers[pop_index]).toString();
						if( f.block != null) {
							f.block.has_census_results = true;
							f.block.population = Double.parseDouble(pop.replaceAll(",",""));
						}
						f.properties.POPULATION = (int) Double.parseDouble(pop.replaceAll(",",""));
					}
				}
				hide();
			}
		});
		btnOk.setBounds(122, 384, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		btnCancel.setBounds(243, 384, 89, 23);
		getContentPane().add(btnCancel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 88, 182, 284);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(list);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(286, 88, 182, 284);
		getContentPane().add(scrollPane_1);
		
		scrollPane_1.setViewportView(list_1);
		
		JButton button = new JButton(">");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = list.getSelectedIndex();
				if( i < 0) return;
				in.add(not_in.get(i));
				not_in.remove(i);
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button.setBounds(214, 143, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = list_1.getSelectedIndex();
				if( i < 0) return;
				not_in.add(in.get(i));
				in.remove(i);
				list.setListData(not_in);
				list_1.setListData(in);
			}
		});
		button_1.setBounds(214, 271, 49, 29);
		getContentPane().add(button_1);
	}
}
