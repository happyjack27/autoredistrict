package ui;

import com.google.common.truth.*;
import org.assertj.swing.core.BasicComponentFinder;
import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;

import static com.google.common.truth.Truth.assertThat;

class MainFrameTest {

    @BeforeAll
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

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
    }
}