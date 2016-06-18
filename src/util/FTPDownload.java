package util;

import java.text.DecimalFormat;

/*
 * 
ownloading census block centroids...
Thu Mar 31 16:56:29 CDT 2016: url: ftp://ftp2.census.gov/geo/pvs/tiger2010st/25_Massachusetts/25/tl_2010_25_tabblock10.zip
Thu Mar 31 16:56:29 CDT 2016: dest_file: /Users/jimbrill/autoredistrict_data/Massachusetts/2010/block_centroids/block_centroids.zip
Thu Mar 31 16:56:29 CDT 2016: start: 0
Thu Mar 31 16:56:29 CDT 2016: 1
Thu Mar 31 16:56:29 CDT 2016: 2 0
Thu Mar 31 16:56:29 CDT 2016: 3a0
failed retrying
java.net.SocketException: Connection reset
	at java.net.SocketInputStream.read(SocketInputStream.java:209)
	at java.net.SocketInputStream.read(SocketInputStream.java:141)
	at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
	at java.io.BufferedInputStream.read(BufferedInputStream.java:265)
	at sun.net.ftp.impl.FtpClient.readServerResponse(FtpClient.java:420)
	at sun.net.ftp.impl.FtpClient.readReply(FtpClient.java:497)
	at sun.net.ftp.impl.FtpClient.connect(FtpClient.java:1013)
	at sun.net.ftp.impl.FtpClient.connect(FtpClient.java:998)
	at sun.net.www.protocol.ftp.FtpURLConnection.connect(FtpURLConnection.java:294)
	at util.FTPDownload.resume(FTPDownload.java:59)
	at util.FTPDownload.download(FTPDownload.java:23)
	at ui.Download.download(Download.java:531)
	at ui.Download.run(Download.java:268)
arg: clean
arg: 25
deleting /Users/jimbrill/autoredistrict_data/Massachusetts/2010/block_centroids

<BR/>
 */
import java.util.*;
import java.io.*;
import java.nio.*;
import java.net.*;

public class FTPDownload {
	private static final int TIMEOUT_VALUE = 10000; //10 seconds
	static long scale = 1;
	static long mDownloadFileLength = 0;
	static boolean mFlagDisableAsyncTask = false;

	static String test = "ftp://ftp2.census.gov/geo/tiger/TIGER2010BLKPOPHU/"
			+"tabblock2010_48_pophu.zip";
	public static void main(String[] args) {
		resume(test,"C:\\Users\\kbaas.000\\Documents\\autoredistrict_data\\test2.zip",0);
	}
	public static boolean download(String surl, String dest_file) {
		long last = 0;
		for( int i = 0; i < 64; i++) {
			try {
				long[] dd = resume(surl,dest_file,last);
				if( dd[0] == dd[1] && dd[1] > 0) {
					System.out.println("ftp done downloading");
					return true;
				}
				last = dd[0];
				Logd("Connection interrupted, resuming... "+i+" / 64");
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("ftp failed downloading");
		return false;
	}
	public static long[] resume(String surl, String dest_file, long start_at) {
		Logd("url: "+surl);
		Logd("dest_file: "+dest_file);
		Logd("start: "+start_at);
		long downloaded = 0;
		long fileLength = 0;
		String strLastModified ="";
		BufferedInputStream input = null;
		RandomAccessFile outFile = null;
		Map<String, List<String>> map;
		Logd("1");

		
	    try {
	
			
		    // Setup connection.
		    URL url = new URL(surl);
		    URLConnection connection = url.openConnection();
		    connection.setConnectTimeout(TIMEOUT_VALUE);
		    connection.setReadTimeout(TIMEOUT_VALUE);
		    downloaded = start_at;
			Logd("2 "+downloaded);
            if (downloaded == 0) {
        		Logd("3a0");
        		try {
        			connection.connect();
        		} catch (Exception ex) {
        			System.out.println("failed retrying");
        			ex.printStackTrace();
        			return new long[]{downloaded,mDownloadFileLength};
        			/*
            		try {
            			connection.connect();
            		} catch (Exception ex2) {
            			System.out.println("failed twice");
            			ex2.printStackTrace();
            		}
            		*/
        			
        		}
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
        		try {
        			connection.connect();
        		} catch (Exception ex) {
        			System.out.println("failed retrying");
        			ex.printStackTrace();
            		try {
            			connection.connect();
            		} catch (Exception ex2) {
            			System.out.println("failed twice");
            			ex2.printStackTrace();
            		}
        			
        		}
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
		    input = new BufferedInputStream(connection.getInputStream(), (int) (8192*scale));
		    //input = new BufferedInputStream(url.openStream(), 8192);
		    outFile = new RandomAccessFile(dest_file, "rw");
		    if (downloaded > 0)  
		        outFile.seek(downloaded);
		    byte data[] = new byte[(int) (1024*scale)];
		
		    // Download file.
		    for (int count=0, i=0; (count=input.read(data, 0, (int)(1024*scale))) != -1; i++) {
		    	try {
			        outFile.write(data, 0, count);
			        downloaded += count; 
			        if (downloaded >= fileLength)
			            break;
			
			        // Display progress.
			        if ((i%1000) == 0)  {
			        	String mb = new DecimalFormat("###,###,##0.00").format(downloaded / 1024 / 1024);
				        Logd("AsyncDownloadFile", "bytes: " + mb+"MB  segment: "+(downloaded/(8192*scale))+" / "+(mDownloadFileLength/(8192*scale))+" "+(int)(downloaded*100/fileLength)+"%");
			            //publishProgress((int)(downloaded*100/fileLength));
			        }
			        if (mFlagDisableAsyncTask) {
			            downloaded = 0;
			            break;
			        }
				} catch (Exception e) {
					System.out.println("eb "+e);

					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
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
	    return new long[]{downloaded,mDownloadFileLength};
	}
	
	public static void Logd(String s, String s2) {
		System.out.println(new Date()+": "+s+": "+s2);
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