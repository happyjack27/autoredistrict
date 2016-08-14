package dbf;

import java.io.*;
import java.nio.charset.Charset;

public class DBFReader {

  DataInputStream stream;
  DBField fields[];
  byte nextRecord[];
  int nFieldCount;

  public DBFReader(String s) throws Exception {
	this(new FileInputStream(s));
  }
  public DBFReader(InputStream inputstream) throws Exception {
    try {
      stream = new DataInputStream(inputstream);
      int i = readHeader();
      fields = new DBField[i];
	  nFieldCount = 0;
      int j = 1;
      for (int k = 0; k < i; k++) {
        fields[k] = readFieldHeader();
        if (fields[k] != null) {
          nFieldCount++;
          j += fields[k].length;
        }
      }
	  
      nextRecord = new byte[j];
      try {
        stream.readFully(nextRecord);
      }
      catch (EOFException eofexception) {
        nextRecord = null;
        stream.close();
      }
      int pos = 0;
      boolean hasBegin = false;
      for (int p = 0; p < j; p++) {
        if (nextRecord[p] == 0X20 || nextRecord[p] == 0X2A) {
          hasBegin = true;
          pos = p;
          break;
        }
      }
      if (pos > 0) {
        byte[] others = new byte[pos];
        stream.readFully(others);

        for (int p = 0; p < j - pos; p++) {
          nextRecord[p] = nextRecord[p + pos];
        }
        for (int p = 0; p < pos; p++) {
          nextRecord[j - p - 1] = others[pos - p - 1];
        }
      }

    }
    catch (IOException ioexception) {
      //throw new Exception(ioexception);
    }
  }

  private int readHeader() throws IOException, Exception {
    byte abyte0[] = new byte[16];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception) {
      throw new Exception("Unexpected end of file reached.");
    }
    int i = abyte0[8];
    if (i < 0)
      i += 256;
    i += 256 * abyte0[9];
    i = --i / 32;
    i--;
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception1) {
      throw new Exception("Unexpected end of file reached.");
    }
    return i;
  }

  private DBField readFieldHeader() throws IOException, Exception {
    byte abyte0[] = new byte[16];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception) {
      throw new Exception("Unexpected end of file reached.");
    }
    //
    if (abyte0[0] == 0X0D || abyte0[0] == 0X00) {
      stream.readFully(abyte0);
      return null;
    }

    StringBuffer sb = new StringBuffer(10);
    int i = 0;
    for (i = 0; i < 10; i++) {
      if (abyte0[i] == 0)
        break;
      //sb.append( (char) abyte0[i]);
    }
    sb.append(new String(abyte0, 0, i));

    char c = (char) abyte0[11];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception1) {
      throw new Exception("Unexpected end of file reached.");
    }

    int j = abyte0[0];
    int k = abyte0[1];
    if (j < 0)
      j += 256;
    if (k < 0)
      k += 256;
    return new DBField(sb.toString(), c, j, k);
  }

  public int getFieldCount() {
    return nFieldCount; //fields.length;
  }
  public DBField getField(int i) {
    return fields[i];
  }

  public boolean hasNextRecord() {
	  if( nextRecord == null) {
		  System.out.println("read last record");
	  }
    return nextRecord != null;
  }
  public Object[] nextRecord() throws Exception {
    if (!hasNextRecord())
      throw new Exception("No more records available.");
    Object aobj[] = new Object[nFieldCount];
    int i = 1;
    for (int j = 0; j < aobj.length; j++) {
      int k = fields[j].length;
      StringBuffer sb = new StringBuffer(k);
      sb.append(new String(nextRecord, i, k));
      aobj[j] = fields[j].parse(sb.toString());
      i += fields[j].length;
    }

    try {
      stream.readFully(nextRecord);
    }
    catch (EOFException eofexception) {
    	System.out.println("eofexception");
      nextRecord = null;
    }
    catch (IOException ioexception) {
    	System.out.println("ioexception");
      throw new Exception(ioexception);
    }
    return aobj;
  }
  
  public Object[] nextRecord(Charset charset) throws Exception {
    if (!hasNextRecord()) {
    	System.out.println("read last record");
      throw new Exception("No more records available.");
    }
    Object aobj[] = new Object[nFieldCount];
    int i = 1;
    for (int j = 0; j < aobj.length; j++) {
      int k = fields[j].length;
      StringBuffer sb = new StringBuffer(k);
      sb.append(new String(nextRecord, i, k, charset));
      aobj[j] = fields[j].parse(sb.toString());
      i += fields[j].length;
    }

    try {
      stream.readFully(nextRecord);
    }
    catch (EOFException eofexception) {
    	System.out.println("eofexception");
      nextRecord = null;
    }
    catch (IOException ioexception) {
    	System.out.println("ioexception");
      throw new Exception(ioexception);
    }
    return aobj;
  }

  public void close() throws Exception {
    nextRecord = null;
    try {
      stream.close();
    }
    catch (IOException ioexception) {
      throw new Exception(ioexception);
    }
  }
}