package excel;

import java.util.*;
import java.applet.Applet;
import java.awt.*;
import java.io.*;

import javax.swing.*;

public class ExportThread extends Thread {
	JTable summary; JTable districts; JTable parties; JTable demo; JTable seats;
	DialogProgressBar progressbar;

	public void exportTableToSheet(ExcelObj ws, String[] header, Vector<String[]> table, int start_x, int start_y) {
		for( int i = 0; i < header.length; i++) {
			ws.Cells(start_y, start_x+i).setValue(header[i]);
		}
		for( int i = 0; i < table.size(); i++) {
			String[] ss = table.get(i);
			for( int j = 0; j < header.length; j++) {
				ws.Cells(start_y+i, start_x+j).setValue(ss[j]);
			}
		}
	}
	public void exportTableToSheet(ExcelObj ws, JTable table) {
		String[] header = new String[table.getColumnCount()];
		for( int i = 0; i < header.length; i++) {
			header[i] = table.getColumnName(i);
		}
		Vector<String[]> cells = new Vector<String[]>();
		for( int i = 0; i < table.getRowCount(); i++) {
			String[] ss = new String[header.length];
			for( int j = 0; j < header.length; j++) {
				ss[j] = (String)table.getValueAt(i,j);
			}
			cells.add(ss);
		}
		exportTableToSheet(ws,header,cells,2,2);
	}
	public void export(JTable summary, JTable districts, JTable parties, JTable demo, JTable seats) {
		this.summary = summary;
		this.districts = districts;
		this.parties = parties;
		this.demo = demo;
		this.seats = seats;
		this.progressbar = new DialogProgressBar();
		progressbar.taskLabel.setText("Exporting...");
		progressbar.show();
		//loadJarDll("/resources/jcom.dll");
		loadJarDll("/resources/jcom.dll","jcom.dll");
		//loadJarDll("/resources/jcom.lib","jcom.so");
		start();
	}
	public void run() {
		ExcelObj app = new ExcelObj();
		app.init();
		InputStream initialStream = Applet.class.getResourceAsStream("/resources/export_template.xls");
		String tmpdir = System.getProperty("java.io.tmpdir");
		
		//File f = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();

		String targetFileName = tmpdir+"autoredistrict_export.xls";
	    File targetFile = new File(targetFileName);//"src/main/resources/targetFile.tmp");
	    
	    try {
		    OutputStream outStream = new FileOutputStream(targetFile);
			while( initialStream.available() > 0) {
			    byte[] buffer = new byte[initialStream.available()];
			    initialStream.read(buffer);
			    outStream.write(buffer);
			}
		    outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		ExcelObj wb = app.Open(targetFileName);
		
	    try {
	    	progressbar.taskLabel.setText("Exporting summary...");
			exportTableToSheet(wb.Worksheets(1),summary);
	    	progressbar.taskLabel.setText("Exporting districts...");
			exportTableToSheet(wb.Worksheets(2),districts);
	    	progressbar.taskLabel.setText("Exporting parties...");
			exportTableToSheet(wb.Worksheets(3),parties);
	    	progressbar.taskLabel.setText("Exporting demo...");
			exportTableToSheet(wb.Worksheets(4),demo);
	    	progressbar.taskLabel.setText("Exporting seats...");
			exportTableToSheet(wb.Worksheets(5),seats);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    app.setVisible(true);
	    /*
		try {		
			wb.Close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			app.Quit();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		progressbar.hide();

	}
	public static void main(String[] ss) {
		System.out.println(System.getProperty("java.io.tmpdir"));
		new ExportThread().export(null,null,null,null,null);
	}
	
	public static void loadJarDll(String name,String saveas) {
		try {
			//System.loadLibrary(name);
			
	    InputStream in = Applet.class.getResourceAsStream(name);
	    byte[] buffer = new byte[1024];
	    int read = -1;
	    File temp = new File(System.getProperty("java.io.tmpdir")+saveas);//File.createTempFile(name, "");
	    FileOutputStream fos = new FileOutputStream(temp);

	    while((read = in.read(buffer)) != -1) {
	        fos.write(buffer, 0, read);
	    }
	    fos.close();
	    in.close();

	    try { 
	    	System.load(temp.getAbsolutePath());
	    } catch (Exception ex) {
	    	JOptionPane.showMessageDialog(null, "This feature only works in Windows.");
	    }
	    
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
