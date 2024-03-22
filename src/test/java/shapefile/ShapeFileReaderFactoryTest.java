package shapefile;

import static com.google.common.truth.Truth.assertThat;

import dbf.DBFReader;
import geography.FeatureCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is really an integration test that mimics the call site of `ShapeFileReaderFactory`
 * and makes sure that calling it in an implementation-agnostic fashion works.
 */
class ShapeFileReaderFactoryTest {

    private ShapeFileReaderFactory systemUnderTest;
    private File sampleShpFile;
    private File sampleDbfFile;

    @BeforeEach
    void setUp() {
        // TODO: replace with the eventual factory class that replaces the Nocrala shapefile reader implementation
        systemUnderTest = new NocralaShapeFileReaderFactory();
        File sampleDataDirectory = new File("data/sample/Portland_City_Council_Districts");
        String shapeFileRootName = "Portland_City_Council_Districts";
        sampleShpFile = new File(sampleDataDirectory, shapeFileRootName + ".shp");
        sampleDbfFile = new File(sampleDataDirectory, shapeFileRootName + ".dbf");
    }

    @Test
    void createShapeFileReader() throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(sampleShpFile)) {

            // make sure we can read the file in without anything blowing up
            ShapeFileReader shapeFileReader = systemUnderTest.createShapeFileReader(fileInputStream);

            // test the reading of the header shape type
            assertThat(shapeFileReader.getHeaderShapeType()).isEqualTo("POLYGON");

            // make sure we can read the columns and data in the file
            DBFReader dbfreader = new DBFReader(new FileInputStream(sampleDbfFile));
            String[] cols = new String[dbfreader.getFieldCount()];
            IntStream.range(0, cols.length).forEach(i -> {
                cols[i] = dbfreader.getField(i).name;
            });
            int total = shapeFileReader.processShapeFile(cols, dbfreader, new FeatureCollection());
            assertThat(total).isEqualTo(4);

        } catch (IOException ioe) {
            fail(ioe.getMessage());
            throw ioe;
        } catch (Exception e) {
            fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}