package ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import solutions.iDiscreteEventListener;

public class PanelGraph extends JPanel implements iDiscreteEventListener {
	PanelGraphDrawArea drawPanel = new PanelGraphDrawArea();
	JSlider slider = new JSlider();

	
	JCheckBox[] cbs = new JCheckBox[16];
	
	public void update() {
		for( int i = 0; i < cbs.length; i++) {
			if( cbs[i] != null) {
				drawPanel.b_draw[i] = cbs[i].isSelected();
			}
		}
		drawPanel.invalidate();
		drawPanel.repaint();
		this.invalidate();
		this.repaint();
	}
	public void makeCBs() {
		
		for( int i = 0; i < drawPanel.ss.length; i++) {
			cbs[i] = new JCheckBox();
			cbs[i].setSelected(drawPanel.b_draw[i]);
			cbs[i].setText(drawPanel.ss[i]);
			cbs[i].setForeground(drawPanel.cc[i]);
			cbs[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					update();
				}
			});
			int x = (i-3)/6;
			int y = (i-3)%6;
			cbs[i].setBounds(x*300+20, y*20+250+60-20, 300, 20);
			add(cbs[i]);
		}
		
	}

	public PanelGraph() {
		this.setLayout(null);
		this.setSize(new Dimension(600, 400));
		this.setPreferredSize(new Dimension(600, 433));
		
		drawPanel.setBounds(20, 6, 559, 210);
		add(drawPanel);
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				drawPanel.pctToHide = ((double)slider.getValue())/100.0;
				System.out.println("pct to hide: "+drawPanel.pctToHide);
				update();
			}
		});
		slider.setBounds(287, 228, 292, 29);
		slider.setValue(0);
		add(slider);
		makeCBs();
	}
	@Override
	public void eventOccured() {
		update();
	}
}
