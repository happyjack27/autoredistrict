package util;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.net.*;

public class FTPDownload {
	static int mDownloadFileLength = 0;
	static boolean mFlagDisableAsyncTask = false;

	static String test = "ftp://ftp2.census.gov/geo/tiger/TIGER2010BLKPOPHU/"
			+"tabblock2010_04_pophu.zip";
	public static void main(String[] args) {
		resume(test,"/Users/jimbrill/test_download.zip",0);
	}
	public static void download(String surl, String dest_file) {
		int last = 0;
		for( int i = 0; i < 32; i++) {
			try {
				int[] dd = resume(surl,dest_file,last);
				if( dd[0] == dd[1] && dd[1] > 0) {
					return;
				}
				last = dd[0];
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static int[] resume(String surl, String dest_file, int start_at) {
		Logd("url: "+surl);
		Logd("dest_file"+dest_file);
		Logd("start: "+start_at);
		int downloaded = 0;
		int fileLength = 0;
		String strLastModified ="";
		BufferedInputStream input = null;
		RandomAccessFile outFile = null;
		Map<String, List<String>> map;
		Logd("1");

		
	    try {
	
			
		    // Setup connection.
		    URL url = new URL(surl);
		    URLConnection connection = url.openConnection();
		    downloaded = start_at;
			Logd("2 "+downloaded);
            if (downloaded == 0) {
        		Logd("3a0");
        		connection.connect();
        		Logd("3a1");
                strLastModified = connection.getHeaderField("Last-Modified");
        		Logd("3a2");
                fileLength = connection.getContentLength();
        		Logd("3a3");
                mDownloadFileLength = fileLength;
        		Logd("3a4");

            }
            else {
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
                connection.setRequestProperty("If-Range", strLastModified);
                connection.connect();
                fileLength = mDownloadFileLength;
                Logd("AsyncDownloadFile", 
                        "new download seek: " + downloaded +
                        "; lengthFile: " + fileLength);
        		Logd("3b");

		    }
            
			Logd("3done");
		    map = connection.getHeaderFields();
			Logd("4");

		    Logd("AsyncDownloadFile", "header fields: " + map.toString());
	
		    // Setup streams and buffers.
		    input = new BufferedInputStream(connection.getInputStream(), 8192);
		    //input = new BufferedInputStream(url.openStream(), 8192);
		    outFile = new RandomAccessFile(dest_file, "rw");
		    if (downloaded > 0)  
		        outFile.seek(downloaded);
		    byte data[] = new byte[1024];
		
		    // Download file.
		    for (int count=0, i=0; (count=input.read(data, 0, 1024)) != -1; i++) { 
		    	try {
			        outFile.write(data, 0, count);
			        downloaded += count; 
			        if (downloaded >= fileLength)
			            break;
			
			        // Display progress.
			        Logd("AsyncDownloadFile", "bytes: " + downloaded);
			        if ((i%10) == 0) 
			            publishProgress((int)(downloaded*100/fileLength));
			        if (mFlagDisableAsyncTask) {
			            downloaded = 0;
			            break;
			        }
				} catch (Exception e) {
					System.out.println("eb "+e);

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("ea "+e);
			e.printStackTrace();
		}
	
	    // Close streams.
	    try {
			outFile.close();
			input.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("e "+e);
			e.printStackTrace();
		}
	    System.out.println("done "+downloaded);
	    return new int[]{downloaded,mDownloadFileLength};
	}
	
	public static void Logd(String s, String s2) {
		System.out.println(new Date()+": "+s+s2);
	}
	public static void Logd(String s) {
		System.out.println(new Date()+": "+s);
	}
	public static void publishProgress(int i) {
		System.out.println("progress: "+i);
	}

	
	/*
	public boolean downloadFile(String remoteFilePath, String localFilePath) {
			try {
			  File localFile = new File(localFilePath);
			  if (localFile.exists()) {
			    //If file exist set append=true, set ofset localFile size and resume
				FileOutputStream fos = new FileOutputStream(localFile, true);
			    ftp.setRestartOffset(localFile.length());
			    ftp.retrieveFile(remoteFilePath, fos);
			  } else {
			    //Create file with directories if necessary(safer) and start download
			    localFile.getParentFile().mkdirs();
			    localFile.createNewFile();
			    FileOutputStream fos = new FileOutputStream(localFile);
			    ftp.retrieveFile(remoteFilePath, fos);
			  }
			} catch (Exception ex) {
			    System.out.println("Could not download file " + ex);
			    ex.printStackTrace();
			    return false;
			  }
	}
	*/


}
