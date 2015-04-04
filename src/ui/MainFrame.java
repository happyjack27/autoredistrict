package ui;

import geoJSON.*;
import geoJSON.Properties;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import mapCandidates.*;

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


public class MainFrame extends JFrame implements iChangeListener {
	/*
	TODO: pre/post serialize to get district #?
			-- need to make data exporter, and make it so that it imports district when applicable.
			need to be able to customize district column name and save in project file, so can load districts.
			*/
	
	//======LOOPBACK
	public static MainFrame mainframe;
	
	

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

	double mutation_rate_multiplier = 0.1;
	public static double boundary_mutation_rate_multiplier = 0.4;
	long load_wait = 100;
	
	double minx,maxx,miny,maxy;
	
	public String openedProjectFilePath = "";	
	
	

	
	//========CONTAINERS
    final JDialog dlg = new JDialog(mainframe, "Working", true);
    JProgressBar dpb = new JProgressBar(0, 500);
    JLabel dlbl = new JLabel();
	public MapPanel mapPanel = new MapPanel(); 
	JFrame frameStats = new JFrame();
	PanelStats panelStats = new PanelStats();
	JFrame frameGraph = new JFrame();
	DialogShowProperties df = new DialogShowProperties();
	public PanelGraph panelGraph = new PanelGraph();

	//===========COMPONENTS - MENU ITEMS
	JCheckBoxMenuItem mntmShowDemographics = new JCheckBoxMenuItem("Show demographics");
	JCheckBoxMenuItem chckbxmntmMutateAll = new JCheckBoxMenuItem("Mutate all");
	JCheckBoxMenuItem chckbxmntmShowPrecinctLabels = new JCheckBoxMenuItem("Show precinct labels");
	JCheckBoxMenuItem chckbxmntmHideMapLines = new JCheckBoxMenuItem("Hide map lines");
	JCheckBoxMenuItem chckbxmntmLatitudeLongitude = new JCheckBoxMenuItem("Latitude / Longitude?");
	JCheckBoxMenuItem chckbxmntmFlipVertical = new JCheckBoxMenuItem("Flip vertical");
	JCheckBoxMenuItem chckbxmntmFlipHorizontal = new JCheckBoxMenuItem("Flip horizontal");
	JCheckBoxMenuItem chckbxmntmReplaceAll = new JCheckBoxMenuItem("Replace all");
	JCheckBoxMenuItem chckbxmntmAutoAnneal = new JCheckBoxMenuItem("Auto anneal");
	JMenuItem mntmSaveProjectFile = new JMenuItem("Save project file");
	JMenuItem mntmExportData = new JMenuItem("Export data");
	JMenuItem mntmImportData = new JMenuItem("Import data");

	//JMenu mnGeography = new JMenu("Geography");
	JMenuItem mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
	//JMenu mnDemographics = new JMenu("Demographics");
	JMenuItem chckbxmntmOpenCensusResults = new JMenuItem("Open Census results");
	JMenuItem mntmOpenElectionResults = new JMenuItem("Open Election results");
	JMenu mnEvolution = new JMenu("Evolution");
	JMenuItem mntmExportcsv = new JMenuItem("Export results .csv");
	JMenuItem mntmImportcsv = new JMenuItem("Import results .csv");
	JMenuItem mntmShowStats = new JMenuItem("Show stats");

	JMenuItem mntmExportPopulation = new JMenuItem("Export population");
	JMenuItem mntmImportPopulation = new JMenuItem("Import population");
	JMenuItem mntmResetZoom = new JMenuItem("Reset zoom");
	JMenuItem mntmZoomIn = new JMenuItem("Zoom in");
	JMenuItem mntmUndoZoom = new JMenuItem("Undo zoom");
	JMenuItem mntmShowGraph = new JMenuItem("Show graph");
	JMenuItem mntmOpenEsriShapefile = new JMenuItem("Open ESRI shapefile");
	JMenuItem mntmSelectLayers = new JMenuItem("Select layers");

	
	//===========COMPONENTS
	public JComboBox comboBoxPrimaryKey = new JComboBox();
	public JComboBox comboBoxPopulation = new JComboBox();
	public JComboBox comboBoxDistrictColumn;

