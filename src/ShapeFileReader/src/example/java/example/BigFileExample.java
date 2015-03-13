package example;

import java.io.FileInputStream;
import java.io.IOException;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;

public class BigFileExample {

  public static void main(String[] args) throws IOException,
      InvalidShapeFileException {

    FileInputStream is = new FileInputStream(
        "testdata/freeworld/10m-coastline/10m_coastline.shp");

    // This file has shapes with more than 10000 points each. Therefore, we need
    // to change the validation preferences to increase the limit of points per
    // shape beyond that number. If we don't use the customized preferences, the
    // reader will throw an InvalidShapeFileException.
    ValidationPreferences prefs = new ValidationPreferences();
    prefs.setMaxNumberOfPointsPerShape(16650);
    ShapeFileReader r = new ShapeFileReader(is, prefs);

    ShapeFileHeader h = r.getHeader();
    System.out.println("The shape type of this files is " + h.getShapeType());

    int total = 0;
    AbstractShape s;
    while ((s = r.next()) != null) {

      switch (s.getShapeType()) {
      case POLYLINE:
        PolylineShape aPolyline = (PolylineShape) s;
        System.out.println("I read a Polyline with "
            + aPolyline.getNumberOfParts() + " parts and "
            + aPolyline.getNumberOfPoints() + " points");
        for (int i = 0; i < aPolyline.getNumberOfParts(); i++) {
          PointData[] points = aPolyline.getPointsOfPart(i);
          System.out.println("- part " + i + " has " + points.length
              + " points.");
        }
        break;
      default:
        System.out.println("Read other type of shape.");
      }
      total++;
    }

    System.out.println("Total shapes read: " + total);

    is.close();

  }

}
