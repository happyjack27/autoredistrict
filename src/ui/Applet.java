package ui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.*;

import util.StaticFunctions;


// failed california connection reset again
// failed on texas again, connection reset

// failed on kentucky again, file not found
// failed on rhode ilsand again, file not found

//failed california, kentucky , new hampshire - states with spaces, rhode island, texas

public class Applet extends JApplet {
	public static MainFrame mainFrame = null;
	public static boolean no_gui = false;
	public static String open_project = null;
	public static String[] args = null;
	
    public static void main( String[] _args ) {
    	args = _args;
		try {
			UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		Download.init();
		
    	if( false) {
    		// failed california connectioin reset
    		//failed kentucky , new hampshire - states with spaces, rhode island, texas
	    	for( int i = 0; i < Download.states.length; i++) {
                //not found - did not participate in census
                //not found --?
                //reset
                if(Download.states[i].equals("Kentucky") || Download.states[i].equals("Rhode Island") || Download.states[i].equals("California") || Download.states[i].equals("Texas") //reset
	    				) {
	    			continue;
	    		}
	    		
	    		System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar delete "+i);
	    		System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar download "+i);
	    		System.out.println("java -jar -Xmx4096M -Xms1024M autoredistrict.jar clean "+i);
	    	}
			System.exit(0);
    	}
		
    	for( int i = 0; i < args.length; i++) {
    		System.out.println("arg: "+args[i]);
    		String arg = args[i];
    		if( arg.contains("nogui") ||  arg.contains("no_gui") || arg.contains("headless")) {
    			System.out.println("requested headless...");
    			no_gui = true;
    		}
    		if( arg.contains("project") || arg.contains("file")) {
    			if( i+1 < args.length) {
    				open_project = args[i+1];
    			}
    		}
    	}
    	
		if( args.length > 1 && args[0].equals("download")) {
			new Applet();
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			if( Download.states[Download.istate].length() == 0) {
				System.exit(0);
			}
			Download.istate--;
			mainFrame.downloadNextState();
		} else
		if( args.length > 1 && args[0].equals("delete")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			Download.delete();
			System.exit(0);
		} else 
		if( args.length > 1 && args[0].equals("clean")) {
			Download.exit_when_done = true;
			Download.prompt = false;
			Download.cyear=2010;
			Download.vyear=2012;
			Download.istate = Integer.parseInt(args[1]);
			Download.clean();
			System.exit(0);
		} else {
			new Applet();
		}
	}
	public static void deleteRecursive(File f)  {
		System.out.println("deleting "+f.getAbsolutePath());
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      deleteRecursive(c);
	  }
	  f.delete();
	}

    public Applet() {
    	
    	String version = System.getProperty("java.version");
    	System.out.println("jre version: "+version);
    	if( versionCompare(version,"1.5") < 0) {
    		JOptionPane.showMessageDialog(null, "You are running an out-of-date version of Java."
    				+"\nWith this current installed version of Java, the program will not be able to allocate enough memory."
    				+"\n"
    				+"\nPlease upgrade your java version."
    				+"\nTo find the latest release, google \"java jre download\"."
    				+"\n"
    				+"\nOnce you hit okay, you will be taken automatically to the download page."
    				+"\n"
    				+"\nAfter you've updated your Java version, run this program again."
    				);
    		browseTo("http://www.google.com/search?q=java+jre+download&btnI");
        	System.exit(0);
    	}
    	
    	mainFrame = new MainFrame();
    	for( int i = 0; i < args.length-1; i++) {
	    	if( args[i].equals("run")) {
				mainFrame.ip.queueInstructionsFromFile(args[i+1]);
	    	}
    	}
    	if( !no_gui) {
    		mainFrame.show();
    	}
    }
	public static void browseTo(String s) {
		try {
			Desktop.getDesktop().browse(new URI(s));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println("failed "+e1);
			e1.printStackTrace();
    		try {
				//Desktop.getDesktop().open(htmlFile.toURI());
			} catch (Exception e2) {
				System.out.println("failed "+e2);
				e1.printStackTrace();
				
			}
		}
	}
    
    public int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))  {
          i++;
        }
        if (i < vals1.length && i < vals2.length)  {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        } else  {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

}
