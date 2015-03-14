package ui;
import geoJSON.Feature;
import geoJSON.FeatureCollection;
import geoJSON.Properties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class DialogImport extends JDialog {
	JCheckBox lblSelectDemographicelectionResult = new JCheckBox("Select demographic / election result columns");
	JCheckBox lblLoadPopulationFrom = new JCheckBox("Load population from");
	JComboBox comboBoxFilePopulationColumn = new JComboBox();
	JComboBox comboBoxMapLayer = new JComboBox();
	JComboBox comboBoxFileLinkColumn = new JComboBox();
	
	FeatureCollection fc;
	String[] data_headers;
	String[][] data;
	String[] map_headers;
	String[][] map_data;
	
	JLabel lblNonmatchesMap = new JLabel("0 non-matches");
	JLabel lblNonmatchesFile = new JLabel("0 non-matches");

	
	public void setData(FeatureCollection fc, String[] data_headers, String[][] data) {
		this.fc = fc;
		this.data_headers = data_headers;
		this.data = data;
		map_headers = fc.getHeaders();
		map_data = fc.getData(map_headers);
		
		comboBoxMapLayer.removeAllItems();
		for( int i = 0; i < map_headers.length; i++) {
			comboBoxMapLayer.addItem(map_headers[i]);
		}
		comboBoxMapLayer.setSelectedIndex(0);
		
		comboBoxFileLinkColumn.removeAllItems();
		for( int i = 0; i < data_headers.length; i++) {
			comboBoxFileLinkColumn.addItem(data_headers[i]);
		}
		comboBoxFileLinkColumn.setSelectedIndex(0);
		
		comboBoxFilePopulationColumn.removeAllItems();
		for( int i = 0; i < data_headers.length; i++) {
			comboBoxFilePopulationColumn.addItem(data_headers[i]);
		}
		comboBoxFilePopulationColumn.setSelectedIndex(0);
	}
	public void recalc_matches() {
		try {
			int non_matches_map = 0;
			int non_matches_file = 0;
			int map_index = comboBoxMapLayer.getSelectedIndex();
			int file_index = comboBoxFileLinkColumn.getSelectedIndex();
			if( map_index < 0 || file_index < 0) {
				System.out.println("recalc: bad index");
				return;
			}
	
			HashMap<String,String> hmfile = new HashMap<String,String>();
			for( int i = 0; i < data.length; i++) {
				hmfile.put(data[i][file_index].trim().toLowerCase(), data[i][file_index]);
			}
			HashMap<String,String> hmmap = new HashMap<String,String>();
			for( int i = 0; i < map_data.length; i++) {
				hmmap.put(map_data[i][map_index].trim().toLowerCase(), map_data[i][map_index]);
			}
			
			for( int i = 0; i < map_data.length; i++) {
				if( !hmfile.containsKey(map_data[i][map_index].trim().toLowerCase())) {
					non_matches_map++;
				}
			}
			for( int i = 0; i < data.length; i++) {
				if( !hmmap.containsKey(data[i][file_index].trim().toLowerCase())) {
					non_matches_file++;
				}
			}
			lblNonmatchesMap.setText(""+non_matches_map+" non-matches");
			lblNonmatchesFile.setText(""+non_matches_file+" non-matches");
			System.out.println("recalc: done.");
			comboBoxMapLayer.repaint();
			comboBoxFileLinkColumn.repaint();
		} catch (Exception ex) { ex.printStackTrace(); }
		
	}
	
	public DialogImport() {
		setTitle("Import data");
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(new Dimension(500,600));
		getContentPane().setPreferredSize(new Dimension(500,600));
		comboBoxMapLayer.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				recalc_matches();
			}
		});
		
		comboBoxMapLayer.setBounds(10, 33, 137, 20);
		getContentPane().add(comboBoxMapLayer);
		comboBoxFileLinkColumn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				recalc_matches();
			}
		});
		
		comboBoxFileLinkColumn.setBounds(10, 87, 137, 20);
		getContentPane().add(comboBoxFileLinkColumn);
		comboBoxFilePopulationColumn.setEnabled(false);
		
		comboBoxFilePopulationColumn.setBounds(10, 139, 137, 20);
		getContentPane().add(comboBoxFilePopulationColumn);
		
		JLabel lblLinkMapLayer = new JLabel("Link map layer");
		lblLinkMapLayer.setBounds(10, 11, 125, 14);
		getContentPane().add(lblLinkMapLayer);
		
		JLabel lblToColumn = new JLabel("To column");
		lblToColumn.setBounds(10, 64, 89, 14);
		getContentPane().add(lblToColumn);
		lblLoadPopulationFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxFilePopulationColumn.setEnabled(lblLoadPopulationFrom.isSelected());
			}
		});
		
		lblLoadPopulationFrom.setBounds(10, 118, 182, 14);
		getContentPane().add(lblLoadPopulationFrom);
		
		lblNonmatchesMap.setBounds(157, 36, 175, 14);
		getContentPane().add(lblNonmatchesMap);
		
		lblNonmatchesFile.setBounds(157, 90, 189, 14);
		getContentPane().add(lblNonmatchesFile);
		lblSelectDemographicelectionResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		lblSelectDemographicelectionResult.setBounds(154, 175, 318, 14);
		getContentPane().add(lblSelectDemographicelectionResult);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int map_index = comboBoxMapLayer.getSelectedIndex();
				int file_index = comboBoxFileLinkColumn.getSelectedIndex();
				int pop_index = comboBoxFilePopulationColumn.getSelectedIndex();
				boolean load_pop = lblLoadPopulationFrom.isSelected();

				HashMap<String,Feature> hmmap = new HashMap<String,Feature>();
				for( int i = 0; i < map_data.length; i++) {
					hmmap.put(map_data[i][file_index], fc.features.get(i));
				}
				
				for( int i = 0; i < data.length; i++) {		
					if( hmmap.containsKey(data[i])) {
						Feature f = hmmap.get(data[i]);
						try {
							if( load_pop) {
								if( f.block != null) {
									f.block.has_census_results = true;
									f.block.population = Double.parseDouble(data[i][pop_index].replaceAll(",",""));
								}
								f.properties.POPULATION = (int) Double.parseDouble(data[i][pop_index]);
							}
						} catch (Exception ex) { }
						for( int j = 0; j < data_headers.length; j++) {
							f.properties.put(data_headers[j], data[i][j]);
						}
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
		scrollPane.setBounds(10, 211, 182, 161);
		getContentPane().add(scrollPane);
		
		JList list = new JList();
		scrollPane.setViewportView(list);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(286, 211, 182, 161);
		getContentPane().add(scrollPane_1);
		
		JList list_1 = new JList();
		scrollPane_1.setViewportView(list_1);
		
		JButton button = new JButton(">");
		button.setBounds(214, 239, 49, 29);
		getContentPane().add(button);
		
		JButton button_1 = new JButton("<");
		button_1.setBounds(214, 296, 49, 29);
		getContentPane().add(button_1);
	}
}
