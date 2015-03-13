package example;

import java.io.FileInputStream;
import java.io.IOException;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;

public class SimpleExample {

  public static void main(String[] args) throws IOException,
      InvalidShapeFileException {

    FileInputStream is = new FileInputStream(
        "testdata/freefiles/polygonfiles/water.shp");
    ShapeFileReader r = new ShapeFileReader(is);

    ShapeFileHeader h = r.getHeader();
    System.out.println("The shape type of this files is " + h.getShapeType());

    int total = 0;
    AbstractShape s;
    while ((s = r.next()) != null) {

      switch (s.getShapeType()) {
      case POINT:
        PointShape aPoint = (PointShape) s;
        // Do something with the point shape...
        break;
      case MULTIPOINT_Z:
        MultiPointZShape aMultiPointZ = (MultiPointZShape) s;
        // Do something with the MultiPointZ shape...
        break;
      case POLYGON:
        PolygonShape aPolygon = (PolygonShape) s;
        System.out.println("I read a Polygon with "
            + aPolygon.getNumberOfParts() + " parts and "
            + aPolygon.getNumberOfPoints() + " points");
        for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
          PointData[] points = aPolygon.getPointsOfPart(i);
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
