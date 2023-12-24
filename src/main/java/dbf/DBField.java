package dbf;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBField {
	public String name;
	public char type;
	public int length;
	public int decimalCount;
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public DBField clone() {
		try {
			return new DBField(name, type, length, decimalCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DBField(String s, char c, int i, int j) throws Exception {
		if (s.length() > 10) {
			throw new Exception("The field name is more than 10 characters long: " + s);
		}
		if (c != 'C' && c != 'N' && c != 'L' && c != 'D' && c != 'F') {
			throw new Exception("The field type is not a valid. Got: " + c);
		}
		if (i < 1) {
			throw new Exception("The field length should be a positive integer. Got: " + i);
		}
		else {
			name = s;
			type = c;
			length = i;
			decimalCount = j;
		}
	}

	public String format(Object obj) throws Exception {
		switch( type) {
			case 'N':
			case 'F':
				if (obj == null) {
					obj = 0.0D;
				}
				if( obj instanceof String) {
					obj = Double.parseDouble(obj.toString());
				} else if (obj instanceof Number) {
				} else {
					obj = (double) 0;
				}
				if (obj instanceof Number number) {
					StringBuffer sb = new StringBuffer(length);
					for (int i = 0; i < length; i++) {
						sb.append("#");

					}
					if (decimalCount > 0) {
						sb.setCharAt(length - decimalCount - 1, '.');
					}
					DecimalFormat decimalformat = new DecimalFormat(sb.toString());
					String s1 = decimalformat.format(number);
					int k = length - s1.length();
					if (k < 0) {
						throw new Exception("Value " + number + " cannot fit in pattern: '" + sb +  "'. " +name);
					}
					StringBuffer sb2 = new StringBuffer(k);
					for (int l = 0; l < k; l++) {
						sb2.append(" ");
					}
					return sb2 + s1;
				}
				else {
					throw new Exception("Expected a Number, got " + obj.getClass() + ".");
				}
		case 'C':
				if (obj == null || obj.equals("<null>") || obj.equals("null")) {
					obj = "";
				}
				if (obj instanceof String s) {
					if (s.length() > length) {
					  throw new Exception("'" + obj + "' is longer than " + length + " characters. "+name);
					}
					StringBuffer sb1 = new StringBuffer(length - s.length());
					for (int j = 0; j < length - s.length(); j++) {
					  sb1.append(' ');
					}
					return s + sb1;
				}
				else {
					throw new Exception("Expected a String, got " + obj.getClass() + ". " +name);
				}
		case 'L':
				if (obj == null) {
					obj = Boolean.FALSE;
				}
				if (obj instanceof Boolean boolean1) {
					return boolean1.booleanValue() ? "Y" : "N";
				}
				else {
					throw new Exception("Expected a Boolean, got " + obj.getClass() + ". " +name);
				}
		case 'D':
				if (obj == null) {
					obj = new Date();
				}
				if (obj instanceof Date date) {
					return sdf.format(date);
				}
				else {
					throw new Exception("Expected a Date, got " + obj.getClass() + ". " +name);
				}
		default:
				throw new Exception("Unrecognized DBFField type: " + type);
		}
	}
	
	public Object parse(String s) throws Exception {
		s = s.trim();
		switch(type) {
			case 'N':
			case 'F':
				if (s.length() == 0) {
					s = "0";
				}
				try {
					if (decimalCount == 0) {
					  return Long.valueOf(s);
					}
					else {
					  return Double.parseDouble(s);
					}
				} catch (NumberFormatException numberformatexception) {
					return 0; //throw numberformatexception;
				}
			case 'C':
				return s;
			case 'L':
				s = s.toUpperCase();
				return Boolean.valueOf(s.equals("Y") || s.equals("T"));
			case 'D':
				try {
					if (s.length() == 0) {
						return null;
					}
					else {
						return sdf.parse(s);
					}
				} catch (ParseException parseexception) {
					throw parseexception;
				}
			default:
				throw new Exception("Unrecognized DBFField type: " + type);
		}
	}
}
