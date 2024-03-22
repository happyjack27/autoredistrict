package shapefile;

import org.nocrala.tools.gis.data.esri.shapefile.NocralaShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;

import java.io.FileInputStream;
import java.io.IOException;

public class NocralaShapeFileReaderFactory implements ShapeFileReaderFactory {
    public NocralaShapeFileReader createShapeFileReader(FileInputStream is) throws InvalidShapeFileException, IOException {
        ValidationPreferences prefs = new ValidationPreferences();
        prefs.setMaxNumberOfPointsPerShape(32650 * 4);
        prefs.setAllowUnlimitedNumberOfPointsPerShape(true);
        prefs.setAllowBadContentLength(true);
        prefs.setAllowBadRecordNumbers(true);

        return new NocralaShapeFileReader(is, prefs);
    }
}
