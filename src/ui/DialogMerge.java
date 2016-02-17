package ui;
import geography.Feature;
import geography.FeatureCollection;
import geography.Properties;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class DialogMerge extends JDialog {
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
		
		int found = -1;
		comboBoxMapLayer.removeAllItems();
		for( int i = 0; i < map_headers.length; i++) {
			comboBoxMapLayer.addItem(map_headers[i]);
			if( found < 0 && map_headers[i].toUpperCase().trim().indexOf("GEOID") == 0) {
				found = i;
			}
		}
		found = found < 0 ? 0 : found; 
		comboBoxMapLayer.setSelectedIndex(found);
		
		found = -1;
		comboBoxFileLinkColumn.removeAllItems();
		for( int i = 0; i < data_headers.length; i++) {
			comboBoxFileLinkColumn.addItem(data_headers[i]);
			if( found < 0 && data_headers[i].toUpperCase().trim().indexOf("GEOID") == 0) {
				found = i;
			}
		}
		found = found < 0 ? 0 : found; 
		comboBoxFileLinkColumn.setSelectedIndex(found);
		
		/*
		comboBoxFilePopulationColumn.removeAllItems();
		for( int i = 0; i < data_headers.length; i++) {
			comboBoxFilePopulationColumn.addItem(data_headers[i]);
		}
		comboBoxFilePopulationColumn.setSelectedIndex(0);
		*/
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
	
	public DialogMerge() {
		setTitle("Merge data");
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(new Dimension(366, 188));
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
		
		JLabel lblLinkMapLayer = new JLabel("Link map layer");
		lblLinkMapLayer.setBounds(10, 11, 125, 14);
		getContentPane().add(lblLinkMapLayer);
		
		JLabel lblToColumn = new JLabel("To column");
		lblToColumn.setBounds(10, 64, 89, 14);
		getContentPane().add(lblToColumn);
		
		lblNonmatchesMap.setBounds(157, 36, 175, 14);
		getContentPane().add(lblNonmatchesMap);
		
		lblNonmatchesFile.setBounds(157, 90, 189, 14);
		getContentPane().add(lblNonmatchesFile);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int map_index = comboBoxMapLayer.getSelectedIndex();
				int file_index = comboBoxFileLinkColumn.getSelectedIndex();
				
				System.out.println("map_data.length "+map_data.length);
				System.out.println("data.length "+data.length);

				HashMap<String,Feature> hmmap = new HashMap<String,Feature>();
				for( int i = 0; i < map_data.length; i++) {
					hmmap.put(map_data[i][map_index], fc.features.get(i));
				}
				System.out.println("hmap.size "+hmmap.size());
				System.out.println("data_headers.length "+data_headers.length);
				for( Feature f : fc.features) {
					f.vtd.temp_bool = false;
				}
				for( int i = 0; i < data.length; i++) {
					try {
						if( i % 10 == 0) {
							System.out.print(".");
						}
						if( i % (100 * 10) == 0) {
							System.out.println(""+i);
						}
						if( hmmap.containsKey(data[i][file_index])) {
							Feature f = hmmap.get(data[i][file_index]);
							if( f == null) {
								System.out.println("f is null!");
							}
							/*
							try {
								if( load_pop) {
									if( f.ward != null) {
										f.ward.has_census_results = true;
										f.ward.population = Double.parseDouble(data[i][pop_index].replaceAll(",",""));
									}
									f.properties.POPULATION = (int) Double.parseDouble(data[i][pop_index]);
								}
							} catch (Exception ex) { }
							*/
							f.vtd.temp_bool = true;
							for( int j = 0; j < data_headers.length; j++) {
								f.properties.put(data_headers[j], data[i][j]);
							}
						} else {
							System.out.println("key not found "+data[i][file_index]);
							
						}
					} catch (Exception ex) {
						System.out.println("ex "+ex);
						ex.printStackTrace();
					}
				}
				for( Feature f : fc.features) {
					if( f.vtd.temp_bool == false) {
						for( int j = 0; j < data_headers.length; j++) {
							f.properties.put(data_headers[j], "0");
						}
					} else {
						f.vtd.temp_bool = false;
					}
				}
				
	
				hide();
			}
		});
		btnOk.setBounds(37, 119, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		btnCancel.setBounds(167, 119, 89, 23);
		getContentPane().add(btnCancel);
	}
}
