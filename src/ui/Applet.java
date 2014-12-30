package ui;

import java.io.InputStream;

import javax.swing.*;

import mapCandidates.Gaussian;
import mapCandidates.StaticFunctions;

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
		//InputStream in = getClass().getResourceAsStream("/resources/Wards_111312_ED_110612.json"); 
		//System.out.println("in: "+in);
		//System.exit(0);

    	StaticFunctions.binomial(1, 1);
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.51));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.52));
    	System.out.println(""+Gaussian.binomial_as_normal(1001, 500, 0.55));
    	/*
    	 * binomial cache created.
binomial_as_normal n:1001.0 k:500.0 p:0.51 ret:0.7468185279694906
0.7468185279694906
binomial_as_normal n:1001.0 k:500.0 p:0.52 ret:0.902888853982651
0.902888853982651
binomial_as_normal n:1001.0 k:500.0 p:0.55 ret:0.9993399261462654
0.9993399261462654
pct to hide: 0.0
    	 * 
    	 */

    	new MainFrame().show();
    }


}
