package tests;

import junit.framework.TestCase;

import org.nocrala.tools.gis.data.esri.shapefile.util.HexaUtil;

public class HexaTests extends TestCase {

  public HexaTests(final String txt) {
    super(txt);
  }

  public void testOneByte() {

    byte[] serialized = HexaUtil.stringToByteArray("12");
    assertNotNull(serialized);
    assertEquals(1, serialized.length);
    assertEquals(18, serialized[0]);

    String back = HexaUtil.byteArrayToString(serialized);
    // System.out.println("back='" + back + "'");
    assertEquals("12", back);

  }

  public void testTwoBytes() {

    byte[] serialized = HexaUtil.stringToByteArray("cdef");
    assertNotNull(serialized);
    assertEquals(2, serialized.length);
    assertEquals(205 - 256, serialized[0]);
    assertEquals(239 - 256, serialized[1]);

    String back = HexaUtil.byteArrayToString(serialized);
    // System.out.println("back='" + back + "'");
    assertEquals("cdef", back);

  }

  public void testTwoBytesUppercase() {

    byte[] serialized = HexaUtil.stringToByteArray("CDEF");
    assertNotNull(serialized);
    assertEquals(2, serialized.length);
    assertEquals(205 - 256, serialized[0]);
    assertEquals(239 - 256, serialized[1]);

    String back = HexaUtil.byteArrayToString(serialized);
    // System.out.println("back='" + back + "'");
    assertEquals("cdef", back);

  }

}
