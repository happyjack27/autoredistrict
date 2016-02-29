package ui;

import java.awt.*;
import javax.swing.*;

/**
 * JComponent used to draw line numbers. This JComponent should be added as a row header view in a JScrollPane. Based upon the 
 * LineNumberModel provided, this component will draw the line numbers as needed.
 * @author Greg Cope
 *
 */

public class LineNumberComponent extends JComponent{
	static final long serialVersionUID = 432143214L;
	public static final int LEFT_ALIGNMENT = 0;
	public static final int RIGHT_ALIGNMENT = 1;
	public static final int CENTER_ALIGNMENT = 2;

	//pixel padding on left and right
	private static final int HORIZONTAL_PADDING = 1;
	//pixel padding on left and right
	private static final int VERTICAL_PADDING = 3;
	private int alignment = LEFT_ALIGNMENT;
	
	private LineNumberModel lineNumberModel;
	private JTextArea textArea;
	
	interface LineNumberModel {
		public int getNumberLines();
		public Rectangle getLineRect(int line);
	}
	

	class LineNumberModelImpl implements LineNumberModel{

		@Override
		public int getNumberLines() {
			return textArea.getLineCount();
		}

		@Override
		public Rectangle getLineRect(int line) {
			try{
				return textArea.modelToView(textArea.getLineStartOffset(line));
			}catch( Exception e){
				e.printStackTrace();
				return new Rectangle();
			}
		}
	}

	public LineNumberComponent(JTextArea textArea){
		super();
		this.textArea = textArea;
		setLineNumberModel(new LineNumberModelImpl());
	}

	public void setLineNumberModel(LineNumberModel model){
		lineNumberModel = model;
		if ( model != null ){
		    adjustWidth();
		}
		repaint();
	}

	public void adjustWidth(){
		int max = lineNumberModel.getNumberLines();
		if ( getGraphics() == null ){
			return;
		}

		int width = getGraphics().getFontMetrics().stringWidth(String.valueOf(max)) + 2 * HORIZONTAL_PADDING;
		JComponent c = (JComponent)getParent();
		if (c == null){//not within a container
			return;
		}

		Dimension dimension = c.getPreferredSize();
		if ( c instanceof JViewport ){//do some climbing up the component tree to get the main JScrollPane view
			JViewport view = (JViewport)c;
			Component parent = view.getParent();
			if ( parent != null && parent instanceof JScrollPane){
				JScrollPane scroller = (JScrollPane)view.getParent();
				dimension = scroller.getViewport().getView().getPreferredSize();
			}			
		}

		if ( width > getPreferredSize().width || width < getPreferredSize().width){
			setPreferredSize(new Dimension(width + 2*HORIZONTAL_PADDING, dimension.height));
			revalidate();
			repaint();
		}
	}

	public void setAlignment(int alignment) throws IllegalArgumentException{
		if ( alignment < 0 || alignment > 2 ){
			throw new IllegalArgumentException("Invalid alignment option");
		}
		this.alignment = alignment;
	}

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if ( lineNumberModel == null ){
			return;
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(getForeground());

		//iterate over all lines to draw the line numbers.

		for ( int i = 0; i < lineNumberModel.getNumberLines(); i++ ){
			Rectangle rect = lineNumberModel.getLineRect(i);
			String text = String.valueOf(i + 1);
			int yPosition = rect.y + rect.height - VERTICAL_PADDING;
			int xPosition = HORIZONTAL_PADDING;//default to left alignment
			switch (alignment){
			case RIGHT_ALIGNMENT:
				xPosition = getPreferredSize().width - g.getFontMetrics().stringWidth(text) - HORIZONTAL_PADDING;
				break;
			case CENTER_ALIGNMENT:
				xPosition = getPreferredSize().width/2 - g.getFontMetrics().stringWidth(text)/2;
				break;	
			default://left alignment, do nothing
				break;
			}
			g2d.drawString(String.valueOf(i+1), xPosition, yPosition);
		}
	}
}