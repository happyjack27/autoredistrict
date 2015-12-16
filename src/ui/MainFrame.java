package ui;

import geography.*;
import geography.Properties;
import solutions.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.*; 
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;

import org.nocrala.tools.gis.data.esri.shapefile.*;
import org.nocrala.tools.gis.data.esri.shapefile.header.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.*;

import com.hexiong.jdbf.DBFReader;
import com.hexiong.jdbf.DBFWriter;
import com.hexiong.jdbf.JDBFException;
import com.hexiong.jdbf.JDBField;


public class MainFrame extends JFrame implements iChangeListener, iDiscreteEventListener {
	/*
	TODO: pre/post serialize to get district #?
			-- need to make data exporter, and make it so that it imports district when applicable.
			need to be able to customize district column name and save in project file, so can load districts.
			*/
	
	//======LOOPBACK
	public static MainFrame mainframe;
	public DialogManageLocks manageLocks = new DialogManageLocks();
	public PanelSeats seatsPanel = new PanelSeats();
	public JProgressBar progressBar = new JProgressBar();
	public FrameSeatsVotesChart frameSeatsVotesChart = new FrameSeatsVotesChart();
	public FrameRankedDistricts frameRankedDist = new FrameRankedDistricts();
	
	public ButtonGroup seatsModeBG = new ButtonGroup();
	 
	

	//public Ecology ecology = new Ecology();
	//========MODELS
	public FeatureCollection featureCollection = new FeatureCollection();
	public Project project = new Project();
	public DemographicSet activeDemographicSet = new DemographicSet();

	
	//========PRIMITIVES

	boolean geo_loaded = false;
	boolean census_loaded = false;
	boolean election_loaded = false;
	boolean evolving = false;
	
	boolean suppress_duplicates = false;
	boolean use_sample = false;
	
	public boolean hushcomboBoxPopulation = false;
	public boolean hushcomboBoxPrimaryKey = false;
	public boolean hushcomboBoxDistrict = false;
	public boolean hushcomboBoxCounty = false;

	double mutation_rate_multiplier = 0.1;
	public static double boundary_mutation_rate_multiplier = 0.2;
	long load_wait = 100;
	
	double minx,maxx;
	public double miny;
	public double maxy;
	
	public String openedProjectFilePath = "";	
	
	

	
	//========CONTAINERS
    public static JDialog dlg = new JDialog(mainframe, "Working", true);
    public static JProgressBar dpb = new JProgressBar(0, 500);
    public static JLabel dlbl = new JLabel();
	public MapPanel mapPanel = new MapPanel(); 
	JFrame frameStats = new JFrame();
	public PanelStats panelStats = new PanelStats();
	JFrame frameGraph = new JFrame();
	DialogShowProperties df = new DialogShowProperties();
	public PanelGraph panelGraph = new PanelGraph();

	//===========COMPONENTS - MENU ITEMS
	public ButtonGroup selectionType = new ButtonGroup();
	JMenuItem mntmShowVoteBalance = new JMenuItem("Color by vote balance");;
	JMenuItem mntmShowDemographics = new JMenuItem("Color by demographic");;
	JCheckBoxMenuItem chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
	JCheckBoxMenuItem chckbxmntmHideMapLines = new JCheckBoxMenuItem("Show map lines");
	JCheckBoxMenuItem chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
	JCheckBoxMenuItem chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
	JCheckBoxMenuItem chckbxmntmAutoAnneal = new JCheckBoxMenuItem("Auto anneal");
	JCheckBoxMenuItem chckbxmntmShowDistrictLabels = new JCheckBoxMenuItem("Show district labels");
	JMenuItem mntmSaveProjectFile = new JMenuItem("Save project file");
	JMenuItem mntmSaveData = new JMenuItem("Save data");
	JMenuItem mntmMergeData = new JMenuItem("Merge data");
	JMenuItem mntmRenumber = new JMenuItem("Renumber districts");

	//JMenu mnGeography = new JMenu("Geography");
	JMenuItem mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
	//JMenu mnDemographics = new JMenu("Demographics");
	JMenuItem chckbxmntmOpenCensusResults = new JMenuItem("Open Census results");
	JMenuItem mntmOpenElectionResults = new JMenuItem("Open Election results");
	JMenu mnEvolution = new JMenu("Evolution");
	JMenu mnHelp = new JMenu("Help");
	JMenuItem mntmWebsite = new JMenuItem("Website");
	JMenuItem mntmSourceCode = new JMenuItem("Source code");
	JMenuItem mntmLicense = new JMenuItem("License");
	JMenuItem mntmExportcsv = new JMenuItem("Export results .csv");
	JMenuItem mntmImportcsv = new JMenuItem("Import results .csv");
	JMenuItem mntmShowStats = new JMenuItem("Show stats");
	JMenuItem mntmShowData = new JMenuItem("Show data");
	
	JMenu mnImportExport = new JMenu("Aggregate/Deaggregate");

	JMenuItem mntmExportPopulation = new JMenuItem("Export population");
	JMenuItem mntmImportPopulation = new JMenuItem("Import population");
	JMenuItem mntmResetZoom = new JMenuItem("Reset zoom");
	JMenuItem mntmZoomIn = new JMenuItem("Zoom in");
	JMenuItem mntmUndoZoom = new JMenuItem("Undo zoom");
	JMenuItem mntmShowGraph = new JMenuItem("Show graph");
	JMenuItem mntmOpenEsriShapefile = new JMenuItem("Open ESRI shapefile");
	public JComboBox comboBoxPopulation = new JComboBox();
	public JComboBox comboBoxDistrictColumn = new JComboBox();
    public JTextField textFieldNumDistricts = new JTextField();
    public JTextField textField = new JTextField();
	public JTextField textFieldSeatsPerDistrict;

	public JSlider slider_mutation = new JSlider();
	public JSlider sliderDisconnected = new JSlider();
	public JSlider sliderBorderLength = new JSlider();
	public JSlider sliderPopulationBalance = new JSlider();
	public JSlider sliderRepresentation = new JSlider();
	
	public JLabel lblDistrictColumn;
	
	public JButton goButton = new JButton();
	public JButton stopButton = new JButton();
	public JMenuItem mntmOpenProjectFile_1;
	public JPanel panel_4;
	public JButton btnElectionColumns;
	public JMenuItem mntmImportCensusData;
	public JMenuItem mntmExportToBlock;
	public final JSeparator separator_1 = new JSeparator();
	public final JSeparator separator_2 = new JSeparator();
	public final JMenuItem mntmImportAggregate = new JMenuItem("Import & aggregate custom");
	public final JMenuItem mntmExportAndDeaggregate = new JMenuItem("Export and de-aggregate custom");
	public final JMenuItem mntmOpenWktTabdelimited = new JMenuItem("Open WKT tab-delimited");
	
	//=========CLASSES
	class FileThread extends Thread {
    	public File f = null;
    	FileThread() { super(); } 
    	FileThread(File f) {
    		super();
    		this.f = f;
    	}
	}

	class ExportCustomThread extends Thread {
		File f;
		File foutput;
		boolean bdivide;
		DialogSelectLayers dlgselect;
		FileOutputStream fos = null;
		String delimiter = "\t";

		ExportCustomThread() { super(); }
		public void init() {
			JOptionPane.showMessageDialog(mainframe, "Select the .dbf file with census block-level data.\n");
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File(Download.getStartPath()));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
			jfc.showOpenDialog(null);
			f = jfc.getSelectedFile();
			if( f == null)  {
				return;
			}
			String fn = f.getName();
			String ext = fn.substring(fn.length()-3).toLowerCase();
			if( !ext.equals("dbf")) {
				JOptionPane.showMessageDialog(null, "File format not recognized.");
				return;
			}
			
			JOptionPane.showMessageDialog(mainframe, "Select the output file.\n");
			JFileChooser jfc2 = new JFileChooser();
			jfc.setCurrentDirectory(new File(Download.getStartPath()));
			jfc2.addChoosableFileFilter(new FileNameExtensionFilter("csv file","csv"));
			jfc2.showSaveDialog(null);
			foutput = jfc2.getSelectedFile();
			if( foutput == null)  {
				return;
			}
			String fullname = foutput.getPath();
			String extension = fullname.substring(fullname.length()-4).toLowerCase();
			if( extension.equals(".txt") || extension.equals(".tsv")) {
				delimiter = "\t";
			} else 
			if( extension.equals(".csv")) {
				delimiter = ",";
			}
			try {
				System.out.println("creating..."+foutput.getAbsolutePath());
				fos = new FileOutputStream(foutput);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//select columns to deaggregate
			dlgselect = new DialogSelectLayers();
			dlgselect.setData(featureCollection,new Vector<String>());
			dlgselect.show();
			if( !dlgselect.ok) {
				//if( is_evolving) { featureCollection.ecology.startEvolving(); }
				return;
			}
			//(select wether to put number as is or divide by points
			bdivide = JOptionPane.showConfirmDialog(mainframe, "Divide values by number of points?") == JOptionPane.YES_OPTION;
			
			this.start();
		}

		

    	public void run() { 
    		try {
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading file...");
	    		

				String dbfname = f.getAbsolutePath();
				//count number of points in each precinct
	    		double[] points = getCounts(dbfname,bdivide);
 
	
				DBFReader dbfreader;
				try {
					dbfreader = new DBFReader(dbfname);
				} catch (JDBFException e1) {
					e1.printStackTrace();
					return;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_lat = -1;
				int col_lon = -1;
				int col_geoid = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).getName();
					if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
						col_geoid = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
						col_lat = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
						col_lon = i;
					}
				}
				if( col_geoid < 0 || col_lat < 0 || col_lon < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
					return;
				}

	    		dlbl.setText("Making polygons...");

				
				int count = 0;
	    		for( Feature feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				Feature.compare_centroid = true;
				Collections.sort(featureCollection.features);
	    		

    			String s0 = "GEOID"+delimiter+"INTPTLAT"+delimiter+"INTPTLON";
    			for(int i = 0; i < dlgselect.in.size(); i++) {
    				s0 += delimiter+dlgselect.in.get(i);
    			}
				try {
					//System.out.println("writing...");
					fos.write((s0+"\n").getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
					return;
				}



			    while (dbfreader.hasNextRecord()) {
			    	try {
			    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
			    		String[] ss = new String[oo.length];
			    		for( int i = 0; i < oo.length; i++) {
			    			ss[i] = oo[i].toString();
			    		}
			    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
			    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
			    		String geoid = ss[col_geoid];
			    		int ilat = (int)(dlat*Geometry.SCALELATLON);
			    		int ilon = (int)(dlon*Geometry.SCALELATLON);
			    		
			    		Feature feat = getHit(dlon,dlat);
			    		
			    		
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");
			    		} else {
			    			String s = ""+geoid+delimiter+dlat+delimiter+dlon;
			    			for(int i = 0; i < dlgselect.in.size(); i++) {
			    				try {
			    					String key = dlgselect.in.get(i);
		    						if( !bdivide) {
		    							try {
						    				Object str = feat.properties.get(key);
						    				if( str instanceof Double) {
						    					s += delimiter+((Double)str);
						    					
						    				} else {
						    					s += delimiter+str;
						    				}
		    							} catch (Exception ex) {
		    								ex.printStackTrace();
		    							}
		    						} else {
		    							try {
						    				Object str = feat.properties.get(key);
						    				if( str instanceof Double) {
						    					s += delimiter+(((Double)str)/points[feat.vtd.id]);
						    				} else {
				    							double d = Double.parseDouble(str.toString())/points[feat.vtd.id];
				    							s += delimiter+d;
						    				}
		    							} catch (Exception ex) {
		    								ex.printStackTrace();
		    							}
		    						}
			    				} catch (Exception ex) {
			    					System.out.println("ex aa: "+ex);
			    					ex.printStackTrace();
			    					System.exit(0);
			    				}
			    			}

	    					
	    					try {
	    						//System.out.println("writing...");
	    						fos.write((s+"\n").getBytes());
	    					} catch (Exception e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    						JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
	    						return;
	    					}
			    			
			    		}
			    		
			    		count++; 
			    		if( count % 100 == 0) {
			    			System.out.print(".");
				    		dlbl.setText("Doing hit tests... "+count);
			    		}
			    		if( count % (100*100) == 0) {
			    			System.out.println(""+count);

							try {
								fos.flush();
							} catch (Exception e) {
								e.printStackTrace();
							}
			    		}
					} catch (Exception e) {
						// TODO Auto-generated catch ward
						e.printStackTrace();
					}
			    }
			    
	    		dlbl.setText("Finalizing...");
				try {
					fos.flush();
					System.out.println("closing...");
					fos.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
					return;
				} 

	    		
	    		dlg.setVisible(false);
	    		JOptionPane.showMessageDialog(mainframe,"Done exporting to block level.\n"+foutput.getAbsolutePath());
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}

	    double[] getCounts(String dbfname, boolean bdivide) {
	    	System.out.println("getting counts...");
			double[] points = new double[featureCollection.features.size()];
	    	try {
				for(int i = 0; i < points.length; i++) {
					points[i] = bdivide ? 0.0 : 1.0;
				}
				if( !bdivide) {
					return points;
				}
		
				DBFReader dbfreader;
				try {
					dbfreader = new DBFReader(dbfname);
				} catch (JDBFException e1) {
					e1.printStackTrace();
					return points;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_lat = -1;
				int col_lon = -1;
				int col_geoid = -1;
				
				dlbl.setText("Reading header...");
		
				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).getName();
					if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
						col_geoid = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
						col_lat = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
						col_lon = i;
					}
				}
				if( col_geoid < 0 || col_lat < 0 || col_lon < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
					return points;
				}
		
				dlbl.setText("Making polygons...");
		
				
				int count = 0;
				for( Feature feat : featureCollection.features) {
					feat.geometry.makePolysFull();
				}
				
