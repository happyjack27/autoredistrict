package ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;

import solutions.Ecology;

import java.awt.*;
import java.util.Vector;

public class PanelGraphDrawArea extends JPanel {
	public double pctToHide = 0.0;
	public boolean use_normalized = false;
	
    public String[] ss = new String[]{
    		"Generation",
    		"Population size",
    		"Num. of districts",
    		
    		"Mutation rate",
    		
    		//4
    		"Proportionalness (global)",
    		"Competitiveness (victory margin)",
    		"Partisan symmetry",
    		"Racial vote dilution",
    		"",//"Voting power imbalance",

    		//9
    		"",//"Annealing floor",
    		
    		//10
    		"Compactness",
    		"Contiguity",
    		"Equal population",
    		"Splits",
    		"",
    };


    public boolean[] b_draw = new boolean[]{
    		false,false,false,
    		true,
    		true,true,true,true,false,
    		true,
    		true,true,true,true,
    		false,
    		//true,true,
    };
    
    
    Color[] cc = new Color[] {
    		Color.gray,
    		Color.black,
    		Color.orange,
    		Color.orange.darker(),
    		
    		Color.blue,
    		Color.cyan,
    		Color.green,
    		Color.red,
    		Color.magenta,
    		
    		Color.gray,
    		
    		Color.blue.darker(),
    		Color.cyan.darker(),
       		Color.green.darker(),
    		Color.red.darker(),
    		Color.magenta.darker(),

    		Color.yellow,
       		Color.yellow.darker(),
    		Color.orange.darker(),

 
    };


    public PanelGraphDrawArea() {
		super();
		setLayout(null);
		
	}
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = this.getSize();
      
        g.setColor(Color.white);
        g.fillRect(0, 0, (int)d.getWidth(), (int)d.getHeight());
        g.setColor(Color.black);
        g.drawRect(0, 0, (int)d.getWidth()-1, (int)d.getHeight()-1);
        
        Vector<double[]> v = use_normalized ? Ecology.normalized_history : Ecology.history;

        if( v != null && v.size() > 0) {
        	int start = (int)(pctToHide*(double)v.size());
            double scalex = ((double)d.getWidth())/(double)(v.size()-start);
            if( start >= v.size()) {
            	return;
            }
            double[] scaleys = new double[v.get(start).length];
            double[] maxys = new double[v.get(start).length];
            double[] minys = new double[v.get(start).length];
            for( int i = 0; i < minys.length; i++) {
            	minys[i] = 0;
            }
            for( int i = start; i < v.size(); i++) {
            	double[] vals = v.get(i);
            	for( int j = 0; j < vals.length; j++) {
            		if( i == 0 || vals[j] > maxys[j]) {
            			maxys[j] = vals[j];
            		}
            		if( vals[j] < minys[j]) {
            			minys[j] = vals[j];
            		}
            	}
            }
            /*
            maxys[3] = maxys[3] > maxys[9] ? maxys[3] : maxys[9];
            maxys[9] = maxys[3];
            */
        	for( int j = 0; j < maxys.length; j++) {
        		scaleys[j] = ((double)d.getHeight())/(maxys[j]-minys[j]);
        	}
            double[] last_vals = v.get(start);
            for( int i = start+1; i < v.size(); i++) {
            	double[] vals = v.get(i);
            	
            	for( int j = 0; j < vals.length; j++) {
            		if( !b_draw[j]) {
            			continue;
            		}
            		g.setColor(cc[j]);
            		g.drawLine(
            				(int)(scalex*(double)(i-1-start)),
            				(int)(d.getHeight()-scaleys[j]*(double)(last_vals[j]-minys[j])),
            				(int)(scalex*(double)(i-start)),
            				(int)(d.getHeight()-scaleys[j]*(double)(vals[j]-minys[j]))
            				);
            	}
            	
            	
            	last_vals = vals;
            }

        } else {
        	System.out.println("no history found! "+v);
        }
    }

}
