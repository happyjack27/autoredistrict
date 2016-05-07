package util;

import java.io.*;

import ui.Download;

public class ScriptMaker {
	public static void main(String[] args) {
		File f = new File(args[0]);
		try {
			FileInputStream fis = new FileInputStream(f);
			String s = Util.readStream(fis);
			make_scripts(s);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public static void make_scripts(String template) {
		String prepend = "";
		//prepend = "xvfb-run -a -e xvfb.log  ";
		boolean gui = true;
		String base_dir = "";//"/Users/jimbrill/git/autoredistrict/jar/";
		Download.init();

		
		StringBuffer main = new StringBuffer();
		int i0 = 0;
		//while( !Download.states[i0].equals("Indiana")) { i0++; }
		for( int i = 0; i < Download.apportionments.length; i++){// && !Download.states[i].equals("Colorado"); i++) {
			if( Download.apportionments[i] < 6) {
				//continue;
			}
			if( Download.apportionments[i] < 1) {
				continue;
			}
			if( Download.apportionments[i] > 5) {
				//continue;
			}
			if( i < 35) {
				//continue;
			}
			String state = Download.states[i];
			
			if( false
					|| state.equals("Texas")
					|| state.equals("Florida")
					|| state.equals("California")) {
				//continue;
			}
			
			String s = "LOAD "+i+ " 2010 2012\n"+template+"\nEXIT\n";
			
			s = s.replaceAll("\\[STATE\\]",Download.states[i]);
			s = s.replaceAll("\\[SEATS\\]",""+Download.apportionments[i]);
			StringBuffer fv = new StringBuffer();
			if( Download.apportionments[i] <= 5) {
				fv.append("SET DISTRICTS SEATS_PER_DISTRICT "+Download.apportionments[i]+"\n"); 			
			} else {
				fv.append("SET DISTRICTS FAIRVOTE_SEATS "+Download.apportionments[i]+"\n"); 
			}
			s = s.replaceAll("\\[FV_SEATS\\]",fv.toString());
				
			File f = new File(base_dir+"subscript"+i);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(f);
				fos.write(s.getBytes());
				fos.flush();
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			main.append(prepend+"java -jar -Xmx4096M -Xms1024M autoredistrict.jar "+(gui?"":"nogui ")+"run subscript"+i+"\n");
			main.append(prepend+"java -jar -Xmx4096M -Xms1024M autoredistrict.jar clean "+i+"\n");
		}
		File f = new File(base_dir+"mainscript");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(main.toString().getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
