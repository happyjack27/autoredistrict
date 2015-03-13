/**
 * <p>Title: java访问DBF文件的接口</p>
 * <p>Description: 这个类用于表示DBF文件中的读操作</p>
 * <p>Copyright: Copyright (c) 2004~2012~2012</p>
 * <p>Company: iihero.com</p>
 * @author : He Xiong
 * @version 1.1
 */

package com.hexiong.jdbf;

import java.io.*;
import java.nio.charset.Charset;

public class DBFReader {
  /**
   * 构造函数
   * @param s dbf文件的文件名
   * @throws JDBFException 文件没有找到时会抛出异常
   */
  public DBFReader(String s) throws JDBFException {
    stream = null;
    fields = null;
    nextRecord = null;
    nFieldCount = 0;
    try {
      init(new FileInputStream(s));
    }
    catch (FileNotFoundException filenotfoundexception) {
      throw new JDBFException(filenotfoundexception);
    }
  }
  /**
   * 使用inputstream来构造DBFReader
   * @param inputstream 输入流
   * @throws JDBFException
   */
  public DBFReader(InputStream inputstream) throws JDBFException {
    stream = null;
    fields = null;
    nextRecord = null;
    init(inputstream);
  }

  /**
   * 初始化读操作
   * @param inputstream 输入流，可以是文件输入流，也可以是别的输入流
   * @throws JDBFException 当发生文件IO异常时会抛出
   */
  private void init(InputStream inputstream) throws JDBFException {
    try {
      stream = new DataInputStream(inputstream);
      int i = readHeader();
      fields = new JDBField[i];
      int j = 1;
      for (int k = 0; k < i; k++) {
        fields[k] = readFieldHeader();
        if (fields[k] != null) {
          nFieldCount++;
          j += fields[k].getLength();
        }
      }

      /*
      if(stream.read() < 1)
          throw new JDBFException("Unexpected end of file reached.");
      */
      nextRecord = new byte[j];
      try {
        stream.readFully(nextRecord);
      }
      catch (EOFException eofexception) {
        nextRecord = null;
        stream.close();
      }
      //判断0x20或0x2a是否位于nextRecord当中
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

        //将nextRecord中的字节挪动pos个位置
        for (int p = 0; p < j - pos; p++) {
          nextRecord[p] = nextRecord[p + pos];
        }
        for (int p = 0; p < pos; p++) {
          nextRecord[j - p - 1] = others[pos - p - 1];
        }
      }

    }
    catch (IOException ioexception) {
      throw new JDBFException(ioexception);
    }
  }

  /**
   * 读取dbf文件的文件头
   * @return dbf文件中一个表的最大字段数，但不一定是有效个数
   * @throws IOException
   * @throws JDBFException
   */
  private int readHeader() throws IOException, JDBFException {
    byte abyte0[] = new byte[16];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception) {
      throw new JDBFException("Unexpected end of file reached.");
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
      throw new JDBFException("Unexpected end of file reached.");
    }
    return i;
  }

  /**
   * 读取一个字段
   * @return 一个字段的描述 JDBField,如果字段描述以0X0D或0X00开头，那么就返回一
   * 个null值
   * @see JDBField
   * @throws IOException
   * @throws JDBFException
   */
  private JDBField readFieldHeader() throws IOException, JDBFException {
    byte abyte0[] = new byte[16];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception) {
      throw new JDBFException("Unexpected end of file reached.");
    }
    //如果字段定义以'0D'开头，则是无效字段，返回一个空的JDBField
    //
    if (abyte0[0] == 0X0D || abyte0[0] == 0X00) {
      stream.readFully(abyte0);
      return null;
    }

    //获取字段名
    StringBuffer stringbuffer = new StringBuffer(10);
    int i = 0;
    for (i = 0; i < 10; i++) {
      if (abyte0[i] == 0)
        break;
      //stringbuffer.append( (char) abyte0[i]);
    }
    stringbuffer.append(new String(abyte0, 0, i));

    char c = (char) abyte0[11];
    try {
      stream.readFully(abyte0);
    }
    catch (EOFException eofexception1) {
      throw new JDBFException("Unexpected end of file reached.");
    }

    int j = abyte0[0];
    int k = abyte0[1];
    if (j < 0)
      j += 256;
    if (k < 0)
      k += 256;
    return new JDBField(stringbuffer.toString(), c, j, k);
  }

  /**
   * 获取有效字段个数
   * @return 表中字段的个数
   */
  public int getFieldCount() {
    return nFieldCount; //fields.length;
  }
  /**
   * 获取第i个字段，i从0开始记
   * @param i 字段序号
   * @return JDBField 第i个字段
   * @see JDBField
   */
  public JDBField getField(int i) {
    return fields[i];
  }

  /**
   * 是否还有下一条记录
   * @return 如果nextRecord不空，则返回真
   */
  public boolean hasNextRecord() {
    return nextRecord != null;
  }
  /**
   * 读取dbf文件中的下一条记录
   * @return 一个对象数组
   * @throws JDBFException
   */
  public Object[] nextRecord() throws JDBFException {
    if (!hasNextRecord())
      throw new JDBFException("No more records available.");
    //Object aobj[] = new Object[fields.length];
    Object aobj[] = new Object[nFieldCount];
    int i = 1;
    for (int j = 0; j < aobj.length; j++) {
      int k = fields[j].getLength();
      StringBuffer stringbuffer = new StringBuffer(k);
      stringbuffer.append(new String(nextRecord, i, k));
      aobj[j] = fields[j].parse(stringbuffer.toString());
      i += fields[j].getLength();
    }

    try {
      stream.readFully(nextRecord);
    }
    catch (EOFException eofexception) {
      nextRecord = null;
    }
    catch (IOException ioexception) {
      throw new JDBFException(ioexception);
    }
    return aobj;
  }
  
  /**
   * 读取dbf文件中的下一条记录, 指定字符集
   * @return 一个对象数组
   * @throws JDBFException
   */
  public Object[] nextRecord(Charset charset) throws JDBFException {
    if (!hasNextRecord())
      throw new JDBFException("No more records available.");
    //Object aobj[] = new Object[fields.length];
    Object aobj[] = new Object[nFieldCount];
    int i = 1;
    for (int j = 0; j < aobj.length; j++) {
      int k = fields[j].getLength();
      StringBuffer stringbuffer = new StringBuffer(k);
      stringbuffer.append(new String(nextRecord, i, k, charset));
      aobj[j] = fields[j].parse(stringbuffer.toString());
      i += fields[j].getLength();
    }

    try {
      stream.readFully(nextRecord);
    }
    catch (EOFException eofexception) {
      nextRecord = null;
    }
    catch (IOException ioexception) {
      throw new JDBFException(ioexception);
    }
    return aobj;
  }

  /**
   * 关闭整个文件
   * @throws JDBFException
   */
  public void close() throws JDBFException {
    nextRecord = null;
    try {
      stream.close();
    }
    catch (IOException ioexception) {
      throw new JDBFException(ioexception);
    }
  }

  private DataInputStream stream;
  private JDBField fields[];
  private byte nextRecord[];
  /**
   * 有效的字段个数
   */
  private int nFieldCount;
}