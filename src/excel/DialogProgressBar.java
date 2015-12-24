package excel;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class DialogProgressBar extends JDialog{

	
	int max;
	JTextField sourceTF1 = new JTextField();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	public JLabel taskLabel = new JLabel();
	private JProgressBar progressBar = new JProgressBar();
	public DialogProgressBar(){
		setTitle("Progress");
		jbInit();
	}
	public void setMax(int max){
		this.max = max;
	}
	public void setValue(int val){
		int percentage = (val * 100) / max;
		progressBar.setValue(percentage);
		progressBar.setString("" + percentage + "%");
	}
	public void setLabel(String text){
		taskLabel.setText(text);
	}
	private void jbInit(){
		taskLabel.setFont(new java.awt.Font("Dialog", 0, 11));
		taskLabel.setText("Performing task...");
		taskLabel.setBounds(new Rectangle(18, 9, 249, 22));
		getContentPane().setLayout(null);
		setSize(new Dimension(399, 106));
		progressBar.setOrientation(SwingConstants.HORIZONTAL);
		progressBar.setFont(new java.awt.Font("Dialog", 0, 11));
		progressBar.setBorder(BorderFactory.createLoweredBevelBorder());
		progressBar.setDebugGraphicsOptions(0);
		progressBar.setMaximumSize(new Dimension(32767, 16));
		progressBar.setOpaque(true);
		progressBar.setMinimum(0);
		progressBar.setString("0%");
		progressBar.setBorderPainted(true);
		progressBar.setStringPainted(true);
		progressBar.setBounds(new Rectangle(16, 38, 356, 19));
		getContentPane().add(taskLabel, null);
		getContentPane().add(progressBar, null);
	}
}
