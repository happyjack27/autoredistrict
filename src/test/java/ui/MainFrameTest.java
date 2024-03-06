package ui;

import org.assertj.swing.core.BasicComponentFinder;
import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JFileChooserFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JFileChooserFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.File;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class MainFrameTest {

    FrameFixture frame;

    @BeforeEach
    protected void setUp() {
        MainFrame mainFrame = GuiActionRunner.execute(MainFrame::new);
        frame = new FrameFixture(mainFrame);
        frame.show();
    }

    @AfterEach
    public void tearDown() {
        frame.cleanUp();
    }

    @Test
    void applicationLaunchesSuccessfully() {
        frame.requireTitle("Automatic Redistricter");

        // verify expected menus are displayed in main application window

        ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

        JMenuBar menuBar = finder.findByType(JMenuBar.class);

        JMenu fileMenu = (JMenu) menuBar.getComponent(0);
        assertThat(fileMenu.getText()).isEqualTo("File");

        JMenu mergeMenu = (JMenu) menuBar.getComponent(1);
        assertThat(mergeMenu.getText()).isEqualTo("Merge");

        JMenu communitiesOfInterestMenu = (JMenu) menuBar.getComponent(2);
        assertThat(communitiesOfInterestMenu.getText()).isEqualTo("Communities of interest");

        JMenu mapMenu = (JMenu) menuBar.getComponent(3);
        assertThat(mapMenu.getText()).isEqualTo("Map");

        JMenu windowsMenu = (JMenu) menuBar.getComponent(4);
        assertThat(windowsMenu.getText()).isEqualTo("Windows");

        // assert that the app can successfully open an ESRI shapefile without throwing any exceptions
        try {
            JMenuItem menuItem = fileMenu.getItem(4);

            JMenuItemFixture openEsriShapefileMenuItem = new JMenuItemFixture(frame.robot(), menuItem);
            openEsriShapefileMenuItem.click();

            JFileChooserFixture fileChooserFixture = JFileChooserFinder.findFileChooser().withTimeout(1000).using(frame.robot());
            File currentWorkingDirectory = new File(System.getProperty("user.dir"));
            File sampleDataDirectory = new File(currentWorkingDirectory, "data/sample/delaware");
            fileChooserFixture.setCurrentDirectory(sampleDataDirectory);
            fileChooserFixture.selectFile(new File(sampleDataDirectory, "delaware.shp"));
            fileChooserFixture.approve();

        } catch (Exception e) {
            fail(e);
        }
    }
}