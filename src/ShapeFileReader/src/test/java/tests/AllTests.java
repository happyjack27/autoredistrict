package tests;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

  public static void main(final String[] args) {
    try {
      TestSuite ts = new TestSuite();

      ts.addTestSuite(IntTests.class);
      ts.addTestSuite(DoubleTests.class);
      ts.addTestSuite(HexaTests.class);
      ts.addTestSuite(ReaderTests.class);

      TestResult tr = junit.textui.TestRunner.run(ts);
      if (!tr.wasSuccessful()) {
        System.exit(1);
      }

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
