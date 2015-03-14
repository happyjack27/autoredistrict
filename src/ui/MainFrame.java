package ui;

import geoJSON.Feature;
import geoJSON.FeatureCollection;
import geoJSON.Geometry;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import mapCandidates.*;

import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;


public class MainFrame extends JFrame implements iChangeListener {
	public static MainFrame mainframe;
	boolean suppress_duplicates = false;
	boolean use_sample = false;
	double mutation_rate_multiplier = 0.1;
	public static double boundary_mutation_rate_multiplier = 0.4;
	long load_wait = 100;
	
	JCheckBoxMenuItem chckbxmntmMutateAll = new JCheckBoxMenuItem("Mutate all");
	JCheckBoxMenuItem chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
	JCheckBoxMenuItem chckbxmntmHideMapLines = new JCheckBoxMenuItem("Hide map lines");
	JCheckBoxMenuItem chckbxmntmLatitudeLongitude = new JCheckBoxMenuItem("Latitude / Longitude?");
	JCheckBoxMenuItem chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
	JCheckBoxMenuItem chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
	JCheckBoxMenuItem chckbxmntmReplaceAll = new JCheckBoxMenuItem("Replace all");
	JCheckBoxMenuItem chckbxmntmAutoAnneal = new JCheckBoxMenuItem("Auto anneal");


	JTextField textField_3 = new JTextField();
	JTextField textField_2 = new JTextField();
	JTextField textField_1 = new JTextField();
	JTextField textField = new JTextField();
	public JSlider slider_1 = new JSlider();
	JSlider slider_2 = new JSlider();
	JSlider slider_3 = new JSlider();
	JSlider slider_5 = new JSlider();
	JSlider slider_6 = new JSlider();
	JSlider slider_7 = new JSlider();
	
	//JMenu mnGeography = new JMenu("Geography");
	JMenuItem mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
	//JMenu mnDemographics = new JMenu("Demographics");
	JMenuItem chckbxmntmOpenCensusResults = new JMenuItem("Open Census results");
	JMenuItem mntmOpenElectionResults = new JMenuItem("Open Election results");
	JMenu mnEvolution = new JMenu("Evolution");
	JMenuItem mntmExportcsv = new JMenuItem("Export results .csv");
	JMenuItem mntmImportcsv = new JMenuItem("Import results .csv");
	JMenuItem mntmShowStats = new JMenuItem("Show stats");

	
	double minx,maxx,miny,maxy;
	
	public MapPanel mapPanel = new MapPanel(); 
	
	JFrame frameStats = new JFrame();
	PanelStats panelStats = new PanelStats();
	JFrame frameGraph = new JFrame();
	DialogShowProperties df = new DialogShowProperties();
	public PanelGraph panelGraph = new PanelGraph();


	//public Ecology ecology = new Ecology();
	public FeatureCollection featureCollection = new FeatureCollection();
	
	boolean geo_loaded = false;
	boolean census_loaded = false;
	boolean election_loaded = false;
	boolean evolving = false;
	JMenuItem mntmExportPopulation = new JMenuItem("Export population");
	JMenuItem mntmImportPopulation = new JMenuItem("Import population");
	JMenuItem mntmResetZoom = new JMenuItem("Reset zoom");
	JMenuItem mntmZoomIn = new JMenuItem("Zoom in");
	JMenuItem mntmUndoZoom = new JMenuItem("Undo zoom");
	JMenuItem mntmShowGraph = new JMenuItem("Show graph");
	private final JMenuItem mntmOpenEsriShapefile = new JMenuItem("Open ESRI shapefile");
	
	public void setEnableds() {
		
		if( !geo_loaded) {
			census_loaded = false;
			election_loaded = false;
		}
		chckbxmntmOpenCensusResults.setEnabled(geo_loaded);
		mntmOpenElectionResults.setEnabled(geo_loaded);
		
	}
	
	public void resetZoom() {
		boolean flipx = chckbxmntmFlipHorizontal.isSelected();
		boolean flipy = !chckbxmntmFlipVertical.isSelected();
		mapPanel.minx = flipx ? maxx : minx;
		mapPanel.maxx = flipx ? minx : maxx;
		mapPanel.miny = flipy ? maxy : miny;
		mapPanel.maxy = flipy ? miny : maxy;
		mapPanel.zoomStack.clear();
		mntmUndoZoom.setEnabled(false);
		mapPanel.invalidate();
		mapPanel.repaint();
	}
	
