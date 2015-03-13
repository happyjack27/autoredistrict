package tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

public class ReaderTests extends TestCase {

  public ReaderTests(final String txt) {
    super(txt);
  }

  public void testGoodFiles() throws IOException, InvalidShapeFileException {

    checkSimpleCase("testdata/freefiles/pointfiles/prop_text.shp",
        ShapeType.POINT, 1897);
    checkSimpleCase("testdata/freefiles/pointfiles/roadtext.shp",
        ShapeType.POINT, 184);
    checkSimpleCase("testdata/freefiles/pointfiles/sbuild.shp", ShapeType.POINT,
        1592);

    checkSimpleCase("testdata/freefiles/polygonfiles/lbuild.shp",
        ShapeType.POLYGON, 27);
    checkSimpleCase("testdata/freefiles/polygonfiles/property.shp",
        ShapeType.POLYGON, 1650);
    checkSimpleCase("testdata/freefiles/polygonfiles/subdiv.shp",
        ShapeType.POLYGON, 29);
    checkSimpleCase("testdata/freefiles/polygonfiles/water.shp",
        ShapeType.POLYGON, 3);

    checkSimpleCase("testdata/freefiles/polylinefiles/roadcl.shp",
        ShapeType.POLYLINE, 231);
    checkSimpleCase("testdata/freefiles/polylinefiles/roadeop.shp",
        ShapeType.POLYLINE, 458);

    checkSimpleCase("testdata/freefiles/multipoint/admin_font_point.shp",
        ShapeType.MULTIPOINT, 2175);

  }

  public void testBigFile() throws FileNotFoundException, IOException,
      InvalidShapeFileException {

    try {
      readFile("testdata/freeworld/10m-coastline/10m_coastline.shp", null,
          ShapeType.POLYLINE, 4177);
      fail();
    } catch (InvalidShapeFileException e) {
      // OK. The file exceeds 10000 shapes.
    }

    ValidationPreferences prefs = new ValidationPreferences();
    prefs.setMaxNumberOfPointsPerShape(16650);
    readFile("testdata/freeworld/10m-coastline/10m_coastline.shp", prefs,
        ShapeType.POLYLINE, 4177);

  }

  public void testRecoverableBadFile() throws FileNotFoundException,
      IOException, InvalidShapeFileException {

    try {
      readFile(
          "testdata/freefiles/badfiles/multipointm-marked-as-multipointz.shp",
          null, ShapeType.MULTIPOINT_Z, 300);
      fail();
    } catch (InvalidShapeFileException e) {
      // OK
    }

    ValidationPreferences prefs = new ValidationPreferences();
    prefs.setForceShapeType(ShapeType.MULTIPOINT_M);
    readFile(
        "testdata/freefiles/badfiles/multipointm-marked-as-multipointz.shp",
        prefs, ShapeType.MULTIPOINT_M, 312);

  }

  // Utils

  private void checkSimpleCase(final String filename,
      final ShapeType expectedShapeType, final int expectedNumberOfShapes)
      throws IOException, InvalidShapeFileException {
    readFile(filename, null, expectedShapeType, expectedNumberOfShapes);
  }

  private void readFile(final String filename,
      final ValidationPreferences prefs, final ShapeType expectedShapeType,
      final int expectedNumberOfShapes) throws FileNotFoundException,
      IOException, InvalidShapeFileException {
    FileInputStream is = null;
    try {

      is = new FileInputStream(filename);
      ShapeFileReader r;
      if (prefs == null) {
        r = new ShapeFileReader(is);
      } else {
        r = new ShapeFileReader(is, prefs);
      }

      ShapeFileHeader header = r.getHeader();
      assertEquals(expectedShapeType, header.getShapeType());

      int actualNumberOfShapes = 0;
      AbstractShape s;
      while ((s = r.next()) != null) {
        actualNumberOfShapes++;
      }
      assertEquals(expectedNumberOfShapes, actualNumberOfShapes);

    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

}
