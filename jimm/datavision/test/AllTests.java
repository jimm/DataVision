package jimm.datavision.test;
import jimm.util.Getopts;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class AllTests extends TestCase {

static final String DATA_FILE_DIR = "jimm/datavision/test/data";

/**
 * Returns a relative file path to <var>fileName</var>. Assumes the app is
 * being run from the top-level DataVision directory.
 *
 * @param fileName the name of a file in the DATA_FILE_DIR directory
 * @return a relative file path to <var>fileName</var>
 */
static String testDataFile(String fileName) {
    return DATA_FILE_DIR + '/' + fileName;
}

public static Test suite(boolean runJdbcTests, boolean skipNonJdbcTests) {
    TestSuite suite = new TestSuite();
    if (!skipNonJdbcTests) {
	suite.addTest(StringUtilsTest.suite());
	suite.addTest(ColumnIteratorTest.suite());
	suite.addTest(DelimParserTest.suite());
	suite.addTest(XMLWriterTest.suite());
	suite.addTest(FormulaTest.suite());
	suite.addTest(FormulaEvalTest.suite());
	suite.addTest(SectionAreaTest.suite());
	suite.addTest(SuppressionProcTest.suite());
	suite.addTest(GroupFormulaTest.suite());
	suite.addTest(GetoptsTest.suite());
	suite.addTest(ParserHelperTest.suite());
	suite.addTest(PDFLETest.suite());
	suite.addTest(ReportTest.suite());
	suite.addTest(ReportRunTest.suite());
	suite.addTest(ScriptingTest.suite());
	suite.addTest(CharSepTest.suite());
	suite.addTest(AggregateTest.suite());
    }
    if (runJdbcTests) {
	suite.addTest(SubreportRunTest.suite());
	suite.addTest(ConnectionTest.suite());
	suite.addTest(QueryTest.suite());
    }
    return suite;
}

public AllTests(String name) {
    super(name);
}

public void testDummy() {
    assertTrue(true);
}

public static void main(String[] args) {
    Getopts g = new Getopts("gjJ", args);
    if (g.error()) {
	System.err.println("usage: AllTests [-g] [-j] [-J]");
	System.err.println("  -g    Use GUI test runner (ignores -j and -J flags)");
	System.err.println("  -j    Run tests that rely upon JDBC and the database");
	System.err.println("  -J    Skip non-JDBC tests");
	System.exit(0);
    }

    if (g.hasOption('g'))
	junit.swingui.TestRunner.run(AllTests.class);
    else {
	junit.textui.TestRunner.run(suite(g.hasOption('j'), g.hasOption('J')));
	System.exit(0);		// For some reason, need this under OS X 10.3
    }
}

}
