package util;

import dbf.DBFReader;
import dbf.DBFWriter;
import dbf.DBField;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

	public static String readStream(InputStream is) {
	    StringBuilder sb = new StringBuilder(512);
	    try {
	        Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
	        int c = 0;
	        while ((c = r.read()) != -1) {
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    return sb.toString();
	}

	public static Vector<String[]> readDelimited(File f, String cell, String line) {
		StringBuffer sb = new StringBuffer(); 
		Vector<String[]> v = new Vector<String[]>();
		try {
			FileInputStream fis = new FileInputStream(f);
			while( fis.available() > 0) {
				byte[] bb = new byte[fis.available()];
				fis.read(bb);
				sb.append( new String(bb));
				Thread.sleep(10);
			}
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String s = sb.toString();
		String[] sss = s.split(line);
		for( int i = 0; i < sss.length; i++) {
			String[] ss = sss[i].split(cell);
			v.add(ss);
		}
		
		return v;
	}

	public static String readText(File f) {
		StringBuffer sb = new StringBuffer(); 
		try {
			FileInputStream fis = new FileInputStream(f);
			while( fis.available() > 0) {
				byte[] bb = new byte[fis.available()];
				fis.read(bb);
				sb.append( new String(bb));
				Thread.sleep(10);
			}
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String s = sb.toString();
		return s;
	}

	public static void writeDelimited(File f, String cell, String line,  Vector<String[]> v) {
		StringBuffer sb = new StringBuffer(); 
		for( int i = 0; i < v.size(); i++) {
			String[] ss = v.get(i);
			for( int j = 0; j < ss.length; j++) {
				if( j > 0) {
					sb.append(cell);
				}
				sb.append(ss[j]);
			}
			sb.append(line);
		}
		
		try {
			FileOutputStream fis = new FileOutputStream(f);
			fis.write(sb.toString().getBytes());
			fis.flush();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void writeText(File f, String text) {
		try {
			FileOutputStream fis = new FileOutputStream(f);
			fis.write(text.getBytes());
			fis.flush();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(String zipFilePath, String destDirectory) throws IOException {
		try {
	    	System.out.println("unzipping "+zipFilePath);
	        File destDir = new File(destDirectory);
	        if (!destDir.exists()) {
	            destDir.mkdir();
	        }
	        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
	        ZipEntry entry = zipIn.getNextEntry();
	        // iterates over entries in the zip file
	        while (entry != null) {
	            String filePath = destDirectory + File.separator + entry.getName();
	            if (!entry.isDirectory()) {
	                // if the entry is a file, extracts it
	            	System.out.println("extracting "+filePath+"...");
	                FileUtil.extractFile(zipIn, filePath);
	            } else {
	                // if the entry is a directory, make the directory
	                File dir = new File(filePath);
	                dir.mkdir();
	            }
	            zipIn.closeEntry();
	            entry = zipIn.getNextEntry();
	        }
	        zipIn.close();
	    } catch (Exception ex) {
	    	System.out.println("unzip fail!! "+ex);
	    	ex.printStackTrace();
	    }
	}

	/**
	 * Size of the buffer to read/write data
	 */
	public static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip entry (file entry)
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
	    byte[] bytesIn = new byte[BUFFER_SIZE];
	    int read = 0;
	    while ((read = zipIn.read(bytesIn)) != -1) {
	        bos.write(bytesIn, 0, read);
	    }
	    bos.close();
	}

	//public static void writeDBF(String filename, String[] headers, String[][] data) {
	public static void writeDBF(String filename, DBField[] fields, String[][] data) {
	
		DBFWriter dbfwriter;
		try {
			dbfwriter = new DBFWriter(filename, fields);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		for( int i = 0; i < data.length; i++) {
			for( int j = 0; j < data[i].length; j++) {
				if( data[i][j].length() > 64) {
					data[i][j] = data[i][j].substring(0,64);
				}
			}
			Object[] oo = new Object[data[i].length];
			for( int j = 0; j < fields.length; j++) {
				if( fields[j].type == 'N') {
					if(  data[i][j] == null || data[i][j].equals("<null>")) {
						oo[j] = (double) 0;
					} else {
						try {
							oo[j] = Double.parseDouble(data[i][j]);
						} catch (Exception ex) {
							ex.printStackTrace();
							oo[j] = data[i][j];
						}
					}
				} else {
					oo[j] = data[i][j];
				}
				
			}
			try {
				//dbfwriter.addRecord(data[i]);
				dbfwriter.addRecord(oo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			dbfwriter.close();
			System.out.println("dbfwriter closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DataAndHeader readDBF(String dbfname) {
		DBFReader dbfreader;
		try {
			dbfreader = new DBFReader(dbfname);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		DataAndHeader dh = new DataAndHeader();
		
		dh.header = new String[dbfreader.getFieldCount()];
		dh.full_header = new DBField[dbfreader.getFieldCount()];
		dh.nameToIndex = new HashMap<String,Integer>();
		for( int i = 0; i < dh.header.length; i++) {
			try {
				dh.header[i] = dbfreader.getField(i).name;
				dh.full_header[i] = dbfreader.getField(i);
				dh.nameToIndex.put(dh.header[i],i);
				//System.out.println("i: "+dh.header[i]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Vector<String[]> vd = new Vector<String[]>();

	    while (dbfreader.hasNextRecord()) {
	    	try {
	    		Object[] oo = dbfreader.nextRecord(Charset.defaultCharset());
	    		String[] ss = new String[oo.length];
	    		for( int i = 0; i < oo.length; i++) {
	    			ss[i] = oo[i].toString();
	    		}
				vd.add(ss);
			} catch (Exception e) {
				System.out.println(" e on read next "+e);
				e.printStackTrace();
			}
	    }
	    dh.data = new String[vd.size()][];
	    for( int i = 0; i < dh.data.length; i++) {
	    	dh.data[i] = vd.get(i);
	    }
	    try {
			dbfreader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return dh;
	}
	
	public static void writeDBF(DataAndHeader dh, String filename) {
		DBField[] fields = dh.full_header;
		String[][] data = dh.data;
		DBFWriter dbfwriter;
		try {
			dbfwriter = new DBFWriter(filename, fields);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		for( int i = 0; i < data.length; i++) {
			//System.out.println("writing line "+i+" of "+data.length);
			for( int j = 0; j < data[i].length; j++) {
				if( data[i][j].length() > 64) {
					data[i][j] = data[i][j].substring(0,64);
				}
			}
			Object[] oo = new Object[data[i].length];
			for( int j = 0; j < fields.length; j++) {
				if( fields[j].type == 'N') {
					if(  data[i][j] == null || data[i][j].equals("<null>")) {
						oo[j] = (double) 0;
					} else {
						try {
							oo[j] = Double.parseDouble(data[i][j]);
						} catch (Exception ex) {
							ex.printStackTrace();
							oo[j] = data[i][j];
						}
					}
				} else {
					oo[j] = data[i][j];
				}
				
			}
			try {
				//dbfwriter.addRecord(data[i]);
				dbfwriter.addRecord(oo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			dbfwriter.close();
			System.out.println("dbfwriter closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