				dlbl.setText("Doing hit tests...");
				Feature.compare_centroid = true;
				Collections.sort(featureCollection.features);
		
		
			    while (dbfreader.hasNextRecord()) {
			    	try {
			    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
			    		String[] ss = new String[oo.length];
			    		for( int i = 0; i < oo.length; i++) {
			    			ss[i] = oo[i].toString();
			    		}
			    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
			    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
			    		String geoid = ss[col_geoid];
			    		int ilat = (int)(dlat*Geometry.SCALELATLON);
			    		int ilon = (int)(dlon*Geometry.SCALELATLON);
			    		
			    		Feature feat = getHit(dlon,dlat);
			    		
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");

			    		} else {
			    			points[feat.vtd.id]++;
							//String district = ""+(1+featureCollection.ecology.population.get(0).ward_districts[feat.ward.id]);
							
							try {
								//System.out.println("writing...");
								//fos.write((""+geoid+","+district+"\n").getBytes());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
								return points;
							}
			    			
			    		}
			    		
			    		count++; 
			    		if( count % 100 == 0) {
			    			System.out.print(".");
				    		dlbl.setText("Doing hit tests... "+count);
			    		}
			    		if( count % (100*100) == 0) {
			    			System.out.println(""+count);
		
							try {
								//fos.flush();
							} catch (Exception e) {
								e.printStackTrace();
							}
			    		}
					} catch (Exception e) {
						// TODO Auto-generated catch ward
						e.printStackTrace();
					}
			    }
			} catch (Exception ex) {
				System.out.println("ex ac: "+ex);
				ex.printStackTrace();
				System.exit(0);
			}
			System.out.println("got counts.");
		    
		    return points;
	    }
	}

	class ExportToBlockLevelThread extends Thread {
		ExportToBlockLevelThread() { super(); }
    	public void run() { 
    		try {
    			JOptionPane.showMessageDialog(mainframe, "Select the .dbf or .txt file with census block-level data.\n");
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("txt file","txt"));
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null)  {
					return;
				}
				String fn = f.getName();
				String ext = fn.substring(fn.length()-3).toLowerCase();
				if( !ext.equals("dbf") && !ext.equals("txt")) {
					JOptionPane.showMessageDialog(null, "File format not recognized.");
					return;
				}
				
    			JOptionPane.showMessageDialog(mainframe, "Select the output file.\n");
				JFileChooser jfc2 = new JFileChooser();
				jfc2.setCurrentDirectory(new File(Download.getStartPath()));
				jfc2.addChoosableFileFilter(new FileNameExtensionFilter("csv file","csv"));
				jfc2.addChoosableFileFilter(new FileNameExtensionFilter("txt file","txt"));
				jfc2.showSaveDialog(null);
				File foutput = jfc2.getSelectedFile();
				if( foutput == null)  {
					return;
				}
				String output_delimiter = ",";
				FileOutputStream fos = null;
				try {
					System.out.println("creating..."+foutput.getAbsolutePath());
					String ext2 = foutput.getAbsolutePath();
					ext2 = ext2.trim().toLowerCase();
					ext2 = ext2.substring(ext2.length()-4);
					if( ext2.equals(".txt")) {
						output_delimiter = "\t";
					}
					fos = new FileOutputStream(foutput);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				boolean include_centroid = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainframe, "Include centroid (aka center point)?");
				boolean include_header = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainframe, "Include header row?");

    			if( include_header) {
    				fos.write(("GEOID10"+output_delimiter+(include_centroid ? ("INTPTLON"+output_delimiter+"INTPTLAT"+output_delimiter) : "")+"DISTRICT"+"\n").getBytes());
    			}

    			
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading file...");
				if( ext.equals("dbf")) {
	
					String dbfname = f.getAbsolutePath();
					
					DBFReader dbfreader;
					try {
						dbfreader = new DBFReader(dbfname);
					} catch (JDBFException e1) {
						e1.printStackTrace();
						return;
					}
					DataAndHeader dh = new DataAndHeader();
					
					int col_lat = -1;
					int col_lon = -1;
					int col_geoid = -1;
					
		    		dlbl.setText("Reading header...");
	
					dh.header = new String[dbfreader.getFieldCount()];
					for( int i = 0; i < dh.header.length; i++) {
						dh.header[i] = dbfreader.getField(i).getName();
						if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
							col_geoid = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
							col_lat = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
							col_lon = i;
						}
					}
					if( col_geoid < 0 || col_lat < 0 || col_lon < 0) {
						JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
						return;
					}
	
		    		dlbl.setText("Making polygons...");
	
					
					int count = 0;
		    		for( Feature feat : featureCollection.features) {
		    			feat.geometry.makePolysFull();
		    		}
		    		
		    		dlbl.setText("Doing hit tests...");
					Feature.compare_centroid = true;
					Collections.sort(featureCollection.features);
		    		
		    		Hashtable<String,String> used = new Hashtable<String,String>(); 
	
	
				    while (dbfreader.hasNextRecord()) {
				    	try {
				    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
				    		String[] ss = new String[oo.length];
				    		for( int i = 0; i < oo.length; i++) {
				    			ss[i] = oo[i].toString();
				    		}
				    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
				    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
				    		String geoid = ss[col_geoid];
				    		if( used.get(geoid) != null) {
				    			System.out.println("duplicate geoid!: "+geoid);
				    		}
				    		used.put(geoid, geoid);
				    		int ilat = (int)(dlat*Geometry.SCALELATLON);
				    		int ilon = (int)(dlon*Geometry.SCALELATLON);
				    		
				    		Feature feat = getHit(dlon,dlat);
				    		
				    		if( feat == null) {
				    			System.out.print("x");
					    		System.out.println();
					    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");
				    		} else {
		    					String district = ""+(1+featureCollection.ecology.population.get(0).vtd_districts[feat.vtd.id]);
		    					
		    					try {
		    						//System.out.println("writing...");
		    						fos.write((""+geoid+output_delimiter+(include_centroid ? (dlon+output_delimiter+dlat+output_delimiter) : "")+district+"\n").getBytes());
		    					} catch (Exception e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    						JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
		    						return;
		    					}
				    			
				    		}
				    		
				    		count++; 
				    		if( count % 100 == 0) {
				    			System.out.print(".");
					    		dlbl.setText("Doing hit tests... "+count);
				    		}
				    		if( count % (100*100) == 0) {
				    			System.out.println(""+count);
	
								try {
									fos.flush();
								} catch (Exception e) {
									e.printStackTrace();
								}
				    		}
						} catch (Exception e) {
							// TODO Auto-generated catch ward
							System.out.println("ex x " +e);
							e.printStackTrace();
						}
				    }
				} else {
					String delimiter = "\t";
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);
					DataAndHeader dh = new DataAndHeader();
					dh.header = br.readLine().split(delimiter);
					
					//now select the columns
		    		System.out.println("reading columns");
					int col_lat = -1;
					int col_lon = -1;
					int col_geoid = -1;
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
							col_geoid = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
							col_lat = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
							col_lon = i;
						}
					}
					if( col_geoid < 0 || col_lat < 0 || col_lon < 0) {
						JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
						return;
					}
	
		    		System.out.println("doing hit tests");

		    		dlbl.setText("Making polygons...");
	
					
					int count = 0;
		    		for( Feature feat : featureCollection.features) {
		    			feat.geometry.makePolysFull();
		    		}
		    		
		    		dlbl.setText("Doing hit tests...");
					Feature.compare_centroid = true;
					Collections.sort(featureCollection.features);
		    		hits = 0;
		    		misses = 0;
		    		
		    		Hashtable<String,String> used = new Hashtable<String,String>(); 
	
	
		    		
				    String line;
				    while ((line = br.readLine()) != null) {
				    	try {
					    	String[] ss = line.split(delimiter);
				    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
				    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
				    		String geoid = ss[col_geoid];
				    		if( used.get(geoid) != null) {
				    			System.out.println("duplicate geoid!: "+geoid);
				    		}
				    		used.put(geoid, geoid);
				    		int ilat = (int)(dlat*Geometry.SCALELATLON);
				    		int ilon = (int)(dlon*Geometry.SCALELATLON);
				    		
				    		Feature feat = getHit(dlon,dlat);
				    		
				    		if( feat == null) {
				    			System.out.print("x");
					    		System.out.println();
					    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");
				    		} else {
		    					String district = ""+(1+featureCollection.ecology.population.get(0).vtd_districts[feat.vtd.id]);
		    					
		    					try {
		    						//System.out.println("writing...");
		    						fos.write((""+geoid+output_delimiter+(include_centroid ? (dlon+output_delimiter+dlat+output_delimiter) : "")+district+"\n").getBytes());
		    					} catch (Exception e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    						JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
		    						return;
		    					}
				    			
				    		}
				    		
				    		count++; 
				    		if( count % 100 == 0) {
				    			System.out.print(".");
					    		dlbl.setText("Doing hit tests... "+count);
				    		}
				    		if( count % (100*100) == 0) {
				    			System.out.println(""+count);
	
								try {
									fos.flush();
								} catch (Exception e) {
									e.printStackTrace();
								}
				    		}
						} catch (Exception e) {
							// TODO Auto-generated catch ward
							System.out.println("ex x " +e);
							e.printStackTrace();
						}
				    }
					
				}
			    
	    		dlbl.setText("Finalizing...");
				try {
					fos.flush();
					System.out.println("closing...");
					fos.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
					return;
				} 

	    		
	    		dlg.setVisible(false);
	    		JOptionPane.showMessageDialog(mainframe,"Done exporting to block level.\n"+foutput.getAbsolutePath());
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
	}

	class LoadCensusFileThread extends Thread {
		File f;
		LoadCensusFileThread() { super(); }
		public void init() {
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File(Download.getStartPath()));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
			jfc.showOpenDialog(null);
			f = jfc.getSelectedFile();
			if( f == null)  {
				return;
			}
			start();
		}
    	public void run() { 
    		try {
				String fn = f.getName();
				String ext = fn.substring(fn.length()-3).toLowerCase();
				if( !ext.equals("dbf")) {
					JOptionPane.showMessageDialog(null, "File format not recognized.");
					return;
				}
	
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading file...");

				String dbfname = f.getAbsolutePath();
				
				DBFReader dbfreader;
				try {
					dbfreader = new DBFReader(dbfname);
				} catch (JDBFException e1) {
					e1.printStackTrace();
					return;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_pop = -1;
				int col_lat = -1;
				int col_lon = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).getName();
					if( dh.header[i].toUpperCase().trim().equals("POP2010") && col_pop < 0) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().equals("POP")) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().equals("POP2020")) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().equals("PERSONS")) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().equals("POPULATION")) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
						col_lat = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
						col_lon = i;
					}
				}
				if( col_pop < 0) {
					col_pop = 0;
				}
				String opt = (String)JOptionPane.showInputDialog(mainframe, "Select the column with the population in it.", "Select population column.", 0, null, dh.header, dh.header[col_pop]);
				if( opt == null)
					return;
				opt = opt.trim().toUpperCase();
				col_pop = -1;
				for( int i = 0; i < dh.header.length; i++) {
					if( dh.header[i].trim().toUpperCase().equals(opt)) {
						col_pop = i;
						break;
					}
				}
				if( col_pop < 0 || col_lat < 0 || col_lon < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
					dlg.setVisible(false);
					return;
				}

	    		dlbl.setText("Making polygons...");

				
				int count = 0;
	    		for( Feature feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.vtd.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				Feature.compare_centroid = true;
				Collections.sort(featureCollection.features);


	    		hits = 0;
	    		misses = 0;
			    while (dbfreader.hasNextRecord()) {
			    	try {
			    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
			    		String[] ss = new String[oo.length];
			    		for( int i = 0; i < oo.length; i++) {
			    			ss[i] = oo[i].toString();
			    		}
			    		int pop18 = Integer.parseInt(ss[col_pop]);
			    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
			    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
			    		int ilat = (int)(dlat*Geometry.SCALELATLON);
			    		int ilon = (int)(dlon*Geometry.SCALELATLON);
			    		
			    		Feature feat = getHit(dlon,dlat);
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println("miss "+dlon+","+dlat+" ");
			    		} else {
	    					feat.vtd.population += pop18;
	    					feat.vtd.has_census_results = true;
			    			
			    		}
			    		
			    		count++; 
			    		if( count % 100 == 0) {
			    			System.out.print(".");
				    		dlbl.setText("Doing hit tests... "+count);
			    		}
			    		if( count % (100*100) == 0) {
			    			System.out.println(""+count);
			    		}
					} catch (Exception e) {
						// TODO Auto-generated catch ward
						e.printStackTrace();
					}
			    }
			    
	    		dlbl.setText("Finalizing...");

			    
	    		for( Feature feat : featureCollection.features) {
				    feat.properties.put("POPULATION",feat.vtd.population);
				    feat.properties.POPULATION = (int) feat.vtd.population;
	    		}
	    		System.out.println("setting pop column");
	    		
	    		comboBoxPopulation.addItem("POPULATION");
			    setPopulationColumn("POPULATION");
	    		comboBoxPopulation.setSelectedItem("POPULATION");
	    		
	    		dlg.setVisible(false);
	    		JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
	}
	class ImportGazzeterThread extends Thread {
		ImportGazzeterThread() { super(); }
    	public void run() { 
    		try {
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading population...");
	    		String delimiter = "\t";
	    		DataAndHeader dh = new DataAndHeader();
				//TODO: CONVERT TO IMPORT CUSTOM
				FileReader fr = new FileReader(Download.census_tract_file);
				BufferedReader br = new BufferedReader(fr);
				dh.header = br.readLine().split(delimiter);
				
				//now select the columns
	    		System.out.println("reading columns");
				int col_lat = -1;
				int col_lon = -1;
				int col_pop = -1;
				for( int i = 0; i < dh.header.length; i++) {
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
						col_lat = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
						col_lon = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("POP") == 0) {
						col_pop = i;
					}
				}
				if( col_pop < 0 || col_lon < 0 || col_lat < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
					dlg.setVisible(false);
					return;
				}
				
				int count = 0;
	    		for( Feature feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.vtd.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				Feature.compare_centroid = true;
				Collections.sort(featureCollection.features);
	    		
	    		hits = 0;
	    		misses = 0;

				String line = null;
			    while ((line = br.readLine()) != null) {
			    	try {
				    	String[] ss = line.split(delimiter);
				    	if( ss.length > dh.header.length) {
				    		System.out.print("+"+(ss.length - dh.header.length));
				    	}
				    	
			    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
			    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
			    		
		    			Feature feat = getHit(dlon,dlat);
				    	if( feat == null) {
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" ");
				    	} else {
	    					feat.vtd.population += Integer.parseInt(ss[col_pop]);
	    					feat.vtd.has_census_results = true;
				    	}
				    } catch (Exception ex) {
				    	ex.printStackTrace();
				    }
			    }

    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    		
    		dlbl.setText("Finalizing...");
		    
    		for( Feature feat : featureCollection.features) {
			    feat.properties.put("POPULATION",feat.vtd.population);
			    feat.properties.POPULATION = (int) feat.vtd.population;
    		}
    		System.out.println("setting pop column");
    		
    		comboBoxPopulation.addItem("POPULATION");
		    setPopulationColumn("POPULATION");
    		
    		dlg.setVisible(false);
    		JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
    	}
	}

	class ImportCensus2Thread extends Thread {
		ImportCensus2Thread() { super(); }
    	public void run() { 
    		try {
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading population...");
	    		Hashtable<String,String> hash_population = new Hashtable<String,String>();
	    		
				
				DBFReader dbfreader;

				try {
					dbfreader = new DBFReader(Download.census_pop_file.getAbsolutePath());
				} catch (JDBFException e1) {
					e1.printStackTrace();
					return;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_pop = -1;
				int col_geoid_pop = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).getName();
					System.out.println(dh.header[i]+" ");
					if( dh.header[i].toUpperCase().trim().indexOf("POP") == 0) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("BLOCKID") == 0) {
						col_geoid_pop = i;
					}
				}
				if( col_geoid_pop < 0 || col_pop < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found.\ncol_geoid_pop "+col_geoid_pop+"\ncol_pop "+col_pop);
					dlg.setVisible(false);
					return;
				}
				System.out.println();
				
	    		dlbl.setText("Loading population...");
	    		int d = 0;
			    while (dbfreader.hasNextRecord()) {
			    	try {
			    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
			    		String[] ss = new String[oo.length];
			    		for( int i = 0; i < oo.length; i++) {
			    			ss[i] = oo[i].toString();
			    		}
			    		String pop = ss[col_pop];
			    		String geoid =  ss[col_geoid_pop];
			    		if( d < 10) {
			    			System.out.println(geoid);
			    			d++;
			    			//continue;
			    		} else {
			    			//if( true) break;
			    		}

			    		hash_population.put(geoid.trim(),pop.replaceAll(",","").trim());
			    	} catch (Exception ex) {
			    		ex.printStackTrace();
			    	}
			    }
			    dbfreader.close();
			    
				try {
					dbfreader = new DBFReader(Download.census_centroid_file.getAbsolutePath());
				} catch (JDBFException e1) {
					e1.printStackTrace();
					return;
				}

			    
				int col_lat = -1;
				int col_lon = -1;
				int col_geoid_centroid = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).getName();
					System.out.println(dh.header[i]+" ");

					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
						col_lat = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
						col_lon = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
						col_geoid_centroid = i;
					}
				}
				if( col_geoid_centroid < 0 || col_lat < 0 || col_lon < 0) {
					JOptionPane.showMessageDialog(mainframe, "Required columns not found."
							+"\ncol_geoid_centroid "+col_geoid_centroid
							+"\ncol_lat "+col_lat
							+"\ncol_lon "+col_lon
							);
					dlg.setVisible(false);
					return;
				}


	    		dlbl.setText("Making polygons...");

				
				int count = 0;
	    		for( Feature feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.vtd.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				Feature.compare_centroid = true;
				Collections.sort(featureCollection.features);


	    		hits = 0;
	    		misses = 0;
	    		int c = 0;
			    while (dbfreader.hasNextRecord()) {
			    	try {
			    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
			    		String[] ss = new String[oo.length];
			    		for( int i = 0; i < oo.length; i++) {
			    			ss[i] = oo[i].toString();
			    		}
			    		String geoid = ss[col_geoid_centroid].trim();
			    		if( c < 10) {
			    			System.out.println(geoid);
			    			c++;
			    			//continue;
			    		} else {
			    			//if( true) return;
			    		}
			    		int pop18 = 0;
			    		String pop = hash_population.get(geoid);
			    		if( pop == null) {
			    			System.out.println("geoid not found: "+geoid);
			    		} else {
			    			pop18 = Integer.parseInt(pop);
			    		}
			    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
			    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
			    		int ilat = (int)(dlat*Geometry.SCALELATLON);
			    		int ilon = (int)(dlon*Geometry.SCALELATLON);
			    		
			    		Feature feat = getHit(dlon,dlat);
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println("miss "+dlon+","+dlat+" ");
			    		} else {
	    					feat.vtd.population += pop18;
	    					feat.vtd.has_census_results = true;
			    			
			    		}
			    		
			    		count++; 
			    		if( count % 100 == 0) {
			    			System.out.print(".");
				    		dlbl.setText("Doing hit tests... "+count);
			    		}
			    		if( count % (100*100) == 0) {
			    			System.out.println(""+count);
			    		}
					} catch (Exception e) {
						// TODO Auto-generated catch ward
						e.printStackTrace();
					}
			    }
			    dbfreader.close();
			    
	    		dlbl.setText("Finalizing...");

			    
	    		for( Feature feat : featureCollection.features) {
				    feat.properties.put("POPULATION",feat.vtd.population);
				    feat.properties.POPULATION = (int) feat.vtd.population;
	    		}
	    		System.out.println("setting pop column");
	    		
	    		comboBoxPopulation.addItem("POPULATION");
			    setPopulationColumn("POPULATION");
	    		
	    		dlg.setVisible(false);
	    		JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
	}

	// ask what file type
	//ask whether to accumulate or use first match
	//if first match, ask whether to convert from 0 to 1 indexed, vice-versa, or none.
	//or just do it anyways?
	
	public int hits = 0;
	public int misses = 0;
	public final JSeparator separator_3 = new JSeparator();
	public final JMenuItem mntmAntialiasingOff = new JMenuItem("Antialiasing off");
	public final JMenuItem mntmxAntialiasing = new JMenuItem("2x antialiasing");
	public final JMenuItem mntmxAntialiasing_1 = new JMenuItem("4x antialiasing");
	public final JSlider sliderWastedVotesTotal = new JSlider();
	public final JLabel lblWastedVotes = new JLabel("Competitiveness (victory margin)");
	public JCheckBoxMenuItem chckbxmntmMutateDisconnected;
	public JCheckBoxMenuItem chckbxmntmMutateExcessPop;
	public JCheckBoxMenuItem chckbxmntmMutateExcessPopOnly;
	public JCheckBoxMenuItem chckbxmntmMutateCompetitive;
	public JCheckBoxMenuItem chckbxmntmMutateCompactness;
	public JCheckBoxMenuItem chckbxmntmMutateAnyAbove;
	public JMenu mnConstraints;
	public JMenuItem mntmWholeCounties;
	public JLabel lblGeometricFairness;
	public JSlider sliderBalance;
	public JMenuItem mntmConvertWktTo;
	public JSeparator separator_4;
	public JMenuItem mntmNoMap;
	public JMenuItem mntmOneMap;
	public JMenuItem mntmFourMaps;
	public JSeparator separator_5;
	public JCheckBoxMenuItem chckbxmntmUseAnnealFloor;
	public JMenuItem mntmResetAnnealFloor;
	public JSeparator separator_6;
	public JMenuItem mntmColorByDistrict;
	public JMenuItem mntmColorByPop;
	public JMenuItem mntmColorByVote;
	public JMenuItem mntmColorByCompactness;
	public JLabel lblElitism;
	public JSlider sliderElitism;
	public JCheckBox chckbxMutateElite;
	public JRadioButton rdbtnTruncationSelection;
	public JRadioButton rdbtnRankSelection;
	public JRadioButton rdbtnRouletteSelection;
	public final JMenuItem mntmColorByWasted = new JMenuItem("Color by wasted votes");
	public final JMenuItem mntmWizard = new JMenuItem("Download vtd shapefile & population from census.gov...");
	public final JSeparator separator_7 = new JSeparator();
	public final JRadioButton rdbtnTournamentSelection = new JRadioButton("Tournament selection");
	public final JSlider tournamentSlider = new JSlider();
	public JMenuItem mntmHarvardElectionData;
	public final JMenuItem mntmDescramble = new JMenuItem("descramble");
	public final JMenuItem mntmShowSeats = new JMenuItem("Show seats / votes");
	public final JMenuItem mntmShowRankedDistricts = new JMenuItem("Show ranked districts");
	public JLabel lblSeatsVotes;
	public JSlider sliderSeatsVotes;
	public JButton btnSubstituteColumns;
	public JCheckBox chckbxSubstituteColumns;
	public final JMenu mnWindows = new JMenu("Windows");
	public final JButton btnElection2Columns = new JButton("Election 2 columns");
	public final JCheckBox chckbxSecondElection = new JCheckBox("Second election");
	public JCheckBox chckbxThirdElection;
	public JButton btnElection3Columns;
	public JMenuItem mntmPublicMappingProject;
	public JPanel panel_5;
	public JLabel srlblSplitReduction;
	public JSlider sliderSplitReduction;
	public JComboBox srcomboBoxCountyColumn;
	public JLabel srlblCountyColumn;
	public JCheckBox chckbxReduceSplits;
	public final JLabel lblFairnessCriteria = new JLabel("Fairness criteria");
	public JButton btnEthnicityColumns;
	private boolean hushcomboBoxCountyColumn;
	public final JLabel lblRacialVoteDilution = new JLabel("Racial vote dilution");
	public final JSlider sliderVoteDilution = new JSlider();
	public final JTextField textFieldTotalSeats = new JTextField();
	public final JRadioButton lblTotalSeats = new JRadioButton("Total seats");
	Feature getHit(double dlon, double dlat) {
		int ilat = (int)(dlat*Geometry.SCALELATLON);
		int ilon = (int)(dlon*Geometry.SCALELATLON);
		dlon *= Geometry.SCALELATLON;
		
		double min_x = 0;
		double max_x = featureCollection.features.size()-1;
		double min_lon = featureCollection.features.get((int)min_x).geometry.full_centroid[0];
		double max_lon = featureCollection.features.get((int)max_x).geometry.full_centroid[0];
		//System.out.println("min_lon "+min_lon);
		//System.out.println("dlon "+dlon);
		//System.out.println("max_lon "+max_lon);
		
		int itestx = 0;
		for( int i = 0; i < 100; i++) {
			if( i == 99) {
				//System.out.println("i0: "+i);
			}
			if( dlon < min_lon || dlon > max_lon) {
				//System.out.println("i1: "+i);
				break;
			}
			double test_x = min_x + (dlon-min_lon)*(max_x-min_x)/(max_lon-min_lon);
			itestx = (int)(Math.round(test_x)+0.00000000000001);
			if( itestx <= min_x+0.00000000000001) {
				itestx++;
			}
			if( itestx >= max_x-0.00000000000001) {
				itestx--;
			}
			if( itestx >= featureCollection.features.size()) {
				itestx = featureCollection.features.size()-1;
			}
			if( itestx < 0) {
				itestx = 0;
			}
			double test_lon = featureCollection.features.get(itestx).geometry.full_centroid[0];
			if( test_lon > dlon) {
				max_lon = test_lon;
				max_x = itestx;
			} else if( test_lon < dlon) {
				min_lon = test_lon;
				min_x = itestx;
			} else {
				//System.out.println("i2: "+i);
				break;
			}
			if( max_x-min_x < 5) {
				//System.out.println("i5: "+i);
				break;
			}
		}
		for( int i = 0; i < featureCollection.features.size(); i++ ) {
			if(  itestx+i < featureCollection.features.size()) {
				Feature testfeat = featureCollection.features.get(itestx+i);
				for( int j = 0; j < testfeat.geometry.polygons_full.length; j++ ) {
					if( testfeat.geometry.polygons_full[j].contains(ilon,ilat)) {
						hits++;
						return testfeat;
					}
				}
			}
			if( itestx-i >= 0) {
				Feature testfeat = featureCollection.features.get(itestx-i);
				for( int j = 0; j < testfeat.geometry.polygons_full.length; j++ ) {
					if( testfeat.geometry.polygons_full[j].contains(ilon,ilat)) {
						hits++;
						return testfeat;
					}
				}
			}
			
		}
		//old way
		System.out.print("n");
		for( Feature feat : featureCollection.features) {
			Polygon[] polys = feat.geometry.polygons_full;
			for( int i = 0; i < polys.length; i++) {
				if( polys[i].contains(ilon,ilat)) {
					hits++;
					return feat;
				}
			}
		}
		System.out.print("o");
		misses++;
		return null;
	}

	class ImportAggregateCustom extends Thread {
		File f;
		ImportAggregateCustom() { super(); }
		public void init() {
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File(Download.getStartPath()));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
			jfc.showOpenDialog(null);
			f = jfc.getSelectedFile();
			if( f == null)  {
				return;
			}
			
			start();
		}
    	public void run() {
    		try {
				String fn = f.getName().trim();
				String ext = fn.substring(fn.length()-3).toLowerCase();
				DataAndHeader dh = new DataAndHeader();

	    		dlg.setVisible(true);
	    		dlbl.setText("Making polygons...");
	    		System.out.println("making polygons");
				int count = 0;
				
	    		for( Feature feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.vtd.population = 0;
	    		}

				if( ext.equals("csv") || ext.equals("txt")) {
		    		System.out.println("loading delimited");
		    		dlbl.setText("Loading file...");

					String delimiter = ",";
					if( ext.equals("csv")) {
						delimiter = ",";
					} else
					if( ext.equals("txt")) {
						delimiter = "\t";
					}
					
					//TODO: CONVERT TO IMPORT CUSTOM
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);
					dh.header = br.readLine().split(delimiter);
					
					//now select the columns
		    		System.out.println("reading columns");
					int col_lat = -1;
					int col_lon = -1;
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
							col_lat = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
							col_lon = i;
						}
					}
		    		dlg.setVisible(false);
					if( col_lat < 0 || col_lon < 0) {
						dlg.setVisible(false);
			    		System.out.println("Required columns not found.");
						JOptionPane.showMessageDialog(mainframe, "Required columns not found.\nMissing centroid columns:\n INTPTLAT\n INTPTLON");
						return;
					}
					DialogMultiColumnSelect dsc = new DialogMultiColumnSelect("Select columns to import",dh.header,new String[]{});
					dsc.show();
					if( !dsc.ok) {
						return;
					}
					
					String[] options = new String[]{"Accumulate","Majority vote"};
					int ACCUMULATE = 0;
					int OVERWRITE = 1;
					int opt = JOptionPane.showOptionDialog(mainframe, "Accumulate values or majority vote?", "Select option", 0,0,null,options,options[0]);
					if( opt < 0) {
						System.out.println("aborted.");
						return;
					}
					
		    		dlg.setVisible(true);
					Object[] col_names = dsc.in.toArray();
					int[] col_indexes = new int[dsc.in.size()]; 
					for( int i = 0; i < col_indexes.length; i++) {
						String test = ((String)col_names[i]).toUpperCase().trim();
						for( int j = 0; j < dh.header.length; j++) {
							if( dh.header[j].toUpperCase().trim().equals(test)) {
								col_indexes[i] = j;
								break;
							}
						}
					}
		    		System.out.println("doing hit tests");

		    		dlbl.setText("Doing hit tests...");
					Feature.compare_centroid = true;
		    		Collections.sort(featureCollection.features);
		    		hits = 0;
		    		misses = 0;
		    		
					 //and finally process the rows
		    		try {
    					if( opt == OVERWRITE) { //overwrite
    						//make properties in first feature
	    					for( int j = 0; j < col_indexes.length; j++) {
	    						featureCollection.features.get(0).properties.put((String)col_names[j]," ");		
	    					}
    						//reset hash in all features
	    					for( Feature feat : featureCollection.features) {
	    						feat.properties.temp_hash.clear();
	    						feat.points = new Vector<double[]>();
	    					}
    					}

					    String line;
					    while ((line = br.readLine()) != null) {
					    	try {
						    	String[] ss = line.split(delimiter);
						    	if( ss.length > dh.header.length) {
						    		System.out.print("+"+(ss.length - dh.header.length));
						    	}
						    	
					    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
					    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
					    		
				    			Feature feat = getHit(dlon,dlat);
						    	if( feat == null) {
						    		System.out.println();
						    		System.out.println("miss "+dlon+","+dlat+" ");
						    	} else {
			    					if( opt == OVERWRITE) { //overwrite
			    						String dist = ss[col_indexes[0]];
			    						feat.points.add(new double[]{dlon,dlat,Integer.parseInt(dist)});
			    						Integer integer = feat.properties.temp_hash.get(dist);
			    						if( integer == null) {
			    							integer = 0;
			    						}
			    						integer++;
			    						feat.properties.temp_hash.put(dist,integer);
			    						//System.out.println(""+feat.properties.get("GEOID10")+": "+ss[col_indexes[0]]+": "+integer);
			    						/*
				    					for( int j = 0; j < col_indexes.length; j++) {
				    						feat.properties.put((String)col_names[j],ss[col_indexes[j]].trim());				    						
				    					}*/
			    					} else if (opt == ACCUMULATE) { //accumulate
				    					for( int j = 0; j < col_indexes.length; j++) {
				    						String key = (String)col_names[j];
				    						double initial = 0;
				    						if( feat.properties.containsKey(key)) {
				    							initial = Double.parseDouble(((String)feat.properties.get(key)).replaceAll(",","").replaceAll("\\+",""));
				    						}
				    						initial += Double.parseDouble(ss[col_indexes[j]].trim().replaceAll(",","").replaceAll("\\+",""));
				    						feat.properties.put(key,""+initial);
				    					}
			    					}
						    	}
					    		
					    		count++; 
					    		if( count % 100 == 0) {
					    			System.out.print(".");
						    		dlbl.setText("Doing hit tests... "+count);
					    		}
					    		if( count % (100*100) == 0) {
					    			System.out.println(""+count);
					    		}
							} catch (Exception e) {
								// TODO Auto-generated catch ward
								e.printStackTrace();
							}
					    }
						String key = ((String)col_names[0]).trim().toUpperCase();
    					if( opt == OVERWRITE) { //overwrite
    						for( Feature feat : featureCollection.features) {
    							if( feat.properties.temp_hash.size() == 0) {
    								System.out.println("no values");
    								feat.properties.put(key,"1");
    								continue;
    							}
    							String max = "";
    							int max_count = 0;
    							for( Entry<String,Integer> entry : feat.properties.temp_hash.entrySet()) {
    								System.out.println(""+feat.properties.get("GEOID10")+": "+entry.getKey()+": "+entry.getValue());
    								if( entry.getValue() > max_count) {
    									max_count = entry.getValue();
    									max = entry.getKey();
    								}
    							}
	    						feat.properties.put(key,max);
    						}
    					}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						br.close();
						fr.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
		    		dlbl.setText("Finalizing...");
					String key = ((String)col_names[0]).trim().toUpperCase();
					System.out.println("key: "+key);
					
					if( opt == OVERWRITE) {
						for( Feature feat : featureCollection.features) {
							feat.setDistFromPoints(key);
						}
					}

		    		dlg.setVisible(false);
					
					project.district_column = (String) comboBoxDistrictColumn.getSelectedItem();
					project.population_column = (String) comboBoxPopulation.getSelectedItem();
					fillComboBoxes();
					mapPanel.invalidate();
					mapPanel.repaint();
		    		JOptionPane.showMessageDialog(mainframe,"Done importing data.\nHits: "+hits+"\nMisses: "+misses);
		    		System.out.println("done");

		    		if( opt == OVERWRITE) {
						for( Feature feat : featureCollection.features) {
							feat.setDistFromPoints(key);
						}
					}

					return;
					
				} else
				if( ext.equals("dbf")) {
					//TODO: CONVERT TO IMPORT CUSTOM
		    		System.out.println("loading dbf");
		    		dlg.setVisible(true);
		    		dlbl.setText("Loading file...");

					String s = getFile(f).toString();
					dh = readDBF(f.getAbsolutePath());
					
					String dbfname = f.getAbsolutePath();
					
					DBFReader dbfreader;
					try {
						dbfreader = new DBFReader(dbfname);
					} catch (JDBFException e1) {
						e1.printStackTrace();
						return;
					}
					
					
		    		dlbl.setText("Reading header...");

					dh.header = new String[dbfreader.getFieldCount()];
					for( int i = 0; i < dh.header.length; i++) {
						dh.header[i] = dbfreader.getField(i).getName();
					}

					//now select the columns
					int col_lat = -1;
					int col_lon = -1;
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
							col_lat = i;
						}
						if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
							col_lon = i;
						}
					}
		    		dlg.setVisible(false);
					DialogMultiColumnSelect dsc = new DialogMultiColumnSelect("Select columns to import",dh.header,new String[]{});
					if( !dsc.ok) {
						return;
					}
					
					String[] options = new String[]{"Accumulate","Overwrite"};
					int opt = JOptionPane.showOptionDialog(mainframe, "Accumulate or overwrite values?", "Select option", 0,0,null,options,options[0]);
					if( opt < 0) {
						System.out.println("aborted.");
						return;
					}
					
		    		dlg.setVisible(true);
					Object[] col_names = dsc.in.toArray();
					int[] col_indexes = new int[dsc.in.size()]; 
					for( int i = 0; i < col_indexes.length; i++) {
						String test = ((String)col_names[i]).toUpperCase().trim();
						for( int j = 0; j < dh.header.length; j++) {
							if( dh.header[j].toUpperCase().trim().equals(test)) {
								col_indexes[i] = j;
								break;
							}
						}
					}
					if( col_lat < 0 || col_lon < 0) {
						dlg.setVisible(false);
						JOptionPane.showMessageDialog(mainframe, "Required columns not found.");
						return;
					}

		    		dlbl.setText("Doing hit tests...");
					Feature.compare_centroid = true;
		    		Collections.sort(featureCollection.features);

		    		
		    		//and finally process each row
				    while (dbfreader.hasNextRecord()) {
				    	try {
				    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
				    		String[] ss = new String[oo.length];
				    		for( int i = 0; i < oo.length; i++) {
				    			ss[i] = oo[i].toString();
				    		}
				    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
				    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
	
			    			Feature feat = getHit(dlon,dlat);
					    	if( feat == null) {
					    		System.out.print("x");
					    	} else {
		    					if( opt == 1) { //overwrite
			    					for( int j = 0; j < col_indexes.length; j++) {
			    						feat.properties.put((String)col_names[j],ss[col_indexes[j]].trim());				    						
			    					}
		    					} else if (opt == 0) { //accumulate
			    					for( int j = 0; j < col_indexes.length; j++) {
			    						String key = (String)col_names[j];
			    						double initial = 0;
			    						if( feat.properties.containsKey(key)) {
			    							initial = Double.parseDouble(((String)feat.properties.get(key)).replaceAll(",","").replaceAll("\\+",""));
			    						}
			    						initial += Double.parseDouble(ss[col_indexes[j]].trim().replaceAll(",","").replaceAll("\\+",""));
			    						feat.properties.put(key,""+initial);
			    					}
		    					}
				    		}
				    		
				    		count++; 
				    		if( count % 100 == 0) {
				    			System.out.print(".");
					    		dlbl.setText("Doing hit tests... "+count);
				    		}
				    		if( count % (100*100) == 0) {
				    			System.out.println(""+count);
				    		}
						} catch (Exception e) {
							// TODO Auto-generated catch ward
							e.printStackTrace();
						}
				    }
				    
		    		dlbl.setText("Finalizing...");

		    		dlg.setVisible(false);
		    		JOptionPane.showMessageDialog(mainframe,"Done importing data.");

					return;
				} else {
		    		dlg.setVisible(false);
					JOptionPane.showMessageDialog(null, "File format not recognized.");
					return;
				}

    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
	}

	class OpenShapeFileThread extends FileThread {
		OpenShapeFileThread(File f) { super(f);  }
		Thread nextThread = null;
    	public void run() { 
    		if( f == null) { this.f = Download.vtd_file; }
    		try {
    		    dlbl.setText("Loading file "+f.getName()+"...");

    		    project.source_file = f.getAbsolutePath();

	    		dlg.setVisible(true);
	    		
				featureCollection = new FeatureCollection(); 
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}
	
				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
	
				String s = f.getName().toLowerCase();
				System.out.println("Processing "+s+"...");
				StringBuffer sb = getFile(f);
				
				stopEvolving();
				geo_loaded = false;
				evolving = false;
				Feature.display_mode = 0;
				setEnableds();
				
				//FeatureCollection fc = new FeatureCollection();
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}
				loadShapeFile(f);
	
	
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				finishLoadingGeography();
				if( nextThread != null) {
					nextThread.start();
					nextThread = null;
				}
				if( Download.istate < 0) {
					setTitle("Automatic Redistricter");
				} else {
					setTitle("Automatic Redistricter - "+Download.states[Download.istate]+" ("+Download.cyear+")");
				}
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
    }
	
	public void renumber() {
		boolean[] used = new boolean[Settings.num_districts];
		for( int i = 0; i < used.length; i++) {
			used[i] = false;
		}
		int[] renumbering = new int[Settings.num_districts];
		for( int i = 0; i < renumbering.length; i++) {
			String s = JOptionPane.showInputDialog("Enter new district number for district "+i+" (0-"+(Settings.num_districts-1)+"):");
			if( s == null) {
				return;
			}
			int newdist = Integer.parseInt(s);
			while( newdist >= used.length || newdist < 0 || used[newdist]) {
				s = JOptionPane.showInputDialog("That number has already been used or is out of range.\nEnter new district number for district "+i+" (0-"+(Settings.num_districts-1)+"):");
				if( s == null) {
					return;
				}
				newdist = Integer.parseInt(s);
			}
			used[newdist] = true;
			renumbering[i] = newdist;
 		}
		System.out.println("renumbering...");
		for( DistrictMap dm : featureCollection.ecology.population) {
			System.out.print(".");
			for( int i = 0; i < dm.vtd_districts.length; i++) {
				dm.vtd_districts[i] = renumbering[dm.vtd_districts[i]];
			}
		}
		System.out.println();
		System.out.println("done.");
		mapPanel.invalidate();
		mapPanel.repaint();
	}
	
	class OpenWKTFileThread extends Thread {
		OpenWKTFileThread() { super(); }
    	public void run() { 
        		try {
        			JFileChooser jfc = new JFileChooser();
        			jfc.setCurrentDirectory(new File(Download.getStartPath()));
    				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
    				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","tsv"));
    				jfc.showOpenDialog(null);
    				File f = jfc.getSelectedFile();
    				if( f == null)  {
    					return;
    				}
    				String fn = f.getName();
    				String ext = fn.substring(fn.length()-3).toLowerCase();
    				DataAndHeader dh = new DataAndHeader();

    				if( !ext.equals("tsv") && !ext.equals("txt")) {
    					JOptionPane.showMessageDialog(mainframe,"Unrecognized file extension");
    					return;
    				}
					String delimiter = "\t";
    					
					//TODO: CONVERT TO IMPORT CUSTOM
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);
					dh.header = br.readLine().split(delimiter);
					String initialSelectionValue = dh.header[0];
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].trim().toUpperCase().equals("MAPOBJECT")) {
							initialSelectionValue = dh.header[i];
							break;
						}
					}
					
					//now get the map column
					String opt = (String)JOptionPane.showInputDialog(mainframe, "Select the column with the map shapes in it.", "Select map object column.", 0, null, dh.header, initialSelectionValue);
					if( opt == null)
						return;
					opt = opt.trim().toUpperCase();
					int map_col = -1;
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].trim().toUpperCase().equals(opt)) {
							map_col = i;
							break;
						}
					}
    	    		dlg.setVisible(true);
    	    		dlbl.setText("Reading file...");

    		    project.source_file = f.getAbsolutePath();
    		    
				featureCollection = new FeatureCollection(); 
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}
	
				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
				
				stopEvolving();
				geo_loaded = false;
				Feature.display_mode = 0;
				setEnableds();

				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}

				//now process the rows
				String line;
			    while ((line = br.readLine()) != null) {
			    	try {
				    	String[] ss = line.split(delimiter);
				    	String mapline = ss[map_col].trim();
				    	if( mapline.length() < "MULTIPOLYGON(((".length()) {
				    		continue;
				    	}
				    	mapline = mapline.substring("MULTIPOLYGON(((".length());
				    	//mapline = mapline.substring(0,mapline.length()-1);
				    	String[] polygons = mapline.split("\\(\\(");
				    	if( polygons.length > 1) {
				    		//System.out.println("found "+polygons.length+" polys");
				    	}
				    	
						Feature feature = new Feature();
						featureCollection.features.add(feature);
						feature.properties = new Properties();
						feature.geometry = new Geometry();
						for( int i = 0; i < ss.length; i++) {
							if( i == map_col)
								continue;
							feature.properties.put(dh.header[i],ss[i].toString());
						}
						feature.properties.post_deserialize();

						feature.geometry.coordinates = new double[polygons.length][][];
						
						  //PointData[] points = aPolygon.getPointsOfPart(i);
						for( int i = 0; i < polygons.length; i++) {
					    	String poly = polygons[i].split("\\)")[0];
					    	if(polygons[i].split("\\)").length > 1) {
					    		//System.out.println("found "+polygons[i].split("\\)").length + " )'s");
					    	}
					    	while( poly.charAt(0) == '(') {
					    		poly = poly.substring(1);
					    	}
					    	String[] coords = poly.split(",");
					    	feature.geometry.coordinates[i] = new double[coords.length][2];
						  
							for( int j = 0; j < feature.geometry.coordinates[i].length; j++) {
								String[] cc = coords[j].trim().split("\\s+");//(" ");
								feature.geometry.coordinates[i][j][0] = Double.parseDouble(cc[0]);
								feature.geometry.coordinates[i][j][1] = Double.parseDouble(cc[1]);
							}
						}
							//for (int j = 0; j < coords.length-1; j+=2) {
						  /*
						  for( int k = 0; k < coords[0].length(); k++ ) {
							  System.out.println(""+((int)coords[0].charAt(k))+" "+coords[0].charAt(k));
						  }
						  */
						feature.geometry.post_deserialize();
						feature.post_deserialize();
				    	
				    	
						//loadShapeFile(f);

			    	} catch (Exception ex) {
			    		ex.printStackTrace();
			    	}
			    }

	    		dlg.setVisible(false);
	    		
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				finishLoadingGeography();
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
    }
	
	class WKTFileToCoordsThread extends Thread {
		WKTFileToCoordsThread() { super(); }
		File f;
		File fout;
		public void init() {
			JOptionPane.showMessageDialog(mainframe,"Select source file\n(first column = geoid, second column = mapobject, no header)");
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File(Download.getStartPath()));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","tsv"));
			jfc.showOpenDialog(null);
			f = jfc.getSelectedFile();
			if( f == null)  {
				return;
			}
			JOptionPane.showMessageDialog(mainframe,"Select output file");
			JFileChooser jfc2 = new JFileChooser();
			jfc2.setCurrentDirectory(new File(Download.getStartPath()));
			jfc2.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
			jfc2.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","tsv"));
			jfc2.showSaveDialog(null);
			fout = jfc2.getSelectedFile();
			if( fout == null)  {
				return;
			}
			start();
		}
    	public void run() { 
        		try {
    				String fn = f.getName();
    				String ext = fn.substring(fn.length()-3).toLowerCase();
    				DataAndHeader dh = new DataAndHeader();

    				if( !ext.equals("tsv") && !ext.equals("txt")) {
    					JOptionPane.showMessageDialog(mainframe,"Unrecognized file extension");
    					return;
    				}
					String delimiter = "\t";
    					
					//TODO: CONVERT TO IMPORT CUSTOM
					FileWriter fw = new FileWriter(fout);
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);
					BufferedWriter bw = new BufferedWriter(fw);
					int geoid_col = 0;
					int map_col = 1;
					
					/*
					 * dh.header = br.readLine().split(delimiter);
					String initialSelectionValue = dh.header[0];
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].trim().toUpperCase().equals("MAPOBJECT")) {
							initialSelectionValue = dh.header[i];
							break;
						}
					}
					
					//now get the map column
					String opt = (String)JOptionPane.showInputDialog(mainframe, "Select the column with the map shapes in it.", "Select map object column.", 0, null, dh.header, initialSelectionValue);
					if( opt == null)
						return;
					opt = opt.trim().toUpperCase();
					int map_col = -1;
					for( int i = 0; i < dh.header.length; i++) {
						if( dh.header[i].trim().toUpperCase().equals(opt)) {
							map_col = i;
							break;
						}
					}
					 */
    	    		dlg.setVisible(true);
    	    		dlbl.setText("Reading file...");


					bw.write("GEOID"+delimiter+"INTPTLON"+delimiter+"INTPTLAT"+"\n");

				//now process the rows
				String line;
			    while ((line = br.readLine()) != null) {
			    	try {
				    	String[] ss = line.split(delimiter);
				    	String mapline = ss[map_col].trim();
				    	String geoid = ss[geoid_col].trim();
				    	if( geoid.length() < "120890503011000".length()) {
				    		continue;
				    	}
				    	String original = ""+mapline;
				    	if( mapline.length() < "MULTIPOLYGON(((".length()) {
				    		continue;
				    	}
				    	mapline = mapline.substring("MULTIPOLYGON(((".length());
				    	//mapline = mapline.substring(0,mapline.length()-1);
				    	String[] polygons = mapline.split("\\(\\(");
				    	if( polygons.length > 1) {
				    		//System.out.println("found "+polygons.length+" polys");
				    	}
				    	
						Feature feature = new Feature();
						feature.geometry = new Geometry();

						feature.geometry.coordinates = new double[polygons.length][][];
						
						  //PointData[] points = aPolygon.getPointsOfPart(i);
				    	boolean broken = false;
						for( int i = 0; i < polygons.length; i++) {
					    	String poly = polygons[i].split("\\)")[0];
					    	if(polygons[i].split("\\)").length > 1) {
					    		//System.out.println("found "+polygons[i].split("\\)").length + " )'s");
					    	}
					    	while( poly.charAt(0) == '(') {
					    		poly = poly.substring(1);
					    	}
					    	String[] coords = poly.split(",");
					    	feature.geometry.coordinates[i] = new double[coords.length][2];
						  
					    	double lastx = 0;
					    	double lasty = 0;
					    	double firstx = 0;
					    	double firsty = 0;
							for( int j = 0; j < feature.geometry.coordinates[i].length; j++) {
								String[] cc = coords[j].trim().split("\\s+");//(" ");
								if( cc.length < 2 || (cc[1].length() < 4 && j == feature.geometry.coordinates[i].length-1) || cc[1].length() < 2) {
									if( j == feature.geometry.coordinates[i].length-1) {
										System.out.println("bad data "+geoid+" "+j+" "+coords[j]+"  using first "+cc.length+" "+j);
										feature.geometry.coordinates[i][j][0] = firstx;
										feature.geometry.coordinates[i][j][1] = firsty;
										broken = true;
									} else {
										System.out.println("bad data "+geoid+" "+j+" "+coords[j]+"  using last "+cc.length+" "+j);
										feature.geometry.coordinates[i][j][0] = lastx;
										feature.geometry.coordinates[i][j][1] = lasty;
										if( j == 0) {
											firstx = feature.geometry.coordinates[i][j][0]; 
											firsty = feature.geometry.coordinates[i][j][1]; 
										}
										broken = true;
									}
									continue;
									
								}
								feature.geometry.coordinates[i][j][0] = Double.parseDouble(cc[0]);
								feature.geometry.coordinates[i][j][1] = Double.parseDouble(cc[1]);
								if( j == 0) {
									firstx = feature.geometry.coordinates[i][j][0]; 
									firsty = feature.geometry.coordinates[i][j][1]; 
								}
								lastx = feature.geometry.coordinates[i][j][0]; 
								lasty = feature.geometry.coordinates[i][j][1]; 
							}
						}
						feature.geometry.makePolysFull();
						double[] centroid = feature.geometry.full_centroid;
						centroid[0] /= Geometry.SCALELATLON;
						centroid[1] /= Geometry.SCALELATLON;
						double[] avg = feature.geometry.getAvg();
						if( broken) {
							System.out.println("broken: "+geoid+" "+centroid[0]+" "+avg[0]+" "+centroid[1]+" "+avg[1]);
							//centroid = avg;
						} else {
							
						}
						
						bw.write(geoid+delimiter+centroid[0]+delimiter+centroid[1]+"\n");
				    	
				    	
						//loadShapeFile(f);

			    	} catch (Exception ex) {
			    		ex.printStackTrace();
			    	}
			    }

	    		dlg.setVisible(false);
	    		bw.flush();
	    		fw.flush();
	    		bw.close();
	    		fw.close();
	    		System.out.println("done");
	    		
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
    }
	
	class OpenProjectFileThread extends FileThread {
		boolean run = false;
		OpenProjectFileThread(File f, boolean run) { super(f); this.run = run;}
    	public void run() { 
    	    project.fromJSON(getFile(f).toString());
    	    if( run) {
				MainFrame.mainframe.featureCollection.ecology.startEvolving();
    	    }
    	}
	}


	class OpenGeoJsonFileThread extends FileThread {
		OpenGeoJsonFileThread(File f) { super(f); }
    	public void run() {
		    dlbl.setText("Loading file "+f.getName()+"...");
		    
		    project.source_file = f.getAbsolutePath();
			
			featureCollection = new FeatureCollection(); 
			if( panelStats != null) {
				panelStats.featureCollection = featureCollection;
			}

			featureCollection.features = new Vector<Feature>();
			HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();

		    dlg.setVisible(true);
	
			String s = f.getName().toLowerCase();
			System.out.println("Processing "+s+"...");
			StringBuffer sb = getFile(f);
			
			stopEvolving();
			geo_loaded = false;
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
				featureCollection.features.add(fe);
				//if( fe.properties.DISTRICT != null && !fe.properties.DISTRICT.toLowerCase().equals("null") ) {
				/*
				if( suppress_duplicates) {
					hmFeatures.put(fe.properties.DISTRICT, fe);
				} else {
					featureCollection.features.add(fe);
				}
				*/
				//}
			}
			
			
		    dlbl.setText("Initializing wards...");

			for( Feature fe : hmFeatures.values()) {
				featureCollection.features.add(fe);
			}
			finishLoadingGeography();
    	}
	}



	//==========FUNCTIONS
	public void addEcologyListeners() {
		if( !featureCollection.ecology.evolveListeners.contains(mapPanel)) {
			featureCollection.ecology.evolveListeners.add(mapPanel);
		}
		if( !featureCollection.ecology.evolveListeners.contains(panelStats)) {
			featureCollection.ecology.evolveListeners.add(panelStats);
		}
		if( !featureCollection.ecology.evolveListeners.contains(panelGraph)) {
			featureCollection.ecology.evolveListeners.add(panelGraph);
		}
		if( !featureCollection.ecology.evolveListeners.contains(this)) {
			featureCollection.ecology.evolveListeners.add(this);
		}
	}
	public void fillComboBoxes() {
		String[] map_headers = featureCollection.getHeaders();
		//String[][] map_data = featureCollection.getData(map_headers);
		System.out.println("population..");
		
		try {
			hushcomboBoxPopulation = true;
			comboBoxPopulation.removeAllItems();
			comboBoxPopulation.addItem("");
			for( int i = 0; i < map_headers.length; i++) {
				comboBoxPopulation.addItem(map_headers[i]);
			}
			hushcomboBoxPopulation = false;
			if( project.population_column != null && project.population_column.length() > 0) {
				comboBoxPopulation.setSelectedItem(project.population_column);
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		System.out.println("pkey..");
		try {
			hushcomboBoxCountyColumn = true;
			srcomboBoxCountyColumn.removeAllItems();
			srcomboBoxCountyColumn.addItem("");
			for( int i = 0; i < map_headers.length; i++) {
				srcomboBoxCountyColumn.addItem(map_headers[i]);
			}
			hushcomboBoxCountyColumn = false;
			if( project.county_column != null && project.county_column.length() > 0) {
				srcomboBoxCountyColumn.setSelectedItem(project.county_column);
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		try {
			hushcomboBoxMuniColumn = true;
			srcomboBoxMuniColumn.removeAllItems();
			srcomboBoxMuniColumn.addItem("");
			for( int i = 0; i < map_headers.length; i++) {
				srcomboBoxMuniColumn.addItem(map_headers[i]);
			}
			hushcomboBoxMuniColumn = false;
			if( project.muni_column != null && project.muni_column.length() > 0) {
				srcomboBoxMuniColumn.setSelectedItem(project.muni_column);
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		try {
			/*
			hushcomboBoxPrimaryKey = true;
			comboBoxPrimaryKey.removeAllItems();
			comboBoxPrimaryKey.addItem("");
			for( int i = 0; i < map_headers.length; i++) {
				comboBoxPrimaryKey.addItem(map_headers[i]);
			}
			hushcomboBoxPrimaryKey = false;
			if( project.primary_key_column != null && project.primary_key_column.length() > 0) {
				comboBoxPrimaryKey.setSelectedItem(project.primary_key_column);
			}
			*/
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		try {
			hushcomboBoxDistrict = true;
			comboBoxDistrictColumn.removeAllItems();
			comboBoxDistrictColumn.addItem("");
			
			comboBoxDistrictColumn.addItem("AR_RESULT");
			for( int i = 0; i < map_headers.length; i++) {
				if( !map_headers[i].equals("AR_RESULT")) {
					comboBoxDistrictColumn.addItem(map_headers[i]);
				}
			}
			hushcomboBoxDistrict = false;
			if( project.district_column != null && project.district_column.length() > 0) {
				comboBoxDistrictColumn.setSelectedItem(project.district_column);
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		/*
		try {
			String selected = (String)comboBoxCounty.getSelectedItem();
			hushcomboBoxCounty = true;
			comboBoxCounty.removeAllItems();
			comboBoxCounty.addItem("");
			
			for( int i = 0; i < map_headers.length; i++) {
				comboBoxCounty.addItem(map_headers[i]);
			}
			comboBoxCounty.setSelectedItem(selected);
			hushcomboBoxCounty = false;
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		*/

		//comboBoxPopulation.setSelectedIndex(0);
		
	}
	public void getMinMaxXY() {
		Vector<Feature> features = featureCollection.features;

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
		featureCollection.recalcDlonlat();
	}
	public void finishLoadingGeography() {
		Vector<Feature> features = featureCollection.features;
		System.out.println(features.size()+" precincts loaded.");
	    getMinMaxXY();
		System.out.println("Initializing wards...");
		featureCollection.initwards();
	    dlbl.setText("Setting min and max coordinates...");
	    
	    getMinMaxXY();
	
		resetZoom();
		
	    dlbl.setText("Initializing ecology...");
	    try {
	    	featureCollection.initEcology();
	    	addEcologyListeners();
	    } catch (Exception ex) {
	    	System.out.println("init ecology ex: "+ex);
	    	ex.printStackTrace();
	    }
		System.out.println("filling combo boxes...");
		fillComboBoxes();
		mapPanel.invalidate();
		mapPanel.repaint();
		
		
		setDistrictColumn(project.district_column);
		//featureCollection.loadDistrictsFromProperties(project.district_column);
		
		mapPanel.featureCollection = featureCollection;
		mapPanel.invalidate();
		mapPanel.repaint();
		//featureCollection.ecology.mapPanel = mapPanel;
		//featureCollection.ecology.statsPanel = panelStats;
    	addEcologyListeners();

		
		dlg.setVisible(false);
		System.out.println("Ready.");

		
		geo_loaded = true;
		setEnableds();
	}
	
	public void openShapeFile(File f,boolean synchronous) {
		Thread t = new OpenShapeFileThread(f);
		t.start();
		try {
			if( synchronous)
				t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch ward
			e.printStackTrace();
		}
	}
	public void openGeoJson(File f,boolean synchronous) {
		Thread t = new OpenGeoJsonFileThread(f);
		t.start();
		try {
			if( synchronous)
				t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch ward
			e.printStackTrace();
		}
	}
	
	public void openProjectFile(File f0, boolean run) {
		project = new Project();
	    openedProjectFilePath = f0.getAbsolutePath();
	    new OpenProjectFileThread(f0,run).start();
	}
	
	public void setEnableds() {
		
		if( !geo_loaded) {
			census_loaded = false;
			election_loaded = false;
		}
		chckbxmntmOpenCensusResults.setEnabled(geo_loaded);
		mntmOpenElectionResults.setEnabled(geo_loaded);
		mntmMergeData.setEnabled(geo_loaded);
		mntmImportcsv.setEnabled(geo_loaded);
		mntmExportcsv.setEnabled(geo_loaded);
		mntmSaveData.setEnabled(geo_loaded);
		mntmRenumber.setEnabled(geo_loaded);
		mntmSaveProjectFile.setEnabled(geo_loaded);

		
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
	
	public void setPrimaryKeyColumn(String pkey) {
		project.primary_key_column = pkey;
	}

	boolean hushSetDistrict = false;
	protected boolean hushcomboBoxMuniColumn;
	public void setDistrictColumn(String district) {
		System.out.println("setDistrictColumn hush?"+hushSetDistrict);
		if( hushSetDistrict) {
			return;
		}
		boolean changed = !district.equals(project.district_column);
		project.district_column = district;
		if( changed) {
			Feature.compare_centroid = false;
			Collections.sort(featureCollection.features);
			
			if( featureCollection.features == null || featureCollection.features.size() == 0) {
				
			} else {
				boolean is_in = featureCollection.features.get(0).properties.containsKey(district);
				
				int min = 999999999;
				int max = -1;
				for( Feature feat : featureCollection.features) {
					int d = 0;
					try {
						d = Integer.parseInt((String)feat.properties.get(district) );
					} catch (Exception ex) {
						d = 0;
						feat.properties.put(district,"0");
						System.out.println("missing district ");
						try {
							System.out.println((String)feat.properties.get("GEOID10"));
						} catch (Exception ex2) {
							
						}
						//ex.printStackTrace();
						continue;
					}
					if( d < min) { min = d; }
					if( d > max) { max = d; }
				}
				if( min > max || max-min > 200) {
					return;
				}
				System.out.println("min: "+min+" max: "+max);
				Settings.num_districts = (max-min)+1;
				textFieldNumDistricts.setText(""+Settings.num_districts);
				
				if( is_in) {
					featureCollection.loadDistrictsFromProperties(district);
				}
			}
			if( featureCollection.ecology != null && featureCollection.ecology.population != null && featureCollection.ecology.population.size() > 0) {
				Settings.ignore_uncontested = false;
				featureCollection.ecology.population.get(0).calcFairnessScores();
				panelStats.getStats();
				featureCollection.findUncontested();
				panelStats.getStats();
				if( featureCollection.vuncontested1.size() > 0 || featureCollection.vuncontested2.size() > 0 && !hushSetDistrict) {
					System.out.println("uncontested found!");
					if(project.substitute_columns.size() > 0 && Settings.substitute_uncontested) {
						hushSetDistrict = true;
						try {
							System.out.println("setting substitutes");
							setSubstituteColumns();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						Settings.ignore_uncontested = false;
						panelStats.getStats();
						hushSetDistrict = true;
						project.district_column = "";
						setDistrictColumn(district);
						hushSetDistrict = false;
					} else {
						int opt = JOptionPane.showConfirmDialog(this, "Uncontested elections detected.  Lock and ignore uncontested districts?", "Uncontested elections detected!", JOptionPane.YES_NO_OPTION);
						if( opt == JOptionPane.YES_OPTION) {
							Settings.ignore_uncontested = true;
							for( Integer d : FeatureCollection.vuncontested1) {
								//District.uncontested[d-1] = true;
								String key = district+","+d;
								if( !manageLocks.locks.contains(key)) {
									manageLocks.locks.add(key);
								}
							}
							panelStats.getStats();
							manageLocks.list.setListData(manageLocks.locks);
							manageLocks.resetLocks();
							manageLocks.show();
						} else {
							Settings.ignore_uncontested = false;
						}
					}
				} else {
					Settings.ignore_uncontested = false;
				}
			}
			hushSetDistrict = false;
			mapPanel.invalidate();
			mapPanel.repaint();
		}
	}

	public void setPopulationColumn(String pop_col) {
		project.population_column = pop_col;
		for( Feature f : featureCollection.features) {
			String pop = f.properties.get(pop_col).toString();
			if( f.vtd != null) {
				f.vtd.has_census_results = true;
				f.vtd.population = Double.parseDouble(pop.replaceAll(",",""));
			}
			f.properties.POPULATION = (int) Double.parseDouble(pop.replaceAll(",",""));
		}
	}
	public void setDemographicColumns() {
		int num_candidates = project.demographic_columns.size();
		
		for( Feature f : featureCollection.features) {
			VTD v = f.vtd;
			v.demographics = new double[num_candidates];
			for( int i = 0; i < project.demographic_columns.size(); i++) {
				try {
					v.demographics[i] = Double.parseDouble(f.properties.get(project.demographic_columns.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					v.demographics[i] = 0;
					f.properties.put(project.demographic_columns.get(i),"0");
				}
			}
		}
	}
	public void setCountyColumn() {
		
		for( Feature f : featureCollection.features) {
			VTD v = f.vtd;
			try {
				v.county = f.properties.get(project.county_column).toString();
			} catch (Exception ex) {
			}
		}
	}
	
	public void setMuniColumn() {
		
		for( Feature f : featureCollection.features) {
			VTD v = f.vtd;
			try {
				v.muni = f.properties.get(project.muni_column).toString();
			} catch (Exception ex) {
			}
		}
	}
	
	public void setElectionColumns() {
		int num_candidates = project.election_columns.size();
		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		
		for( Feature f : featureCollection.features) {
			VTD b = f.vtd;
			b.resetOutcomes();
			double[] dd = new double[num_candidates];
			for( int i = 0; i < project.election_columns.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns.get(i),"0");
				}
			}
		
			if( b.elections.size() < 1) {
				b.elections.add(new Vector<Election>());
			} else {
				b.elections.get(0).clear();
			}
			for( int j = 0; j < num_candidates; j++) {
				Election d = new Election();
				//d.ward_id = b.id;
				d.turnout_probability = 1;
				d.population = (int) dd[j];
				d.vote_prob = new double[num_candidates];
				for( int i = 0; i < d.vote_prob.length; i++) {
					d.vote_prob[i] = 0;
				}
				d.vote_prob[j] = 1;
				if( b.elections.size() < 1) {
					b.elections.add(new Vector<Election>());
				}

				b.elections.get(0).add(d);
				System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
			}
		}
		
		Settings.num_candidates = num_candidates;
		featureCollection.ecology.reset();
		election_loaded = true;
		
		setEnableds();	
	}
	
	public void setElectionColumns2() {
		if( chckbxSubstituteColumns.isSelected()) {
			chckbxSubstituteColumns.doClick();
		}
		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		
		for( Feature f : featureCollection.features) {
			VTD b = f.vtd;
			b.resetOutcomes();
			double[] dd = new double[Settings.num_candidates];
			for( int i = 0; i < project.election_columns_2.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns_2.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns_2.get(i),"0");
				}
			}
		
			while( b.elections.size() < 2) {
				b.elections.add(new Vector<Election>());
			}
			b.elections.get(1).clear();
			
			for( int j = 0; j < Settings.num_candidates; j++) {
				Election d = new Election();
				//d.ward_id = b.id;
				d.turnout_probability = 1;
				d.population = (int) dd[j];
				d.vote_prob = new double[Settings.num_candidates];
				for( int i = 0; i < d.vote_prob.length; i++) {
					d.vote_prob[i] = 0;
				}
				d.vote_prob[j] = 1;
				while( b.elections.size() < 2) {
					b.elections.add(new Vector<Election>());
				}

				b.elections.get(1).add(d);
				System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
			}
		}
		
		featureCollection.ecology.reset();
		election_loaded = true;
		
		setEnableds();	
	}

	public void setElectionColumns3() {
		if( chckbxSubstituteColumns.isSelected()) {
			chckbxSubstituteColumns.doClick();
		}
		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		
		for( Feature f : featureCollection.features) {
			VTD b = f.vtd;
			b.resetOutcomes();
			double[] dd = new double[Settings.num_candidates];
			for( int i = 0; i < project.election_columns_3.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns_3.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns_3.get(i),"0");
				}
			}
		
			while( b.elections.size() < 3) {
				b.elections.add(new Vector<Election>());
			}
			b.elections.get(2).clear();
			
			for( int j = 0; j < Settings.num_candidates; j++) {
				Election d = new Election();
				//d.ward_id = b.id;
				d.turnout_probability = 1;
				d.population = (int) dd[j];
				d.vote_prob = new double[Settings.num_candidates];
				for( int i = 0; i < d.vote_prob.length; i++) {
					d.vote_prob[i] = 0;
				}
				d.vote_prob[j] = 1;
				while( b.elections.size() < 3) {
					b.elections.add(new Vector<Election>());
				}

				b.elections.get(2).add(d);
				System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
			}
		}
		
		featureCollection.ecology.reset();
		election_loaded = true;
		
		setEnableds();	
	}

	
	public void setSubstituteColumns() {
		int max = chckbxThirdElection.isSelected() ? 3 : chckbxSecondElection.isSelected() ? 2 : 1;
		boolean[][] buncontested = new boolean[][]{FeatureCollection.buncontested1, FeatureCollection.buncontested2, FeatureCollection.buncontested3};
		Vector<String>[] dems = (Vector<String>[])new Vector[]{project.election_columns, project.election_columns_2, project.election_columns_3};
		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		for( int idem = 0; idem < max; idem++) {
			try {
				// TODO Auto-generated method stub
				DistrictMap dm = featureCollection.ecology.population.get(0);
				
				double[] total_origs = new double[Settings.num_candidates];
				double[] total_substs = new double[Settings.num_candidates];
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_origs[i] = 1;
				}
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_substs[i] = 1;
				}
				
				for( int id = 0; id < featureCollection.features.size(); id++) {
					Feature f = featureCollection.features.get(id);
					if( buncontested[idem][dm.vtd_districts[id]]) {
						continue;
					}
					for( int i = 0; i < project.substitute_columns.size(); i++) {
						try {
							total_origs[i] += Double.parseDouble(f.properties.get(dems[idem].get(i)).toString().replaceAll(",",""));
						} catch (Exception ex) {
						}
						try {
							total_substs[i] += Double.parseDouble(f.properties.get(project.substitute_columns.get(i)).toString().replaceAll(",",""));
						} catch (Exception ex) {
						}
					}
				}	
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_origs[i] /= total_substs[i];
				}
				
				for( int id = 0; id < featureCollection.features.size(); id++) {
					Feature f = featureCollection.features.get(id);
					VTD b = f.vtd;
					if( !buncontested[idem][dm.vtd_districts[id]]) {
						continue;
					}
					
					b.resetOutcomes();
					double[] dd = new double[Settings.num_candidates];
					for( int i = 0; i < project.substitute_columns.size(); i++) {
						try {
							dd[i] = total_origs[i]*Double.parseDouble(f.properties.get(project.substitute_columns.get(i)).toString().replaceAll(",",""));
						} catch (Exception ex) {
							
							dd[i] = 0;
							f.properties.put(project.substitute_columns.get(i),"0");
						}
					}
				
					while( b.elections.size() <= idem) {
						b.elections.add(new Vector<Election>());
					}
					b.elections.get(idem).clear();
					
					for( int j = 0; j < Settings.num_candidates; j++) {
						Election d = new Election();
						//added 2015.11.18 - write back so substitutes are part of data export.
						f.properties.put(project.election_columns.get(j),""+((int)dd[j]));
						
						//d.ward_id = b.id;
						d.turnout_probability = 1;
						d.population = (int) dd[j];
						d.vote_prob = new double[Settings.num_candidates];
						for( int i = 0; i < d.vote_prob.length; i++) {
							d.vote_prob[i] = 0;
						}
						d.vote_prob[j] = 1;
						if( b.elections.size() < 1) {
							b.elections.add(new Vector<Election>());
						}
						b.elections.get(idem).add(d);
						System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		featureCollection.ecology.reset();
		election_loaded = true;
		
		setEnableds();	
		
	}
	
	public void selectLayers() {
		boolean is_evolving = this.evolving;
		if( is_evolving) { featureCollection.ecology.stopEvolving(); }
		setDistrictColumn(project.district_column);
		//featureCollection.loadDistrictsFromProperties(project.district_column);
		DialogSelectLayers dlg = new DialogSelectLayers();
		dlg.setData(featureCollection,project.election_columns);
		dlg.show();
		if( !dlg.ok) {
			if( is_evolving) { featureCollection.ecology.startEvolving(); }
			return;
		}

		try {
			project.election_columns = dlg.in;
			setElectionColumns();
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		//mntmShowDemographics.setSelected(true);
		//Feature.display_mode = 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		
		if( is_evolving) { featureCollection.ecology.startEvolving(); }
	}
	public void selectLayers2() {
		boolean is_evolving = this.evolving;
		if( is_evolving) { featureCollection.ecology.stopEvolving(); }
		setDistrictColumn(project.district_column);
		//featureCollection.loadDistrictsFromProperties(project.district_column);
		DialogSelectLayers dlg = new DialogSelectLayers();
		dlg.setData(featureCollection,project.election_columns_2);
		dlg.show();
		if( !dlg.ok) {
			if( is_evolving) { featureCollection.ecology.startEvolving(); }
			return;
		}
		

		try {
			project.election_columns_2 = dlg.in;
			if( project.election_columns_2.size() != project.election_columns.size()) {
				JOptionPane.showMessageDialog(mainframe, "Election columns and second election columns must match one-to-one.");
				chckbxSecondElection.doClick();//.setSelected(false);
				//remove from demographics
			} else {
				//set as demographics
				setElectionColumns2();
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		//mntmShowDemographics.setSelected(true);
		//Feature.display_mode = 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		
		if( is_evolving) { featureCollection.ecology.startEvolving(); }
	}
	public void setMaxElections() {
		int imax = chckbxThirdElection.isSelected() ? 3 : chckbxSecondElection.isSelected() ? 2 : 1;
		for( Feature f : featureCollection.features) {
			VTD b = f.vtd;
			while( b.elections.size() > imax) {
				b.elections.remove(imax-1);//.add(new Vector<Demographic>());
			}
			b.resetOutcomes();
		}
	}
	
	public void selectLayers3() {
		boolean is_evolving = this.evolving;
		if( is_evolving) { featureCollection.ecology.stopEvolving(); }
		setDistrictColumn(project.district_column);
		//featureCollection.loadDistrictsFromProperties(project.district_column);
		DialogSelectLayers dlg = new DialogSelectLayers();
		dlg.setData(featureCollection,project.election_columns_3);
		dlg.show();
		if( !dlg.ok) {
			if( is_evolving) { featureCollection.ecology.startEvolving(); }
			return;
		}
		

		try {
			project.election_columns_3 = dlg.in;
			if( project.election_columns_3.size() != project.election_columns.size()) {
				JOptionPane.showMessageDialog(mainframe, "Election columns and second election columns must match one-to-one.");
				chckbxThirdElection.doClick();//.setSelected(false);
				//remove from demographics
			} else {
				//set as demographics
				setElectionColumns3();
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		//mntmShowDemographics.setSelected(true);
		//Feature.display_mode = 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		
		if( is_evolving) { featureCollection.ecology.startEvolving(); }
	}
	
	public DataAndHeader readDelimited(String s, String delimiter, boolean has_headers) {
		DataAndHeader dh = new DataAndHeader();
		try {
			String[] lines = s.split("\n");
			dh.header = lines[0].split(delimiter);
			if( !has_headers) {
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = "col_"+i;
				}
			}
			dh.data = new String[lines.length - (has_headers ? 1 : 0)][];
			for( int i = has_headers ? 1 : 0; i < lines.length; i++) {
				dh.data[i-(has_headers ? 1 : 0)] = lines[i].split(delimiter);
			}
			return dh;
		} catch (Exception ex) {
			
		}
		return dh;
	}
	public void writeDBF(String filename, String[] headers, String[][] data) {
        JDBField[] fields = new JDBField[headers.length];
        
		for( int i = 0; i < headers.length; i++) {
			try {
				fields[i] = new JDBField(headers[i].length() > 10 ? headers[i].substring(0,10) : headers[i], 'C', 64, 0);
			} catch (JDBFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DBFWriter dbfwriter;
		try {
			dbfwriter = new DBFWriter(filename, fields);
		} catch (JDBFException e1) {
			e1.printStackTrace();
			return;
		}
		for( int i = 0; i < data.length; i++) {
			for( int j = 0; j < data[i].length; j++) {
				if( data[i][j].length() > 64) {
					data[i][j] = data[i][j].substring(0,64);
				}
			}
			try {
				dbfwriter.addRecord(data[i]);
			} catch (JDBFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			dbfwriter.close();
		} catch (JDBFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataAndHeader readDBF(String dbfname) {
		DBFReader dbfreader;
		try {
			dbfreader = new DBFReader(dbfname);
		} catch (JDBFException e1) {
			e1.printStackTrace();
			return null;
		}
		DataAndHeader dh = new DataAndHeader();
		
		dh.header = new String[dbfreader.getFieldCount()];
		for( int i = 0; i < dh.header.length; i++) {
			dh.header[i] = dbfreader.getField(i).getName();
		}
		Vector<String[]> vd = new Vector<String[]>();

	    while (dbfreader.hasNextRecord()) {
	    	try {
	    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
	    		String[] ss = new String[oo.length];
	    		for( int i = 0; i < oo.length; i++) {
	    			ss[i] = oo[i].toString();
	    		}
				vd.add(ss);
			} catch (Exception e) {
				// TODO Auto-generated catch ward
				e.printStackTrace();
			}
	    }
	    dh.data = new String[vd.size()][];
	    for( int i = 0; i < dh.data.length; i++) {
	    	dh.data[i] = vd.get(i);
	    }
	    return dh;
	}
	public void loadShapeFile(File f) {

	    try {
			FileInputStream is = new FileInputStream(f);
			ValidationPreferences prefs = new ValidationPreferences();
		    prefs.setMaxNumberOfPointsPerShape(32650*4);
		    prefs.setAllowUnlimitedNumberOfPointsPerShape(true);
		    //prefs.setMaxNumberOfPointsPerShape(16650);
		    ShapeFileReader r = new ShapeFileReader(is, prefs);
		    
			String dbfname = f.getAbsolutePath();//.getName();
			dbfname = dbfname.substring(0,dbfname.length()-4)+".dbf";
			
			DBFReader dbfreader = new DBFReader(dbfname);
			String[] cols = new String[dbfreader.getFieldCount()];
			for( int i=0; i<cols.length; i++) {
				cols[i] = dbfreader.getField(i).getName();
				System.out.print(cols[i]+"  ");
			}
			System.out.print("\n");
		    
			

		    ShapeFileHeader h = r.getHeader();
		    System.out.println("The shape type of this files is " + h.getShapeType());

		    int total = 0;
		    AbstractShape s;
		    while ((s = r.next()) != null) {
		    	Object aobj[] = dbfreader.nextRecord(Charset.defaultCharset());
		      switch (s.getShapeType()) {
		      case POLYGON_Z:
			      {
			    	  int rec_num = s.getHeader().getRecordNumber();
			    	  //System.out.println("record number: "+rec_num);
			          PolygonZShape aPolygon = (PolygonZShape) s;
			          
			          Feature feature = new Feature();
			          featureCollection.features.add(feature);
			          feature.properties = new Properties();
			          feature.geometry = new Geometry();
			          feature.properties.esri_rec_num = rec_num;
			          feature.properties.from_shape_file = true;
			          for( int i = 0; i < cols.length; i++) {
			        	  feature.properties.put(cols[i],aobj[i].toString());
			        	  //System.out.print(aobj[i].toString()+" ");
			          }
			          //System.out.println();
			          feature.properties.post_deserialize();
			          feature.geometry.coordinates = new double[aPolygon.getNumberOfParts()][][];
			          
			          for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
			            PointData[] points = aPolygon.getPointsOfPart(i);
			            feature.geometry.coordinates[i] = new double[points.length][2];
			            for( int j = 0; j < points.length; j++) {
			            	feature.geometry.coordinates[i][j][0] = points[j].getX();
			            	feature.geometry.coordinates[i][j][1] = points[j].getY();
			            }
			          }
			          feature.geometry.post_deserialize();
			          feature.post_deserialize();
			      }
		          break;
		      case POLYGON:
			      {
			    	  int rec_num = s.getHeader().getRecordNumber();
			    	  //System.out.println("record number: "+rec_num);
			          PolygonShape aPolygon = (PolygonShape) s;
			          
			          Feature feature = new Feature();
			          featureCollection.features.add(feature);
			          feature.properties = new Properties();
			          feature.geometry = new Geometry();
			          feature.properties.esri_rec_num = rec_num;
			          feature.properties.from_shape_file = true;
			          for( int i = 0; i < cols.length; i++) {
			        	  feature.properties.put(cols[i],aobj[i].toString());
			        	  //System.out.print(aobj[i].toString()+" ");
			          }
			          //System.out.println();
			          feature.properties.post_deserialize();
			          feature.geometry.coordinates = new double[aPolygon.getNumberOfParts()][][];
			          
			          for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
			            PointData[] points = aPolygon.getPointsOfPart(i);
			            feature.geometry.coordinates[i] = new double[points.length][2];
			            for( int j = 0; j < points.length; j++) {
			            	feature.geometry.coordinates[i][j][0] = points[j].getX();
			            	feature.geometry.coordinates[i][j][1] = points[j].getY();
			            }
			          }
			          feature.geometry.post_deserialize();
			          feature.post_deserialize();
			      }
		          break;
		      default:
		        System.out.println("Read other type of shape.");
		      }
		      total++;
		    }
			for( int i=0; i<cols.length; i++) {
				cols[i] = dbfreader.getField(i).getName();
				System.out.print(cols[i]+"  ");
			}
			System.out.print("\n");


		    System.out.println("Total shapes read: " + total);

		    is.close();		
		} catch (Exception ex) {
			System.out.println("exception in processing shapefile: "+ex);
			ex.printStackTrace();
			
		}
	}

	public void loadElection(String s) {
		try {
			String[] lines = s.split("\n");
			int num_candidates = lines[0].split("\t").length - 1;
			
			Vector<String> not_found_in_geo = new Vector<String>();
			for( VTD b : featureCollection.vtds) {
				b.has_election_results = false;
			}
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				String district = ss[0].trim();
				VTD b = featureCollection.wardHash.get(district);
				if( b == null) {
					not_found_in_geo.add(district);
					System.out.println("not in geo: "+district);

				} else {
					b.has_election_results = true;
				}
			}
			Vector<String> not_found_in_census = new Vector<String>();
			for( VTD b : featureCollection.vtds) {
				if( b.has_election_results == false) {
					//not_found_in_census.add(b.name);
					//System.out.println("not in election: |"+b.name+"|");

				}
			}
			if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
				for( VTD b : featureCollection.vtds) {
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
				VTD b = featureCollection.wardHash.get(es.getKey());
				double[] dd = es.getValue();
				for( int j = 0; j < num_candidates; j++) {
					Election d = new Election();
					//d.ward_id = b.id;
					d.turnout_probability = 1;
					d.population = (int) dd[j];
					d.vote_prob = new double[num_candidates];
					for( int i = 0; i < d.vote_prob.length; i++) {
						d.vote_prob[i] = 0;
					}
					d.vote_prob[j] = 1;
					if( b.elections.size() < 1) {
						b.elections.add(new Vector<Election>());
					}
					b.elections.get(0).add(d);
					System.out.println("ward "+b.id+" added demo "+d.population+" "+j);
				}
			}
			Settings.num_candidates = num_candidates;
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
		System.out.println("Getting min/max...");
	    getMinMaxXY();
		System.out.println("Initializing wards...");
		featureCollection.initwards();
		Settings.resetAnnealing();
		featureCollection.ecology.generation = 0;
/*
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
		*/
		resetZoom();
		mapPanel.featureCollection = featureCollection;
		mapPanel.invalidate();
		mapPanel.repaint();
		//featureCollection.ecology.mapPanel = mapPanel;
		//featureCollection.ecology.statsPanel = panelStats;
		featureCollection.initEcology();
    	addEcologyListeners();

		System.out.println("Ready.");
		
		geo_loaded = true;
		setEnableds();
	}
	
	public MainFrame() {
		mainframe = this;
		jbInit();
		sliderRepresentation.setValue(50);
		sliderSeatsVotes.setValue(50);
		lblFairnessCriteria.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFairnessCriteria.setBounds(9, 11, 179, 16);
		
		panel_4.add(lblFairnessCriteria);
		lblRacialVoteDilution.setToolTipText("<html><img src=\"file:/C:/Users/kbaas.000/git/autoredistrict/bin/resources/voting_power.png\">");
		lblRacialVoteDilution.setBounds(9, 225, 172, 16);
		
		panel_4.add(lblRacialVoteDilution);
		sliderVoteDilution.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.vote_dilution_weight = sliderVoteDilution.getValue()/100.0;
			}
		});
		sliderVoteDilution.setValue(50);
		sliderVoteDilution.setBounds(9, 246, 180, 29);
		
		panel_4.add(sliderVoteDilution);
		
		Settings.mutation_rate = 0; 
		Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_mutation.getValue()/100.0;
		Settings.geo_or_fair_balance_weight = sliderBalance.getValue()/100.0;
		Settings.wasted_votes_total_weight = sliderWastedVotesTotal.getValue()/100.0;
		Settings.seats_votes_asymmetry_weight = sliderSeatsVotes.getValue()/100.0;
		//Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;
		Settings.diagonalization_weight = sliderRepresentation.getValue()/100.0;
		Settings.population_balance_weight = sliderPopulationBalance.getValue()/100.0;
		Settings.geometry_weight = sliderBorderLength.getValue()/100.0;
		Settings.disconnected_population_weight = sliderDisconnected.getValue()/100.0;
		Settings.split_reduction_weight = sliderSplitReduction.getValue()/100.0;
		Settings.vote_dilution_weight = sliderVoteDilution.getValue()/100.0;
		Settings.mutate_all = true;
		
		Settings.mutation_rateChangeListeners.add(this);
		Settings.populationChangeListeners.add(this);

		setEnableds();
		
		if( Applet.open_project != null && Applet.open_project.length() > 0) {
			File fd = new File(Applet.open_project);
			if( fd == null || !fd.exists()) {
				System.out.println("project file not found: "+Applet.open_project);
				return;
			}
			openProjectFile(fd,true);
			
		}
	}
	public void jbInit() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Automatic Redistricter");
		Dimension d = new Dimension(800,1024);
		//this.setPreferredSize(d);
		this.setSize(new Dimension(1021, 779));
		
		dlg.setModal(false);
	    dpb.setIndeterminate(true);
	    dlg.getContentPane().add(BorderLayout.CENTER, dpb);
	    dlg.getContentPane().add(BorderLayout.NORTH, dlbl);
	    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	    dlg.setSize(300, 75);
	    dlg.setLocationRelativeTo(mainframe);
	    dlbl.setText("Loading file...");

		//this.getContentPane().setPreferredSize(d);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		menuBar.add(mnFile);
		menuBar.add(mnImportExport);
		
		JMenuItem mntmOpenProjectFile = new JMenuItem("Open project file");
		mntmOpenProjectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".json files", "json");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				openProjectFile(fd,false);
			}
		});
		//mnFile.add(mntmOpenProjectFile);
		
		mntmSaveProjectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".json files", "json");
				jfc.setFileFilter(filter);
				jfc.showSaveDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				String s = project.toJSON();
				try {
					FileOutputStream fos = new FileOutputStream(fd);
					fos.write(s.getBytes());
					fos.flush();
					fos.close();
					JOptionPane.showMessageDialog(mainframe,"Project file saved.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		});
		
		mntmOpenProjectFile_1 = new JMenuItem("Open project file & run");
		mntmOpenProjectFile_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".json files", "json");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				openProjectFile(fd,true);
			}
		});
		//mnFile.add(mntmOpenProjectFile_1);
		//mnFile.add(mntmSaveProjectFile);
		
		JSeparator separator = new JSeparator();
		mntmWizard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenShapeFileThread ost = new OpenShapeFileThread(Download.vtd_file);
				if( Download.census_merge_working) {
					if( Download.census_merge_old) {
						ost.nextThread = new ImportCensus2Thread(); 
					} else {
						ost.nextThread = new ImportGazzeterThread(); 
						
					}
				}
				Download.nextThread = ost;
				DialogDownload dd = new DialogDownload();
				dd.show();
				//!Download.downloadData(dlg, dlbl))
			}
		});
		
		mnFile.add(mntmWizard);
		
		mntmHarvardElectionData = new JMenuItem("Harvard Election Data Archive...");
		mntmHarvardElectionData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	    		// open the default web browser for the HTML page
				Applet.browseTo("http://projects.iq.harvard.edu/eda/data");
			}
		});
		mnFile.add(mntmHarvardElectionData);
		
		mntmPublicMappingProject = new JMenuItem("Public Mapping Project data...");
		mntmPublicMappingProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Applet.browseTo("http://www.publicmapping.org/resources/data");
			}
		});
		mnFile.add(mntmPublicMappingProject);
		
		mnFile.add(separator_7);
		//mnFile.add(separator);
		
		mnFile.add(mntmOpenGeojson);
		mntmOpenEsriShapefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ESRI shapefiles", "shp");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				Download.istate = -1;
				new OpenShapeFileThread(fd).start();
			}				
		});
		
		mnFile.add(mntmOpenEsriShapefile);
		
		mntmImportCensusData = new JMenuItem("Import Census Data");
		mntmImportCensusData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new LoadCensusFileThread().init();
			}
		});
		mntmOpenWktTabdelimited.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new OpenWKTFileThread().start();
			}
		});
		
		mnFile.add(mntmOpenWktTabdelimited);
		
		mnFile.add(separator_1);
		mnImportExport.add(mntmImportCensusData);
		
		mntmExportToBlock = new JMenuItem("Export districts to block level");
		mntmExportToBlock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean ok = true;
				if( comboBoxDistrictColumn.getSelectedIndex() < 0 ) {
					ok = false;
				}
				if( ok && comboBoxDistrictColumn.getSelectedItem() == null) {
					ok = false;
				}
				if( ok && ((String)comboBoxDistrictColumn.getSelectedItem()).length() == 0) {
					ok = false;
				}
				if( ok) {
					new ExportToBlockLevelThread().start();
				} else {
					JOptionPane.showMessageDialog(MainFrame.mainframe,"You must select a district column first.");
				}
			}
		});
		mntmImportAggregate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new ImportAggregateCustom().init();
			}
		});
		
		mnImportExport.add(mntmImportAggregate);
		
		mnImportExport.add(separator_2);
		mnImportExport.add(mntmExportToBlock);
		mntmExportAndDeaggregate.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				/*
				JFileChooser jfc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Dbase file", "dbf");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}*/
				new ExportCustomThread().init();
			}				
		});
		
		mnImportExport.add(mntmExportAndDeaggregate);
		mnImportExport.add(new JSeparator());


		
		//mnFile.add(new JSeparator());
		
		//mnFile.add(chckbxmntmOpenCensusResults);
		
		//mnFile.add(mntmOpenElectionResults);
		
		
		mntmRenumber.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				renumber();
			}
		});
		mnFile.add(mntmRenumber);
		mntmMergeData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","tsv"));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null)  {
					return;
				}
				String fn = f.getName();
				String ext = fn.substring(fn.length()-3).toLowerCase();
				DataAndHeader dh = null;
				if( ext.equals("csv")) {
					String s = getFile(f).toString();
					dh = readDelimited(s,",",true);
				} else
				if( ext.equals("txt") || ext.equals("tsv")) {
					String s = getFile(f).toString();
					dh = readDelimited(s,"\t",true);
				} else
				if( ext.equals("dbf")) {
					String s = getFile(f).toString();
					dh = readDBF(f.getAbsolutePath());
				} else {
					JOptionPane.showMessageDialog(null, "File format not recognized.");
					return;
				}
				DialogMerge di = new DialogMerge();
				di.setData(featureCollection, dh.header, dh.data);
				di.show();
				
				project.district_column = (String) comboBoxDistrictColumn.getSelectedItem();
				project.population_column = (String) comboBoxPopulation.getSelectedItem();
				
				fillComboBoxes();
				
				hushcomboBoxPopulation = true;
				comboBoxPopulation.setSelectedItem("");
				hushcomboBoxPopulation = false;
				comboBoxPopulation.setSelectedItem(project.population_column);
				
				setElectionColumns();

				hushcomboBoxDistrict = true;
				comboBoxDistrictColumn.setSelectedItem("");
				hushcomboBoxDistrict = false;
				comboBoxDistrictColumn.setSelectedItem(project.district_column);
				
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnFile.add(mntmMergeData);
		
		mntmSaveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)",".dbf (dbase file)"};
				if( project.source_file.substring(project.source_file.length()-4).toLowerCase().equals(".shp")) {
					options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)"};//,".dbf (in separate dbase file)",".dbf (in original shapefile)"};
				}
				int opt = JOptionPane.showOptionDialog(mainframe, "Select desired format to save in.", "Select format", 0,0,null,options,options[0]);
				if( opt < 0) {
					System.out.println("aborted.");
					return;
				}
				
				System.out.println("collection districts...");
				featureCollection.storeDistrictsToProperties(project.district_column);
				System.out.println("getting headers...");
				String[] headers = featureCollection.getHeaders();
				System.out.println("getting data...");
				String[][] data = featureCollection.getData(headers);
				
				if( opt == 3 || opt == 2) {
					String filename = "";
					File f = null;
					if( opt == 3) {
						filename = project.source_file.substring(0,project.source_file.length()-3)+"dbf";
						f = new File(filename);
						if( f == null)  {
							return;
						}
					} else if( opt == 2) {
						JFileChooser jfc = new JFileChooser();
						jfc.setCurrentDirectory(new File(Download.getStartPath()));
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbase file","dbf"));
						jfc.showSaveDialog(null);
						f = jfc.getSelectedFile();
						if( f == null)  {
							return;
						}
						filename = f.getName();
					}
					System.out.println("writedbf start.");
					writeDBF(filename,headers,data);					
					System.out.println("writedbf done.");
				} else {
					JFileChooser jfc = new JFileChooser();
					jfc.setCurrentDirectory(new File(Download.getStartPath()));
					String delimiter = ",";
					if( opt == 0) { 
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
					} else if( opt == 1) {
						delimiter = "\t";
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
					}
					jfc.showSaveDialog(null);
					File f = jfc.getSelectedFile();
					if( f == null)  {
						System.out.println("f is null.");
						return;
					}
					String s = f.getAbsolutePath();
					if( !s.substring(s.length()-4).toLowerCase().equals(opt == 0 ? ".csv" : ".txt")) {
						s += opt == 0 ? ".csv" : ".txt";
						f = new File(s);
					}
					try {
						System.out.println("creating...");
						FileOutputStream fos = new FileOutputStream(f);
						StringBuffer sb = new StringBuffer();
						for(int i = 0; i < headers.length; i++) {
							sb.append((i>0?delimiter:"")+headers[i]);
						}
						sb.append("\n");
						
						for(int j = 0; j < data.length; j++) {
							for(int i = 0; i < headers.length; i++) {
								sb.append((i>0?delimiter:"")+data[j][i]);
							}
							sb.append("\n");
						}
						System.out.println("writing...");
						fos.write(sb.toString().getBytes());
						
						fos.flush();
						System.out.println("closing...");
						fos.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
						return;
					} 
				}
				JOptionPane.showMessageDialog(mainframe,"File saved.");
			}
		});
		mnFile.add(mntmSaveData);
		
		separator_4 = new JSeparator();
		mnFile.add(separator_4);
		
		mntmConvertWktTo = new JMenuItem("Convert WKT to lat/lon");
		mntmConvertWktTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new WKTFileToCoordsThread().init();
			}
		});
		mnImportExport.add(mntmConvertWktTo);
		mntmDescramble.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for( Feature feat : featureCollection.features) {
					feat.geometry.makePolysFull();
				}

				String[] districts = new String[featureCollection.features.size()];
				Feature.compare_centroid = true;
				String s = (String)mainframe.comboBoxDistrictColumn.getSelectedItem();
				Collections.sort(featureCollection.features);
				for( int i = 0; i < featureCollection.features.size(); i++) {
					districts[i] = featureCollection.features.get(i).properties.getString(s);
				}
				Feature.compare_centroid = false;
				Collections.sort(featureCollection.features);
				for( int i = 0; i < featureCollection.features.size(); i++) {
					featureCollection.features.get(i).properties.put(s,districts[i]);
				}
				System.out.println("done descrambling");
			}
		});
		
		mnImportExport.add(mntmDescramble);

		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
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
					// TODO Auto-generated catch ward
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
					// TODO Auto-generated catch ward
					e.printStackTrace();
				}
				loadElection(sb.toString());

			}
		});
		
		
		mntmOpenGeojson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				
				new OpenGeoJsonFileThread(fd).start();
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
						jfc.setCurrentDirectory(new File(Download.getStartPath()));
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
					for( VTD b : featureCollection.vtds) {
						b.has_census_results = false;
					}
					for( int i = 0; i < lines.length; i++) {
						String[] ss = lines[i].split("\t");
						String district = ss[0].trim();
						VTD b = featureCollection.wardHash.get(district);
						if( b == null) {
							not_found_in_geo.add(district);
						} else {
							b.has_census_results = true;
						}
					}
					Vector<String> not_found_in_census = new Vector<String>();
					for( VTD b : featureCollection.vtds) {
						if( b.has_census_results == false) {
							//not_found_in_census.add(b.name);
						}
					}
					if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
						for( VTD b : featureCollection.vtds) {
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
						VTD b = featureCollection.wardHash.get(district);
						b.has_census_results = true;
						b.population = Double.parseDouble(ss[1].replaceAll(",",""));
						System.out.println("ward "+b.id+" added census "+b.population);
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
						jfc.setCurrentDirectory(new File(Download.getStartPath()));
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
		chckbxmntmAutoAnneal.setSelected(true);
		
		chckbxmntmAutoAnneal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.auto_anneal = chckbxmntmAutoAnneal.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmAutoAnneal);
		
		chckbxmntmUseAnnealFloor = new JCheckBoxMenuItem("Use anneal floor");
		chckbxmntmUseAnnealFloor.setSelected(Settings.use_annealing_floor);
		chckbxmntmUseAnnealFloor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.use_annealing_floor = chckbxmntmUseAnnealFloor.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmUseAnnealFloor);
		
		mntmResetAnnealFloor = new JMenuItem("Reset anneal floor");
		mntmResetAnnealFloor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.annealing_starts_at = featureCollection.ecology.generation;
			}
		});
		mnEvolution.add(mntmResetAnnealFloor);
		mnEvolution.add(new JSeparator());
		
		mnConstraints = new JMenu("Communities of interest");
		menuBar.add(mnConstraints);
		
		mntmWholeCounties = new JMenuItem("Manage Locks");
		mntmWholeCounties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manageLocks.show();
				mapPanel.invalidate();
				mapPanel.repaint();
				//JOptionPane.showMessageDialog(mainframe,"Not implemented.");
			}
		});
		mnConstraints.add(mntmWholeCounties);
		
		JMenu mnView = new JMenu("Map");
		menuBar.add(mnView);
		chckbxmntmFlipVertical.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetZoom();
			}
		});
		//mnView.add(chckbxmntmFlipVertical);
		
		chckbxmntmFlipHorizontal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetZoom();
			}
		});
		//mnView.add(chckbxmntmFlipHorizontal);
		
		chckbxmntmShowPrecinctLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.showPrecinctLabels = chckbxmntmShowPrecinctLabels.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		//mnView.add(new JSeparator());
		//mnView.add(chckbxmntmShowPrecinctLabels);
		chckbxmntmShowDistrictLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.showDistrictLabels = chckbxmntmShowDistrictLabels.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
				//JOptionPane.showMessageDialog(null,"Not implemented");
			}
		});
		mnView.add(chckbxmntmShowDistrictLabels);
		chckbxmntmHideMapLines.setSelected(Feature.draw_lines );
		chckbxmntmHideMapLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Feature.draw_lines = chckbxmntmHideMapLines.isSelected();
				MapPanel.FSAA = Feature.draw_lines ? 4 : 1;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(chckbxmntmHideMapLines);
		
		chckbxmntmSimplifyPolygons = new JCheckBoxMenuItem("Simplify polygons");
		chckbxmntmSimplifyPolygons.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.b_make_simplifiied_polys = chckbxmntmSimplifyPolygons.isSelected();
			}
		});
		chckbxmntmSimplifyPolygons.setToolTipText("This makes the map draw considerably faster by reducing the number of lines in each polygon.");
		mnView.add(chckbxmntmSimplifyPolygons);
		
		
		separator_6 = new JSeparator();
		mnView.add(separator_6);
		
		mntmColorByDistrict = new JMenuItem("Color by district");
		mntmColorByDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.display_mode = Feature.DISPLAY_MODE_NORMAL;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByDistrict);
		
		mntmColorByPop = new JMenuItem("Color by pop imbalance");
		mntmColorByPop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Feature.display_mode = Feature.DISPLAY_MODE_DIST_POP;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByPop);
		
		mntmColorByVote = new JMenuItem("Color by vote");
		mntmColorByVote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Feature.display_mode = Feature.DISPLAY_MODE_DIST_DEMO;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByVote);
		
		
		mntmColorByCompactness = new JMenuItem("Color by compactness");
		mntmColorByCompactness.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.display_mode = Feature.DISPLAY_MODE_COMPACTNESS;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mntmColorByWasted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Feature.display_mode = Feature.DISPLAY_MODE_WASTED_VOTES;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		mnView.add(mntmColorByWasted);
		mnView.add(mntmColorByCompactness);
		
		mntmShowVoteBalance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Feature.display_mode = Feature.DISPLAY_MODE_VOTES;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmShowVoteBalance);
		mntmShowDemographics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( true) {
					JOptionPane.showMessageDialog(null,"Not implemented");
					return;
				}
				Feature.display_mode = Feature.DISPLAY_MODE_DEMOGRAPHICS;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmShowDemographics);
		
		
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
		
		//mnView.add(separator_3);
		mntmAntialiasingOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MapPanel.FSAA = 1;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		//mnView.add(mntmAntialiasingOff);
		mntmxAntialiasing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MapPanel.FSAA = 2;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		//mnView.add(mntmxAntialiasing);
		mntmxAntialiasing_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MapPanel.FSAA = 4;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mntmxAntialiasing_1.setSelected(true);
		
		//mnView.add(mntmxAntialiasing_1);
		
		separator_5 = new JSeparator();
		mnView.add(separator_5);
		
		mntmNoMap = new JMenuItem("No map");
		mntmNoMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.num_maps_to_draw = 0;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmNoMap);
		
		mntmOneMap = new JMenuItem("One map");
		mntmOneMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.num_maps_to_draw = 1;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmOneMap);
		
		mntmFourMaps = new JMenuItem("Four maps");
		mntmFourMaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.num_maps_to_draw = 4;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmFourMaps);
		
		//JMenu mnResults = new JMenu("Results");
		//menuBar.add(mnResults);
		mntmLicense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(mainframe,""
					    +"\nCopyright (C) 2015 Kevin Baas"
					    +"\n"
					    +"\nThis program is free software: you can redistribute it and/or modify"
					    +"\nit under the terms of the GNU General Public License as published by"
					    +"\nthe Free Software Foundation, either version 3 of the License, or"
					    +"\n(at your option) any later version."
					    +"\n"
					    +"\nThis program is distributed in the hope that it will be useful,"
					    +"\nbut WITHOUT ANY WARRANTY; without even the implied warranty of"
					    +"\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
					    +"\nGNU General Public License for more details."
					    +"\n"
					    +"\nYou should have received a copy of the GNU General Public License"
					    +"\nalong with this program.  If not, see <http://www.gnu.org/licenses/>."
						);
			}	
		});
		mntmSourceCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				URI uri = null;
				try {
					uri = new URL("https://github.com/happyjack27/autoredistrict").toURI();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
			            desktop.browse(uri);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    }
			}	
		});
		mntmWebsite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				URI uri = null;
				try {
					uri = new URL("http://autoredistrict.org/documentation.html").toURI();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
			            desktop.browse(uri);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    }
			}	
		});
		
		mntmExportcsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( featureCollection.ecology.population == null || featureCollection.ecology.population.size() == 0) {
					JOptionPane.showMessageDialog(null,"No results");
				}
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.showSaveDialog(null);
				File f = jfc.getSelectedFile();
				if( f == null) {
					return;
				}
				StringBuffer sb = new StringBuffer();
				try {
					FileOutputStream fis = new FileOutputStream(f);
					
					DistrictMap dm = featureCollection.ecology.population.get(0);
					for( int i = 0; i < dm.vtd_districts.length; i++) {
						VTD b = featureCollection.ecology.wards.get(i);
						//sb.append(b.name+", "+dm.ward_districts[i]+"\n\r");
						//fis.write((""+b.name.trim()+", "+dm.ward_districts[i]+"\r\n").getBytes());
					}
					
					//fis.write(sb.toString().getBytes());
					fis.flush();
					fis.close();
					JOptionPane.showMessageDialog(null,"File saved.");
				} catch (Exception ex) {
					// TODO Auto-generated catch ward
					ex.printStackTrace();
					return;
				}
			}
		});
		
		
		mntmImportcsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();

				String s = getFile(f).toString();
				String[] lines = s.split("\n");
				
				Vector<String> not_found_in_geo = new Vector<String>();
				for( VTD b : featureCollection.vtds) {
					b.temp = -1;
				}
				for( int i = 0; i < lines.length; i++) {
					try {
						String[] ss = lines[i].split(",");
						String district = ss[0].trim();
						VTD b = featureCollection.wardHash.get(district);
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
				for( VTD b : featureCollection.vtds) {
					if( b.temp < 0) {
						//not_found_in_census.add(b.name);
					}
				}
				if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
					for( VTD b : featureCollection.vtds) {
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
				int[] new_ward_districts = new int[featureCollection.vtds.size()];
				int num_districts = 0;
				for( int i = 0; i < new_ward_districts.length; i++) {
					int d = featureCollection.vtds.get(i).temp;
					if( num_districts < d) {
						num_districts = d;
					}
					new_ward_districts[i] = d;
				}
				num_districts++;
				Settings.num_districts = num_districts;
				textFieldNumDistricts.setText(""+Settings.num_districts);
				if( featureCollection.ecology.population == null) {
					featureCollection.ecology.population = new Vector<DistrictMap>();
				}
				if( featureCollection.ecology.population.size() < 1) {
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.vtds,Settings.num_districts,new_ward_districts));
				}
				while( featureCollection.ecology.population.size() < Settings.population) {
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.vtds,Settings.num_districts,new_ward_districts));
				}
				for( DistrictMap dm : featureCollection.ecology.population) {
					dm.setGenome(new_ward_districts);
					dm.fillDistrictwards();
				
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
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
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
						for( int i = 0; i < dm.vtd_districts.length; i++) {
							VTD b = featureCollection.ecology.wards.get(i);
							//sb.append(b.name+", "+dm.ward_districts[i]+"\n\r");
							//fis.write((""+b.name.trim()+", "+dm.ward_districts[i]+"\r\n").getBytes());
						}
						
						//fis.write(sb.toString().getBytes());
						fis.flush();
						fis.close();
					} catch (Exception ex) {
						// TODO Auto-generated catch ward
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
		//menuBar.add(S)
		mnHelp.add(mntmWebsite);
		mnHelp.add(mntmSourceCode);
		mnHelp.add(mntmLicense);
		
		menuBar.add(mnWindows);
		mnWindows.add(mntmShowStats);
		mnWindows.add(mntmShowGraph);
		mnWindows.add(mntmShowData);
		
				JSeparator separator_8 = new JSeparator();
				mnWindows.add(separator_8);
		mnWindows.add(mntmShowSeats);
		mnWindows.add(mntmShowRankedDistricts);
		mntmShowRankedDistricts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameRankedDist.show();
			}
		});
		mntmShowSeats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frameSeatsVotesChart.show();
			}
		});
		
		
		mntmShowData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] headers = featureCollection.getHeaders();
				String[][] data = featureCollection.getData(headers);

				df.setTableSource(new DataAndHeader(data,headers));
				df.setTitle("Map Data");
				df.show();//new DialogShowProperties(featureCollection).show();
			}
		});
		mntmShowGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameGraph.show();
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
		menuBar.add(Box.createHorizontalGlue());
		mnHelp.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); 
		menuBar.add(mnHelp);
		
		JSplitPane splitPane = new JSplitPane();
		JSplitPane splitPane2 = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		panel.setPreferredSize(new Dimension(600, 100));
		panel_1.setPreferredSize(new Dimension(200,100));
		panel.setLayout(null);
		panel_1.setLayout(null);
		splitPane.setLeftComponent(panel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(400, 55, 200, 221);
		panel.add(panel_2);
		panel_2.setLayout(null);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel lblCompactness = new JLabel("Compactness");
		lblCompactness.setBounds(6, 97, 90, 16);
		panel_2.add(lblCompactness);
		sliderBorderLength.setBounds(6, 118, 190, 29);
		panel_2.add(sliderBorderLength);
		
		JLabel lblEvolutionaryPressure = new JLabel("Geometric criteria");
		lblEvolutionaryPressure.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEvolutionaryPressure.setBounds(6, 9, 179, 16);
		panel_2.add(lblEvolutionaryPressure);
		
		JLabel lblProportionalRepresentation = new JLabel("Equal population");
		lblProportionalRepresentation.setBounds(6, 158, 172, 16);
		panel_2.add(lblProportionalRepresentation);
		sliderPopulationBalance.setBounds(6, 179, 190, 29);
		panel_2.add(sliderPopulationBalance);
		
		JLabel lblConnectedness = new JLabel("Contiguity");
		lblConnectedness.setBounds(6, 36, 172, 16);
		panel_2.add(lblConnectedness);
		
		sliderDisconnected.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.disconnected_population_weight = sliderDisconnected.getValue()/100.0;
			}
		});
		sliderDisconnected.setBounds(6, 57, 190, 29);
		panel_2.add(sliderDisconnected);
		
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setBounds(200, 0, 200, 399);
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
		
		textField.setText(""+Settings.population);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Evolution");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(6, 6, 159, 16);
		panel_3.add(lblNewLabel);
		JLabel lblBorderMutation = new JLabel("% mutation");
		lblBorderMutation.setBounds(6, 103, 172, 16);
		panel_3.add(lblBorderMutation);
		slider_mutation.setBounds(6, 130, 190, 29);
		panel_3.add(slider_mutation);
		
		lblElitism = new JLabel("% elitism");
		lblElitism.setBounds(6, 181, 69, 16);
		panel_3.add(lblElitism);
		
		sliderElitism = new JSlider();
		sliderElitism.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.elite_fraction = ((double)sliderElitism.getValue())/100.0;
			}
		});
		sliderElitism.setValue((int)(Settings.elite_fraction*100.0));
		sliderElitism.setBounds(6, 208, 190, 29);
		panel_3.add(sliderElitism);
		
		chckbxMutateElite = new JCheckBox("mutate elite");
		chckbxMutateElite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.mutate_all = chckbxMutateElite.isSelected();
			}
		});
		chckbxMutateElite.setBounds(81, 178, 115, 23);
		chckbxMutateElite.setSelected(Settings.mutate_all);
		panel_3.add(chckbxMutateElite);
		
		rdbtnTruncationSelection = new JRadioButton("Truncation selection");
		rdbtnTruncationSelection.setBounds(6, 244, 188, 23);
		panel_3.add(rdbtnTruncationSelection);
		rdbtnTruncationSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.TRUNCATION_SELECTION;
				tournamentSlider.setVisible(false);
			}
		});
		
		rdbtnTruncationSelection.setSelected(Settings.SELECTION_MODE == Settings.TRUNCATION_SELECTION);
		
		selectionType.add(rdbtnTruncationSelection);
		
		rdbtnRankSelection = new JRadioButton("Rank selection");
		rdbtnRankSelection.setBounds(6, 270, 188, 23);
		panel_3.add(rdbtnRankSelection);
		rdbtnRankSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.RANK_SELECTION;
				tournamentSlider.setVisible(false);
			}
		});
		rdbtnRankSelection.setSelected(Settings.SELECTION_MODE == Settings.RANK_SELECTION);
		selectionType.add(rdbtnRankSelection);
		
		rdbtnRouletteSelection = new JRadioButton("Roulette selection");
		rdbtnRouletteSelection.setBounds(6, 298, 188, 23);
		panel_3.add(rdbtnRouletteSelection);
		rdbtnRouletteSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.ROULETTE_SELECTION;
				tournamentSlider.setVisible(false);
			}
		});
		rdbtnRouletteSelection.setSelected(Settings.SELECTION_MODE == Settings.ROULETTE_SELECTION);
		selectionType.add(rdbtnRouletteSelection);
		rdbtnTournamentSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.TOURNAMENT_SELECTION;
				tournamentSlider.setVisible(true);
			}
		});
		rdbtnTournamentSelection.setBounds(6, 326, 172, 23);
		selectionType.add(rdbtnTournamentSelection);
		
		panel_3.add(rdbtnTournamentSelection);
		tournamentSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.tournament_exponent = Settings.tournament_exponent_max*(100.0-(double)tournamentSlider.getValue())/100.0;
			}
		});
		tournamentSlider.setBounds(6, 361, 190, 29);
		tournamentSlider.setVisible(false);
		tournamentSlider.setValue((int)(100.0-100.0*(Settings.tournament_exponent/Settings.tournament_exponent_max)));
		
		
		panel_3.add(tournamentSlider);
		textFieldNumDistricts.setText(""+Settings.num_districts);
		
		
		textFieldNumDistricts.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textFieldNumDistricts.postActionEvent();
			}
		});
		textFieldNumDistricts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.num_districts = Integer.parseInt(textFieldNumDistricts.getText());
					setSeatsMode();
				} catch (Exception ex) { }
			}
		});
		textFieldNumDistricts.setColumns(10);
		textFieldNumDistricts.setBounds(132, 69, 52, 28);
		panel.add(textFieldNumDistricts);
		
		JLabel lblNumOfDistricts = new JLabel("Num. of districts");
		lblNumOfDistricts.setBounds(6, 75, 124, 16);
		panel.add(lblNumOfDistricts);
		
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop evolving a solution.");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopEvolving();
			}
		});
		stopButton.setBounds(6, 29, 83, 29);
		panel.add(stopButton);
		
		goButton.setText("Go");
		goButton.setToolTipText("Start evolving a solution.");
		goButton.setBorder(BorderFactory.createRaisedBevelBorder());
		stopButton.setBorder(BorderFactory.createRaisedBevelBorder());
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				if( project.election_columns == null || project.election_columns.size() == 0) {
					JOptionPane.showMessageDialog(mainframe, "Must select election columns");
					return;
				}*/
				Ecology.invert = 1;
				addEcologyListeners();
				featureCollection.ecology.startEvolving();
				evolving = true;
				setEnableds();
				goButton.setEnabled(false);
				stopButton.setEnabled(true);
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(true);
				progressBar.setValue(0);
			}
		});
		goButton.setBounds(109, 29, 83, 29);
		panel.add(goButton);
		
		textFieldSeatsPerDistrict = new JTextField();
		textFieldSeatsPerDistrict.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textFieldSeatsPerDistrict.postActionEvent();
			}
		});
		textFieldSeatsPerDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.seats_number_per_district = Integer.parseInt(textFieldSeatsPerDistrict.getText());
					setSeatsMode();
					panelStats.getStats();
				} catch (Exception ex) { }
			}
		});
		textFieldSeatsPerDistrict.setText("1");
		textFieldSeatsPerDistrict.setColumns(10);
		textFieldSeatsPerDistrict.setBounds(132, 106, 52, 28);
		panel.add(textFieldSeatsPerDistrict);
		
		JRadioButton lblMembersPerDistrict = new JRadioButton("Seats/district");
		lblMembersPerDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSeatsMode();
			}
		});
		lblMembersPerDistrict.setSelected(true);
		seatsModeBG.add(lblMembersPerDistrict);
		lblMembersPerDistrict.setBounds(6, 112, 124, 16);
		panel.add(lblMembersPerDistrict);
		
		comboBoxPopulation.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxPopulation) return;
				setPopulationColumn((String)comboBoxPopulation.getSelectedItem());
			}
		});
		comboBoxPopulation.setBounds(8, 237, 178, 20);
		panel.add(comboBoxPopulation);
		
		JLabel lblPopulationColumn = new JLabel("Population column");
		lblPopulationColumn.setBounds(8, 217, 182, 16);
		panel.add(lblPopulationColumn);
		
		lblDistrictColumn = new JLabel("District column");
		lblDistrictColumn.setBounds(8, 503, 182, 16);
		panel.add(lblDistrictColumn);
		
		comboBoxDistrictColumn = new JComboBox();
		comboBoxDistrictColumn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxDistrict) return;
				setDistrictColumn((String)comboBoxDistrictColumn.getSelectedItem());
			}
		});
		comboBoxDistrictColumn.setBounds(8, 522, 178, 20);
		panel.add(comboBoxDistrictColumn);
		
		panel_4 = new JPanel();
		panel_4.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_4.setLayout(null);
		panel_4.setBounds(400, 274, 200, 302);
		panel.add(panel_4);
		
		JLabel lblContiguency = new JLabel("Representativeness");
		lblContiguency.setBounds(9, 160, 172, 16);
		lblContiguency.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/representativeness_tooltip.png") + "\">");
		panel_4.add(lblContiguency);
		sliderRepresentation.setBounds(9, 185, 180, 29);
		sliderRepresentation.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/representativeness_tooltip.png") + "\">");
		panel_4.add(sliderRepresentation);
		sliderWastedVotesTotal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.wasted_votes_total_weight = sliderWastedVotesTotal.getValue()/100.0;
			}
		});
		sliderWastedVotesTotal.setBounds(10, 120, 180, 29);
		sliderWastedVotesTotal.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/wasted_votes.png") + "\">");
		
		panel_4.add(sliderWastedVotesTotal);
		lblWastedVotes.setBounds(10, 99, 172, 16);
		lblWastedVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/wasted_votes.png") + "\">");
		
		panel_4.add(lblWastedVotes);
		
		lblSeatsVotes = new JLabel("Partisan symmetry");
		lblSeatsVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/seats_votes_asymmetry_tooltip.png") + "\">");
		lblSeatsVotes.setBounds(10, 38, 179, 16);
		panel_4.add(lblSeatsVotes);
		
		sliderSeatsVotes = new JSlider();
		sliderSeatsVotes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.seats_votes_asymmetry_weight = sliderSeatsVotes.getValue()/100.0;
			}
		});
		sliderSeatsVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/seats_votes_asymmetry_tooltip.png") + "\">");
		sliderSeatsVotes.setBounds(10, 59, 180, 29);
		panel_4.add(sliderSeatsVotes);
		
		btnElectionColumns = new JButton("Election columns");
		btnElectionColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectLayers();
			}
		});
		btnElectionColumns.setBounds(6, 268, 184, 23);
		panel.add(btnElectionColumns);
		
		lblGeometricFairness = new JLabel("Geometric <===> Fairness");
		lblGeometricFairness.setHorizontalAlignment(SwingConstants.CENTER);
		lblGeometricFairness.setBounds(410, 11, 180, 16);
		panel.add(lblGeometricFairness);
		
		sliderBalance = new JSlider();
		sliderBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.geo_or_fair_balance_weight = sliderBalance.getValue()/100.0;
			}
		});
		sliderBalance.setBounds(408, 26, 182, 29);
		panel.add(sliderBalance);
		
		progressBar.setBounds(10, 11, 180, 14);
		panel.add(progressBar);
		
		btnSubstituteColumns = new JButton("Substitute columns");
		btnSubstituteColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean is_evolving = evolving;
				if( is_evolving) { featureCollection.ecology.stopEvolving(); }
				setDistrictColumn(project.district_column);
				//featureCollection.loadDistrictsFromProperties(project.district_column);
				DialogSelectLayers dlg = new DialogSelectLayers();
				dlg.setData(featureCollection,project.substitute_columns);
				dlg.show();
				if( !dlg.ok) {
					if( is_evolving) { featureCollection.ecology.startEvolving(); }
					return;
				}

				try {
					project.substitute_columns = dlg.in;
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
				if( project.substitute_columns.size() != project.election_columns.size()) {
					JOptionPane.showMessageDialog(mainframe, "Election columns and substitute columns must match one-to-one.");
					chckbxSubstituteColumns.doClick();//.setSelected(false);
				}
				//mntmShowDemographics.setSelected(true);
				//Feature.display_mode = 1;
				mapPanel.invalidate();
				mapPanel.repaint();
				
				if( is_evolving) { featureCollection.ecology.startEvolving(); }
			}

		});
		btnSubstituteColumns.setEnabled(false);
		btnSubstituteColumns.setBounds(6, 469, 184, 23);
		panel.add(btnSubstituteColumns);
		
		chckbxSubstituteColumns = new JCheckBox("Substitute uncontested");
		chckbxSubstituteColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean b = chckbxSubstituteColumns.isSelected();
				if( b && false) {
					JOptionPane.showMessageDialog(mainframe, "Not implemented.");
					chckbxSubstituteColumns.setSelected(false);
					return;
				}
				btnSubstituteColumns.setEnabled(b);
				Settings.substitute_uncontested = b;
				if( b) {
					if( project.election_columns == null || project.election_columns.size() < 1) {
						JOptionPane.showMessageDialog(mainframe, "Must select election columns first.");
						chckbxSubstituteColumns.doClick();
					} else {
						btnSubstituteColumns.doClick();
					}
				}
			}
		});
		chckbxSubstituteColumns.setBounds(6, 439, 178, 23);
		panel.add(chckbxSubstituteColumns);
		btnElection2Columns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLayers2();
			}
		});
		btnElection2Columns.setEnabled(false);
		btnElection2Columns.setBounds(7, 333, 184, 23);
		
		panel.add(btnElection2Columns);
		chckbxSecondElection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b = chckbxSecondElection.isSelected();
				if( b && false) {
					JOptionPane.showMessageDialog(mainframe, "Not implemented.");
					chckbxSecondElection.setSelected(false);
					return;
				}
				btnElection2Columns.setEnabled(b);
				chckbxThirdElection.setEnabled(b);
				//Settings.substitute_uncontested = b;
				if( b) {
					if( project.election_columns == null || project.election_columns.size() < 1) {
						JOptionPane.showMessageDialog(mainframe, "Must select election columns first.");
						chckbxSecondElection.doClick();
					} else {
						btnElection2Columns.doClick();
					}
				} else {
					setMaxElections();					
				}
			}
		});
		chckbxSecondElection.setBounds(6, 301, 178, 23);
		
		panel.add(chckbxSecondElection);
		
		chckbxThirdElection = new JCheckBox("Third election");
		chckbxThirdElection.setEnabled(false);
		chckbxThirdElection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean b = chckbxThirdElection.isSelected();
				if( b && false) {
					JOptionPane.showMessageDialog(mainframe, "Not implemented.");
					chckbxThirdElection.setSelected(false);
					return;
				}
				btnElection3Columns.setEnabled(b);
				//Settings.substitute_uncontested = b;
				if( b) {
					if( project.election_columns_2 == null || project.election_columns_2.size() < 1) {
						JOptionPane.showMessageDialog(mainframe, "Must select second election columns first.");
						chckbxThirdElection.doClick();
					} else {
						btnElection3Columns.doClick();
					}
				} else {
					setMaxElections();					
				}
			}
		});
		chckbxThirdElection.setBounds(6, 373, 178, 23);
		panel.add(chckbxThirdElection);
		
		btnElection3Columns = new JButton("Election 3 columns");
		btnElection3Columns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLayers3();
			}
		});
		btnElection3Columns.setEnabled(false);
		btnElection3Columns.setBounds(7, 405, 184, 23);
		panel.add(btnElection3Columns);
		
		panel_5 = new JPanel();
		panel_5.setBounds(200, 397, 200, 210);
		panel_5.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_5.setLayout(null);
		panel.add(panel_5);
		//panel_5.setVisible(false);
		
		srlblSplitReduction = new JLabel("Split reduction");
		srlblSplitReduction.setBounds(10, 136, 172, 16);
		panel_5.add(srlblSplitReduction);
		srlblSplitReduction.setToolTipText("<html><img src=\"file:/C:/Users/kbaas.000/git/autoredistrict/bin/resources/voting_power.png\">");
		
		sliderSplitReduction = new JSlider();
		sliderSplitReduction.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.split_reduction_weight = sliderSplitReduction.getValue()/100.0;
			}
		});
		sliderSplitReduction.setBounds(10, 157, 180, 29);
		panel_5.add(sliderSplitReduction);
		sliderSplitReduction.setValue(0);
		
		srcomboBoxCountyColumn = new JComboBox();
		srcomboBoxCountyColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hushcomboBoxCountyColumn) {
					return;
				}
				MainFrame.mainframe.project.county_column = (String)srcomboBoxCountyColumn.getSelectedItem();
				setCountyColumn();
				panelStats.getStats();
			}
		});
		srcomboBoxCountyColumn.setBounds(8, 56, 178, 20);
		panel_5.add(srcomboBoxCountyColumn);
		
		srlblCountyColumn = new JLabel("County column");
		srlblCountyColumn.setBounds(8, 37, 182, 16);
		panel_5.add(srlblCountyColumn);
		
		chckbxReduceSplits = new JCheckBox("Reduce splits");
		chckbxReduceSplits.setFont(new Font("Tahoma", Font.BOLD, 14));
		chckbxReduceSplits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( false) {
					JOptionPane.showMessageDialog(null, ""
							+"\nNot implemented."
							+"\n\nUse Constraints, Manage locks instead."
							);
					chckbxReduceSplits.setSelected(false);
					return;
				}
				/*
				if( chckbxReduceSplits.isSelected()) {
					int opt = JOptionPane.showConfirmDialog(null, ""
							+"\nThis will negatively impact ALL criteria, both geometric and fairness."
							+"\n\nAnd has absolutely NO benefits."
							+"\n\nAlso, the impact will NOT be neutral."
							+"\nIt will have the effect of packing votes in urban areas,"
							+"\nwhich are predominately Democratic voters."
							+"\nSo it's essentially rule-based gerrymandering in favor of Republicans."
							+"\n\nThis violates both the equal protection amendment"
							+"\nand the first amendment (by taking away urban voters' voice)."
							+"\n\nDo you understand and acknowledge these negative impacts"
							+"\nand still want to proceed?"
							);
					if( opt != JOptionPane.YES_OPTION) {
						chckbxReduceSplits.setSelected(false);
						return;
					}
				}*/
				Settings.reduce_splits = chckbxReduceSplits.isSelected();
				sliderSplitReduction.setEnabled(Settings.reduce_splits);
				srlblSplitReduction.setEnabled(Settings.reduce_splits);
				srlblCountyColumn.setEnabled(Settings.reduce_splits);
				srcomboBoxCountyColumn.setEnabled(Settings.reduce_splits);
				srlblMuniColumn.setEnabled(Settings.reduce_splits);
				srcomboBoxMuniColumn.setEnabled(Settings.reduce_splits);
			}
		});
		chckbxReduceSplits.setBounds(6, 7, 176, 23);
		panel_5.add(chckbxReduceSplits);
		srlblCountyColumn.setEnabled(Settings.reduce_splits);
		sliderSplitReduction.setEnabled(Settings.reduce_splits);
		srcomboBoxCountyColumn.setEnabled(Settings.reduce_splits);
		srlblSplitReduction.setEnabled(Settings.reduce_splits);
		srlblMuniColumn.setEnabled(false);
		srlblMuniColumn.setBounds(8, 87, 182, 16);
		
		panel_5.add(srlblMuniColumn);
		srcomboBoxMuniColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hushcomboBoxMuniColumn) {
					return;
				}
				MainFrame.mainframe.project.muni_column = (String)srcomboBoxMuniColumn.getSelectedItem();
				setMuniColumn();
				panelStats.getStats();
			}
		});
		srcomboBoxMuniColumn.setEnabled(false);
		srcomboBoxMuniColumn.setBounds(8, 106, 178, 20);
		
		panel_5.add(srcomboBoxMuniColumn);
		
		btnEthnicityColumns = new JButton("Ethnicity columns");
		btnEthnicityColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( false) {
					JOptionPane.showMessageDialog(null, "Not yet implemented.");
					return;
				}
				boolean is_evolving = evolving;
				if( is_evolving) { featureCollection.ecology.stopEvolving(); }
				//setDistrictColumn(project.district_column);
				//featureCollection.loadDistrictsFromProperties(project.district_column);
				DialogSelectLayers dlg = new DialogSelectLayers();
				dlg.setData(featureCollection,project.demographic_columns);
				dlg.show();
				if( !dlg.ok) {
					if( is_evolving) { featureCollection.ecology.startEvolving(); }
					return;
				}

				try {
					project.demographic_columns = dlg.in;
					setDemographicColumns();
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
				//mntmShowDemographics.setSelected(true);
				//Feature.display_mode = 1;
				mapPanel.invalidate();
				mapPanel.repaint();
				panelStats.getStats();
				
				if( is_evolving) { featureCollection.ecology.startEvolving(); }
			}
		});
		btnEthnicityColumns.setBounds(8, 553, 184, 23);
		panel.add(btnEthnicityColumns);
		textFieldTotalSeats.setEnabled(false);
		textFieldTotalSeats.setText("1");
		textFieldTotalSeats.setBounds(132, 140, 52, 28);
		textFieldTotalSeats.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textFieldTotalSeats.postActionEvent();
			}
		});
		textFieldTotalSeats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.seats_number_total = Integer.parseInt(textFieldTotalSeats.getText());
					setSeatsMode();
					panelStats.getStats();
				} catch (Exception ex) { }
			}
		});
		
		
		panel.add(textFieldTotalSeats);
		lblTotalSeats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSeatsMode();
			}
		});
		seatsModeBG.add(lblTotalSeats);
		lblTotalSeats.setBounds(6, 146, 124, 16);
		
		panel.add(lblTotalSeats);
		chckbxNewCheckBox.setSelected(true);
		chckbxNewCheckBox.setEnabled(false);
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.setNo4s(chckbxNewCheckBox.isSelected());
				setSeatsMode();
			}
		});
		chckbxNewCheckBox.setBounds(16, 180, 172, 23);
		
		panel.add(chckbxNewCheckBox);
		sliderRepresentation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				//Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;
				Settings.diagonalization_weight = sliderRepresentation.getValue()/100.0;

			}
		});
		slider_mutation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_mutation.getValue()/100.0;
			}
		});
		sliderPopulationBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.population_balance_weight = sliderPopulationBalance.getValue()/100.0;
			}
		});
		sliderBorderLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.geometry_weight = sliderBorderLength.getValue()/100.0;
			}
		});
		
		chckbxmntmMutateDisconnected = new JCheckBoxMenuItem("Mutate disconnected");
		chckbxmntmMutateDisconnected.setSelected(Settings.mutate_disconnected );
		chckbxmntmMutateDisconnected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_disconnected = chckbxmntmMutateDisconnected.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmMutateDisconnected);
		
		mnEvolution.add(new JSeparator());
		
		chckbxmntmMutateExcessPop = new JCheckBoxMenuItem("Mutate towards equal pop only");
		chckbxmntmMutateExcessPop.setSelected(Settings.mutate_excess_pop );
		chckbxmntmMutateExcessPop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_excess_pop = chckbxmntmMutateExcessPop.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmMutateExcessPop);

		chckbxmntmMutateExcessPopOnly = new JCheckBoxMenuItem("Mutate overpopulated only");
		chckbxmntmMutateExcessPopOnly.setSelected(Settings.mutate_overpopulated);
		chckbxmntmMutateExcessPopOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_overpopulated = chckbxmntmMutateExcessPopOnly.isSelected();
			}
		});
		//mnEvolution.add(chckbxmntmMutateExcessPopOnly);

		chckbxmntmMutateCompetitive = new JCheckBoxMenuItem("Mutate towards competitive only");
		chckbxmntmMutateCompetitive.setSelected(Settings.mutate_competitive );
		chckbxmntmMutateCompetitive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_competitive = chckbxmntmMutateCompetitive.isSelected();
			}
		});
		mnEvolution.add(chckbxmntmMutateCompetitive);

		chckbxmntmMutateCompactness = new JCheckBoxMenuItem("Mutate towards compactness only");
		chckbxmntmMutateCompactness.setSelected(Settings.mutate_compactness );
		chckbxmntmMutateCompactness.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_compactness = chckbxmntmMutateCompactness.isSelected();
			}
		});
		if( Settings.mutate_compactness_working) {
			mnEvolution.add(chckbxmntmMutateCompactness);
		}
		
		chckbxmntmMutateAnyAbove = new JCheckBoxMenuItem("Mutate towards any above only");
		chckbxmntmMutateAnyAbove.setSelected(Settings.mutate_good );
		chckbxmntmMutateAnyAbove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_good = chckbxmntmMutateAnyAbove.isSelected();
				chckbxmntmMutateCompactness.setEnabled(!Settings.mutate_good);
				chckbxmntmMutateCompetitive.setEnabled(!Settings.mutate_good);
				chckbxmntmMutateExcessPop.setEnabled(!Settings.mutate_good);
			}
		});
		mnEvolution.add(chckbxmntmMutateAnyAbove);



		//Settings.speciation_fraction = 0.5;//1.0;
		//Settings.disconnected_population_weight = 0.0;

		splitPane.setRightComponent(splitPane2);
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane2.setTopComponent(seatsPanel);
		splitPane2.setBottomComponent(mapPanel);
		mapPanel.seatsPanel = seatsPanel;
		seatsPanel.mapPanel= mapPanel;
		
		panelStats.featureCollection = featureCollection;
		frameStats = new JFrame();
		frameStats.setContentPane(panelStats);
		frameStats.setTitle("Stats");
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
		frameGraph.show();
		frameSeatsVotesChart.move(this.getWidth(), this.getX()+frameGraph.getHeight());
		frameSeatsVotesChart.show();
		frameStats.move(this.getWidth()+frameSeatsVotesChart.getWidth(), this.getX()+frameGraph.getHeight());
		frameStats.show();
		
	}
	public boolean hush_setSeatsMode = false;
	public final JCheckBox chckbxNewCheckBox = new JCheckBox("No 4 seat districts");
	public final JLabel srlblMuniColumn = new JLabel("Muni column");
	public final JComboBox srcomboBoxMuniColumn = new JComboBox();
	public JCheckBoxMenuItem chckbxmntmSimplifyPolygons;
	public void setSeatsMode() {
		System.out.println("setSeatsMode called hushed?: "+hush_setSeatsMode);
		if( hush_setSeatsMode) {
			return;
		}
		hush_setSeatsMode = true;
		boolean was_total = Settings.seats_mode  == Settings.SEATS_MODE_TOTAL;
		int prev_total_seats = Settings.total_seats();
		Settings.seats_mode = this.lblTotalSeats.isSelected() ? Settings.SEATS_MODE_TOTAL : Settings.SEATS_MODE_PER_DISTRICT;
		boolean is_total = Settings.seats_mode  == Settings.SEATS_MODE_TOTAL;
		textFieldTotalSeats.setEnabled(is_total);
		textFieldSeatsPerDistrict.setEnabled(!is_total);
		textFieldNumDistricts.setEnabled(!is_total);
		chckbxNewCheckBox.setEnabled(is_total);
		if( is_total) {
			textFieldSeatsPerDistrict.setText("");
			if( !was_total) {
				textFieldTotalSeats.setText(""+(int)(prev_total_seats*5));
				Settings.seats_number_total = prev_total_seats*5;
			}
			try {
				int seats = Integer.parseInt(textFieldTotalSeats.getText());
				int[] sc = Settings.getSeatDistribution(seats);
				int tot = 0;
				for( int i = 0; i < sc.length; i++) {
					tot += sc[i];
				}
				textFieldNumDistricts.setText(""+tot);
				Settings.num_districts = tot;
			} catch (Exception ex) {
				
			}
		} else {
			if( was_total) {
				textFieldSeatsPerDistrict.setText("1");
				textFieldNumDistricts.setText(""+prev_total_seats);
				Settings.num_districts = prev_total_seats;
			} else {
				//textFieldTotalSeats.setText(""+(Settings.num_districts*Settings.seats_number_per_district));
			}
		}
		try {
			featureCollection.ecology.resize_districts();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		panelStats.getStats();
		hush_setSeatsMode = false;
		System.out.println("setSeatsMode returned");
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
			// TODO Auto-generated catch ward
			ex.printStackTrace();
			return sb;
		} 
		return sb;
	}

	@Override
	public void valueChanged() {
 		textField.setText(""+Settings.population);
		//System.out.println("new boundary mutation rate: "+Settings.mutation_boundary_rate+" total: "+total+" mutated: "+mutated);
		slider_mutation.setValue((int)(Settings.mutation_boundary_rate*100.0/MainFrame.boundary_mutation_rate_multiplier));
		invalidate();
		repaint();
	}
	@Override
	public void eventOccured() {
		if( project.num_generations > 0 && featureCollection.ecology.generation >= project.num_generations) {
			stopEvolving();
			System.out.println("collection districts...");
			featureCollection.storeDistrictsToProperties(project.district_column);
			System.out.println("getting headers...");
			String[] headers = featureCollection.getHeaders();
			System.out.println("getting data...");
			String[][] data = featureCollection.getData(headers);

			if( project.save_to != null && project.save_to.length() > 0) {
				String filename = project.save_to;
				String ext = project.save_to.substring(project.save_to.length()-3).toLowerCase();
				if( ext.equals("dbf")) {
					writeDBF(filename,headers,data);					
					System.out.println("writedbf done.");
				} else {
					String delimiter = ",";
					if( ext.equals("txt")) delimiter = "\t";
					try {
						System.out.println("creating...");
						File f = new File(filename);
						FileOutputStream fos = new FileOutputStream(f);
						StringBuffer sb = new StringBuffer();
						for(int i = 0; i < headers.length; i++) {
							sb.append((i>0?delimiter:"")+headers[i]);
						}
						sb.append("\n");
						
						for(int j = 0; j < data.length; j++) {
							for(int i = 0; i < headers.length; i++) {
								sb.append((i>0?delimiter:"")+data[j][i]);
							}
							sb.append("\n");
						}
						System.out.println("writing...");
						fos.write(sb.toString().getBytes());
						
						fos.flush();
						System.out.println("closing...");
						fos.close();
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(mainframe,"Save failed!\nDo you have the file open in another program?");
						return;
					} 
				}
			}
			if( Applet.no_gui) {
				System.exit(0);
			}
		}
	}	
	public void stopEvolving() {
		featureCollection.ecology.stopEvolving();
		evolving = false;
		setEnableds();
		goButton.setEnabled(true);
		stopButton.setEnabled(false);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
	}
}