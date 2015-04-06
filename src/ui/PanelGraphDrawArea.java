package ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;

import solutions.Ecology;

import java.awt.*;

public class PanelGraphDrawArea extends JPanel {
	public double pctToHide = 0.0;
	
    public String[] ss = new String[]{
    		"Generation",
    		"Population size",
    		"Num. of districts",
    		"Mutation rate",
    		
    		//4
    		"Border length weight",
    		"Disconnected population weight",
    		"Population imbalance weight",
    		"Representation imbalance weight",
    		"Voting power imbalance weight",

    		//9
    		"Annealing floor",
    		
    		//10
    		"Border length",
    		"Disconnected population",
    		"Population imbalance",
    		"Representation imbalance",
    		"Voting power imbalance",
    };


    public boolean[] b_draw = new boolean[]{
    		false,true,false,true,
    		false,false,false,false,false,
    		true,
    		true,true,true,true,true,
    		true,true,
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

        if( Ecology.history != null && Ecology.history.size() > 0) {
        	int start = (int)(pctToHide*(double)Ecology.history.size());
            double scalex = ((double)d.getWidth())/(double)(Ecology.history.size()-start);
            double[] maxys = new double[Ecology.history.get(start).length];
            for( int i = start; i < Ecology.history.size(); i++) {
            	double[] vals = Ecology.history.get(i);
            	for( int j = 0; j < vals.length; j++) {
            		if( i == 0 || vals[j] > maxys[j]) {
            			maxys[j] = vals[j];
            		}
            	}
            }
            maxys[3] = maxys[3] > maxys[9] ? maxys[3] : maxys[9];
            maxys[9] = maxys[3];
        	for( int j = 0; j < maxys.length; j++) {
        		maxys[j] =  ((double)d.getHeight())/maxys[j];
        	}
            double[] last_vals = Ecology.history.get(start);
            for( int i = start+1; i < Ecology.history.size(); i++) {
            	double[] vals = Ecology.history.get(i);
            	
            	for( int j = 0; j < vals.length; j++) {
            		if( !b_draw[j]) {
            			continue;
            		}
            		g.setColor(cc[j]);
            		g.drawLine(
            				(int)(scalex*(double)(i-1-start)),
            				(int)(d.getHeight()-maxys[j]*(double)(last_vals[j])),
            				(int)(scalex*(double)(i-start)),
            				(int)(d.getHeight()-maxys[j]*(double)(vals[j]))
            				);
            	}
            	
            	
            	last_vals = vals;
            }

        } else {
        	System.out.println("no history found!");
        }
    }

}
