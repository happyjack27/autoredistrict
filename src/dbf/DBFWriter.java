package dbf;

import java.io.*;
import java.util.Calendar;

public class DBFWriter {

  BufferedOutputStream stream;
  int recCount;
  DBField fields[];
  String fileName;
  String encoding;

  public DBFWriter(String s, DBField fields[]) throws Exception {
	fileName = s;
	init(new FileOutputStream(s), fields);
  }
 
  public DBFWriter(OutputStream outputstream, DBField fields[]) throws Exception {
    init(outputstream, fields);
  }

  public DBFWriter(String s, DBField fields[], String s1) throws Exception {
	fileName = s;
	encoding = s1;
	init(new FileOutputStream(s), fields);
  }

  private void init(OutputStream outputstream, DBField fields[]) throws Exception {
	recCount = 0;
	this.fields = fields;
	stream = new BufferedOutputStream(outputstream);
	writeHeader();
	for (int i = 0; i < fields.length; i++) {
		writeFieldHeader(fields[i]);
	}
	stream.write(13);
	stream.flush();
  }

  private void writeHeader() throws Exception {
    byte b[] = new byte[16];
    b[0] = 3;
    Calendar calendar = Calendar.getInstance();
    b[1] = (byte) (calendar.get(1) - 1900);
    b[2] = (byte) calendar.get(2);
    b[3] = (byte) calendar.get(5);
    b[4] = 0;
    b[5] = 0;
    b[6] = 0;
    b[7] = 0;
    int i = (fields.length + 1) * 32 + 1;
    b[8] = (byte) (i % 256);
    b[9] = (byte) (i / 256);
    int j = 1;
    for (int k = 0; k < fields.length; k++) {
      j += fields[k].length;
    }
    b[10] = (byte) (j % 256);
    b[11] = (byte) (j / 256);
    b[12] = 0;
    b[13] = 0;
    b[14] = 0;
    b[15] = 0;
    stream.write(b, 0, b.length);
    for (int l = 0; l < 16; l++) {
      b[l] = 0;
    }
    stream.write(b, 0, b.length);
  }

  private void writeFieldHeader(DBField jdbfield) throws Exception {
    byte b[] = new byte[16];
    String s = jdbfield.name;
    int i = s.length();
    if (i > 10) {
      i = 10;
    }
    for (int j = 0; j < i; j++) {
      b[j] = (byte) s.charAt(j);
    }
    for (int k = i; k <= 10; k++) {
      b[k] = 0;
    }
    b[11] = (byte) jdbfield.type;
    b[12] = 0;
    b[13] = 0;
    b[14] = 0;
    b[15] = 0;
    stream.write(b, 0, b.length);
    for (int l = 0; l < 16; l++) {
      b[l] = 0;
    }
    b[0] = (byte) jdbfield.length;
    b[1] = (byte) jdbfield.decimalCount;
    stream.write(b, 0, b.length);
  }


  public void addRecord(Object aobj[]) throws Exception {
    if (aobj.length != fields.length) {
      throw new Exception("Error adding record: Wrong number of values. Expected " + fields.length + ", got " + aobj.length + ".");
    }
    int i = 0;
    for (int j = 0; j < fields.length; j++) {
      i += fields[j].length;
    }
    byte b[] = new byte[i];
    int k = 0;
    for (int l = 0; l < fields.length; l++) {
		String s = fields[l].format(aobj[l]);
		byte abyte1[];
        if (encoding != null) {
          abyte1 = s.getBytes(encoding);
        }
        else {
          abyte1 = s.getBytes();
        }
      for (int i1 = 0; i1 < fields[l].length; i1++) {
        b[k + i1] = abyte1[i1];
      }
      k += fields[l].length;
    }

	stream.write(32);
	stream.write(b, 0, b.length);
	stream.flush();
	
    recCount++;
  }

  public void close() throws Exception {
	stream.write(26);
	stream.close();
	RandomAccessFile randomaccessfile = new RandomAccessFile(fileName, "rw");
	randomaccessfile.seek(4L);
	byte b[] = new byte[4];
	b[0] = (byte) (recCount % 256);
	b[1] = (byte) ( (recCount / 256) % 256);
	b[2] = (byte) ( (recCount / 0x10000) % 256);
	b[3] = (byte) ( (recCount / 0x1000000) % 256);
	randomaccessfile.write(b, 0, b.length);
	randomaccessfile.close();
  }
}