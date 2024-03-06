package ui;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void applicationDisplaysExpectedTitle() {
        frame.requireTitle("Automatic Redistricter");
    }
}