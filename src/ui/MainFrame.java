package ui;

import geography.*;
import geography.Properties;
import new_metrics.Metrics;
import solutions.*;
import ui.MainFrame.OpenShapeFileThread;
import util.*;
import util.GenericClasses.BiMap;
import util.GenericClasses.Quadruplet;
import util.GenericClasses.Triplet;

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
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*; 
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;

import org.nocrala.tools.gis.data.esri.shapefile.*;
import org.nocrala.tools.gis.data.esri.shapefile.header.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.*;

import dbf.*;
/*
import com.hexiong.jdbf.DBFReader;
import com.hexiong.jdbf.DBFWriter;
import com.hexiong.jdbf.Exception;
import com.hexiong.jdbf.DBField;
*/

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
	public PanelFeatures featuresPanel = new PanelFeatures();
	public JProgressBar progressBar = new JProgressBar();
	public FrameSeatsVotesChart frameSeatsVotesChart = new FrameSeatsVotesChart();
	public InstructionProcessor ip = new InstructionProcessor();
	public FramePieCharts pie = new FramePieCharts();
	
	public ButtonGroup seatsModeBG = new ButtonGroup();
	public JButton btnInit = new JButton("Init");
	public JComboBox comboBoxInitMethod = new JComboBox();

	 
	

	//public Ecology ecology = new Ecology();
	//========MODELS
	public FeatureCollection featureCollection = new FeatureCollection();
	public Project project = new Project();
	public DemographicSet activeDemographicSet = new DemographicSet();
	public JRadioButton lblMembersPerDistrict;
	
	public ButtonGroup groupColoringMode = new ButtonGroup();

	
	//========PRIMITIVES

	boolean geo_loaded = false;
	boolean census_loaded = false;
	boolean election_loaded = false;
	public boolean evolving = false;
	
	boolean suppress_duplicates = false;
	boolean use_sample = false;
	
	public boolean hushcomboBoxPopulation = false;
	public boolean hushcomboBoxPrimaryKey = false;
	public boolean hushcomboBoxDistrict = false;
	public boolean hushcomboBoxCounty = false;

	double mutation_rate_multiplier = 0.1;
	public static double boundary_mutation_rate_multiplier = 2.0;//0.2;
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
	JRadioButtonMenuItem mntmColorDistDemo;
	JRadioButtonMenuItem mntmColorPartisanPacking;
	JRadioButtonMenuItem mntmColorRacialPacking;
	JMenuItem mntmExportToHtml;
	public ButtonGroup selectionType = new ButtonGroup();
	JRadioButtonMenuItem mntmShowVoteBalance = new JRadioButtonMenuItem("Color by vote balance");;
	JRadioButtonMenuItem mntmShowDemographics = new JRadioButtonMenuItem("Color by demographic");;
	JCheckBoxMenuItem chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
	JCheckBoxMenuItem chckbxmntmHideMapLines = new JCheckBoxMenuItem("Show map lines");
	JCheckBoxMenuItem chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
	JCheckBoxMenuItem chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
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
	private JMenu mnHelp_1;
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
    public JTextField evolutionPopulationTF;
	public JTextField textFieldSeatsPerDistrict;

	public JSlider slider_mutation;
	public JSlider sliderDisconnected;
	public JSlider sliderBorderLength;
	public JSlider sliderPopulationBalance;
	public JSlider sliderRepresentation;
	
	public JLabel lblDistrictColumn;
	
	public JButton goButton = new JButton();
	public JButton stopButton = new JButton();
	public JMenuItem mntmOpenProjectFile_1;
	public JPanel panel_4;
	public JSeparator separator_1 = new JSeparator();
	public JSeparator separator_2 = new JSeparator();
	public JMenuItem mntmImportAggregate = new JMenuItem("Import & aggregate custom");
	public JMenuItem mntmExportAndDeaggregate = new JMenuItem("Export and de-aggregate custom");
	public JMenuItem mntmOpenWktTabdelimited = new JMenuItem("Open WKT tab-delimited");
	
	
	/**
	 * Returns the class name of the installed LookAndFeel with a name
	 * containing the name snippet or null if none found.
	 * 
	 * @param nameSnippet a snippet contained in the Laf's name
	 * @return the class name if installed, or null
	 */
	public static String getLookAndFeelClassName(String nameSnippet) {
	    LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
	    for (LookAndFeelInfo info : plafs) {
	        if (info.getName().contains(nameSnippet)) {
	            return info.getClassName();
	        }
	    }
	    return null;
	}

	//=========CLASSES
	class FileThread extends Thread {
    	public File f = null;
    	FileThread() { super(); } 
    	FileThread(File f) {
    		super();
    		this.f = f;
    	}
    	
	}
	public void downloadNextState() {
		try {
			Download.istate++;
			while( Download.istate < Download.states.length && Download.states[Download.istate].length() == 0) {
				Download.istate++;
			}		
			System.out.println("downloadNextState run "+Download.istate+" "+Download.states[Download.istate]+"...");
			//JOptionPane.showMessageDialog(null,"hi");
			if( Download.istate >= Download.states.length ) {
				Download.prompt = true;
				Download.downloadAll = false;
				JOptionPane.showMessageDialog(null, "Done downloading all states!");
				return;
			}
			
			downloadState();

		} catch (Exception ex) {
			System.out.println("ex in cyclehtread "+ex);
			ex.printStackTrace();
		}

	}
	
	public void downloadState() {	
		Download.initPaths();
		OpenShapeFileThread ost = new OpenShapeFileThread(Download.vtd_file);
		ImportCensus2Thread ir = new ImportCensus2Thread();
		
		ir.nextThread = new MergeInDemoAndElection();
		ost.nextThread = ir;
		Download.nextThread = ost;
		
		System.out.println("starting "+Download.states[Download.istate]+"...");
		Download.downloadState(Download.istate,Download.cyear,Download.vyear);	
	}
	
	public void mergeInDemoAndElection() {
		ip.addHistory("MERGE");
		new MergeInDemoAndElection().start();
	}
	public void importCountyData() {
		ip.addHistory("IMPORT COUNTY");
		ImportCountyLevel ivtd = new ImportCountyLevel();	
		ivtd.nextThread = new ThreadFinishImport();
		ivtd.start();
	}
	public void importTranslations() {
		ip.addHistory("IMPORT TRANSLATIONS");
		ImportTranslations ivtd = new ImportTranslations();	
		ivtd.nextThread = new ThreadFinishImport();
		ivtd.start();
	}
	public void importBlockElection() {
		ip.addHistory("IMPORT ELECTIONS");
		ImportFeatureLevel ivtd = new ImportFeatureLevel();	
		ivtd.nextThread = new ThreadFinishImport();
		ivtd.start();
	}
	
	public void importBlockDemographics() {
		ip.addHistory("IMPORT DEMOGRAPHICS");
		ImportDemographics ivtd = new ImportDemographics();	
		ivtd.nextThread = new ThreadFinishImport();
		ivtd.start();
	}
	
	public void importBlockBdistricting() {
		ip.addHistory("IMPORT BDISTRICTING");
		Download.prompt = false;
		importBlocksBdistricting();	
	}
	
	public void importBlockCurrentDistricts() {
		ip.addHistory("IMPORT CURRENT_DISTRICTS");
		Download.prompt = false;
		importBlocksCurrentDistricts();		
	}
	
	class MergeInDemoAndElection extends Thread {
		public void run() {
			ImportTranslations it = new ImportTranslations();
			ImportCountyLevel icl = new ImportCountyLevel();
			ImportFeatureLevel ivtd = new ImportFeatureLevel();
			ImportDemographics idg = new ImportDemographics();
			
			it.nextThread = icl;
			icl.nextThread = ivtd;
			ivtd.nextThread = idg;
			idg.nextThread = new ThreadFinishImport();
			
			it.start();
		}
	}
	class ThreadFinishImport extends Thread {
		public void run() {
			trySetGroupColumns();

			
			Download.makeDoneFile();
			dlbl.setText("Saving...");
			saveData(Download.vtd_dbf_file, 2,false);
			dlg.hide();
			
			if( Download.prompt) {
				JOptionPane.showMessageDialog(null, "Import complete.");
			}
			if( Download.exit_when_done) {
				System.exit(0);
			}
			if( Download.downloadAll) {
				downloadNextState();
				
			}
			ip.eventOccured();
		}

	}
	class ThreadFinishThreads extends Thread {
		public void run() {
			trySetGroupColumns();
			dlg.hide();
			if( Download.exit_when_done) {
				System.exit(0);
			}
			ip.eventOccured();
		}

	}
	
	class CycleThread extends Thread {
		public int cyear;
		public int eyear;
		int state = 0;
		
		public void run() {
			try {
			System.out.println("CycleThread run "+state+" "+Download.states[state]+"...");
			//JOptionPane.showMessageDialog(null,"hi");
			if( state >= Download.states.length ) {
				Download.prompt = true;
				JOptionPane.showMessageDialog(null, "Done downloading all states!");
				return;
			}
			int next_state = state+1;
			while( next_state < Download.states.length && Download.states[next_state].length() == 0) {
				next_state++;
			}
			Download.initPaths();
			OpenShapeFileThread ost = new OpenShapeFileThread(Download.vtd_file);
			ImportCensus2Thread ir = new ImportCensus2Thread();
			MergeInDemoAndElection it = new MergeInDemoAndElection();
			//ImportCountyLevel icl = new ImportCountyLevel();
			CycleThread ct = new CycleThread();
			ct.cyear = cyear;
			ct.eyear = eyear;
			ct.state = next_state;		
			if( ct == null) {
				JOptionPane.showMessageDialog(null,"ct is null!");
			}
			//icl.nextThread = ct;
			//it.nextThread = icl;
			ir.nextThread =  it;
			ost.nextThread = ir;
			Download.nextThread = ost;

			System.out.println("starting "+Download.states[state]+"...");
			Download.downloadState(state,cyear,eyear);	
			
			} catch (Exception ex) {
				System.out.println("ex in cyclehtread "+ex);
				ex.printStackTrace();
			}
		}
	}

	class ExportCustomThread extends Thread {
		public File censusdbf_file;
		public File foutput;
		public boolean bdivide;
		public boolean bdivide_choosen = false;
		public Vector<String> vselected = null;
		
		FileOutputStream fos = null;
		String delimiter = "\t";
		public Thread nextThread;

		ExportCustomThread() { super(); }
		public void init() {
			if( censusdbf_file == null) {
				JOptionPane.showMessageDialog(mainframe, "Select the .dbf file with census block-level data.\n");
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbf file","dbf"));
				jfc.showOpenDialog(null);
				censusdbf_file = jfc.getSelectedFile();
			}
			if( censusdbf_file == null)  {
				return;
			}
			String fn = censusdbf_file.getName();
			String ext = fn.substring(fn.length()-3).toLowerCase();
			if( !ext.equals("dbf")) {
				JOptionPane.showMessageDialog(null, "File format not recognized.");
				return;
			}
			if( foutput == null) {
				JOptionPane.showMessageDialog(mainframe, "Select the output file.\n");
				JFileChooser jfc2 = new JFileChooser();
				jfc2.setCurrentDirectory(new File(Download.getStartPath()));
				jfc2.addChoosableFileFilter(new FileNameExtensionFilter("csv file","csv"));
				jfc2.showSaveDialog(null);
				foutput = jfc2.getSelectedFile();
			}
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
			if( vselected == null) {
				DialogSelectLayers dlgselect;
				dlgselect = new DialogSelectLayers();
				dlgselect.setData(featureCollection,new Vector<String>());
				dlgselect.show();
				if( !dlgselect.ok) {
					//if( is_evolving) { featureCollection.ecology.startEvolving(); }
					return;
				}
				vselected = dlgselect.in;
			
			}
			//(select wether to put number as is or divide by points
			if( !bdivide_choosen) {
				bdivide = JOptionPane.showConfirmDialog(mainframe, "Divide values by number of points?") == JOptionPane.YES_OPTION;
			}
			this.start();
		}

		

    	public void run() { 
    		try {
    			System.out.println("export start");
	    		dlg.setVisible(true);
	    		dlbl.setText("Loading file...");
	    		

				String dbfname = censusdbf_file.getAbsolutePath();
				//count number of points in each precinct
	    		double[] points = getCounts(dbfname,bdivide);
 
    			System.out.println("read start");
	
				DBFReader dbfreader = null;
				try {
					dbfreader = new DBFReader(dbfname);
				} catch (Exception e1) {
					e1.printStackTrace();
	    			System.out.println("read exception");

					//return;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_lat = -1;
				int col_lon = -1;
				int col_geoid = -1;
				
				featureCollection.header_data = new HashMap<String,Quadruplet<String,Integer,Integer,Byte>>();
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).name;
					featureCollection.header_data.put(
						dh.header[i],
						new Quadruplet<String,Integer,Integer,Byte>(
							dh.header[i],
							dbfreader.getField(i).length,
							dbfreader.getField(i).decimalCount,
							(byte)dbfreader.getField(i).type
						)
					);

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
	    		for( VTD feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				//Feature.compare_centroid = true;
				//Collections.sort(featureCollection.features);
	    		

    			String s0 = "GEOID"+delimiter+"INTPTLAT"+delimiter+"INTPTLON";
    			
    			for(int i = 0; i < vselected.size(); i++) {
    				s0 += delimiter+vselected.get(i);
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
			    		
			    		VTD feat = getHit(dlon,dlat);
			    		
			    		
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");
			    		} else {
			    			String s = ""+geoid+delimiter+dlat+delimiter+dlon;
			    			for(int i = 0; i < vselected.size(); i++) {
			    				try {
			    					String key = vselected.get(i);
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
						    					s += delimiter+(((Double)str)/points[feat.id]);
						    				} else {
				    							double d = Double.parseDouble(str.toString())/points[feat.id];
				    							s += delimiter+d;
						    				}
		    							} catch (Exception ex) {
		    								ex.printStackTrace();
		    							}
		    						}
			    				} catch (Exception ex) {
			    					System.out.println("ex aa: "+ex);
			    					ex.printStackTrace();
			    					//System.exit(0);
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
	    		if( Download.prompt) {
	    			JOptionPane.showMessageDialog(mainframe,"Done exporting to block level.\n"+foutput.getAbsolutePath());
	    		}
	    		if( nextThread != null) {
	    			nextThread.start();
	    		}
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
				} catch (Exception e1) {
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
					dh.header[i] = dbfreader.getField(i).name;
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
				for( VTD feat : featureCollection.features) {
					feat.geometry.makePolysFull();
				}
				
				dlbl.setText("Doing hit tests...");
				//Feature.compare_centroid = true;
				//Collections.sort(featureCollection.features);
		
		
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
			    		
			    		VTD feat = getHit(dlon,dlat);
			    		
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" "+geoid+" ");

			    		} else {
			    			points[feat.id]++;
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
				} catch (Exception e1) {
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
					dh.header[i] = dbfreader.getField(i).name;
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
	    		for( VTD feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				//Feature.compare_centroid = true;
				//Collections.sort(featureCollection.features);


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
			    		
			    		VTD feat = getHit(dlon,dlat);
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println("miss "+dlon+","+dlat+" ");
			    		} else {
	    					feat.population += pop18;
	    					feat.has_census_results = true;
			    			
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

			    
	    		for( VTD feat : featureCollection.features) {
				    feat.properties.put("POPULATION",feat.population);
				    feat.properties.POPULATION = (int) feat.population;
	    		}
	    		System.out.println("setting pop column");
	    		
	    		comboBoxPopulation.addItem("POPULATION");
			    setPopulationColumn("POPULATION");
	    		comboBoxPopulation.setSelectedItem("POPULATION");
	    		
	    		dlg.setVisible(false);
	    		if( Download.prompt) {
	    			JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
	    		}
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
	    		for( VTD feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				//Feature.compare_centroid = true;
				//Collections.sort(featureCollection.features);
	    		
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
			    		
		    			VTD feat = getHit(dlon,dlat);
				    	if( feat == null) {
				    		System.out.println();
				    		System.out.println("miss "+dlon+","+dlat+" ");
				    	} else {
	    					feat.population += Integer.parseInt(ss[col_pop]);
	    					feat.has_census_results = true;
				    	}
				    } catch (Exception ex) {
				    	ex.printStackTrace();
				    }
			    }

    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    		
    		dlbl.setText("Finalizing...");
		    
    		for( VTD feat : featureCollection.features) {
			    feat.properties.put("POPULATION",feat.population);
			    feat.properties.POPULATION = (int) feat.population;
    		}
    		System.out.println("setting pop column");
    		
    		comboBoxPopulation.addItem("POPULATION");
		    setPopulationColumn("POPULATION");
    		
    		dlg.setVisible(false);
    		if( Download.prompt) {
    			JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
    		}
    	}
	}

	class ImportCensus2Thread extends Thread {
		protected Thread nextThread;
		ImportCensus2Thread() { super(); }
    	public void run() { 
    		dlbl.setText("Loading population...");
    		importBlockDataDBF(Download.census_pop_file.getAbsolutePath(),"POPULATION","POP",false);
    	}
    	public void importBlockDataDBF(String path, String column, String source_column, boolean MAJORITY_VOTE) {
    		try {
	    		dlg.setVisible(true);
	    		Hashtable<String,String> hash_population = new Hashtable<String,String>();
	    		
				
				DBFReader dbfreader;

				try {
					dbfreader = new DBFReader(path);
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
				DataAndHeader dh = new DataAndHeader();
				
				int col_pop = -1;
				int col_geoid_pop = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).name;
					System.out.println(dh.header[i]+" ");
					if( dh.header[i].toUpperCase().trim().indexOf(source_column) == 0) {
						col_pop = i;
					}
					if( dh.header[i].toUpperCase().trim().indexOf("BLOCKID") == 0 || dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0) {
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
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}

			    
				int col_lat = -1;
				int col_lon = -1;
				int col_geoid_centroid = -1;
				
	    		dlbl.setText("Reading header...");

				dh.header = new String[dbfreader.getFieldCount()];
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = dbfreader.getField(i).name;
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
	    		for( VTD feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.population = 0;
	    		}
	    		
	    		dlbl.setText("Doing hit tests...");
				//Feature.compare_centroid = true;
				//Collections.sort(featureCollection.features);
				/*
				for(Feature f : featureCollection.features) {
					System.out.println("centroid: "+f.geometry.full_centroid[0]+","+f.geometry.full_centroid[0]);
				}
				JOptionPane.showConfirmDialog(null, "continue.");
				*/


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
			    		
			    		VTD feat = getHit(dlon,dlat);
			    		if( feat == null) {
			    			System.out.print("x");
				    		System.out.println("miss "+dlon+","+dlat+" "+ilat+","+ilon);
			    		} else {
	    					feat.population += pop18;
	    					feat.has_census_results = true;
			    			
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
	    		
				VTD.compare_centroid = false;
				Collections.sort(featureCollection.features);


			    
	    		for( VTD feat : featureCollection.features) {
				    feat.properties.put("POPULATION",feat.population);
				    feat.properties.POPULATION = (int) feat.population;
	    		}
	    		System.out.println("setting pop column");
	    		
	    		comboBoxPopulation.addItem("POPULATION");
			    setPopulationColumn("POPULATION");
	    		
	    		dlg.setVisible(false);
	    		if( Download.prompt) {
	    			JOptionPane.showMessageDialog(mainframe,"Done importing census data.\nHits: "+hits+"\nMisses: "+misses);
	    		}
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
			VTD.compare_centroid = false;
			Collections.sort(featureCollection.features);
			
    		trySetBasicColumns();
    		trySetGroupColumns();
    		
			if( nextThread != null) {
				nextThread.start();
				nextThread = null;
			}

    	}
	}

	// ask what file type
	//ask whether to accumulate or use first match
	//if first match, ask whether to convert from 0 to 1 indexed, vice-versa, or none.
	//or just do it anyways?
	
	public int hits = 0;
	public int misses = 0;
	public JSeparator separator_3 = new JSeparator();
	public JMenuItem mntmAntialiasingOff = new JMenuItem("Antialiasing off");
	public JMenuItem mntmxAntialiasing = new JMenuItem("2x antialiasing");
	public JMenuItem mntmxAntialiasing_1 = new JMenuItem("4x antialiasing");
	public JSlider sliderWastedVotesTotal = new JSlider();
	public JLabel lblWastedVotes = new JLabel("Competitiveness (victory margin)");
	public JCheckBoxMenuItem chckbxmntmMutateExcessPopOnly;
	public JCheckBoxMenuItem chckbxmntmMutateCompactness;
	public JMenu mnConstraints;
	public JMenuItem mntmWholeCounties;
	public JLabel lblGeometricFairness;
	public JSlider sliderBalance;
	public JSeparator separator_4;
	public JMenuItem mntmNoMap;
	public JMenuItem mntmOneMap;
	public JMenuItem mntmFourMaps;
	public JMenuItem mntmNineMaps;
	public JMenuItem mntmSixteenMaps;
	public JSeparator separator_5;
	public JSeparator separator_6;
	public JRadioButtonMenuItem mntmColorByDistrict;
	public JRadioButtonMenuItem mntmColorByPop;
	public JRadioButtonMenuItem mntmColorByVote;
	public JRadioButtonMenuItem mntmColorByCompactness;
	public JLabel lblElitism;
	public JSlider sliderElitism;
	public JRadioButton rdbtnTruncationSelection;
	public JRadioButton rdbtnRankSelection;
	public JRadioButton rdbtnRouletteSelection;
	public JRadioButtonMenuItem mntmColorByWasted = new JRadioButtonMenuItem("Color by wasted votes");
	public JMenuItem mntmWizard = new JMenuItem("Download vtd shapefile & population from census.gov...");
	public JSeparator separator_7 = new JSeparator();
	public JMenuItem mntmHarvardElectionData;
	public JMenuItem mntmDescramble = new JMenuItem("descramble");
	public JMenuItem mntmShowSeats = new JMenuItem("Show seats / votes");
	public JMenuItem mntmShowRankedDistricts = new JMenuItem("Show ranked districts");
	public JLabel lblSeatsVotes;
	public JSlider sliderSeatsVotes;
	public JButton btnSubstituteColumns;
	public JCheckBox chckbxSubstituteColumns;
	public JMenu mnWindows = new JMenu("Windows");
	public JButton btnElection2Columns = new JButton("Election 2 columns");
	public JCheckBox chckbxSecondElection = new JCheckBox("Second election");
	public JMenuItem mntmPublicMappingProject;
	public JPanel panel_5;
	public JLabel srlblSplitReduction;
	public JSlider sliderSplitReduction;
	public JComboBox srcomboBoxCountyColumn;
	public JLabel srlblCountyColumn;
	public JCheckBox chckbxReduceSplits;
	public JLabel lblFairnessCriteria = new JLabel("Fairness criteria");
	public JButton btnEthnicityColumns;
	private boolean hushcomboBoxCountyColumn;
	public JLabel lblRacialVoteDilution = new JLabel("Racial vote dilution");
	public JSlider sliderVoteDilution = new JSlider();
	public JTextField textFieldTotalSeats = new JTextField();
	public JRadioButton lblTotalSeats = new JRadioButton("Total seats");
	VTD getHit(double dlon, double dlat) {
		int ilat = (int)(dlat*Geometry.SCALELATLON);
		int ilon = (int)(dlon*Geometry.SCALELATLON);
		dlon *= Geometry.SCALELATLON;
		
		double min_x = 0;
		double max_x = featureCollection.sortedFeatures.size()-1;
		double min_lon = featureCollection.sortedFeatures.get((int)min_x).geometry.full_centroid[0];
		double max_lon = featureCollection.sortedFeatures.get((int)max_x).geometry.full_centroid[0];
		//System.out.println("min_lon "+min_lon);
		//System.out.println("dlon "+dlon);
		//System.out.println("max_lon "+max_lon);
		
		int itestx = 0;
		for( int i = 0; i < 250; i++) {
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
			if( itestx >= featureCollection.sortedFeatures.size()) {
				itestx = featureCollection.sortedFeatures.size()-1;
			}
			if( itestx < 0) {
				itestx = 0;
			}
			double test_lon = featureCollection.sortedFeatures.get(itestx).geometry.full_centroid[0];
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
		for( int i = 0; i < featureCollection.sortedFeatures.size(); i++ ) {
			if(  itestx+i < featureCollection.sortedFeatures.size()) {
				VTD testfeat = featureCollection.sortedFeatures.get(itestx+i);
				for( int j = 0; j < testfeat.geometry.polygons_full.length; j++ ) {
					if( testfeat.geometry.polygons_full[j].contains(ilon,ilat)) {
						hits++;
						return testfeat;
					}
				}
			}
			if( itestx-i >= 0) {
				VTD testfeat = featureCollection.sortedFeatures.get(itestx-i);
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
		for( VTD feat : featureCollection.sortedFeatures) {
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
		int opt = 0;
		int ACCUMULATE = 0;
		int OVERWRITE = 1;
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
			String[] options = new String[]{"Accumulate","Majority vote"};
			opt = JOptionPane.showOptionDialog(mainframe, "Accumulate values or majority vote?", "Select option", 0,0,null,options,options[0]);
			if( opt < 0) {
				System.out.println("aborted.");
				return;
			}
			
			start();
		}
    	public void run() {
    		boolean CONTAINS_HEADER = false; 
    		boolean MAJORITY_VOTE = false;
   			System.out.println("importBlockData0 "+f.getAbsolutePath());
   		    		
    		importBlockData( f.getAbsolutePath().trim(), true, opt == 1, new String[]{"IMPORTED"}, new String[]{"IMPORTED"});
    	}
    	public void importBlockData(String fn, boolean CONTAINS_HEADER, boolean MAJORITY_VOTE, String[] source_column_names,String[] dest_column_names) {
			boolean one_indexed = true;
			Object[] col_names = null;
    		try {
    			System.out.println("importBlockData "+fn);
    			opt = MAJORITY_VOTE ? 1 : 0;
    			f = new File(fn);
				//String fn = f.getName().trim();
				String ext = fn.substring(fn.length()-3).toLowerCase();
				DataAndHeader dh = new DataAndHeader();

	    		dlg.setVisible(true);
	    		dlbl.setText("Making polygons...");
	    		System.out.println("making polygons");
				int count = 0;
				
	    		for( VTD feat : featureCollection.features) {
	    			feat.geometry.makePolysFull();
					feat.population = 0;
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
					if( !CONTAINS_HEADER) {
						dh.header = new String[source_column_names.length+1];
						dh.header[0] = "GEOID";
						for( int i = 0; i < source_column_names.length; i++) {
							dh.header[i+1] = source_column_names[i];
						}
					} else {
						dh.header = br.readLine().split(delimiter);
					}
					
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
					

					
		    		dlg.setVisible(true);
					col_names = dsc.in.toArray();
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
					//Feature.compare_centroid = true;
		    		//Collections.sort(featureCollection.features);
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
	    					for( VTD feat : featureCollection.features) {
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
					    		
				    			VTD feat = getHit(dlon,dlat);
						    	if( feat == null) {
						    		System.out.println();
						    		System.out.println("miss "+dlon+","+dlat+" ");
						    	} else {
			    					if( opt == OVERWRITE) { //overwrite
			    						String dist = ss[col_indexes[0]];
			    						//ignore zz's
			    						if(dist.trim().equals("ZZ")) {
			    							continue;
			    						}
			    						if( dist.trim().equals("0")) {
			    							one_indexed = false;
			    						}
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
    						for( VTD feat : featureCollection.features) {
    							if( feat.properties.temp_hash.size() == 0) {
    								System.out.println("no values");
    								//feat.properties.put(key,"1");
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
					
					Vector<VTD> missed = new Vector<VTD>();
					if( opt == OVERWRITE) {
						for( VTD feat : featureCollection.features) {
							boolean found = feat.setDistFromPoints(key,false);//one_indexed);
							if( !found) {
								missed.add(feat);
							}
						}
						
						//check for zeros, if none, decrement all.
						boolean found_zero = false;
						for( VTD feat : featureCollection.features) {
							if( feat.get(key) == null) {
								continue;
							}
							int ndx = Integer.parseInt(feat.get(key).toString());
							if( ndx == 0) {
								found_zero = true;
								break;
							}
						}
						if( !found_zero && false) {
							for( VTD feat : featureCollection.features) {
								if( feat.get(key) == null) {
									continue;
								}
								int ndx = Integer.parseInt(feat.get(key).toString());
								feat.put(key,""+(ndx-1));
							}
						}
					}
					try {
						
						//fill in empties with neighbors
						Hashtable<String,Integer> counts = new Hashtable<String,Integer>();
						int l = 0;
						System.out.println(""+missed.size()+" missing");
						while (l < 5 && missed.size() > 0) {
							System.out.println(""+missed.size()+" missing");
							for( int k = 0; k < missed.size(); k++) {
								VTD feat = missed.get(k);
								counts.clear();
								for(VTD vtd : feat.neighbors) {
									String p = vtd.feature.properties.get(key).toString();
									Integer i = counts.get(p);
									if( i == null) {
										i = new Integer(0);
										counts.put(p, i);
									}
									i++;
								}
								String best = "";
								int max = 0;
								if( counts.entrySet().size() == 0) {
									System.out.println("no neighbors found! "+l);
								} else {
									missed.remove(feat);
									k--;
									for( Entry<String,Integer> entries : counts.entrySet()) {
										if( entries.getValue() > max) {
											max = entries.getValue();
											best = entries.getKey();
										}
									}
									feat.properties.put(key,best);
								}
							}
							l++;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
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
						for( VTD feat : featureCollection.features) {
							feat.setDistFromPoints(key,false);//,one_indexed);
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
					dh = FileUtil.readDBF(f.getAbsolutePath());
					
					String dbfname = f.getAbsolutePath();
					
					DBFReader dbfreader;
					try {
						dbfreader = new DBFReader(dbfname);
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					
		    		dlbl.setText("Reading header...");

					dh.header = new String[dbfreader.getFieldCount()];
					for( int i = 0; i < dh.header.length; i++) {
						dh.header[i] = dbfreader.getField(i).name;
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
					col_names = dsc.in.toArray();
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
					//Feature.compare_centroid = true;
		    		//Collections.sort(featureCollection.features);

		    		
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
	
			    			VTD feat = getHit(dlon,dlat);
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
    		if( opt == 1) {
    			for( int i = 0; i < col_names.length; i++) {
    				featureCollection.fixDistrictAssociations((String)col_names[i]);
    			}	
    		}
    	}
	}
	public void exportBlockData(String string) {//, boolean equals) {
		ip.addHistory("EXPORT BLOCKS");
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
			/*
			JFileChooser jfc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Dbase file", "dbf");
			jfc.setFileFilter(filter);
			jfc.showOpenDialog(null);
			File fd = jfc.getSelectedFile();
			if( fd == null) {
				return;
			}*/
			//ExportToBlockLevelThread exp = new ExportToBlockLevelThread();
			/*
			 		File f;
		File foutput;
		boolean bdivide;
		DialogSelectLayers dlgselect;
		FileOutputStream fos = null;
		String delimiter = "\t";
		public ThreadFinishThreads nextThread;

			 */

			/*
			public File censusdbf_file;
			public File foutput;
			public boolean bdivide;
			public boolean bdivide_choosen = false;
			public Vector<String> vselected = null;
			 */
			Download.downloadAndExtractCentroids();
			ExportCustomThread exp = new ExportCustomThread();
			//Download.nextThread = exp;
			exp.nextThread = new ThreadFinishThreads();
			//Download.census_centroid_file  
			Download.initPaths();
			exp.censusdbf_file = Download.census_centroid_file;
			String output = Download.getStartPath()+"blocks.txt";
			System.out.println("input: "+exp.censusdbf_file);
			System.out.println("output: "+exp.foutput);
			exp.foutput = new File(output);
			exp.bdivide = false;
			exp.bdivide_choosen = true;
			exp.vselected = new Vector<String>();
			exp.vselected.add(Applet.mainFrame.project.district_column);
			exp.init();
		} else {
			JOptionPane.showMessageDialog(MainFrame.mainframe,"You must select a district column first.");
		}
	}

	public void open(File f) {
		OpenShapeFileThread ot = new OpenShapeFileThread(f);
		ot.nextThread = new ThreadFinishThreads();
		ot.start();
	}

	public class OpenShapeFileThread extends FileThread {
		OpenShapeFileThread(File f) { super(f);  }
		Thread nextThread = null;
    	public void run() { 
    		if( f == null) { 
    			System.out.println("OpenShapeFileThread null file, getting from Download class... ");
    			Download.initPaths(); 
    			this.f = Download.vtd_file; 
    		} else {
				ip.addHistory("OPEN \""+f.getAbsolutePath()+"\"");
			}
    		try {
    		    dlbl.setText("Loading file "+f.getName()+"...");

    		    project.source_file = f.getAbsolutePath();

	    		dlg.setVisible(true);
	    		
				featureCollection = new FeatureCollection(); 
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}
	
				featureCollection.features = new Vector<VTD>();
				HashMap<String,VTD> hmFeatures = new HashMap<String,VTD>();
	
				String s = f.getName().toLowerCase();
				System.out.println("Processing "+s+"...");
				StringBuffer sb = getFile(f);
				
				stopEvolving();
				geo_loaded = false;
				evolving = false;
				VTD.display_mode = 0;
				setEnableds();
				
				//FeatureCollection fc = new FeatureCollection();
				if( panelStats != null) {
					panelStats.featureCollection = featureCollection;
				}
				loadShapeFile(f);
	
	
				for( VTD fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				finishLoadingGeography();
				if( Download.istate < 0) {
					setTitle("Automatic Redistricter");
				} else {
					setTitle("Automatic Redistricter - "+Download.states[Download.istate]+" ("+Download.cyear+")");
				}
				if( nextThread != null) {
					nextThread.start();
					nextThread = null;
				} else {
					ip.eventOccured();
				}

    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    		trySetBasicColumns();
    		trySetGroupColumns();

    	}
    }
	
	class ImportTranslations extends Thread {
		Thread nextThread = null;
		public void run() {
			dlg.show();
			dlbl.setText("Adding translations...");
			VTD.compare_centroid = false;
			Collections.sort(featureCollection.features);

			Hashtable<String,VTD> featmap = new Hashtable<String,VTD>();
			String skey="GEOID";
			String[] headers = featureCollection.getHeaders();
			for( int i = 0; i < headers.length; i++) {
				if( headers[i].indexOf(skey) == 0) {
					skey = headers[i];
					break;
				}
			}
			String skey2="VTDST";
			for( int i = 0; i < headers.length; i++) {
				if( headers[i].indexOf(skey2) == 0) {
					skey2 = headers[i];
					break;
				}
			}
			int ikey = 9;
			Download.init();
			String state_abbr = Download.state_to_abbr.get(Download.states[Download.istate]);
			System.out.println("state_abbr: |"+state_abbr+"|");
			for(VTD feat : featureCollection.features) {
				featmap.put((String) feat.properties.get(skey),feat);
			}
			Vector<String[]> v = processFeaturerenameFile();
			System.out.println("size: "+v.size());
			String[] header = v.remove(0);
			int[] care = new int[]{3,5,6};
			int matches = 0;
			int non_matches = 0;
			for(String[] ss : v) {
				if( !ss[0].equals(state_abbr)) {
					//System.out.print(".");
					continue;
				}
				VTD feat = featmap.get(ss[ikey]);
				if( feat == null) {
					if( ss[ikey].length() >= 6) {
						ss[ikey] = ss[ikey].substring(3);
					}
					feat = featmap.get(ss[ikey]);
					if( feat == null) {
						System.out.println("no match found!: "+ss[ikey]);
						non_matches++;
						continue;
					}
				}
				matches++;
				//System.out.println("match found!: "+ss[ikey]);
				for( int j = 0; j < care.length; j++) {
					int k = care[j];
					feat.properties.put(header[k],ss[k]);
				}
			}
			System.out.println("translations matches: "+matches+" non_matches: "+non_matches);
			System.out.println("done nextThred: "+nextThread);
			if( nextThread != null) {
				nextThread.start();
				nextThread = null;
			}
		}
	}

	class ImportCountyLevel extends Thread {
		public Thread nextThread;
		public void run() {
			dlbl.setText("Importing county data...");

			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			}
			VTD.compare_centroid = false;
			Collections.sort(featureCollection.features);

			System.out.println("import county level start");
			//String path = "ftp://autoredistrict.org/pub/county_level_stats/Merged%20--%20"+Download.states[Download.istate]+".txt";
			String path = "http://autoredistrict.org/county_level_stats/Merged%20--%20"+Download.states[Download.istate].replaceAll(" ", "%20")+".txt";
			System.out.println("url: "+path);
	    	System.out.println("0");
		    URL url;
		    InputStream is = null;
	    	System.out.println("0.0");

		    BufferedReader br;
		    String line;
	    	System.out.println("0.1");

	        Vector<String[]> v = new Vector<String[]>();

		    try {
		    	System.out.println("1");
		        url = new URL(path);
		    	System.out.println("2");
		        is = url.openStream();  // throws an IOException
		    	System.out.println("3");
		        br = new BufferedReader(new InputStreamReader(is));
		    	System.out.println("4..");

		        while ((line = br.readLine()) != null) {
			    	System.out.print(".");
		        	System.out.println(line);
		        	String[] ss = line.split("\t");
		        	for( int i = 0; i < ss.length; i++) {
		        		ss[i] = ss[i].replaceAll("\"", "").replaceAll(",", "");
		        	}
		        	v.add(ss);
		        }
		    } catch (Exception mue) {
		    	System.out.print("ex "+mue);
		        mue.printStackTrace();
		    }
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			}
			System.out.println("5..");
			
	        String[] headers = v.remove(0);
			//todo: populate v with the data.
			String county_column = "COUNTYFP10";
			String county_column2 = "COUNTY_NAM";
			int iCountyColumn = 0;
			
			//collect counties;
			HashMap<String,Vector<VTD>> county_feats = new HashMap<String,Vector<VTD>>();
			HashMap<String,Double> county_pops = new HashMap<String,Double>();
			for( VTD feat : featureCollection.features) {
				try {
				String county = (String) feat.properties.get(county_column);
				if(county == null) {
					county = (String) feat.properties.get(county_column2);	
				}
				county = county.trim().toUpperCase();
				
				Vector<VTD> vf = county_feats.get(county);
				if( vf == null) { 
					vf = new Vector<VTD>();
					county_feats.put(county,vf);
				}
				vf.add(feat);
				Double i = county_pops.get(county);
				if( i == null) { 
					i = new Double(0);
					county_pops.put(county,i);
				}
				//if( feat.properties.POPULATION == 0) {
				Double d = Double.parseDouble(feat.properties.get(project.population_column).toString());
				feat.properties.POPULATION = d.intValue();
				//}
				i += feat.properties.POPULATION;
				county_pops.put(county,i);
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
			}
			
			//now deaggregate proportional
			System.out.println("deaggregating proportional...");
			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			}

			for( String[] ss : v) {
				String[] tries = new String[]{" COUNTY"," PARISH"," BOROUGH"," CENSUS AREA"," MUNICIPALITY"};
				try {
				String incounty = ss[iCountyColumn+1].trim().toUpperCase();
				Vector<VTD> vf = county_feats.get(incounty);
				if( vf == null) {
					while( incounty.length() > 1 && incounty.substring(0,1).equals("0")) {
						incounty = incounty.substring(1);
					}
					vf = county_feats.get(incounty);
					if( vf == null) {
						incounty = ss[iCountyColumn].trim().toUpperCase();
						vf = county_feats.get(incounty);
						if( vf == null) {
							for( int i = 0; i < tries.length; i++) {
								vf = county_feats.get(incounty+tries[i]);
								if( vf != null) {
									incounty += tries[i];
									break;
								}
							}
							if( vf == null) {
									//System.out.println("not found!: "+incounty);
									continue;
							}
						}
					}
				}
				double total_pop = (double)county_pops.get(incounty);
				//System.out.println("found!: "+incounty+" "+total_pop);
				double[] dd = new double[ss.length];
				for( int i = 0; i < ss.length; i++) {
					if( headers[i].equals("VAP_HISPANIC")) {
						headers[i] = "VAP_HISPAN";
					}
					if( i == iCountyColumn || headers[i].equals("COUNTY_NAME") || headers[i].equals("COUNTY_FIPS")) {
						continue;
					}
					dd[i] = Double.parseDouble(ss[i]);///total_pop;
				}
				double confirm_tot = 0;
				double confirm_tot2 = 0;
				double confirm_tot3 = 0;
				//double[] dd0 = new double[ss.length];
				for(VTD feat : vf) {
					double feat_pop = feat.properties.POPULATION;
					confirm_tot += feat_pop/total_pop;
					for( int i = 0; i < ss.length; i++) {
						if( i == iCountyColumn || headers[i].equals("COUNTY_NAME") || headers[i].equals("COUNTY_FIPS")) {
							continue;
						}
						double add = (dd[i]*feat_pop/total_pop);
						feat.properties.put(headers[i], ""+add);
						if( i == 3) {
							confirm_tot2 += add;
						}
						if( i == 4) {
							confirm_tot3 += add;
						}
					}
				}
				System.out.println("found!: "+incounty+" "+total_pop+" "+confirm_tot+" "
						+dd[3]+" "+confirm_tot2+" "+dd[4]+" "+confirm_tot3+" "
						+ss.length
						+" "+headers[3]+" "+headers[4]
						);

				} catch (Exception ex) {
					System.out.println("ex: "+ex);
					ex.printStackTrace();
				}
			}
			
			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			}
			
			System.out.println("setting columns final...");
			trySetBasicColumns();
			trySetGroupColumns();
			System.out.println("done county data merge");
			if( nextThread == null) {
				System.out.println("no next thread");
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			} else {
				System.out.println("starting next thread");
				nextThread.start();
				nextThread = null;
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
		System.out.println("renumber done.");
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
	
				featureCollection.features = new Vector<VTD>();
				HashMap<String,VTD> hmFeatures = new HashMap<String,VTD>();
				
				stopEvolving();
				geo_loaded = false;
				VTD.display_mode = 0;
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
				    	
						VTD feature = new VTD();
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
	    		
				for( VTD fe : hmFeatures.values()) {
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
				    	
						VTD feature = new VTD();
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
    	    project.fromJson(getFile(f).toString());
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

			featureCollection.features = new Vector<VTD>();
			HashMap<String,VTD> hmFeatures = new HashMap<String,VTD>();

		    dlg.setVisible(true);
	
			String s = f.getName().toLowerCase();
			System.out.println("Processing "+s+"...");
			StringBuffer sb = getFile(f);
			
			stopEvolving();
			geo_loaded = false;
			VTD.display_mode = 0;
			setEnableds();
			
			FeatureCollection fc = new FeatureCollection();
			if( panelStats != null) {
				panelStats.featureCollection = featureCollection;
			}

			try {
				fc.fromJson(sb.toString());
			} catch (Exception ex) {
				System.out.println("ex "+ex);
				ex.printStackTrace();
			}
			for( VTD fe : fc.features) {
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

			for( VTD fe : hmFeatures.values()) {
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
		if( Settings.national_map) {
			System.out.println("national");
			minx = -127.50;
			miny = 28.70;
			maxx = -55.90;
			maxy = 49.10;
			System.out.println(""+minx+","+miny);
			System.out.println(""+maxx+","+maxy);
			featureCollection.recalcDlonlat();
			return;
		}
		Vector<VTD> features = featureCollection.features;

		minx = features.get(0).geometry.coordinates[0][0][0];
		maxx = features.get(0).geometry.coordinates[0][0][0];
		miny = features.get(0).geometry.coordinates[0][0][1];
		maxy = features.get(0).geometry.coordinates[0][0][1];
		HashSet<String> types = new HashSet<String>();
		for( VTD f : features) {
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
		Vector<VTD> features = featureCollection.features;
		System.out.println(features.size()+" precincts loaded.");
	    getMinMaxXY();
		System.out.println("Initializing wards...");
		featureCollection.initFeatures();
	    dlbl.setText("Setting min and max coordinates...");
	    
	    getMinMaxXY();
	
		resetZoom();
		
	    dlbl.setText("Initializing ecology...");
	    try {
	    	//featureCollection.initEcology();
	    	addEcologyListeners();
	    } catch (Exception ex) {
	    	System.out.println("init ecology ex: "+ex);
	    	ex.printStackTrace();
	    }
	    
    	System.out.println("resize ex1");

	    try {
	    	featureCollection.ecology.resize_population();
			//featureCollection.loadDistrictsFromProperties("");
	    } catch (Exception ex) {
	    	System.out.println("resize ex: "+ex);
	    	ex.printStackTrace();
	    }
		System.out.println("filling combo boxes...");
		fillComboBoxes();
		mapPanel.invalidate();
		mapPanel.repaint();
		
    	System.out.println("resize ex2");
	    try {
	    	featureCollection.ecology.resize_population();
			//featureCollection.loadDistrictsFromProperties("");
	    } catch (Exception ex) {
	    	System.out.println("resize ex2: "+ex);
	    	ex.printStackTrace();
	    }
		
		
		setDistrictColumn(project.district_column);
		//featureCollection.loadDistrictsFromProperties(project.district_column);
    	System.out.println("resize ex3");
	    try {
	    	featureCollection.ecology.resize_population();
			//featureCollection.loadDistrictsFromProperties("");
	    } catch (Exception ex) {
	    	System.out.println("resize ex3: "+ex);
	    	ex.printStackTrace();
	    }
		
		mapPanel.featureCollection = featureCollection;
		mapPanel.invalidate();
		mapPanel.repaint();
		//featureCollection.ecology.mapPanel = mapPanel;
		//featureCollection.ecology.statsPanel = panelStats;
    	addEcologyListeners();
    	
    	System.out.println("resize ex4");
	    try {
	    	featureCollection.ecology.resize_population();
			//featureCollection.loadDistrictsFromProperties("");
	    } catch (Exception ex) {
	    	System.out.println("resize ex3: "+ex);
	    	ex.printStackTrace();
	    }

		
		dlg.setVisible(false);
		System.out.println("Ready.");

		
		geo_loaded = true;
		System.out.println("resize_population start0 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));

		setEnableds();
		System.out.println("resize_population start1 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));
		project.demographic_columns.clear();
		setDemographicColumns();
		/*
		System.out.println("resize_population start2 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));
		project.election_columns.clear();
		setElectionColumns();
		System.out.println("resize_population start3 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));
		project.election_columns_2.clear();
		setElectionColumns2();
		System.out.println("resize_population start4 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));
		project.election_columns_3.clear();
		setElectionColumns3();
		
		System.out.println("resize_population start5 "+ (featureCollection.ecology.population == null ? "null" : featureCollection.ecology.population.size()));
		project.substitute_columns.clear();
		setSubstituteColumns();
		*/
		
    	System.out.println("resize ex5");
	    try {
	    	featureCollection.ecology.resize_population();
			//featureCollection.loadDistrictsFromProperties("");
	    } catch (Exception ex) {
	    	System.out.println("resize ex3: "+ex);
	    	ex.printStackTrace();
	    }
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
		mntmCopyColumn.setEnabled(geo_loaded);

		
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
	ButtonGroup buttonGroupPopMinMethod = new ButtonGroup();
	public void setDistrictColumn(String district) {
		System.out.println("setDistrictColumn hush?"+hushSetDistrict);
		if( hushSetDistrict) {
			return;
		}
		boolean changed = !district.equals(project.district_column);
		project.district_column = district;
		if( changed) {
			VTD.compare_centroid = false;
			Collections.sort(featureCollection.features);
			
			if( featureCollection.features == null || featureCollection.features.size() == 0) {
				
			} else {
				boolean is_in = featureCollection.features.get(0).properties.containsKey(district);
				
				int min = 999999999;
				int max = -1;
				for( VTD feat : featureCollection.features) {
					int d = 0;
					try {

						d = Integer.parseInt(feat.properties.get(district).toString() );
					} catch (Exception ex) {
						System.out.println("ex aa "+ex);
						d = 0;
						feat.properties.put(district,"0");
						System.out.println("missing district "+district );
						try {
							System.out.println((String)feat.properties.get("GEOID10"));
						} catch (Exception ex2) {
							
						}
						try {
							System.out.println((String)feat.properties.get(district));
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
				while( featureCollection.ecology.population.size() > 1) {
					featureCollection.ecology.population.remove(1);
				}
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
						int opt = JOptionPane.NO_OPTION;//JOptionPane.showConfirmDialog(this, "Uncontested elections detected.  Lock and ignore uncontested districts?", "Uncontested elections detected!", JOptionPane.YES_NO_OPTION);
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
		ip.addHistory("SET POPULATION COLUMN "+pop_col);

		project.population_column = pop_col;
		for( VTD f : featureCollection.features) {
			String pop = f.properties.get(pop_col).toString();
			pop = pop.replaceAll(",","");
			if( pop == null || pop.length() == 0) {
				pop = "0";
			}
			if( f != null) {
				f.has_census_results = true;
				f.population = Double.parseDouble(pop);
			}
			f.properties.POPULATION = (int) Double.parseDouble(pop.replaceAll(",",""));
		}
	}
	public void setDemographicColumns() {
		int num_candidates = project.demographic_columns.size();
		StringBuffer sb = new StringBuffer("SET ETHNICITY COLUMNS");
		for( String s : project.demographic_columns) {
			if( s == null || s.length() == 0) {
				continue;
			}
			sb.append(" "+s.trim());
		}
		ip.addHistory(sb.toString());

		
		for( int k = 0; k < featureCollection.features.size(); k++) {
			VTD f = featureCollection.features.get(k);
			f.demographics = new double[num_candidates];
			for( int i = 0; i < project.demographic_columns.size(); i++) {
				try {
					f.demographics[i] = Double.parseDouble(f.properties.get(project.demographic_columns.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					f.demographics[i] = 0;
					f.properties.put(project.demographic_columns.get(i),"0");
				}
			}
		}
	}
	public void setCountyColumn() {
		ip.addHistory("SET COUNTY COLUMN "+project.county_column);

		
		for( VTD f : featureCollection.features) {
			try {
				f.county = f.properties.get(project.county_column).toString();
			} catch (Exception ex) {
			}
		}
	}
	
	public void setMuniColumn() {
		ip.addHistory("SET MUNI COLUMN "+project.muni_column);
		
		for( VTD f : featureCollection.features) {
			try {
				f.muni = f.properties.get(project.muni_column).toString();
			} catch (Exception ex) {
			}
		}
	}
	
	public void setElectionColumns() {
		int num_candidates = project.election_columns.size();
		StringBuffer sb = new StringBuffer("SET ELECTION COLUMNS");
		for( String s : project.election_columns) {
			sb.append(" "+s);
		}
		ip.addHistory(sb.toString());

		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		
		for( int k = 0; k < featureCollection.features.size(); k++) {
			VTD f = featureCollection.features.get(k);
			f.resetOutcomes();
			double[] dd = new double[num_candidates];
			for( int i = 0; i < project.election_columns.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns.get(i),"0");
				}
			}
		
			if( f.elections.size() < 1) {
				f.elections.add(new Vector<Election>());
			} else {
				f.elections.get(0).clear();
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
				if( f.elections.size() < 1) {
					f.elections.add(new Vector<Election>());
				}

				f.elections.get(0).add(d);
				//System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
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
		
		for( VTD f : featureCollection.features) {
			f.resetOutcomes();
			double[] dd = new double[Settings.num_candidates];
			for( int i = 0; i < project.election_columns_2.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns_2.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns_2.get(i),"0");
				}
			}
		
			while( f.elections.size() < 2) {
				f.elections.add(new Vector<Election>());
			}
			f.elections.get(1).clear();
			
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
				while( f.elections.size() < 2) {
					f.elections.add(new Vector<Election>());
				}

				f.elections.get(1).add(d);
				//System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
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
		
		for( VTD f : featureCollection.features) {
			f.resetOutcomes();
			double[] dd = new double[Settings.num_candidates];
			for( int i = 0; i < project.election_columns_3.size(); i++) {
				try {
					dd[i] = Double.parseDouble(f.properties.get(project.election_columns_3.get(i)).toString().replaceAll(",",""));
				} catch (Exception ex) {
					
					dd[i] = 0;
					f.properties.put(project.election_columns_3.get(i),"0");
				}
			}
		
			while( f.elections.size() < 3) {
				f.elections.add(new Vector<Election>());
			}
			f.elections.get(2).clear();
			
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
				while( f.elections.size() < 3) {
					f.elections.add(new Vector<Election>());
				}

				f.elections.get(2).add(d);
				//System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
			}
		}
		
		featureCollection.ecology.reset();
		election_loaded = true;
		
		setEnableds();	
	}

	
	public void setSubstituteColumns() {
		int max = 1;//chckbxThirdElection.isSelected() ? 3 : chckbxSecondElection.isSelected() ? 2 : 1;
		boolean[][] buncontested = new boolean[][]{FeatureCollection.buncontested1, FeatureCollection.buncontested2, FeatureCollection.buncontested3};
		Vector<String>[] dems = (Vector<String>[])new Vector[]{project.election_columns, project.election_columns_2, project.election_columns_3};
		
		featureCollection.ecology.reset();
		featureCollection.ecology.population.add(new DistrictMap(featureCollection.ecology.vtds,Settings.num_districts));
		
		for( VTD b : featureCollection.vtds) {
			b.resetOutcomes();
			b.has_election_results = true;
		}
		
		for( int idem = 0; idem < max; idem++) {
			try {
				System.out.println(" a "+featureCollection.ecology.population.size());
				// TODO Auto-generated method stub
				DistrictMap dm = featureCollection.ecology.population.get(0);
				
				//count totals in contested districts
				double[] total_origs = new double[Settings.num_candidates];
				double[] total_substs = new double[Settings.num_candidates];
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_origs[i] = 1;
				}
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_substs[i] = 1;
				}
				
				for( int id = 0; id < featureCollection.features.size(); id++) {
					VTD f = featureCollection.features.get(id);
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
				//compute ratio of totals
				for( int i = 0; i < Settings.num_candidates; i++) {
					total_origs[i] /= total_substs[i];
				}
				
				//now replace uncontested with swing adjusted totals
				for( int id = 0; id < featureCollection.features.size(); id++) {
					VTD f = featureCollection.features.get(id);
					if( !buncontested[idem][dm.vtd_districts[id]]) {
						continue;
					}
					
					f.resetOutcomes();
					double[] dd = new double[Settings.num_candidates];
					for( int i = 0; i < project.substitute_columns.size(); i++) {
						try {
							dd[i] = total_origs[i]*Double.parseDouble(f.properties.get(project.substitute_columns.get(i)).toString().replaceAll(",",""));
							f.properties.put(project.election_columns.get(i),""+((int)dd[i]));
						} catch (Exception ex) {
							ex.printStackTrace();
							dd[i] = 0;
							f.properties.put(project.substitute_columns.get(i),"0");
						}
					}
				
					while( f.elections.size() <= idem) {
						f.elections.add(new Vector<Election>());
					}
					f.elections.get(idem).clear();
					
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
						if( f.elections.size() < 1) {
							f.elections.add(new Vector<Election>());
						}
						f.elections.get(idem).add(d);
						//System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		featureCollection.ecology.reset();
		featureCollection.ecology.population.add(new DistrictMap(featureCollection.ecology.vtds,Settings.num_districts));

		election_loaded = true;
		
		setEnableds();	
		
	}
	public void setMaxElections() {
		int imax = 1;//chckbxThirdElection.isSelected() ? 3 : chckbxSecondElection.isSelected() ? 2 : 1;
		for( VTD f : featureCollection.features) {
			while( f.elections.size() > imax) {
				f.elections.remove(imax-1);//.add(new Vector<Demographic>());
			}
			f.resetOutcomes();
		}
	}

	public void importPopulation() {
		ip.addHistory("IMPORT POPULATION");
	
		if( Download.census_merge_working) {
			if( Download.census_merge_old) {
				Download.nextThread = new ImportCensus2Thread(); 
			} else {
				Download.nextThread = new ImportGazzeterThread(); 
				
			}
		}
		Download.download_vtd = false;
		Download.download_census = true;
		new Download().start();
		
		//DialogDownload dd = new DialogDownload();
		//dd.setTitle("Download and aggregate census data from census.gov");
		//dd.show();
	}
	public void appendStuff() {
		String fn = Download.getBasePath()+File.separator+"missing_data.txt";
		String s = getFile(new File(fn)).toString();
		DataAndHeader dh =  readDelimited(s,"\t", true);
		if( dh.data.length == 0) {
			dh.header = new String[]{"STATE","GEOID10","DISTRICT","VTDNAME","VTD CODE","LAND","WATER","POPULATION","COUNTY","PRES12_DEM","PRES12_REP"};
		}
		Vector<String[]> v = arrayToVector(dh.data);
		System.out.println("starting with "+v.size()+" elements.");
		
		String[] source_cols = new String[]{"","GEOID10","CD_2010","VTDNAME","VTDST10","ALAND10","AWATER10","POPULATION","COUNTY_NAM","PRES12_DEM","PRES12_REP"};
		for( VTD f : this.featureCollection.features) {
			String pdem = f.properties.getString("PRES12_DEM").trim();
			String prep = f.properties.getString("PRES12_REP").trim();
			pdem = pdem == null ? "0" : pdem.equals("") ? "0" : pdem.equals("null") ? "0" : pdem;
			prep = prep == null ? "0" : prep.equals("") ? "0" : prep.equals("null") ? "0" : prep;
			double pd = 0, pr = 0;
			try {
				pd = Double.parseDouble(pdem.replaceAll(",", ""));
				pr = Double.parseDouble(prep.replaceAll(",", ""));
			} catch(Exception ex) { }
			
			if( pd <= 0 && pr <= 0 ) {
				System.out.println("found a bad precinct");
				String[] data = new String[source_cols.length];
				data[0] = Download.states[Download.istate];
				for( int i = 1; i < source_cols.length; i++) {
					data[i] = f.properties.getString(source_cols[i]);
				}
				v.add(data);
			}
		}
		
		dh.data = StaticFunctions.vectorToArray(v);
		writeDelimited(fn,dh,"\t",true);
	}
	public Vector<String[]> arrayToVector(String[][] sss) {
		Vector<String[]> v = new Vector<String[]>();
		for( String[] ss : sss) {
			v.add(ss);
		}
		return v;
	}
	public void writeDelimited(String filename, DataAndHeader dh, String delimiter, boolean has_headers) {
		String[] headers = dh.header;
		String[][] data = dh.data;
		System.out.println("creating...");
		File f = new File(filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
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
		}
	}
	public void printss(String[] ss) {
		System.out.print("ss("+ss.length+"): ");
		for( String s : ss) {
			System.out.print("["+s+"] ");
		}
		System.out.println();
	}
	public DataAndHeader readDelimited(String s, String delimiter, boolean has_headers) {
		DataAndHeader dh = new DataAndHeader();
		if( delimiter.equals(",")) {
			delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		}
		try {
			String[] lines = s.split("\n");
			dh.header = lines[0].split(delimiter);
			if( !has_headers) {
				for( int i = 0; i < dh.header.length; i++) {
					dh.header[i] = "col_"+i;
				}
			}
			for( int i = 0; i < dh.header.length; i++) {
				dh.header[i] = dh.header[i].trim();
				if(dh.header[i].length() > 2 && dh.header[i].charAt(0) == '"' && dh.header[i].charAt(dh.header[i].length()-2) == '"' ) {
					dh.header[i] = dh.header[i].substring(1, dh.header[i].length()-1);
				}
			}
			printss( dh.header);
			dh.data = new String[lines.length - (has_headers ? 1 : 0)][];
			for( int i = has_headers ? 1 : 0; i < lines.length; i++) {
				int k = i-(has_headers ? 1 : 0); 
				dh.data[k] = lines[i].split(delimiter);
				for( int j = 0; j < dh.data[k].length; j++) {
					dh.data[k][j] = dh.data[k][j].trim();
					if(dh.data[k][j].length() > 2 && dh.data[k][j].charAt(0) == '"' && dh.data[k][j].charAt(dh.data[k][j].length()-1) == '"' ) {
						dh.data[k][j] = dh.data[k][j].substring(1, dh.data[k][j].length()-1);
					}
				}
				printss( dh.data[k]);
			}
			System.out.println();
			return dh;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//System.exit(0);
		return dh;
	}
	public void loadShapeFile(File f) {

	    try {
			FileInputStream is = new FileInputStream(f);
			ValidationPreferences prefs = new ValidationPreferences();
		    prefs.setMaxNumberOfPointsPerShape(32650*4);
		    prefs.setAllowUnlimitedNumberOfPointsPerShape(true);
		    prefs.setAllowBadContentLength(true);
		    prefs.setAllowBadRecordNumbers(true);
		    
		    //prefs.setMaxNumberOfPointsPerShape(16650);
		    ShapeFileReader r = new ShapeFileReader(is, prefs);
		    
			String dbfname = f.getAbsolutePath();//.getName();
			dbfname = dbfname.substring(0,dbfname.length()-4)+".dbf";
			
			DBFReader dbfreader = new DBFReader(dbfname);
			String[] cols = new String[dbfreader.getFieldCount()];
			for( int i=0; i<cols.length; i++) {
				cols[i] = dbfreader.getField(i).name;
				System.out.print(cols[i]+"  ");
			}
			System.out.print("\n");
		    
			

		    ShapeFileHeader h = r.getHeader();
		    System.out.println("The shape type of this files is " + h.getShapeType());

		    int total = 0;
		    AbstractShape s;
		    while ((s = r.next()) != null) {
		    	Object aobj[] = new Object[cols.length];
		    	try {
		    		aobj = dbfreader.nextRecord(Charset.defaultCharset());
		    	} catch (Exception ex) {
		    		System.out.println("aobj ex "+ex);
		    		ex.printStackTrace();
		    		for( int i = 0; i < aobj.length; i++) {
		    			aobj[i] = "";
		    		}
		    		
		    	}
		      switch (s.getShapeType()) {
		      case POLYGON_Z:
			      {
			    	  int rec_num = s.getHeader().getRecordNumber();
			    	  //System.out.println("record number: "+rec_num);
			          PolygonZShape aPolygon = (PolygonZShape) s;
			          
			          VTD feature = new VTD();
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
			          
			          VTD feature = new VTD();
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
				cols[i] = dbfreader.getField(i).name;
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
					//System.out.println("ward "+b.id+" added demo "+d.population+" "+j);
				}
			}
			Settings.num_candidates = num_candidates;
			featureCollection.ecology.reset();
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		VTD.display_mode = 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		election_loaded = true;
		setEnableds();
	}
	
	public MainFrame() {
		mainframe = this;
		ip.mainFrame = this;

		String className = getLookAndFeelClassName("Nimbus");
		try {
			UIManager.setLookAndFeel(className);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		jbInit();
		/*
		chckbxmntmOutlineState.setSelected(true);
		Feature.outline_state = chckbxmntmOutlineState.isSelected();
		MapPanel.FSAA = Feature.outline_vtds ? 4 : 1;
		mapPanel.invalidate();
		mapPanel.repaint();
		*/
		
		try {
			UIManager.setLookAndFeel(className);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		sliderRepresentation.setValue(50);
		sliderSeatsVotes.setValue(50);
		lblFairnessCriteria.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFairnessCriteria.setBounds(10, 10, 179, 16);
		
		panel_4.add(lblFairnessCriteria);
		lblRacialVoteDilution.setToolTipText("<html><img src=\"file:/C:/Users/kbaas.000/git/autoredistrict/bin/resources/voting_power.png\">");
		lblRacialVoteDilution.setBounds(10, 220, 172, 16);
		
		panel_4.add(lblRacialVoteDilution);
		sliderVoteDilution.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.vote_dilution_weight = sliderVoteDilution.getValue()/100.0;
				ip.addHistory("SET WEIGHT RACIAL "+Settings.vote_dilution_weight);
			}
		});
		sliderVoteDilution.setValue(50);
		sliderVoteDilution.setBounds(10, 240, 180, 29);
		
		panel_4.add(sliderVoteDilution);
		
		chckbxConstrain_1 = new JCheckBox("constrain");
		chckbxConstrain_1.setToolTipText("This rejects mutations that decrease competitiveness at the cost of slower evolution.");
		chckbxConstrain_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.mutate_competitive = chckbxConstrain_1.isSelected();
				Settings.mutate_good = chckbxConstrain.isSelected() && chckbxConstrain_1.isSelected();
				ip.addHistory("SET CONSTRAIN COMPETITION  "+(chckbxConstrain_1.isSelected() ? "TRUE" : "FALSE"));
			}
		});
		chckbxConstrain_1.setFont(new Font("Tahoma", Font.PLAIN, 9));
		chckbxConstrain_1.setBounds(133, 37, 61, 23);
		panel_4.add(chckbxConstrain_1);
		
		lblDescriptiveRepr = new JLabel("Descriptive representation");
		lblDescriptiveRepr.setToolTipText("<html><img src=\"file:/C:/Users/kbaas.000/git/autoredistrict/bin/resources/voting_power.png\">");
		lblDescriptiveRepr.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblDescriptiveRepr.setBounds(10, 265, 172, 16);
		panel_4.add(lblDescriptiveRepr);
		
		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.descr_rep_weight = slider.getValue()/100.0;
				ip.addHistory("SET WEIGHT DESCRIPTIVE "+Settings.descr_rep_weight);

			}
		});
		slider.setValue(0);
		slider.setBounds(10, 285, 180, 29);
		panel_4.add(slider);
		
		Settings.mutation_rate = 0; 
		Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*Math.exp(-(100-slider_mutation.getValue())/Settings.exp_mutate_factor);
		Settings.geo_or_fair_balance_weight = sliderBalance.getValue()/100.0;
		Settings.competitiveness_weight = sliderWastedVotesTotal.getValue()/100.0;
		Settings.seats_votes_asymmetry_weight = sliderSeatsVotes.getValue()/100.0;
		//Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;
		Settings.diagonalization_weight = sliderRepresentation.getValue()/100.0;
		Settings.population_balance_weight = sliderPopulationBalance.getValue()/100.0;
		Settings.geometry_weight = sliderBorderLength.getValue()/100.0;
		Settings.disconnected_population_weight = 2*sliderDisconnected.getValue()/100.0;
		Settings.split_reduction_weight = sliderSplitReduction.getValue()/100.0;
		Settings.vote_dilution_weight = sliderVoteDilution.getValue()/100.0;
		Settings.descr_rep_weight = slider.getValue()/100.0;
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
		this.setSize(new Dimension(1021+100, 779));
		
		sliderWastedVotesTotal = new JSlider();
		lblWastedVotes = new JLabel("Competitive");
		lblWastedVotes.setFont(new Font("Tahoma", Font.PLAIN, 10));
		goButton = new JButton();
		stopButton = new JButton();
		
		lblFairnessCriteria = new JLabel("Equality criteria");
		lblRacialVoteDilution = new JLabel("Anti-racial gerrymandering");
		lblRacialVoteDilution.setFont(new Font("Tahoma", Font.PLAIN, 10));
		sliderVoteDilution = new JSlider();
		textFieldTotalSeats = new JTextField();
		lblTotalSeats = new JRadioButton("Total seats");
		srlblMuniColumn = new JLabel("Muni column");
		srcomboBoxMuniColumn = new JComboBox();
		rdbtnReduceTotalSplits = new JRadioButton("Reduce total splits");
		rdbtnReduceSplitCounties = new JRadioButton("Reduce split counties");
		splitReductionType = new ButtonGroup();
		chckbxAutoAnneal = new JCheckBox("anneal rate");
		lblElitisesMutated = new JLabel("% elites mutated");
		sliderElitesMutated = new JSlider();
		sliderElitesMutated.setToolTipText("<html>Elitism involves copying a small proportion of the fittest candidates, unchanged, into the <br/>next generation. This can sometimes have a dramatic impact on performance by ensuring <br/>that the EA does not waste time re-discovering previously discarded partial solutions. <br/>Candidate solutions that are preserved unchanged through elitism remain eligible for <br/>selection as parents when breeding the remainder of the next generation.<br/>\r\nSo basically it takes a small fraction of the best candidates, and copies them over unchanged <br/>to the next generation.  So these are essential your immortals -- every one else only lasts one <br/>generation.<br/><br/>\r\nExperimentally, about 25% elitism seems to work fine.<br/><br/>\r\nThere is also be a slider \"% elites mutated\".  Notice the description above is that the elites <br/>remain unchanged between generations.  With mutate elites selected, the elites will slowly <br/>mutate along with the rest of the population. This helps it search a little faster, but when it <br/>gets down to fine-tuning, where you only want the very best, you want to turn this off, as <br/>otherwise you'd just be hovering around the best.<br/></html>");
		rdbtnMinimizeMaxDev = new JRadioButton("Minimize squared dev.");
		rdbtnMinimizeMeanDev = new JRadioButton("Minimize absolute dev.");
		mntmWizard = new JMenuItem("Download vtd shapefile & population from census.gov...");
		separator_7 = new JSeparator();
		mnWindows = new JMenu("Windows");
		
		
		//========CONTAINERS
	    dlg = new JDialog(mainframe, "Working", true);
	    dpb = new JProgressBar(0, 500);
	    dlbl = new JLabel();
		mapPanel = new MapPanel(); 
		frameStats = new JFrame();
		panelStats = new PanelStats();
		frameGraph = new JFrame(); 
		df = new DialogShowProperties();
		panelGraph = new PanelGraph();

		//===========COMPONENTS - MENU ITEMS
		selectionType = new ButtonGroup();
		mntmShowVoteBalance = new JRadioButtonMenuItem("Color by vote balance");;
		mntmShowDemographics = new JRadioButtonMenuItem("Color by demographic");;
		chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
		chckbxmntmHideMapLines = new JCheckBoxMenuItem("Show map lines");
		chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
		chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
		chckbxmntmShowDistrictLabels = new JCheckBoxMenuItem("Show district labels");
		mntmSaveProjectFile = new JMenuItem("Save project file");
		mntmSaveData = new JMenuItem("Save data");
		mntmMergeData = new JMenuItem("Merge data");
		mntmRenumber = new JMenuItem("Renumber districts");

		mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
		chckbxmntmOpenCensusResults = new JMenuItem("Open Census results");
		
		mntmOpenElectionResults = new JMenuItem("Open Election results");
		mnEvolution = new JMenu("Evolution");
		mnHelp = new JMenu("Help");
		mntmWebsite = new JMenuItem("Website");
		mntmSourceCode = new JMenuItem("Source code");
		mntmLicense = new JMenuItem("License");
		mntmExportcsv = new JMenuItem("Export results .csv");
		mntmImportcsv = new JMenuItem("Import results .csv");
		mntmShowStats = new JMenuItem("Show stats");
		mntmShowData = new JMenuItem("Show data");
		
		mnImportExport = new JMenu("Aggregate/Deaggregate");

		mntmExportPopulation = new JMenuItem("Export population");
		mntmImportPopulation = new JMenuItem("Import population");
		mntmResetZoom = new JMenuItem("Reset zoom");
		mntmZoomIn = new JMenuItem("Zoom in");
		mntmUndoZoom = new JMenuItem("Undo zoom");
		mntmShowGraph = new JMenuItem("Show graph");
		mntmOpenEsriShapefile = new JMenuItem("Open ESRI shapefile");
		comboBoxPopulation = new JComboBox();
		comboBoxDistrictColumn = new JComboBox();
	    textFieldNumDistricts = new JTextField();

		manageLocks = new DialogManageLocks();
		seatsPanel = new PanelSeats();
		featuresPanel = new PanelFeatures();
		progressBar = new JProgressBar();
		frameSeatsVotesChart = new FrameSeatsVotesChart();;;
		chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
		chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
		chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
		chckbxmntmShowDistrictLabels = new JCheckBoxMenuItem("Show district labels");
		mntmSaveProjectFile = new JMenuItem("Save project file");
		mntmSaveData = new JMenuItem("Save data");
		mntmMergeData = new JMenuItem("Merge data");
		mntmRenumber = new JMenuItem("Renumber districts");

		//mnGeography = new JMenu("Geography");
		mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
		//mnDemographics = new JMenu("Demographics");
		chckbxmntmOpenCensusResults = new JMenuItem("Open Census results");
		mntmOpenElectionResults = new JMenuItem("Open Election results");
		mnHelp_1 = new JMenu("Help");
		mntmWebsite = new JMenuItem("Website");
		mntmSourceCode = new JMenuItem("Source code");
		mntmLicense = new JMenuItem("License");
		mntmExportcsv = new JMenuItem("Export results .csv");
		mntmImportcsv = new JMenuItem("Import results .csv");
		mntmShowStats = new JMenuItem("Show stats");
		mntmShowData = new JMenuItem("Show data");

		mntmExportPopulation = new JMenuItem("Export population");
		mntmImportPopulation = new JMenuItem("Import population");
		mntmResetZoom = new JMenuItem("Reset zoom");
		mntmZoomIn = new JMenuItem("Zoom in");
		mntmUndoZoom = new JMenuItem("Undo zoom");
		mntmShowGraph = new JMenuItem("Show graph");
		comboBoxPopulation = new JComboBox();
		comboBoxDistrictColumn = new JComboBox();
	    textFieldNumDistricts = new JTextField();
	    evolutionPopulationTF = new JTextField();


		separator_1 = new JSeparator();
		mntmOpenWktTabdelimited = new JMenuItem("Open WKT tab-delimited");

	    dlg = new JDialog(mainframe, "Working", true);


		
		slider_mutation = new JSlider();
		slider_mutation.setValue(100);
		sliderDisconnected = new JSlider();
		sliderBorderLength = new JSlider();
		sliderPopulationBalance = new JSlider();
		sliderRepresentation = new JSlider();
		
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
		
		mnMerge = new JMenu("Merge");
		menuBar.add(mnMerge);
		
		mntmCountyVote = new JMenuItem("2010 county vote and ethnic data");
		mntmCountyVote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importCountyData();

			}
		});
		mnMerge.add(mntmCountyVote);
		
		separator_13 = new JSeparator();
		mnMerge.add(separator_13);
		
		mntmVtdVote = new JMenuItem("2010 vtd vote estimates");
		mntmVtdVote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importBlockElection();
			}
		});
		mnMerge.add(mntmVtdVote);
		
		separator_12 = new JSeparator();
		mnMerge.add(separator_12);
		
		mntmBlocklevelPopulation = new JMenuItem("Block-level population");
		mntmBlocklevelPopulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importPopulation();
			}
		});
		/*
		mntmBlocklevelPopulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ip.addHistory("IMPORT POPULATION");

				if( Download.census_merge_working) {
					if( Download.census_merge_old) {
						Download.nextThread = new ImportCensus2Thread(); 
					} else {
						Download.nextThread = new ImportGazzeterThread(); 
						
					}
				}
				//Download.nextThread = ost;
				DialogDownload dd = new DialogDownload();
				dd.setTitle("Download and aggregate census data from census.gov");
				dd.show();
			}
		});
		*/
		mnMerge.add(mntmBlocklevelPopulation);
		
		mntmBlocklevelEthnicData = new JMenuItem("Block-level ethnic data");
		mnMerge.add(mntmBlocklevelEthnicData);
		mntmBlocklevelEthnicData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importBlockDemographics();
			}
		});

		
		mntmBlocklevelCurrentDistricts = new JMenuItem("Block-level CY2000 districts");
		mntmBlocklevelCurrentDistricts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importBlockCurrentDistricts();
			}
		});
		mnMerge.add(mntmBlocklevelCurrentDistricts);
		
		
		mntmBlocklevelBdistricting = new JMenuItem("Block-level bdistricting");
		mntmBlocklevelBdistricting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importBlockBdistricting();
			}
		});
		mnMerge.add(mntmBlocklevelBdistricting);
		
		mntmImportBlockFile = new JMenuItem("Import block file");
		mntmImportBlockFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ImportAggregateCustom().init();
			}
		});
		mnMerge.add(mntmImportBlockFile);
		
		separator_14 = new JSeparator();
		mnMerge.add(separator_14);
		mntmExportAndDeaggregate = new JMenuItem("Export & de-aggregate custom");
		mnMerge.add(mntmExportAndDeaggregate);
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
				String s = project.toString();
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
				Download.initPaths();
				OpenShapeFileThread ost = new OpenShapeFileThread(null);
				ImportCensus2Thread ir = new ImportCensus2Thread();
				MergeInDemoAndElection it = new MergeInDemoAndElection();
				//it.nextThread = new ImportCountyLevel();
				ir.nextThread =  it;
				ost.nextThread = ir;
				Download.nextThread = ost;
				Download.prompt = true;
				DialogDownload dd = new DialogDownload();
				dd.show();
				System.out.println("dd hidden "+dd.ok+" "+dd.all);
				if( dd.ok && dd.all) {
					System.out.println("download all");
					//JOptionPane.showMessageDialog(null,"hi");

					Download.prompt = false;
					Download.downloadAll = true;
					
					Download.istate = 0;
					Download.cyear = Integer.parseInt((String)dd.comboBoxCensusYear.getSelectedItem());
					Download.vyear = Integer.parseInt((String)dd.comboBoxElectionYear.getSelectedItem());
					downloadNextState();
					/*
					CycleThread ct = new CycleThread();
					ct.cyear = Integer.parseInt((String)dd.comboBoxCensusYear.getSelectedItem());
					ct.eyear = Integer.parseInt((String)dd.comboBoxElectionYear.getSelectedItem());
					ct.state = 1;
					ct.start();
					*/
				}
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
		
		mntmMergeInElection = new JMenuItem("Merge in election data and demographics");
		mntmMergeInElection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mergeInDemoAndElection();
			}
		});
		//mnFile.add(mntmMergeInElection);
		mnFile.add(mntmHarvardElectionData);
		
		mntmPublicMappingProject = new JMenuItem("Public Mapping Project data...");
		mntmPublicMappingProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Applet.browseTo("http://www.publicmapping.org/resources/data");
			}
		});
		mnFile.add(mntmPublicMappingProject);
		
		mnFile.add(separator_7);
		mntmOpenEsriShapefile = new JMenuItem("Open ESRI shapefile");
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
		//mnFile.add(separator);
		
		mnFile.add(mntmOpenGeojson);
		mntmOpenWktTabdelimited.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new OpenWKTFileThread().start();
			}
		});
		
		mnFile.add(mntmOpenWktTabdelimited);
		
		mnFile.add(separator_1);


		
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
					dh = FileUtil.readDBF(f.getAbsolutePath());
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
				saveData(null,-1,true);
			}
		});
		mnFile.add(mntmSaveData);
		
		separator_9 = new JSeparator();
		mnFile.add(separator_9);
		
		mntmCopyColumn = new JMenuItem("Copy column");
		mntmCopyColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(mainframe, "Not implemented yet.");
			}
		});
		mntmCopyColumn.setEnabled(false);
		mnFile.add(mntmCopyColumn);
		
		separator_4 = new JSeparator();
		mnFile.add(separator_4);

		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		//add at ened of jmenu (move code in panelstats to function)
		mntmExportToHtml = new JMenuItem("Export to html");
		mntmExportToHtml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelStats.exportToHtml(true);
			}
		});
		mnFile.add(mntmExportToHtml);
		

		
		mntmExportBlockFile = new JMenuItem("Export block file");
		mntmExportBlockFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ip.addHistory("EXPORT BLOCKS");
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
					ExportCustomThread exp = new ExportCustomThread();
					exp.init();
					//new ExportToBlockLevelThread().start();
				} else {
					JOptionPane.showMessageDialog(MainFrame.mainframe,"You must select a district column first.");
				}
			}
		});
		
		mntmExportNationalMaps = new JMenuItem("Export national maps");
		mntmExportNationalMaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelStats.exportTransparent();
			}
		});
		mnFile.add(mntmExportNationalMaps);
		
		mntmExportPieCharts = new JMenuItem("Export pie charts");
		mntmExportPieCharts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelStats.exportPieCharts();
			}
		});
		mnFile.add(mntmExportPieCharts);
		mnFile.add(mntmExportBlockFile);
		
		separator_10 = new JSeparator();
		mnFile.add(separator_10);

		
		mnFile.add(mntmExit);		
		
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
				VTD.display_mode = 2;
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
				VTD.showPrecinctLabels = chckbxmntmShowPrecinctLabels.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		//mnView.add(new JSeparator());
		//mnView.add(chckbxmntmShowPrecinctLabels);
		chckbxmntmShowDistrictLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VTD.showDistrictLabels = chckbxmntmShowDistrictLabels.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
				//JOptionPane.showMessageDialog(null,"Not implemented");
			}
		});
		mnView.add(chckbxmntmShowDistrictLabels);
		
		chckbxmntmSimplifyPolygons = new JCheckBoxMenuItem("Simplify polygons");
		chckbxmntmSimplifyPolygons.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.b_make_simplified_polys = chckbxmntmSimplifyPolygons.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		chckbxmntmSimplifyPolygons.setToolTipText("This makes the map draw considerably faster by reducing the number of lines in each polygon.");
		mnView.add(chckbxmntmSimplifyPolygons);
		chckbxmntmHideMapLines = new JCheckBoxMenuItem("Outline vtd's");
		chckbxmntmHideMapLines.setSelected(VTD.outline_vtds );
		chckbxmntmHideMapLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.outline_vtds = chckbxmntmHideMapLines.isSelected();
				MapPanel.FSAA = VTD.isOutlineActive() ? 4 : 1;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		chckbxmntmFitToNation = new JCheckBoxMenuItem("Fit to nation");
		chckbxmntmFitToNation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.setNationalMap(chckbxmntmFitToNation.isSelected());

			}
		});
		mnView.add(chckbxmntmFitToNation);
		
		separator_15 = new JSeparator();
		mnView.add(separator_15);
		mnView.add(chckbxmntmHideMapLines);
		
		chckbxmntmOutlineState = new JCheckBoxMenuItem("Outline state");
		chckbxmntmOutlineState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.outline_state = chckbxmntmOutlineState.isSelected();
				MapPanel.FSAA = VTD.isOutlineActive() ? 4 : 1;
				mapPanel.invalidate();
				mapPanel.repaint();

			}
		});
		mnView.add(chckbxmntmOutlineState);
		
		mntmOutlineDistricts = new JCheckBoxMenuItem("Outline districts");
		mntmOutlineDistricts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					VTD.outline_districts = mntmOutlineDistricts.isSelected();
					MapPanel.FSAA = VTD.isOutlineActive() ? 4 : 1;
					mapPanel.invalidate();
					mapPanel.repaint();
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}
			}
		});
		mnView.add(mntmOutlineDistricts);
		
		mntmOutlineCounties = new JCheckBoxMenuItem("Outline counties");
		mntmOutlineCounties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					VTD.outline_counties = mntmOutlineCounties.isSelected();
					MapPanel.FSAA = VTD.isOutlineActive() ? 4 : 1;
					mapPanel.invalidate();
					mapPanel.repaint();
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}


			}
		});
		mnView.add(mntmOutlineCounties);
		
		separator_16 = new JSeparator();
		mnView.add(separator_16);
		
		chckbxmntmDividePackingBy = new JCheckBoxMenuItem("Divide by population density");
		chckbxmntmDividePackingBy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.divide_packing_by_area = chckbxmntmDividePackingBy.isSelected();
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		chckbxmntmDividePackingBy.setSelected(true);
		mnView.add(chckbxmntmDividePackingBy);
		
		
		separator_6 = new JSeparator();
		mnView.add(separator_6);
		
		mntmColorByDistrict = new JRadioButtonMenuItem("Color by district");
		mntmColorByDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VTD.display_mode = VTD.DISPLAY_MODE_NORMAL;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByDistrict);
		
		mntmColorByPop = new JRadioButtonMenuItem("Color by district pop imbalance");
		mntmColorByPop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_DIST_POP;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByPop);
		
		mntmColorByVote = new JRadioButtonMenuItem("Color by district vote");
		mntmColorByVote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_DIST_VOTE;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		
		mntmColorByCompactness = new JRadioButtonMenuItem("Color by district compactness");
		mntmColorByCompactness.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VTD.display_mode = VTD.DISPLAY_MODE_COMPACTNESS;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByCompactness);
		
		mntmColorByWasted = new JRadioButtonMenuItem("Color by district wasted votes");
		mntmColorByWasted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_WASTED_VOTES;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		mnView.add(mntmColorByWasted);
		
		mntmColorByCounty = new JRadioButtonMenuItem("Color by county");
		mntmColorByCounty.setEnabled(false);
		mntmColorByCounty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_COUNTIES;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByCounty);
		
		mntmColorBySplits = new JRadioButtonMenuItem("Color by splits");
		mntmColorBySplits.setEnabled(false);
		mntmColorBySplits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_COUNTY_SPLITS;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorBySplits);
		
		separator_17 = new JSeparator();
		mnView.add(separator_17);
		
				mntmShowVoteBalance = new JRadioButtonMenuItem("Color by vtd vote");
				
				mntmShowVoteBalance.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						VTD.display_mode = VTD.DISPLAY_MODE_VOTES;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				mnView.add(mntmShowVoteBalance);
		mnView.add(mntmColorByVote);
		
		//add before the jseparator
				mntmColorDistDemo = new JRadioButtonMenuItem("Color by district demographics");
				mntmColorDistDemo.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						VTD.display_mode = VTD.DISPLAY_MODE_DIST_DEMO;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				
								mntmColorPartisanPacking = new JRadioButtonMenuItem("Color by district partisan vote packing");
								mntmColorPartisanPacking.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING;
										mapPanel.invalidate();
										mapPanel.repaint();
									}
								});
								mnView.add(mntmColorPartisanPacking);
				
				mntmColorByVtd = new JRadioButtonMenuItem("Color by vtd partisan packing");
				mntmColorByVtd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING2;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				
				mntmColorByCentered = new JRadioButtonMenuItem("Color by centered partisan vote packing");
				mntmColorByCentered.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						VTD.display_mode = VTD.DISPLAY_MODE_PARTISAN_PACKING_MEAN;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				mnView.add(mntmColorByCentered);
				mnView.add(mntmColorByVtd);
				
				rdbtnmntmColorBySeats = new JRadioButtonMenuItem("Color by seats won");
				rdbtnmntmColorBySeats.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						VTD.display_mode = VTD.DISPLAY_MODE_DIST_SEATS;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				mnView.add(rdbtnmntmColorBySeats);
				
				separator_18 = new JSeparator();
				mnView.add(separator_18);
				mntmShowDemographics = new JRadioButtonMenuItem("Color by vtd demographic");
				mntmShowDemographics.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if( false) {
							JOptionPane.showMessageDialog(null,"Not implemented");
							return;
						}
						VTD.display_mode = VTD.DISPLAY_MODE_DEMOGRAPHICS;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				mnView.add(mntmShowDemographics);
				mnView.add(mntmColorDistDemo);

				mntmColorRacialPacking = new JRadioButtonMenuItem("Color by district racial vote packing");
				mntmColorRacialPacking.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING;
						mapPanel.invalidate();
						mapPanel.repaint();
					}
				});
				mnView.add(mntmColorRacialPacking);
		
		mntmColorByVtd_1 = new JRadioButtonMenuItem("Color by vtd racial packing");
		mntmColorByVtd_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING2;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		
		mntmColorByCentered_1 = new JRadioButtonMenuItem("Color by centered racial vote packing");
		mntmColorByCentered_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_RACIAL_PACKING_MEAN;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmColorByCentered_1);
		mnView.add(mntmColorByVtd_1);
		
		rdbtnmntmColorByDescr = new JRadioButtonMenuItem("Color by descr. representation");
		rdbtnmntmColorByDescr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VTD.display_mode = VTD.DISPLAY_MODE_DIST_DESCR;
				mapPanel.invalidate();
				mapPanel.repaint();

				//DISPLAY_MODE_DIST_DESCR
			}
		});
		mnView.add(rdbtnmntmColorByDescr);
		
		
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
		
		mntmNineMaps = new JMenuItem("Nine maps");
		mntmNineMaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.num_maps_to_draw = 9;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmNineMaps);
		
		mntmSixteenMaps = new JMenuItem("Sixteen maps");
		mntmSixteenMaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.num_maps_to_draw = 16;
				mapPanel.invalidate();
				mapPanel.repaint();
			}
		});
		mnView.add(mntmSixteenMaps);
		
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
						VTD b = featureCollection.ecology.vtds.get(i);
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
				VTD.display_mode = 0;
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
							VTD b = featureCollection.ecology.vtds.get(i);
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
		
		mntmExplainStats = new JMenuItem("Explain stats");
		mntmExplainStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Applet.browseTo(PanelStats.stats_descriptions_url);
			}
		});
		mnHelp_1.add(mntmExplainStats);
		//menuBar.add(S)
		mnHelp_1.add(mntmWebsite);
		mnHelp_1.add(mntmSourceCode);
		mnHelp_1.add(mntmLicense);
		
		menuBar.add(mnWindows);
		mnWindows.add(mntmShowStats);
		mnWindows.add(mntmShowGraph);
		mntmShowSeats = new JMenuItem("Show seats / votes");
		mnWindows.add(mntmShowSeats);
		mntmShowSeats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frameSeatsVotesChart.show();
			}
		});
		mnWindows.add(mntmShowData);
		
		mntmShowScripts = new JMenuItem("Show scripts");
		mntmShowScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ip.show();
			}
		});
		mnWindows.add(mntmShowScripts);
		
		mntmShowPieCharts = new JMenuItem("Show pie charts");
		mntmShowPieCharts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pie.show();
			}
		});
		mnWindows.add(mntmShowPieCharts);
		
		mntmShowAdvancedStats = new JMenuItem("Show advanced stats (snapshot)");
		mntmShowAdvancedStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( MainFrame.mainframe.project.multiElections.size() < 3) {
					JOptionPane.showMessageDialog(null, "Must have at least three elections selected to compute advanced statistics.");
					return;
				}
				if( featureCollection.ecology.population == null || featureCollection.ecology.population.size() == 0) {
					featureCollection.ecology.population =  new Vector<DistrictMap>();
		    		featureCollection.ecology.population.add(new DistrictMap(featureCollection.ecology.vtds,Settings.num_districts));
				}
				DistrictMap dm = featureCollection.ecology.population.get(0);
				double[][][] ee = dm.getAllElectionOutcomes();
				Metrics m = new Metrics(ee[0],ee[1],Settings.num_districts);
				m.trials = 10000;
				m.showBetas();
				m.computeSeatProbs(false);
				m.showSeats();
				m.computeAsymmetry(false);
				m.showAsymmetry();
				m.showHistogram();
				m.showBetaParameters();
				m.showSeatsVotes();
				m.showHeatMap();
			}
		});
		mnWindows.add(mntmShowAdvancedStats);
		
		
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
		mnHelp_1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); 
		menuBar.add(mnHelp_1);
		
		JSplitPane splitPane = new JSplitPane();
		JSplitPane splitPane2 = new JSplitPane();
		splitPane_1 = new JSplitPane();

		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		panel.setPreferredSize(new Dimension(600, 100));
		panel_1.setPreferredSize(new Dimension(200,100));
		panel.setLayout(null);
		panel_1.setLayout(null);
		splitPane.setLeftComponent(panel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(400, 55, 200, 274);
		panel.add(panel_2);
		panel_2.setLayout(null);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel lblCompactness = new JLabel("Compact");
		lblCompactness.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblCompactness.setBounds(6, 97, 90, 16);
		panel_2.add(lblCompactness);
		sliderBorderLength.setBounds(6, 118, 190, 29);
		panel_2.add(sliderBorderLength);
		
		JLabel lblEvolutionaryPressure = new JLabel("Geometric criteria");
		lblEvolutionaryPressure.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEvolutionaryPressure.setBounds(6, 9, 179, 16);
		panel_2.add(lblEvolutionaryPressure);
		
		JLabel lblProportionalRepresentation = new JLabel("Equal population");
		lblProportionalRepresentation.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblProportionalRepresentation.setBounds(6, 158, 90, 16);
		panel_2.add(lblProportionalRepresentation);
		sliderPopulationBalance.setBounds(6, 179, 190, 29);
		panel_2.add(sliderPopulationBalance);
		
		JLabel lblConnectedness = new JLabel("Contiguous");
		lblConnectedness.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblConnectedness.setBounds(6, 36, 90, 16);
		panel_2.add(lblConnectedness);
		
		sliderDisconnected.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.disconnected_population_weight = sliderDisconnected.getValue()/100.0;
				ip.addHistory("SET WEIGHT CONTIGUITY "+Settings.disconnected_population_weight);
			}
		});
		sliderDisconnected.setBounds(6, 57, 190, 29);
		panel_2.add(sliderDisconnected);
		rdbtnMinimizeMaxDev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.minimize_absolute_deviation = !rdbtnMinimizeMaxDev.isSelected();
			}
		});
		rdbtnMinimizeMaxDev.setSelected(true);
		rdbtnMinimizeMaxDev.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		rdbtnMinimizeMaxDev.setBounds(6, 220, 169, 23);
		
		panel_2.add(rdbtnMinimizeMaxDev);
		rdbtnMinimizeMeanDev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.minimize_absolute_deviation = !rdbtnMinimizeMaxDev.isSelected();
			}
		});
		rdbtnMinimizeMeanDev.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		rdbtnMinimizeMeanDev.setBounds(6, 244, 169, 23);
		
		panel_2.add(rdbtnMinimizeMeanDev);
		buttonGroupPopMinMethod.add(rdbtnMinimizeMeanDev);
		buttonGroupPopMinMethod.add(rdbtnMinimizeMaxDev);
		
		chckbxNewCheckBox_1 = new JCheckBox("constrain");
		chckbxNewCheckBox_1.setToolTipText("This adds an extra mutation step to non-contiguous regions at the cost of slower evolution.");
		chckbxNewCheckBox_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_disconnected = chckbxNewCheckBox_1.isSelected();
				ip.addHistory("SET CONSTRAIN CONTIGUITY  "+(chckbxNewCheckBox_1.isSelected() ? "TRUE" : "FALSE"));
			}
		});
		chckbxNewCheckBox_1.setFont(new Font("Tahoma", Font.PLAIN, 9));
		chckbxNewCheckBox_1.setBounds(132, 32, 62, 23);
		panel_2.add(chckbxNewCheckBox_1);
		
		chckbxConstrain = new JCheckBox("constrain");
		chckbxConstrain.setToolTipText("This rejects mutations that increase population imbalance at the cost of slower evolution.");
		chckbxConstrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.mutate_excess_pop = chckbxConstrain.isSelected();
				Settings.mutate_good = chckbxConstrain.isSelected() && chckbxConstrain_1.isSelected();
				ip.addHistory("SET CONSTRAIN POPULATION  "+(chckbxConstrain.isSelected() ? "TRUE" : "FALSE"));
			}
		});
		chckbxConstrain.setFont(new Font("Tahoma", Font.PLAIN, 9));
		chckbxConstrain.setBounds(132, 154, 62, 23);
		panel_2.add(chckbxConstrain);
		
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setBounds(200, 0, 200, 399);
		panel.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblPopulation = new JLabel("Population");
		lblPopulation.setBounds(6, 40, 104, 16);
		panel_3.add(lblPopulation);
		evolutionPopulationTF.setBounds(105, 34, 91, 28);
		panel_3.add(evolutionPopulationTF);
		evolutionPopulationTF.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				evolutionPopulationTF.postActionEvent();
			}
		});
		evolutionPopulationTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 int n = new Integer(evolutionPopulationTF.getText());
					 if( n == Settings.population) {
						 return;
					 }
					 Settings.population = n;
					 ip.addHistory("SET EVOLUTION POPULATION "+Settings.population);
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		evolutionPopulationTF.setText(""+Settings.population);
		evolutionPopulationTF.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Evolution");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(6, 6, 159, 16);
		panel_3.add(lblNewLabel);
		JLabel lblBorderMutation = new JLabel("% mutation");
		lblBorderMutation.setBounds(6, 138, 172, 16);
		panel_3.add(lblBorderMutation);
		slider_mutation.setBounds(6, 165, 190, 29);
		panel_3.add(slider_mutation);
		
		lblElitism = new JLabel("% elitism");
		lblElitism.setBounds(6, 265, 69, 16);
		panel_3.add(lblElitism);
		
		sliderElitism = new JSlider();
		sliderElitism.setToolTipText("<html>Elitism involves copying a small proportion of the fittest candidates, unchanged, into the <br/>next generation. This can sometimes have a dramatic impact on performance by ensuring <br/>that the EA does not waste time re-discovering previously discarded partial solutions. <br/>Candidate solutions that are preserved unchanged through elitism remain eligible for <br/>selection as parents when breeding the remainder of the next generation.<br/>\r\nSo basically it takes a small fraction of the best candidates, and copies them over unchanged <br/>to the next generation.  So these are essential your immortals -- every one else only lasts one <br/>generation.<br/><br/>\r\nExperimentally, about 25% elitism seems to work fine.<br/><br/>\r\nThere is also be a slider \"% elites mutated\".  Notice the description above is that the elites <br/>remain unchanged between generations.  With mutate elites selected, the elites will slowly <br/>mutate along with the rest of the population. This helps it search a little faster, but when it <br/>gets down to fine-tuning, where you only want the very best, you want to turn this off, as <br/>otherwise you'd just be hovering around the best.<br/></html>");
		sliderElitism.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.elite_fraction = ((double)sliderElitism.getValue())/100.0;
				ip.addHistory("SET EVOLUTION ELITE_FRAC "+Settings.elite_fraction);

			}
		});
		sliderElitism.setValue((int)(Settings.elite_fraction*100.0));
		sliderElitism.setBounds(6, 292, 190, 29);
		panel_3.add(sliderElitism);
		chckbxAutoAnneal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.auto_anneal = chckbxAutoAnneal.isSelected();
				slider_anneal.setEnabled(Settings.auto_anneal);
			}
		});
		chckbxAutoAnneal.setSelected(true);
		chckbxAutoAnneal.setBounds(6, 67, 159, 23);
		
		panel_3.add(chckbxAutoAnneal);
		lblElitisesMutated.setBounds(6, 332, 172, 16);
		
		panel_3.add(lblElitisesMutated);
		sliderElitesMutated.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.elite_mutate_fraction = ((double)sliderElitesMutated.getValue())/100.0;
				ip.addHistory("SET EVOLUTION ELITE_MUTATE_FRAC "+Settings.elite_mutate_fraction);

			}
		});
		sliderElitesMutated.setValue(100);
		sliderElitesMutated.setBounds(6, 359, 190, 29);
		
		panel_3.add(sliderElitesMutated);
		
		slider_anneal = new JSlider();
		slider_anneal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.anneal_rate = Math.exp(3.0*((double)slider_anneal.getValue()-50.0)/50.0);
				ip.addHistory("SET EVOLUTION ANNEAL_RATE "+(((double)slider_anneal.getValue())/100.0));

				//System.out.println("anneal rate set to: "+Settings.anneal_rate);
			}
		});
		slider_anneal.setBounds(6, 97, 190, 29);
		panel_3.add(slider_anneal);
		
		chckbxRecombination = new JCheckBox("Recombination");
		chckbxRecombination.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.recombination_on = chckbxRecombination.isSelected();
			}
		});
		chckbxRecombination.setSelected(true);
		chckbxRecombination.setBounds(6, 228, 128, 23);
		panel_3.add(chckbxRecombination);
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
					ip.addHistory("SET DISTRICTS NUM_DISTRICTS "+Settings.num_districts);
				} catch (Exception ex) { }
			}
		});
		textFieldNumDistricts.setColumns(10);
		textFieldNumDistricts.setBounds(136, 175, 52, 28);
		panel.add(textFieldNumDistricts);
		
		JLabel lblNumOfDistricts = new JLabel("Num. of districts");
		lblNumOfDistricts.setBounds(10, 181, 124, 16);
		panel.add(lblNumOfDistricts);
		
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop evolving a solution.");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopEvolving();
			}
		});
		stopButton.setBounds(10, 135, 83, 29);
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
				ip.addHistory("GO\n");
			}
		});
		goButton.setBounds(113, 135, 83, 29);
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
					ip.addHistory("SET DISTRICTS SEATS_PER_DISTRICT "+Settings.seats_number_per_district);
				} catch (Exception ex) { }
			}
		});
		textFieldSeatsPerDistrict.setText("1");
		textFieldSeatsPerDistrict.setColumns(10);
		textFieldSeatsPerDistrict.setBounds(136, 212, 52, 28);
		panel.add(textFieldSeatsPerDistrict);
		
		lblMembersPerDistrict = new JRadioButton("Seats/district");
		lblMembersPerDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSeatsMode();
			}
		});
		lblMembersPerDistrict.setSelected(true);
		seatsModeBG.add(lblMembersPerDistrict);
		lblMembersPerDistrict.setBounds(10, 218, 124, 16);
		panel.add(lblMembersPerDistrict);
		
		comboBoxPopulation.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxPopulation) return;
				setPopulationColumn((String)comboBoxPopulation.getSelectedItem());
			}
		});
		comboBoxPopulation.setBounds(12, 343, 178, 20);
		panel.add(comboBoxPopulation);
		
		JLabel lblPopulationColumn = new JLabel("Population column");
		lblPopulationColumn.setBounds(12, 323, 182, 16);
		panel.add(lblPopulationColumn);
		
		lblDistrictColumn = new JLabel("District column");
		lblDistrictColumn.setBounds(8, 503, 182, 16);
		panel.add(lblDistrictColumn);
		
		comboBoxDistrictColumn = new JComboBox();
		comboBoxDistrictColumn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxDistrict) return;
				setDistrictColumn((String)comboBoxDistrictColumn.getSelectedItem());
				ip.addHistory("SET DISTRICTS COLUMN "+comboBoxDistrictColumn.getSelectedItem());
			}
		});
		comboBoxDistrictColumn.setBounds(8, 522, 178, 20);
		panel.add(comboBoxDistrictColumn);
		
		panel_4 = new JPanel();
		panel_4.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_4.setLayout(null);
		panel_4.setBounds(400, 329, 200, 320);
		panel.add(panel_4);
		
		JLabel lblContiguency = new JLabel("Proportional");
		lblContiguency.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblContiguency.setBounds(10, 100, 104, 16);
		lblContiguency.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/representativeness_tooltip.png") + "\">");
		panel_4.add(lblContiguency);
		sliderRepresentation.setBounds(10, 120, 180, 29);
		sliderRepresentation.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/representativeness_tooltip.png") + "\">");
		panel_4.add(sliderRepresentation);
		sliderWastedVotesTotal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.competitiveness_weight = sliderWastedVotesTotal.getValue()/100.0;
				ip.addHistory("SET WEIGHT COMPETITION "+Settings.competitiveness_weight);
			}
		});
		sliderWastedVotesTotal.setBounds(11, 60, 180, 29);
		sliderWastedVotesTotal.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/wasted_votes.png") + "\">");
		
		panel_4.add(sliderWastedVotesTotal);
		lblWastedVotes.setBounds(11, 40, 84, 16);
		lblWastedVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/wasted_votes.png") + "\">");
		
		panel_4.add(lblWastedVotes);
		
		lblSeatsVotes = new JLabel("Anti-partisan gerrymandering");
		lblSeatsVotes.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblSeatsVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/seats_votes_asymmetry_tooltip.png") + "\">");
		lblSeatsVotes.setBounds(10, 160, 179, 16);
		panel_4.add(lblSeatsVotes);
		
		sliderSeatsVotes = new JSlider();
		sliderSeatsVotes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.seats_votes_asymmetry_weight = sliderSeatsVotes.getValue()/100.0;
				ip.addHistory("SET WEIGHT PARTISAN "+Settings.seats_votes_asymmetry_weight);
			}
		});
		sliderSeatsVotes.setToolTipText("<html><img src=\"" + Applet.class.getResource("/resources/seats_votes_asymmetry_tooltip.png") + "\">");
		sliderSeatsVotes.setBounds(10, 180, 180, 29);
		panel_4.add(sliderSeatsVotes);
		
		lblGeometricFairness = new JLabel("Geometric <===> Fairness");
		lblGeometricFairness.setHorizontalAlignment(SwingConstants.CENTER);
		lblGeometricFairness.setBounds(410, 11, 180, 16);
		panel.add(lblGeometricFairness);
		
		sliderBalance = new JSlider();
		sliderBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.geo_or_fair_balance_weight = sliderBalance.getValue()/100.0;
				ip.addHistory("SET WEIGHT GEOMETRY_FAIRNESS "+Settings.geo_or_fair_balance_weight);
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
		
		panel_5 = new JPanel();
		panel_5.setBounds(200, 397, 200, 252);
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
				ip.addHistory("SET WEIGHT SPLITS "+Settings.split_reduction_weight);
			}
		});
		sliderSplitReduction.setBounds(10, 157, 180, 29);
		panel_5.add(sliderSplitReduction);
		
		srcomboBoxCountyColumn = new JComboBox();
		srcomboBoxCountyColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hushcomboBoxCountyColumn) {
					return;
				}
				MainFrame.mainframe.project.county_column = (String)srcomboBoxCountyColumn.getSelectedItem();
				setCountyColumn();
				mntmColorByCounty.setEnabled(MainFrame.mainframe.project.county_column != null && MainFrame.mainframe.project.county_column.length() > 0);
				mntmColorBySplits.setEnabled(MainFrame.mainframe.project.county_column != null && MainFrame.mainframe.project.county_column.length() > 0);
				panelStats.getStats();
			}
		});
		srcomboBoxCountyColumn.setBounds(8, 56, 178, 20);
		panel_5.add(srcomboBoxCountyColumn);
		
		srlblCountyColumn = new JLabel("County column");
		srlblCountyColumn.setBounds(8, 37, 182, 16);
		panel_5.add(srlblCountyColumn);
		
		chckbxReduceSplits = new JCheckBox("Reduce splits");
		chckbxReduceSplits.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
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
				ip.addHistory("SET WEIGHT COUNT_SPLITS "+(chckbxReduceSplits.isSelected() ? "TRUE" : "FALSE"));
			}
		});
		chckbxReduceSplits.setFont(new Font("Tahoma", Font.BOLD, 14));
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
		rdbtnReduceTotalSplits.setSelected(true);
		rdbtnReduceTotalSplits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.minimize_number_of_counties_split = rdbtnReduceSplitCounties.isSelected();
			}
		});
		rdbtnReduceTotalSplits.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		rdbtnReduceTotalSplits.setBounds(10, 198, 172, 23);
		
		panel_5.add(rdbtnReduceTotalSplits);
		rdbtnReduceSplitCounties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.minimize_number_of_counties_split = rdbtnReduceSplitCounties.isSelected();
			}
		});
		rdbtnReduceSplitCounties.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		rdbtnReduceSplitCounties.setBounds(10, 223, 172, 23);
		
		panel_5.add(rdbtnReduceSplitCounties);
		
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
		textFieldTotalSeats.setBounds(136, 246, 52, 28);
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
					ip.addHistory("SET DISTRICTS FAIRVOTE_SEATS "+Settings.seats_number_total);
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
		lblTotalSeats.setBounds(10, 252, 124, 16);
		
		panel.add(lblTotalSeats);
		sliderRepresentation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				//Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;
				Settings.diagonalization_weight = sliderRepresentation.getValue()/100.0;
				ip.addHistory("SET WEIGHT PROPORTIONAL "+Settings.diagonalization_weight);


			}
		});
		slider_mutation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				//Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_mutation.getValue()/100.0;;
				if( Settings.hush_mutate_rate) {
					return;
				}
				Settings.mutation_boundary_rate = Math.exp((slider_mutation.getValue()-100)/Settings.exp_mutate_factor);
				ip.addHistory("SET EVOLUTION MUTATE_RATE "+(((double)slider_mutation.getValue())/100.0));
			}
		});
		sliderPopulationBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.population_balance_weight = sliderPopulationBalance.getValue()/100.0;
				ip.addHistory("SET WEIGHT POPULATION "+Settings.population_balance_weight);

			}
		});
		sliderBorderLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.geometry_weight = sliderBorderLength.getValue()/100.0;
				ip.addHistory("SET WEIGHT COMPACTNESS "+Settings.geometry_weight);

			}
		});

		chckbxmntmMutateExcessPopOnly = new JCheckBoxMenuItem("Mutate overpopulated only");
		chckbxmntmMutateExcessPopOnly.setSelected(Settings.mutate_overpopulated);
		chckbxmntmMutateExcessPopOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.mutate_overpopulated = chckbxmntmMutateExcessPopOnly.isSelected();
			}
		});

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
		splitReductionType.add(rdbtnReduceTotalSplits);
		splitReductionType.add(rdbtnReduceSplitCounties);
		
		rdbtnTruncationSelection = new JRadioButton("Truncation");
		rdbtnTruncationSelection.setBounds(6, 634, 188, 23);
		panel.add(rdbtnTruncationSelection);
		rdbtnTruncationSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.TRUNCATION_SELECTION;
				//tournamentSlider.setVisible(false);
			}
		});
		
		rdbtnTruncationSelection.setSelected(Settings.SELECTION_MODE == Settings.TRUNCATION_SELECTION);
		
		selectionType.add(rdbtnTruncationSelection);
		
		rdbtnRankSelection = new JRadioButton("Rank");
		rdbtnRankSelection.setBounds(6, 660, 188, 23);
		panel.add(rdbtnRankSelection);
		rdbtnRankSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.RANK_SELECTION;
				//tournamentSlider.setVisible(false);
			}
		});
		rdbtnRankSelection.setSelected(Settings.SELECTION_MODE == Settings.RANK_SELECTION);
		selectionType.add(rdbtnRankSelection);
		
		rdbtnRouletteSelection = new JRadioButton("Roulette");
		rdbtnRouletteSelection.setBounds(6, 688, 188, 23);
		panel.add(rdbtnRouletteSelection);
		rdbtnRouletteSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.SELECTION_MODE = Settings.ROULETTE_SELECTION;
				//tournamentSlider.setVisible(false);
			}
		});
		rdbtnRouletteSelection.setSelected(Settings.SELECTION_MODE == Settings.ROULETTE_SELECTION);
		selectionType.add(rdbtnRouletteSelection);
		
		comboBoxQuota = new JComboBox();
		comboBoxQuota.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = (String) comboBoxQuota.getSelectedItem();
				if( s.equals("DROOP")) { 
					Settings.quota_method = Settings.QUOTA_METHOD_DROOP;
				} else {
					Settings.quota_method = Settings.QUOTA_METHOD_HARE;
				}
			}
		});
		comboBoxQuota.setBounds(56, 284, 136, 27);
		comboBoxQuota.addItem("DROOP");
		comboBoxQuota.addItem("HARE");
		panel.add(comboBoxQuota);
		
		lblQuotaMethod = new JLabel("Quota");
		lblQuotaMethod.setBounds(10, 288, 52, 16);
		panel.add(lblQuotaMethod);
		
		btnMultielectionColumns = new JButton("Multi-Election columns");
		btnMultielectionColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean is_evolving = evolving;
				if( is_evolving) { featureCollection.ecology.stopEvolving(); }
				setDistrictColumn(project.district_column);
				//featureCollection.loadDistrictsFromProperties(project.district_column);
				DialogManageElections dlg = new DialogManageElections("Multiple election column selection"
						,featureCollection.getHeaders()
						,project.multiElections
						,project.multiImputators
						);
				//dlg.setData(featureCollection,project.election_columns);
				dlg.show();
				if( !dlg.ok) {
					if( is_evolving) { featureCollection.ecology.startEvolving(); }
					return;
				}

				try {
					project.multiElections = dlg.elections;
					project.multiImputators = dlg.imputators;
					project.election_columns = dlg.elections.get(0);
					setElectionColumns();
					//setElectionColumns();
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
		});
		btnMultielectionColumns.setBounds(8, 409, 184, 23);
		panel.add(btnMultielectionColumns);
		
		chckbxParetoFront = new JCheckBox("Pareto front");
		chckbxParetoFront.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.paretoMode = chckbxParetoFront.isSelected();
			}
		});
		chckbxParetoFront.setBounds(32, 599, 128, 23);
		panel.add(chckbxParetoFront);
		
		btnInit.setBounds(138, 42, 52, 23);
		btnInit.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.add(btnInit);
		btnInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String method = (String)comboBoxInitMethod.getSelectedItem();
				ip.addHistory("INIT "+method);
				
				featureCollection.ecology.population = new Vector<DistrictMap>();
				featureCollection.ecology.swap_population = new Vector<DistrictMap>();
			}
		});


		
		comboBoxInitMethod.setBounds(10, 43, 124, 20);
		panel.add(comboBoxInitMethod);
		comboBoxInitMethod.addItem("Contiguous");
		comboBoxInitMethod.addItem("Random");
		comboBoxInitMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Ecology.initMethod = (String)comboBoxInitMethod.getSelectedItem();
			}
		});
		
		rdbtnRouletteSelection.setVisible(false);
		rdbtnRankSelection.setVisible(false);
		
		rdbtnTruncationSelection.setVisible(false);
		



		//Settings.speciation_fraction = 0.5;//1.0;
		//Settings.disconnected_population_weight = 0.0;
		featuresPanel.setPreferredSize(new Dimension(100,100));
		featuresPanel.setSize(new Dimension(100,100));
			
		splitPane.setRightComponent(splitPane2);
		
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane2.setTopComponent(seatsPanel);
		splitPane_1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane_1.setLeftComponent(mapPanel);
		splitPane_1.setRightComponent(featuresPanel);
		splitPane_1.setResizeWeight(1.0);
	
		splitPane2.setBottomComponent(splitPane_1);
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
		frameSeatsVotesChart.move(this.getWidth(), this.getX()+frameGraph.getHeight());
		frameStats.move(this.getWidth()+frameSeatsVotesChart.getWidth(), this.getX()+frameGraph.getHeight());
		if( !Applet.no_gui) {
			frameGraph.show();
			frameSeatsVotesChart.show();
			frameStats.show();
		}
		JRadioButtonMenuItem[] mapModes = new JRadioButtonMenuItem[]{
				mntmShowVoteBalance,
				mntmShowDemographics,
				mntmColorByDistrict,
				mntmColorByPop,
				mntmColorByCompactness,
				mntmColorByVote,
				mntmColorByCounty,
				mntmColorBySplits,
				mntmShowVoteBalance,
				mntmColorDistDemo,
				mntmColorPartisanPacking,
				mntmColorByVtd,
				mntmColorByCentered,
				mntmShowDemographics,
				mntmColorRacialPacking,
				mntmColorByVtd_1,
				mntmColorByCentered_1,
				mntmColorByWasted,
				rdbtnmntmColorByDescr,
				rdbtnmntmColorBySeats,
			};
			for( int i = 0; i < mapModes.length; i++) {
				groupColoringMode.add(mapModes[i]);
			}
			mntmColorByDistrict.setSelected(true);
		
	}
	public boolean hush_setSeatsMode = false;
	public JCheckBox chckbxNewCheckBox = new JCheckBox("No 4 seat districts");
	public JLabel srlblMuniColumn = new JLabel("Muni column");
	public JComboBox srcomboBoxMuniColumn = new JComboBox();
	public JCheckBoxMenuItem chckbxmntmSimplifyPolygons;
	public JRadioButton rdbtnReduceTotalSplits = new JRadioButton("Reduce total splits");
	public JRadioButton rdbtnReduceSplitCounties = new JRadioButton("Reduce split counties");
	public ButtonGroup splitReductionType = new ButtonGroup();
	public JCheckBox chckbxAutoAnneal = new JCheckBox("auto anneal");
	public JLabel lblElitisesMutated = new JLabel("% elites mutated");
	public JSlider sliderElitesMutated = new JSlider();
	public JRadioButton rdbtnMinimizeMaxDev = new JRadioButton("Minimize squared dev.");
	public JRadioButton rdbtnMinimizeMeanDev = new JRadioButton("Minimize absolute dev.");
	public JMenuItem mntmCopyColumn;
	public JSeparator separator_9;
	public JSlider slider_anneal;
	public JCheckBox chckbxNewCheckBox_1;
	public JCheckBox chckbxConstrain;
	public JCheckBox chckbxConstrain_1;
	public JMenuItem mntmShowScripts;
	public JRadioButtonMenuItem mntmColorByCounty;
	public JMenuItem mntmMergeInElection;
	public JRadioButtonMenuItem mntmColorBySplits;
	public JMenu mnMerge;
	public JMenuItem mntmCountyVote;
	public JMenuItem mntmVtdVote;
	public JMenuItem mntmBlocklevelCurrentDistricts;
	public JMenuItem mntmBlocklevelBdistricting;
	public JSeparator separator_12;
	public JMenuItem mntmBlocklevelPopulation;
	public JSeparator separator_13;
	public JMenuItem mntmBlocklevelEthnicData;
	public JMenuItem mntmImportBlockFile;
	public JMenuItem mntmExportBlockFile;
	public JSeparator separator_14;
	public JSeparator separator_10;
	public JMenuItem mntmExportNationalMaps;
	public JComboBox comboBoxQuota;
	public JLabel lblQuotaMethod;
	public JSeparator separator_15;
	public JCheckBoxMenuItem chckbxmntmOutlineState;
	public JCheckBoxMenuItem mntmOutlineDistricts;
	public JCheckBoxMenuItem mntmOutlineCounties;
	public JCheckBoxMenuItem chckbxmntmFitToNation;
	public JLabel lblDescriptiveRepr;
	public JSlider slider;
	public JSeparator separator_16;
	public JCheckBoxMenuItem chckbxmntmDividePackingBy;
	public JRadioButtonMenuItem mntmColorByVtd;
	public JRadioButtonMenuItem mntmColorByVtd_1;
	public JSeparator separator_17;
	public JSeparator separator_18;
	public JRadioButtonMenuItem mntmColorByCentered;
	public JRadioButtonMenuItem mntmColorByCentered_1;
	public JMenuItem mntmExportPieCharts;
	public JRadioButtonMenuItem rdbtnmntmColorByDescr;
	public JRadioButtonMenuItem rdbtnmntmColorBySeats;
	public JSplitPane splitPane_1;
	public JMenuItem mntmShowPieCharts;
	public JCheckBox chckbxRecombination;
	public JButton btnMultielectionColumns;
	public JMenuItem mntmShowAdvancedStats;
	public JMenuItem mntmExplainStats;
	public JCheckBox chckbxParetoFront;
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
		double num_features = featureCollection.features.size();
		double combinations = num_features * Math.log((double)Settings.num_districts);
		System.out.println("log combinations: "+combinations);
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
		double d = Settings.mutation_boundary_rate;
 		evolutionPopulationTF.setText(""+Settings.population);
 		double e = Math.log(Settings.mutation_boundary_rate)*Settings.exp_mutate_factor+100;
 		//double d = Math.exp((slider_mutation.getValue()-100)/Settings.exp_mutate_factor);
		//System.out.println("new boundary mutation rate: "+Settings.mutation_boundary_rate+" total: "+total+" mutated: "+mutated);
		slider_mutation.setValue((int)e);
		Settings.mutation_boundary_rate = d;
		//slider_mutation.setValue((int)(Settings.mutation_boundary_rate*100.0/MainFrame.boundary_mutation_rate_multiplier));
		invalidate();
		repaint();
	}
	@Override
	public void eventOccured() {
		if( project.num_generations > 0 && featureCollection.ecology.generation >= project.num_generations) {
			stopEvolving();
			System.out.println("collecting districts...");
			featureCollection.storeDistrictsToProperties(project.district_column);
			System.out.println("getting headers...");
			String[] headers = featureCollection.getHeaders();
			System.out.println("getting data...");
			String[][] data = featureCollection.getData(headers);

			if( project.save_to != null && project.save_to.length() > 0) {
				String filename = project.save_to;
				String ext = project.save_to.substring(project.save_to.length()-3).toLowerCase();
				if( ext.equals("dbf")) {
					FileUtil.writeDBF(filename,featureCollection.getDBFields(),data);					
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
		ip.addHistory("STOP\n");
	}
	public void saveData(File f, int opt, boolean confirm) {
		if( opt < 0) {
			String[] options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)",".dbf (dbase file)"};
			if( project.source_file.substring(project.source_file.length()-4).toLowerCase().equals(".shp")) {
				//options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)"};//,".dbf (in separate dbase file)",".dbf (in original shapefile)"};
			}
			opt = JOptionPane.showOptionDialog(mainframe, "Select desired format to save in.", "Select format", 0,0,null,options,options[0]);
			if( opt < 0) {
				System.out.println("aborted.");
				return;
			}
		}
		System.out.println("collecting districts...");
		featureCollection.storeDistrictsToProperties(project.district_column);
		System.out.println("getting headers...");
		String[] headers = featureCollection.getHeaders();
		
		System.out.println("getting data...");
		String[][] data = featureCollection.getData(headers);
		
		if( opt == 3 || opt == 2) {
			String filename = "";
			opt = 2;
			if( opt == 3) {
				filename = project.source_file.substring(0,project.source_file.length()-3)+"dbf";
				f = new File(filename);
				if( f == null)  {
					return;
				}
			} else if( opt == 2) {
				if( f == null) {
					JFileChooser jfc = new JFileChooser();
					jfc.setCurrentDirectory(new File(Download.getStartPath()));
					jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbase file","dbf"));
					jfc.showSaveDialog(null);
					f = jfc.getSelectedFile();
					if( f == null)  {
						return;
					}
				}
				filename = f.getAbsolutePath().trim();
			}
			if( !filename.toLowerCase().substring(filename.length()-4).equals(".dbf")) {
				filename += ".dbf";
			}
			System.out.println("writedbf start.");
			FileUtil.writeDBF(filename,featureCollection.getDBFields(),data);					
			System.out.println("writedbf done.");
		} else {
			String delimiter = opt == 1 ? "\t" : ",";
			if( f == null) {
			JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(Download.getStartPath()));
				if( opt == 0) { 
					jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
				} else if( opt == 1) {
					jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
				}
				jfc.showSaveDialog(null);
				f = jfc.getSelectedFile();
				if( f == null)  {
					System.out.println("f is null.");
					return;
				}
				String s = f.getAbsolutePath();
				if( !s.substring(s.length()-4).toLowerCase().equals(opt == 0 ? ".csv" : ".txt")) {
					s += opt == 0 ? ".csv" : ".txt";
					f = new File(s);
				}
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
		if( confirm) {
			JOptionPane.showMessageDialog(mainframe,"File saved.");
		}
	}
	
	public static Vector processFeaturerenameFile() {
		String path = "http://www2.census.gov/geo/docs/reference/codes/files/national_vtd.txt";
	    URL url;
	    InputStream is = null;
	    BufferedReader br;
	    String line;
        Vector<String[]> v = new Vector<String[]>();

	    try {
	        url = new URL(path);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));
	        v.add( new String[]{"STATE","STATEFP","COUNTYFP","COUNTY_NAME","VTDST","VTDNAME","CTYFP_FUL","VTDST_FUL","VTDST_HLF","GEO"});
	        int i = 0;

	        while ((line = br.readLine()) != null) {
	        	if( i == 0) { //skip first line
	        		i++;
	        		continue;
	        	}
	        	String[] row = line.split("\\|");//[|]");
	        	if( row.length < 6) {
	        		continue;
	        	}
	            //System.out.println(line);
	        	String[] ss = new String[]{
	        			row[0],
	        			row[1],
	        			row[2],
	        			row[3],
	        			row[4],
	        			row[5],
	        			row[1]+row[2],
	        			row[1]+row[2]+row[4],
	        			row[2]+row[4],
	        			row[1]+row[2]+row[4],
	        	};
	        	
	        	v.add(ss);
        		i++;
	        }
	    } catch (Exception mue) {
	    	System.out.print(mue);
	         mue.printStackTrace();
	    }
        try {
            if (is != null) is.close();
        } catch (IOException ioe) {
            // nothing to see here
        }

	    return v;
	}
	public void trySetBasicColumns() {
		String[] trys = new String[]{"POPULATION","POP18","VAP","VAP_TOTAL"};
		for( int i = 0; i < trys.length; i++) {
			if(((DefaultComboBoxModel)comboBoxPopulation.getModel()).getIndexOf(trys[i]) >= 0 ) {
				System.out.println(" found "+trys[i]);
				comboBoxPopulation.setSelectedItem(trys[i]);
				setPopulationColumn(trys[i]);
				break;
			}
		}
		trys = new String[]{"COUNTY","COUNTY_NAM","COUNTY_NAME","COUNTYFP","COUNTYFP10"};
		for( int i = 0; i < trys.length; i++) {
			if(((DefaultComboBoxModel)srcomboBoxCountyColumn.getModel()).getIndexOf(trys[i]) >= 0 ) {
				System.out.println(" found "+trys[i]);
				srcomboBoxCountyColumn.setSelectedItem(trys[i]);
				chckbxReduceSplits.setSelected(true);
				//chckbxReduceSplits.postEvent(new ActionEvent(null, 0, ""));
				break;
			}
		}
		//comboBoxDistrictColumn.setSelectedItem("AR_RESULT");
		//setDistrictColumn("AR_RESULT");
	}


	public void trySetGroupColumns() {
		project.demographic_columns.clear();
		project.election_columns.clear();
		project.election_columns_2.clear();
		project.election_columns_3.clear();
		project.substitute_columns.clear();
		
		try {
			setDemographicColumns();
			setElectionColumns();
			setElectionColumns2();
			setElectionColumns3();
		} catch (Exception ex) { 
			ex.printStackTrace();
		}


		String[] headers = featureCollection.getHeaders();
		String[] demo = new String[]{
				"CTY_VAP_WHITE","CTY_VAP_BLACK","CTY_VAP_HISPANIC","CTY_VAP_ASIAN","CTY_VAP_INDIAN","CTY_VAP_OTHER",
				"CTY_VAP_WH","CTY_VAP_BL","CTY_VAP_HI","CTY_VAP_AS","CTY_VAP_IN","CTY_VAP_OT",
				"VAP_WHITE","VAP_BLACK","VAP_HISPANIC","VAP_HISPAN","VAP_ASIAN","VAP_HAWAII","VAP_INDIAN","VAP_MULTI","VAP_OTHER",
				};
		String[] elect = new String[]{
				"CTY_PRES12_DEM","CTY_PRES12_REP",
				"PRES12_DEM","PRES12_REP",
				};

		for( int i = 0; i < elect.length; i++) {
			for( int j = 0; j < headers.length; j++) {
				if( headers[j].equals(elect[i])) {
					project.election_columns.add(elect[i]);
				}
			}
		}

		for( int i = 0; i < demo.length; i++) {
			for( int j = 0; j < headers.length; j++) {
				if( headers[j].equals(demo[i])) {
					project.demographic_columns.add(demo[i]);
				}
			}
		}

		try {
			setDemographicColumns();
			setElectionColumns();
			setElectionColumns2();
			setElectionColumns3();
		} catch (Exception ex) { 
			ex.printStackTrace();
		}
		//setSubstituteColumns();
	}
	public OpenShapeFileThread createOpenShapeFileThread(File vtd_file) {
		return new OpenShapeFileThread(vtd_file);
	}
	
	
	public int[] joinToTxt( boolean test, String path, String primary_key, int foreign_key, int start_row, int[] cols, String[] names) {
		boolean try_trim_zeros = false;

		int found = 0;
		int not_found = 0;
		System.out.println("url: "+path);
		System.out.println("0");
		URL url;
		InputStream is = null;
		System.out.println("0.0");

		BufferedReader br;
		String line;
		System.out.println("0.1");

		Vector<String[]> v = new Vector<String[]>();

		Hashtable<String,VTD> dictionary = new Hashtable<String,VTD>();
		for( int i = 0; i < featureCollection.features.size(); i++) {
			VTD f =  featureCollection.features.get(i);
			Object o = f.properties.get(primary_key);
			if( o == null) {
				System.out.println("primary key not found!");
			}
			dictionary.put(f.properties.get(primary_key).toString(),f);
		}
		int total_edit_distance = 0;
		int total_match_distance = 0;
		
		try {
			System.out.println("1");
			url = new URL(path);
			System.out.println("2");
			is = url.openStream();  // throws an IOException
			System.out.println("3");
			br = new BufferedReader(new InputStreamReader(is));
			System.out.println("4..");

			while ((line = br.readLine()) != null) {
				System.out.print(".");
				//System.out.println(line);
				String[] ss = line.split("\t");
				for( int i = 0; i < ss.length; i++) {
					ss[i] = ss[i].replaceAll("\"", "").replaceAll(",", "").replaceAll("\\n", "").replaceAll("\\r", "").trim().toUpperCase();
				}
				v.add(ss);
			}
		} catch (Exception mue) {
			System.out.print("ex "+mue);
			mue.printStackTrace();
			return new int[]{0,0};
		}
		try {
			if (is != null) is.close();
		} catch (IOException ioe) {
			// nothing to see here
		}
		System.out.println("5..");
		
		if( start_row == 1) {
			String[] header = v.get(0);
			int vtd_start = -1;
			int pres_start = -1;
			for( int i = 0; i < header.length; i++) {
				String s = header[i].toUpperCase().trim();
				if( vtd_start < 0) {
					if( header[i].equals("VTD NAME") || header[i].equals("NAME")) {
						vtd_start = i;
					}
				}
				if( pres_start < 0) {
					if( header[i].equals("OBAMA") || header[i].equals("BARACK OBAMA")) {
						pres_start = i;
					}
				}
				if( pres_start >= 0 && vtd_start >= 0) {
					break;
				}
			}
			if( pres_start >= 0 && vtd_start >= 0) {
				foreign_key = vtd_start;
				cols = new int[]{pres_start,pres_start+1,pres_start+2,pres_start+3,pres_start+4,pres_start+5};
			}
		}
		for( int i = 0; i < start_row; i++) {
			v.remove(0);
		}
		
		//collect counties;
		HashMap<String,Vector<VTD>> county_feats = new HashMap<String,Vector<VTD>>();
		HashMap<String,Integer> county_pops = new HashMap<String,Integer>();
		for( VTD feat : featureCollection.features) {
			try {
			String county = (String) feat.properties.get(primary_key);
			if( county == null) {
				System.out.println("no county found!");
				continue;
			}
			county = county.trim().toUpperCase();
			
			Vector<VTD> vf = county_feats.get(county);
			if( vf == null) { 
				vf = new Vector<VTD>();
				county_feats.put(county,vf);
			}
			vf.add(feat);
			Integer i = county_pops.get(county);
			if( i == null) { 
				i = new Integer(0);
				county_pops.put(county,i);
			}
			if( feat.properties.POPULATION == 0) {
				Double d = Double.parseDouble(feat.properties.get(project.population_column).toString());
				feat.properties.POPULATION = d.intValue();
			}
			i += feat.properties.POPULATION;
			county_pops.put(county,i);
			} catch (Exception ex) {
				System.out.println("ex "+ex);
				ex.printStackTrace();
			}
		}
		
		//now deaggregate proportional
		System.out.println("deaggregating proportional...");

		for( String[] ss : v) {
			String[] tries = new String[]{" COUNTY"," PARISH"," BOROUGH"," CENSUS AREA"," MUNICIPALITY"," VOTING DISTRICT"};
			try {
			String incounty = ss[foreign_key].trim().toUpperCase();
			Vector<VTD> vf = county_feats.get(incounty);
			if( vf == null) {
				if( try_trim_zeros ) {
					while( incounty.length() > 1 && incounty.substring(0,1).equals("0")) {
						incounty = incounty.substring(1);
					}
				}
				vf = county_feats.get(incounty);
				if( vf == null) {
					incounty = ss[foreign_key].trim().toUpperCase();
					vf = county_feats.get(incounty);
					if( vf == null) {
						for( int i = 0; i < tries.length; i++) {
							vf = county_feats.get(incounty+tries[i]);
							if( vf != null) {
								incounty += tries[i];
								break;
							}
						}
						/*
						if( vf == null) {
								System.out.println("not found!: "+incounty);
								not_found++;
								continue;
						}*/
					}
				}
			}
			if( vf != null) {
				total_match_distance += incounty.length();
			}
			if( vf == null) {
				Triplet<String,VTD,Integer> best_match = util.Util.findBestMatch(incounty,dictionary);
				if( best_match == null) {
					System.out.println("not found!: "+incounty);
					not_found++;
					total_edit_distance += incounty.length();
					continue;
				} else {
					found++;
					incounty = best_match.a;
					total_edit_distance += best_match.c;
					total_match_distance += incounty.length() - best_match.c;
				}
			}	
			
			double total_pop = (double)county_pops.get(incounty);
			System.out.println("found!: "+incounty+" "+total_pop);
			found++;
			double[] dd = new double[cols.length];
			boolean[] bb = new boolean[cols.length];
			
			for( int i = 0; i < cols.length; i++) {
				try {
					ss[cols[i]] = ss[cols[i]].replaceAll(",","").replaceAll("\\+","").trim();
					dd[i] = Double.parseDouble(ss[cols[i]]);///total_pop;
					bb[i] = true;
				} catch (Exception ex) {
					dd[i] = 0;
					bb[i] = false;
					ex.printStackTrace();
				}
			}
			if( total_pop == total_pop) {
				for(VTD feat : vf) {
					double feat_pop = feat.properties.POPULATION;
					if( !test) {
						for( int i = 0; i < cols.length; i++) { 
							if( bb[i] == false) {
								continue;
							}
							int other =  i+(i % 2 == 1 ? -1 : 1);
							if( dd[i] == 0 && dd[other] == 0) {
								System.out.println("both dem and rep are 0, skipping..");
								continue;
							}
							try {
								if( feat_pop != feat_pop || feat_pop == 0 || total_pop == 0 || feat_pop == total_pop) {
									feat.properties.put(names[i], ""+dd[i]);
								} else {
									feat.properties.put(names[i], ""+(dd[i]*feat_pop/total_pop));
								}
							} catch (Exception ex) {
								System.out.println("ex x "+ex);
								ex.printStackTrace();
							}
						}
					}
				}
			}
			} catch (Exception ex) {
				System.out.println("ex: "+ex);
				ex.printStackTrace();
			}
		}
		return new int[]{total_match_distance,total_edit_distance};

		//return new int[]{found,not_found};
		
	}

	class ImportFeatureLevel extends Thread {
		public Thread nextThread;
		public void run() {
			dlbl.setText("Importing election data...");
			//JOptionPane.showMessageDialog(null,"importing election data.");
			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
			}
			VTD.compare_centroid = false;
			Collections.sort(featureCollection.features);

			System.out.println("import vtd level start");
			//String path = "ftp://autoredistrict.org/pub/county_level_stats/VTD%20Detail%20Estimates%20--%20"+Download.states[Download.istate]+".txt";
			String path = "http://autoredistrict.org/county_level_stats/VTD%20Detail%20Estimates%20--%20"+Download.states[Download.istate].replaceAll(" ", "%20")+".txt";
			
			int[] ii;
			int[] matches = new int[2];
			int[] non_matches = new int[2];
			for( int m = 0; m < 2; m++) {
				ii =  joinToTxt(true,path, "VTDNAME", 3+m, 1, new int[]{4+m,5+m,6+m,7+m,8+m,9+m,10+m,11+m}, new String[]{"PRES12_DEM","PRES12_REP","PRES08_DEM","PRES08_REP","PRES04_DEM","PRES04_REP","CD12_DEM","CD12_REP"});
				System.out.println("m="+m+" total found: "+ii[0]+"\ntotal not found: "+ii[1]);
				if( ii[0] == 0 && ii[1] == 0) {
					/*
					if( nextThread != null) {
						nextThread.start();
						nextThread = null;
					}
					return;
					*/
				}
				matches[m] = ii[0];
				non_matches[m] = ii[1];
			}
			double match_pct0 = ((double)matches[0])/(double)(matches[0]+non_matches[0]);
			double match_pct1 = ((double)matches[1])/(double)(matches[1]+non_matches[1]);
			if( match_pct1 > 0.95 && matches[1] >= matches[0]) {
				System.out.println("trying shifting columns by 1...");
				ii =  joinToTxt(false,path, "VTDNAME", 4, 1, new int[]{5,6,7,8,9,10,11,12}, new String[]{"PRES12_DEM","PRES12_REP","PRES08_DEM","PRES08_REP","PRES04_DEM","PRES04_REP","CD12_DEM","CD12_REP"});
				System.out.println("total found: "+ii[0]+"\ntotal not found: "+ii[1]);
			}
			if( match_pct0 > 0.95 && matches[0] > matches[1]) {
				System.out.println("trying shifting columns by 1...");
				ii =  joinToTxt(false,path, "VTDNAME", 3, 1, new int[]{4,5,6,7,8,9,10,11}, new String[]{"PRES12_DEM","PRES12_REP","PRES08_DEM","PRES08_REP","PRES04_DEM","PRES04_REP","CD12_DEM","CD12_REP"});
				System.out.println("total found: "+ii[0]+"\ntotal not found: "+ii[1]);
			}
			
			//JOptionPane.showMessageDialog(null,"total found: "+ii[0]+"\ntotal not found: "+ii[1]);
						
			System.out.println("setting columns final...");
			trySetBasicColumns();
			trySetGroupColumns();
			System.out.println("done county data merge");
			if( nextThread == null) {
				//JOptionPane.showMessageDialog(null,"nextThread is null!");
				System.out.println("no next thread!");
			}

			if( nextThread != null) {
				System.out.println("starting next thread");
				nextThread.start();
				nextThread = null;
			}
		}
	}
	class ImportDemographics extends Thread {
		final static boolean uselatlon = true;
		//150,700
		String SUMMARY_LEVEL = "700";
		Thread nextThread = null;
		public void run() {
			dlbl.setText("Importing demographics...");

			String state = Download.states[Download.istate];
			
			if( true) {
				getDemographics(state,Download.state_to_abbr.get(state),""+Download.cyear);
			}
		
			System.out.println("done nextThred: "+nextThread);
			if( nextThread != null) {
				nextThread.start();
				nextThread = null;
			}
		}
		public void getDemographics(String statename, String stateabbr, String year) {
			if( false) {
				return;
			}
			try {
				boolean remove_hyphens = false;

			int DATA_STARTS_AT = 3;
			boolean trim_leading_zeros = false;
		
			BiMap<String,String> column_renames = new BiMap<String,String>();
			String base_url = "http://www2.census.gov/census_2010/01-Redistricting_File--PL_94-171/";
			
			String fn_zipfile = stateabbr.toLowerCase()+year+".pl.zip";
			String url = base_url+statename.replaceAll(" ", "_")+"/"+fn_zipfile;
			String path = Download.census_tract_path+"demographics"+File.separator;

			Download.download(url,path,fn_zipfile);
			FileUtil.unzip(path+fn_zipfile, Download.census_tract_path+"demographics"+File.separator);
			
			String fn_geoheader = stateabbr.toLowerCase()+"geo"+year+".pl";
			String fn_part1 = stateabbr.toLowerCase()+"00001"+year+".pl";
			String fn_part2 = stateabbr.toLowerCase()+"00002"+year+".pl";
			
			File f_geoheader = new File(path+fn_geoheader);
			FileInputStream fis_geoheader = new FileInputStream(f_geoheader);
			BufferedReader br_geoheader = new BufferedReader(new InputStreamReader(fis_geoheader));

			File f_part1 = new File(path+fn_part1);
			FileInputStream fis_part1;
				fis_part1 = new FileInputStream(f_part1);
			BufferedReader br_part1 = new BufferedReader(new InputStreamReader(fis_part1));
			
			File f_part2 = new File(path+fn_part2);
			FileInputStream fis_part2 = new FileInputStream(f_part2);
			BufferedReader br_part2 = new BufferedReader(new InputStreamReader(fis_part2));
			
			
			column_renames.put("INTPTLAT","INTPTLAT");
			column_renames.put("INTPTLON","INTPTLON");
			column_renames.put("VTD","VTD");
			
			column_renames.put("P0020001","POP_TOTAL");
			column_renames.put("P0020002","POP_HISPAN");
			column_renames.put("P0020005","POP_WHITE");
			column_renames.put("P0020006","POP_BLACK");
			column_renames.put("P0020007","POP_INDIAN");
			column_renames.put("P0020008","POP_ASIAN");
			column_renames.put("P0020009","POP_HAWAII");
			column_renames.put("P0020010","POP_OTHER");
			column_renames.put("P0020011","POP_MULTI");
			
			column_renames.put("P0040001","VAP_TOTAL");
			column_renames.put("P0040002","VAP_HISPAN");
			column_renames.put("P0040005","VAP_WHITE");
			column_renames.put("P0040006","VAP_BLACK");
			column_renames.put("P0040007","VAP_INDIAN");
			column_renames.put("P0040008","VAP_ASIAN");
			column_renames.put("P0040009","VAP_HAWAII");
			column_renames.put("P0040010","VAP_OTHER");
			column_renames.put("P0040011","VAP_MULTI");
			
			String[] headers = new String[]{
					"INTPTLAT",
					"INTPTLON",
					"VTD",
					
					"POP_TOTAL",
					"POP_HISPAN",
					"POP_WHITE",
					"POP_BLACK",
					"POP_INDIAN",
					"POP_ASIAN",
					"POP_HAWAII",
					"POP_OTHER",
					"POP_MULTI",
					
					"VAP_TOTAL",
					"VAP_HISPAN",
					"VAP_WHITE",
					"VAP_BLACK",
					"VAP_INDIAN",
					"VAP_ASIAN",
					"VAP_HAWAII",
					"VAP_OTHER",
					"VAP_MULTI",
			};
			int ILAT = 0;
			int ILON = 1;
			int FOREIGN_KEY = 2;
			String[] original_headers = new String[headers.length];
			for( int i = 0; i < original_headers.length; i++) {
				original_headers[i] = column_renames.getBackward(headers[i]);
			}
			

			Vector<String[]> data = new Vector<String[]>();
			
			String sgeo_header = FileUtil.readStream(getClass().getResourceAsStream("/resources/geoheader_cols.txt")); 
			String spart1_header = FileUtil.readStream(getClass().getResourceAsStream("/resources/part1_cols.txt")); 
			String spart2_header = FileUtil.readStream(getClass().getResourceAsStream("/resources/part2_cols.txt")); 
			
			Vector<Triplet<String,String,Integer>> geo_header = new Vector<Triplet<String,String,Integer>>();
			Vector<Triplet<String,String,Integer>> part1_header = new Vector<Triplet<String,String,Integer>>();
			Vector<Triplet<String,String,Integer>> part2_header = new Vector<Triplet<String,String,Integer>>();
			
			//System.out.println(sgeo_header.length()+" "+spart1_header.length()+" "+spart2_header.length());
			
			String[] lines = sgeo_header.split("\n");
			//System.out.println("lines: "+lines.length);
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				if( ss.length < 3) {
					continue;
				}
				for( int j = 0; j < ss.length; j++ ) {
					ss[j] = ss[j].trim().replaceAll("\"","").replaceAll("\\r","").replaceAll("\\n","");
				}
				//System.out.println("adding |"+ss[0]+"|");
				geo_header.add(new Triplet<String,String,Integer>(ss[0],ss[1],Integer.parseInt(ss[2])));
			}
			lines = spart1_header.split("\n");
			//System.out.println("lines: "+lines.length);
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				if( ss.length < 3) {
					continue;
				}
				for( int j = 0; j < ss.length; j++ ) {
					ss[j] = ss[j].trim().replaceAll("\"","").replaceAll("\\r","").replaceAll("\\n","");
				}
				//System.out.println("adding |"+ss[0]+"|");
				part1_header.add(new Triplet<String,String,Integer>(ss[0],ss[1],Integer.parseInt(ss[2])));
			}
			lines = spart2_header.split("\n");
			//System.out.println("lines: "+lines.length);
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				if( ss.length < 3) {
					continue;
				}
				for( int j = 0; j < ss.length; j++ ) {
					ss[j] = ss[j].trim().replaceAll("\"","").replaceAll("\\r","").replaceAll("\\n","");
				}
				//System.out.println("adding |"+ss[0]+"|");
				part2_header.add(new Triplet<String,String,Integer>(ss[0],ss[1],Integer.parseInt(ss[2])));
			}
			String yr = ""+(Download.cyear % 100); 
			while( yr.length() < 2) {
				yr = "0"+yr;
			}
			String primary_key = "VTDST"+yr;
			
			if( uselatlon) {
				featureCollection.sortedFeatures = new Vector<VTD>();
				for( int i = 0; i < featureCollection.vtds.size(); i++) {
					VTD vtd = featureCollection.vtds.get(i);
					featureCollection.sortedFeatures.add(vtd);
					vtd.geometry.makePolysFull();
					for( int j = DATA_STARTS_AT; j < headers.length; j++) {
						vtd.properties.put(headers[j],"0");
					}
				}
				VTD.compare_centroid = true;
				Collections.sort(featureCollection.sortedFeatures);
				VTD.compare_centroid = false;

			} else {
				
			}
			
			//collect primary keys;
			HashMap<String,double[]> from_import = new HashMap<String,double[]>();
			HashMap<String,Vector<VTD>> county_feats = new HashMap<String,Vector<VTD>>();
			HashMap<String,Double> county_pops = new HashMap<String,Double>();
			for( VTD feat : featureCollection.features) {
				try {
				String county = (String) feat.properties.get(primary_key);
				county = county.trim().toUpperCase();
				if( county.contains("-") && remove_hyphens) {
					county = county.split("-")[1];
				}
				
				Vector<VTD> vf = county_feats.get(county);
				if( vf == null) { 
					vf = new Vector<VTD>();
					county_feats.put(county,vf);
				}
				vf.add(feat);
				Double i = county_pops.get(county);
				if( i == null) { 
					i = new Double(0);
					county_pops.put(county,i);
				}
				//if( feat.properties.POPULATION == 0) {
					Double d = new Double(0);
					try { 
						d = Double.parseDouble(feat.properties.get(project.population_column).toString());
					} catch (Exception ex) {
						//ex.printStackTrace();
					}
					feat.properties.POPULATION = d.intValue();
				//}
				i += feat.properties.POPULATION;
				county_pops.put(county,i);
				} catch (Exception ex) {
					System.out.println("ex aa4 "+ex);
					ex.printStackTrace();
					//System.exit(0);
				}
			}
			
			//read file into hash table (aggregating into vtd's)
			int lline = 0;
			while( true) {
				
				lline++;
				if( lline % 1000 == 0) {
					System.out.print(".");
				}
				if( lline % 100000 == 0) {
					System.out.println(""+lline);
				}				String geo_line = br_geoheader.readLine();
				String part1_line =  br_part1.readLine();
				String part2_line =  br_part2.readLine();
				if( geo_line == null) {
					break;
				}
				
				String pseudo_or_actual = ""+geo_line.charAt(167);
				String summary_level = geo_line.substring(8,11);
				//System.out.print(summary_level+pseudo_or_actual);
				//ignore non-vtd's.
				
				if( !summary_level.equals(SUMMARY_LEVEL)) {
				//if( !summary_level.equals("700")) {
					continue;
				}

				/*
				these are "SUMLEV"'; census summary levels
				700 = vtd
				150 = block group
				*/
				
				String[] geo_ss = parse_fixed_width(geo_line, geo_header);
				String[] part1_ss = part1_line.split(",");
				String[] part2_ss = part2_line.split(",");
				
				String[] ss = new String[column_renames.size()];
				int ndx = 0;
				
				for( int i = 0; i < geo_header.size(); i++) {
					String rename = column_renames.get(geo_header.get(i).a);
					//if( rename != null) {
						for( int j = 0; j < headers.length; j++) {
							if( geo_header.get(i).a.equals(original_headers[j])) {
								//System.out.println("matched "+j+" "+headers[j]);
								ss[j] = geo_ss[i];
								break;
							}
						}
						//ss[ndx++] = geo_ss[i];
					//}
				}
				for( int i = 0; i < part1_header.size(); i++) {
					String rename = column_renames.get(part1_header.get(i).a);
					//if( rename != null) {
						for( int j = 0; j < headers.length; j++) {
							if( part1_header.get(i).a.equals(original_headers[j])) {
								//System.out.println("matched "+j+" "+headers[j]);
								ss[j] = part1_ss[i];
								break;
							}
						}
					//}
				}
				for( int i = 0; i < part2_header.size(); i++) {
					String rename = column_renames.get(part2_header.get(i).a);
					//if( rename != null) {
						for( int j = 0; j < headers.length; j++) {
							if( part2_header.get(i).a.equals(original_headers[j])) {
								//System.out.println("matched "+j+" "+headers[j]);
								ss[j] = part2_ss[i];
								break;
							}
						}
					//}
				}
				for( int i = 0; i < ss.length; i++) {
					//System.out.println(""+i+": "+ss[i]);
				}
				
				String s = ss[FOREIGN_KEY];
				if( trim_leading_zeros) {
					while( s.length() > 1 && s.charAt(0) == '0') {
						s = s.substring(1);
					}
				}
				
				if( uselatlon) {
					
					double lat = Double.parseDouble(ss[ILAT]);
					double lon = Double.parseDouble(ss[ILON]);
					//System.out.println(ss[ILAT]+" "+lat);
					//System.out.println(ss[ILON]+" "+lon);
					
					VTD vtd = getHit(lon,lat);
					if( vtd == null) {
						System.out.print("m");
					} else {
						for( int j = DATA_STARTS_AT; j < ss.length; j++) {
							double d0 = 0;
							double d = Double.parseDouble(ss[j].replaceAll(",",""));
							Object o = vtd.properties.get(headers[j]);
							if( o != null) {
								d0 = Double.parseDouble(vtd.properties.get(headers[j]).toString());
							}
							d0 += d;
							vtd.properties.put(headers[j],""+d0);
						}
					}
				} else {
					
					double[] dd = from_import.get(s);
					if( dd == null) {
						dd = new double[ss.length-DATA_STARTS_AT];
						for( int i = 0; i < dd.length; i++) {
							dd[i] = 0;
						}
						from_import.put(s,dd);
					}
					for( int i = 0; i < dd.length; i++) {
						try {
							dd[i] += Double.parseDouble(ss[i+DATA_STARTS_AT]);
						} catch (Exception ex) { 
							System.out.println("ex cc "+ex+""+i+" "+ss[i+DATA_STARTS_AT]+" "+headers[i+DATA_STARTS_AT]);
							System.exit(0);
							ex.printStackTrace(); 
						}
					}
				}
			}
			
			//close everything
			try {
				br_geoheader.close();
				br_part1.close();
				br_part2.close();
				
				fis_geoheader.close();
				fis_part1.close();
				fis_part2.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
			if( !uselatlon) {
				//System.exit(0);
				int found = 0;
				int not_found = 0;
				
				//now write it out into features.
				for( Map.Entry<String,double[]> entry : from_import.entrySet() ) {
					try { 
						String incounty = entry.getKey();
						String orig = incounty;
						double[] dd = entry.getValue();
						Vector<VTD> vf = county_feats.get(incounty);
						if( vf == null) {
							if( incounty.length() > 3) {
								incounty = incounty.substring(0,2)+"-"+incounty.substring(2);
							}
							vf = county_feats.get(incounty);
							if( vf == null) {
								incounty = incounty.split("-")[1];
								vf = county_feats.get(incounty);
								if( vf == null) {
									incounty = "0"+incounty;
									vf = county_feats.get(incounty);
									if( vf == null) {
										System.out.println("not found!: "+orig);
										not_found++;
										continue;
									}
								}
							}
						}
						county_feats.remove(incounty);
						found++;
						double total_pop = (double)county_pops.get(incounty);
						System.out.println("found!: "+incounty+" "+total_pop);
						System.out.println("feat size "+vf.size());
						for(VTD feat : vf) {
							double feat_pop = feat.properties.POPULATION;
							System.out.println("setting "+feat.properties.get("VTDST10").toString()+" "+feat_pop+" "+total_pop+" "+dd[0]+" "+dd[1]);
							for( int i = 0; i < dd.length; i++) {
								feat.properties.put(headers[i+DATA_STARTS_AT], ""+(dd[i]*feat_pop/total_pop));
							}
						}
					} catch (Exception ex) {
						System.out.println("ex bb: "+ex);
						ex.printStackTrace();
						System.exit(0);
	
					}
				}	
			}
			//finish up unmatched entries.
			/*
			for( Entry<String, Vector<Feature>> entry : county_feats.entrySet() ) {
				System.out.println("filling "+entry.getKey());
				for( Feature feat : entry.getValue()) {
					for( int i = DATA_STARTS_AT; i < headers.length; i++) {
						feat.properties.put(headers[i], ""+0);
					}
				}
			}
			*/
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);

			}

		}

		public String[] parse_fixed_width(String s, Vector<Triplet<String,String,Integer>> header) {
			String[] ss = new String[header.size()];
			for( int i = 0; i < header.size(); i++) {
				int n = header.get(i).c < s.length() ? header.get(i).c : s.length();
				ss[i] = s.substring(0,n);
				s = s.substring(n); 
			}
			return ss;
		}
	}
	
	public void importBlocksBdistricting() {
		VTD.compare_centroid = false;
		Collections.sort(featureCollection.features);

		String path = Download.getStartPath()+File.separator+"bdistricting";
		Download.downloadAndExtract(Download.bdistricting_congress_url(),path);
		Download.downloadAndExtract(Download.bdistricting_senate_url(),path);
		String[] urls = Download.bdistricting_house_urls();
		for( int i = 0 ; i < urls.length; i++) {
			Download.downloadAndExtract(urls[i],path);
		}
		String abbr = Download.state_to_abbr.get(Download.states[Download.istate]);
		
		importBlockData(path+File.separator+abbr+"_Congress.csv", true, false, new String[]{"CD_BD"},new String[]{"CD_BD"},false);
		importBlockData(path+File.separator+abbr+"_Senate.csv", true, false, new String[]{"SLDU_BD"},new String[]{"SLDU_BD"},false);
		String[] tries = new String[]{"House","General","Legislature","Assembly"};
		for( int i = 0; i < tries.length; i++) {
			if( new File(path+File.separator+abbr+"_"+tries[i]+".csv").exists() || i == tries.length-1) {
				importBlockData(path+File.separator+abbr+"_"+tries[i]+".csv", true, false, new String[]{"SLDL_BD"},new String[]{"SLDL_BD"},false);
				break;
			}
		}
		VTD.compare_centroid = false;
		Collections.sort(featureCollection.features);

		Applet.deleteRecursive(new File(path));
	}


	public void importBlocksCurrentDistricts() {
		VTD.compare_centroid = false;
		Collections.sort(featureCollection.features);

		String path = Download.getStartPath()+File.separator+"current";
		Download.downloadAndExtract(Download.census_districts_url(),path);
		String abbr = Download.state_to_abbr.get(Download.states[Download.istate]);
		String fips = ""+Download.istate;
		if( fips.length() < 2) {
			fips = "0"+fips;
		}
		try {
			new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_CD.txt").renameTo(new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_CD.csv"));
			new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDU.txt").renameTo(new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDU.csv"));
			new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDL.txt").renameTo(new File(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDL.csv"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			importBlockData(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_CD.csv", true, true, new String[]{"DISTRICT"},new String[]{"CD_NOW"},true);
			importBlockData(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDU.csv", true, true, new String[]{"DISTRICT"},new String[]{"SLDU_NOW"},true);
			importBlockData(path+File.separator+"BlockAssign_ST"+fips+"_"+abbr+"_SLDL.csv", true, true, new String[]{"DISTRICT"},new String[]{"SLDL_NOW"},true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		VTD.compare_centroid = false;
		Collections.sort(featureCollection.features);
		
		Applet.deleteRecursive(new File(path));
	}

/*preparing:

					String[] options = new String[]{"Accumulate","Majority vote"};
					int ACCUMULATE = 0;
					int MAJORITY = 1;
					int opt = JOptionPane.showOptionDialog(mainframe, "Accumulate values or majority vote?", "Select option", 0,0,null,options,options[0]);
					if( opt < 0) {
						System.out.println("aborted.");
						return;
					}

					DialogMultiColumnSelect dsc = new DialogMultiColumnSelect("Select columns to import",dh.header,new String[]{});
					dsc.show();
					if( !dsc.ok) {
						return;
					}
					Object[] dest_columns = dsc.in.toArray();
					int[] col_indexes = new int[dsc.in.size()]; 
					for( int i = 0; i < col_indexes.length; i++) {
						String test = ((String)dest_columns[i]).toUpperCase().trim();
						for( int j = 0; j < dh.header.length; j++) {
							if( dh.header[j].toUpperCase().trim().equals(test)) {
								col_indexes[i] = j;
								break;
							}
						}
					}
					

ui.Mainframe:	
*/				

	public void importBlockData(String filename, boolean MAJORITY_VOTE, boolean HAS_HEADER_ROW, String[] source_columns, String[] dest_columns, boolean one_indexed) {
			int ACCUMULATE = 0;
			int MAJORITY = 1;
			int opt = MAJORITY_VOTE ? 1 : 0;
			//TODO - if missed, fill with majority neighbor.
			
			DataAndHeader dh = new DataAndHeader();

			int col_lat = -1;
			int col_lon = -1;
			int col_geoid_centroid = -1;
			//Hashtable<String,int[]> hash_centroids = new Hashtable<String,int[]>();
			Hashtable<String,VTD> hash_geoid_feat = new Hashtable<String,VTD>();
			

			DBFReader dbfReader;



    		dlg.setVisible(true);

//download/extract centroid file if not there.
			File test = new File(Download.census_centroid_file.getAbsolutePath());
			if( !test.exists()) {
				Download.downloadAndExtractCentroids();
			}


//make polygons
    		dlbl.setText("Making polygons...");

			
			int count = 0;
    		for( VTD feat : featureCollection.features) {
    			feat.geometry.makePolysFull();
				feat.population = 0;
    		}
    		
    		dlbl.setText("Doing hit tests...");
			//Feature.compare_centroid = true;
			//Collections.sort(featureCollection.features);
			
//make centroid hash
			try {
				dbfReader = new DBFReader(Download.census_centroid_file.getAbsolutePath());
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
			
    		dlbl.setText("Reading header...");

			dh.header = new String[dbfReader.getFieldCount()];
			for( int i = 0; i < dh.header.length; i++) {
				dh.header[i] = dbfReader.getField(i).name;
				System.out.println(dh.header[i]+" ");

				if( dh.header[i].toUpperCase().trim().indexOf("INTPTLAT") == 0) {
					col_lat = i;
				}
				if( dh.header[i].toUpperCase().trim().indexOf("INTPTLON") == 0) {
					col_lon = i;
				}
				if( dh.header[i].toUpperCase().trim().indexOf("GEOID") == 0 || dh.header[i].toUpperCase().trim().indexOf("BLOCKID") == 0) {
					col_geoid_centroid = i;
				}
			}
			if( col_geoid_centroid < 0 || col_lat < 0 || col_lon < 0) {
				JOptionPane.showMessageDialog(mainframe, "Required columns not found."
						+"\ncol_geoid_centroid "+col_geoid_centroid
						//+"\ncol_lat "+col_lat
						//+"\ncol_lon "+col_lon
						);
				dlg.setVisible(false);
				return;
			}

    		dlbl.setText("Creating blockid to feature hash...");
    		System.out.println("Creating blockid to feature hash...");
    		int n = 0;
		    while (dbfReader.hasNextRecord()) {
		    	if( n % 100 == 0) {
		    		System.out.print(".");
		    	}
		    	if( n % 10000 == 0) {
		    		System.out.println(""+n);
		    	}
		    	n++;
		    	try {
		    		Object[] oo = dbfReader.nextRecord(Charset.defaultCharset());
		    		String[] ss = new String[oo.length];
		    		for( int i = 0; i < oo.length; i++) {
		    			ss[i] = oo[i].toString().trim();
		    		}
		    		double dlat = Double.parseDouble(ss[col_lat].replaceAll(",","").replaceAll("\\+",""));
		    		double dlon = Double.parseDouble(ss[col_lon].replaceAll(",","").replaceAll("\\+",""));
					
	    			VTD feat = getHit(dlon,dlat);
	    			if( ss != null && ss[col_geoid_centroid] != null && feat != null) {
	    				hash_geoid_feat.put(ss[col_geoid_centroid],feat);
	    			}
					
					
		    		//int ilat = (int)(dlat*Geometry.SCALELATLON);
		    		//int ilon = (int)(dlon*Geometry.SCALELATLON);
		    		//hash_centroids.put(ss[col_geoid_centroid].trim(),new int[]{ilat,ilon});
		    	} catch (Exception ex) {
		    		ex.printStackTrace();
		    	}
		    }
		    try {
				dbfReader.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    System.out.println();

//resort features
			//VTD.compare_centroid = false;
			//Collections.sort(featureCollection.features);
							
//read headers
    		System.out.println("Reading headers...");
    		dlbl.setText("Reading headers...");

			File f = new File(filename);
			String ext = filename.substring(filename.length()-3).toLowerCase();
			dh = new DataAndHeader();

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
				
				FileReader fr = null;
				try {
					fr = new FileReader(f);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					for( VTD feat : featureCollection.features) {
						for( int i = 0; i < dest_columns.length; i++) {
							feat.properties.put(dest_columns[i],"0");
						}
					}
					dlg.hide();
					return;
				}
				BufferedReader br = new BufferedReader(fr);
				
				if( HAS_HEADER_ROW) {
					try {
						dh.header = br.readLine().split(delimiter);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					dh.header = new String[source_columns.length+1];
					dh.header[0] = "GEOID";
					for( int i = 0; i < source_columns.length; i++) {
						dh.header[i+1] = source_columns[i];
					}
				}
				
				//now select the columns
	    		System.out.println("reading columns");
				int col_geoid = -1;
				int[] col_indexes = new int[source_columns.length];
				for( int i = 0; i < dh.header.length; i++) {
					String header = dh.header[i].toUpperCase().trim();
					if( header.indexOf("GEOID") == 0 || header.indexOf("BLOCKID") == 0) {
						col_geoid = i;
					}
					for( int j = 0; j < source_columns.length; j++) {
						if( header.equals(source_columns[j])) {
							col_indexes[j] = i;
						}
					}
				}
				if( col_geoid < 0) {
					dlg.setVisible(false);
		    		System.out.println("Required columns not found.");
					if( Download.prompt) {
						JOptionPane.showMessageDialog(mainframe, "Required columns not found.\nMissing geoid column.");
					}
					return;
				}
				
	    		
//read and put
				System.out.println("Reading data...");
				dlbl.setText("Reading data...");
				
				hits = 0;
	    		misses = 0;
	    		
	    		try {
					if( opt == MAJORITY) { //MAJORITY
						//make properties in first feature
    					for( int j = 0; j < dest_columns.length; j++) {
    						featureCollection.features.get(0).properties.put((String)dest_columns[j],"");		
    					}
						//reset hash in all features
    					for( VTD feat : featureCollection.features) {
    						feat.properties.temp_hash.clear();
    					}
					} else {
    					for( VTD feat : featureCollection.features) {
							for( int i = 0; i < dest_columns.length; i++) {
								feat.properties.put(dest_columns[i],"0");
							}
    					}
					}

				    String line;
				    while ((line = br.readLine()) != null) {
				    	try {
					    	String[] ss = line.split(delimiter);
					    	if( ss.length > dh.header.length) {
					    		System.out.print("+"+(ss.length - dh.header.length));
					    	}
							VTD feat = hash_geoid_feat.get(ss[col_geoid]);
							if( feat == null) {
								misses++;
					    		//System.out.println("miss "+dlon+","+dlat+" ");
								continue;
							}
							hits++;
							
							if( hits % 100 == 0) {
				    			System.out.print(".");
				    		}
				    		if( hits % (100*100) == 0) {
				    			System.out.println(""+hits);
				    		}
							
							String[] results = new String[dest_columns.length];
							for( int i = 0; i < dest_columns.length; i++) {
								results[i] = ss[col_indexes[i]].trim();
							}

							if( opt == MAJORITY) { //MAJORITY
								String dist = ss[col_indexes[0]].trim();
								Integer integer = feat.properties.temp_hash.get(dist);
								if( integer == null) {
									integer = 0;
								}
								integer++;
								feat.properties.temp_hash.put(dist,integer);
							} else if (opt == ACCUMULATE) { //accumulate
								for( int i = 0; i < dest_columns.length; i++) {
									try {
										Double initial = Double.parseDouble(feat.properties.get(dest_columns[i]).toString());
										initial += Double.parseDouble(results[i].replaceAll(",","").replaceAll("\\+",""));
										feat.properties.put(dest_columns[i],""+initial);
									} catch (Exception ex) { ex.printStackTrace(); } 
								}
							}
				    		
						} catch (Exception e) {
							// TODO Auto-generated catch ward
							e.printStackTrace();
						}
				    }
					
					//note majority vote is only handling 1 column right now!
					String key = ((String)dest_columns[0]).trim().toUpperCase();
					if( opt == MAJORITY) { //MAJORITY
						Vector<VTD> missed = new Vector<VTD>();
						for( int i = 0; i < featureCollection.vtds.size(); i++) {
							missed.add(featureCollection.vtds.get(i));
						}
						
						Vector<VTD> missing = new Vector<VTD>();
						for( VTD feat : featureCollection.features) {
							if( feat.properties.temp_hash.size() == 0) {
								System.out.println("no values! "+key);
								missing.add(feat);
								feat.properties.put(key,"");
								continue;
							}
							String max = "";
							int max_count = 0;
							for( Entry<String,Integer> entry : feat.properties.temp_hash.entrySet()) {
								System.out.println(""+feat.properties.get("GEOID10")+": "+entry.getKey()+": "+entry.getValue());
								if( entry.getValue() > max_count) {
									if( max.equals("0") && one_indexed) {
										continue;
									}
									max_count = entry.getValue();
									max = entry.getKey();
								}
							}
							if( one_indexed) {
								max = ""+(Integer.parseInt(max)-1);
							}
    						feat.properties.put(key,max);
						}
						
						//fill in empties with neighbors
						Hashtable<String,Integer> counts = new Hashtable<String,Integer>();
						int l = 0;
						System.out.println(""+missing.size()+" missing");
						while (l < 5 && missing.size() > 0) {
							System.out.println(""+missing.size()+" missing");
							for( int k = 0; k < missing.size(); k++) {
								VTD feat = missing.get(k);
								counts.clear();
								for(VTD vtd : feat.neighbors) {
									String p = vtd.feature.properties.get(key).toString();
									Integer i = counts.get(p);
									if( i == null) {
										i = new Integer(0);
										counts.put(p, i);
									}
									i++;
								}
								String best = "";
								int max = 0;
								if( counts.entrySet().size() == 0) {
									System.out.println("no neighbors found! "+l);
								} else {
									missing.remove(feat);
									k--;
									for( Entry<String,Integer> entries : counts.entrySet()) {
										if( entries.getValue() > max) {
											max = entries.getValue();
											best = entries.getKey();
										}
									}
									feat.properties.put(key,best);
								}
							}
							l++;
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
//finish up
				try {
					br.close();
					fr.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	    		dlbl.setText("Finalizing...");
				
				
				project.district_column = (String) comboBoxDistrictColumn.getSelectedItem();
				project.population_column = (String) comboBoxPopulation.getSelectedItem();
				fillComboBoxes();
				mapPanel.invalidate();
				mapPanel.repaint();
				
	    		dlg.setVisible(false);

				if(Download.prompt) {
					JOptionPane.showMessageDialog(mainframe,"Done importing data.\nHits: "+hits+"\nMisses: "+misses);
				}
	    		System.out.println("done");

				return;
				
			}
	}
	
	//more than 1 party above the vote threshold (as %) is counted as contested.  (original had it at 2%)
	public void imputeUncontestedFeatures( Vector<String> election_columns, Vector<String> substitute_columns, double vote_threshold) {
		if( election_columns.size() != substitute_columns.size()) {
			return;
		}
		
		//find contested and uncontested
		Vector<VTD> contested_vtds = new Vector<VTD>();
		Vector<VTD> uncontested_vtds = new Vector<VTD>();
		for( VTD f : featureCollection.features) {

			double tot = 0;
			for( String s : election_columns) {
				try {
					tot += Double.parseDouble(f.properties.get(s).toString());
				} catch (Exception ex) {}
			}

			int nonzeros = 0;
			for( String s : election_columns) {
				try {
					double val = Double.parseDouble(f.properties.get(s).toString());
					if( val > vote_threshold*tot) {
						nonzeros++;
					}
				} catch (Exception ex) {}
			}
			if( nonzeros > 1) {
				contested_vtds.add(f);
			} else {
				uncontested_vtds.add(f);
			}
		}
		
		//find the total vote counts for contested races
		double[] election_sums = new double[election_columns.size()];
		double[] substitute_sums = new double[election_columns.size()];
		for( int i = 0; i < election_sums.length; i++) {
			election_sums[i] = 0;
			substitute_sums[i] = 0;
		}
		for( VTD f : contested_vtds) {
			for( int i = 0; i < election_sums.length; i++) {
				try {
					election_sums[i] += Double.parseDouble(f.properties.get(election_columns.get(i)).toString());
				} catch (Exception ex) {}
				try {
					election_sums[i] += Double.parseDouble(f.properties.get(substitute_columns.get(i)).toString());
				} catch (Exception ex) {}
			}
		}

		//now find the ratio for that for election vs substitute columns
		for( int i = 0; i < election_sums.length; i++) {
			election_sums[i] = substitute_sums[i] == 0 ? 0 : election_sums[i] / substitute_sums[i];
		}
		
		//now fill in uncontested, adjusted by vote ratio of contested.
		for( VTD f : uncontested_vtds) {
			for( int i = 0; i < election_sums.length; i++) {
				try {
					double val = Double.parseDouble(f.properties.get(substitute_columns.get(i)).toString())*election_sums[i];
					f.properties.put(election_columns.get(i),""+val);
				} catch (Exception ex) {}
			}
		}
	}
		
		
	//more than 1 party above the vote threshold (as %) is counted as contested.  (original had it at 2%)
	public void imputeUncontestedDistricts( Vector<String> election_columns, Vector<String> substitute_columns, String district_column, double vote_threshold) {
		if( election_columns.size() != substitute_columns.size()) {
			return;
		}
		
		//accumulate by district
		Hashtable<String,double[]> district_election_sums = new Hashtable<String,double[]>();
		for( VTD f : featureCollection.features) {
		
			//get or create district in hashtable
			String key = f.properties.get(district_column).toString();
			double[] dd = district_election_sums.get(key);
			if( dd == null) {
				dd = new double[election_columns.size()];
				for( int i = 0; i < dd.length; i++) {
					dd[i] = 0;
				}
				district_election_sums.put(key,dd);
			}
		
			//accumulate
			for( int i = 0; i < election_columns.size(); i++) {
				try {
					dd[i] += Double.parseDouble(f.properties.get(election_columns.get(i)).toString());
				} catch (Exception ex) {}
			}
		}

		//find contested and uncontested
		Vector<VTD> contested_vtds = new Vector<VTD>();
		Vector<VTD> uncontested_vtds = new Vector<VTD>();
		for( VTD f : featureCollection.features) {
			String key = f.properties.get(district_column).toString();
			double[] dd = district_election_sums.get(key);

			double tot = 0;
			for( int i = 0; i < dd.length; i++) {
				tot += dd[i];
			}

			int nonzeros = 0;
			for( int i = 0; i < dd.length; i++) {
				if( dd[i] > vote_threshold*tot) {
					nonzeros++;
				}
			}
			if( nonzeros > 1) {
				contested_vtds.add(f);
			} else {
				uncontested_vtds.add(f);
			}
		}

		
		//find the total vote counts for contested races
		double[] election_sums = new double[election_columns.size()];
		double[] substitute_sums = new double[election_columns.size()];
		for( int i = 0; i < election_sums.length; i++) {
			election_sums[i] = 0;
			substitute_sums[i] = 0;
		}
		for( VTD f : contested_vtds) {
			for( int i = 0; i < election_sums.length; i++) {
				try {
					election_sums[i] += Double.parseDouble(f.properties.get(election_columns.get(i)).toString());
				} catch (Exception ex) {}
				try {
					election_sums[i] += Double.parseDouble(f.properties.get(substitute_columns.get(i)).toString());
				} catch (Exception ex) {}
			}
		}

		//now find the ratio for that for election vs substitute columns
		for( int i = 0; i < election_sums.length; i++) {
			election_sums[i] = substitute_sums[i] == 0 ? 0 : election_sums[i] / substitute_sums[i];
		}
		
		//now fill in uncontested, adjusted by vote ratio of contested.
		for( VTD f : uncontested_vtds) {
			for( int i = 0; i < election_sums.length; i++) {
				try {
					double val = Double.parseDouble(f.properties.get(substitute_columns.get(i)).toString())*election_sums[i];
					f.properties.put(election_columns.get(i),""+val);
				} catch (Exception ex) {}
			}
		}
	}
	
	public void importURL(String url, String fk, String pk, String[] cols) {
		try {
		String s0 = "IMPORT URL "+url+" "+fk+" "+pk;
		for( int i = 0; i < cols.length; i++) {
			s0 += " "+cols[i];
		}
		ip.addHistory(s0);
		
		System.out.println("0: ");
		String[] dots = url.split("\\.");
		System.out.println("1: ");
		String ext = ""+dots[dots.length-1].toLowerCase();
		System.out.println("2: ");

		String dest_path = Download.getStartPath();
		String dest_name = "temp."+ext;
		System.out.println("download: ");
		System.out.println(url);
		System.out.println(dest_path);
		System.out.println(dest_name);
		try {
			if( !Download.download(url,dest_path,dest_name)) {
				System.out.println("failed to download "+url+"!");
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed to download "+url+"!");
			return;
		}
		
		File fi = new File(dest_path+dest_name);

		String fn = fi.getName();
		System.out.println("file: "+fn);
		DataAndHeader dh = null;
		if( ext.equals("csv")) {
			String s = getFile(fi).toString();
			dh = readDelimited(s,",",true);
		} else
		if( ext.equals("txt") || ext.equals("tsv")) {
			String s = getFile(fi).toString();
			dh = readDelimited(s,"\t",true);
		} else
		if( ext.equals("dbf")) {
			String s = getFile(fi).toString();
			dh = FileUtil.readDBF(fi.getAbsolutePath());
		}
		System.out.println("read "+dh.data.length+" rows");
		int fk_index = -1;
		for( int i = 0; i < dh.header.length; i++) {
			if( dh.header[i].equals(fk)) {
				fk_index = i;
				break;
			}
		}
		int[] col_indices;
		if( cols == null) {
			cols = dh.header;
		}
		
		col_indices	= new int[cols.length];
		for( int i = 0; i < cols.length; i++) {
			col_indices[i] = -1;
			for( int j = 0; j < dh.header.length; j++) {
				if( dh.header[j].equals(cols[i])) {
					col_indices[i] = j;
					break;
				}
			}
		}
		
		for( VTD f : featureCollection.features) {
			f.temp_bool = false;
		}	
		
		Hashtable<String,VTD> hmmap = new Hashtable<String,VTD>();
		for( VTD f : featureCollection.features) {
			hmmap.put(f.properties.get(pk).toString(), f);
		}
		
		for( int i = 0; i < dh.data.length; i++) {
			try {
				if( i % 10 == 0) {
					System.out.print(".");
				}
				if( i % (100 * 10) == 0) {
					System.out.println(""+i);
				}
				String key = dh.data[i][fk_index];
				if( !hmmap.containsKey(key)) {
					key = "0"+key;
				}
				if( hmmap.containsKey(key)) {
					VTD f = hmmap.get(key);
					if( f == null) {
						System.out.println("f is null!");
					}
					f.temp_bool = true;
					for( int j = 0; j < cols.length; j++) {
						f.properties.put(cols[j], dh.data[i][col_indices[j]]);
					}
				} else {
					System.out.println("key not found "+dh.data[i][fk_index]);
					
				}
			} catch (Exception ex) {
				System.out.println("ex "+ex);
				ex.printStackTrace();
			}
		}

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
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
	}

	public void impute(String[] ss) {
		String s = "IMPUTE";
		project.substitute_columns.clear();
		for( int i = 0; i < ss.length; i++) {
			project.substitute_columns.add(ss[i]);
			s += " "+ss[i];
		}
		ip.addHistory(s);
		setSubstituteColumns();		
	}
}


