package shapefile;

import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;

import java.io.FileInputStream;
import java.io.IOException;

public interface ShapeFileReaderFactory {
    ShapeFileReader createShapeFileReader(FileInputStream is) throws InvalidShapeFileException, IOException;
}
