/**
 * <p>Copyright: Copyright (c) 2004~2012~2012</p>
 * <p>Company: iihero.com</p>
 * @author : He Xiong
 * @version 1.1
 */

package com.hexiong.jdbf;

import java.text.*;
import java.util.Date;

/**
 * <p>Copyright: Copyright (c) 2004~2012~2012</p>
 * <p>Company: iihero.com</p>
 * @author : He Xiong
 * @version 1.1
 */
public class JDBField {
  /**
   */
  public JDBField(String s, char c, int i, int j) throws JDBFException {
    if (s.length() > 10) {
      throw new JDBFException(
          "The field name is more than 10 characters long: " + s);
    }
    if (c != 'C' && c != 'N' && c != 'L' && c != 'D' && c != 'F') {
      throw new JDBFException("The field type is not a valid. Got: " + c);
    }
    if (i < 1) {
      throw new JDBFException(
          "The field length should be a positive integer. Got: " + i);
    }
    /*
    if (c == 'C' && i >= 255) {
      throw new JDBFException("The field length should be less than 255 characters for character fields. Got: " +
                              i);
    }
    if (c == 'N' && i >= 21) {
      throw new JDBFException(
          "The field length should be less than 21 digits for numeric fields. Got: " +
          i);
    }
    if (c == 'L' && i != 1) {
      throw new JDBFException(
          "The field length should be 1 characater for logical fields. Got: " +
          i);
    }
    if (c == 'D' && i != 8) {
      throw new JDBFException(
          "The field length should be 8 characaters for date fields. Got: " + i);
    }
    if (c == 'F' && i >= 21) {
      throw new JDBFException("The field length should be less than 21 digits for floating point fields. Got: " +
                              i);
    }
    if (j < 0) {
      throw new JDBFException(
          "The field decimal count should not be a negative integer. Got: " + j);
    }
    if ( (c == 'C' || c == 'L' || c == 'D') && j != 0) {
      throw new JDBFException("The field decimal count should be 0 for character, logical, and date fields. Got: " +
                              j);
    }
    if (j > i - 1) {
      throw new JDBFException(
          "The field decimal count should be less than the length - 1. Got: " +
          j);
    }*/
    else {
      name = s;
      type = c;
      length = i;
      decimalCount = j;
      return;
    }
  }

  /**
   * èŽ·å�–å­—æ®µå��
   * @return å­—æ®µå��
   */
  public String getName() {
    return name;
  }

  /**
   * èŽ·å�–å­—æ®µç±»åž‹
   * @return å­—æ®µç±»åž‹
   */
  public char getType() {
    return type;
  }

  /**
   * èŽ·å�–å­—æ®µé•¿åº¦
   * @return å­—æ®µé•¿åº¦
   */
  public int getLength() {
    return length;
  }

  /**
   * èŽ·å�–å­—æ®µçš„å°�æ•°ä½�æ•°
   * @return å­—æ®µå°�æ•°ä½�æ•°
   */
  public int getDecimalCount() {
    return decimalCount;
  }

