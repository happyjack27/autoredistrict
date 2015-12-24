package excel;

import java.util.*;
import java.applet.Applet;
import java.awt.*;
import java.io.InputStream;

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
		progressbar.sourceTF1.setText("Exporting...");
		progressbar.show();
		start();
	}
	public void run() {
		ExcelObj app = new ExcelObj();
		app.init();
		InputStream is = Applet.class.getResourceAsStream("/resources/export_template.xls");
		try {
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ExcelObj wb = app.Workbooks();
		
		
		exportTableToSheet(wb.Worksheets(1),summary);
		exportTableToSheet(wb.Worksheets(2),districts);
		exportTableToSheet(wb.Worksheets(3),parties);
		exportTableToSheet(wb.Worksheets(4),demo);
		exportTableToSheet(wb.Worksheets(5),seats);

	}

}
