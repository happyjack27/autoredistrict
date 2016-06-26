package ui;

import geography.VTD;

import java.awt.*;
import java.util.Map.Entry;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import solutions.Ecology;
import util.Pair;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PanelFeatures extends JPanel {
	public JScrollPane scrollPane;
	public JTable table;
	public JLabel lblDistrict;
	public JTextField districtTF;
	public JButton btnNewButton;
	
	VTD vtd = null;
	
	public PanelFeatures() {
		initComponents();
	}
	public void setFeature(VTD vtd) {
		this.vtd = vtd;
		Set<Entry<String,Object>> es = vtd.properties.entrySet();
		String[][] sss = new String[es.size()][];
		Vector<Pair<String,String>> vp = new Vector<Pair<String,String>>();
		for( Entry<String,Object> e : es) {
			vp.add( new Pair<String,String>(e.getKey(),e.getValue().toString()));
		}
		Collections.sort(vp);
		for( int i = 0; i < vp.size(); i++) {
			sss[i] = new String[]{vp.get(i).a,vp.get(i).b};
		}
		String[] dcolumns = new String[]{"Name","Value"};
		TableModel tm1 = new DefaultTableModel(sss,dcolumns);
		table.setModel(tm1);
		
		String s = MainFrame.mainframe.project.district_column;
		if( s != null) {
			districtTF.setText(vtd.properties.getString(s));
		}
	}
	private void initComponents() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int w = getWidth();
				int h = getHeight();
				scrollPane.setBounds(5,45,w-10,h-10-40);
			}
		});
		this.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 48, 213, 381);
		scrollPane.setVerticalScrollBarPolicy(scrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(scrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		table = new JTable();
		
		String[] dcolumns = new String[]{"Name","Value"};
		String[][] ddata = new String[1][2];
		TableModel tm1 = new DefaultTableModel(ddata,dcolumns);
		table.setModel(tm1);

		scrollPane.setViewportView(table);
		add(scrollPane);

		
		lblDistrict = new JLabel("District:");
		lblDistrict.setBounds(6, 6, 61, 16);
		add(lblDistrict);
		
		districtTF = new JTextField();
		districtTF.setBounds(64, 0, 92, 28);
		add(districtTF);
		districtTF.setColumns(10);
		
		btnNewButton = new JButton("Set");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if( vtd == null) {
					return;
				}
				try {
					int  i = Integer.parseInt(districtTF.getText());
					String s = MainFrame.mainframe.project.district_column;
					if( s != null) {
						vtd.properties.put(s, ""+i);
						Ecology e = MainFrame.mainframe.featureCollection.ecology;
						if( e == null || e.population.size() < 1) {
							return;
						}
						e.population.get(0).vtd_districts[vtd.id] = i;
						MainFrame.mainframe.mapPanel.invalidate();
						MainFrame.mainframe.mapPanel.repaint();
						
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});
		btnNewButton.setBounds(163, 1, 61, 29);
		add(btnNewButton);
	}
}
