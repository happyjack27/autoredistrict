package stats;

import solutions.*;
import geography.*;

import javax.swing.*;
import javax.swing.event.*;

public class Stat implements ChangeListener {
	private double weight = 1;
	public void setWeight(double w) { weight = w; }
	public double getWeight() { return weight; }
	
	public String getName() { return ""; }
	public String getDescription() { return ""; }

	public double getScore(DistrictMap dm, FeatureCollection fc) { return 0; }
	
	public double[][] getScoresByDistrict(DistrictMap dm, FeatureCollection fc) { return new double[][]{}; }
	public double[][] getScoresByParty(DistrictMap dm, FeatureCollection fc) { return new double[][]{}; }
	
	public void stateChanged(ChangeEvent changeEvent) {
		Object source = changeEvent.getSource();
		if (source instanceof JSlider) {
			JSlider theJSlider = (JSlider) source;
			weight = theJSlider.getValue()/100;
		}
	}

}