	public void loadElection(String s) {
		try {
			String[] lines = s.split("\n");
			int num_candidates = lines[0].split("\t").length - 1;
			
			Vector<String> not_found_in_geo = new Vector<String>();
			for( Block b : featureCollection.blocks) {
				b.has_election_results = false;
			}
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				String district = ss[0].trim();
				Block b = featureCollection.precinctHash.get(district);
				if( b == null) {
					not_found_in_geo.add(district);
					System.out.println("not in geo: "+district);

				} else {
					b.has_election_results = true;
				}
			}
			Vector<String> not_found_in_census = new Vector<String>();
			for( Block b : featureCollection.blocks) {
				if( b.has_election_results == false) {
					not_found_in_census.add(b.name);
					System.out.println("not in election: |"+b.name+"|");

				}
			}
			if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
				for( Block b : featureCollection.blocks) {
					b.has_election_results = false;
				}
				JOptionPane.showMessageDialog(null,""
						+"Election data doesn't match geographic data.\n"
						+"Election data without matching geo data: "+not_found_in_geo.size()+"\n"
						+"Geo data without matching election data: "+not_found_in_census.size()
						, "Mismatch of geographic regions"
						, 0);
				return;
			}
			
			
			
			HashMap<String,double[]> votes = new HashMap<String,double[]>();
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				String district = ss[0].trim();
				double[] dd = votes.get(district);
				if( dd == null) {
					dd = new double[num_candidates];
					for( int j = 0; j < num_candidates; j++) {
						dd[j] = 0;
					}
					votes.put(district, dd);
				}
				for( int j = 0; j < num_candidates && j < ss.length-1; j++) {
					try {
						dd[j] += Double.parseDouble(ss[j+1].replaceAll(",",""));
					} catch (Exception ex) {
						
					}
				}
			}
			
			for( Entry<String, double[]> es : votes.entrySet()) {
				Block b = featureCollection.precinctHash.get(es.getKey());
				double[] dd = es.getValue();
				for( int j = 0; j < num_candidates; j++) {
					Demographic d = new Demographic();
					d.block_id = b.id;
					d.turnout_probability = 1;
					d.population = (int) dd[j];
					d.vote_prob = new double[num_candidates];
					for( int i = 0; i < d.vote_prob.length; i++) {
						d.vote_prob[i] = 0;
					}
					d.vote_prob[j] = 1;
					b.demographics.add(d);
					System.out.println("block "+b.id+" added demo "+d.population+" "+j);
				}
			}
			
			Candidate.candidates = new Vector<Candidate>();
			for( int i = 0; i < num_candidates; i++) {
				Candidate c = new Candidate();
				c.index = i;
				c.id = ""+i;
				Candidate.candidates.add(c);
			}
			featureCollection.ecology.reset();
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		Feature.display_mode = 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		election_loaded = true;
		setEnableds();
		
	}
	
	public void initFeatures() {
		Vector<Feature> features = featureCollection.features;
		System.out.println(features.size()+" precincts loaded.");
		System.out.println("Initializing blocks...");
		featureCollection.initBlocks();
		Settings.resetAnnealing();
		featureCollection.ecology.generation = 0;

		minx = features.get(0).geometry.coordinates[0][0][0];
		maxx = features.get(0).geometry.coordinates[0][0][0];
		miny = features.get(0).geometry.coordinates[0][0][1];
		maxy = features.get(0).geometry.coordinates[0][0][1];
		HashSet<String> types = new HashSet<String>();
		for( Feature f : features) {
			double[][][] coordinates2 = f.geometry.coordinates;
			for( int j = 0; j < coordinates2.length; j++) {
				double[][] coordinates = coordinates2[j];
				for( int i = 0; i < coordinates.length; i++) {
					if( coordinates[i][0] < minx) {
						minx = coordinates[i][0];
					}
					if( coordinates[i][0] > maxx) {
						maxx = coordinates[i][0];
					}
					if( coordinates[i][1] < miny) {
						miny = coordinates[i][1];
					}
					if( coordinates[i][1] > maxy) {
						maxy = coordinates[i][1];
					}
				}
			}					
		}
		System.out.println(""+minx+","+miny);
		System.out.println(""+maxx+","+maxy);
		resetZoom();
		mapPanel.featureCollection = featureCollection;
		mapPanel.invalidate();
		mapPanel.repaint();
		featureCollection.ecology.mapPanel = mapPanel;
		featureCollection.ecology.statsPanel = panelStats;
		featureCollection.initEcology();
		System.out.println("Ready.");
		
		geo_loaded = true;
		setEnableds();
	}
	
	public MainFrame() {
		mainframe = this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Automatic Redistricter");
		Dimension d = new Dimension(800,1024);
		//this.setPreferredSize(d);
		this.setSize(d);
		//this.getContentPane().setPreferredSize(d);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		menuBar.add(mnFile);
		
		mnFile.add(mntmOpenGeojson);
		mntmOpenEsriShapefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ESRI shapefiles", "shp");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				File[] ff = new File[]{fd};//fd.listFiles();
				
				featureCollection = new FeatureCollection(); 
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}

				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
				
				for( int i = 0; i < ff.length; i++) {
					String s = ff[i].getName().toLowerCase();
					/*if(s.indexOf(".json") < 0) {
						continue;
					}*/
					System.out.println("Processing "+s+"...");
					File f = ff[i];
					StringBuffer sb = getFile(f);
					
					featureCollection.ecology.stopEvolving();
					geo_loaded = false;
					evolving = false;
					Feature.display_mode = 0;
					setEnableds();
					
					//FeatureCollection fc = new FeatureCollection();
					if( panelStats != null) {
						panelStats.featureCollection = featureCollection;
					}
					featureCollection.loadShapeFile(f);
					/*
					for( Feature fe : fc.features) {
						if( suppress_duplicates) {
							hmFeatures.put(fe.properties.DISTRICT, fe);
						} else {
							featureCollection.features.add(fe);
						}
					}
					*/
					
				}
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				Vector<Feature> features = featureCollection.features;
				System.out.println(features.size()+" precincts loaded.");
				System.out.println("Initializing blocks...");
				featureCollection.initBlocks();
				minx = features.get(0).geometry.coordinates[0][0][0];
				maxx = features.get(0).geometry.coordinates[0][0][0];
				miny = features.get(0).geometry.coordinates[0][0][1];
				maxy = features.get(0).geometry.coordinates[0][0][1];
				HashSet<String> types = new HashSet<String>();
				for( Feature f : features) {
					double[][][] coordinates2 = f.geometry.coordinates;
					for( int j = 0; j < coordinates2.length; j++) {
						double[][] coordinates = coordinates2[j];
						for( int i = 0; i < coordinates.length; i++) {
							if( coordinates[i][0] < minx) {
								minx = coordinates[i][0];
							}
							if( coordinates[i][0] > maxx) {
								maxx = coordinates[i][0];
							}
							if( coordinates[i][1] < miny) {
								miny = coordinates[i][1];
							}
							if( coordinates[i][1] > maxy) {
								maxy = coordinates[i][1];
							}
						}
					}					
				}
				System.out.println(""+minx+","+miny);
				System.out.println(""+maxx+","+maxy);
				resetZoom();
				
				mapPanel.featureCollection = featureCollection;
				mapPanel.invalidate();
				mapPanel.repaint();
				featureCollection.ecology.mapPanel = mapPanel;
				featureCollection.ecology.statsPanel = panelStats;
				featureCollection.initEcology();
				

				System.out.println("Ready.");
				
				geo_loaded = true;
				setEnableds();

			}
		});
		
		mnFile.add(mntmOpenEsriShapefile);
		mnFile.add(chckbxmntmLatitudeLongitude);
		
		mnFile.add(new JSeparator());
		
		mnFile.add(chckbxmntmOpenCensusResults);
		
		mnFile.add(mntmOpenElectionResults);
		
		JMenuItem mntmImportData = new JMenuItem("Import data");
		mntmImportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		mnFile.add(mntmImportData);

		mnFile.add(new JSeparator());

		mnFile.add(mntmImportcsv);
		mnFile.add(mntmExportcsv);

		
		mnFile.add(new JSeparator());
		
		mnFile.add(mntmImportPopulation);
		mnFile.add(mntmExportPopulation);

		
		mnFile.add(new JSeparator());
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnSamples = new JMenu("Samples");
		menuBar.add(mnSamples);
		
		JMenuItem mntmWisconsin = new JMenuItem("Wisconsin 2012");
		mntmWisconsin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StringBuffer sb = new StringBuffer();
				InputStream in = getClass().getResourceAsStream("/resources/Wards_111312_ED_110612.json"); 
				//System.exit(0);
				//BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				try {
					while( in.available() > 0) {
						byte[] bb = new byte[in.available()];
						in.read(bb);
						sb.append(new String(bb));
						Thread.sleep(load_wait);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				FeatureCollection fc = new FeatureCollection();
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}

				try {
					fc.fromJSON(sb.toString());
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
				for( Feature fe : fc.features) {
						featureCollection.features.add(fe);
				}
				initFeatures();

				sb = new StringBuffer();
				in = getClass().getResourceAsStream("/resources/combined_results.txt"); 
				//System.exit(0);
				//BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				try {
					while( in.available() > 0) {
						byte[] bb = new byte[in.available()];
						in.read(bb);
						sb.append(new String(bb));
						Thread.sleep(load_wait);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				loadElection(sb.toString());

			}
		});
		mnSamples.add(mntmWisconsin);
		
		//menuBar.add(mnGeography);
		
		chckbxmntmLatitudeLongitude.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Edge.isLatLon = chckbxmntmLatitudeLongitude.isSelected();
				featureCollection.recalcEdgeLengths();
			}
		});
		
		
		mntmOpenGeojson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				File[] ff = new File[]{fd};//fd.listFiles();
				
				featureCollection = new FeatureCollection(); 
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}

				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
				
				for( int i = 0; i < ff.length; i++) {
					String s = ff[i].getName().toLowerCase();
					if(s.indexOf(".json") < 0) {
						continue;
					}
					System.out.println("Processing "+s+"...");
					File f = ff[i];
					StringBuffer sb = getFile(f);
					
					featureCollection.ecology.stopEvolving();
					geo_loaded = false;
					evolving = false;
					Feature.display_mode = 0;
					setEnableds();
					
					FeatureCollection fc = new FeatureCollection();
					if( panelStats != null) {
						panelStats.featureCollection = featureCollection;
					}

					try {
						fc.fromJSON(sb.toString());
					} catch (Exception ex) {
						System.out.println("ex "+ex);
						ex.printStackTrace();
					}
					for( Feature fe : fc.features) {
						//if( fe.properties.DISTRICT != null && !fe.properties.DISTRICT.toLowerCase().equals("null") ) {
						if( suppress_duplicates) {
							hmFeatures.put(fe.properties.DISTRICT, fe);
						} else {
							featureCollection.features.add(fe);
						}
						//}
					}
					
				}
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				Vector<Feature> features = featureCollection.features;
				System.out.println(features.size()+" precincts loaded.");
				System.out.println("Initializing blocks...");
				featureCollection.initBlocks();
				minx = features.get(0).geometry.coordinates[0][0][0];
				maxx = features.get(0).geometry.coordinates[0][0][0];
				miny = features.get(0).geometry.coordinates[0][0][1];
				maxy = features.get(0).geometry.coordinates[0][0][1];
				HashSet<String> types = new HashSet<String>();
				for( Feature f : features) {
					double[][][] coordinates2 = f.geometry.coordinates;
					for( int j = 0; j < coordinates2.length; j++) {
						double[][] coordinates = coordinates2[j];
						for( int i = 0; i < coordinates.length; i++) {
							if( coordinates[i][0] < minx) {
								minx = coordinates[i][0];
							}
							if( coordinates[i][0] > maxx) {
								maxx = coordinates[i][0];
							}
							if( coordinates[i][1] < miny) {
								miny = coordinates[i][1];
							}
							if( coordinates[i][1] > maxy) {
								maxy = coordinates[i][1];
							}
						}
					}					
				}
				System.out.println(""+minx+","+miny);
				System.out.println(""+maxx+","+maxy);
				resetZoom();
				
				mapPanel.featureCollection = featureCollection;
				mapPanel.invalidate();
				mapPanel.repaint();
				featureCollection.ecology.mapPanel = mapPanel;
				featureCollection.ecology.statsPanel = panelStats;
				featureCollection.initEcology();
				

				System.out.println("Ready.");
				
				geo_loaded = true;
				setEnableds();

			}
		});
		
		//menuBar.add(mnDemographics);
		
		chckbxmntmOpenCensusResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					File f = null;
					
					if( use_sample) {
						String os_name = System.getProperty("os.name").toLowerCase();
						if( os_name.indexOf("windows") >= 0) {
							f = new File("C:\\Users\\kbaas.000\\Documents\\shapefiles\\dallas texas\\2012\\census\\population.txt");
						}
					} else {
						JFileChooser jfc = new JFileChooser();
						jfc.showOpenDialog(null);
						f = jfc.getSelectedFile();
					}
					if( f == null) {
						return;
					}
					StringBuffer sb = getFile(f);
					String s = sb.toString();
					String[] lines = s.split("\n");
					
					Vector<String> not_found_in_geo = new Vector<String>();
					for( Block b : featureCollection.blocks) {
						b.has_census_results = false;
					}
					for( int i = 0; i < lines.length; i++) {
						String[] ss = lines[i].split("\t");
						String district = ss[0].trim();
						Block b = featureCollection.precinctHash.get(district);
						if( b == null) {
							not_found_in_geo.add(district);
						} else {
							b.has_census_results = true;
						}
					}
					Vector<String> not_found_in_census = new Vector<String>();
					for( Block b : featureCollection.blocks) {
						if( b.has_census_results == false) {
							not_found_in_census.add(b.name);
						}
					}
					if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
						for( Block b : featureCollection.blocks) {
							b.has_census_results = false;
						}
						JOptionPane.showMessageDialog(null,""
								+"Census data doesn't match geographic data.\n"
								+"Census data without matching geo data: "+not_found_in_geo.size()+"\n"
								+"Geo data without matching census data: "+not_found_in_census.size()
								, "Mismatch of geographic regions"
								, 0);
						return;
					}
					for( int i = 0; i < lines.length; i++) {
						String[] ss = lines[i].split("\t");
						String district = ss[0].trim();
						Block b = featureCollection.precinctHash.get(district);
						b.has_census_results = true;
						b.population = Double.parseDouble(ss[1].replaceAll(",",""));
						System.out.println("block "+b.id+" added census "+b.population);
					}
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
				Feature.display_mode = 2;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mntmOpenElectionResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					File f = null;
					
					if( use_sample) {
						String os_name = System.getProperty("os.name").toLowerCase();
						if( os_name.indexOf("windows") >= 0) {
							//f = new File("C:\\Users\\kbaas.000\\Documents\\shapefiles\\dallas texas\\2012\\general election - presidential\\results.txt");
							f = new File("C:\\Users\\kbaas.000\\git\\autoredistrict_\\data sources\\wisconsin\\2012\\combined_results.txt");
						}
					} else {
						JFileChooser jfc = new JFileChooser();
						jfc.showOpenDialog(null);
						f = jfc.getSelectedFile();
					}
					if( f == null) {
						return;
					}
					StringBuffer sb = getFile(f);
					String s = sb.toString();
					loadElection(s);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		menuBar.add(mnEvolution);
		
		chckbxmntmMutateAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_all = chckbxmntmMutateAll.isSelected();
			}
		});
		chckbxmntmReplaceAll.setSelected(Settings.replace_all);
		
		chckbxmntmReplaceAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.replace_all = chckbxmntmReplaceAll.isSelected();
				chckbxmntmMutateAll.setEnabled(!Settings.replace_all);
			}
		});
		chckbxmntmAutoAnneal.setSelected(true);
		
		chckbxmntmAutoAnneal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.auto_anneal = chckbxmntmAutoAnneal.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmAutoAnneal);
		mnEvolution.add(chckbxmntmReplaceAll);
		mnEvolution.add(chckbxmntmMutateAll);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		chckbxmntmFlipVertical.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetZoom();
			}
		});
		mnView.add(chckbxmntmFlipVertical);
		
		chckbxmntmFlipHorizontal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetZoom();
			}
		});
		mnView.add(chckbxmntmFlipHorizontal);
		
		chckbxmntmShowPrecinctLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.showPrecinctLabels = chckbxmntmShowPrecinctLabels.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		mnView.add(new JSeparator());
		mnView.add(chckbxmntmShowPrecinctLabels);
		JCheckBoxMenuItem chckbxmntmShowDistrictLabels = new JCheckBoxMenuItem("Show district labels");
		chckbxmntmShowDistrictLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null,"Not implemented");
			}
		});
		mnView.add(chckbxmntmShowDistrictLabels);

		chckbxmntmHideMapLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Feature.draw_lines = !chckbxmntmHideMapLines.isSelected();
			}
		});
		mnView.add(chckbxmntmHideMapLines);
		
		
		mnView.add(new JSeparator());
		mntmResetZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetZoom();
			}
		});
		
		mnView.add(mntmResetZoom);
		mntmZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapPanel.selection = null;
				mapPanel.zooming = true;
				mntmUndoZoom.setEnabled(true);
				//JOptionPane.showMessageDialog(null,"Not implemented");
			}
		});
		mntmUndoZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapPanel.zoomOut();
				if( mapPanel.zoomStack.empty()){
					mntmUndoZoom.setEnabled(false);
				}
			}
		});
		
		mnView.add(mntmUndoZoom);
		
		mnView.add(mntmZoomIn);
		
		mnView.add(new JSeparator());

		mnView.add(mntmShowStats);
		mnView.add(mntmShowGraph);
		
		JMenuItem mntmShowProperties = new JMenuItem("Show properties");
		mntmShowProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				df.setTableSource(featureCollection);
				df.show();//new DialogShowProperties(featureCollection).show();
			}
		});
		mnView.add(mntmShowProperties);

		
		//JMenu mnResults = new JMenu("Results");
		//menuBar.add(mnResults);
		
		mntmExportcsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( featureCollection.ecology.population == null || featureCollection.ecology.population.size() == 0) {
					JOptionPane.showMessageDialog(null,"No results");
				}
				JFileChooser jfc = new JFileChooser();
				jfc.showSaveDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null) {
					return;
				}
				StringBuffer sb = new StringBuffer();
				try {
					FileOutputStream fis = new FileOutputStream(f);
					
					DistrictMap dm = featureCollection.ecology.population.get(0);
					for( int i = 0; i < dm.block_districts.length; i++) {
						Block b = featureCollection.ecology.blocks.get(i);
						//sb.append(b.name+", "+dm.block_districts[i]+"\n\r");
						fis.write((""+b.name.trim()+", "+dm.block_districts[i]+"\r\n").getBytes());
					}
					
					//fis.write(sb.toString().getBytes());
					fis.flush();
					fis.close();
					JOptionPane.showMessageDialog(null,"File saved.");
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					return;
				}
			}
		});
		
		mntmShowStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					frameStats.show();
					frameStats.invalidate();
					frameStats.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
					
				}
			}
		});
		mntmShowGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameGraph.show();
			}
		});
		
		
		mntmImportcsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();

				String s = getFile(f).toString();
				String[] lines = s.split("\n");
				
				Vector<String> not_found_in_geo = new Vector<String>();
				for( Block b : featureCollection.blocks) {
					b.temp = -1;
				}
				for( int i = 0; i < lines.length; i++) {
					try {
						String[] ss = lines[i].split(",");
						String district = ss[0].trim();
						Block b = featureCollection.precinctHash.get(district);
						if( b == null) {
							not_found_in_geo.add(district);
						} else {
							b.temp = Integer.parseInt(ss[1].trim());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				Vector<String> not_found_in_census = new Vector<String>();
				for( Block b : featureCollection.blocks) {
					if( b.temp < 0) {
						not_found_in_census.add(b.name);
					}
				}
				if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
					for( Block b : featureCollection.blocks) {
						b.temp = -1;
					}
					JOptionPane.showMessageDialog(null,""
							+"Result data doesn't match geographic data.\n"
							+"Result data without matching geo data: "+not_found_in_geo.size()+"\n"
							+"Geo data without matching result data: "+not_found_in_census.size()
							, "Mismatch of geographic regions"
							, 0);
					return;
				}
				int[] new_block_districts = new int[featureCollection.blocks.size()];
				int num_districts = 0;
				for( int i = 0; i < new_block_districts.length; i++) {
					int d = featureCollection.blocks.get(i).temp;
					if( num_districts < d) {
						num_districts = d;
					}
					new_block_districts[i] = d;
				}
				num_districts++;
				Settings.num_districts = num_districts;
				textField_2.setText(""+Settings.num_districts);
				if( featureCollection.ecology.population == null) {
					featureCollection.ecology.population = new Vector<DistrictMap>();
				}
				if( featureCollection.ecology.population.size() < 1) {
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.blocks,Settings.num_districts,new_block_districts));
				}
				while( featureCollection.ecology.population.size() < Settings.population) {
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.blocks,Settings.num_districts,new_block_districts));
				}
				for( DistrictMap dm : featureCollection.ecology.population) {
					dm.setGenome(new_block_districts);
					dm.fillDistrictBlocks();
				
				}
				Feature.display_mode = 0;
				mapPanel.invalidate();
				mapPanel.repaint();
				panelStats.getStats();
				JOptionPane.showMessageDialog(null, "Result loaded.");
			}
			
		});
		mntmExportPopulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showMessageDialog(null,"Not implemented.");
				if( featureCollection.ecology.population == null || featureCollection.ecology.population.size() == 0) {
					JOptionPane.showMessageDialog(null,"No results");
				}
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showSaveDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				for( int pop = 0; pop < featureCollection.ecology.population.size(); pop++) {
					System.out.println("Saving map "+pop+"...");
					//StringBuffer sb = new StringBuffer();
					try {
						File f = new File(fd.getAbsolutePath()+File.separator+pop+".csv");
						FileOutputStream fis = new FileOutputStream(f);
						
						DistrictMap dm = featureCollection.ecology.population.get(pop);
						for( int i = 0; i < dm.block_districts.length; i++) {
							Block b = featureCollection.ecology.blocks.get(i);
							//sb.append(b.name+", "+dm.block_districts[i]+"\n\r");
							fis.write((""+b.name.trim()+", "+dm.block_districts[i]+"\r\n").getBytes());
						}
						
						//fis.write(sb.toString().getBytes());
						fis.flush();
						fis.close();
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(null,"File saved.");
			}
		});
		
		mntmImportPopulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,"Not implemented.");
			}
		});
		
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		panel.setPreferredSize(new Dimension(200,100));
		panel_1.setPreferredSize(new Dimension(200,100));
		panel.setLayout(null);
		panel_1.setLayout(null);
		splitPane.setLeftComponent(panel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(0, 299, 200, 416);
		panel.add(panel_2);
		panel_2.setLayout(null);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel lblCompactness = new JLabel("Border length");
		lblCompactness.setBounds(6, 36, 172, 16);
		panel_2.add(lblCompactness);
		slider_3.setBounds(6, 57, 190, 29);
		panel_2.add(slider_3);
		
		JLabel lblContiguency = new JLabel("Representation imbalance");
		lblContiguency.setBounds(6, 220, 172, 16);
		panel_2.add(lblContiguency);
		slider_7.setBounds(6, 245, 190, 29);
		panel_2.add(slider_7);
		
		JLabel lblEvolutionaryPressure = new JLabel("Evolutionary pressure");
		lblEvolutionaryPressure.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEvolutionaryPressure.setBounds(6, 8, 179, 16);
		panel_2.add(lblEvolutionaryPressure);
		
		JLabel lblProportionalRepresentation = new JLabel("Population imbalance");
		lblProportionalRepresentation.setBounds(6, 158, 172, 16);
		panel_2.add(lblProportionalRepresentation);
		slider_5.setBounds(6, 179, 190, 29);
		panel_2.add(slider_5);
		
		JLabel lblVotingPowerBalance = new JLabel("Voting power imbalance");
		lblVotingPowerBalance.setBounds(6, 286, 172, 16);
		panel_2.add(lblVotingPowerBalance);
		slider_6.setBounds(6, 307, 190, 29);
		panel_2.add(slider_6);
		
		JLabel lblConnectedness = new JLabel("Disconnected population");
		lblConnectedness.setBounds(6, 97, 172, 16);
		panel_2.add(lblConnectedness);
		
		slider_2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.disconnected_population_weight = slider_2.getValue()/100.0;
			}
		});
		slider_2.setBounds(6, 118, 190, 29);
		panel_2.add(slider_2);
		
		JLabel lblMaxPop = new JLabel("Max population % diff ");
		lblMaxPop.setBounds(6, 353, 134, 16);
		panel_2.add(lblMaxPop);
		
		textField_3.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textField_3.postActionEvent();
			}
		});
		textField_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.max_pop_diff = Integer.parseInt(textField_3.getText());
				} catch (Exception ex) { }
			}
		});
		textField_3.setText("9");
		textField_3.setColumns(10);
		textField_3.setBounds(138, 347, 58, 28);
		panel_2.add(textField_3);
		
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setBounds(0, 92, 200, 195);
		panel.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblPopulation = new JLabel("Population");
		lblPopulation.setBounds(6, 40, 104, 16);
		panel_3.add(lblPopulation);
		textField.setBounds(105, 34, 91, 28);
		panel_3.add(textField);
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
		
		textField.setText("64");
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Population dynamics");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(6, 6, 159, 16);
		panel_3.add(lblNewLabel);
		JLabel lblBorderMutation = new JLabel("% border mutation");
		lblBorderMutation.setBounds(6, 128, 172, 16);
		panel_3.add(lblBorderMutation);
		slider_1.setBounds(6, 149, 190, 29);
		panel_3.add(slider_1);
		
		JLabel lblTrials = new JLabel("Elections simulated");
		lblTrials.setBounds(6, 74, 134, 16);
		panel_3.add(lblTrials);
		textField_1.setBounds(138, 68, 58, 28);
		panel_3.add(textField_1);
		textField_1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textField_1.postActionEvent();
			}
		});
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.num_elections_simulated = new Integer(textField_1.getText());
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textField_1.setText("2");
		textField_1.setColumns(10);
		textField_2.setText("3");
		
		
		textField_2.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textField_2.postActionEvent();
			}
		});
		textField_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.num_districts = Integer.parseInt(textField_2.getText());
				} catch (Exception ex) { }
			}
		});
		textField_2.setColumns(10);
		textField_2.setBounds(132, 52, 56, 28);
		panel.add(textField_2);
		
		JLabel lblNumOfDistricts = new JLabel("Num. of districts");
		lblNumOfDistricts.setBounds(6, 58, 124, 16);
		panel.add(lblNumOfDistricts);
		
		JButton button = new JButton("<|");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Ecology.invert = -1;
				
				featureCollection.ecology.startEvolving();
				evolving = true;
				setEnableds();

			}
		});
		button.setBounds(26, 17, 46, 29);
		panel.add(button);
		
		JButton btnX = new JButton("| |");
		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				featureCollection.ecology.stopEvolving();
				evolving = false;
				setEnableds();

			}
		});
		btnX.setBounds(84, 17, 46, 29);
		panel.add(btnX);
		
		JButton button_2 = new JButton("|>");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Ecology.invert = 1;
				featureCollection.ecology.startEvolving();
				evolving = true;
				setEnableds();

			}
		});
		button_2.setBounds(142, 17, 46, 29);
		panel.add(button_2);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_1.getValue()/100.0;
			}
		});
		slider_6.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.voting_power_balance_weight = slider_6.getValue()/100.0;
			}
		});
		slider_7.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disenfranchise_weight = slider_7.getValue()/100.0;

			}
		});
		slider_5.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.population_balance_weight = slider_5.getValue()/100.0;
			}
		});
		slider_3.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.geometry_weight = slider_3.getValue()/100.0;
			}
		});
		
		chckbxmntmMutateAll.setEnabled(!Settings.replace_all);
		
		Settings.mutation_rate = 0; 
		Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_1.getValue()/100.0;
		Settings.voting_power_balance_weight = slider_6.getValue()/100.0;
		Settings.disenfranchise_weight = slider_7.getValue()/100.0;
		Settings.population_balance_weight = slider_5.getValue()/100.0;
		Settings.geometry_weight = slider_3.getValue()/100.0;
		Settings.disconnected_population_weight = slider_2.getValue()/100.0;

		
		chckbxmntmMutateAll.setSelected(true);
		Settings.mutate_all = true;
		//Settings.speciation_fraction = 0.5;//1.0;
		//Settings.disconnected_population_weight = 0.0;

		splitPane.setRightComponent(mapPanel);
		
		panelStats.featureCollection = featureCollection;
		frameStats = new JFrame();
		frameStats.setContentPane(panelStats);
		frameStats.setTitle("Map stats");
		Dimension dim = panelStats.getPreferredSize();
		dim.height += 20;		
		frameStats.setSize(dim);
		
		frameGraph = new JFrame();
		frameGraph.setContentPane(panelGraph);
		frameGraph.setTitle("Graph");
		dim = panelGraph.getPreferredSize();
		dim.height += 20;
		frameGraph.setSize(dim);
		
		frameGraph.move(this.getWidth(), this.getX());
		frameStats.move(this.getWidth(), this.getX()+frameGraph.getHeight());
		frameStats.show();
		frameGraph.show();
		
		Settings.mutation_rateChangeListeners.add(this);
		Settings.populationChangeListeners.add(this);

		
		setEnableds();
	}
	public StringBuffer getFile(File f) {		
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(f);
			while( fis.available() > 0) {
				byte[] bb = new byte[fis.available()];
				fis.read(bb);
				sb.append(new String(bb));
				Thread.sleep(load_wait);
			}
			
			fis.close();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			return sb;
		} 
		return sb;
	}

	@Override
	public void valueChanged() {
 		textField.setText(""+Settings.population);
		//System.out.println("new boundary mutation rate: "+Settings.mutation_boundary_rate+" total: "+total+" mutated: "+mutated);
		slider_1.setValue((int)(Settings.mutation_boundary_rate*100.0/MainFrame.boundary_mutation_rate_multiplier));
		invalidate();
		repaint();
	}
}