  /**
   * å°†å¯¹è±¡æ ¼å¼�åŒ–ä¸ºä¸€ä¸ªå­—ç¬¦ä¸²
   * @param obj æµ�ä¸­çš„å¯¹è±¡
   * @return ç”¨äºŽè¡¨ç¤ºå­—æ®µå€¼çš„å¯¹è±¡
   * @throws JDBFException å½“è¯»å�–æ—¶å�‘ç”Ÿé”™è¯¯æ—¶ï¼ŒæŠ›å‡ºå¼‚å¸¸
   */
  public String format(Object obj) throws JDBFException {
    if (type == 'N' || type == 'F') {
      if (obj == null) {
        obj = new Double(0.0D);
      }
      if (obj instanceof Number) {
        Number number = (Number) obj;
        StringBuffer stringbuffer = new StringBuffer(getLength());
        for (int i = 0; i < getLength(); i++) {
          stringbuffer.append("#");

        }
        if (getDecimalCount() > 0) {
          stringbuffer.setCharAt(getLength() - getDecimalCount() - 1, '.');
        }
        DecimalFormat decimalformat = new DecimalFormat(stringbuffer.toString());
        String s1 = decimalformat.format(number);
        int k = getLength() - s1.length();
        if (k < 0) {
          throw new JDBFException("Value " + number +
                                  " cannot fit in pattern: '" + stringbuffer +
                                  "'.");
        }
        StringBuffer stringbuffer2 = new StringBuffer(k);
        for (int l = 0; l < k; l++) {
          stringbuffer2.append(" ");

        }
        return stringbuffer2 + s1;
      }
      else {
        throw new JDBFException("Expected a Number, got " + obj.getClass() +
                                ".");
      }
    }
    if (type == 'C') {
      if (obj == null) {
        obj = "";
      }
      if (obj instanceof String) {
        String s = (String) obj;
        if (s.length() > getLength()) {
          throw new JDBFException("'" + obj + "' is longer than " + getLength() +
                                  " characters.");
        }
        StringBuffer stringbuffer1 = new StringBuffer(getLength() - s.length());
        for (int j = 0; j < getLength() - s.length(); j++) {
          stringbuffer1.append(' ');

        }
        return s + stringbuffer1;
      }
      else {
        throw new JDBFException("Expected a String, got " + obj.getClass() +
                                ".");
      }
    }
    if (type == 'L') {
      if (obj == null) {
        obj = new Boolean(false);
      }
      if (obj instanceof Boolean) {
        Boolean boolean1 = (Boolean) obj;
        return boolean1.booleanValue() ? "Y" : "N";
      }
      else {
        throw new JDBFException("Expected a Boolean, got " + obj.getClass() +
                                ".");
      }
    }
    if (type == 'D') {
      if (obj == null) {
        obj = new Date();
      }
      if (obj instanceof Date) {
        Date date = (Date) obj;
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
        return simpledateformat.format(date);
      }
      else {
        throw new JDBFException("Expected a Date, got " + obj.getClass() + ".");
      }
    }
    else {
      throw new JDBFException("Unrecognized JDBFField type: " + type);
    }
  }

  /**
   * å°†ä¸€ä¸ªå­—ç¬¦ä¸²è§£æž�ä¸ºå¯¹åº”çš„å­—æ®µå€¼ç±»åž‹å¯¹è±¡
   * @param s è¡¨ç¤ºå­—æ®µå€¼çš„å­—ç¬¦ä¸²
   * @return å¯¹åº”çš„å­—æ®µå€¼ç±»åž‹å¯¹è±¡
   * @throws JDBFException è§£æž�å‡ºé”™æ—¶æŠ›å‡º
   */
  public Object parse(String s) throws JDBFException {
    s = s.trim();
    if (type == 'N' || type == 'F') {
      if (s.equals("")) {
        s = "0";
      }
      try {
        if (getDecimalCount() == 0) {
          return new Long(s);
        }
        else {
          return new Double(s);
        }
      }
      catch (NumberFormatException numberformatexception) {
        throw new JDBFException(numberformatexception);
      }
    }
    if (type == 'C') {
      return s;
    }
    if (type == 'L') {
      if (s.equals("Y") || s.equals("y") || s.equals("T") || s.equals("t")) {
        return new Boolean(true);
      }
      if (s.equals("N") || s.equals("n") || s.equals("F") || s.equals("f")) {
        return new Boolean(false);
      }
      else {
        throw new JDBFException("Unrecognized value for logical field: " + s);
      }
    }
    if (type == 'D') {
      SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
      try {
        if ("".equals(s)) {
          return null;
        }
        else {
          return simpledateformat.parse(s);
        }
      }
      catch (ParseException parseexception) {
        throw new JDBFException(parseexception);
      }
    }
    else {
      throw new JDBFException("Unrecognized JDBFField type: " + type);
    }
  }

  /**
   * èŽ·å�–å­—æ®µå��
   * @return å­—æ®µå��
   */
  public String toString() {
    return name;
  }

  /**
   * å­—æ®µå��
   */
  private String name;
  /**
   * å­—æ®µç±»åž‹
   */
  private char type;
  /**
   * å­—æ®µé•¿åº¦
   */
  private int length;
  /**
   * å­—æ®µçš„å°�æ•°ä½�æ•°
   */
  private int decimalCount;
}