    public JTextField textField_3 = new JTextField();
    public JTextField textFieldNumDistricts = new JTextField();
    public JTextField textFieldElectionsSimulated = new JTextField();
    public JTextField textField = new JTextField();
	public JTextField textFieldMembersPerDistrict;

	public JSlider slider_1 = new JSlider();
	public JSlider sliderDisconnected = new JSlider();
	public JSlider sliderBorderLength = new JSlider();
	public JSlider sliderPopulationBalance = new JSlider();
	public JSlider sliderVotingPowerBalance = new JSlider();
	public JSlider sliderRepresentation = new JSlider();
	
	public JLabel lblDistrictColumn;
	
	//=========CLASSES
	class FileThread extends Thread {
    	public File f = null;
    	FileThread() { super(); } 
    	FileThread(File f) {
    		super();
    		this.f = f;
    	}
	}
	

	class OpenShapeFileThread extends FileThread {
		OpenShapeFileThread(File f) { super(f); }
    	public void run() { 
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
				
				featureCollection.ecology.stopEvolving();
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
    		} catch (Exception ex) {
    			System.out.println("ex "+ex);
    			ex.printStackTrace();
    		}
    	}
    }
	
	class OpenProjectFileThread extends FileThread {
		OpenProjectFileThread(File f) { super(f); }
    	public void run() { 
    	    project.fromJSON(getFile(f).toString());
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
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		try {
			hushcomboBoxDistrict = true;
			comboBoxDistrictColumn.removeAllItems();
			comboBoxDistrictColumn.addItem("");
			comboBoxDistrictColumn.addItem("AUTOREDISTRICT_RESULT");
			for( int i = 0; i < map_headers.length; i++) {
				comboBoxDistrictColumn.addItem(map_headers[i]);
			}
			hushcomboBoxDistrict = false;
			if( project.district_column != null && project.district_column.length() > 0) {
				comboBoxDistrictColumn.setSelectedItem(project.district_column);
			}
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}

		//comboBoxPopulation.setSelectedIndex(0);
		
	}
	public void finishLoadingGeography() {
		Vector<Feature> features = featureCollection.features;
		System.out.println(features.size()+" precincts loaded.");
		System.out.println("Initializing wards...");
		featureCollection.initwards();
	    dlbl.setText("Setting min and max coordinates...");
	
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
		
		featureCollection.loadDistrictsFromProperties(project.district_column);
		
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
	
	public void openProjectFile(File f0) {
		project = new Project();
	    openedProjectFilePath = f0.getAbsolutePath();
	    new OpenProjectFileThread(f0).start();
	}
	
	public void setEnableds() {
		
		if( !geo_loaded) {
			census_loaded = false;
			election_loaded = false;
		}
		chckbxmntmOpenCensusResults.setEnabled(geo_loaded);
		mntmOpenElectionResults.setEnabled(geo_loaded);
		mntmImportData.setEnabled(geo_loaded);
		mntmSelectLayers.setEnabled(geo_loaded);
		mntmImportcsv.setEnabled(geo_loaded);
		mntmExportcsv.setEnabled(geo_loaded);
		mntmExportData.setEnabled(geo_loaded);
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

	public void setDistrictColumn(String district) {
		boolean changed = !district.equals(project.district_column);
		project.district_column = district;
		if( changed) {
			if( featureCollection.features == null || featureCollection.features.size() == 0) {
				
			} else {
				boolean is_in = featureCollection.features.get(0).properties.containsKey(district);
				if( is_in) {
					featureCollection.loadDistrictsFromProperties(district);
				}
			}
		}
	}

	public void setPopulationColumn(String pop_col) {
		project.population_column = pop_col;
		for( Feature f : featureCollection.features) {
			String pop = f.properties.get(pop_col).toString();
			if( f.ward != null) {
				f.ward.has_census_results = true;
				f.ward.population = Double.parseDouble(pop.replaceAll(",",""));
			}
			f.properties.POPULATION = (int) Double.parseDouble(pop.replaceAll(",",""));
		}
	}
	public void setDemographicColumns(Vector<String> candidate_cols) {
		int num_candidates = candidate_cols.size();
		
		for( Ward b : featureCollection.wards) {
			b.has_election_results = true;
		}
		
		for( Feature f : featureCollection.features) {
			Ward b = f.ward;
			b.resetOutcomes();
			double[] dd = new double[num_candidates];
			for( int i = 0; i < candidate_cols.size(); i++) {
				dd[i] = Double.parseDouble(f.properties.get(candidate_cols.get(i)).toString().replaceAll(",",""));
			}
		
			b.demographics.clear();
			for( int j = 0; j < num_candidates; j++) {
				Demographic d = new Demographic();
				//d.ward_id = b.id;
				d.turnout_probability = 1;
				d.population = (int) dd[j];
				d.vote_prob = new double[num_candidates];
				for( int i = 0; i < d.vote_prob.length; i++) {
					d.vote_prob[i] = 0;
				}
				d.vote_prob[j] = 1;
				b.demographics.add(d);
				System.out.println("ward "+b.id+" added demo "+j+" "+d.population);
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
		election_loaded = true;
		
		setEnableds();	
	}
	public void selectLayers() {
		boolean is_evolving = this.evolving;
		if( is_evolving) { featureCollection.ecology.stopEvolving(); }
		featureCollection.loadDistrictsFromProperties(project.district_column);
		DialogSelectLayers dlg = new DialogSelectLayers();
		dlg.setData(featureCollection,project.demographic_columns);
		dlg.show();
		if( !dlg.ok) {
			if( is_evolving) { featureCollection.ecology.startEvolving(); }
			return;
		}

		try {
			project.demographic_columns = dlg.in;
			setDemographicColumns(dlg.in);
		} catch (Exception ex) {
			System.out.println("ex "+ex);
			ex.printStackTrace();
		}
		Feature.display_mode = 1;
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
				fields[i] = new JDBField(headers[i], 'C', 64, 0);
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
		    prefs.setMaxNumberOfPointsPerShape(16650);
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
		      case POLYGON:
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
			for( Ward b : featureCollection.wards) {
				b.has_election_results = false;
			}
			for( int i = 0; i < lines.length; i++) {
				String[] ss = lines[i].split("\t");
				String district = ss[0].trim();
				Ward b = featureCollection.wardHash.get(district);
				if( b == null) {
					not_found_in_geo.add(district);
					System.out.println("not in geo: "+district);

				} else {
					b.has_election_results = true;
				}
			}
			Vector<String> not_found_in_census = new Vector<String>();
			for( Ward b : featureCollection.wards) {
				if( b.has_election_results == false) {
					//not_found_in_census.add(b.name);
					//System.out.println("not in election: |"+b.name+"|");

				}
			}
			if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
				for( Ward b : featureCollection.wards) {
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
				Ward b = featureCollection.wardHash.get(es.getKey());
				double[] dd = es.getValue();
				for( int j = 0; j < num_candidates; j++) {
					Demographic d = new Demographic();
					//d.ward_id = b.id;
					d.turnout_probability = 1;
					d.population = (int) dd[j];
					d.vote_prob = new double[num_candidates];
					for( int i = 0; i < d.vote_prob.length; i++) {
						d.vote_prob[i] = 0;
					}
					d.vote_prob[j] = 1;
					b.demographics.add(d);
					System.out.println("ward "+b.id+" added demo "+d.population+" "+j);
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
		System.out.println("Initializing wards...");
		featureCollection.initwards();
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
		
		Settings.mutation_rate = 0; 
		Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_1.getValue()/100.0;
		Settings.voting_power_balance_weight = sliderVotingPowerBalance.getValue()/100.0;
		Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;
		Settings.population_balance_weight = sliderPopulationBalance.getValue()/100.0;
		Settings.geometry_weight = sliderBorderLength.getValue()/100.0;
		Settings.disconnected_population_weight = sliderDisconnected.getValue()/100.0;
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
			openProjectFile(fd);
			
		}
	}
	public void jbInit() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Automatic Redistricter");
		Dimension d = new Dimension(800,1024);
		//this.setPreferredSize(d);
		this.setSize(d);
		
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
		
		JMenuItem mntmOpenProjectFile = new JMenuItem("Open project file");
		mntmOpenProjectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".json files", "json");
				jfc.setFileFilter(filter);
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( fd == null) {
					return;
				}
				openProjectFile(fd);
			}
		});
		mnFile.add(mntmOpenProjectFile);
		
		mntmSaveProjectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
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
		mnFile.add(mntmSaveProjectFile);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
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
				new OpenShapeFileThread(fd).start();
			}				
		});
		
		mnFile.add(mntmOpenEsriShapefile);
		mnFile.add(chckbxmntmLatitudeLongitude);
		
		mnFile.add(new JSeparator());
		
		//mnFile.add(chckbxmntmOpenCensusResults);
		
		//mnFile.add(mntmOpenElectionResults);
		
		
		mntmImportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
				jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
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
				if( ext.equals("txt")) {
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
				DialogImport di = new DialogImport();
				di.setData(featureCollection, dh.header, dh.data);
				di.show();
			}
		});
		mnFile.add(mntmImportData);
		mntmSelectLayers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLayers();
			}
		});
		
		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)",".dbf (dbase file)"};
				if( project.source_file.substring(project.source_file.length()-4).toLowerCase().equals(".shp")) {
					options = new String[]{".csv (comma-separated)",".txt (tab-deliminted)",".dbf (in separate dbase file)",".dbf (in original shapefile)"};
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
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("dbase file","dbf"));
						jfc.showOpenDialog(null);
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
					String delimiter = ",";
					if( opt == 0) { 
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values","csv"));
					} else if( opt == 1) {
						delimiter = "\t";
						jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab-delimited file","txt"));
					}
					jfc.showOpenDialog(null);
					File f = jfc.getSelectedFile();
					if( f == null)  {
						System.out.println("f is null.");
						return;
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
		mnFile.add(mntmExportData);

		mnFile.add(new JSeparator());
		
		mnFile.add(mntmSelectLayers);

		//mnFile.add(new JSeparator());

		//mnFile.add(mntmImportcsv);
		//mnFile.add(mntmExportcsv);

		
		//mnFile.add(new JSeparator());
		
		//mnFile.add(mntmImportPopulation);
		//mnFile.add(mntmExportPopulation);

		
		mnFile.add(new JSeparator());
		
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
					for( Ward b : featureCollection.wards) {
						b.has_census_results = false;
					}
					for( int i = 0; i < lines.length; i++) {
						String[] ss = lines[i].split("\t");
						String district = ss[0].trim();
						Ward b = featureCollection.wardHash.get(district);
						if( b == null) {
							not_found_in_geo.add(district);
						} else {
							b.has_census_results = true;
						}
					}
					Vector<String> not_found_in_census = new Vector<String>();
					for( Ward b : featureCollection.wards) {
						if( b.has_census_results == false) {
							//not_found_in_census.add(b.name);
						}
					}
					if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
						for( Ward b : featureCollection.wards) {
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
						Ward b = featureCollection.wardHash.get(district);
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
		
		mntmShowDemographics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("old mode: "+Feature.display_mode );
				Feature.display_mode = mntmShowDemographics.isSelected() ? 3 : 0;
				System.out.println("new mode: "+Feature.display_mode );
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
		
		mnView.add(new JSeparator());

		mnView.add(mntmShowStats);
		mnView.add(mntmShowGraph);
		
		JMenuItem mntmShowProperties = new JMenuItem("Show properties");
		mntmShowProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] headers = featureCollection.getHeaders();
				String[][] data = featureCollection.getData(headers);

				df.setTableSource(new DataAndHeader(data,headers));
				df.setTitle("Map Properties");
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
					for( int i = 0; i < dm.ward_districts.length; i++) {
						Ward b = featureCollection.ecology.wards.get(i);
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
				for( Ward b : featureCollection.wards) {
					b.temp = -1;
				}
				for( int i = 0; i < lines.length; i++) {
					try {
						String[] ss = lines[i].split(",");
						String district = ss[0].trim();
						Ward b = featureCollection.wardHash.get(district);
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
				for( Ward b : featureCollection.wards) {
					if( b.temp < 0) {
						//not_found_in_census.add(b.name);
					}
				}
				if( not_found_in_census.size() > 0 || not_found_in_geo.size() > 0) {
					for( Ward b : featureCollection.wards) {
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
				int[] new_ward_districts = new int[featureCollection.wards.size()];
				int num_districts = 0;
				for( int i = 0; i < new_ward_districts.length; i++) {
					int d = featureCollection.wards.get(i).temp;
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
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.wards,Settings.num_districts,new_ward_districts));
				}
				while( featureCollection.ecology.population.size() < Settings.population) {
					featureCollection.ecology.population.add(new DistrictMap(featureCollection.wards,Settings.num_districts,new_ward_districts));
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
						for( int i = 0; i < dm.ward_districts.length; i++) {
							Ward b = featureCollection.ecology.wards.get(i);
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
		panel_2.setBounds(0, 449, 200, 386);
		panel.add(panel_2);
		panel_2.setLayout(null);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel lblCompactness = new JLabel("Border length");
		lblCompactness.setBounds(6, 36, 90, 16);
		panel_2.add(lblCompactness);
		sliderBorderLength.setBounds(6, 57, 190, 29);
		panel_2.add(sliderBorderLength);
		
		JLabel lblContiguency = new JLabel("Representation imbalance");
		lblContiguency.setBounds(6, 220, 172, 16);
		panel_2.add(lblContiguency);
		sliderRepresentation.setBounds(6, 245, 190, 29);
		panel_2.add(sliderRepresentation);
		
		JLabel lblEvolutionaryPressure = new JLabel("Evolutionary pressure");
		lblEvolutionaryPressure.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEvolutionaryPressure.setBounds(6, 9, 179, 16);
		panel_2.add(lblEvolutionaryPressure);
		
		JLabel lblProportionalRepresentation = new JLabel("Population imbalance");
		lblProportionalRepresentation.setBounds(6, 158, 172, 16);
		panel_2.add(lblProportionalRepresentation);
		sliderPopulationBalance.setBounds(6, 179, 190, 29);
		panel_2.add(sliderPopulationBalance);
		
		JLabel lblVotingPowerBalance = new JLabel("Voting power imbalance");
		lblVotingPowerBalance.setBounds(6, 286, 172, 16);
		panel_2.add(lblVotingPowerBalance);
		sliderVotingPowerBalance.setBounds(6, 307, 190, 29);
		panel_2.add(sliderVotingPowerBalance);
		
		JLabel lblConnectedness = new JLabel("Disconnected population");
		lblConnectedness.setBounds(6, 97, 172, 16);
		panel_2.add(lblConnectedness);
		
		sliderDisconnected.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Settings.disconnected_population_weight = sliderDisconnected.getValue()/100.0;
			}
		});
		sliderDisconnected.setBounds(6, 118, 190, 29);
		panel_2.add(sliderDisconnected);
		
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
		panel_3.setBounds(0, 272, 200, 166);
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
		
		textField.setText("128");
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Population dynamics");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(6, 6, 159, 16);
		panel_3.add(lblNewLabel);
		JLabel lblBorderMutation = new JLabel("% border mutation");
		lblBorderMutation.setBounds(6, 103, 172, 16);
		panel_3.add(lblBorderMutation);
		slider_1.setBounds(6, 130, 190, 29);
		panel_3.add(slider_1);
		
		JLabel lblTrials = new JLabel("Elections simulated");
		lblTrials.setBounds(6, 74, 134, 16);
		panel_3.add(lblTrials);
		textFieldElectionsSimulated.setBounds(138, 68, 58, 28);
		panel_3.add(textFieldElectionsSimulated);
		textFieldElectionsSimulated.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textFieldElectionsSimulated.postActionEvent();
			}
		});
		textFieldElectionsSimulated.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.num_elections_simulated = new Integer(textFieldElectionsSimulated.getText());
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textFieldElectionsSimulated.setText("4");
		textFieldElectionsSimulated.setColumns(10);
		textFieldNumDistricts.setText("10");
		
		
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
				} catch (Exception ex) { }
			}
		});
		textFieldNumDistricts.setColumns(10);
		textFieldNumDistricts.setBounds(132, 51, 52, 28);
		panel.add(textFieldNumDistricts);
		
		JLabel lblNumOfDistricts = new JLabel("Num. of districts");
		lblNumOfDistricts.setBounds(6, 57, 124, 16);
		panel.add(lblNumOfDistricts);
		
		JButton button = new JButton("<|");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Ecology.invert = -1;
				
		    	addEcologyListeners();
				featureCollection.ecology.startEvolving();
				evolving = true;
				setEnableds();

			}
		});
		button.setBounds(22, 11, 46, 29);
		panel.add(button);
		
		JButton btnX = new JButton("| |");
		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				featureCollection.ecology.stopEvolving();
				evolving = false;
				setEnableds();

			}
		});
		btnX.setBounds(80, 11, 46, 29);
		panel.add(btnX);
		
		JButton button_2 = new JButton("|>");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Ecology.invert = 1;
				addEcologyListeners();
				featureCollection.ecology.startEvolving();
				evolving = true;
				setEnableds();

			}
		});
		button_2.setBounds(138, 11, 46, 29);
		panel.add(button_2);
		
		textFieldMembersPerDistrict = new JTextField();
		textFieldMembersPerDistrict.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				textFieldMembersPerDistrict.postActionEvent();
			}
		});
		textFieldMembersPerDistrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Settings.members_per_district = Integer.parseInt(textFieldMembersPerDistrict.getText());
				} catch (Exception ex) { }
			}
		});
		textFieldMembersPerDistrict.setText("1");
		textFieldMembersPerDistrict.setColumns(10);
		textFieldMembersPerDistrict.setBounds(132, 88, 52, 28);
		panel.add(textFieldMembersPerDistrict);
		
		JLabel lblMembersPerDistrict = new JLabel("Members per district");
		lblMembersPerDistrict.setBounds(6, 94, 124, 16);
		panel.add(lblMembersPerDistrict);
		
		comboBoxPrimaryKey.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxPrimaryKey) return;
				setPrimaryKeyColumn((String)comboBoxPrimaryKey.getSelectedItem());
			}
		});
		comboBoxPrimaryKey.setBounds(6, 140, 178, 20);
		panel.add(comboBoxPrimaryKey);
		
		comboBoxPopulation.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxPopulation) return;
				setPopulationColumn((String)comboBoxPopulation.getSelectedItem());
			}
		});
		comboBoxPopulation.setBounds(6, 191, 178, 20);
		panel.add(comboBoxPopulation);
		
		JLabel lblPrimaryKeyColumn = new JLabel("Primary key column");
		lblPrimaryKeyColumn.setBounds(6, 121, 182, 16);
		panel.add(lblPrimaryKeyColumn);
		
		JLabel lblPopulationColumn = new JLabel("Population column");
		lblPopulationColumn.setBounds(6, 171, 182, 16);
		panel.add(lblPopulationColumn);
		
		lblDistrictColumn = new JLabel("District column");
		lblDistrictColumn.setBounds(6, 222, 182, 16);
		panel.add(lblDistrictColumn);
		
		comboBoxDistrictColumn = new JComboBox();
		comboBoxDistrictColumn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( hushcomboBoxDistrict) return;
				setDistrictColumn((String)comboBoxDistrictColumn.getSelectedItem());
			}
		});
		comboBoxDistrictColumn.setBounds(6, 241, 178, 20);
		panel.add(comboBoxDistrictColumn);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_boundary_rate = boundary_mutation_rate_multiplier*slider_1.getValue()/100.0;
			}
		});
		sliderVotingPowerBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.voting_power_balance_weight = sliderVotingPowerBalance.getValue()/100.0;
			}
		});
		sliderRepresentation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disenfranchise_weight = sliderRepresentation.getValue()/100.0;

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
		
		chckbxmntmMutateAll.setEnabled(!Settings.replace_all);

		
		chckbxmntmMutateAll.setSelected(true);
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
		slider_1.setValue((int)(Settings.mutation_boundary_rate*100.0/MainFrame.boundary_mutation_rate_multiplier));
		invalidate();
		repaint();
	}	
}
