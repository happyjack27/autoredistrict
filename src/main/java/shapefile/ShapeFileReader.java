package shapefile;

import dbf.DBFReader;
import geography.FeatureCollection;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;

import java.io.IOException;

public interface ShapeFileReader {

    String getHeaderShapeType();

    int processShapeFile(String[] cols, DBFReader dbfreader, FeatureCollection featureCollection) throws InvalidShapeFileException, IOException;
}
