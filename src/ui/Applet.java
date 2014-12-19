package ui;

import javax.swing.*;

/*
 * 
 * convert shapefile to geojson:
 * for %f in (*.shp) do ogr2ogr -f "GeoJSON" "%~dpnf.json" "%f"

 */

public class Applet extends JApplet {
    public static void main( String[] args ) {
		new Applet();
	}
    public Applet() {
    	new MainFrame().show();
    }


}
