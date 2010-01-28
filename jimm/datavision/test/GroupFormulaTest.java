package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.layout.CharSepLE;
import jimm.datavision.source.charsep.CharSepSource;
import java.io.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import org.xml.sax.SAXException;

/**
 * Tests formula evals when formulas are hidden or appear multiple
 * times.
 * <p>
 * These tests are tightly coupled with the contents of the
 * <code>group_formulas_*</code> files.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class GroupFormulaTest extends TestCase {

protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_grp_form_test_out.txt");
protected static final File GROUP_EVAL_REPORT =
    new File(AllTests.testDataFile("group_formulas.xml"));
protected static final String GROUP_EVAL_DATA_FILE =
    AllTests.testDataFile("group_formulas_data.csv");
protected static final String GROUP_EVAL_EXPECTED_FILE =
    AllTests.testDataFile("group_formulas_expected.csv");

protected Report report;
protected CharSepSource dataSource;

public static Test suite() {
    return new TestSuite(GroupFormulaTest.class);
}

public GroupFormulaTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();

    OUT_FILE.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
    report.setLayoutEngine(new CharSepLE(out, ','));

    report.read(GROUP_EVAL_REPORT); // Must come after setting password

    dataSource = (CharSepSource)report.getDataSource();
    dataSource.setSepChar(',');
    dataSource.setInput(GROUP_EVAL_DATA_FILE);
}

public void tearDown() {
    if (OUT_FILE.exists())
	OUT_FILE.delete();
}

public void testGroupHeaderFormula()
    throws IOException, FileNotFoundException, SAXException
{
    // Run report in this thread, not a separate one. Running the
    // report closes the output stream.
    report.runReport();

    // Open the output and the expected output and compare them.
    BufferedReader out = new BufferedReader(new FileReader(OUT_FILE));
    BufferedReader expected =
	new BufferedReader(new FileReader(GROUP_EVAL_EXPECTED_FILE));

    String outLine;
    while ((outLine = out.readLine()) != null) {
	String expectedLine = expected.readLine();
	if (expectedLine == null)
	    fail("Too much data in output");
	assertEquals(expectedLine, outLine);
    }

    // Make sure we are at the end of the expected file
    assertNull(expected.readLine());

    expected.close();
    out.close();
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
